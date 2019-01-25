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

package org.springframework.web.servlet.mvc.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * Contract for request mapping conditions.
 *
 * <p>Request conditions can be combined via {@link #combine(Object)}, matched to
 * a request via {@link #getMatchingCondition(HttpServletRequest)}, and compared
 * to each other via {@link #compareTo(Object, HttpServletRequest)} to determine
 * which is a closer match for a given request.
 *
 * <p>
 *     请求映射条件的合同。
 * </p>
 * <p>
 *     请求条件可以通过combine（Object）组合，通过getMatchingCondition（HttpServletRequest）与请求匹配，并通过compareTo（Object，HttpServletRequest）相互比较，以确定哪个是给定请求的更接近匹配。
 * </p>
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 * @param <T> the type of objects that this RequestCondition can be combined
 * with and compared to
 */
public interface RequestCondition<T> {

	/**
	 * Combine this condition with another such as conditions from a
	 * type-level and method-level {@code @RequestMapping} annotation.
	 * @param other the condition to combine with.
	 * @return a request condition instance that is the result of combining
	 * the two condition instances.
	 */
	T combine(T other);

	/**
	 * Check if the condition matches the request returning a potentially new
	 * instance created for the current request. For example a condition with
	 * multiple URL patterns may return a new instance only with those patterns
	 * that match the request.
	 * <p>For CORS pre-flight requests, conditions should match to the would-be,
	 * actual request (e.g. URL pattern, query parameters, and the HTTP method
	 * from the "Access-Control-Request-Method" header). If a condition cannot
	 * be matched to a pre-flight request it should return an instance with
	 * empty content thus not causing a failure to match.
	 *
	 * <p>
	 *     检查条件是否与返回为当前请求创建的潜在新实例的请求匹配。 例如，具有多个URL模式的条件可能仅返回与该请求匹配的那些模式的新实例。
	 * </p>
	 * <p>
	 * 对于CORS飞行前请求，条件应与可能的实际请求（例如，URL模式，查询参数和来自“Access-Control-Request-Method”标头的HTTP方法）匹配。 如果条件无法与飞行前请求匹配，则应返回具有空内容的实例，从而不会导致匹配失败
	 * </p>
	 * @return a condition instance in case of a match or {@code null} otherwise.
	 */
	T getMatchingCondition(HttpServletRequest request);

	/**
	 * Compare this condition to another condition in the context of
	 * a specific request. This method assumes both instances have
	 * been obtained via {@link #getMatchingCondition(HttpServletRequest)}
	 * to ensure they have content relevant to current request only.
	 */
	int compareTo(T other, HttpServletRequest request);

}
