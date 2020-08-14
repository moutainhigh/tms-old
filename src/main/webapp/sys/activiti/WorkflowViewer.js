Ext.namespace('org.nw');
/**
 * 查看审批图片
 * params
 * deployment_id,流程部署id
 * 
 */
org.nw.WorkflowViewer= function(config){
	this.shadow=false;
	Ext.apply(this, config);
	uft.Utils.assert(this.deployment_id, "没有设置参数【deployment_id】！");
	this.modal = (config.modal == undefined)?true:config.modal;
	this.width = (config.width == undefined)?600:config.width;
	this.height = (config.height == undefined)?400:config.height;
	
	if(!this.url){
		this.url = 'preview.do?deployment_id='+this.deployment_id+'&_dc='+(new Date()).getTime();
	}
	var btmPanel = new Ext.Panel({
		layout : 'fit',
		autoScroll : true,
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
	
	org.nw.WorkflowViewer.superclass.constructor.call(this, {
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
Ext.extend(org.nw.WorkflowViewer,Ext.Window, {
});