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
}

body {
	font-family: 'Hiragino Sans GB','Microsoft YaHei','黑体',sans-serif;
	font-size: 14px;
	line-height: 1.428571429;
	color: #333;
	margin:1
}


</style>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=gmeOhHQnu6B5oYBy9UQ65dpobqrreYDp"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/TextIconOverlay/1.2/src/TextIconOverlay_min.js"></script>
<script type="text/javascript" src="http://api.map.baidu.com/library/MarkerClusterer/1.2/src/MarkerClusterer_min.js"></script>
<script type="text/javascript" src="<c:url value="/busi/map/jquery-1.12.2.min.js"/>"></script>
<!-- Bootstrap 支持 -->
<script src="//cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">
<%-- <link href="<c:url value="/busi/map/style.css"/>" rel="stylesheet" type="text/css" /> --%>
<link href="<c:url value="/busi/map/etList.css"/>" rel="stylesheet" type="text/css" />
<link>
</head>
<body>
	<div id="container">
		<div class=g-mn2>
			<div id="baidu_map"></div>
			<div id="equip_info" style="display: none;"></div>
			<div id="float_div" style="display: none;"><a href="javascript:closeEquipInfo();">关闭</a></div>
			
		</div>

		<div id="query_cmp">
			<div class="col-md-17" id='input'>
				<input type="text" class="form-control" id="keyword" name="keyword" placeholder="请输入设备ID或设备编码">
			</div>
			<ul class="nav nav-tabs"  id="myTab">
				<li class="active"><a href="#tab_0" data-toggle="tab">全部(<span
						style="color: red;" id="tab_0_length">0</span>)
				</a></li>
				<li class=""><a href="#tab_1" data-toggle="tab">在线(<span
						style="color: red;" id="tab_1_length">0</span>)
				</a></li>
				<li><a href="#tab_2" data-toggle="tab">离线(<span
						style="color: red;" id="tab_2_length">0</span>)
				</a></li>
				<li><a href="#tab_3" data-toggle="tab">监控(<span
						style="color: red;" id="tab_3_length">0</span>)
				</a></li>
			</ul>
			<div class="tab-content" >
				<div class="tab-pane active" id="tab_0" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_1" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_2" style="overflow-y: auto;"></div>
				<div class="tab-pane" id="tab_3" style="overflow-y: auto;"></div>
			</div>

		</div>
	</div>
