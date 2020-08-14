<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/sys/activiti/WorkflowViewer.js"/>"></script>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" 
		bodyGridsPagination="false,false" useFieldSetInHeader="true"  headerGridPageSize="20"
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(new uft.extend.Button({
					text : '发布流程',
					scope : this,
					handler : function(){
						var permitted_extensions = ['xml','bpmn','png'];
						var fileUpload = new uft.jf.FileUpload({title:'请选择要部署的文件，文件类型[xml,bpmn,png]',uploadUrl:'deploy.do',inputNum:2,permitted_extensions:permitted_extensions}).show();
						fileUpload.on('fileupload',function(uploadField){
							this.app.headerGrid.getStore().reload();
							this.fireEvent('upload',this);
						},this);
					},
					enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
				}));
				btns.push(new uft.extend.Button({
					text : '启动流程',
					scope : this,
					handler : function(){
						var rs = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!rs || rs.length != 1){
							uft.Utils.showWarnMsg('有且只能选择一条记录！');
							return false;
						}
						var r = rs[0];
						var params = this.app.newAjaxParams();
						params['billId'] = r.get('id_');
						uft.Utils.doAjax({
					    	scope :  this,
					    	params : params,
					    	url : 'startFlow.json',
					    	success : function(result){
					    		
					    	}
					    });
					}
				}));
				btns.push(new uft.extend.Button({
					text : '预览流程图',
					scope : this,
					handler : function(){
						var rs = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!rs || rs.length != 1){
							uft.Utils.showWarnMsg('有且只能选择一条记录！');
							return false;
						}
						var r = rs[0];
						var deployment_id = r.get('id_');
						new org.nw.WorkflowViewer({deployment_id:deployment_id}).show();
					}
				}));
				return btns;
			}
		});
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
