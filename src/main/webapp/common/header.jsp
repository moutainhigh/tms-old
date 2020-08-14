<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nw" uri="/WEB-INF/tlds/nw.tld" %>
<%@ page import="org.nw.web.vo.LoginInfo"%>
<%@ page import="org.nw.web.utils.WebUtils"%>
<%@ page import="org.nw.Global"%>
<%
	boolean debug = Boolean.valueOf(Global.getPropertyValue("debug")).booleanValue();
	String theme = Global.getPropertyValue("theme");
	String version = Global.getResourceVersion();
	LoginInfo loginInfo=WebUtils.getLoginInfo();
	
%>
<div id="loading-beforehtml">
	<div style="position: absolute; z-index: 20000; left: 45%; top: 40%;">
		<img src="<c:url value="/images/loading.gif"/>" style="margin-right:8px;float:left;">
		<span style="font: normal 12px arial;">加载中，请稍候...</span>
	</div>
</div>
<script type="text/javascript" >
	window.onbeforeunload = function(){
		if(window.checkLeave !== false){
			if(app && app.statusMgr && (app.statusMgr.getCurrentPageStatus()==1 
					|| app.statusMgr.getCurrentPageStatus()==4
					|| app.statusMgr.getCurrentPageStatus()==5
					|| app.statusMgr.getCurrentPageStatus()==6
					|| app.statusMgr.getCurrentPageStatus()==7)){
				return ("数据未保存，您确定要离开吗？");
			}
		}
	};
	
	Constants = {};
	Constants.DEBUG = <%=debug%>;
	Constants.timeOut = 90000;
	Constants.csstheme='<%=theme%>';
	Constants.pageSize = <%=org.nw.utils.ParameterHelper.getPageSize()%>; //读取个性化配置中的单据页大小信息
	ctxPath = '<%=request.getContextPath()%>';
	language = '<%=loginInfo.getLanguage() %>';
	resourceCtxPath = ctxPath;
</script>
<c:set var="version"><%=version %></c:set>
<c:set var="theme"><%=theme %></c:set>
<%if(debug){ %>
	<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/jquery/ajaxfileupload.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/core/common.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/ext/import/ext.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/base.js"/>"></script>
	<link href="<c:url value="/theme/${theme}/css/xtheme.css"/>" rel="stylesheet" type="text/css"></link>
<%}else{ %>
	<script type="text/javascript" src="<c:url value="/js/base-min.js?v=${version}"/>"></script>
	<link href="<c:url value="/theme/${theme}/css/base-min.css?v=${version}"/>" rel="stylesheet" type="text/css"></link>			
<%} %>
	<script type="text/javascript" src='<c:url value="/js/ext/jf/UnConfirm.js?v=${version}" />'></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />