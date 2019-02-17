/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.support;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Delegate for resolving constructors and factory methods.
 * Performs constructor resolution through argument matching.
 *
 * <p>
 *     代表解决构造函数和工厂方法。 通过参数匹配执行构造函数解析。
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @since 2.0
 * @see #autowireConstructor
 * @see #instantiateUsingFactoryMethod
 * @see AbstractAutowireCapableBeanFactory
 */
class ConstructorResolver {

	private static final NamedThreadLocal<InjectionPoint> currentInjectionPoint =
			new NamedThreadLocal<InjectionPoint>("Current injection point");

	private final AbstractAutowireCapableBeanFactory beanFactory;


	/**
	 * Create a new ConstructorResolver for the given factory and instantiation strategy.
	 * @param beanFactory the BeanFactory to work with
	 */
	public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/**
	 * "autowire constructor" (with constructor arguments by type) behavior.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based
	 * dependency resolution.
	 * <p>
	 *     “autowire constructor”（带有类型的构造函数参数）行为。 如果指定了显式构造函数参数值，则还应用，将所有剩余参数与bean工厂中的bean匹配。
	 * </p>
	 * <p>
	 *    这对应于构造函数注入：在此模式下，Spring bean工厂能够托管期望基于构造函数的依赖项解析的组件
	 * </p>
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param chosenCtors chosen candidate constructors (or {@code null} if none)
	 * @param explicitArgs argument values passed in programmatically via the getBean method,
	 * or {@code null} if none (-> use constructor argument values from bean definition)
	 * @return a BeanWrapper for the new instance
	 */
	public BeanWrapper autowireConstructor(final String beanName, final RootBeanDefinition mbd,
			Constructor<?>[] chosenCtors, final Object[] explicitArgs) {

		//构建包装器
		BeanWrapperImpl bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);


		Constructor<?> constructorToUse = null;
		ArgumentsHolder argsHolderToUse = null;
		Object[] argsToUse = null;

		//存在显式指定的参数，让我们使用它来构造
		if (explicitArgs != null) {
			argsToUse = explicitArgs;
		}
		else {
			Object[] argsToResolve = null;
			synchronized (mbd.constructorArgumentLock) {
				constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
				if (constructorToUse != null && mbd.constructorArgumentsResolved) {
					// Found a cached constructor...
					argsToUse = mbd.resolvedConstructorArguments;
					if (argsToUse == null) {
						argsToResolve = mbd.preparedConstructorArguments;
					}
				}
			}
			if (argsToResolve != null) {
				argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
			}
		}

