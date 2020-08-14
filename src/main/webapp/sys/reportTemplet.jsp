<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/report/Win.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/report/Contextmenu.js"/>"></script>
		<style type="text/css">
			.x-btn-padd {
			   	margin-top:5px;
				margin-bottom:5px;
			}
			.x-table-layout-cell{
				vertical-align:top
			}
			.up {
			    background-image: url(../sys/images/up.png) !important;
			}
			.down {
			    background-image: url(../sys/images/down.png) !important;
			}
			.top {
			    background-image: url(../sys/images/top.png) !important;
			}
			.bottom {
			    background-image: url(../sys/images/bottom.png) !important;
			}				
		</style>		
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true" 
	headerGridSingleSelect="false" bodyGridsDragDropRowOrder="true" />				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_copy);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				return btns;
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar,{
			getRowDefaultValues : function(gridId){
				var values = MyBodyAssistToolbar.superclass.getRowDefaultValues.call(this,gridId);
				var grid = Ext.getCmp(gridId);
				if(grid){
					var ds = grid.getStore(),count = ds.getCount();
					values['display_order'] = count+1;	
				}
				return values;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar();
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		//拷贝行
		function copyRow(){
			if(app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_ADD 
					&& app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_EDIT){
				return false;
			}
			app.bodyAssistToolbar.currentRecord = app.getActiveBodyGrid().getSelectedRow();
			app.bodyAssistToolbar.btn_row_pas_handler();
		}
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='copyRow()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(app.getBodyGrids()[0],'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;	
		}
		
		//检查页面的状态，某些状态下，不能执行拖拽操作，或者不能弹出右键菜单
		function checkPageStatus(){
			if(app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_ADD && app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_EDIT){
				return false;
			}
			return true;
		}
		//拖动和释放路线信息的行记录时，需要进行判定 
		var bGrid = app.getBodyGrids()[0];
		//表体的右键菜单，只有在编辑状态下出现
		bGrid.on('rowcontextmenu',function(grid,rowIndex,e){
			if(!checkPageStatus()){
				return false;
			}
			e.preventDefault();//阻止事件的传递
			var record = grid.getStore().getAt(rowIndex);
			grid.getSelectionModel().selectRow(rowIndex);
        	var menu = new uft.report.RowContextMenu({app:app,grid:grid,record:record});
            menu.showAt(e.getXY());
		},this);
		bGrid.on('contextmenu',function(e){
			if(!checkPageStatus()){
				return false;
			}
			e.preventDefault();
        	var menu = new uft.report.GridContextMenu({grid:bGrid});
            menu.showAt(e.getXY());
		},this);
		
		bGrid.on('beforeedit',function(e){
			var column = uft.Utils.getColumn(e.grid,e.field);
			if(e.field == 'reftype'){
				var r = e.record;
				var datatype = r.get('data_type');
				if(String(datatype) == '5'){//参照
					if(!(column.editor instanceof uft.bill.ReftypeField)){//这里需要判断下，避免出现多次设置的情况，如果多次设置，那么对象不一样，导致值无法设置
						column.setEditor(new uft.bill.ReftypeField());
					}
				}else if(String(datatype) == '6'){//下拉
					if(!(column.editor instanceof uft.bill.SelecttypeField)){
						column.setEditor(new uft.bill.SelecttypeField());
					}
				}else if(String(datatype) == '9' || String(datatype) == '11'){//大文本,对象
					column.setEditor(new Ext.form.TextField());
				}else{
					return false;
				}
			}
		},this);
		bGrid.on('afteredit',function(e){
			if(e.field == 'data_type'){//编辑数据类型
				if(String(e.value) == '9'){
					//大文本
					e.record.set('reftype','(100,50)');
				}else{
					e.record.set('reftype','');
				}
			}
		},this);		
		
		var dragDropPlugin;
		for(var i=0;i<bGrid.plugins.length;i++){
			if(bGrid.plugins[i] instanceof Ext.ux.dd.GridDragDropRowOrder){
				dragDropPlugin = bGrid.plugins[i];//取得插件
			}
		}
		if(dragDropPlugin){
			//plugin对象、原始行的行号，新的行号，选择的行[是个数组]
			dragDropPlugin.on('beforerowmove',function(plugin,rowIndex,toRowIndex,rows){
				if(!checkPageStatus()){
					uft.Utils.showWarnMsg('非编辑态不能拖动行！');
					return false;
				}
				bGrid.stopEditing();
			},this);
			dragDropPlugin.on('afterrowmove',function(plugin,rowIndex,toRowIndex,rows){
				setDispSequence();
			},this);
		}
		//重新设置排序号
		function setDispSequence(){
			var store = bGrid.getStore();
			for(var i=0;i<store.getCount();i++){
				var record = store.getAt(i);
				record.set('display_order',(i+1));
			}
		}
		//重新排序
		function reOrderField(grid,itemkeyAry){
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
	    			record.set('display_order',i+1);
	    			records.push(record);
	    		}
	    		ds.add(records);
			}		
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
