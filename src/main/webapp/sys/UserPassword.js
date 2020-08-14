Ext.namespace('uft.user');
uft.user.UserPassword = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		var items = [];
		items.push({
        	xtype : 'hidden',
            name : 'pk_user',
            value : this.pk_user
        });
		
		items.push({fieldLabel : '新密码',inputType : 'password',
			name : 'distPassword',id : 'distPassword'
		}); //这个id是作为验证用的
			
		items.push({fieldLabel : '重复新密码',inputType : 'password',
            name : 'rePassword',id : 'rePassword',vtype : 'password',initial : 'distPassword'
        });		
		
		this.passwordForm = new uft.extend.form.FormPanel({
	        labelWidth : 80,
	        border : false,
	        autoWidth : true,
			autoHeight : true,
			frame : true,
			defaults : {
				xtype : 'textfield',
				allowBlank : false,
				anchor : '85%',
				width : 200
			},
			monitorValid : true,
			items : items
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '保&nbsp;&nbsp;存',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		});		
		uft.user.UserPassword.superclass.constructor.call(this, {
			title : this.title || '修改密码',
			width : 300,
			autoHeight : true,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.passwordForm],
			buttons : btns
		});
	},
	saveHandler : function(){
		var errors = this.passwordForm.getErrors();
		if(errors.length==0){
			var values = this.passwordForm.getForm().getFieldValues(false);
			uft.Utils.doAjax({
		    	scope : this,
		    	isTip : true,
		    	url : ctxPath + '/user/editPassword.json',
		    	params : values,
		    	success : function(){
		    		this.destroy();
		    	}
		    });
		}else{
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return false;
		}
	}
});
