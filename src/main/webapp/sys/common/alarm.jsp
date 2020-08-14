<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script>
		function billnoRenderer(v,meta,record){
			var type = record.get('type');
			if(String(type) == '0'){
				//代办事项
				var billId = record.get('pk_alarm');
				return '<a href="javascript:openNode(\''+billId+'\')">'+v+'</a>';
			}
			return v;			
		}
		function openNode(billId){
			var url = ctxPath+"/c/alarm/goTodo.html?pk_alarm="+billId;
			if(parent){
				parent.openNode(billId,'代办事项',url,true,true);
			}else{
				alert('该页面必须放在框架中！');
			}
		}		
		</script>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true" headerGridCheckboxSelectionModel="true"
				headerGridSingleSelect="false" isBuildHeaderCard="false"/>
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
