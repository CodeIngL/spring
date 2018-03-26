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

package org.springframework.core.type;

/**
 * Interface that defines abstract metadata of a specific class,
 * in a form that does not require that class to be loaded yet.
 *
 * <p>
 *     定义特定类的抽象元数据的接口，其形式不需要该类被加载。
 * </p>
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see StandardClassMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getClassMetadata()
 * @see AnnotationMetadata
 */
public interface ClassMetadata {

	/**
	 * Return the name of the underlying class.
	 * <p>
	 *     返回基础类的名称。
	 * </p>
	 */
	String getClassName();

	/**
	 * Return whether the underlying class represents an interface.
	 * <p>
	 *     返回底层类是否表示一个接口。
	 * </p>
	 */
	boolean isInterface();

	/**
	 * Return whether the underlying class represents an annotation.
	 * <p>
	 *     返回基础类是否代表注解
	 * </p>
	 * @since 4.1
	 */
	boolean isAnnotation();

	/**
	 * Return whether the underlying class is marked as abstract.
	 * <p>
	 *     返回底层类是否被标记为抽象。
	 * </p>
	 */
	boolean isAbstract();

	/**
	 * Return whether the underlying class represents a concrete class,
	 * i.e. neither an interface nor an abstract class.
	 * <p>
	 *     返回底层类是否代表具体类，即既不是接口也不是抽象类。
	 * </p>
	 */
	boolean isConcrete();

	/**
	 * Return whether the underlying class is marked as 'final'
	 * <p>
	 *     返回底层类是否被标记为“final”
	 * </p>.
	 */
	boolean isFinal();

	/**
	 * Determine whether the underlying class is independent, i.e. whether
	 * it is a top-level class or a nested class (static inner class) that
	 * can be constructed independently from an enclosing class.
	 * <p>
	 *     确定底层类是否是独立的，即它是一个顶级类还是一个嵌套类（静态内部类），它可以独立于一个封闭类构造。
	 * </p>
	 */
	boolean isIndependent();

	/**
	 * Return whether the underlying class is declared within an enclosing
	 * class (i.e. the underlying class is an inner/nested class or a
	 * local class within a method).
	 * <p>If this method returns {@code false}, then the underlying
	 * class is a top-level class.
	 * <p>
	 *     返回底层类是否在封闭类中声明（即底层类是方法中的内部/嵌套类或本地类）。
	 *     如果此方法返回false，则基础类是顶级类。
	 * </p>
	 */
	boolean hasEnclosingClass();

	/**
	 * Return the name of the enclosing class of the underlying class,
	 * or {@code null} if the underlying class is a top-level class.
	 */
	String getEnclosingClassName();

	/**
	 * Return whether the underlying class has a super class.
	 */
	boolean hasSuperClass();

	/**
	 * Return the name of the super class of the underlying class,
	 * or {@code null} if there is no super class defined.
	 */
	String getSuperClassName();

	/**
	 * Return the names of all interfaces that the underlying class
	 * implements, or an empty array if there are none.
	 */
	String[] getInterfaceNames();

	/**
	 * Return the names of all classes declared as members of the class represented by
	 * this ClassMetadata object. This includes public, protected, default (package)
	 * access, and private classes and interfaces declared by the class, but excludes
	 * inherited classes and interfaces. An empty array is returned if no member classes
	 * or interfaces exist.
	 * @since 3.1
	 */
	String[] getMemberClassNames();

}
