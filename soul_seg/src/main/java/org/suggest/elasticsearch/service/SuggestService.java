package org.suggest.elasticsearch.service;

import java.util.Iterator;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.service.IndexShard;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshRequest;
import org.suggest.elasticsearch.action.refresh.TransportSuggestRefreshAction;

public class SuggestService extends AbstractLifecycleComponent<SuggestService> {

	private final TimeValue suggestRefreshInterval; // 更新间隔
	private final boolean suggestRefreshDisabled; // 是否关闭更新
	private volatile Thread suggestUpdaterThread;
	private volatile boolean closed;
	private final TransportSuggestRefreshAction suggestRefreshAction;
	private final ClusterService clusterService;
	private final IndicesService indicesService;

	@Inject
	public SuggestService(Settings settings,
			TransportSuggestRefreshAction suggestRefreshAction,
			ClusterService clusterService, IndicesService indicesService) {
		super(settings);
		this.suggestRefreshAction = suggestRefreshAction;
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		suggestRefreshDisabled = settings.getAsBoolean(
				"suggest.refresh_disabled", false);
		suggestRefreshInterval = settings.getAsTime("suggest.refresh_interval",
				TimeValue.timeValueMinutes(10));
	}

	@Override
	protected void doStart() throws ElasticSearchException {
		if (suggestRefreshDisabled) {
			logger.info("Suggest component started with out refreshing automatically");
		} else {
			suggestUpdaterThread = EsExecutors.daemonThreadFactory(settings,
					"suggest_updater").newThread(new SuggestUpdaterThread());
			suggestUpdaterThread.start();
			logger.info("Suggest component started with refresh interval [{}]",
					suggestRefreshInterval);
		}

		// When the instance is shut down or the index is deleted
		indicesService.indicesLifecycle().addListener(
				new IndicesLifecycle.Listener() {
					@Override
					public void beforeIndexClosed(IndexService indexService) {
						for (Iterator<IndexShard> shardServiceIterator = indexService
								.iterator(); shardServiceIterator.hasNext();) {
							IndexShard indexShard = shardServiceIterator.next();
							ShardSuggestService suggestShardService = indexService
									.shardInjectorSafe(
											indexShard.shardId().id())
									.getInstance(ShardSuggestService.class);
							suggestShardService.shutDown();
						}
					}
				});

		// when the shard is deleted (or moved to another cluster instance)
		// using this in the above case fails, because i cannot get the
		// indexService anymore at beforeIndexShardClosed()
		indicesService.indicesLifecycle().addListener(
				new IndicesLifecycle.Listener() {
					@Override
					public void beforeIndexShardClosed(ShardId shardId,
							@Nullable IndexShard indexShard) {
						IndexService indexService = indicesService
								.indexService(shardId.index().name());
						if (indexService != null) {
							ShardSuggestService suggestShardService = indexService
									.shardInjectorSafe(shardId.id())
									.getInstance(ShardSuggestService.class);
							suggestShardService.shutDown();
						}
					}
				});
	}

	@Override
	protected void doClose() throws ElasticSearchException {
		if (closed) {
			return;
		}
		closed = true;

		if (suggestUpdaterThread != null) {
			suggestUpdaterThread.interrupt();
		}
		logger.info("Suggest component stopped");
	}

	@Override
	protected void doStop() throws ElasticSearchException {
	}

	public class SuggestUpdaterThread implements Runnable {
		@Override
		public void run() {
			while (!closed) {
				DiscoveryNode node = clusterService.localNode();
				boolean isClusterStarted = clusterService.lifecycleState()
						.equals(Lifecycle.State.STARTED);

				if (isClusterStarted && node != null && node.isMasterNode()) {
					StopWatch sw = new StopWatch().start();
					suggestRefreshAction.execute(new SuggestRefreshRequest())
							.actionGet();
					logger.info(
							"Suggest update took [{}], next update in [{}]", sw
									.stop().totalTime(), suggestRefreshInterval);
				} else {
					if (node != null) {
						logger.debug(
								"[{}]/[{}] is not master node, not triggering update",
								node.getId(), node.getName());
					}
				}

				try {
					Thread.sleep(suggestRefreshInterval.millis());
				} catch (InterruptedException e1) {
					continue;
				}
			}
		}
	}
}
