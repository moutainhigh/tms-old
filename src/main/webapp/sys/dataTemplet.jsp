<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="false" bodyGridsDragDropRowOrder="true" headerGridImmediatelyLoad="false" />
	</body>
	<script type="text/javascript">
		var funTree = new uft.extend.tree.Tree({
			treeRootNodeText:'功能节点', //默认根节点名称
			rootVisible : true,
			dataUrl : 'getFunTree.json', //默认数据来源
			isTreeFilter:true
		});
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
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
			},
			btn_edit_handler : function(){
				var node = funTree.getSelectedNode();
				if(!node || !node.leaf){
					uft.Utils.showWarnMsg('请选择功能菜单树的叶子节点！');
					return false;
				}
				MyToolbar.superclass.btn_edit_handler.call(this);
			},
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				var node = funTree.getSelectedNode();
				var fun_code = node.attributes['code'];
				Ext.getCmp('pk_fun').setValue(node.id);
				Ext.getCmp('nodecode').setValue(fun_code);
				Ext.getCmp('pk_corp').setValue(Ext.getCmp('_pk_corp').getValue());
			}
		});
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			getRowDefaultValues : function(gridId){
				var values = MyBodyAssistToolbar.superclass.getRowDefaultValues.call(this,gridId);
				var bGrid = app.getBodyGrids()[0];
				var count = bGrid.getStore().getCount();
				values['display_order'] = count;
				return values;
			}
		});
		${moduleName}.appUiConfig.leftTree=funTree;	
		${moduleName}.appUiConfig.treePkField='pk_fun';
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
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
				record.set('display_order',i);
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
