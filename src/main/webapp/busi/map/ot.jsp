<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
body, html {
	width: 100%;
	height: 100%;
	margin: 0;
	/* font-family: "微软雅黑"; */
}
</style>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=gmeOhHQnu6B5oYBy9UQ65dpobqrreYDp"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/TextIconOverlay/1.2/src/TextIconOverlay_min.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/MarkerClusterer/1.2/src/MarkerClusterer_min.js"></script>
<script type="text/javascript" src="<c:url value="/busi/map/jquery-1.12.2.min.js"/>"></script>
<!-- Bootstrap 支持 -->
<script src="//cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link href="<c:url value="/busi/map/otList.css"/>" rel="stylesheet" type="text/css" />
<link>
</head>
<body>
	<div id="container">
		<div class=g-mn2>
			<div id="baidu_map"></div>
		</div>

		<div id="query_cmp">
			<div class="col-md-17" id='input'>
				<input type="text" class="form-control" id="keyword" name="keyword"
					placeholder="请输入订单号，客户订单号，发货单号">
			</div>
			<ul class="nav nav-tabs ">
				<li class="active"><a href="#tab_0" data-toggle="tab">全部(<span
						style="color: red;" id="tab_0_length">0</span>)
				</a></li>
				<li><a href="#tab_1" data-toggle="tab">未提货(<span
						style="color: red;" id="tab_1_length">0</span>)
				</a></li>
				<li><a href="#tab_2" data-toggle="tab">已提货(<span
						style="color: red;" id="tab_2_length">0</span>)
				</a></li>
				<li><a href="#tab_3" data-toggle="tab">已到达(<span
						style="color: red;" id="tab_3_length">0</span>)
				</a></li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="tab_0" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_1" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_2" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_3" style="overflow-y: auto;"></div>
			</div>

		</div>

	</div>
