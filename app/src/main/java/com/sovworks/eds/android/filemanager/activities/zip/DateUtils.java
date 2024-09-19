package com.sovworks.eds.android.filemanager.activities.zip;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间格式
 *
 * @author Andy.R
 */
public class DateUtils {

    public static final String FORMAT_DATE_FOR_24 = "yyyy-MM-dd HH:mm:ss";

    public static final String FORMAT_DATE_FOR_12 = "yyyy-MM-dd hh:mm:ss";

    public static String getDateToString(Context context, Date date) {
        SimpleDateFormat DateToString;
        boolean is24Format = DateFormat.is24HourFormat(context);
        if (is24Format) {
            DateToString = new SimpleDateFormat(FORMAT_DATE_FOR_24);
        } else {
            DateToString = new SimpleDateFormat(FORMAT_DATE_FOR_12);
        }
        return DateToString.format(date);
    }
}
