/**
 * 
 * @author jackson roehrig
 */
package org.javacint.time;

import org.javacint.common.Strings;

/**
 * A calendar class that requires one long integer. The year ranges from 0 to
 * 4096. It contains an offset in milliseconds corresponsing to the difference 
 * between time now and chip time, as determined in TimeRetriever. If offset is
 * set to zero, the class is considered not initialized
 */
public class ShortCalendar {
    // time

    private static final long MASK_MS = 0x03FF; // 10 => 1111111111 2^10 = 1024
    private static final long MASK_SE = 0x003F; //  6 => 111111     2^6 = 64
    private static final long MASK_MI = 0x003F; //  6 => 111111     2^6 = 64
    private static final long MASK_HH = 0x001F; //  5 => 11111      2^5 = 32
    // date
    private static final long MASK_DD = 0x001F; //  5 => 11111      2^5 = 32
    private static final long MASK_MM = 0x000F; //  4 => 1111       2^4 = 16
    private static final long MASK_YY = 0xFFFF; // 12 => 11 ... 11  2^12 = 4096
    // time
    private static final long SHIFT_SE = 10;
    private static final long SHIFT_MI = SHIFT_SE + 6;
    private static final long SHIFT_HH = SHIFT_MI + 6;
    // date
    private static final long SHIFT_DD = SHIFT_HH + 5;
    private static final long SHIFT_MM = SHIFT_DD + 5;
    private static final long SHIFT_YY = SHIFT_MM + 4;
    
