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

package org.springframework.context.annotation;

/**
 * Enumerates the various scoped-proxy options.
 *
 * <p>For a more complete discussion of exactly what a scoped proxy is, see the
 * section of the Spring reference documentation entitled '<em>Scoped beans as
 * dependencies</em>'.
 *
 * <p>
 *     枚举各种范围代理选项。
 * </p>
 * <p>
 *     有关stroped代理的确切内容的更完整讨论，请参阅名为“Scoped beans as dependencies”的Spring参考文档部分。
 * </p>
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */
public enum ScopedProxyMode {

	/**
	 * Default typically equals {@link #NO}, unless a different default
	 * has been configured at the component-scan instruction level.
	 * <p>
	 *     除非在component-scan指令级别配置了不同的默认值，否则默认值通常等于NO。
	 * </p>
	 */
	DEFAULT,

	/**
	 * Do not create a scoped proxy.
	 * <p>This proxy-mode is not typically useful when used with a
	 * non-singleton scoped instance, which should favor the use of the
	 * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
	 * is to be used as a dependency.
	 *
	 * <p>
	 *     不要创建范围代理。
	 *     当与非单例作用域实例一起使用时，此代理模式通常不常用，如果要将其用作依赖项，则应优先使用INTERFACES或TARGET_CLASS代理模式。
	 * </p>
	 */
	NO,

	/**
	 * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
	 * the class of the target object.
	 *
	 * <p>
	 *     创建一个JDK动态代理，实现目标对象的类所公开的所有接口。
	 * </p>
	 */
	INTERFACES,

	/**
	 * Create a class-based proxy (uses CGLIB).
	 * <p>
	 *     创建一个基于类的代理（使用CGLIB）。
	 * </p>
	 */
	TARGET_CLASS;

}
