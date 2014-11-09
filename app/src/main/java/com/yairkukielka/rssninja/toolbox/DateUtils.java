package com.yairkukielka.rssninja.toolbox;

import java.util.Date;

public class DateUtils {

    public static Date getDateFromJson(String sDate) {
        long l = Long.valueOf(sDate);
        return new Date(l);
    }

    public static Date getDateFromJson(Long lDate) {
        return new Date(lDate);
    }

    public static String dateToString(Date date) {
        CharSequence timePassedString = android.text.format.DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), android.text.format.DateUtils.SECOND_IN_MILLIS);
        return timePassedString.toString();
//		return formatter.format(date);
    }
}
