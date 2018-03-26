/*
 * Copyright 2002-2008 the original author or authors.
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

/**
 * Generic interface to be implemented by resource holders.
 * Allows Spring's transaction infrastructure to introspect
 * and reset the holder when necessary.
 *
 * <p>
 *     通用接口由资源持有者实施。 允许Spring的事务基础架构在必要时反思和重置持有者。
 * </p>
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see ResourceHolderSupport
 * @see ResourceHolderSynchronization
 */
public interface ResourceHolder {

	/**
	 * Reset the transactional state of this holder.
	 *
	 * 重置此持有者的事务状态。
	 */
	void reset();

	/**
	 * Notify this holder that it has been unbound from transaction synchronization.
	 * <p>
	 *     通知此持有者它已从事务同步中解除绑定。
	 * </p>
	 */
	void unbound();

	/**
	 * Determine whether this holder is considered as 'void',
	 * i.e. as a leftover from a previous thread.
	 *
	 * <p>
	 *     确定这个持有者是否被认为是“void”，即作为先前线索的剩余物。
	 * </p>
	 */
	boolean isVoid();

}
