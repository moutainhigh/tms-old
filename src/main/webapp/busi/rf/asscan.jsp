

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">

window.onload = function(){
	  var items = document.getElementsByTagName('input');
	    for(var i = 0; i < items.length; i ++) {

	        if (items[i].type == 'text') {

	            items[i].focus();break;
	        }
	    }
} 

	function keyDown(e) {
	  	var ev= window.event||e;
		//13是键盘上面固定的回车键
	 	 if (ev.keyCode == 13) {
	 		var value=document.getElementById("barcode").value;
	 		$.ajax({
	            type:"GET",
	            url:'<%=request.getContextPath()%>/rf/sc/validation.json',
	            data:value,
	            success:function(msg){  
	            	if(msg == "abnormal"){
	            		//这个条码在系统不存在。
	            		 if(confirm("这个条码不属于扫描范围内，是否扫描？")){
	            		    value = value +",";
	            		     addToSession(value);
	            		     
	            		     var scanNum = $("#scanNum").text();
	            		     scanNum++;
	            		     $("#scanNum").text(scanNum);
	            		     
	            		     var abnormalNum = $("#abnormalNum").text();
	            		     abnormalNum++;
	            		     $("#abnormalNum").text(abnormalNum);
	            		     
	            		     $("#barcode").val("");
	            		 }else{
	            			 $("#barcode").val("");
	            		 }
	            	}else if(msg == "normal"){
	            		 addToSession(value);
	            		 var scanNum = $("#scanNum").text();
            		     scanNum++;
            		     $("#scanNum").text(scanNum);
            		     
            		     $("#barcode").val("");
	            		
	            	}else if(msg == "repeat"){
	            		//这个条码已被扫描。
	            		 if(confirm("这个条码已经被扫描，是否扫删除？")){
	            			 var scanNum = $("#scanNum").text();
	            		     scanNum--;
	            		     $("#scanNum").text(scanNum);
	            		     $("#barcode").val("");
	            		     checkAbnormalBarCodes(value);
	            			 removeToSession(value);
	            		 }else{
	            			  $("#barcode").val("");
	            		 }
	            	} 
                }
	 		});
	  	}
	 }
	
	function addToSession(value) {
		$.ajax({
			type:"GET",
            url:'<%=request.getContextPath()%>/rf/sc/addToSession.json?code='+ value,
            data:value,
            success:function(msg){
            	
            }
		});
	}
	
	function removeToSession(value) {
		$.ajax({
			type:"GET",
            url:'<%=request.getContextPath()%>/rf/sc/remiveToSession.json?code='+ value,
            data:value,
            success:function(msg){
            	
            }
		});
	}

	function checkAbnormalBarCodes(value) {
		$.ajax({
			type:"GET",
            url:'<%=request.getContextPath()%>/rf/sc/checkAbnormalBarCodes.json?code='+ value,
            data:value,
            success:function(msg){
            	if(msg == "YES"){
            		var abnormalNum = $("#abnormalNum").text();
    		   		 abnormalNum--;
    		   	 	 $("#abnormalNum").text(abnormalNum);
            	}
            }
		});
	}
	
	function checkBarcodes() {
		var scanNum = $("#scanNum").text();
		if(scanNum == 0){
			alert("请输入条码！");
			return false;
		}else{
			$("#saveBttn").one("click", function () { 
				return false;
			}); 
		}
	}		
	
</script>
<title>到货扫描</title>

<style type="text/css">  
html,body{height:100%;font-size:12px;}  

td {font-size: 12px;color: #000000;margin-left: 0px;margin-top: 0px;margin-right: 0px;margin-bottom: 0px;}
  
#main{width:100%;min-height:100%;height:100%;overflow:hidden !important;overflow: visible;}  

#header{height:94px;border:1px solid red;}  



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

</head>
<body>
	<table id="main" border="3" bordercolor="FFFFFF">
	<tr bgcolor="#009FCC">
		<td colspan="4" align="center"><font size="40px">到货扫描</font></td>
	</tr>
	<tr bgcolor="#009FCC">
		<td colspan="4" align="left" ><font size="10px">批次号：${lot}</font></td>
	</tr>
	<tr bgcolor="#009FCC">
		<td colspan="4" align="left" ><font size="10px">承运商：${carr_name} </td>
	</tr>
	<tr bgcolor="#009FCC">
		<td colspan="4" align="left" ><font size="10px">总件数：${allNum} </td>
		<td style="display:none;" id = "abnormalBarCodes">${abnormalBarCodes}</td>
	</tr>
	<tr bgcolor="#009FCC">
	
		<td colspan="4" align="left"><font size="60px" > 条码：</font><input type="text" name="barcode" id="barcode" onkeydown="keyDown(event)"></td>
	</tr>
	<tr bgcolor="#009FCC">
		<td colspan="2" align="center" ><font size="10px">扫描件数：<span style="color: red" id = "scanNum"><font size="10px">0</span></td> 
		<td colspan="2" align="center" ><font size="10px">异常件数：<span style="color: red" id = "abnormalNum"><font size="10px">0</span></td> 
	</tr>

	<tr  align="center" bgcolor="#009FCC">
		<td colspan="2"> <a href="<%=request.getContextPath()%>/busi/rf/asshow.jsp">返回</a>  </td>
		<td colspan="2"> <a id="saveBttn" href="<%=request.getContextPath()%>/rf/sc/arriScanConfirm.json" onclick="return checkBarcodes();">完成</a></td>
	</tr>
	</table>
</body>
</html>