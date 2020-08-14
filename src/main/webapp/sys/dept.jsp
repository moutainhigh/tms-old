<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" isBuildHeaderGrid="false"  tableColumns="2"/>
	</body>
	<script type="text/javascript">
		var tree = new uft.extend.tree.Tree({
			id : 'tree',
			rootVisible : true,
			treeRootNodeText : '部门',
			dataUrl : 'getDeptTree.json', //默认数据来源
			isTreeFilter:true
		});
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		T004Toolbar = Ext.extend(uft.jf.TreeFormToolbar, {
			btn_edit_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要修改的节点！');
				}else if(this.app.leftTree.getSelectedNode().id == '0001'){
					uft.Utils.showWarnMsg('根节点不允许修改！');
				}else{
					T004Toolbar.superclass.btn_edit_handler.call(this);
					var dept_code = uft.Utils.getField('dept_code');
					dept_code.setReadOnly(true);
				}
			},
			btn_del_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要删除的节点！');
				}else if(this.app.leftTree.getSelectedNode().id =='0001'){
					uft.Utils.showWarnMsg('根节点不允许删除！');
				}else{
					T004Toolbar.superclass.btn_del_handler.call(this);
				}
			}
		});		
		${moduleName}.appUiConfig.leftTree=tree;	
		${moduleName}.appUiConfig.treePkField='pk_dept';
		${moduleName}.appUiConfig.treeParentPkField='fatherdept';
		${moduleName}.appUiConfig.toolbar = new T004Toolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
