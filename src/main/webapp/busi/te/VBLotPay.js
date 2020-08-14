Ext.namespace('uft.te');

uft.te.VBLotPay = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.record){
			uft.Utils.showErrorMsg('请先选中记录！');
			return false;
		}
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t4032',tabCode:'ts_pay_detail_b'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode:t4032,tabcode:ts_pay_detail_b!');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t4032,tabcode:ts_pay_detail_b！');
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
			dataUrl : 'loadPayDetail.json',
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
		appConfig.context.setBodyTabCode('ts_pay_detail_b');
		appConfig.context.setFunCode(params.funCode);
		appConfig.autoRender = false;//不渲染该panel，因为要将他放在window中
		
		var MyBodyToolbar = Ext.extend(uft.jf.BodyToolbar,{
			saveUrl : ctxPath+'/te/vb/saveVBLotPay.json?lot='+lot,
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push('-');
				btns.push(this.btn_row_add);
				btns.push(this.btn_row_del);
				btns.push(this.btn_row_ins);
				btns.push('-');
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
			}
		});
		appConfig.toolbar = new MyBodyToolbar(Ext.apply({lot:this.record.get('lot')},appConfig));
		var app = new uft.jf.ToftPanel(appConfig);
		
		uft.te.VBLotPay.superclass.constructor.call(this, {
			title : '录入批次费用',
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
