Ext.namespace('uft.te');
uft.te.VehiclePay = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.record){
			uft.Utils.showErrorMsg('请先选中记录！');
			return false;
		}
		
		var dataMap = Utils.doSyncRequest(ctxPath+'/te/vb/getKilometreAndDays.json',{lot:this.record.get('lot')});
		var kilometre;
		var days;
		if(dataMap){
			kilometre = dataMap['kilometre'];
			days = dataMap[days];
		}
		
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t50608',tabCode:'ts_vehicle_pay'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode:t50608,tabcode:ts_vehicle_pay!');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t50608,tabcode:ts_vehicle_pay！');
			}
			return false;
		}
		var R=values.data.records;
		var C=values.data.columns;
		var params = values.data.params;
		var lot = this.record.get('lot');
		params['lot'] = this.record.get('lot');
		
		this.payDetailGrid = new uft.extend.grid.EditorGrid({
			pkFieldName : 'lot',
			dataUrl : 'loadVehiclePay.json',
			params : Ext.apply({lot:lot},params),
			border : true,
			isCheckboxSelectionModel : false,
			singleSelect : true,
			isAddBbar : false,
			immediatelyLoad : true,
			recordType : R,
			columns : C
		});
		
		var appConfig = {};
		appConfig.ajaxLoadDefaultValue = false;
		appConfig.headerPkField = '';
		appConfig.bodyGrids = [];
		appConfig.bodyGrids.push(this.payDetailGrid);
		appConfig.context = new uft.jf.Context();
		appConfig.context.setTemplateID(params.templateID);
		appConfig.context.setBodyTabCode('ts_vehicle_pay');
		appConfig.context.setFunCode(params.funCode);
		appConfig.autoRender = true;//不渲染该panel，因为要将他放在window中
		var MyBodyToolbar = Ext.extend(uft.jf.BodyToolbar,{
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
//				btns.push('-');
//				btns.push(this.btn_row_add);
//				btns.push(this.btn_row_del);
//				btns.push(this.btn_row_ins);
				btns.push('-');
				btns.push('公里数：');
				btns.push({
					id : '_kilometre',
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					name : 'kilometre',
					value : kilometre,
					fieldLabel : '公里数：',
					colspan : 1
				});
				btns.push('KM');
				btns.push('天数：');
				btns.push({
					id : '_days',
					xtype : 'uftnumberfield',
					decimalPrecision : 1,
					name : 'days',
					value : kilometre,
					fieldLabel : '天数：',
					colspan : 1
				});
				btns.push('天');
				return btns;
			},
			btn_row_del_handler : function(){
				var grid = this.app.getActiveBodyGrid();
				var record = grid.getSelectedRow();
				var system_create = record.get("system_create");
				if(system_create && system_create == "Y"){
					uft.Utils.showWarnMsg('系统创建的费用，不允许删除！');
					return false;
				}
				MyBodyToolbar.superclass.btn_row_del_handler.call(this);
			},
			btn_save_handler : function(){
				var url = ctxPath+'/te/vb/saveVehiclePay.json?lot='+lot+'&kilometre='+Ext.getCmp('_kilometre').getValue()+'&days='+Ext.getCmp('_days').getValue();
				if(this.app.hasBodyGrid()){
					var grid=this.app.getActiveBodyGrid();
					if(typeof(grid.stopEditing) == "function"){
						grid.stopEditing();
					}			
				}
				var params=this.app.newAjaxParams();
				var appPostData={};
				var bodyGridData={};
				var tabCodes=this.app.context.getBodyTabCode().split(',');//bodyGrids与tabCodes的长度肯定相同
				var bodyGrids = this.app.getBodyGrids();
				for(var i = 0; i < bodyGrids.length; i++) {
					if(typeof(bodyGrids[i].getAllRecordValue) == "function"){
						if(bodyGrids[i].isValid()) {//这里使用第三方插件进行验证
							bodyGridData[tabCodes[i]] = bodyGrids[i].getAllRecordValue();
						}else{
							var errors = bodyGrids[i].getAllErrors();
							if(errors.length > 0){
								uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
								return false;
							}
						}
					}
				}
				//加入从表数据
				appPostData[uft.jf.Constants.BODY]=bodyGridData;
		    	if(appPostData !== false){
					params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
					uft.Utils.doAjax({
				    	scope : this,
				    	url : url,
				    	params : params,
				    	success : function(values){
				    		var m = this.app;
				    		if(this.app.hasBodyGrid()) {
				    			this.app.reloadBodyGrids();
				    		}
				    		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
				    		this.app.statusMgr.updateStatus();
				    	}
				    });
		    	}
			},
		});
		appConfig.toolbar = new MyBodyToolbar(Ext.apply({lot:this.record.get('lot')},appConfig));
		var app = new uft.jf.ToftPanel(appConfig);
		uft.te.VehiclePay.superclass.constructor.call(this, {
			title : '车队费用',
			width : 800,
			height : 400,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			buttons : [new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();
					}
			})],
			items : [app]
	    });		
	}
});
