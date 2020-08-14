Ext.namespace('uft.jf');
/**
 * jf框架中使用的常量
 * @type String
 */
uft.jf.Constants = {
	/**
	 * 当单据的主键不存在时，使用该变量作为key进行缓存
	 */
	PK_NEW : "pk_new",
	/**
	 * 作为整个新增返回对象的缓存key，这里属于比较特殊的地方，通常不需要缓存该内容。
	 * @type String
	 */
	PK_NEW_EXTRA : "pk_new_extra",
	APP_POST_DATA : "APP_POST_DATA",
	HEADER : "HEADER",
	BODY : "BODY",
	PROCESS_MSG : "加载中，请稍候...",
	UPLOADING_MSG : '上传中，请稍候...',
	PUB_PARAMS : 'PUB_PARAMS', //查询窗口的查询条件
	//ulw框架使用锚传递的动作
	ACTION : {NEW:'new',
			  COPY:'copy',
			  EDIT:'edit',
			  SHOW:'show',
			  APPROVEEDIT:'approveEdit',
			  REVISE:'revise',
			  REFBILL:'refbill'
			 },
	DATE_FORMAT : 'Y-m-d',//日期格式
	DATETIME_FORMAT : 'Y-m-d H:i:s' //日期时间格式
}
