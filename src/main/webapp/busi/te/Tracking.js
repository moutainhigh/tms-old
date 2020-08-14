function _processImg(){
	return "<div align='center'><img src='"+ctxPath+"/images/plus.gif' border=0 onclick='deleteEntTracking()' style='cursor:pointer'>";
};
function deleteEntTracking(){
	var grid = Ext.getCmp('ts_ent_tracking');
	var record = uft.Utils.getSelectedRecord(grid);
	if(record){
		var last_pk = null;
		var store = grid.getStore();
		var lr = store.getAt(store.getCount()-1);//最后一条
		var index = store.indexOf(record);
		if(index == store.getCount()-1){
			//最后一条
			if(index > 0){//读取倒数第二条
				last_pk = store.getAt(index-1).get('pk_ent_tracking');
			}
		}else{
			last_pk = lr.get('pk_ent_tracking');
		}
		for(var i=0;i<store.getCount();i++){
			if(index > 0){
				var r = store.getAt(index-1);
				last_pk = r.get('pk_ent_tracking');//得到上一条跟踪记录的pk
			}
		}
		var pk_ent_tracking = record.get('pk_ent_tracking');
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'GET',
	    	url : 'deleteEntTracking.json',
	    	actionType : '删除',
	    	params : {pk_ent_tracking:pk_ent_tracking,last_pk:last_pk},
	    	success : function(values){
	    		grid.getStore().remove(record);//删除记录
	    		app.toReload = true;
	    	}
		});
	}
}
Ext.namespace('uft.te');
/**
 * 异常跟踪
 * @class uft.te.Tracking
 * @extends Ext.Window
 */
