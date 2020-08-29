package io.xream.sqli.api;

import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

/**
 * @Author Sim
 */
public interface Parseable {

    default Parsed get(Class clzz){
        return Parser.get(clzz);
    }
}
