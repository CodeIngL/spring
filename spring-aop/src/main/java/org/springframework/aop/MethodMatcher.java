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
 *     {@link Pointcut}��һ���֣����Ŀ�귽���Ƿ���Ͻ����������
 * ���Ծ�̬�ػ�������ʱ����̬�أ�����MethodMatcher����̬ƥ���漰�����ͣ����ܣ��������ԡ�
 * ��̬ƥ�仹ʹ�ض����õĲ������ã��Լ�����Ӧ�������ӵ����ǰ������κ�Ч����
 * </p>
 * <p>
 *  ���ʵ�ִ���{@link #isRuntime()}��������{@code false}������Ծ�ִ̬����ֵ�����Ҷ��ڴ˷��������е��ã������������Σ����������ͬ�ġ�
 *  ����ζ�����{@link #isRuntime()}��������false������Զ�������3-argƥ��{@link #matches(java.lang.reflect.Method, Class, Object[])} ������
 * </p>
 * <p>
 *  ���ʵ�ִ���2-argƥ��{@link #matches(java.lang.reflect.Method, Class)}��������true������ {@link #isRuntime()}��������true��
 *  ��3-argƥ�� {@link #matches(java.lang.reflect.Method, Class, Object[])}��������ÿ��Ǳ��ִ��֮ǰ����������ؽ��飬�Ծ��������Ƿ�Ӧ�����С�֮ǰ�����н��飨�������������е��������������������У����������ʱ����ʹ�ò�����ThreadLocal״̬���ɵ��κ�״̬���ġ�
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
     *     ִ�о�̬�����������Ƿ�ƥ�䡣
     *     �������false���� {@link #isRuntime()}��������false���򲻻��������ʱ��飨����ƥ��{@link #matches(java.lang.reflect.Method, Class, Object[])}���ã�
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
	 *     ���MethodMatcher�Ƿ��Ƕ�̬�ģ���������������ʱ��ƥ��{@link #matches(java.lang.reflect.Method, Class, Object[])}�����������յ��ã���ʹ2-arg matches��������trueҲ����ˣ�
	 * </p>
	 * <p>
	 * �����ڴ���AOP����ʱ���ã�����������ÿ�η�������֮ǰ�ٴε���
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
     *     ���˷����Ƿ��������ʱ����̬��ƥ�䣬�÷������뾲̬ƥ�䡣
     * ����2-arg matches����Ϊ����������Ŀ���෵��true������isRuntime������������trueʱ���Ż���ô˷�����
     * �ڽ������е��κν�������֮����Ǳ�����н���֮ǰ�������á�
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
