<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="st" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>到货扫描</title>
<style type="text/css">  
html,body{height:100%;font-size:12px;}  

td {font-size: 12px;color: #000000;margin-left: 0px;margin-top: 0px;margin-right: 0px;margin-bottom: 0px;}
  
#main{width:100%;min-height:100%;height:100%;overflow:hidden !important;overflow: visible;}  

#header{height:94px;}  

font{      /* 统一设置所以样式 */
     font-family:Arial;
     font-size:80px;
     margin:3px;
}

input{      /* 统一设置所以样式 */
     font-family:Arial;
     font-size:60px;
     text-align:left;
     margin:3px;
}

a{      /* 统一设置所以样式 */
     font-family:Arial;
     font-size:100px;
     text-align:center;
     margin:3px;
}
a:link,a:visited{  /* 超链接正常状态、被访问过的样式 */
    color:#A62020;
    padding:4px 10px 4px 10px;
    background-color:#FF7744;
    text-decoration:none;
 
    border-top:1px solid #EEEEEE; /* 边框实现阴影效果 */
    border-left:1px solid #EEEEEE;
    border-bottom:1px solid #717171;
    border-right:1px solid #717171;
}
a:hover{       /* 鼠标指针经过时的超链接 */
    color:#821818;     /* 改变文字颜色 */
    padding:5px 8px 3px 12px;  /* 改变文字位置 */
	background-color:#e2c4c9;  /* 改变背景色 */
	border-top:1px solid #717171; /* 边框变换，实现“按下去”的效果 */ 
    border-left:1px solid #717171;
    border-bottom:1px solid #EEEEEE;
    border-right:1px solid #EEEEEE;
}

</style> 
<script type="text/javascript">

window.onload = function(){
	  var items = document.getElementsByTagName('input');
	    for(var i = 0; i < items.length; i ++) {

	        if (items[i].type == 'text') {

	            items[i].focus();break;
	        }
	    }
	} 

function go() {
	var lot=document.getElementById("lot").value;
	var page=document.getElementById("page").value;
	var operate_type=document.getElementById("operate_type").value;
	var nowPage=document.getElementById("nowPage").value;
	location.href="<%=request.getContextPath()%>/rf/sc/getScan.json?lot="+lot+"&operate_type="+operate_type+"&page="+page+"&nowPage="+nowPage;
	return false;
}

function keyDown(e) {
  	var ev= window.event||e;
	//13是键盘上面固定的回车键
 	 if (ev.keyCode == 13) {
 		var lot=document.getElementById("lot").value;
		var page=document.getElementById("page").value;
		var operate_type=document.getElementById("operate_type").value;
		var nowPage=document.getElementById("nowPage").value;
		location.href="<%=request.getContextPath()%>/rf/sc/getScan.json?lot="+lot+"&operate_type="+operate_type+"&page="+page+"&nowPage="+nowPage;
		return false; 
  	}
 }
	
</script>
</head>
<body>

<table id="main" border="3" bordercolor="FFFFFF">  
   		<tr id = "header" bgcolor="#009FCC">
			<td align="center" colspan="2">
					<font >到货扫描</font>	
			</td>
		</tr>
		
		<tr bgcolor="#009FCC">
			<td align="center" colspan="2">
				<font >	批次号：</font><input style="height: 100px;" type="text" name="lot" id="lot"   onkeydown="keyDown(event)">
				<!-- 设置隐藏域传递页面信息，避免重复代码 -->
				<input type="hidden" name="page" id="page" value="asshow.jsp">
				<input type="hidden" name="nowPage" id="nowPage" value="as.jsp">
				<input type="hidden" name="operate_type" id="operate_type" value="2">
			</td >
		</tr>
		
		<tr bgcolor="#009FCC">
			<td align="center">
				<a href="<%=request.getContextPath()%>/rfIndex.html" >返回</a> 
		    </td>
		    <td align="center">
		  		<a href="#" onclick="return go()">确认<a/>
		    </td>
		</tr>  
</table> 

</body>
</html>