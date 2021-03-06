/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.context.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.StringUtils;

/**
 * Reads a given fully-populated set of ConfigurationClass instances, registering bean
 * definitions with the given {@link BeanDefinitionRegistry} based on its contents.
 *
 * <p>This class was modeled after the {@link BeanDefinitionReader} hierarchy, but does
 * not implement/extend any of its artifacts as a set of configuration classes is not a
 * {@link Resource}.
 *
 *
 * <p>
 *     	读取给定的一组完整的ConfigurationClass实例，根据BeanDefinitionRegistry的内容注册Bean定义。
 * </p>
 * <p>
 *     该类是在BeanDefinitionReader层次结构之后建模的，但是由于一组配置类不是资源，因此不实现/扩展任何工件。
 * </p>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.0
 * @see ConfigurationClassParser
 */
class ConfigurationClassBeanDefinitionReader {

	private static final Log logger = LogFactory.getLog(ConfigurationClassBeanDefinitionReader.class);

	private static final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private final BeanDefinitionRegistry registry;

	private final SourceExtractor sourceExtractor;

	private final ResourceLoader resourceLoader;

	private final Environment environment;

	private final BeanNameGenerator importBeanNameGenerator;

	private final ImportRegistry importRegistry;

	private final ConditionEvaluator conditionEvaluator;


