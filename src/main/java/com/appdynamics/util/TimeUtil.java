package com.appdynamics.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final Logger logger = LogManager.getFormatterLogger();
    private static Pattern patternConnectionString = Pattern.compile("^(?<jdbc>[j|J][d|D][b|B][c|C]:)?(?<vendor>[^:]+):(?<driver>[^:]+):(?<path>.*);?");
    private static Pattern patternAnalyticsDateString = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String getDateString(long dateTime) { //ISO8601 Date (Extend) https://dencode.com/en/date/iso8601
        return ZonedDateTime.ofInstant(new Date(dateTime).toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }
    public static String getEncodedDateString( long dateTime ) {
        return Parser.encode(getDateString(dateTime));
    }

    public static long now() { return System.currentTimeMillis(); }

    public static boolean isThisStringADate(String data) {
        if( data == null ) return false;
        //"2021-10-14T18:00:51.435Z"
        Matcher matcher = patternAnalyticsDateString.matcher(data);
        if(matcher.matches()) return true;
        return false;
    }

    public static long parseDateString(String data) throws ParseException {
        //"2021-10-14T18:00:51.435Z"
        return simpleDateFormat.parse(data).getTime();
    }

    public static long getDaysBackTimestamp(int daysToRetrieveData) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( new Date());
        calendar.add(Calendar.DATE, daysToRetrieveData*-1);
        return calendar.getTimeInMillis();
    }

    public static String getMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( new Date(timestamp));
        return Month.of(calendar.get( Calendar.MONTH)+1).getDisplayName(TextStyle.FULL, new Locale("en"));
    }
}
