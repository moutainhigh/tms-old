Ext.ns('uft.te');
/**
 * 录入异常事故的窗口
 * @param {} config
 */
uft.te.ExpAccident = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选中一条异常记录！');
		return false;
	}
	var invoice_vbillno = this.record.get('vbillno');//发货单
	var pk_customer = this.record.get('pk_customer');//客户
	var entrust_vbillno = this.record.get('pod_entrust_vbillno');//委托单
	var pk_carrier = this.record.get('pod_carrier');//承运商
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 60,
		border : true,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:2},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield'
				},      
				items : [
			   {"id":"vbillno","name":"vbillno","fieldLabel":"单据号","xtype":"textfield","colspan":1,"maxLength":50}
		   ,
			   {"id":"vbillstatus","name":"vbillstatus","fieldLabel":"状态","itemCls":"uft-form-label-not-edit",value : 0,"xtype":"localcombo","readOnly":true,"colspan":1,"hidden":false,"maxLength":200,"store":{"fields":["text","value"],"data":[["&nbsp;",""],["新建","0"],["待处理","71"],["处理中","72"],["已处理","73"],["已关闭","74"]],"xtype":"arraystore"}}
		   ,	
		   {value:invoice_vbillno,"fieldLabel":"发货单号","readOnly":true,xtype:'textfield',id:'invoice_vbillno',name:'invoice_vbillno'}
//		   	   {value:invoice_vbillno,"readOnly":true,"refName":"发货单-web","xtype":"headerreffield","id":"invoice_vbillno","name":"invoice_vbillno","fieldLabel":"发货单号","itemCls":"uft-form-label-not-null","width":1,"colspan":1,"refWindow":{"model":1,"leafflag":false,"params":"refName:'发货单-web'","gridDataUrl":ctxPath+"/ref/common/inv/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn","hidden":true},{"header":"发货单号","type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn"},{"header":"客户订单号","type":"string","width":120,"dataIndex":"cust_orderno","xtype":"gridcolumn"},{"header":"订单号","type":"string","width":120,"dataIndex":"orderno","xtype":"gridcolumn"},{"header":"客户","type":"string","width":120,"dataIndex":"customer_name","xtype":"gridcolumn"},{"header":"结算客户","type":"string","width":120,"dataIndex":"bala_customer_name","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"pk_customer","xtype":"gridcolumn","hidden":true},{"type":"string","width":120,"dataIndex":"bala_customer","xtype":"gridcolumn","hidden":true}]},"pkField":"vbillno","codeField":"vbillno","nameField":"vbillno","fillinable":true,"showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/inv/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/inv/getByCode.do"}
		   ,
		   	   {value:pk_customer,"readOnly":true,"refName":"客户档案-web","xtype":"headerreffield","id":"pk_customer","name":"pk_customer","fieldLabel":"客户","width":1,"colspan":1,"editFormulaUrl":"execFormula.json","refWindow":{"model":1,"leafflag":false,"params":"refName:'客户档案-web'","gridDataUrl":ctxPath+"/ref/common/cust/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_customer","xtype":"gridcolumn","hidden":true},{"header":"客户编码","type":"string","width":120,"dataIndex":"cust_code","xtype":"gridcolumn"},{"header":"客户名称","type":"string","width":120,"dataIndex":"cust_name","xtype":"gridcolumn"},{"header":"客户类型","type":"string","width":120,"dataIndex":"cust_type_name","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"cust_type","xtype":"gridcolumn","hidden":true}]},"pkField":"pk_customer","codeField":"cust_code","nameField":"cust_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/cust/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/cust/getByCode.do"}
//		   ,
//			   {value:entrust_vbillno,"readOnly":true,"refName":"委托单-web","xtype":"headerreffield","id":"entrust_vbillno","name":"entrust_vbillno","fieldLabel":"委托单号","itemCls":"uft-form-label-not-null","width":1,"colspan":1,"refWindow":{"model":1,"leafflag":false,"params":"refName:'委托单-web'","gridDataUrl":ctxPath+"/ref/common/ent/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn","hidden":true},{"header":"委托单号","type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn"},{"header":"客户订单号","type":"string","width":120,"dataIndex":"cust_orderno","xtype":"gridcolumn"},{"header":"订单号","type":"string","width":120,"dataIndex":"orderno","xtype":"gridcolumn"},{"header":"承运商","type":"string","width":120,"dataIndex":"carrier_name","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"pk_carrier","xtype":"gridcolumn","hidden":true}]},"pkField":"vbillno","codeField":"vbillno","nameField":"vbillno","fillinable":true,"showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/ent/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/ent/getByCode.do"}
//		   ,
		   ,{value:entrust_vbillno,"fieldLabel":"委托单号","readOnly":true,xtype:'textfield',id:'entrust_vbillno',name:'entrust_vbillno'}
			,{value:pk_carrier,"readOnly":true,"refName":"承运商档案-web","xtype":"headerreffield","id":"pk_carrier","name":"pk_carrier","fieldLabel":"承运商","width":1,"colspan":1,"editFormulaUrl":"execFormula.json","refWindow":{"model":1,"leafflag":false,"params":"refName:'承运商档案-web'","gridDataUrl":ctxPath+"/ref/common/carr/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_carrier","xtype":"gridcolumn","hidden":true},{"header":"承运商编码","type":"string","width":120,"dataIndex":"carr_code","xtype":"gridcolumn"},{"header":"承运商名称","type":"string","width":120,"dataIndex":"carr_name","xtype":"gridcolumn"},{"header":"承运商类型","type":"string","width":120,"dataIndex":"carr_type","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"pk_carrier","codeField":"carr_code","nameField":"carr_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/carr/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/carr/getByCode.do"}
		   ,
			   {"name":"type",
				   "fieldLabel":"类型",
				   "xtype":"multiselectfield",
				   "colspan":1,
				   "allowBlank":false,
				   layzyLoad : false,
				   value : '3',
				   dataUrl : ctxPath + '/datadict/getDataDict4MultiSelect.json',
				   baseParams : {datatype_code:'exp_accident_type'}
				},
			   {"id":"memo","name":"memo","fieldLabel":"描述","xtype":"textfield","colspan":2,"maxLength":200}
		   ,
			   {"id":"fb_user","name":"fb_user","fieldLabel":"反馈人","xtype":"textfield","colspan":1,"maxLength":20}
		   ,
			   {"id":"fb_date","name":"fb_date","fieldLabel":"反馈日期","xtype":"uftdatefield","colspan":1,"maxLength":20}
		   ,
			   {"id":"occur_date","name":"occur_date","fieldLabel":"发生日期","xtype":"uftdatefield","colspan":1,"maxLength":10}
		   ,
			   {"id":"occur_addr","name":"occur_addr","fieldLabel":"发生地点","xtype":"textfield","colspan":1,"maxLength":200}
				]
			}]
	});
