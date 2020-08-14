Ext.namespace('uft.tp');
/**
 * 批量配载
 * @class uft.tp.BatchPZ
 * @extends Ext.Window
 */
uft.tp.BatchPZ = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.records){
			uft.Utils.showErrorMsg('请先选中记录！');
			return false;
		}
		this.formPanel = new uft.extend.form.FormPanel({
			height : 225,
			labelWidth : 80,
			autoScroll : true,
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
					items : [
					{"refName":"车辆档案-web","xtype":"headerreffield","name":"carno","fieldLabel":"车牌号","colspan":1,"pkBilltemplet":"0001A410000000000O0Y","pkBilltempletB":"0001A410000000000O0Z","hasEditformula":true,"refWindow":{"model":1,"leafflag":false,"params":"refName:'车辆档案-web'","gridDataUrl":ctxPath+"/ref/common/car/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"carno","xtype":"gridcolumn","hidden":true},{"header":"车牌号","type":"string","width":120,"dataIndex":"carno","xtype":"gridcolumn"},{"header":"车辆类型","type":"string","width":120,"dataIndex":"car_type_name","xtype":"gridcolumn"},{"header":"车辆性质","type":"string","width":120,"dataIndex":"car_prop_name","xtype":"gridcolumn"},{"type":"string","width":120,"dataIndex":"pk_car_type","xtype":"gridcolumn","hidden":true},{"type":"string","width":120,"dataIndex":"car_prop","xtype":"gridcolumn","hidden":true}]},"pkField":"carno","codeField":"carno","nameField":"carno","fillinable":true,"showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/car/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/car/getByCode.do"}
				   ,
					   {"refName":"承运商档案-web","allowBlank":false,"xtype":"headerreffield","name":"pk_carrier","fieldLabel":"承运商","itemCls":"uft-form-label-not-null","colspan":1,"script":"refreshPayDetail()","refWindow":{"model":1,"leafflag":false,"params":"refName:'承运商档案-web'","gridDataUrl":ctxPath+"/ref/common/carr/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_carrier","xtype":"gridcolumn","hidden":true},{"header":"承运商编码","type":"string","width":120,"dataIndex":"carr_code","xtype":"gridcolumn"},{"header":"承运商名称","type":"string","width":120,"dataIndex":"carr_name","xtype":"gridcolumn"},{"header":"承运商类型","type":"string","width":120,"dataIndex":"carr_type","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"pk_carrier","codeField":"carr_code","nameField":"carr_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/carr/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/carr/getByCode.do"}
				   ,
					   {"refName":"司机档案-web","xtype":"headerreffield","name":"pk_driver","fieldLabel":"司机","colspan":1,"refWindow":{"model":1,"leafflag":false,"params":"refName:'司机档案-web'","gridDataUrl":ctxPath+"/ref/common/driver2/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_driver","xtype":"gridcolumn","hidden":true},{"header":"司机编码","type":"string","width":120,"dataIndex":"driver_code","xtype":"gridcolumn"},{"header":"司机名称","type":"string","width":120,"dataIndex":"driver_name","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"driver_name","codeField":"driver_name","nameField":"driver_name","fillinable":true,"showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/driver2/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/driver2/getByCode.do"}
				   ,
					   {"refName":"车辆类型-web","xtype":"headerreffield","name":"pk_car_type","fieldLabel":"车型","colspan":1,"script":"refreshPayDetail()","refWindow":{"model":1,"leafflag":false,"params":"refName:'车辆类型-web'","gridDataUrl":ctxPath+"/ref/common/cartype/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_car_type","xtype":"gridcolumn","hidden":true},{"header":"类型编码","type":"string","width":120,"dataIndex":"code","xtype":"gridcolumn"},{"header":"类型名称","type":"string","width":120,"dataIndex":"name","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"pk_car_type","codeField":"code","nameField":"name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/cartype/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/cartype/getByCode.do"}
				   ,
					   {"refName":"运输方式-web","allowBlank":false,"xtype":"headerreffield","name":"pk_trans_type","fieldLabel":"运输方式","itemCls":"uft-form-label-not-null","colspan":1,"script":"updateHeaderFeeWeightCount();refreshPayDetail()","refWindow":{"model":1,"leafflag":false,"params":"refName:'运输方式-web'","gridDataUrl":ctxPath+"/ref/common/transtype/load4Grid.json","extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_trans_type","xtype":"gridcolumn","hidden":true},{"header":"运输方式编码","type":"string","width":120,"dataIndex":"code","xtype":"gridcolumn"},{"header":"运输方式名称","type":"string","width":120,"dataIndex":"name","xtype":"gridcolumn"},{"header":"备注","type":"string","width":120,"dataIndex":"memo","xtype":"gridcolumn"}]},"pkField":"pk_trans_type","codeField":"code","nameField":"name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/transtype/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/transtype/getByCode.do"}
				   ,
					   {"name":"balatype","fieldLabel":"结算方式","xtype":"localcombo","colspan":1,"hidden":false,"maxLength":200,"store":{"fields":["text","value"],"data":[["&nbsp;",""],["月结","0"],["类型2","1"]],"xtype":"arraystore"}}
				  // ,
					//   {"name":"valuation_type","fieldLabel":"计价方式","xtype":"localcombo","colspan":1,"maxLength":200,"store":{"fields":["text","value"],"data":[["&nbsp;",""],["重量","0"],["体积","1"],["件数","2"],["设备","3"],["吨公里","4"],["票","5"],["节点","6"]],"xtype":"arraystore"}}
					,
					{"name":"gps_id","fieldLabel":"GPS_ID","xtype":"textfield","colspan":1,"maxLength":50}
					,
					{"name":"lot","fieldLabel":"批次号","xtype":"textfield","colspan":1,"maxLength":50}
					,
					   {"name":"memo","fieldLabel":"备注","xtype":"textfield","colspan":4,"maxLength":200}
					]
				}]
		});
		this.fieldMap = {};//fieldname和field对象的一个map
		this.formPanel.getForm().items.each(function(field){
			this.fieldMap[field.name] = field;
			field.on('change',function(field,value,originalValue){
				if(field.hasEditformula===true){
					var params = this.formPanel.getForm().getFieldValues(false);
					params['pkBilltemplet']=field.pkBilltemplet;
					params['pkBilltempletB']=field.pkBilltempletB;							
					var body = Ext.getBody();
					body.mask(uft.jf.Constants.PROCESS_MSG);//显示操作提示
					//不要使用异步方法,以免点击保存后还出现没有执行完
					var values = Utils.doSyncRequest('execFormula.json',params,'POST');
					//设置执行公式后的值
					this.setRetObj(values);
					body.unmask();
				}
			},this);
		},this);
		uft.tp.BatchPZ.superclass.constructor.call(this, {
			title : '批量排单',
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
					handler : this.batchPZ
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
	setRetObj : function(retObj){
		var key;
		for(key in retObj){
			var field = this.fieldMap[key];
			if(field){
				field.setValue(retObj[key]);
			}
		}		
	},
	batchPZ : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		var vbillno = [],rs = this.records;
		for(var i=0;i<rs.length;i++){
			vbillno.push(rs[i].get('vbillno'));//运段号
		}
		Ext.apply(values,{vbillno:vbillno});
		var params = this.app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : ctxPath+'/tp/pz/batchSave.json',
	    	params : params,
	    	success : function(values){
	    		this.app.toReload = true;
	    		this.close();
	    	}
		});
	}
});
