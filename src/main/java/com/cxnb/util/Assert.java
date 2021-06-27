package com.cxnb.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.cxnb.exception.IllegalParameterException;

import java.util.Date;
import java.util.List;

public abstract class Assert {
    public static void notBlank(final CharSequence cs, String message) {
        if (StrUtil.isBlank(cs)) {
            throw new IllegalParameterException(message);
        }
    }
    public static void notBlank(final CharSequence cs, RuntimeException exception) {
        if (StrUtil.isBlank(cs)) {
            throw exception;
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalParameterException(message);
        }
    }

    public static void notNull(Object object, RuntimeException exception) {
        if (object == null) {
            throw exception;
        }
    }

    public static void isNull(Object object, RuntimeException exception) {
        if (object != null) {
            throw exception;
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isEmpty(List list, RuntimeException exception) {
        if (list != null && !list.isEmpty()) {
            throw exception;
        }
    }

    public static void isEmpty(List list, String message) {
        if (list != null && !list.isEmpty()) {
            throw new IllegalParameterException(message);
        }
    }


    public static void isNotEmpty(List list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalParameterException(message);
        }
    }

    public static void notTrue(boolean expression, String message) {
        if (expression) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isTrue(boolean expression, RuntimeException exception) {
        if (!expression) {
            throw exception;
        }
    }

    public static void notTrue(boolean expression, RuntimeException exception) {
        if (expression) {
            throw exception;
        }
    }


    public static void isBeforeDay(Date date, long day, String message) {
        long betweenDay = DateUtil.between(DateUtil.beginOfDay(DateUtil.date()), DateUtil.beginOfDay(date), DateUnit.DAY);
        if ((betweenDay + 1) <= day) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isBeforeMonth(Date date, long month, String message) {
        long betweenMonth = DateUtil.betweenMonth(DateUtil.beginOfDay(DateUtil.date()), DateUtil.beginOfDay(date), true);
        if (betweenMonth + 1 <= month) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isBetweenDay(Date beginDate, Date endDate, long day, String message) {
        long betweenDay = DateUtil.betweenDay(DateUtil.beginOfDay(beginDate), DateUtil.beginOfDay(endDate), true);
        if (betweenDay + 1 > day) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isBetweenMonth(Date beginDate, Date endDate, long month, String message) {
        long betweenMonth = DateUtil.betweenMonth(DateUtil.beginOfDay(beginDate), DateUtil.beginOfDay(endDate), true);
        if (betweenMonth + 1 > month) {
            throw new IllegalParameterException(message);
        }
    }

    public static void isBetweenYear(Date beginDate, Date endDate, long year, String message) {
        long betweenYear = DateUtil.betweenYear(DateUtil.beginOfDay(beginDate), DateUtil.beginOfDay(endDate), true);
        if (betweenYear + 1 > year) {
            throw new IllegalParameterException(message);
        }
    }

    public static void main(String[] args) {
        String dateStr = "2021-04-09 11:05:00";
        String dateStrEnd = "2021-04-10 00:00:00";
        Date date = DateUtil.parse(dateStr);
        Date dateEnd = DateUtil.parse(dateStrEnd);
        Assert.isBetweenMonth(date, dateEnd, 3, "只能是3个月之内的范围");

    }
}
