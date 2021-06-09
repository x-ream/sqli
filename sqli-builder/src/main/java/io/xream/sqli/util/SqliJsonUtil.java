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
package io.xream.sqli.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public final class SqliJsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(SqliJsonUtil.class);

    private static ObjectMapper objectMapper;

    static {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, true);
            objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true);

            JavaTimeModule javaTimeModule = new JavaTimeModule();

            //LocalDateTime.class
            javaTimeModule.addSerializer(LocalDateTime.class,new JsonSerializer<LocalDateTime>(){
                @Override
                public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeNumber(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                }
            });
            javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                    Long ts = jsonParser.getLongValue();
                    return Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            });

            //LocalDate.class
            javaTimeModule.addSerializer(LocalDate.class,new JsonSerializer<LocalDate>(){
                @Override
                public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    long ts = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    jsonGenerator.writeNumber(ts);
                }
            });
            javaTimeModule.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
                @Override
                public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                    Long ts = jsonParser.getLongValue();
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()).toLocalDate();
                }
            });

            objectMapper.registerModule(javaTimeModule);
        }
    }

    private SqliJsonUtil(){}

    protected static void config(ObjectMapper om) {
        objectMapper = om;
    }

    public static String toJson(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof String)
            return obj.toString();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.info(SqliExceptionUtil.getMessage(e));
        }
        return null;
    }

    public static <T> T toObject(String json, Class<T> clzz) {
        if (SqliStringUtil.isNullOrEmpty(json))
            return null;
        if (clzz == String.class)
            return (T)json;

        try {
            return objectMapper.readValue(json, clzz);
        } catch (Exception e) {
            logger.info(SqliExceptionUtil.getMessage(e));
        }
        return null;
    }


    public static <T> T toObject(Object jsonObject, Class<T> clzz) {
        if (jsonObject == null )
            return null;
        if (clzz == String.class)
            return (T)jsonObject;

        try {
            return objectMapper.convertValue(jsonObject,clzz);
        } catch (Exception e) {
            logger.info(SqliExceptionUtil.getMessage(e));
        }
        return null;
    }

    public static <E> List<E> toList(Object jsonObject, Class<E> clzz) {
        if (jsonObject == null )
            return null;
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, clzz);
        try {
            return objectMapper.convertValue(jsonObject,javaType);
        } catch (Exception e) {
            logger.info(SqliExceptionUtil.getMessage(e));
        }
        return null;
    }


    public static <E> List<E> toList(String json,  Class<E> clzz) {
        if (SqliStringUtil.isNullOrEmpty(json))
            return null;
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, clzz);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public static <K,V> Map<K,V> toMap(String json, Class<K> kClzz, Class<V> vClZZ) {
        if (SqliStringUtil.isNullOrEmpty(json))
            return null;
        try {
            MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, kClzz,vClZZ);
            return objectMapper.readValue(json, mapType);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public static Map toMap(String json) {
        if (SqliStringUtil.isNullOrEmpty(json))
            return null;
        try {
            return objectMapper.readValue(json, Map.class);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public interface Customizer {

        ObjectMapper customize();

        default void onStarted(ObjectMapper objectMapper) {
            SqliJsonUtil.config(objectMapper);
        }
    }
}
