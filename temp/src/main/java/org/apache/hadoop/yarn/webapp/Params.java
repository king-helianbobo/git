package org.apache.hadoop.yarn.webapp;

import org.apache.hadoop.classification.InterfaceAudience;

/**
 * Public static constants for webapp parameters. Do NOT put any private or
 * application specific constants here as they're part of the API for users of
 * the controllers and views.
 */
@InterfaceAudience.LimitedPrivate({ "YARN", "MapReduce" })
public interface Params {
	static final String TITLE = "title";
	static final String TITLE_LINK = "title.href";
	static final String USER = "user";
	static final String ERROR_DETAILS = "error.details";
}