</body>
<script type="text/javascript">
	var ctxPath = '<%=request.getContextPath()%>';
	var map = new BMap.Map("baidu_map");
	var contentMap = {};//提示信息的对照表，存储的是：{lng_lat:提示信息}
	var vbillnoMap = {};//提示信息的对照表，存储的是：{lng_lat:提示vbillno}
	var divID = {};//提示信息的对照表，存储的是：{lng_lat:提示信息}
	map.centerAndZoom(new BMap.Point(110.2316, 35.3349), 5);
	map.enableScrollWheelZoom();
	map.addControl(new BMap.NavigationControl());
	map.addControl(new BMap.ScaleControl());
	map.addControl(new BMap.OverviewMapControl());
	map.addControl(new BMap.MapTypeControl());
	map.setCurrentCity("北京"); // 仅当设置城市信息时，MapTypeControl的切换功能才能可用

	//初始化加载事件
	window.onload = function() {//用window的onload事件，窗体加载完毕的时候 

		//设置设备显示窗体的高度
		$(document).ready(function() {
				$("#tab_0")[0].style.height = $("#baidu_map")[0].offsetHeight- 76 + "px";
				$("#tab_1")[0].style.height = $("#baidu_map")[0].offsetHeight- 76 + "px";
				$("#tab_2")[0].style.height = $("#baidu_map")[0].offsetHeight- 76 + "px";
				$("#tab_3")[0].style.height = $("#baidu_map")[0].offsetHeight- 76 + "px";
			});
		//获取设备信息，生成地图标志和设备详细信息列表
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/ot/loadOrderData.json',
			data : '',
			success : function(data) {
				if (!data || data.length == 0 || data == "参数不正确") {
					return;
				}
				var dataList = JSON.parse(data).info;
				map.clearOverlays();
				//返回值分为两种情况 简略信息和详细信息

				if (JSON.parse(data).roughInfo) {
					var roughInfo = JSON.parse(data).roughInfo;
					createOrderRoughInfo(roughInfo,dataList);
				} else {
					createOrderInfo(dataList);
				}
				createQueryCmp(dataList);
			}
		});
	}

	//生成设备详细信息列表	
	function createQueryCmp(datalist) {
		var allStr = "";
		var unDeliStr = "";//未提货
		var deliStr = "";//已提货
		var arriStr = "";//已到货
		
		var allNum = 0;
		var unDeliNum = 0;//未提货
		var deliNum = 0;//已提货
		var arriNum = 0;//已到货
		for (var i = 0; i < datalist.length; i++) {
			var orderVO = datalist[i];
			var order_status_class = "";
			if (orderVO.vbillstatus == "新建" || orderVO.vbillstatus == "确认") {
				order_status_class = "item-order-unDeli"
				unDeliStr += createOrderInfoUnit(orderVO, order_status_class);
				unDeliNum++;
			}
			if (orderVO.vbillstatus == "提货") {
				order_status_class = "item-order-deli"
				deliStr += createOrderInfoUnit(orderVO, order_status_class);
				deliNum++;
			}
			if (orderVO.vbillstatus == "到货" || orderVO.vbillstatus == "签收" || orderVO.vbillstatus == "回单") {
				order_status_class = "item-order-arri"
				arriStr += createOrderInfoUnit(orderVO, order_status_class);
				arriNum++;
			}
			allStr += createOrderInfoUnit(orderVO, order_status_class);
			allNum++;
		}
		document.getElementById("tab_0").innerHTML = allStr;
		document.getElementById("tab_0_length").innerHTML = allNum;
		document.getElementById("tab_1").innerHTML = unDeliStr;
		document.getElementById("tab_1_length").innerHTML = unDeliNum;
		document.getElementById("tab_2").innerHTML = deliStr;
		document.getElementById("tab_2_length").innerHTML = deliNum;
		document.getElementById("tab_3").innerHTML = arriStr;
		document.getElementById("tab_3_length").innerHTML = arriNum;
	}

	//生成谁被信息列表单元
	function createOrderInfoUnit(orderVO, order_status_class) {
		var vbillno = orderVO.vbillno;
		var photo = "";
		if(orderVO.photo){
			photo = orderVO.photo;
		}else{
			photo = "default"
		}
		var infoStr = "<div class='item-mod'><div class='item-img'>"
				+ "<a><img style='height:100px;width=100px;' onclick='orderClick("+ orderVO.longitude+ ","+ orderVO.latitude + ","+'"'+ orderVO.vbillno+ '"'+ ")'  src='<c:url value='/images/cust_photo/"+photo+".jpg'/>'>"
				+ "</a></div>"
				+ "<div class='item-info'><div><span class='"+order_status_class+"'><span>"+vbillno +"</span></span>";
		
		if(orderVO.vbillstatus == "新建" || orderVO.vbillstatus == "确认"){
			infoStr += "</span>"
				+ "<span class='item-status'>" + orderVO.vbillstatus + "</span></div>"
				+ "<div class='comment_inner' title='要求提货时间'>"+ orderVO.req_deli_date + "</div>"
				+ "<div class='comment_inner' title='要求到货时间'>"+ orderVO.req_arri_date + "</div>"
				+ "<div class='item-city' title='提货地址'>"+ orderVO.deli_name + "</div>"
				+ "<div class='item-city' title='到货地址'>"+ orderVO.arri_name + "</div>"
		}else if(orderVO.vbillstatus == "到货" || orderVO.vbillstatus == "签收" || orderVO.vbillstatus == "回单"){
			infoStr += "</span>"
				+ "<span class='item-status'>" + orderVO.vbillstatus + "</span></div>"
				+ "<div class='comment_inner' title='要求提货时间'>"+ orderVO.req_deli_date + "</div>"
				+ "<div class='comment_inner' title='要求到货时间'>"+ orderVO.req_arri_date + "</div>"
				+ "<div class='item-city' title='提货地址'>"+ orderVO.deli_name + "</div>"
				+ "<div class='item-city' title='到货地址'>"+ orderVO.arri_name + "</div>"
		}else{
			var speed =  "";
			var road_name = ""
			var place_name = ""
			
			if(orderVO.trackVO){
				speed = orderVO.trackVO.speed + "";
				road_name = orderVO.trackVO.road_name + ""
				place_name = orderVO.trackVO.place_name + ""
			}
			if (speed.length == 1) {
				infoStr += ("&nbsp;&nbsp;" + speed);
			}
			if (speed.length == 2) {
				infoStr += ("&nbsp;" + speed);
			}
			if (speed.length == 3) {
				infoStr += speed;
			}
			infoStr += "</span><span class='item-kilo'>公里/小时</span>"
					+ "<span class='item-status'>" + orderVO.vbillstatus
					+ "</span></div>"
					+ "<div class='comment_inner' title='"+road_name+"'>"+ road_name + "</div>"
					+ "<div class='item-city' title='"+place_name+"'>"+ place_name + "</div>"
					+ "<div class='item-carr'><span>" + orderVO.carr_name+ "</span><span>" + orderVO.driver + "</span><span>"+ orderVO.driver_mobile + "</span></div>";
		}
		
		infoStr += "</div></div><div style='clear:both;border-bottom:dashed 1px #000;'></div>";
		return infoStr;
	}

	//查询
	$('#keyword').keyup(function() {
		var keyword = $(this).val();
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/ot/loadQueryOrderData.json',
			data : {keyword : keyword},
			success : function(data) {
				if(data){
					var datalist = eval(data);
					createQueryCmp(datalist);
				}
			}
		});
	});

	function createOrderRoughInfo(roughInfo,datalist) {
		for (var i = 0; i < datalist.length; i++) {
			var orderVO = datalist[i];
			//标注信息
			var speed =  "";
			var road_name = "";
			var place_name = "";
			var	gps_time = "";
			if(orderVO.trackVO){
				speed = orderVO.trackVO.speed + "";
				road_name = orderVO.trackVO.road_name + ""
				place_name = orderVO.trackVO.place_name + ""
			}
			var key = String(orderVO.longitude) + '_' + orderVO.latitude + '_' + orderVO.vbillno;
			var content = '<div style="font-size:10px;height:auto;">';
			content += "单号：" + orderVO.vbillno;
			content += "<br/>";
			content += "车牌号：" + orderVO.carno;
			content += "<br/>";
			content += "地址：" + place_name + road_name;
			content += "<br/>";
			content += "速度：" + speed + "公里/小时";
			content += "<br/>";
			content += "油耗：10.5 百公里/升  温度：32℃  压力：2.1MPa ";
			content += "<br/>";
			content += "定位时间：" + gps_time;
			content += "<br/>";
			if(orderVO.memo && orderVO.memo != 'null' && orderVO.memo != null){
				content += "备注：" + orderVO.memo;
			}else{
				content += "备注：";
			}
			
			content += ""+getLinkBtns(orderVO.vbillno);
			content += "</div>";
			contentMap[key] = content;
		}
		for (var i = 0; i < roughInfo.length; i++) {
			var orderRoughInfo = roughInfo[i];
			var point_lat = orderRoughInfo.point_lat;
			var point_lng = orderRoughInfo.point_lng;
			var gpsPoint = new BMap.Point(point_lng, point_lat);
			//创建地图覆盖物
			function ComplexCustomOverlay(point, text, mouseoverText) {
				this._point = point;
				this._text = text;
				this._overText = mouseoverText;
			}
			ComplexCustomOverlay.prototype = new BMap.Overlay();
			ComplexCustomOverlay.prototype.initialize = function(map) {
				this._map = map;
				var div = this._div = document.createElement("div");
				div.style.position = "absolute";
				div.style.zIndex = BMap.Overlay.getZIndex(this._point.lat);
				div.style.width = '70px';
				div.style.height = '70px';
				div.id = String(point_lat);
				div.name = String(point_lng);
				div.style.backgroundColor = '#47aa4d';
				div.style.borderRadius = '35px';
				var span = this._span = document.createElement("span");
				span.style.height = '70px';
				span.style.lineHeight = '70px';
				span.style.display = 'block';
				span.style.color = '#FFF';
				span.style.textAlign = 'center';
				div.appendChild(span);
				span.appendChild(document.createTextNode(this._text));
				var that = this;

				div.onmouseover = function() {
					this.style.backgroundColor = "#ff6501";
					this.style.borderColor = "#0000ff";
					this.getElementsByTagName("span")[0].innerHTML = that._overText;
					this.style.cursor = 'point';
				}

				div.onmouseout = function() {
					this.style.backgroundColor = "#47aa4d";
					this.style.borderColor = "#BC3B3A";
					this.getElementsByTagName("span")[0].innerHTML = that._text;
				}
				div.onclick = function() {
					var point = new BMap.Point(this.name, this.id);
					map.centerAndZoom(point, map.getZoom() + 2);
				    map.clearOverlays();
					getOrders(map); 
				}

				map.getPanes().labelPane.appendChild(div);
				return div;
			}
			ComplexCustomOverlay.prototype.draw = function() {
				var map = this._map;
				var pixel = map.pointToOverlayPixel(this._point);
				this._div.style.left = pixel.x + "px";
				this._div.style.top = pixel.y - 30 + "px";
			}
			var txt = orderRoughInfo.num;

			var myMarker = new ComplexCustomOverlay(gpsPoint, txt, txt);

			map.addOverlay(myMarker);

		}
	}

	function createOrderInfo(datalist) {
		for (var i = 0; i < datalist.length; i++) {
			var orderVO = datalist[i];
			var pt = new BMap.Point(orderVO.longitude, orderVO.latitude);
			var myIcon = new BMap.Icon(ctxPath + "/images/inv_icon/inv.png", new BMap.Size(32, 32),{anchor : new BMap.Size(16, 5)});
			var myMarker = new BMap.Marker(pt, {
				icon : myIcon
			}); // 创建标注

			map.addOverlay(myMarker); // 将标注添加到地图中

			//标注信息
			var speed =  "";
			var road_name = "";
			var place_name = "";
			var	gps_time = "";
			if(orderVO.trackVO){
				speed = orderVO.trackVO.speed + "";
				road_name = orderVO.trackVO.road_name + ""
				place_name = orderVO.trackVO.place_name + ""
			}
			var key = String(orderVO.longitude) + '_' + orderVO.latitude + '_' + orderVO.vbillno;
			var content = '<div style="font-size:10px;height:auto;">';
			content += "单号：" + orderVO.vbillno;
			content += "<br/>";
			content += "车牌号：" + orderVO.carno;
			content += "<br/>";
			content += "地址：" + place_name + road_name;
			content += "<br/>";
			content += "速度：" + speed + "公里/小时";
			content += "<br/>";
			content += "油耗：10.5 百公里/升  温度：32℃  压力：2.1MPa ";
			content += "<br/>";
			content += "定位时间：" + gps_time;
			content += "<br/>";
			if(orderVO.memo && orderVO.memo != 'null' && orderVO.memo != null){
				content += "备注：" + orderVO.memo;
			}else{
				content += "备注：";
			}
			
			content += ""+getLinkBtns(orderVO.vbillno);
			content += "</div>";
			contentMap[key] = content;
			myMarker.addEventListener("click", function(e) {
				var p = e.target;
				var lng = p.getPosition().lng, lat = p.getPosition().lat;
				orderClick(lng, lat, 15);
			});
		}
	}
	
	//链接按钮,参数是设备编码
	function getLinkBtns(vbillno){
		/* return "<div><a href=\"javascript:tracking('"+orderVO+"');\">跟踪</a>&nbsp;&nbsp;" +
				"<a href=\"javascript:playback('"+orderVO+"');\">回放</a>&nbsp;&nbsp;" +
				"<a href=\"javascript:fence('"+orderVO+"');\">电子围栏</a>&nbsp;&nbsp;" +
				"<a href=\"javascript:equipinfo('"+orderVO+"');\">设备信息</a></div>"; */
			return "<div><a href=\"javascript:playback('"+ vbillno +"');\">轨迹回放</a></div>";	
	}

	//跟踪
	function tracking(orderVO){
		window.open(ctxPath+'/gps/tracking/index.html?equipcode='+equipcode);
	}
	//设备信息
	function equipinfo(orderVO){
		window.open(ctxPath+'/base/equip/viewByCode.html?equipcode='+equipcode);
	}
	//回放
	function playback(vbillno){
		//window.open(ctxPath+'/gps/playback/index.html?equipcode='+equipcode);
		var url = ctxPath + "/inv/it/toIndex.json?vbillno="+vbillno;
		window.open(url);
	}
	
	function orderClick(longitude, latitude, vbillno) {
		if (longitude && latitude && longitude != 'null' && latitude != 'null') {
			openOrderInfo(longitude, latitude, 15, vbillno);
		} else {
			alert('没有该设备的任何GPS信息!');
		}
	}

	function openOrderInfo(lng, lat, mapLevel,vbillno) {
		var opts = {
			enableMessage : false
		//设置允许信息窗发送短息  
		};
		var key = lng + '_' + lat + '_' + vbillno;
		var content = contentMap[key];
		var point = new BMap.Point(lng, lat);
		if (mapLevel) {
			map.centerAndZoom(point, mapLevel);
		}
		var infoWindow = new BMap.InfoWindow(content, opts); // 创建信息窗口对象 
		map.openInfoWindow(infoWindow, point); //开启信息窗口
	}

	function getOrders(map) {
		//可视区域经纬度
		var bounds = map.getBounds();
		//获取当前地图级别
		var level = map.getZoom();
		//西南交经纬度
		var sw_lat = bounds.getSouthWest().lat;
		var sw_lng = bounds.getSouthWest().lng;
		//东北角经纬度
		var ne_lat = bounds.getNorthEast().lat;
		var ne_lng = bounds.getNorthEast().lng;
		//从后台获取参数
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/ot/loadOrderData.json',
			data : {"sw_lat" : sw_lat,"sw_lng" : sw_lng,"ne_lat" : ne_lat,"ne_lng" : ne_lng,"level" : level},
			success : function(data) {
				//创建覆盖物
				if (!data || data.length == 0 || data == "参数不正确") {
					return;
				}
				var dataList = JSON.parse(data).info;

				//返回值分为两种情况 简略信息和详细信息
				if (JSON.parse(data).roughInfo) {
					var roughInfo = JSON.parse(data).roughInfo;
					createOrderRoughInfo(roughInfo,dataList);
				} else {
					createOrderInfo(dataList);
				}
				createQueryCmp(dataList);
			}
		});
	}

	//地图拖拽事件 和 地图级别缩放事件	和地图移动事件
	map.addEventListener("dragstart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("dragend", function() {
		//从后台获取参数
		getOrders(map);
	});
	map.addEventListener("zoomstart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("zoomend", function() {
		//从后台获取参数
		getOrders(map);
	});
	map.addEventListener("movestart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("moveend", function() {
		//从后台获取参数
		getOrders(map);
	});
</script>
</html>