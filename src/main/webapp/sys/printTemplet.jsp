<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridCheckboxSelectionModel="true" tableColumns="3" headerGridSingleSelect="true" headerGridImmediatelyLoad="false" 
		/>
	</body>
	<script type="text/javascript">
		var itemTree = new uft.extend.tree.Tree({
			id : 'itemTree',
			treeRootNodeText:'功能节点',
			rootVisible : true,
			dataUrl : 'getFunCodeTree.json',
			isTreeFilter:true
		});
		${moduleName}.appUiConfig.leftTree=itemTree;	
		${moduleName}.appUiConfig.treePkField='nodecode';
		
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_edit);
				btns.push(this.btn_del);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push(this.btn_card);
				btns.push(this.btn_list);
				btns.push(new uft.extend.Button({
					text : '上传模板',
					iconCls : 'btnUpload',
					scope : this,
					handler : function(){
						var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
						if(!record){
							//新增一条记录
							var node = this.app.leftTree.getSelectedNode();
							if(!node || !node.leaf){
								uft.Utils.showWarnMsg('请先选中末级节点！');
								return;
							}
							var win = new uft.extend.UploadWindow({
								submitUrl : 'upload.do',
								params : {nodecode:node.id},
								extendItems : [{name:'vtemplatecode',fieldLabel:'模板编码',xtype:'textfield',allowBlank:false,itemCls:'uft-form-label-not-null'},
								               {name:'vtemplatename',fieldLabel:'模板名称',xtype:'textfield',allowBlank:false,itemCls:'uft-form-label-not-null'}
								               ],
								permitted_extensions : ['jrxml']
							});
							win.on('aftersuccess',function(result){
								this.app.headerGrid.getStore().reload();
								win.close();
								this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
								this.app.statusMgr.updateStatus();
							},this);
							win.show();
						}else{
							//修改所选模板的模板文件
							var win = new uft.extend.UploadWindow({
								title : '修改模板文件，请选择jrxml类型的文件',
								submitUrl : 'upload.do',
								params : {billId:record.get(this.app.headerPkField)},
								permitted_extensions : ['jrxml']
							});
							win.on('aftersuccess',function(result){
								this.app.headerGrid.getStore().reload();
								win.close();
								this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
								this.app.statusMgr.updateStatus();
							},this);
							win.show();
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				btns.push(new uft.extend.Button({
					text : '下载模板',
					iconCls : 'btnDownload',
					scope : this,
					handler : function(){
						var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
						if(!record){
							uft.Utils.showWarnMsg('请选择一行记录！');
							return false;
						}
						window.location.href=ctxPath+"/sys/pt/download.do?pk_print_templet="+record.data['pk_print_templet'];
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				return btns;
			}
		});
		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
