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

package org.springframework.transaction.support;

import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;

/**
 * Template class that simplifies programmatic transaction demarcation and
 * transaction exception handling.
 *
 * <p>The central method is {@link #execute}, supporting transactional code that
 * implements the {@link TransactionCallback} interface. This template handles
 * the transaction lifecycle and possible exceptions such that neither the
 * TransactionCallback implementation nor the calling code needs to explicitly
 * handle transactions.
 *
 * <p>Typical usage: Allows for writing low-level data access objects that use
 * resources such as JDBC DataSources but are not transaction-aware themselves.
 * Instead, they can implicitly participate in transactions handled by higher-level
 * application services utilizing this class, making calls to the low-level
 * services via an inner-class callback object.
 *
 * <p>Can be used within a service implementation via direct instantiation with
 * a transaction manager reference, or get prepared in an application context
 * and passed to services as bean reference. Note: The transaction manager should
 * always be configured as bean in the application context: in the first case given
 * to the service directly, in the second case given to the prepared template.
 *
 * <p>Supports setting the propagation behavior and the isolation level by name,
 * for convenient configuration in context definitions.
 *
 * <p>
 *     模板类，简化了程序化事务划分和事务异常处理。
 * </p>
 * <p>
 *      中心方法是execute，支持实现TransactionCallback接口的事务代码。
 *      此模板处理事务生命周期和可能的异常，因此TransactionCallback实现和调用代码都不需要显式处理事务。
 * </p>
 * <p>
 *      典型用法：允许编写使用JDBC DataSource等资源但本身不具有事务感知功能的低级数据访问对象。
 *      相反，它们可以隐式参与由使用此类的更高级应用程序服务处理的事务，通过内部类回调对象调用低级服务。
 * </p>
 * <p>
 *      可以通过使用事务管理器引用直接实例化在服务实现中使用，或者在应用程序上下文中准备并作为bean引用传递给服务。
 *      注意：事务管理器应始终在应用程序上下文中配置为bean：在第一种情况下直接提供给服务，在第二种情况下给予准备好的模板。
 * </p>
 * <p>
 *      支持按名称设置传播行为和隔离级别，以便在上下文定义中进行配置。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see #execute
 * @see #setTransactionManager
 * @see org.springframework.transaction.PlatformTransactionManager
 */
@SuppressWarnings("serial")
public class TransactionTemplate extends DefaultTransactionDefinition
		implements TransactionOperations, InitializingBean {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 事务管理器
	 */
	private PlatformTransactionManager transactionManager;


	/**
	 * Construct a new TransactionTemplate for bean usage.
	 * <p>Note: The PlatformTransactionManager needs to be set before
	 * any {@code execute} calls.
	 * @see #setTransactionManager
	 */
	public TransactionTemplate() {
	}

	/**
	 * Construct a new TransactionTemplate using the given transaction manager.
	 * @param transactionManager the transaction management strategy to be used
	 */
	public TransactionTemplate(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Construct a new TransactionTemplate using the given transaction manager,
	 * taking its default settings from the given transaction definition.
	 * @param transactionManager the transaction management strategy to be used
	 * @param transactionDefinition the transaction definition to copy the
	 * default settings from. Local properties can still be set to change values.
	 */
	public TransactionTemplate(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
		super(transactionDefinition);
		this.transactionManager = transactionManager;
	}


	/**
	 * Set the transaction management strategy to be used.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the transaction management strategy to be used.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("Property 'transactionManager' is required");
		}
	}


	@Override
	public <T> T execute(TransactionCallback<T> action) throws TransactionException {
		//如果Manager是可回调的CallbackPreferringPlatformTransactionManager，直接委托给其处理
		if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager) {
			return ((CallbackPreferringPlatformTransactionManager) this.transactionManager).execute(this, action);
		}
		//否则模板式展开
		else {
			//获得事务状态
			TransactionStatus status = this.transactionManager.getTransaction(this);
			//执行结果
			T result;
			try {
				//执行
				result = action.doInTransaction(status);
			}
			catch (RuntimeException ex) {
				// Transactional code threw application exception -> rollback
				// 事务代码抛出应用程序异常 - > 回滚
				rollbackOnException(status, ex);
				throw ex;
			}
			catch (Error err) {
				// Transactional code threw error -> rollback
				// 事务代码抛出错误 - > 回滚
				rollbackOnException(status, err);
				throw err;
			}
			catch (Throwable ex) {
				// Transactional code threw unexpected exception -> rollback
				// 事务代码引发意外异常 - > 回滚
				rollbackOnException(status, ex);
				throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
			}
			// 提交
			this.transactionManager.commit(status);
			// 返回结果
			return result;
		}
	}

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 */
	private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
		logger.debug("Initiating transaction rollback on application exception", ex);
		try {
			this.transactionManager.rollback(status);
		}
		catch (TransactionSystemException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			ex2.initApplicationException(ex);
			throw ex2;
		}
		catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
		catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}

}
