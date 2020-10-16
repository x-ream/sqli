package io.xream.sqli.starter;

import io.xream.sqli.api.customizer.DialectCustomizer;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.dialect.DynamicDialect;

/**
 * @Author Sim
 */
public class DialectListener {

    protected static void customizeOnStarted(Dialect dialect, DialectCustomizer dialectCustomizer) {
        if (dialectCustomizer == null)
            return;
        Dialect customedDialect = dialectCustomizer.customize();
        if (customedDialect == null)
            return;
        if (dialect instanceof DynamicDialect) {
            DynamicDialect dynamicDialect = (DynamicDialect) dialect;
            dynamicDialect.setDefaultDialect(customedDialect);
        }
    }
}
