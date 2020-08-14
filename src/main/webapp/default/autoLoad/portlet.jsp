<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<style type="text/css">
body{
	font-style:normal;
	font-weight:normal;
	font-size:12px;
}
.commentLi{
	padding: 8px 5px 0px 5px;
	height : 25px;
}
.commentTip {
	overflow-x: hidden;
	overflow-y: hidden;
	height: 15px;
	float: right;
	cursor: pointer;
	background-color: transparent;
	background-image: url(images/comment.gif);
	padding-left: 20px;
	background-repeat: no-repeat;
	background-attachment: scroll;
	background-position: 0% 0%;
	margin-left: 2px;
}
.commentMore {
	overflow-x: hidden;
	overflow-y: hidden;
	height: 15px;
	float: right;
	cursor: pointer;
	background-color: transparent;
	padding-left: 20px;
	background-repeat: no-repeat;
	background-attachment: scroll;
	background-position: 0% 0%;
	margin-left: 2px;
}
</style>
	<c:if test="${ empty dataList }">
      	暂时还没有该类的提醒
    </c:if>
    <c:if test="${ not empty dataList }">
	    <ul>
			<c:forEach items="${dataList}" var="mapObj" varStatus="status" >
				<li class="commentLi">
					<span class="commentTip" onclick="openPortletNode('','${funVO.fun_code}','${funVO.fun_name}','${funVO.class_name}','${mapObj.pk}')">详情</span>
					${mapObj._title}
				</li>
			</c:forEach>
				<li class="commentLi">
					<span class="commentMore"><a href="javascript:openPortletNodeWithBillIds('${pk_portlet}','${funVO.fun_code}','${funVO.fun_name}','${funVO.class_name}','${ billIds }');">更多</a></span>
				</li>
	    </ul>
    </c:if>
