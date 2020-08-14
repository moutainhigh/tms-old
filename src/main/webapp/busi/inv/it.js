var map,lushu;//全局变量


function getQueryString(name) {
    var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i');
    var r = window.location.search.substr(1).match(reg);
    if (r != null) {
        return unescape(r[2]);
    }
    return null;
}


jQuery(document).ready(function() {
	$("#query_cmp")[0].style.height = $("#baidu_map")[0].offsetHeight+ "px";
	if(document.getElementById(surrentCarno)){
		document.getElementById(surrentCarno).style.color="#FF0000";
	}
    map = new BMap.Map("baidu_map");// 创建地图实例
    map.enableScrollWheelZoom(true);//使用鼠标滚轮来缩放地图
    var points = [];
    points.push(sp);
    points.push(ep);
    setZoom(points);

    var startIcon = new BMap.Icon(ctxPath + "/busi/inv/icon/start.png", new BMap.Size(40, 80),{anchor : new BMap.Size(20, 40)});
    var endIcon = new BMap.Icon(ctxPath + "/busi/inv/icon/end.png", new BMap.Size(40, 80),{anchor : new BMap.Size(20, 40)});
    var startMarker = new BMap.Marker(sp, {icon : startIcon});
    var endMarker = new BMap.Marker(ep, {icon : endIcon});
    map.addOverlay(startMarker);
    map.addOverlay(endMarker);

    var vbillno = getQueryString("vbillno");
    $.ajax({
        type : "GET",
        data : {vbillno : vbillno},
        url : 'loadTrailsByVbillno.json',
        success : function (data) {
            if(data){
                goPlayback(data);
            }
        },
    });

})


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

function goPlayback(trackVOs){
    if(!trackVOs || trackVOs.length == 0){
        return;
    }
    //先标记当前设备
    var ptAry = [],ssp;
    var surrentTrackVO = trackVOs[trackVOs.length-1];
    for(var i=0;i<trackVOs.length;i++){
        var trackVO = trackVOs[i];
        var obj = {lng:trackVO.longitude,lat:trackVO.latitude,title : trackVO.place_name+trackVO.road_name};
        if (i == trackVOs.length-1){
            ssp = obj;
        }
        ptAry.push(new BMap.Point(trackVO.longitude, trackVO.latitude));

    }
    //标注信息
    var content = '<div style="font-size:10px;height:auto;">';
    content += "车牌号：" + surrentCarno;
    content += "<br/>";
    content += "地址：" + surrentTrackVO.place_name + surrentTrackVO.road_name;
    content += "<br/>";
    content += "速度：" + surrentTrackVO.speed + "公里/小时";
    content += "<br/>";
    content += "油耗：10.5 百公里/升  温度：32℃  压力：2.1MPa ";
    content += "<br/>";
    content += "定位时间：" + surrentTrackVO.gps_time;
    content += "<br/>";
    content += "备注：" + surrentTrackVO.memo;
    content += "</div>";

    //从起点到终点
    if(!status || !(status == '到货' || status == '签收' || status == '回单')){
        var ssp = new BMap.Point(ssp.lng, ssp.lat);//终点
        var startIcon = new BMap.Icon(ctxPath + "/busi/inv/icon/current.png", new BMap.Size(40, 80));
        var surrentMarker = new BMap.Marker(ssp, {icon : startIcon});
        map.addOverlay(surrentMarker);
        surrentMarker.addEventListener("click",function (e) {
            map.centerAndZoom(ssp, 15);
            var opts = {
                enableMessage : false
                //设置允许信息窗发送短息
            };
            var infoWindow = new BMap.InfoWindow(content, opts); // 创建信息窗口对象
            map.openInfoWindow(infoWindow, ssp); //开启信息窗口
        })
    }
    map.addOverlay(new BMap.Polyline(ptAry, {strokeColor: '#CC0033'}));
    map.setViewport(ptAry);

}

