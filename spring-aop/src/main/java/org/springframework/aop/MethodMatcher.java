/*
 * Copyright 2002-2015 the original author or authors.
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

import java.lang.reflect.Method;

/**
 * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.
 *
 * <p>A MethodMatcher may be evaluated <b>statically</b> or at <b>runtime</b> (dynamically).
 * Static matching involves method and (possibly) method attributes. Dynamic matching
 * also makes arguments for a particular call available, and any effects of running
 * previous advice applying to the joinpoint.
 *
 * <p>If an implementation returns {@code false} from its {@link #isRuntime()}
 * method, evaluation can be performed statically, and the result will be the same
 * for all invocations of this method, whatever their arguments. This means that
 * if the {@link #isRuntime()} method returns {@code false}, the 3-arg
 * {@link #matches(java.lang.reflect.Method, Class, Object[])} method will never be invoked.
 *
 * <p>If an implementation returns {@code true} from its 2-arg
 * {@link #matches(java.lang.reflect.Method, Class)} method and its {@link #isRuntime()} method
 * returns {@code true}, the 3-arg {@link #matches(java.lang.reflect.Method, Class, Object[])}
 * method will be invoked <i>immediately before each potential execution of the related advice</i>,
 * to decide whether the advice should run. All previous advice, such as earlier interceptors
 * in an interceptor chain, will have run, so any state changes they have produced in
 * parameters or ThreadLocal state will be available at the time of evaluation.
 *
 * <p>
 *     {@link Pointcut}的一部分：检查目标方法是否符合建议的条件。
 * 可以静态地或在运行时（动态地）评估MethodMatcher。静态匹配涉及方法和（可能）方法属性。
 * 动态匹配还使特定调用的参数可用，以及运行应用于连接点的先前建议的任何效果。
 * </p>
 * <p>
 *  如果实现从其{@link #isRuntime()}方法返回{@code false}，则可以静态执行求值，并且对于此方法的所有调用，无论其参数如何，结果都是相同的。
 *  这意味着如果{@link #isRuntime()}方法返回false，则永远不会调用3-arg匹配{@link #matches(java.lang.reflect.Method, Class, Object[])} 方法。
 * </p>
 * <p>
 *  如果实现从其2-arg匹配{@link #matches(java.lang.reflect.Method, Class)}方法返回true并且其 {@link #isRuntime()}方法返回true，
 *  则3-arg匹配 {@link #matches(java.lang.reflect.Method, Class, Object[])}方法将在每次潜在执行之前立即调用相关建议，以决定建议是否应该运行。之前的所有建议（例如拦截器链中的早期拦截器）都将运行，因此在评估时可以使用参数或ThreadLocal状态生成的任何状态更改。
 * </p>
 *
 * @author Rod Johnson
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

	/**
	 * Perform static checking whether the given method matches. If this
	 * returns {@code false} or if the {@link #isRuntime()} method
	 * returns {@code false}, no runtime check (i.e. no.
	 * {@link #matches(java.lang.reflect.Method, Class, Object[])} call) will be made.
     * <p>
     *     执行静态检查给定方法是否匹配。
     *     如果返回false或者 {@link #isRuntime()}方法返回false，则不会进行运行时检查（即不匹配{@link #matches(java.lang.reflect.Method, Class, Object[])}调用）
     * </p>
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @return whether or not this method matches statically
	 */
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * Is this MethodMatcher dynamic, that is, must a final call be made on the
	 * {@link #matches(java.lang.reflect.Method, Class, Object[])} method at
	 * runtime even if the 2-arg matches method returns {@code true}?
	 * <p>Can be invoked when an AOP proxy is created, and need not be invoked
	 * again before each method invocation,
	 * <p>
	 *     这个MethodMatcher是否是动态的，即，必须在运行时对匹配{@link #matches(java.lang.reflect.Method, Class, Object[])}方法进行最终调用，即使2-arg matches方法返回true也是如此？
	 * </p>
	 * <p>
	 * 可以在创建AOP代理时调用，并且无需在每次方法调用之前再次调用
	 * </p>
	 * @return whether or not a runtime match via the 3-arg
	 * {@link #matches(java.lang.reflect.Method, Class, Object[])} method
	 * is required if static matching passed
	 */
	boolean isRuntime();

	/**
	 * Check whether there a runtime (dynamic) match for this method,
	 * which must have matched statically.
	 * <p>This method is invoked only if the 2-arg matches method returns
	 * {@code true} for the given method and target class, and if the
	 * {@link #isRuntime()} method returns {@code true}. Invoked
	 * immediately before potential running of the advice, after any
	 * advice earlier in the advice chain has run.
     * <p>
     *     检查此方法是否存在运行时（动态）匹配，该方法必须静态匹配。
     * 仅当2-arg matches方法为给定方法和目标类返回true，并且isRuntime（）方法返回true时，才会调用此方法。
     * 在建议链中的任何建议运行之后，在潜在运行建议之前立即调用。
     * </p>
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @param args arguments to the method
	 * @return whether there's a runtime match
	 * @see MethodMatcher#matches(Method, Class)
	 */
	boolean matches(Method method, Class<?> targetClass, Object... args);


	/**
	 * Canonical instance that matches all methods.
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
