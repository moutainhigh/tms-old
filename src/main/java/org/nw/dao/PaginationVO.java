package org.nw.dao;

import java.util.List;
import java.util.Map;

/**
 * 分页查询vo 2011-4-9 注：本类可能在jsp中直接使用，所以不要使用泛型，切记<br/>
 * XXX 不再使用低版本的tomcat作为容器,这里可以使用泛型
 * 
 * @author fangw
 */
public class PaginationVO {
	public static final String script = "_ChangePage";

	public final static int PAGESIZE = 10;

	private int pageSize = PAGESIZE;

	private int pageNumber;

	private int totalCount = 0;

	/**
	 * 这里用了弱类型，因为这个放的格式有两种，并不推荐采用这种方式，但这样在执行公式时确实方便，因为要从原本的super变成map
	 */
	private List<?> items;

	/**
	 * 当使用pk定位查询分页时，第一次查询需要返回该值，存储所有pk值
	 */
	private List<?> pks;

	/**
	 * 合计行的数据
	 */
	private Map<String, Object> summaryRowMap;

	public PaginationVO() {

	}

	public PaginationVO(int totalCount, int offset, int pageSize) {
		int newoffset = offset;
		if(newoffset > totalCount) {
			newoffset = totalCount - pageSize;
		}
		this.setPageSize(pageSize);
		this.setTotalCount(totalCount);
		if(totalCount == 0) {
			this.setPageNumber(0);
		} else {
			this.setPageNumber((newoffset / pageSize) + 1);
		}
		// 如果传入的页数不正确，则查不出数据，不需要重新计算。
		// recomputePageNumber();
	}

	// private void recomputePageNumber() {
	// if(Integer.MAX_VALUE == this.pageNumber || this.pageNumber >
	// getLastPageNumber()) { // last
	// this.pageNumber = getLastPageNumber();
	// }
	// }

	public int getStartOffset() {
		int offset = (this.pageNumber - 1) * this.pageSize + 1;
		if(offset < 0) {
			offset = 0;
		}
		return offset;
	}

	public int getEndOffset() {
		return this.pageNumber * this.pageSize;
	}

	@SuppressWarnings("rawtypes")
	public List getItems() {
		return items;
	}

