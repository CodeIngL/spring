/*
 * Copyright 2002-2008 the original author or authors.
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

/**
 * Simple factory facade for obtaining {@link PropertyAccessor} instances,
 * in particular for {@link BeanWrapper} instances. Conceals the actual
 * target implementation classes and their extended public signature.
 *
 * <p>
 *     用于获取PropertyAccessor实例的简单工厂外观，特别是用于BeanWrapper实例。
 *     隐藏实际的目标实施类别及其扩展的公共签名。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public abstract class PropertyAccessorFactory {

	/**
	 * Obtain a BeanWrapper for the given target object,
	 * accessing properties in JavaBeans style.
	 * <p>
	 *     获取给定目标对象的BeanWrapper，以JavaBeans风格访问属性。
	 * </p>
	 * @param target the target object to wrap
	 * @return the property accessor
	 * @see BeanWrapperImpl
	 */
	public static BeanWrapper forBeanPropertyAccess(Object target) {
		return new BeanWrapperImpl(target);
	}

	/**
	 * Obtain a PropertyAccessor for the given target object,
	 * accessing properties in direct field style.
	 *
	 * <p>
	 *     获取给定目标对象的PropertyAccessor，以直接字段样式访问属性。
	 * </p>
	 * @param target the target object to wrap
	 * @return the property accessor
	 * @see DirectFieldAccessor
	 */
	public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
		return new DirectFieldAccessor(target);
	}

}
