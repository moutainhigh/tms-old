<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>${newsVO.title}</title>
	<link href="<c:url value="/css/ufoa.css"/>" rel="stylesheet" type="text/css"></link>
	<link href="<c:url value="/css/list.css"/>" rel="stylesheet" type="text/css"></link>
</head>
<body>
<div class="sf-panel">
    <div class="content clearfix">
		<div class="global-main">
        	<div class="global-wrap global-full">
          		<div class="bd-content">
					<h2>${newsVO.title}</h2>
					<div class="time">
						发布日期：${newsVO.post_date}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;来源：${newsVO.source}
					</div>
					<div class="bd-detail">
					</div>
					<div class="download">
						${newsVO.content}
					</div>
					<div class="print">
						<a href="javascript:closeWin();" id="closewindow">[关闭窗口]</a>
					</div>
				</div>
        </div>
    </div>
</div>
</div>
</body>
</html>
<script>
	function closeWin(){
		if(parent && typeof(parent.closeNode) == 'function'){
			parent.closeNode('${newsVO.pk_news}');
		}else{
			window.close();
		}
	}
</script>