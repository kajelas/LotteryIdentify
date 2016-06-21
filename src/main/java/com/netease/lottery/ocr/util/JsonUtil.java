package com.netease.lottery.ocr.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Component
public class JsonUtil
{
	/**
	 * Json类型转换为Bean类型
	 * @param jsonStr
	 * @param objectClass
	 * @return
	 */
	public Object jsonToBean(String jsonStr, Class<?> objectClass)
	{

		JSONObject jsonObj = JSONObject.fromObject(jsonStr);
		return JSONObject.toBean(jsonObj, objectClass);
	}

	public Object jsonToBeanWithMap(String jsonStr, Class<?> objectClass, Map<?, ?> configMap)
	{

		JSONObject jsonObj = JSONObject.fromObject(jsonStr);
		return JSONObject.toBean(jsonObj, objectClass, configMap);
	}

	public Object jsonToBeanWithMapWithIgnore(String jsonStr, Class<?> objectClass, Map<?, ?> configMap,
			Class<?> annotation)
	{

		JSONObject jsonObj = JSONObject.fromObject(jsonStr);
		return JSONObject.toBean(jsonObj, objectClass, configMap);
	}

	/**
	 * Bean类型转换为Json类型
	 * replaced by serialize
	 * @param object
	 * @return
	 */
	@Deprecated
	public String BeanToJson(Object object)
	{

		JSONObject jsonObj = JSONObject.fromObject(object);
		return jsonObj.toString();
	}

	/**
	 * Map类型转换为Json类型
	 * @param map
	 * @return
	 */
	public String mapToJson(Map<?, ?> map)
	{

		JSONObject jsonObj = JSONObject.fromObject(map);
		return jsonObj.toString();
	}

	/**
	 * Bean类型转换为Json类型,忽略指定annotation标注的get方法
	 * @param object
	 * @return
	 */
	public String BeanToJsonWithIgnore(Object object, Class<?> annotation)
	{
		JsonConfig config = new JsonConfig();
		config.addIgnoreFieldAnnotation(annotation);
		return JSONObject.fromObject(object, config).toString();
	}

	/**
	 * List类型转换为Json类型
	 * replaced by serialize
	 * @param list
	 * @return
	 */
	@Deprecated
	public String ListToJson(List<?> list)
	{

		JSONArray jsonArray = JSONArray.fromObject(list);
		return jsonArray.toString();
	}

	/**
	 * bean对象转成一个map
	 * @param bean
	 * @return
	 */
	public Map<?, ?> beanToJsonMap(Object bean)
	{
		JSONObject jsonObj = JSONObject.fromObject(bean);
		return jsonObj;
	}

	/**
	 * 通用的序列化方法
	 * 和LotteryJsonHelper相比，可以多些json-lib定制化的功能，譬如jsonConfig等。
	 * @param obj
	 * @return
	 */
	public String serialize(Object obj)
	{
		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[]
		{ "tmpAccountId" });
		config.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampValueProcessor());
		return JSONObject.fromObject(obj, config).toString();
	}

	/**
	 * json字符串转list
	 * @param jsonStr
	 * @param objectClass
	 * @return
	 */
	public List<?> jsonToList(String jsonStr, Class<?> objectClass)
	{
		JSONArray jsonArray = JSONArray.fromObject(jsonStr);
		return (List<?>) JSONArray.toCollection(jsonArray, objectClass);
	}

	/**
	 * Json类型转换为List<Map>类型
	 * @param jsonStr
	 * @return
	 */
	public List<?> jsonToList(String jsonStr)
	{

		JSONArray jsonArray = JSONArray.fromObject(jsonStr);
		return (List<?>) parseJSONObj(jsonArray);
	}

	/**
	 * Json类型转换为Map类型
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonToMap(String jsonStr)
	{
		JSONObject jsonObj = JSONObject.fromObject(jsonStr);
		return (Map<String, Object>) parseJSONObj(jsonObj);
	}

	/**
	 * 递归地将JSONArray转换为List对象，将JSONObject转换为Map对象
	 *
	 * @param obj
	 * @return
	 */
	private Object parseJSONObj(Object obj)
	{

		Object result = null;
		if (obj == null)
		{
			// error
			return null;
		}
		else
		{
			if (obj instanceof JSONArray)
			{
				JSONArray arrayObj = (JSONArray) obj;
				List<Object> list = new ArrayList<Object>();
				for (Object element : arrayObj.toArray())
				{
					list.add(parseJSONObj(element));
				}
				result = list;
			}
			else if (obj instanceof JSONObject)
			{
				JSONObject jsonObj = (JSONObject) obj;
				Map<String, Object> map = new HashMap<String, Object>();
				for (Object key : jsonObj.keySet())
				{
					map.put(key.toString(), parseJSONObj(jsonObj.get(key.toString())));
				}
				return map;
			}
			else
			{
				result = obj;
			}
		}
		return result;
	}

}
