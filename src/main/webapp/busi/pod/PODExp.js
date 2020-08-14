Ext.ns('uft.pod');
/**
 * 异常签收
 * @param {} config
 */
uft.pod.PODExp = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要签收的记录！');
		return false;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		region : 'north',
		height : 160,
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
					name : 'pod_man',
					fieldLabel : '签收人',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					colspan : 1
				},{
					xtype : 'datetimefield',
					name : 'pod_date',
					fieldLabel : '签收日期',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					value : DateUtils.formatDate(new Date(),'yyyy-MM-dd H:m:s'),
					colspan : 1
				},{"name":"pod_exp_type",
				   "fieldLabel":"异常类型",
				   "xtype":"multiselectfield",
				   "colspan":1,
				   dataUrl : ctxPath + '/datadict/getDataDict4MultiSelect.json',
				   baseParams : {datatype_code:'exp_accident_type'}
				},{
					name : 'pod_memo',
					fieldLabel : '签收备注',
					colspan : 2
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 0,
					name : 'pod_num_count',
					fieldLabel : '签收件数',
					value : this.record.get('num_count'),
					readOnly : true,
					itemCls:'uft-grid-header-column-not-edit',
					newlineflag : true,
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 0,
					name : 'reject_num_count',
					fieldLabel : '拒签件数',
					readOnly : true,
					itemCls:'uft-grid-header-column-not-edit',
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 0,
					name : 'damage_num_count',
					fieldLabel : '破损件数',
					readOnly : true,
					itemCls:'uft-grid-header-column-not-edit',
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 0,
					name : 'lost_num_count',
					fieldLabel : '丢失件数',
					readOnly : true,
					itemCls:'uft-grid-header-column-not-edit',
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					name : 'pod_weight_count',
					id : '_pod_weight_count',
					fieldLabel : '签收重量',
					value : this.record.get('weight_count'),
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					name : 'pod_volume_count',
					id : '_pod_volume_count',
					fieldLabel : '签收体积',
					value : this.record.get('volume_count'),
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					name : 'pod_volume_weight_count',
					id : '_pod_volume_weight_count',
					fieldLabel : '签收体积重',
					value : this.record.get('volume_weight_count'),
					readOnly : true,
					itemCls:'uft-grid-header-column-not-edit',
					colspan : 1
				},{
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					name : 'pod_fee_weight_count',
					id : '_pod_fee_weight_count',
					fieldLabel : '签收计费重',
					value : this.record.get('fee_weight_count'),
					colspan : 1
				},{
					xtype : 'uftcheckbox',
					name : 'update_rece_detail',
					fieldLabel : '更新应收明细',
					colspan : 1
				}]
			}]
	});
	//注册表头的after change事件
	this.formPanel.getForm().items.each(function(field){
		field.on('change',function(field,value,originalValue){
			if(field.name == 'pod_weight_count'){
				//签收重量
				this.compute();
			}else if(field.name == 'pod_volume_count'){
				//签收体积
				this.compute();
			}
		},this);
	},this);
	
	var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t601',tabCode:'ts_inv_pack_b'},'POST');
	if(!values || !values.data){
		if(langague && langague == 'en_US'){
			uft.Utils.showErrorMsg('No template data,funCode：t601,tabcode:ts_inv_pack_b!');
		}else{
			uft.Utils.showErrorMsg('没有模板数据，funCode：t601,tabcode:ts_inv_pack_b！');
		}
		return false;
	}
	var R=values.data.records;
	var C=values.data.columns;
	var params = values.data.params;
	params['isBody'] = 'true';
	params['pk_invoice'] = this.record.data['pk_invoice'];
	params['fromExp'] = true;
	this.grid = new uft.extend.grid.EditorGrid({
		region : 'center',
		id : 'ts_inv_pack_b_1',
		pkFieldName : 'pk_inv_pack_b',
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
	uft.pod.PODExp.superclass.constructor.call(this, {
		title : '异常签收',
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
Ext.extend(uft.pod.PODExp,Ext.Window, {
	compute : function(){
		uft.Utils.doAjax({
	    	scope : this,
	    	isTip : false,
	    	params : {
	    		pk_trans_type:this.record.get('pk_trans_type'),
	    		pk_customer:this.record.get('pk_customer'),
	    		deli_city:this.record.get('deli_city'),
	    		arri_city:this.record.get('arri_city')
	    		},
	    	url : ctxPath+'/inv/inv/getFeeRate.json',
	    	success : function(values){
	    		if(values.data){
	    			this._compute(values.data);
	    		}
	    	}
	    });	
	},
	//计算体积重和计费重
	_compute : function(rate){
		//缓存中已经存在换算比率
		var volume_count = uft.Utils.getField('_pod_volume_count').getValue(); //总体积
		var weight_count = uft.Utils.getField('_pod_weight_count').getValue(); //总重量
		var volume_weight_count = volume_count*rate;//总体积重=总体积*体积重换算比率
		uft.Utils.getField('_pod_volume_weight_count').setValue(volume_weight_count);
		var fee = volume_weight_count; //总体积/体积重换算比率
		if(fee < weight_count){
			fee =  weight_count;
		}
		uft.Utils.getField('_pod_fee_weight_count').setValue(fee);		
	},
	checkBeforeSave : function(){
		var store = this.grid.getStore();
		for(var i=0;i<store.getCount();i++){
			var record = store.getAt(i);
			var num = uft.Utils.getNumberColumnValue(record,'num');//每行的总件数
			var podNum = uft.Utils.getNumberColumnValue(record,'pod_num');
			var rejectNum = uft.Utils.getNumberColumnValue(record,'reject_num');
			var damageNum = uft.Utils.getNumberColumnValue(record,'damage_num');
			var lostNum = uft.Utils.getNumberColumnValue(record,'lost_num');
			if(num != (podNum+rejectNum+damageNum+lostNum)){
				uft.Utils.showWarnMsg('第'+(i+1)+'行签收件数+拒收件数+破损件数+丢失件数必须等于件数！');
//				if(langague && langague == 'en_US'){
//					uft.Utils.showWarnMsg('Line '+i+1+',sign number + number + number + lost to reject the damaged pieces must be equal to the number of!');
//				}else{
//					uft.Utils.showWarnMsg('第'+(i+1)+'行签收件数+拒收件数+破损件数+丢失件数必须等于件数！');
//				}
				return false;
			}
		}
	},
	checkAfterEditNum:function(record,columnName){
		//统计到表头
		var valueMap = uft.Utils.getGridSumValueMap('ts_inv_pack_b_1',['pod_num','reject_num','damage_num','lost_num']);
		this.formPanel.getForm().items.each(function(f) {
			var name = n = f.getName();
			if(name == 'pod_num_count' || name=='reject_num_count' || name=='damage_num_count' || name=='lost_num_count'){
				f.setValue(valueMap[name.substring(0,name.indexOf('_count'))]);
			}
		});
		if(columnName == 'pod_num'){//签收数量
			//1、更新同一行重量体积
			var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight');
			var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume');
			var pod_num = uft.Utils.getNumberColumnValue(record,'pod_num');
			record.beginEdit();
			uft.Utils.setColumnValue(record,'weight',pod_num*unit_weight);
			uft.Utils.setColumnValue(record,'volume',pod_num*unit_volume);
			record.endEdit();
			//2、更新表头的重量、体积
			var resultMap = uft.Utils.getGridSumValueMap('ts_inv_pack_b_1',['num','weight','volume']);
			uft.Utils.getField('_pod_weight_count').setValue(resultMap['weight']);
			uft.Utils.getField('_pod_volume_count').setValue(resultMap['volume']);
			//3、更新表头的计费重、体积重
			this.compute();
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
			bodyGridData['ts_inv_pack_b'] = this.grid.getAllRecordValue();
			//加入子表数据
			appPostData[uft.jf.Constants.BODY]=bodyGridData;
			return appPostData;			
		}
		return null;
	},	
	saveAction : function(){
		if(this.checkBeforeSave() === false){
			return;
		}
		var params=this.app.newAjaxParams();
		var appPostData = this.getAppParams();
		if(!appPostData){
			return;
		}
		params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
		params['pk_invoice'] = this.record.data['pk_invoice'];
		uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	url : 'expPod.json',
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
