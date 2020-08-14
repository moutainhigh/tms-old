<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
Object oStatusCode = request.getAttribute("javax.servlet.error.status_code");
int statusCode = 0;
if(oStatusCode != null){
	try{
		statusCode = Integer.parseInt(oStatusCode.toString());
	}catch(Exception e){}
}
Object oMessage = request.getAttribute("javax.servlet.error.message");
String message = "";
if(oMessage != null && oMessage.toString().length() > 0){
	message = oMessage.toString();
}else{
	if(statusCode == 404){
		message = "您访问的页面不存在！";
	}else if(statusCode == 500){
		message = "服务器内部错误，请刷新页面或<a href=\"javascript:window.location.href='"+request.getContextPath()+"/logout.do'\">重新登录</a>，如果问题还存在，请联系管理员！";
	}else if(statusCode == 403){
		message = "您没有权限访问该页面！";
	}
}
Object oCause = request.getAttribute("javax.servlet.error.cause");
String cause = "";
if(oCause != null){
	cause = oCause.toString();
}
%>
<HTML>
<HEAD>
	<title><%=request.getAttribute("javax.servlet.error.status_code")==null?"运行时":request.getAttribute("javax.servlet.error.status_code") %> 错误</title>
	<META http-equiv=Content-Type content="text/html; charset=UTF-8">
	<STYLE type=text/css>
	TD {
		FONT-SIZE: 12px
	}
	
	A {
		COLOR: #1b6ad8;
		TEXT-DECORATION: none
	}
	
	A:hover {
		COLOR: red
	}
	.btn
	{
		width:145px;
		height:34px;
		line-height:22px;
		font-size:22px;
		background:url("<%=request.getContextPath() %>/images/error/btn_bg.jpg") no-repeat left top;
		color:#959595;
		padding-bottom:4px;
	}
	input,button
	{
		border:0px;
		vertical-align:middle;
		margin:8px;
		line-height:18px;	
		font-size:18px;
	}
	</STYLE>
</HEAD>
<BODY onload="initPage()">
	<P align=center></P>
	<P align=center></P>
	<TABLE cellSpacing=0 cellPadding=0 width=540 align=center border=0>
		<TBODY>
			<TR>
				<TD vAlign=top height=270>
					<DIV align=center>
						<BR> <IMG height=211 src="<%=request.getContextPath() %>/images/error/error.gif" width=329><BR>
						<BR>
						<TABLE cellSpacing=0 cellPadding=0 width="80%" border=0>
							<TBODY>
								<%
								if(oStatusCode != null){
									%>
								<tr align="left">
									<td height=25>
										<font color="red">错&nbsp;误&nbsp;码:&nbsp;<%=statusCode%></font>
									</td>
								</tr>										
									<%
								}
								%>						
								<tr align="left">
									<td height=25>
											<font color="#000000">错误信息:&nbsp;<%
											if(request.getAttribute("obj")==null){
												out.print(message);
											}else{
												out.print(request.getAttribute("obj"));
											}
											%>
											</font>
									</td>
								</tr>
								<tr align="left">
									<td height=25>
											<font color="#000000">原&nbsp;&nbsp;&nbsp;&nbsp;因:&nbsp;<%=cause %>
											</font>
									</td>
								</tr>								
							</TBODY>
						</TABLE>
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD height=5></TD>
			<TR>
				<TD valign=middle>
					<CENTER>
						<TABLE cellSpacing=0 cellPadding=0 width=480 border=0>
							<TBODY>
								<TR>
									<TD>
										<DIV align=center>
											<input id="reLoginBtn" class="btn" type="button" style="color: #222222;visibility:hidden;" value="重新登录" onclick='window.location.href="<%=request.getContextPath() %>/logout.do"'>
											<input id="backBtn" class="btn" type="button" style="color: #222222;visibility:hidden;" value="返  回" onclick="window.history.go(-1);">
<!-- 											<input class="btn" type="button" value="关闭本页" onclick="window.close();"> -->
										</DIV>
									</TD>
								</TR>
							</TBODY>
						</TABLE>
					</CENTER>
				</TD>
			</TR>
		</TBODY>
	</TABLE>
	<P align=center></P>
	<P align=center></P>
</BODY>
</HTML>
<script type="text/javascript">
function initPage(){
	if(window.top==window){
		//如果当前页面不属于iframe里面，则显示重新登录按钮
		var obj=document.getElementById("reLoginBtn");
		obj.style.visibility="visible";
		
		var obj=document.getElementById("backBtn");
		obj.style.visibility="visible";
	}
}
</script>
<!-- 
详细信息：
<%
	java.util.Enumeration e=request.getAttributeNames();
	while(e.hasMoreElements()){
		String key=String.valueOf(e.nextElement());
%>
<%=key%>=<%=request.getAttribute(key)%>
<%
	}
%>
 -->