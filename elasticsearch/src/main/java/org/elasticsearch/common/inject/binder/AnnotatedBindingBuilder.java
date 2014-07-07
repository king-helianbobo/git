/**
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.common.inject.binder;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link org.elasticsearch.common.inject.Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

    /**
     * See the EDSL examples at {@link org.elasticsearch.common.inject.Binder}.
     */
    LinkedBindingBuilder<T> annotatedWith(
            Class<? extends Annotation> annotationType);

    /**
     * See the EDSL examples at {@link org.elasticsearch.common.inject.Binder}.
     */
    LinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
