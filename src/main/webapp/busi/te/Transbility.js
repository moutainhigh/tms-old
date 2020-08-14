Ext.namespace('uft.te');
/**
 * 异常跟踪-维护运力信息
 * @class uft.te.Tracking
 * @extends Ext.Window
 */
$import('/busi/te/TrackingMap.js');
uft.te.Transbility = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.record){
			uft.Utils.showErrorMsg('请先选中一条记录！');
			return false;
		}
		
		var pk_entrust = this.record.get('pk_entrust');
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t501',tabCode:'ts_ent_transbility_b'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode：t501,tabcode:ts_ent_transbility_b！');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t501,tabcode:ts_ent_transbility_b！');
			}
			return false;
		}
		var R=values.data.records;
		var C=values.data.columns;
		var params = values.data.params;
		this.entTransbilityGrid = new uft.extend.grid.EditorGrid({
			id : 'ts_ent_transbility_b',
			pkFieldName : 'pk_ent_transbility_b',
			dataUrl : 'loadEntTransbilityB.json',
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
		appConfig.bodyGrids.push(this.entTransbilityGrid);
		appConfig.context = new uft.jf.Context();
		appConfig.context.setTemplateID(params.templateID);
		appConfig.context.setBodyTabCode('ts_ent_transbility_b');
		appConfig.context.setFunCode(params.funCode);
		appConfig.autoRender = false;//不渲染该panel，因为要将他放在window中
		
		var MyBodyToolbar = Ext.extend(uft.jf.BodyToolbar,{
			saveUrl : ctxPath+'/te/tb/saveTransbility.json?pk_entrust='+pk_entrust,
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
				btns.push(new uft.extend.Button({
					text : '路线跟踪',
					scope : this,
					handler : function(){
						var r = this.app.getActiveBodyGrid().getSelectedRow();
						if(!r){
							uft.Utils.showErrorMsg('请先选中一条记录！');
							return false;
						}
						var pk_entrust = r.get('pk_entrust');
						var gps_id = r.get('gps_id');
						if(!gps_id){
							uft.Utils.showErrorMsg('该委托单还没有绑定GPS！');
							return false;
						}
						var win = Ext.getCmp('TrackingMap');
						if(!win){
							win = new uft.te.TrackingMap({pk_entrust:pk_entrust,gps_id:gps_id});
						}
						win.show();
					}
				}));
				return btns;
			},
			getRowDefaultValues : function(gridId){
				var values = MyBodyToolbar.superclass.getRowDefaultValues.call(this,gridId);
				return Ext.apply({pk_entrust:this.pk_entrust},values);//这里的值是从config中传入
			}
		});
		appConfig.toolbar = new MyBodyToolbar(Ext.apply({pk_entrust:pk_entrust},appConfig));
		var app = new uft.jf.ToftPanel(appConfig);
		
		uft.te.Transbility.superclass.constructor.call(this, {
			title : '运力信息',
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
