<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="uft" uri="/WEB-INF/tlds/compress.tld" %>
<html>
	<head>
		<title>JS & CSS 合并压缩</title>
		<uft:compress jsTo="/default/default-min.js">
			<!--	注意引入文件的顺序不能随意修改	-->
			<script type="text/javascript" src="/js/ext/portal/Portal.js"></script>
			<script type="text/javascript" src="/js/ext/portal/PortalColumn.js"></script>
			<script type="text/javascript" src="/js/ext/portal/Portlet.js"></script>
			<script type="text/javascript" src="/default/default.js"></script>
			<script type="text/javascript" src="/default/UserPortlet.js"></script>
	    </uft:compress>
	</head>
	<body>
	</body>
</html>
<%
out.println("压缩完成！");
%>
