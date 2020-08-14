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
				padding: 5px;
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
							<!-- 货品按日期统计 -->
				<div id = "main_amount" class="col-md-9">
					<div id = "toolbar" class="row show-grid">
						<!-- 工具条 -->
						<div class="col-md-4">
							<button type="button" name="time" id="lastWeek" onclick="saveTime(this.id);" class="btn btn-success btn-sm">上周</button>
							<button type="button" name="time" id="theWeek" onclick="saveTime(this.id);" class="btn btn-default btn-sm">本周</button>
							<button type="button" name="time" id="lastMonth" onclick="saveTime(this.id);" class="btn btn-default btn-sm">上月</button>
							<button type="button" name="time" id="theMonth" onclick="saveTime(this.id);" class="btn btn-default btn-sm">本月</button>
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
					</div>
					<div id = "amount">
					
						<!-- 折线图  -->
					
					</div>
				</div>
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
									<td>应收</td>
									<td><span id="day_rece">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="default">
									<td>成本</td>
									<td><span id="day_pay">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="warning">
									<td>毛利</td>
									<td><span id="day_fee">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="default">
									<td>毛利率</td>
									<td><span id="day_fee_profit">0</span></td>
									<td>%</td>
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
									<td>应收</td>
									<td><span id="month_rece">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="default">
									<td>成本</td>
									<td><span id="month_pay">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="warning">
									<td>毛利</td>
									<td><span id="month_fee">0</span></td>
									<td>￥</td>
								</tr>
								<tr class="default">
									<td>毛利率</td>
									<td><span id="month_fee_profit">0</span></td>
									<td>%</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div id="charge_analyze" class="row show-grid">
				<div id = "rece"  class="col-md-6">
					<div id = "rece_analyze"></div>
				</div>
				<div id = "pay"  class="col-md-6">
					<div id = "pay_analyze"></div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			var ctxPath = '<%=request.getContextPath()%>';
			var chargeAmountData = [];//全局变量，用来纪录费用统计的查询结果
			var tagId = "lastWeek";//默认显示上周数据
			var startTime;
			var endTime;

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
				$("#charge_analyze")[0].style.height = screen_height*0.4 + "px";
				$("#rece_analyze")[0].style.height = screen_height*0.4-20 + "px";
				$("#pay_analyze")[0].style.height = screen_height*0.4-20 + "px";
				
				//获取货量分析图形数据
				getChargeChartData();
				//获取当日和当月的数据
				getChargeDateAmountData();
				//获取按线路显示当月信息数据
				getReceAnalyze();
				//获取按客户显示当月信息数据
				getPayAnalyze();
				
				
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
				tagId = id;
				//加载数据
				getChargeChartData();
			}
			
			function getChargeChartData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadChargeAmount.json',
					data : {"stratTime":startTime,"endTime":endTime,'timeId':tagId},
					success : function(data) {
						if(data){
							chargeAmountData = data;
							drawChargeChart();
						}
					}
				});
			}
			
			function drawChargeChart(){
				var amountChart = echarts.init(document.getElementById('amount'));
				var tagName = "";
				if(tagId == "lastWeek"){
					tagName = "上周";
				}
				if(tagId == "thisWeek"){
					tagName = "本周";
				}
				if(tagId == "lastMonth"){
					tagName = "上月";
				}
				if(tagId == "theisMonth"){
					tagName = "本月";
				}
				var option = {
					    title : {
					        text: '费用统计',
					        subtext: tagName
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:['应收','成本','毛利']
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
					            data : chargeAmountData.dates,
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
					            name:'应收',
					            type:'line',
					            smooth:true,
					            itemStyle: {normal: {areaStyle: {type: 'default'}}},
					            data:chargeAmountData.reces
					        },
					        {
					            name:'成本',
					            type:'line',
					            smooth:true,
					            itemStyle: {normal: {areaStyle: {type: 'default'}}},
					            data:chargeAmountData.pays
					        },
					        {
					            name:'毛利',
					            type:'line',
					            smooth:true,
					            itemStyle: {normal: {areaStyle: {type: 'default'}}},
					            data:chargeAmountData.fees

					        }
					    ]
					};
				amountChart.setOption(option);
			}
			
			function getChargeDateAmountData(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadChargeDateAmount.json',
					data : {},
					success : function(data) {
						if(data){
							var dayData = data.day;
							var monthData = data.month;
							document.getElementById("day_rece").innerHTML = dayData.rece;
							document.getElementById("day_pay").innerHTML = dayData.pay;
							document.getElementById("day_fee").innerHTML = dayData.fee;
							document.getElementById("day_fee_profit").innerHTML = dayData.profit;
							
							document.getElementById("month_rece").innerHTML = monthData.rece;
							document.getElementById("month_pay").innerHTML = monthData.pay;
							document.getElementById("month_fee").innerHTML = monthData.fee;
							document.getElementById("month_fee_profit").innerHTML = monthData.profit;
							
						}
					}
				});
			}
			
			function getReceAnalyze(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadReceAnalyze.json',
					data : {},
					success : function(data) {
						if(data){
							drawReceAnalyzeChart(data);
						}
					}
				});
			}
			function drawReceAnalyzeChart(data){
				var receAnalyzeChart = echarts.init(document.getElementById('rece_analyze'));
				var values = data.amounts;
				var custs = data.custs;
				var option = {
					    title : {
					        text: '应收统计',
					        subtext: "上月"
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:['应收']
					    },
					    toolbox: {
					        show : true,
					        feature : {
					            mark : {show: true},
					            dataView : {show: true, readOnly: false},
					            magicType : {show: true, type: ['line', 'bar']},
					            restore : {show: true},
					            saveAsImage : {show: true}
					        }
					    },
					    calculable : true,
					    xAxis : [
					        {
					            type : 'category',
					            data : custs,
					            axisLabel:{  
                                    interval:'auto' ,
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
					            name:'应收',
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
				receAnalyzeChart.setOption(option);               
			}
			
			function getPayAnalyze(){
				$.ajax({
					type:"GET",
		       		url:ctxPath + '/workBench/loadPayAnalyze.json',
					data : {},
					success : function(data) {
						if(data){
							drawPayAnalyzeChart(data);
						}
					}
				});
			}
			
			function drawPayAnalyzeChart(data){
				var payAnalyzeChart = echarts.init(document.getElementById('pay_analyze'));
				var values = data.amounts;
				var carrs = data.carrs
				var option = {
					    title : {
					        text: '成本统计',
					        subtext: "上月"
					    },
					    tooltip : {
					        trigger: 'axis'
					    },
					    legend: {
					        data:['成本']
					    },
					    toolbox: {
					        show : true,
					        feature : {
					            mark : {show: true},
					            dataView : {show: true, readOnly: false},
					            magicType : {show: true, type: ['line', 'bar']},
					            restore : {show: true},
					            saveAsImage : {show: true}
					        }
					    },
					    calculable : true,
					    xAxis : [
					        {
					            type : 'category',
					            data : carrs,
					            axisLabel:{  
                                    interval: 'auto' ,
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
					            name:'成本',
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
				payAnalyzeChart.setOption(option);                          
			}
			
		</script>
	</body>
</html>