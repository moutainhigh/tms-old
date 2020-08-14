<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="true" bodyGridsImmediatelyLoad="true"/>
	</body>
	<script type="text/javascript">
	var itemTree = new uft.extend.tree.Tree({
		id : 'itemTree',
		treeRootNodeText:'功能菜单', //默认根节点名称
		rootVisible : true,
		dataUrl : 'getFunCodeTree.json', //默认数据来源
		isTreeFilter:true
	});
	${moduleName}.appUiConfig.leftTree=itemTree;	
	${moduleName}.appUiConfig.treePkField='fun_code';
	${moduleName}.appUiConfig.treeParentPkField='fun_code';

		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_add);
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
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		${moduleName}.appUiConfig.toolbar.on('beforeadd',function(toolbar){
			var node = this.app.leftTree.getSelectedNode();
			if(!node.leaf){
				uft.Utils.showWarnMsg('请选择叶子节点！');
				return false;
			}
		
			//检测是否还能新增，如果已经存在单据号规则，则不能新增，只能修改
			var result = Utils.request({
				type : false,
				url : 'checkCodeRule.json',
				params : {fun_code:this.app.leftTree.getSelectedNode().id}
			});
			if(result.data == 'N'){
				uft.Utils.showWarnMsg('公司编码规则已经存在，不能新增，只能修改！');
				return false;
			}else{
				//检测缓存是否存在
				if(!this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW)){
					//加载默认值
					this.app.loadDefaultValue();
				}			
				var appBufferData = this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW);
				if(appBufferData){
					//不是根节点,则加入父级节点参数
					appBufferData.HEADER[this.app.getTreeParentPkField()] = node.id;
					//这里调用setAppValues和setCardValues效果相同，因为肯定没有表体
					this.app.setCardValues(appBufferData.HEADER);
				}
			}
		},this);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
