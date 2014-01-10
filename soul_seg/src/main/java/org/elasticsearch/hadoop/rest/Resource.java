package org.elasticsearch.hadoop.rest;

/**
 * ElasticSearch Rest Resource (index or query).
 */
public class Resource {

	private final StringBuilder resource;
	// cleaned up index and type with trailing "/"
	private final String root;
	private final String type;
	private final String index;

	public Resource(String resource) {
		this.resource = new StringBuilder(resource);
		int location = resource.lastIndexOf("_");
		if (location <= 0) {
			location = resource.length();
		}
		String localRoot = resource.substring(0, location);
		if (!localRoot.endsWith("/")) {
			localRoot = localRoot + "/";
		}
		root = localRoot;
		location = localRoot.substring(0, root.length() - 1).lastIndexOf("/");
		type = root.substring(location + 1, root.length() - 1);
		index = root.substring(0, location);
	}

	String bulkIndexing() {
		return root + "_bulk";
	}

	// https://github.com/elasticsearch/elasticsearch/issues/2726
	String targetShards() {
		return root + "_search_shards";
	}

	String mapping() {
		return root + "_mapping";
	}

	String indexAndType() { // get root resource
		return root;
	}

	public String type() { // get type
		return type;
	}

	public String index() { // get index
		return index;
	}
}
