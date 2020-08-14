<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
body, html {
	width: 100%;
	height: 100%;
	margin: 0;
}

/* body {
	font-family: 'Hiragino Sans GB', 'Microsoft YaHei', '黑体', sans-serif;
	font-size: 14px;
	line-height: 1.428571429;
	color: #333;
	margin: 1
} */
</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=gmeOhHQnu6B5oYBy9UQ65dpobqrreYDp"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/TextIconOverlay/1.2/src/TextIconOverlay_min.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/MarkerClusterer/1.2/src/MarkerClusterer_min.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/LuShu/1.2/src/LuShu_min.js"></script>
<script type="text/javascript" src="//cdn.bootcss.com/jquery/2.2.4/jquery.min.js"></script>
<script type="text/javascript" src='<c:url value="/busi/inv/it.js?v=${version}" />'></script>
<link type="text/css" rel="stylesheet" href='<c:url value="/busi/inv/it.css?v=${version}" />'>
<!-- Bootstrap 支持 -->
<script type="text/javascript" src="//cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<link type="text/css" rel="stylesheet" href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">
<!-- QNUI 这个CSS可以让百度地图上面的图标自适应大小 -->
<link type="text/css" rel="stylesheet" href="//g.alicdn.com/sj/qnui/1.5.1/css/sui.min.css">
<link>
</head>
<body>
	<div id="container">
		<div class=g-mn2>
			<div id="baidu_map"></div>
		</div>
		<div id="query_cmp" style="overflow-y: auto;">
			<div class="col-md-17">
				<c:choose>
					<c:when test="${invoice.vbillstatus == '新建'}">
						<img src='<c:url value="/busi/inv/icon/steps/new.png"/>'
							class="img-responsive" alt="新建">
					</c:when>
					<c:when test="${invoice.vbillstatus == '确认'}">
						<img src='<c:url value="/busi/inv/icon/steps/confirm.png"/>'
							class="img-responsive" alt="确认">
					</c:when>
					<c:when test="${invoice.vbillstatus == '提货'}">
						<img src='<c:url value="/busi/inv/icon/steps/delivery.png"/>'
							class="img-responsive" alt="提货">
					</c:when>
					<c:when test="${invoice.vbillstatus == '到货'}">
						<img src='<c:url value="/busi/inv/icon/steps/arrival.png"/>'
							class="img-responsive" alt="到货">
					</c:when>
					<c:when test="${invoice.vbillstatus == '签收'}">
						<img src='<c:url value="/busi/inv/icon/steps/sign.png"/>'
							class="img-responsive" alt="签收">
					</c:when>
					<c:when test="${invoice.vbillstatus == '回单'}">
						<img src='<c:url value="/busi/inv/icon/steps/back.png"/>'
							class="img-responsive" alt="回单">
					</c:when>
				</c:choose>
				<table>
					<tr>
						<td>
							<span>
								<c:if test="${!empty invoice.create_time}">
										<span>${fn:substring(invoice.create_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.create_time}">
										<span>${fn:substring(invoice.create_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
						<td>
							<span>
								<c:if test="${!empty invoice.confirm_time}">
										<span>${fn:substring(invoice.confirm_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.confirm_time}">
										<span>${fn:substring(invoice.confirm_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
						<td>
							<span>
								<c:if test="${!empty invoice.deli_time}">
										<span>${fn:substring(invoice.deli_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.deli_time}">
										<span>${fn:substring(invoice.deli_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
						<td>
							<span>
								<c:if test="${!empty invoice.arri_time}">
										<span>${fn:substring(invoice.arri_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.arri_time}">
										<span>${fn:substring(invoice.arri_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
						<td>
							<span>
								<c:if test="${!empty invoice.sign_time}">
										<span>${fn:substring(invoice.sign_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.sign_time}">
										<span>${fn:substring(invoice.sign_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
						<td>
							<span>
								<c:if test="${!empty invoice.back_time}">
										<span>${fn:substring(invoice.back_time, 0, 10)}</span>
								</c:if>
							</span>
							<br/>
							<span>
								<c:if test="${!empty invoice.back_time}">
										<span>${fn:substring(invoice.back_time, 11, 19)}</span>
								</c:if>
							</span>
						</td>
					</tr>
				</table>
			</div>

			<div id="orderInfo">
				<h5>订单详情</h5>
				<dl>
					<dd>
						单据号：<span style="font-size: 10px;">${invoice.vbillno}</span>&nbsp;
						状态：<span class="item-warning">${invoice.vbillstatus}</span>
						<c:choose>
							<c:when test="${invoice.urgent_level != 0}">
								<span class="item-warning">急</span>
							</c:when>
							<c:when test="${invoice.if_customs_official == 'Y'}">
								<span class="item-warning">/海关监管</span>
							</c:when>
						</c:choose>
					</dd>
					<dd>
						客户订单号：<span style="font-size: 10px;">${invoice.cust_orderno}</span>&nbsp;
						运输方式：<span>${invoice.trans_type_name}</span>
					</dd>
					<dd>
						要求提货时间：<span>${invoice.req_deli_date}</span>
					</dd>
					<dd>
						要求到货时间：<span>${invoice.req_arri_date}</span>
					</dd>
					<dd>
						提货地址：<span>${invoice.deli_name}</span>
					</dd>
					<dd>
						到货地址：<span>${invoice.arri_name}</span>
					</dd>
					<dd>
						件数：<span>${invoice.num_count}</span>&nbsp;
						重量：<span>${invoice.weight_count}</span>&nbsp;
						体积：<span>${invoice.volume_count}</span>
					</dd>
				</dl>
			</div>
			<div style='clear:both;border-bottom:dashed 1px #000;'></div>
			<div id="transbility">
				<c:if test="${!empty invoice.transbility}">
					<h5>运力信息</h5>
					<dl>
						<c:forEach items="${invoice.transbility}" var="tran">
							<dd>
								承运商：<span>${invoice.carr_name}</span>&nbsp;
								车牌号：<span id="${tran.carno}">${tran.carno}</span>
								<c:if test="${!empty tran.driver}">
										司机：<span>${tran.driver}</span>
								</c:if>
								<c:if test="${!empty tran.mobile}">
										电话号：<span>${tran.mobile}</span>
								</c:if>
							</dd>
						</c:forEach>
					</dl>
				</c:if>
			</div>
			<div style='clear:both;border-bottom:dashed 1px #000;'></div>
			<div id="trackingProcess">
				<h5>跟踪详情</h5>
				<ul>
					<c:forEach items="${trackings}" var="tracking">
						<li>${tracking.tracking_time}:${tracking.tracking_memo}</li>
					</c:forEach>
				</ul>

			</div>

		</div>
	</div>
</body>
<script type="text/javascript">
	var ctxPath = '<%=request.getContextPath()%>';
	var deli_longitude = '${invoice.deli_longitude}'
	var deli_latitude = '${invoice.deli_latitude}'
	var arri_longitude = '${invoice.arri_longitude}'
	var arri_latitude = '${invoice.arri_latitude}'
	var surrentCarno = '${invoice.carno}'

	var sp = new BMap.Point(deli_longitude, deli_latitude);//起始点
	var ep = new BMap.Point(arri_longitude, arri_latitude);//终点
	var status = '${invoice.vbillstatus}';
</script>
</html>