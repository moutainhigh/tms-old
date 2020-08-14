<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
		<script type="text/javascript">
		var statusMgr = new uft.jf.StatusMgr();
		var funTree = new uft.extend.tree.Tree({
			id : 'funTree',
			treeRootNodeText:'功能菜单', //默认根节点名称
			rootVisible : true,
			border : false,
			enableDD: true,//节点是否可拖动由节点的draggable属性决定
	        containerScroll: true,
			dataUrl : 'getFunTree.json',
			onstatuschange : function(tree,status){
				if(status==uft.jf.pageStatus.OP_INIT){
					tree.disable();
				}else if(status==uft.jf.pageStatus.OP_EDIT){
					tree.enable();
				}
			}
		});
		UFunToolbar = function(config){
			Ext.apply(this, config);
			this.btn_edit = new uft.extend.Button({
				text : '修改模块内结构',
				tooltip : 'Ctrl+E',
				keyBinding: {
		            key: 'e',
		            ctrl : true
		        },
				handler : this.btn_edit_handler,
				enabledStatus:[uft.jf.pageStatus.OP_INIT]
			});
			this.btn_save = new uft.extend.Button({
				text : '保存',
				tooltip : 'Ctrl+S',
				keyBinding: {
		            key: 's',
		            ctrl : true
		        },
				iconCls : 'btnSave',
				handler : this.btn_save_handler,
				enabledStatus:[uft.jf.pageStatus.OP_EDIT]
			});
			this.btn_cancel = new uft.extend.Button({
				text : '取消',
				tooltip : 'Ctrl+Q',
				keyBinding: {
		            key: 'q',
		            ctrl : true
		        },
				iconCls : 'btnCancel',
				handler : this.btn_cancel_handler,
				enabledStatus:[uft.jf.pageStatus.OP_EDIT]
			});
			this.btn_ref = new uft.extend.Button({
				text : '刷新',
				tooltip : 'Ctrl+R',
				keyBinding: {
		            key: 'r',
		            ctrl : true
		        },
				iconCls : 'btnRef',
				handler : this.btn_ref_handler,
				enabledStatus:[uft.jf.pageStatus.OP_INIT]
			});
			UFunToolbar.superclass.constructor.call(this,{
				autoHeight : true,
				defaults : {
					scope : this
				},
				plugins:new Ext.ux.ToolbarKeyMap(),
				items : [this.btn_edit,this.btn_save,this.btn_cancel,this.btn_ref]
			});
		};
		Ext.extend(UFunToolbar,Ext.Toolbar, {
			btn_edit_handler : function(){
				statusMgr.setPageStatus(uft.jf.pageStatus.OP_EDIT);
				statusMgr.updateStatus();
			},
			btn_save_handler : function(){
				var nodeAry = [],index=0;
				funTree.getRootNode().cascade(function(node){
					if(!node.isRoot){//不能调节父节点
						var param = {};
						param.pk_fun = node.id;
						if(node.parentNode.isRoot){
							//父节点是根节点
							param.parent_id = '0001';
						}else{
							param.parent_id = node.parentNode.id;
						}
						param.display_order = index;
						nodeAry.push(param);
						index++;
					}
				});
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : {nodeAry: Ext.encode(nodeAry)},
			    	url : 'saveDisplayOrder.json',
			    	isTip : true,
			    	success : function(values){
			    		statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
						statusMgr.updateStatus();
			    	}
			    });						
			},
			btn_cancel_handler : function(){
				statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
				statusMgr.updateStatus();
				funTree.getRootNode().reload();
			},
			btn_ref_handler : function(){
				funTree.getRootNode().reload();
			}
		});
		var toolbar = new UFunToolbar();
		var app = new Ext.Panel({
			layout:'fit',
			border : false,
			tbar : toolbar,
			renderTo : document.body,
			height : document.documentElement.clientHeight,
			items : [funTree]			
		});
		Ext.onReady(function(){
			var items = toolbar.items.items;
			for(var i=0;i<items.length;i++){
				statusMgr.addListener(items[i],items[i].onstatuschange);
				statusMgr.addBizListener(items[i],items[i].onbizstatuschange);
			}
			statusMgr.addListener(funTree,funTree.onstatuschange);
			statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
			statusMgr.updateStatus();
		});
		</script>	
	</body>
	<%@ include file="/common/footer.jsp"%>
</html>
	
