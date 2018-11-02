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

package org.springframework.context;

/**
 * A common interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 * <b>NOTE: This interface does not imply specific auto-startup semantics.
 * Consider implementing {@link SmartLifecycle} for that purpose.</b>
 *
 * <p>Can be implemented by both components (typically a Spring bean defined in a
 * Spring context) and containers  (typically a Spring {@link ApplicationContext}
 * itself). Containers will propagate start/stop signals to all components that
 * apply within each container, e.g. for a stop/restart scenario at runtime.
 *
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 *
 * <p>Note that the Lifecycle interface is only supported on <b>top-level singleton
 * beans</b>. On any other component, the Lifecycle interface will remain undetected
 * and hence ignored. Also, note that the extended {@link SmartLifecycle} interface
 * provides integration with the application context's startup and shutdown phases.
 *
 * <p>
 *     定义启动/停止生命周期控制方法的通用接口。这种情况的典型用例是控制异步处理。
 *     注意：此接口不暗示特定的自动启动语义。考虑为此目的实现SmartLifecycle。
 * </p>
 * <p>
 *      可以由两个组件（通常是Spring上下文中定义的Spring bean）和容器（通常是Spring ApplicationContext本身）实现。
 *      容器将开始/停止信号传播到每个容器内应用的所有组件，
 *      例如，用于运行时的停止/重新启动方案。
 * </p>
 * <p>
 *      可以通过JMX用于直接调用或管理操作。
 *      在后一种情况下，org.springframework.jmx.export.MBeanExporter通常使用org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler定义，
 *      从而限制活动控制组件对Lifecycle接口的可见性。
 * </p>
 * <p>
 *      请注意，仅在顶级单例bean上支持Lifecycle接口。
 *      在任何其他组件上，Lifecycle接口将保持未被检测到，因此将被忽略。另请注意，扩展的SmartLifecycle接口提供了与应用程序上下文的启动和关闭阶段的集成。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
public interface Lifecycle {

	/**
	 * Start this component.
	 * <p>Should not throw an exception if the component is already running.
	 * <p>In the case of a container, this will propagate the start signal to all
	 * components that apply.
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void start();

	/**
	 * Stop this component, typically in a synchronous fashion, such that the component is
	 * fully stopped upon return of this method. Consider implementing {@link SmartLifecycle}
	 * and its {@code stop(Runnable)} variant when asynchronous stop behavior is necessary.
	 * <p>Note that this stop notification is not guaranteed to come before destruction: On
	 * regular shutdown, {@code Lifecycle} beans will first receive a stop notification before
	 * the general destruction callbacks are being propagated; however, on hot refresh during a
	 * context's lifetime or on aborted refresh attempts, only destroy methods will be called.
	 * <p>Should not throw an exception if the component isn't started yet.
	 * <p>In the case of a container, this will propagate the stop signal to all components
	 * that apply.
	 * @see SmartLifecycle#stop(Runnable)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	void stop();

	/**
	 * Check whether this component is currently running.
	 * <p>In the case of a container, this will return {@code true} only if <i>all</i>
	 * components that apply are currently running.
	 * @return whether the component is currently running
	 */
	boolean isRunning();

}
