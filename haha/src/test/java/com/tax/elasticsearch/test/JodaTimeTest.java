package com.tax.elasticsearch.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.joda.FormatDateTimeFormatter;
import org.elasticsearch.common.joda.Joda;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Days;
import org.elasticsearch.common.joda.time.LocalDate;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class JodaTimeTest {

	private final Log log = LogFactory.getLog(JodaTimeTest.class);

	// @Test(enabled = true)
	public void jodaTimeTest1() {
		final float da = 0.08f;
		final float db = 0.05f;
		final float dm = (float) (3.16f * Math.pow(10.0f, -11.0f));
		DateTime date = new DateTime();
		String a = date.toString();
		String b = date.toString("dd:MM:yy");
		log.info(a + "," + b + "," + date.getMillis());
		DateTimeFormatter outputFormat = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm");
		String[] strs = { "2013-5-31 15:59:58", "2014-6-25", "2009-9-1 15:30",
				"2009-2-28 5:3", "1970-01-01", "2014-07-03" };
		FormatDateTimeFormatter formatter = Joda
				.forPattern("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||yyyy-MM-dd HH:mm");
		long millis1 = 1404369067654L;
		DateTime dt1 = new DateTime(millis1);

		log.info(dt1.toString(outputFormat));
		for (String strInputDateTime : strs) {
			long millis = formatter.parser().parseMillis(strInputDateTime);
			DateTime dt = formatter.parser().parseDateTime(strInputDateTime);
			// assertThat(input, is(formatter.printer().print(millis)));
			// fmt.parseDateTime(strInputDateTime);
			// String strOutputDateTime = fmt.print(dt);
			int numDays = Days.daysBetween(new LocalDate(dt1),
					new LocalDate(dt)).getDays();
			log.info("number of days = " + numDays + ", date="
					+ strInputDateTime);
			int iDoW = dt.getDayOfWeek();
			log.info(dt.toString(outputFormat) + "," + iDoW);
			log.info("current time is " + millis + "," + dt.getMillis());
			// DateTime dt1 = outputFormat.parseDateTime(dt.toString());
			// log.info(dt1.toString());
			float dx = Math.abs(date.getMillis() - dt.getMillis());
			float f = 1.0f + (float) (da / (db + dm * dx));
			log.info(f);
		}
	}

	@Test(enabled = true)
	public void jodaTimeTest2() {
		final float startA = 17.001f;
		final float endA = 1.001f;
		final float startDay = 1.0f;
		final float endDay = 2000.0f;
		DateTimeFormatter outputFormat = DateTimeFormat
				.forPattern("yyyy-MM-dd HH:mm");
		String[] strs = { "2013-5-31 15:59:58", "2014-6-25", "2009-03-20",
				"2009-2-28 5:3", "1970-01-01", "2014-07-03" };
		FormatDateTimeFormatter formatter = Joda
				.forPattern("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||yyyy-MM-dd HH:mm");
		DateTime date = new DateTime();
		for (String strInputDateTime : strs) {
			long millis = formatter.parser().parseMillis(strInputDateTime);
			DateTime dt = new DateTime(millis);
			int numDays = Days.daysBetween(new LocalDate(date),
					new LocalDate(dt)).getDays();
			int days = Math.abs(numDays) + 1;
			log.info("number of days = " + days + ", date=" + strInputDateTime);
			float dx;
			if (days <= endDay)
				dx = (days - startDay) * (endA - startA) / (endDay - startDay)
						+ startA;
			else
				dx = (float) (1.0f / (Math.log10(10 + days - endDay)));
			log.info(dx);
		}
	}

}
