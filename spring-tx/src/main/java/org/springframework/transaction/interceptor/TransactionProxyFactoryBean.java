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

package org.springframework.transaction.interceptor;

import java.util.Properties;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Proxy factory bean for simplified declarative transaction handling.
 * This is a convenient alternative to a standard AOP
 * {@link org.springframework.aop.framework.ProxyFactoryBean}
 * with a separate {@link TransactionInterceptor} definition.
 *
 * <p><strong>HISTORICAL NOTE:</strong> This class was originally designed to cover the
 * typical case of declarative transaction demarcation: namely, wrapping a singleton
 * target object with a transactional proxy, proxying all the interfaces that the target
 * implements. However, in Spring versions 2.0 and beyond, the functionality provided here
 * is superseded by the more convenient {@code tx:} XML namespace. See the <a
 * href="http://bit.ly/qUwvwz">declarative transaction management</a> section of the
 * Spring reference documentation to understand the modern options for managing
 * transactions in Spring applications. For these reasons, <strong>users should favor of
 * the {@code tx:} XML namespace as well as
 * the @{@link org.springframework.transaction.annotation.Transactional Transactional}
 * and @{@link org.springframework.transaction.annotation.EnableTransactionManagement
 * EnableTransactionManagement} annotations.</strong>
 *
 * <p>There are three main properties that need to be specified:
 * <ul>
 * <li>"transactionManager": the {@link PlatformTransactionManager} implementation to use
 * (for example, a {@link org.springframework.transaction.jta.JtaTransactionManager} instance)
 * <li>"target": the target object that a transactional proxy should be created for
 * <li>"transactionAttributes": the transaction attributes (for example, propagation
 * behavior and "readOnly" flag) per target method name (or method name pattern)
 * </ul>
 *
 * <p>If the "transactionManager" property is not set explicitly and this {@link FactoryBean}
 * is running in a {@link ListableBeanFactory}, a single matching bean of type
 * {@link PlatformTransactionManager} will be fetched from the {@link BeanFactory}.
 *
 * <p>In contrast to {@link TransactionInterceptor}, the transaction attributes are
 * specified as properties, with method names as keys and transaction attribute
 * descriptors as values. Method names are always applied to the target class.
 *
 * <p>Internally, a {@link TransactionInterceptor} instance is used, but the user of this
 * class does not have to care. Optionally, a method pointcut can be specified
 * to cause conditional invocation of the underlying {@link TransactionInterceptor}.
 *
 * <p>The "preInterceptors" and "postInterceptors" properties can be set to add
 * additional interceptors to the mix, like
 * {@link org.springframework.aop.interceptor.PerformanceMonitorInterceptor}.
 *
 * <p><b>HINT:</b> This class is often used with parent / child bean definitions.
 * Typically, you will define the transaction manager and default transaction
 * attributes (for method name patterns) in an abstract parent bean definition,
 * deriving concrete child bean definitions for specific target objects.
 * This reduces the per-bean definition effort to a minimum.
 *
 * <pre code="class">
 * {@code
 * <bean id="baseTransactionProxy" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"
 *     abstract="true">
 *   <property name="transactionManager" ref="transactionManager"/>
 *   <property name="transactionAttributes">
 *     <props>
 *       <prop key="insert*">PROPAGATION_REQUIRED</prop>
 *       <prop key="update*">PROPAGATION_REQUIRED</prop>
 *       <prop key="*">PROPAGATION_REQUIRED,readOnly</prop>
 *     </props>
 *   </property>
 * </bean>
 *
 * <bean id="myProxy" parent="baseTransactionProxy">
 *   <property name="target" ref="myTarget"/>
 * </bean>
 *
 * <bean id="yourProxy" parent="baseTransactionProxy">
 *   <property name="target" ref="yourTarget"/>
 * </bean>}</pre>
 *
 * <p>
 *     代理工厂bean用于简化声明式事务处理。这是标准AOP org.springframework.aop.framework.ProxyFactoryBean的一个方便的替代方法，它具有单独的TransactionInterceptor定义。
 * 需要指定三个主要属性：
 * 如果没有显式设置“transactionManager”属性并且此FactoryBean在ListableBeanFactory中运行，则将从BeanFactory中获取类型为PlatformTransactionManager的匹配bean。
 * 与TransactionInterceptor相反，事务属性被指定为属性，方法名称为键，事务属性描述符为值。方法名称始终应用于目标类。
 * 在内部，使用了TransactionInterceptor实例，但此类的用户不必关心。可选地，可以指定方法切入点以引起对底层TransactionInterceptor的条件调用。
 * 可以设置“preInterceptors”和“postInterceptors”属性以向混合中添加其他拦截器，如org.springframework.aop.interceptor.PerformanceMonitorInterceptor。
 * 提示：此类通常与父/子bean定义一起使用。通常，您将在抽象父bean定义中定义事务管理器和缺省事务属性（对于方法名称模式），从而为特定目标对象派生具体的子bean定义。这将每个bean的定义工作量降至最低。
 * </p>
 * <p>
 *      * 历史注释：此类最初设计用于涵盖声明性事务划分的典型情况：即使用事务代理包装单个目标对象，代理目标实现的所有接口。但是，在Spring 2.0及更高版本中，此处提供的功能被更方便的tx：XML命名空间所取代。请参阅Spring参考文档的声明式事务管理部分，以了解用于管理Spring应用程序中的事务的现代选项。出于这些原因，用户应该支持tx：XML命名空间以及@Transactional和@EnableTransactionManagement注释。
 * </p>
 * <ul>
 * <li>" * “transactionManager”：要使用的PlatformTransactionManager实现（例如，org.springframework.transaction.jta.JtaTransactionManager实例）
 * <li> * “target”：应为其创建事务代理的目标对象
 * <li> * “transactionAttributes”：每个目标方法名称（或方法名称模式）的事务属性（例如，传播行为和“readOnly”标志）
 * </ul>
 * <p></p>
 * <p></p>
 * <p></p>
 * <p></p>
 * <p></p>
 * <p></p>
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @author Rod Johnson
 * @author Chris Beams
 * @since 21.08.2003
 * @see #setTransactionManager
 * @see #setTarget
 * @see #setTransactionAttributes
 * @see TransactionInterceptor
 * @see org.springframework.aop.framework.ProxyFactoryBean
 */
