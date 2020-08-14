package org.nw.jf.vo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.constants.Constants;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.COLUMN_XTYPE;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.UiConstants.FORM_XTYPE;
import org.nw.jf.UiConstants.REF_CONFIG;
import org.nw.jf.ext.Checkbox;
import org.nw.jf.ext.CheckboxGroup;
import org.nw.jf.ext.ColumnEditor;
import org.nw.jf.ext.Combox;
import org.nw.jf.ext.DisplayField;
import org.nw.jf.ext.Field;
import org.nw.jf.ext.HiddenField;
import org.nw.jf.ext.IdentityField;
import org.nw.jf.ext.ImageField;
import org.nw.jf.ext.ListColumn;
import org.nw.jf.ext.MultiSelectField;
import org.nw.jf.ext.NumberField;
import org.nw.jf.ext.PasswordField;
import org.nw.jf.ext.RadioGroup;
import org.nw.jf.ext.RecordType;
import org.nw.jf.ext.TextArea;
import org.nw.jf.ext.TextField;
import org.nw.jf.ext.TimeField;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.ext.ref.userdefine.IMultiSelect;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;

/**
 * 单据模板中设置一个字段的属性VO
 * 
 * @author xuqc
 * 
 */
public class BillTempletBVO extends SuperVO {

	private static final long serialVersionUID = -2818823901854539921L;
	private static final Log log = LogFactory.getLog(BillTempletBVO.class);

	private Integer totalflag;
	private String pk_corp;
	private UFDateTime ts;
	private String userdefine3;
	private String defaultvalue;
	private String pk_billtemplet;
	private UFBoolean userdefflag;
	private Integer showflag;
	private UFBoolean reviseflag;
	private Integer editflag;
	private String userdefine2;
	private String idcolname; // 关键字
	private Integer usereditflag;
	private String userdefine1;
	private String table_code;
	private String pk_billtemplet_b;
	private UFBoolean userreviseflag;
	private Integer showorder;
	private Integer listflag;
	private String validateformula;
	private UFBoolean leafflag;
	private Integer itemtype;
	private Integer cardflag;
	private Integer foreground;
	private Integer usershowflag;
	private UFBoolean newlineflag;
	private String table_name;
	private Integer inputlength;
	private UFBoolean listshowflag;
	private String metadataproperty;
	private Integer datatype;
	private String metadatarelation;
	private Integer pos;
	private String editformula;
	private Integer userflag;
	private Integer lockflag;
	private String options;
	private String resid_tabname;
	private String resid;
	private String reftype;
	private String itemkey;
	private Integer dr;
	private String metadatapath;
	private String loadformula;
	private Integer nullflag;
	private Integer width;
	private String defaultshowname;
	private UFBoolean if_revise;

	/**
	 * 纯web自定义项：精度(小数点后位数)
	 */
	private int precision;
	/**
	 * 纯web自定义项：0是否转null
	 */
	private Boolean zeroToNull;

	private Boolean locked; // 是否锁定列（表格锁定列），这个与lockflag的意义不同

	private String renderer; // 渲染函数，目前只用于表体

	private String beforeRenderer;// 渲染函数，当存在默认的renderer时，不要设置renderer，设置这个

	private String summaryRenderer;// 统计行的列渲染函数，只用于表格

	public BillTempletBVO() {
	}

	public BillTempletBVO(String pk_billtemplet_b, String itemkey) {
		this.pk_billtemplet_b = pk_billtemplet_b;
		this.itemkey = itemkey;
	}

	public String getItemkey() {
		return this.itemkey;
	}

	// 避免生成的id重复，使用tablecode_itemkey作为id
	// 目前没有使用autoLoad加载页面，不会出现id重复情况
	/**
	 * @param isForColumn
	 *            是否可编辑表格使用的对象
	 */
	public String genUniqueId() {
		if(this.isBody()) {
			// 对于可编辑表格的编辑对象id统一加入一个前缀，这样避免了与表头或表尾的表单域对象id冲突。
			// 如对于下拉框，如果表体也存在相同的对象，那么其变量名称和id都会不同，这样避免冲突
			return "col_" + getItemkey().trim();
		}
		return /* this.getTable_code()+"_"+ */getItemkey().trim();
	}

	public String getBeforeRenderer() {
		return beforeRenderer;
	}

	public void setBeforeRenderer(String beforeRenderer) {
		this.beforeRenderer = beforeRenderer;
	}

	public void setItemkey(String itemkey) {
		this.itemkey = itemkey;
	}

	public Integer getTotalflag() {
		return totalflag;
	}

	public void setTotalflag(Integer totalflag) {
		this.totalflag = totalflag;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pkCorp) {
		pk_corp = pkCorp;
	}

	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getUserdefine3() {
		return userdefine3;
	}

