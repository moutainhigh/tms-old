Ext.namespace('uft.tp');
/**
 * 委派
 * @class uft.tp.Delegate
 * @extends Ext.Window
 */
uft.tp.Delegate = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.records){
			uft.Utils.showErrorMsg('请先选中记录！');
			return false;
		}
		this.formPanel = new uft.extend.form.FormPanel({
			layout : 'form',
			frame : true,
			height : 80,
			labelWidth : 60,
			autoScroll : true,
			border : false,
			items : [
			{"refName":"公司目录-web","xtype":"headerreffield",allowBlank:false,"name":"pk_corp","fieldLabel":"公司","width":150,"refWindow":{"model":0,"leafflag":false,"params":"refName:'公司目录-web'","treeDataUrl":ctxPath+"/ref/common/corp/load4Tree.json"},"pkField":"pk_corp","codeField":"corp_code","nameField":"corp_name","showCodeOnBlur":false,"getByPkUrl":ctxPath+"/ref/common/corp/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/corp/getByCode.do"}
			]
		});
		uft.tp.Delegate.superclass.constructor.call(this, {
			title : '委派公司',
			width : 300,
			height : 150,
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
					handler : this.delegate
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
	delegate : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		var billId = [],rs = this.records;
		for(var i=0;i<rs.length;i++){
			billId.push(rs[i].get('pk_segment'));//运段号
		}
		Ext.apply(values,{billId:billId});
		var params = this.app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'delegate.json',
	    	params : params,
	    	success : function(values){
	    		this.app.setHeaderValues(this.records,values.datas);
	    		if(values.append){
	    			uft.Utils.showWarnMsg(values.append);
	    		}
	    		this.close();
	    	}
		});
	}
});
