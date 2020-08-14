Ext.namespace('uft.extend.grid');
/*
 * 返回一个EditorGridPanel对象
 * id 该grid的id
 * renderTo 该grid所要渲染的html对象
 * width  grid的宽度
 * height grid的高度
 * dataUrl  grid的数据url
 * params : 参数
 * columns 列模型
 * recordType 记录
 * pageSize : 每页记录数，默认10
 * autoExpandColumn : 自动扩充的列，该列会自动填充grid的空白空间
 * isAddNm : 是否增加行号列 ,默认true
 * isAddSm : 是否增加checkbox选择框，默认true
 * remoteSort : 是否使用远程排序，默认是
 */
uft.extend.grid.EditorGrid = Ext.extend(Ext.grid.EditorGridPanel, {
	pkFieldName:null, //表格数据的主键域，一般是id，否则使用自定义
	bakUp : null,//存储字段的disabled状态
	editable : false,//设置整个grid是否可编辑。是一个标记，在其他地方需要引用到
	index : 0,
	currentRecord : null,
	onstatuschange : null,
	singleSelect : true,
	pageSize:10,
	//是否在生成表格对象的时候就加载数据
	//考虑到该参数可能从freeMarker的模板文件中传入，而模板中的变量不支持boolean
	immediatelyLoad:false,
	border:true,
	//数据源是否使用远程排序
	remoteSort:false,
	isAddSummaryPlugin : false, //是否加入合计行插件,只要有一个列存在summaryType时，则加入该插件
	isAddNm : true,
	//是否加入checkbox选择列
	isCheckboxSelectionModel : false,
	//对于加入checkbox的表格，是否只能单选
	isCheckboxSingleSelect : false,
	//是否加入底部工具栏，通常是分页工具栏
	isAddBbar : true,
	dragDropRowOrder : false, //使用拖放来调整行顺序
	bufferView : false,//是否使用BufferView
	allColumnHide : true, //所有列都是隐藏列
	lockingGridView : false,//是否使用列锁定
	
	isLoadMask : true, 
	timeout : 90000,
	enableHdMenu : false,
	enableColumnMove : false, //列不能移动
	newRowWhenWalkInLastCell : true, //在最后一个单元格中点击Enter时，是否自动增加一行
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
		
		//加入一个引用，便于调用
		this.originalColumns = this.columns;
		var sm;
		if(this.isCheckboxSelectionModel){
			sm = new Ext.grid.CheckboxSelectionModel({singleSelect :this.singleSelect,refMultiSelectionModel:this.refMultiSelectionModel});
			this.columns.unshift(sm);
		}else{
			if(!config.sm){
				if(this.dragDropRowOrder){//如果启用拖放调整行顺序，需要使用行选择模式
					this.clicksToEdit=1;//单击进入编辑
					sm = new Ext.grid.RowSelectionModel();
				}else{
					sm = new uft.extend.grid.CellSelectionModel({
						singleSelect :this.singleSelect,
						disabled:true,
						newRowWhenWalkInLastCell:this.newRowWhenWalkInLastCell});
				}
			}else{
				sm = config.sm;
			}
		}
		if(this.isAddNm){
			if(!this.rowNumberer){
				this.rowNumberer = new Ext.grid.RowNumberer({disabled:true,editable:false,width:35}); //行号,不能编辑
			}
			this.columns.unshift(this.rowNumberer);
		}
		//只要column中存在summaryType，则加入合计插件
		var lockIndex = -1;//锁定列的标识
		for(var i=0;i<this.columns.length;i++){
			var col = this.columns[i];
			if(col.id == 'numberer'){
				//行号列
				continue;
			}
			if(col.summaryType && col.summaryType!=''){
				this.isAddSummaryPlugin=true;
			}
			if(col.hidden !== true){
				this.allColumnHide = false;
			}
			//如果统一设置了不可排序，那么所有列都不可排序,否则使用列定义的排序方式
			col.sortable = (this.sortable!==false)?true:col.sortable;
			if(col.lockflag){//对应模板中的“是否锁定”
				lockIndex = i;
				this.lockingGridView = true;
			}
		}
		//设置锁定列
		if(this.lockingGridView){
			for(var i=0;i<=lockIndex;i++){
				var col = this.columns[i];
				col.locked = true;
			}
			cm = new Ext.ux.grid.LockingColumnModel({columns : this.columns});
			this.view = new Ext.ux.grid.LockingHeaderGroupView();
			//使用了锁定列，不要加入Ext.ux.grid.ColumnHeaderGroup这个插件
			var ps = this.plugins;
			if(ps){
				for(var i=0;i<ps.length;i++){
					var p = ps[i];
					if(p instanceof Ext.ux.grid.ColumnHeaderGroup){
						if(!this.view.grows){
							Ext.apply(this.view,{grows : p.config.rows});
						}
						ps.remove(p);
						i--;
					}
				}
			}
		}else{
			cm = new Ext.grid.ColumnModel({columns : this.columns});
			if(this.bufferView){
				this.view = new Ext.ux.grid.BufferView();
			}
		}	
		
	    var record = Ext.data.Record.create(this.recordType);
		var reader = new Ext.data.JsonReader({totalProperty: "totalRecords",root: "records"},record);
		var httpProxy=new Ext.data.HttpProxy({method :'POST',url:this.dataUrl,timeout:this.timeout,disableCaching:true});
		var baseParams = {start:0,limit:this.pageSize}; 
		if(this.params){
			for(var key in this.params){
				baseParams[key] = this.params[key]; //将params中的参数设置到baseParams中
			}
		};
		
		var datastore = new uft.extend.data.Store({
			pkFieldName : this.pkFieldName,
			isAddBbar : this.isAddBbar,
			autoLoad:false,
			proxy:httpProxy,
			reader:reader,
			remoteSort:this.remoteSort,
			baseParams:baseParams
		});
		if(this.immediatelyLoad){
	    	datastore.load();
		}
	    datastore.addListener('load',function(){
	    	//当重新加载数据后，当前选择行清空
	    	this.currentRecord=null;
	    },this);	
	    
		//插件
		var plugins=['autosizecolumns',this.rowNumberer];//默认加入行号插件，这个插件可以自动更新行号
	    if(this.plugins){
	    	plugins = plugins.concat(this.plugins);
	    	delete this.plugins;
		};	
		plugins.push(new Ext.ux.plugins.GridValidator());//加入验证插件
		
		if(this.isAddSummaryPlugin){
			//加入合计插件
			plugins.push(new Ext.ux.grid.GridSummary());
		}
		if(this.dragDropRowOrder){
			plugins.push(new Ext.ux.dd.GridDragDropRowOrder());
		}
	    	
	    if(!this.bbar){//若没有定义bbar
		    if(this.isAddBbar){
			    var pagingToolbarPlugins = [];
			    this.bbar = new uft.extend.PagingToolbar({
		        	plugins:pagingToolbarPlugins,
		            pageSize: this.pageSize,
		            store: datastore,
		            displayInfo: true
		        });
		    }
	    }

	    uft.extend.grid.EditorGrid.superclass.constructor.call(this,{
	    	id : this.id,
	    	renderTo : this.renderTo,
	        store: datastore,
	        cm: cm,
	        sm: sm,
		    border : this.border,
		    frame : false,
	        autoScroll : true, //为表格增加自动滚动条
			loadMask : this.loadMask,
	        bbar: this.bbar,
	        plugins:plugins,
	        scope : this,
	        listeners:{
		        "beforeedit":function(e){
					//当column定义了disabled：true时
					if(this.originalColumns[e.column].editable===false){
						return false;
					}
		        },
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
		    }
	    });
		if(this.dragDropRowOrder){//加入一个友好性提示
			this.on('render', function(){
	        	this.tip = new Ext.ToolTip({
		          view: this.getView(),
		          target: this.getView().mainBody,
		          delegate: '.x-grid3-row',
		          trackMouse: true,
		          renderTo:document.body,
		          listeners: {
	              	beforeshow: function updateTipBody(tip) {
	                   tip.body.dom.innerHTML = '拖动行来调整顺序...';
	                }
		          }
	           });
			},this);
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
	//返回选中行
	getSelectedRow : function(){
		var selectionModel = this.getSelectionModel();
		if(typeof(selectionModel.getSelectedCell) == 'function'){
			//单元格选择模式
			var cell=selectionModel.getSelectedCell();
			if(cell){
				this.currentRecord=this.getStore().getAt(cell[0]);
			}
		}else{
			//行选择模式
			this.currentRecord=selectionModel.getSelected();
		}
		return this.currentRecord;
	},
	setSelectedRow : function(record){
		this.currentRecord = record;
	},
	//返回表格的主键
	getPkFieldName : function(){
		if(this.pkFieldName==undefined)
			return "id";
		return this.pkFieldName;
	},
	/**
	 * 恢复初始化状态,即将store中的modified和removed清空
	 * 目前发现修改后的记录并没有放在modified中，并且我们支持标记删除，与Ext默认的不一致
	 * @deprecated
	 */
	restoreInit : function(){
		this.getStore().rejectChanges();
	},
	addParams : function(jsonParam){
		var ds = this.getStore();
		if(jsonParam){
			for(key in jsonParam){
				ds.baseParams[key] = jsonParam[key]; //将params中的参数设置到baseParams中
			}
		};
	},	
	isExist:function(arr, data){
		for(var i=0; i<arr.length; i++){
			if(data == arr[i]){
				return true;
			}
		}
		return false;
	},
	/**
	 * 返回值是个对象
	 * 这里稍微修改了下store.js，当重新加载数据后，清空modified
	 * 改变的记录和删除的记录统一从modified中读取，如果其store变量为null，表示删除的记录。
	 * @return {}
	 */
	getModifyValue : function(){
		var toUpt = new Array();
		var toDel = new Array();
		var modified = this.getStore().modified;
		if(modified){
			for(var i=0;i<modified.length;i++){
				//格式化日期
	    		for(var key in modified[i].data){
	    			if(modified[i].data[key] instanceof Date){
	    				//目前系统支持日期+时间格式的控件，可能会有不同的日期格式了，不能定死了
	    				var column = uft.Utils.getColumn(this,key);
	    				var value = modified[i].data[key].dateFormat(column.format||'Y-m-d');
	    				modified[i].set(key,value);
	    			}
	    		}
				if(modified[i].store){
					//修改的记录
					toUpt.push(modified[i].data);
				}else{
					//删除的记录，但是必须是存在pk的才能记入真正被删除的
					if(modified[i].get(this.getPkFieldName())){
						toDel.push(modified[i].data);
					}
				}
			}
		}
    	return {"delete":toDel,"update":toUpt};
	},
	/**
	 * 返回所有记录的对象，该方法与getModifyValue的区别是对于没有改动的记录也加入到update里面
	 * @return {}
	 */
	getAllRecordValue : function(){
    	var toDel = new Array();
		var modified = this.getStore().modified;
		if(modified){
			for(var i=0;i<modified.length;i++){
				//格式化日期
	    		for(key in modified[i].data){
	    			if(modified[i].data[key] instanceof Date){
	    				//目前系统支持日期+时间格式的控件，可能会有不同的日期格式了，不能定死了
	    				var column = uft.Utils.getColumn(this,key);
	    				var value = modified[i].data[key].dateFormat(column.format||'Y-m-d');
	    				modified[i].set(key,value);
	    			}
	    		}
				if(!modified[i].store){
					//删除的记录，但是必须是存在pk的才能记入真正被删除的
					if(modified[i].get(this.getPkFieldName())){
						toDel.push(modified[i].data);
					}
				}
			}
		}		
		var toUpt = new Array();
    	for(var i=0;i<this.getStore().getCount(); i++){
    		var record = this.getStore().getAt(i);
    		//格式化日期
    		for(key in record.data){
    			if(record.data[key] instanceof Date){
    				//目前系统支持日期+时间格式的控件，可能会有不同的日期格式了，不能定死了
	    			var column = uft.Utils.getColumn(this,key);
    				var value = record.data[key].dateFormat(column.format||'Y-m-d');
    				record.set(key,value);
    			}
    		}
    		toUpt.push(record.data);
        }
    	return {"delete":toDel,"update":toUpt};
	},	
	initComponent : function(){
	    uft.extend.grid.EditorGrid.superclass.initComponent.call(this);
	    this.addEvents(
			'afterrowselect' //增加一个选择行后触发事件
		);
	},
    onEditComplete : function(ed, value, startValue){
        this.editing = false;
        this.lastActiveEditor = this.activeEditor;
        this.activeEditor = null;

        var r = ed.record,
            field = this.colModel.getDataIndex(ed.col);
        value = this.postEditValue(value, startValue, r, field);
        if(this.forceValidation === true || String(value) !== String(startValue)){
            var e = {
                grid: this,
                record: r,
                field: field,
                originalValue: startValue,
                value: value,
                row: ed.row,
                column: ed.col,
                cancel:false
            };
            if(this.fireEvent("validateedit", e) !== false && !e.cancel && String(value) !== String(startValue)){
                r.set(field, e.value);
                delete e.cancel;
                //当执行完afteredit事件后,将修改该标志
                this.afterEditComplete = false;
                if(this.fireEvent("afteredit", e) !== false){
                	this.afterEditComplete = true;
                }
            }
        }
        this.view.focusCell(ed.row, ed.col);
    },
	setDisabled : function(){
		if(this.bakUp == null) {
			this.storeDisabled();
		}
		for(var i=0; i<this.originalColumns.length; i++){
			this.originalColumns[i].editable = false;
		}
		this.editable=false;//设置整个grid不可编辑。是一个标记，在其他地方需要引用到
	},
	//private
	storeDisabled : function() {
		var bakUp = {};
		for(var i=0; i<this.originalColumns.length; i++){
			var editable = this.originalColumns[i].editable;
			if(editable ===false){
				bakUp[this.originalColumns[i].id] = false;	
			}else{
				bakUp[this.originalColumns[i].id] = true;
			}
		}
		this.bakUp = bakUp;
	},	
	/**
	 * 还原可编辑列，如果存在自定义列，则只启用自定义列
	 * @param {} editableColumns 可编辑列
	 */
	reStoreDisabled : function(editableColumns) {
		if(editableColumns){
			if(editableColumns instanceof Array){
				//可编辑列是一个集合
				for(var j=0;j<editableColumns.length;j++){
					for(var i=0; i<this.originalColumns.length; i++){
						if(this.originalColumns[i].id==editableColumns[j]){
							this.originalColumns[i].editable=true;
							break;
						}
					}
				}
			}else{
				for(var i=0; i<this.originalColumns.length; i++){
					if(this.originalColumns[i].id==editableColumns){
						this.originalColumns[i].editable=true;
						break;
					}
				}
			}
		}else{
			if(this.bakUp == null){
				this.storeDisabled();
			}
			var bakUp = this.bakUp;
			for(var i=0; i<this.originalColumns.length; i++){
				this.originalColumns[i].editable = bakUp[this.originalColumns[i].id];
			}
			this.editable=true;			
		}
	} ,
	/**
	 * 将reviseflag===true的列设置成可编辑状态
	 * 设置修订
	 */
	enableRevise : function() {
		var cols = this.originalColumns;
		for(var i=0; i<cols.length; i++){
			if(cols[i].reviseflag===true){
				cols[i].editable=true;
			}else{
				cols[i].editable=false;				
			}			
		}
	},
	
	/**
	 * 将所有列设置成不可编辑状态
	 */
	disableRevise : function() {
		for(var i=0; i<this.originalColumns.length; i++){
			this.originalColumns[i].editable=false;
		}
	},		
    onCellDblClick : function(g, row, col){
        this.startEditing(row, col);
    },
    // private
    onAutoEditClick : function(e, t){
        if(e.button !== 0){
            return;
        }
        var row = this.view.findRowIndex(t),
            col = this.view.findCellIndex(t);
        if(row !== false && col !== false){
            this.stopEditing();
            if(this.selModel.getSelectedCell){ // cell sm
                var sc = this.selModel.getSelectedCell();
                if(sc && sc[0] === row && sc[1] === col){
                    this.startEditing(row, col);
                }
            }else{
                if(this.selModel.isSelected(row)){
                    this.startEditing(row, col);
                }
            }
        }
    },
	/**
	 * 为表格增加一行
	 * @param {} grid
	 * @param {} data
	 */
	addRow : function(data){
		if(!data){
			data={};
		}
		var ds = this.getStore();
		if(Ext.isArray(data)){
			//数组
			var rs = [];
			for(var i=0;i<data.length;i++){
				var value = this.getDefaultValue(data[i]);
				var recordType = ds.recordType;
		        var record = new recordType();
				record.data=value;
				rs.push(record);
			}
			this.stopEditing();
	        ds.insert(ds.getCount(), rs);
		}else{
			data = this.getDefaultValue(data);
			var recordType = ds.recordType;
	        var record = new recordType();
			record.data=data;
	        if(this.fireEvent('beforeaddrow',this,record) !== false){
		        this.stopEditing();
		        ds.insert(ds.getCount(), record);
		        this.setSelectedRow(record); //设置当前行
		        this.fireEvent('addrow',this,record);
	        }
		}
	},
	/**
	 * 为表格插入一行
	 * @param {} grid
	 * @param {} data
	 * @param {} index 插入的位置
	 */
	insRow : function(data,index){
		if(!data){
			data={};
		}
		var ds = this.getStore();
		if(Ext.isArray(data)){
			//数组
			var rs = [];
			for(var i=0;i<data.length;i++){
				var value = this.getDefaultValue(data[i]);
				var recordType = ds.recordType;
		        var newRecord = new recordType();
		        newRecord.data = value;;
		        rs.push(newRecord);
			}
			this.stopEditing();
	        ds.insert(index, rs);
		}else{
			data = this.getDefaultValue(data);
			var recordType = this.getStore().recordType;
	        var newRecord = new recordType();
	        newRecord.data = data;;
	        if(this.fireEvent('beforeinsrow',this,newRecord) !== false){
		        this.stopEditing();
		        this.getStore().insert(index, newRecord);
		        this.setSelectedRow(newRecord);
		        this.fireEvent('insrow',this,newRecord);
	        }
		}
	},
	/**
	 * 从recordType属性中读取默认值，该默认值在模板中设置的
	 * 与前台的默认值合并，前台的默认值可以覆盖模板中设置的默认值
	 * @param {} grid
	 * @param {} data 前台设置的默认值
	 * @private
	 */
	getDefaultValue : function(data){
		var targetData={};
        var orginalRecordType = this.recordType;
        for(var i=0;i<orginalRecordType.length;i++){
        	if(orginalRecordType[i].value!=undefined){//当recordType存在value属性时，将其加入默认值
        		targetData[orginalRecordType[i].name]=orginalRecordType[i].value;
        	}
        }
        Ext.apply(targetData,data);
        delete data;
        return targetData;
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
		data = this.getDefaultValue(data);
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
	 * 只更新总记录数，存在分页的情况下，如果一次新增了100行，而每页记录数只有50行的情况下，此时也先不要分页，等保存后再自动分页
	 */
	updateInfo : function(){
		var tb=this.bottomToolbar;
        if(tb){
        	tb.updateInfo();
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
	},
	/**
	 * 返回当前grid的总记录数，排除标记删除的记录
	 */
	getAvailableCount : function(){
		var store = this.getStore();
		var num=store.getCount();
//		for(var i=0;i<store.getCount();i++){
//			var record = store.getAt(i);
//		}
		return num;
	}
});
