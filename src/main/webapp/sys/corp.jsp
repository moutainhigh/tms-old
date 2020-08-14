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
		var corpTree = new uft.extend.tree.Tree({
			id : 'corpTree',
			rootVisible : false,
			dataUrl : 'getCorpTree.json', //默认数据来源
			isTreeFilter:true
		});
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		T003Toolbar = Ext.extend(uft.jf.TreeFormToolbar, {
			btn_edit_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要修改的节点！');
				}else if(this.app.leftTree.getSelectedNode().id == '0001'){
					uft.Utils.showWarnMsg('根节点不允许修改！');
				}else{
					T003Toolbar.superclass.btn_edit_handler.call(this);
					var corp_code = uft.Utils.getField('corp_code');
					corp_code.setReadOnly(true);
				}
			},
			btn_del_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要删除的节点！');
				}else if(this.app.leftTree.getSelectedNode().id =='0001'){
					uft.Utils.showWarnMsg('根节点不允许删除！');
				}else{
					T003Toolbar.superclass.btn_del_handler.call(this);
				}
			}
		});		
		${moduleName}.appUiConfig.leftTree=corpTree;	
		${moduleName}.appUiConfig.treePkField='pk_corp';
		${moduleName}.appUiConfig.treeParentPkField='fathercorp';
		${moduleName}.appUiConfig.toolbar = new T003Toolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
