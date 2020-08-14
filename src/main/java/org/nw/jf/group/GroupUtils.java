package org.nw.jf.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.json.JacksonUtils;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ReportTempletBVO;


/**
 * 表头分组的工具类
 * 
 * @author xuqc
 * @date 2013-3-26 上午10:21:28
 */
public class GroupUtils {

	static Logger logger = Logger.getLogger(GroupUtils.class);
	public static final String JS_GROUP_CLASS = "Ext.ux.grid.ColumnHeaderGroup";

	/**
	 * 将options字段的内容解析成vo，方便处理<br/>
	 * <root><tab code="demo_product_detail" showflag="Y" listshowflag="Y"
	 * mulicolhead="合并后名称_abc" /></root>
	 * 
	 * @param optoins
	 * @return
	 * @author xuqc
	 * @date 2013-3-26
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static OptionsVO resolveOptions(String optoins) {
		if(StringUtils.isNotBlank(optoins)) {
			try {
				Document document = (Document) DocumentHelper.parseText(optoins);
				List nodeAry = DocumentHelper.selectNodes("/root/tab", document);
				if(nodeAry != null && nodeAry.size() > 0) {
					Element tabNode = (Element) nodeAry.get(0);
					OptionsVO vo = new OptionsVO();
					vo.setCode(tabNode.attributeValue("code"));
					vo.setListshowflag(new UFBoolean(tabNode.attributeValue("listshowflag")));
					vo.setShowflag(new UFBoolean(tabNode.attributeValue("showflag")));
					String mulicolhead = tabNode.attributeValue("mulicolhead");
					if(StringUtils.isBlank(mulicolhead)) {
						// 这里存储的是nc共享页签的内容，不是分组信息
						return null;
					}
					vo.setMulicolhead(mulicolhead);
					vo.setMulicolheadAry(mulicolhead.split("_"));
					return vo;
				}
			} catch(DocumentException e) {
				logger.error("parse xml string error:" + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * <ul>
	 * <li>分组表头的示例：</li>
	 * <li>continentGroupRow at this point is:</li>
	 * <li>[</li>
	 * <li>{header: 'Asia', colspan: 4, align: 'center'},</li>
	 * <li>{header: 'Europe',colspan: 6, align: 'center'} ]</li>
	 * <li>cityGroupRow at this point is:</li>
	 * <li>[ {header:'Beijing', colspan: 2, align: 'center'},</li>
	 * <li>{header: 'Tokyo', colspan: 2,align: 'center'},</li>
	 * <li>{header: 'Berlin', colspan: 2, align: 'center'},</li>
	 * <li>{header: 'London', colspan: 2, align: 'center'},</li>
	 * <li>{header: 'Paris', colspan: 2, align: 'center'} ]</li>
	 * <li>var group = new Ext.ux.grid.ColumnHeaderGroup({ rows:
	 * [continentGroupRow, cityGroupRow] });</li>
	 * <li></li>
	 * <li>加入headerGroup的数据如：</li>
	 * <li>2，relationMap，3，relationMap，4</li>
	 * <li>relationMap包括：header，level，count</li>
	 * </ul>
	 * FIXME
	 * 该方法存在一个bug，当在一个表格中设置了一个2级的分组，和一个3级的分组时，最高级的分组计算colspan存在bug。没有加入只设置2级的列
	 * 
	 * @param fieldVOs
	 *            必须是同一个位置(表头或表体)，同一个页签（针对表体）的字段
	 * @return
	 * @author xuqc
	 * @date 2013-3-26
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static List<List<GroupVO>> buildGroupHeaderAry(List<String> optionsAry, int appendCol) {
		if(optionsAry != null) {
			int blankHeadNum = appendCol;
			List<Object> headerGroup = new ArrayList<Object>();
			// 表示分组的最大级次,会生成相应的几个的groupRow对象,比如level为2，则整个表头包括3级
			int maxLevel = 0;
			// 分组关系图,需要记住顺序
			Map<String, GroupTreeVO> relationMap = new LinkedHashMap<String, GroupTreeVO>();
			for(int i = 0; i < optionsAry.size(); i++) {
				OptionsVO optionsVO = resolveOptions(optionsAry.get(i));
				if(optionsVO != null) {
					if(blankHeadNum != 0) {
						// 空白分组头的个数
						headerGroup.add(new Integer(blankHeadNum));
						// 已收集完空白列，重新置为0
						blankHeadNum = 0;
					}
					String[] mulicolheadAry = optionsVO.getMulicolheadAry();
					if(mulicolheadAry.length > maxLevel) {
						maxLevel = mulicolheadAry.length;
					}
					int level = 1;// 级别
					for(int j = mulicolheadAry.length; j > 0; j--) {
						GroupTreeVO treeVO = relationMap.get(mulicolheadAry[j - 1]);
						if(treeVO == null) {
							treeVO = new GroupTreeVO();
							treeVO.setHeader(mulicolheadAry[j - 1]);
							treeVO.setCount(1);
							treeVO.setLevel(level);
							relationMap.put(treeVO.getHeader(), treeVO);
						} else {
							treeVO.setCount(treeVO.getCount() + 1);
						}
						level++;
					}
				} else {
					// 空白的列
					blankHeadNum++;
					if(relationMap.keySet().size() > 0) {
						headerGroup.add(relationMap);
						relationMap = new LinkedHashMap<String, GroupTreeVO>();
					}
				}
			}
			if(blankHeadNum != 0 && maxLevel > 0) {// maxLevel大于0，表示模板中设置了分组
				// blankHeadNum不等于0表示最后一个字段不处于分组中
				headerGroup.add(new Integer(blankHeadNum));
			} else if(relationMap.size() > 0) {// 表示最后一个字段处于分组中
				headerGroup.add(relationMap);
			}
			// 分组结构对象
			List<List<GroupVO>> groupRowAry = new ArrayList<List<GroupVO>>();
			for(int i = maxLevel; i > 0; i--) {
				List<GroupVO> groupRow = new ArrayList<GroupVO>();
				for(Object obj : headerGroup) {
					if(obj instanceof Integer) {
						Integer colspan = (Integer) obj;
						for(int j = 0; j < colspan; j++) {
							groupRow.add(new GroupVO());
						}
					} else {
						// instanceof LinkedHashMap
						Map<String, GroupTreeVO> map = (Map<String, GroupTreeVO>) obj;
						for(String key : map.keySet()) {
							GroupTreeVO treeVO = map.get(key);
							if(treeVO.getLevel() == i) {// 当前级别的行
								GroupVO groupVO = new GroupVO();
								groupVO.setHeader(treeVO.getHeader());
								groupVO.setColspan(treeVO.getCount());
								groupVO.setAlign(GroupVO.ALIGN[1]);
								groupRow.add(groupVO);
							}
						}
					}
				}
				groupRowAry.add(groupRow);
			}
			return groupRowAry;
		}
		return null;
	}

	/**
	 * 生成分组的定义信息，如： new Ext.ux.grid.ColumnHeaderGroup({rows : []});
	 * 
	 * @param optionsAry
	 * @return
	 * @author xuqc
	 * @date 2013-3-28
	 * 
	 */
	public static String buildGroupHeaderString(List<String> optionsAry) {
		List<List<GroupVO>> groupHeaderAry = buildGroupHeaderAry(optionsAry, 0);
		if(groupHeaderAry == null || groupHeaderAry.size() == 0) {
			// 没有分组信息
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("new ").append(JS_GROUP_CLASS)
				.append("({rows : " + JacksonUtils.writeValueAsString(groupHeaderAry) + "})");
		return sb.toString();
	}

	/**
	 * 生成分组的定义信息，如： new Ext.ux.grid.ColumnHeaderGroup({rows : []});
	 * 
	 * @param optionsAry
	 * @param appendCol
	 *            包括的附加列数，如：行号，checkbox列
	 * @return
	 * @author xuqc
	 * @date 2013-3-28
	 * 
	 */
	public static String buildGroupHeaderString(List<String> optionsAry, int appendCol) {
		List<List<GroupVO>> groupHeaderAry = buildGroupHeaderAry(optionsAry, appendCol);
		if(groupHeaderAry == null || groupHeaderAry.size() == 0) {
			// 没有分组信息
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("new ").append(JS_GROUP_CLASS)
				.append("({rows : " + JacksonUtils.writeValueAsString(groupHeaderAry) + "})");
		return sb.toString();
	}

	/**
	 * 返回options字段
	 * 
	 * @param fieldVOs
	 * @return
	 */
	public static List<String> getOptionsAry(List<BillTempletBVO> fieldVOs) {
		List<String> optionsAry = new ArrayList<String>();
		if(fieldVOs != null) {
			for(BillTempletBVO fieldVO : fieldVOs) {
				optionsAry.add(fieldVO.getOptions());
			}
		}
		return optionsAry;
	}

	/**
	 * 返回options字段
	 * 
	 * @param fieldVOs
	 * @return
	 */
	public static List<String> getOptionsAry2(List<ReportTempletBVO> fieldVOs) {
		List<String> optionsAry = new ArrayList<String>();
		if(fieldVOs != null) {
			for(ReportTempletBVO fieldVO : fieldVOs) {
				optionsAry.add(fieldVO.getOptions());
			}
		}
		return optionsAry;
	}

	public static void main(String[] args) {
		String xml = "<root><tab code=\"demo_product_detail\" showflag=\"Y\" listshowflag=\"Y\" mulicolhead=\"合并后名称_abc\" /></root>";
		OptionsVO vo = GroupUtils.resolveOptions(xml);
		System.out.println(vo.getCode());
	}
}
