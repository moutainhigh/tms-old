<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<style type="text/css">
  h2,p{    
    font-size:100%;    
    font-weight:normal;    
  }    
  ul,li{    
    list-style:none;    
  }    
  ul{    
    overflow:hidden;    
    padding:3em;    
  }    
  ul li a{    
    text-decoration:none;    
    color:#000  ;    
    background: #ffc ;    
    display:block;    
    height:17em;    
    width:13em;    
    padding:1em; 
  }    
  ul li{    
    margin:5px;    
    float:left;    
  }

</style>
<html>
	<head>
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js?v=${version}"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/echarts.min.js?v=${version}" />"></script>
	</head>
	<body>
	
	
	<script type="text/javascript">
	
	function loadGrapData(datalist){
		var str = "";
		for(var i=0;i<datalist.length;i++){
			var code = datalist[i].code + "code";
			var text = datalist[i].code + "text";
			str += "<li><a href='#'> <div id="+ code +" style='width: 200px;height:200px' ></div><div id="+ text +" style='width: 200px;height:100px;font-size: 13px; color: black'></div></a></li> ";
		}
		document.getElementById("vessel").innerHTML = str;
		
		for(var i=0;i<datalist.length;i++){
			var code = datalist[i].code + "code";
			var text = datalist[i].code + "text";
			var name = datalist[i].name;
			var count = datalist[i].count;
			var exp_count = datalist[i].exp_count;
			var percent = datalist[i].percent;
			var score = datalist[i].score;
			var total_score = datalist[i].total_score;
			var percent_num = percent.substring(0,percent.length-1)*1/100.0;
			document.getElementById(text).innerHTML = name+": "+percent+"<br>总件数:"+count+"<br>异常件数:"+ exp_count +"<br>分数:"+score+"<br>总数:"+total_score;
			
			var chart = echarts.init(document.getElementById(code));
			 // 指定图表的配置项和数据
        	var option = {
         	    tooltip: {
         	        trigger: 'item',
         	        formatter: "{a} <br/>{b}: {c} ({d}%)"
         	    },
         	    legend: {
         	        orient: 'vertical',
         	        x: 'left',
         	        data:[name]
         	    },
         	    series: [
         	        {
         	            name:name,
         	            type:'pie',
         	            radius: ['50%', '70%'],
         	            avoidLabelOverlap: false,
         	            label: {
         	                normal: {
         	                    show: false,
         	                    position: 'center'
         	                },
         	                emphasis: {
         	                    show: true,
         	                    textStyle: {
         	                        fontSize: '30',
         	                        fontWeight: 'bold'
         	                    }
         	                }
         	            },
         	            labelLine: {
         	                normal: {
         	                    show: false
         	                }
         	            },
         	            data:[
         	                {value:percent_num, name:name},
         	                {value:(1-percent_num), name:name},
         	            ]
         	        }
         	    ]
         	};
        	chart.setOption(option);
		}
	}
	
	window.onload=function(){//用window的onload事件，窗体加载完毕的时候
		$.ajax({
			type:"GET",
       		url:'<%=request.getContextPath()%>/report/custGD/unitData.json',
       		data:'',
       		success:function(data){
       		var datalist = eval(data);
       		loadGrapData(datalist);
        }
		});
		 $.ajax({
		        type:"get",
		        dataType:"json",
		        contentType:"application/json;charset=utf-8",
		        url:"<%=request.getContextPath()%>/report/custGD/getCustomer.json",
		        success:function(result){
		        	var data = eval(result);
		        	for(var i=0;i<data.length;i++){
		        		 $("#cust").append("<option value='"+data[i].name+"'>"+data[i].name+"</option>");
		        		}
		       		 },
		        error : function(XMLHttpRequest, textStatus, errorThrown) {
		            alert(errorThrown);
		   		 	 }, 
		        async:false             //false表示同步
		        }

		       );
		  $.ajax({
		        type:"get",
		        dataType:"json",
		        contentType:"application/json;charset=utf-8",
		        url:"<%=request.getContextPath()%>/report/custGD/getCorp.json",
		        success:function(result){
		        	var data = eval(result);
		        	for(var i=0;i<data.length;i++){
		        		 $("#corp").append("<option value='"+data[i].name+"'>"+data[i].name+"</option>");
		        		}
		       		 },
		        error : function(XMLHttpRequest, textStatus, errorThrown) {
		            alert(errorThrown);
		   		 	 }, 
		        async:false             //false表示同步
		        }
		       ); 
	}
	
	function reloadData(){
		var year=document.getElementById("year").value;
		var month=document.getElementById("month").value;
		var cust=document.getElementById("cust").value;
		var corp=document.getElementById("corp").value;
		 $.ajax({
		        type:"get",
		        dataType:"json",
		        contentType:"application/json;charset=utf-8",
		        data:{"year":year,"month":month,"cust":cust,"corp":corp},
		        url:'<%=request.getContextPath()%>/report/custGD/getData.json',
		        success:function(data){
		       		var datalist = eval(data);
		       		loadGrapData(datalist);
		        },
			});
		  return false;
	}
	
	 function YYYYMMDDstart(){   
         MonHead = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];   
         //先给年下拉框赋内容   
         var y = new Date().getFullYear();   
         for (var i = (y-10);i < (y+10); i++){    //以今年为准，前30年，后30年   
                 document.reg_testdate.YYYY.options.add(new Option(" "+ i +"年", i)); 
         }
         //赋月份的下拉框   
         for (var i = 1; i < 13; i++){  
                 document.reg_testdate.MM.options.add(new Option(" "+ i + "月", i));
         }
         document.reg_testdate.YYYY.value = y;   
         document.reg_testdate.MM.value = new Date().getMonth();   
         var n = MonHead[new Date().getMonth()];   
         if (new Date().getMonth() ==1 && IsPinYear(YYYYvalue)) n++;   
              writeDay(n); //赋日期下拉框Author:meizz   
         document.reg_testdate.DD.value = new Date().getDate();     
 	}   
 	if(document.attachEvent)   
     	 window.attachEvent("onload",    YYYYMMDDstart);   
 	else   
    	 window.addEventListener('load',    YYYYMMDDstart,    false);  
	</script>
	
 	<div style="background-color:#ffc;width:100%;height:50px;" align="center">
 	<form name="reg_testdate" >
  		<select id="year" name="YYYY" onchange="YYYYDD(this.value)">
   			 <option value="" >请选择 年份</option>
  		</select>
  		<select id="month" name="MM" onchange="MMDD(this.value)">
   			 <option value="">选择 月份</option>
  		</select>
  		<select name="corp" id="corp" >
   			 <option value="">---选择 公司---</option>
  		</select>
  		<select name="cust" id="cust" >
   			 <option value="">-------------选择 客户-------------</option>
  		</select>
  		<button onclick="return reloadData()">查看</button>
	</form>
	</div> 
	<!-- 产生图表 -->
	<ul id="vessel"> 
   </ul>
	</body>
</html>
	
