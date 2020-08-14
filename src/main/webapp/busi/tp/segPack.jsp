<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
<head>
	<title>运段包装明细</title>
	<%@ include file="/common/ulwHeader.jsp"%>
</head>
<body>
<div class="sf-panel">
  <form name="form" method="POST">
    <!-- 表格 -->
    <div class="sf-content">
    	<!-- 表格内容 -->
	    <tms:table paginationVO="${paginationVO}" fieldVOs="${headerListFieldVOs}" isAddCheckbox="false" isAddProcessor="false"/>
	    <!-- 表格翻页栏 -->
	    <tms:pagingToolbar paginationVO="${paginationVO}"/>
	</div>
	<!-- 隐藏域 -->
	<%@ include file="/common/hidden.jsp"%>  
  </form>
</div>
</body>
<%@ include file="/common/footer.jsp"%>
</html>