    static final int[] DAYS_IN_MONTH = {31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    static final int[] ACCUM_DAYS_IN_MONTH = {-30, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
    static final int[] ACCUM_DAYS_IN_MONTH_LEAP = {-30, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};
    private static final int MILLISECOND_PER_SECOND = 1000;
    private static final int MILLISECOND_PER_MINUTE = 60 * MILLISECOND_PER_SECOND;
    private static final int MILLISECOND_PER_HOUR = 60 * MILLISECOND_PER_MINUTE;
    private static final long MILLISECOND_PER_DAY = 24 * MILLISECOND_PER_HOUR;
    private static final long MILLISECOND_PER_YEAR = 365 * MILLISECOND_PER_DAY;
    private static long offset = 0L;
    private long dt = 0L;

    public ShortCalendar() {
        setTime(System.currentTimeMillis() + offset);
    }

    public ShortCalendar(int yy, int MM, int dd, int hh, int mm, int ss) {
        setDate(yy, MM, dd, hh, mm, ss);
    }

    public ShortCalendar(ShortCalendar sc) {
        setDate(sc);
    }

    public static void setOffset(long offset) {
        ShortCalendar.offset = offset;
    }

    public static boolean isInitialized() {
        return offset > 0L;
    }

    public static String timeNow() {
        return new ShortCalendar().toString();
    }

    public int compare(ShortCalendar other) {
        long tdt = this.dt;
        long odt = other.dt;
        return (tdt == odt) ? 0 : ((tdt < odt) ? -1 : 1);
    }

    public final void setDate(ShortCalendar sc) {
        this.dt = sc.dt;
    }

    public void setDate(int yy, int MM, int dd, int hh, int mm, int ss, int ms) {
        setDateIntern(yy, MM, dd, hh, mm, ss, ms);
    }

    public final void setDate(int yy, int MM, int dd, int hh, int mm, int ss) {
        setDate(yy, MM, dd, hh, mm, ss, 0);
    }

    public void setDate(int yy, int MM, int dd) {
        setDate(yy, MM, dd, 0, 0, 0, 0);
    }

    private void setDateIntern(int yy, int MM, int dd, int hh, int mm, int ss, int ms) {
        dt = 0L;
        dt = ((((long) yy) & MASK_YY) << SHIFT_YY)
                | ((((long) MM) & MASK_MM) << SHIFT_MM)
                | ((((long) dd) & MASK_DD) << SHIFT_DD)
                | ((((long) hh) & MASK_HH) << SHIFT_HH)
                | ((((long) mm) & MASK_MI) << SHIFT_MI)
                | ((((long) ss) & MASK_SE) << SHIFT_SE)
                | ((((long) ms) & MASK_MS));
    }

    public int getYear() {
        return (int) ((dt >> SHIFT_YY) & MASK_YY);
    }

    public int getMonth() {
        return (int) ((dt >> SHIFT_MM) & MASK_MM);
    }

    public int getDay() {
        return (int) ((dt >> SHIFT_DD) & MASK_DD);
    }

    public int getHour() {
        return (int) ((dt >> SHIFT_HH) & MASK_HH);
    }

    public int getMinute() {
        return (int) ((dt >> SHIFT_MI) & MASK_MI);
    }

    public int getSecond() {
        return (int) ((dt >> SHIFT_SE) & MASK_SE);
    }

    public int getMillisecond() {
        return (int) (dt & MASK_MS);
    }

    public void setYear(int yy) {
        dt &= ~(MASK_YY << SHIFT_YY);
        dt |= ((yy & MASK_YY) << SHIFT_YY);
    }

    public void setMonth(int MM) {
        dt &= ~(MASK_MM << SHIFT_MM);
        dt |= ((MM & MASK_MM) << SHIFT_MM);
    }

    public void setDay(int dd) {
        dt &= ~(MASK_DD << SHIFT_DD);
        dt |= ((dd & MASK_DD) << SHIFT_DD);
    }

    public void setHour(int hh) {
        dt &= ~(MASK_HH << SHIFT_HH);
        dt |= ((hh & MASK_HH) << SHIFT_HH);
    }

    public void setMinute(int mm) {
        dt &= ~(MASK_MI << SHIFT_MI);
        dt |= ((mm & MASK_MI) << SHIFT_MI);
    }

    public void setSecond(int ss) {
        dt &= ~(MASK_SE << SHIFT_SE);
        dt |= ((ss & MASK_SE) << SHIFT_SE);
    }

    public void setMillisecond(int ms) {
        dt &= ~MASK_MS;
        dt |= (ms & MASK_MS);
    }

    public String toString() {
        final char DOT = '.';
        final char COLON = ':';
        final char ZERO = '0';
        final char BLANK = ' ';
        final String DOTZERO = ".0";
        final String COLONZERO = ":0";
        //final String COLONZEROZERO = ":00";
        final String BLANKZERO = " 0";

        int dd = getDay();
        int MM = getMonth();
        int yy = getYear();
        int hh = getHour();
        int mm = getMinute();
        //int ss = getSecond(); // do not print seconds and milliseconds
        //int ms = getMillisecond();

        StringBuffer sb = new StringBuffer(17);
        if (dd < 10) {
            sb.append(ZERO);
        }
        sb.append(dd);

        if (MM < 10) {
            sb.append(DOTZERO);
        } else {
            sb.append(DOT);
        }
        sb.append(MM);
        sb.append(DOT);
        sb.append(yy);

        if (hh < 10) {
            sb.append(BLANKZERO);
        } else {
            sb.append(BLANK);
        }
        sb.append(hh);

        if (mm < 10) {
            sb.append(COLONZERO);
        } else {
            sb.append(COLON);
        }
        sb.append(mm);

//        if (ss<10) sb.append(COLONZERO);
//        else sb.append(COLON);
//        sb.append(ss);  
//        
//        if (ms<10) sb.append(COLONZEROZERO);
//        else if (ms<100)sb.append(COLONZERO);
//        else sb.append(COLON);
//        sb.append(ms);

        return sb.toString();
    }

    public boolean equals(ShortCalendar other) {
        return (other != null) && (other.dt == this.dt) && (this.dt == other.dt);
    }

    public long getTime() {
        long t = getHour() * MILLISECOND_PER_HOUR;
        t += getMinute() * MILLISECOND_PER_MINUTE;
        t += getSecond() * MILLISECOND_PER_SECOND;
        t += getMillisecond();
        t += getDaysSinceEpoch(getYear(), getMonth(), getDay()) * MILLISECOND_PER_DAY;
        return t;
    }

    public final void setTime(long milliseconds) {
        long day1 = (milliseconds / MILLISECOND_PER_DAY);
        int y = getYear(day1);
        boolean isLeap = isLeapYear(y);
        long jan1 = getDaysSinceEpoch(y, 1, 1);
        long mar1 = jan1 + (isLeap ? 60 : 59);
        int dayOfYear = (int) (day1 - jan1 + 1);
        if (isLeap && (dayOfYear >= mar1)) {
            dayOfYear--;
        }
        int[] mm = isLeap ? ACCUM_DAYS_IN_MONTH_LEAP : ACCUM_DAYS_IN_MONTH;
        for (int i = 1; i <= 13; ++i) {
            if ((mm[i]) >= dayOfYear) {
                setDate(y, i - 1, dayOfYear - mm[i - 1]);
                break;
            }
        }
        int millisecOfDay = (int) (milliseconds % MILLISECOND_PER_DAY);
        if (millisecOfDay != 0) {
            setHour(millisecOfDay / MILLISECOND_PER_HOUR);
            millisecOfDay %= MILLISECOND_PER_HOUR;
            setMinute(millisecOfDay / MILLISECOND_PER_MINUTE);
            millisecOfDay %= MILLISECOND_PER_MINUTE;
            setSecond(millisecOfDay / MILLISECOND_PER_SECOND);
            millisecOfDay %= MILLISECOND_PER_SECOND;
            setMillisecond(millisecOfDay);
        } else {
            setHour(0);
            setMinute(0);
            setSecond(0);
            setMillisecond(0);
        }
    }

    public static int getDaysSinceEpoch(int year, int month, int day) {
        boolean isLeapYear = ShortCalendar.isLeapYear(year);
        year--;
        return 365 * year + year / 4 - year / 100 + year / 400
                + (367 * month - 362) / 12 + (month <= 2 ? 0 : isLeapYear ? -1 : -2) + day;
    }

    public static int getDayOfYear(int y, int m, int d) {
        return d + (isLeapYear(y) ? ACCUM_DAYS_IN_MONTH_LEAP[m] : ACCUM_DAYS_IN_MONTH[m]);
    }

    private int getYear(long days) {
        long d0 = days - 1;
        int d1 = (int) (d0 % 146097);
        int d2 = d1 % 36524;
        int d3 = d2 % 1461;
        //int d4 = (d3 % 365) + 1;
        int n400 = (int) (d0 / 146097);
        int n100 = d1 / 36524;
        int n4 = d2 / 1461;
        int n1 = d3 / 365;
        int year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
        if (!(n100 == 4 || n1 == 4)) {
            ++year;
        }
        return year;
    }

    public void addMillsecond(long value) {
        setTime(getTime() + value);
    }

    public void addSecond(long value) {
        setTime(getTime() + (value * MILLISECOND_PER_SECOND));
    }

    public void addMinute(long value) {
        setTime(getTime() + (value * MILLISECOND_PER_MINUTE));
    }

    public void addHour(long value) {
        setTime(getTime() + (value * MILLISECOND_PER_HOUR));
    }

    public void addDay(long value) {
        setTime(getTime() + (value * MILLISECOND_PER_DAY));
    }

    public void addMonth(long value) {
        for (int i = 0; i < value; ++i) {
            setTime(getTime() + getMillisecondPerMonth());
        }
    }

    public void addYear(long value) {
        for (int i = 0; i < value; ++i) {
            long time = getTime() + MILLISECOND_PER_YEAR;
            if (isLeapYear(getYear())) {
                time += MILLISECOND_PER_DAY;
            }
            setTime(time);
        }
    }

    public long getMillisecondPerMonth() {
        int m = getMonth();
        long days = DAYS_IN_MONTH[m];
        if ((m == 2) && isLeapYear(getYear())) {
            days++;
        }
        return days * MILLISECOND_PER_DAY;
    }

    public static boolean isLeapYear(int y) {
        return ((((y % 4) == 0) && ((y % 100) != 0)) || ((y % 400) == 0));
    }

    public static ShortCalendar parseToShortCalendar(String str) throws NumberFormatException {
        //15.04.2012 00:45
        ShortCalendar sc = null;
        if (str != null) {
            int d = -1, M = -1, y = -1, h = 0, m = 0, s = 0;
            String[] str1 = Strings.split(' ', str);
            if (str1.length > 0) {
                String[] str2 = Strings.split('.', str1[0]);
                if (str2.length == 3) {
                    while ((str2[0].charAt(0) == '0') && (str2[0].length() > 1)) {
                        str2[0] = str2[0].substring(1);
                    }
                    while ((str2[1].charAt(0) == '0') && (str2[1].length() > 1)) {
                        str2[1] = str2[1].substring(1);
                    }
                    while ((str2[2].charAt(0) == '0') && (str2[2].length() > 1)) {
                        str2[2] = str2[2].substring(1);
                    }
                    d = Integer.parseInt(str2[0]);
                    M = Integer.parseInt(str2[1]);
                    y = Integer.parseInt(str2[2]);
                }
            }
            if (str1.length > 1) {
                String[] str2 = Strings.split(':', str1[1] );
                if (str2.length > 1) {
                    while ((str2[0].charAt(0) == '0') && (str2[0].length() > 1)) {
                        str2[0] = str2[0].substring(1);
                    }
                    while ((str2[1].charAt(0) == '0') && (str2[1].length() > 1)) {
                        str2[1] = str2[1].substring(1);
                    }
                    h = Integer.parseInt(str2[0]);
                    m = Integer.parseInt(str2[1]);
                }
                if (str2.length > 2) {
                    s = Integer.parseInt(str2[2]);
                }
            }
            sc = ((y != -1) && (M != -1) && (d != -1)) ? new ShortCalendar(y, M, d, h, m, s) : null;
        }
        return sc;
    }
}
