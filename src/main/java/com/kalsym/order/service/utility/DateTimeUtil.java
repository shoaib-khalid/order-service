package com.kalsym.order.service.utility;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Sarosh
 */
public class DateTimeUtil {

    /**
     * *
     * Generate current timestamp string with format 'yyyy-MM-dd HH:mm:ss'
     *
     * @return
     */
    public static String currentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date currentDate = new Date();
        String currentTimeStamp = sdf.format(currentDate);
        return currentTimeStamp;
    }

    /**
     * *
     * Generate expiry time by adding seconds, hours or minutes with format
     * 'yyyy-MM-dd HH:mm:ss'
     *
     * @param seconds
     * @return
     */
    public static String expiryTimestamp(int seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.SECOND, seconds);
        Date expiryDate = c.getTime();
        return dateFormat.format(expiryDate);
    }
    
    
    public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert, ZoneId timezone) {
        return dateToConvert.toInstant()
          .atZone(timezone)
          .toLocalDateTime();
    }
    
    public static String formatDateTime(String strDt) {
        String[] d = strDt.split(" ");
        String dateStr = d[0];
        String timeStr = d[1];
        return dateStr+" "+timeStr.substring(0, 5);
    }
    
    public static String yesterdayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        return dateFormat.format(yesterday);
    }
}
