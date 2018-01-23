/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.web.context.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;

/**
 * {@link org.springframework.web.context.WebApplicationContext WebApplicationContext}
 * implementation which accepts annotated classes as input - in particular
 * {@link org.springframework.context.annotation.Configuration @Configuration}-annotated
 * classes, but also plain {@link org.springframework.stereotype.Component @Component}
 * classes and JSR-330 compliant classes using {@code javax.inject} annotations. Allows
 * for registering classes one by one (specifying class names as config location) as well
 * as for classpath scanning (specifying base packages as config location).
 *
 * <p>This is essentially the equivalent of
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext
 * AnnotationConfigApplicationContext} for a web environment.
 *
 * <p>To make use of this application context, the
 * {@linkplain ContextLoader#CONTEXT_CLASS_PARAM "contextClass"} context-param for
 * ContextLoader and/or "contextClass" init-param for FrameworkServlet must be set to
 * the fully-qualified name of this class.
 *
 * <p>As of Spring 3.1, this class may also be directly instantiated and injected into
 * Spring's {@code DispatcherServlet} or {@code ContextLoaderListener} when using the
 * new {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializer}
 * code-based alternative to {@code web.xml}. See its Javadoc for details and usage examples.
 *
 * <p>Unlike {@link XmlWebApplicationContext}, no default configuration class locations
 * are assumed. Rather, it is a requirement to set the
 * {@linkplain ContextLoader#CONFIG_LOCATION_PARAM "contextConfigLocation"}
 * context-param for {@link ContextLoader} and/or "contextConfigLocation" init-param for
 * FrameworkServlet.  The param-value may contain both fully-qualified
 * class names and base packages to scan for components. See {@link #loadBeanDefinitions}
 * for exact details on how these locations are processed.
 *
 * <p>As an alternative to setting the "contextConfigLocation" parameter, users may
 * implement an {@link org.springframework.context.ApplicationContextInitializer
 * ApplicationContextInitializer} and set the
 * {@linkplain ContextLoader#CONTEXT_INITIALIZER_CLASSES_PARAM "contextInitializerClasses"}
 * context-param / init-param. In such cases, users should favor the {@link #refresh()}
 * and {@link #scan(String...)} methods over the {@link #setConfigLocation(String)}
 * method, which is primarily for use by {@code ContextLoader}.
 *
 * <p>Note: In case of multiple {@code @Configuration} classes, later {@code @Bean}
 * definitions will override ones defined in earlier loaded files. This can be leveraged
 * to deliberately override certain bean definitions via an extra Configuration class.
 *
 * <p>
 *     接受注释类作为输入的WebApplicationContext实现 - 特别是@Configuration注释的类，还包括普通的@Component类和使用javax.inject注解的兼容JSR-330的类。允许逐个注册类（指定类名作为配置位置）以及类路径扫描（指定基本包作为配置位置）。
 * <p>
 *     这基本上相当于Web环境的AnnotationConfigApplicationContext。
 * <p>
 *     为了使用这个应用程序上下文，必须将ContextLoader和/或contextClass的init-param的contextClass context-param设置为这个类的完全限定名称。
 * <p>
 *     从Spring 3.1开始，当使用基于代码的新WebApplicationInitializer替代web.xml时，这个类也可以直接实例化并注入到Spring的DispatcherServlet或ContextLoaderListener中。有关详细信息和用法示例，请参阅其Javadoc。
 * <p>
 *     与XmlWebApplicationContext不同，没有默认的配置类位置。
 *     而是需要为FrameworkServlet的ContextLoader和/或“contextConfigLocation”init-param设置“contextConfigLocation”上下文参数。参数值可以包含完全限定的类名和基本包来扫描组件。有关如何处理这些位置的详细信息，请参阅loadBeanDefinitions。
 * <p>
 *     作为设置“contextConfigLocation”参数的替代方案，用户可以实现ApplicationContextInitializer并设置“contextInitializerClasses”上下文参数/初始参数。 在这种情况下，用户应该优先使用setConfigLocation（String）方法的refresh（）和scan（String ...）方法，该方法主要供ContextLoader使用。
 * <p>
 *     注意：如果有多个@Configuration类，稍后的@Bean定义将覆盖在先前加载的文件中定义的定义。 这可以用来通过一个额外的配置类故意重写某些bean定义。
 * <p>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public class AnnotationConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext
		implements AnnotationConfigRegistry {

	private BeanNameGenerator beanNameGenerator;

	private ScopeMetadataResolver scopeMetadataResolver;

	private final Set<Class<?>> annotatedClasses = new LinkedHashSet<Class<?>>();

	private final Set<String> basePackages = new LinkedHashSet<String>();


	/**
	 * Set a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Return the custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 */
	protected BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}

	/**
	 * Set a custom {@link ScopeMetadataResolver} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}.
	 * <p>Default is an {@link org.springframework.context.annotation.AnnotationScopeMetadataResolver}.
	 * @see AnnotatedBeanDefinitionReader#setScopeMetadataResolver
	 * @see ClassPathBeanDefinitionScanner#setScopeMetadataResolver
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}

	/**
	 * Return the custom {@link ScopeMetadataResolver} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 */
	protected ScopeMetadataResolver getScopeMetadataResolver() {
		return this.scopeMetadataResolver;
	}


	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link org.springframework.context.annotation.Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #loadBeanDefinitions(DefaultListableBeanFactory)
	 * @see #setConfigLocation(String)
	 * @see #refresh()
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		this.annotatedClasses.addAll(Arrays.asList(annotatedClasses));
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param basePackages the packages to check for annotated classes
	 * @see #loadBeanDefinitions(DefaultListableBeanFactory)
	 * @see #register(Class...)
	 * @see #setConfigLocation(String)
	 * @see #refresh()
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.basePackages.addAll(Arrays.asList(basePackages));
	}


	/**
	 * Register a {@link org.springframework.beans.factory.config.BeanDefinition} for
	 * any classes specified by {@link #register(Class...)} and scan any packages
	 * specified by {@link #scan(String...)}.
	 * <p>For any values specified by {@link #setConfigLocation(String)} or
	 * {@link #setConfigLocations(String[])}, attempt first to load each location as a
	 * class, registering a {@code BeanDefinition} if class loading is successful,
	 * and if class loading fails (i.e. a {@code ClassNotFoundException} is raised),
	 * assume the value is a package and attempt to scan it for annotated classes.
	 * <p>Enables the default set of annotation configuration post processors, such that
	 * {@code @Autowired}, {@code @Required}, and associated annotations can be used.
	 * <p>Configuration class bean definitions are registered with generated bean
	 * definition names unless the {@code value} attribute is provided to the stereotype
	 * annotation.
	 * <p>
	 *     注册由register（Class ...）指定的任何类的org.springframework.beans.factory.config.BeanDefinition并扫描scan（String ...）指定的任何包。
	 * <p>
	 *     对于由setConfigLocation（String）或setConfigLocations（String []）指定的任何值，首先尝试将每个位置作为类加载，如果类加载成功，则注册BeanDefinition，如果类加载失败（即引发ClassNotFoundException） 该值是一个包，并尝试扫描它的注释类。
	 * <p>
	 *     对于由setConfigLocation（String）或setConfigLocations（String []）指定的任何值，首先尝试将每个位置作为类加载，如果类加载成功，则注册BeanDefinition，如果类加载失败（即引发ClassNotFoundException） 该值是一个包，并尝试扫描它的注释类。
	 * <p>
	 *     启用默认的注释配置后处理器集合，以便可以使用@Autowired，@Required和关联的注释。
	 * <p>
	 *     除非value属性被提供给构造型注释，否则配置类的bean定义被注册到生成的bean定义名称中。
	 *
	 * @param beanFactory the bean factory to load bean definitions into
	 * @see #register(Class...)
	 * @see #scan(String...)
	 * @see #setConfigLocation(String)
	 * @see #setConfigLocations(String[])
	 * @see AnnotatedBeanDefinitionReader
	 * @see ClassPathBeanDefinitionScanner
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
		//获得bean定义读取器
		AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		//获得bean定义扫描器
		ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);

		//获得名字生成器
		BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
		//先对这个名字生成器进行注册
		if (beanNameGenerator != null) {
			reader.setBeanNameGenerator(beanNameGenerator);
			scanner.setBeanNameGenerator(beanNameGenerator);
			beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
		}

		//获得策略解析器解析bean定义范围，单例或者多例之类的
		ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();
		if (scopeMetadataResolver != null) {
			reader.setScopeMetadataResolver(scopeMetadataResolver);
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		//获得注解类
		if (!this.annotatedClasses.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Registering annotated classes: [" +
						StringUtils.collectionToCommaDelimitedString(this.annotatedClasses) + "]");
			}
			reader.register(this.annotatedClasses.toArray(new Class<?>[this.annotatedClasses.size()]));
		}

		//获得基本表
		if (!this.basePackages.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Scanning base packages: [" +
						StringUtils.collectionToCommaDelimitedString(this.basePackages) + "]");
			}
			scanner.scan(this.basePackages.toArray(new String[this.basePackages.size()]));
		}

		//获得配置定位
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			//处理配置类
			for (String configLocation : configLocations) {
				try {
					Class<?> clazz = getClassLoader().loadClass(configLocation);
					if (logger.isInfoEnabled()) {
						logger.info("Successfully resolved class for [" + configLocation + "]");
					}
					//进行注册
					reader.register(clazz);
				}
				catch (ClassNotFoundException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Could not load class for config location [" + configLocation +
								"] - trying package scan. " + ex);
					}
					int count = scanner.scan(configLocation);
					if (logger.isInfoEnabled()) {
						if (count == 0) {
							logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
						}
						else {
							logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
						}
					}
				}
			}
		}
	}


	/**
	 * Build an {@link AnnotatedBeanDefinitionReader} for the given bean factory.
	 * <p>This should be pre-configured with the {@code Environment} (if desired)
	 * but not with a {@code BeanNameGenerator} or {@code ScopeMetadataResolver} yet.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @since 4.1.9
	 * @see #getEnvironment()
	 * @see #getBeanNameGenerator()
	 * @see #getScopeMetadataResolver()
	 */
	protected AnnotatedBeanDefinitionReader getAnnotatedBeanDefinitionReader(DefaultListableBeanFactory beanFactory) {
		return new AnnotatedBeanDefinitionReader(beanFactory, getEnvironment());
	}

	/**
	 * Build a {@link ClassPathBeanDefinitionScanner} for the given bean factory.
	 * <p>This should be pre-configured with the {@code Environment} (if desired)
	 * but not with a {@code BeanNameGenerator} or {@code ScopeMetadataResolver} yet.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @since 4.1.9
	 * @see #getEnvironment()
	 * @see #getBeanNameGenerator()
	 * @see #getScopeMetadataResolver()
	 */
	protected ClassPathBeanDefinitionScanner getClassPathBeanDefinitionScanner(DefaultListableBeanFactory beanFactory) {
		return new ClassPathBeanDefinitionScanner(beanFactory, true, getEnvironment());
	}

}
