/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * TargetSourceCreator that enforces a LazyInitTargetSource for each bean
 * that is defined as "lazy-init". This will lead to a proxy created for
 * each of those beans, allowing to fetch a reference to such a bean
 * without actually initializing the target bean instance.
 *
 * <p>To be registered as custom TargetSourceCreator for an auto-proxy creator,
 * in combination with custom interceptors for specific beans or for the
 * creation of lazy-init proxies only. For example, as autodetected
 * infrastructure bean in an XML application context definition:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator"&gt;
 *   &lt;property name="customTargetSourceCreators"&gt;
 *     &lt;list&gt;
 *       &lt;bean class="org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myLazyInitBean" class="mypackage.MyBeanClass" lazy-init="true"&gt;
 *   ...
 * &lt;/bean&gt;</pre>
 *
 * <p>
 *     TargetSourceCreator为每个定义为“lazy-init”的bean强制执行LazyInitTargetSource。 这将导致为每个bean创建一个代理，允许在不实际初始化目标bean实例的情况下获取对这样的bean的引用。
 * </p>
 * <p>
 *     要为自动代理创建者注册为自定义TargetSourceCreator，请将其与特定bean的自定义拦截器一起注册，或仅用于创建lazy-init代理。 例如，作为XML应用程序上下文定义中的自动检测基础结构bean：
 * </p>
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
public class LazyInitTargetSourceCreator extends AbstractBeanFactoryBasedTargetSourceCreator {

	@Override
	protected boolean isPrototypeBased() {
		return false;
	}

	@Override
	protected AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class<?> beanClass, String beanName) {

		if (getBeanFactory() instanceof ConfigurableListableBeanFactory) {
			BeanDefinition definition =
					((ConfigurableListableBeanFactory) getBeanFactory()).getBeanDefinition(beanName);
			if (definition.isLazyInit()) {
				return new LazyInitTargetSource();
			}
		}
		return null;
	}

}
