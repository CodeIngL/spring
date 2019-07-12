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

import org.springframework.aop.SpringProxy;

/**
 * A marker interface for manually created transactional proxies.
 *
 * <p>{@link TransactionAttributeSourcePointcut} will ignore such existing
 * transactional proxies during AOP auto-proxying and therefore avoid
 * re-processing transaction metadata on them.
 * <p>
 *     用于手动创建的事务代理的标记接口。
 * </p>
 * <p>
 *  {@link TransactionAttributeSourcePointcut}将在AOP自动代理期间忽略此类现有事务代理，因此避免重新处理它们上的事务元数据。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 4.1.7
 */
public interface TransactionalProxy extends SpringProxy {

}
