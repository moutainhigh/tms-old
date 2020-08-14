<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
		function speed_statusBeforeRenderer(value,meta,record){
			if(value == '超速'){
				meta.css = 'cssRed';
			}else if(value == '离线'){
				meta.css = 'css9';
			}else{
				meta.css = 'css0';
			}
			
		}
		function temp_statusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value == '正常'){
				meta.css = 'css0';
			}else if(value == '高温'){
				meta.css = 'cssRed';
			}else if(value == '低温'){
				meta.css = 'cssBlue';
			}
			var highTemp = record.data.hight_temp;
			var lowTemp = record.data.low_temp;
			var temp = record.data.temp;
			if(highTemp && highTemp && temp){
				if((highTemp*1 - temp*1) < 1 || (temp*1 - lowTemp*1) < 1){
					meta.css = 'cssYellow';
				}
			}
		}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css1{
				background-color: #FAC090;
			}
			.css2{
				background-color: #FFFF00;
			}
			.css3{
				background-color: #538ED5;
			}
			.css4{
				background-color: #7030A0;
			}
			.css6{
				background-color: #44964C;
			}
			.cssRed{
				background-color: #FF0000;
			}
			.cssBlue{
				background-color: #0000CC ;
			}
			.cssYellow{
				background-color: #FFCC22 ;
			}
			.css7{
				background-color: #FFFF00;
			}
			.css8{
				background-color: #538ED5;
			}
			.css9{
				background-color:#888888;
			}
		</style>
	</head>
	<body>
	<nw:Report templetVO="${templetVO}" headerGridPageSizePlugin="true" />				
	</body>
	<script type="text/javascript">
		var app = new uft.jf.ReportPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
