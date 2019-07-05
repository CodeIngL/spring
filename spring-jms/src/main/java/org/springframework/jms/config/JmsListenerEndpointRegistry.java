/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.jms.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.util.Assert;

/**
 * Creates the necessary {@link MessageListenerContainer} instances for the
 * registered {@linkplain JmsListenerEndpoint endpoints}. Also manages the
 * lifecycle of the listener containers, in particular within the lifecycle
 * of the application context.
 *
 * <p>Contrary to {@link MessageListenerContainer}s created manually, listener
 * containers managed by registry are not beans in the application context and
 * are not candidates for autowiring. Use {@link #getListenerContainers()} if
 * you need to access this registry's listener containers for management purposes.
 * If you need to access to a specific message listener container, use
 * {@link #getListenerContainer(String)} with the id of the endpoint.
 *
 * <p>
 * 为已注册的{@linkplain JmsListenerEndpoint endpoints}创建必要的{@link MessageListenerContainer}实例。
 * 还管理侦听器容器的生命周期，特别是在应用程序上下文的生命周期内。
 * </p>
 * <p>
 * 与手动创建的{@link MessageListenerContainer}相反，由注册表管理的侦听器容器不是应用程序上下文中的bean，也不是自动装配的候选者。
 * 如果您需要访问此注册表的侦听器容器以进行管理，请使用{@link #getListenerContainers()} 。
 * 如果需要访问特定的消息侦听器容器，请使用具有端点ID的{@link #getListenerContainer(String)}。
 * </p>
 *
 * <p>
 *     里面的监听端点是不受bean容器管理的
 * </p>
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @see JmsListenerEndpoint
 * @see MessageListenerContainer
 * @see JmsListenerContainerFactory
 * @since 4.1
 */
