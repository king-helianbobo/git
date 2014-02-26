package org.splitword.lionsoul.jcseg.core;

import java.io.IOException;
import java.io.Reader;

public interface ISegment {

	// Whether to check the Chinese and English mixed word.
	public static final int CHECK_CE_MASk = 1 << 0;
	// Whether to check the Chinese fraction.
	public static final int CHECK_CF_MASK = 1 << 1;
	// Whether to start the Latin secondary segmentation.
	public static final int START_SS_MASK = 1 << 2;

	/**
	 * reset the reader
	 * 
	 * @param input
	 */
	public void reset(Reader input) throws IOException;

	/**
	 * get the current length of the stream
	 * 
	 * @return int
	 */
	public int getStreamPosition();

	/**
	 * segment a word from a char array from a specified position.
	 * 
	 * @return IWord
	 */
	public IWord next() throws IOException;
}
