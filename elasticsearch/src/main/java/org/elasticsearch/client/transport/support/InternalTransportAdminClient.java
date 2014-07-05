/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.transport.support;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClientNodesService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

/**
 *
 */
public class InternalTransportAdminClient extends AbstractComponent implements AdminClient {

    private final TransportClientNodesService nodesService;

    private final InternalTransportIndicesAdminClient indicesAdminClient;

    private final InternalTransportClusterAdminClient clusterAdminClient;

    @Inject
    public InternalTransportAdminClient(Settings settings, TransportClientNodesService nodesService,
                                        InternalTransportIndicesAdminClient indicesAdminClient, InternalTransportClusterAdminClient clusterAdminClient) {
        super(settings);
        this.nodesService = nodesService;
        this.indicesAdminClient = indicesAdminClient;
        this.clusterAdminClient = clusterAdminClient;
    }

    @Override
    public IndicesAdminClient indices() {
        return indicesAdminClient;
    }

    @Override
    public ClusterAdminClient cluster() {
        return clusterAdminClient;
    }
}