	public void setUserdefine3(String userdefine3) {
		this.userdefine3 = userdefine3;
	}

	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

	public String getPk_billtemplet() {
		return pk_billtemplet;
	}

	public void setPk_billtemplet(String pkBilltemplet) {
		pk_billtemplet = pkBilltemplet;
	}

	public UFBoolean getUserdefflag() {
		return userdefflag;
	}

	public void setUserdefflag(UFBoolean userdefflag) {
		this.userdefflag = userdefflag;
	}

	public Integer getShowflag() {
		return showflag;
	}

	public void setShowflag(Integer showflag) {
		this.showflag = showflag;
	}

	public UFBoolean getReviseflag() {
		return reviseflag;
	}

	public void setReviseflag(UFBoolean reviseflag) {
		this.reviseflag = reviseflag;
	}

	public Integer getEditflag() {
		return editflag;
	}

	public void setEditflag(Integer editflag) {
		this.editflag = editflag;
	}

	public String getUserdefine2() {
		return userdefine2;
	}

	public void setUserdefine2(String userdefine2) {
		this.userdefine2 = userdefine2;
	}

	public String getIdcolname() {
		return idcolname;
	}

	public void setIdcolname(String idcolname) {
		this.idcolname = idcolname;
	}

	public Integer getUsereditflag() {
		return usereditflag;
	}

	public void setUsereditflag(Integer usereditflag) {
		this.usereditflag = usereditflag;
	}

	public String getUserdefine1() {
		return userdefine1;
	}

	public void setUserdefine1(String userdefine1) {
		this.userdefine1 = userdefine1;
	}

	public String getTable_code() {
		return table_code;
	}

	public void setTable_code(String tableCode) {
		table_code = tableCode;
	}

	public String getPk_billtemplet_b() {
		return pk_billtemplet_b;
	}

	public void setPk_billtemplet_b(String pkBilltempletB) {
		pk_billtemplet_b = pkBilltempletB;
	}

	public UFBoolean getUserreviseflag() {
		return userreviseflag;
	}

	public void setUserreviseflag(UFBoolean userreviseflag) {
		this.userreviseflag = userreviseflag;
	}

	public Integer getShoworder() {
		return showorder;
	}

	public void setShoworder(Integer showorder) {
		this.showorder = showorder;
	}

	public Integer getListflag() {
		return listflag;
	}

	public void setListflag(Integer listflag) {
		this.listflag = listflag;
	}

	public String getValidateformula() {
		return validateformula;
	}

	public void setValidateformula(String validateformula) {
		this.validateformula = validateformula;
	}

	public UFBoolean getLeafflag() {
		return leafflag;
	}

	public void setLeafflag(UFBoolean leafflag) {
		this.leafflag = leafflag;
	}

	public Integer getItemtype() {
		return itemtype;
	}

	public void setItemtype(Integer itemtype) {
		this.itemtype = itemtype;
	}

	public Integer getCardflag() {
		return cardflag;
	}

	public void setCardflag(Integer cardflag) {
		this.cardflag = cardflag;
	}

	public Integer getForeground() {
		return foreground;
	}

	public void setForeground(Integer foreground) {
		this.foreground = foreground;
	}

	public Integer getUsershowflag() {
		return usershowflag;
	}

	public void setUsershowflag(Integer usershowflag) {
		this.usershowflag = usershowflag;
	}

	public UFBoolean getNewlineflag() {
		return newlineflag;
	}

