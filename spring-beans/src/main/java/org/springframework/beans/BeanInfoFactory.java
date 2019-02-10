/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

/**
 * Strategy interface for creating {@link BeanInfo} instances for Spring beans.
 * Can be used to plug in custom bean property resolution strategies (e.g. for other
 * languages on the JVM) or more efficient {@link BeanInfo} retrieval algorithms.
 *
 * <p>BeanInfoFactories are instantiated by the {@link CachedIntrospectionResults},
 * by using the {@link org.springframework.core.io.support.SpringFactoriesLoader}
 * utility class.
 *
 * When a {@link BeanInfo} is to be created, the {@code CachedIntrospectionResults}
 * will iterate through the discovered factories, calling {@link #getBeanInfo(Class)}
 * on each one. If {@code null} is returned, the next factory will be queried.
 * If none of the factories support the class, a standard {@link BeanInfo} will be
 * created as a default.
 *
 * <p>Note that the {@link org.springframework.core.io.support.SpringFactoriesLoader}
 * sorts the {@code BeanInfoFactory} instances by
 * {@link org.springframework.core.annotation.Order @Order}, so that ones with a
 * higher precedence come first.
 *
 * <p>
 *     公共接口BeanInfoFactory
 * </p>
 *
 * <p>
 *  用于为Spring bean创建BeanInfo实例的策略接口。 可用于插入自定义bean属性解析策略（例如，对于JVM上的其他语言）或更高效的BeanInfo检索算法。
 * </p>
 * <p>
 *      BeanInfoFactories由CachedIntrospectionResults实例化，使用org.springframework.core.io.support.SpringFactoriesLoader实用程序类。 当要创建BeanInfo时，CachedIntrospectionResults将遍历已发现的工厂，并在每个工厂上调用getBeanInfo（Class）。 如果返回null，则将查询下一个工厂。 如果没有工厂支持该类，则将创建标准BeanInfo作为默认值。
 * </p></P>
 * <p>
 *      请注意，org.springframework.core.io.support.SpringFactoriesLoader按@Order对BeanInfoFactory实例进行排序，以便优先级较高的实例排在第一位。
 * </p></P>
 *
 * @author Arjen Poutsma
 * @since 3.2
 * @see CachedIntrospectionResults
 * @see org.springframework.core.io.support.SpringFactoriesLoader
 */
public interface BeanInfoFactory {

	/**
	 * Return the bean info for the given class, if supported.
	 * @param beanClass the bean class
	 * @return the BeanInfo, or {@code null} if the given class is not supported
	 * @throws IntrospectionException in case of exceptions
	 */
	BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;

}
