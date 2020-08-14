Ext.ns('uft.user');
uft.user.UserRole= function(config){
	Ext.apply(this, config);
	
	var ds= new Ext.data.JsonStore({  
         url: 'loadUnAuthorizeRoleByUser.json',
         baseParams : {pk_user: config.pk_user},
         remoteSort: true,   
         root:'records',   
         fields:['value', 'text']  
     });
    ds.load();
	var ds1= new Ext.data.JsonStore({  
         url: 'loadRoleByUser.json',
         baseParams : {pk_user: config.pk_user},
         remoteSort: true,   
         root:'records',   
         fields:['value', 'text']  
     });
     ds1.load();
     
    var isForm = new Ext.form.FormPanel({
        width:510,
        border : false,
        hideLabel :true,
        items:[{
            xtype: 'itemselector',
            name: 'itemselector',
            drawUpIcon : false,
            drawDownIcon : false,
            hideLabel :true,
            leftLegend : '可添加的角色',
            rightLegend : '已添加的角色',
	        imagePath: ctxPath + '/theme/default/images/default/btn',
            multiselects: [{
                width: 250,
                height: 300,
                store: ds,
                displayField: 'text',
                valueField: 'value'
            },{
                width: 250,
                height: 300,
                hideLabel :true,
                displayField: 'text',
                valueField: 'value',
                store: ds1
            }]
        }]
    });
    
    var main = new Ext.Panel({
    	bodyStyle: 'padding:10px 10px;',
    	layout : 'fit',
    	items : [isForm]
    });
    
    this.submit = new Ext.Button({
    	iconCls : 'btnYes',
		text : '保&nbsp;&nbsp;存',
		actiontype : 'submit',
		scope : this,
		handler : function() {
			this.submit.disable();
			var pkRoles = [];
            if(isForm.getForm().isValid()){
                var value = isForm.getForm().getValues(true);
                value = value.substring(value.indexOf("=")+1, value.length);
                pkRoles = value.split('%2C');
            }
		    uft.Utils.doAjax({
		    	scope : this,
		    	isTip : true,
		    	url : 'addRoleToUser.json',
		    	params : {pk_role:pkRoles,pk_user:this.pk_user},
		    	success : function(values){
		    		this.submit.enable();
		    	}
		    });
		}
	});
	
	var btn = [this.submit, {
		xtype : 'button',
		iconCls : 'btnCancel',
		text : '关&nbsp;&nbsp;闭',
		scope : this,
		handler : function() {
			this.destroy();
		}
	}]	;	
	uft.user.UserRole.superclass.constructor.call(this, {
		title : this.title||'分配角色',
		width : 580,
		height : 390,
		closable : true,
		collapsible : true,
		draggable : true,
		resizable : true,
		border : false,
		modal : true,
		layout : 'fit',
		items : [main],
		buttons : [btn]
    });    
};
Ext.extend(uft.user.UserRole,Ext.Window, {

});