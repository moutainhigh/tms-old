<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html>
	<head>
		<script type="text/javascript" src="<c:url value="/busi/map/jquery-1.12.2.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/echarts.min.js?v=${version}" />"></script>
		<!-- Bootstrap 支持 -->
		<script src="//cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		<link rel="stylesheet" href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">
		<script type="text/javascript" src="<c:url value="/js/bootstrap-datetimepicker.min.js"/>"></script>
		<style type="text/css">
			body, html {
				width: 100%;
				height: 100%;
				margin: 0;
				padding: 4px;
				/* padding-left: 2px; */
				padding-right: 8px;
			}
					
			#container{
				height: 100%;
				width: 100%;
			}
			#goods_amount{
				
			}
			#toolbar{
				height: 40px;
				padding : 3px;
				padding-left: 10px;
				/* background-color: #AA7700; */
			}
			#amount{
				/* background: #4400B3 ; */
				
			}
			#main_amount{
			}
			#date_amount{
				
			}
			#day_amount{
			}
			#month_amount{
			}
			#goods_analyze{
			}
			#line_analyze{
			}
			#cust_analyze{
			}
			/* table{
				border-collapse: collapse; 
				border: none; 
			}
			th{
				border-bottom: solid #000 1px; 
			} */
			td {
				font-size :12px;
			}
		span{
			color: red;
		}
		</style>
	</head>
	
	<body>
		<!-- 页面容器 -->
		<div id = "container">
			<!-- 货品统计 -->
			<div id = "goods_amount" class="row show-grid">
				<div id = "main_amount" class="col-md-9">
					<div id = "toolbar" class="row show-grid">
						<!-- 工具条 -->
						<div class="col-md-4">
							<button type="button" name="time" id="lastWeek" onclick="saveTime(this.id);" class="btn btn-success btn-sm">上周</button>
							<button type="button" name="time" id="thisWeek" onclick="saveTime(this.id);" class="btn btn-default btn-sm">本周</button>
							<button type="button" name="time" id="lastMonth" onclick="saveTime(this.id);" class="btn btn-default btn-sm">上月</button>
							<button type="button" name="time" id="thisMonth" onclick="saveTime(this.id);" class="btn btn-default btn-sm">本月</button>
						</div>
						<!-- 时间选择器 -->
						<div class="col-md-4 row show-grid">
							<div class="col-xs-6">
							    <input class="form-control input-sm" type="text" id="stratTime"
						  			placeholder="开始">
							</div>
							<div class="col-xs-6">
							    <input class="form-control input-sm" type="text" id="endTime"
						  			placeholder="结束">
							</div>
						</div>
						<div class="col-md-4">
							<button type="button" name="tag" id="weight" onclick="switchTag(this.id);" class="btn btn-danger btn-sm">重量</button>
							<button type="button" name="tag" id="volume" onclick="switchTag(this.id);"  class="btn btn-default btn-sm">体积</button>
							<button type="button" name="tag" id="packNum" onclick="switchTag(this.id);"  class="btn btn-default btn-sm">件数</button>
							<button type="button" name="tag" id="orderNum" onclick="switchTag(this.id);"  class="btn btn-default btn-sm">单量</button>
						</div>
					</div>
					<div id = "amount">
					
						<!-- 折线图  -->
					
					</div>
				</div>
				<!-- 货品按日期统计 -->
				<div id = "date_amount" class="col-md-3">
					<div id = "day_amount">
						<table class="table table-condensed">
							<tbody>
								<tr>
									<td><strong>今日</strong></td>
									<td></td>
									<td></td>
								</tr>
								<tr class="warning">
									<td>重量</td>
									<td><span id="day_weight">0</span></td>
									<td>T</td>
								</tr>
								<tr class="default">
									<td>体积</td>
									<td><span id="day_volume">0</span></td>
									<td>M³</td>
								</tr>
								<tr class="warning">
									<td>件数</td>
									<td><span id="day_packNum">0</span></td>
									<td>件</td>
								</tr>
								<tr class="default">
									<td>单数</td>
									<td><span id="day_orderNum">0</span></td>
									<td>单</td>
								</tr>
							</tbody>
						</table>
					</div>
					<div id = "month_amount">
						<table class="table table-condensed">
							<tbody>
								<tr>
									<td><strong>本月</strong></td>
									<td></td>
									<td></td>
								</tr>
								<tr class="warning">
									<td>重量</td>
									<td><span id="month_weight">0</span></td>
									<td>T</td>
								</tr>
								<tr class="default">
									<td>体积</td>
									<td><span id="month_volume">0</span></td>
									<td>M³</td>
								</tr>
								<tr class="warning">
									<td>件数</td>
									<td><span id="month_packNum">0</span></td>
									<td>件</td>
								</tr>
								<tr class="default">
									<td>单数</td>
									<td><span id="month_orderNum">0</span></td>
									<td	>单</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div id="goods_analyze" class="row show-grid">
				<div id = "route"  class="col-md-6">
					<div id = "route_bar" style="text-align:right">
						<button type="button" name="route" id="route_weight" onclick="switchRoute(this.id);" class="btn btn-danger btn-xs">重量</button>
						<button type="button" name="route" id="route_volume" onclick="switchRoute(this.id);"  class="btn btn-default btn-xs">体积</button>
						<button type="button" name="route" id="route_packNum" onclick="switchRoute(this.id);"  class="btn btn-default btn-xs">件数</button>
						<button type="button" name="route" id="route_orderNum" onclick="switchRoute(this.id);"  class="btn btn-default btn-xs">单量</button>
					</div>
					<div id = "route_analyze"></div>
				</div>
				<div id = "cust"  class="col-md-6">
					<div id = "cust_bar" style="text-align:right">
						<button type="button" name="cust" id="cust_weight" onclick="switchCust(this.id);" class="btn btn-danger btn-xs">重量</button>
						<button type="button" name="cust" id="cust_volume" onclick="switchCust(this.id);"  class="btn btn-default btn-xs">体积</button>
						<button type="button" name="cust" id="cust_packNum" onclick="switchCust(this.id);"  class="btn btn-default btn-xs">件数</button>
						<button type="button" name="cust" id="cust_orderNum" onclick="switchCust(this.id);"  class="btn btn-default btn-xs">单量</button>
					</div>
					<div id = "cust_analyze"></div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			var ctxPath = '<%=request.getContextPath()%>';
			var goodsAmountData = [];//全局变量，用来纪录货物统计的查询结果
			var routeAnalyzeData = [];//货物按照线路统计数据
			var custAnalyzeData = [];//货物按照客户统计数据
			var timeId = "lastWeek";//时间类型，默认是上周
			var tagId = "weight";//默认显示重量
			var routeId = "weight";//默认显示重量
			var custId = "weight";//默认显示重量
			var startTime = "";
			var endTime = "";
			
			$('#stratTime').datetimepicker({
				format : 'yyyy-mm-dd',
				weekStart : 1,
				autoclose : 1,
				todayBtn : 'linked',
				language : 'zh-CN'
			});
			
			
			$('#endTime').datetimepicker({
				format : 'yyyy-mm-dd',
				weekStart : 1,
				autoclose : true,
				todayBtn : 'linked',
				language : 'zh-CN'
			}).on('changeDate', function(ev) {
				var end = ev.date.valueOf();
				if (start && end < start) {
					alert("“失效时间 ”不能早于“生效时间 ” ！");
				}
			});
			$(document).ready(function() { 
				var screen_height = $("#container")[0].offsetHeight;
				$("#goods_amount")[0].style.height = screen_height*0.6 + "px";
				$("#amount")[0].style.height = screen_height*0.6-40 + "px";
				$("#day_amount")[0].style.height = screen_height*0.3 + "px";
				$("#month_amount")[0].style.height = screen_height*0.3 + "px";
				$("#goods_analyze")[0].style.height = screen_height*0.4 + "px";
				$("#route_analyze")[0].style.height = screen_height*0.4-20 + "px";
				$("#cust_analyze")[0].style.height = screen_height*0.4-20 + "px";
				
				//获取货量分析图形数据
				getGoodsChartData();
				//获取当日和当月的数据
				getDateAmountData();
				//获取按线路显示当月信息数据
				getRouteAnalyzeData();
				//获取按客户显示当月信息数据
				getCustAnalyzeData();
				
				
			}); 
			
			function saveTime(id){
				var timeButtons = $("[name='time']");
				for(var i=0;i<timeButtons.length;i++){
					if(timeButtons[i].id == id){
						timeButtons[i].setAttribute("class","btn btn-success btn-sm");
					}else{
						timeButtons[i].setAttribute("class","btn btn-default btn-sm");
					}
				}
				timeId = id;
				//加载数据
				getGoodsChartData();
			}
			
			function switchTag(id){
				var tagButtons = $("[name='tag']");
				for(var i=0;i<tagButtons.length;i++){
					if(tagButtons[i].id == id){
						tagButtons[i].setAttribute("class","btn btn-danger btn-sm");
					}else{
						tagButtons[i].setAttribute("class","btn btn-default btn-sm");
					}
				}
				tagId = id;
				//需要把时间信息清除掉，要不然没法判断到底是按时间来查询还是按照ID来查询
				startTime="";
				endTime="";
				drawGoodsChart();
			}
			
			function switchRoute(id){
				var routeButtons = $("[name='route']");
				for(var i=0;i<routeButtons.length;i++){
					if(routeButtons[i].id == id){
						routeButtons[i].setAttribute("class","btn btn-danger btn-xs");
					}else{
						routeButtons[i].setAttribute("class","btn btn-default btn-xs");
					}
				}
				routeId = id.substring(6,id.length);
				drawRouteAnalyzeChart();
			}
			
			function switchCust(id){
				var custButtons = $("[name='cust']");
				for(var i=0;i<custButtons.length;i++){
					if(custButtons[i].id == id){
						custButtons[i].setAttribute("class","btn btn-danger btn-xs");
					}else{
						custButtons[i].setAttribute("class","btn btn-default btn-xs");
					}
				}
				custId = id.substring(5,id.length);
				drawCustAnalyzeChart();
			}
			
			function getGoodsChartData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadGoodsAmount.json',
					data : {"stratTime":startTime,"endTime":endTime,"timeId":timeId},
					success : function(data) {
						if(data){
							goodsAmountData = data;
							drawGoodsChart();
						}
					}
				});
			}
			
			
			function drawGoodsChart(){
				var amountChart = echarts.init(document.getElementById('amount'));
				var values =[];
				var tagName = "";
				if(tagId == "weight"){
					tagName = "重量/T";
					values = goodsAmountData.weights;
				}
				if(tagId == "volume"){
					tagName = "体积/M³";
					values = goodsAmountData.volumes;
				}
				if(tagId == "packNum"){
					tagName = "件数/件";
					values = goodsAmountData.packNums;
				}
				if(tagId == "orderNum"){
					tagName = "单量/单";
					values = goodsAmountData.orderNums;
				}
				var option = {
					    title : {
					        text: '货量分析',
					        subtext: tagName
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:[tagName]
					    },
					    toolbox: {
					        show : true,
					        feature : {
					            mark : {show: true},
					            dataView : {show: true, readOnly: false},
					            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
					            restore : {show: true},
					            saveAsImage : {show: true}
					        }
					    },
					    calculable : true,
					    xAxis : [
					        {
					            type : 'category',
					            boundaryGap : false,
					            data : goodsAmountData.dates,
					            interval:0   
					        }
					    ],
					    yAxis : [
					        {
					            type : 'value'
					        }
					    ],
					    series : [
					        {
					            name:tagName,
					            type:'line',
					            smooth:true,
					            itemStyle: {normal: {areaStyle: {type: 'default'}}},
					            data:values
					        }
					    ]
					};
				amountChart.setOption(option);
			}
			
			function getDateAmountData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadDateAmount.json',
					data : {},
					success : function(data) {
						if(data){
							var dayData = data.day;
							var monthData = data.month;
							document.getElementById("day_weight").innerHTML = dayData.weight;
							document.getElementById("day_volume").innerHTML = dayData.volume;
							document.getElementById("day_packNum").innerHTML = dayData.packNum;
							document.getElementById("day_orderNum").innerHTML = dayData.orderNum;
							
							document.getElementById("month_weight").innerHTML = monthData.weight;
							document.getElementById("month_volume").innerHTML = monthData.volume;
							document.getElementById("month_packNum").innerHTML = monthData.packNum;
							document.getElementById("month_orderNum").innerHTML = monthData.orderNum;
							
						}
					}
				});
			}
			
			function getRouteAnalyzeData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadRouteAnalyze.json',
					data : {},
					success : function(data) {
						if(data){
							routeAnalyzeData = data;
							drawRouteAnalyzeChart();
						}
					}
				});
			}
			function drawRouteAnalyzeChart(){
				var routeAnalyzeChart = echarts.init(document.getElementById('route_analyze'));
				var values =[];
				var routeName = "";
				var routes = routeAnalyzeData.routes;
				if(routeId == "weight"){
					routeName = "重量/T";
					values = routeAnalyzeData.weights;
				}
				if(routeId == "volume"){
					routeName = "体积/M³";
					values = routeAnalyzeData.volumes;
				}
				if(routeId == "packNum"){
					routeName = "件数/件";
					values = routeAnalyzeData.packNums;
				}
				if(routeId == "orderNum"){
					routeName = "单量/单";
					values = routeAnalyzeData.orderNums;
				}
				var option = {
					    title : {
					        text: '货量—线路分析',
					        subtext: routeName
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:[routeName]
					    },
					    toolbox: {
					        show : true,
					        feature : {
					            mark : {show: true},
					            magicType : {show: true, type: ['line', 'bar']},
					            saveAsImage : {show: true}
					           
					        }
					    },
					    calculable : true,
					    xAxis : [
					        {
					            type : 'category',
					            data : routes,
					            axisLabel:{  
                                    interval:0 ,
                                    rotate:45,
					            }
					        }
					    ],
					    yAxis : [
					        {
					            type : 'value'
					        }
					    ],
					    series : [
					        {
					            name:routeName,
					            type:'bar',
					            data:values,
					            markLine : {
					                data : [
					                    {type : 'average', name : '平均值'}
					                ]
					            }
					        }
					    ]
					};
				routeAnalyzeChart.setOption(option);               
			}
			
			function getCustAnalyzeData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadCustAnalyze.json',
					data : {},
					success : function(data) {
						if(data){
							custAnalyzeData = data;
							drawCustAnalyzeChart();
						}
					}
				});
			}
			
			function drawCustAnalyzeChart(){
				var custAnalyzeChart = echarts.init(document.getElementById('cust_analyze'));
				var values =[];
				var custs = custAnalyzeData.custs;
				var custName = "";
				if(custId == "weight"){
					custName = "重量/T";
					values = custAnalyzeData.weights;
				}
				if(custId == "volume"){
					custName = "体积/M³";
					values = custAnalyzeData.volumes;
				}
				if(custId == "packNum"){
					custName = "件数/件";
					values = custAnalyzeData.packNums;
				}
				if(custId == "orderNum"){
					custName = "单量/单";
					values = custAnalyzeData.orderNums;
				}
				var option = {
					    title : {
					        text: '货量—客户分析',
					        subtext: "",
					        x:'center'
					    },
					    tooltip : {
					        trigger: 'item',
					        formatter: "{a} <br/>{b} : {c} ({d}%)"
					    },
					    legend: {
					        orient : 'vertical',
					        x : 'left',
					        data:custs
					    },
					    toolbox: {
					        show : true,
					        feature : {
					            mark : {show: true},
					            dataView : {show: true, readOnly: false},
					            magicType : {
					                show: true, 
					                type: ['pie', 'funnel'],
					                option: {
					                    funnel: {
					                        x: '25%',
					                        width: '50%',
					                        funnelAlign: 'left',
					                        max: 1548
					                    }
					                }
					            },
					            restore : {show: true},
					            saveAsImage : {show: true}
					        }
					    },
					    calculable : true,
					    series : [
					        {
					            name:custName,
					            type:'pie',
					            radius : '55%',
					            center: ['50%', '60%'],
					            data:values
					        }
					    ]
					};
				custAnalyzeChart.setOption(option);                          
			}
			
		</script>
	</body>
</html>