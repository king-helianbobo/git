package org.search.highlighter.experimental;

import java.util.List;

import org.search.highlighter.experimental.Snippet.Hit;

public interface SnippetWeigher {
    float weigh(List<Hit> hits);
}
