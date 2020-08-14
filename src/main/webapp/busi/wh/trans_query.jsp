<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Report templetVO="${templetVO}" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false" headerGridPageSize="20"/>				
	</body>
	<script type="text/javascript">
		var app = new uft.jf.ReportPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
