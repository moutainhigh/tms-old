Ext.namespace('uft.jf');
/**
 * 查看审批图片
 * params
 * billId,单据ID
 * funCode，节点编码
 * billType,单据类型
 * 
 * 对于首页这种类型,传入的参数是billId,funCode
 * 对于单据页面,传入的参数是billId,billType
 */
uft.jf.WorkflowViewer= function(config){
	this.shadow=false;
	Ext.apply(this, config);
	this.modal = (config.modal == undefined)?true:config.modal;
	this.width = (config.width == undefined)?600:config.width;
	this.height = (config.height == undefined)?400:config.height;
	
	if(!this.url){
		this.url = 'getApprovePic.do?billId='+this.billId+'&_dc='+(new Date()).getTime();
		if(this.funCode){
			this.url += "&funCode="+this.funCode;
		}
		if(this.billType){
			this.url += "&billType="+this.billType;
		}
	}
	var btmPanel = new Ext.Panel({
		layout : 'fit',
		autoScroll : true,
		height : 200,
		html:'<div style="position: absolute;vertical-align:middle"><img src="'+this.url+'" border=0/></div>'
	});
	
	var btn = [{
		xtype : 'button',
		text : '关&nbsp;&nbsp;闭',
		scope : this,
		handler : function() {
			this.destroy();
		}
	}];
	
	uft.jf.WorkflowViewer.superclass.constructor.call(this, {
		title : '流程图',
		width : this.width,
		height : this.height,
		collapsible : false,
		shim : true,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		maximizable : true,
		border : false,
		modal : this.modal,
		border : false,
		layout : 'fit',
		items : [btmPanel],
		buttons : [btn]
    });
};
Ext.extend(uft.jf.WorkflowViewer,Ext.Window, {
});