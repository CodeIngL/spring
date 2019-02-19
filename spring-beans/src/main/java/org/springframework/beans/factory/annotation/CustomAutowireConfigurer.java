/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * A {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}
 * implementation that allows for convenient registration of custom autowire
 * qualifier types.
 *
 * <pre class="code">
 * &lt;bean id="customAutowireConfigurer" class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer"&gt;
 *   &lt;property name="customQualifierTypes"&gt;
 *     &lt;set&gt;
 *       &lt;value&gt;mypackage.MyQualifier&lt;/value&gt;
 *     &lt;/set&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>
 *      {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}实现，允许方便地注册自定义autowire qualifier类型。
 * </p>
 * <pre class="code">
 * &lt;bean id="customAutowireConfigurer" class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer"&gt;
 *   &lt;property name="customQualifierTypes"&gt;
 *     &lt;set&gt;
 *       &lt;value&gt;mypackage.MyQualifier&lt;/value&gt;
 *     &lt;/set&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.annotation.Qualifier
 */
public class CustomAutowireConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	/**
	 * 自定义的注入限定符
	 */
	private Set<?> customQualifierTypes;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	/**
	 * Register custom qualifier annotation types to be considered
	 * when autowiring beans. Each element of the provided set may
	 * be either a Class instance or a String representation of the
	 * fully-qualified class name of the custom annotation.
	 * <p>Note that any annotation that is itself annotated with Spring's
	 * {@link org.springframework.beans.factory.annotation.Qualifier}
	 * does not require explicit registration.
	 *
	 * <p>
	 *     注册自动装配bean时要考虑的自定义限定符注释类型。 提供的集合的每个元素可以是Class实例，也可以是自定义注解的完全限定类名称的String表示形式。
	 * </p>
	 * <p>
	 *     	 请注意，任何使用Spring的{@link org.springframework.beans.factory.annotation.Qualifier}的注解都不需要显式注册。
	 * </p>
	 * @param customQualifierTypes the custom types to register
	 */
	public void setCustomQualifierTypes(Set<?> customQualifierTypes) {
		this.customQualifierTypes = customQualifierTypes;
	}


	@Override
	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.customQualifierTypes != null) {
			if (!(beanFactory instanceof DefaultListableBeanFactory)) {
				throw new IllegalStateException(
						"CustomAutowireConfigurer needs to operate on a DefaultListableBeanFactory");
			}
			DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) beanFactory;
			if (!(dlbf.getAutowireCandidateResolver() instanceof QualifierAnnotationAutowireCandidateResolver)) {
				dlbf.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
			}
			QualifierAnnotationAutowireCandidateResolver resolver =
					(QualifierAnnotationAutowireCandidateResolver) dlbf.getAutowireCandidateResolver();
			for (Object value : this.customQualifierTypes) {
				Class<? extends Annotation> customType = null;
				if (value instanceof Class) {
					customType = (Class<? extends Annotation>) value;
				}
				else if (value instanceof String) {
					String className = (String) value;
					customType = (Class<? extends Annotation>) ClassUtils.resolveClassName(className, this.beanClassLoader);
				}
				else {
					throw new IllegalArgumentException(
							"Invalid value [" + value + "] for custom qualifier type: needs to be Class or String.");
				}
				if (!Annotation.class.isAssignableFrom(customType)) {
					throw new IllegalArgumentException(
							"Qualifier type [" + customType.getName() + "] needs to be annotation type");
				}
				resolver.addQualifierType(customType);
			}
		}
	}

}
