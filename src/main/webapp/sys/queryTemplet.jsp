<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeWindow.js"/>"></script>
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
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true" headerGridSingleSelect="false" bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" bodyGridsDragDropRowOrder="true" 
		/>				
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
		${moduleName}.appUiConfig.toolbar = new MyToolbar();
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
		
		//拖动和释放路线信息的行记录时，需要进行判定 
		var bGrid = app.getBodyGrids()[0];
		var dragDropPlugin;
		for(var i=0;i<bGrid.plugins.length;i++){
			if(bGrid.plugins[i] instanceof Ext.ux.dd.GridDragDropRowOrder){
				dragDropPlugin = bGrid.plugins[i];//取得插件
			}
		}
		if(dragDropPlugin){
			//plugin对象、原始行的行号，新的行号，选择的行[是个数组]
			dragDropPlugin.on('beforerowmove',function(plugin,rowIndex,toRowIndex,rows){
				if(app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_ADD && app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_EDIT){
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
				record.set('disp_sequence',i);
			}
		}
		//编辑datatype时，如果选择了参照，那么retype_type默认为2,否则默认为0
		function afterEditData_type(record){
			var datatype = record.get('data_type');
			if(String(datatype) == '5'){//参照
				record.set('return_type',2);
			}else{
				record.set('return_type',0);
			}
		}
		
		bGrid.on('beforeedit',function(e){
			var column = uft.Utils.getColumn(e.grid,e.field);
			if(e.field == 'consult_code'){
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
			}else if(e.field == 'show_value'){
				var data_type = uft.Utils.getColumnValue(e.record,'data_type');
				var reftype = uft.Utils.getColumnValue(e.record,'consult_code');
				if(String(data_type) == '5' && reftype && reftype != '-99'){
					//默认值那一列，如果data_type是参照，那么检测下当前的editor是否是参照，如果不是，那么需要重新加载
					var column = uft.Utils.getColumn(e.grid,'show_value');
					if(column.editor.refName != reftype){
						//参照名称
						var values = Utils.doSyncRequest('getRefModel.json',{reftype:reftype},'POST');
						var refModel = Ext.create(values.data);
						column.setEditor(refModel);
					}
				}else{
					return false;
				}
			}else if(e.field == 'value'){
				var data_type = uft.Utils.getColumnValue(e.record,'data_type');
				if(String(data_type) == '5'){
					return false;
				}
			}
		},this);
		bGrid.on('afteredit',function(e){
			if(e.field == 'data_type'){//编辑数据类型
				if(String(e.value) == '9'){
					//大文本
					e.record.set('consult_code','(100,50)');
					return;
				}else{
					e.record.set('consult_code','-99');
				}
				
				var reftype = uft.Utils.getColumnValue(e.record,'consult_code');
				if(reftype && reftype != '-99'){
					if(String(e.value == '5')){
						//参照类型
						uft.Utils.setColumnEditable(e.grid,'value',false);//"默认值"不能编辑，"参照默认值"可编辑
						uft.Utils.setColumnEditable(e.grid,'show_value',true);
					}else{
						uft.Utils.setColumnEditable(e.grid,'value',true);//"默认值"不能编辑，"参照默认值"可编辑
						uft.Utils.setColumnEditable(e.grid,'show_value',false);
					}
				}
			}
		},this);		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
