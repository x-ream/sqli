package io.xream.sqli.core.builder;

import io.xream.sqli.common.util.BeanUtil;
import io.xream.sqli.common.util.SqlStringUtil;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.sqli.util.BeanUtilX;

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
            if (SqlStringUtil.isNullOrEmpty(p)) {
                return ((Criteria.ResultMappedCriteria) criteria).getResultKeyAliaMap().get(key);
            }

            String value = parsed.getTableName(alia) + SqlScript.DOT + p;


            return value;
        }

        if (criteria instanceof Criteria.ResultMappedCriteria) {
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
