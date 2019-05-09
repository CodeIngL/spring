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

package org.springframework.aop;

/**
 * Superinterface for advisors that perform one or more AOP <b>introductions</b>.
 *
 * <p>This interface cannot be implemented directly; subinterfaces must
 * provide the advice type implementing the introduction.
 *
 * <p>Introduction is the implementation of additional interfaces
 * (not implemented by a target) via AOP advice.
 *
 * <p>
 * 对于执行一个或多个AOP introductions 的advisors的超级接口。
 * </p>
 * <p>
 * 这个接口不能直接实现; 子接口必须提供实现introduction的advice type。
 * </p>
 * <p>
 * Introduction是通过AOP advice 实现的额外接口（不是由目标实现）。
 * </p>
 *
 * @author Rod Johnson
 * @see IntroductionInterceptor
 * @since 04.04.2003
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

    /**
     * Return the filter determining which target classes this introduction
     * should apply to.
     * <p>This represents the class part of a pointcut. Note that method
     * matching doesn't make sense to introductions.
     *
     * @return the class filter
     */
    ClassFilter getClassFilter();

    /**
     * Can the advised interfaces be implemented by the introduction advice?
     * Invoked before adding an IntroductionAdvisor.
     *
     * @throws IllegalArgumentException if the advised interfaces can't be
     *                                  implemented by the introduction advice
     */
    void validateInterfaces() throws IllegalArgumentException;

}
