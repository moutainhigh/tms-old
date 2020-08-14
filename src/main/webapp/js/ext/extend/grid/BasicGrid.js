Ext.namespace('uft.extend.grid');
/*
 * 参数包括：
 * singleSelect : 是否单选,默认true
 * columns : 表格列模型
 * recordType : 表格记录匹配
 * dataUrl  ： 数据来源，json格式数据
 * params : 参数
 * id :
 * width  grid的宽度
 * height grid的高度
 * renderTo : 表格要渲染的html元素
 * pageSize : 每页记录数，默认10
 * autoExpandColumn : 自动扩充的列，该列会自动填充grid的空白空间
 * immediatelyLoad : 是否渲染表格的时候马上加载数据？true/false, default true
 * isAddNm : 是否增加行号列 ,默认true
 * isAddSm : 是否增加checkbox选择框，默认true
 * remoteSort : 是否使用远程排序，默认是
 */
uft.extend.grid.BasicGrid = Ext.extend(Ext.grid.GridPanel, {
	cls:"uft-toolbar-title-grid",
	pkFieldName:null, //表格数据的主键域，一般是id，否则使用自定义
	rowIndex : null,
	cellIndex : null,
	onstatuschange : null,
	singleSelect:true,
	pageSize:10,
	//是否在生成表格对象的时候就加载数据
	immediatelyLoad:false,
	border:true,
	//数据源是否使用远程排序
	remoteSort:true,
	isAddSummaryPlugin : false, //是否加入合计行插件,只要有一个列存在summaryType时，则加入该插件
	//是否加入查询插件
	isAddSearchPlugin : false,
	//是否加入行数列
	isAddNm : true,
	//是否加入checkbox选择列
	isCheckboxSelectionModel : false,
	//是否加入底部工具栏，通常是分页工具栏
	isAddBbar : true,
	bufferView : false,//是否使用BufferView
	allColumnHide : true, //所有列都是隐藏列
	lockingGridView : false,//是否使用列锁定
	
	timeout : 90000,
	isLoadMask : true,
	enableHdMenu : false,
	enableColumnMove : false, //列不能移动
	trackMouseOver : false, //mouseover事件
	constructor : function (config){
		Ext.apply(this, config);
		//设置超时时间,每页记录数
		if(Constants){
			if(Constants.timeOut){
				this.timeout=Constants.timeOut;
			}
			if(Constants.pageSize&&(!config||!config.pageSize)){
				this.pageSize=Constants.pageSize;
			}
		}
		if(this.isLoadMask){
			this.loadMask = {
				msg : '加载中，请稍候...'
			};
		}
		
		//重新配置columns
		this.initConfig(this.columns);
		this.initPlugins();//初始化插件
		this.initStore();//初始化数据集


		if(!this.bbar){ //若没有定义bbar
		    if(this.isAddBbar){
		    	//加入分页栏
			    var pagingToolbarPlugins = [];
				if(this.isAddPageSizePlugin){
			    	pagingToolbarPlugins.push(new Ext.ux.Andrie.pPageSize());
			    }			    
			    this.bbar = new uft.extend.PagingToolbar({
			    	plugins:pagingToolbarPlugins,
		            pageSize: this.pageSize,
		            store: this.datastore,
		            displayInfo: true
		        });
		    }
	    }
		uft.extend.grid.BasicGrid.superclass.constructor.call(this, {
			id: this.id,
			renderTo: this.renderTo, //将表格渲染至页面的grid-div元素，前提是页面必须存在这个元素
			scope: this,
			autoScroll : true, //为表格增加自动滚动条
		    store: this.datastore,  //设置表格的数据源
		    border : this.border,
		    frame : false,
			loadMask : this.loadMask,
		    cm: this.cm,  //设置表格的列模型
		    sm:this.sm,
		    view : this.view,
		    bbar: this.bbar,
	        plugins:this.plugins,
	        listeners:{
		        'headercontextmenu':function(grid,col,e){//右键点击表头时
		        	var cm = grid.getColumnModel();
		        	var c = cm.getColumnAt(col);
		        	if(c.id == 'numberer' || c.id == 'checker'){
		        		return;
		        	}
		        	var menu = new uft.extend.grid.HeaderContextMenu({grid:grid,col:col});
		        	menu.showAt(e.getXY());
		        	stopDefault(e);//阻止浏览器默认的右键菜单
		        }
		    },
	        scope : this
		});	
	},
	//从后台得到的columns，这里根据一些参数，可能加入checkbox或者rownum等列
	initConfig : function(columns){
		var g = this;
		if(columns){
			g.columns = columns;
		}
		if(!g.sm){
			if(g.isCheckboxSelectionModel){
				//FIXME 传入是否是参照的多行选择模型，这个模型比较特殊,在RefGrid中使用
				g.sm = new Ext.grid.CheckboxSelectionModel({singleSelect :g.singleSelect,refMultiSelectionModel:g.refMultiSelectionModel});
				g.columns.unshift(this.sm);
			}else{
				g.sm = new Ext.grid.RowSelectionModel({singleSelect :g.singleSelect});
			}
		}else{
			if(g.isCheckboxSelectionModel){
				g.columns.unshift(this.sm);
			}
		}
		
		if(g.isAddNm){
			if(!g.rowNumberer){
				g.rowNumberer = new Ext.grid.RowNumberer({width:35}); //行号
			}
			g.columns.unshift(g.rowNumberer);
		}
		
		//只要column中存在summaryType，则加入合计插件
		var lockIndex = -1;//锁定列的标识
		for(var i=0;i<g.columns.length;i++){
			var col = g.columns[i];
			if(col.id == 'numberer'){
				//行号列
				continue;
			}
			if(col.summaryType && col.summaryType!=''){
				g.isAddSummaryPlugin=true;
			}
			if(col.hidden !== true){
				g.allColumnHide = false;
			}
			//如果统一设置了不可排序，那么所有列都不可排序,否则使用列定义的排序方式
			col.sortable = (g.sortable!==false)?true:col.sortable;
			if(col.lockflag){//对应模板中的“是否锁定”
				lockIndex = i;
				g.lockingGridView = true;
			}
		}
		//设置锁定列
		if(g.lockingGridView){
			for(var i=0;i<=lockIndex;i++){
				var col = g.columns[i];
				col.locked = true;
			}
			g.cm = new Ext.ux.grid.LockingColumnModel({columns : g.columns});
			g.view = new Ext.ux.grid.LockingHeaderGroupView();
			//使用了锁定列，不要加入Ext.ux.grid.ColumnHeaderGroup这个插件
			var ps = g.plugins;
			if(ps){
				for(var i=0;i<ps.length;i++){
					var p = ps[i];
					if(p instanceof Ext.ux.grid.ColumnHeaderGroup){
						if(!g.view.grows){
							Ext.apply(g.view,{grows : p.config.rows});
						}
						ps.remove(p);
						i--;
					}
				}
			}
		}else{
			g.cm = new Ext.grid.ColumnModel({columns : g.columns});
			if(g.bufferView){
				g.view = new Ext.ux.grid.BufferView();
			}
		}		
	},
	//初始化插件
	initPlugins : function(){
		var plugins = ['autosizecolumns',this.rowNumberer];//默认加入行号插件，这个插件可以自动更新行号
	    if(this.plugins){
	    	plugins = plugins.concat(this.plugins);
	    	delete this.plugins;
		};	
		
		if(this.isAddSummaryPlugin){
			//加入合计插件
			plugins.push(new Ext.ux.grid.GridSummary());
		}		
	    if(this.isAddSearchPlugin){
			var searchPlugin = new Ext.ux.grid.Search({
				 mode:'remote'
				,iconCls:'btnZoom'
				,minChars:2
				,width: 100
				,minLength:2
				,autoFocus:true
			});
			plugins.push(searchPlugin);
	    }
	    this.plugins = plugins;
	},
	initStore : function(){
		if(this.datastore){//如果外部已经传入了datastore，则不需要创建
			return;
		}
		/* load datastore by JsonReader*/
		var record = Ext.data.Record.create(this.recordType);
		var reader = new Ext.data.JsonReader({totalProperty: "totalRecords",root: "records"},record);
		var httpProxy=new Ext.data.HttpProxy({method :'POST',url:this.dataUrl,timeout:this.timeout,disableCaching:true});
		var baseParams = {start:0,limit:this.pageSize}; 
		if(this.params){
			for(var key in this.params){
				baseParams[key] = this.params[key]; //将params中的参数设置到baseParams中
			}
		};
		
		this.datastore = new uft.extend.data.Store({
			isAddBbar : this.isAddBbar,
			autoLoad:false,
			proxy:httpProxy,
			reader:reader,
			remoteSort:this.remoteSort,
			baseParams:baseParams
		});
		if(this.immediatelyLoad){
	    	this.datastore.load();
		}
	},
	//后台导出
	exportOnBO : function(){
		if (typeof(app) == "undefined") {
			alert("页面没有创建app对象，无法导出！");
			return false;
		}
		var config = {};
		config.funCode=this.params.funCode;
		config.nodeKey=this.params.nodeKey; 
		config.templateID=this.params.templateID;
		config.tabCode=this.params.tabCode;
		config.isBody=Boolean(this.params.isBody);
		if(config.isBody && app.getHeaderGrid()){
			if(this.params.billId && this.params.billId != ''){
				config[app.getHeaderPkField()] = this.params.billId;
			}
			var ids = uft.Utils.getSelectedRecordIds(app.getHeaderGrid(),app.getHeaderPkField());
			if(ids && ids.length > 0){
				config[app.getHeaderPkField()] = ids[0];
			}
		}
		var PUB_PARAMS = this.getStore().baseParams.PUB_PARAMS;
		if(PUB_PARAMS){
			//存在查询参数
			config.PUB_PARAMS = PUB_PARAMS;//这里不需要Ext.encode了，下面的encodeURI已经会处理
		}
		this.doExport(config);
	},	
	//导出
	doExport : function(config){
		var url="exportExcel.do";
		var index = 0;
		for(var key in config){
			if(index == 0){
				url += "?";
			}else{
				url += "&";
			}
			url += key + "=" + config[key];
			index++;
		}
		window.open(encodeURI(encodeURI(url)));
	},	
	initComponent : function(){
	    uft.extend.grid.BasicGrid.superclass.initComponent.call(this);
	    this.addEvents(
			'afterrowselect'
		);
	},
	//返回表格的主键
	getPkFieldName : function(){
		if(this.pkFieldName==undefined)
			return "id";
		return this.pkFieldName;
	},
	//增加表格查询参数
	addParams : function(jsonParam){
		var ds = this.getStore();
		if(jsonParam){
			var key;
			for(key in jsonParam){
				ds.baseParams[key] = jsonParam[key]; //将params中的参数设置到baseParams中
			}
		};
	},
	/**
	 * 增加一条记录
	 * @param data
	 * 			待增加的数据
	 * @param index
	 * 			增加的记录所在行
	 */
	addRecord : function(data,index){
		if(index == undefined)
			index = 0;
		if(!data){
			data = {};
		}
		var recordType = this.getStore().recordType;
        var record = new recordType();
        var key;
		for(key in data){
			record.set(key,data[key]);
		}        
        this.getStore().insert(index, record);
        this.getStore().totalLength+=1;
        return record;
	},
	/**
	 * 增加多条记录
	 * @param {} datas
	 * 			待增加的数据集	
	 * @param {} index
	 * 			从哪一行开始增加
	 */
	addRecords : function(datas,index){
		if(index == undefined)
			index = 0;
		for(var i=0;i<datas.length;i++){
			this.addRecord(datas[i],index);
			index++;
		}
	},
	/**
	 * 更新底部工具栏信息
	 */
	updateTbarInfo : function(){
        var tb=this.bottomToolbar;
        if(tb && tb instanceof uft.extend.PagingToolbar){
        	//自定义pagingToolbar
        	tb.updateTbarInfo();
        }else if(tb&& tb instanceof Ext.PagingToolbar){
        	var pt=tb;
        	var ap = Math.round(pt.cursor/pt.pageSize)+1;
			var d = pt.getPageData();
			pt.afterTextItem.el.dom.innerHTML = String.format(pt.afterPageText, d.pages);
			pt.inputItem.el.dom.value = ap;
			pt.first.setDisabled(ap == 1);
			pt.prev.setDisabled(ap == 1);
			pt.next.setDisabled(ap == d.pages);
			pt.last.setDisabled(ap == d.pages);			
        	pt.updateInfo();
        }
	}
});