	/**
	 * Create a new {@link ConfigurationClassBeanDefinitionReader} instance that will be used
	 * to populate the given {@link BeanDefinitionRegistry}.
	 */
	ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry, SourceExtractor sourceExtractor,
			ResourceLoader resourceLoader, Environment environment, BeanNameGenerator importBeanNameGenerator,
			ImportRegistry importRegistry) {

		this.registry = registry;
		this.sourceExtractor = sourceExtractor;
		this.resourceLoader = resourceLoader;
		this.environment = environment;
		this.importBeanNameGenerator = importBeanNameGenerator;
		this.importRegistry = importRegistry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
	}


	/**
	 * Read {@code configurationModel}, registering bean definitions
	 * with the registry based on its contents.
	 * <p>
	 *     读取configurationModel，根据其内容向注册表注册bean定义。
	 * </p>
	 */
	public void loadBeanDefinitions(Set<ConfigurationClass> configurationModel) {
		TrackedConditionEvaluator trackedConditionEvaluator = new TrackedConditionEvaluator();
		for (ConfigurationClass configClass : configurationModel) {
			loadBeanDefinitionsForConfigurationClass(configClass, trackedConditionEvaluator);
		}
	}

	/**
	 * Read a particular {@link ConfigurationClass}, registering bean definitions
	 * for the class itself and all of its {@link Bean} methods.
	 *
	 * <p>
	 *     read一个特定的ConfigurationClass，为类本身及其所有Bean方法注册bean定义。
	 * </p>
	 */
	private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass,
			TrackedConditionEvaluator trackedConditionEvaluator) {

		//是否跳过，一旦跳过，就删除前面的信息
		if (trackedConditionEvaluator.shouldSkip(configClass)) {
			String beanName = configClass.getBeanName();
			if (StringUtils.hasLength(beanName) && this.registry.containsBeanDefinition(beanName)) {
				this.registry.removeBeanDefinition(beanName);
			}
			this.importRegistry.removeImportingClass(configClass.getMetadata().getClassName());
			return;
		}

		if (configClass.isImported()) {
			//注册由importby产生的配置类，需要转换为beanDefinition
			registerBeanDefinitionForImportedConfigurationClass(configClass);
		}
		for (BeanMethod beanMethod : configClass.getBeanMethods()) {//注册@bean产生的
			loadBeanDefinitionsForBeanMethod(beanMethod);
		}
		//注册由@importedResources产生的
		loadBeanDefinitionsFromImportedResources(configClass.getImportedResources());
		//注册由ImportBeanDefinitionRegistrar产生的
		loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
	}

	/**
	 * Register the {@link Configuration} class itself as a bean definition.
	 * <p>
	 *     将{@link Configuration}类本身注册为bean定义。
	 * </p>
	 */
	private void registerBeanDefinitionForImportedConfigurationClass(ConfigurationClass configClass) {
		//获得注解元信息
		AnnotationMetadata metadata = configClass.getMetadata();
		//构建beanDefinition
		AnnotatedGenericBeanDefinition configBeanDef = new AnnotatedGenericBeanDefinition(metadata);

		//应用作用域
		ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(configBeanDef);
		configBeanDef.setScope(scopeMetadata.getScopeName());
		//生成名称
		String configBeanName = this.importBeanNameGenerator.generateBeanName(configBeanDef, this.registry);
		//处理通用的注解
		AnnotationConfigUtils.processCommonDefinitionAnnotations(configBeanDef, metadata);

		//实际的暴露形式
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(configBeanDef, configBeanName);
		//处理是否代理相关的形式
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		//向注册表中注册相关的bean
		this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
		//设置名字
		configClass.setBeanName(configBeanName);

		if (logger.isDebugEnabled()) {
			logger.debug("Registered bean definition for imported class '" + configBeanName + "'");
		}
	}

	/**
	 * Read the given {@link BeanMethod}, registering bean definitions
	 * with the BeanDefinitionRegistry based on its contents.
	 * <p>
	 *    读取给定的BeanMethod，根据BeanDefinitionRegistry的内容注册bean定义。
	 * </p>
	 */
	private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {
		//获得所说的配置类
		ConfigurationClass configClass = beanMethod.getConfigurationClass();
		//获得方法元信息
		MethodMetadata metadata = beanMethod.getMetadata();
		//获得方法名
		String methodName = metadata.getMethodName();

		// Do we need to mark the bean as skipped by its condition?
		// 我们是否需要将bean标记为跳过它的状态？
		if (this.conditionEvaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
			configClass.skippedBeanMethods.add(methodName);
			return;
		}
		//已经在跳过的方法，直接跳过吧
		if (configClass.skippedBeanMethods.contains(methodName)) {
			return;
		}

		// Consider name and any aliases
		// 考虑名称和任何别名
		// 获得bean注解中的信息
		AnnotationAttributes bean = AnnotationConfigUtils.attributesFor(metadata, Bean.class);
		//如果指定了名字，使用名字
		List<String> names = new ArrayList<String>(Arrays.asList(bean.getStringArray("name")));
		//名字为第一个，其他都是别名
		String beanName = (!names.isEmpty() ? names.remove(0) : methodName);

		// Register aliases even when overridden
		// 即使这个bean被覆盖了也注册别名
		for (String alias : names) {
			this.registry.registerAlias(beanName, alias);
		}

		// Has this effectively been overridden before (e.g. via XML)?
		// 之前是否有效地覆盖了它（例如通过XML）？
		if (isOverriddenByExistingDefinition(beanMethod, beanName)) {
			if (beanName.equals(beanMethod.getConfigurationClass().getBeanName())) {
				throw new BeanDefinitionStoreException(beanMethod.getConfigurationClass().getResource().getDescription(),
						beanName, "Bean name derived from @Bean method '" + beanMethod.getMetadata().getMethodName() +
						"' clashes with bean name for containing configuration class; please make those names unique!");
			}
			return;
		}

		//通过beanMethod来构建对应的beandefine
		ConfigurationClassBeanDefinition beanDef = new ConfigurationClassBeanDefinition(configClass, metadata);
		beanDef.setResource(configClass.getResource());
		beanDef.setSource(this.sourceExtractor.extractSource(metadata, configClass.getResource()));

		//静态方法
		if (metadata.isStatic()) {
			// static @Bean method
			beanDef.setBeanClassName(configClass.getMetadata().getClassName());
			beanDef.setFactoryMethodName(methodName);
		}
		else {
			// instance @Bean method
			beanDef.setFactoryBeanName(configClass.getBeanName());
			beanDef.setUniqueFactoryMethodName(methodName);
		}
		//设置注入方式为构造模式方式的注入
		beanDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		beanDef.setAttribute(RequiredAnnotationBeanPostProcessor.SKIP_REQUIRED_CHECK_ATTRIBUTE, Boolean.TRUE);

		//处理通用的注解
		AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDef, metadata);

		//显示的进行了相关的设置
		Autowire autowire = bean.getEnum("autowire");
		if (autowire.isAutowire()) {
			beanDef.setAutowireMode(autowire.value());
		}

		//initMethod方法
		String initMethodName = bean.getString("initMethod");
		if (StringUtils.hasText(initMethodName)) {
			beanDef.setInitMethodName(initMethodName);
		}

		//destoryMethod方法处理
		String destroyMethodName = bean.getString("destroyMethod");
		if (destroyMethodName != null) {
			beanDef.setDestroyMethodName(destroyMethodName);
		}

		// Consider scoping
		// 考虑作用域
		ScopedProxyMode proxyMode = ScopedProxyMode.NO;
		AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(metadata, Scope.class);
		if (attributes != null) {
			beanDef.setScope(attributes.getString("value"));
			proxyMode = attributes.getEnum("proxyMode");
			if (proxyMode == ScopedProxyMode.DEFAULT) {
				proxyMode = ScopedProxyMode.NO;
			}
		}

		// Replace the original bean definition with the target one, if necessary
		//如有必要，将原始bean定义替换为目标bean定义
		BeanDefinition beanDefToRegister = beanDef;
		if (proxyMode != ScopedProxyMode.NO) {
			BeanDefinitionHolder proxyDef = ScopedProxyCreator.createScopedProxy(
					new BeanDefinitionHolder(beanDef, beanName), this.registry,
					proxyMode == ScopedProxyMode.TARGET_CLASS);
			beanDefToRegister = new ConfigurationClassBeanDefinition(
					(RootBeanDefinition) proxyDef.getBeanDefinition(), configClass, metadata);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Registering bean definition for @Bean method %s.%s()",
					configClass.getMetadata().getClassName(), beanName));
		}

		this.registry.registerBeanDefinition(beanName, beanDefToRegister);
	}

	/**
	 *
	 * 是否存在着已存在的beandefinition
	 * @param beanMethod
	 * @param beanName
	 * @return
	 */
	protected boolean isOverriddenByExistingDefinition(BeanMethod beanMethod, String beanName) {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return false;
		}
		BeanDefinition existingBeanDef = this.registry.getBeanDefinition(beanName);

		// Is the existing bean definition one that was created from a configuration class?
		// -> allow the current bean method to override, since both are at second-pass level.
		// However, if the bean method is an overloaded case on the same configuration class,
		// preserve the existing bean definition.
		// 现有的bean定义是从配置类创建的吗？
		// - >允许当前bean方法覆盖，因为两者都处于second-pass级别。
		// 但是，如果bean方法是同一配置类上的重载大小写，则保留现有的bean定义。
		if (existingBeanDef instanceof ConfigurationClassBeanDefinition) {
			ConfigurationClassBeanDefinition ccbd = (ConfigurationClassBeanDefinition) existingBeanDef;
			return ccbd.getMetadata().getClassName().equals(
					beanMethod.getConfigurationClass().getMetadata().getClassName());
		}

		// A bean definition resulting from a component scan can be silently overridden
		// by an @Bean method, as of 4.2...
		// 从4.2开始，@Bean方法可以静默覆盖由组件扫描产生的bean定义...
		if (existingBeanDef instanceof ScannedGenericBeanDefinition) {
			return false;
		}

		// Has the existing bean definition bean marked as a framework-generated bean?
		// -> allow the current bean method to override it, since it is application-level
		// 将现有的bean定义bean标记为框架生成的bean吗? - >允许当前的bean方法覆盖它，因为它是应用程序级别的
		if (existingBeanDef.getRole() > BeanDefinition.ROLE_APPLICATION) {
			return false;
		}

		// At this point, it's a top-level override (probably XML), just having been parsed
		// before configuration class processing kicks in...
		// 此时，它是一个顶级覆盖（可能是XML），只是在配置类处理开始之前解析了...
		if (this.registry instanceof DefaultListableBeanFactory &&
				!((DefaultListableBeanFactory) this.registry).isAllowBeanDefinitionOverriding()) {
			throw new BeanDefinitionStoreException(beanMethod.getConfigurationClass().getResource().getDescription(),
					beanName, "@Bean definition illegally overridden by existing bean definition: " + existingBeanDef);
		}
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Skipping bean definition for %s: a definition for bean '%s' " +
					"already exists. This top-level bean definition is considered as an override.",
					beanMethod, beanName));
		}
		return true;
	}

	/**
	 * 从ImportResource中加载bean定义
	 * ImportResource指向的是资源的位置，很可能触发xml形式的解析，或者其他方式的解析
	 * @param importedResources
	 */
	private void loadBeanDefinitionsFromImportedResources(
			Map<String, Class<? extends BeanDefinitionReader>> importedResources) {

		Map<Class<?>, BeanDefinitionReader> readerInstanceCache = new HashMap<Class<?>, BeanDefinitionReader>();

		//遍历处理，资源和一种方式的读取器对应，比如xml应该和xmlBedefinitionRead对应
		for (Map.Entry<String, Class<? extends BeanDefinitionReader>> entry : importedResources.entrySet()) {
			//资源值
			String resource = entry.getKey();
			//Reader类型
			Class<? extends BeanDefinitionReader> readerClass = entry.getValue();

			// Default reader selection necessary?
			// 是否需要默认读卡器？
			if (BeanDefinitionReader.class == readerClass) {//没有指定的情况下，我们简单给他们进行指定，一般是groovy和xml两种方式
				if (StringUtils.endsWithIgnoreCase(resource, ".groovy")) {
					// When clearly asking for Groovy, that's what they'll get...
					// 当明确要求Groovy时，那就是他们会得到的......
					readerClass = GroovyBeanDefinitionReader.class;
				}
				else {
					// Primarily ".xml" files but for any other extension as well
					// 主要是“.xml”文件，但也适用于任何其他扩展名
					readerClass = XmlBeanDefinitionReader.class;
				}
			}

			//具体指定过了的，则不用指定这些，从缓存中尝试先读取一个
			BeanDefinitionReader reader = readerInstanceCache.get(readerClass);
			//不存在就讲过相关的构造器狗杂一遍。
			if (reader == null) {
				try {
					// Instantiate the specified BeanDefinitionReader
					// 实例化指定的BeanDefinitionReader
					reader = readerClass.getConstructor(BeanDefinitionRegistry.class).newInstance(this.registry);
					// Delegate the current ResourceLoader to it if possible
					// 如果可能，请将当前的ResourceLoader委派给它
					// 设置相关资源，比如resourceloader和environment
					if (reader instanceof AbstractBeanDefinitionReader) {
						AbstractBeanDefinitionReader abdr = ((AbstractBeanDefinitionReader) reader);
						abdr.setResourceLoader(this.resourceLoader);
						abdr.setEnvironment(this.environment);
					}
					readerInstanceCache.put(readerClass, reader);
				}
				catch (Throwable ex) {
					throw new IllegalStateException(
							"Could not instantiate BeanDefinitionReader class [" + readerClass.getName() + "]");
				}
			}

			// TODO SPR-6310: qualify relative path locations as done in AbstractContextLoader.modifyLocations
			// 使用阅读器去加载这些资源。
			reader.loadBeanDefinitions(resource);
		}
	}

	/**
	 * 从ImportBeanDefinitionRegistrar里面加载相应的bean定义
	 * @param registrars
	 */
	private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {
		for (Map.Entry<ImportBeanDefinitionRegistrar, AnnotationMetadata> entry : registrars.entrySet()) {
			entry.getKey().registerBeanDefinitions(entry.getValue(), this.registry);
		}
	}


	/**
	 * {@link RootBeanDefinition} marker subclass used to signify that a bean definition
	 * was created from a configuration class as opposed to any other configuration source.
	 * Used in bean overriding cases where it's necessary to determine whether the bean
	 * definition was created externally.
	 */
	@SuppressWarnings("serial")
	private static class ConfigurationClassBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {

		private final AnnotationMetadata annotationMetadata;

		private final MethodMetadata factoryMethodMetadata;

		public ConfigurationClassBeanDefinition(ConfigurationClass configClass, MethodMetadata beanMethodMetadata) {
			this.annotationMetadata = configClass.getMetadata();
			this.factoryMethodMetadata = beanMethodMetadata;
			setLenientConstructorResolution(false);
		}

		public ConfigurationClassBeanDefinition(
				RootBeanDefinition original, ConfigurationClass configClass, MethodMetadata beanMethodMetadata) {
			super(original);
			this.annotationMetadata = configClass.getMetadata();
			this.factoryMethodMetadata = beanMethodMetadata;
		}

		private ConfigurationClassBeanDefinition(ConfigurationClassBeanDefinition original) {
			super(original);
			this.annotationMetadata = original.annotationMetadata;
			this.factoryMethodMetadata = original.factoryMethodMetadata;
		}

		@Override
		public AnnotationMetadata getMetadata() {
			return this.annotationMetadata;
		}

		@Override
		public MethodMetadata getFactoryMethodMetadata() {
			return this.factoryMethodMetadata;
		}

		@Override
		public boolean isFactoryMethod(Method candidate) {
			return (super.isFactoryMethod(candidate) && BeanAnnotationHelper.isBeanAnnotated(candidate));
		}

		@Override
		public ConfigurationClassBeanDefinition cloneBeanDefinition() {
			return new ConfigurationClassBeanDefinition(this);
		}
	}


	/**
	 * Evaluate {@code @Conditional} annotations, tracking results and taking into
	 * account 'imported by'.
	 * <p>
	 *     评估@Conditional注释，跟踪结果并考虑'imported by'。
	 * </p>
	 */
	private class TrackedConditionEvaluator {

		//配置是否跳过
		private final Map<ConfigurationClass, Boolean> skipped = new HashMap<ConfigurationClass, Boolean>();

		public boolean shouldSkip(ConfigurationClass configClass) {
			Boolean skip = this.skipped.get(configClass);
			if (skip == null) { //没有设置，进行设置
				if (configClass.isImported()) { //如果是导入的
					boolean allSkipped = true;
					for (ConfigurationClass importedBy : configClass.getImportedBy()) { //检查导入源是否被取消
						if (!shouldSkip(importedBy)) {
							allSkipped = false;
							break;
						}
					}
					if (allSkipped) { //导入源被取消了，自然自己也要被取消了
						// The config classes that imported this one were all skipped, therefore we are skipped...
						skip = true;
					}
				}
				if (skip == null) { //再次校验条件，这时候是Bean的
					skip = conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN);
				}
				this.skipped.put(configClass, skip);
			}
			return skip;
		}
	}

}
