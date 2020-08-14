Ext.ns('uft.extend.form');
/**
 * 参照窗口
 * 默认情况是只包含grid
 * refName : 参照窗口的title
 * params ：格式如aaa=1;bbb=2，不支持直接使用sql语句，这种情况很少出现。
 * extendParams : 扩展参数 //支持{key:value}格式，若使用{_cond:where},可以直接传递sql语句
 * leftTreeExtendParams : 对于左树右表的参照，左边树的扩展参数
 * treeDataUrl : 树的数据加载url 
 *
 * 需要注意的是：grid的第一列为id，可能隐藏，grid的第二列为text，这是为了取值方便。
 * gridDataUrl : grid的数据加载url
 * 
 * treePkField : 树的主键，用于根据选择的树节点查询表格
 * @param {} config
 */
uft.extend.form.RefWindow= Ext.extend(Ext.Window, {
	shadow : false,
	currentNode : null,
	width :650,
	height :400,
	minWidth : 230,
	tree:null,
	grid:null,
	srcField:null, //所属参照域
	byConstructor : false, //是否经过构造函数创建对象，用于模糊查询判断
	isMulti : false,  //是否多选
	isBodyMulti : false, //是否是表体参照的多选,该参数在srcField中，因为脱离了srcField，则该参数没有作用
	allDataTabTitle : '全部数据',
	selectedTabTitle : '已选数据({0}条)',
	isRemoteFilter : false,//是否后台过虑
	constructor : function(config) {
		this.byConstructor = true;
		Ext.apply(this,config);
		
		this.refName = this.srcField.refName;
		//有可能是以-web结尾
		if(this.refName){
			this.refName = this.refName.replace('-web','');
		}
		this.extendParams = this.srcField.extendParams;
		this.leftTreeExtendParams = this.srcField.leftTreeExtendParams;
		
		this.pkField = this.srcField.pkField;
		this.codeField = this.srcField.codeField;
		this.nameField = this.srcField.nameField;
		this.id = this.srcField.refWinId;
		
		this.immediatelyLoad = this.srcField.immediatelyLoad;
		this.isMulti = this.srcField.isMulti;
		this.isBodyMulti = this.srcField.isBodyMulti;
		this.isRemoteFilter = this.srcField.isRemoteFilter;
		
		this.treeOnly = (this.model==0);
		this.hasTree = (this.model==0 || this.model==2);
		this.hasGrid = (this.model==1 || this.model==2);
		if(this.immediatelyLoad == undefined){
			//在参照类中可能会设置grid是否马上加载数据
			this.immediatelyLoad=this.model==1;
		}
		
		this.treeParams = {};
		this.gridParams = {};
		//在创建对象的时候需要解析当前参数
		this.resolveExtendParams();
		
		if(this.hasTree){ //有树，但不一定只有树
			if(this.treeOnly && this.extendParams){
				//只有treeOnly的情况下才将extendParams加入，否则使用leftTreeExtendParams
				for(key in this.extendParams){
					this.treeParams[key] = this.extendParams[key];
				}
			}	
			if(!this.treeOnly && this.leftTreeExtendParams){
				//左树右表的情况
				for(key in this.leftTreeExtendParams){
					this.treeParams[key] = this.leftTreeExtendParams[key];
				}
			}
			if((this.isMulti && this.treeOnly) || (this.isBodyMulti && this.treeOnly)){
				//多选使用checkbox
				this.tree = new uft.extend.tree.CheckboxTree({
					isTreeFilter : this.treeOnly,
					dataUrl : this.treeDataUrl,
					isRemoteFilter : this.isRemoteFilter,
					params : this.treeParams
				});
			}else{
				this.tree = new uft.extend.tree.Tree({
					isTreeFilter : this.treeOnly,
					dataUrl : this.treeDataUrl,
					isRemoteFilter : this.isRemoteFilter,
					params : this.treeParams
				});
			}
			var treePanel = new Ext.Panel({
				layout : 'fit',
				region : this.treeOnly?'center':'west',
				width : this.treeOnly?this.width:this.minWidth,
				frame : false,
				border : true,
				split : true,
				items : [this.tree]
			});
			
			//使用selectionchange代替了
//			this.tree.on('click', function(node, e){
//				
//			}, this);
			this.tree.getSelectionModel().on('selectionchange',function(sm,node){
				if(this.treeOnly){
					this.currentNode = node;
				}else{
					var ds = this.grid.getStore();
					ds.setBaseParam(this.treePkField, node.id);
					//重新设置查询框的值
					if(this.grid.gridSearch){
						//当点击左边的树时，查询的条件不再跟模糊查询框相关
						ds.setBaseParam('GRID_QUERY_KEYWORD',null);
					}
					ds.load();
				}
			},this);
			if(this.treeOnly){ //若只有tree，双击选择该节点，并返回
				if(!this.isMulti){
					this.tree.on('dblclick', function(node, e){
						if(node.leaf == true&&this.currentNode.attributes['code']){
							e.preventDefault();  
							this.currentNode = node;
							this.process();
						}
					}, this);
				}
			}
			
		};
		
		if(this.hasGrid){
			if(this.extendParams){
				for(key in this.extendParams){
					this.gridParams[key] = this.extendParams[key];
				}
			}				
			var _recordType = [];
			var _columns = [];
			if(this.extGridColumnDescns){
				for(var i=0; i<this.extGridColumnDescns.length; i++){
					var extGridColumnDescn = this.extGridColumnDescns[i];
					var sr = "{name: '"+extGridColumnDescn.dataIndex+"', type: '"+extGridColumnDescn.type+"'}";
					_recordType[i] = Ext.decode(sr);
					_columns[i] = extGridColumnDescn;;
				}	
			}
			this.grid = new uft.extend.grid.RefGrid({
				refMultiSelectionModel : this.isMulti || this.isBodyMulti,//使用参照的特殊选择模型
				isCheckboxSelectionModel : this.isMulti || this.isBodyMulti,//是否多选
				singleSelect : (!this.isMulti && !this.isBodyMulti),
				immediatelyLoad : this.immediatelyLoad,
				dataUrl : this.gridDataUrl,
				params : this.gridParams,
		    	recordType : _recordType.slice(0),
				columns : _columns.slice(0)
			});

			var gridPanel = new Ext.Panel({
				layout : 'fit',
				region : 'center',
				frame : false,
				border : false,
				items : [this.grid]
			});
			//选中默认值
			if(this.isMulti || this.isBodyMulti){
				//已选数据的tab
				var selectedGridRecordType = _recordType.slice(0);
				var selectedGridColumns = _columns.slice(0);
				//插入一个操作列
				selectedGridRecordType.unshift({name:'_processor',type:'string'});
				selectedGridColumns.unshift({header:'操作',dataIndex:'_processor',width:30,renderer : this.processorRender});
				this.selectedGrid = new uft.extend.grid.RefGrid({
					isAddBbar : false, //不需要分页
					addGridSearch : false,
					pkFieldName : this.pkField,
			    	recordType : selectedGridRecordType,
					columns : selectedGridColumns
				});
				this.grid.getStore().on('load',this.revertStatus,this);
			}else{
				this.grid.addListener('rowdblclick', this.process, this);
			}
			delete _recordType,_columns; //删除没用的变量		
		};
		
		var items=[];
		if(treePanel)
			items.push(treePanel);
		if(gridPanel)
			items.push(gridPanel);
		var main = new Ext.Panel({
			layout : 'border',
			frame : false,
			border : false,
			items : items
		});		
		if(this.selectedGrid){
			//存在已选表格，此时需要使用tabPanel
			this.tabPanel = new Ext.TabPanel({
				deferredRender : false,
				resizeTabs:false,
				border : false,
				activeTab:0,
			    frame : false
			});
			this.tabPanel.add({
				title : this.allDataTabTitle,
				border : false,
				frame : false,
				layout : 'fit',
				items : [main]
			});				
			//加入已选数据的tab
			this.tabPanel.add({
				title : String.format(this.selectedTabTitle, 0),
				border : false,
				frame : false,
				layout : 'fit',
				items : [this.selectedGrid]
			});
			if(this.grid){
				//如果存在grid，注册grid的选中和未选中事件
				this.registerGridRowEvent(this.grid);
			}
		}
		
		this.submitBtn = new Ext.Button({
			iconCls : 'btnYes',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : this.process
		});
		this.cancelBtn = new Ext.Button({
			iconCls : 'btnCancel',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.hide();
			}
		});
		var btnAry = [];
		if(this.isMulti){
			//多选的情况，多增加一个重置按钮
			btnAry.push(new Ext.Button({
				iconCls : 'btnReset',
				text : '重&nbsp;&nbsp;置',
				scope : this,
				handler : function() {
					this.resetStatus();
				}
			}));
		}
		btnAry.push('->');
		btnAry.push(this.submitBtn);
		btnAry.push(this.cancelBtn);
		uft.extend.form.RefWindow.superclass.constructor.call(this, {
			title : this.refName,
			width : this.width ,
			height : this.height,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [this.tabPanel||main],
			buttonAlign : 'left',
			buttons : btnAry
	    });
	},
	initComponent : function() {
        this.addEvents(
            /**
             * 提交表单前触发
             * @event expand
             * @param {uft.extend.form.RefWindow} 
             */
            'beforesubmit',
            /**
             * 提交表单后触发
             * @event expand
             * @param {uft.extend.form.RefWindow} 
             */
            'submit'
        );
        uft.extend.form.RefWindow.superclass.initComponent.call(this);
    },
	initEvents : function(){
		uft.extend.form.RefWindow.superclass.initEvents.call(this);
        this.keyNav = new Ext.KeyNav(this.el, {
            "down": function(e) {
            	var g = this.grid,t = this.tree;
                //在参照窗口点击向下快捷键时，如果是树，那么选择第一行，如果是表格，那么选中第一个节点
            	if(t){
            		var fn = t.getRootNode().firstChild;
            		if(fn){
            			t.getSelectionModel().select(fn);
            		}
            	}else if(g){
            		var v = g.getView();
            		g.getSelectionModel().selectFirstRow();
            		v.focusRow(0);
            	}
            },
            "enter":function(e){
            	var g = this.grid,t = this.tree;
            	if(t){
            		if(this.treeOnly){
	            		//如果只有树的情况下，那么直接选中这个节点
            			this.process();
	            	}else{
	            		//左树右表，相当于点击这个树节点
	            		var n = t.getSelectionModel().getSelectedNode();
	            		n.fireEvent('click',n);
	            	}
            	}
				if(this.grid){
					this.process();
				}
            },
            scope: this,
            forceKeyDown: true
        });		
	},    
    triggerGridSearch : function(srcFieldValue){
    	if(this.grid.gridSearch.field){
    		if(this.grid.gridSearch.field.getValue() != srcFieldValue){
	    		this.grid.gridSearch.field.setValue(srcFieldValue);
				this.grid.gridSearch.onTriggerSearch();
    		}
			clearInterval(this.intervalID);
    	}
    },
    //已选数据表格的删除行图标
    processorRender : function(value,meta,record){
    	var imgPath = resourceCtxPath+'/theme/'+Constants.csstheme+'/images/default/btn/delete.gif';
    	return "<div align='center'><img id='del_"+record.id+"' src='"+imgPath+"' width='16' border='0' class='h_img' title='删除' /></div>";
    },
    //注册删除小图标的事件
    registerImgClickEvent : function(id){
		var delCmp = Ext.get('del_'+id);
		if(delCmp){
			delCmp.on('click',function(event){
				for(var i=0;i<this.selectedGrid.getStore().getCount();i++){
					var record = this.selectedGrid.getStore().getAt(i);
					if(record.id == id){
						//同步更新其他表格的选中状态
						this.syncGridSelectedStatus(record);
						this.selectedGrid.getStore().remove(record);
						//更新行号信息
						this._updateSelectedGridTabTitle();
						return;
					}
				}
			},this);
		}
    },
    //当删除已选表格的记录时，同步其他表格的选中状态
    syncGridSelectedStatus : function(record){
    	if(this.grid){
    		for(var i=0;i<this.grid.getStore().getCount();i++){
    			var _record = this.grid.getStore().getAt(i);
    			if(_record.get(this.pkField) == record.get(this.pkField)){
    				this.grid.getSelectionModel().deselectRow(i);
    				break;
    			}
    		}
    	}
    },
    //当存在已选数据Tab时，需要注册表格的行选择事件
    registerGridRowEvent : function(grid){
    	grid.getSelectionModel().on('rowselect',this._onGridRowSelect,this);
    	//反选
    	grid.getSelectionModel().on('rowdeselect',this._onGridRowDeselect,this);
    },
    //存在已选数据tab的情况下，选择行的事件
    _onGridRowSelect : function(sm, rowIndex, record){
    	//判断record是否已经存在
    	var ifExist = false;
    	var pk = record.get(this.pkField);
		var store = this.selectedGrid.getStore();
		for(var i=0;i<store.getCount();i++){
			var _curRecord = store.getAt(i);
			if(pk == _curRecord.get(this.pkField)){
				//记录已存在
				ifExist = true;
				break;
			}
		}
		if(!ifExist){
    		store.add(record);
    		this._updateSelectedGridTabTitle();
    		this.registerImgClickEvent(record.id);
		}
    },
    //存在已选数据tab的情况下，反选行的事件
    _onGridRowDeselect : function(sm, rowIndex, record){
    	//判断record是否已经存在
    	var ifExist = false;
    	var pk = record.get(this.pkField);
		var store = this.selectedGrid.getStore();
		for(var i=0;i<store.getCount();i++){
			var _curRecord = store.getAt(i);
			if(pk == _curRecord.get(this.pkField)){
				//记录已存在
				ifExist = true;
				store.remove(_curRecord);
				this._updateSelectedGridTabTitle();
				break;
			}
		}
    },
    //更新已选tab的title
    _updateSelectedGridTabTitle : function(){
    	var tabItems = this.tabPanel.items.items;
    	tabItems[tabItems.length-1].setTitle(String.format(this.selectedTabTitle, this.selectedGrid.getStore().getCount()));
    },    
	//双击或者点击确定按钮
	process : function(){
		if(this.isMulti || this.isBodyMulti){
			//多选
			if(this.treeOnly){
				this.currentNodes = this.tree.getCheckedNodes();
			}else{
				//多选，而不是表体多选，从selectedGrid中读取数据
				this.records = this.selectedGrid.getStore().data.items; //该表格的所有记录
			}
			return this.doProcess();
		}else{
			//单选
			if(this.treeOnly){
				var flag=true;
				if(this.leafflag){
					if(!this.currentNode.leaf){
						flag=false;
					}
				}
				if(this.currentNode&&flag){
					return this.doProcess();
				}
			}else{
				this.record = uft.Utils.getSelectedRecord(this.grid);
				if(this.record){
					return this.doProcess();
				}
			}
		}
	},
	doProcess : function(){
		if(this.fireEvent('beforesubmit',this)!==false){
			this.submitBtn.disable();
			this.setReturnValue();
			this.fireEvent('submit',this);
			if(this.isBodyMulti){
				//若是表体多选，抛出该事件，业务上可以处理整个返回值，但实际上对于参照，只返回第一个值
				this.fireEvent('process',this,this.returnValue);
			}
			if(this.srcField && this.srcField.gridEditor){
				//如果是表体参照，则隐藏，可能需要录入多行
				this.hide();
			}else{
				this.close();
			}
		}
	},
	getReturnValue : function(){
		if(this.isBodyMulti){
			//若是表体多选，此时的事件参数只有一个值
			return this.returnValue[0];
		}
		return this.returnValue;
	},
	//返回值是一个object，格式如：{pk:'111':code:02,name:'a'}
	setReturnValue : function(){
		if(this.isMulti || this.isBodyMulti){
			//多选
			this.returnValue = [];
			if(this.records){
				for(var i=0;i<this.records.length;i++){
					var obj = this.getGridObj(this.records[i]);
					this.returnValue.push(obj);
				}
			}else if(this.currentNodes){
				for(var i=0;i<this.currentNodes.length;i++){
					if(this.currentNodes[i].id == '_root'){
						//过滤根节点，这个节点没有实际的意义
						continue;
					}
					if(this.leafflag){
						//是否只返回叶子节点
						if(!this.currentNodes[i].leaf){
							continue;
						}
					}
					var obj = this.getTreeObj(this.currentNodes[i]);
					this.returnValue.push(obj);
				}
			}
		}else{
			if(this.record){
				this.returnValue = this.getGridObj(this.record);
			}else if(this.currentNode){
				this.returnValue = this.getTreeObj(this.currentNode);
			}
		}
	},
	//private
	getTreeObj : function(node){
		if(!node.attributes['properties']){
			node.attributes['properties'] = {};
		}
		var name = node.attributes['properties'][this.nameField];
		if(!name){
			name=node.attributes['hiddenText']?node.attributes['hiddenText']:node.text;
		}
		var pk = node.attributes['properties'][this.pkField];
		if(!pk){
			pk = node.id;
		}
		var code = node.attributes['properties'][this.codeField];
		if(!code){
			code = node.attributes['code'];
		}
		return {pk:pk,code:code,name:name};
	},
	//private
	getGridObj : function(record){
		var obj = record.data;
		obj['pk'] = record.data[this.pkField];
		obj['code'] = record.data[this.codeField];
		obj['name'] = record.data[this.nameField];
		return obj;
	},
	getGrid : function(){
		this.grid;
	},
	getTree : function(){
		this.tree;
	},
	/**
	 * 解析自定义的参数，或者从其他表单域设置过来的参数
	 * @param {} context
	 * 			识别表达式的上下文环境，对于表体，就是一个record
	 */
	resolveExtendParams : function(){
		//default params aaa=1;bbb=2,因为nc模板的自定义字段3不支持单引号等特殊字符
		//aaa:'1';bbb:'2';ccc:${Ext.getCmp('id').getValue()};_cond:'abc=1'
		if(this.params){
			var arr = this.params.split(";");
			for(var i=0;i<arr.length;i++){
				if(arr[i].indexOf(":")==-1){
					//不是使用冒号分隔
					var arr1=arr[i].split("=");
				}else{
					var arr1=arr[i].split(":");
				}
				this.extendParams[arr1[0]]=uft.Utils.resolveExpression(arr1[1],this.record);
			}
		}
	},
	//比较两个json变量是否完全相等
	compareTo : function(params, newParams){
		for(key in params){
			if(params[key] != newParams[key]){
				return false;
			}
		}
		for(key in newParams){
			if(params[key] != newParams[key]){
				return false;
			}
		}
		return true;
	},
	/**
	 * params是否包含了newParams中的所有元素，是则返回true
	 * @param {} params
	 * @param {} newParams
	 */
	contains : function(params,newParams){
		for(key in newParams){
			if(params[key]!=newParams[key]){
				return false;
			}
		}
		return true;
	},
	//存在已选数据tab的情况下，当加载表格数据的时候需要恢复当前选中记录的状态
	revertStatus : function(){
		if(this.selectedGrid){
			var _store = this.selectedGrid.getStore();
			if(_store&& _store.getCount() > 0){
				if(this.grid){
					this._doRevert(_store,this.grid);
				}
			}
		}
	},
	//私有方法，供revertStatus调用，比较已选表格和其他表格的数据
	_doRevert : function(_store,grid){
		var rows = [];//选中的行
		var store = grid.getStore();
		for(var i=0;i<store.getCount();i++){
			var record = store.getAt(i);
			for(var j=0;j<_store.getCount();j++){
				var _record = _store.getAt(j);
				if(record.get(this.pkField) == _record.get(this.pkField)){
					rows.push(i);
				}
			}
		}
		grid.getSelectionModel().selectRows(rows);
	},
	//重置按钮的方法，必须是多选的时候才能执行该操作
	resetStatus : function(){
		if(!(this.isMulti || this.isBodyMulti)){
			return;
		}
		if(this.treeOnly){
			this.tree.getSelectionModel().clearSelections();//先放弃所有选择
			this.tree.uncheckAll();		
		}else{
			//注意该方法必须在数据加载后才能执行
			this.grid.getSelectionModel().clearSelections();//先放弃所有选择	
		}
		if(this.selectedGrid){
			//如果存在已选数据tab，则清除
			this.selectedGrid.getStore().removeAll();
			this._updateSelectedGridTabTitle();
		}
	},
	/**
	 * 对于表体参照，这个的context就是参照所在的行
	 * @param {} context
	 */
	show : function(record){
		this.record = record; //对于表体，每次打开都需要更新当前的行对象
		this.submitBtn.enable(); //启用按钮
		//参照窗口对象已经创建，但同样需要解析参数，如果参数改变过了，则需要重新加载数据
		this.resolveExtendParams();
		if(Utils.isNotBlank(this.beforeShowScript)){ //如果模板中设置了脚本，则执行该脚本
			eval(this.beforeShowScript);
		}
		if(this.treeOnly){
			var loader = this.tree.getLoader();
			if(!this.contains(this.treeParams,this.extendParams)){
				for(key in this.extendParams){
					this.tree.params[key]=this.extendParams[key];//在Tree.js中的beforeLoad事件中会将params中的参数设置到baseParams中，不要直接操作loader对象
					//当参数发生变化时，将变化的参数加入待比较的参数
					this.treeParams[key] = this.extendParams[key];
				}
				this.tree.reload();
			};
		}else if(this.hasGrid){
			if(this.hasTree){
				//左树右表的情况
				var loader = this.tree.getLoader();
				if(!this.contains(this.treeParams,this.leftTreeExtendParams)){
					for(key in this.leftTreeExtendParams){
						this.tree.params[key]=this.leftTreeExtendParams[key];//在Tree.js中的beforeLoad事件中会将params中的参数设置到baseParams中，不要直接操作loader对象
						//当参数发生变化时，将变化的参数加入待比较的参数
						this.treeParams[key] = this.leftTreeExtendParams[key];
					}
					this.tree.reload();
				};
			}
			var ds = this.grid.getStore();
			if(!this.contains(this.gridParams,this.extendParams)){
				ds.removeAll();//先移除所有数据，考虑到加载数据需要一定的时间
				for(key in this.extendParams){
					ds.baseParams[key] = this.extendParams[key]; //将params中的参数设置到baseParams中
					//当参数发生变化时，将变化的参数加入待比较的参数
					this.gridParams[key] = this.extendParams[key];
				}
				ds.load();
			};
		}
		uft.extend.form.RefWindow.superclass.show.call(this);
		//如果没有值，那么重置下选中值
		if(this.srcField){
			var val = this.srcField.getValue();
			if(!val){
				this.resetStatus();
			}
		}
	},
	/**
	 * 窗口显示后的动作，需要主动调用
	 * @param {} config
	 */
	processAfterShow : function(config){
		if(config && config.keyword){
			//设置查询关键字
			if(this.grid && this.grid.gridSearch){
				this.grid.gridSearch.field.setValue(config.keyword);
				if(this.tabPanel){
					this.tabPanel.setActiveTab(0);
				}
			}
		}
	},	
	close : function(){
		if(this.isMulti){
			//多选的话隐藏
			this.hide();
		}else{
			uft.extend.form.RefWindow.superclass.close.call(this);
		}
	}
});
