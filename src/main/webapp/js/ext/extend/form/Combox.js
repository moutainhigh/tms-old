Ext.namespace("uft.extend.form");

/**
 * 定义了一个从本地读取数据的combox，并定义了一个基本的数据选项(是，否)，
 * 可配置项包括：
 * id,hiddenName,renderTo,editable,fieldLabel,emptyText,store.
 * @class uft.extend.form.LocalCombox
 * @extends Ext.form.ComboBox
 */
uft.extend.form.LocalCombox= Ext.extend(Ext.form.ComboBox, {
	constructor:function(config){
		Ext.apply(this, config);
//		var emptyText = (config.emptyText == undefined)?'请选择...':config.emptyText;
		var editable = (config.editable ===true)?true:false;
		var allowBlank = (config.allowBlank ===false)?false:true;
		var hiddenLabel = (config.hiddenLabel == true)?true:false;
		var store;
		if(config.store == undefined){
			store = new Ext.data.SimpleStore({  //填充的数据
                 fields : ['text', 'value'],
                 data : [['是', '1'], ['否', '0']]
            });
		}else{
			store = config.store;
		}
		
	     uft.extend.form.LocalCombox.superclass.constructor.call(this, {
			id : config.id,
			hiddenName : config.hiddenName,
			renderTo : config.renderTo,
			fieldLabel : config.fieldLabel,
			emptyText : config.emptyText,
			editable : editable, 
			hiddenLabel : hiddenLabel,
			selectOnFocus : true,
			width : config.width,
			triggerAction: 'all', 
			displayField : 'text',
			valueField : 'value',
			typeAhead: true,   
			minChars:2,
			autocomplete:'on', 
			mode: 'local',
			allowBlank: allowBlank,
			store : store		
		 });
	}
});
Ext.reg("localcombo",uft.extend.form.LocalCombox);

/**
 * 从远程读取数据的combox
 * 可配置的数据项：
 * id,renderTo,editable,fieldLabel,emptyText,dataUrl,baseParams
 * 返回的数据格式如：
 * {"records":[{"text":"abc","id":"123"},{"text":"def","id":"345"}]}
 * xtype比较特殊，区别于Ext自身的combo，故使用uftcombo
 * @param {} config
 */
uft.extend.form.Combox= Ext.extend(Ext.form.ComboBox, {
	constructor:function(config){
		Ext.apply(this, config);
//		var emptyText = (config.emptyText == undefined)?'请选择...':config.emptyText;
		var editable = (config.editable ===true)?true:false;
		var allowBlank = (config.allowBlank ===false)?false:true;
		
	     uft.extend.form.Combox.superclass.constructor.call(this, {
			id : config.id,
			name : config.name,
			hiddenName : config.hiddenName,
			renderTo : config.renderTo,
			fieldLabel : config.fieldLabel,
			emptyText : config.emptyText,
			editable : editable,  
			triggerAction: 'all', 
			displayField : 'text',
			valueField : 'value',
			allowBlank: allowBlank,
			selectOnFocus : true,
			typeAhead: true,   
			minChars:config.minChars || 2,
			autocomplete:'on', 
			mode: 'remote',
			store: new Ext.data.JsonStore({  
		         url: config.dataUrl,
		         baseParams : config.baseParams,
		         remoteSort: true,   
		         root:'records',   
		         fields:['value', 'text']  
		     })	     
		 });
	}
});
Ext.reg("uftcombo",uft.extend.form.Combox);


/**
 * 支持分页的combox，从远程读取数据
 * 返回的数据如：
 * {"records":[{"name":"abc","id":"12321"},{"name":"hfh","id":"567"}],"totalRecords":7}
 * @class uft.extend.form.PageCombox
 * @extends Ext.form.ComboBox
 */
uft.extend.form.PageCombox= Ext.extend(Ext.form.ComboBox, {
	constructor:function(config){
		 Ext.apply(this, config);
//		 var emptyText = (config.emptyText == undefined)?'请选择...':config.emptyText;
		 var editable = (config.editable ===true)?true:false;
		var allowBlank = (config.allowBlank ===false)?false:true;
		 var pageSize = (config.pageSize == undefined)?10:config.pageSize;
	     uft.extend.form.PageCombox.superclass.constructor.call(this, {
	     	 id : config.id,
	     	 name : config.name,
	     	 hiddenName : config.hiddenName,
	    	 renderTo : config.renderTo,
	         valueField: 'value',  
	         displayField: 'text',  
	         editable : editable,  
	         typeAhead : true,
//	         emptyText  : emptyText,
	         allowBlank: allowBlank,
	         selectOnFocus : true,
	         triggerAction: 'all',  
			 minChars:config.minChars || 2,
			 autocomplete:'on',	         
	         mode: 'remote',  
	         minListWidth:250,  
	         pageSize:pageSize,  
	         store: new Ext.data.JsonStore({  
	             url: config.dataUrl,
	             baseParams : config.baseParams,
	             root:'records',   
	             totalProperty: 'totalRecords',   
	             remoteSort: true,   
	             fields:['value', 'text']  
	         })         
		 })	;
	}
});
Ext.reg("pagecombo", uft.extend.form.PageCombox);

/**
 * 从页面的select元素读取数据的combox
 * transform属性是必须的，作为数据源
 * @class uft.extend.form.TranCombox
 * @extends Ext.form.ComboBox
 */
uft.extend.form.TranCombox= Ext.extend(Ext.form.ComboBox, {
	constructor:function(config){
		 Ext.apply(this, config);
//		 var emptyText = (config.emptyText == undefined)?'请选择...':config.emptyText;
		var editable = (config.editable ===true)?true:false;
		var allowBlank = (config.allowBlank ===false)?false:true;
		 var width = (config.width == undefined)?200:config.width;
		 var anchor = (config.anchor == undefined)?"85%":config.anchor;
	     uft.extend.form.TranCombox.superclass.constructor.call(this, {
			id : config.id,
			name : config.name,
			hiddenName : config.hiddenName,
	        fieldLabel :  config.fieldLabel,
	        renderTo : config.renderTo,
//	        emptyText : emptyText,
	        editable : editable,  
	        allowBlank: allowBlank,
	        selectOnFocus : true,
	        anchor : anchor,
	        typeAhead: true,
	   		triggerAction: 'all',
	   		autocomplete:'on', 
	   		lazyRender: true,
	        transform : config.transform  //页面中的select元素的id，是必须的
		 })	;
	}
});
Ext.reg("trancombo", uft.extend.form.TranCombox);