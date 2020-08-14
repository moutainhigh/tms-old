Ext.ns('uft.jf');
/**
 * 表体的Toolbar，操作习惯上的方便，将这些按钮放在表体上
 * 包括：增行、删行、撤销删行、插入行、复制行、粘帖行
 * @class uft.jf.BodyAssistToolbar
 * @extends uft.base.UIToolbar
 */
uft.jf.BodyAssistToolbar = Ext.extend(Ext.Toolbar, {
	cls : "uft-grid-row-toolbar",
	app : null,
	btnEnableStatus : null,//按钮的启用状态，是指在新增和编辑状态下的按钮状态,值如：{'ts_invoice':{'add':false,'cop':false,'del':true}}
	//暂时不支持
	//btnVisibleStatus : null,//按钮的启用状态,值如：{'ts_invoice':{'add':false,'cop':false,'del':true}}
	constructor : function(config){
		Ext.apply(this,config);
		this.btn_row_add = new uft.extend.Button({
	        iconCls : 'btnRowAdd',
	        showHotKey : true,
			keyBinding: {
	            key: 'i',
	            ctrl : true
	        },
	        tooltip : '增行(Ctrl+i)',
	        handler : this.btn_row_add_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE]
		});
		this.btn_row_del = new uft.extend.Button({
	        iconCls : 'btnRowDel',
	        showHotKey : true,
			keyBinding: {
	            key: 'd',
	            ctrl : true
	        },
	        tooltip : '删行(Ctrl+D)',
	        handler : this.btn_row_del_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REFADD_CARD,uft.jf.pageStatus.OP_REVISE]
		});
		this.btn_row_ins = new uft.extend.Button({
	        iconCls : 'btnRowIns',
	        showHotKey : true,
			keyBinding: {
	            key: 'i',
	            alt : true
	        },
	        tooltip : '插入行(Alt+I)',
	        handler : this.btn_row_ins_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE]
		});
		this.btn_row_cop = new uft.extend.Button({
	        iconCls : 'btnRowCopy',
	        showHotKey : true,
			keyBinding: {
	            key: 'c',
	            alt : true
	        },
	        tooltip : '复制行(Alt+C)',
	        handler : this.btn_row_cop_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE]
		});
		this.btn_row_pas = new uft.extend.Button({
	        iconCls : 'btnRowPas',
	        showHotKey : true,
			keyBinding: {
	            key: 'v',
	            alt : true
	        },
	        tooltip : '粘帖行(Alt+V)',
	        handler : this.btn_row_pas_handler,
	        enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE]
		});	
		uft.jf.BodyAssistToolbar.superclass.constructor.call(this,{
			autoHeight : true,
			defaults : {
				scope : this
			},
			plugins:new Ext.ux.ToolbarKeyMap(),
			items : this.getBtnArray()
		});
		this.onReady();
	},
	getBtnArray : function(){
		var btns = new Array();
		btns.push(this.btn_row_add);
		btns.push('-');
		btns.push(this.btn_row_del);
		btns.push('-');
		btns.push(this.btn_row_ins);
		btns.push('-');
		btns.push(this.btn_row_cop);
		btns.push('-');
		btns.push(this.btn_row_pas);
		return btns;
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
				uft.Utils.showErrorMsg('子表的主键字段不能为空，请检查Service中的billInfo配置！');
				return false;
			}
			grid.stopEditing();//先提交当前在编辑的行
			var selectionModel = grid.getSelectionModel();
			var store = grid.getStore();
			var row=0,col=0;
			if(typeof(selectionModel.getSelectedCell)=='function'){
				//单元格选择模式
				var cell=selectionModel.getSelectedCell();
				if(!cell){
					//未选中任何单元格
					uft.Utils.showWarnMsg('请先选中要操作的数据！');
					return false;
				}
				row = cell[0];
				col = cell[1];
			}else{
				row = store.indexOf(selectionModel.getSelected());
			}
			var nextRecord = store.getAt(row+1);
			var frontRecord = store.getAt(row-1);
			store.remove(record);
			//更新底部工具栏的显示信息
			store.totalLength=store.getTotalCount()-1;
			grid.updateTbarInfo();
			if(nextRecord){
				if(col != 0){
					selectionModel.select(row,col);
				}else{
					selectionModel.selectRow(row);
				}
				grid.setSelectedRow(nextRecord);
			}else{
				if(frontRecord){
					if(col != 0){
						selectionModel.select(row-1,col);
					}else{
						selectionModel.selectRow(row-1);
					}
					grid.setSelectedRow(frontRecord);
				}else{
					grid.setSelectedRow(null);
				}
			}
			if(grid.id != 'ts_orderlot_rd'){
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
				//更新底部工具栏的显示信息
				grid.getStore().totalLength=grid.getStore().getTotalCount()+1;
				grid.updateTbarInfo();		
		        this.fireEvent('rowins',grid,rowDefaultValues);
			}
		}else{
			uft.Utils.showWarnMsg('请先选中要操作的数据！');
			return false;
		}
	},
	/**
	 * 复制行按钮操作
	 * @Override
	 */
	btn_row_cop_handler : function(){
		var grid = this.app.getActiveBodyGrid();
		this.currentRecord = grid.getSelectedRow();
		if(!this.currentRecord){
			uft.Utils.showWarnMsg('请先选中要操作的数据！');
			return false;
		}
	},
	/**
	 * 粘帖行按钮操作
	 * @Override
	 */
	btn_row_pas_handler : function(){
		if(!this.currentRecord){
			uft.Utils.showWarnMsg('请先复制一行！');
			return false
		}else{
			var grid = this.app.getActiveBodyGrid();
			if(grid.getPkFieldName()==null||grid.getPkFieldName()==''){
				uft.Utils.showErrorMsg('子表的主键字段不能为空，请检查VOTable的配置！');
				return false;
			}		
			grid.stopEditing();//先提交当前在编辑的行
			var newJson={};
			uft.Utils.cloneJsonObject(this.currentRecord.data,newJson);//先拷贝一份，防止原有数据被修改
			var defaultValue = this.getRowPasDefaultValue(grid.id,newJson);
			defaultValue[grid.getPkFieldName()] = null; //将主键设置为空
			grid.addRow(defaultValue);
			//更新底部工具栏的显示信息
			grid.getStore().totalLength=grid.getStore().getTotalCount()+1;
			grid.updateTbarInfo();			
	        this.fireEvent('rowpas',grid,defaultValue);					
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
					if(column.lockflag===true && column.editor){
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
	},
	/**
	 * 设置粘帖行默认值，可以被继承,复制行以后通常需要修改默认值，而又不必要在后台处理，可以继承该方法
	 * @param gridId 要设置默认值的grid
	 * @param {} selectRecordValue
	 */
	getRowPasDefaultValue : function(gridId,selectRecordValue){
		return selectRecordValue;
	},
	/**
	 * 是否禁用Toolbar
	 * @param {} flag
	 */
	setDisabled : function(flag){
		if(Utils.getParameter('bbar') != 2){
			var grid = this.app.getActiveBodyGrid();
			this.setOperatorEnabled(grid,{'add':!flag,'cop':!flag,'del':!flag});
		}else{
			var items = this.getBtnArray();
			for(var i=0;i<items.length;i++){
				if(typeof(items[i].disable)== 'function'){ //disable和enable肯定是成对出现
		             if(flag){
		             	items[i].disable();
		             }else{
		             	items[i].enable();
		             }
				}
			}
		}
	},
	/**
	 * 操作列增行定义
	 */
	getOperatorAdd : function(flag){
		return  flag ? "<div><span class='uft-grid-header-operator-disable'></span></div>":
	 	              "<span class='uft-grid-header-operator' title='增行' onclick='javascript:app.bodyAssistToolbar.operatorCheck(\"add\");return false;'></span>";
	},
	/**
	 * 操作列复制行定义
	 */
	getOperatorCop : function(flag){
	 	return  flag ? "<span  class='btn-row-cop-disable' ></span>":
	 	               "<span class='btn-row-cop' title='复制行' onclick='javascript:app.bodyAssistToolbar.operatorCheck(\"cop\");return false;'></span>";
	},
	/**
	 * 操作列删除行定义
	 */
	getOperatorDel : function(flag){
		return flag ? "<span  class='btn-row-del-disable'></span>":
	 	              "<span class='btn-row-del' title='删除行' onclick='javascript:app.bodyAssistToolbar.operatorCheck(\"del\");return false;'></span>";
	},
	 /**
	 * 获取操作列增行的定义
	 */
	getOperatorHeader : function(operator){
		var blAdd = true,app = window.app,bat=this;
		if(app){
			bat = app.bodyAssistToolbar;
		}
	    //根据页面状态设置按钮状态
	    if(app && app.statusMgr){
        	for(var i=0;i<bat.btn_row_add.enabledStatus.length;i++){
	 	    	if(app.statusMgr.getCurrentPageStatus()==bat.btn_row_add.enabledStatus[i]){
	 		    	blAdd = false;
	 			}
			}
		}
	    //根据增行按钮标识设置状态
	    if(operator && operator.hasOwnProperty('btn_row_add_disable')){
	    	blAdd = operator.btn_row_add_disable;
	    }
	 	return bat.getOperatorAdd(blAdd);
	 },
	 /**
	  * 获取操作复制行与删除行的定义
	  */
	 getOperatorRenderer : function(operator){
	    var str = "<div class='uft-grid-row-operator'>";
	    var app = window.app,bat = this,blCop = true,blDel = true;
	    if(app){
			bat = app.bodyAssistToolbar;
		}
	    if(app && app.statusMgr){
	    	//根据页面状态设置复制行按钮状态
	    	for(var i=0; i < bat.btn_row_cop.enabledStatus.length; i++){
				if(app.statusMgr.getCurrentPageStatus() == bat.btn_row_cop.enabledStatus[i]){
					blCop = false;
				}
 		    }
 		    //根据页面状态设置删除行按钮状态
	 		for(var i=0; i < bat.btn_row_del.enabledStatus.length; i++){
	 			if(app.statusMgr.getCurrentPageStatus() == bat.btn_row_del.enabledStatus[i]){
	 				blDel = false;
	 			}
 			}
	    }
	    //根据复制行按钮标识设置状态
	    if(operator && operator.hasOwnProperty('btn_row_cop_disable')){
	    	blCop = operator.btn_row_cop_disable;
	    }
	    //根据删除行按钮标识设置状态
	    if(operator && operator.hasOwnProperty('btn_row_del_disable')){
	    	blDel = operator.btn_row_del_disable;
	    }
 		str += bat.getOperatorCop(blCop);
 		str += "&nbsp;"
 		str += bat.getOperatorDel(blDel);
 		str += "</div>";
	 	return str
	 },
	 /**
	  * 执行增行、复制行 、删除行
	  */
	 operatorCheck : function(action){
	 	if(action == 'add'){
			this.btn_row_add_handler();
	 	} else if(action == 'cop'){
			if(this.btn_row_cop_handler() != false){
				this.btn_row_pas_handler();
			}
	 	} else{
			this.btn_row_del_handler();
	 	}
	 },
	 /**
	  * 根据页面状态更新操作列按钮禁用启用状态
	  */
	 updateOperatorStatus : function(){
	 	var app = this.app,bat = app.bodyAssistToolbar;
 		if(this.app.hasBodyGrid()){
			var bGrids = this.app.getBodyGrids();
			for(var i=0; i < bGrids.length; i++){
				var operator = uft.Utils.getColumn(this.app.getBodyGrids()[i],'_operator');
				if(operator){
					operator.renderer = function(){
						return app.bodyAssistToolbar.getOperatorRenderer(this);
					};//this指向operator
					operator.header = this.getOperatorHeader(operator);
					if(bGrids[i].rendered){
						bGrids[i].getView().refresh(true);
					}
				}
			}
		}
	 },
	 /**
	  * grid,{'add':true,'cop':true,'del':true}
	  *禁用启用操作列增行，复制行，删除行按钮状态
	  */
	 setOperatorEnabled : function(grid, btns){
	 	if(grid && btns){
	 		var operator = uft.Utils.getColumn(grid, '_operator');
	 		if(operator){
		 		if(btns.hasOwnProperty('add')){
	 		    	operator.btn_row_add_disable = !btns.add;
	 				operator.header = this.getOperatorHeader(operator);
		 		} 
		 		if(btns.hasOwnProperty('cop') || btns.hasOwnProperty('del')){
		 			if(btns.hasOwnProperty('cop')){
		 				operator.btn_row_cop_disable = !btns.cop;
		 			}
		 			if(btns.hasOwnProperty('del')){
		 			    operator.btn_row_del_disable = !btns.del;
		 			}
		 		 	operator.renderer = function(){return app.bodyAssistToolbar.getOperatorRenderer(this);};//this指向operator
		 		}
		 		if(grid.rendered){
		 			grid.getView().refresh(true);
		 		}
	 		}
	 	}
	 },
     /**grid, flag(true隐藏, false显示)
      * 隐藏显示操作列
      */
	 setOperatorHidden : function(grid, flag){
	 	if(grid){
	 		var operator = uft.Utils.getColumn(grid,'_operator');
	 		if(flag){
	 			grid.getColumnModel().setHidden(operator.id, true);
	 		} else {
				operator.hidden = false;
				grid.getColumnModel().totalWidth = null;
				grid.getColumnModel().fireEvent("hiddenchange", grid.getColumnModel(), operator.id, false);
	 		}
	 	}
	 },
	 onReady : function(){
	 	Ext.onReady(function(){
	 		var bes = this.btnEnableStatus,app = this.app,sm = app.statusMgr;
	 		if(bes){
	 			app.statusMgr.addAfterUpdateCallback(function(){
					var bes = this.btnEnableStatus;
					for(var key in bes){
						var grid = Ext.getCmp(key);
						if(grid){
							this.setBodyAssistToolbarEnable(grid,bes[key]);
						}
					}
				},this);
				var btp = app.getBodyTabPanel();
				if(btp instanceof Ext.TabPanel){
					btp.addListener('tabchange',function(){
						var app = this.app,grid = app.getActiveBodyGrid();
						var bes = this.btnEnableStatus;
						for(var key in bes){
							if(grid.id == key){
								this.setBodyAssistToolbarEnable(grid,bes[key]);
							}
						}
						
					},this);
				}
	 		}
	 	},this);
	 },
	 setBodyAssistToolbarEnable : function(grid,des){
	 	var app = this.app,pageStatus = app.statusMgr.getCurrentPageStatus();
		if(pageStatus == uft.jf.pageStatus.OP_EDIT || pageStatus == uft.jf.pageStatus.OP_ADD){
			this.setOperatorEnabled(grid,des);
		}else{
			this.setOperatorEnabled(grid,{'add':false,'cop':false,'del':false});
		}
	 }
});