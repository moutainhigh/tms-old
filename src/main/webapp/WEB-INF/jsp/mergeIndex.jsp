<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="uft" uri="/WEB-INF/tlds/compress.tld" %>
<html>
	<head>
		<title>JS & CSS 合并压缩</title>
		<uft:compress jsTo="/index/index-min.js">
			<!--	注意引入文件的顺序不能随意修改	-->
			<script type="text/javascript" src="/index/Index.js"></script>
			<script type="text/javascript" src="/index/Workspace.js"></script>
			<script type="text/javascript" src="/index/QuickLink.js"></script>
			<script type="text/javascript" src="/index/ShortcutPanel.js"></script>
	    </uft:compress>
	</head>
	<body>
	</body>
</html>
<%
out.println("压缩完成！");
%>
