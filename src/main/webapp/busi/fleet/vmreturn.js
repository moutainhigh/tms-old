Ext.ns('uft.vm');
uft.vm.vmreturn = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选择要出车的记录！');
		return false;
	}
	this.formPanel = new uft.extend.form.FormPanel({
		labelWidth : 60,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:2},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield' 
				},      
				items : [{
					name : 'mileage',
					fieldLabel : '码表读数',
					value : '',
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					colspan : 1
				},{
					name : 'gps',
					fieldLabel : 'GPS读数',
					value : '',
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					colspan : 1
				},{
					name : 'fule',
					fieldLabel : '油量',
					xtype : 'uftnumberfield',
					decimalPrecision : 2,
					value : '',
					colspan : 1
				},{
					xtype : 'datetimefield',
					name : 'return_time',
					fieldLabel : '收车时间',
					allowBlank : false,
					itemCls:'uft-form-label-not-null',
					value : DateUtils.formatDate(new Date(),'yyyy-MM-dd H:m:s'),
					newlineflag : true,
					colspan : 1
				},{"refName":"地址档案-web","itemCls":"uft-form-label-not-null","xtype":"headerreffield","name":"addr","fieldLabel":"收车地址",allowBlank : false,editable:true, 
					"refWindow":{"model":1,"leafflag":false,"gridDataUrl":ctxPath+"/ref/common/addr/load4Grid.json",
						"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_address","xtype":"gridcolumn","hidden":true},
												{"header":"地址编码","type":"string","width":120,"dataIndex":"addr_code","xtype":"gridcolumn"},
												{"header":"地址名称","type":"string","width":120,"dataIndex":"addr_name","xtype":"gridcolumn"}]},
												"pkField":"pk_address","codeField":"addr_code","nameField":"addr_name","fillinable":true,"showCodeOnBlur":true,
												"getByPkUrl":ctxPath+"/ref/common/addr/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/addr/getByCode.do"
				},{
					name : 'memo',
					fieldLabel : '备注',
					colspan : 2
				}]
			}]
	});

	uft.vm.vmreturn.superclass.constructor.call(this, {
		title : '收车',
		width : 500,
		height : 300,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		items : [this.formPanel],
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

Ext.extend(uft.vm.vmreturn,Ext.Window, {
	saveAction : function(){
		var form = this.formPanel.getForm();
		var errors = this.formPanel.getErrors();
		if(errors.length!=0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}		
		var values = form.getFieldValues(false);
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		
		Ext.apply(values,{ids:ids});
		var params = app.newAjaxParams();
		Ext.apply(params,values);
		uft.Utils.doAjax({
	    	scope : this,
	    	method : 'POST',
	    	url : 'vmreturn.json',
	    	params : params,
	    	success : function(values){
	    		app.headerGrid.getStore().reload();
	    		this.close();
	    	}
		});
	}
});

