package org.soul.utility;

import org.apache.tika.Tika;

public class TikaInstance {

	private static final Tika tika = new Tika();

	public static Tika tika() {
		return tika;
	}
}
