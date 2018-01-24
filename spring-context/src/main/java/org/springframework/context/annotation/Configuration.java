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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Indicates that a class declares one or more {@link Bean @Bean} methods and
 * may be processed by the Spring container to generate bean definitions and
 * service requests for those beans at runtime, for example:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // instantiate, configure and return bean ...
 *     }
 * }</pre>
 *
 * <h2>Bootstrapping {@code @Configuration} classes</h2>
 *
 * <h3>Via {@code AnnotationConfigApplicationContext}</h3>
 *
 * {@code @Configuration} classes are typically bootstrapped using either
 * {@link AnnotationConfigApplicationContext} or its web-capable variant,
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext}. A simple example with the former follows:
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * See {@link AnnotationConfigApplicationContext} Javadoc for further details and see
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} for {@code web.xml} configuration instructions.
 *
 * <h3>Via Spring {@code <beans>} XML</h3>
 *
 * <p>As an alternative to registering {@code @Configuration} classes directly against an
 * {@code AnnotationConfigApplicationContext}, {@code @Configuration} classes may be
 * declared as normal {@code <bean>} definitions within Spring XML files:
 * <pre class="code">
 * {@code
 * <beans>
 *    <context:annotation-config/>
 *    <bean class="com.acme.AppConfig"/>
 * </beans>}</pre>
 *
 * In the example above, {@code <context:annotation-config/>} is required in order to
 * enable {@link ConfigurationClassPostProcessor} and other annotation-related
 * post processors that facilitate handling {@code @Configuration} classes.
 *
 * <h3>Via component scanning</h3>
 *
 * <p>{@code @Configuration} is meta-annotated with {@link Component @Component}, therefore
 * {@code @Configuration} classes are candidates for component scanning (typically using
 * Spring XML's {@code <context:component-scan/>} element) and therefore may also take
 * advantage of {@link Autowired @Autowired}/{@link javax.inject.Inject @Inject}
 * like any regular {@code @Component}. In particular, if a single constructor is present
 * autowiring semantics will be applied transparently:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *     private final SomeBean someBean;
 *
 *     public AppConfig(SomeBean someBean) {
 *         this.someBean = someBean;
 *     }
 *
 *     // &#064;Bean definition using "SomeBean"
 *
 * }</pre>
 *
 * <p>{@code @Configuration} classes may not only be bootstrapped using
 * component scanning, but may also themselves <em>configure</em> component scanning using
 * the {@link ComponentScan @ComponentScan} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 *
 * See the {@link ComponentScan @ComponentScan} javadoc for details.
 *
 * <h2>Working with externalized values</h2>
 *
 * <h3>Using the {@code Environment} API</h3>
 *
 * Externalized values may be looked up by injecting the Spring
 * {@link org.springframework.core.env.Environment} into a {@code @Configuration}
 * class the usual (e.g. using the {@code @Autowired} annotation):
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064Autowired Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         MyBean myBean = new MyBean();
 *         myBean.setName(env.getProperty("bean.name"));
 *         return myBean;
 *     }
 * }</pre>
 *
 * Properties resolved through the {@code Environment} reside in one or more "property
 * source" objects, and {@code @Configuration} classes may contribute property sources to
 * the {@code Environment} object using
 * the {@link org.springframework.core.env.PropertySources @PropertySources} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Inject Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(env.getProperty("bean.name"));
 *     }
 * }</pre>
 *
 * See {@link org.springframework.core.env.Environment Environment}
 * and {@link PropertySource @PropertySource} Javadoc for further details.
 *
 * <h3>Using the {@code @Value} annotation</h3>
 *
 * Externalized values may be 'wired into' {@code @Configuration} classes using
 * the {@link Value @Value} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Value("${bean.name}") String beanName;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(beanName);
 *     }
 * }</pre>
 *
 * This approach is most useful when using Spring's
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer}, usually enabled via XML with
 * {@code <context:property-placeholder/>}.  See the section below on composing
 * {@code @Configuration} classes with Spring XML using {@code @ImportResource},
 * see {@link Value @Value} Javadoc, and see {@link Bean @Bean} Javadoc for details on working with
 * {@code BeanFactoryPostProcessor} types such as
 * {@code PropertySourcesPlaceholderConfigurer}.
 *
 * <h2>Composing {@code @Configuration} classes</h2>
 *
 * <h3>With the {@code @Import} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be composed using the {@link Import @Import} annotation,
 * not unlike the way that {@code <import>} works in Spring XML. Because
 * {@code @Configuration} objects are managed as Spring beans within the container,
 * imported configurations may be injected the usual way (e.g. via constructor injection):
 *
 * <pre class="code">
 * &#064;Configuration
 * public class DatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return DataSource
 *     }
 * }
 *
 * &#064;Configuration
 * &#064;Import(DatabaseConfig.class)
 * public class AppConfig {
 *
 *     private final DatabaseConfig dataConfig;
 *
 *     public AppConfig(DatabaseConfig dataConfig) {
 *         this.dataConfig = dataConfig;
 *     }
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // reference the dataSource() bean method
 *         return new MyBean(dataConfig.dataSource());
 *     }
 * }</pre>
 *
 * Now both {@code AppConfig} and the imported {@code DatabaseConfig} can be bootstrapped
 * by registering only {@code AppConfig} against the Spring context:
 *
 * <pre class="code">
 * new AnnotationConfigApplicationContext(AppConfig.class);</pre>
 *
 * <h3>With the {@code @Profile} annotation</h3>
 *
 * {@code @Configuration} classes may be marked with the {@link Profile @Profile} annotation to
 * indicate they should be processed only if a given profile or profiles are <em>active</em>:
 *
 * <pre class="code">
 * &#064;Profile("development")
 * &#064;Configuration
 * public class EmbeddedDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return embedded DataSource
 *     }
 * }
 *
 * &#064;Profile("production")
 * &#064;Configuration
 * public class ProductionDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return production DataSource
 *     }
 * }</pre>
 *
 * Alternatively, you may also declare profile conditions at the {@code @Bean} method level,
 * e.g. for alternative bean variants within the same configuration class:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class ProfileDatabaseConfig {
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("development")
 *     public DataSource embeddedDatabase() { ... }
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("production")
 *     public DataSource productionDatabase() { ... }
 * }</pre>
 *
 * See the {@link Profile @Profile} and {@link org.springframework.core.env.Environment}
 * javadocs for further details.
 *
 * <h3>With Spring XML using the {@code @ImportResource} annotation</h3>
 *
 * As mentioned above, {@code @Configuration} classes may be declared as regular Spring
 * {@code <bean>} definitions within Spring XML files. It is also possible to
 * import Spring XML configuration files into {@code @Configuration} classes using
 * the {@link ImportResource @ImportResource} annotation. Bean definitions imported from
 * XML can be injected the usual way (e.g. using the {@code Inject} annotation):
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ImportResource("classpath:/com/acme/database-config.xml")
 * public class AppConfig {
 *
 *     &#064Inject DataSource dataSource; // from XML
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // inject the XML-defined dataSource bean
 *         return new MyBean(this.dataSource);
 *     }
 * }</pre>
 *
 * <h3>With nested {@code @Configuration} classes</h3>
 *
 * {@code @Configuration} classes may be nested within one another as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(dataSource);
 *     }
 *
 *     &#064;Configuration
 *     static class DatabaseConfig {
 *         &#064;Bean
 *         DataSource dataSource() {
 *             return new EmbeddedDatabaseBuilder().build();
 *         }
 *     }
 * }</pre>
 *
 * When bootstrapping such an arrangement, only {@code AppConfig} need be registered
 * against the application context. By virtue of being a nested {@code @Configuration}
 * class, {@code DatabaseConfig} <em>will be registered automatically</em>. This avoids
 * the need to use an {@code @Import} annotation when the relationship between
 * {@code AppConfig} {@code DatabaseConfig} is already implicitly clear.
 *
 * <p>Note also that nested {@code @Configuration} classes can be used to good effect
 * with the {@code @Profile} annotation to provide two options of the same bean to the
 * enclosing {@code @Configuration} class.
 *
 * <h2>Configuring lazy initialization</h2>
 *
 * <p>By default, {@code @Bean} methods will be <em>eagerly instantiated</em> at container
 * bootstrap time.  To avoid this, {@code @Configuration} may be used in conjunction with
 * the {@link Lazy @Lazy} annotation to indicate that all {@code @Bean} methods declared within
 * the class are by default lazily initialized. Note that {@code @Lazy} may be used on
 * individual {@code @Bean} methods as well.
 *
 * <h2>Testing support for {@code @Configuration} classes</h2>
 *
 * The Spring <em>TestContext framework</em> available in the {@code spring-test} module
 * provides the {@code @ContextConfiguration} annotation, which as of Spring 3.1 can
 * accept an array of {@code @Configuration} {@code Class} objects:
 *
 * <pre class="code">
 * &#064;RunWith(SpringJUnit4ClassRunner.class)
 * &#064;ContextConfiguration(classes={AppConfig.class, DatabaseConfig.class})
 * public class MyTests {
 *
 *     &#064;Autowired MyBean myBean;
 *
 *     &#064;Autowired DataSource dataSource;
 *
 *     &#064;Test
 *     public void test() {
 *         // assertions against myBean ...
 *     }
 * }</pre>
 *
 * See TestContext framework reference documentation for details.
 *
 * <h2>Enabling built-in Spring features using {@code @Enable} annotations</h2>
 *
 * Spring features such as asynchronous method execution, scheduled task execution,
 * annotation driven transaction management, and even Spring MVC can be enabled and
 * configured from {@code @Configuration}
 * classes using their respective "{@code @Enable}" annotations. See
 * {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync},
 * {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling},
 * {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement},
 * {@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy},
 * and {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}
 * for details.
 *
 * <h2>Constraints when authoring {@code @Configuration} classes</h2>
 *
 * <ul>
 * <li>Configuration classes must be provided as classes (i.e. not as instances returned
 * from factory methods), allowing for runtime enhancements through a generated subclass.
 * <li>Configuration classes must be non-final.
 * <li>Configuration classes must be non-local (i.e. may not be declared within a method).
 * <li>Any nested configuration classes must be declared as {@code static}.
 * <li>{@code @Bean} methods may not in turn create further configuration classes
 * (any such instances will be treated as regular beans, with their configuration
 * annotations remaining undetected).
 * </ul>
 *
 * <p>
 *     指示一个类声明一个或多个@Bean方法，并且可以由Spring容器处理，以便在运行时为这些bean生成bean定义和服务请求，例如：
 * </p>
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // instantiate, configure and return bean ...
 *     }
 * }</pre>
 *
 * <h2>Bootstrapping {@code @Configuration} classes</h2>
 *
 * <h3>Via {@code AnnotationConfigApplicationContext}</h3>
 *
 * <p>
 *     &#064;Configuration 类通常使用AnnotationConfigApplicationContext或其支持Web的变体AnnotationConfigWebApplicationContext进行引导。
 *     前者的一个简单例子如下：
 * </p>
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * <p>
 *     有关更多详细信息，请参阅AnnotationConfigApplicationContext Javadoc，有关web.xml配置说明，请参阅AnnotationConfigWebApplicationContext。
 * </p>
 *
 * <h3>Via Spring {@code <beans>} XML</h3>
 *
 * <p>
 *     作为直接针对AnnotationConfigApplicationContext注册@Configuration类的替代方法，可以在Spring XML文件中将@Configuration类声明为正常的<bean>定义：
 * </p>
 * <pre class="code">
 * {@code
 * <beans>
 *    <context:annotation-config/>
 *    <bean class="com.acme.AppConfig"/>
 * </beans>}
 * </pre>
 *
 *
 * <p>
 *     在上面的示例中，为了启用ConfigurationClassPostProcessor和其他与注释有关的后置处理器，需要<context：annotation-config />来处理@Configuration类。
 * </p>
 *
 * <h3>Via component scanning</h3>
 *
 * <p>
 *     &#064;Configuration是用&#064;Component进行元注释的，因此@Configuration类是组件扫描的候选对象（通常使用Spring XML的<context:component-scan />元素），
 *     因此也可以像任何常规的@ @元素那样利用@ Autowired / @ Inject 零件。 特别是，如果存在单个构造函数，则自动装配语义将被透明地应用：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *     private final SomeBean someBean;
 *
 *     public AppConfig(SomeBean someBean) {
 *         this.someBean = someBean;
 *     }
 *
 *     // &#064;Bean definition using "SomeBean"
 *
 * }</pre>
 * <p>
 *     &#064;Configuration类不仅可以使用组件扫描进行引导，还可以使用@ComponentScan注释来配置组件扫描：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 * <p>
 *     有关详细信息，请参阅@ComponentScan javadoc。
 * </p>
 * <h2>Working with externalized values</h2>
 *
 * <h3>Using the {@code Environment} API</h3>
 * <p>
 *     通过将Spring org.springframework.core.env.Environment注入@Configuration类（例如使用@Autowired注解），可以查找外部化的值：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064Autowired Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         MyBean myBean = new MyBean();
 *         myBean.setName(env.getProperty("bean.name"));
 *         return myBean;
 *     }
 * }</pre>
 * <p>
 *     通过Environment解决的属性驻留在一个或多个“属性源”对象中，而@Configuration类可以使用@PropertySources注释向Environment对象提供属性源：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Inject Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(env.getProperty("bean.name"));
 *     }
 * }</pre>
 * <p>
 *     有关更多详细信息，请参阅Environment和@PropertySource Javadoc。
 * </p>
 *
 * <h3>Using the {@code @Value} annotation</h3>
 *
 * <p>
 *     外部化的值可以通过@Value注解'连接到@Configuration类中：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Value("${bean.name}") String beanName;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(beanName);
 *     }
 * }</pre>
 * <p>
 *     这种方法在使用Spring的PropertySourcesPlaceholderConfigurer时非常有用，通常通过XML通过<context：property-placeholder />来启用。 有关使用BeanFactoryPostProcessor类型（例如PropertySourcesPlaceholderConfigurer）的详细信息，
 *     请参阅下面有关使用@ImportResource使用Spring XML构建@Configuration类的部分，请参阅@Value Javadoc，并参阅@Bean Javadoc。
 * </p>
 * <h2>Composing {@code @Configuration} classes</h2>
 *
 * <h3>With the {@code @Import} annotation</h3>
 * <p>
 *     &#064;Configuration类可以使用@Import注释编写，与<import>在Spring XML中的工作方式不同。
 *     由于@Configuration对象是作为容器内的Spring bean进行管理的，所以导入的配置可以按通常的方式注入（例如通过构造函数注入）：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * public class DatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return DataSource
 *     }
 * }
 *
 * &#064;Configuration
 * &#064;Import(DatabaseConfig.class)
 * public class AppConfig {
 *
 *     private final DatabaseConfig dataConfig;
 *
 *     public AppConfig(DatabaseConfig dataConfig) {
 *         this.dataConfig = dataConfig;
 *     }
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // reference the dataSource() bean method
 *         return new MyBean(dataConfig.dataSource());
 *     }
 * }</pre>
 * <p>
 *     现在AppConfig和导入的DatabaseConfig都可以通过在Spring上下文中注册AppConfig来引导：
 * </p>
 * <pre class="code">
 * new AnnotationConfigApplicationContext(AppConfig.class);</pre>
 *
 * <h3>With the {@code @Profile} annotation</h3>
 *
 * <p>
 *     &#064;Configuration类可以使用@Profile注释标记，以表明只有给定的一个或多个配置文件处于活动状态时才应该处理它们：
 * </p>
 * <pre class="code">
 * &#064;Profile("development")
 * &#064;Configuration
 * public class EmbeddedDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return embedded DataSource
 *     }
 * }
 *
 * &#064;Profile("production")
 * &#064;Configuration
 * public class ProductionDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return production DataSource
 *     }
 * }</pre>
 * <p>
 *     或者，您也可以在@Bean方法级别声明配置文件条件，例如 用于相同配置类中的替代bean变体：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * public class ProfileDatabaseConfig {
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("development")
 *     public DataSource embeddedDatabase() { ... }
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("production")
 *     public DataSource productionDatabase() { ... }
 * }</pre>
 * <p>有关更多详细信息，请参阅@Profile和org.springframework.core.env.Environment javadocs。</p>
 *
 * <h3>With Spring XML using the {@code @ImportResource} annotation</h3>
 *
 * <p>
 *     如上所述，@Configuration类可以在Spring XML文件中声明为常规的Spring <bean>定义。 也可以使用@ImportResource注解将Spring XML配置文件导入到@Configuration类中。 从XML导入的Bean定义可以通常的方式注入（例如使用Inject注解）：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;ImportResource("classpath:/com/acme/database-config.xml")
 * public class AppConfig {
 *
 *     &#064Inject DataSource dataSource; // from XML
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // inject the XML-defined dataSource bean
 *         return new MyBean(this.dataSource);
 *     }
 * }</pre>
 *
 * <h3>With nested {@code @Configuration} classes</h3>
 *
 * <p>
 *     &#064;Configuration类可以如下嵌套在一起：
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(dataSource);
 *     }
 *
 *     &#064;Configuration
 *     static class DatabaseConfig {
 *         &#064;Bean
 *         DataSource dataSource() {
 *             return new EmbeddedDatabaseBuilder().build();
 *         }
 *     }
 * }</pre>
 * <p>
 *     当引导这样的安排时，只有AppConfig需要针对应用上下文进行注册。 由于是一个嵌套的@Configuration类，DatabaseConfig将被自动注册。 这样可以避免在AppConfig DatabaseConfig之间的关系已经隐式清除时使用@Import注释。
 * </p>
 * <p>
 *     还要注意，可以使用嵌套的@Configuration类对@Profile注释产生良好的效果，以将同一个bean的两个选项提供给封闭的@Configuration类。
 * </p>
 *
 * <h2>Configuring lazy initialization</h2>
 *
 * <p>
 *     默认情况下，@Bean方法将在容器引导时间被迫切地实例化。 为了避免这种情况，可以将@Configuration与@Lazy注释结合使用，以表示在类中声明的所有@Bean方法在默认情况下是懒惰地初始化的。 请注意@Lazy也可以用于单独的@Bean方法。
 * </p>
 *
 * <h2>Testing support for {@code @Configuration} classes</h2>
 *
 * <p>
 *     Spring-Test模块中提供的Spring TestContext框架提供@ContextConfiguration注解，从Spring 3.1开始，它可以接受一个@Configuration Class对象的数组：
 * </p>
 * <pre class="code">
 * &#064;RunWith(SpringJUnit4ClassRunner.class)
 * &#064;ContextConfiguration(classes={AppConfig.class, DatabaseConfig.class})
 * public class MyTests {
 *
 *     &#064;Autowired MyBean myBean;
 *
 *     &#064;Autowired DataSource dataSource;
 *
 *     &#064;Test
 *     public void test() {
 *         // assertions against myBean ...
 *     }
 * }</pre>
 * <p>
 *     有关详细信息，请参阅TestContext框架参考文档。
 * </p>
 *
 * <h2>Enabling built-in Spring features using {@code @Enable} annotations</h2>
 *
 * <p>
 *     诸如异步方法执行，计划任务执行，注解驱动事务管理，甚至Spring MVC等Spring特性可以使用各自的@Enable注解从@Configuration类中启用和配置。 有关详细信息，请参阅@EnableAsync，@EnableScheduling，@EnableTransactionManagement，@EnableAspectJAutoProxy和@EnableWebMvc。
 * </p>
 *
 * <h2>Constraints when authoring {@code @Configuration} classes</h2>
 *
 * <ul>
 * <li>配置类必须以类的形式提供（即不能作为从工厂方法返回的实例），允许通过生成的子类进行运行时增强。
 * <li>配置类不能为final
 * <li>配置类必须是非本地的（即不能在方法中声明）。
 * <li>任何嵌套的配置类都必须声明为{@code static}.
 * <li>@Bean方法可能不会创建进一步的配置类（任何这样的实例将被视为常规bean，其配置注释未被检测到）。
 * </ul>
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 3.0
 * @see Bean
 * @see Profile
 * @see Import
 * @see ImportResource
 * @see ComponentScan
 * @see Lazy
 * @see PropertySource
 * @see AnnotationConfigApplicationContext
 * @see ConfigurationClassPostProcessor
 * @see org.springframework.core.env.Environment
 * @see org.springframework.test.context.ContextConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

	/**
	 * Explicitly specify the name of the Spring bean definition associated
	 * with this Configuration class. If left unspecified (the common case),
	 * a bean name will be automatically generated.
	 * <p>The custom name applies only if the Configuration class is picked up via
	 * component scanning or supplied directly to a {@link AnnotationConfigApplicationContext}.
	 * If the Configuration class is registered as a traditional XML bean definition,
	 * the name/id of the bean element will take precedence.
	 * @return the suggested component name, if any (or empty String otherwise)
	 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
	 */
	String value() default "";

}
