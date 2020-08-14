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
			padding-right: 8px;
		}
		
		#container {
			height: 100%;
			width: 100%;
		}
		
		#toolbar {
			height: 40px;
			padding: 3px;
			padding-left: 10px;
		}
		
		td {
			font-size: 12px;
		}
		
		span {
			color: red;
		}
		</style>
</head>
	
	<body>
		<!-- 页面容器 -->
		<div id = "container">
			<!-- kpi统计 -->
			<div id = "kpi_amount" class="row show-grid">
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
							    <input class="form-control input-sm" type="text" id="stratTime" placeholder="开始">
							</div>
							<div class="col-xs-6">
							    <input class="form-control input-sm" type="text" id="endTime" placeholder="结束">
							</div>
						</div>
					<div class="col-md-4">
						<div class="row">
							<div class="input-group">
								<input type="text" class="form-control input-sm" id="keyword"
									aria-label="..."  placeholder="搜索承运商">
								<div class="input-group-btn">
									<button type="button"
										class="btn btn-sm btn-default dropdown-toggle"
										id="carr_choice" data-toggle="dropdown" aria-haspopup="true"
										aria-expanded="false" onclick="carrier_search();">搜索</span>
									</button>
									<ul class="dropdown-menu dropdown-menu-right" id="carr_choice_ul">
									</ul>
								</div>
								<!-- /btn-group -->
								
							</div>
						</div>
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
									<td><strong>上周</strong></td>
									<td></td>
									<td></td>
								</tr>
								<tr class="warning">
									<td></td>
									<td><span id="day_top1_name"> </span></td>
									<td><span id="day_top1_score">0</span></td>
								</tr>
								<tr class="default">
									<td></td>
									<td><span id="day_top2_name"> </span></td>
									<td><span id="day_top2_score">0</span></td>
								</tr>
								<tr class="warning">
									<td></td>
									<td><span id="day_top3_name"> </span></td>
									<td><span id="day_top3_score">0</span></td>
								</tr>
							</tbody>
						</table>
					</div>
					<div id = "month_amount">
						<table class="table table-condensed">
							<tbody>
								<tr>
									<td><strong>上月</strong></td>
									<td></td>
									<td></td>
								</tr>
								<tr class="warning">
									<td></td>
									<td><span id="month_top1_name"> </span></td>
									<td><span id="month_top1_score">0</span></td>
								</tr>
								<tr class="default">
									<td></td>
									<td><span id="month_top2_name"> </span></td>
									<td><span id="month_top2_score">0</span></td>
								</tr>
								<tr class="warning">
									<td></td>
									<td><span id="month_top3_name"> </span></td>
									<td><span id="month_top3_score">0</span></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div id="kpi_analyze" class="row show-grid">
				<div id = "route"  class="col-md-6">
					<div id = "route_analyze"></div>
				</div>
				<div id = "carr"  class="col-md-6">
					<div id = "carr_analyze"></div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			var ctxPath = '<%=request.getContextPath()%>';
			var amountData = [];//全局变量，用来纪录货物统计的查询结果
			var routeAnalyzeData = [];//货物按照线路统计数据
			var carrAnalyzeData = [];//货物按照客户统计数据
			var timeId = "lastWeek";//时间类型，默认是上周
			var tagName = "全部";//默认显示全部承运商
			var pk_carrier = "";
			var indexRouteName = "全部";//默认显示全部指标
			var indexCarrName = "全部";//默认显示全部指标
			var startTime = "";
			var endTime = "";
			
			//查询
			function carrier_search(){
				var keyword = document.getElementById('keyword').value;
				var innerHTML="";
				$.ajax({
					type : "GET",
					url : ctxPath + '/workBench/loadCarriers.json',
					data : {keyword : keyword},
					async : false,
					success : function(data) {
						if(data && data.length > 0){
							for(var i=0;i<data.length;i++){
								var carrier = data[i];
								var unit = "<li onclick='getChartData("+JSON.stringify(carrier)+")'><a href='#'>"+carrier.carr_name+"</a></li>"
								innerHTML += unit;
							}
						}
					}
				});
				document.getElementById('carr_choice_ul').innerHTML = innerHTML;
			};
			
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
					alert("[失效时间 ]不能早于[生效时间 ] ！");
				}
			});
			$(document).ready(function() { 
				var screen_height = $("#container")[0].offsetHeight;
				$("#kpi_amount")[0].style.height = screen_height*0.6 + "px";
				$("#amount")[0].style.height = screen_height*0.6-40 + "px";
				$("#day_amount")[0].style.height = screen_height*0.3 + "px";
				$("#month_amount")[0].style.height = screen_height*0.3 + "px";
				$("#kpi_analyze")[0].style.height = screen_height*0.4 + "px";
				$("#route_analyze")[0].style.height = screen_height*0.4-20 + "px";
				$("#carr_analyze")[0].style.height = screen_height*0.4-20 + "px";
				
				//获取货量分析图形数据
				getChartData();
				//获取当日和当月的数据
				getDateAmountData();
				//获取按线路显示当月信息数据
				getRouteAnalyzeData();
				//获取按客户显示当月信息数据
				getCarrAnalyzeData();
				
				
			}); 
			
			function loadCarriers(){
				var innerHTML="";
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadCarriers.json',
					data : {},
					success : function(data) {
						if(data && data.length > 0){
							for(var i=0;i<data.length;i++){
								var carrier = data[i];
								var unit = "<li onclick='saveCarrier("+JSON.stringify(carrier)+")'><a href='#'>"+carrier.carr_name+"</a></li>"
								innerHTML += unit;
							}
						}
					}
				});
				document.getElementById('carr_choice_ul').innerHTML = innerHTML;
			}
			
			function saveCarrier(carrier){
				pk_carrier = carrier.pk_carrier;
				tagName = carrier.carr_name;
			}
			
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
				getChartData();
			}
			
			
			function getChartData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadKPIAmountData.json',
					data : {"stratTime":startTime,"endTime":endTime,"timeId":timeId,"pk_carrier":pk_carrier},
					success : function(data) {
						if(data){
							amountData = data;
							drawChart();
						}
					}
				});
			}
			
			function drawChart(){
				var amountChart = echarts.init(document.getElementById('amount'));
				var values = amountData.scores;
				var indexs = amountData.indexs;
				var option = {
					    title : {
					        text: '绩效分析',
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
					            data : indexs
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
		       		url:ctxPath + '/workBench/loadKPIDateAmount.json',
					data : {},
					success : function(data) {
						if(data){
							var dayData = data.day;
							var monthData = data.month;
							document.getElementById("day_top1_name").innerHTML = dayData.top1_name;
							document.getElementById("day_top1_score").innerHTML = dayData.top1_score;
							document.getElementById("day_top2_name").innerHTML = dayData.top2_name;
							document.getElementById("day_top2_score").innerHTML = dayData.top2_score;
							document.getElementById("day_top3_name").innerHTML = dayData.top3_name;
							document.getElementById("day_top3_score").innerHTML = dayData.top3_score;
							
							document.getElementById("month_top1_name").innerHTML = monthData.top1_name;
							document.getElementById("month_top1_score").innerHTML = monthData.top1_score;
							document.getElementById("month_top2_name").innerHTML = monthData.top2_name;
							document.getElementById("month_top2_score").innerHTML = monthData.top2_score;
							document.getElementById("month_top3_name").innerHTML = monthData.top3_name;
							document.getElementById("month_top3_score").innerHTML = monthData.top3_score;
							
						}
					}
				});
			}
			
			function getRouteAnalyzeData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadKPIRouteAnalyze.json',
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
				var values =routeAnalyzeData.scores;
				var routes = routeAnalyzeData.routes;
				var option = {
					    title : {
					        text: '绩效—线路分析',
					        subtext: indexRouteName
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:[indexRouteName]
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
					            data : routes
					        }
					    ],
					    yAxis : [
					        {
					            type : 'value'
					        }
					    ],
					    series : [
					        {
					            name:indexRouteName,
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
			
			function getCarrAnalyzeData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadKPICarrAnalyze.json',
					data : {},
					success : function(data) {
						if(data){
							carrAnalyzeData = data;
							drawCarrAnalyzeChart();
						}
					}
				});
			}
			
			function drawCarrAnalyzeChart(){
				var carrAnalyzeChart = echarts.init(document.getElementById('carr_analyze'));
				var values =carrAnalyzeData.scores;
				var carrs = carrAnalyzeData.carrs;
				var option = {
					    title : {
					        text: '绩效—承运商分析',
					        subtext: indexCarrName
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:[indexCarrName]
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
					            data : carrs
					        }
					    ],
					    yAxis : [
					        {
					            type : 'value'
					        }
					    ],
					    series : [
					        {
					            name:indexCarrName,
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
				carrAnalyzeChart.setOption(option);                          
			}
			
		</script>
	</body>
</html>