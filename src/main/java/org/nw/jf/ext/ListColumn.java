/**
 * @(#)ExtListColumn.java 2010-8-26
 *                        Copyright 2000-2010 by UFida Corporation.
 *                        All rights reserved.
 *                        This software is the confidential and proprietary information of
 *                        UFida Corporation ("Confidential Information"). You shall not
 *                        disclose such Confidential Information and shall use it only in
 *                        accordance with the terms of the license agreement you entered
 *                        into with UFida.
 */
package org.nw.jf.ext;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nw.jf.UiConstants;
import org.nw.json.JacksonUtils;

/**
 * Ext Gridж
 * 
 * @author wuqb
 * @date 2010-8-26
 * @version $Revision$
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
// editor
// @JsonIgnoreProperties(value = { "editor" })
public class ListColumn implements Serializable {
	private static final long serialVersionUID = 6824148516417049668L;
	private String header;
	// 主要是参照框中冗余该字段，方便在生成参照框时生成recordType，单据的表格中recordType对象已经包含该字段，就不需要返回了。
	private String type;
	private Integer width = UiConstants.DEFAULT_WIDTH_STEP;
	private String dataIndex;
	private String id;
	private Boolean sortable;
	// 对于数字类型的列，需要定义format属性。
	private String format;
	private String xtype = UiConstants.COLUMN_XTYPE.DEFAULT.toString();
	private String editor;
	private Boolean hidden;
	private Boolean editable;
	private String align;
	private Boolean lockflag;// 锁定后它的值会参考上一行的值，对应单据模板中的【是否锁定】
	private Boolean locked; // 是否锁定该列，对应表格的列锁定

	/**
	 * 列的渲染属性,可以在程序里面指定，通常是直接设置函数名称即可 <br/>
	 * 如：setRenderer("columnRenderer");此时columnRenderer在前台是一个函数,并且注意应该实现定义该函数
	 * 需要注意到是，如果指定了自定义的renderer，那么默认的renderer将不再生效。通常的处理方式是将两个renderer进行合并
	 */
	private String renderer;

	/**
	 * 有些列已经有一个默认的renderer方法，这里定义一个before方法，用于兼容默认的renderer，
	 * 实际上是在默认的renderer中调用beforeRenderer
	 */
	private String beforeRenderer;

	/**
	 * 统计行中列的渲染属性，可以在程序里面指定，参考renderer,
	 * 如果不定义该属性，则GridSummary会默认使用renderer，所以定义该变量的时候需要注意，需要将默认处理进行合并。
	 */
	private String summaryRenderer;

	// 表格统计行的统计类型，只有设置了该值的列才会进行统计
	private String summaryType;
	// 自定义统计函数，目前使用默认函数即可
	// private String summaryRenderer;

	/**
	 * 是否可修订的标志，修订是指在修订按钮后，该field是否可编辑。 只有为该标识为true的field才能编辑
	 */
	private Boolean reviseflag;
	// 模板中【自定义项一】配置的js脚本,用于监听grid的afteredit的js脚本
	// 需要注意的是脚本不能使用双引号，js脚本可以使用单引号代替
	private String script;

	// 模板中【自定义项二】配置的js脚本，用于监听可编辑表格的beforeedit事件
	// 需要注意的是脚本不能使用双引号，js脚本可以使用单引号代替
	private String beforeEditScript;

	/**
	 * 参照所在的单据模板ID 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	protected String pkBilltemplet;

	/**
	 * 单据模板中定义该参照的主键，必须在读取单据模板时实例化 注意是单据模板，对于查询模板，不需要查询公式了
	 */
	protected String pkBilltempletB;

	/**
	 * 是否包含需要执行的编辑公式，前台会检验这个变量，如果为true则发送请求，否则不发送
	 */
	protected Boolean hasEditformula;
	// 这个样式用来定义字体的颜色和单元格的背景色，表的字段存储的是颜色的值，比如#339966，实际到这个字段变成color:#339966
	protected String css;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getDataIndex() {
		return dataIndex;
	}

	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getSortable() {
		return sortable;
	}

	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getXtype() {
		return xtype;
	}

	public void setXtype(String xtype) {
		this.xtype = xtype;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public Boolean getLockflag() {
		return lockflag;
	}

	public void setLockflag(Boolean lockflag) {
		this.lockflag = lockflag;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public String getSummaryType() {
		return summaryType;
	}

	public void setSummaryType(String summaryType) {
		this.summaryType = summaryType;
	}

	public Boolean getReviseflag() {
		return reviseflag;
	}

	public void setReviseflag(Boolean reviseflag) {
		this.reviseflag = reviseflag;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getBeforeEditScript() {
		return beforeEditScript;
	}

	public void setBeforeEditScript(String beforeEditScript) {
		this.beforeEditScript = beforeEditScript;
	}

	public String getPkBilltemplet() {
		return pkBilltemplet;
	}

	public void setPkBilltemplet(String pkBilltemplet) {
		this.pkBilltemplet = pkBilltemplet;
	}

	public String getPkBilltempletB() {
		return pkBilltempletB;
	}

	public void setPkBilltempletB(String pkBilltempletB) {
		this.pkBilltempletB = pkBilltempletB;
	}

	public Boolean getHasEditformula() {
		return hasEditformula;
	}

	public void setHasEditformula(Boolean hasEditformula) {
		this.hasEditformula = hasEditformula;
	}

	public String getSummaryRenderer() {
		return summaryRenderer;
	}

	public void setSummaryRenderer(String summaryRenderer) {
		this.summaryRenderer = summaryRenderer;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public String getBeforeRenderer() {
		return beforeRenderer;
	}

	public void setBeforeRenderer(String beforeRenderer) {
		this.beforeRenderer = beforeRenderer;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String toString() {
		String result = JacksonUtils.writeValueAsString(this);
		return reOrginize(result, this.getEditor());
	}

	public static String reOrginize(String result, String editor) {
		// 需要做处理的字段，实际上是要把引号去掉
		String[] arr = new String[] { "renderer", "beforeRenderer", "summaryRenderer" };
		for(int i = 0; i < arr.length; i++) {
			// 对于renderer属性，不能加入双引号
			int index = result.indexOf(arr[i]);
			if(index > -1) {
				// 存在renderer属性，将其值的前后引号去掉
				String prefix = result.substring(0, index + arr[i].length() + 1);// 注意：这里需要包括renderer后面的引号
				String subfix = result.substring(index + arr[i].length() + 1);
				subfix = subfix.replaceFirst("\"", "").replaceFirst("\"", ""); // 替换第一个和第二个引号
				result = prefix + subfix;
			}
		}
		// FIXME2013-4-10 不需要过滤editor字段了.在Column.js中对string类型的editor进行了处理
		// if(StringUtils.isNotBlank(editor)) {//
		// // editor在writeValueAsString时没有加入，这个在类声明中已经排除,//
		// // 对于返回的editor属性，不能包含双引号，这是使用字符串拼接的原因
		// result = result.substring(0, result.length() - 1);
		// result = result + ",editor:" + editor + "}";
		// }
		return result;
	}
}
