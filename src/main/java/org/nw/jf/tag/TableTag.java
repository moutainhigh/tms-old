package org.nw.jf.tag;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.nw.dao.PaginationVO;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;

/**
 * ulw表格tag的处理类
 * <p>
 * <uft:table paginationVO="${paginationVO}" fieldVOs="${headerListFieldVOs}"/>
 * </p>
 * 
 * @author xuqc
 * @date 2012-5-16
 */
public class TableTag extends TagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7006531794198078258L;
	// 用于生成表格header的配置信息
	private List fieldVOs;
	// 是否加入行号列
	private Boolean isAddNm = true;
	// 是否加入checkbox列
	private Boolean isAddCheckbox = true;
	// 是否加入操作列
	private Boolean isAddProcessor = true;
	private PaginationVO paginationVO;

	public int doEndTag() throws JspException {
		int result = super.doEndTag();
		if(fieldVOs == null || fieldVOs.size() == 0) {
			return result;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("<div class='boxlist'>");
		sb.append("<table id='content' class='bought'>");
		sb.append(getTHead());
		if(paginationVO != null && paginationVO.getItems() != null && paginationVO.getItems().size() > 0) {
			sb.append(getTBody());
		}
		sb.append("</table>");
		sb.append("</div>");
		JspWriter writer = this.pageContext.getOut();
		try {
			writer.print(sb.toString()); // 写入客户端
		} catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 返回thead的内容
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-5-16
	 * 
	 */
	private String getTHead() {
		StringBuffer sb = new StringBuffer();
		sb.append("<thead>");
		sb.append("<tr class='sep-start'>");
		if(isAddNm.booleanValue()) {
			sb.append("<th style='width:20px'></th>");
		}
		if(isAddCheckbox.booleanValue()) {
			sb.append("<th style='width:20px'><input type='checkbox' id='checkbox_0' onclick='selectAll(this)'/></th>");
		}
		if(isAddProcessor.booleanValue()) {
			sb.append("<th>操作</th>");
		}
		if(fieldVOs != null) {
			for(int i = 0; i < fieldVOs.size(); i++) {
				BillTempletBVO fieldVO = (BillTempletBVO) fieldVOs.get(i);
				if(fieldVO.getListshowflag().booleanValue()) {
					// 显示列
					sb.append("<th itemKey='" + fieldVO.getItemkey() + "'>" + fieldVO.getDefaultshowname() + "</th>");
				} else {
					// 隐藏列
					sb.append("<th itemKey='" + fieldVO.getItemkey() + "' style='display:none;'>"
							+ fieldVO.getDefaultshowname() + "</th>");
				}
			}
		}
		sb.append("</tr>");
		sb.append("</thead>");
		return sb.toString();
	}

	/**
	 * 返回tbody内容
	 * 
	 * @return
	 * @author xuqc
	 * @date 2012-5-16
	 * 
	 */
	@SuppressWarnings("rawtypes")
	private String getTBody() {
		StringBuffer sb = new StringBuffer();
		sb.append("<tbody>");
		if(paginationVO != null && paginationVO.getItems() != null && paginationVO.getItems().size() > 0) {
			List items = paginationVO.getItems();
			for(int i = 0; i < items.size(); i++) {
				List item = (List) items.get(i);
				sb.append(buildOneRow(item, i));
			}
		}
		sb.append("</tbody>");
		return sb.toString();
	}

	/**
	 * 根据数据生成一行的tr
	 * 
	 * @param item
	 * @param index
	 * @return
	 * @author xuqc
	 * @date 2012-6-5
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public String buildOneRow(List item, int index) {
		if(item == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>");
		if(isAddNm.booleanValue()) {
			sb.append("<td align='center'>" + (index + 1) + "</td>");
		}
		if(isAddCheckbox.booleanValue()) {
			sb.append("<td align='center'><input type='checkbox' id='checkbox_" + (index + 1) + "'/></td>");
		}
		if(isAddProcessor.booleanValue()) {
			sb.append("<td><a href='javascript:show()'>查看</a></td>");
		}
		for(int j = 0; j < item.size(); j++) {
			BillTempletBVO fieldVO = (BillTempletBVO) fieldVOs.get(j);
			if(fieldVO.getListshowflag().booleanValue()) {
				sb.append("<td");
				if(fieldVO.getDatatype().intValue() == UiConstants.DATATYPE.INTEGER.intValue()
						|| fieldVO.getDatatype().intValue() == UiConstants.DATATYPE.DECIMAL.intValue()) {
					sb.append(" class='txr'");
				}
				sb.append(">");
				if(item.get(j) != null) {
					sb.append(item.get(j));
				}
				sb.append("</td>");
			} else {
				sb.append("<td style='display:none'>");
				if(item.get(j) != null) {
					sb.append(item.get(j));
				}
				sb.append("</td>");
			}
		}
		sb.append("</tr>");
		return sb.toString();
	}

	public List getFieldVOs() {
		return fieldVOs;
	}

	public void setFieldVOs(List fieldVOs) {
		this.fieldVOs = fieldVOs;
	}

	public Boolean getIsAddNm() {
		return isAddNm;
	}

	public void setIsAddNm(Boolean isAddNm) {
		this.isAddNm = isAddNm;
	}

	public Boolean getIsAddCheckbox() {
		return isAddCheckbox;
	}

	public void setIsAddCheckbox(Boolean isAddCheckbox) {
		this.isAddCheckbox = isAddCheckbox;
	}

	public Boolean getIsAddProcessor() {
		return isAddProcessor;
	}

	public void setIsAddProcessor(Boolean isAddProcessor) {
		this.isAddProcessor = isAddProcessor;
	}

	public PaginationVO getPaginationVO() {
		return paginationVO;
	}

	public void setPaginationVO(PaginationVO paginationVO) {
		this.paginationVO = paginationVO;
	}
}
