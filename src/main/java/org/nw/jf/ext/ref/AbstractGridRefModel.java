package org.nw.jf.ext.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.weaver.loadtime.WeavingURLClassLoader;
import org.nw.jf.ext.ListColumn;
import org.nw.jf.ext.ref.win.AbstractRefWindow;
import org.nw.jf.ext.ref.win.GridRefWindow;
import org.nw.web.utils.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 表格参照抽象类
 * 
 * @author xuqc
 * @date 2011-6-17
 */
public abstract class AbstractGridRefModel extends BaseRefModel {

	private static final long serialVersionUID = -2968153562739455924L;

	/**
	 * 返回表格显示列的字段编码
	 * 
	 * @return
	 */
	protected abstract String[] getFieldCode();

	/**
	 * 返回表格显示列的字段名称
	 * 
	 * @return
	 */
	protected abstract String[] getFieldName();

	/**
	 * 列的字段类型，包括：string,int,float,date,默认是string
	 * 
	 * @return
	 */
	protected String[] getFieldType() {
		return null;
	}

	/**
	 * 表格隐藏列的字段编码
	 * 
	 * @return
	 */
	protected String[] getHiddenFieldCode() {
		return null;
	}

	/**
	 * 表格隐藏列的字段名称
	 * 
	 * @return
	 */
	protected String[] getHiddenFieldName() {
		return null;
	}

	/**
	 * 表格隐藏列的字段类型，包括：string,int,float,date,默认string
	 * 
	 * @return
	 */
	protected String[] getHiddenFieldType() {
		return null;
	}

	/**
	 * 表格加载数据的url，子类不需要改变
	 * 
	 * @return
	 */
	final protected String getGridDataUrl() {
		return WebUtils.getContextPath() + getRequestMappingValue() + "/load4Grid.json";
	}

	/**
	 * 返回列宽度的map子类可以继承，否则使用默认宽度，数据格式如：dept_code:100
	 * 
	 * @return
	 */
	protected Map<String, Integer> getColumnWithMap() {
		return null;
	}

	/**
	 * 表格的列模型包括：显示字段，隐藏字段，以及pk字段
	 * 
	 * @return
	 */
	protected List<ListColumn> genListColumn() {
		List<ListColumn> extGridColumnDescns = new ArrayList<ListColumn>();
		Map<String, Integer> withMap = this.getColumnWithMap();
		String pkField = this.getPkFieldCode();
		ListColumn column = new ListColumn();
		column.setType("string");
		column.setDataIndex(pkField);
		column.setHidden(true);
		extGridColumnDescns.add(column);
		String[] fieldCode = this.getFieldCode();
		String[] fieldType = this.getFieldType();
		String[] fieldName = this.getFieldName();
		if(fieldCode == null) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("Error message: " + this.getClass ().getName() + " must inherit the getFieldCode method");
			}else if( WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("错误信息:" + this.getClass().getName() + "必须继承getFieldCode方法");
			}
			throw new RuntimeException("错误信息:" + this.getClass().getName() + "必须继承getFieldCode方法");
		}
		if(fieldName == null) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("Error message: " + this.getClass ().getName() + " must inherit the getFieldCode method");
			}else if( WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("错误信息:" + this.getClass().getName() + "必须继承getFieldCode方法");
			}
			throw new RuntimeException("错误信息:" + this.getClass().getName() + "必须继承getFieldName方法");
		}
		for(int i = 0; i < fieldCode.length; i++) {
			column = new ListColumn();
			if(fieldType != null) {
				column.setType(fieldType[i]);
			} else {
				column.setType("string");
			}
			column.setDataIndex(fieldCode[i]);
			// column.setHidden(false);
			column.setSortable(null);// 参照窗口不要排序了，这个属性不输出到前台
			column.setHeader(fieldName[i]);
			if(withMap != null) {
				column.setWidth(withMap.get(fieldCode[i]));
			}
			extGridColumnDescns.add(column);
		}
		String[] hiddenFieldCode = this.getHiddenFieldCode();
		String[] hiddenFieldType = this.getHiddenFieldType();
		if(hiddenFieldCode != null) {
			for(int i = 0; i < hiddenFieldCode.length; i++) {
				column = new ListColumn();
				if(hiddenFieldType != null) {
					column.setType(hiddenFieldType[i]);
				} else {
					column.setType("string");
				}
				column.setDataIndex(hiddenFieldCode[i]);
				column.setHidden(true);
				extGridColumnDescns.add(column);
			}
		}
		return extGridColumnDescns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uft.webnc.jf.ext.ref.BaseRefModel#genRefWindow()
	 */
	public AbstractRefWindow genRefWindow() {
		return new GridRefWindow(getGridDataUrl(), genListColumn());
	}

	/**
	 * 加载表格数据的方法，必须被继承
	 */
	@RequestMapping(value = "/load4Grid.json")
	@ResponseBody
	public abstract Map<String, Object> load4Grid(HttpServletRequest request);
}
