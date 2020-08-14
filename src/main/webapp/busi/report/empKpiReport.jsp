<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src='<c:url value="/busi/kpi/echarts.min.js?v=${version}" />'></script>
		<script type="text/javascript" src='<c:url value="/busi/kpi/KPI.js?v=${version}" />'></script>
		<script>
		function openeCharts(record){
			var win = Ext.getCmp('KPI');
			if(!win){
				win = new uft.kpi.KPI({record:record});
			}
			win.show(); 
			//window.open("/tms/busi/kpi/carrCharts.jsp?record=" + record); 
			//location.href="/tms/busi/kpi/carrCharts.jsp?record=" + record;
		}
		
		function carrierRenderer(value,meta,record){
			var arr = value.split('|'),str='';
			for(var i=0;i<arr.length;i++){
				str+= "<a href=\"javascript:openeCharts('"+ record +"')\">"+arr[i]+"</a>";
				str+="|";
			}
			return str.substring(0,str.length-1);
		}
		
		
		</script>
	</head>
	<body>
	
	<nw:Report templetVO="${templetVO}" headerGridPageSizePlugin="true"/>				
	</body>
	<script type="text/javascript">
		var app = new uft.jf.ReportPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
