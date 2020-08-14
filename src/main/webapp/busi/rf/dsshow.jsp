<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>提货扫描</title>

<style type="text/css">  
html,body{height:100%;font-size:12px;}  

td {font-size: 12px;color: #000000;margin-left: 0px;margin-top: 0px;margin-right: 0px;margin-bottom: 0px;}
  
#main{width:100%;min-height:100%;height:100%;overflow:hidden !important;overflow: visible;}  

#header{height:94px;border:1px solid red;}  


</style>

	<script type="text/javascript">

	function checkStatus(arrival_flag,result){
		if(arrival_flag == "完成"){
			alert("该点已完成，无法操作")
			return false;
		}
	}
	</script>

</head>
<body >
	<table id="main" border="3" bordercolor="FFFFFF">  
	<tr id = "header"   bgcolor="#009FCC" >
		<td colspan="4" align="center"><font size="40px">提货扫描</font></td>
	</tr>
	<tr   bgcolor="#009FCC" >
		<td  colspan="4" align="left" ><font size="10px">批次号：${lot}</font></td>
	</tr>
	<tr   bgcolor="#009FCC" >
		<td colspan="4" align="left" ><font size="10px">承运商：${carr_name}</font></td>
	</tr>
	<tr   bgcolor="#009FCC" >
		<td colspan="4" align="left" ><font size="10px">车牌号：${carno}</font></td>
	</tr>
	<tr   bgcolor="#009FCC" >
		<th colspan="2"><font size="10px" >地址</font></th> <th colspan="1"><font size="10px">托盘数</font></th> <th colspan="1"><font size="10px">状态</font></th>
	</tr>
	
	<c:forEach items="${resultsList}" var="result">
		<tr  bgcolor="#009FCC" >
			<td colspan="2"><a href="<%=request.getContextPath()%>/rf/sc/nodeScan.json?lot=${result.lot}&addr_name=${result.addr_name}&num=${result.num}&pk_ent_line_b=${result.pk_ent_line_b}&page=dsscan.jsp" onClick="return checkStatus('${result.arrival_flag}');" ><font size="10px">${result.addr_name} </font> </a></td> <td colspan="1"><font size="10px">${result.num}</font></td> <td colspan="1"><font size="10px">${result.arrival_flag}</font></td>
		</tr>
	</c:forEach>
	
	<tr align="center"  bgcolor="#009FCC" >
		<td colspan="4"> <a href="<%=request.getContextPath()%>/busi/rf/ds.jsp"><font size="10px" >返回</font></a>  </td>
	</tr>
	</table>
</body>
</html>
