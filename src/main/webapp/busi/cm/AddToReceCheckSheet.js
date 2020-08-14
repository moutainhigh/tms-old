Ext.ns('uft.cm');
/**
 * 加入对账单
 * @param {} config
 */
uft.cm.AddToReceCheckSheet = function(config){
	Ext.apply(this,config);
	if(!this.records){
		uft.Utils.showErrorMsg('请先选择要加入对账单的记录！');
		return false;
	}
	
	var values = Utils.doSyncRequest('getTempletMap.json',{'funCode':'t405','tabCode':'ts_rece_check_sheet'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode:t405！');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t405！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	var bala_customer = this.records[0].data['bala_customer'];
	params['bala_customer'] = bala_customer;
	this.grid = new uft.extend.grid.BasicGrid({
		pkFieldName : 'pk_rece_check_sheet',
		dataUrl : 'loadReceCheckSheet.json',
		params : params,
		singleSelect : true,
		isCheckboxSelectionModel : false,
		isAddBbar : false,
		immediatelyLoad : true,
		recordType : R,
		columns :  C
	});
	uft.cm.AddToReceCheckSheet.superclass.constructor.call(this, {
		title : '加入对账单',
		width : 800,
		height : 500,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		items : [this.grid],
		buttons : [{
			iconCls : 'btnYes',
			text : '保&nbsp;&nbsp;存',
			scope : this,
			handler : function() {
				this.saveAction();
			}
		},{
			iconCls : 'btnCancel',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}]
    });	
};
Ext.extend(uft.cm.AddToReceCheckSheet,Ext.Window, {
	saveAction : function(){
		var record = uft.Utils.getSelectedRecord(this.grid);
		if(!record){
			uft.Utils.showErrorMsg('请选择记录！');
			return false;
		}
		var bala_customer = record.data['bala_customer'];
		var params = this.app.newAjaxParams(),pks = [],new_bala_customer;
		for(var i=0;i<this.records.length;i++){
			pks.push(this.records[i].data['pk_receive_detail']);
			new_bala_customer = this.records[i].data['bala_customer'];
		}
		if(bala_customer != new_bala_customer){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('You must select a settlement account for ['+bala_customer+']!');
			}else{
				uft.Utils.showErrorMsg('您必须选择结算客户为['+bala_customer+']的记录！');
			}
			return false;
		}
		params['pk_receive_detail'] = pks;
		params['pk_rece_check_sheet'] = record.data['pk_rece_check_sheet'];
		uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	isTip : false,
	    	url : 'addToReceCheckSheet.json',
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
});
