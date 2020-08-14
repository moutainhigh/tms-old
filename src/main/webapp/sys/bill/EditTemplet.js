Ext.namespace('uft.bill');
/**
 * templetDesc是最主要的数据格式，起格式如下
 * {
 * 		pk_billtemplet:'',
 * 		pk_billtypecode:'',
 * 		B : {tabcode:[],tabcode:[]},
 * 		T : {tabcode:{},tabcode:{}},
 * 		headerTabAry : [],
 * 		bodyTabAry : []
 * }
 * 提交
 * @class uft.bill.EditTemplet
 * @extends Ext.Window
 */
uft.bill.EditTemplet = Ext.extend(Ext.Window, {
	pk_billtemplet : null,//模板ID,如果是修改的话，那么该值存在
	pk_billtypecode : null,
	headerTableMultiSelectID : 'b_headerTable',//表格的多选框ID
	headerTableFieldMultiSelectID : 'b_headerTableField',//字段的多选框ID
	panelID : 'editTempletPanel',
	templetDataChange : false,//标记模板数据是否改变，在表格和tab的afteredit事件中标记，如果数据改变的情况下，退出时会提示
	constructor : function(config){
		Ext.apply(this,config);
		/*
		 * 模板数据的对象，如果是修改，那么从后台读取该数据，如果是新增，那么在保存时收集该数据
		 * 包括的属性有B、T、headerTabAry、bodyTabAry
		 * */
		if(!this.templetDesc){
			this.templetDesc = {};
		}
		//表格和字段的对应map，页面缓存
		if(!this.tableFieldMap){
			this.tableFieldMap = {};
		}
		//字段名称和字段长度的map，页面缓存，字段名称以表名开头
		if(!this.fieldLengthMap){
			this.fieldLengthMap = {};
		}
		//将模板的表头信息放入templetDesc统一管理,templetData从其他页面传入，渲染完成则会删除该变量
		this.pk_billtemplet = this.templetData.pk_billtemplet;
		this.pk_billtypecode = this.templetData.pk_billtypecode;
		if(this.pk_billtemplet){
			//如果是修改模板，那么将模板中的主表信息拷贝到templetDesc中
			var TEMPLET = this.templetData.TEMPLET;
			for(var i=0;i<TEMPLET.length;i++){
				if(this.pk_billtemplet == TEMPLET[i].pk_billtemplet){
					//定位到该模板的主表信息
					Ext.apply(this.templetDesc,TEMPLET[i]);
					break;
				}
			}
		}
		
		this.toolbar = new Ext.Toolbar({
			plugins:new Ext.ux.ToolbarKeyMap(),
			items : [{
				text : '保存模板',
				scope : this,
				tooltip : 'Ctrl+S',
				iconCls : 'btnSave',
				keyBinding: {
		            key: 's',
		            ctrl : true
		        },
				handler : this.saveBillTemplet
			},{
				text : '生成查询模板',
				tooltip : 'Ctrl+Q',
				keyBinding: {
		            key: 'q',
		            ctrl : true
		        },
				scope : this,
				handler : this.genQueryTemplet
			},{
				text : '生成报表模板',
				tooltip : 'Ctrl+R',
				keyBinding: {
		            key: 'r',
		            ctrl : true
		        },
				scope : this,
				handler : this.genReportTemplet
			},{
				text : '返回',
				iconCls : 'btnBack',
				scope : this,
				tooltip : 'ESC',
				handler : function(){
					if(this.templetDataChange){
						Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('模板数据已改变，是否保存？'), function(btn) {
							if (btn == 'yes') {
								this.saveBillTemplet();
							}else{
								this.close();
							}
						},this);
					}else{
						this.close();
					}
				}
			}]
		});
		//表头
		this.headerPanel = new Ext.Panel({
			region : 'center',
			layout : 'fit',
			split : true,
			items : []
		});
		//表体
		this.bodyPanel = new Ext.Panel({
			layout : 'fit',
			region : 'south',
			height : 230,
			split : true,
			collapseMode: 'mini',
			items : []
		});
		var centerPanel = new Ext.Panel({
			region : 'center',
			layout : 'border',
			border : false,
			items : [this.headerPanel,this.bodyPanel]
		});
		//表的Tab，用来选择表和表的字段
		this.tableTab = new Ext.TabPanel({
			region : 'north',
			height : 320,
			activeTab : 0,
			items : [{
				title : '表',
				layout : 'fit',
				items : [new Ext.ux.form.MultiSelect({
					id : this.headerTableMultiSelectID,
					displayField: 'text',
			        valueField: 'value',
			        style : 'height:100%;',
           	 		draggable: true,
					store: new Ext.data.JsonStore({
				        root:'records',
				        fields:['value', 'text']
				    })
				})]
			},{
				id : 'fieldTab',
				title : '字段',
				layout : 'fit',
				items : [new Ext.ux.form.MultiSelect({
					id : this.headerTableFieldMultiSelectID,
					displayField: 'text',
			        valueField: 'value',
			        height : 300,
           	 		draggable: true,
					store: new Ext.data.JsonStore({
				        root:'records',
				        fields:['value', 'text']
				    })
				})]
			}]
		});
		//切换表时，根据表名返回所有字段
		this.tableTab.on('tabchange',function(tabPanel,panel){
			if(panel.id == 'fieldTab'){//切换到字段那个页签
				var tableName = Ext.getCmp(this.headerTableMultiSelectID).getValue();
				if(tableName){
					var index = tableName.indexOf(",");//如果多选，那么取第一个
					if( index != -1){
						tableName = tableName.substring(0,index);
					}
					var fields = this.tableFieldMap[tableName];
					if(fields){
						Ext.getCmp(this.headerTableFieldMultiSelectID).setRecord(fields);
					}else{
						//选中了一行，则根据该行去查询表的所有字段
					    var fn = function(values){
					    	if(values && values.append){
				    			Ext.apply(this.fieldLengthMap,values.append);
				    		}
				    		if(values && values.datas){
				    			Ext.getCmp(this.headerTableFieldMultiSelectID).setRecord(values.datas);
								this.tableFieldMap[tableName] = values.datas;
				    		}
				    	};
					    this.loadTableFields(tableName,fn,this);
					}
				}
			}
		},this);
		//页签属性tab
		this.tabPropertiesGrid = new Ext.grid.PropertyGrid({
			fields : [{
				id : 'tabcode',
				header : this.getNotEditHeader('页签编码'),
				editable : false
			},{
				id : 'tabname',
				header : '页签名称'
			},{
				id : 'tabindex',
				header : '显示顺序'
			},{
				id : 'pos',
				header : this.getNotEditHeader('页签显示位置'),
				editable : false,
				renderer : this.comboRenderer,
				editor : new uft.extend.form.LocalCombox({
		            store : new Ext.data.SimpleStore({
		                 fields : ['text', 'value'],
		                 data : [['表头', 0], ['表体', 1], ['表尾', 2]]
		            })
		    	})
			}]
		});
		this.tabPropertiesGrid.on('afteredit',function(e){
			this.templetDataChange = true;
			if(e.record.id == 'tabname'){
				//编辑了"页签名称"
				var grid = e.grid;
				var tabcode = grid.store.getById('tabcode').get('value');
        		var tabname = grid.store.getById('tabname').get('value');
        		//更新缓存中的tab信息
        		this.updateTabinfo(tabcode,'tabname',tabname);
        		//更新tabPanel的title信息
        		var p = Ext.getCmp(tabcode);
        		if(p){
        			p.setTitle(tabname);
        		}
			}else if(e.record.id == 'tabindex'){
				//编辑页签的显示顺序
				var grid = e.grid;
				var tabcode = grid.store.getById('tabcode').get('value');
				var tabindex = grid.store.getById('tabindex').get('value');
				//更新缓存中的tab信息
        		this.updateTabinfo(tabcode,'tabindex',tabindex);
			}
		},this);
		this.tabPropertiesTab = new Ext.TabPanel({
			split : true,
			activeTab : 0,
			items : [{
				title : '页签属性',
				layout : 'fit',
				items : [this.tabPropertiesGrid]
			}]
		});
		
		//页签属性panel
		this.propertyTab = new Ext.Panel({
			region : 'center',
			layout : 'fit',
			border : false,
			items : [this.tabPropertiesTab]
		});
		this.eastPanel = new Ext.Panel({
			region : 'east',
			width : 200,
			border : false,
			split : true,
			layout : 'border',
			collapseMode: 'mini',
			items : [this.tableTab,this.propertyTab]
		});
		var main = new Ext.Panel({
			tbar : this.toolbar,
			layout : 'border',
			border : false,
			items : [centerPanel,this.eastPanel]
		});
		uft.bill.EditTemplet.superclass.constructor.call(this, {
			id : this.panelID,
			title : '模板初始化',
			width : 1000,
			height : 550,
			layout : 'fit',
			shim : true,
			closable : false,
			maximizable : true,
			border : false,
			modal : true,
			items : [main]
		});
	},
	//初始化数据
	afterRender : function(){
		uft.bill.EditTemplet.superclass.afterRender.call(this);
		if(this.templetData){//模板数据，包括表头表名、表体名称、模板名称
			var tableNameAry = [];
			if(this.templetData.HEADERTABLE){
				tableNameAry.push(this.templetData.HEADERTABLE);
			}
			if(this.templetData.BODYTABLE){
				tableNameAry = tableNameAry.concat(this.templetData.BODYTABLE);
			}
			Ext.getCmp(this.headerTableMultiSelectID).setRecord(tableNameAry);
			delete this.templetData;
		}
		this.onAppReady();
	},	
	/**
	 * 页面初始化后调用的函数
	 */
	onAppReady : function(){
		this.initDD();
		//选中第一个表格
		var tf = Ext.getCmp(this.headerTableMultiSelectID);
		tf.view.select(0);
		tf.view.on('contextmenu',function(dv,index,node,e){
			e.preventDefault();
        	var menu = new uft.bill.TableContextMenu({et:this});
            menu.showAt(e.getXY());
		},this);
		var f = Ext.getCmp(this.headerTableFieldMultiSelectID);
		f.on('_set',function(){
			f.view.on('contextmenu',function(dv,index,node,e){
				e.preventDefault();
	        	var menu = new uft.bill.FieldContextMenu({et:this});
	            menu.showAt(e.getXY());
			},this);
		},this);
		//加载模板数据
		this.getTempletMap();
		//加载表头和表体的模板数据
		if(this.pk_billtemplet){
			this.loadTempletDesc();
		}
	},
	getTempletMap : function(){
		var values = Utils.doSyncRequest('getTempletMap.json',{funCode:'t015',tabCode:'nw_billtemplet_b'},'POST');
		if(!values || !values.data){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('No template data,funCode：t015,tabcode:nw_billtemplet_b！');
			}else{
				uft.Utils.showErrorMsg('没有模板数据，funCode：t015,tabcode:nw_billtemplet_b！');
			}
			return false;
		}
		this.R=values.data.records;
		this.C=values.data.columns;
		this.params = values.data.params;
	},
	/**
	 * 加载模板的详细数据
	 */
	loadTempletDesc : function(){
		var params = Ext.apply({},this.params);
		Ext.apply(params,{pk_billtemplet:this.pk_billtemplet});
		//存在模板id，则加载模板信息
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	url : 'loadTempletDesc.json',
	    	isTip : false,
	    	success : function(values){
	    		if(values && values.data){
	    			//这里返回的模板数据需要和原有的templetDesc进行合并
	    			this.templetDesc = Ext.apply(this.templetDesc,values.data);//存储模板的数据
	    			this.buildTemplet();
	    		}
	    	}
	    });
	},
	/**
	 * 根据templetDesc中描述的模板信息，构建模板初始化
	 */
	buildTemplet : function(){
		var d = this.templetDesc;
		var fieldMap = d.B;//字段信息
		var tabMap = d.T;//tab信息
		var headerTabAry = d.headerTabAry;//表头tabcode的集合
		var bodyTabAry = d.bodyTabAry;//表体tabcode的集合
		if(headerTabAry.length > 0){
			//表头多个tab
			var ht = this.createTab();
			for(var i=0;i<headerTabAry.length;i++){//从第一个开始
				var tab = tabMap[headerTabAry[i]];
				var grid = this.buildTempletGrid(true,tab.tabcode);
				grid.tabcode = tab.tabcode;
				grid.title = tab.tabname;
				ht.add(grid);
				grid.addRecords(fieldMap[headerTabAry[i]]);
			}
			//加入tabPanel
			this.headerPanel.add(ht);
			this.headerPanel.doLayout();
			//设置tab信息,默认设置第一个
			this.setTabProperty(tabMap[headerTabAry[0]]);
		}
		
		if(bodyTabAry.length > 0){
			//表体多个tab
			var bt = this.createTab();
			for(var i=0;i<bodyTabAry.length;i++){//从第一个开始
				var tab = tabMap[bodyTabAry[i]];
				var grid = this.buildTempletGrid(false,tab.tabcode);
				grid.tabcode = tab.tabcode;
				grid.title = tab.tabname;
				bt.add(grid);
				grid.addRecords(fieldMap[bodyTabAry[i]]);
			}
			//加入tabPanel
			this.bodyPanel.add(bt);
			this.bodyPanel.doLayout();
		}		
	},
	getNotEditHeader : function(text){
		return '<span class="uft-grid-header-column-not-edit">'+text+'</span>';
	},
	//下拉框在propertyGrid的渲染函数
	comboRenderer : function(val, meta, rec){
		var field = rec.data.field;
		var store = field.editor.store;
		var record = store.query('value', new RegExp('^' + val + '$', "i")).itemAt(0);
		if(record){
			return record.get('text');
		}
		return '';
	},
	loadTableFields : function(tableName,fn,scope){
		//选中了一行，则根据该行去查询表的所有字段
	    uft.Utils.doAjax({
	    	scope : scope,
	    	params : {tableName:tableName},
	    	url : 'loadTableFields.json',
	    	isTip : false,
	    	success : fn
	    });
	},
	/**
	 * 拖动的是否是表格	
	 * @param {} ddSource
	 * @param {} e
	 * @param {} data
	 */
	isTable : function(ddSource){
		if(ddSource.ms && ddSource.ms.id == this.headerTableMultiSelectID){
			//拖动表
			return true;
		}
		return false;
	},
	/**
	 * 拖动的是否是字段
	 * @param {} ddSource
	 * @param {} e
	 * @param {} data
	 * @return {Boolean}
	 */
	isField : function(ddSource){
		if(ddSource.ms && ddSource.ms.id == this.headerTableFieldMultiSelectID){
			//拖动字段
			return true;
		}
		return false;
	},
	buildTempletGrid : function(isHeader,gridId){
		//如果是表体的模板，则位置、表头实际宽度不需要显示
		var R = uft.Utils.clone(this.R);
		var C = uft.Utils.clone(this.C);
		
		for(var i=0;i<C.length;i++){
			if(isHeader === false){
				if(C[i].dataIndex=='metadataproperty'){
					//表体的模板不需要显示实际宽度，因为编辑的就是实际宽度
					C[i].hidden=true;
				}else if(C[i].dataIndex == 'width'){
					R[i].value = 100;
				}
			}
			if(C[i].dataIndex == 'loadformula' || C[i].dataIndex == 'editformula'){
				var editor = C[i].editor;
				editor.tooltip == '请输入';
			}
		}
		var dragDrop = new Ext.ux.dd.GridDragDropRowOrder();
		var grid = new uft.extend.grid.EditorGrid({
			border : false,
			id : gridId,
			pkFieldName : 'pk_billtemplet_b',
			isAddBbar : false,
			sortable : false,
			plugins : [dragDrop],
			sm : new Ext.grid.RowSelectionModel(),
			recordType : R,
			columns : C
		});
		dragDrop.on('afterrowmove',function(target,rowIndex,toRowIndex,rows){
			var store = grid.getStore(),count = store.getCount();
			for(var i=0;i<count;i++){
				var r = store.getAt(i);
				r.set('showorder',i+1);
			}
		},this);
		grid.on('rowcontextmenu',function(grid,rowIndex,e){
			e.preventDefault();//阻止事件的传递
			var record = grid.getStore().getAt(rowIndex);
			grid.getSelectionModel().selectRow(rowIndex);
        	var menu = new uft.bill.RowContextMenu({et:this,grid:grid,record:record});
            menu.showAt(e.getXY());
		},this);
		grid.on('contextmenu',function(e){
			e.preventDefault();
        	var menu = new uft.bill.GridContextMenu({et:this,grid:grid});
            menu.showAt(e.getXY());
		},this);
		grid.on('render',function(c){
			c.body.on('click', function(){
				var tabcode = grid.tabcode;
				var d = this.templetDesc;
				var tabMap = d.T;//tab信息
				if(tabMap){
					var tabInfo = tabMap[tabcode];
					this.setTabProperty(tabInfo);
				}
			},this);
		},this);
		grid.on('afteredit',function(e){
			this.templetDataChange = true;
			if(e.field == 'itemkey'){
				var tp = this.getTabPanel(e.grid);
				//编辑项目主键，检测是否存在
				var flag = this.hasColumn(e.grid,e.value,tp[1],true,e.record.id);
				if(flag){
					uft.Utils.showWarnMsg('项目主键已存在，请换一个！');
					e.record.set('itemkey',null);
					return;
				}
			}else if(e.field == 'datatype'){//编辑数据类型
				if(String(e.value) == '9'){
					//大文本
					e.record.set('reftype','(100,50)');
				}else{
					e.record.set('reftype','');
				}
			}
		},this);
		grid.on('beforeedit',function(e){
			var column = uft.Utils.getColumn(e.grid,e.field);
			if(e.field == 'reftype'){
				var r = e.record;
				var datatype = String(r.get('datatype'));
				if(datatype == '2'){//小数
					if(!(column.editor instanceof uft.bill.DecimaltypeField)){//这里需要判断下，避免出现多次设置的情况，如果多次设置，那么对象不一样，导致值无法设置
						column.setEditor(new uft.bill.DecimaltypeField());
					}
				}else if(datatype == '5'){//参照
					if(!(column.editor instanceof uft.bill.ReftypeField)){
						column.setEditor(new uft.bill.ReftypeField());
					}
				}else if(datatype == '6'){//下拉
					if(!(column.editor instanceof uft.bill.SelecttypeField)){
						column.setEditor(new uft.bill.SelecttypeField());
					}
				}else if(datatype == '9' || datatype == '11'){//大文本,对象
					column.setEditor(new Ext.form.TextField());
				}else{
					return false;
				}
			}
		},this);
		return grid;
	},
	/**
	 * 创建表头或表体的tabPanel
	 * @param {} item
	 * @return {}
	 */
	createTab : function(item){
		var items = [];
		if(item){
			items.push(item);
		}
		var tp = new Ext.TabPanel({
			activeTab : 0,
			border : false,
			items : items
		});
		tp.on('tabchange',function(tp,at){
			if(at && at.tabcode)
				this.changeTabProperty(at.tabcode);
		},this);
		return tp;
	},
	/**
	 * 当tab改变时，相应改变tabGrid的信息
	 */
	changeTabProperty : function(tabcode){
		var d = this.templetDesc;
		var tabMap = d.T;//tab信息
		if(tabMap){
			var tabInfo = tabMap[tabcode];
			this.setTabProperty(tabInfo);
		}
	},
	/**
	 * 删除页签是，销毁页签的相关信息
	 * @param {} tabcode
	 * @param {} isHeader
	 */
	removeTabInfo : function(tabcode,isHeader){
		var td = this.templetDesc;
		if(!td.T){
			return;
		}
		if(!td.DT){
			td.DT = {};
		}
		td.DT[tabcode] = td.T[tabcode];//将删除的页签放入DT中
		td.T[tabcode] = null;
		delete td.T[tabcode];//从当前的tab集合中删除
		if(isHeader){
			if(!td.headerTabAry){
				return;
			}
			td.headerTabAry.remove(tabcode);
		}else{
			if(!td.bodyTabAry){
				return;
			}
			td.bodyTabAry.remove(tabcode);
		}
	},
	/**
	 * 更新缓存中的tab信息，如tabname，tabindex
	 * @param {} tabcode
	 * @param {} field
	 * @param {} value
	 */
	updateTabinfo : function(tabcode,field,value){
		var td = this.templetDesc;
		if(!td.T){
			return;
		}
		var tabInfo = td.T[tabcode];
		if(tabInfo){
			tabInfo[field] = value;
		}
	},
	/**
	 * 点击表头和表体的时候，设置tab信息
	 * @param isHeader 是否表头
	 * @param isSave 是否保存到缓存中
	 */
	setTabProperty : function(tabInfo,isHeader,isSave){
		if(!tabInfo){
			return;
		}
		if(isSave){
			var td = this.templetDesc;
			if(!td.T){
				td.T = {};
			}
			td.T[tabInfo.tabcode] = tabInfo;
			if(isHeader){
				if(!td.headerTabAry){
					td.headerTabAry = [];
				}
				td.headerTabAry.push(tabInfo.tabcode);
				//激活该tab
				var tp = this.headerPanel.items.items[0];
				tp.setActiveTab(tp.items.items.length-1);
			}else{
				if(!td.bodyTabAry){
					td.bodyTabAry = [];
				}
				td.bodyTabAry.push(tabInfo.tabcode);
				//激活该tab
				var tp = this.bodyPanel.items.items[0];
				tp.setActiveTab(tp.items.items.length-1);
			}
		}
		var grid = this.tabPropertiesGrid;
		grid.setValue('tabcode',tabInfo.tabcode);
		grid.setValue('tabname',tabInfo.tabname);
		grid.setValue('tabindex',tabInfo.tabindex);
		grid.setValue('pos',tabInfo.pos);
	},
	/**
	 * 列是否已经存在,注意这里要检查的是每一行
	 * 如果是表头，那么需要检查表头的所有tab
	 * @param {} grid
	 * @param {} id
	 * @param {} itself 是否排除其自身，以recordId来判断是否是其自身
	 * @return {Boolean}
	 */
	hasColumn : function(grid,id,isHeader,itself,recordId){
		if(isHeader){
			//表头存在多个tab，检查表头的所有表格
			var items = this.headerPanel.items.items;
			if(items.length > 0){
				//存在tabPanel
				var grids = items[0].items.items;
				for(var i=0;i<grids.length;i++){
					if(this._checkColumn(grids[i],id,itself,recordId)){
						return true;
					}
				}
			}
		}else{
			return this._checkColumn(grid,id,itself,recordId);
		}
		return false;
	},
	_checkColumn : function(grid,id,itself,recordId){
		var ds = grid.getStore(),count = ds.getCount();
		for(var i=0;i<count;i++){
			var record = ds.getAt(i);
			var itemkey = record.get('itemkey');
			if(itself){
				if(itemkey == id && record.id != recordId){
					return true;
				}
			}else{
				if(itemkey == id){
					return true;
				}
			}
		}
		return false;
	},
	/**
	 * 检查tab是否已经存在
	 * @param {} tabcode
	 * @return {Boolean}
	 */
	hasTab : function(tabcode){
		var td = this.templetDesc;
		if(td && td.headerTabAry){
			for(var i=0;i<td.headerTabAry.length;i++){
				if(td.headerTabAry[i] == tabcode){
					return true;
				}
			}
		}
		if(td && td.bodyTabAry){
			for(var i=0;i<td.bodyTabAry.length;i++){
				if(td.bodyTabAry[i] == tabcode){
					return true;
				}
			}
		}
		return false;
	},
	/**
	 * 使用右键增加新页签时，需要根据grid查询grid所属的tabPanel
	 * @param {} grid
	 * @return {}
	 */
	getTabPanel : function(grid){
		var tp = this.headerPanel.items.items[0];
		if(tp){
			var items = tp.items.items;
			for(var i=0;i<items.length;i++){
				if(items[i].id == grid.id){
					return [tp,true];
				}
			}
		}
		tp = this.bodyPanel.items.items[0];
		if(tp){
			var items = tp.items.items;
			for(var i=0;i<items.length;i++){
				if(items[i].id == grid.id){
					return [tp,false];
				}
			}
		}
		return null;
	},
	/**
	 * 返回新的tab的index
	 * @param {} isHeader
	 * @return {}
	 */
	getNewTabIndex : function(isHeader){
		if(isHeader){
			var tp = this.headerPanel.items.items[0];
			if(!tp){
				return 0;
			}
			return tp.items.items.length;
		}else{
			var tp = this.bodyPanel.items.items[0];
			if(!tp){
				return 0;
			}
			return tp.items.items.length;
		}
	},
	/**
	 * 拖动表格或字段后执行的动作，
	 * 1、如果放到表头和表尾，则生成一个form的一个输入框
	 * 2、如果放到表体，则生成表格的一列	
	 */
	initDD : function(toGrid,isHeader){
		if(!toGrid){
			//定义拖拽到表头panel的对象
			new Ext.dd.DropTarget(this.headerPanel.body.dom, {
				ddGroup     : 'MultiselectDD',
				et : this,
				notifyDrop  : function(ddSource, e, data){
					var et = this.et;
					et.dropTo(ddSource, e, data,null,true);
					return(true);
				}
			});
			//定义拖拽到表体panel的对象
			new Ext.dd.DropTarget(this.bodyPanel.body.dom, {
				ddGroup     : 'MultiselectDD',
				et : this,
				notifyDrop  : function(ddSource, e, data){
					var et = this.et;
					et.dropTo(ddSource, e, data,null,false);
					return(true);
				}
			});
		}else{
			//定义拖拽到表格的对象,外围的panel中已经定义了，不需要再定义表格的了
//			new Ext.dd.DropTarget(toGrid.getEl(), {
//				ddGroup     : 'MultiselectDD_grid',
//				et : this,
//				notifyDrop  : function(ddSource, e, data){
//					var et = this.et;
//					et.dropTo(ddSource, e, data,toGrid,isHeader);
//					return(true);
//				}
//			});
		}
	},
	/**
	 * 拖动到表头
	 */
	dropTo : function(ddSource, e, data,toGrid,isHeader){
		if(this.isTable(ddSource)){
			//拖动表格,不支持一次拖动多个表格
			var records = data.records;
			var tableName = records[0].get('value');
			this.addTableTo(toGrid,tableName,isHeader);
		}else if(this.isField(ddSource)){
			var tableName = Ext.getCmp(this.headerTableMultiSelectID).getValue(); 
			//拖动字段
			var records = data.records;
			var fields = [];
			for(var i=0;i<records.length;i++){
				fields.push(records[i].data);
			}			
			this.addFieldTo(toGrid,tableName,fields,isHeader);
		}
	},	
	/**
	 * 添加表格
	 */
	addTableTo : function(toGrid,tableName,isHeader,newTab,tabInfo){
		var fields = this.tableFieldMap[tableName];
		if(fields){
			this.buildField(toGrid,tableName,fields,isHeader,newTab,tabInfo);
		}else{
			var fn = function(values){
				if(values && values.append){
	    			Ext.apply(this.fieldLengthMap,values.append);
	    		}
				if(values && values.datas){
					this.buildField(toGrid,tableName,values.datas,isHeader,newTab,tabInfo);
					this.tableFieldMap[tableName] = values.datas;
				}
			}
			this.loadTableFields(tableName,fn,this);
		}
	},
	/**
	 * 添加字段
	 */
	addFieldTo : function(toGrid,tableName,fields,isHeader,newTab,tabInfo){
		this.buildField(toGrid,tableName,fields,isHeader,newTab,tabInfo);
	},
	/**
	 * 往form中添加默认的输入框
	 * @param {} toGrid 拖放到的表格
	 * @param {} tableName 字段的表名称
	 * @param {} fields 一个对象，包括text，value，或者一个数组，包括一些对象
	 * @param {} isHeader 是否是表头
	 * @param {} newTab 是否一定要放在新页签上
	 * @param {} tabInfo 如果是新页签，那么必须传入新页签的信息
	 */
	buildField : function(toGrid,tableName,fields,isHeader,newTab,tabInfo){
		if(!toGrid){
			if(newTab === true){
				toGrid = this.buildTempletGrid(isHeader,tabInfo.tabcode);
				toGrid.tabcode = tabInfo.tabcode;
				toGrid.title = tabInfo.tabname;
				var tp;//tabPanel
				if(isHeader){
					var items = this.headerPanel.items.items;//是tabPanel级别
					if(items.length > 0){
						tp = items[0];
						tp.add(toGrid);
					}else{
						tp = this.createTab(toGrid);
						this.headerPanel.add(tp);
					}
					this.headerPanel.doLayout(true);
				}else{
					var items = this.bodyPanel.items.items;//是tabPanel级别
					if(items.length > 0){
						tp = items[0];
						tp.add(toGrid);
					}else{
						tp = this.createTab(toGrid);
						this.bodyPanel.add(tp);
					}
					this.bodyPanel.doLayout(true);
				}
				this.setTabProperty(tabInfo,isHeader,true);
				this.initDD(toGrid,isHeader);
			}else{
				//查询当前是否存在表格，如果存在，那么使用这个表格，否则新建一个
				if(isHeader){
					var items = this.headerPanel.items.items;//是tabPanel级别
					if(items.length > 0){
						//已经存在,则使用这个
						toGrid = items[0].getActiveTab();//grid级别
					}
				}else{
					var items = this.bodyPanel.items.items;
					if(items.length > 0){
						//已经存在,则使用这个
						toGrid = items[0].getActiveTab();
					}
				}
				if(!toGrid){
					var tabcode = tableName;
					if(this.hasTab(tabcode)){
						tabcode = 'new';
					}
					toGrid = this.buildTempletGrid(isHeader,tabcode);
					var tabInfo = {};
					tabInfo.tabcode = tabcode;
					tabInfo.tabname = tabcode;
					tabInfo.pos = isHeader?0:1;
					tabInfo.tabindex = 0;
					
					toGrid.tabcode = tabcode;
					toGrid.title = tabcode;
					var tabPanel = this.createTab(toGrid);
					if(isHeader){
						this.headerPanel.add(tabPanel);
						this.headerPanel.doLayout();
					}else{
						this.bodyPanel.add(tabPanel);
						this.bodyPanel.doLayout();
					}
					this.setTabProperty(tabInfo,isHeader,true);
					this.initDD(toGrid,isHeader);
				}
			}
		}
		if(!Ext.isArray(fields)){
			//转成数组
			fields = [fields];
		}
		var tabcode = toGrid.tabcode;
		var tabname = toGrid.title;
		var order = toGrid.getStore().getCount();
		for(var i=0;i<fields.length;i++){
			var itemkey = fields[i].value;
			if(this.hasColumn(toGrid,itemkey,isHeader)){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Item['+itemkey+'],Already exist!');
				}else{
					uft.Utils.showWarnMsg('项目['+itemkey+'],已存在！');
				}
				
			}else{
				var showorder = order + i + 1;
				if(fields[i].data){//移动行的情况,实际上将行复制了
					Ext.apply(fields[i].data,{showorder:showorder});
					toGrid.addRow(fields[i].data);
				}else{
					var length = this.fieldLengthMap[tableName+"_"+itemkey];
					//dr，ts的数据类型
					var dv = {itemkey:itemkey,
									defaultshowname:itemkey,
									inputlength:length,
									showorder:showorder,
									table_code:tabcode,
									table_name:tabname,
									pos : isHeader?0:1};
					if(itemkey == 'dr' || itemkey == 'ts'){
						dv['showflag'] = 0;
						dv['listshowflag'] = 'N';
						dv['editflag'] = 0;
						if(itemkey == 'dr'){
							dv['datatype'] = 1;
						}else if(itemkey == 'ts'){
							dv['datatype'] = 15;
						}
					}
					toGrid.addRow(dv);
				}
			}
		}
	},
	/**
	 * 增加自定义字段到现有页签
	 * @param {} grid
	 */
	addNullField : function(grid){
		var tp = this.getTabPanel(grid);
    	var s = grid.getStore();
    	var dv = {showorder:s.getCount()+1,table_code:grid.tabcode,table_name:grid.tabname};
    	dv.pos = tp[1]?0:1;
    	grid.addRow(dv);
	},
	/**
	 * 增加自定义字段到新页签
	 */
	addNullFieldToNewTab : function(grid,tabInfo){
		var tp = this.getTabPanel(grid);
		var tabindex = tp[0].items.items.length;
		tabInfo.tabindex = tabindex;
		tabInfo.pos = tp[1]?0:1;
		var newGrid = this.buildTempletGrid(tp[1],tabInfo.tabcode);
		newGrid.title = tabInfo.tabname;
		newGrid.tabcode = tabInfo.tabcode;
		tp[0].add(newGrid);
		tp[0].doLayout();
		tp[0].setActiveTab(newGrid);
		var dv = {showorder:1,table_code:tabInfo.tabcode,table_name:tabInfo.tabname};
		dv.pos = tp[1]?0:1;
    	newGrid.addRow(dv);
    	this.setTabProperty(tabInfo,tp[1],true);
	},
	/**
	 * 项目重新排序
	 * @param {} grid
	 * @param {} itemkeyAry
	 */
	reOrderField : function(grid,itemkeyAry){
		if(itemkeyAry && itemkeyAry.length > 0){
    		var recordMap = {};
    		var ds = grid.getStore(),count = ds.getCount();
    		for(var i=0;i<count;i++){
    			var record = ds.getAt(i);
    			var itemkey = record.get('itemkey');
    			recordMap[itemkey] = record;
    		}
    		ds.removeAll(true);
    		ds.modified = [];//重新排序的时候会重置所有的modified，这里先清空
    		var records = [];
    		for(var i=0;i<itemkeyAry.length;i++){
    			var record = recordMap[itemkeyAry[i]];
    			record.set('showorder',i+1);
    			records.push(record);
    		}
    		ds.add(records);
		}		
	},
	/**
	 * 移动项目到新页签
	 */
	moveFieldToNewTab : function(grid,record,values){
		var field = {};
		field.value = record.get('itemkey');
		field.text = record.get('defaultshowname');
		field.data = record.data;
		//删除原有的
		grid.getStore().remove(record);
		
		var tableName = Ext.getCmp(this.headerTableMultiSelectID).getValue();
		values.pos = 0;
		values.tabindex=this.getNewTabIndex(true);
		var tp = this.getTabPanel(grid);
		this.addFieldTo(null,tableName,[field],tp[1],true,values);		
	},
	/**
	 * 将字段移动到选择的tab中，表头只能移动到表头，表体只能移动到表体
	 * @param {} grid
	 * @param {} record
	 * @param {} tabcode
	 */
	moveFieldTo : function(grid,record,tabcode){
		var field = {};
		field.value = record.get('itemkey');
		field.text = record.get('defaultshowname');
		field.data = record.data;
		field.data.table_code=tabcode;
		var d = this.templetDesc;
		var tabMap = d.T;//tab信息
		if(tabMap){
			var tabInfo = tabMap[tabcode];
			field.data.table_name=tabInfo.tabname;
		}
		//删除原有的
		grid.getStore().remove(record);
		
		var toGrid;
		var tp = this.getTabPanel(grid);
		var tabs = tp[0].items.items;
		for(var i=0;i<tabs.length;i++){
			if(tabs[i].tabcode == tabcode){
				toGrid = tabs[i];
				break;
			}
		}
		this.addFieldTo(toGrid,null,[field],tp[1]);
	},
	/**
	 * 返回grid所在的表头或者表体的所有tab
	 * @param {} grid
	 * @return {}
	 */
	getTabs : function(grid){
		var tp = this.getTabPanel(grid);
		return tp[0].items.items;
	},
	/**
	 * 删除页签
	 */
	deleteTab : function(grid){
		var tabcode = grid.tabcode;
    	var tp = this.getTabPanel(grid);
    	this.removeTabInfo(tabcode,tp[1]);
    	var items = tp[0].items.items;
    	if(items.length == 1){
    		//只有一个tab，那么连同整个tabPanel也删除
    		if(tp[1]){
    			this.headerPanel.remove(tp[0]);
    			this.headerPanel.doLayout();
    		}else{
    			this.bodyPanel.remove(tp[0]);
    			this.bodyPanel.doLayout();
    		}
    	}else{
    		tp[0].remove(grid,true);
    		tp[0].doLayout();
    	}		
	},
	//保存模板数据
	saveBillTemplet : function(){
		var win = new uft.bill.SaveBillWin({et:Ext.getCmp(this.panelID)});
		win.on('ok',function(win,values){
			var appPostData = this.getAppParams(values);
			if(appPostData !== false){
				var params = {};
				params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
				params['pk_billtypecode'] = this.pk_billtypecode;
				uft.Utils.doAjax({
			    	scope : this,
			    	url : 'saveBillTemplet.json',
			    	actionType : '保存',
			    	params : params,
			    	success : function(values){
			    		this.templetDataChange = false;
						//保存后返回的是templetDesc的信息，需要将其中的B信息更新到对应的界面上
			    		if(values){
			    			if(values.data){
				    			var grids = this.getAllGridAry();
				    			var td = this.templetDesc = values.data;
				    			this.pk_billtemplet = td.pk_billtemplet;
				    			this.pk_billtypecode = td.pk_billtypecode;
				    			var B = td.B,key;
				    			for(key in B){//key is tabcode
				    				for(var i=0;i<grids.length;i++){
				    					if(key == grids[i].tabcode){//待更新的表格
				    						//更新数据
				    						this.updateGrid(grids[i],B[key]);
				    					}
				    				}
				    			}
			    			}
			    		}
			    		this.fireEvent('save',this,values);
			    	}
			    });
			}
		},this);
		win.show();
	},
	/**
	 * 将valueAry中的数据更新到grid中,valueAry的数据格式如：
	 * [{pk_billtemplet_b:'1'},{pk_billtemplet_b:'2'}]
	 * @param {} newRecords
	 * @param {} grid
	 */
	updateGrid : function(grid,valueAry){
		var ds = grid.getStore(),count = ds.getCount();
		for(var i=0;i<valueAry.length;i++){
			var valueObj = valueAry[i];
			for(var j=0;j<count;j++){
				var record = ds.getAt(j);
				if(record.get('itemkey') == valueObj['itemkey']){//待更新的行
					record.beginEdit();
					var key;
					for(key in valueObj){
						record.set(key,valueObj[key]);
					}
					record.endEdit();
					break;
				}
			}
		}
	},
	/**
	 * 返回所有表格的集合
	 */
	getAllGridAry : function(){
		var grids = [];
		var items = this.headerPanel.items.items;
		if(items && items.length > 0){
			//表头存在模板数据
			grids = grids.concat(items[0].items.items);
		}
		//表体的表格修改过的数据
		items = this.bodyPanel.items.items;
		if(items && items.length > 0){
			//表头存在模板数据
			grids = grids.concat(items[0].items.items);
		}
		return grids;
	},
	/**
	 * 收集保存模板所需要的信息
	 * @param {} values
	 * @return {Boolean}
	 */
	getAppParams : function(values){
		var appPostData = {};
		//收集表头的信息
		var HEADER = {};
		HEADER.pk_billtypecode = this.pk_billtypecode;//单据模板类型
		HEADER.pk_billtemplet = this.pk_billtemplet;//单据模板PK
		HEADER.bill_templetcaption = this.templetDesc.bill_templetcaption= values.bill_templetcaption;//模板标题
		HEADER.ts = this.templetDesc.ts;//用于模板校验
		HEADER.nodecode = this.templetDesc.nodecode = values.nodecode;//节点号
		appPostData[uft.jf.Constants.HEADER]=HEADER;
		
		var grids = this.getAllGridAry();//所有表格，模板编辑器中，每个tab都是一个表格
		if(grids.length == 0){
			uft.Utils.showWarnMsg('没有模板数据，不能保存！');
			return false;
		}
		//模板field信息
		var BODY = {};
		var data = {'delete' : [],'update':[]};
		for(var i = 0; i<grids.length; i++) {
			if(!grids[i].isValid()) {//这里使用第三方插件进行验证
				errors = grids[i].getAllErrors();
				uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				return false;
			}
			var newData = grids[i].getModifyValue();
			data['delete'] = data['delete'].concat(newData['delete']);
			data['update'] = data['update'].concat(newData['update']);
		}
		BODY['nw_billtemplet_b'] = data;
		//模板tab信息,post所有tab信息，不区分是否修改
		var d = this.templetDesc,tabMap = d.T,dt = d.DT,tAry=[],dtAry=[],key;
		for(key in tabMap){
			tAry.push(tabMap[key]);
		}
		for(key in dt){
			dtAry.push(dt[key]);
		}
		BODY['nw_billtemplet_t'] = {'delete':dtAry,'update':tAry};
		appPostData[uft.jf.Constants.BODY]=BODY;
		return appPostData;
	},
	/**
	 * 返回表格中修改过的数据
	 * @param {} grids
	 * @return {Boolean}
	 */
	getGridModifyData : function(grids){
		var data = {};
		for(var i = 0; i<grids.length; i++) {
			if(!grids[i].isValid()) {//这里使用第三方插件进行验证
				errors = grids[i].getAllErrors();
				uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				return false;
			}
			data[grids[i].tabcode] = grids[i].getModifyValue();
		}
		return data;
	},
	/**
	 * 生成查询模板
	 */
	genQueryTemplet : function(){
		if(!this.pk_billtemplet){
			uft.Utils.showWarnMsg('请先保存模板！');
			return false;
		}
		var win = new uft.bill.BuildQueryTempletWin();
		win.on('ok',function(win,values){
			uft.Utils.doAjax({
		    	scope : this,
		    	url : 'buildQueryTemplet.json',
		    	actionType : '生成查询模板',
		    	params : Ext.apply(values,{pk_billtemplet:this.pk_billtemplet}),
		    	success : function(values){
					
		    	}
		    });
		},this);
		win.show();
	},
	/**
	 * 生成报表模板
	 * @return {Boolean}
	 */
	genReportTemplet : function(){
		if(!this.pk_billtemplet){
			uft.Utils.showWarnMsg('请先保存模板！');
			return false;
		}
		var win = new uft.bill.BuildReportTempletWin();
		win.on('ok',function(win,values){
			uft.Utils.doAjax({
		    	scope : this,
		    	url : 'buildReportTemplet.json',
		    	actionType : '生成报表模板',
		    	params : Ext.apply(values,{pk_billtemplet:this.pk_billtemplet}),
		    	success : function(values){
					
		    	}
		    });
		},this);
		win.show();
	}
});
