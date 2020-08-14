Ext.namespace('uft.extend.form');
/**
 * 该类继承Ext.FormPanel,自定义一些formPanel的常用方法
 * @param {} config
 */
uft.extend.form.FormPanel = function(config) {
	this.onstatuschange = null; //页面状态改变时触发的属性，一般的application中将该属性注册到状态管理器中
	this.bakUp=null; //存储field的disabled状态
	this.bakUpReadOnly=null; //存储field的readOnly状态
	this.registerEnterKey = true;
	Ext.apply(this, config);
	uft.extend.form.FormPanel.superclass.constructor.call(this);
	this.registerSpecialKey();
};
Ext.extend(uft.extend.form.FormPanel, Ext.FormPanel, {
	registerSpecialKey : function(){
	    this.getForm().items.each(function(f) {
	    	f.on('specialkey', function(f,e){
				if(e.getKey()==13){
					//如果单纯跳转到下一个输入框，可能没办法执行编辑后事件
					this.focusNextField(f);
				} 
	        },this);
	    },this);
	},
	/**
	 * 聚焦到下一个可编辑的节点
	 * @param {} f
	 */
	focusNextField : function(f){
		var efs = this.getEditableFields();
		if(efs.length == 0){
			return;
		}
		if(!f){
			efs[0].focus();
			return;
		}		
		for(var i=0;i<efs.length;i++){
			if(f.id == efs[i].id){
				if(i < efs.length-1){
					if(f.triggerBlur){
						f.triggerBlur();
					}
					efs[i+1].focus();
					return;
				}
			}
		}
	},
	/**
	 * 返回表单上所有field
	 * @return {}
	 */
	getFields : function(){
		var arr = new Array();
		var items = this.getForm().items.items;
		for(var i=0; i<items.length; i++){
			arr.push(items[i].getName());
		}
		return arr;
	},
	/**
	 * 返回所有可见的field
	 * @return {}
	 */
	getVisibleFields : function(){
		var arr = new Array();
		var items = this.getForm().items.items;
		for(var i=0; i<items.length; i++){
			if(!items[i].xtype == 'hidden')
				arr.push(items[i].getName());
		}
		return arr;
	},
	/**
	 * 返回所有可编辑的field
	 */
	getEditableFields : function(){
		var arr = new Array();
		var items = this.getForm().items.items;
		for(var i=0; i<items.length; i++){
			if(items[i].xtype != 'hidden' && !items[i].readOnly)
				arr.push(items[i]);
		}
		return arr;
	},
	/**
	 * 备份表单的disabled field，并将所有field置为disabled
	 */
	setDisabled : function() {
		if(this.bakUp == null) {
			this.storeDisabled();
		}
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			arr[i].setDisabled(true);
		}
	},
	
	/**
	 * 备份所有disabled field
	 */
	storeDisabled : function() {
		var bakUp = {};
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			var disabled = arr[i].disabled;
			if(!disabled)
				bakUp[arr[i].id] = false;
			else
				bakUp[arr[i].id] = true;	
		}
		this.bakUp = bakUp;
	},
	
	/**
	 * 还原表单的所有disabled属性
	 * @param editableFields 自定义可编辑域，如果存在则只开启自定义项
	 */
	reStoreDisabled : function(editableFields) {
		if(editableFields){
			if(editableFields instanceof Array){
				for(var i=0;i<editableFields.length;i++){
					var cmp = Ext.getCmp(editableFields[i]);
					if(cmp){
						cmp.setDisabled(false);
					}
				}
			}else{
				var cmp = Ext.getCmp(editableFields);
				if(cmp){
					cmp.setDisabled(false);
				}
			}
		}else{
			if(this.bakUp==null){
				this.storeDisabled();
			}
			var bakUp = this.bakUp;
			var arr = this.getForm().items.items;
			for(var i=0; i<arr.length; i++){
				arr[i].setDisabled(bakUp[arr[i].id]);
			}
		}
	},
	/**
	 * 将reviseflag===true的field设置成可编辑状态
	 * 设置修订
	 */
	enableRevise : function() {
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			if(arr[i].reviseflag===true){ //只有该选项为true时，才开放编辑
				arr[i].setReadOnly(false);
			}else{
				if(arr[i].readOnly !== true){
					arr[i].setReadOnly(true);
				}
			}
		}
	},
	
	/**
	 * 将所有field设置成不可编辑状态
	 */
	disableRevise : function() {
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			arr[i].setReadOnly(false);
		}
	},		
	
	/**
	 * 备份表单的readOnly field，并将所有field置为readOnly
	 */
	setReadOnly : function() {
		if(this.bakUpReadOnly == null) {
			this.storeReadOnly();
		}
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			if(arr[i].readOnly !== true){
				arr[i].setReadOnly(true);
			}
		}		
	},
	
	/**
	 * 备份表单field的readOnly属性
	 */
	storeReadOnly : function() {
		var bakUpReadOnly = {};
		var arr = this.getForm().items.items;
		for(var i=0; i<arr.length; i++){
			var readOnly = arr[i].readOnly;
			if(!readOnly)
				bakUpReadOnly[arr[i].id] = false;
			else
				bakUpReadOnly[arr[i].id] = true;	
		}
		this.bakUpReadOnly = bakUpReadOnly;
	},
	
	/**
	 * 还原表单的readOnly属性
	 */
	reStoreReadOnly : function(editableFields) {
		if(editableFields){
			if(editableFields instanceof Array){
				for(var i=0;i<editableFields.length;i++){
					var cmp = Ext.getCmp(editableFields[i]);
					if(cmp){
						cmp.setReadOnly(false);
					}
				}
			}else{
				var cmp = Ext.getCmp(editableFields);
				if(cmp){
					cmp.setReadOnly(false);
				}
			}
		}else{
			if(this.bakUpReadOnly==null){
				this.storeReadOnly();
			}
			var bakUpReadOnly = this.bakUpReadOnly;
			var arr = this.getForm().items.items;
			for(var i=0; i<arr.length; i++){
				arr[i].setReadOnly(bakUpReadOnly[arr[i].id]);
			}
		}		
	},
	/**
	 * 返回验证错误的信息集合
	 * @return {}
	 */
    getErrors : function(onlyOne){
    	var errors = new Array();
	    this.getForm().items.each(function(f){
	       if(!f.validate()){
	       	   errors.push('[<span class=\''+f.itemCls+'\'>'+f.fieldLabel+'</span>],'+f.getErrors()[0]+'<br/>');
	       	   if(onlyOne){
	       	   		//只加入第一个field的错误信息
	       	   		var lastEle = errors[errors.length-1];
	    			errors[errors.length-1]=lastEle.substring(0,lastEle.length-5); //去掉最后一个元素的<br/>
	    			return errors;
	       	   }	       	   
	       }
	    });
	    if(errors.length>0){
	    	var lastEle = errors[errors.length-1];
	    	errors[errors.length-1]=lastEle.substring(0,lastEle.length-5); //去掉最后一个元素的<br/>
	    }
	    return errors;
    },
    /**
     * 返回form中某个field的值
     * @param {} key
     */
    getValue : function(key){
    	var cmp = Ext.getCmp(key);
    	if(!cmp){
    		return null;
    	}
    	return cmp.getValue();
    },
    /**
     * 设置form中某个field的值
     * @param {} key
     * @param {} value
     */
    setValue : function(key,value){
    	var cmp = Ext.getCmp(key);
    	if(!cmp){
    		return;
    	}
    	cmp.setValue(value);
    }
});