uft.te.Tracking = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.record){
			uft.Utils.showErrorMsg('请先选中一条记录！');
			return false;
		}
		//初始化是否需要重新加载app的数据
		app.toReload = false;
		
		var entrust_vbillno = this.record.get('vbillno');//委托单
		var pk_carrier = this.record.get('pk_carrier');//承运商
		
		//可同步的发货单的表格
		var values = Utils.doSyncRequest('getTempletMap.json',{pk_billtypecode:'ts_te_trin',tabCode:'ts_invoice'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,pk_billtypecode：ts_te_trin,tabCode:ts_invoice!');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，pk_billtypecode：ts_te_trin,tabCode:ts_invoice！');
			}
			return false;
		}		
		var R=values.data.records;
		var C=values.data.columns;
		this.trackingInvoiceGrid = new uft.extend.grid.BasicGrid({
			height : 200,
			id : 'tracking_invoice',
			pkFieldName : 'pk_invoice',
			dataUrl : 'loadTrackingInvoice.json',
			params : {entrust_vbillno:entrust_vbillno},
			border : true,
			isCheckboxSelectionModel : true,
			singleSelect : false,
			immediatelyLoad : true,
			recordType : R,
			columns : C
		});
		this.trackingInvoiceGrid.getStore().on('load',function(store,rs,ops){
			var sm = this.trackingInvoiceGrid.getSelectionModel().selectAll();
		},this);
		
		this.formPanel = new uft.extend.form.FormPanel({
			id : 'trackingForm',
			region : 'north',
			height : 240,
			labelWidth : 80,
			bodyStyle:'overflow-y:auto;overflow-x:hidden',
			margins : '0px 5px 0px 5px',
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
						height:50,
						colspan : 3
					},{
						xtype : 'uftcheckbox',
						name : 'exp_flag',
						fieldLabel : '是否异常',
						"inputValue":"false",
						colspan : 1
					},{
						id : 'exp_info',
						xtype : 'fieldset',
						title : '异常详细信息',
						collapsible : false,
						collapsed : true,
						layout : 'tableform',
						padding : '5px 5px 0',
						colspan : 3,
						autoScroll : false,
						layoutConfig: {columns:3},
						defaults:{
							anchor: '95%'
						},
						items : [
				   			{"name":"vbillno","fieldLabel":"单据号","xtype":"textfield","colspan":1,"maxLength":50}
					   		,{"name":"vbillstatus","fieldLabel":"状态","itemCls":"uft-form-label-not-edit",value : 0,"xtype":"localcombo","readOnly":true,"colspan":1,"hidden":false,"maxLength":200,"store":{"fields":["text","value"],"data":[["&nbsp;",""],["新建","0"],["待处理","71"],["处理中","72"],["已处理","73"],["已关闭","74"]],"xtype":"arraystore"}}
				   			,{"refName":"发货单-web","xtype":"headerreffield","name":"invoice_vbillno","fieldLabel":"发货单号","width":1,"colspan":1,"refWindow":{"model":1,"leafflag":false,"params":"refName:'发货单-web'","gridDataUrl":ctxPath+"/ref/common/inv/load4Grid.json?entrust_vbillno="+entrust_vbillno,"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn","hidden":true},{"header":"发货单号","type":"string","width":120,"dataIndex":"vbillno","xtype":"gridcolumn"},{"header":"客户订单号","type":"string","width":120,"dataIndex":"cust_orderno","xtype":"gridcolumn"},{"header":"订单号","type":"string","width":120,"dataIndex":"orderno","xtype":"gridcolumn"},{"header":"客户","type":"string","width":120,"dataIndex":"customer_name","xtype":"gridcolumn"},{"header":"结算客户","type":"string","width":120,"dataIndex":"bala_customer_name","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"pk_customer","xtype":"gridcolumn","hidden":true},{"type":"string","width":120,"dataIndex":"bala_customer","xtype":"gridcolumn","hidden":true}]},"pkField":"vbillno","codeField":"vbillno","nameField":"vbillno","fillinable":true,"showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/inv/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/inv/getByCode.do"}
			   				,{id : '_pk_customer',itemCls:'uft-form-label-not-edit',"readOnly":true,"refName":"客户档案-web","xtype":"headerreffield","name":"pk_customer","fieldLabel":"客户","width":1,"colspan":1,"editFormulaUrl":"execFormula.json","refWindow":{"model":1,"leafflag":false,"params":"refName:'客户档案-web'","gridDataUrl":ctxPath+"/ref/common/cust/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_customer","xtype":"gridcolumn","hidden":true},{"header":"客户编码","type":"string","width":120,"dataIndex":"cust_code","xtype":"gridcolumn"},{"header":"客户名称","type":"string","width":120,"dataIndex":"cust_name","xtype":"gridcolumn"},{"header":"客户类型","type":"string","width":120,"dataIndex":"cust_type_name","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"cust_type","xtype":"gridcolumn","hidden":true}]},"pkField":"pk_customer","codeField":"cust_code","nameField":"cust_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/cust/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/cust/getByCode.do"}
			   				,{itemCls:'uft-form-label-not-edit',value:entrust_vbillno,"fieldLabel":"委托单号","readOnly":true,xtype:'textfield',name:'entrust_vbillno'}
							,{itemCls:'uft-form-label-not-edit',value:pk_carrier,"readOnly":true,"refName":"承运商档案-web","xtype":"headerreffield","name":"pk_carrier","fieldLabel":"承运商","width":1,"colspan":1,"editFormulaUrl":"execFormula.json","refWindow":{"model":1,"leafflag":false,"params":"refName:'承运商档案-web'","gridDataUrl":ctxPath+"/ref/common/carr/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_carrier","xtype":"gridcolumn","hidden":true},{"header":"承运商编码","type":"string","width":120,"dataIndex":"carr_code","xtype":"gridcolumn"},{"header":"承运商名称","type":"string","width":120,"dataIndex":"carr_name","xtype":"gridcolumn"},{"header":"承运商类型","type":"string","width":120,"dataIndex":"carr_type","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"pk_carrier","codeField":"carr_code","nameField":"carr_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/carr/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/carr/getByCode.do"}
			   				,{"name":"exp_type",
							   "fieldLabel":"类型",
							   "xtype":"multiselectfield",
							   "colspan":1,
							   "allowBlank":true,
							   layzyLoad : false,
							   dataUrl : ctxPath + '/datadict/getDataDict4MultiSelect.json',
							   baseParams : {datatype_code:'exp_accident_type'}
							}
				   			,{"name":"memo","fieldLabel":"描述","xtype":"textfield","colspan":2,"maxLength":200}
			   				,{"name":"fb_user","fieldLabel":"反馈人","xtype":"textfield","colspan":1,"maxLength":20}
			   				,{"name":"fb_date","fieldLabel":"反馈日期","xtype":"datetimefield","colspan":1,"maxLength":20}
			  				,{"name":"occur_date","fieldLabel":"发生日期","xtype":"datetimefield","colspan":1,"maxLength":20,newlineflag:true}
			   				,{"name":"occur_addr","fieldLabel":"发生地点","xtype":"textfield","colspan":1,"maxLength":200}
			   				,{"id":"pk_filesystem1","name":"pk_filesystem1","fieldLabel":"附件","xtype":"fileuploadfield","colspan":1,"maxLength":200,permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png'],newlineflag : true}
			   				,{"id":"pk_filesystem2","name":"pk_filesystem2","fieldLabel":"附件","xtype":"fileuploadfield","colspan":1,"maxLength":200,permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png']}
			   				,{"id":"pk_filesystem3","name":"pk_filesystem3","fieldLabel":"附件","xtype":"fileuploadfield","colspan":1,"maxLength":200,permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png']}
						]
					},{
						xtype : 'uftcheckbox',
						name : 'sync_flag',
						fieldLabel : '是否同步',
						"inputValue":"false",
						checked : true,
						colspan : 1
					},{
						id : 'sync_info',
						xtype : 'fieldset',
						title : '可同步的发货单',
						collapsible : true,
						collapsed : true,
						layout : 'fit',
						autoScroll : false,
						colspan : 3,
						items : [
				   			this.trackingInvoiceGrid
						]
					},{"name":"mainno",
					  "fieldLabel":"主单号",
					  "xtype":"textfield",
					  newlineflag : true,
					  "colspan":1,
					  "maxLength":50
					},{"name":"flightno",
					  "fieldLabel":"航班号",
					  "xtype":"textfield",
					  "colspan":1,
					  "maxLength":50
					},{"name":"flight_time",
					  "fieldLabel":"航班时间",
					  "xtype":"datetimefield",
					  "colspan":1,
					  "maxLength":50
					}]
				}]
		});
		this.formPanel.getForm().items.each(function(field){
			field.on('change',function(field,value,originalValue){
				if(field.name == 'invoice_vbillno'){
					//发货单，带出客户
					var values = Utils.doSyncRequest(ctxPath+'/te/ea/getCustomerByInvoice_vbillno.json',{invoice_vbillno:value},'POST');
					if(values && values.data){
						Ext.getCmp('_pk_customer').setValue(values.data);
					}
				}
			});
			if(field.name == 'exp_flag'){//是否异常
				field.on('check',function(field,value){
					var exp_info = Ext.getCmp('exp_info');
					var form = Ext.getCmp('trackingForm');
					if(value == 'Y'){
						exp_info.expand();
						form.setHeight(form.getHeight()+135);
						this.doLayout();
					}else{
						exp_info.collapse();
						form.setHeight(form.getHeight()-135);
						this.doLayout();
					}
				},this);
			}else if(field.name == 'pk_filesystem1'){
					field.on('fileselected',function(field,fileName){
						//上传
						var body = Ext.getBody();
						body.mask(uft.jf.Constants.UPLOADING_MSG);
						  $.ajaxFileUpload({
			                url:'uploadAttach.json',
			                secureuri:false,
			                fileElementId:'pk_filesystem1-file',
			                referTarget : field,
			                dataType: 'json',
			                success: function (result, status){
			                	body.unmask();
			                    if(result.success){
			                    	uft.Utils.showInfoMsg('文件上传成功！');
			                    	field.setValue(result.data['pk_filesystem1']); //这里实际上是文件的pk
			                    }else{
			                    	field.setValue(null);
			                    	uft.Utils.showErrorMsg(result.msg);
			                    	return;
			                    }
			                }
			          	});
					},this);
				}else if(field.name == 'pk_filesystem2'){
					field.on('fileselected',function(field,fileName){
						//上传
						var body = Ext.getBody();
						body.mask(uft.jf.Constants.UPLOADING_MSG);
						  $.ajaxFileUpload({
			                url:'uploadAttach.json',
			                secureuri:false,
			                fileElementId:'pk_filesystem2-file',
			                referTarget : field,
			                dataType: 'json',
			                success: function (result, status){
			                	body.unmask();
			                    if(result.success){
			                    	uft.Utils.showInfoMsg('文件上传成功！');
			                    	field.setValue(result.data['pk_filesystem2']); //这里实际上是文件的pk
			                    }else{
			                    	field.setValue(null);
			                    	uft.Utils.showErrorMsg(result.msg);
			                    	return;
			                    }
			                }
			          	});
					},this);
				}else if(field.name == 'pk_filesystem3'){
					field.on('fileselected',function(field,fileName){
						//上传
						var body = Ext.getBody();
						body.mask(uft.jf.Constants.UPLOADING_MSG);
						  $.ajaxFileUpload({
			                url:'uploadAttach.json',
			                secureuri:false,
			                fileElementId:'pk_filesystem3-file',
			                referTarget : field,
			                dataType: 'json',
			                success: function (result, status){
			                	body.unmask();
			                    if(result.success){
			                    	uft.Utils.showInfoMsg('文件上传成功！');
			                    	field.setValue(result.data['pk_filesystem3']); //这里实际上是文件的pk
			                    }else{
			                    	field.setValue(null);
			                    	uft.Utils.showErrorMsg(result.msg);
			                    	return;
			                    }
			                }
			          	});
					},this);
				}
		},this);
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t510',tabCode:'ts_ent_tracking'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode：t510,tabcode:ts_ent_tracking!');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t510,tabcode:ts_ent_tracking！');
			}
			return false;
		}		
		var R=values.data.records;
		var C=values.data.columns;
		R.unshift({"name":"_processor","type":"string"});
		C.unshift({"header":"<span class='uft-grid-header-column'>操作</span>",renderer:_processImg,"width":30,"dataIndex":"_processor","xtype":"gridcolumn","editable":false});
		var entTrackingGrid = new uft.extend.grid.BasicGrid({
			region : 'center',
			id : 'ts_ent_tracking',
			pkFieldName : 'pk_ent_tracking',
			dataUrl : 'loadEntTracking.json',
			params : {entrust_vbillno:entrust_vbillno},
			border : true,
			isCheckboxSelectionModel : false,
			sm : new Ext.grid.RowSelectionModel(),
			singleSelect : true,
			isAddBbar : true,//自动跟踪的数据可能很多,这里使用分页
			immediatelyLoad : true,
			recordType : R,
			columns : C
		});				
		uft.te.Tracking.superclass.constructor.call(this, {
			title : '异常跟踪',
			width : 850,
			height : 520,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'border',
			items : [this.formPanel,entTrackingGrid],
			buttons : [{
					xtype : 'button',
					text : '保存',
					iconCls : 'btnSave',
					scope : this,
					handler : this.saveEntTracking
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
	/**
	 * 保存
	 */
	saveEntTracking : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		Ext.apply(values,{origin:'异常跟踪'});//异常来源
		var params = app.newAjaxParams();
		Ext.apply(params,values);
		//所选择的发货单号
		var invoiceVbillnoAry = [];
		var rs = uft.Utils.getSelectedRecords(this.trackingInvoiceGrid);
		if(rs){
			for(var i=0;i<rs.length;i++){
				invoiceVbillnoAry.push(rs[i].get('vbillno'));
			}
		}
		params['invoiceVbillnoAry'] = invoiceVbillnoAry;
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'saveEntTracking.json',
	    	params : params,
	    	success : function(values){
	    		//保存后，清空表单值
	    		this.formPanel.getForm().reset();
	    		//像表体动态增加记录
	    		if(values && values.data){
	    			var data = values.data;
		    		var grid = Ext.getCmp('ts_ent_tracking');
		    		var recordType = grid.getStore().recordType;
			        var record = new recordType();
					record.beginEdit();
					for(var key in data){
						if(data[key] && data[key].pk){
							//这属于参照类型的返回值
							record.set(key,data[key].pk);
						}else{
							record.set(key,data[key]);
						}
					}
					record.endEdit();
			        var index = grid.getStore().getCount();
			        grid.getStore().insert(index, record);
			        app.toReload = true;
	    		}
	    	}
		});
	}
});
