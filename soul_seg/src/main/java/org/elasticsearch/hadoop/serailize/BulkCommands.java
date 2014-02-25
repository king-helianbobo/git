/*
 * Copyright 2013 the original author or authors.
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
package org.elasticsearch.hadoop.serailize;

import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;

/**
 * Handles the instantiation of bulk commands.
 */
public abstract class BulkCommands {

    public static Command create(Settings settings) {

        String operation = settings.getOperation();

        if (ConfigurationOptions.ES_OPERATION_CREATE.equals(operation)) {
            return new CreateCommand(settings);
        }
        if (ConfigurationOptions.ES_OPERATION_INDEX.equals(operation)) {
            return new IndexCommand(settings);
        }
        if (ConfigurationOptions.ES_OPERATION_UPDATE.equals(operation)) {
            return new UpdateCommand(settings);
        }

        throw new IllegalArgumentException("Unknown operation " + operation);
    }
}
