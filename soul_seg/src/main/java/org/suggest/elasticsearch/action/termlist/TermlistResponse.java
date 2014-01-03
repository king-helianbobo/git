package org.suggest.elasticsearch.action.termlist;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.soul.utility.CompactHashMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A response for TermList action.
 */
public class TermlistResponse extends BroadcastOperationResponse {

	private Map<String, TermInfoPojo> map;

	TermlistResponse() {
	}

	TermlistResponse(int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures,
			Map<String, TermInfoPojo> map) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.map = map;
	}

	public Map<String, TermInfoPojo> getTermlist() {
		return map;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		int n = in.readInt();
		map = new CompactHashMap<String, TermInfoPojo>();
		for (int i = 0; i < n; i++) {
			String text = in.readString();
			TermInfoPojo t = new TermInfoPojo();
			t.readFrom(in);
			map.put(text, t);
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeInt(map.size());
		for (Map.Entry<String, TermInfoPojo> t : map.entrySet()) {
			out.writeString(t.getKey());
			t.getValue().writeTo(out);
		}
	}
}