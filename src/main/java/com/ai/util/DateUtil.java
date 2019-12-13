package com.ai.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author tq
 * @date 2019/12/13 15:57
 */
public class DateUtil {

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


    public static String getToday() {
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String getTomorrow() {
        Date date = new Date(System.currentTimeMillis()+24*60*60*1000);
        return formatter.format(date);
    }

    public static void main(String[] args) {
        System.out.println(getToday());
        System.out.println(getTomorrow());
    }
}
