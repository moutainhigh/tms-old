<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script>
		function invoice_vbillnoRenderer(value,meta,record){
			var arr = value.split('|'),str='';
			for(var i=0;i<arr.length;i++){
				var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+arr[i];
				str+= "<a href=\"javascript:uft.Utils.openNode('"+arr[i]+"','发货单','"+url+"')\">"+arr[i]+"</a>";
				str+="|";
			}
			return str.substring(0,str.length-1);
		}
		</script>
	</head>
	<body>
	<nw:Report templetVO="${templetVO}" headerGridPageSizePlugin="true" headerGridPagination="true" />				
	</body>
	<script type="text/javascript">
		var app = new uft.jf.ReportPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
