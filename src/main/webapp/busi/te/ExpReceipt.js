Ext.ns('uft.te');
/**
 * 异常回单
 * @param {} config
 */
uft.te.ExpReceipt = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要回单的记录！');
		return false;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		region : 'north',
		height : 100,
		labelWidth : 80,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:3},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield'
				},      
				items : [{
					name : 'receipt_man',
					fieldLabel : '回单人',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					colspan : 1
				},{
					xtype : 'datetimefield',
					name : 'act_receipt_date',
					fieldLabel : '回单时间',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					value : DateUtils.formatDate(new Date(),'yyyy-MM-dd H:m:s'),
					colspan : 1
				},{
					 name:"receipt_exp_type",
					 fieldLabel:"回单异常类型",
					 allowBlank : false,
					 itemCls:'uft-form-label-not-null',
					 xtype:"multiselectfield",
					 dataUrl : ctxPath + '/datadict/getDataDict4MultiSelect.json',
					 baseParams : {datatype_code:'exp_accident_type'},
					 colspan:1
				},{
					name : 'receipt_memo',
					fieldLabel : '回单备注',
					colspan : 2
				}]
			}]
	});

	var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t501',tabCode:'ts_ent_pack_b'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode：t501,tabcode:ts_ent_pack_b！');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t501,tabcode:ts_ent_pack_b！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	params['isBody'] = 'true';
	params['pk_entrust'] = this.record.data['pk_entrust'];
	params['fromExp'] = true;
	this.grid = new uft.extend.grid.EditorGrid({
		region : 'center',
		id : 'ts_ent_pack_b_1',
		pkFieldName : 'pk_ent_pack_b',
		dataUrl:'loadData.json',
		immediatelyLoad : true,
		isCheckboxSelectionModel : false,
		isAddBbar : false,
		params : params,
		recordType : R,
		columns : C
	});
	this.grid.on('afteredit',function(e){
		this.checkAfterEditNum(e.record,e.field);
	},this);
	uft.te.ExpReceipt.superclass.constructor.call(this, {
		title : '异常回单',
		width : 800,
		height : 500,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'border',
		items : [this.formPanel,this.grid],
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
Ext.extend(uft.te.ExpReceipt,Ext.Window, {
	checkAfterEditNum:function(record,columnName){
		if(columnName == 'num'){
			var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var num = uft.Utils.getNumberColumnValue(record,'num');
			record.beginEdit();
			uft.Utils.setColumnValue(record,'weight',num*unit_weight);
			uft.Utils.setColumnValue(record,'volume',num*unit_volume);
			record.endEdit();
		}
	},
	getAppParams : function(){
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return null;
		}		
		var form = this.formPanel.getForm();
		if(form.isValid()){
			var appPostData =  {};
			values = form.getFieldValues(false);
			appPostData[uft.jf.Constants.HEADER] = values;
			var bodyGridData={};
			bodyGridData['ts_ent_pack_b'] = this.grid.getAllRecordValue();
			//加入子表数据
			appPostData[uft.jf.Constants.BODY]=bodyGridData;
			return appPostData;			
		}
		return null;
	},	
	saveAction : function(){
		var params=this.app.newAjaxParams();
		var appPostData = this.getAppParams();
		if(!appPostData){
			return;
		}
		params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
		params['pk_entrust'] = this.record.data['pk_entrust'];
		uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	url : 'expReceipt.json',
	    	success : function(values){
	    		if(values && values.data){//保存成功直接销毁窗口
    				this.app.setAppValues(values.data,{updateToHeaderGrid:true,saveToCache:true,updateBody:false});
    				if(typeof(this.app.getBillStatusField) == 'function'){
		    			this.app.statusMgr.setBizStatus(values.data.HEADER[this.app.getBillStatusField()]);
		    		}
	    			this.app.statusMgr.updateStatus();
	    		}
	    		this.destroy();
	    	}
	    });	
	}
});
