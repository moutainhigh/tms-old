package org.nw.constants;

import org.nw.Global;

/**
 * TMS常量
 */
public class Constants {

	public static final String DEFAULT_CHARSET = "UTF-8";
	/**
	 * 系统持有的特殊code，其他所有创建的code都不能使用该code
	 */
	public static final String SYSTEM_CODE = "0001";
	/**
	 * 用于多选下拉框的key
	 */
	public static final String VALUE = "value";
	public static final String TEXT = "text";
	/**
	 * 登录信息在session中的名字
	 */
	public static final String SESSION_NAME = "LOGIN_INFO";

	public static final String PAGINATIONVO = "paginationVO"; // 设置到jsp页面上的翻页数据

	/**
	 * 是否显示隐藏的默认查询条件
	 */
	public static final String SHOWDEFAULTLIST = "showDefaultList";

	public static final String HEADERLISTFIELDVOS = "headerListFieldVOs"; // 设置到jsp页面上的表头List显示的字段

	/**
	 * 查询窗口返回的查询参数，格式如：{PUB_PARAMS:{}}
	 */
	public static final String PUB_QUERY_PARAMETER = "PUB_PARAMS";

	/**
	 * 分页查询传入后台的起始记录参数名称
	 */
	public static final String OFFSET_PARAM = "PAGE_PARAM_START";

	/**
	 * 分页查询传入后台的每页记录参数名称
	 */
	public static final String PAGE_SIZE_PARAM = "PAGE_PARAM_LIMIT";

	/**
	 * 查询工具栏查询参数
	 */
	public static final String GRID_QUERY_FIELDS = "GRID_QUERY_FIELDS";

	/**
	 * 查询工具栏查询参数
	 */
	public static final String GRID_QUERY_FIELDS_TYPE = "GRID_QUERY_FIELDS_TYPE";

	/**
	 * 查询工具栏查询参数
	 */
	public static final String GRID_QUERY_KEYWORD = "GRID_QUERY_KEYWORD";

	/**
	 * 查询工具栏查询参数
	 */
	public static final String TREE_QUERY_KEYWORD = "TREE_QUERY_KEYWORD";

	/**
	 * 排序方向的参数名，如{dir:ASC}
	 */
	public static final String SORT_DIR_PARAM = "dir";

	/**
	 * 排序的列的参数名，如{sort:title}
	 */
	public static final String SORT_FIELD_PARAM = "sort";

	/**
	 * 主从表提交的数据参数名
	 */
	public static final String APP_POST_DATA = "APP_POST_DATA";

	public static final String HEADER = "HEADER";

	public static final String BODY = "BODY";

	// 发送主从表数据时，用来标记更新的数据参数
	public static final String UPDATE = "update";
	// 发送主从表数据时，用来标记删除的数据参数
	public static final String DELETE = "delete";
	/**
	 * 当offset等于-1时，不进行分页
	 */
	public static final Integer DEFAULT_OFFSET_WITH_NOPAGING = -1;

	/**
	 * 当pageSize等于-1时，不进行分页
	 */
	public static final Integer DEFAULT_PAGESIZE_WITH_NOPAGING = -1;

	/**
	 * 分页查询默认的开始记录
	 */
	public static final Integer DEFAULT_OFFSET = 0;

	/**
	 * 分页查询默认的每页记录数
	 */
	public static final Integer DEFAULT_PAGESIZE = 10;

	public static final String ORDER_DESC = "desc";
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_BY = "order by";

	/**
	 * 可用
	 */
	public static final Integer ABLED = 1;

	/**
	 * 禁用
	 */
	public static final Integer DISABLED = 0;

	/**
	 * 是
	 */
	public static final Integer YES = 1;

	/**
	 * 否
	 */
	public static final Integer NO = 0;

	public static final String Y = "Y";

	public static final String N = "N";

	public static final String ON = "on";

	public static final String OFF = "off";

	public static final String TRUE = "true";

	public static final String FALSE = "false";

	/** 操作成功 */
	public static final String FRAME_OPERATE_SUCCESS = "操作成功！";

	/**
	 * 属性分割符
	 */
	public static final String SPLIT_ATTR = ";";

	public static final String SPLIT_KEY_VALUE = "=";

	public static final String TEMPLETVO = "templetVO"; // 设置到jsp页面上的模板VO
	public static final String MODULENAME = "moduleName"; // 设置到jsp页面上的module

	// 因为要区别nc和web这边的模板数据,以后安装系统的时候需要导出这些数据,所以web这边的模板编码都要求使用ts作为前缀
	// 2013-7-2 现在已经没有nc的模板了。不需要使用这个做前缀来区分了
	@Deprecated
	public static final String TEMPLET_CODE_PREFIX = ""; // web模板的前缀，分配模板时，系统只读取ts前缀的模板

	/**
	 * 生成的开始日期查询域的id和name的前缀
	 */
	public static final String START_DATE_FIELD_PREFIX = "_s_";

	/**
	 * 生成的结束日期查询域的id和name的前缀
	 */
	public static final String END_DATE_FIELD_PREFIX = "_e_";
	// 多条数据使用这个符号隔开，目前使用在生成配载中
	public static final String SPLIT_CHAR = ",";

	// 执行时间大于该值的sql将被打印出来
	public static final long printSqlTime = Global.getIntValue("print.sql.time");

	// 平台级参数的type值，平台级参数不能删除，只能通过插入数据库增加或删除
	public static final int PLATFORM_PARAM = 0;
	// 产品级参数的type值，界面上操作的都是产品级参数
	public static final int PRODUCT_PARAM = 1;

	/**
	 * 用户类型,包括：管理员\内部用户\客户\承运商
	 * 
	 * @author xuqc
	 * 
	 */
	public enum USER_TYPE {
		ADMIN(0), INTERNAL(1), CUSTOMER(2), CARRIER(3);

		private USER_TYPE(Integer value) {
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
	 * 公司级别,包括：SYSTEM\GENERAL
	 * 
	 * @author xuqc
	 * 
	 */
	public enum CORP_LEVEL {
		SYSTEM(0), GENERAL(1);

		private CORP_LEVEL(Integer value) {
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
	 * NW的系统临时文件目录，比如存放临时的上传文件
	 */
	public static final String TMPDIR = System.getProperty("java.io.tmpdir") + "NW";
}
