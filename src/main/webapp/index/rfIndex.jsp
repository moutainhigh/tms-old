<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>闰知RF操作系统</title>
<style type="text/css">  
html,body{height:100%;font-size:12px;}  

td {font-size: 12px;color: #000000;margin-left: 0px;margin-top: 0px;margin-right: 0px;margin-bottom: 0px;}
  
#main{width:100%;min-height:100%;height:100%;overflow:hidden !important;overflow: visible;}  

#header{height:94px;border:1px solid red;}  

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

</head>
<body>  
<table id="main" border="3" bordercolor="FFFFFF">  
   	<tr  bgcolor="#009FCC" >
		<td align="center">
			<a href="busi/rf/ds.jsp" >提货扫描</a> 
		</td>
		<td align="center">
			<a href="busi/rf/as.jsp">到货扫描</a> 
		</td>
	</tr>
	
	<tr   bgcolor="#009FCC">
		<td align="center">
			<a href="busi/rf/ar.jsp">到达</a> 
		</td>
		<td align="center">
			<a href="busi/rf/le.jsp">离开</a> 
		</td>
	</tr>
	 
	<tr  bgcolor="#009FCC">
		<td align="center">
			<a href="<%=request.getContextPath()%>/rflogin.jsp">退出</a> 
		</td>
		<td align="center">
		</td>
	</tr> 
</table>  
</body>  
</html>