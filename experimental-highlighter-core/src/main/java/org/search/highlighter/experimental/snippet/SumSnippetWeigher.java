package org.search.highlighter.experimental.snippet;

import java.util.List;

import org.search.highlighter.experimental.SnippetWeigher;
import org.search.highlighter.experimental.Snippet.Hit;

/**
 * Figures the weight of a snippet as the sum of the weight of its hits.
 */
public class SumSnippetWeigher implements SnippetWeigher {
    @Override
    public float weigh(List<Hit> hits) {
        float weight = 0;
        for (Hit hit : hits) {
            weight += hit.weight();
        }
        return weight;
    }
}
