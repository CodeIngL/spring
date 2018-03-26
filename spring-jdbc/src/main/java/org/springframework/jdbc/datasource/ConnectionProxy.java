/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

/**
 * Subinterface of {@link java.sql.Connection} to be implemented by
 * Connection proxies. Allows access to the underlying target Connection.
 *
 * <p>This interface can be checked when there is a need to cast to a
 * native JDBC Connection such as Oracle's OracleConnection. Spring's
 * {@link org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter}
 * automatically detects such proxies before delegating to the actual
 * unwrapping for a specific connection pool. Alternatively, all such
 * connections also support JDBC 4.0's {@link Connection#unwrap}.
 *
 *
 * <p>
 *     连接的子接口由连接代理实现。 允许访问底层的目标连接。
 * </p>
 * <p>
 *     当需要转换为本地JDBC连接（如Oracle的OracleConnection）时，可以检查此接口。
 *     Spring的org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter在委托给特定连接池的实际解包之前自动检测这样的代理。
 *     或者，所有这样的连接也支持JDBC 4.0的Connection.unwrap。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionAwareDataSourceProxy
 * @see LazyConnectionDataSourceProxy
 * @see DataSourceUtils#getTargetConnection(java.sql.Connection)
 */
public interface ConnectionProxy extends Connection {

	/**
	 * Return the target Connection of this proxy.
	 * <p>This will typically be the native driver Connection
	 * or a wrapper from a connection pool.
	 *
	 * <p>
	 *     返回此代理的目标连接。
	 *     这通常是本地驱动程序连接或来自连接池的包装器。
	 * </p>
	 * @return the underlying Connection (never {@code null})
	 */
	Connection getTargetConnection();

}
