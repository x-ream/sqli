/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.dialect;

import io.xream.sqli.api.Dialect;
import io.xream.sqli.common.util.BeanUtil;
import io.xream.sqli.common.util.JsonWrapper;
import io.xream.sqli.common.util.SqlStringUtil;
import io.xream.sqli.common.util.SqliExceptionUtil;
import io.xream.sqli.core.builder.BeanElement;
import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.SqlScript;
import io.xream.sqli.exception.NotSupportedException;


import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class OracleDialect implements Dialect {

    private final Map<String, String> map = new HashMap<String, String>() {
        {
            put(DATE, "date");
            put(BYTE, "number(3, 0)");
            put(INT, "number(10, 0)");
            put(LONG, "number(18, 0)");
            put(BIG, "number(19, 2)");
            put(STRING, "varchar2");
            put(TEXT, "clob");
            put(LONG_TEXT, "clob");
            put(INCREAMENT, "");
            put(ENGINE, "");
        }

    };

    private final static String ORACLE_PAGINATION = "SELECT * FROM (SELECT A.*, ROWNUM RN FROM ( ${SQL} ) A   WHERE ROWNUM <= ${END}  )  WHERE RN > ${BEGIN} ";
    private final static String ORACLE_PAGINATION_REGX_SQL = "${SQL}";
    private final static String ORACLE_PAGINATION_REGX_BEGIN = "${BEGIN}";
    private final static String ORACLE_PAGINATION_REGX_END = "${END}";


    public String match(String sql, long start, long rows) {

        if (rows > 0)
            return ORACLE_PAGINATION.replace(ORACLE_PAGINATION_REGX_END, String.valueOf(start + rows))
                    .replace(ORACLE_PAGINATION_REGX_BEGIN, String.valueOf(start)).replace(ORACLE_PAGINATION_REGX_SQL, sql);
        return sql;

    }

    public String match(String sql, String sqlType) {
        String dateV = map.get(DATE);
        String byteV = map.get(BYTE);
        String intV = map.get(INT);
        String longV = map.get(LONG);
        String bigV = map.get(BIG);
        String textV = map.get(TEXT);
        String longTextV = map.get(LONG_TEXT);
        String stringV = map.get(STRING);
        String increamentV = map.get(INCREAMENT);
        String engineV = map.get(ENGINE);

        return sql.replace(DATE.trim(), dateV).replace(BYTE.trim(), byteV).replace(INT.trim(), intV)
                .replace(LONG.trim(), longV).replace(BIG.trim(), bigV).replace(TEXT.trim(), textV)
                .replace(LONG_TEXT.trim(), longTextV).replace(STRING.trim(), stringV)
                .replace(INCREAMENT.trim(), increamentV).replace(ENGINE.trim(), engineV);
    }

    public Object mappingToObject(Object obj, BeanElement element) {
        if (obj == null)
            return null;

        Class ec = element.clz;

        if (element.isJson) {

            String str = null;
            if (obj instanceof oracle.sql.NCLOB) {

                oracle.sql.NCLOB clob = (oracle.sql.NCLOB) obj;

                Reader reader = null;
                try {
                    reader = clob.getCharacterStream();

                    char[] charArr = new char[(int) clob.length()];
                    reader.read(charArr);
                    str = new String(charArr);//FIXME UIF-8 ?
                } catch (Exception e) {
                    throw new RuntimeException(SqliExceptionUtil.getMessage(e));
                }finally{
                    if (reader !=null) {
                        try {
                            reader.close();
                        }catch (Exception e){

                        }
                    }
                }

            }else if (obj instanceof String) {
                str = obj.toString();
            }

            if (SqlStringUtil.isNullOrEmpty(str))
                return null;

            str = str.trim();

            if (!(str.startsWith("{") || str.startsWith("[")))
                return str;
            if (ec == List.class) {
                Class geneType = element.geneType;
                return JsonWrapper.toList(str, geneType);
            } else if (ec == Map.class) {
                return JsonWrapper.toMap(str);
            } else {
                return JsonWrapper.toObject(str, ec);
            }
        }

        if (obj instanceof BigDecimal) {

            BigDecimal bg = (BigDecimal) obj;
            if (ec == BigDecimal.class) {
                return bg;
            } else if (ec == int.class || ec == Integer.class) {
                return bg.intValue();
            } else if (ec == long.class || ec == Long.class) {
                return bg.longValue();
            } else if (ec == double.class || ec == Double.class) {
                return bg.doubleValue();
            } else if (ec == float.class || ec == Float.class) {
                return bg.floatValue();
            } else if (ec == boolean.class || ec == Boolean.class) {
                int i = bg.intValue();
                return i == 0 ? false : true;
            } else if (ec == Date.class) {
                long l = bg.longValue();
                return new Date(l);
            } else if (ec == java.sql.Date.class) {
                long l = bg.longValue();
                return new java.sql.Date(l);
            } else if (ec == Timestamp.class) {
                long l = bg.longValue();
                return new Timestamp(l);
            } else if (ec == byte.class || ec == Byte.class) {
                return bg.byteValue();
            }

        } else if (obj instanceof Timestamp && ec == Date.class) {
            Timestamp ts = (Timestamp) obj;
            return new Date(ts.getTime());
        }
        if (BeanUtil.isEnum(ec)) {
            return Enum.valueOf(ec, obj.toString());
        }

        return obj;

    }

    @Override
    public String createOrReplaceSql(String sql) {
        throw new NotSupportedException("x7/x7-repor/x7-jdbc-template-plus not support createOrReplace() for Oracle");
    }

    @Override
    public String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap) {

        if (!resultKeyAliaMap.isEmpty()) {
            if (resultKeyAliaMap.containsKey(mapper)) {
                mapper = resultKeyAliaMap.get(mapper);
            }
        }
        if (aliaMap.isEmpty())
            return mapper;

        if (mapper.contains(".")) {
            String[] arr = mapper.split("\\.");
            String alia = arr[0];
            String p = arr[1];
            String clzName = aliaMap.get(alia);
            if (SqlStringUtil.isNullOrEmpty(clzName)) {
                clzName = alia;
            }
            return clzName + "." + p;
        }

        return mapper;

    }

    @Override
    public String resultKeyAlian(String mapper, Criteria.ResultMappedCriteria criteria) {

        if (mapper.contains(".") && (!mapper.contains(SqlScript.SPACE) || !mapper.contains(SqlScript.AS) )) {
            Map<String, String> aliaMap = criteria.getResultKeyAliaMap();
            String alian = "c" + aliaMap.size();
            aliaMap.put(alian, mapper);
            String target = mapper + SqlScript.AS + alian;
            return target;
        }
        return mapper;
    }

    public Object filterValue(Object value) {
        if (value instanceof String) {
            String str = (String) value;
            value = str.replace("<", "&lt").replace(">", "&gt");
        } else if (value instanceof Date) {
            Date date = (Date) value;
            Timestamp timestamp = new Timestamp(date.getTime());
            return timestamp;
        } else if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            return b.booleanValue() == true ? 1 : 0;
        }
        if (Objects.nonNull(value) && BeanUtil.isEnum(value.getClass()))
            return ((Enum) value).name();
        return value;
    }

    @Override
    public Object[] toArr(Collection<Object> list) {

        if (list == null || list.isEmpty())
            return null;
        int size = list.size();
        Object[] arr = new Object[size];
        int i = 0;
        for (Object obj : list) {
            obj = filterValue(obj);
            arr[i++] = obj;
        }
        return arr;
    }

}
