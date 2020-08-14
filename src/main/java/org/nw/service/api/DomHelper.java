package org.nw.service.api;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.nw.vo.api.RootVO;
import org.nw.xml.BeanXmlMapping;

/**
 * document操作类
 * 
 * @author xuqc
 * @date 2015-1-24 下午05:37:10
 */
public class DomHelper {

	/**
	 * <root><result></result><dataset><data></data></dataset><msg></msg></root>
	 * 
	 * @param rootVO
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String asXML(RootVO rootVO) {
		if(rootVO == null) {
			return null;
		}
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");

		Element result = root.addElement("result");
		result.setText(String.valueOf(rootVO.isResult()));

		Element source = root.addElement("source");
		source.setText(String.valueOf(rootVO.getSource()));

		if(StringUtils.isNotBlank(rootVO.getMsg())) {
			Element msg = doc.addElement("msg");
			msg.setText(rootVO.getMsg());
		}
		List dataset = rootVO.getDataset();
		if(dataset != null) {
			Element datasetEl = root.addElement("dataset");
			BeanXmlMapping mapping = new BeanXmlMapping();
			mapping.setParentNode("data");
			try {
				List<Node> nodeList = mapping.getNodesFromBeans(dataset);
				if(nodeList != null) {
					for(Node node : nodeList) {
						datasetEl.add(node);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return doc.asXML();
	}
}
