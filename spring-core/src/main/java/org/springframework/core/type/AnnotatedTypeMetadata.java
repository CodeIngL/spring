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

package org.springframework.core.type;

import java.util.Map;

import org.springframework.util.MultiValueMap;

/**
 * Defines access to the annotations of a specific type ({@link AnnotationMetadata class}
 * or {@link MethodMetadata method}), in a form that does not necessarily require the
 * class-loading.
 *
 * <p>
 *     定义对特定类型（类或方法）的注解的访问，其格式不一定需要类加载。
 * </p>
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Mark Pollack
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see AnnotationMetadata
 * @see MethodMetadata
 */
public interface AnnotatedTypeMetadata {

	/**
	 * Determine whether the underlying element has an annotation or meta-annotation
	 * of the given type defined.
	 * <p>If this method returns {@code true}, then
	 * {@link #getAnnotationAttributes} will return a non-null Map.
	 * <p>
	 *     确定底层元素是否具有定义的给定类型的注解或元注解。
	 * </p>
	 * <p>
	 *     如果此方法返回true，则getAnnotationAttributes将返回一个非空Map。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return whether a matching annotation is defined
	 */
	boolean isAnnotated(String annotationName);

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation),
	 * also taking attribute overrides on composed annotations into account.
	 *
	 * <p>
	 *     检索给定类型（如果在底层元素上定义为直接注解或元注解）的给定类型注解的属性，也考虑组合注解的属性覆盖。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 */
	Map<String, Object> getAnnotationAttributes(String annotationName);

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation),
	 * also taking attribute overrides on composed annotations into account.
	 *
	 * <p>
	 *     检索给定类型（如果在底层元素上定义为直接注释或元注释）的给定类型注释的属性，也考虑组合注释的属性覆盖。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @param classValuesAsString whether to convert class references to String
	 * class names for exposure as values in the returned Map, instead of Class
	 * references which might potentially have to be loaded first
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 */
	Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString);

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * Note that this variant does <i>not</i> take attribute overrides into account.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
	 * and a list of the defined attribute values as Map value. This return value will
	 * be {@code null} if no matching annotation is defined.
	 * @see #getAllAnnotationAttributes(String, boolean)
	 */
	MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName);

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * Note that this variant does <i>not</i> take attribute overrides into account.
	 *
	 * <p>
	 *     检索给定类型的所有注解的所有属性（如果在底层元素上定义为直接注解或元注解）。 请注意，该变体不考虑属性覆盖。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for（要查找的注释类型的完全限定类名称）
	 * @param classValuesAsString  whether to convert class references to String(是否将类引用转换为String)
	 * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
	 * and a list of the defined attribute values as Map value. This return value will
	 * be {@code null} if no matching annotation is defined.(以属性名称作为关键字（例如“value”）的属性的MultiMap，
	 * 以及定义的属性值的列表作为Map值。 如果没有定义匹配的注解，则此返回值将为空。)
	 * @see #getAllAnnotationAttributes(String)
	 */
	MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString);

}
