Ext.ns('uft.tp');
uft.tp.DelegateCarrier = function(config){
	Ext.apply(this,config);
	if(!this.ids || this.ids.length == 0){
		uft.Utils.showErrorMsg('请选择运段！');
		return false;
	}
	var pk_carrier = "";
	if(this.carrier){
		pk_carrier = this.carrier;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 80,
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
				items : [{"refName":"承运商档案-web",
					   "xtype":"headerreffield",
					   "id":"carrier",
					   "name":"carrier",
					   "fieldLabel":"承运商",
					   "value":pk_carrier,
					   "colspan":1,
					   "refWindow":{"model":1,"leafflag":false,"params":"refName:'承运商档案-web'",
					   				"gridDataUrl":ctxPath + "/ref/common/carr/load4Grid.json",
					   				"extGridColumnDescns":[
					   					{"type":"string","width":120,"dataIndex":"pk_carrier","sortable":true,"xtype":"gridcolumn","hidden":true},
					   					{"header":"承运商编码","type":"string","width":120,"dataIndex":"carr_code","xtype":"gridcolumn"},
					   					{"header":"承运商名称","type":"string","width":120,"dataIndex":"carr_name","xtype":"gridcolumn"},
					   					{"header":"承运商类型","type":"string","width":120,"dataIndex":"carr_type","xtype":"gridcolumn"},
					   					{"header":"备注",    "type":"string","width":120,"dataIndex":"memo",     "xtype":"gridcolumn"}]},
					   				"pkField":"pk_carrier","codeField":"carr_code","nameField":"carr_name",
					   				"showCodeOnBlur":false,
					   				"getByPkUrl":ctxPath + "/ref/common/carr/getByPk.do",
					   				"getByCodeUrl":ctxPath + "/ref/common/carr/getByCode.do"
					},{
						name : 'if_email',
						xtype : 'uftcheckbox',
						fieldLabel : '发送邮件',
						colspan : 1
					}]
			}]
	});
	uft.tp.DelegateCarrier.superclass.constructor.call(this, {
		title : '委派承运商',
		width : 500,
		height : 150,
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
			text : '保&nbsp;&nbsp;存',
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
Ext.extend(uft.tp.DelegateCarrier,Ext.Window, {
	saveAction : function(){
		var ids = this.ids;
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}	
		var values = form.getFieldValues(false);
		var params = this.app.newAjaxParams();
		Ext.apply(values,{ids:ids});
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	isTip : false,
	    	url : 'delegateCarrier.json',
	    	success : function(values){
	    		this.app.setHeaderValues(this.records,values.datas);
	    		if(values.append){
	    			uft.Utils.showWarnMsg(values.append);
	    		}
	    		this.close();
	    	}
	    });	
	}
});