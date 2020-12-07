/*
 * Copyright 2020 io.xream.sqli
 *
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

import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliExceptionUtil;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Sim
 */
public final class TAOSDialect extends MySqlDialect {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String getKey(){
        return "taosdata";
    }

    @Override
    public String getInsertTagged() {
        return "? USING #stb# TAGS ";
    }

    @Override
    public void filterTags(List<BeanElement> list, List<Field> tagList) {

        Iterator<BeanElement> ite = list.iterator();
        while (ite.hasNext()) {
            BeanElement be = ite.next();
            for (Field field : tagList) {
                if (be.getProperty().equals(field.getName())) {
                    ite.remove();
                    continue;
                }
            }
        }
    }

    @Override
    public Object filterValue(Object value) {
        if (value instanceof Date) {
            return sdf.format(value);
        }
        return value;
    }

    @Override
    public Object mappingToObject(Object obj, BeanElement element) {
        if (obj == null)
            return null;
        Class ec = element.getClz();
        if (ec == Date.class) {
            return new Date((Long) obj);
        } else if (ec == Timestamp.class) {
            return new Timestamp((Long) obj);
        }
        return super.mappingToObject(obj, element);
    }

    @Override
    public List<Object> objectToListForCreate(Object obj, Parsed parsed) {
        List<Object> list = new ArrayList<>();
        List<BeanElement> tempList = new ArrayList<>();
        tempList.addAll(parsed.getBeanElementList());
        List<Field> tagFieldList = parsed.getTagFieldList();
        filterTags(tempList, tagFieldList);
        try {
            boolean hasSubKey = parsed.getTagKeyField() != null;
            String dynamicTableName = parsed.getTableName();
            if (hasSubKey) {
                dynamicTableName = dynamicTableName + "_" + parsed.getTagKeyField().get(obj);
                list.add(dynamicTableName);
            }
            for (Field field : tagFieldList) {
                Object value = field.get(obj);
                if (EnumUtil.isEnum(field.getType())) {
                    Object enumObj = EnumUtil.serialize((Enum) value);
                    list.add(enumObj);
                } else {
                    list.add(value);
                }
                if (!hasSubKey) {
                    dynamicTableName = dynamicTableName + "_" + value.hashCode();
                }
            }
            if (!hasSubKey) {
                list.add(0, dynamicTableName);
            }
        } catch (Exception e) {
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(SqliExceptionUtil.getMessage(e));
        }

        objectToListForCreate(list, obj, tempList);

        return list;
    }

    @Override
    public String createSql(Parsed parsed, List<BeanElement> tempList) {
        if (parsed.getTagFieldList().isEmpty()) {
            return getDefaultCreateSql(parsed, tempList);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        String insertTagged = getInsertTagged();
        insertTagged = insertTagged.replace("#stb#", BeanUtil.getByFirstLower(parsed.getClzName()));
        int size = parsed.getTagFieldList().size();
        sb.append(insertTagged).append("(");
        for (int i = 0; i < size; i++) {
            sb.append("?");
            if (i < size - 1) {
                sb.append(",");
            }
        }
        filterTags(tempList, parsed.getTagFieldList());

        sb.append(") VALUES (");

        size = tempList.size();

        for (int i = 0; i < size; i++) {

            sb.append("?");
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }

}
