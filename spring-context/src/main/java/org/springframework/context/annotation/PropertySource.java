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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link org.springframework.core.env.PropertySource PropertySource} to Spring's
 * {@link org.springframework.core.env.Environment Environment}. To be used in
 * conjunction with @{@link Configuration} classes.
 *
 * <h3>Example usage</h3>
 *
 * <p>Given a file {@code app.properties} containing the key/value pair
 * {@code testbean.name=myTestBean}, the following {@code @Configuration} class
 * uses {@code @PropertySource} to contribute {@code app.properties} to the
 * {@code Environment}'s set of {@code PropertySources}.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/app.properties")
 * public class AppConfig {
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * Notice that the {@code Environment} object is @{@link
 * org.springframework.beans.factory.annotation.Autowired Autowired} into the
 * configuration class and then used when populating the {@code TestBean} object. Given
 * the configuration above, a call to {@code testBean.getName()} will return "myTestBean".
 *
 * <h3>Resolving ${...} placeholders in {@code <bean>} and {@code @Value} annotations</h3>
 *
 * In order to resolve ${...} placeholders in {@code <bean>} definitions or {@code @Value}
 * annotations using properties from a {@code PropertySource}, one must register
 * a {@code PropertySourcesPlaceholderConfigurer}. This happens automatically when using
 * {@code <context:property-placeholder>} in XML, but must be explicitly registered using
 * a {@code static} {@code @Bean} method when using {@code @Configuration} classes. See
 * the "Working with externalized values" section of @{@link Configuration}'s javadoc and
 * "a note on BeanFactoryPostProcessor-returning @Bean methods" of @{@link Bean}'s javadoc
 * for details and examples.
 *
 * <h3>Resolving ${...} placeholders within {@code @PropertySource} resource locations</h3>
 *
 * Any ${...} placeholders present in a {@code @PropertySource} {@linkplain #value()
 * resource location} will be resolved against the set of property sources already
 * registered against the environment. For example:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
 * public class AppConfig {
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * Assuming that "my.placeholder" is present in one of the property sources already
 * registered, e.g. system properties or environment variables, the placeholder will
 * be resolved to the corresponding value. If not, then "default/path" will be used as a
 * default. Expressing a default value (delimited by colon ":") is optional.  If no
 * default is specified and a property cannot be resolved, an {@code
 * IllegalArgumentException} will be thrown.
 *
 * <h3>A note on property overriding with @PropertySource</h3>
 *
 * In cases where a given property key exists in more than one {@code .properties}
 * file, the last {@code @PropertySource} annotation processed will 'win' and override.
 *
 * For example, given two properties files {@code a.properties} and
 * {@code b.properties}, consider the following two configuration classes
 * that reference them with {@code @PropertySource} annotations:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/a.properties")
 * public class ConfigA { }
 *
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/b.properties")
 * public class ConfigB { }
 * </pre>
 *
 * The override ordering depends on the order in which these classes are registered
 * with the application context.
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx =
 *     new AnnotationConfigApplicationContext();
 * ctx.register(ConfigA.class);
 * ctx.register(ConfigB.class);
 * ctx.refresh();
 * </pre>
 *
 * In the scenario above, the properties in {@code b.properties} will override any
 * duplicates that exist in {@code a.properties}, because {@code ConfigB} was registered
 * last.
 *
 * <p>In certain situations, it may not be possible or practical to tightly control
 * property source ordering when using {@code @ProperySource} annotations. For example,
 * if the {@code @Configuration} classes above were registered via component-scanning,
 * the ordering is difficult to predict. In such cases - and if overriding is important -
 * it is recommended that the user fall back to using the programmatic PropertySource API.
 * See {@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}
 * and {@link org.springframework.core.env.MutablePropertySources MutablePropertySources}
 * javadocs for details.
 *
 * <p>
 *     Annotation提供了一个便捷的声明机制，用于将一个PropertySource添加到Spring的环境中。 与@Configuration类一起使用。
 * </p>
 *
 * <h3>Example usage</h3>
 *
 * <p>
 *    给定一个包含键/值对testbean.name = myTestBean的文件app.properties，以下@Configuration类使用@PropertySource将app.properties提供给Environment的一组PropertySources。
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/app.properties")
 * public class AppConfig {
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 * <p>
 *     请注意，Environment对象是@Autowired到配置类中，然后在填充TestBean对象时使用。 鉴于上面的配置，调用testBean.getName（）将返回“myTestBean”。
 * </p>
 * <h3>Resolving ${...} placeholders in {@code <bean>} and {@code @Value} annotations</h3>
 *
 * <p>
 *     为了使用PropertySource中的属性来解析<bean>定义或@Value注解中的$ {...}占位符，必须注册一个PropertySourcesPlaceholderConfigurer。
 *     当在XML中使用<context：property-placeholder>时会自动发生，但在使用@Configuration类时必须使用静态@Bean方法显式注册。
 *     有关详细信息和示例，请参阅@Contact的javadoc中的“使用外部化值”部分以及@ Bean的javadoc中的“关于BeanFactoryPostProcessor返回@Bean方法的说明”。
 * </p>
 *
 * <h3>Resolving ${...} placeholders within {@code @PropertySource} resource locations</h3>
 *
 * <p>
 *     存在于@PropertySource资源位置中的任何$ {...}占位符将根据已经在环境中注册的一组属性源来解析。 例如：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
 * public class AppConfig {
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * <p>
 *     假设“my.placeholder”存在于已经注册的一个财产来源中，例如 系统属性或环境变量，占位符将被解析为相应的值。 如果不是，则默认使用“default / path”。
 *     表示一个默认值（用冒号“：”分隔）是可选的。 如果没有指定默认值，并且无法解析属性，则会抛出IllegalArgumentException异常。
 * </p>
 *
 * <h3>A note on property overriding with @PropertySource</h3>
 *
 * <p>
 *     在给定属性键存在于多个.properties文件中的情况下，最后处理的@PropertySource注释将“赢”并被覆盖。 例如，给定两个属性文件a.properties和b.properties，请考虑以下两个使用@PropertySource注释引用它们的配置类：
 * </p>
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/a.properties")
 * public class ConfigA { }
 *
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/b.properties")
 * public class ConfigB { }
 * </pre>
 *
 * <p>
 *     覆盖顺序取决于这些类在应用程序上下文中注册的顺序。
 * </p>
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx =
 *     new AnnotationConfigApplicationContext();
 * ctx.register(ConfigA.class);
 * ctx.register(ConfigB.class);
 * ctx.refresh();
 * </pre>
 *
 * <p>
 *     在上面的场景中，b.properties中的属性将覆盖a.properties中存在的任何重复项，因为ConfigB是最后注册的。
 * </p>
 * <p>
 *      在某些情况下，使用@ProperySource注解时严格控制属性来源顺序可能不切实际。
 *      例如，如果上面的@Configuration类是通过组件扫描注册的，则排序很难预测。
 *      在这种情况下 - 如果重写很重要 - 建议用户回退使用编程的PropertySource API。
 *      有关详细信息，请参阅ConfigurableEnvironment和MutablePropertySources javadocs。
 * </p>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 3.1
 * @see PropertySources
 * @see Configuration
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.env.ConfigurableEnvironment#getPropertySources()
 * @see org.springframework.core.env.MutablePropertySources
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySources.class)
public @interface PropertySource {

	/**
	 * Indicate the name of this property source. If omitted, a name will
	 * be generated based on the description of the underlying resource.
	 * @see org.springframework.core.env.PropertySource#getName()
	 * @see org.springframework.core.io.Resource#getDescription()
	 */
	String name() default "";

	/**
	 * Indicate the resource location(s) of the properties file to be loaded.
	 * For example, {@code "classpath:/com/myco/app.properties"} or
	 * {@code "file:/path/to/file"}.
	 * <p>Resource location wildcards (e.g. *&#42;/*.properties) are not permitted;
	 * each location must evaluate to exactly one {@code .properties} resource.
	 * <p>${...} placeholders will be resolved against any/all property sources already
	 * registered with the {@code Environment}. See {@linkplain PropertySource above}
	 * for examples.
	 * <p>Each location will be added to the enclosing {@code Environment} as its own
	 * property source, and in the order declared.
	 */
	String[] value();

	/**
	 * Indicate if failure to find the a {@link #value() property resource} should be
	 * ignored.
	 * <p>{@code true} is appropriate if the properties file is completely optional.
	 * Default is {@code false}.
	 * @since 4.0
	 */
	boolean ignoreResourceNotFound() default false;

	/**
	 * A specific character encoding for the given resources, e.g. "UTF-8".
	 * @since 4.3
	 */
	String encoding() default "";

	/**
	 * Specify a custom {@link PropertySourceFactory}, if any.
	 * <p>By default, a default factory for standard resource files will be used.
	 * @since 4.3
	 * @see org.springframework.core.io.support.DefaultPropertySourceFactory
	 * @see org.springframework.core.io.support.ResourcePropertySource
	 */
	Class<? extends PropertySourceFactory> factory() default PropertySourceFactory.class;

}
