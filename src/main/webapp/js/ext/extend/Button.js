Ext.ns('uft.extend');
/**
 * 扩展button
 * @class uft.extend.Toolbar.Button
 * @extends Ext.Toolbar.Button
 */
uft.extend.Button = Ext.extend(Ext.Button, {
	onstatuschange : null,    //页面状态改变时触发
	onbizstatuschange : null, //业务状态改变时触发
	enabledStatus:null,		  //按钮处于enable的页面状态
	enabledBizStatus:null,	  //按钮处于enable的业务状态
	visialbStatus : null,	  //按钮可见的状态
	
	keyBinding : null,		  //按钮绑定的快捷键配置
	showHotKey : false, 	  //是否在按钮后显示快捷键
	tooltipType : 'title'	  //默认使用title的提示信息
});

Ext.reg("uftbutton",uft.extend.Button);