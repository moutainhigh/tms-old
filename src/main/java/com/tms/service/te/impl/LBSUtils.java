package com.tms.service.te.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nw.exception.BusiException;
import org.nw.json.JacksonUtils;
import org.nw.utils.HttpUtils;
import org.nw.vo.api.RootVO;
import org.nw.web.utils.WebUtils;

import com.tms.service.job.lbs.TrackInfoConverter;
/**
 	调用LBS的工具类，传入车牌号数组，查询对应的经纬度信息
 */
public class LBSUtils {

	static Logger logger = Logger.getLogger(LBSUtils.class);
	
	private static LbsApiVO lbsApiVO = null;

	private static SAXReader getSAXReaderInstance() {
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		saxReader.setIgnoreComments(true);
		return saxReader;
	}

	/**
	 * 实例化配置文件
	 */
	@SuppressWarnings("rawtypes")
	protected static void parse() throws Exception {
		String path = WebUtils.getClientConfigPath() + java.io.File.separator + "lbs.xml";
		File file = new File(path);

		Reader reader = null;
		if(!file.exists()) {
			throw new BusiException(path + ":文件不存在!");
		}
		LbsApiVO apiVO = new LbsApiVO();
		reader = new InputStreamReader(new FileInputStream(file));
		SAXReader saxReader = getSAXReaderInstance();
		Document doc = saxReader.read(reader);

		Node host = doc.selectSingleNode("/lbs/api/host");
		apiVO.setHost(host.getText() == null ? "" : host.getText().trim());

		Node uid = doc.selectSingleNode("/lbs/api/uid");
		apiVO.setUid(uid.getText() == null ? "" : uid.getText().trim());

		Node pwd = doc.selectSingleNode("/lbs/api/pwd");
		apiVO.setPwd(pwd.getText() == null ? "" : pwd.getText().trim());

		Map<String, String> methodMap = new HashMap<String, String>();
		List list = doc.selectNodes("/lbs/api/methods/method");
		if(list != null && list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Node node = (Node) list.get(i);
				Node key = node.selectSingleNode("key");
				Node url = node.selectSingleNode("url");

				methodMap.put(key.getText().trim(), url.getText() == null ? "" : url.getText());
			}

		}
		apiVO.setMethodMap(methodMap);

		lbsApiVO = apiVO;
	}

	public static RootVO getCurrentTrackVO(String[] gps_ids) {
		if(gps_ids == null || gps_ids.length == 0) {
			return null;
		}

		if(lbsApiVO == null) {
			try {
				parse();
			} catch(Exception e) {
				throw new BusiException("解析配置文件时出错，错误信息[?]！",e.getMessage());
			}
		}
		if(lbsApiVO == null) {
			return null;
		}
		logger.info("----------------从LBS系统读取GPS设备的当前位置信息,开始--------------------");
		Map<String, String> methodMap = lbsApiVO.getMethodMap();
		StringBuffer url = new StringBuffer();
		url.append(lbsApiVO.getHost());
		url.append(methodMap.get("getPositionByGpsID"));
		logger.info("参数信息如下：");
		logger.info("URL：" + url.toString());
		String gps_idString = "";
		for(String gps_id : gps_ids){
			gps_idString += (gps_id + ",");
		}
		
		logger.info("设备号：" + gps_idString.substring(0,gps_idString.length()-1));
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("gpsID", gps_idString.substring(0,gps_idString.length()-1));
		paramMap.put("uid", lbsApiVO.getUid());
		paramMap.put("pwd", lbsApiVO.getPwd());
		try {
			String xmlText = HttpUtils.post(url.toString(), paramMap);
			logger.info("请求结果：" + xmlText);
			TrackInfoConverter converter = new TrackInfoConverter();
			logger.info("----------------从LBS系统读取GPS设备的当前位置信息,结束--------------------");
			return converter.convertResponse(xmlText);
		} catch(java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			throw new BusiException("请求LBS数据时出错,请求地址不存在,URL[?]！",url.toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw new BusiException("请求LBS数据时出错,错误信息[?]！",e.getMessage());
		}
	}

	public static RootVO getCurrentTrackVO(List<HashMap> gpsInfos){

		if(gpsInfos == null || gpsInfos.size() == 0) {
			return null;
		}

		if(lbsApiVO == null) {
			try {
				parse();
			} catch(Exception e) {
				throw new BusiException("解析配置文件时出错，错误信息[?]！",e.getMessage());
			}
		}
		if(lbsApiVO == null) {
			return null;
		}
		logger.info("----------------从LBS系统读取GPS历史信息,开始--------------------");
		Map<String, String> methodMap = lbsApiVO.getMethodMap();
		StringBuffer url = new StringBuffer();
		url.append(lbsApiVO.getHost());
		url.append(methodMap.get("getTrackInfoByGpsInfo"));
		logger.info("参数信息如下：");
		logger.info("URL：" + url.toString());
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("gpsInfos", JacksonUtils.writeValueAsString(gpsInfos));
		paramMap.put("uid", lbsApiVO.getUid());
		paramMap.put("pwd", lbsApiVO.getPwd());
		try {
			String xmlText = HttpUtils.post(url.toString(), paramMap);
			logger.info("请求结果：" + xmlText);
			TrackInfoConverter converter = new TrackInfoConverter();
			logger.info("----------------从LBS系统读取GPS历史信息,结束--------------------");
			return converter.convertResponse(xmlText);
		} catch(java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			throw new BusiException("请求LBS数据时出错,请求地址不存在,URL[?]！",url.toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw new BusiException("请求LBS数据时出错,错误信息[?]！",e.getMessage());
		}
	}
}
