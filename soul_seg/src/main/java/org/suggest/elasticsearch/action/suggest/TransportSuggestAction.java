package org.suggest.elasticsearch.action.suggest;

import static org.elasticsearch.common.collect.Lists.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.TransportBroadcastOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.collect.ImmutableSortedSet;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.suggest.elasticsearch.service.ShardSuggestService;

public class TransportSuggestAction
		extends
			TransportBroadcastOperationAction<SuggestRequest, SuggestResponse, ShardSuggestRequest, ShardSuggestResponse> {

	private final IndicesService indicesService; // we need indicesService

	@Inject
	public TransportSuggestAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService,
			IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	@Override
	protected String transportAction() {
		return SuggestAction.NAME; // use TransportSuggestAction
	}

	@Override
	protected String executor() {
		return ThreadPool.Names.SEARCH;
	}

	@Override
	protected SuggestRequest newRequest() {
		return new SuggestRequest();
	}

	@Override
	protected SuggestResponse newResponse(SuggestRequest request,
			AtomicReferenceArray shardsResponses, ClusterState clusterState) {
		logger.trace("Entered TransportSuggestAction.newResponse()");
		int successfulShards = 0; // number of Shards which is successful
		int failedShards = 0; // number of failed Shards
		List<ShardOperationFailedException> shardFailures = null;
		List<String> items = Lists.newArrayList();
		for (int i = 0; i < shardsResponses.length(); i++) {
			Object shardResponse = shardsResponses.get(i);
			if (shardResponse == null) {
				failedShards++;
			} else if (shardResponse instanceof BroadcastShardOperationFailedException) {
				failedShards++;
				if (shardFailures == null) {
					shardFailures = newArrayList();
				}
				shardFailures
						.add(new DefaultShardOperationFailedException(
								(BroadcastShardOperationFailedException) shardResponse));
			} else if (shardResponse instanceof ShardSuggestResponse) {
				ShardSuggestResponse shardSuggestResponse = (ShardSuggestResponse) shardResponse;
				List<String> shardItems = shardSuggestResponse.suggestions();
				items.addAll(shardItems);
				successfulShards++;
			} else {
				successfulShards++;
			}
		}
		List<String> resultItems = ImmutableSortedSet.copyOf(items).asList();

		// number of items at most request.size()
		return new SuggestResponse(resultItems.subList(0,
				Math.min(resultItems.size(), request.size())),
				shardsResponses.length(), successfulShards, failedShards,
				shardFailures);
	}

	@Override
	protected ShardSuggestRequest newShardRequest() {
		return new ShardSuggestRequest();
	}

	@Override
	protected ShardSuggestRequest newShardRequest(ShardRouting shard,
			SuggestRequest request) {
		return new ShardSuggestRequest(shard.index(), shard.id(), request);
	}

	@Override
	protected ShardSuggestResponse newShardResponse() {
		return new ShardSuggestResponse();
	}

	@Override
	protected ShardSuggestResponse shardOperation(ShardSuggestRequest request)
			throws ElasticSearchException {
		logger.trace("Entered TransportSuggestAction.shardOperation()");
		IndexService indexService = indicesService.indexServiceSafe(request
				.index());
		ShardSuggestService suggestShardService = indexService
				.shardInjectorSafe(request.shardId()).getInstance(
						ShardSuggestService.class);
		return suggestShardService.suggest(request);
	}

	@Override
	protected GroupShardsIterator shards(ClusterState clusterState,
			SuggestRequest request, String[] concreteIndices) {
		logger.trace("Entered TransportSuggestAction.shards()");
		return clusterService.operationRouting().searchShards(clusterState,
				request.indices(), concreteIndices, null, null);
	}

	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state,
			SuggestRequest request) {
		return state.blocks()
				.globalBlockedException(ClusterBlockLevel.METADATA);
	}

	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state,
			SuggestRequest request, String[] concreteIndices) {
		return state.blocks().indicesBlockedException(
				ClusterBlockLevel.METADATA, concreteIndices);
	}

}