@SuppressWarnings("serial")
public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean
		implements BeanFactoryAware {

	private final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();

	private Pointcut pointcut;


	/**
	 * Set the default transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 * @see TransactionInterceptor#setTransactionManager
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionInterceptor.setTransactionManager(transactionManager);
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 * @see #setTransactionAttributeSource
	 * @see TransactionInterceptor#setTransactionAttributes
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionInterceptor.setTransactionAttributes(transactionAttributes);
	}

	/**
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 * @see #setTransactionAttributes
	 * @see TransactionInterceptor#setTransactionAttributeSource
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource);
	}

	/**
	 * Set a pointcut, i.e a bean that can cause conditional invocation
	 * of the TransactionInterceptor depending on method and attributes passed.
	 * Note: Additional interceptors are always invoked.
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * This callback is optional: If running in a BeanFactory and no transaction
	 * manager has been set explicitly, a single matching bean of type
	 * {@link PlatformTransactionManager} will be fetched from the BeanFactory.
	 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
	 * @see org.springframework.transaction.PlatformTransactionManager
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.transactionInterceptor.setBeanFactory(beanFactory);
	}


	/**
	 * Creates an advisor for this FactoryBean's TransactionInterceptor.
	 */
	@Override
	protected Object createMainInterceptor() {
		this.transactionInterceptor.afterPropertiesSet();
		if (this.pointcut != null) {
			return new DefaultPointcutAdvisor(this.pointcut, this.transactionInterceptor);
		}
		else {
			// Rely on default pointcut.
			return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);
		}
	}

	/**
	 * As of 4.2, this method adds {@link TransactionalProxy} to the set of
	 * proxy interfaces in order to avoid re-processing of transaction metadata.
	 * <p>
	 *     从4.2开始，此方法将{@link TransactionalProxy}添加到代理接口集，以避免重新处理事务元数据
	 * </p>
	 */
	@Override
	protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
		proxyFactory.addInterface(TransactionalProxy.class);
	}

}
