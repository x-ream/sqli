package io.xream.sqli.dialect;

import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;
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
    public String getInsertTagged() {
        return " ? USING #stb# TAGS ";
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
            boolean hasSubKey = parsed.getSubField() != null;
            String dynamicTableName = parsed.getTableName();
            if (hasSubKey){
                dynamicTableName = dynamicTableName + "_" + parsed.getSubField().get(obj);
                list.add(dynamicTableName);
            }
            for (Field field : tagFieldList) {
                Object value = field.get(obj);
                if (BeanUtil.isEnum(field.getType())) {
                    String str = ((Enum) value).name();
                    list.add(str);
                    if (!hasSubKey){
                        dynamicTableName = dynamicTableName + "_" + str.hashCode();
                    }
                } else {
                    list.add(value);
                    if (!hasSubKey){
                        dynamicTableName = dynamicTableName + "_" + value.hashCode();
                    }
                }
            }
            if (!hasSubKey){
                list.add(0,dynamicTableName);
            }
        } catch (Exception e) {
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(SqliExceptionUtil.getMessage(e));
        }

        objectToListForCreate(list, obj, tempList);

        return list;
    }
}
