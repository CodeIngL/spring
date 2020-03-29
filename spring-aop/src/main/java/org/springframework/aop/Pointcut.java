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

package org.springframework.aop;

/**
 * Core Spring pointcut abstraction.
 *
 * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations
 * (e.g. through {@link org.springframework.aop.support.ComposablePointcut}).
 * <p>
 *     核心Spring切入点抽象。
 * </p>
 * <p>
 *     切入点由ClassFilter和MethodMatcher组成。
 * </p>
 * <p>
 *    这些基本术语和Pointcut本身都可以组合起来构建组合(例如通过{@link org.springframework.aop.support.ComposablePointcut})。
 * </p>
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {

	/**
	 * Return the ClassFilter for this pointcut.
	 * <p>
	 *     返回此pointcut的ClassFilter。
	 * </p>
	 * @return the ClassFilter (never {@code null})
	 */
	ClassFilter getClassFilter();

	/**
	 * Return the MethodMatcher for this pointcut.
	 * <p>
	 *     返回此pointcut的MethodMatcher。
	 * </p>
	 * @return the MethodMatcher (never {@code null})
	 */
	MethodMatcher getMethodMatcher();


	/**
	 *  anonical Pointcut instance that always matches.
	 *  <p>
	 *      非常规的Pointcut实例标识始终匹配。
	 *  </p>
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}
