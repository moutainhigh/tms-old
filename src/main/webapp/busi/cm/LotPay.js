Ext.namespace('uft.cm');

uft.cm.LotPay = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.records){
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
		
		var ids = [],rs = this.records;
		for(var i=0;i<rs.length;i++){
			ids.push(rs[i].get('pk_pay_detail'));//委托单PK
		}
		params['pk_pay_detail'] = ids;
		
		this.payDetailGrid = new uft.extend.grid.EditorGrid({
			//id : 'ts_pay_detail', 与主界面的id冲突
			pkFieldName : 'pk_pay_detail',
			dataUrl : 'loadPayDetail.json',
			params : Ext.apply({pk_pay_detail:pk_pay_detail},params),
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
			saveUrl : ctxPath+'/cm/pd/saveLotPay.json?pk_pay_detail='+ids,
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
			getRowDefaultValues : function(gridId){
				var values = MyBodyToolbar.superclass.getRowDefaultValues.call(this,gridId);
				return Ext.apply({pk_pay_detail:this.pk_pay_detail},values);//这里的值是从config中传入
			}
		});
		appConfig.toolbar = new MyBodyToolbar(Ext.apply({pk_pay_detail:pk_pay_detail},appConfig));
		var app = new uft.jf.ToftPanel(appConfig);
		
		uft.cm.LotPay.superclass.constructor.call(this, {
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
