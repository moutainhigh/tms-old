<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="uft" uri="/WEB-INF/tlds/compress.tld" %>
<html>
	<head>
		<title>JS & CSS 合并压缩</title>
		<uft:compress jsTo="/busi/tp/sto-min.js">
			<script type="text/javascript" src='/busi/tp/SegmentWindow.js'></script>
			<script type="text/javascript" src='/busi/tp/QuantityWindow.js'></script>
			<script type="text/javascript" src='/busi/tp/BatchPZ.js'></script>
			<script type="text/javascript" src='/busi/tp/Delegate.js'></script>
	    </uft:compress>
	</head>
	<body>
	</body>
</html>
<%
out.println("压缩完成！");
%>
