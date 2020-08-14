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

#chart1 {
	height: 100%;
}

#chart2 {
	height: 100%;
}
</style>
</head>
	
	<body>
		<!-- 页面容器 -->
	<div id="container">
		<div id="toolbar" class="row show-grid">
			<!-- 工具条 -->
			<div class="col-md-4">
				<button type="button" name="chart" id="chart1"
					onclick="chartChange(this.id);" class="btn btn-success btn-sm">送达时效</button>
				<button type="button" name="chart" id="chart2"
					onclick="chartChange(this.id);" class="btn btn-default btn-sm">到货及时率</button>
			</div>
		</div>
		<div>
			<div id="chart" class="col-md-12"></div>
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
			
			function chartChange(id){
				var chartButtons = $("[name='chart']");
				for(var i=0;i<chartButtons.length;i++){
					if(chartButtons[i].id == id){
						chartButtons[i].setAttribute("class","btn btn-success btn-sm");
					}else{
						chartButtons[i].setAttribute("class","btn btn-default btn-sm");
					}
				}
				//加载数据
				if(id=='chart1'){
					drawChart1();
				}
				if(id=='chart2'){
					drawChart2();
				}
			}

			$(document).ready(function() { 
				var screen_height = $("#container")[0].offsetHeight;
				$("#chart")[0].style.height = screen_height-20 + "px";
				
				drawChart1();
				
			}); 
			
			var theme = {
				    // 默认色板
				    color: [
				        '#2ec7c9','#b6a2de','#5ab1ef','#ffb980','#d87a80',
				        '#8d98b3','#e5cf0d','#97b552','#95706d','#dc69aa',
				        '#07a2a4','#9a7fd1','#588dd5','#f5994e','#c05050',
				        '#59678c','#c9ab00','#7eb00a','#6f5553','#c14089'
				    ],

				    // 图表标题
				    title: {
				        textStyle: {
				            fontWeight: 'normal',
				            color: '#008acd'          // 主标题文字颜色
				        }
				    },
				    
				    // 值域
				    dataRange: {
				        itemWidth: 15,
				        color: ['#5ab1ef','#e0ffff']
				    },

				    // 工具箱
				    toolbox: {
				        color : ['#1e90ff', '#1e90ff', '#1e90ff', '#1e90ff'],
				        effectiveColor : '#ff4500'
				    },

				    // 提示框
				    tooltip: {
				        backgroundColor: 'rgba(50,50,50,0.5)',     // 提示背景颜色，默认为透明度为0.7的黑色
				        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
				            type : 'line',         // 默认为直线，可选为：'line' | 'shadow'
				            lineStyle : {          // 直线指示器样式设置
				                color: '#008acd'
				            },
				            crossStyle: {
				                color: '#008acd'
				            },
				            shadowStyle : {                     // 阴影指示器样式设置
				                color: 'rgba(200,200,200,0.2)'
				            }
				        }
				    },

				    // 区域缩放控制器
				    dataZoom: {
				        dataBackgroundColor: '#efefff',            // 数据背景颜色
				        fillerColor: 'rgba(182,162,222,0.2)',   // 填充颜色
				        handleColor: '#008acd'    // 手柄颜色
				    },

				    // 网格
				    grid: {
				        borderColor: '#eee'
				    },

				    // 类目轴
				    categoryAxis: {
				        axisLine: {            // 坐标轴线
				            lineStyle: {       // 属性lineStyle控制线条样式
				                color: '#008acd'
				            }
				        },
				        splitLine: {           // 分隔线
				            lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
				                color: ['#eee']
				            }
				        }
				    },

				    // 数值型坐标轴默认参数
				    valueAxis: {
				        axisLine: {            // 坐标轴线
				            lineStyle: {       // 属性lineStyle控制线条样式
				                color: '#008acd'
				            }
				        },
				        splitArea : {
				            show : true,
				            areaStyle : {
				                color: ['rgba(250,250,250,0.1)','rgba(200,200,200,0.1)']
				            }
				        },
				        splitLine: {           // 分隔线
				            lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
				                color: ['#eee']
				            }
				        }
				    },

				    polar : {
				        axisLine: {            // 坐标轴线
				            lineStyle: {       // 属性lineStyle控制线条样式
				                color: '#ddd'
				            }
				        },
				        splitArea : {
				            show : true,
				            areaStyle : {
				                color: ['rgba(250,250,250,0.2)','rgba(200,200,200,0.2)']
				            }
				        },
				        splitLine : {
				            lineStyle : {
				                color : '#ddd'
				            }
				        }
				    },

				    timeline : {
				        lineStyle : {
				            color : '#008acd'
				        },
				        controlStyle : {
				            normal : { color : '#008acd'},
				            emphasis : { color : '#008acd'}
				        },
				        symbol : 'emptyCircle',
				        symbolSize : 3
				    },

				    // 柱形图默认参数
				    bar: {
				        itemStyle: {
				            normal: {
				                barBorderRadius: 5
				            },
				            emphasis: {
				                barBorderRadius: 5
				            }
				        }
				    },

				    // 折线图默认参数
				    line: {
				        smooth : true,
				        symbol: 'emptyCircle',  // 拐点图形类型
				        symbolSize: 3           // 拐点图形大小
				    },
				    
				    // K线图默认参数
				    k: {
				        itemStyle: {
				            normal: {
				                color: '#d87a80',       // 阳线填充颜色
				                color0: '#2ec7c9',      // 阴线填充颜色
				                lineStyle: {
				                    color: '#d87a80',   // 阳线边框颜色
				                    color0: '#2ec7c9'   // 阴线边框颜色
				                }
				            }
				        }
				    },
				    
				    // 散点图默认参数
				    scatter: {
				        symbol: 'circle',    // 图形类型
				        symbolSize: 4        // 图形大小，半宽（半径）参数，当图形为方向或菱形则总宽度为symbolSize * 2
				    },

				    // 雷达图默认参数
				    radar : {
				        symbol: 'emptyCircle',    // 图形类型
				        symbolSize:3
				        //symbol: null,         // 拐点图形类型
				        //symbolRotate : null,  // 图形旋转控制
				    },

				    map: {
				        itemStyle: {
				            normal: {
				                areaStyle: {
				                    color: '#ddd'
				                },
				                label: {
				                    textStyle: {
				                        color: '#d87a80'
				                    }
				                }
				            },
				            emphasis: {                 // 也是选中样式
				                areaStyle: {
				                    color: '#fe994e'
				                }
				            }
				        }
				    },
				    
				    force : {
				        itemStyle: {
				            normal: {
				                linkStyle : {
				                    color : '#1e90ff'
				                }
				            }
				        }
				    },

				    chord : {
				        itemStyle : {
				            normal : {
				                borderWidth: 1,
				                borderColor: 'rgba(128, 128, 128, 0.5)',
				                chordStyle : {
				                    lineStyle : {
				                        color : 'rgba(128, 128, 128, 0.5)'
				                    }
				                }
				            },
				            emphasis : {
				                borderWidth: 1,
				                borderColor: 'rgba(128, 128, 128, 0.5)',
				                chordStyle : {
				                    lineStyle : {
				                        color : 'rgba(128, 128, 128, 0.5)'
				                    }
				                }
				            }
				        }
				    },

				    gauge : {
				        axisLine: {            // 坐标轴线
				            lineStyle: {       // 属性lineStyle控制线条样式
				                color: [[0.2, '#2ec7c9'],[0.8, '#5ab1ef'],[1, '#d87a80']], 
				                width: 10
				            }
				        },
				        axisTick: {            // 坐标轴小标记
				            splitNumber: 10,   // 每份split细分多少段
				            length :15,        // 属性length控制线长
				            lineStyle: {       // 属性lineStyle控制线条样式
				                color: 'auto'
				            }
				        },
				        splitLine: {           // 分隔线
				            length :22,         // 属性length控制线长
				            lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
				                color: 'auto'
				            }
				        },
				        pointer : {
				            width : 5
				        }
				    },
				    
				    textStyle: {
				        fontFamily: '微软雅黑, Arial, Verdana, sans-serif'
				    }
				};
			
			
			function drawChart1(){
				
		var option = {
				
				 title : {
				        subtext: 'Delivery  Lead Time in 2016'
				    },
				tooltip : {
					trigger : 'axis',
					axisPointer : { // 坐标轴指示器，坐标轴触发有效
						type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
					}
				},
				legend : {
					data : [ 'T+1', 'T+2', 'T+3', 'T+4', 'T+5' ]
				},

				calculable : true,
				yAxis : [ {
					type : 'value',
					axisLabel : {
						show : true,
						interval : 'auto',
						formatter : '{value} %'
					}
				} ],
				xAxis : [ {
					type : 'category',
					data : [ '201508', '201509', '201510','201511', '201512', '201601', '201602', '201603','201604', '201605', '201606', '201607', '平均' ]
				} ],
				series : [
						{
							name : 'T+1',
							type : 'bar',
							stack : '总量',
							itemStyle : {
								normal : {
									label : {
										show : true,
										position : 'insideRight',
										formatter : '{c}%'
									}
								}
							},
							data : [ 32, 22, 20, 18, 20, 18, 23, 14, 37, 37,
									40, 38, 27 ]
						},
						{
							name : 'T+2',
							type : 'bar',
							stack : '总量',
							itemStyle : {
								normal : {
									label : {
										show : true,
										position : 'insideRight',
										formatter : '{c}%'
									}
								}
							},
							data : [ 43, 53, 54, 52, 53, 50, 52, 54, 39, 40,
									39, 38, 47 ]
						},
						{
							name : 'T+3',
							type : 'bar',
							stack : '总量',
							itemStyle : {
								normal : {
									label : {
										show : true,
										position : 'insideRight',
										formatter : '{c}%'
									}
								}
							},
							data : [ 17, 17, 18, 20, 18, 23, 18, 20, 14, 15,
									14, 15, 17 ]
						}, {
							name : 'T+4',
							type : 'bar',
							stack : '总量',
							itemStyle : {
								normal : {
									label : {
										show : true,
										position : 'insideRight',
										formatter : '{c}%'
									}
								}
							},
							data : [ 5, 4, 5, 6, 5, 4, 4, 6, 5, 4, 4, 4, 5 ]
						}, {
							name : 'T+5',
							type : 'bar',
							stack : '总量',
							itemStyle : {
								normal : {
									label : {
										show : true,
										position : 'insideRight',
										formatter : '{c}%'
									}
								}
							},
							data : [ 3, 4, 3, 4, 4, 5, 3, 6, 5, 4, 3, 5, 4 ]
						} ]
			};

			var chart = echarts.init(document.getElementById('chart'), theme);
			chart.setOption(option);
		}

		function drawChart2() {
			var option = {
					title : {
						subtext : 'On Time Delivery Rate'
					},
					tooltip : {
						trigger : 'axis'
					},
					legend : {
						data : [ 'CNP', 'ZJS', 'Menlo', 'SF', 'flyon' ]
					},
				    calculable : true,
				    xAxis : [
				        {
				            type : 'category',
				            boundaryGap : false,
				            data : [  '201507', '201508', '201509', '201510',
										'201511', '201512','201601', '201602', '201603', '201604', '201605',
										'201606', '平均' ]
				        }
				    ],
				    yAxis : [
				        {
				            type : 'value',
				            axisLabel : {
				                formatter: '{value} %'
				            }
				        }
				    ],
					series : [
								{
									name : 'CNP',
									type : 'line',
									itemStyle : {
										normal : {
											label : {
												show : true,
												//position : 'insideRight',
												formatter : '{c}%'
											}
										}
									},
									data : [ 92, 93, 85, 90, 90, 77, 85, 94, 93, 87,
											90, 91, 77 ]
								},
								{
									name : 'ZJS',
									type : 'line',
									//stack : '总量',
									itemStyle : {
										normal : {
											label : {
												show : true,
												//position : 'insideRight',
												formatter : '{c}%'
											}
										}
									},
									data : [ 95, 92, 87, 90, 40, 87, 85, 65, 93, 87,
											90, 70, 87 ]
								},
								{
									name : 'Menlo',
									type : 'line',
									//stack : '总量',
									itemStyle : {
										normal : {
											label : {
												show : true,
												//position : 'insideRight',
												formatter : '{c}%'
											}
										}
									},
									data : [ 95, 94, 97, 90, 90, 57, 85, 75, 63, 87,
											90, 90, 87 ]
								},
								{
									name : 'SF',
									type : 'line',
									//stack : '总量',
									itemStyle : {
										normal : {
											label : {
												show : true,
												//position : 'insideRight',
												formatter : '{c}%'
											}
										}
									},
									data : [ 95, 83, 87, 90, 90, 87, 85, 85, 93, 87,
											90, 90, 87 ]
								},
								{
									name : 'flyon',
									type : 'line',
									//stack : '总量',
									itemStyle : {
										normal : {
											label : {
												show : true,
												//position : 'insideRight',
												formatter : '{c}%'
											}
										}
									},
									data : [ 95, 93, 88, 98, 80, 77, 85, 85, 93, 87,
											80, 80, 87 ]
								} ]
				};

			var chart = echarts.init(document.getElementById('chart'), theme);
			chart.setOption(option);
		}
	</script>
	</body>
</html>