	public void setNewlineflag(UFBoolean newlineflag) {
		this.newlineflag = newlineflag;
	}

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String tableName) {
		table_name = tableName;
	}

	public Integer getInputlength() {
		return inputlength;
	}

	public void setInputlength(Integer inputlength) {
		this.inputlength = inputlength;
	}

	public UFBoolean getListshowflag() {
		return listshowflag;
	}

	public void setListshowflag(UFBoolean listshowflag) {
		this.listshowflag = listshowflag;
	}

	public String getMetadataproperty() {
		return metadataproperty;
	}

	public void setMetadataproperty(String metadataproperty) {
		this.metadataproperty = metadataproperty;
	}

	public Integer getDatatype() {
		return datatype;
	}

	public void setDatatype(Integer datatype) {
		this.datatype = datatype;
	}

	public String getMetadatarelation() {
		return metadatarelation;
	}

	public void setMetadatarelation(String metadatarelation) {
		this.metadatarelation = metadatarelation;
	}

	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	public String getEditformula() {
		return editformula;
	}

	public void setEditformula(String editformula) {
		this.editformula = editformula;
	}

	public Integer getUserflag() {
		return userflag;
	}

	public void setUserflag(Integer userflag) {
		this.userflag = userflag;
	}

	public Integer getLockflag() {
		return lockflag;
	}

	public void setLockflag(Integer lockflag) {
		this.lockflag = lockflag;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public String getResid_tabname() {
		return resid_tabname;
	}

	public void setResid_tabname(String residTabname) {
		resid_tabname = residTabname;
	}

	public String getResid() {
		return resid;
	}

	public void setResid(String resid) {
		this.resid = resid;
	}

	public String getReftype() {
		return reftype;
	}

	public void setReftype(String reftype) {
		this.reftype = reftype;
	}

	public Integer getDr() {
		return dr;
	}

	public void setDr(Integer dr) {
		this.dr = dr;
	}

	public String getMetadatapath() {
		return metadatapath;
	}

	public void setMetadatapath(String metadatapath) {
		this.metadatapath = metadatapath;
	}

	public String getLoadformula() {
		return loadformula;
	}

	public void setLoadformula(String loadformula) {
		this.loadformula = loadformula;
	}

	public Integer getNullflag() {
		return nullflag;
	}

	public void setNullflag(Integer nullflag) {
		this.nullflag = nullflag;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getDefaultshowname() {
		return defaultshowname;
	}

	public void setDefaultshowname(String defaultshowname) {
		this.defaultshowname = defaultshowname;
	}

	public UFBoolean getIf_revise() {
		return if_revise;
	}

	public void setIf_revise(UFBoolean if_revise) {
		this.if_revise = if_revise;
	}

	/**
	 * 生成表单域，生成string类型
	 * 
	 * @return
	 */
	public String genFormItem() {
		return buildFormItem().toString();
	}

	/**
	 * 生成表单域，返回各种类型的对象
	 * 
	 * @return
	 */
	public Object buildFormItem() {
		// 对于隐藏域,可以使用Ext.form.Hidden或者属性hidden来表示,比较好的方法是使用hidden属性来表示,<br/>
		// 这样生成的表单域是不变的,对于程序来说有更好的扩展性,但是这里为了性能上面的考虑,全部使用隐藏域来表示<br/>
		// 毕竟渲染一个隐藏域肯定比TextField快很多
		Object item;
		if(this.getShowflag().intValue() == Constants.YES) {
			item = genShowItem();
		} else {
			item = genHiddenItem();
		}
		return item;
	}

	/**
	 * 返回Ext的form表单域 <br/>
	 * 对于隐藏域不再是xtype=hidden，而是改用hidden属性去控制
	 * 
	 * @return
	 */
	public Object genShowItem() {
		if(DATATYPE.INTEGER.equals(this.getDatatype())) {
			NumberField nf = new NumberField();
			nf.setDecimalPrecision(0);
			setItem(nf);
			return nf;
		} else if(DATATYPE.DECIMAL.equals(this.getDatatype())) {
			NumberField nf = new NumberField();
			nf.setDecimalPrecision(UiTempletUtils.getPrecision(this.getReftype()));
			setItem(nf);
			return nf;
		} else if(DATATYPE.PASSWORD.equals(this.getDatatype())) {
			PasswordField pf = new PasswordField();
			pf.setInitial(this.getIdcolname());// password的initial属性使用模板中的关键字设置
			setItem(pf);
			return pf;
		} else if(DATATYPE.SELECT.equals(this.getDatatype())) {
			if(this.getReftype() != null) {
				if(this.getReftype().startsWith(UiConstants.COMBOX_TYPE.CG.toString())) {
					// checkboxgroup
					return UIUtils.buildCheckboxGroup(this);
				} else if(this.getReftype().startsWith(UiConstants.COMBOX_TYPE.RG.toString())) {
					// radiogroup
					return UIUtils.buildRadioGroup(this);
				}
			}
			return UIUtils.buildCombox(this);
		} else if(DATATYPE.REF.equals(this.getDatatype())) {
			// 参照不需要设置默认值
			if(UIUtils.isMultiSelect(reftype, datatype)) {
				// 对于多选下拉框，与当选一样，返回同名对象
				return this.genMultiSelect(false);
			}
			return genRefItem(false);
		} else if(DATATYPE.CHECKBOX.equals(this.getDatatype())) {
			Checkbox checkbox = new Checkbox();
			setItem(checkbox);
			checkbox.setInputValue(this.getDefaultvalue());
			return checkbox;
		} else if(DATATYPE.TIME.equals(this.getDatatype())) {
			TimeField tf = new TimeField();
			setItem(tf);
			return tf;
		} else if(DATATYPE.BLOCK.equals(this.getDatatype())) {
			DisplayField df = new DisplayField();
			df.setItemCls(UiConstants.DISPLAY_FIELD_LABEL_STYLE);
			setItem(df);
			return df;
		} else if(DATATYPE.PHOTO.equals(this.getDatatype())) {
			// 图片
			ImageField df = new ImageField();
			setItem(df);
			return df;
		} else if(DATATYPE.IDENTITY.equals(this.getDatatype())) {
			// 身份证
			IdentityField df = new IdentityField();
			setItem(df);
			return df;
		} else if(DATATYPE.TEXTAREA.equals(this.getDatatype())) {
			TextArea ta = new TextArea();
			ta.setMaxLength(this.getInputlength()); // 设置最大长度
			if(StringUtils.isNotBlank(this.getReftype())) {
				// 格式如(100,50)
				try {
					String sHeight = this.getReftype().substring(this.getReftype().indexOf(",") + 1,
							this.getReftype().indexOf(")"));
					ta.setHeight(Integer.valueOf(sHeight));
				} catch(Exception e) {
				}
			}
			setItem(ta);
			return ta;
		} else {
			// 此处已包括大文本类型
			TextField item = new TextField();
			item.setMaxLength(this.getInputlength()); // 设置最大长度
			setItem(item);
			return item;
		}
	}

	/**
	 * 生成表单中的隐藏域
	 * 
	 * @return
	 */
	private Object genHiddenItem() {
		if(DATATYPE.SELECT.equals(this.getDatatype())) {
			// 对于下拉框，必须渲染出来，列表界面的下拉字段通常需要根据该下拉值返回一个render值
			return UIUtils.buildCombox(this);
		}
		HiddenField hf = new HiddenField();
		hf.setId(this.genUniqueId());
		hf.setName(this.getItemkey().trim());
		hf.setValue(this.getDefaultvalue());
		// hf.setPkBilltemplet(this.getPk_billtemplet());
		// hf.setPkBilltempletB(this.getPk_billtemplet_b());
		return hf;
	}

	/**
	 * 设置form表单域的基本属性
	 * 
	 * @param item
	 */
	private void setItem(Field item) {
		if(!this.isBody()) {
			// 对于表格的colmodel，使用自动创建的id，否则会出现id重复的问题。
			item.setId(this.genUniqueId());
		}
		item.setName(this.getItemkey().trim());
		item.setFieldLabel(this.getDefaultshowname());
		// FIXME 使用tableformlayout，不需要定义宽度
		// item.setWidth(this.getWidth());
		item.setColspan(this.getWidth());
		if(this.getEditflag().equals(Constants.NO)) {
			// item.setDisabled(true);
			item.setReadOnly(true);
		}
		item.setXtype(DataTypeConverter.getFieldType(this));
		if(item.getXtype().equals(UiConstants.FORM_XTYPE.TEXTAREA.toString())) {
			// 对于大文本输入框，模板设置的长度是1，但默认是占满整屏，这么默认设置为4
			item.setWidth(4);
			item.setColspan(4);
		}
		if(this.isBody()) {
			if(this.getListshowflag().booleanValue()) {
				item.setHidden(true);
			}
		} else {
			if(this.getShowflag().intValue() == Constants.NO) {
				// form隐藏域的xtype设置为hidden
				item.setHidden(true);
			}
		}
		if(item instanceof Field) {
			// 对于TextField需要设置以下变量
			Field f = (Field) item;
			if(this.getNullflag().equals(Constants.YES)) {
				f.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
			}
			if(this.getEditflag().equals(Constants.NO)) {
				f.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
			}
		}
		if(item instanceof TextField) {
			TextField tf = (TextField) item;
			if(this.getNullflag().equals(Constants.YES)) {
				tf.setAllowBlank(false);
			}
			tf.setVtype(this.getValidateformula());
		}
		if(this.getNewlineflag() != null && this.getNewlineflag().booleanValue()) { // 布局到下一行
			item.setNewlineflag(true);
		}
		// 设置默认值
		item.setValue(this.getDefaultvalue());
		item.setScript(this.getUserdefine1());// 前台执行的js脚本
		if(this.getReviseflag().booleanValue()) {
			item.setReviseflag(true);
		}
		if(StringUtils.isNotBlank(this.getEditformula())) {
			item.setHasEditformula(true);
			item.setPkBilltemplet(this.getPk_billtemplet());
			item.setPkBilltempletB(this.getPk_billtemplet_b());
		}
	}

	/**
	 * 设置参照的基本属性，参照依据不继承自Field了，具体原因参加BaseRefModel的说明
	 * 
	 * @param item
	 * @param isForListColumn
	 *            是否是表格Editor使用的
	 */
	private void setBaseRefModel(BaseRefModel item, boolean isForListColumn) {
		if(!isForListColumn) {
			// 对于表格的colmodel，使用自动创建的id，否则会出现id重复的问题。
			item.setId(this.genUniqueId());

			// 表体参照，不需要如下变量，减少io输出
			item.setName(this.getItemkey().trim());
			item.setFieldLabel(this.getDefaultshowname());
			item.setWidth(this.getWidth());
			item.setColspan(this.getWidth());

			// 是否布局到下一行
			if(this.getNewlineflag() != null && this.getNewlineflag().booleanValue()) { // 布局到下一行
				item.setNewlineflag(true);
			}
		}
		if(this.getEditflag().equals(Constants.NO)) {
			item.setReadOnly(true);
			item.setItemCls(UiConstants.LABEL_NOT_EDIT_STYLE);
		}
		item.setXtype(DataTypeConverter.getFieldType(this));
		if(this.isBody()) {
			// FIXME nc的表头参照在处理方式上与表体截然不同，需要特殊处理
			// 这里要区分自定义档案项，模板中不定义关键字，故使用表头的参照
			item.setXtype(UiConstants.FORM_XTYPE.BODYREFFIELD.toString());
			if(this.getListshowflag().booleanValue()) {
				item.setHidden(true);
			}
		} else {
			if(this.getShowflag().intValue() == Constants.NO.intValue()) {
				// form隐藏域的xtype设置为hidden
				item.setHidden(true);
			}
		}
		if(this.getNullflag().equals(Constants.YES)) {
			item.setAllowBlank(false);
			item.setItemCls(UiConstants.LABEL_NOT_NULL_STYLE);
		}
		item.setVtype(this.getValidateformula());
		// 设置默认值
		item.setValue(this.getDefaultvalue());
		item.setScript(this.getUserdefine1());// 前台执行的js脚本
		item.setBeforeEditScript(this.getUserdefine2()); // 当打开参照窗口前执行的脚本
		if(this.getReviseflag().booleanValue()) {
			item.setReviseflag(true);
		}

		if(StringUtils.isNotBlank(this.getEditformula())) {
			item.setHasEditformula(true);
			item.setPkBilltemplet(this.getPk_billtemplet());
			item.setPkBilltempletB(this.getPk_billtemplet_b());
		}
	}

	/**
	 * 生成表格的列模型
	 * 
	 * @return
	 */
	public ListColumn buildListColumn() {
		ListColumn column = new ListColumn();
		column.setDataIndex(this.getItemkey().trim());
		if(this.getEditflag().equals(Constants.NO)) {
			column.setEditable(false);
		}
		if(this.getLockflag().equals(Constants.YES)) {// 默认一般是为false，当为false时，前台不会序列化，减少输出
			column.setLockflag(true);
		}
		if(this.getReviseflag().booleanValue()) {
			column.setReviseflag(true);
		}
		if(this.getTotalflag().equals(Constants.YES)) {
			column.setSummaryType(UiConstants.SUMMARY_TYPE.SUM.toString());
		}

		// 当是数字类型时，让header居中，使用align属性会让表头也居右
		if(DATATYPE.INTEGER.equals(this.getDatatype()) || DATATYPE.DECIMAL.equals(this.getDatatype())) {
			this.setDefaultshowname("<center>" + this.getDefaultshowname() + "</center>");
		}
		if(this.getNullflag().equals(Constants.YES)) {
			// 当列不能为空时，列的字体需要改变颜色
			column.setHeader("<span class='uft-grid-header-column-not-null'>" + this.getDefaultshowname() + "</span>");
		} else if(this.isBody() && this.getEditflag().equals(Constants.NO)) {
			column.setHeader("<span class='uft-grid-header-column-not-edit'>" + this.getDefaultshowname() + "</span>");
		} else {
			column.setHeader("<span class='uft-grid-header-column'>" + this.getDefaultshowname() + "</span>");
		}
		if(this.isBody()) {
			if(this.getShowflag().equals(Constants.NO)) {
				// FIXME 2011-10-20 列表中以"卡片是否显示"为准,忽略"列表是否显示"
				column.setHidden(true);
			}
			column.setWidth(this.getWidth() + UiConstants.BODYGRID_DEFAULT_WIDTH_SUIT);
		} else {
			if(!this.getListshowflag().booleanValue()) {
				column.setHidden(true);
			}
			// 表头顶列表页的列宽优先使用“元数据属性”字段
			Integer headerColumnWith = this.getWidth() * UiConstants.DEFAULT_WIDTH_STEP;
			try {
				if(this.getMetadataproperty() != null) {
					headerColumnWith = Integer.parseInt(this.getMetadataproperty().trim());
				}
			} catch(Exception e) {

			}
			column.setWidth(headerColumnWith);
		}
		setListColumnValue(column);
		if(StringUtils.isNotBlank(this.getUserdefine1())) {
			column.setScript(this.getUserdefine1());// 用于监听grid的afteredit事件
		}
		if(StringUtils.isNotBlank(this.getUserdefine2())) {
			column.setBeforeEditScript(this.getUserdefine2());// 用于监听grid的beforeedit事件
		}
		column.setRenderer(this.getRenderer()); // 设置渲染效果
		column.setBeforeRenderer(this.getBeforeRenderer());// 设置渲染函数的before函数，改函数在renderer中使用
		column.setSummaryRenderer(this.getSummaryRenderer());// 设置统计行的列渲染函数
		// FIXME 2013-5-19 模板的pk是为了执行模板编辑公式使用，如果没有编辑公式，一般不需要设置这2个值
		if(StringUtils.isNotBlank(this.getEditformula())) {
			column.setHasEditformula(true);
			column.setPkBilltemplet(this.getPk_billtemplet());
			column.setPkBilltempletB(this.getPk_billtemplet_b());
		}
		// FIXME 2013-4-11
		if(this.isBody() || COLUMN_XTYPE.SELECT.equals(column.getXtype())
				|| COLUMN_XTYPE.MULTISELECTCOLUMN.equals(column.getXtype())) {
			// 表体、或者select等类型的列需要有editor属性
		} else {
			column.setEditor(null);
		}
		if(column.getHidden() != null && column.getHidden().booleanValue()) {
			// 隐藏的列,这里不能使用不可编辑的列，有时候在前台需要动态设置该列是否可编辑。
			// FIXME 这里也要排除前台不能动态设置列的隐藏和显示
			column.setEditor(null);
		}
		// 设置字体颜色以及背景色
		if(StringUtils.isNotBlank(this.getResid())) {// 字体颜色
			column.setCss("color:" + this.getResid() + ";");
		}
		if(StringUtils.isNotBlank(this.getResid_tabname())) {// 背景颜色
			column.setCss("background-color:" + this.getResid_tabname() + ";");
		}
		return column;
	}

	/**
	 * 生成表格的列模型
	 * 
	 * @return
	 */
	public String genListColumn() {
		ListColumn column = buildListColumn();
		return column.toString();
	}

	/**
	 * 一些与datatype有关的字段，统一使用该方法设置值 包括xtype,editor,type
	 * 
	 * @param column
	 */
	private void setListColumnValue(ListColumn column) {
		// 是否能够为空
		boolean allowBlank = !this.getNullflag().equals(Constants.YES);
		String vtype = this.getValidateformula();
		String editor = new ColumnEditor(FORM_XTYPE.TEXTFIELD.toString(), vtype, allowBlank).toString();
		if(DATATYPE.INTEGER.equals(this.getDatatype())) {
			editor = new ColumnEditor(FORM_XTYPE.NUMBERFIELD.toString(), vtype, allowBlank).toString();
			editor = editor.substring(0, editor.length() - 1) + ",decimalPrecision:0}";
			column.setAlign(UiConstants.ALIGN.RIGHT.toString()); // 数字使用右对齐
		} else if(DATATYPE.DECIMAL.equals(this.getDatatype())) {
			int precision = UiTempletUtils.getPrecision(this.getReftype());
			editor = new ColumnEditor(FORM_XTYPE.NUMBERFIELD.toString(), vtype, allowBlank).toString();
			editor = editor.substring(0, editor.length() - 1) + ",decimalPrecision:" + precision + "}";
			column.setAlign(UiConstants.ALIGN.RIGHT.toString()); // 数字使用右对齐
			column.setXtype(UiConstants.COLUMN_XTYPE.NUMBERCOLUMN.toString());
			column.setFormat(UiTempletUtils.getNumberFormat(this.getReftype()));
		} else if(DATATYPE.SELECT.equals(this.getDatatype())) {
			if(this.getReftype() != null && this.getReftype().startsWith(UiConstants.COMBOX_TYPE.CG.toString())) {
				// checkboxgroup
				CheckboxGroup cg = UIUtils.buildCheckboxGroup(this);
				cg.setId(null);
				cg.setReadOnly(null);
				editor = cg.toString();
			} else if(this.getReftype() != null && this.getReftype().startsWith(UiConstants.COMBOX_TYPE.RG.toString())) {
				// radiogroup
				RadioGroup rg = UIUtils.buildRadioGroup(this);
				rg.setId(null);
				rg.setReadOnly(null);
				editor = rg.toString();
			} else {
				// 不能直接返回同名的对象，可编辑表格的可编辑对象与表单域的对象不能是同一个，会出现id冲突的问题。
				// 可参考参照中对于可编辑表格不设置id。
				// editor = this.genUniqueId();// 下拉时候直接返回同名select javascript对象
				// XXX 2012-09-27,下拉框的生成不再使用TransCombox，改成LocalCombox，效率更快，更简洁
				Combox combox = UIUtils.buildCombox(this);
				// 对于列表页的editor对象，不要设置id属性，否则会跟卡片页的对象冲突
				combox.setId(null);
				combox.setReadOnly(null);
				editor = combox.toString();
				column.setXtype(COLUMN_XTYPE.SELECT.toString());
			}
		} else if(DATATYPE.OBJECT.equals(this.getDatatype())) {
			ColumnEditor oEditor = new ColumnEditor(this.getReftype(), vtype, allowBlank);
			if(FORM_XTYPE.MONTHPICKER.toString().equalsIgnoreCase(this.getReftype())) {
				// 若是 monthpicker类型，则使用monthcolumn
				column.setXtype(COLUMN_XTYPE.MONTHCOLUMN.toString());
			} else if(FORM_XTYPE.PERCENTFIELD.toString().equalsIgnoreCase(this.getReftype())) {
				// 若是 percentfield类型，则使用percentcolumn
				column.setXtype(COLUMN_XTYPE.PERCENTCOLUMN.toString());
			} else if(FORM_XTYPE.FORMULAFIELD.toString().equalsIgnoreCase(this.getReftype())) {
				oEditor.setMaxLength(2000);
			}
			editor = oEditor.toString();
		} else if(DATATYPE.PASSWORD.equals(this.getDatatype())) {
			editor = new ColumnEditor(FORM_XTYPE.PASSWORD.toString(), vtype, allowBlank).toString();
			editor = editor.substring(0, editor.length() - 1) + ",initial:" + this.getIdcolname() + "}";
		} else if(DATATYPE.REF.equals(this.getDatatype()) || DATATYPE.USERDEFINE.equals(this.getDatatype())) {
			// 参照或者自定义档案
			editor = this.genRefItem(true).toString();
			column.setXtype(COLUMN_XTYPE.REF.toString());
			if(UIUtils.isMultiSelect(this.getReftype(), this.getDatatype())) {
				// 如果是多选类型
				column.setXtype(COLUMN_XTYPE.MULTISELECTCOLUMN.toString());
			}
		} else if(DATATYPE.TEXTAREA.equals(this.getDatatype())) {
			ColumnEditor cEditor = new ColumnEditor(UiConstants.BIGTEXT_XTYPE, vtype, allowBlank);
			cEditor.setMaxLength(this.getInputlength());
			editor = cEditor.toString();
		} else if(DATATYPE.CHECKBOX.equals(this.getDatatype())) {
			column.setXtype(COLUMN_XTYPE.CHECKBOX.toString());
			editor = new ColumnEditor(FORM_XTYPE.UFTCHECKBOX.toString(), allowBlank).toString(); // checkbox暂时不需要定义vtype
		} else if(DATATYPE.DATE.equals(this.getDatatype())) {
			column.setXtype(COLUMN_XTYPE.DATE.toString());
			editor = new ColumnEditor(FORM_XTYPE.UFTDATEFIELD.toString(), vtype, allowBlank).toString();
		} else if(DATATYPE.TIME.equals(this.getDatatype())) {
			editor = new ColumnEditor(FORM_XTYPE.TIMEFIELD.toString(), vtype, allowBlank).toString();
			editor = editor.substring(0, editor.length() - 1) + ",format:'" + TimeField.FORMAT_24HOUR + "'}";
		} else if(DATATYPE.TIMESTAMP.equals(this.getDatatype())) {
			column.setXtype(COLUMN_XTYPE.DATETIME.toString());
			editor = new ColumnEditor(FORM_XTYPE.DATETIMEFIELD.toString(), vtype, allowBlank).toString();
			editor = editor.substring(0, editor.length() - 1) + "}";
		} else if(DATATYPE.TEXT.equals(this.getDatatype())) {
			ColumnEditor cEditor = new ColumnEditor(FORM_XTYPE.TEXTFIELD.toString(), vtype, allowBlank);
			cEditor.setMaxLength(this.getInputlength()); //
			// 针对TextField设置最大的输入长度
			editor = cEditor.toString();
		}
		column.setEditor(editor);
	}

	/**
	 * 生成Ext grid中的recordType对象
	 * 
	 * @return
	 */
	public RecordType buildRecordType() {
		RecordType record = new RecordType();
		record.setName(this.getItemkey().trim());
		record.setType(UIUtils.getColumnType(this.getDatatype()));
		if(StringUtils.isNotBlank(this.getIdcolname())) {
			record.setSortName(this.getIdcolname());
		}
		// TODO，如果多增加一个自定义参照类型，这里可能需要斟酌下，到底能否设置默认值
		// 参照不需要设置默认值,注意有些自定义参照可能需要设置默认值
		// 多选属于参照的一种，它需要设置默认值
		// FIXME 2012/2/8表格的参照一个是不会设置默认值的
		// if(UIUtils.isMultiSelect(this.getReftype(), this.getDatatype())) {
		// record.setValue(this.getDefaultvalue());
		// }
		// if(!DATATYPE.REF.equals(this.getDatatype())) {
		// record.setValue(this.getDefaultvalue());
		// }
		record.setValue(this.getDefaultvalue());
		return record;
	}

	/**
	 * 生成Ext grid中的recordType对象
	 * 
	 * @return
	 */
	public String genRecordType() {
		return buildRecordType().toString();
	}

	/**
	 * 生成参照类型的数据
	 * 
	 * @param isForListColumn
	 *            是否是表格editor使用的
	 * @return
	 */
	public Object genRefItem(boolean isForListColumn) {
		String refclass = DataTypeConverter.getRefClazz(reftype, datatype);
		try {
			BaseRefModel refModel = null;
			Class<?> clazz = Class.forName(refclass);
			Object obj = clazz.newInstance();
			if(obj instanceof IMultiSelect) {
				// 如果是多选类型
				return this.genMultiSelect(isForListColumn);
			}
			refModel = (BaseRefModel) obj;
			if(isBody() && StringUtils.isBlank(this.getIdcolname())) {
				log.warn("表体参照字段[" + this.getDefaultshowname() + "] 没有设置关键字！");
			}
			refModel.setIdcolname(this.getIdcolname());

			if(StringUtils.isBlank(refModel.getRefName())) {
				// 异常已经在方法里面转化了
				refModel.setRefName(DataTypeConverter.getRefName(reftype, datatype));
			}
			// 将参照名称作为参数，有些业务可能需要根据该参照名称进行判断
			refModel.getRefWindow().addParam("refName:'" + refModel.getRefName() + "'");

			refModel.setShowCodeOnBlur(this.getPos().intValue() == 1); // 表体默认都是显示code

			// 是否只能选择末级节点
			// 2011-07-20,是否选择末级节点貌似应该在参照里面设置。
			refModel.getRefWindow().setLeafflag(this.getLeafflag().booleanValue());

			if(reftype.indexOf(",") > -1) {
				// dp=N,seal=Y,code=Y,nl=N
				String[] arr = reftype.split(",");
				for(int i = 1; i < arr.length; i++) {// 这里i从第二个开始算，第一个是参照类型
					String[] keyValueMap = arr[i].split("=");
					if(REF_CONFIG.CODE.toString().equalsIgnoreCase(keyValueMap[0])) {// 鼠标移开时是否显示code
						refModel.setShowCodeOnBlur((UFBoolean.valueOf(keyValueMap[1])).booleanValue());
					} else if(REF_CONFIG.NL.toString().equalsIgnoreCase(keyValueMap[0])) {// 树形参照是否可以选择非叶子节点
						refModel.getRefWindow().setLeafflag(!(UFBoolean.valueOf(keyValueMap[1])).booleanValue());
					}
				}
			}
			// 这里不再使用setParams方法，该方法会将原有的params清空
			if(StringUtils.isNotBlank(this.getUserdefine3())) {
				refModel.getRefWindow().addParam(this.getUserdefine3()); // 在模板中定义的参数
			}

			setBaseRefModel(refModel, isForListColumn);
			return refModel;
		} catch(Exception e) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("字段：" + this.getDefaultshowname() + ",参照类型：" + reftype + ",参照类：" + refclass
						+ "详细信息：" + e.getMessage());
			}else if( WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Field: "+ this.getDefaultshowname() +",Reference type:" + reftype + ",Reference class：" + refclass
						+ "Information：" + e.getMessage());
			}
			throw new RuntimeException("字段：" + this.getDefaultshowname() + ",参照类型：" + reftype + ",参照类：" + refclass
					+ "详细信息：" + e.getMessage());
		}
	}

	/**
	 * 生成多选下拉框
	 * 
	 * @return
	 * @author xuqc
	 * @date 2011-12-30
	 */
	public MultiSelectField genMultiSelect(boolean isForListColumn) {
		// 多选
		Object obj = UIUtils.getReftypeObj(this.getReftype(), this.getDatatype());
		IMultiSelect multiSelect = (IMultiSelect) obj;
		MultiSelectField m_item = UIUtils.buildMultiSelectField(this, multiSelect.getConsult_code());
		if(!isForListColumn) {
			this.setItem(m_item);
		} else {
			m_item.setId(null);// 如果是表格的editor属性，不要设置id，否则会和form中的重复
			m_item.setName(null);
		}
		m_item.setXtype(UiConstants.FORM_XTYPE.MULTISELECTFIELD.toString()); // xtype在setItem方法中会被覆盖
		return m_item;
	}

	/**
	 * 是否是可编辑表格
	 * 
	 * @return
	 */
	private boolean isBody() {
		return this.getPos().intValue() == UiConstants.POS[1];
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public Boolean getZeroToNull() {
		return zeroToNull;
	}

	public void setZeroToNull(Boolean zeroToNull) {
		this.zeroToNull = zeroToNull;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
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

	@Override
	public String getParentPKFieldName() {
		return "pk_billtemplet";
	}

	@Override
	public String getPKFieldName() {
		return "pk_billtemplet_b";
	}

	@Override
	public String getTableName() {
		return "nw_billtemplet_b";
	}

	@Override
	public Object clone() {
		Object o = null;
		o = super.clone();
		return o;
	}
}