		//如果使用的构造参数为null，也就是还没有缓存，只能进行解析
		if (constructorToUse == null) {
			// Need to resolve the constructor.
			// 需要去解析构造函数
			boolean autowiring = (chosenCtors != null ||
					mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
			ConstructorArgumentValues resolvedValues = null;

			//最小的参数
			int minNrOfArgs;
			//显示指定了的使用显式说明的
			if (explicitArgs != null) {
				minNrOfArgs = explicitArgs.length;
			}
			else {
				//获得beanDefinition中的信息
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				resolvedValues = new ConstructorArgumentValues();
				//解析构造函数的参数信息
				minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}

			// Take specified constructors, if any.
			//可选的构造函数的信息
			Constructor<?>[] candidates = chosenCtors;
			if (candidates == null) {//没有指定相应的构造函数
				Class<?> beanClass = mbd.getBeanClass();
				try {
					//获得相关的构造函数的信息
					candidates = (mbd.isNonPublicAccessAllowed() ?
							beanClass.getDeclaredConstructors() : beanClass.getConstructors());
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Resolution of declared constructors on bean Class [" + beanClass.getName() +
							"] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
				}
			}
			//排序相应的构造函数的
			AutowireUtils.sortConstructors(candidates);
			int minTypeDiffWeight = Integer.MAX_VALUE;
			Set<Constructor<?>> ambiguousConstructors = null;
			LinkedList<UnsatisfiedDependencyException> causes = null;

			//遍历相关的构造函数
			for (Constructor<?> candidate : candidates) {
				//获得构造函数的参数类型数组
				Class<?>[] paramTypes = candidate.getParameterTypes();


				if (constructorToUse != null && argsToUse.length > paramTypes.length) {
					// Already found greedy constructor that can be satisfied ->
					// do not look any further, there are only less greedy constructors left.
					break;
				}
				//参数小于构造函数的
				if (paramTypes.length < minNrOfArgs) {
					continue;
				}

				//参数holder
				ArgumentsHolder argsHolder;
				if (resolvedValues != null) {
					try {
						//计算相关的名字
						String[] paramNames = ConstructorPropertiesChecker.evaluate(candidate, paramTypes.length);
						if (paramNames == null) {
							ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
							if (pnd != null) {
								paramNames = pnd.getParameterNames(candidate);
							}
						}
						//构建相关的参数数组
						argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames,
								getUserDeclaredConstructor(candidate), autowiring);
					}
					catch (UnsatisfiedDependencyException ex) {
						if (this.beanFactory.logger.isTraceEnabled()) {
							this.beanFactory.logger.trace(
									"Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
						}
						// Swallow and try next constructor.
						if (causes == null) {
							causes = new LinkedList<UnsatisfiedDependencyException>();
						}
						causes.add(ex);
						continue;
					}
				}
				else {
					// Explicit arguments given -> arguments length must match exactly.
					if (paramTypes.length != explicitArgs.length) {
						continue;
					}
					argsHolder = new ArgumentsHolder(explicitArgs);
				}

				int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
						argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
				// Choose this constructor if it represents the closest match.
				if (typeDiffWeight < minTypeDiffWeight) {
					constructorToUse = candidate;
					argsHolderToUse = argsHolder;
					argsToUse = argsHolder.arguments;
					minTypeDiffWeight = typeDiffWeight;
					ambiguousConstructors = null;
				}
				else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
					if (ambiguousConstructors == null) {
						ambiguousConstructors = new LinkedHashSet<Constructor<?>>();
						ambiguousConstructors.add(constructorToUse);
					}
					ambiguousConstructors.add(candidate);
				}
			}

			//到最后还是无法确定使用的构造函数，那么我们来收集这些错误的信息
			if (constructorToUse == null) {
				if (causes != null) {
					UnsatisfiedDependencyException ex = causes.removeLast();
					for (Exception cause : causes) {
						this.beanFactory.onSuppressedException(cause);
					}
					throw ex;
				}
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Could not resolve matching constructor " +
						"(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities)");
			}
			//虽然确定了构造函数，但是这个是模糊的,那么我们还是需要扔出相关的错误的。
			else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Ambiguous constructor matches found in bean '" + beanName + "' " +
						"(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
						ambiguousConstructors);
			}

			//没有显式指定我们使用的构造函数，那么我们就把他缓存起来
			if (explicitArgs == null) {
				argsHolderToUse.storeCache(mbd, constructorToUse);
			}
		}

		try {
			Object beanInstance;

			if (System.getSecurityManager() != null) {
				final Constructor<?> ctorToUse = constructorToUse;
				final Object[] argumentsToUse = argsToUse;
				beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						return beanFactory.getInstantiationStrategy().instantiate(
								mbd, beanName, beanFactory, ctorToUse, argumentsToUse);
					}
				}, beanFactory.getAccessControlContext());
			}
			else {
				beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(
						mbd, beanName, this.beanFactory, constructorToUse, argsToUse);
			}

			//设置bean实例
			bw.setBeanInstance(beanInstance);
			//返回包装类型
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean instantiation via constructor failed", ex);
		}
	}

	/**
	 * Resolve the factory method in the specified bean definition, if possible.
	 * {@link RootBeanDefinition#getResolvedFactoryMethod()} can be checked for the result.
	 *
	 * <p>
	 *     如果可能，解析指定bean定义中的工厂方法。 可以检查{@link RootBeanDefinition#getResolvedFactoryMethod()}的结果。
	 * </p>
	 * @param mbd the bean definition to check
	 */
	public void resolveFactoryMethodIfPossible(RootBeanDefinition mbd) {
		Class<?> factoryClass;
		boolean isStatic;
		if (mbd.getFactoryBeanName() != null) {
			factoryClass = this.beanFactory.getType(mbd.getFactoryBeanName());
			isStatic = false;
		}
		else {
			factoryClass = mbd.getBeanClass();
			isStatic = true;
		}
		factoryClass = ClassUtils.getUserClass(factoryClass);

		Method[] candidates = getCandidateMethods(factoryClass, mbd);
		Method uniqueCandidate = null;
		for (Method candidate : candidates) {
			if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
				if (uniqueCandidate == null) {
					uniqueCandidate = candidate;
				}
				else if (!Arrays.equals(uniqueCandidate.getParameterTypes(), candidate.getParameterTypes())) {
					uniqueCandidate = null;
					break;
				}
			}
		}
		synchronized (mbd.constructorArgumentLock) {
			mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
		}
	}

	/**
	 * Retrieve all candidate methods for the given class, considering
	 * the {@link RootBeanDefinition#isNonPublicAccessAllowed()} flag.
	 * Called as the starting point for factory method determination.
	 */
	private Method[] getCandidateMethods(final Class<?> factoryClass, final RootBeanDefinition mbd) {
		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
				@Override
				public Method[] run() {
					return (mbd.isNonPublicAccessAllowed() ?
							ReflectionUtils.getAllDeclaredMethods(factoryClass) : factoryClass.getMethods());
				}
			});
		}
		else {
			return (mbd.isNonPublicAccessAllowed() ?
					ReflectionUtils.getAllDeclaredMethods(factoryClass) : factoryClass.getMethods());
		}
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the
	 * bean definition parameter specifies a class, rather than a "factory-bean", or
	 * an instance variable on a factory object itself configured using Dependency Injection.
	 * <p>Implementation requires iterating over the static or instance methods with the
	 * name specified in the RootBeanDefinition (the method may be overloaded) and trying
	 * to match with the parameters. We don't have the types attached to constructor args,
	 * so trial and error is the only way to go here. The explicitArgs array may contain
	 * argument values passed in programmatically via the corresponding getBean method.
	 *
	 * <p>
	 *     使用命名的工厂方法实例化bean。 如果bean定义参数指定一个类而不是“factory-bean”，或者工厂对象本身使用依赖注入配置的实例变量，则该方法可以是静态的。
	 * </p>
	 * <p>
	 *     	实现需要使用RootBeanDefinition中指定的名称迭代静态或实例方法（方法可能会重载）并尝试与参数匹配。
	 *     	我们没有附加到构造函数args的类型，因此试验和错误是唯一的方法。
	 *     	explicitArgs数组可以包含通过相应的getBean方法以编程方式传递的参数值。
	 * </p>
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param explicitArgs argument values passed in programmatically via the getBean
	 * method, or {@code null} if none (-> use constructor argument values from bean definition)
	 * @return a BeanWrapper for the new instance
	 */
	public BeanWrapper instantiateUsingFactoryMethod(
			final String beanName, final RootBeanDefinition mbd, final Object[] explicitArgs) {

		//构建包装类
		BeanWrapperImpl bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);

		Object factoryBean;
		Class<?> factoryClass;
		boolean isStatic;

		//获得beanDefinition的factoryBean，这不同于的FactoryBean
		String factoryBeanName = mbd.getFactoryBeanName();
		if (factoryBeanName != null) {
			if (factoryBeanName.equals(beanName)) {
				throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
						"factory-bean reference points back to the same bean definition");
			}
			factoryBean = this.beanFactory.getBean(factoryBeanName);
			if (factoryBean == null) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"factory-bean '" + factoryBeanName + "' (or a BeanPostProcessor involved) returned null");
			}
			if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
				throw new IllegalStateException("About-to-be-created singleton instance implicitly appeared " +
						"through the creation of the factory bean that its bean definition points to");
			}
			factoryClass = factoryBean.getClass();
			isStatic = false;
		}
		else {
			// It's a static factory method on the bean class.
			// 是一个竟态方法充当factoryMethod来执行的
			if (!mbd.hasBeanClass()) {
				throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
						"bean definition declares neither a bean class nor a factory-bean reference");
			}
			factoryBean = null;
			factoryClass = mbd.getBeanClass();
			isStatic = true;
		}

		Method factoryMethodToUse = null;
		ArgumentsHolder argsHolderToUse = null;
		Object[] argsToUse = null;

		//如果指定了构造函数的参数
		if (explicitArgs != null) {
			argsToUse = explicitArgs;
		}
		else {
			//没有尝试去推导
			Object[] argsToResolve = null;
			synchronized (mbd.constructorArgumentLock) {
				factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
				if (factoryMethodToUse != null && mbd.constructorArgumentsResolved) {
					// Found a cached factory method...
					argsToUse = mbd.resolvedConstructorArguments;
					if (argsToUse == null) {
						argsToResolve = mbd.preparedConstructorArguments;
					}
				}
			}
			//缓存存在,但是需要待解析，因为不完善，尝试去解析
			if (argsToResolve != null) {
				argsToUse = resolvePreparedArguments(beanName, mbd, bw, factoryMethodToUse, argsToResolve);
			}
		}

		//通常的第一次,
		if (factoryMethodToUse == null || argsToUse == null) {
			// Need to determine the factory method...
			// Try all methods with this name to see if they match the given arguments.
			// 需要确定工厂方法......
			// 尝试使用此名称的所有方法来查看它们是否与给定的参数匹配。
			factoryClass = ClassUtils.getUserClass(factoryClass);

			//获得候选的方法
			Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
			List<Method> candidateSet = new ArrayList<Method>();
			for (Method candidate : rawCandidates) {
				//名字相同,并且是同一类修饰符的
				if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
					candidateSet.add(candidate);
				}
			}
			//符合的候选者
			Method[] candidates = candidateSet.toArray(new Method[candidateSet.size()]);
			//进行简单的排序
			AutowireUtils.sortFactoryMethods(candidates);

			//构造函数进行解析
			ConstructorArgumentValues resolvedValues = null;
			//构造模式注入
			boolean autowiring = (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
			int minTypeDiffWeight = Integer.MAX_VALUE;
			//模糊的的工厂反复
			Set<Method> ambiguousFactoryMethods = null;

			//最少的的参数
			int minNrOfArgs;
			//显示指定，最少的参数就是显式指定的长度
			if (explicitArgs != null) {
				minNrOfArgs = explicitArgs.length;
			}
			else {
				// We don't have arguments passed in programmatically, so we need to resolve the
				// arguments specified in the constructor arguments held in the bean definition.
				//我们没有以编程方式传递的参数，因此我们需要解析bean定义中保存的构造函数参数中指定的参数。
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				resolvedValues = new ConstructorArgumentValues();
				//进行解析beanDefinition中的指定的构造函数，来推导出最少的需要的构造函数的参数个数
				minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}

			LinkedList<UnsatisfiedDependencyException> causes = null;

			//处理候选的方法
			for (Method candidate : candidates) {
				//获得方法参数类型
				Class<?>[] paramTypes = candidate.getParameterTypes();

				//方法参数类型大于最小的数量这是筛选的最小条件
				if (paramTypes.length >= minNrOfArgs) {
					ArgumentsHolder argsHolder;

					if (resolvedValues != null) {
						// Resolved constructor arguments: type conversion and/or autowiring necessary.
						// 解析构造函数的参数
						try {
							String[] paramNames = null;
							ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
							if (pnd != null) {
								paramNames = pnd.getParameterNames(candidate);
							}
							argsHolder = createArgumentArray(
									beanName, mbd, resolvedValues, bw, paramTypes, paramNames, candidate, autowiring);
						}
						catch (UnsatisfiedDependencyException ex) {
							//出现问题，进行抛出异常
							if (this.beanFactory.logger.isTraceEnabled()) {
								this.beanFactory.logger.trace("Ignoring factory method [" + candidate +
										"] of bean '" + beanName + "': " + ex);
							}
							// Swallow and try next overloaded factory method.
							if (causes == null) {
								causes = new LinkedList<UnsatisfiedDependencyException>();
							}
							causes.add(ex);
							continue;
						}
					}

					else {
						// Explicit arguments given -> arguments length must match exactly.
						if (paramTypes.length != explicitArgs.length) {
							continue;
						}
						argsHolder = new ArgumentsHolder(explicitArgs);
					}

					//类型权重处理，用于筛选最为确切的方法
					int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
							argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
					// Choose this factory method if it represents the closest match.
					// 如果它代表最接近的匹配，请选择此工厂方法。
					if (typeDiffWeight < minTypeDiffWeight) {
						//使用的方法
						factoryMethodToUse = candidate;
						//使用的参数包装
						argsHolderToUse = argsHolder;
						//使用的参数
						argsToUse = argsHolder.arguments;
						//权重
						minTypeDiffWeight = typeDiffWeight;
						ambiguousFactoryMethods = null;
					}
					// Find out about ambiguity: In case of the same type difference weight
					// for methods with the same number of parameters, collect such candidates
					// and eventually raise an ambiguity exception.
					// However, only perform that check in non-lenient constructor resolution mode,
					// and explicitly ignore overridden methods (with the same parameter signature).
					// 了解歧义：对于具有相同参数数量的方法，如果类型差异权重相同，则收集此类候选项并最终引发歧义异常。
					// 但是，仅在非宽松构造函数解析模式下执行该检查，并显式忽略重写方法（具有相同的参数签名）。
					else if (factoryMethodToUse != null && typeDiffWeight == minTypeDiffWeight &&
							!mbd.isLenientConstructorResolution() &&
							paramTypes.length == factoryMethodToUse.getParameterTypes().length &&
							!Arrays.equals(paramTypes, factoryMethodToUse.getParameterTypes())) {
						if (ambiguousFactoryMethods == null) {
							ambiguousFactoryMethods = new LinkedHashSet<Method>();
							ambiguousFactoryMethods.add(factoryMethodToUse);
						}
						ambiguousFactoryMethods.add(candidate);
					}
				}
			}

			//出现问题，我们将收集相关的信息，进行报错
			if (factoryMethodToUse == null) {
				if (causes != null) {
					UnsatisfiedDependencyException ex = causes.removeLast();
					for (Exception cause : causes) {
						this.beanFactory.onSuppressedException(cause);
					}
					throw ex;
				}
				List<String> argTypes = new ArrayList<String>(minNrOfArgs);
				if (explicitArgs != null) {
					for (Object arg : explicitArgs) {
						argTypes.add(arg != null ? arg.getClass().getSimpleName() : "null");
					}
				}
				else {
					Set<ValueHolder> valueHolders = new LinkedHashSet<ValueHolder>(resolvedValues.getArgumentCount());
					valueHolders.addAll(resolvedValues.getIndexedArgumentValues().values());
					valueHolders.addAll(resolvedValues.getGenericArgumentValues());
					for (ValueHolder value : valueHolders) {
						String argType = (value.getType() != null ? ClassUtils.getShortName(value.getType()) :
								(value.getValue() != null ? value.getValue().getClass().getSimpleName() : "null"));
						argTypes.add(argType);
					}
				}
				String argDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"No matching factory method found: " +
						(mbd.getFactoryBeanName() != null ?
							"factory bean '" + mbd.getFactoryBeanName() + "'; " : "") +
						"factory method '" + mbd.getFactoryMethodName() + "(" + argDesc + ")'. " +
						"Check that a method with the specified name " +
						(minNrOfArgs > 0 ? "and arguments " : "") +
						"exists and that it is " +
						(isStatic ? "static" : "non-static") + ".");
			}
			else if (void.class == factoryMethodToUse.getReturnType()) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Invalid factory method '" + mbd.getFactoryMethodName() +
						"': needs to have a non-void return type!");
			}
			else if (ambiguousFactoryMethods != null) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Ambiguous factory method matches found in bean '" + beanName + "' " +
						"(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
						ambiguousFactoryMethods);
			}

			//进行缓存操作
			if (explicitArgs == null && argsHolderToUse != null) {
				argsHolderToUse.storeCache(mbd, factoryMethodToUse);
			}
		}

		//已经使用FactoryMethod和相关的参数，进行构造
		try {
			Object beanInstance;

			if (System.getSecurityManager() != null) {
				final Object fb = factoryBean;
				final Method factoryMethod = factoryMethodToUse;
				final Object[] args = argsToUse;
				beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						return beanFactory.getInstantiationStrategy().instantiate(
								mbd, beanName, beanFactory, fb, factoryMethod, args);
					}
				}, beanFactory.getAccessControlContext());
			}
			else {
				beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(
						mbd, beanName, this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
			}

			if (beanInstance == null) {
				return null;
			}
			bw.setBeanInstance(beanInstance);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean instantiation via factory method failed", ex);
		}
	}

	/**
	 * Resolve the constructor arguments for this bean into the resolvedValues object.
	 * This may involve looking up other beans.
	 * <p>This method is also used for handling invocations of static factory methods.
	 * <p>
	 *     将此bean的构造函数参数解析为resolvedValues对象。 这可能涉及查找其他bean。
	 * </p>
	 * <p>
	 *     此方法还用于处理静态工厂方法的调用。
	 * </p>
	 */
	private int resolveConstructorArguments(String beanName, RootBeanDefinition mbd, BeanWrapper bw,
			ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {


		TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
		TypeConverter converter = (customConverter != null ? customConverter : bw);
		BeanDefinitionValueResolver valueResolver =
				new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);

		//一定存在，获得参数个数
		int minNrOfArgs = cargs.getArgumentCount();

		//参数的位置和值
		for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : cargs.getIndexedArgumentValues().entrySet()) {
			int index = entry.getKey();
			if (index < 0) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Invalid constructor argument index: " + index);
			}
			//位置都大于个数了，说明某些位置没有设置，可能没有配置，但是至少这个构造函数的个数需要提升
			if (index > minNrOfArgs) {
				minNrOfArgs = index + 1;
			}
			ConstructorArgumentValues.ValueHolder valueHolder = entry.getValue();
			//如果里面的值已经被解析了，我们设置进去
			if (valueHolder.isConverted()) {
				resolvedValues.addIndexedArgumentValue(index, valueHolder);
			}
			else {
				//没有的话，我们尝试解析后进行包装。
				Object resolvedValue =
						valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder =
						new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
				resolvedValueHolder.setSource(valueHolder); //设置来源
				resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
			}
		}

		//同样进行相关的操作
		for (ConstructorArgumentValues.ValueHolder valueHolder : cargs.getGenericArgumentValues()) {
			if (valueHolder.isConverted()) {
				resolvedValues.addGenericArgumentValue(valueHolder);
			}
			else {
				Object resolvedValue =
						valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder =
						new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addGenericArgumentValue(resolvedValueHolder);
			}
		}

		return minNrOfArgs;
	}

	/**
	 * Create an array of arguments to invoke a constructor or factory method,
	 * given the resolved constructor argument values.
	 * <p>
	 *     在给定已解析的构造函数参数值的情况下，创建一个参数数组以调用构造函数或工厂方法。
	 * </p>
	 */
	private ArgumentsHolder createArgumentArray(
			String beanName, RootBeanDefinition mbd, ConstructorArgumentValues resolvedValues,
			BeanWrapper bw, Class<?>[] paramTypes, String[] paramNames, Object methodOrCtor,
			boolean autowiring) throws UnsatisfiedDependencyException {

		//构建相关的类型转换器
		TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
		TypeConverter converter = (customConverter != null ? customConverter : bw);

		//构建参数持有实例
		ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
		//已经被使用的“构造器”的参数
		Set<ConstructorArgumentValues.ValueHolder> usedValueHolders =
				new HashSet<ConstructorArgumentValues.ValueHolder>(paramTypes.length);
		Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);

		//变量参数
		for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
			//类型参数
			Class<?> paramType = paramTypes[paramIndex];
			//参数名
			String paramName = (paramNames != null ? paramNames[paramIndex] : "");
			// Try to find matching constructor argument value, either indexed or generic.
			// 尝试查找匹配的构造函数参数值，索引或泛型。
			ConstructorArgumentValues.ValueHolder valueHolder =
					resolvedValues.getArgumentValue(paramIndex, paramType, paramName, usedValueHolders);
			// If we couldn't find a direct match and are not supposed to autowire,
			// let's try the next generic, untyped argument value as fallback:
			// it could match after type conversion (for example, String -> int).
			// 如果我们找不到直接匹配并且不应该自动装配，
			// 让我们尝试下一个通用的无类型参数值作为后备：
			// 它可以在类型转换后匹配（例如，String  - > int）。
			if (valueHolder == null && (!autowiring || paramTypes.length == resolvedValues.getArgumentCount())) {
				valueHolder = resolvedValues.getGenericArgumentValue(null, null, usedValueHolders);
			}
			if (valueHolder != null) {
				// We found a potential match - let's give it a try.
				// Do not consider the same value definition multiple times!
				// 我们找到了一个潜在的匹配 - 让我们试一试。 不要多次考虑相同的值定义！
				usedValueHolders.add(valueHolder);
				//原始值
				Object originalValue = valueHolder.getValue();
				//存储转换至
				Object convertedValue;
				if (valueHolder.isConverted()) {
					//已经被转换了，加入到prepared中
					convertedValue = valueHolder.getConvertedValue();
					args.preparedArguments[paramIndex] = convertedValue;
				}
				else {
					//没有被转换，进行转换
					ConstructorArgumentValues.ValueHolder sourceHolder =
							(ConstructorArgumentValues.ValueHolder) valueHolder.getSource();
					//原来的value
					Object sourceValue = sourceHolder.getValue();
					MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, paramIndex);
					try {
						//转换后的value
						convertedValue = converter.convertIfNecessary(originalValue, paramType, methodParam);
						// TODO re-enable once race condition has been found (SPR-7423)
						/*
						if (originalValue == sourceValue || sourceValue instanceof TypedStringValue) {
							// Either a converted value or still the original one: store converted value.
							sourceHolder.setConvertedValue(convertedValue);
							args.preparedArguments[paramIndex] = convertedValue;
						}
						else {
						*/
							//是否还需要解析
							args.resolveNecessary = true;
							//也是prepared，是sourceValue
							args.preparedArguments[paramIndex] = sourceValue;
						// }
					}
					catch (TypeMismatchException ex) {
						throw new UnsatisfiedDependencyException(
								mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam),
								"Could not convert argument value of type [" +
								ObjectUtils.nullSafeClassName(valueHolder.getValue()) +
								"] to required type [" + paramType.getName() + "]: " + ex.getMessage());
					}
				}
				//被转换的参数
				args.arguments[paramIndex] = convertedValue;
				//原始的参数
				args.rawArguments[paramIndex] = originalValue;
			}
			else {
				//不存在使用
				MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, paramIndex);
				// No explicit match found: we're either supposed to autowire or
				// have to fail creating an argument array for the given constructor.
				// 找不到明确的匹配：我们要么应该自动装配，要么必须为给定的构造函数创建一个参数数组时失败。
				if (!autowiring) {
					throw new UnsatisfiedDependencyException(
							mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam),
							"Ambiguous argument values for parameter of type [" + paramType.getName() +
							"] - did you specify the correct bean references as arguments?");
				}
				try {
					Object autowiredArgument =
							resolveAutowiredArgument(methodParam, beanName, autowiredBeanNames, converter);
					args.rawArguments[paramIndex] = autowiredArgument;
					args.arguments[paramIndex] = autowiredArgument;
					args.preparedArguments[paramIndex] = new AutowiredArgumentMarker();
					args.resolveNecessary = true;
				}
				catch (BeansException ex) {
					throw new UnsatisfiedDependencyException(
							mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam), ex);
				}
			}
		}

		for (String autowiredBeanName : autowiredBeanNames) {
			this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
			if (this.beanFactory.logger.isDebugEnabled()) {
				this.beanFactory.logger.debug("Autowiring by type from bean name '" + beanName +
						"' via " + (methodOrCtor instanceof Constructor ? "constructor" : "factory method") +
						" to bean named '" + autowiredBeanName + "'");
			}
		}

		return args;
	}

	/**
	 * Resolve the prepared arguments stored in the given bean definition.
	 * <p>解析已经准备好的bean定义中的参数，即存在相关的依赖</p>
	 */
	private Object[] resolvePreparedArguments(
			String beanName, RootBeanDefinition mbd, BeanWrapper bw, Member methodOrCtor, Object[] argsToResolve) {

		//自定义的类型转换器
		TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
		//转换器
		TypeConverter converter = (customConverter != null ? customConverter : bw);
		//值解析器
		BeanDefinitionValueResolver valueResolver =
				new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
		//方法的入参的类型数组
		Class<?>[] paramTypes = (methodOrCtor instanceof Method ?
				((Method) methodOrCtor).getParameterTypes() : ((Constructor<?>) methodOrCtor).getParameterTypes());

		//承载被解析的参数的结果
		Object[] resolvedArgs = new Object[argsToResolve.length];
		//一个个来
		for (int argIndex = 0; argIndex < argsToResolve.length; argIndex++) {
			Object argValue = argsToResolve[argIndex];
			//找的参数包装类型methodparameter
			MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, argIndex);
			//解析一下参数的类型
			GenericTypeResolver.resolveParameterType(methodParam, methodOrCtor.getDeclaringClass());
			//特殊的类型之一
			if (argValue instanceof AutowiredArgumentMarker) {
				argValue = resolveAutowiredArgument(methodParam, beanName, null, converter);
			}
			//特殊的类型之一
			else if (argValue instanceof BeanMetadataElement) {
				argValue = valueResolver.resolveValueIfNecessary("constructor argument", argValue);
			}
			//最为简单的参数类型
			else if (argValue instanceof String) {
				argValue = this.beanFactory.evaluateBeanDefinitionString((String) argValue, mbd);
			}
			Class<?> paramType = paramTypes[argIndex];
			try {
				//使用转换器进行尝试转换
				resolvedArgs[argIndex] = converter.convertIfNecessary(argValue, paramType, methodParam);
			}
			catch (TypeMismatchException ex) {
				throw new UnsatisfiedDependencyException(
						mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam),
						"Could not convert argument value of type [" + ObjectUtils.nullSafeClassName(argValue) +
						"] to required type [" + paramType.getName() + "]: " + ex.getMessage());
			}
		}
		return resolvedArgs;
	}

	protected Constructor<?> getUserDeclaredConstructor(Constructor<?> constructor) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		Class<?> userClass = ClassUtils.getUserClass(declaringClass);
		if (userClass != declaringClass) {
			try {
				return userClass.getDeclaredConstructor(constructor.getParameterTypes());
			}
			catch (NoSuchMethodException ex) {
				// No equivalent constructor on user class (superclass)...
				// Let's proceed with the given constructor as we usually would.
			}
		}
		return constructor;
	}

	/**
	 * Template method for resolving the specified argument which is supposed to be autowired.
	 * <p>
	 *     用于解析应该自动装配的指定参数的模板方法
	 * </p>
	 */
	protected Object resolveAutowiredArgument(
			MethodParameter param, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) {

		//可以是一个注入点
		if (InjectionPoint.class.isAssignableFrom(param.getParameterType())) {
			InjectionPoint injectionPoint = currentInjectionPoint.get();
			if (injectionPoint == null) {
				throw new IllegalStateException("No current InjectionPoint available for " + param);
			}
			return injectionPoint;
		}
		//去解析依赖
		return this.beanFactory.resolveDependency(
				new DependencyDescriptor(param, true), beanName, autowiredBeanNames, typeConverter);
	}



	static InjectionPoint setCurrentInjectionPoint(InjectionPoint injectionPoint) {
		InjectionPoint old = currentInjectionPoint.get();
		if (injectionPoint != null) {
			currentInjectionPoint.set(injectionPoint);
		}
		else {
			currentInjectionPoint.remove();
		}
		return old;
	}


	/**
	 * Private inner class for holding argument combinations.
	 */
	private static class ArgumentsHolder {


		/**
		 * 原始的参数列表
		 */
		public final Object rawArguments[];

		/**
		 * 目标的参数列表
		 */
		public final Object arguments[];

		/**
		 * 准备好的参数列表
		 */
		public final Object preparedArguments[];

		public boolean resolveNecessary = false;

		public ArgumentsHolder(int size) {
			this.rawArguments = new Object[size];
			this.arguments = new Object[size];
			this.preparedArguments = new Object[size];
		}

		public ArgumentsHolder(Object[] args) {
			this.rawArguments = args;
			this.arguments = args;
			this.preparedArguments = args;
		}

		/**
		 * 宽松模式下或的权重
		 * @param paramTypes
		 * @return
		 */
		public int getTypeDifferenceWeight(Class<?>[] paramTypes) {
			// If valid arguments found, determine type difference weight.
			// Try type difference weight on both the converted arguments and
			// the raw arguments. If the raw weight is better, use it.
			// Decrease raw weight by 1024 to prefer it over equal converted weight.
			//如果找到有效参数，则确定类型差异权重。
			//尝试对转换的参数和类型赋予差异权重
			//原始参数 如果原始重量更好，请使用它。
			//将原始重量减少1024，优先于相等的转换重量。
			int typeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.arguments);
			int rawTypeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
			return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
		}

		/**
		 * 严格模式下的获得权重
		 * @param paramTypes
		 * @return
		 */
		public int getAssignabilityWeight(Class<?>[] paramTypes) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (!ClassUtils.isAssignableValue(paramTypes[i], this.arguments[i])) {
					return Integer.MAX_VALUE;
				}
			}
			for (int i = 0; i < paramTypes.length; i++) {
				if (!ClassUtils.isAssignableValue(paramTypes[i], this.rawArguments[i])) {
					return Integer.MAX_VALUE - 512;
				}
			}
			return Integer.MAX_VALUE - 1024;
		}

		/**
		 * 缓存操作
		 * @param mbd
		 * @param constructorOrFactoryMethod
		 */
		public void storeCache(RootBeanDefinition mbd, Object constructorOrFactoryMethod) {
			synchronized (mbd.constructorArgumentLock) {
				//将已经解析出来的的构造实例的方法进行缓存
				mbd.resolvedConstructorOrFactoryMethod = constructorOrFactoryMethod;
				//标记构造实例的参数以及被解析
				mbd.constructorArgumentsResolved = true;
				if (this.resolveNecessary) {
					//还需要解析，构建已经准备好的参数
					mbd.preparedConstructorArguments = this.preparedArguments;
				}
				else {
					//不需要解析了，直接全部给他
					mbd.resolvedConstructorArguments = this.arguments;
				}
			}
		}
	}


	/**
	 * Marker for autowired arguments in a cached argument array.
 	 */
	private static class AutowiredArgumentMarker {
	}


	/**
	 * Delegate for checking Java 6's {@link ConstructorProperties} annotation.
	 */
	private static class ConstructorPropertiesChecker {

		public static String[] evaluate(Constructor<?> candidate, int paramCount) {
			ConstructorProperties cp = candidate.getAnnotation(ConstructorProperties.class);
			if (cp != null) {
				String[] names = cp.value();
				if (names.length != paramCount) {
					throw new IllegalStateException("Constructor annotated with @ConstructorProperties but not " +
							"corresponding to actual number of parameters (" + paramCount + "): " + candidate);
				}
				return names;
			}
			else {
				return null;
			}
		}
	}

}
