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
package io.xream.sqli.cache.internal;

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.cache.QueryForCache;
import io.xream.sqli.exception.L2CacheException;
import io.xream.sqli.exception.NoResultUnderProtectionException;
import io.xream.sqli.exception.NotQueryUnderProtectionException;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.spi.L2CacheConsistency;
import io.xream.sqli.spi.L2CacheResolver;
import io.xream.sqli.spi.L2CacheStorage;
import io.xream.sqli.util.JsonWrapper;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 
 * Level Two Cache
 * @author sim
 *
 */
public final class DefaultL2CacheResolver extends CriteriaCacheKeyBuilder implements L2CacheResolver {

	private final static Logger logger = LoggerFactory.getLogger(DefaultL2CacheResolver.class);
	private static L2CacheResolver instance;
	public final static String NANO_SECOND = ".ns.";

	private static int validSecond;
	private static boolean isEnabled;
	private L2CacheStorage cacheStorage;
    private L2CacheConsistency l2CacheConsistency;

    private DefaultL2CacheResolver(){}
    public static L2CacheResolver newInstance(){
    	if (instance == null){
    		instance = new DefaultL2CacheResolver();
    		return instance;
		}
    	return null;
	}

    @Override
    public void setL2CacheConsistency(L2CacheConsistency l2CacheConsistency){
        this.l2CacheConsistency = l2CacheConsistency;
    }

	public static void enabled(){
		isEnabled = true;
	}

	public boolean isEnabled(){
		return isEnabled;
	}
	public static void setValidSecond(int vs){
		validSecond = vs;
		logger.info("L2 Cache try to starting.... cache time = {}s",validSecond);
	}
	private int getValidSecondAdjusted(){
		return  this.validSecond;
	}

	public void setCacheStorage(L2CacheStorage cacheStorage){
		this.cacheStorage = cacheStorage;
	}

	protected L2CacheStorage getCachestorage(){
		if (this.cacheStorage == null)
			throw new L2CacheException("No implements of L2CacheStorage, like the project x7-repo/x7-redis-integration");
		return this.cacheStorage;
	}

	private String getGroupedKey(String nsKey) {
		return nsKey + getFilterFactor();
	}
	/**
	 * 标记缓存要更新
	 * @param clzz
	 * @return nanuTime_String
	 */
	@SuppressWarnings("rawtypes")
	public String markForRefresh(Class clzz){

        String str = markForRefresh0(clzz);
        close();
        return str;
	}

	public String markForRefresh0(Class clz){

		String key = getNSKey(clz);
		String time = String.valueOf(System.nanoTime());
		if (this.l2CacheConsistency != null){
			this.l2CacheConsistency.markForRefresh(key);
		}
		getCachestorage().set(key, time);

		if (getFilterFactor() != null) {
			String groupedKey = getGroupedKey(key);
			if (this.l2CacheConsistency != null){
				this.l2CacheConsistency.markForRefresh(groupedKey);
			}
			getCachestorage().set(groupedKey, time);
		}

		return time;
	}

	@Override
	public boolean refresh(Class clz, String cacheKey) {
		if (cacheKey == null){
			remove(clz);
		}else{
			remove(clz, cacheKey);
		}
		markForRefresh0(clz);
		close();
		return true;
	}

	@Override
	public boolean refresh(Class clz) {
		return refresh(clz, null);
	}

	/**
	 * 
	 * FIXME {hash tag}
	 */
	@SuppressWarnings("rawtypes")
	public void remove(Class clz, String cacheKey){

		String key = getSimpleKey(clz, cacheKey);
		if (this.l2CacheConsistency != null){
			this.l2CacheConsistency.remove(key);
		}
		getCachestorage().delete(key);
	}

	public void remove(Class clz) {

		String key = getSimpleKeyLike(clz);

		Set<String> keySet = getCachestorage().keys(key);

		if (this.l2CacheConsistency != null){
			this.l2CacheConsistency.remove(keySet);
		}

		for (String k : keySet) {
			getCachestorage().delete(k);
		}

	}

