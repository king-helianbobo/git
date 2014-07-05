package org.highlighter.experimental.es;

import org.search.highlighter.experimental.Segment;
import org.search.highlighter.experimental.Segmenter;
import org.search.highlighter.experimental.snippet.CharScanningSegmenter;

public class CharScanningSegmenterFactory implements SegmenterFactory {
    private final int fragmentCharSize;
    private final int boundaryMaxScan;

    public CharScanningSegmenterFactory(int fragmentCharSize, int boundaryMaxScan) {
        this.fragmentCharSize = fragmentCharSize;
        this.boundaryMaxScan = boundaryMaxScan;
    }

    @Override
    public Segmenter build(String value) {
        return new CharScanningSegmenter(value, fragmentCharSize, boundaryMaxScan);
    }

    @Override
    public String extractNoMatchFragment(String value, int size) {
        // We can just delegate down the the fragmenter and let it scan characters.
        Segment bounds = new CharScanningSegmenter(value, size, boundaryMaxScan).memo(0, size).pickBounds(0, value.length());
        return value.substring(bounds.startOffset(), bounds.endOffset());
    }
}
