package org.suggest.elasticsearch.action.refresh;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse;


public class ShardSuggestRefreshResponse extends BroadcastShardOperationResponse {

    public ShardSuggestRefreshResponse() {}

    public ShardSuggestRefreshResponse(String index, int shardId) {
        super(index, shardId);
    }

}
