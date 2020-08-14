<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.nw.Global"%>
<%
	boolean debug = Boolean.valueOf(Global.getPropertyValue("debug")).booleanValue();
	String theme = Global.getPropertyValue("theme");
	String version = Global.getResourceVersion();
	String errorMsg = request.getAttribute("errorMsg")==null?"":request.getAttribute("errorMsg").toString();
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>闰知RF操作系统-登录</title>
		<link href="<%=request.getContextPath() %>/css/login.css" rel="stylesheet" type="text/css" />
		<script src="<%=request.getContextPath() %>/js/jquery/jquery.js"></script>
<style type="text/css">  
html,body{height:100%;font-size:12px;}  

td {font-size: 12px;color: #000000;margin-left: 0px;margin-top: 0px;margin-right: 0px;margin-bottom: 0px;}
  
#main{width:100%;min-height:100%;height:100%;overflow:hidden !important;overflow: visible;}  

#header{height:170px;}  


input{      /* 统一设置所以样式 */
     font-family:Arial;
     font-size:55px;
     text-align:left;
     margin:3px;
}


</style> 
	</head>
	<body bgcolor="#009FCC">
			<table id="main" >  
			<form id="login_form" action="login.do" method="post" onSubmit="return formCheck();" >
				<tr ><td colspan="8"></td></tr>
				<tr id = "header" align="center">
					<td>&nbsp;</td>
					<!-- <td colspan="6" style="border-left:3px solid;border-right:3px solid;border-top:3px solid;border-bottom:0px solid; border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;"><font size="90px">欢迎使用闰知RF操作系统</font></td> -->
					<td colspan="6" style="border-left:3px solid;border-right:3px solid;border-top:3px solid;border-bottom:0px solid; border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;"><span style="font-size: 50pt; font-family:微软雅黑">欢迎使用闰知RF操作系统</span></td>
					<td>&nbsp;</td>
				</tr>
				<tr align="center" id = "header">
					<td>&nbsp;</td>
					<td colspan="2" align="right" style="border-left:3px solid;border-right:0px solid;border-top:0px solid;border-bottom:0px solid;border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white; " ><font size="55px">用户名：</font></td>
					<td colspan="4" align="left" style="border-left:0px solid;border-right:3px solid;border-top:0px solid;border-bottom:0px solid;border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;" ><input style="height:90px" type="text" id="user_code" name="user_code" /></td>
					<td>&nbsp;</td>
				</tr>
				<tr align="center" id = "header">
					<td>&nbsp;</td>
					<td colspan="2" align="right" style="border-left:3px solid;border-right:0px solid;border-top:0px solid;border-bottom:0px solid;border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;" ><font size="55px">密&nbsp;&nbsp;&nbsp;&nbsp;码：</font></td>
					<td colspan="4" align="left" style="border-left:0px solid;border-right:3px solid;border-top:0px solid;border-bottom:0px solid;border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;" ><input style="height:90px" type="password" id="user_password" name="user_password" /></td>
					<td>&nbsp;</td>
				</tr>
				<tr align="center"  style="height:200px;">
					<td>&nbsp;</td>
					<td colspan="6" style="border-left:3px solid;border-right:3px solid;border-top:0px solid;border-bottom:3px solid;border-left-color:white;border-right-color:white;border-top-color:white;border-bottom-color:white;" ><button type="submit" style="height:100px;width:260px;"; ><font size="140px">&nbsp;&nbsp;登&nbsp;&nbsp;录&nbsp;&nbsp;</font></button></td>
					<td>&nbsp;</td>
				</tr>
				
				<tr  ><td colspan="8"></td></tr>
				
			</form>		
			</table>
	<script type="text/javascript">
    $(function(){
    	//检测是否有错误信息
    	var errorMsg = '<%=errorMsg%>';
    	if(errorMsg){
    		alert(errorMsg);
    	}
    	
        $('#user_code').bind('keypress',function(event){
            if(event.keyCode == "13"){
                $('#user_password').focus();
            }
        });
        $('#user_password').bind('keypress',function(event){
            if(event.keyCode == "13"){
                $('#login_form').submit();
            }
        });
    });
    
    function formCheck(){
    	var user_code = $('#user_code').val();
    	var user_password = $('#user_password').val();
		if(!user_code){
			$('#user_code').focus();
			alert('用户名不能为空！');
			return false;
		}
		if(!user_password){
			$('#user_password').focus();
			alert('密码不能为空！');
			return false;
		}
    }
    </script>

	</body>
</html>

 

 
 
 
 
 
 
 
 
 