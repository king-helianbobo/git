package org.splitword.soul.utility;

import org.splitword.lionsoul.jcseg.JcSegment;

public class JcsegInstance {
	private static final JcSegment jcSegment = new JcSegment();

	public static JcSegment instance() {
		return jcSegment;
	}
}
