<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.nw.web.vo.LoginInfo"%>
<%@ page import="org.nw.web.utils.WebUtils"%>
<%@ page import="org.nw.Global"%>
<%
	boolean debug = Boolean.parseBoolean(Global.getPropertyValue("debug"));
	String theme = Global.getPropertyValue("theme");
	String version = Global.getPropertyValue("version");
	LoginInfo loginInfo=WebUtils.getLoginInfo();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>设置</title>
<link href="<c:url value="/css/style.css"/>" type="text/css" rel="stylesheet" />
<script src="<c:url value="/js/jquery/jquery.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/core/common.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/core/Utils.js"/>" type="text/javascript"></script>
</head>
<body>
<div id="loading-beforehtml">
	<div style="position: absolute; z-index: 20000; left: 45%; top: 40%;">
		<img src="<c:url value="/images/loading.gif"/>" style="margin-right:8px;float:left;">
		<span style="font: normal 12px arial;">加载中，请稍候...</span>
	</div>
</div>
<div class="home">
    <form name="myForm" method="post" action="<c:url value="/password/change.do"/>" onsubmit="return checkdata(this);">
    <div class="inner">
   	     <div class="mod-title">
            <h3>修改密码</h3>
        </div>
        <div class="mod-info">
           <ul>
                <li>
	                <label>当前密码：</label>
	                <input name="oldPwd" type="password" value="${pwd.oldPwd}" class="minp" onblur="checkOldPassword()" />
	                <em>
	               		<span id="errPwd"></span>
	               		<form:errors path="pwd.oldPwd" cssErrorClass="error"/>
	                </em>
                </li>
                <li>
	                <label>新密码：</label>
	                <input name="newPwd" type="password" value="${pwd.newPwd}" class="minp" onblur="checkNewPassword()"/>
	                <em>
	               		<span id="errNewPwd"></span>
	                </em>
                </li>
                <li>
	                <label>确认密码：</label>
	                <input name="rePwd" type="password"  value="${pwd.rePwd}" class="minp" onblur="checkNewPasswordConfirm()"/>
	                <em>
	                	<span id="errNewConfirmPwd"></span>
	                	<form:errors path="pwd.newPwd" cssErrorClass="error"/>
	                </em>
                </li>
                <li class="mod-save">
                	<button type="Submit" class="sbtn">保存</button>
                	<c:if test="${result==\"true\"}">
				    	<div class="mod-suc">保存成功</div>
				    </c:if>
                </li>
            </ul>
        </div>
   	</div>
   	</form>
</div>
</body>
<%@ include file="/common/footer.jsp"%>
<script type="text/javascript">
//检查旧密码
function checkOldPassword(){
	var oldPwd=document.myForm.oldPwd.value;
	var hasErr = false;
	//检查旧密码
	if(oldPwd.length < 1){
		document.getElementById("errPwd").innerHTML = "请输入您现在的密码";
		document.getElementById("errPwd").style.display = "inline-block";
		hasErr = true;
	}else{
		document.getElementById("errPwd").style.display = "none";
	}
	return hasErr;	
}
//检查新密码
function checkNewPassword(){
	var oldPwd=document.myForm.oldPwd.value;
	var newPwd=document.myForm.newPwd.value;
	var errNewPwdMsg = "";
	var hasErr = false;
	if(newPwd.length < 1){
		errNewPwdMsg = "请输入新密码";
	}
	if(errNewPwdMsg != ""){
		document.getElementById("errNewPwd").innerHTML = errNewPwdMsg;
		document.getElementById("errNewPwd").style.display = "inline-block";
		document.myForm.newPwd.className="minp error";
		hasErr = true;
	}else{
		document.getElementById("errNewPwd").style.display = "none";
		document.myForm.newPwd.className="minp";
	}
	return hasErr;
}
//检查新密码确认
function checkNewPasswordConfirm(){
	var newPwd=document.myForm.newPwd.value;
	var rePwd=document.myForm.rePwd.value;
	var hasErr = false;
	if(rePwd != newPwd ){
		document.getElementById("errNewConfirmPwd").innerHTML = "您两次输入的新密码不一致，请确认";
		document.getElementById("errNewConfirmPwd").style.display = "inline-block";
		document.myForm.rePwd.className="minp error";
		hasErr  = true;
	}else{
		document.getElementById("errNewConfirmPwd").style.display = "none";
		document.myForm.rePwd.className="minp";
	}
	return hasErr;
}
function checkdata(myForm) {
	//检查旧密码
	if(checkOldPassword()){
		document.myForm.oldPwd.focus();
	    return false;
	}
	
	//检查新密码
	if(checkNewPassword()){
		document.myForm.newPwd.focus();
	    return false;
	}
	
	//检查新密码确认
	if(checkNewPasswordConfirm()){
		document.myForm.rePwd.focus();
	    return false;
	}
}
$(function() {
    $(".mod-suc").animate({opacity: 1.0}, 1000).fadeOut("normal",function(){
 	   $(this).remove();
 	}); 
});
</script>
</html>