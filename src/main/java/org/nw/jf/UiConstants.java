package org.nw.jf;

/**
 * UI模板的常量定义
 */
public class UiConstants {

	public final static String BIGTEXT_XTYPE = "bigtextfield"; // 该field只应用于grid中，form已经有textarea
	public final static Integer DEFAULT_WIDTH_STEP = 120;
	public final static Integer HEADERGRID_DEFAULT_WIDTH = DEFAULT_WIDTH_STEP * 2;
	public final static Integer BODYGRID_DEFAULT_WIDTH_SUIT = 0; // 表体的长度在NC的定义长度加上这个值
	// public final static Integer DEFAULT_ANCHOR=90;
	public final static int[] LABEL_WIDTH = { 50, 72, 72 };// label的长度，根据label的字符数分3个等级
	public final static Integer DEFAULT_LABEL_WIDTH = 50;
	public final static Integer DEFAULT_TABLE_COLUMNS = 4; // TABLELAYOUT的列数
	public final static String COLUMN_NOTNULL_TIPS = "该列为必输项";

	// 表单域非空时label的样式
	public static final String LABEL_NOT_NULL_STYLE = "uft-form-label-not-null";
	// 表单域不能编辑时label的样式
	public static final String LABEL_NOT_EDIT_STYLE = "uft-form-label-not-edit";
	// displayfield label的特殊样式
	public static final String DISPLAY_FIELD_LABEL_STYLE = "label-zwf";

	// 默认小数位数
	public static final int DEFAULT_PRECISION = 2;

	// 单据模板中定义的字段位置，包括0表头、1表体、2表尾
	public static final int[] POS = new int[] { 0, 1, 2 };

	// 查询模板的参照的返回值类型，code/name/pk
	public static final int[] RETURN_TYPE = new int[] { 0, 1, 2 };

	/**
	 * 获得焦点时是否全选
	 */
	public static final boolean SELECT_ON_FOCUS = true;

	/**
	 * 当等于该数值时，表示无效的，目前使用在TextField的maxLength,NumberField的maxValue等
	 */
	public static final Integer INVALID_LENGTH = -99;

	/**
	 * TextField中maxLength的默认数值
	 */
	public static final Integer DEFAULT_LENGTH = 200;

	/**
	 * NumberField中最大值的默认值，目前还没有启用
	 */
	public static final Integer DEFAULT_MAX_VALUE = 65533;

	/**
	 * 默认单据模板文件
	 */
	public static final String DEFAULT_BILL_TEMPLATE = "bill.ftl";

	/**
	 * 替换了换行符和tab符的单据模板文件
	 */
	public static final String DEFAULT_BILL_TEMPLATE_BIN = "bill-bin.ftl";

	/**
	 * 默认报表模板文件
	 */
	public static final String DEFAULT_REPORT_TEMPLATE = "report.ftl";

	/**
	 * 替换了换行符和tab符的报表模板文件
	 */
	public static final String DEFAULT_REPORT_TEMPLATE_BIN = "report-bin.ftl";

	/**
	 * 生成的Ext主键的默认命名空间
	 */
	public static final String DEFAULT_MODULE_NAME = "Y";

	/**
	 * Ext表格的类名，两个类都属于自扩展类
	 */
	public static final String[] EXT_GRID_CLASS = new String[] { "uft.extend.grid.BasicGrid",
			"uft.extend.grid.EditorGrid" };
	/**
	 * 单据默认的数据加载URL
	 */
	public static final String BILL_LOADDATA_URL = "loadData.json";

	/**
	 * 报表默认的数据加载URL
	 */
	public static final String REPORT_LOADDATA_URL = "loadData.json";

	/**
	 * 复制节点的参数名称（注册在功能节点的参数中）
	 * 
	 * @author fangw
	 */
	public static final String COPY_NODE_CONFIG_NAME = "copyNodeConfig";

	public static final String BETWEEN_OPERA_CODE = "between@";
	public static final String BETWEEN_OPERA_NAME = "介于@";

	/**
	 * 一个特殊的nodeKey，表示这个模板是用来卡片页查看的，此时不需要生成列表页
	 */
	public static final String NODEKEY_VIEW = "view";

	public enum ALIGN {
		LEFT("left"), CENTER("center"), RIGHT("right");
		private String value;

