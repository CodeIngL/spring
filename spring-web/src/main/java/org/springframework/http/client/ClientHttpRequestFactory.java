/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.http.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;

/**
 * Factory for {@link ClientHttpRequest} objects.
 * Requests are created by the {@link #createRequest(URI, HttpMethod)} method.
 *
 * <p>
 *     ClientHttpRequest对象的工厂。 Requests由createRequest(URI, HttpMethod)方法创建。
 * </p>
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public interface ClientHttpRequestFactory {

	/**
	 * Create a new {@link ClientHttpRequest} for the specified URI and HTTP method.
	 * <p>The returned request can be written to, and then executed by calling
	 * {@link ClientHttpRequest#execute()}.
	 *
	 * <p>
	 *     为指定的URI和HTTP方法创建新的ClientHttpRequest。
	 * </p>
	 * <p>
	 *     	 可以写入返回的请求，然后通过调用ClientHttpRequest.execute（）来执行。
	 * </p>
	 * @param uri the URI to create a request for
	 * @param httpMethod the HTTP method to execute
	 * @return the created request
	 * @throws IOException in case of I/O errors
	 */
	ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException;

}
