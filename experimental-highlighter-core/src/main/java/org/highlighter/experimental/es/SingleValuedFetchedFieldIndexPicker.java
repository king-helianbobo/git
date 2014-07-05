package org.highlighter.experimental.es;

import org.search.highlighter.experimental.Snippet;

/**
 * FetchedFieldIndexPicker that always returns the first field.
 */
public class SingleValuedFetchedFieldIndexPicker implements FetchedFieldIndexPicker {
    @Override
    public int index(Snippet snippet) {
        return 0;
    }
}
