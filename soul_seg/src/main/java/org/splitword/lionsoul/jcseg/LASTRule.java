package org.splitword.lionsoul.jcseg;

import org.splitword.lionsoul.jcseg.core.IChunk;
import org.splitword.lionsoul.jcseg.core.ILastRule;
import org.splitword.lionsoul.jcseg.core.IRule;

/**
 * the last rule. -clear the ambiguity after the four rule.
 * 
 */
public class LASTRule implements ILastRule {

	/**
	 * maximum match rule instance.
	 */
	private static LASTRule __instance = null;

	/**
	 * return the quote to the maximum match instance.
	 * 
	 * @return MMRule
	 */
	public static LASTRule createRule() {
		if (__instance == null)
			__instance = new LASTRule();
		return __instance;
	}

	private LASTRule() {
	}

	/**
	 * last rule interface. here we simply return the first chunk.
	 * 
	 * @see IRule#call(IChunk[])
	 */
	@Override
	public IChunk call(IChunk[] chunks) {
		return chunks[0];
	}

}
