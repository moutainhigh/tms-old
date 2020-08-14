<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="true" bodyGridsImmediatelyLoad="true"/>
	</body>
	<script type="text/javascript">
		${moduleName}.appUiConfig.toolbar = new uft.jf.BodyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		var bodyGrid = app.getBodyGrids()[0];
		bodyGrid.addListener('beforeedit',function(e){
			var record = e.record;
			var fieldName=e.field;
			var exp_corp = record.get('pk_corp');
			var login_corp = '<%=loginInfo.getPk_corp() %>';
			var pk = record.get('pk_expense_type');
			if(login_corp != '0001' && exp_corp == '0001'){
				uft.Utils.showWarnMsg('非集团用户不允许修改集团数据！')
				return false;
			}
			if(fieldName == 'code' && pk && pk.trim() != ''){
				//如果该行记录已经存在pk，属于修改动作，不能修改编码
				return false;
			}
			var if_preset = record.get('if_preset');
			if(if_preset == 'Y' || if_preset=='true'){
				return false;
			}
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
