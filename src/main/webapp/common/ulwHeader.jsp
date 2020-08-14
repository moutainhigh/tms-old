<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tms" uri="/WEB-INF/tlds/nw.tld" %>
<%@ page import="org.nw.web.vo.LoginInfo"%>
<%@ page import="org.nw.web.utils.WebUtils"%>
<%@ page import="org.nw.Global"%>
<%
	boolean debug = Boolean.parseBoolean(Global.getPropertyValue("debug"));
	String theme = Global.getPropertyValue("theme");
	String version = Global.getPropertyValue("version");
	LoginInfo loginInfo=WebUtils.getLoginInfo();
%>
<div id="loading-beforehtml">
	<div style="position: absolute; z-index: 20000; left: 45%; top: 40%;">
		<img src="<c:url value="/images/loading.gif"/>" style="margin-right:8px;float:left;">
		<span style="font: normal 12px arial;">加载中，请稍候...</span>
	</div>
</div>
<link href="<c:url value="/css/style.css"/>" type="text/css" rel="stylesheet" />
<%if(debug){ %>
	<script src="<c:url value="/js/jquery/jquery.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/js/core/date.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/js/core/json2.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/js/core/common.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/js/core/Utils.js"/>" type="text/javascript"></script>
	<script src="<c:url value="/js/core/base.js"/>" type="text/javascript"></script>
<%}else{ %>
	<script type="text/javascript" src="<%=request.getContextPath() %>/js/baseUlw-min.js?v=<%=version%>"></script>
<%} %>


