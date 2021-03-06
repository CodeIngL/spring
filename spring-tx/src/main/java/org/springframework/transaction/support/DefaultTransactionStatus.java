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

package org.springframework.transaction.support;

import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;

/**
 * Default implementation of the {@link org.springframework.transaction.TransactionStatus}
 * interface, used by {@link AbstractPlatformTransactionManager}. Based on the concept
 * of an underlying "transaction object".
 *
 * <p>Holds all status information that {@link AbstractPlatformTransactionManager}
 * needs internally, including a generic transaction object determined by the
 * concrete transaction manager implementation.
 *
 * <p>Supports delegating savepoint-related methods to a transaction object
 * that implements the {@link SavepointManager} interface.
 *
 * <p><b>NOTE:</b> This is <i>not</i> intended for use with other PlatformTransactionManager
 * implementations, in particular not for mock transaction managers in testing environments.
 * Use the alternative {@link SimpleTransactionStatus} class or a mock for the plain
 * {@link org.springframework.transaction.TransactionStatus} interface instead.
 *
 * <p>
 *     由AbstractPlatformTransactionManager使用的org.springframework.transaction.TransactionStatus接口的默认实现。 基于底层“交易对象”的概念。
 * 保存AbstractPlatformTransactionManager内部需要的所有状态信息，包括由具体事务管理器实现确定的通用事务对象。
 * 支持将与保存点相关的方法委派给实现SavepointManager接口的事务对象。
 * 注意：这不适用于其他PlatformTransactionManager实现，特别是不适用于测试环境中的模拟事务管理器。 请使用替代的SimpleTransactionStatus类或简单的org.springframework.transaction.TransactionStatus接口的模拟。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.transaction.SavepointManager
 * @see #getTransaction
 * @see #createSavepoint
 * @see #rollbackToSavepoint
 * @see #releaseSavepoint
 * @see SimpleTransactionStatus
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {

	/**
	 * 标识了当前事务的对象
	 */
	private final Object transaction;

	/**
	 * 是否是一个新事务
	 */
	private final boolean newTransaction;

	/**
	 * 是否是一个新同步
	 */
	private final boolean newSynchronization;

	/**
	 * 只读选项
	 */
	private final boolean readOnly;

	/**
	 * 日志是否答应
	 */
	private final boolean debug;

	/**
	 * 被挂起的资源持有者
	 */
	private final Object suspendedResources;


	/**
	 * Create a new DefaultTransactionStatus instance.
	 * <p>
	 *     创建一个新的DefaultTransactionStatus实例。
	 * </p>
	 * @param transaction underlying transaction object that can hold
	 * state for the internal transaction implementation
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 * @param newSynchronization if a new transaction synchronization
	 * has been opened for the given transaction
	 * @param readOnly whether the transaction is read-only
	 * @param debug should debug logging be enabled for the handling of this transaction?
	 * Caching it in here can prevent repeated calls to ask the logging system whether
	 * debug logging should be enabled.
	 * @param suspendedResources a holder for resources that have been suspended
	 * for this transaction, if any
	 */
	public DefaultTransactionStatus(
			Object transaction, boolean newTransaction, boolean newSynchronization,
			boolean readOnly, boolean debug, Object suspendedResources) {

		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.readOnly = readOnly;
		this.debug = debug;
		this.suspendedResources = suspendedResources;
	}


	/**
	 * Return the underlying transaction object.
	 */
	public Object getTransaction() {
		return this.transaction;
	}

	/**
	 * Return whether there is an actual transaction active.
	 */
	public boolean hasTransaction() {
		return (this.transaction != null);
	}

	@Override
	public boolean isNewTransaction() {
		return (hasTransaction() && this.newTransaction);
	}

	/**
	 * Return if a new transaction synchronization has been opened
	 * for this transaction.
	 */
	public boolean isNewSynchronization() {
		return this.newSynchronization;
	}

	/**
	 * Return if this transaction is defined as read-only transaction.
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Return whether the progress of this transaction is debugged. This is used
	 * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
	 * calls to logger.isDebug(). Not really intended for client code.
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * Return the holder for resources that have been suspended for this transaction,
	 * if any.
	 */
	public Object getSuspendedResources() {
		return this.suspendedResources;
	}


	//---------------------------------------------------------------------
	// Enable functionality through underlying transaction object
	//---------------------------------------------------------------------

	/**
	 * Determine the rollback-only flag via checking both the transaction object,
	 * provided that the latter implements the {@link SmartTransactionObject} interface.
	 * <p>Will return "true" if the transaction itself has been marked rollback-only
	 * by the transaction coordinator, for example in case of a timeout.
	 * @see SmartTransactionObject#isRollbackOnly
	 */
	@Override
	public boolean isGlobalRollbackOnly() {
		return ((this.transaction instanceof SmartTransactionObject) &&
				((SmartTransactionObject) this.transaction).isRollbackOnly());
	}

	/**
	 * Delegate the flushing to the transaction object,
	 * provided that the latter implements the {@link SmartTransactionObject} interface.
	 */
	@Override
	public void flush() {
		if (this.transaction instanceof SmartTransactionObject) {
			((SmartTransactionObject) this.transaction).flush();
		}
	}

	/**
	 * This implementation exposes the SavepointManager interface
	 * of the underlying transaction object, if any.
	 */
	@Override
	protected SavepointManager getSavepointManager() {
		if (!isTransactionSavepointManager()) {
			throw new NestedTransactionNotSupportedException(
				"Transaction object [" + getTransaction() + "] does not support savepoints");
		}
		return (SavepointManager) getTransaction();
	}

	/**
	 * Return whether the underlying transaction implements the
	 * SavepointManager interface.
	 * @see #getTransaction
	 * @see org.springframework.transaction.SavepointManager
	 */
	public boolean isTransactionSavepointManager() {
		return (getTransaction() instanceof SavepointManager);
	}

}
