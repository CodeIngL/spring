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

package org.springframework.beans.factory;

/**
 * Callback interface triggered at the end of the singleton pre-instantiation phase
 * during {@link BeanFactory} bootstrap. This interface can be implemented by
 * singleton beans in order to perform some initialization after the regular
 * singleton instantiation algorithm, avoiding side effects with accidental early
 * initialization (e.g. from {@link ListableBeanFactory#getBeansOfType} calls).
 * In that sense, it is an alternative to {@link InitializingBean} which gets
 * triggered right at the end of a bean's local construction phase.
 *
 * <p>This callback variant is somewhat similar to
 * {@link org.springframework.context.event.ContextRefreshedEvent} but doesn't
 * require an implementation of {@link org.springframework.context.ApplicationListener},
 * with no need to filter context references across a context hierarchy etc.
 * It also implies a more minimal dependency on just the {@code beans} package
 * and is being honored by standalone {@link ListableBeanFactory} implementations,
 * not just in an {@link org.springframework.context.ApplicationContext} environment.
 *
 * <p><b>NOTE:</b> If you intend to start/manage asynchronous tasks, preferably
 * implement {@link org.springframework.context.Lifecycle} instead which offers
 * a richer model for runtime management and allows for phased startup/shutdown.
 *
 * <p>
 *     在BeanFactory引导期间，在单例预实例化阶段结束时触发回调接口。
 *     该接口可以由单例bean实现，以便在常规单例实例化算法之后执行一些初始化，避免意外早期初始化的副作用（例如，从ListableBeanFactory.getBeansOfType调用）。
 *     从这个意义上说，它是InitializingBean的替代品，它在bean的本地构建阶段结束时被触发。
 * </p>
 * <p>
 *      这个回调变体有点类似于org.springframework.context.event.ContextRefreshedEvent，
 *      但不需要org.springframework.context.ApplicationListener的实现，
 *      不需要跨上下文层次结构等过滤上下文引用。
 *      它还意味着更多对bean包的最小依赖性，并且受到独立的ListableBeanFactory实现的尊重，而不仅仅是在org.springframework.context.ApplicationContext环境中。
 * </p>
 * <p>
 *      注意：如果您打算启动/管理异步任务，最好实现org.springframework.context.Lifecycle，它为运行时管理提供了更丰富的模型，并允许分阶段启动/关闭。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
public interface SmartInitializingSingleton {

	/**
	 * Invoked right at the end of the singleton pre-instantiation phase,
	 * with a guarantee that all regular singleton beans have been created
	 * already. {@link ListableBeanFactory#getBeansOfType} calls within
	 * this method won't trigger accidental side effects during bootstrap.
	 * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
	 * lazily initialized on demand after {@link BeanFactory} bootstrap,
	 * and not for any other bean scope either. Carefully use it for beans
	 * with the intended bootstrap semantics only.
	 *
	 * <p>
	 *     在单例预实例化阶段结束时调用，保证已经创建了所有常规单例bean。
	 *     此方法中的ListableBeanFactory.getBeansOfType调用不会在引导期间触发意外的副作用。
	 * </p>
	 * <p>
	 *     注意：对于在BeanFactory引导程序之后根据需要延迟初始化的单例bean，不会触发此回调，也不会对任何其他bean作用域触发。
	 *     小心地将它用于具有预期引导语义的bean。
	 * </p>
	 */
	void afterSingletonsInstantiated();

}
