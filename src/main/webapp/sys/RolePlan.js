Ext.ns('uft.role');
uft.role.RolePlan= function(config){
	Ext.apply(this, config);
	
	var ds= new Ext.data.JsonStore({  
         url: 'loadUnAuthorizePlanByRole.json',
         baseParams : {pk_role: config.pk_role},
         remoteSort: true,   
         root:'records',   
         fields:['value', 'text']  
     });
    ds.load();
	var ds1= new Ext.data.JsonStore({  
         url: 'loadPlanByRole.json',
         baseParams : {pk_role: config.pk_role},
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
            leftLegend : '可添加的方案',
            rightLegend : '已添加的方案',
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
			var pkPlans = [];
            if(isForm.getForm().isValid()){
                var value = isForm.getForm().getValues(true);
                value = value.substring(value.indexOf("=")+1, value.length);
                pkPlans = value.split('%2C');
            }
		    uft.Utils.doAjax({
		    	scope : this,
		    	isTip : true,
		    	url : 'addPlanToRole.json',
		    	params : {pk_plan:pkPlans,pk_role:this.pk_role},
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
	uft.role.RolePlan.superclass.constructor.call(this, {
		title : this.title||'分配方案',
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
Ext.extend(uft.role.RolePlan,Ext.Window, {

});