Ext.ns('uft.te');
/**
 * yaojiie 2015 12 27
 *退单
 * @param {} config
 */
uft.te.Vent = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要退单的记录！');
		return false;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 60,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:2},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield'
				},      
				items : [{
					name : 'vent_type',
					fieldLabel : '退单原因',
					value : '',
					xtype : 'uftcombo',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					dataUrl : 'getVentReasonTypeList.json',
					colspan : 1
				},{
					name : 'vent_memo',
					fieldLabel : '退单说明',
					colspan : 2
				}]
			}]
	});
	uft.te.Vent.superclass.constructor.call(this, {
		title : '退单确认',
		width : 500,
		height : 300,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		items : [this.formPanel],
		buttons : [{
			iconCls : 'btnYes',
			text : '确&nbsp;&nbsp;认',
			actiontype : 'submit',
			scope : this,
			handler : this.saveAction
		},{
			iconCls : 'btnCancel',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}]
    });	
};
Ext.extend(uft.te.Vent,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length==0) {
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		if(ids.length > 0){
				var params=this.app.newAjaxParams();
				var values = form.getFieldValues(false);
				params[this.app.getBillIdField()]=ids;
				params[uft.jf.Constants.HEADER] = Ext.encode(values);
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	isTip : true,
			    	url : 'vent.json',
			    	success : function(values){
			    		this.app.setHeaderValues(this.record,values.datas);
			    		if(values.datas&&values.datas.length>0){
			    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
			    		}
			    		this.app.statusMgr.updateStatus();
			    		if(values.append){
			    			uft.Utils.showWarnMsg(values.append);
			    		}
			    		this.destroy();
			    		}
			    	});
			}
		}else{
		uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
		return;
	}
}
});
