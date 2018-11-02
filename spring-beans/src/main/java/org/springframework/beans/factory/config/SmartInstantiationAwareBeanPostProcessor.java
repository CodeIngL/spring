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

package org.springframework.beans.factory.config;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;

/**
 * Extension of the {@link InstantiationAwareBeanPostProcessor} interface,
 * adding a callback for predicting the eventual type of a processed bean.
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. In general, application-provided
 * post-processors should simply implement the plain {@link BeanPostProcessor}
 * interface or derive from the {@link InstantiationAwareBeanPostProcessorAdapter}
 * class. New methods might be added to this interface even in point releases.
 *
 * <p>
 *     扩展InstantiationAwareBeanPostProcessor接口，添加一个回调以预测已处理bean的最终类型。
 * </p>
 * <p>
 *     注意：此接口是一个专用接口，主要供框架内部使用。
 *     通常，应用程序提供的后处理器应该只实现普通的BeanPostProcessor接口，或者从InstantiationAwareBeanPostProcessorAdapter类派生。 即使在点发行版中，也可以向此接口添加新方法。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see InstantiationAwareBeanPostProcessorAdapter
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/**
	 * Predict the type of the bean to be eventually returned from this
	 * processor's {@link #postProcessBeforeInstantiation} callback.
	 *
	 * <p>
	 *     预测最终从此处理器的postProcessBeforeInstantiation回调返回的bean的类型。
	 * </p>
	 * @param beanClass the raw class of the bean
	 * @param beanName the name of the bean
	 * @return the type of the bean, or {@code null} if not predictable
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException;

	/**
	 * Determine the candidate constructors to use for the given bean.
	 *
	 * <p>
	 *     确定要用于给定bean的候选构造函数。
	 * </p>
	 *
	 * @param beanClass the raw class of the bean (never {@code null})
	 * @param beanName the name of the bean
	 * @return the candidate constructors, or {@code null} if none specified
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException;

	/**
	 * Obtain a reference for early access to the specified bean,
	 * typically for the purpose of resolving a circular reference.
	 * <p>This callback gives post-processors a chance to expose a wrapper
	 * early - that is, before the target bean instance is fully initialized.
	 * The exposed object should be equivalent to the what
	 * {@link #postProcessBeforeInitialization} / {@link #postProcessAfterInitialization}
	 * would expose otherwise. Note that the object returned by this method will
	 * be used as bean reference unless the post-processor returns a different
	 * wrapper from said post-process callbacks. In other words: Those post-process
	 * callbacks may either eventually expose the same reference or alternatively
	 * return the raw bean instance from those subsequent callbacks (if the wrapper
	 * for the affected bean has been built for a call to this method already,
	 * it will be exposes as final bean reference by default).
	 * <p>
	 *     获取早期访问指定bean的引用，通常用于解析循环引用。
	 * 这个回调使后处理器有机会尽早公开包装器 - 也就是说，在目标bean实例完全初始化之前。
	 * 暴露的对象应该等同于postProcessBeforeInitialization / postProcessAfterInitialization否则将公开的对象。
	 * 请注意，此方法返回的对象将用作bean引用，除非后处理器从所述后处理回调中返回不同的包装器。
	 * 换句话说：那些后处理回调可能最终公开相同的引用，或者从后续回调中返回原始bean实例（如果受影响的bean的包装器已经构建用于调用此方法，那么它将被暴露 默认为最终bean引用）。
	 * </p>
	 *
	 * @param bean the raw bean instance
	 * @param beanName the name of the bean
	 * @return the object to expose as bean reference
	 * (typically with the passed-in bean instance as default)
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object getEarlyBeanReference(Object bean, String beanName) throws BeansException;

}
