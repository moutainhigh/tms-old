Ext.namespace('uft.te');
/**
 * 可选费用维护
 * @class uft.te.optional
 * @extends Ext.Window
 */
uft.te.Operation = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.record){
			uft.Utils.showErrorMsg('请先选中一条记录！');
			return false;
		}
		
		var pk_entrust = this.record.get('pk_entrust');
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t501',tabCode:'ts_ent_operation_b'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode：t501,tabcode:ts_ent_operation_b！');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t501,tabcode:ts_ent_operation_b！');
			}
			return false;
		}
		var R=values.data.records;
		var C=values.data.columns;
		var params = values.data.params;
		this.entOptionalGrid = new uft.extend.grid.EditorGrid({
			id : 'ts_ent_operation_b',
			pkFieldName : 'ts_ent_operation_b',
			dataUrl : ctxPath+'/te/tracking/loadEntOperationB.json',
			params : Ext.apply({pk_entrust:pk_entrust},params),
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
		appConfig.bodyGrids.push(this.entOptionalGrid);
		appConfig.context = new uft.jf.Context();
		appConfig.context.setTemplateID(params.templateID);
		appConfig.context.setBodyTabCode('ts_ent_operation_b');
		appConfig.context.setFunCode(params.funCode);
		appConfig.autoRender = false;//不渲染该panel，因为要将他放在window中
		
		var MyBodyToolbar = Ext.extend(uft.jf.BodyToolbar,{
			saveUrl : ctxPath+'/te/tracking/saveOperation.json?pk_entrust='+pk_entrust,
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
				return btns;
			},
			getRowDefaultValues : function(gridId){
				var values = MyBodyToolbar.superclass.getRowDefaultValues.call(this,gridId);
				return Ext.apply({pk_entrust:this.pk_entrust},values);//这里的值是从config中传入
			}
		});
		appConfig.toolbar = new MyBodyToolbar(Ext.apply({pk_entrust:pk_entrust},appConfig));
		var app = new uft.jf.ToftPanel(appConfig);
		
		uft.te.Operation.superclass.constructor.call(this, {
			title : '作业反馈',
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
