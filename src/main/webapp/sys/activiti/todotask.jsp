<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" isBuildHeaderCard="false"
		bodyGridsPagination="false,false" useFieldSetInHeader="true"  headerGridPageSize="20"
		bodyGridsDataUrl="loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_ref);
				btns.push(new uft.extend.Button({
					text : '审核',
					scope : this,
					handler : function(){
						var rs = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!rs || rs.length != 1){
							uft.Utils.showWarnMsg('有且只能选择一条待办进行处理！');
							return false;
						}
						var r = rs[0];
						var task_id = r.get('id_');
						//new uft.jf.WorkflowApprove({billId:task_id,app:this.app}).show();
						var r = rs[0];
						var params = this.app.newAjaxParams();
						params['billId'] = r.get('id_');
						uft.Utils.doAjax({
					    	scope :  this,
					    	params : params,
					    	url : 'approve.json',
					    	success : function(result){
					    		
					    	}
					    });
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
	
