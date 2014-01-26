package org.soul.utility;

import org.lionsoul.jcseg.JcSegment;

public class JcsegInstance {
	private static final JcSegment jcSegment = new JcSegment();

	public static JcSegment instance() {
		return jcSegment;
	}
}
