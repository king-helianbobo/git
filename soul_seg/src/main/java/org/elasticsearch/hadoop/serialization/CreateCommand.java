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
package org.elasticsearch.hadoop.serialization;

import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.util.StringUtils;

class CreateCommand extends AbstractCommand {

    private final byte[] HEADER = ("{\"" + ConfigurationOptions.ES_OPERATION_CREATE + "\":{}}\n").getBytes(StringUtils.UTF_8);
    private final byte[] HEADER_PREFIX = ("{\"" + ConfigurationOptions.ES_OPERATION_CREATE + "\":{\"_id\":\"").getBytes(StringUtils.UTF_8);
    private final byte[] HEADER_SUFFIX = ("\"}}\n").getBytes(StringUtils.UTF_8);

    CreateCommand(Settings settings) {
        super(settings);
    }

    @Override
    protected byte[] headerPrefix() {
        return HEADER_PREFIX;
    }

    @Override
    protected byte[] headerSuffix() {
        return HEADER_SUFFIX;
    }


    @Override
    protected byte[] header() {
        return HEADER;
    }
}
