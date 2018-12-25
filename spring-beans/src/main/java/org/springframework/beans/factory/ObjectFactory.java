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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Defines a factory which can return an Object instance
 * (possibly shared or independent) when invoked.
 *
 * <p>This interface is typically used to encapsulate a generic factory which
 * returns a new instance (prototype) of some target object on each invocation.
 *
 * <p>This interface is similar to {@link FactoryBean}, but implementations
 * of the latter are normally meant to be defined as SPI instances in a
 * {@link BeanFactory}, while implementations of this class are normally meant
 * to be fed as an API to other beans (through injection). As such, the
 * {@code getObject()} method has different exception handling behavior.
 *
 *
 *
 * <p>
 *     定义一个可以在调用时返回Object实例（可能是共享或独立）的工厂。
 * </p>
 * <p>
 *     此接口通常用于封装通用工厂，该工厂在每次调用时返回某个目标对象的新实例（原型）。
 * </p>
 * <p>
 *     此接口类似于FactoryBean，但后者的实现通常意味着在BeanFactory中定义为SPI实例，而此类的实现通常意味着作为API提供给其他bean（通过注入）。
 *     因此，getObject（）方法具有不同的异常处理行为
 * </p>
 *
 * @author Colin Sampaleanu
 * @since 1.0.2
 * @see FactoryBean
 */
public interface ObjectFactory<T> {

	/**
	 * Return an instance (possibly shared or independent)
	 * of the object managed by this factory.
	 * @return an instance of the bean (should never be {@code null})
	 * @throws BeansException in case of creation errors
	 */
	T getObject() throws BeansException;

}
