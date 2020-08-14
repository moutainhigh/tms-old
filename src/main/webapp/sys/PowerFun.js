Ext.namespace('uft.role');
uft.role.PowerFun = Ext.extend(Ext.Window, {
	pk_role : null, //当前选择角色
	constructor : function(config){
		Ext.apply(this, config);
		this.powerTree = new uft.extend.tree.CheckboxTree({
			border : true,
			checkModel : 'cascade',
			dataUrl : 'loadPowerFun.json',
			params : {pk_role:this.pk_role}
		});
		
		var btns = [];
		btns.push({
			iconCls : 'btnYes',
			xtype : 'button',
			text : '分&nbsp;&nbsp;配',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			iconCls : 'btnCancel',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		});		
		uft.role.PowerFun.superclass.constructor.call(this, {
			title : this.title||'分配权限',
			width : 400,
			height : 500,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.powerTree],
			buttons : btns
		});
	},
	//保存权限
	saveHandler : function(){
		var ids = this.powerTree.getCheckedNodesId();
		uft.Utils.doAjax({
	    	scope : this,
	    	params : {pk_fun:ids,pk_role:this.pk_role},
	    	isTip : true,
	    	url : 'savePowerFun.json',
	    	success : function(values){
	    		
	    	}
	    });	
	}
});
