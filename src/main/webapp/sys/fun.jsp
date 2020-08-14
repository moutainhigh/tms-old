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
		var funTree = new uft.extend.tree.Tree({
			id : 'funTree',
			rootVisible : false,
			dataUrl : 'getFunTree.json', //默认数据来源
			isTreeFilter:true
		});
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		T001Toolbar = Ext.extend(uft.jf.TreeFormToolbar, {
			btn_add_handler : function(){
				T001Toolbar.superclass.btn_add_handler.call(this);
				//新增情况下可以编辑节点类型
				var fun_property = uft.Utils.getField('fun_property');
				fun_property.setReadOnly(false);
				//设置默认的编码
				var parentNode = this.app.leftTree.getSelectedNode();
				var fun_code = uft.Utils.getField('fun_code');
				fun_code.setValue(parentNode.attributes['code']);
				fun_code.focus();
			},
			btn_edit_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要修改的节点！');
				}else if(this.app.leftTree.getSelectedNode().id == '0001'){
					uft.Utils.showWarnMsg('根节点不允许修改！');
				}else{
					T001Toolbar.superclass.btn_edit_handler.call(this);
				}
			},
			btn_del_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要删除的节点！');
				}else if(this.app.leftTree.getSelectedNode().id =='0001'){
					uft.Utils.showWarnMsg('根节点不允许删除！');
				}else{
					T001Toolbar.superclass.btn_del_handler.call(this);
				}
			}
		});		
		${moduleName}.appUiConfig.leftTree=funTree;	
		${moduleName}.appUiConfig.treePkField='pk_fun';
		${moduleName}.appUiConfig.treeParentPkField='parent_id';
		${moduleName}.appUiConfig.toolbar = new T001Toolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		function afterChangeFun_property(value,originalValue){
			var bill_type = uft.Utils.getField('bill_type');
			if(value == '<%=org.nw.constants.FunRegisterConst.LFW_FUNC_NODE%>'){
				bill_type.setReadOnly(false);
			}else{
				bill_type.setReadOnly(true);
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