		private ALIGN(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * HTMLEDITOR没有被引用到，因为在类型设置中已经直接设置该变量了。这里定义这个变量是为了声明有支持
	 * 
	 * @author xuqc
	 * @date 2012-2-8
	 */
	public enum FORM_XTYPE {
		TEXTFIELD("textfield"), HIDDEN("ufthidden"), NUMBERFIELD("uftnumberfield"), UFTDATEFIELD("uftdatefield"), HEADERREFFIELD(
				"headerreffield"), BODYREFFIELD("bodyreffield"), COMBO("combo"), TIMEFIELD("timefield"), TEXTAREA(
				"textarea"), PASSWORD("password"), LOCALCOMBO("localcombo"), UFTCOMBO("uftcombo"), TRANCOMBO(
				"trancombo"), MULTISELECTFIELD("multiselectfield"), UFTCHECKBOX("uftcheckbox"), MONTHPICKER(
				"monthpicker"), DISPLAYFIELD("displayfield"), HTMLEDITOR("htmleditor"), IMAGEFIELD("imagefield"), DATETIMEFIELD(
				"datetimefield"), PERCENTFIELD("percentfield"), FORMULAFIELD("formulafield"), RADIO("radio"), CHECKBOXGROUP(
				"checkboxgroup"), RADIOGROUP("radiogroup"),IDENTITYFIELD("identityField"), ;
		private String value;

		private FORM_XTYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 模板中数据类型选择对象，类型设置中支持的类型
	 * 
	 * @author xuqc
	 * @date 2012-8-30
	 */
	public enum OBJECT_TYPE {
		HTMLEDITOR("htmleditor"), IMAGEFIELD("imagefield"), ATTACHFIELD("attachfield"), FORMULAFIELD("formulafield");
		private String value;

		private OBJECT_TYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 表格的column type checkcolumn在BasicGrid下有些缺陷(竟然可以编辑)，故加入uftcheckcolumn
	 * 
	 * @author xuqc
	 * @date 2010-9-20
	 * @version $Revision$
	 */
	public enum COLUMN_XTYPE {
		DEFAULT("gridcolumn"), SELECT("combocolumn"), CHECKBOX("checkcolumn"), REF("refcolumn"), DATE("datecolumn"), NUMBERCOLUMN(
				"numbercolumn"), MONTHCOLUMN("monthcolumn"), MULTISELECTCOLUMN("multiselectcolumn"), DATETIME(
				"datetimecolumn"), PERCENTCOLUMN("percentcolumn");
		private String value;

		private COLUMN_XTYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 数据类型,BLOCK表示占位块
	 * 
	 * @author xuqc
	 * @date 2010-9-20
	 * @version $Revision$
	 */
	public enum DATATYPE {
		TEXT(0), INTEGER(1), DECIMAL(2), DATE(3), CHECKBOX(4), REF(5), SELECT(6), USERDEFINE(7), TIME(8), TEXTAREA(9), PHOTO(
				10), OBJECT(11), BLOCK(12), PASSWORD(13), EMAIL(14), TIMESTAMP(15), CUSTOM(16),IDENTITY(17);//身份证作为特殊图片

		private DATATYPE(Integer value) {
			this.value = value;
		}

		public int intValue() {
			return this.value;
		}

		public boolean equals(Integer value) {
			if(this.value == value)
				return true;
			return false;
		}

		private int value;
	}

	/**
	 * 列的数据类型
	 * 
	 * @author xuqc
	 * @date 2010-11-23
	 */
	public enum COLUMN_TYPE {
		INT("int"), FLOAT("float"), STRING("string"), DATE("date");
		private String value;

		private COLUMN_TYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 模板类型
	 */
	public enum TPL_STYLE {
		// ITemplateStyle
		// * <li>单据模板 0
		// * <li>查询模板 1
		// * <li>报表模板 2
		// * <li>打印模板 3
		// * <li>卡片模板 4
		// * <li>数据模板 5
		BILL(0), QUERY(1), REPORT(2), PRINT(3), CARD(4), DATA(5);

		private TPL_STYLE(Integer value) {
			this.value = value;
		}

		public int intValue() {
			return this.value;
		}

			public String toString() {
			return String.valueOf(this.value);
		}

		public boolean equals(Integer value) {
			if(this.value == value)
				return true;
			return false;
		}

		private int value;
	};

	public enum TPL_STYLE_NAME {
		// ITemplateStyle
		// * <li>单据模板 0
		// * <li>查询模板 1
		// * <li>报表模板 2
		// * <li>打印模板 3
		// * <li>卡片模板 4
		// * <li>数据模板 5
		BILL("单据模板"), QUERY("查询模板"), REPORT("报表模板"), PRINT("打印模板"), CARD("卡片模板"), DATA("数据模板");

		private TPL_STYLE_NAME(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}

		private String value;
	};

	/**
	 * 参照支持的类型，包括树、表、左树右表、树表(表格中每一行都是一棵树)
	 */
	public enum REF_MODEL {
		TREE(0), GRID(1), TREEANDGRID(2), TREEGRID(3);

		private REF_MODEL(Integer value) {
			this.value = value;
		}

		public int intValue() {
			return this.value;
		}

			public String toString() {
			return String.valueOf(this.value);
		}

		public boolean equals(Integer value) {
			if(this.value == value)
				return true;
			return false;
		}

		private int value;
	};

	/**
	 * 表格的列的统计类型： 记录数 和 最大值 最小值 平均值
	 * 
	 * @author xuqc
	 * @date 2010-9-20
	 * @version $Revision$
	 */
	public enum SUMMARY_TYPE {
		COUNT("count"), SUM("sum"), MAX("max"), MIN("min"), AVERAGE("average");
		private String value;

		private SUMMARY_TYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 模板中定义的下拉的类型 --2014-11-29 NF标识这个是下拉框的值是数字类型的，在设置值的时候需要根据数值型比较
	 * 
	 * @author xuqc
	 * @date 2012-9-27
	 */
	public enum COMBOX_TYPE {
		P("P,"), I("I,"), S("S,"), SPXT("SPXT,"), SX("SX,"), IX("IX,"), IP("IP,"), SP("SP,"), IF("IF,"), SR("SR,"), 
		SF("SF,"), CG("CG,"), RG("RG,"), NF("NF");
		private String value;

		private COMBOX_TYPE(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}

	/**
	 * 模板中对参照的配置项
	 * 
	 * @author xuqc
	 * @date 2012-12-17
	 */
	public enum REF_CONFIG {
		DP("dp")/* 是否启用数据权限 */, CODE("code")/* 鼠标移开是否显示code */, SEAL("seal")/* 是否显示封存数据 */, NL("nl")/* 树参照是否只能选择叶子节点 */;
		private String value;

		private REF_CONFIG(String value) {
			this.value = value;
		}

		public boolean equals(String value) {
			return this.value.equals(value);
		}

			public String toString() {
			return this.value;
		}
	}
}