//	this.formPanel.getForm().items.each(function(field){
//		field.addListener('change',function(field,value,originalValue){
//			if(field.id == 'invoice_vbillno'){
//				//发货单，带出客户
//				var values = Utils.doSyncRequest(ctxPath+'/te/ea/getCustomerByInvoice_vbillno.json',{invoice_vbillno:value},'POST');
//				if(values && values.data){
//					Ext.getCmp('pk_customer').setValue(values.data);
//				}
//			}else if(field.id == 'entrust_vbillno'){
//				//委托单，带出承运商
//				var values = Utils.doSyncRequest(ctxPath+'/te/ea/getCarrierByEntrust_vbillno.json',{entrust_vbillno:value},'POST');
//				if(values && values.data){
//					Ext.getCmp('pk_carrier').setValue(values.data);
//				}
//			}
//	})});
	uft.te.ExpAccident.superclass.constructor.call(this, {
		title : '登记异常事故',
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
Ext.extend(uft.te.ExpAccident,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		var pod_exp = this.record.get('pod_exp');
		if(pod_exp == 'true'){//是否是签收异常
			Ext.apply(values,{origin:'签收'});
		}else{
			Ext.apply(values,{origin:'回单'});
		}
		uft.Utils.doAjax({
	    	scope : this,
	    	params : values,
	    	url : ctxPath+'/te/ea/addExpAccident.json',
	    	success : function(values){
	    		this.destroy();
	    	}
	    });				
	}
});
