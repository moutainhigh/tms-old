package org.nw.jf.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.nw.dao.PaginationVO;

/**
 * ulw底部分页栏
 * <p>
 * <uft:pagingToolbar paginationVO="${paginationVO}"/>
 * </p>
 * 
 * @author xuqc
 * @date 2012-5-16
 */
public class PagingToolbarTag extends TagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -103987535518539202L;
	private PaginationVO paginationVO;

	public int doEndTag() throws JspException {
		int result = super.doEndTag();
		if(paginationVO == null || paginationVO.getTotalCount() == 0) {
			return result;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='fn_l'>当前共有<span class='r'>");
		sb.append(paginationVO.getTotalCount());
		sb.append("</span>条数据</div>");
		sb.append("<div class='pagenum'>");
		sb.append(PaginationVO.getListBtmPagingbar(paginationVO));
		sb.append("</div>");
		JspWriter writer = this.pageContext.getOut();
		try {
			writer.print(sb.toString()); // 写入客户端
		} catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public PaginationVO getPaginationVO() {
		return paginationVO;
	}

	public void setPaginationVO(PaginationVO paginationVO) {
		this.paginationVO = paginationVO;
	}

}
