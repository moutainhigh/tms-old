<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%
	String errorMsg = request.getParameter("errorMsg");
	if(errorMsg == null){
		errorMsg = (String)request.getAttribute("errorMsg");
	}
	if(errorMsg!=null){
		errorMsg = new String(Base64.decodeBase64(errorMsg.getBytes()));
	}
%>
<html>
<head>
	<title>403 - 缺少权限</title>
</head>
<body>
<div>
	<div><h1>你没有访问该页面的权限.</h1></div>
	<%
		if(errorMsg != null){
			try{
	%>
				<div>错误信息：<%=errorMsg %>" </div>
	<%
			}catch(Exception e){
				
			}
	}
	%>
	<div><a href="javascript:window.history.back()">返回</a> <a href="<c:url value="/logout.do"/>">退出登录</a></div>
</div>
</body>
</html>