public class JmsListenerEndpointRegistry implements DisposableBean, SmartLifecycle,
        ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     *
     */
    private final Map<String, MessageListenerContainer> listenerContainers =
            new ConcurrentHashMap<String, MessageListenerContainer>();

    private int phase = Integer.MAX_VALUE;

    private ApplicationContext applicationContext;

    private boolean contextRefreshed;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() == this.applicationContext) {
            this.contextRefreshed = true;
        }
    }


    /**
     * Return the {@link MessageListenerContainer} with the specified id or
     * {@code null} if no such container exists.
     *
     * @param id the id of the container
     * @return the container or {@code null} if no container with that id exists
     * @see JmsListenerEndpoint#getId()
     * @see #getListenerContainerIds()
     */
    public MessageListenerContainer getListenerContainer(String id) {
        Assert.notNull(id, "Container identifier must not be null");
        return this.listenerContainers.get(id);
    }

    /**
     * Return the ids of the managed {@link MessageListenerContainer} instance(s).
     *
     * @see #getListenerContainer(String)
     * @since 4.2.3
     */
    public Set<String> getListenerContainerIds() {
        return Collections.unmodifiableSet(this.listenerContainers.keySet());
    }

    /**
     * Return the managed {@link MessageListenerContainer} instance(s).
     */
    public Collection<MessageListenerContainer> getListenerContainers() {
        return Collections.unmodifiableCollection(this.listenerContainers.values());
    }

    /**
     * Create a message listener container for the given {@link JmsListenerEndpoint}.
     * <p>This create the necessary infrastructure to honor that endpoint
     * with regards to its configuration.
     * <p>The {@code startImmediately} flag determines if the container should be
     * started immediately.
     *
     * <p>
     *     为给定的 {@link JmsListenerEndpoint}创建消息侦听器容器。
     * </p>
     * <p>
     *     这创建了必要的基础架构来支持该端点的配置
     * </p>
     * <p>
     *     {@code startImmediately}标志确定是否应立即启动该监听容器
     * </p>
     *
     * @param endpoint         the endpoint to add
     * @param factory          the listener factory to use
     * @param startImmediately start the container immediately if necessary
     * @see #getListenerContainers()
     * @see #getListenerContainer(String)
     */
    public void registerListenerContainer(JmsListenerEndpoint endpoint, JmsListenerContainerFactory<?> factory,
                                          boolean startImmediately) {

        Assert.notNull(endpoint, "Endpoint must not be null");
        Assert.notNull(factory, "Factory must not be null");

        //endpoint的id
        String id = endpoint.getId();
        Assert.notNull(id, "Endpoint id must not be null");
        synchronized (this.listenerContainers) {
            if (this.listenerContainers.containsKey(id)) {
                throw new IllegalStateException("Another endpoint is already registered with id '" + id + "'");
            }
            //
            MessageListenerContainer container = createListenerContainer(endpoint, factory);
            this.listenerContainers.put(id, container);
            if (startImmediately) { //是否要立即启动
                startIfNecessary(container);
            }
        }
    }

    /**
     * Create a message listener container for the given {@link JmsListenerEndpoint}.
     * <p>This create the necessary infrastructure to honor that endpoint
     * with regards to its configuration.
     * <p>
     * 为给定的JmsListenerEndpoint创建消息侦听器容器。
     * </p>
     * <p>
     * 这创建了必要的基础架构来支持该端点的配置。
     * </p>
     *
     * @param endpoint the endpoint to add
     * @param factory  the listener factory to use
     * @see #registerListenerContainer(JmsListenerEndpoint, JmsListenerContainerFactory, boolean)
     */
    public void registerListenerContainer(JmsListenerEndpoint endpoint, JmsListenerContainerFactory<?> factory) {
        registerListenerContainer(endpoint, factory, false);
    }

    /**
     * Create and start a new container using the specified factory.
     */
    protected MessageListenerContainer createListenerContainer(JmsListenerEndpoint endpoint,
                                                               JmsListenerContainerFactory<?> factory) {

        //使用endpoint来构建消息监听容器，
        MessageListenerContainer listenerContainer = factory.createListenerContainer(endpoint);

        //虽然不受应用上下文管理，但是我们尽量进行一些必要的处理，比如我们尝试调用容器的InitializingBean
        if (listenerContainer instanceof InitializingBean) {
            try {
                ((InitializingBean) listenerContainer).afterPropertiesSet();
            } catch (Exception ex) {
                throw new BeanInitializationException("Failed to initialize message listener container", ex);
            }
        }

        //选取一次
        int containerPhase = listenerContainer.getPhase();
        if (containerPhase < Integer.MAX_VALUE) {  // a custom phase value
            if (this.phase < Integer.MAX_VALUE && this.phase != containerPhase) {
                throw new IllegalStateException("Encountered phase mismatch between container factory definitions: " +
                        this.phase + " vs " + containerPhase);
            }
            this.phase = listenerContainer.getPhase();
        }

        return listenerContainer;
    }


    // Delegating implementation of SmartLifecycle

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            startIfNecessary(listenerContainer);
        }
    }

    @Override
    public void stop() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            listenerContainer.stop();
        }
    }

    @Override
    public void stop(Runnable callback) {
        Collection<MessageListenerContainer> listenerContainers = getListenerContainers();
        AggregatingCallback aggregatingCallback = new AggregatingCallback(listenerContainers.size(), callback);
        for (MessageListenerContainer listenerContainer : listenerContainers) {
            listenerContainer.stop(aggregatingCallback);
        }
    }

    @Override
    public boolean isRunning() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            if (listenerContainer.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start the specified {@link MessageListenerContainer} if it should be started
     * on startup or when start is called explicitly after startup.
     * <p>
     * 如果应该在启动时启动或在启动后显式调用start，则启动指定的MessageListenerContainer。
     *
     * @see MessageListenerContainer#isAutoStartup()
     */
    private void startIfNecessary(MessageListenerContainer listenerContainer) {
        if (this.contextRefreshed || listenerContainer.isAutoStartup()) {
            listenerContainer.start();
        }
    }

    @Override
    public void destroy() {
        for (MessageListenerContainer listenerContainer : getListenerContainers()) {
            if (listenerContainer instanceof DisposableBean) {
                try {
                    ((DisposableBean) listenerContainer).destroy();
                } catch (Throwable ex) {
                    logger.warn("Failed to destroy message listener container", ex);
                }
            }
        }
    }


    private static class AggregatingCallback implements Runnable {

        private final AtomicInteger count;

        private final Runnable finishCallback;

        public AggregatingCallback(int count, Runnable finishCallback) {
            this.count = new AtomicInteger(count);
            this.finishCallback = finishCallback;
        }

        @Override
        public void run() {
            if (this.count.decrementAndGet() == 0) {
                this.finishCallback.run();
            }
        }
    }

}
