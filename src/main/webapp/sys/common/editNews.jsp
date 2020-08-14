<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>编辑新闻</title>
	<script charset="utf-8" type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
	<script charset="utf-8" type="text/javascript"src="<c:url value="/js/editor/kindeditor.js"/>"></script>
	<script charset="utf-8" type="text/javascript" src="<c:url value="/js/core/base.js"/>"></script>
	<link href="<c:url value="/css/style.css"/>" rel="stylesheet" type="text/css"></link>
	<link href="<c:url value="/css/list.css"/>" rel="stylesheet" type="text/css"></link>
</head>
<body>
<div class="sf-panel">
    <div class="content clearfix">
		<div class="global-main">
           <div class="global-wrap  global-full">
                <div class="inner">
                    <div class="mod-title">
                    	<h3>编辑新闻</h3>
                    </div>
                    <div class="mod-info">
                    <form name="form" method="POST" action="saveNews.do" onsubmit="return formCheck();">
                        <ul>
                        	<li>
                            <label>标题：</label>
                            <input name="title" type="text" class="minp" maxlength="100" value="${newsVO.title}" style="width:400px">
                            </li>
                            <li>
                            <label>内容：</label>
                            <textarea id="note" name="content" style="width:800px;height:400px;visibility:hidden;">${newsVO.content}</textarea>
	                        <script>
								var editor;
								KindEditor.ready(function(K) {
									editor = K.create('textarea[name="content"]');
								});
							</script>
                            </li>
                            <li>
                            <label>标签：</label>
                            <input name="tags" type="text" class="minp" maxlength="50" value="${newsVO.tags}">
                            </li>
                            <li>
                            <label>来源：</label>
                            <input name="source" type="text" class="minp" maxlength="50" value="${newsVO.source}">
                            </li>
                            <li class="mod-save">
                            <button type="Submit" class="modbtn">保存</button>
                            <button type="button" class="cancelbtn" onclick="preview()">预览</button>
<!--                             <button type="button" class="cancelbtn" onclick="history.back()">取消</button> -->
                            </li>
                        </ul>
                        <input type="hidden" name="dr" value="${newsVO.dr}" />
                        <input type="hidden" name="ts" value="${newsVO.ts}" />
                        <input type="hidden" name="publisher" value="${newsVO.publisher}" />
                        <input type="hidden" name="post_date" value="${newsVO.post_date}" />
                        <input type="hidden" name="pk_corp" value="${newsVO.pk_corp}" />
                        <input type="hidden" name="read_num" value="${newsVO.read_num}" />
                        <input type="hidden" name="pk_news" value="${newsVO.pk_news}" />
	                    <input type="hidden" name="funCode" value="${funCode}"/>
                        </form>
                    </div>
                </div>         
           </div>
        </div>
    </div>
</div>
</body>
</html>
<script>
function formCheck(){
	var title = document.form.title.value;
	if(!title || title.length == 0){
		alert('新闻标题是必须的!');
		document.form.title.focus();
		return false;
	}
}
/**
 * 预览
 */
function preview(){
	editor._preview();
}
</script>