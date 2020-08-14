Ext.namespace('uft.te');
/**
 * 异常跟踪
 * @class uft.te.BatchTracking
 * @extends Ext.Window
 */
uft.te.BatchTracking = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.records){
			uft.Utils.showErrorMsg('请先选中记录！');
			return false;
		}
		this.formPanel = new uft.extend.form.FormPanel({
			region : 'north',
			height : 225,
			labelWidth : 80,
			autoScroll : true,
			border : true,
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
						xtype : 'uftcombo',
						name : 'tracking_status',
						fieldLabel : '跟踪状态',
						itemCls:'uft-form-label-not-null',
						allowBlank : false,
						dataUrl : ctxPath+'/datadict/getDataDict4Combo.json',
						baseParams : {datatype_code:'tracking_status'},
						colspan : 1
					},{
						name : 'tracking_time',
						fieldLabel : '跟踪时间',
						itemCls:'uft-form-label-not-null',
						allowBlank : false,
						value : new Date().dateFormat('Y-m-d H:i:s'),
						xtype : 'datetimefield',
						colspan : 1
					},{
						name : 'est_arri_time',
						fieldLabel : '预计到达时间',
						xtype : 'datetimefield',
						colspan : 1
					},{
						xtype : 'textarea',
						name : 'tracking_memo',
						fieldLabel : '跟踪信息',
						height:100,
						colspan : 3
					}]
				}]
		});
		uft.te.BatchTracking.superclass.constructor.call(this, {
			title : '跟踪',
			width : 700,
			height : 300,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [this.formPanel],
			buttons : [{
					xtype : 'button',
					text : '保存',
					iconCls : 'btnSave',
					scope : this,
					handler : this.batchSaveEntTracking
				},new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();
					}
			})]
	    });		
	},
	batchSaveEntTracking : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		Ext.apply(values,{origin:'异常跟踪'});//异常来源
		
		var vbillno = [],rs = this.records;
		for(var i=0;i<rs.length;i++){
			vbillno.push(rs[i].get('vbillno'));//委托单号
		}
		Ext.apply(values,{vbillno:vbillno});
		var params = app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'batchSaveEntTracking.json',
	    	params : params,
	    	success : function(values){
	    		app.headerGrid.getStore().reload();
	    		this.close();
	    	}
		});
	}
});
