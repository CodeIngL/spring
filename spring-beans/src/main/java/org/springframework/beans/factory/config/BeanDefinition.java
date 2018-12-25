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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} such as {@link PropertyPlaceholderConfigurer}
 * to introspect and modify property values and other bean metadata.
 *
 * <p>
 *    BeanDefinition描述了一个bean实例，它具有属性值，构造函数参数值以及具体实现提供的更多信息。
 * </p>
 * <p>
 *    这只是一个最小的接口:主要目的是允许BeanFactoryPostProcessor（如PropertyPlaceholderConfigurer）内省和修改属性值和其他bean元数据。
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	/**
	 * Scope identifier for the standard singleton scope: "singleton".
	 * <p>Note that extended bean factories might support further scopes.
	 * @see #setScope
	 */
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * Scope identifier for the standard prototype scope: "prototype".
	 * <p>Note that extended bean factories might support further scopes.
	 * @see #setScope
	 */
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


	/**
	 * Role hint indicating that a {@code BeanDefinition} is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 *
	 * 表示BeanDefinition是应用程序主要部分的角色提示。 通常对应于用户定义的bean。
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is a supporting
	 * part of some larger configuration, typically an outer
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * {@code SUPPORT} beans are considered important enough to be aware
	 * of when looking more closely at a particular
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition},
	 * but not when looking at the overall configuration of an application.
	 *
	 * 表明BeanDefinition是一些大型配置的支持部分，通常是外部org.springframework.beans.factory.parsing.ComponentDefinition。
	 * SUPPORT bean被认为足够重要，以便在更仔细地查看特定的org.springframework.beans.factory.parsing.ComponentDefinition时注意到，但在查看应用程序的整体配置时不会。
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is providing an
	 * entirely background role and has no relevance to the end-user. This hint is
	 * used when registering beans that are completely part of the internal workings
	 * of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 *
	 * 角色提示表明一个BeanDefinition提供了一个完全的后台角色，并且与最终用户没有关系。
	 * 这个提示用于注册完全属于org.springframework.beans.factory.parsing.ComponentDefinition内部工作的一部分的bean。
	 */
	int ROLE_INFRASTRUCTURE = 2;


	// Modifiable attributes

	/**
	 * Set the name of the parent definition of this bean definition, if any.
	 * <p>设置此bean定义的父定义的名称（如果有）。</p>
	 */
	void setParentName(String parentName);

	/**
	 * Return the name of the parent definition of this bean definition, if any.
	 * <p>
	 *     返回此bean定义的父定义的名称（如果有）。
	 * </p>
	 */
	String getParentName();

	/**
	 * Specify the bean class name of this bean definition.
	 * <p>The class name can be modified during bean factory post-processing,
	 * typically replacing the original class name with a parsed variant of it.
	 *
	 * <p>
	 *     指定此bean定义的bean类名称。
	 * </p>
	 * <p>
	 *     	 可以在bean工厂后处理期间修改类名，通常使用已解析的变体替换原始类名。
	 * </p>
	 * @see #setParentName
	 * @see #setFactoryBeanName
	 * @see #setFactoryMethodName
	 */
	void setBeanClassName(String beanClassName);

	/**
	 * Return the current bean class name of this bean definition.
	 * <p>Note that this does not have to be the actual class name used at runtime, in
	 * case of a child definition overriding/inheriting the class name from its parent.
	 * Also, this may just be the class that a factory method is called on, or it may
	 * even be empty in case of a factory bean reference that a method is called on.
	 * Hence, do <i>not</i> consider this to be the definitive bean type at runtime but
	 * rather only use it for parsing purposes at the individual bean definition level.
     * <p>
     *     返回此bean定义的当前bean类名。
     * </p>
     * <p>
     *          请注意，如果子定义从其父级覆盖/继承类名，则不必是运行时使用的实际类名。
     *          此外，这可能只是调用工厂方法的类，或者在工厂bean引用调用方法的情况下甚至可能为空。
     *          因此，不要在运行时将其视为最终的bean类型，而是仅在单个bean定义级别将其用于解析目的。
     * </p>
	 * @see #getParentName()
	 * @see #getFactoryBeanName()
	 * @see #getFactoryMethodName()
	 */
	String getBeanClassName();

	/**
	 * Override the target scope of this bean, specifying a new scope name.
     * <p>
     *     覆盖此bean的目标scope，指定新的scope名称。
     * </p>
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	void setScope(String scope);

	/**
	 * Return the name of the current target scope for this bean,
     * <p>
     *     返回此bean的当前目标cope的名称，
     * </p>
	 * or {@code null} if not known yet.
	 */
	String getScope();

	/**
	 * Set whether this bean should be lazily initialized.
	 * <p>If {@code false}, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
     * <p>
     *     设置是否应该懒惰地初始化此bean。
     * </p>
     * <p>
     *     如果为false，那么bean将在启动时由bean工厂实例化，这些工厂执行单例的初始化。
     * </p>
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
     * <p>
     *     返回是否应该懒惰地初始化此bean，即在启动时不急切实例化。 仅适用于单例bean。
     * </p>
	 */
	boolean isLazyInit();

	/**
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
     *
     * <p>
     *     设置此bean依赖于初始化的bean的名称。 bean工厂将保证首先初始化这些bean。
     * </p>
	 */
	void setDependsOn(String... dependsOn);

	/**
	 * Return the bean names that this bean depends on.
     * <p>
     *     返回此bean依赖的bean名称。
     * </p>
	 */
	String[] getDependsOn();

	/**
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 * <p>Note that this flag is designed to only affect type-based autowiring.
	 * It does not affect explicit references by name, which will get resolved even
	 * if the specified bean is not marked as an autowire candidate. As a consequence,
	 * autowiring by name will nevertheless inject a bean if the name matches.
     *
     * <p>
     *     设置此bean是否可以自动连接到其他bean。
     * </p>
     * <p>
     *          请注意，此标志旨在仅影响基于类型的自动装配。
     *          它不会影响名称的显式引用，即使指定的bean未标记为autowire候选，也会解析它。
     *          因此，如果名称匹配，按名称自动装配将注入bean。
     * </p>
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 */
	boolean isAutowireCandidate();

	/**
	 * Set whether this bean is a primary autowire candidate.
	 * <p>If this value is {@code true} for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
     *
     * <p>
     *     设置此bean是否为主要autowire候选者。
     * </p>
     * <p>
     *     如果这个值对于多个匹配的候选者中的一个bean来说是真的，那么它将作为打破平局。
     * </p>
	 */
	void setPrimary(boolean primary);

	/**
	 * Return whether this bean is a primary autowire candidate.
	 */
	boolean isPrimary();

	/**
	 * Specify the factory bean to use, if any.
	 * This the name of the bean to call the specified factory method on.
	 * @see #setFactoryMethodName
	 */
	void setFactoryBeanName(String factoryBeanName);

	/**
	 * Return the factory bean name, if any.
	 */
	String getFactoryBeanName();

	/**
	 * Specify a factory method, if any. This method will be invoked with
	 * constructor arguments, or with no arguments if none are specified.
	 * The method will be invoked on the specified factory bean, if any,
	 * or otherwise as a static method on the local bean class.
	 * @see #setFactoryBeanName
	 * @see #setBeanClassName
	 */
	void setFactoryMethodName(String factoryMethodName);

	/**
	 * Return a factory method, if any.
	 */
	String getFactoryMethodName();

	/**
	 * Return the constructor argument values for this bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
     *
     * <p>
     *     返回此bean的构造函数参数值。
     * </p>
     * <p>
     *          可以在bean工厂后处理期间修改返回的实例。
     * </p>
	 * @return the ConstructorArgumentValues object (never {@code null})
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * Return the property values to be applied to a new instance of the bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
     *
     * <p>
     *     返回要应用于bean的新实例的属性值。
     * </p>
     * <p>
     *          可以在bean工厂后处理期间修改返回的实例。
     * </p>
	 * @return the MutablePropertyValues object (never {@code null})
	 */
	MutablePropertyValues getPropertyValues();


	// Read-only attributes

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * @since 3.0
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	/**
	 * Return whether this bean is "abstract", that is, not meant to be instantiated.
	 */
	boolean isAbstract();

	/**
	 * Get the role hint for this {@code BeanDefinition}. The role hint
	 * provides the frameworks as well as tools with an indication of
	 * the role and importance of a particular {@code BeanDefinition}.
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();

	/**
	 * Return a human-readable description of this bean definition.
	 */
	String getDescription();

	/**
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	String getResourceDescription();

	/**
	 * Return the originating BeanDefinition, or {@code null} if none.
	 * Allows for retrieving the decorated bean definition, if any.
	 * <p>Note that this method returns the immediate originator. Iterate through the
	 * originator chain to find the original BeanDefinition as defined by the user.
	 * <p>
	 *     返回原始BeanDefinition，如果没有则返回null。 允许检索修饰的bean定义（如果有）。
	 * </p>
	 * <p>
	 *     请注意，此方法返回直接发起者。 遍历创建者链以查找用户定义的原始BeanDefinition。
	 * </p>
	 */
	BeanDefinition getOriginatingBeanDefinition();

}