</body>
<script type="text/javascript">
	var refresh_interval = 30; //默认30s刷新
	var ctxPath = '<%=request.getContextPath()%>';
	var map = new BMap.Map("baidu_map");
	var contentMap = {};//提示信息的对照表，存储的是：{lng_lat:提示信息}
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
	
		//获取预设的刷新事件
		/* $.ajax({
			type : "GET",
			url : ctxPath + '/te/et/getRefreshInterval.json',
			data : {},
			success : function(data) {
				//创建覆盖物
				if (!data || data == 0) {
					return;
				}
				refresh_interval = data;
			}
		}); */
		//在页面加载时，直接获取设备信息，生成地图标志和设备详细信息列表
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/et/loadEquipData.json',
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
					createEquipRoughInfo(roughInfo,dataList);
				} else {
					createEquipInfo(dataList);
				}
				createQueryCmp(dataList);
			}
		});
	}

	//生成列表页面的信息
	function createQueryCmp(datalist) {
		var allStr = "";
		var onlineStr = "";
		var offlineStr = "";
		var watchStr = "<div><input type='checkbox' onclick='watchFlagClick()' id='watchFlag'/>监控</div>";
		var allNum = 0;
		var onlineNum = 0;
		var offlineNum = 0;
		for (var i = 0; i < datalist.length; i++) {
			var equipVO = datalist[i];
			var speed_status_class = "";
			if (equipVO.speed_status == "离线") {
				speed_status_class = "item-veicle-offline"
				offlineStr += createEquipInfoUnit(equipVO, speed_status_class);
				offlineNum++;
			}
			if (equipVO.speed_status == "在线") {
				speed_status_class = "item-veicle-online"
				onlineStr += createEquipInfoUnit(equipVO, speed_status_class);
				onlineNum++;
			}
			if (equipVO.speed_status == "超速") {
				speed_status_class = "item-veicle-speeding"
				onlineStr += createEquipInfoUnit(equipVO, speed_status_class);
				onlineNum++;
			}
			watchStr += createWatchStr(equipVO, speed_status_class);
			allStr += createEquipInfoUnit(equipVO, speed_status_class);
			allNum++;
		}
		document.getElementById("tab_0").innerHTML = allStr;
		document.getElementById("tab_0_length").innerHTML = allNum;
		document.getElementById("tab_1").innerHTML = onlineStr;
		document.getElementById("tab_1_length").innerHTML = onlineNum;
		document.getElementById("tab_2").innerHTML = offlineStr;
		document.getElementById("tab_2_length").innerHTML = offlineNum;
		//如果存在就不要再刷新了。
		if(!document.getElementById("watchFlag")){
			document.getElementById("tab_3").innerHTML = watchStr;
		}
		
	}

	
	//查询锁定栏的数据 
	function createWatchStr(equipVO, speed_status_class){
		var equipcode = equipVO.equipcode;
		var infoStr = "<div style='margin-left:20px;''><input onclick='checkboxClick()' type='checkbox' id="+equipcode+" name='watch'/><span id="+"span"+equipcode+">"+equipcode+"<span></div>";
		return infoStr;
	}
	
	//设备锁定栏的点击操作事件，分为两部，计算数量的刷新地图
	function checkboxClick(){
		var watchs = $(":checked");
		var num=0;
		if(watchs && watchs.length > 0){
			if(document.getElementById("watchFlag").checked){
				getEquipsByLock();
			}
			for(var i=0;i<watchs.length;i++){
				var watch = watchs[i];
				if(watch.id != "watchFlag"){
					num++;
				}
			}
			document.getElementById("tab_3_length").innerHTML = num;
		}
	}
	
	//设备列表的数据
	function createEquipInfoUnit(equipVO, speed_status_class) {
		var equipcode = equipVO.equipcode;
		var infoStr = "<div class='item-mod'><div class='item-img'>"
				+ "<a><img onclick='equipClick("+ equipVO.longitude+ ","+ equipVO.latitude+ ")'  src='<c:url value='/images/car_photo/"+equipVO.photo+".jpg'/>'>"
				+ "</a></div>"
				+ "<div class='item-info'><div><span class='"+speed_status_class+"'><a id='"+equipcode+"' href='javascript:void(0);' onclick='showEquipInfo(this.id)'>";
		if (equipcode.length > 11) {
			equipcode = equipcode.substring(0, 8) + "..."
			infoStr += equipcode;
		} else {
			infoStr += equipcode;
			for (var i = 0; i < 11 - equipcode.length; i++) {
				infoStr += "&nbsp;";
			}

		}
		infoStr += "</a></span><span class='item-speed'>";
		var speed = equipVO.speed + "";
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
				+ "<span class='item-status'>" + equipVO.ent_status
				+ "</span></div>"
				+ "<div class='comment_inner' title='"+equipVO.road_name+"'>"+ equipVO.road_name + "</div>"
				+ "<div class='item-city'  title='"+equipVO.place_name+"'>"+ equipVO.place_name + "</div>"
				+ "<div class='item-carr'><span>" + equipVO.carrier+ "</span><span>" + equipVO.driver + "</span><span>"+ equipVO.mobile + "</span></div>"
				+ "<div class='tags'>";
		if (equipVO.customs) {
			infoStr += "<span class='diy-tag'>" + equipVO.customs + "</span>";
		}
		if (equipVO.dangerous_veh) {
			infoStr += "<span class='diy-tag'>" + equipVO.dangerous_veh
					+ "</span>";
		}
		if (equipVO.tonnage) {
			infoStr += "<span class='diy-tag'>" + equipVO.tonnage + "</span>";
		}
		infoStr += "</div></div></div><div style='clear:both;border-bottom:dashed 1px #000;'></div>";
		return infoStr;
	}

	
	//设备详情展示窗口
	function showEquipInfo(equipcode) {
		var equipInfo="";
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/et/loadEquipLotsOrdersInfo.json',
			data : {equipcode : equipcode},
			async: false,
			success : function(data) {
				equipInfo = data;
			}
		});
		var orderInfo = "<table width='660' height='268' id='divtable'>";
		orderInfo += "<tr><th colspan='4' align='center' style='font-size:20px; font-family:Verdana, Geneva, sans-serif'><img src='<c:url value='/busi/map/car_info.jpg'/>' alt='23' width='63' height='43' align='absmiddle' style='margin-right:10px;'>车辆信息</th></tr>";
		orderInfo += "<tr><th colspan='4' align='left'><img src='<c:url value='/busi/map/base_info.jpg'/>' alt='11' width='25' height='20' align='top' style='margin-right:2px;'>基本信息</th></tr>";
		orderInfo += "<tr><td width='113'>车牌号：</td><td colspan='3'>"+equipInfo.carno+"</td></tr>";
		orderInfo += "<tr><td>承运商：</td><td width='246'>"+equipInfo.carr_name+"</td><td width='115'>车型：</td><td width='182'>"+equipInfo.car_type+"</td></tr>";
		orderInfo += "<tr><td>状态：</td><td>"+equipInfo.car_status+"</td><td>司机：</td><td>"+equipInfo.driver_name+"</td></tr>"; 
		orderInfo += "<tr><td>总单数：</td><td>"+equipInfo.bill_num+"</td><td>绩效分数：</td><td>"+equipInfo.kpi_point+"</td></tr>";  
		orderInfo += "<tr><td>异常单数：</td><td>"+equipInfo.exp_num+"</td><td>近期单数：</td><td>"+equipInfo.near_num+"</td></tr>";  
		orderInfo += "<tr><td>到货及时率：</td><td>"+equipInfo.arri_rate+"</td><td>提货及时率：</td><td>"+equipInfo.deli_rate+"</td></tr>";  
		orderInfo += " <tr><td>货物破损率：</td><td>"+equipInfo.brok_rate+"</td><td>货物丢失率：</td><td>"+equipInfo.lost_rate+"</td></tr>";  
		var lots = equipInfo.lots;
		if(lots){
			for(var i=0;i<lots.length;i++){
				var lot = lots[i];
				var lotInfoUnit = "<tr><th colspan='4' class='line'><img src='<c:url value='/busi/map/lot_info.jpg'/>' alt='12' width='25' height='20' align='top' style='margin-right:2px;'>批次信息</th><td>&nbsp;</td><td>&nbsp;</td></tr>";
				lotInfoUnit += "<tr><td>批次号：</td><td colspan='3'>"+lot.lot+"</td></tr>";
				lotInfoUnit += "<tr><td>起始时间：</td><td>"+lot.req_deli_date+"</td><td>结束时间：</td><td>"+lot.req_arri_date+"</td></tr>";
				lotInfoUnit += "<tr><td>起始地址：</td><td>"+lot.deli_addr+"</td><td>结束地址：</td><td>"+lot.arri_addr+"</td> </tr>";
				lotInfoUnit += "<tr><td colspan='4'><table width='659'><tr><td width='65'>总件数：</td><td width='105'>"+lot.num_count+"</td><td width='80'>总重量：</td><td width='110'>"+lot.weight_count+"</td><td width='120'>总体积：</td><td width='140'>"+lot.volume_count+"</td></tr></table></td></tr>";
				var orders = lot.orders;
				if(orders){
					for(var i=0;i<orders.length;i++){ 
						var order = orders[i];
						var orderInfoUnit = "<tr><th colspan='4' class='line'><img src='<c:url value='/busi/map/order_info.jpg'/>' alt='33' width='25' height='20' align='top'>订单信息</th><td>&nbsp;</td><td>&nbsp;</td></tr>";
						orderInfoUnit += "<tr><td>单号：</td><td>"+order.vbillno+"</td><td>客户订单号：</td><td>"+order.cust_orderno+"</td></tr>";
						orderInfoUnit += "<tr><td>客户：</td><td>"+order.cust_name+"</td><td>订单状态：</td><td>"+order.vbillstatus+"</td></tr>";
						orderInfoUnit += "<tr><td>要求提货时间：</td><td>"+order.req_deli_date+"</td><td>要求到货时间：</td><td>"+order.req_arri_date+"</td></tr>";
						orderInfoUnit += "<tr><td>实际提货时间：</td><td>"+order.act_deli_date+"</td><td>实际到货日期：</td><td>"+order.act_arri_date+"</td></tr>";
						orderInfoUnit += "<tr><td>提货地址：</td><td>"+order.deli_addr+"</td><td>到货地址</td><td>"+order.arri_addr+"</td></tr>";
						orderInfoUnit += "<tr><td>备注</td><td colspan='3'>"+order.ent_memo+"</td></tr>";
						lotInfoUnit += orderInfoUnit;
					}
				}
				orderInfo += lotInfoUnit;
			}
		}
		var innetHtml ="<div>" + orderInfo + "</table></div>";
		document.getElementById("equip_info").innerHTML = innetHtml;
		document.getElementById("equip_info").style.display="";
		document.getElementById("float_div").style.display="";
	}

	//设备详情展示窗口关闭
	function closeEquipInfo(){
		   document.getElementById("equip_info").style.display="none";
		   document.getElementById("float_div").style.display="none";
	}

	//查询
	$('#keyword').keyup(function() {
		var keyword = $(this).val();
		var  getCK=document.getElementsByTagName('input'); 
		for(var i=0;i<getCK.length;i++){
			whichObj=getCK[i];   
	          if(whichObj.type=="checkbox" && whichObj.id != "watchFlag"){
	        	  document.getElementById("span"+whichObj.id).innerHTML = whichObj.id.replace(keyword,'<font color="red">'+keyword+'</font>');
	          }
		} 
		$.ajax({
			type : "GET",
			url : ctxPath + '/te/et/loadQueryEquipData.json',
			data : {keyword : keyword},
			success : function(data) {
				var datalist = eval(data);
				createQueryCmp(datalist);
			}
		});
	});

	//当地图上设备数量超过限制时，生成聚合信息
	function createEquipRoughInfo(roughInfo,datalist) {
		for (var i = 0; i < datalist.length; i++) {
			var equipVO = datalist[i];
			//标注信息 这里也要标记一次标注信息，否则第一次点击定位的时候没有信息可以显示
			createContent(equipVO);
		}
		
		for (var i = 0; i < roughInfo.length; i++) {
			var equipRoughInfo = roughInfo[i];
			var point_lat = equipRoughInfo.point_lat;
			var point_lng = equipRoughInfo.point_lng;
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
					getEquips();
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
			var txt = equipRoughInfo.num;

			var myMarker = new ComplexCustomOverlay(gpsPoint, txt, txt);

			map.addOverlay(myMarker);

		}
	}
	
	//根据点集合确定地图中心点位置和地图级别配合getZoom()方法使用
	function setZoom(points){
	    if(points.length>0){
	        var maxLng = points[0].lng;
	        var minLng = points[0].lng;
	        var maxLat = points[0].lat;
	        var minLat = points[0].lat;
	        var res;
	        for (var i = points.length - 1; i >= 0; i--) {
	            res = points[i];
	            if(res.lng > maxLng) maxLng =res.lng;
	            if(res.lng < minLng) minLng =res.lng;
	            if(res.lat > maxLat) maxLat =res.lat;
	            if(res.lat < minLat) minLat =res.lat;
	        };
	        var cenLng =(parseFloat(maxLng)+parseFloat(minLng))/2;
	        var cenLat = (parseFloat(maxLat)+parseFloat(minLat))/2;
	        var zoom = getZoom(maxLng, minLng, maxLat, minLat);
	        map.centerAndZoom(new BMap.Point(cenLng,cenLat), zoom);
	    }else{
	        //没有坐标，显示全中国
	        map.centerAndZoom(new BMap.Point(103.388611,35.563611), 5);
	    }
	}

	function getZoom (maxLng, minLng, maxLat, minLat) {
	    var zoom = ["50","100","200","500","1000","2000","5000","10000","20000","25000","50000","100000","200000","500000","1000000","2000000"]//级别18到3。
	    var pointA = new BMap.Point(maxLng,maxLat);  // 创建点坐标A
	    var pointB = new BMap.Point(minLng,minLat);  // 创建点坐标B
	    var distance = map.getDistance(pointA,pointB).toFixed(1);  //获取两点距离,保留小数点后两位
	    for (var i = 0,zoomLen = zoom.length; i < zoomLen; i++) {
	        if(zoom[i] - distance > 0){
	            return 18-i+3;//之所以会多3，是因为地图范围常常是比例尺距离的10倍以上。所以级别会增加3。
	        }
	    };
	}
	
	//当锁定订单时在地图上标记车辆详情，这个方法需要自动设置地图级别和中心点。
	function createEquipInfoByLock(datalist){
		var points = [];
		for (var i = 0; i < datalist.length; i++) {
			var equipVO = datalist[i];
			var pt = new BMap.Point(equipVO.longitude, equipVO.latitude);
			points.push(pt);
			var myIcon = new BMap.Icon(ctxPath + "/images/car_icon/"+ equipVO.icon + ".gif", new BMap.Size(28, 28));
			var myMarker = new BMap.Marker(pt, { icon : myIcon }); // 创建标注
			map.addOverlay(myMarker); // 将标注添加到地图中
			
			createContent(equipVO);
			myMarker.addEventListener("click", function(e) {
				var p = e.target;
				var lng = p.getPosition().lng, lat = p.getPosition().lat;
				equipClick(lng, lat, 15);
			});
		}
		setZoom(points);
	}

	//正常的地图标记方法，不需要考虑地图级别
	function createEquipInfo(datalist) {
		for (var i = 0; i < datalist.length; i++) {
			var equipVO = datalist[i];
			var pt = new BMap.Point(equipVO.longitude, equipVO.latitude);
			var myIcon = new BMap.Icon(ctxPath + "/images/car_icon/" + equipVO.icon + ".gif", new BMap.Size(28, 28));
			var myMarker = new BMap.Marker(pt, { icon : myIcon }); // 创建标注
			map.addOverlay(myMarker); // 将标注添加到地图中
			
			createContent(equipVO);
			myMarker.addEventListener("click", function(e) {
				var p = e.target;
				var lng = p.getPosition().lng, lat = p.getPosition().lat;
				equipClick(lng, lat, 15);
			});
		}
	}
	
	//生成标注信息
	function createContent(equipVO){
		//标注信息
		var content = '<div style="font-size:10px;height:auto;">';
		content += "车牌号：" + equipVO.equipcode;
		content += "<br/>";
		content += "地址：" + equipVO.place_name + equipVO.road_name;
		content += "<br/>";
		content += "速度：" + equipVO.speed + "公里/小时";
		content += "<br/>";
		content += "油耗：10.5 百公里/升  温度：32℃  压力：2.1MPa ";
		content += "<br/>";
		content += "定位时间：" + equipVO.gps_time;
		content += "<br/>";
		content += "备注：" + equipVO.memo;
		content += "</div>";
		var key = String(equipVO.longitude) + '_' + equipVO.latitude;
		contentMap[key] = content;
	}

	//小车点击事件，依赖openEquipInfo()，展示小车信息。
	function equipClick(longitude, latitude) {
		if (longitude && latitude && longitude != 'null' && latitude != 'null') {
			openEquipInfo(longitude, latitude, 15);
		} else {
			alert('没有该设备的任何GPS信息!');
		}
	}

	function openEquipInfo(lng, lat, mapLevel) {
		var opts = {
			enableMessage : false
		//设置允许信息窗发送短息  
		};
		var key = lng + '_' + lat;
		var content = contentMap[key];
		var point = new BMap.Point(lng, lat);
		if (mapLevel) {
			map.centerAndZoom(point, mapLevel);
		}
		var infoWindow = new BMap.InfoWindow(content, opts); // 创建信息窗口对象 
		map.openInfoWindow(infoWindow, point); //开启信息窗口
	}
	function watchFlagClick(){
		$('#myTab a:first').tab('show'); // Select first tab 
		getEquipsByLock();
	}
	//锁定按钮的勾选事件，加入if_else判断，是因为取消勾选时，需要加载原本的样式。
	function getEquipsByLock(){
		map.clearOverlays();
		if(document.getElementById("watchFlag").checked){
			var watchs = $(":checked");
			var equipcodes = "";
			//第一个是flag，要排除。
			if(watchs && watchs.length-1 > 0){
				for(var i=1;i<watchs.length;i++){
					var watch = watchs[i];
					equipcodes += watch.id+",";
				}
				$.ajax({
					type : "GET",
					url : ctxPath + '/te/et/loadEquipDataByLock.json',
					data : {equipcodes:equipcodes},
					success : function(data) {
						//创建覆盖物
						if (!data || data.length == 0 || data == "参数不正确") {
							return;
						}
						var dataList = JSON.parse(data).info;
						createEquipInfoByLock(dataList);
						createQueryCmp(dataList);
					}
				});
				
				
			}
		}else{
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
				url : ctxPath + '/te/et/loadEquipData.json',
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
						createEquipRoughInfo(roughInfo,dataList);
					} else {
						createEquipInfo(dataList);
					}
					createQueryCmp(dataList);
				}
			});
		}
	}

	//正常的加载数据方法
	function getEquips() {
		map.clearOverlays();
		if(document.getElementById("watchFlag").checked){
			var watchs = $(":checked");
			var equipcodes = "";
			//第一个是flag，要排除。
			if(watchs && watchs.length-1 > 0){
				for(var i=1;i<watchs.length;i++){
					var watch = watchs[i];
					equipcodes += watch.id+",";
				}
				$.ajax({
					type : "GET",
					url : ctxPath + '/te/et/loadEquipDataByLock.json',
					data : {equipcodes:equipcodes},
					success : function(data) {
						//创建覆盖物
						if (!data || data.length == 0 || data == "参数不正确") {
							return;
						}
						var dataList = JSON.parse(data).info;

						//返回值分为两种情况 简略信息和详细信息
						if (JSON.parse(data).roughInfo) {
							var roughInfo = JSON.parse(data).roughInfo;
							createEquipRoughInfo(roughInfo,dataList);
						} else {
							createEquipInfo(dataList);
						}
						createQueryCmp(dataList);
					}
				});
				
				
			}
		}else{
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
				url : ctxPath + '/te/et/loadEquipData.json',
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
						createEquipRoughInfo(roughInfo,dataList);
					} else {
						createEquipInfo(dataList);
					}
					createQueryCmp(dataList);
				}
			});
		}

	}
	//定时器
	window.setInterval(getEquipsByLock,1000*30); 
	

	//地图拖拽事件 和 地图级别缩放事件	和地图移动事件
	map.addEventListener("dragstart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("dragend", function() {
		//从后台获取参数
		getEquips();
	});
	map.addEventListener("zoomstart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("zoomend", function() {
		//从后台获取参数
		getEquips();
	});
	map.addEventListener("movestart", function() {
		//从后台获取参数
		map.clearOverlays();
	});
	map.addEventListener("moveend", function() {
		//从后台获取参数
		getEquips();
	});
</script>
</html>