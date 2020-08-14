<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" isBuildHeaderGrid="false"  tableColumns="2"/>
	</body>
	<script type="text/javascript">
		var areaTree = new uft.extend.tree.Tree({
			id : 'areaTree',
			rootVisible : true,
			treeRootNodeText : '区域树',
			dataUrl : 'getAreaTree.json', //默认数据来源
			isTreeFilter:true,
			isRemoteFilter : true
		});
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		T103Toolbar = Ext.extend(uft.jf.TreeFormToolbar, {
			constructor : function (config){ 
				Ext.apply(this, config);
				T103Toolbar.superclass.constructor.call(this);
				this.btn_import.enabledStatus='ALL';
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_add);
				btns.push(this.btn_edit);
				btns.push(this.btn_del);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push(this.btn_import);
				return btns;
			},
			btn_add_handler : function(){
				T103Toolbar.superclass.btn_add_handler.call(this);
				//设置默认的编码
				var parentNode = this.app.leftTree.getSelectedNode();
				var code = uft.Utils.getField('code');
				code.setValue(parentNode.attributes['code']);
				code.focus();
			},
			btn_edit_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要修改的节点！');
				}else if(this.app.leftTree.getSelectedNode().id == this.app.leftTree.getRootNode().id){
					uft.Utils.showWarnMsg('根节点不允许修改！');
				}else{
					T103Toolbar.superclass.btn_edit_handler.call(this);
				}
			},
			btn_del_handler : function() {
				if(!this.app.leftTree.getSelectedNode()){
					uft.Utils.showWarnMsg('请先选中要删除的节点！');
				}else if(this.app.leftTree.getSelectedNode().id == this.app.leftTree.getRootNode().id){
					uft.Utils.showWarnMsg('根节点不允许删除！');
				}else{
					T103Toolbar.superclass.btn_del_handler.call(this);
				}
			},
			btn_import_handler : function(){
				var win = Ext.getCmp('importWin');
				if(!win){
					win = new uft.extend.UploadWindow({
						id:'importWin',
						params : this.app.newAjaxParams(),
						isLog : true,
						permitted_extensions : ['xls','xlsx']
					});
				}
				win.show();		
			}
		});		
		${moduleName}.appUiConfig.leftTree=areaTree;	
		${moduleName}.appUiConfig.treePkField='pk_area';
		${moduleName}.appUiConfig.treeParentPkField='parent_id';
		${moduleName}.appUiConfig.toolbar = new T103Toolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		areaTree.getLoader().on('load',function(loader,node){
			if(node.isRoot){
				//根节点
				node.expand();
			}
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