	private String getNSKeyReadable(Class clz){
		if (getFilterFactor() == null)
			return clz.getName()+ NANO_SECOND;
		String str = clz.getName() + NANO_SECOND + getFilterFactor();
		return str;
	}
	
	@SuppressWarnings("rawtypes")
	private String getNSKey(Class clz){
		return clz.getName()+ NANO_SECOND;
	}

	private String getNSReadable(Class clzz){
		final String nsKey = getNSKeyReadable(clzz);
		String ns = getCachestorage().get(nsKey);
		if (SqliStringUtil.isNullOrEmpty(ns)){
			ns = String.valueOf(System.nanoTime());
			getCachestorage().set(nsKey,ns);
		}
		if (getFilterFactor() == null){
			return ns;
		}
		return getFilterFactor() + ns;
	}
	
	@SuppressWarnings("rawtypes")
	private List<String> getKeyList(Class clz, List<String> conditionSet){
		if (conditionSet == null || conditionSet.isEmpty())
			return null;
		List<String> keyList = new ArrayList<>();
		for (String condition : conditionSet){
			String key = getSimpleKey(clz, condition);
			keyList.add(key);
		}
		if (keyList.isEmpty())
			return null;

		return keyList;
	}
	
	/**
	 * FIXME 有简单simpleKey的地方全改成字符串存储, value为bytes, new String(bytes)
	 * @param clz
	 * @param condition
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String getSimpleKey(Class clz, String condition){
		return "{"+clz.getName()+"}." + condition;
	}

	private String getTotalRowsKey(Class clz, String condition){
		condition = MD5Helper.toMD5(condition) + "~TR";
		return "{"+clz.getName()+"}." + getNSReadable(clz) + "." + condition;
	}

	private String getConditionedKey(Class clz, String condition){
		condition = MD5Helper.toMD5(condition) + "~C";
		return "{"+clz.getName()+"}." + getNSReadable(clz)  + condition;
	}

	private String getSimpleKeyLike(Class clz){
		return "{"+clz.getName()+"}.*" ;
	}

	private String getKeyForOneObject(Class clz, Object condition){
		if (condition == null)
			throw new L2CacheException("getKeyForOneObject, id = " + condition);
		return getPrefixForOneObject(clz) +"."+MD5Helper.toMD5(""+condition);
	}

	private String getPrefixForOneObject(Class clz){
		String nsStr = getNSReadable(clz);
		if (nsStr == null){
			String str = markForRefresh0(clz);
			return "{"+clz.getName()+"}." + str;
		}
		return "{"+clz.getName()+"}."  + nsStr;
	}

	private void setTotalRows(Class clz, String cacheKey, long obj) {
		String key = getTotalRowsKey(clz, cacheKey);
		int validSecond =  getValidSecondAdjusted();
		getCachestorage().set(key, String.valueOf(obj), validSecond,TimeUnit.SECONDS);
	}

	private void setResultKeyList(Class clz, Object condition, List<String> keyList) {
		String key = getConditionedKey(clz, condition.toString());
		try{
			int validSecond = getValidSecondAdjusted();
			getCachestorage().set(key, JsonWrapper.toJson(keyList), validSecond,TimeUnit.SECONDS);
		}catch (Exception e) {
			throwException(e);
		}
	}
	
	private  <T> void setResultKeyListPaginated(Class<T> clz, Object condition, Page<T> pagination) {
		String key = getConditionedKey(clz,condition.toString());
		try{
			int validSecond = getValidSecondAdjusted();
			getCachestorage().set(key, JsonWrapper.toJson(pagination), validSecond, TimeUnit.SECONDS);
		}catch (Exception e) {
			throwException(e);
		}
	}

	private List<String> getResultKeyList(Class clz, Object condition) throws NotQueryUnderProtectionException{
		String key = getConditionedKey(clz,condition.toString());
		String str = getCachestorage().get(key);
		if (SqliStringUtil.isNullOrEmpty(str))
			throw new NotQueryUnderProtectionException();
		
		return JsonWrapper.toList(str, String.class);
	}
	
	private Page<String> getResultKeyListPaginated(Class clz, Object condition) {
		String key = getConditionedKey(clz, condition.toString());
		String json = getCachestorage().get(key);
		
		if (SqliStringUtil.isNullOrEmpty(json))
			return null;
		
		return toPagination(json);
	}

	private <T> Page<T> toPagination(String json) {
		if (SqliStringUtil.isNullOrEmpty(json))
			return null;
		Page<T> pagination = JsonWrapper.toObject(json, Page.class);
		return pagination;
	}

	private  <T> List<T> list(Class<T> clz, List<String> keyList) {
		List<String> keyArr = getKeyList(clz, keyList);//转换成缓存需要的keyList
		
		List<String> jsonList = getCachestorage().multiGet(keyArr);
		
		if (jsonList == null)
			return new ArrayList<T>();
		
		List<T> list = new ArrayList<T>();
		for (String json : jsonList){
			if (SqliStringUtil.isNotNull(json)) {
				T t = JsonWrapper.toObject(json,clz);
				list.add(t);
			}
		}
		
		return list;
	}

	private <T> T get(Class<T> clz, Object cacheKey) throws NoResultUnderProtectionException {
		String k = getSimpleKey(clz, cacheKey.toString());
		String str = getCachestorage().get(k);
		if (SqliStringUtil.isNullOrEmpty(str))
			return null;
		if (str.trim().equals(DEFAULT_VALUE))
			throw new NoResultUnderProtectionException();
		return JsonWrapper.toObject(str,clz);
	}

	/**
	 * FIXME {hash tag}
	 */
	private void set(Class clz, Object cacheKey, Object obj) {
		if (cacheKey == null )
			return;
		String k = getSimpleKey(clz, cacheKey.toString());
		String v = JsonWrapper.toJson(obj == null ? DEFAULT_VALUE : obj);
		getCachestorage().set(k, v, validSecond,TimeUnit.SECONDS);
	}