	@SuppressWarnings("rawtypes")
	public void setItems(List items) {
		this.items = items;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getDisPageNumber() {
		return getPageNumber() + 1;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public boolean isFirstPage() {
		return getPageNumber() == 0;
	}

	public boolean isLastPage() {
		return getPageNumber() + 1 >= getDisLastPageNumber();
	}

	public boolean hasNextPage() {
		return getLastPageNumber() > getPageNumber() + 1;
	}

	public boolean hasPreviousPage() {
		return getPageNumber() > 0;
	}

	public int getFirstPageNumber() {
		return 0;
	}

	public int getDisLastPageNumber() {
		return getLastPageNumber();
	}

	public int getLastPageNumber() {
		// java.lang.ArithmeticException: / by zero
		if(0 == this.pageSize) {
			return 0;
		}
		return totalCount % this.pageSize == 0 ? totalCount / this.pageSize : totalCount / this.pageSize + 1;
	}

	public int getFirstItemNumber() {
		return getPageNumber() * getPageSize() + 1;
	}

	public int getLastItemNumber() {
		int fullPage = getFirstItemNumber() + getPageSize() - 1;
		return getTotalCount() < fullPage ? getTotalCount() : fullPage;
	}

	public int getNextPageNumber() {
		return getPageNumber() + 1;
	}

	public int getPreviousPageNumber() {
		return getPageNumber() - 1;
	}

	/**
	 * @return the pks
	 */
	@SuppressWarnings("rawtypes")
	public List getPks() {
		return pks;
	}

	/**
	 * @param pks
	 *            the pks to set
	 */
	@SuppressWarnings("rawtypes")
	public void setPks(List pks) {
		this.pks = pks;
	}

	public String getLiteHtml() {
		return getLiteHtml(script);
	}

	/**
	 * 生成通用分页栏
	 * 
	 * @return
	 */
	public String getHtml() {
		return getHtml(script);
	}

	/**
	 * <code>
	 * <li class="prev-page"><a href="#" class="" onclick="javascript:void(0)">上一页</a></li>
	 * <li class="next-page"><a href="#" class="disabled" onclick="javascript:void(0)">下一页</a></li>
	 * </code>
	 * 
	 * @param url
	 * @return
	 */
	public String getLiteHtml(String script) {
		// 总页数 = (总记录数+每页记录数-1)/每页记录数
		int totalPages = (this.totalCount + this.pageSize - 1) / this.pageSize;

		StringBuffer sb = new StringBuffer();
		if(pageNumber > 1) {
			sb.append("<li class=\"prev-page\"><a href=\"javascript:;\" onclick=\"" + script + ".call(this,"
					+ (pageNumber - 1) + "," + this.pageSize + ")\">上一页</a></li>");
		} else {
			// 禁用的上一页
			sb.append("<li class=\"prev-page\"><a href=\"javascript:;\" class=\"disabled\" onclick=\"javascript:void(0)\">上一页</a></li>");
		}
		sb.append("<li class=\"page-num\">" + pageNumber + "/" + totalPages + "</li>");
		if(pageNumber == totalPages) {
			// 禁用的下一页
			sb.append("<li class=\"next-page\"><a href=\"javascript:;\" class=\"disabled\" onclick=\"javascript:void(0)\">下一页</a></li>");
		} else {
			sb.append("<li class=\"next-page\"><a href=\"javascript:;\" onclick=\"" + script + ".call(this,"
					+ (pageNumber + 1) + "," + this.pageSize + ")\">下一页</a></li>");
		}
		return sb.toString();
	}

	/**
	 * 生成通用分页栏
	 * 
	 * @param script
	 *            跳转函数
	 * @return
	 */
	public String getHtml(String script) {
		// 总页数 = (总记录数+每页记录数-1)/每页记录数
		int totalPages = 1;
		// 若每页记录数为-1，则不分页
		if(!(this.pageSize == -1)) {
			totalPages = (this.totalCount + this.pageSize - 1) / this.pageSize;
		}

		StringBuffer sb = new StringBuffer();
		// 生成"上一页"
		if(this.pageNumber > 1) {
			sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + (pageNumber - 1) + ","
					+ this.pageSize + "); return false;\">&laquo;</a>");
		} else {
			// 禁用的上一页
			sb.append("<span class=\"disabled\">&laquo;</span>");
		}
		// 生成中间页码
		for(int i = 1; i <= totalPages; i++) {
			if(i > 2 && pageNumber > 5) {
				sb.append("......");
				i = pageNumber - 2;
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
						+ "); return false;\">" + i + "</a>");
				i = pageNumber - 1;
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
						+ "); return false;\">" + i + "</a>");
				i = pageNumber;
				sb.append("<span class=\"current\">" + i + "</span>");
				if(this.pageNumber + 1 <= totalPages) {
					i = pageNumber + 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
				}
				if(this.pageNumber + 2 <= totalPages) {
					i = pageNumber + 2;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
				}
				if(totalPages - pageNumber > 4) {
					sb.append("......");
				}
				if(totalPages - 1 > i) {
					i = totalPages - 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
				}
				if(totalPages > i) {
					i = totalPages;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
				}
				break;
			}
			if(i == pageNumber) {
				sb.append("<span class=\"current\">" + i + "</span>");
			} else {
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
						+ "); return false;\">" + i + "</a>");
			}
			if(i == pageNumber) {
				if(totalPages - pageNumber > 4) {
					i = pageNumber + 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
					i = pageNumber + 2;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + this.pageSize
							+ "); return false;\">" + i + "</a>");
					sb.append("......");
					i = totalPages - 2;
				}
			}
		}
		// 生成"下一页"
		if(pageNumber == totalPages) {
			// 禁用的下一页
			sb.append("<span class=\"disabled\">&raquo;</span>");
		} else {
			sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + (pageNumber + 1) + ","
					+ this.pageSize + "); return false;\">&raquo;</a>");
		}

		sb.append("跳至<input id=\"_PageNum\" type=\"text\" class=\"snum\" onkeydown=\"if(event.keyCode==13){" + script
				+ ".call(this,document.getElementById('_PageNum').value," + this.pageSize + ")};\"/>");
		sb.append("<button type=\"button\" class=\"sgo\" onclick=\"" + script
				+ ".call(this,document.getElementById('_PageNum').value," + this.pageSize + ")\">GO</button>");
		return sb.toString();
	}

	/**
	 * 生成表格顶部的分页栏，使用静态方法，当传入参数为空时，同样可以生成
	 * 
	 * @param paginationVO
	 * @return
	 * @author xuqc
	 * @date 2012-5-3
	 */
	public static String getLiteTopPagingbar(PaginationVO paginationVO) {
		int totalPages = 0;
		int pageNumber = 0;
		int pageSize = PAGESIZE;
		if(paginationVO != null) {
			// 总页数 = (总记录数+每页记录数-1)/每页记录数
			totalPages = (paginationVO.totalCount + paginationVO.pageSize - 1) / paginationVO.pageSize;
			pageNumber = paginationVO.pageNumber;
			pageSize = paginationVO.pageSize;
		}

		StringBuffer sb = new StringBuffer();
		if(pageNumber > 1) {
			sb.append("<li class=\"prev-page\"><a href=\"javascript:;\" onclick=\"" + script + ".call(this,"
					+ (pageNumber - 1) + "," + pageSize + ")\">上一页</a></li>");
		} else {
			// 禁用的上一页
			sb.append("<li class=\"prev-page\"><a href=\"javascript:;\" class=\"disabled\" onclick=\"javascript:void(0)\">上一页</a></li>");
		}
		sb.append("<li class=\"page-num\">" + pageNumber + "/" + totalPages + "</li>");
		if(pageNumber == totalPages) {
			// 禁用的下一页
			sb.append("<li class=\"next-page\"><a href=\"javascript:;\" class=\"disabled\" onclick=\"javascript:void(0)\">下一页</a></li>");
		} else {
			sb.append("<li class=\"next-page\"><a href=\"javascript:;\" onclick=\"" + script + ".call(this,"
					+ (pageNumber + 1) + "," + pageSize + ")\">下一页</a></li>");
		}
		return sb.toString();
	}

	/**
	 * 生成表格底部的分页栏，使用静态方法，当传入参数为空时，同样可以生成
	 * 
	 * @param paginationVO
	 * @return
	 * @author xuqc
	 * @date 2012-5-3
	 */
	public static String getListBtmPagingbar(PaginationVO paginationVO) {
		int totalPages = 0;
		int pageNumber = 0;
		int pageSize = PAGESIZE;
		if(paginationVO != null) {
			// 总页数 = (总记录数+每页记录数-1)/每页记录数
			totalPages = (paginationVO.totalCount + paginationVO.pageSize - 1) / paginationVO.pageSize;
			pageNumber = paginationVO.pageNumber;
			pageSize = paginationVO.pageSize;
		}

		StringBuffer sb = new StringBuffer();
		// 生成"上一页"
		if(pageNumber > 1) {
			sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + (pageNumber - 1) + ","
					+ pageSize + ")\">&laquo;</a>");
		} else {
			// 禁用的上一页
			sb.append("<span class=\"disabled\">&laquo;</span>");
		}
		// 生成中间页码
		for(int i = 1; i <= totalPages; i++) {
			if(i > 2 && pageNumber > 5) {
				sb.append("......");
				i = pageNumber - 2;
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize + ")\">"
						+ i + "</a>");
				i = pageNumber - 1;
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize + ")\">"
						+ i + "</a>");
				i = pageNumber;
				sb.append("<span class=\"current\">" + i + "</span>");
				if(pageNumber + 1 <= totalPages) {
					i = pageNumber + 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
				}
				if(pageNumber + 2 <= totalPages) {
					i = pageNumber + 2;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
				}
				if(totalPages - pageNumber > 4) {
					sb.append("......");
				}
				if(totalPages - 1 > i) {
					i = totalPages - 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
				}
				if(totalPages > i) {
					i = totalPages;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
				}
				break;
			}
			if(i == pageNumber) {
				sb.append("<span class=\"current\">" + i + "</span>");
			} else {
				sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize + ")\">"
						+ i + "</a>");
			}
			if(i == pageNumber) {
				if(totalPages - pageNumber > 4) {
					i = pageNumber + 1;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
					i = pageNumber + 2;
					sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + i + "," + pageSize
							+ ")\">" + i + "</a>");
					sb.append("......");
					i = totalPages - 2;
				}
			}
		}
		// 生成"下一页"
		if(pageNumber == totalPages) {
			// 禁用的下一页
			sb.append("<span class=\"disabled\">&raquo;</span>");
		} else {
			sb.append("<a href=\"javascript:;\" onclick=\"" + script + ".call(this," + (pageNumber + 1) + ","
					+ pageSize + ")\">&raquo;</a>");
		}

		sb.append("跳至<input id=\"_PageNum\" type=\"text\" value=\"" + pageNumber
				+ "\" class=\"snum\" onkeydown=\"if(event.keyCode==13){" + script
				+ ".call(this,document.getElementById('_PageNum').value," + pageSize + ")};\"/>");
		sb.append("<button type=\"button\" class=\"sgo\" onclick=\"" + script
				+ ".call(this,document.getElementById('_PageNum').value," + pageSize + ")\">GO</button>");
		return sb.toString();
	}

	public Map<String, Object> getSummaryRowMap() {
		return summaryRowMap;
	}

	public void setSummaryRowMap(Map<String, Object> summaryRowMap) {
		this.summaryRowMap = summaryRowMap;
	}

}
