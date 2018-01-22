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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Beans on which the current bean depends. Any beans specified are guaranteed to be
 * created by the container before this bean. Used infrequently in cases where a bean
 * does not explicitly depend on another through properties or constructor arguments,
 * but rather depends on the side effects of another bean's initialization.
 *
 * <p>May be used on any class directly or indirectly annotated with
 * {@link org.springframework.stereotype.Component} or on methods annotated
 * with {@link Bean}.
 *
 * <p>Using {@link DependsOn} at the class level has no effect unless component-scanning
 * is being used. If a {@link DependsOn}-annotated class is declared via XML,
 * {@link DependsOn} annotation metadata is ignored, and
 * {@code <bean depends-on="..."/>} is respected instead.
 *
 *
 * <p>
 *     当前bean依赖的bean。
 *     在这个bean之前，指定的任何bean都被保证由容器创建。
 *     在bean没有明确依赖另一个属性或构造函数参数的情况下使用，而是依赖于另一个bean的初始化。
 * <p>
 *     可用于任何直接或间接使用org.springframework.stereotype.Component进行注解的类或使用Bean注解的方法。
 * <p>
 *     除非正在使用组件扫描，否则在类层次上使用DependsOn将无效。
 *     如果DependsOn注解的类是通过XML声明的，则DependsOn注释元数据将被忽略，而<bean depends-on =“...”/>会被使用。
 *
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependsOn {

	String[] value() default {};

}
