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

package org.springframework.context.annotation;

/**
 * A variation of {@link ImportSelector} that runs after all {@code @Configuration} beans
 * have been processed. This type of selector can be particularly useful when the selected
 * imports are {@code @Conditional}.
 *
 * <p>Implementations can also extend the {@link org.springframework.core.Ordered}
 * interface or use the {@link org.springframework.core.annotation.Order} annotation to
 * indicate a precedence against other {@link DeferredImportSelector}s.
 *
 *
 * <p>
 *      ImportSelector的一个变体，在所有的@Configuration bean被处理之后运行。 当所选的导入是@Conditional时，这种类型的选择器特别有用。
 * <p>
 *     实现还可以扩展org.springframework.core.Ordered接口或使用org.springframework.core.annotation.Order注释来指示其他DeferredImportSelector的优先级。
 *
 * @author Phillip Webb
 * @since 4.0
 */
public interface DeferredImportSelector extends ImportSelector {

}
