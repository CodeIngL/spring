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

package org.springframework.context.support;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

/**
 * Specialization of {@link PlaceholderConfigurerSupport} that resolves ${...} placeholders
 * within bean definition property values and {@code @Value} annotations against the current
 * Spring {@link Environment} and its set of {@link PropertySources}.
 *
 * <p>This class is designed as a general replacement for {@code PropertyPlaceholderConfigurer}
 * in Spring 3.1 applications. It is used by default to support the {@code property-placeholder}
 * element in working against the spring-context-3.1 XSD, whereas spring-context versions
 * &lt;= 3.0 default to {@code PropertyPlaceholderConfigurer} to ensure backward compatibility.
 * See the spring-context XSD documentation for complete details.
 *
 * <p>Any local properties (e.g. those added via {@link #setProperties}, {@link #setLocations}
 * et al.) are added as a {@code PropertySource}. Search precedence of local properties is
 * based on the value of the {@link #setLocalOverride localOverride} property, which is by
 * default {@code false} meaning that local properties are to be searched last, after all
 * environment property sources.
 *
 * <p>See {@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}
 * and related javadocs for details on manipulating environment property sources.
 *
 * <p>
 *    特殊的{@link PlaceholderConfigurerSupport}，它针对当前Spring {@link Environment}及其{@link PropertySources}解析bean定义属性值和{@code @Value}注解中的${...}占位符。
 * </p>
 * <p>
 *    此类被设计为Spring 3.1应用程序中{@code PropertyPlaceholderConfigurer}的一般替代品。
 *    默认情况下，它在{@code property-placeholder}中使用元素来支持spring-context-3.1 XSD，而spring-context version <= 3.0。
 *    默认使用{@code PropertyPlaceholderConfigurer} 来确保向后兼容性。有关完整的详细信息，请参阅spring-context XSD文档。
 * </p>
 * <p>
 *    任何本地属性（例如通过{@link #setProperties}，{@link #setLocations}等添加的属性）都将添加作为一个{@code PropertySource}。
 *    本地属性的搜索优先级基于{@link #setLocalOverride localOverride}属性的值，默认情况下为{@code false}，表示在所有环境属性源之后最后搜索本地属性。
 * </p>
 * <p>
 *   有关操作环境属性源的详细信息，请参阅{@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}和相关的javadoc。
 * </p>
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.beans.factory.config.PlaceholderConfigurerSupport
 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 */
public class PropertySourcesPlaceholderConfigurer extends PlaceholderConfigurerSupport implements EnvironmentAware {

	/**
	 * {@value} is the name given to the {@link PropertySource} for the set of
	 * {@linkplain #mergeProperties() merged properties} supplied to this configurer.
	 */
	public static final String LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties";

	/**
	 * {@value} is the name given to the {@link PropertySource} that wraps the
	 * {@linkplain #setEnvironment environment} supplied to this configurer.
	 */
	public static final String ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties";


	private MutablePropertySources propertySources;

	private PropertySources appliedPropertySources;

	private Environment environment;


	/**
	 * Customize the set of {@link PropertySources} to be used by this configurer.
	 * Setting this property indicates that environment property sources and local
	 * properties should be ignored.
	 * @see #postProcessBeanFactory
	 */
	public void setPropertySources(PropertySources propertySources) {
		this.propertySources = new MutablePropertySources(propertySources);
	}

	/**
	 * {@inheritDoc}
	 * <p>{@code PropertySources} from this environment will be searched when replacing ${...} placeholders.
	 * @see #setPropertySources
	 * @see #postProcessBeanFactory
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}


	/**
	 * {@inheritDoc}
	 * <p>Processing occurs by replacing ${...} placeholders in bean definitions by resolving each
	 * against this configurer's set of {@link PropertySources}, which includes:
	 * <ul>
	 * <li>all {@linkplain org.springframework.core.env.ConfigurableEnvironment#getPropertySources
	 * environment property sources}, if an {@code Environment} {@linkplain #setEnvironment is present}
	 * <li>{@linkplain #mergeProperties merged local properties}, if {@linkplain #setLocation any}
	 * {@linkplain #setLocations have} {@linkplain #setProperties been}
	 * {@linkplain #setPropertiesArray specified}
	 * <li>any property sources set by calling {@link #setPropertySources}
	 * </ul>
	 * <p>If {@link #setPropertySources} is called, <strong>environment and local properties will be
	 * ignored</strong>. This method is designed to give the user fine-grained control over property
	 * sources, and once set, the configurer makes no assumptions about adding additional sources.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.propertySources == null) { //延迟初始化
			this.propertySources = new MutablePropertySources();
			if (this.environment != null) { //加入环境源
				this.propertySources.addLast(
					new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, this.environment) {
						@Override
						public String getProperty(String key) {
							return this.source.getProperty(key);
						}
					}
				);
			}
			try {
				//构建本地源
				PropertySource<?> localPropertySource =
						new PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties());
				if (this.localOverride) {
					this.propertySources.addFirst(localPropertySource);
				}
				else {
					this.propertySources.addLast(localPropertySource);
				}
			}
			catch (IOException ex) {
				throw new BeanInitializationException("Could not load properties", ex);
			}
		}

		processProperties(beanFactory, new PropertySourcesPropertyResolver(this.propertySources));
		this.appliedPropertySources = this.propertySources;
	}

	/**
	 * Visit each bean definition in the given bean factory and attempt to replace ${...} property
	 * placeholders with values from the given properties.
	 * <p>
	 *     访问给定bean工厂中的每个bean定义，并尝试使用给定属性中的值替换${...}属性占位符。
	 * </p>
	 */
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			final ConfigurablePropertyResolver propertyResolver) throws BeansException {

		//前缀
		propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
		//后缀
		propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
		//值的分割符
		propertyResolver.setValueSeparator(this.valueSeparator);

		StringValueResolver valueResolver = new StringValueResolver() {
			@Override
			public String resolveStringValue(String strVal) {
				String resolved = (ignoreUnresolvablePlaceholders ?
						propertyResolver.resolvePlaceholders(strVal) :
						propertyResolver.resolveRequiredPlaceholders(strVal));
				if (trimValues) {
					resolved = resolved.trim();
				}
				return (resolved.equals(nullValue) ? null : resolved);
			}
		};

		doProcessProperties(beanFactoryToProcess, valueResolver);
	}

	/**
	 * Implemented for compatibility with {@link org.springframework.beans.factory.config.PlaceholderConfigurerSupport}.
	 * @deprecated in favor of {@link #processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver)}
	 * @throws UnsupportedOperationException
	 */
	@Override
	@Deprecated
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) {
		throw new UnsupportedOperationException(
				"Call processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver) instead");
	}

	/**
	 * Returns the property sources that were actually applied during
	 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory) post-processing}.
	 * @return the property sources that were applied
	 * @throws IllegalStateException if the property sources have not yet been applied
	 * @since 4.0
	 */
	public PropertySources getAppliedPropertySources() throws IllegalStateException {
		Assert.state(this.appliedPropertySources != null, "PropertySources have not get been applied");
		return this.appliedPropertySources;
	}

}
