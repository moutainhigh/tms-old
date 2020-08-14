<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>新闻中心</title>
	<script charset="utf-8" type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
	<script charset="utf-8" src="<c:url value="/js/core/base.js"/>"></script>
	<link href="<c:url value="/css/ufoa.css"/>" rel="stylesheet" type="text/css"></link>
	<link href="<c:url value="/css/list.css"/>" rel="stylesheet" type="text/css"></link>
</head>
<body>
<div class="sf-panel">
    <div class="content clearfix">
		<div class="global-main">
           <div class="global-wrap">
                <div class="inner">
                	<form name="form" method="POST" >
                    <div class="mod-search">
                        <input name="keyword" placeholder="请输入关键字查询" class="search-text" value="${keyword}">
                        <button type="submit" title="搜索" class="search"></button>
                        <button type="button" onclick="location.href='edit.html?funCode=${funCode}'" class="addbtn">新增</button>
                        <button type="button" onclick="deleteNews('${funCode}')" class="addbtn">删除</button>
                    </div>
                    <div class="boxlist">
						<table class="bought">
							<thead>
								<tr class="sep-start">
									<th style="width:30px"><input type="checkbox" id="checkbox_0" onclick='selectAll(this)' /></th>
									<th>标题</th>
									<th>标签</th>
									<th>发布人</th>
                                    <th>来源</th>
                                    <th style="width:80px">发布日期</th>
                                    <th style="width:60px">阅读次数</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${newsVOs}" var="newsVO" varStatus="status">
	                                <tr class="">
	                                    <td class="txc">
	                                        <input type="checkbox" id="checkbox_${status.index+1}" pk_news="${newsVO.pk_news}"/>
	                                    </td>
	                                    <td><a href="edit.html?funCode=${funCode}&pk_news=${newsVO.pk_news}">${newsVO.title}</a></td>
	                                    <td>${newsVO.tags}</td>
	                                    <td>${newsVO.publisher_name}</td>
	                                    <td>${newsVO.source}</td>
	                                    <td>${newsVO.post_date}</td>
	                                    <td align="right">${newsVO.read_num}</td>
	                                </tr>
                                </c:forEach>
							</tbody>
						</table>
					</div>
                    <div class="pagenum"><span>共${totalCount}条记录</span>&nbsp;&nbsp;&nbsp;&nbsp;${pageHtml}</div>
                    <input type="hidden" name="pk_news" value="${pk_news}" />
                    <input type="hidden" name="funCode" value="${funCode}"/>
                    <input type="hidden" name="PAGE_PARAM_START" value="${PAGE_PARAM_START}"/>
 					<input type="hidden" name="PAGE_PARAM_LIMIT" value="${PAGE_PARAM_LIMIT}"/>
                    </form>
                </div>         
           </div>
        </div>
    </div>
</div>
</body>
</html>
<script>
function deleteNews(funCode){
	var ids = getSelectedIds('pk_news');
	if(ids.length == 0){
		alert('请选择要删除的行！')
		return;
	}
	var bool = window.confirm("您确定删除吗?");
   	if(bool){
		var idstring = ids.join(",");
		location.href='delete.do?funCode='+funCode+'&pk_news='+idstring;
   	}
}
</script>