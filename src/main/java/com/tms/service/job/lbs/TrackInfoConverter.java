package com.tms.service.job.lbs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.nw.basic.util.StringUtils;
import org.nw.job.IConverter;
import org.nw.vo.api.RootVO;
import org.nw.xml.BeanXmlMapping;

/**
 * lbs返回数据的转换类
 * 
 * @author xuqc
 * @date 2014-11-14 下午05:35:34
 */
public class TrackInfoConverter implements IConverter {

	Logger logger = Logger.getLogger(TrackInfoConverter.class);

	@SuppressWarnings("rawtypes")
	public RootVO convertResponse(String xmlText) {
		if(StringUtils.isBlank(xmlText)) {
			return null;
		}
		RootVO rootVO = new RootVO();
		List<TrackVO> vos = new ArrayList<TrackVO>();
		try {
			Document doc = DocumentHelper.parseText(xmlText);
			Node result = doc.selectSingleNode("/root/result");
			if(result != null) {
				rootVO.setResult(Boolean.valueOf(result.getText()));
			}
			Node msg = doc.selectSingleNode("/root/msg");
			if(msg != null) {
				rootVO.setMsg(msg.getText());
			}
			Node source = doc.selectSingleNode("/root/source");
			if(source != null) {
				rootVO.setSource(source.getText());
			}

			List nodeList = doc.selectNodes("/root/dataset/data");
			for(int i = 0; i < nodeList.size(); i++) {
				Element el = (Element) nodeList.get(i);
				BeanXmlMapping mapper = new BeanXmlMapping(TrackVO.class);
				TrackVO vo = (TrackVO) mapper.getBeanFromNode(el);
				vos.add(vo);
			}
			rootVO.setDataset(vos);
		} catch(Exception e) {
			logger.error("解析返回数据出错，错误信息：" + e.getMessage());
			e.printStackTrace();
		}
		return rootVO;
	}

}
