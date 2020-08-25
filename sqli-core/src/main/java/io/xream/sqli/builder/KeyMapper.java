package io.xream.sqli.builder;

import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliStringUtil;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtilX;

/**
 * @Author Sim
 */
public interface KeyMapper {

    default String mapping(String key, CriteriaCondition criteria) {


        if (key.contains(SqlScript.DOT)) {

            String[] arr = key.split("\\.");
            String alia = arr[0];
            String property = arr[1];


            String clzName = BeanUtilX.getClzName(alia, criteria);

            Parsed parsed = Parser.get(clzName);
            if (parsed == null)
                throw new RuntimeException("Entity Bean Not Exist: " + BeanUtil.getByFirstUpper(key));

            String p = parsed.getMapper(property);
            if (SqliStringUtil.isNullOrEmpty(p)) {
                return ((Criteria.ResultMapCriteria) criteria).getResultKeyAliaMap().get(key);
            }

            String value = parsed.getTableName(alia) + SqlScript.DOT + p;


            return value;
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

        Parsed parsed = criteria.getParsed();
        if (key.equals(BeanUtilX.getByFirstLower(parsed.getClz().getSimpleName())))
            return parsed.getTableName();
        String value = parsed.getMapper(key);
        if (value == null)
            return key;
        return value;

    }
}
