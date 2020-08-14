<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="uft" uri="/WEB-INF/tlds/compress.tld" %>
<html>
	<head>
		<title>JS & CSS 合并压缩</title>
		<uft:compress jsTo="/js/baseUlw-min.js">
			<!--	注意引入文件的顺序不能随意修改	-->
			<script src="/js/jquery/jquery.js" type="text/javascript"></script>
			<script src="/js/jquery/tmpl.js" type="text/javascript"></script>
			<script src="/js/core/date.js" type="text/javascript"></script>
			<script src="/js/core/json2.js" type="text/javascript"></script>
			<script src="/js/core/common.js" type="text/javascript"></script>
			<script src="/js/core/Utils.js" type="text/javascript"></script>
			<script src="/js/core/base.js" type="text/javascript"></script>
	    </uft:compress>
	</head>
	<body>
	</body>
</html>
<%
out.println("压缩完成！");
%>
