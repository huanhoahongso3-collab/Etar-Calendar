/*
 * Vietnamese lunar (Am Lich) calendar conversion utilities.
 *
 * The solar<->lunar conversion algorithm implemented below is based on the
 * well known public-domain algorithm published by Ho Ngoc Duc
 * (http://www.informatik.uni-leipzig.de/~duc/amlich/), which is valid for
 * Vietnam's timezone (UTC+7) between the years 1200 and 2199.
 */
package com.android.calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.calendar.settings.GeneralPreferences;

public class VietnameseLunarUtils {

    /** Preference key for the "Show Vietnamese lunar calendar" toggle. */
    public static final String KEY_SHOW_VIETNAMESE_LUNAR = "pref_show_lunar_calendar_vn";

    /** Vietnam standard time offset used by the reference algorithm. */
    private static final double VN_TIMEZONE = 7.0;

    private static final String[] CAN = {
            "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
    };
    private static final String[] CHI = {
            "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    };

    private VietnameseLunarUtils() {}

    /** Whether the user has enabled the Vietnamese lunar calendar overlay. */
    public static boolean isEnabled(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = GeneralPreferences.Companion.getSharedPreferences(context);
        return prefs.getBoolean(KEY_SHOW_VIETNAMESE_LUNAR, false);
    }

    /** Simple holder for a converted lunar date. */
    public static class LunarDate {
        public final int day;
        public final int month;
        public final int year;
        public final boolean isLeapMonth;

        LunarDate(int day, int month, int year, boolean isLeapMonth) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.isLeapMonth = isLeapMonth;
        }
    }

    /**
     * Returns a short label for the lunar date corresponding to the given
     * Gregorian date, e.g. "12/9" for the 12th day of the 9th lunar month,
     * or "1/9 (nhuận)" if the 1st day falls in a leap month.
     */
    public static String getShortLunarLabel(int year, int month /* 0-based */, int day) {
        LunarDate lunar = solarToLunar(year, month + 1, day);
        StringBuilder sb = new StringBuilder();
        sb.append(lunar.day).append('/').append(lunar.month);
        if (lunar.isLeapMonth) {
            sb.append('n');
        }
        return sb.toString();
    }

    /**
     * Returns a longer label, e.g. "Ngày 12 tháng 9 (Quý Mão)", suitable for
     * headers with more room.
     */
    public static String getLongLunarLabel(int year, int month /* 0-based */, int day) {
        LunarDate lunar = solarToLunar(year, month + 1, day);
        String canChi = getCanChiYear(lunar.year);
        StringBuilder sb = new StringBuilder();
        sb.append(lunar.day).append('/').append(lunar.month);
        if (lunar.isLeapMonth) {
            sb.append(" (nhuận)");
        }
        sb.append(" AL - ").append(canChi);
        return sb.toString();
    }

    private static String getCanChiYear(int year) {
        int can = (year + 6) % 10;
        int chi = (year + 8) % 12;
        return CAN[can] + " " + CHI[chi];
    }

    // ---- Solar <-> Lunar conversion (Ho Ngoc Duc public-domain algorithm) ----

    private static int jdFromDate(int dd, int mm, int yy) {
        int a = (14 - mm) / 12;
        int y = yy + 4800 - a;
        int m = mm + 12 * a - 3;
        int jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
        }
        return jd;
    }

    private static int[] jdToDate(int jd) {
        int a, b, c;
        if (jd > 2299160) {
            a = jd + 32044;
            b = (4 * a + 3) / 146097;
            c = a - (b * 146097) / 4;
        } else {
            b = 0;
            c = jd + 32082;
        }
        int d = (4 * c + 3) / 1461;
        int e = c - (1461 * d) / 4;
        int m = (5 * e + 2) / 153;
        int day = e - (153 * m + 2) / 5 + 1;
        int month = m + 3 - 12 * (m / 10);
        int year = b * 100 + d - 4800 + m / 10;
        return new int[]{day, month, year};
    }

    private static double newMoon(int k) {
        double t = k / 1236.85;
        double t2 = t * t;
        double t3 = t2 * t;
        double dr = Math.PI / 180;
        double jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
        jd1 = jd1 + 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr);
        double m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
        double mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
        double f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;
        double c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * dr * m);
        c1 = c1 - 0.4068 * Math.sin(mpr * dr) + 0.0161 * Math.sin(dr * 2 * mpr);
        c1 = c1 - 0.0004 * Math.sin(dr * 3 * mpr);
        c1 = c1 + 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mpr));
        c1 = c1 - 0.0074 * Math.sin(dr * (m - mpr)) + 0.0004 * Math.sin(dr * (2 * f + m));
        c1 = c1 - 0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mpr));
        c1 = c1 + 0.0010 * Math.sin(dr * (2 * f - mpr)) + 0.0005 * Math.sin(dr * (2 * mpr + m));
        double deltat;
        if (t < -11) {
            deltat = 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3;
        } else {
            deltat = -0.000278 + 0.000265 * t + 0.000262 * t2;
        }
        double jdNew = jd1 + c1 - deltat;
        return jdNew;
    }

    private static int sunLongitude(double jdn) {
        double t = (jdn - 2451545.0) / 36525;
        double t2 = t * t;
        double dr = Math.PI / 180;
        double m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
        double l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
        double dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m);
        dl = dl + (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) + 0.000290 * Math.sin(dr * 3 * m);
        double l = l0 + dl;
        l = l * dr;
        l = l - Math.PI * 2 * Math.floor(l / (Math.PI * 2));
        return (int) Math.floor(l / Math.PI * 6);
    }

    private static int getNewMoonDay(int k, double timeZone) {
        double jd = newMoon(k);
        return (int) Math.floor(jd + 0.5 + timeZone / 24);
    }

    private static int getSunLongitude(int dayNumber, double timeZone) {
        return sunLongitude(dayNumber - 0.5 - timeZone / 24);
    }

    private static int getLunarMonth11(int yy, double timeZone) {
        int off = jdFromDate(31, 12, yy) - 2415021;
        int k = (int) Math.floor(off / 29.530588853);
        int nm = getNewMoonDay(k, timeZone);
        int sunLong = getSunLongitude(nm, timeZone);
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone);
        }
        return nm;
    }

    private static int getLeapMonthOffset(int a11, double timeZone) {
        int k = (int) Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last = 0;
        int i = 1;
        int arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        } while (arc != last && i < 14);
        return i - 1;
    }

    /** Converts a Gregorian date to its corresponding lunar date (Vietnam). */
    public static LunarDate solarToLunar(int yy, int mm, int dd) {
        int dayNumber = jdFromDate(dd, mm, yy);
        int k = (int) Math.floor((dayNumber - 2415021.076998695) / 29.530588853);
        int monthStart = getNewMoonDay(k + 1, VN_TIMEZONE);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, VN_TIMEZONE);
        }
        int a11 = getLunarMonth11(yy, VN_TIMEZONE);
        int b11 = a11;
        int lunarYear;
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1, VN_TIMEZONE);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1, VN_TIMEZONE);
        }
        int lunarDay = dayNumber - monthStart + 1;
        int diff = (int) Math.floor((monthStart - a11) / 29.0);
        boolean isLeapMonth = false;
        int lunarMonth = diff + 11;
        if (b11 - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11, VN_TIMEZONE);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    isLeapMonth = true;
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }
        return new LunarDate(lunarDay, lunarMonth, lunarYear, isLeapMonth);
    }
}
