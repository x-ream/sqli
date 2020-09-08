package io.xream.sqli.builder;

import io.xream.sqli.api.Alias;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.Map;

/**
 * @Author Sim
 */
public interface KeyMapper {

    default String mapping(String key, Alias criteria) {

        if (SqliStringUtil.isNullOrEmpty(key))
            return key;
        if (key.contains(SqlScript.DOT)) {

            String[] arr = key.split("\\.");
            String alia = arr[0];
            String property = arr[1];

            String clzName = ParserUtil.getClzName(alia, criteria);

            Parsed parsed = Parser.get(clzName);
            if (parsed == null)
                return key;
//                throw new ParsingException("Entity Bean Not Exist: " + BeanUtil.getByFirstUpper(key));

            String p = parsed.getMapper(property);
            if (SqliStringUtil.isNullOrEmpty(p)) {
                return ((Criteria.ResultMapCriteria) criteria).getResultKeyAliaMap().get(key);
            }

            return parsed.getTableName(alia) + SqlScript.DOT + p;
        }

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Parsed parsed = Parser.get(key);
            if (parsed != null) {
                return parsed.getTableName();
            }
        }

        if (criteria instanceof RefreshCondition){
            Parsed parsed = Parser.get(key);
            if (parsed != null) {
                return parsed.getTableName();
            }
        }

        Parsed parsed = ((CriteriaCondition)criteria).getParsed();
        if (parsed == null)
            return key;
        if (key.equals(BeanUtil.getByFirstLower(parsed.getClz().getSimpleName())))
            return parsed.getTableName();
        String value = parsed.getMapper(key);
        if (value == null)
            return key;
        return value;

    }
}
