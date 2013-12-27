package org.suggest.elasticsearch.action.suggest;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class SuggestResponse extends BroadcastOperationResponse {

	private List<String> suggestions;

	public SuggestResponse() {
	}

	public SuggestResponse(List<String> suggestions, int totalShards,
			int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.suggestions = suggestions;
	}

	public List<String> suggestions() {
		return Lists.newArrayList(suggestions);
	}

	public List<String> getSuggestions() {
		return Lists.newArrayList(suggestions);
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		suggestions = (List<String>) in.readGenericValue();
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeGenericValue(suggestions);
	}
}
