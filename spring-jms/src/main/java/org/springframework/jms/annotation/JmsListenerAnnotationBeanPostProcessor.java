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

package org.springframework.jms.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.jms.config.JmsListenerConfigUtils;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.MethodJmsListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * Bean post-processor that registers methods annotated with {@link JmsListener}
 * to be invoked by a JMS message listener container created under the cover
 * by a {@link org.springframework.jms.config.JmsListenerContainerFactory}
 * according to the attributes of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link JmsListener}.
 *
 * <p>This post-processor is automatically registered by Spring's
 * {@code <jms:annotation-driven>} XML element, and also by the {@link EnableJms}
 * annotation.
 *
 * <p>Autodetects any {@link JmsListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container
 * factory or for fine-grained control over endpoints registration. See the
 * {@link EnableJms} javadocs for complete usage details.
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 * @see JmsListener
 * @see EnableJms
 * @see JmsListenerConfigurer
 * @see JmsListenerEndpointRegistrar
 * @see JmsListenerEndpointRegistry
 * @see org.springframework.jms.config.JmsListenerEndpoint
 * @see MethodJmsListenerEndpoint
 */
public class JmsListenerAnnotationBeanPostProcessor
		implements MergedBeanDefinitionPostProcessor, Ordered, BeanFactoryAware, SmartInitializingSingleton {

	/**
	 * The bean name of the default {@link JmsListenerContainerFactory}.
	 */
	static final String DEFAULT_JMS_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "jmsListenerContainerFactory";


	protected final Log logger = LogFactory.getLog(getClass());

	private JmsListenerEndpointRegistry endpointRegistry;

	private String containerFactoryBeanName = DEFAULT_JMS_LISTENER_CONTAINER_FACTORY_BEAN_NAME;

	private final MessageHandlerMethodFactoryAdapter messageHandlerMethodFactory =
			new MessageHandlerMethodFactoryAdapter();

	private BeanFactory beanFactory;

	private StringValueResolver embeddedValueResolver;

	private final JmsListenerEndpointRegistrar registrar = new JmsListenerEndpointRegistrar();

	private final AtomicInteger counter = new AtomicInteger();

	private final Set<Class<?>> nonAnnotatedClasses =
			Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>(64));


	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	/**
	 * Set the {@link JmsListenerEndpointRegistry} that will hold the created
	 * endpoint and manage the lifecycle of the related listener container.
	 */
	public void setEndpointRegistry(JmsListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	/**
	 * Set the name of the {@link JmsListenerContainerFactory} to use by default.
	 * <p>If none is specified, "jmsListenerContainerFactory" is assumed to be defined.
	 */
	public void setContainerFactoryBeanName(String containerFactoryBeanName) {
		this.containerFactoryBeanName = containerFactoryBeanName;
	}

	/**
	 * Set the {@link MessageHandlerMethodFactory} to use to configure the message
	 * listener responsible to serve an endpoint detected by this processor.
	 * <p>By default, {@link DefaultMessageHandlerMethodFactory} is used and it
	 * can be configured further to support additional method arguments
	 * or to customize conversion and validation support. See
	 * {@link DefaultMessageHandlerMethodFactory} Javadoc for more details.
	 */
	public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
		this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
	}

	/**
	 * Making a {@link BeanFactory} available is optional; if not set,
	 * {@link JmsListenerConfigurer} beans won't get autodetected and an
	 * {@link #setEndpointRegistry endpoint registry} has to be explicitly configured.
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof ConfigurableBeanFactory) {
			this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
		}
		this.registrar.setBeanFactory(beanFactory);
	}


	@Override
	public void afterSingletonsInstantiated() {
		// Remove resolved singleton classes from cache
		this.nonAnnotatedClasses.clear();

		if (this.beanFactory instanceof ListableBeanFactory) {
			// Apply JmsListenerConfigurer beans from the BeanFactory, if any
			// 如果有的话，从BeanFactory应用JmsListenerConfigurer bean
			Map<String, JmsListenerConfigurer> beans =
					((ListableBeanFactory) this.beanFactory).getBeansOfType(JmsListenerConfigurer.class);
			List<JmsListenerConfigurer> configurers = new ArrayList<JmsListenerConfigurer>(beans.values());
			AnnotationAwareOrderComparator.sort(configurers);
			for (JmsListenerConfigurer configurer : configurers) {
				configurer.configureJmsListeners(this.registrar);
			}
		}

		if (this.registrar.getEndpointRegistry() == null) {
			// Determine JmsListenerEndpointRegistry bean from the BeanFactory
			if (this.endpointRegistry == null) {
				Assert.state(this.beanFactory != null, "BeanFactory must be set to find endpoint registry by bean name");
				this.endpointRegistry = this.beanFactory.getBean(
						JmsListenerConfigUtils.JMS_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, JmsListenerEndpointRegistry.class);
			}
			this.registrar.setEndpointRegistry(this.endpointRegistry);
		}

		if (this.containerFactoryBeanName != null) {
			this.registrar.setContainerFactoryBeanName(this.containerFactoryBeanName);
		}

		// Set the custom handler method factory once resolved by the configurer
		MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
		if (handlerMethodFactory != null) {
			this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(handlerMethodFactory);
		}

		// Actually register all listeners
		this.registrar.afterPropertiesSet();
	}


	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 处理JmsListener,构建端点
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		//如果一个类不存在相关的注解被处理过了。下一次会直接跳过，因为我们知道这个类是不在拥有这个注解
		if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
			//获得目标类
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			//选择相关的方法
			Map<Method, Set<JmsListener>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
					new MethodIntrospector.MetadataLookup<Set<JmsListener>>() {
						@Override
						public Set<JmsListener> inspect(Method method) {
							Set<JmsListener> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
									method, JmsListener.class, JmsListeners.class);
							return (!listenerMethods.isEmpty() ? listenerMethods : null);
						}
					});
			if (annotatedMethods.isEmpty()) {
				//不存在，我们添加这个类到相关的集合里面
				this.nonAnnotatedClasses.add(bean.getClass());
				if (logger.isTraceEnabled()) {
					logger.trace("No @JmsListener annotations found on bean type: " + bean.getClass());
				}
			}
			else {
				// Non-empty set of methods
				//存在相关的方法
				for (Map.Entry<Method, Set<JmsListener>> entry : annotatedMethods.entrySet()) {
					Method method = entry.getKey();
					for (JmsListener listener : entry.getValue()) {
						//让我们一个个处理注解和携带他的方法，我们使用这个bean来进行处理哦
						processJmsListener(listener, method, bean);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @JmsListener methods processed on bean '" + beanName +
							"': " + annotatedMethods);
				}
			}
		}
		return bean;
	}

	/**
	 * Process the given {@link JmsListener} annotation on the given method,
	 * registering a corresponding endpoint for the given bean instance.
	 * <p>
	 *     在给定方法上处理给定的JmsListener注解，为给定的bean实例注册相应的端点。
	 * @param jmsListener the annotation to process 待处理的注解
	 * @param mostSpecificMethod the annotated method 指定的方法
	 * @param bean the instance to invoke the method on 实际bean
	 * @see #createMethodJmsListenerEndpoint()
	 * @see JmsListenerEndpointRegistrar#registerEndpoint
	 */
	protected void processJmsListener(JmsListener jmsListener, Method mostSpecificMethod, Object bean) {
		Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());

		/**
		 * 方法级暴露出来的endpoint
		 */
		MethodJmsListenerEndpoint endpoint = createMethodJmsListenerEndpoint();
		endpoint.setBean(bean);
		endpoint.setMethod(invocableMethod);
		endpoint.setMostSpecificMethod(mostSpecificMethod);
		endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
		endpoint.setEmbeddedValueResolver(this.embeddedValueResolver);
		endpoint.setBeanFactory(this.beanFactory);
		endpoint.setId(getEndpointId(jmsListener));
		//监听的目标
		endpoint.setDestination(resolve(jmsListener.destination()));
		//jmsSelector存在
		if (StringUtils.hasText(jmsListener.selector())) {
			//设置Selector
			endpoint.setSelector(resolve(jmsListener.selector()));
		}
		//持久化订阅
		if (StringUtils.hasText(jmsListener.subscription())) {
			endpoint.setSubscription(resolve(jmsListener.subscription()));
		}
		//并发限制
		if (StringUtils.hasText(jmsListener.concurrency())) {
			endpoint.setConcurrency(resolve(jmsListener.concurrency()));
		}

		//构建factory
		JmsListenerContainerFactory<?> factory = null;
		//获得factoryBeanName的名字
		String containerFactoryBeanName = resolve(jmsListener.containerFactory());
		if (StringUtils.hasText(containerFactoryBeanName)) {
			Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
			try {
				factory = this.beanFactory.getBean(containerFactoryBeanName, JmsListenerContainerFactory.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				throw new BeanInitializationException("Could not register JMS listener endpoint on [" +
						mostSpecificMethod + "], no " + JmsListenerContainerFactory.class.getSimpleName() +
						" with id '" + containerFactoryBeanName + "' was found in the application context", ex);
			}
		}

		//由registrar进行注册
		this.registrar.registerEndpoint(endpoint, factory);
	}

	/**
	 * Instantiate an empty {@link MethodJmsListenerEndpoint} for further
	 * configuration with provided parameters in {@link #processJmsListener}.
	 * @return a new {@code MethodJmsListenerEndpoint} or subclass thereof
	 * @since 4.1.9
	 * @see MethodJmsListenerEndpoint#createMessageListenerInstance()
	 */
	protected MethodJmsListenerEndpoint createMethodJmsListenerEndpoint() {
		return new MethodJmsListenerEndpoint();
	}

	private String getEndpointId(JmsListener jmsListener) {
		if (StringUtils.hasText(jmsListener.id())) {
			return resolve(jmsListener.id());
		}
		else {
			return "org.springframework.jms.JmsListenerEndpointContainer#" + this.counter.getAndIncrement();
		}
	}

	private String resolve(String value) {
		return (this.embeddedValueResolver != null ? this.embeddedValueResolver.resolveStringValue(value) : value);
	}


	/**
	 * A {@link MessageHandlerMethodFactory} adapter that offers a configurable underlying
	 * instance to use. Useful if the factory to use is determined once the endpoints
	 * have been registered but not created yet.
	 * @see JmsListenerEndpointRegistrar#setMessageHandlerMethodFactory
	 */
	private class MessageHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

		private MessageHandlerMethodFactory messageHandlerMethodFactory;

		public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
			this.messageHandlerMethodFactory = messageHandlerMethodFactory;
		}

		@Override
		public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
			return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
		}

		private MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
			if (this.messageHandlerMethodFactory == null) {
				this.messageHandlerMethodFactory = createDefaultJmsHandlerMethodFactory();
			}
			return this.messageHandlerMethodFactory;
		}

		private MessageHandlerMethodFactory createDefaultJmsHandlerMethodFactory() {
			DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
			defaultFactory.setBeanFactory(beanFactory);
			defaultFactory.afterPropertiesSet();
			return defaultFactory;
		}
	}

}
