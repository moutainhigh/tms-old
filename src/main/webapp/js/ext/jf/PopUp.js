Ext.ns('uft.jf');
/**
 * 弹出框
 * @param {} config
 */
uft.jf.PopUp = function(config){
	Ext.apply(this,config);
	
	var popInfos = this.popInfos;
	var htmlStr = '<table id="popup_table">';
	for (var pk_portlet in popInfos){
			htmlStr += '<tr>'
			var popInfo = popInfos[pk_portlet];
			htmlStr += ('<td>'+popInfo+'</td>')
			htmlStr += ('<td>不再关注</td>')
			htmlStr += '</tr>'
	}
	htmlStr += '</table>';
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 60,
		border : false,
		html : htmlStr
	});
	
	
	
	
	
	uft.jf.PopUp.superclass.constructor.call(this, {
		title : '异常报警',
		x: Ext.getBody().getWidth() - 303,
        y: Ext.getBody().getHeight() - 305,
		width : 300,
		height : 300,
		collapsible : true,
		shadow : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : false,
		border : false,
		layout : 'fit',
		items : [this.formPanel]
    });	
};
Ext.extend(uft.jf.PopUp,Ext.Window, {
	saveAction : function(){
	}
});
