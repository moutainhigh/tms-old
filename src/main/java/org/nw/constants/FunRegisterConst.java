package org.nw.constants;

/**
 * 功能节点属性常量
 * 
 */
public interface FunRegisterConst {
	/**
	 * 功能编码每级长度
	 */
	public final static int LEVELLEN = 2; //

	/**
	 * 可执行功能节点
	 */
	public final static int EXECUTABLE_FUNC_NODE = 0;

	/**
	 * 虚功能节点
	 */
	public final static int INEXECUTABLE_FUNC_NODE = 1;

	/**
	 * 待分配权限按钮
	 */
	public final static int POWERFUL_BUTTON = 2;

	/**
	 * 默认有权限按钮
	 */
	public final static int POWERLESS_BUTTON = 3;

	/**
	 * 参数
	 */
	public final static int PARAMETER = 4;

	/**
	 * 可执行功能帧
	 */
	public final static int EXECUTABLE_FUNC_FRAME = 5;

	/**
	 * 轻量级Web节点 leijun+2008-3-10
	 */
	public final static int LFW_FUNC_NODE = 6;
	/**
	 * 所有类型 ewei+2009-5-27
	 */
	public final static int ALL_FUNC_NODE = 99;

	// 资源压缩功能编码
	public final static String COMPRESSOR_FUN_CODE = "t013";
	// 站内信功能节点
	public final static String SMS_FUN_CODE = "t020";
	// 系统待办功能节点
	public final static String ALARM_FUN_CODE = "t036";

	// 通知公告功能节点
	public final static String TZGG_FUN_CODE = "t022";
	// 新闻中心功能节点
	public final static String NEWS_FUN_CODE = "t024";
	// 文档管理功能节点
	public final static String WDGL_FUN_CODE = "t03002";
}
