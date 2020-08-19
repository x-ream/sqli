package io.xream.sqli.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class SqliExceptionUtil {

    private SqliExceptionUtil(){}

    public static String getMessage(Exception e){
        String msg = e.getMessage();
        msg += "\n";
        StackTraceElement[] eleArr = e.getStackTrace();
        if (eleArr == null || eleArr.length == 0)
            return msg;
        msg += eleArr[0].toString();
        msg += "\n";
        int length = eleArr.length;
        if (eleArr != null && length > 0){
            if (length > 2){
                msg += eleArr[1].toString();
                msg += "\n";
                msg += eleArr[2].toString();
            }else if (length > 1){
                msg += eleArr[1].toString();
            }
        }

        return msg;
    }

    public static String getMessage(Throwable e){
        String msg = e.getMessage();
        msg += "\n";
        StackTraceElement[] eleArr = e.getStackTrace();
        if (eleArr == null || eleArr.length == 0)
            return msg;
        msg += eleArr[0].toString();
        msg += "\n";
        int length = eleArr.length;
        if (eleArr != null && length > 0){
            if (length > 2){
                msg += eleArr[1].toString();
                msg += "\n";
                msg += eleArr[2].toString();
            }else if (length > 1){
                msg += eleArr[1].toString();
            }
        }

        return msg;
    }

    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }
}
