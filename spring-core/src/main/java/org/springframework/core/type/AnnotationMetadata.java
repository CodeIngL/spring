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

import java.util.Set;

/**
 * Interface that defines abstract access to the annotations of a specific
 * class, in a form that does not require that class to be loaded yet.
 *
 * <p>
 *     定义对特定类的注解的抽象访问的接口，该形式不需要该类被加载。
 * </p>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.5
 * @see StandardAnnotationMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getAnnotationMetadata()
 * @see AnnotatedTypeMetadata
 */
public interface AnnotationMetadata extends ClassMetadata, AnnotatedTypeMetadata {

	/**
	 * Get the fully qualified class names of all annotation types that
	 * are <em>present</em> on the underlying class.
	 *
	 * <p>
	 *     获取基础类中存在的所有注解类型的完全限定类名。
	 * </p>
	 * @return the annotation type names
	 */
	Set<String> getAnnotationTypes();

	/**
	 * Get the fully qualified class names of all meta-annotation types that
	 * are <em>present</em> on the given annotation type on the underlying class.
	 *
	 * <p>
	 *     获取基础类上给定注解类型上存在的所有元注解类型的完全限定类名。
	 * </p>
	 * @param annotationName the fully qualified class name of the meta-annotation
	 * type to look for
	 * @return the meta-annotation type names
	 */
	Set<String> getMetaAnnotationTypes(String annotationName);

	/**
	 * Determine whether an annotation of the given type is <em>present</em> on
	 * the underlying class.
	 *
	 * <p>
	 *     确定基础类上是否存在给定类型的注解。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return {@code true} if a matching annotation is present
	 */
	boolean hasAnnotation(String annotationName);

	/**
	 * Determine whether the underlying class has an annotation that is itself
	 * annotated with the meta-annotation of the given type.
	 *
	 * <p>
	 *     确定基础类是否具有注解，该注解本身使用给定类型的元注解进行注解。
	 * </p>
	 * @param metaAnnotationName the fully qualified class name of the
	 * meta-annotation type to look for
	 * @return {@code true} if a matching meta-annotation is present
	 */
	boolean hasMetaAnnotation(String metaAnnotationName);

	/**
	 * Determine whether the underlying class has any methods that are
	 * annotated (or meta-annotated) with the given annotation type.
	 * <p>
	 *     确定基础类是否具有使用给定注解类型进行注解（或元注解）的任何方法。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 */
	boolean hasAnnotatedMethods(String annotationName);

	/**
	 * Retrieve the method metadata for all methods that are annotated
	 * (or meta-annotated) with the given annotation type.
	 * <p>For any returned method, {@link MethodMetadata#isAnnotated} will
	 * return {@code true} for the given annotation type.
	 *
	 * <p>
	 *     检索使用给定注解类型注释（或元注解）的所有方法的方法元数据。
	 * </p>
	 * <p>
	 *     	 对于任何返回的方法，MethodMetadata.isAnnotated将对给定的注解类型返回true。
	 * </p>
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a set of {@link MethodMetadata} for methods that have a matching
	 * annotation. The return value will be an empty set if no methods match
	 * the annotation type.
	 */
	Set<MethodMetadata> getAnnotatedMethods(String annotationName);

}
