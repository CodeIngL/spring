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

package org.springframework.transaction.interceptor;

import org.springframework.transaction.TransactionDefinition;

/**
 * This interface adds a {@code rollbackOn} specification to {@link TransactionDefinition}.
 * As custom {@code rollbackOn} is only possible with AOP, this class resides
 * in the AOP transaction package.
 *
 * <p>
 *     此接口向{@link TransactionDefinition}添加{@code rollbackOn} 规范。 由于自定义{@code rollbackOn} 仅适用于AOP，因此该类驻留在AOP事务包中
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.03.2003
 * @see DefaultTransactionAttribute
 * @see RuleBasedTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {

	/**
	 * Return a qualifier value associated with this transaction attribute.
	 * <p>This may be used for choosing a corresponding transaction manager
	 * to process this specific transaction.
	 * <p>
	 *     返回与此事务属性关联的限定符值。
	 * </p>
	 * <p>
	 *     	 这可以用于选择相应的事务管理器来处理该特定事务。
	 * </p>
	 */
	String getQualifier();

	/**
	 * Should we roll back on the given exception?
	 * @param ex the exception to evaluate
	 * @return whether to perform a rollback or not
	 */
	boolean rollbackOn(Throwable ex);

}
