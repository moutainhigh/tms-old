Ext.ns('uft.cm');
/**
 * 创建应收对账单
 * @param {} config
 */
uft.cm.BuildReceCheckSheet = function(config){
	Ext.apply(this,config);
	if(!this.records){
		uft.Utils.showErrorMsg('请先选择要对账的记录！');
		return false;
	}
	var cost_amount=0;//总金额
	for(var i=0;i<this.records.length;i++){
		cost_amount+=uft.Utils.getNumberColumnValue(this.records[i],'cost_amount');
	}
	var values = Utils.doSyncRequest(ctxPath+'/cm/rd/getCheckHead.json',{bala_customer:this.records[0].data['bala_customer']});
	var checkHead = values.checkHead;
	
	var DefaultCheckType = Utils.doSyncRequest(ctxPath+'/cm/rd/getDefaultCheckType.json',{bala_customer:this.records[0].data['bala_customer']});
	var checkType = DefaultCheckType.checkType;
	
	var DefaultCheckCorp = Utils.doSyncRequest(ctxPath+'/cm/rd/getDefaultCheckCorp.json',{bala_customer:this.records[0].data['bala_customer']});
	var checkCorp = DefaultCheckCorp.checkCorp;
	
	//js返回当前年份，月份
	var date = new Date();
	var year = date.getFullYear();
	var month = date.getMonth()+1;
	var yearData = [];
	for(var i=year-5;i<year+5;i++){
		yearData.push([i,i]);
	}
	var yearStore = new Ext.data.ArrayStore({
		fields : ["text","value"],
		data : yearData
	});
	var monthStore = new Ext.data.ArrayStore({
		fields : ["text","value"],
		data : [[1,1],[2,2],[3,3],[4,4],[5,5],[6,6],[7,7],[8,8],[9,9],[10,10],[11,11],[12,12]]
	});
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
					name : 'cost_amount_1',//该字段在后台会重新计算,不要使用cost_amount,因为和子表使用了相同的字段名称
					fieldLabel : '总金额',
					xtype : 'uftnumberfield',
					readOnly : true,
					decimalPrecision : 2,
					value : cost_amount,
					colspan : 1
				},{
					xtype : 'localcombo',
					name : 'year',
					fieldLabel : '对账年份',
					store:yearStore,
					value : year,
					colspan : 1,
					newlineflag : true
				},{
					xtype : 'localcombo',
					name : 'month',
					fieldLabel : '对账月份',
					store : monthStore,
					value : month,
					colspan : 1,
				},{
					name : 'check_head',
					fieldLabel : '发票抬头',
					//itemCls:'uft-form-label-not-null',
					value : checkHead,
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'check_corp',
					fieldLabel : '开票公司',
					//itemCls:'uft-form-label-not-null',
					value:checkCorp,
					dataUrl : ctxPath+'/cm/rd/getCheckCorp.json',
					colspan : 1
				},{
					xtype : 'uftcombo',
					name : 'check_type',
					fieldLabel : '发票类型',
					//itemCls:'uft-form-label-not-null',
					value:checkType,
					dataUrl : ctxPath+'/cm/rd/getCheckType.json',
					colspan : 1
				},{
					name : 'check_remark',
					fieldLabel : '发票备注',
					colspan : 2
				},{
					name : 'memo',
					fieldLabel : '备注',
					colspan : 2
				}]
			}]
	});
	uft.cm.BuildReceCheckSheet.superclass.constructor.call(this, {
		title : '生成对账单',
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
Ext.extend(uft.cm.BuildReceCheckSheet,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		if(form.isValid()){
			var values = form.getFieldValues(false);
			var arr = [];
			for(var i=0;i<this.records.length;i++){
				var data = this.records[i].data;//这里可以得到需要的字段信息，如：客户、结算客户
				Ext.apply(data,values);
				arr.push(data);
			}
			var params = this.app.newAjaxParams();
			params[uft.jf.Constants.HEADER] = Ext.encode(arr);
			uft.Utils.doAjax({
		    	scope : this,
		    	params : params,
		    	isTip : false,
		    	url : 'buildReceCheckSheet.json',
		    	success : function(values){
		    		if(values){//保存成功直接销毁窗口
		    			if(values.datas){
		    				this.app._setHeaderValues(this.records,values.datas);
		    				this.app.statusMgr.setBizStatus(values.datas[0][this.app.getBillStatusField()]);
		    				this.app.statusMgr.updateStatus();
		    			}
		    			if(values.msg){
		    				uft.Utils.showWarnMsg(values.msg);
		    			}
			    		this.destroy();
		    		}
		    	}
		    });	
		}
	}
});
