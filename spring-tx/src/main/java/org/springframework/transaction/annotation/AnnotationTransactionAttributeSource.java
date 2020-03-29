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

package org.springframework.transaction.annotation;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Implementation of the
 * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}
 * interface for working with transaction metadata in JDK 1.5+ annotation format.
 *
 * <p>This class reads Spring's JDK 1.5+ {@link Transactional} annotation and
 * exposes corresponding transaction attributes to Spring's transaction infrastructure.
 * Also supports JTA 1.2's {@link javax.transaction.Transactional} and EJB3's
 * {@link javax.ejb.TransactionAttribute} annotation (if present).
 * This class may also serve as base class for a custom TransactionAttributeSource,
 * or get customized through {@link TransactionAnnotationParser} strategies.
 *
 * <p>
 *     org.springframework.transaction.interceptor.TransactionAttributeSource接口的实现，用于处理JDK 1.5+注释格式的事务元数据。
 * </p>
 * <p>
 *      该类读取Spring的JDK 1.5+ {@link Transactional} 注解，并向Spring的事务基础结构公开相应的事务属性。
 *      还支持JTA 1.2的{@link javax.transaction.Transactional}和EJB3的{@link javax.ejb.TransactionAttribute}注解（如果存在）。
 *      此类还可以作为自定义TransactionAttributeSource的基类，或通过TransactionAnnotationParser策略进行自定义。
 * </p>
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see Transactional
 * @see TransactionAnnotationParser
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource
 */
@SuppressWarnings("serial")
public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource
		implements Serializable {

	private static final boolean jta12Present = ClassUtils.isPresent(
			"javax.transaction.Transactional", AnnotationTransactionAttributeSource.class.getClassLoader());

	private static final boolean ejb3Present = ClassUtils.isPresent(
			"javax.ejb.TransactionAttribute", AnnotationTransactionAttributeSource.class.getClassLoader());

	//是否仅仅是public方法
	private final boolean publicMethodsOnly;

	//支持事物注解的解析器
	private final Set<TransactionAnnotationParser> annotationParsers;


	/**
	 * Create a default AnnotationTransactionAttributeSource, supporting
	 * public methods that carry the {@code Transactional} annotation
	 * or the EJB3 {@link javax.ejb.TransactionAttribute} annotation.
	 */
	public AnnotationTransactionAttributeSource() {
		this(true);
	}

	/**
	 * Create a custom AnnotationTransactionAttributeSource, supporting
	 * public methods that carry the {@code Transactional} annotation
	 * or the EJB3 {@link javax.ejb.TransactionAttribute} annotation.
	 * @param publicMethodsOnly whether to support public methods that carry
	 * the {@code Transactional} annotation only (typically for use
	 * with proxy-based AOP), or protected/private methods as well
	 * (typically used with AspectJ class weaving)
	 */
	public AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
		this.publicMethodsOnly = publicMethodsOnly;
		this.annotationParsers = new LinkedHashSet<TransactionAnnotationParser>(2);
		this.annotationParsers.add(new SpringTransactionAnnotationParser());
		if (jta12Present) {
			this.annotationParsers.add(new JtaTransactionAnnotationParser());
		}
		if (ejb3Present) {
			this.annotationParsers.add(new Ejb3TransactionAnnotationParser());
		}
	}

	/**
	 * Create a custom AnnotationTransactionAttributeSource.
	 * @param annotationParser the TransactionAnnotationParser to use
	 */
	public AnnotationTransactionAttributeSource(TransactionAnnotationParser annotationParser) {
		this.publicMethodsOnly = true;
		Assert.notNull(annotationParser, "TransactionAnnotationParser must not be null");
		this.annotationParsers = Collections.singleton(annotationParser);
	}

	/**
	 * Create a custom AnnotationTransactionAttributeSource.
	 * @param annotationParsers the TransactionAnnotationParsers to use
	 */
	public AnnotationTransactionAttributeSource(TransactionAnnotationParser... annotationParsers) {
		this.publicMethodsOnly = true;
		Assert.notEmpty(annotationParsers, "At least one TransactionAnnotationParser needs to be specified");
		Set<TransactionAnnotationParser> parsers = new LinkedHashSet<TransactionAnnotationParser>(annotationParsers.length);
		Collections.addAll(parsers, annotationParsers);
		this.annotationParsers = parsers;
	}

	/**
	 * Create a custom AnnotationTransactionAttributeSource.
	 * @param annotationParsers the TransactionAnnotationParsers to use
	 */
	public AnnotationTransactionAttributeSource(Set<TransactionAnnotationParser> annotationParsers) {
		this.publicMethodsOnly = true;
		Assert.notEmpty(annotationParsers, "At least one TransactionAnnotationParser needs to be specified");
		this.annotationParsers = annotationParsers;
	}


	@Override
	protected TransactionAttribute findTransactionAttribute(Method method) {
		return determineTransactionAttribute(method);
	}

	@Override
	protected TransactionAttribute findTransactionAttribute(Class<?> clazz) {
		return determineTransactionAttribute(clazz);
	}

	/**
	 * Determine the transaction attribute for the given method or class.
	 * <p>This implementation delegates to configured
	 * {@link TransactionAnnotationParser TransactionAnnotationParsers}
	 * for parsing known annotations into Spring's metadata attribute class.
	 * Returns {@code null} if it's not transactional.
	 * <p>Can be overridden to support custom annotations that carry transaction metadata.
	 * @param ae the annotated method or class
	 * @return TransactionAttribute the configured transaction attribute,
	 * or {@code null} if none was found
	 * <p>
	 *     确定给定方法或类的事务属性。
	 * </p>
	 * <p>
	 *    此实现委托配置的TransactionAnnotationParsers，用于将已知注释解析为Spring的元数据属性类。 如果它不是事务性的，则返回null。
	 * </p>
	 * <p>
	 *    可以重写以支持带有事务元数据的自定义注释。
	 * </p>
	 */
	protected TransactionAttribute determineTransactionAttribute(AnnotatedElement ae) {
		if (ae.getAnnotations().length > 0) {
			for (TransactionAnnotationParser annotationParser : this.annotationParsers) {
				TransactionAttribute attr = annotationParser.parseTransactionAnnotation(ae);
				if (attr != null) {
					return attr;
				}
			}
		}
		return null;
	}

	/**
	 * By default, only public methods can be made transactional.
	 */
	@Override
	protected boolean allowPublicMethodsOnly() {
		return this.publicMethodsOnly;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationTransactionAttributeSource)) {
			return false;
		}
		AnnotationTransactionAttributeSource otherTas = (AnnotationTransactionAttributeSource) other;
		return (this.annotationParsers.equals(otherTas.annotationParsers) &&
				this.publicMethodsOnly == otherTas.publicMethodsOnly);
	}

	@Override
	public int hashCode() {
		return this.annotationParsers.hashCode();
	}

}
