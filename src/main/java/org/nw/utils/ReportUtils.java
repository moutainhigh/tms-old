package org.nw.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nw.jf.UiConstants;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.ReportTempletBVO;

/**
 * 报表工具类
 * 
 * @author xuqc
 * @date 2015-1-14 下午06:12:38
 */
public class ReportUtils {

	/**
	 * 根据列名生成一个报表模板的列对象
	 * 
	 * @param pk_report_templet
	 *            报表模板PK
	 * @param objAry
	 *            包括3个元素：列的key，列名，列类型参考UiConstants.DATATYPE
	 * @return
	 */
	public static ReportTempletBVO buildField(String pk_report_templet, Object[] objAry) {
		if(objAry == null) {
			return null;
		}
		int data_type = Integer.parseInt(String.valueOf(objAry[2]));
		ReportTempletBVO fieldVO = new ReportTempletBVO();
		fieldVO.setItemkey(String.valueOf(objAry[0]));// 数据返回的是数组，所以itemkey没有多大关系，但必须唯一
		fieldVO.setData_type(data_type);
		fieldVO.setDefaultshowname(String.valueOf(objAry[1]));
		fieldVO.setShow_flag(UFBoolean.TRUE);
		if(data_type == UiConstants.DATATYPE.INTEGER.intValue() || data_type == UiConstants.DATATYPE.DECIMAL.intValue()) {
			// 数字类型，默认加上合计
			fieldVO.setTotal_flag(UFBoolean.TRUE);
		}
		fieldVO.setPk_report_templet_b(UUID.randomUUID().toString());
		fieldVO.setPk_report_templet(pk_report_templet);
		return fieldVO;
	}

	/**
	 * 根据列名生成一个报表模板的列对象
	 * 
	 * @param pk_report_templet
	 *            报表模板PK
	 * @param fieldAry
	 *            列数组，每个元素又包括数组对象，这个数组对象包括3个元素，分别是：列的key，列名，列的类型
	 * @return
	 */
	public static List<ReportTempletBVO> buildFields(String pk_report_templet, List<Object[]> fieldAry) {
		if(fieldAry == null || fieldAry.size() == 0) {
			return null;
		}
		List<ReportTempletBVO> fieldVOs = new ArrayList<ReportTempletBVO>();
		for(Object[] objAry : fieldAry) {
			ReportTempletBVO fieldVO = buildField(pk_report_templet, objAry);
			fieldVOs.add(fieldVO);
		}
		return fieldVOs;
	}

	/**
	 * 生成合并列的信息，信息类似 <root><tab code="nw_report_templet_b" showflag="Y"
	 * listshowflag="Y" mulicolhead="合并后名称" /></root>
	 * 
	 * @param mulicolhead合并后的名称
	 * @return
	 */
	public static String buildOptions(String mulicolhead) {
		String options = "<root><tab code=\"nw_report_templet_b\" showflag=\"Y\" listshowflag=\"Y\" mulicolhead=\"{mulicolhead}\" /></root>";
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("mulicolhead", mulicolhead);
		return VariableHelper.resolve(options, values);
	}
}
