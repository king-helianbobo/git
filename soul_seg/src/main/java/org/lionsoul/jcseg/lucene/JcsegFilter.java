package org.lionsoul.jcseg.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class JcsegFilter extends TokenFilter {

	// private CharTermAttribute termAtt =
	// addAttribute(CharTermAttribute.class);

	protected JcsegFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		while (input.incrementToken()) {
			// char text[] = termAtt.buffer();
			// int termLength = termAtt.length();
			return true;
		}
		return false;
	}

}
