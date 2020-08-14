<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header_lite.jsp"%>
		<title>默认面板</title>
		<link rel="stylesheet" type="text/css" href="<c:url value="/css/Portal.css"/>" />
		<%if(debug){ %>
		<script type='text/javascript' src='<c:url value="/js/ext/portal/Portal.js?v=${version}"/>'></script>
		<script type='text/javascript' src='<c:url value="/js/ext/portal/PortalColumn.js?v=${version}"/>'></script>
		<script type='text/javascript' src='<c:url value="/js/ext/portal/Portlet.js?v=${version}"/>'></script>
		<script type='text/javascript' src='<c:url value="/default/default.js?v=${version}"/>'></script>
		<script type='text/javascript' src='<c:url value="/default/UserPortlet.js?v=${version}"/>'></script>
		<%}else{ %>
			<script type="text/javascript" src='<c:url value="/default/default-min.js?v=${version}"/>'></script>
		<%} %>
		<script type='text/javascript' src='<c:url value="/sys/common/SmsSender.js?v=${version}"/>'></script>
	</head>
	<body>
		<script type="text/javascript">
	   	 	Ext.onReady(function(){
			   	var portletCounter = [];
			   	//动态更新
			   	function portletPull() {
			   		for (var i = 0, iLen = portletCounter.length; i < iLen; i++) {
			   			var portlet = portletCounter[i];
			   			if(portlet.autoLoad){
			   				portlet.getUpdater().update(portlet.autoLoad);
			   			}
			   		}
			   	}
			   	window.setInterval(portletPull, 60000*5);//5分钟刷新一次
			   	var pcVOs = ${pcVOs};
		 	 	var portal = new DefaultPortal({pcVOs:pcVOs,portletCounter:portletCounter});
		 	 }); 
		</script>	
	</body>
	<%@ include file="/common/footer.jsp"%>
</html>

