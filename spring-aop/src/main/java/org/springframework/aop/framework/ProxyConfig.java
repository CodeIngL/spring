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

package org.springframework.aop.framework;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 * <p>
 *     用于创建代理的配置的便捷超类，以确保所有代理创建者具有一致的属性。
 * </p>
 * 配置类
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

    /**
     * use serialVersionUID from Spring 1.2 FOR interoperability
     */
    private static final long serialVersionUID = -8409359707199703185L;


    private boolean proxyTargetClass = false;

    private boolean optimize = false;

    boolean opaque = false;

    boolean exposeProxy = false;

    private boolean frozen = false;


    /**
     * Set whether to proxy the target class directly, instead of just proxying
     * specific interfaces. Default is "false".
     * <p>Set this to "true" to force proxying for the TargetSource's exposed
     * target class. If that target class is an interface, a JDK proxy will be
     * created for the given interface. If that target class is any other class,
     * a CGLIB proxy will be created for the given class.
     * <p>Note: Depending on the configuration of the concrete proxy factory,
     * the proxy-target-class behavior will also be applied if no interfaces
     * have been specified (and no interface autodetection is activated).
     * <p>
     * 设置是否直接代理目标类，而不是仅代理特定的接口。 默认为“false”。
     * </p>
     * <p>
     * 将其设置为“true”以强制代理TargetSource的公开目标类。
     * 如果该目标类是接口，则将为给定接口创建JDK代理。
     * 如果该目标类是任何其他类，则将为给定类创建CGLIB代理。
     * </p>
     * <p>
     * 注意：根据具体代理工厂的配置，如果未指定任何接口，则也将应用proxy-target-class行为（并且未激活任何接口自动检测）
     * </p>
     *
     * @see org.springframework.aop.TargetSource#getTargetClass()
     */
    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    /**
     * Return whether to proxy the target class directly as well as any interfaces.
     * <p>
     * 返回是否直接代理目标类以及任何接口。
     */
    public boolean isProxyTargetClass() {
        return this.proxyTargetClass;
    }

    /**
     * Set whether proxies should perform aggressive optimizations.
     * The exact meaning of "aggressive optimizations" will differ
     * between proxies, but there is usually some tradeoff.
     * Default is "false".
     * <p>For example, optimization will usually mean that advice changes won't
     * take effect after a proxy has been created. For this reason, optimization
     * is disabled by default. An optimize value of "true" may be ignored
     * if other settings preclude optimization: for example, if "exposeProxy"
     * is set to "true" and that's not compatible with the optimization.
     * <p>
     * 设置代理是否应执行积极优化。 代理之间“积极优化”的确切含义会有所不同，但通常会有一些权衡。 默认为“false”。
     * </p>
     * <p>
     * 例如，optimize通常意味着在创建代理后，advice更改不会生效。 因此，默认情况下禁用optimize。
     * 如果其他设置预先排除了optimize，则可以忽略optimize=“true”：例如，如果“exposeProxy”设置为“true”并且与optimize不兼容
     * </p>
     */
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /**
     * Return whether proxies should perform aggressive optimizations.
     */
    public boolean isOptimize() {
        return this.optimize;
    }

    /**
     * Set whether proxies created by this configuration should be prevented
     * from being cast to {@link Advised} to query proxy status.
     * <p>Default is "false", meaning that any AOP proxy can be cast to
     * {@link Advised}.
     * <p>
     * 设置是否应阻止将此配置创建的代理强制转换为 {@link Advised}查询代理状态。
     * <p>
     * 默认值为“false”，表示任何AOP代理都可以转换为{@link Advised}
     */
    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    /**
     * Return whether proxies created by this configuration should be
     * prevented from being cast to {@link Advised}.
     */
    public boolean isOpaque() {
        return this.opaque;
    }

    /**
     * Set whether the proxy should be exposed by the AOP framework as a
     * ThreadLocal for retrieval via the AopContext class. This is useful
     * if an advised object needs to call another advised method on itself.
     * (If it uses {@code this}, the invocation will not be advised).
     * <p>Default is "false", in order to avoid unnecessary extra interception.
     * This means that no guarantees are provided that AopContext access will
     * work consistently within any method of the advised object.
     * <p>
     * 设置选项:代理是否应该由AOP框架作为ThreadLocal公开，以便通过AopContext类进行检索。 如果advised object需要调用自己的另一个advised method，这将非常有用。
     * （如果使用this，则不会进行advised的方法的调用）。
     * </p>
     * <p>
     * 默认为“false”，以避免不必要的额外拦截。 这意味着不保证AopContext访问将在advised object的任何方法中一致地工作。
     * </p>
     */
    public void setExposeProxy(boolean exposeProxy) {
        this.exposeProxy = exposeProxy;
    }

    /**
     * Return whether the AOP proxy will expose the AOP proxy for
     * each invocation.
     */
    public boolean isExposeProxy() {
        return this.exposeProxy;
    }

    /**
     * Set whether this config should be frozen.
     * <p>When a config is frozen, no advice changes can be made. This is
     * useful for optimization, and useful when we don't want callers to
     * be able to manipulate configuration after casting to Advised.
     * <p>
     * 设置选项:是否应冻结此配置。
     * </p>
     * <p>
     * 当配置被冻结时，不能进行任何advice更改。 这对于optimization很有用，当我们不希望调用者在转换为Advised之后能够操作配置时非常有用
     * </p>
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * Return whether the config is frozen, and no advice changes can be made.
     * <p>
     * 返回是否冻结配置，并且不能进行任何建议更改。
     * </p>
     */
    public boolean isFrozen() {
        return this.frozen;
    }


    /**
     * Copy configuration from the other config object.
     *
     * @param other object to copy configuration from
     */
    public void copyFrom(ProxyConfig other) {
        Assert.notNull(other, "Other ProxyConfig object must not be null");
        this.proxyTargetClass = other.proxyTargetClass;
        this.optimize = other.optimize;
        this.exposeProxy = other.exposeProxy;
        this.frozen = other.frozen;
        this.opaque = other.opaque;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
        sb.append("optimize=").append(this.optimize).append("; ");
        sb.append("opaque=").append(this.opaque).append("; ");
        sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
        sb.append("frozen=").append(this.frozen);
        return sb.toString();
    }

}
