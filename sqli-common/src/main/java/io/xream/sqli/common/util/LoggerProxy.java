package io.xream.sqli.common.util;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class LoggerProxy {

    private LoggerProxy(){}

    private final static Map<Class, Logger> loggerMap = new HashMap<>();

    public static void put(Class clzz, Logger logger) {
        loggerMap.put(clzz,logger);
    }

    public static void debug(Class clzz, Object obj) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null || obj == null)
            return;
        if (logger.isDebugEnabled()) {
            logger.debug(obj.toString());
        }
    }


    public static void debug(Class clzz,  LogCallable callable) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null )
            return;
        if (logger.isDebugEnabled()) {
            if (callable == null)
                return;
            String str = callable.call();
            if (SqlStringUtil.isNullOrEmpty(str))
                return;
            logger.debug(str);
        }
    }

    public static void info(Class clzz, Object obj) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null || obj == null)
            return;
        else if (logger.isInfoEnabled()){
            logger.info(obj.toString());
        }
    }

    public static long getTimeMills(Class clzz){
        Logger logger = loggerMap.get(clzz);
        if (logger == null)
            return 0;
        if (logger.isDebugEnabled())
            return System.currentTimeMillis();
        return 0;
    }

    public interface LogCallable{
        String call();
    }
}