	/**
	 * FIXME {hash tag}
	 */
	private void setOne(Class clz, Object condition, Object obj) {

		String key = getKeyForOneObject(clz, condition);
		Object objKey = null;
		if (obj != null) {
			Parsed parsed = Parser.get(clz);
			Field field = parsed.getKeyField(X.KEY_ONE);
			field.setAccessible(true);
			try {
				objKey = field.get(obj);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		doSetKeyOne(clz,key,objKey,obj);
	}

	private void doSetKeyOne(Class clz,String cacheKey, Object objKey,Object obj) {

		int validSecond =  getValidSecondAdjusted();

		getCachestorage().set(cacheKey, objKey == null ? DEFAULT_VALUE : objKey.toString(), validSecond,TimeUnit.SECONDS);
		set(clz,objKey,obj);
	}


	private  <T> T getOne(Class<T> clz, Object condition) throws NoResultUnderProtectionException{
		String key = getKeyForOneObject(clz,condition);
		String keyOne = doGetKeyOne(key);
		if (keyOne == null)
			return null;
		return get(clz, keyOne);
	}

	private <T> String doGetKeyOne(String cacheKey) throws NoResultUnderProtectionException{
		String str = getCachestorage().get(cacheKey);
		if (SqliStringUtil.isNullOrEmpty(str))
			return null;
		if (str.trim().equals(DEFAULT_VALUE))
			throw new NoResultUnderProtectionException();
		return str;
	}

	private  <T> long getTotalRows(Class<T> clz, String cacheKey) {
		String key = getTotalRowsKey(clz,cacheKey);
		String str = getCachestorage().get(key);
		if (SqliStringUtil.isNullOrEmpty(str))
			return DEFAULT_NUM;
		return Long.valueOf(str);
	}


	@Override
	public <T> List<T> listUnderProtection(Class<T> clz, Object conditionObj, QueryForCache queryForCache, QueryFromDb<List<T>> QueryFromDb) {

		Parsed parsed = Parser.get(clz);
		List<String> keyList = null;
		try {
			keyList = getResultKeyList(clz, conditionObj);
		}catch (NotQueryUnderProtectionException upe) {

		}
		if (keyList == null) {

			List<T> list = null;
			try {
				list = QueryFromDb.query();
			} catch (Exception e) {
				close();
				throwException(e);
			}

			keyList = new ArrayList<>();

			for (T t : list) {
				String key = ParserUtil.getCacheKey(t, parsed);
				keyList.add(key);
			}

			setResultKeyList(clz, conditionObj, keyList);
			close();
			return list;
		}

		if (keyList.isEmpty()) {
			close();
			return new ArrayList<>();
		}

		List<T> list = list(clz, keyList);

		if (keyList.size() == list.size()) {
			close();
			return list;
		}

		replenishAndRefreshCache(keyList, list, clz, parsed,queryForCache);

		List<T> sortedList = sort(keyList, list, parsed);
		close();
		return sortedList;
	}

	@Override
	public <T> List<T> listUnderProtection(Criteria criteria, QueryForCache queryForCache, QueryFromDb<List<T>> QueryFromDb) {
		final String criteriaKey = buildCacheKey(criteria);
		final Class clz = criteria.getClzz();
		List<String> keyList = null;
		try {
			keyList = getResultKeyList(clz, criteriaKey);
		}catch (NotQueryUnderProtectionException upe) {

		}
		Parsed parsed = Parser.get(clz);
		if (keyList == null) {

			List<T> list = null;
			try {
				list = QueryFromDb.query();
			} catch (Exception e) {
				close();
				throwException(e);
			}

			keyList = new ArrayList<>();

			for (T t : list) {
				String key = ParserUtil.getCacheKey(t, parsed);
				keyList.add(key);
			}

			setResultKeyList(clz, criteriaKey, keyList);
			close();
			return list;
		}

		if (keyList.isEmpty()) {
			close();
			return new ArrayList<>();
		}

		List<T> list = list(clz, keyList);

		if (keyList.size() == list.size()) {
			close();
			return list;
		}

		replenishAndRefreshCache(keyList, list, clz, parsed,queryForCache);

		List<T> sortedList = sort(keyList, list, parsed);

		close();
		return sortedList;
	}

	@Override
	public <T> T getUnderProtection(Class<T> clz, Object conditionObj, QueryFromDb<T> QueryFromDb) {

		T obj;
		try{
			obj = get(clz,conditionObj);
		}catch (NoResultUnderProtectionException e){
			close();
			return null;
		}

		if (obj == null) {
			try {
				obj = QueryFromDb.query();
			}catch (Exception e){
				close();
				throwException(e);
			}
			set(clz, conditionObj, obj);
		}
		close();
		return obj;
	}

	@Override
	public <T> T getOneUnderProtection(Class<T> clz, Object conditionObj, QueryFromDb<T> QueryFromDb) {

		T obj;
		try{
			obj = getOne(clz,conditionObj);
		}catch (NoResultUnderProtectionException e){
			close();
			return null;
		}

		if (obj == null) {
			try {
				obj = QueryFromDb.query();
			}catch (Exception e){
				close();
				throwException(e);
			}
			setOne(clz, conditionObj, obj);
		}

		close();
		return obj;
	}


	@Override
	public <T> Page<T> findUnderProtection(Criteria criteria,QueryForCache queryForCache, QueryFromDb<Page<T>> findQueryFromDb, QueryFromDb<List<T>> listQueryFromDb){
		Class clz = criteria.getClzz();
		Parsed parsed = Parser.get(clz);
		final String criteriaKey = buildCacheKey(criteria);
		Page p = getResultKeyListPaginated(clz, criteriaKey);// FIXME

		if (p == null) {

			if (!criteria.isTotalRowsIgnored()) {
				final String totalRowsString = buildCacheKeyOfTotalRows(criteria);
				long totalRows = getTotalRows(clz, totalRowsString);
				if (totalRows == DEFAULT_NUM) {
					try {
						p = findQueryFromDb.query();
					}catch (Exception e){
						close();
						throwException(e);
					}

					setTotalRows(clz, totalRowsString, p.getTotalRows());

				} else {
					List<T> list = null;
					try {
						list = listQueryFromDb.query();
					} catch (Exception e) {
						close();
						if (e instanceof RuntimeException){
							throw (RuntimeException) e;
						}
						throw new L2CacheException(SqliExceptionUtil.getMessage(e));
					}
					p = new Page<>();
					p.setTotalRows(totalRows);
					p.setPage(criteria.getPage());
					p.setRows(criteria.getRows());
					p.reSetList(list);
				}
			} else {
				try {
					p = findQueryFromDb.query();
				}catch (Exception e){
					close();
					throwException(e);
				}
			}

			List<T> list = p.getList(); // 结果

			List<String> keyList = new ArrayList<>();
			for (T t : list) {
				String key = ParserUtil.getCacheKey(t, parsed);
				keyList.add(key);
			}
			p.setKeyList(keyList);
			p.reSetList(null);

			setResultKeyListPaginated(clz, criteriaKey, p);

			p.setKeyList(null);
			p.reSetList(list);

			close();
			return p;
		}

		List<String> keyList = p.getKeyList();

		if (keyList == null || keyList.isEmpty()) {
			close();
			return p;
		}

		List<T> list = list(clz, keyList);

		if (keyList.size() == list.size()) {
			p.reSetList(list);
			close();
			return p;
		}

		replenishAndRefreshCache(keyList, list, clz, parsed, queryForCache);

		List<T> sortedList = sort(keyList, list, parsed);

		p.reSetList(sortedList);
		close();
		return p;
	}

	private  <T> void replenishAndRefreshCache(List<String> keyList, List<T> list, Class<T> clz, Parsed parsed, QueryForCache queryForCache) {

		Set<String> keySet = new HashSet<String>();
		for (T t : list) {
			String key = ParserUtil.getCacheKey(t, parsed);
			keySet.add(key);
		}

		Field f = parsed.getKeyField(X.KEY_ONE);
		Class keyClz = f.getType();
		List<Object> idList = new ArrayList<>();
		for (String key : keyList) {
			if (!keySet.contains(key)) {
				try {
					if (keyClz == String.class) {
						idList.add(key);
					} else if (keyClz == long.class || keyClz == Long.class) {
						idList.add(Long.valueOf(key));
					} else if (keyClz == int.class || keyClz == Integer.class) {
						idList.add(Integer.valueOf(key));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		String key = parsed.getKey(X.KEY_ONE);
		InCondition inCondition = InCondition.of(key, idList);
		inCondition.setClz(clz);
		List<T> objList = queryForCache.in(inCondition);

		if (objList.isEmpty()) {
			markForRefresh0(clz);
			return;
		}

		try {
			for (T obj : objList) {
				list.add(obj);
				Object id = f.get(obj);
				set(clz, String.valueOf(id), obj);
			}
		} catch (Exception e) {

		}

	}

	private  <T> List<T> sort(List<String> keyList, List<T> list, Parsed parsed) {
		List<T> sortedList = new ArrayList<T>();
		for (String key : keyList) {
			Iterator<T> ite = list.iterator();
			while (ite.hasNext()) {
				T t = ite.next();
				if (key.equals(ParserUtil.getCacheKey(t, parsed))) {
					ite.remove();
					sortedList.add(t);
					break;
				}
			}
		}
		return sortedList;
	}


	private void throwException(Exception e) {
		if (e instanceof RuntimeException){
			throw (RuntimeException) e;
		}
		throw new L2CacheException(SqliExceptionUtil.getMessage(e));
	}

	
	public static final class MD5Helper {
		public static String toMD5(String str){
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(str.getBytes());
				byte[] byteDigest = md.digest();
				StringBuffer buf = new StringBuffer("");
				int i = 0;
				for (int offset = 0; offset < byteDigest.length; offset++) {
					i = byteDigest[offset];
					if (i < 0)
						i += 256;
					if (i < 16)
						buf.append("0");
					buf.append(Integer.toHexString(i));
				}
				return buf.toString();
				  
			} catch (NoSuchAlgorithmException e) {
			}
			return null;
		}
	}
}
