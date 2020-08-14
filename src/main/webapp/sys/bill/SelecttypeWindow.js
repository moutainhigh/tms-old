Ext.ns('uft.bill');
/**
 * 参照选择对话框
 * 供uft.bill.ReftypeField调用，不能单独使用
 * @class uft.bill.SelecttypeWindow
 * @extends Ext.Window
 */
uft.bill.SelecttypeWindow= Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		this.s_selectfield = new Ext.ux.form.MultiSelect({
								hideLabel : true,
								newlineflag : true,
								height : 175,
								name : 's_selectfield',
								displayField: 'text',
						        valueField: 'value',
								store: new Ext.data.JsonStore({
							        root:'records',
							        fields:['value', 'text']
							    })
							});
		this.s_selectfield.on('_add',this.updateStatus,this);
		this.s_selectfield.on('_update',this.updateStatus,this);
		this.s_selectfield.on('_remove',this.updateStatus,this);
		this.s_selectfield.on('_clear',this.updateStatus,this);
		this.formPanel = new uft.extend.form.FormPanel({
			labelWidth : 90,
			border : true,
			items : [{
					layout : 'form',
					border : false,
					padding : '5px 5px 0',
					defaults:{
						anchor: '98%'
					},      
					items : [{
						fieldLabel: '返回值设置',
						xtype : 'radiogroup',
						id : 'returnValueTypeRadio',
						items: [{boxLabel: '返回值', name: 'returnValueType', inputValue: 'S',checked: true},
								{boxLabel: '返回索引', name: 'returnValueType', inputValue: 'I'},
								{hidden : true}
						]
					},{
						fieldLabel: '值类型设置',
						xtype : 'radiogroup',
						id : 'valueTypeRadio',
						items: [{boxLabel: '普通', name: 'valueType', inputValue: ''},
								{boxLabel: '名值对方式', name: 'valueType', inputValue: 'X',checked : true},
								{boxLabel: '数据库方式', name: 'valueType', inputValue: 'F',id : 'databaseValueType'}
						]
					},{
						autoWidth : true,
						xtype : 'fieldset',
						title : '枚举值设置',
						layout : 'tableform',
						height : 230,
						labelWidth : 60,
						layoutConfig: {columns:3},
						items : [{
							xtype : 'textfield',
							fieldLabel : '表名',
							colspan : 1,
							id : 's_tablename',
							name : 's_tablename'
						},{
							xtype : 'textfield',
							fieldLabel : '字段名',
							colspan : 1,
							id : 's_fieldname',
							name : 's_fieldname'
						},{
							colspan : 2,
							border : false,
							layout : 'fit',
							height : 180,
							autoScroll : false,
							items : [this.s_selectfield]
						},{
							colspan : 1,
							border : false,
							padding : '8px 0 0 5px',
							baseCls: 'x-plain',
							xtype : 'buttongroup',
							id : 's_buttongroup',
							width : 50,
							layoutConfig:{
				        		columns:1
				        	},
				        	items : [{
					        	text:'增加',
					        	width : 50,
					        	cls : 'x-btn-padd',
					        	scope : this,
					        	handler : function(){
					        		var form = this.formPanel.getForm();
					        		var values = form.getFieldValues(false);
					        		//2014-10-21修改了radioGroup的getValue
					        		var valueType = Ext.getCmp('valueTypeRadio').getValue();//这里得到的是选中的那个radio的对象的值
//					        		var valueType = item.inputValue;//值类型
					        		var win = new uft.bill.InputValueWin({s_selectfield:this.s_selectfield,valueType:valueType,isAdd:true});
					        		win.show();
					        	}
					        },{
					        	text:'修改',
					        	width : 50,
					        	cls : 'x-btn-padd',
					        	scope : this,
					        	handler : function(){
					        		var selectionsArray = this.s_selectfield.view.getSelectedIndexes();
					        		if(selectionsArray && selectionsArray.length > 1){
					        			uft.Utils.showWarnMsg('有且只能选择一行记录！');
					        			return;
					        		}
					        		var form = this.formPanel.getForm();
					        		var values = form.getFieldValues(false);
					        		var valueType = Ext.getCmp('valueTypeRadio').getValue();//这里得到的是选中的那个radio的对象
//					        		var valueType = item.inputValue;//值类型
					        		var win = new uft.bill.InputValueWin({s_selectfield:this.s_selectfield,valueType:valueType,isAdd:false});
					        		win.show();
					        	}
					        },{
					        	text:'删除',
					        	width : 50,
					        	cls : 'x-btn-padd',
					        	scope : this,
					        	handler : function(){
					        		var selectionsArray = this.s_selectfield.view.getSelectedIndexes();
					        		if(selectionsArray && selectionsArray.length > 1){
					        			uft.Utils.showWarnMsg('有且只能选择一行记录！');
					        			return;
					        		}
					        		var store = this.s_selectfield.view.store;
					        		var record = store.getAt(selectionsArray[0]);
					        		store.remove(record);
					        	}
					        },{
					        	text:'清除',
					        	width : 50,
					        	cls : 'x-btn-padd',
					        	scope : this,
					        	handler : function(){
					        		this.s_selectfield.clearRecord();
					        	}
					        }]
						}]
					}]
				}]
		});
		
		var btns = [{
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : function(){
				var form = this.formPanel.getForm();
				if(form.isValid()){
					var returnValue='';
					var values = form.getFieldValues(false);
					var returnValueType = Ext.getCmp('returnValueTypeRadio').getValue();
					//var returnValueType = item.inputValue;//返回值类型
					
					var valueType = Ext.getCmp('valueTypeRadio').getValue();
//					var valueType = item.inputValue;//值类型
					returnValue += returnValueType;
					returnValue += valueType;
					if(valueType == 'F'){
						//数据库类型
						var s_tablename = values['s_tablename']||'';//表名
						var s_fieldname = values['s_fieldname']||'';//字段名
						returnValue += ',';
						returnValue += s_tablename;
						returnValue += ',';
						returnValue += s_fieldname;
					}else{
						var store = this.s_selectfield.view.store,count = store.getCount();
						for(var i=0;i<count;i++){
							var r = store.getAt(i);
							returnValue +=',';
							returnValue +=r.get(this.s_selectfield.valueField);
						}
					}
				}
				this.fireEvent('aftersubmit',returnValue);
				this.close();
			}
		}, {
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		}];
		uft.bill.SelecttypeWindow.superclass.constructor.call(this, {
			title : '下拉类型设置',
			width : 550,
			height : 370,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true,
			draggable : false,
			modal : true,
			resizable : true,
			border : false,
			layout : 'fit',
			items : [this.formPanel],
			buttons : btns
	    });
	},
	/**
	 * 恢复值
	 */
	restoreValue : function(){
		if(!this.startValue){
			return;
		}
		var returnValueTypeRadio = Ext.getCmp('returnValueTypeRadio');//返回值类型设置
		var valueTypeRadio = Ext.getCmp('valueTypeRadio');//值类型设置
		var s_tablename = Ext.getCmp('s_tablename');
		var s_fieldname = Ext.getCmp('s_fieldname');
		
		var valueAry = this.startValue.split(',');
		if(valueAry[0].indexOf('S') == 0 || valueAry[0].indexOf('CG') == 0 || valueAry[0].indexOf('RG') == 0){
			//返回值
			returnValueTypeRadio.items.items[0].setValue('S');
		}else{
			//返回索引
			returnValueTypeRadio.items.items[1].setValue('I');
		}
		if(valueAry[0].indexOf('SX') == 0 || valueAry[0].indexOf('IX') == 0 || valueAry[0].indexOf('CG') == 0 || valueAry[0].indexOf('RG') == 0){
			valueTypeRadio.items.items[1].setValue('X');
			//名值对应
			var records = [];
			for(var i=1;i<valueAry.length;i++){
				var obj = {};
				obj[this.s_selectfield.displayField] = valueAry[i];
				obj[this.s_selectfield.valueField] = valueAry[i];
				records.push(obj);
			}
			this.s_selectfield.setRecord(records);
		}else if(valueAry[0].indexOf('SF') == 0 || valueAry[0].indexOf('IF') == 0){
			//数据库
			valueTypeRadio.items.items[2].setValue('F');
			s_tablename.setValue(valueAry[1]);
			s_fieldname.setValue(valueAry[2]);
		}else{
			//普通
			valueTypeRadio.items.items[0].setValue('');
			var records = [];
			for(var i=1;i<valueAry.length;i++){
				var obj = {};
				obj[this.s_selectfield.displayField] = valueAry[i];
				obj[this.s_selectfield.valueField] = valueAry[i];
				records.push(obj);
			}
			this.s_selectfield.setRecord(records);
		}
	},
	/**
	 * 更新界面的状态
	 */
	updateStatus : function(){
		var valueTypeRadio = Ext.getCmp('valueTypeRadio');//值类型设置
		var s_tablename = Ext.getCmp('s_tablename');
		var s_fieldname = Ext.getCmp('s_fieldname');
		//1、默认不能编辑表、字段
		s_tablename.setReadOnly(true);
		s_fieldname.setReadOnly(true);
		//2、如果multiselect存在数据，那么不能编辑“值类型”
		var store = this.s_selectfield.view.store;
		if(store.getCount() > 0){
			valueTypeRadio.items.each(function(item){
			   item.disable();
			});
		}else{
			valueTypeRadio.items.each(function(item){
			   item.enable();
			});
		}
		//3、如果选择了“数据库方式”，那么表、字段可编辑
		var item = valueTypeRadio.getValue();
		if(item.inputValue == 'F'){
			this.setStatusIfDBType(true);
		}else{
			this.setStatusIfDBType(false);
		}
	},
	/**
	 * 选择了数据库方式的时候的状态
	 */
	setStatusIfDBType : function(checked){
		var s_tablename = Ext.getCmp('s_tablename');
		var s_fieldname = Ext.getCmp('s_fieldname');
		var s_buttongroup = Ext.getCmp('s_buttongroup');
		if(checked){
			s_tablename.setReadOnly(false);
			s_fieldname.setReadOnly(false);
			//禁用按钮
			s_buttongroup.items.each(function(item){
			   item.disable();
			});
		}else{
			s_tablename.setValue(null);
			s_fieldname.setValue(null);
			s_tablename.setReadOnly(true);
			s_fieldname.setReadOnly(true);
			//启用按钮
			s_buttongroup.items.each(function(item){
			   item.enable();
			});
		}
	},
	afterRender : function(){
		uft.bill.SelecttypeWindow.superclass.afterRender.call(this);
		this.updateStatus();
		this.restoreValue();
	    //注册点击“数据库方式”时的事件
		var valueTypeRadio = Ext.getCmp('valueTypeRadio');//值类型设置
		valueTypeRadio.items.each(function(item){
		   item.on('aftercheck',function(item,value){
		   		if(item.id == 'databaseValueType'){
		   			this.setStatusIfDBType(true);
		   		}else{
		   			this.setStatusIfDBType(false);
		   		}
		   },this);
		},this);
	}
});
//输入值的window
uft.bill.InputValueWin = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		var s_selectfield = this.s_selectfield;//显示下拉框值的multiselect
		var valueType = this.valueType;//值类型
		var isAdd = this.isAdd;
		
		var displayname='',value='';
		if(!isAdd){
			//修改
			var store = s_selectfield.view.store;
			var selectionsArray = s_selectfield.view.getSelectedIndexes();
			var record = store.getAt(selectionsArray[0]);
			var valueObj = record.get(s_selectfield.valueField);
			if(valueObj.indexOf('=') != -1){
				//名值对类型
				var arr = valueObj.split('=');
				displayname = arr[0];
				value = arr[1];
			}else{
				//只有值
				value = valueObj;
			}
		}
		
		var items = [];
		if(valueType == 'X'){
			items.push({fieldLabel : '显示名称',name : 's_displayname',value:displayname,"itemCls":"uft-form-label-not-null",allowBlank:false});
		}
		items.push({fieldLabel : '值',name : 's_value',value:value,"itemCls":"uft-form-label-not-null",allowBlank:false});		
		
		this.form = new uft.extend.form.FormPanel({
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
			items : items
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : function(){
				var form = this.form.getForm();
				if(form.isValid()){
					var returnValue;
					var values = form.getFieldValues(false);
					if(valueType == 'X'){
						returnValue = values['s_displayname'];
						returnValue += "=";
						returnValue += values['s_value'];
					}else{
						returnValue = values['s_value'];
					}
					
					var valueObj = {};
					valueObj[s_selectfield.valueField] = returnValue;
					valueObj[s_selectfield.displayField] = returnValue;
					if(isAdd){//增加记录
						s_selectfield.addRecord([valueObj]);
					}else{//修改记录
						s_selectfield.updateRecord(valueObj);
					}
					this.close();
				}
			}
		},{
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		uft.bill.InputValueWin.superclass.constructor.call(this, {
			title : '输入值',
			width : 300,
			autoHeight : true,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.form],
			buttons : btns
		});
	}
});
