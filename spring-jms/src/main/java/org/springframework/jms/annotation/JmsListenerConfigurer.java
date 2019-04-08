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

package org.springframework.jms.annotation;

import org.springframework.jms.config.JmsListenerEndpointRegistrar;

/**
 * Optional interface to be implemented by a Spring managed bean willing
 * to customize how JMS listener endpoints are configured. Typically
 * used to define the default {@link org.springframework.jms.config.JmsListenerContainerFactory
 * JmsListenerContainerFactory} to use or for registering JMS endpoints
 * in a <em>programmatic</em> fashion as opposed to the <em>declarative</em>
 * approach of using the @{@link JmsListener} annotation.
 *
 * <p>See @{@link EnableJms} for detailed usage examples.
 * <p>
 *     可由Spring托管bean实现的可选接口，该bean愿意自定义如何配置JMS侦听器端点。
 *     通常用于定义要使用的默认JmsListenerContainerFactory或以编程方式注册JMS端点，而不是使用@JmsListener注解的声明方法。
 * <p>
 *      * 有关详细用法示例，请参阅@EnableJms。
 * <p>
 *
 *
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see EnableJms
 * @see JmsListenerEndpointRegistrar
 */
public interface JmsListenerConfigurer {

	/**
	 * Callback allowing a {@link org.springframework.jms.config.JmsListenerEndpointRegistry
	 * JmsListenerEndpointRegistry} and specific {@link org.springframework.jms.config.JmsListenerEndpoint
	 * JmsListenerEndpoint} instances to be registered against the given
	 * {@link JmsListenerEndpointRegistrar}. The default
	 * {@link org.springframework.jms.config.JmsListenerContainerFactory JmsListenerContainerFactory}
	 * can also be customized.
	 * @param registrar the registrar to be configured
	 */
	void configureJmsListeners(JmsListenerEndpointRegistrar registrar);

}
