package tsl.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TimeUtils {

	public static String DefaultDateTimeFormat = "yyyy/MM/dd HH:mm:ss";

	public static void main(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String str = "";
		boolean quit = false;
		String laststr = null;
		while (!quit) {
			System.out.print("Time/format> ");
			try {
				str = in.readLine();
				str = str.trim();
				String[] strs = str.split(" ");
				if (strs == null || strs.length != 2) {
					System.out.println("Improper delimitation:  length = "
							+ strs.length);
				}
				String timestr = strs[0].trim();
				String[] formats = new String[] {strs[1].trim()};
				Date date = getDateFromString(timestr, formats);
				if (date != null) {
					Calendar c = new GregorianCalendar();
					c.setTime(date);
					System.out.println("day=" + c.get(Calendar.DAY_OF_MONTH)
							+ ", month=" + (c.get(Calendar.MONTH) + 1)
							+ ",year=" + (c.get(Calendar.YEAR)));
				} else {
					System.out.println("Unable to parse " + timestr);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getDateTimeString() {
		return getDateTimeString(DefaultDateTimeFormat);
	}

	public static String getDateTimeString(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		String dtstr = dateFormat.format(date);
		return dtstr;
	}

	public static Date getDateFromString(String datestring, String[] formats) {
		Date date = null;
		for (String format : formats) {
			format = format.trim();
			DateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
			try {
				date = dateFormat.parse(datestring);
			} catch (ParseException e) {
			}
			if (date != null) {
				return date;
			}
		}
		return null;
	}
	

}
