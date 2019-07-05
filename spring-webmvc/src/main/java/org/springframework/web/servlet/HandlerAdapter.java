/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC framework SPI, allowing parameterization of the core MVC workflow.
 *
 * <p>Interface that must be implemented for each handler type to handle a request.
 * This interface is used to allow the {@link DispatcherServlet} to be indefinitely
 * extensible. The {@code DispatcherServlet} accesses all installed handlers through
 * this interface, meaning that it does not contain code specific to any handler type.
 *
 * <p>Note that a handler can be of type {@code Object}. This is to enable
 * handlers from other frameworks to be integrated with this framework without
 * custom coding, as well as to allow for annotation-driven handler objects that
 * do not obey any specific Java interface.
 *
 * <p>This interface is not intended for application developers. It is available
 * to handlers who want to develop their own web workflow.
 *
 * <p>Note: {@code HandlerAdapter} implementors may implement the {@link
 * org.springframework.core.Ordered} interface to be able to specify a sorting
 * order (and thus a priority) for getting applied by the {@code DispatcherServlet}.
 * Non-Ordered instances get treated as lowest priority.
 *
 * <p>
 * MVC框架SPI，允许核心MVC工作流的参数化。
 * </p>
 * <p>
 * 必须为每个处理程序类型实现的接口，以处理请求。此接口用于允许DispatcherServlet无限扩展。 DispatcherServlet通过此接口访问所有已安装的处理程序，这意味着它不包含特定于任何处理程序类型的代码。
 * </p>
 * <p>
 * 请注意，处理程序可以是Object类型。这是为了使其他框架的处理程序能够与此框架集成，而无需自定义编码，以及允许不遵循任何特定Java接口的注释驱动的处理程序对象。
 * </p>
 * <p>
 * 此接口不适用于应用程序开发人员。它适用于想要开发自己的Web工作流程的处理程序。
 * </p>
 * <p>
 * 注意：HandlerAdapter实现者可以实现org.springframework.core.Ordered接口，以便能够指定DispatcherServlet应用的排序顺序（以及优先级）。非有序实例被视为最低优先级
 *
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
public interface HandlerAdapter {

    /**
     * Given a handler instance, return whether or not this {@code HandlerAdapter}
     * can support it. Typical HandlerAdapters will base the decision on the handler
     * type. HandlerAdapters will usually only support one handler type each.
     * <p>A typical implementation:
     * <p>{@code
     * return (handler instanceof MyHandler);
     * }
     *
     * @param handler handler object to check
     * @return whether or not this object can use the given handler
     */
    boolean supports(Object handler);

    /**
     * Use the given handler to handle this request.
     * The workflow that is required may vary widely.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  handler to use. This object must have previously been passed
     *                 to the {@code supports} method of this interface, which must have
     *                 returned {@code true}.
     * @return ModelAndView object with the name of the view and the required
     * model data, or {@code null} if the request has been handled directly
     * @throws Exception in case of errors
     */
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    /**
     * Same contract as for HttpServlet's {@code getLastModified} method.
     * Can simply return -1 if there's no support in the handler class.
     *
     * @param request current HTTP request
     * @param handler handler to use
     * @return the lastModified value for the given handler
     * @see javax.servlet.http.HttpServlet#getLastModified
     * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
     */
    long getLastModified(HttpServletRequest request, Object handler);

}
