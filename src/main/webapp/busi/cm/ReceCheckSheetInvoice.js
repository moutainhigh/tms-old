Ext.ns('uft.cm');
/**
 * yaojiie	2015 12 30 应收对账开票界面
 * @param {} config
 */
uft.cm.ReceCheckSheetInvoice = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要开票的记录！');
		return false;
	}
	var values = Utils.doSyncRequest(ctxPath+'/cm/rcs/getReceCheckSheetCheckHead.json',{vbillno:this.record.data['vbillno']});
	var checkHead = values.checkHead;
	
	var DefaultCheckType = Utils.doSyncRequest(ctxPath+'/cm/rcs/getReceCheckSheetCheckType.json',{check_type:this.record.data['check_type']});
	var checkType = DefaultCheckType.checkType;
	
	var DefaultCheckCorp = Utils.doSyncRequest(ctxPath+'/cm/rcs/getReceCheckSheetCheckCorp.json',{check_corp:this.record.data['check_corp']});
	var checkCorp = DefaultCheckCorp.checkCorp;
	
	var DefaultTaxtCat = Utils.doSyncRequest(ctxPath+'/cm/rcs/getReceCheckSheetTaxtCat.json',{check_tax_cat:this.record.data['check_tax_cat']});
	var taxtCat = DefaultTaxtCat.taxtCat;
	
	var DefaultTaxtRate = Utils.doSyncRequest(ctxPath+'/cm/rcs/getReceCheckSheetCheckTaxtRate.json',{check_tax_rate:this.record.data['check_tax_rate']});
	var taxtRate = DefaultTaxtRate.taxtRate;
	
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
				items : [{
					name : 'vbillno',
					fieldLabel : '对账单号',
					itemCls:'uft-form-label-not-edit',
					value : this.record.data['vbillno'],
					readOnly : true,
					colspan : 1
				},{
					name : 'bala_customer_name',
					fieldLabel : '结算客户',
					itemCls:'uft-form-label-not-edit',
					readOnly : true,
					value : this.record.data['bala_customer_name'],
					colspan : 1
				},{
					name : 'cost_amount',
					fieldLabel : '总金额',
					itemCls:'uft-form-label-not-edit',
					xtype : 'uftnumberfield',
					readOnly : true,
					decimalPrecision : 2,
					value : this.record.data['cost_amount'],
					colspan : 1
				},{
					name : 'got_amount',
					fieldLabel : '已收金额',
					itemCls:'uft-form-label-not-edit',
					xtype : 'uftnumberfield',
					readOnly : true,
					decimalPrecision : 2,
					value : this.record.data['got_amount'],
					colspan : 1
				},{
					name : 'ungot_amount',
					fieldLabel : '未收金额',
					itemCls:'uft-form-label-not-edit',
					xtype : 'uftnumberfield',
					readOnly : true,
					decimalPrecision : 2,
					value : this.record.data['ungot_amount'],
					colspan : 1
				},{
					name : 'check_amount',
					fieldLabel : '开票金额',
					xtype : 'uftnumberfield',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					decimalPrecision : 2,
					value : this.record.data['ungot_amount'],
					colspan : 1
				},{
					xtype : 'uftdatefield',
					name : 'check_date',
					fieldLabel : '开票日期',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					value : DateUtils.formatDate(new Date(),'yyyy-MM-dd'),
					colspan : 1
				},{
					name : 'check_no',
					fieldLabel : '发票号',
					itemCls:'uft-form-label-not-null',
					value : this.record.data['check_no'],
					colspan : 1
				},{
					name : 'check_head',
					fieldLabel : '发票抬头',
					itemCls:'uft-form-label-not-null',
					value : checkHead,
					colspan : 1
				},{
					name:'check_man',
					fieldLabel:'发票签收人',
					value : this.record.data['check_man'],
					colspan:1,
				},{
					xtype : 'uftcombo',
					name : 'check_tax_rate',
					fieldLabel : '税率',
					value : taxtRate,
					dataUrl : ctxPath+'/cm/rd/getTaxRate.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'check_tax_cat',
					fieldLabel : '税种',
					value : taxtCat,
					dataUrl : ctxPath+'/cm/rd/getTaxCat.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'check_corp',
					fieldLabel : '开票公司',
					itemCls:'uft-form-label-not-null',
					value : checkCorp,
					dataUrl : ctxPath+'/cm/rd/getCheckCorp.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'check_type',
					fieldLabel : '发票类型',
					//itemCls:'uft-form-label-not-null',
					value : checkType,
					dataUrl : ctxPath+'/cm/rd/getCheckType.json',
					colspan : 1
				},{
					name : 'check_remark',
					fieldLabel : '发票备注',
					value : this.record.data['check_remark'],
					colspan : 2
				}]
			}]
	});
	uft.cm.ReceCheckSheetInvoice.superclass.constructor.call(this, {
		title : '开票',
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
Ext.extend(uft.cm.ReceCheckSheetInvoice,Ext.Window, {
	saveAction : function(){
		var pk_rece_check_sheet = this.record.data['pk_rece_check_sheet'];
		var vbillno = this.record.data['vbillno'];
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length==0) {
			var values = form.getFieldValues(false);
			Ext.apply(values,{relationid:pk_rece_check_sheet,vbillno:vbillno});
			var params = this.app.newAjaxParams();
			params[uft.jf.Constants.HEADER] = Ext.encode(values);
			uft.Utils.doAjax({
		    	scope : this,
		    	params : params,
		    	isTip : false,
		    	url : 'receCheckSheetInvoice.json',
		    	success : function(values){
		    		if(values){//保存成功直接销毁窗口
		    			if(values.datas){
		    				this.app._setHeaderValues([this.record],values.datas);
		    				this.app.statusMgr.setBizStatus(values.datas[0][this.app.getBillStatusField()]);
		    				this.app.statusMgr.updateStatus();
		    			}
			    		this.destroy();
		    		}
		    	}
		    });	
		}else{
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}
	}
});
