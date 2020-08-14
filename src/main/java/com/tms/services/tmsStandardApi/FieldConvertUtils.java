package com.tms.services.tmsStandardApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;

public class FieldConvertUtils {
	
	static Logger logger = Logger.getLogger(FieldConvertUtils.class);
	private final static String ORIGINAL = "original";
	private final static String CONVERSION = "conversion";
	private final static String DATATYPE = "dataType";
	private final static String LENGTH = "length";
	private final static String MANDATORY = "mandatory";
	private final static String DEFAULTVALUE = "defaultValue";
	private final static String HEAD = "HEAD";
	private final static String TRUE = "TRUE";
	private final static String FALSE = "FALSE";
	
	private static SAXReader getSAXReaderInstance() {
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		saxReader.setIgnoreComments(true);
		return saxReader;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String,List<Map<String,String>>> getConfig(String XMLName) throws Exception{
		String config_file_path = WebUtils.getClientConfigPath() + File.separator + "api" + File.separator + XMLName;
		File config_file = new File(config_file_path);
		if (!config_file.exists()) {
			return null;
		}
		Reader reader = new InputStreamReader(new FileInputStream(config_file));
		SAXReader saxReader = getSAXReaderInstance();
		Document document = saxReader.read(reader);
		Map<String,List<Map<String,String>>> config = new HashMap<String, List<Map<String,String>>>();
		List list = document.selectNodes("/config");
		if(list != null && list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				DefaultElement elementNode = (DefaultElement) list.get(i);
				Iterator it = elementNode.elementIterator();
				while(it.hasNext()){
					DefaultElement childElementNode = (DefaultElement) it.next();
					List childList = document.selectNodes("/config/"+childElementNode.getName()+"/param");
					if(childList != null && childList.size() > 0){
						List<Map<String,String>> configMapList = new ArrayList<Map<String,String>>();
						for(int j = 0; j < childList.size(); j++){
							Map<String,String> configMap = new HashMap<String, String>();
							DefaultElement defaultElement = (DefaultElement) childList.get(j);
							Iterator attIT = defaultElement.attributeIterator();
							while(attIT.hasNext()){
								DefaultAttribute attribute = (DefaultAttribute) attIT.next();
								if(StringUtils.isNotBlank(attribute.getStringValue())){
									configMap.put(attribute.getName(), attribute.getStringValue());
								}
							}
							configMapList.add(configMap);
						}
						config.put(childElementNode.getName(), configMapList);
					}
				}
			}
		}
		return config;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String,String> getCodeTransferConfig(String XMLName) throws Exception{
		String config_file_path = WebUtils.getClientConfigPath() + File.separator + "api" + File.separator + XMLName;
		File config_file = new File(config_file_path);
		if (!config_file.exists()) {
			return null;
		}
		Reader reader = new InputStreamReader(new FileInputStream(config_file));
		SAXReader saxReader = getSAXReaderInstance();
		Document document = saxReader.read(reader);
		Map<String,String> config = new HashMap<String, String>();
		List list = document.selectNodes("/config/param");
		if(list != null && list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				DefaultElement elementNode = (DefaultElement) list.get(i);
				elementNode.attribute(0).getStringValue();
				config.put(elementNode.attribute(0).getStringValue(), elementNode.attribute(1).getStringValue());
			}
		}
		return config;
	}
	
	
	private static String convert(Map<String,Object> map,Map<String,String> configMap) throws Exception{
		Object value = map.get(configMap.get(ORIGINAL).trim());
		//判断字段是否为空
		boolean mandatory = false;
		if(configMap.get(MANDATORY) != null && configMap.get(MANDATORY).trim().equalsIgnoreCase("Y")){
			mandatory = true;
		}
		if(mandatory){
			//为必输且空值
			if(null == value){
				return "字段：" + configMap.get(ORIGINAL) + "不能为空";
			}
		}
		//判断长度
		Integer length = configMap.get(LENGTH) == null ? null : Integer.parseInt(configMap.get(LENGTH).trim());
		if(null != length && null != value){
			if(value.toString().length() > length){
				return "字段：" + configMap.get(ORIGINAL) + "超长";
			}
		}
		//判断类型 
		String type = configMap.get(DATATYPE) == null ? "" : configMap.get(DATATYPE).trim();
		if(type.equalsIgnoreCase("VARCHAR")){
			//字符型不需要检测
		}
		//日期
		if(type.equalsIgnoreCase("DATE") && null != value
				&&  StringUtils.isNotBlank(value.toString())){
			try {
				new UFDate(value.toString());
			} catch (Exception e) {
				return "字段：" + configMap.get(ORIGINAL) + "不能转换成DATE";
			}
		}
		//时间
		if(type.equalsIgnoreCase("DATETIME") && null != value
				&&  StringUtils.isNotBlank(value.toString())){
			try {
				new UFDateTime(value.toString());
			} catch (Exception e) {
				return "字段：" + configMap.get(ORIGINAL) + "不能转换成DATETIME";
			}
		}
		//整型
		if(type.equalsIgnoreCase("INT") && null != value 
				&&  StringUtils.isNotBlank(value.toString())){
			try {
				Integer.parseInt(value.toString());
			} catch (Exception e) {
				return "字段：" + configMap.get(ORIGINAL) + "不能转换成INT";
			}
		}
		//小数
		if(type.equalsIgnoreCase("DOUBLE") && null != value
				&&  StringUtils.isNotBlank(value.toString())){
			try {
				Double.parseDouble(value.toString());
			} catch (Exception e) {
				return "字段：" + configMap.get(ORIGINAL) + "不能转换成DOUBLE";
			}
		}
		//布尔
		if(type.equalsIgnoreCase("BOOLEAN") && null != value
				&&  StringUtils.isNotBlank(value.toString())){
			if( !value.toString().equalsIgnoreCase("Y") && !value.toString().equalsIgnoreCase("N")){
				return "字段：" + configMap.get(ORIGINAL) + "逻辑值只接受'Y'或者'N'";
			}
		}
		//转换字段名 只有字段名称不相等，才需要转换
		if(StringUtils.isNotBlank(configMap.get(ORIGINAL)) && StringUtils.isNotBlank(configMap.get(CONVERSION)) 
				&& !configMap.get(ORIGINAL).equals(configMap.get(CONVERSION))){
			//这里如果为null就不要转换，否则会把"null"传入
			if(value != null){
				map.put(configMap.get(CONVERSION).trim(), value);
				//移除原有值
				map.remove(configMap.get(ORIGINAL));
			}
		}
		//处理默认值
		String defultValue = configMap.get(DEFAULTVALUE);
		if(StringUtils.isNotBlank(defultValue)){
			map.put(configMap.get(CONVERSION).trim(),value == null ? defultValue : value);
		}
		//处理公司字段
		Map<String,String> corpConvert = getCodeTransferConfig("corpCodeConvert.xml");
		if(map.get("pk_corp") != null && StringUtils.isNotBlank(map.get("pk_corp").toString())){
			//有公司字段
			if(StringUtils.isNotBlank(corpConvert.get(map.get("pk_corp").toString()))){
				map.put("pk_corp", corpConvert.get(map.get("pk_corp").toString()));
				return "";
			}
			
			if(corpConvert != null && corpConvert.size() > 0){
				for(String key : corpConvert.keySet()){
					if(key.equalsIgnoreCase(map.get("pk_corp").toString().trim())){
						map.put("pk_corp", corpConvert.get(key));
						break;
					}
				}
			}
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> convertByXML(JsonNode jsonNodes, String XMLName) throws Exception {
		List<Map<String,Object>> JsonList = JacksonUtils.readValue(jsonNodes, ArrayList.class);
		Map<String,Object> result = new HashMap<String, Object>();
		if(JsonList == null || JsonList.size() == 0){
			result.put(TRUE, jsonNodes);
			return result;
		}
		Map<String,List<Map<String,String>>> config = getConfig(XMLName);
		if(config == null || config.size() == 0){
			result.put(TRUE, jsonNodes);
			return result;
		}
		for(Map<String,Object> map : JsonList){
			//一条JSON数据
			for(String key : config.keySet()){
				List<Map<String,String>> configList = config.get(key);
				if(configList == null || configList.size() == 0){
					logger.info(key + "没有配置转换信息！");
					continue;
				}
				if(key.equals(HEAD)){
					for(Map<String,String> configMap : configList){
						if(configMap == null || configMap.size() == 0){
							continue;
						}
						String msg = convert(map, configMap);
						if(StringUtils.isNotBlank(msg)){
							result.put(FALSE, msg);
							return result;
						}
					}	
				}else{
					List<Map<String,Object>> childJsonList = (List<Map<String, Object>>) map.get(key);
					if(childJsonList != null && childJsonList.size() > 0){
						for(Map<String,Object> childMap : childJsonList){
							for(Map<String,String> configMap : configList){
								if(configMap == null || configMap.size() == 0){
									continue;
								}
								String msg = convert(childMap, configMap);
								if(StringUtils.isNotBlank(msg)){
									result.put(FALSE, msg);
									return result;
								}
							}	
						}
					}
				}
			}
		}
		result.put(TRUE, JacksonUtils.readTree(JacksonUtils.writeValueAsString(JsonList)));
		return result;
	}
	
	//对部分字段进行处理，对CODE进行转换

}
