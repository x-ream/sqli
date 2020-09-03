package io.xream.sqli.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;


/**
 * @Author Sim
 */
public class SqliExceptionUtil {

    private SqliExceptionUtil(){}

    public static String getMessage(Throwable e){
        String msg = e.getMessage();
        msg += "\n";
        StackTraceElement[] eleArr = e.getStackTrace();
        if (eleArr == null || eleArr.length == 0)
            return msg;
        for (StackTraceElement ele : eleArr) {
            msg += ele.toString();
            msg += "\n";
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
