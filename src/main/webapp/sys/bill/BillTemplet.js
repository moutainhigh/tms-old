Ext.namespace('uft.bill');
uft.bill.BillTemplet = Ext.extend(Ext.Panel, {
	clientWidth : document.documentElement.clientWidth,
	clientHeight : document.documentElement.clientHeight,
	headerFieldHeight : 30,
	headerTableHeight : 200,
	leftButtonWidth : 50,
	rightButtonWidth : 70,
	constructor : function (config){
		Ext.apply(this, config); //页面上可以将该值覆盖，
		this.bodyTableHeight = this.clientHeight-this.headerTableHeight-this.headerFieldHeight;
		this.tableField = new Ext.ux.form.MultiSelect({
			height : this.clientHeight-this.headerFieldHeight,
			width : this.clientWidth/4,
			legend : '数据库表',
			displayField: 'text',
	        valueField: 'value',
			store: new Ext.data.JsonStore({
		         url: 'loadUserTable.json',
		         root:'records',
		         fields:['value', 'text']  
		    })
		});
		this.headerTableField = new Ext.ux.form.MultiSelect({
			height : this.headerTableHeight,
			width : this.clientWidth/4-this.leftButtonWidth-2,
			legend : '主表',
			displayField: 'text',
	        valueField: 'value',
			store: new Ext.data.JsonStore({
		         url: 'loadHeaderTable.json',
		         root:'records',
		         fields:['value', 'text']  
		     })	
		});
		this.bodyTableField = new Ext.ux.form.MultiSelect({
			height : this.bodyTableHeight,
			width : this.clientWidth/4-this.leftButtonWidth-2,
			legend : '子表',
			displayField: 'text',
	        valueField: 'value',
			store: new Ext.data.JsonStore({
		         url: 'loadBodyTable.json',
		         root:'records',
		         fields:['value', 'text']  
		     })	
		});
		/*********过滤输入框开始*********/
		this.filterField = new uft.extend.form.FilterField({
			emptyText : '表前缀过滤',
			enableKeyEvents : true,
			scope : this,
			fn : this.onFilter
		});
		/*********过滤输入框结束*********/
		//左边的grid的Panel，列出可选的表
		var leftPanel = new Ext.Panel({
			width : this.clientWidth/2,
			region : 'west',
			layout : 'table',
			layoutConfig : {
				columns : 2
			},
			items : [{
				padding : '3px 0 0 5px',
				height : this.headerFieldHeight,
				colspan : 2,
				border : false,
				items : [this.filterField]
			},{
				border : false,
				layout : 'fit',
				rowspan : 2,
				items : [this.tableField]
			},{
				height : this.headerTableHeight,
				border : false,
				layout : 'fit',
				items : [{
					border : false,
					width : this.clientWidth/4,
					height : this.headerTableHeight,
					layout  :'border',
					items : [{
						padding : '8px 0 0 5px',
						border : false,
						region : 'west',
						baseCls: 'x-plain',
						xtype : 'buttongroup',
						width : this.leftButtonWidth,
						layoutConfig:{
			        		columns:1
			        	},
			        	items : [{
				        	xtype : 'button',
				        	width : this.leftButtonWidth-10,
				        	text : '>',
				        	scope : this,
				        	handler : this.addToHeaderTable
				        },{
				        	xtype : 'button',
				        	width : this.leftButtonWidth-10,
				        	text : '<',
				        	scope : this,
				        	handler : this.removeFromHeaderTable
				        }]
					},{
						border : false,
						region : 'center',
						layout : 'fit',
						items : [this.headerTableField]
					}]
				}]
			},{
				height : this.bodyTableHeight,
				border : false,
				layout : 'fit',
				items : [{
					border : false,
					width : this.clientWidth/4,
					height : this.bodyTableHeight,
					layout  :'border',
					items : [{
						border : false,
						padding : '8px 0 0 5px',
						region : 'west',
						baseCls: 'x-plain',
						xtype : 'buttongroup',
						width : this.leftButtonWidth,
						layoutConfig:{
			        		columns:1
			        	},
			        	items : [{
				        	xtype : 'button',
				        	width : this.leftButtonWidth-10,
				        	text : '>',
				        	scope : this,
				        	handler : this.addToBodyTable
				        },{
				        	xtype : 'button',
				        	width : this.leftButtonWidth-10,
				        	text : '<',
				        	scope : this,
				        	handler : this.removeFromBodyTable
				        }]
					},{
						border : false,
						region : 'center',
						layout : 'fit',
						items : [this.bodyTableField]
					}]
				}]
			}]
		});
		this.billtypecodeField = new uft.extend.form.HeaderRefField({
			fieldLabel : '单据模板编码(10个字符内)',
			"refName":"单据模板-web",
			"xtype":"headerreffield",
			"id":"pk_billtypecode",
			maxLength : 10,
			"refWindow":{
				"model":1,
				"leafflag":false,
				"gridDataUrl":ctxPath +"/ref/common/bt/load4Grid.json",
				"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_billtemplet","sortable":true,"xtype":"gridcolumn","hidden":true},
				                       {"header":"模板编码","type":"string","width":120,"dataIndex":"pk_billtypecode","xtype":"gridcolumn"},
				                       {"header":"模板类型","type":"string","width":120,"dataIndex":"bill_templetcaption","xtype":"gridcolumn"}]},
			"pkField":"pk_billtypecode",
			"codeField":"pk_billtypecode",
			"nameField":"pk_billtypecode",
			showCodeOnFocus : true,
			isFocusAfterSubmit : true,
			showCodeOnBlur : true,
			"getByPkUrl":ctxPath +"/ref/common/bt/getByPk.do",
			"getByCodeUrl":ctxPath +"/ref/common/bt/getByCode.do"
		});
		//回车时失去焦点，这样可以执行查询模板的动作
    	this.billtypecodeField.on('specialkey', function(f,e){
			if(e.getKey()==13){
				if(Ext.isGecko){
				}else{
					//其他浏览器的实现,IE
					e.keyCode=9;
            		e.browserEvent.keyCode=9;
				}
			} 
        },this);		
		this.billtypecodeField.on('change',function(field,value,originalValue){
			//加载模板，以及读取主表和子表
		    uft.Utils.doAjax({
		    	scope : this,
		    	params : {pk_billtypecode:value},
		    	url : 'loadTempletData.json',
		    	isTip : false,
		    	success : function(values){
		    		if(values && values.data){
		    			var data = this.templetData = values.data;//缓存一个templetData
		    			if(data.HEADERTABLE){
		    				this.headerTableField.setRecord([data.HEADERTABLE]);
		    			}else{
		    				this.headerTableField.setRecord(null);
		    			}
		    			if(data.BODYTABLE && data.BODYTABLE.length > 0){
		    				this.bodyTableField.setRecord(data.BODYTABLE);
		    			}else{
		    				this.bodyTableField.setRecord(null);
		    			}
		    			if(data.TEMPLET && data.TEMPLET.length > 0){
		    				this.templetField.setRecord(data.TEMPLET);
		    				this.templetField.select(0);//选择第一行
		    			}
		    		}else{
		    			this.templetData = null;
		    			this.headerTableField.setRecord(null);
		    			this.bodyTableField.setRecord(null);
		    			this.templetField.setRecord(null);
		    			this.templetField.select(0);
		    		}
		    		Ext.getCmp('btn_add').enable();
		    	}
		    });	
		},this);
		this.templetField = new Ext.ux.form.MultiSelect({
			height : this.clientHeight,
			width : this.clientWidth/2-this.rightButtonWidth,
			legend : '已定义模板',
			displayField: 'text',
	        valueField: 'value',
			store: new Ext.data.JsonStore({
		         root:'records',
		         fields:['value', 'text']  
		    })
		});
		//当选择一行记录后，启用和禁用按钮
		this.templetField.on('_select',function(field,index){
			var value = this.templetField.getValue();
			var btn_edit = Ext.getCmp('btn_edit');
			var btn_copy = Ext.getCmp('btn_copy');
			var btn_del = Ext.getCmp('btn_del');
			if(value){
				btn_copy.enable();
				btn_edit.enable();
				btn_del.enable();
			}else{
				btn_copy.disable();
				btn_edit.disable();
				btn_del.disable();
			}
		},this);
		var centerPanel = new Ext.Panel({
			region : 'center',
			autoHeight : true,
			layout : 'table',
			layoutConfig : {
				columns : 2
			},
			items : [{
				border : false,
				padding : '3px 0 0 5px',
				height : this.headerFieldHeight,
				layout : 'form',
				labelWidth:150,
				colspan : 2,
				items : [this.billtypecodeField]
			},{
				border : false,
				layout : 'fit',
				items : [this.templetField]
			},{
				border : false,
				padding : '8px 0 0 5px',
				baseCls: 'x-plain',
				xtype : 'buttongroup',
				width : this.rightButtonWidth,
				layoutConfig:{
	        		columns:1
	        	},
	        	items : [{
	        		id : 'btn_add',
		        	xtype : 'button',
		        	width : this.rightButtonWidth-10,
		        	cls : 'x-btn-padd',
		        	text : '新建',
		        	disabled : true,
		        	scope : this,
		        	handler : this.addTemplet
		        },{
		        	id : 'btn_edit',
		        	xtype : 'button',
		        	width : this.rightButtonWidth-10,
		        	cls : 'x-btn-padd',
		        	text : '修改',
		        	disabled : true,
		        	scope : this,
		        	handler : this.editTemplet
		        },{
		        	id : 'btn_copy',
		        	xtype : 'button',
		        	width : this.rightButtonWidth-10,
		        	cls : 'x-btn-padd',
		        	text : '复制',
		        	disabled : true,
		        	scope : this,
		        	handler : this.copyTemplet
		        },{
		        	id : 'btn_del',
		        	xtype : 'button',
		        	width : this.rightButtonWidth-10,
		        	cls : 'x-btn-padd',
		        	text : '删除',
		        	disabled : true,
		        	scope : this,
		        	handler : this.deleteTemplet
		        }]
			}]
		});
		uft.bill.BillTemplet.superclass.constructor.call(this, {
			layout:'border',
			border : false,
			padding : '0 0 2px 0',
			renderTo : document.body,
			height : this.clientHeight,
			items : [leftPanel,centerPanel]	
		});		
	},
	onFilter : function (){
		var store = this.tableField.store;
		var options = {};
		options.params = {keyword:this.filterField.getValue()};
		store.load(options);
	},	
    fromTo : function(fromMultiselect,toMultiselect) {
        var selectionsArray = fromMultiselect.view.getSelectedIndexes();
        var records = [],record;
        if (selectionsArray.length > 0) {
            for (var i=0; i<selectionsArray.length; i++) {
                record = fromMultiselect.view.store.getAt(selectionsArray[i]);
                records.push(record);
            }
            for (var i=0; i<records.length; i++) {
            	var data = records[i].data;
            	var id = data[fromMultiselect.valueField];
            	var r = toMultiselect.view.store.getById(id);
            	if(r){
            		//记录以及存在
            		continue;
            	}
                record = new Ext.data.Record(data,id);//将value设置为id
                toMultiselect.view.store.add(record);
                selectionsArray.push((toMultiselect.view.store.getCount() - 1));
            }
        }
        toMultiselect.view.refresh();
        toMultiselect.view.clearSelections();
        toMultiselect.view.select(selectionsArray);
    },	
	addToHeaderTable : function(){
		var count = this.headerTableField.view.store.getCount();
		if(count > 0){
			uft.Utils.showWarnMsg('不支持多主表！');
			return;
		}
		this.fromTo(this.tableField,this.headerTableField);
	},
	removeFromHeaderTable : function(){
		var selectionsArray = this.headerTableField.view.getSelectedIndexes();
		for (var i=0; i<selectionsArray.length; i++) {
            var record = this.headerTableField.view.store.getAt(selectionsArray[i]);
            this.headerTableField.view.store.remove(record);
        }
	},
	addToBodyTable : function(){
		this.fromTo(this.tableField,this.bodyTableField);
	},
	removeFromBodyTable : function(){
		var selectionsArray = this.bodyTableField.view.getSelectedIndexes();
		for (var i=0; i<selectionsArray.length; i++) {
            var record = this.bodyTableField.view.store.getAt(selectionsArray[i]);
            this.bodyTableField.view.store.remove(record);
        }
	},
	/**
	 * 新建模板
	 */
	addTemplet : function(){
		var td = this.getTempletData();
		var et = new uft.bill.EditTemplet({
			templetData : td
		});
		//新建模板，需要将新建后的模板设置到templetField中
		et.on('close',function(win){
			var desc = win.templetDesc;//模板的描述信息
			var value = desc.pk_billtemplet;
			if(desc && value){
				var text = desc.pk_billtypecode + ' '+desc.bill_templetcaption;
				this.templetField.addRecord([{value:value,text:text}]);
				//更新按钮
				var btn_edit = Ext.getCmp('btn_edit');
				var btn_copy = Ext.getCmp('btn_copy');
				var btn_del = Ext.getCmp('btn_del');
				btn_edit.enable();
				btn_copy.enable();
				btn_del.enable();
			}
		},this);
		et.on('save',function(win,values){
			if(values && values.append){//保存后更新templetData
				this.templetData = values.append;
			}
		},this);
		et.show();
	},
	/**
	 * 编辑模板
	 */
	editTemplet : function(){
		var pk_billtemplet = this.templetField.getValue();
		if(pk_billtemplet){
			var td = this.getTempletData();
			td.pk_billtemplet = pk_billtemplet;
			new uft.bill.EditTemplet({
				templetData : td
			}).show();
		}else{
			uft.Utils.showWarnMsg('请先选择一行记录！')
			return;
		}
	},
	/**
	 * 复制模板
	 */
	copyTemplet : function(){
		var pk_billtemplet = this.templetField.getValue();
		if(pk_billtemplet){
			var td = this.getTempletData();
			var pk_billtypecode = td.pk_billtypecode;
			var templet = td.TEMPLET,bill_templetcaption='',nodecode='';
			for(var i=0;i<templet.length;i++){
				if(pk_billtemplet == templet[i].pk_billtemplet){
					//定位到选中的记录
					bill_templetcaption = templet[i].bill_templetcaption;
					nodecode = templet[i].nodecode;
					break;
				}
			}
			var win = new uft.bill.CopyBillWin({bill_templetcaption:bill_templetcaption,nodecode:nodecode});
        	win.show();
        	win.on('ok',function(win,values){
        		//执行复制模板操作
				uft.Utils.doAjax({
			    	scope : this,
			    	method : 'POST',
			    	url : 'copyBillTemplet.json',
			    	actionType : '复制模板',
			    	params : {pk_billtypecode:pk_billtypecode,pk_billtemplet:pk_billtemplet,bill_templetcaption:values['bill_templetcaption'],nodecode:values['nodecode']},
			    	success : function(values){
			    		if(values){
			    			if(values.data){
			    				var data = values.data,value = data.pk_billtemplet;
				    			var text = data.pk_billtypecode + ' '+data.bill_templetcaption;
								this.templetField.addRecord([{value:value,text:text}]);
			    			}
			    			if(values.append){//重新设置templetData
			    				this.templetData = values.append;
			    			}
			    		}
			    	}
				});
        	},this);
		}else{
			uft.Utils.showWarnMsg('请先选择一行记录！')
			return;
		}
	},
	/**
	 * 删除模板操作
	 */
	deleteTemplet : function(){
		var pk_billtemplet = this.templetField.getValue();
		if(pk_billtemplet){
			Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定要删除模板吗？'), function(btn) {
				if (btn == 'yes') {
					uft.Utils.doAjax({
				    	scope : this,
				    	method : 'GET',
				    	url : 'deleteBillTemplet.json',
				    	actionType : '删除模板',
				    	params : {pk_billtemplet:pk_billtemplet},
				    	success : function(values){
				    		this.templetField.removeRecord([pk_billtemplet]);
				    	}
					});
				}
			},this);
		}else{
			uft.Utils.showWarnMsg('请先选择一行记录！')
			return;
		}
	},
	getTempletData : function(){
		var td = Ext.apply({},this.templetData);
		var ds = this.headerTableField.store;
		if(ds && ds.getCount() > 0){
			td.HEADERTABLE = ds.getAt(0).data;
		}
		var arr = [];
		ds = this.bodyTableField.store;
		if(ds && ds.getCount() > 0){
			for(var i=0;i<ds.getCount();i++){
				arr.push(ds.getAt(i).data);
			}
		}
		td.BODYTABLE = arr;		
		if(!this.templetData){
			td.pk_billtypecode = this.billtypecodeField.getValue();
			td.TEMPLET = [];
			var ds = this.templetField.store,count = ds.getCount();
			for(var i=0;i<count;i++){
				td.TEMPLET.push(ds.getAt(i));
			}
		}
		if(td.pk_billtemplet){
			//该值可能从EditTemplet中来，需要删除该变量，否则会导致其他操作（新增）使用到旧的变量
			delete td.pk_billtemplet;
		}
		this.templetData = td;
		return td;
	}
});