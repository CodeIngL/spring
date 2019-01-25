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
package org.springframework.web.context.request;

/**
 * Extends {@code WebRequestInterceptor} with a callback method invoked during
 * asynchronous request handling.
 *
 * <p>When a handler starts asynchronous request handling, the DispatcherServlet
 * exits without invoking {@code postHandle} and {@code afterCompletion}, as it
 * normally does, since the results of request handling (e.g. ModelAndView) are
 * not available in the current thread and handling is not yet complete.
 * In such scenarios, the {@link #afterConcurrentHandlingStarted(WebRequest)}
 * method is invoked instead allowing implementations to perform tasks such as
 * cleaning up thread bound attributes.
 *
 * <p>When asynchronous handling completes, the request is dispatched to the
 * container for further processing. At this stage the DispatcherServlet invokes
 * {@code preHandle}, {@code postHandle} and {@code afterCompletion} as usual.
 *
 * <p>
 *   使用在异步请求处理期间调用的回调方法扩展{@code WebRequestInterceptor}。
 * </p>
 * <p> 当处理程序启动异步请求处理时，DispatcherServlet退出而不像通常那样调用{@code postHandle}和{@code afterCompletion}，因为请求处理的结果（例如ModelAndView）在当前线程中不可用，并且处理尚未完成。
 * 在这种情况下，调用 {@link #afterConcurrentHandlingStarted(WebRequest)}方法，允许实现执行诸如清理线程绑定属性之类的任务。
 * </p>
 * <p>
 *     异步处理完成后，将请求分派给容器以进行进一步处理。 在这个阶段，DispatcherServlet像往常一样调用{@code preHandle}，{@code postHandle}和{@code afterCompletion}
 * </p>
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 *
 * @see org.springframework.web.context.request.async.WebAsyncManager
 */
public interface AsyncWebRequestInterceptor extends WebRequestInterceptor{

	/**
	 * Called instead of {@code postHandle} and {@code afterCompletion}, when the
	 * the handler started handling the request concurrently.
	 *
	 * @param request the current request
	 */
	void afterConcurrentHandlingStarted(WebRequest request);

}
