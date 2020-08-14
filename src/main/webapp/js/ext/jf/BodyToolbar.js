Ext.ns('uft.jf');
/**
 * 单表体的panel的工具栏
 * @class uft.jf.BodyToolbar
 * @extends uft.base.UIToolbar
 */
uft.jf.BodyToolbar = Ext.extend(uft.base.UIToolbar, {
	app : null,
	queryWindowId : null,
	constructor : function (config){ 
		Ext.apply(this, config);
		this.btn_row_add = {
			variable : 'btn_row_add',
	        iconCls : 'btnRowAdd',
	        text : '增行',
	        showHotKey : true,
			keyBinding: {
	            key: 'i',
	            ctrl : true
	        },
	        tooltip : '增行(Ctrl+i)',
	        handler : this.btn_row_add_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT]
		};
		this.btn_row_del = {
			variable : 'btn_row_del',
	        iconCls : 'btnRowDel',
	        text : '删行',
	        showHotKey : true,
			keyBinding: {
	            key: 'd',
	            ctrl : true
	        },
	        tooltip : '删行(Ctrl+D)',
	        handler : this.btn_row_del_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REFADD_CARD]
		};
		this.btn_row_ins = {
			variable : 'btn_row_ins',
	        iconCls : 'btnRowIns',
	        text : '插行',
	        showHotKey : true,
			keyBinding: {
	            key: 'i',
	            alt : true
	        },
	        tooltip : '插入行(Alt+I)',
	        handler : this.btn_row_ins_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT]
		};		
		uft.jf.BodyToolbar.superclass.constructor.call(this);
		this.queryWindowId = Ext.id();
		this.btn_edit.enabledStatus=[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_INIT];
	},
	getBtnArray : function(){
		var btns = new Array();
		btns.push(this.btn_query);
		btns.push(this.btn_edit);
		btns.push(this.btn_save);
		btns.push(this.btn_can);
		btns.push(this.btn_ref);
		btns.push('-');
		btns.push(this.btn_row_add);
		btns.push(this.btn_row_del);
		btns.push(this.btn_row_ins);
		return btns;
	},	
	btn_query_handler : function(){
		var win = Ext.getCmp(this.queryWindowId);
		if(win){
			win.show();
		}else{
			win=new uft.jf.QueryWindow({
				id : this.queryWindowId,
				funCode : this.app.context.getFunCode(),
				nodeKey : this.app.context.getNodeKey(),
				billType: this.app.context.getBillType(),
				grid : this.app.getActiveBodyGrid()
			}).show();
		}
	},	
	/**
	 * 编辑按钮操作
	 * @Override
	 */
	btn_edit_handler : function() {
		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_EDIT);
		this.app.statusMgr.updateStatus();
	},
	/**
	 * 保存按钮操作
	 * @Override
	 */
	btn_save_handler : function() {
		//可编辑表格结束编辑，否则当编辑表格处于编辑状态，数据会没有提交。
		if(this.app.hasBodyGrid()){
			var grid=this.app.getActiveBodyGrid();
			if(typeof(grid.stopEditing) == "function"){
				grid.stopEditing();
			}			
		}
		
    	var params=this.app.newAjaxParams();
    	var appPostData = this.getAppParams();
    	if(appPostData !== false){
			params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
			uft.Utils.doAjax({
		    	scope : this,
		    	url : this.saveUrl||'save.json',
		    	params : params,
		    	success : function(values){
		    		this.doAfterSave(values);
		    	}
		    });
    	}
	},
	getAppParams : function(){
		var appPostData={};
		var bodyGridData={};
		var tabCodes=this.app.context.getBodyTabCode().split(',');//bodyGrids与tabCodes的长度肯定相同
		var bodyGrids = this.app.getBodyGrids();
		for(var i = 0; i < bodyGrids.length; i++) {
			if(typeof(bodyGrids[i].getAllRecordValue) == "function"){
				if(bodyGrids[i].isValid()) {//这里使用第三方插件进行验证
					bodyGridData[tabCodes[i]] = bodyGrids[i].getAllRecordValue();
				}else{
					var errors = bodyGrids[i].getAllErrors();
					if(errors.length > 0){
						uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
						return false;
					}
				}
			}
		}
		//加入从表数据
		appPostData[uft.jf.Constants.BODY]=bodyGridData;
		return appPostData;
	},
	doAfterSave : function(values){
		//刷新表体,这里刷新表体的作用是保存以后经常会出现有些字段是使用公式带出，不刷新的话前台看不到
		//如果没有刷新，新增的记录将没有pk，此时对这行进行操作将报错
		var m = this.app;
		if(this.app.hasBodyGrid()) {
			this.app.reloadBodyGrids();
		}
		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
		this.app.statusMgr.updateStatus();
	},
	/**
	 * 取消按钮操作
	 * @Override
	 */
	btn_can_handler : function() {
		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
		this.app.statusMgr.updateStatus();		
		this.app.reloadBodyGrids();
	},
	btn_ref_handler : function(){
		var grid=this.app.getActiveBodyGrid();
		var o = {};
		if(grid.isAddBbar){
			//是否使用分页
			o[grid.getStore().paramNames.start] = 0;
			o[grid.getStore().paramNames.limit] = grid.pageSize;
		}
		grid.getStore().reload({params:o});		
	},
	/**
	 * 增行按钮操作
	 */	
	btn_row_add_handler : function(){
		var grid = this.app.getActiveBodyGrid();
		grid.stopEditing();//先提交当前在编辑的行		
		if(this.fireEvent('beforerowadd',this) !== false){
			var rowDefaultValues = this.getRowDefaultValues(grid.id);
			grid.addRow(rowDefaultValues); //增加行
			var lastRowNumber = grid.getStore().getCount()-1;
			grid.getSelectionModel().select(lastRowNumber,1);//将焦点放在新增加的行上
			grid.setSelectedRow(grid.getStore().getAt(lastRowNumber));
			//更新底部工具栏的显示信息
			grid.getStore().totalLength=grid.getStore().getTotalCount()+1;
			grid.updateTbarInfo();
			this.fireEvent('rowadd',grid,rowDefaultValues);
		}
	},	
	/**
	 * 删行按钮操作
	 * @Override
	 */
	btn_row_del_handler : function(){
		var grid = this.app.getActiveBodyGrid();
		var record = grid.getSelectedRow();
		if(record){
			if(grid.getPkFieldName()==null||grid.getPkFieldName()==''){
				uft.Utils.showErrorMsg('子表的主键字段不能为空，请检查VOTable的配置！');
				return false;
			}
			grid.stopEditing();//先提交当前在编辑的行
			var cell=grid.getSelectionModel().getSelectedCell();
			if(cell==null){
				//未选中任何行
				uft.Utils.showWarnMsg('请先选中要操作的数据！');
				return false;
			}
			if(this.fireEvent('beforerowdel',this) !== false){
				var row = cell[0];
				var col = cell[1];
				var nextRecord = grid.getStore().getAt(row+1);
				var frontRecord = grid.getStore().getAt(row-1);
				grid.getStore().remove(record);
				//更新底部工具栏的显示信息
				grid.getStore().totalLength=grid.getStore().getTotalCount()-1;
				grid.updateTbarInfo();
				if(nextRecord){
					grid.getSelectionModel().select(row,col);
					grid.setSelectedRow(nextRecord);
				}else{
					if(frontRecord){
						grid.getSelectionModel().select(row-1,col);
						grid.setSelectedRow(frontRecord);
					}else{
						grid.setSelectedRow(null);
					}
				}
				//更新行信息，其实这里就更新行号有用
				for(var i=row;i<grid.getStore().getCount();i++){
					var _record = grid.getStore().getAt(i);
					grid.getView().refreshRow(_record);
				}
				this.fireEvent('rowdel',grid,record.data);
			}
		}else{
			uft.Utils.showWarnMsg('请先选中要操作的数据！');
			return false;
		}
	},
	/**
	 * 插入行按钮操作
	 * @Override
	 */
	btn_row_ins_handler : function(){
		var grid = this.app.getActiveBodyGrid();
		var record = grid.getSelectedRow();
		if(record){
			grid.stopEditing();//先提交当前在编辑的行
			if(this.fireEvent('beforerowins',this) !== false){
				var rowDefaultValues = this.getRowDefaultValues(grid.id);
				var index = grid.getStore().indexOf(record);
				grid.insRow(rowDefaultValues,index);
				grid.getSelectionModel().select(index,1);//将焦点放在新插入的行上
				grid.setSelectedRow(grid.getStore().getAt(index));
				//更新底部工具栏的显示信息
				grid.getStore().totalLength=grid.getStore().getTotalCount()+1;
				grid.updateTbarInfo();		
				//更新行信息，其实这里就更新行号有用
				for(var i=index;i<grid.getStore().getCount();i++){
					var _record = grid.getStore().getAt(i);
					grid.getView().refreshRow(_record);
				}
		        this.fireEvent('rowins',grid,rowDefaultValues);
			}
		}else{
			uft.Utils.showWarnMsg('请先选中要操作的数据！');
			return false;
		}
	},
	/**
	 * 返回行默认值
	 * @return {}
	 */
	getRowDefaultValues : function(gridId){
		//行默认数据
		var rowDefaultValues = {};
		//1、先通过新增单据时的缓存去取
		if(!this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW)){
			this.app.loadDefaultValue();
		}
		var appBufferData = this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW);
		if(appBufferData){
			if(appBufferData.BODY){
				if(appBufferData.BODY[gridId] && Ext.isArray(appBufferData.BODY[gridId])){
					//appBufferData.BODY[grid.id]的值肯定是一个array，这是统一格式的规范
					rowDefaultValues = appBufferData.BODY[gridId][0];
				}
			}
		}
		return Ext.applyIf(rowDefaultValues,this.getLockColumnValues(gridId));
	},
	/**
	 * 当该列的lockflag字段为true时，参照上一行的值
	 * @param {} gridId
	 * @return {}
	 */
	getLockColumnValues : function(gridId){
		var values={};
		var grid=Ext.getCmp(gridId);
		if(grid){
			var count = grid.getStore().getCount();
			var record = grid.getStore().getAt(count-1); //最后一行的值
			if(record){
				for(var i=0;i<grid.colModel.columns.length;i++){
					var column = grid.colModel.columns[i];
					if(column.lockflag===true){
						//该列参考上一行的值
						values[column.dataIndex]=record.data[column.dataIndex];
						if(column.xtype='refcolumn'){
							//参照，则需要同时将隐藏ID域赋值
							values[column.editor.idcolname]=record.data[column.editor.idcolname];
						}
					}
				}
			}
		}
		return values;
	}	
});