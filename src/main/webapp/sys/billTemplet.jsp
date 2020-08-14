<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<%if(debug){ %>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/ReftypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/DecimaltypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/DecimaltypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeField.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/SelecttypeWindow.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/BillTemplet.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/Win.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/Contextmenu.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/sys/bill/EditTemplet.js"/>"></script>
		<%}else{ %>
			<script type="text/javascript" src="<c:url value="/sys/bill/bill-min.js?v=${version}"/>"></script>
		<%} %>
		<style type="text/css">
			.x-btn-padd {
			   	margin-top:5px;
				margin-bottom:5px;
			}
			.x-table-layout-cell{
				vertical-align:top
			}
			.up {
			    background-image: url(../sys/images/up.png) !important;
			}
			.down {
			    background-image: url(../sys/images/down.png) !important;
			}
			.top {
			    background-image: url(../sys/images/top.png) !important;
			}
			.bottom {
			    background-image: url(../sys/images/bottom.png) !important;
			}				
		</style>
	</head>
	<body>
		<script type="text/javascript">
			new uft.bill.BillTemplet();
		</script>	
	</body>
	<%@ include file="/common/footer.jsp"%>
</html>
	
