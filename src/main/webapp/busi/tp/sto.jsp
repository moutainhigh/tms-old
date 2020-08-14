<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<html>
<head>
<%@ include file="/common/header.jsp"%>
<%if(debug){ %>
<script type="text/javascript" src='<c:url value="/busi/tp/SegmentWindow.js?v=${version}" />'></script>
<script type="text/javascript" src='<c:url value="/busi/tp/QuantityWindow.js?v=${version}" />'></script>
<script type="text/javascript" src='<c:url value="/busi/tp/BatchPZ.js?v=${version}" />'></script>
<script type="text/javascript" src='<c:url value="/busi/tp/Delegate.js?v=${version}" />'></script>
<script type="text/javascript" src='<c:url value="/busi/tp/CurrentTracking.js?v=${version}" />'></script>
<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js?v=${version}"/>"></script>
<script type="text/javascript" src='<c:url value="/busi/tp/DelegateCarrier.js?v=${version}" />'></script>
<%}else{ %>
<script type="text/javascript" src="<c:url value="/busi/tp/sto-min.js?v=${version}" />"></script>
<%} %>
<script type="text/javascript" src='http://api.map.baidu.com/api?v=2.0&ak=EMfamWxMfh0n812MGeVBImXn'></script>
<script type="text/javascript" src='http://api.map.baidu.com/library/LuShu/1.2/src/LuShu.js'></script>
<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			var no_delivery = record.data.no_delivery;
			if(value == 10 && no_delivery && no_delivery == 'Y'){
				meta.css = 'cssYellow';
			}else{
				meta.css = 'css'+value;
			}
			
		};		
		function req_deli_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 11){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function req_arri_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 12){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function invoice_vbillnoRenderer(value,meta,record){
			var url = ctxPath+"/inv/inv/index.html?funCode=t201&nodeKey=view&_waterfallScene=true&_vbillno="+value;
			return "<a href=\"javascript:uft.Utils.openNode('"+value+"','发货单','"+url+"')\">"+value+"</a>";
		}
		window.qConfig = {cols : 4};//锁定查询条件的panel，每行放4个查询项
		</script>
		<style type="text/css">
		.css10 {
			background-color: #92D050;
		}
		
		.css11 {
			background-color: #FAC090;
		}
		
		.css12 {
			background-color: #FFFF00;
		}
		
		.css13 {
			background-color: #538ED5;
		}
		.css14 {
			background-color: #696969;
		}
		.cssYellow {
			background-color: #FF4500;
		}
		
		
		.cssRed {
			background-color: #FF0000;
		}
		
		.btnSeg {
			background-image: url(../../busi/tp/images/fd.png) !important;
		}
		
		.btnCanSeg {
			background-image: url(../../busi/tp/images/cxfd.png) !important;
		}
		
		.btnWei {
			background-image: url(../../busi/tp/images/fl.png) !important;
		}
		
		.btnCanWei {
			background-image: url(../../busi/tp/images/cxfl.png) !important;
		}
		
		.btnSto {
			background-image: url(../../busi/tp/images/pz.gif) !important;
		}
		
		#total li {
			font-size: 12px;
			margin: 0px 0px 0px 5px;
			line-height: 25px;
		}
		
		#total li span {
			color: red;
			font-size: 8px;
		}
		
		#carrierInfoList{
			font-size: 12px;
		}
		
		#carInfoList{
			font-size: 12px;
		}
		.row-left0 {
			width: 190px;
			float: left;
		}
		.row-left {
			width: 75px;
			float: left;
		}
		.row-left1 {
			width: 105px;
			float: left;
		}
		.row-left2 {
		 	height:1px;
			width: 190px;
			float: left;
			background:#FF4500;
			overflow:hidden;
		}
		.row-left3 {
		 	width: 48px;
			float: left;
		}
		
		</style>
</head>
<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true"
		isBuildHeaderCard="false" headerGridPageSizePlugin="true"
		headerGridSingleSelect="false" headerGridCheckboxSelectionModel="true" />
</body>
<script type="text/javascript">
		//合计
		var totalPanel = new Ext.Panel({
			region : 'north',
			height : 110,
			title : '订单合计',
			border : true,
			split : true,
			html : '<ul id="total">'+
						'<li><div class="row-left0">件数：<span id="numCount">0</span></div></li>'+
						'<li><div class="row-left">重量：<span id="weightCount">0</span></div>'+
							'<div class="row-left1">满载率：<span id="weightRate">0%</span></div>'+
							'<div class="row-left">体积：<span id="volumeCount">0</span></div>'+
							'<div class="row-left1">满载率：<span id="volumeRate">0%</span></div>'+
						'</li>'+
				   '</ul>'
		});
		var _weight_count;
		var _volume_count;
		
		var carrMemory = [];//全局变量 记录那些承运商是系统已经查出来的
		var carMemory = [];//全局变量 记录那些车辆是系统已经查出来的
		var seg_billnos = []; //当前选中的运段号
		
		window.onload = function() {
			doProc();
		}
		
		 
		function createCar(cars){
			var checked_car = $("input[name='car_check']:checked").val() || '';
			var checked_carrier = $("input[name='carr_check']:checked").val();
			carMemory = cars;
			var carStr = '';
			//找出当前选中的那辆车，放在列表最上面显示
			if(cars && cars.length > 0){
				carStr = '<table width="270" style="TABLE-LAYOUT:fixed;WORD-BREAK:break-all">';
				for(var i = 0; i < cars.length; i++){
					var car = cars[i];
					var pk_car = car.pk_car;
					if(checked_carrier){
						if(pk_car == checked_car && car.pk_carrier == checked_carrier){//选中的
							var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' checked name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
			    							  +'<td width="30%">'+car.carno+'</td>'
			    							  +'<td width="30%">'+car.carType+'</td>'
			    							  +'<td width="32%">'+car.carInfo+'</td></tr>';
			   	 			carStr += htmlUnit;
			   	 			break;
						}
					}else{
						if(pk_car == checked_car){//选中的
							var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' checked name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
			    							  +'<td width="30%">'+car.carno+'</td>'
			    							  +'<td width="30%">'+car.carType+'</td>'
			    							  +'<td width="32%">'+car.carInfo+'</td></tr>';
			   	 			carStr += htmlUnit;
			   	 			break;
						}
					}
					
				}
				for (var i = 0; i < cars.length; i++){
					var car = cars[i];
					var pk_car = car.pk_car;
					if(checked_carrier){
						if(pk_car != checked_car && car.pk_carrier == checked_carrier){//未选中的
							var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
			    							  +'<td width="30%">'+car.carno+'</td>'
			    							  +'<td width="30%">'+car.carType+'</td>'
			    							  +'<td width="32%">'+car.carInfo+'</td></tr>';
			   	 			carStr += htmlUnit;
						}
					}else{
						if(pk_car != checked_car){//未选中的
							var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
			    							  +'<td width="30%">'+car.carno+'</td>'
			    							  +'<td width="30%">'+car.carType+'</td>'
			    							  +'<td width="32%">'+car.carInfo+'</td></tr>';
			   	 			carStr += htmlUnit;
						}
					}
					
				}
				carStr += '</table>';
			}
			document.getElementById('carInfoList').innerHTML = carStr;
		}
		
		function getCarByCarrier(Obj){
			var select_carr = Obj.value;
			var carrs = document.getElementsByName("carr_check");
			for(var i=0;i<carrs.length;i++){
				if(carrs[i].value == select_carr){//当前承运商
					if(carrs[i].checked == true){
						carrs[i].checked = true;
					}else{
						carrs[i].checked = false;
					}
				}else{
					carrs[i].checked = false;
				}
            } 
			var checked_car = $("input[name='car_check']:checked").val();
			var checked_carrier = $("input[name='carr_check']:checked").val();
			var carStr = "";
			if(carMemory.length > 0){
				carStr = '<table width="270" style="TABLE-LAYOUT:fixed;WORD-BREAK:break-all">';
				//如果当前没有被勾选的承运商，那么显示所有车辆
				if(!checked_carrier){
					for(var i=0;i<carMemory.length;i++){
						var car = carMemory[i];
						var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+car.pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
						    			+'<td width="30%">'+car.carno+'</td>'
						    			+'<td width="30%">'+car.carType+'</td>'
						    			+'<td width="32%">'+car.carInfo+'</td></tr>';
							carStr += htmlUnit;
					}
				}else{
					for(var i=0;i<carMemory.length;i++){
						if(carMemory[i].pk_carrier == checked_carrier){
							var car = carMemory[i];
							var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+car.pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
							    			+'<td width="30%">'+car.carno+'</td>'
							    			+'<td width="30%">'+car.carType+'</td>'
							    			+'<td width="32%">'+car.carInfo+'</td></tr>';
								carStr += htmlUnit;
						}
						
					}
				}
				carStr += '</table>';
			}
			document.getElementById('carInfoList').innerHTML = carStr;
		}
		
		function getCarInMemory(key){
			var checked_carrier = $("input[name='carr_check']:checked").val();
			var checked_car = $("input[name='car_check']:checked").val();
			if(carMemory && carMemory.length > 0){
				htmlStr = '<table width="270" style="TABLE-LAYOUT:fixed;WORD-BREAK:break-all">';
				for(var i=0; i<carMemory.length;i++){
					var car = carMemory[i];
    				var carno = car.carno;
    				var pk_car = car.pk_car;
    				//key为空则显示全部，否则显示key对应的车辆
    				if(key != ''){
    					if(carno.indexOf(key) == -1){
    						continue;//输入了关键字，但是这个车辆不在搜索范围内，跳过
    					}
    				}
    				if(checked_carrier){
    					if(checked_car == pk_car && car.pk_carrier == checked_carrier){
        					var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' checked name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
    					    				  +'<td width="30%">'+car.carno+'</td>'
    					    				  +'<td width="30%">'+car.carType+'</td>'
    					   					  +'<td width="30%">'+car.carInfo+'</td></tr>';
    						htmlStr += htmlUnit;
    						break;
        				}
    				}else{
    					if(checked_car == pk_car){
        					var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' checked name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
    					    				  +'<td width="30%">'+car.carno+'</td>'
    					    				  +'<td width="30%">'+car.carType+'</td>'
    					   					  +'<td width="30%">'+car.carInfo+'</td></tr>';
    						htmlStr += htmlUnit;
    						break;
        				}
    				}
    				
				}
				for(var i=0; i<carMemory.length;i++){
					var car = carMemory[i];
    				var carno = car.carno;
    				var pk_car = car.pk_car;
    				//key为空则显示全部，否则显示key对应的车辆
    				if(key != ''){
    					if(carno.indexOf(key) == -1){
    						continue;//输入了关键字，但是这个车辆不在搜索范围内，跳过
    					}
    				}
    				if(checked_carrier){
    					if(checked_car != pk_car  && car.pk_carrier == checked_carrier){
        					var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
    					    				  +'<td width="30%">'+car.carno+'</td>'
    					    				  +'<td width="30%">'+car.carType+'</td>'
    					   					  +'<td width="30%">'+car.carInfo+'</td></tr>';
    						htmlStr += htmlUnit;
        				}
    				}else{
    					if(checked_car != pk_car){
        					var htmlUnit = '<tr><td width="8%" align="center"><input value='+"'"+pk_car+"'"+' name="car_check" type="checkbox" onClick="getFullWithCar(this);"/></td>'
    					    				  +'<td width="30%">'+car.carno+'</td>'
    					    				  +'<td width="30%">'+car.carType+'</td>'
    					   					  +'<td width="30%">'+car.carInfo+'</td></tr>';
    						htmlStr += htmlUnit;
        				}
    				}
    				
    				
				}
				htmlStr += '</table>';
			}
			document.getElementById('carInfoList').innerHTML = htmlStr;
		}
		
		function createCarrier(carriers){
			var checked_carrier = $("input[name='carr_check']:checked").val() || '';
			carrMemory = carriers;//存入全局变量
			var carrStr = '';
			if(carriers && carriers.length > 0){
				carrStr = '<table width="240" style="TABLE-LAYOUT:fixed;WORD-BREAK:break-all">';
				for (var i = 0; i < carriers.length; i++){
					var carrInfo = carriers[i];
					var pk_carrier = carrInfo.pk_carrier;
					if(pk_carrier == checked_carrier){
						var htmlUnit = '<tr><td width="10%" align="center"><input value='+"'"+pk_carrier+"'"+' name="carr_check" type="checkbox" onClick="getCarByCarrier(this);"/></td>'
										  +'<td width="40%" color = "black">'+carrInfo.carr_name+'</td>'
										  +'<td center width="20%"><div style="margin-left:7px;">'+carrInfo.car_avallable_account+'</div></td>'
										  +'<td width="15%">'+carrInfo.ent_count+'</td>'
										  +'<td width="10%">'+carrInfo.accid_count+'</td>'
										  +'<td width="10%">'+carrInfo.price+'</td>'
										  +'<td width="5">'+carrInfo.kpi+'</td></tr>';
							carrStr += htmlUnit;	
		  				 break;
					}
				}
				for (var i = 0; i < carriers.length; i++){
					var carrInfo = carriers[i];
					var pk_carrier = carrInfo.pk_carrier;
					if(pk_carrier != checked_carrier){
						var htmlUnit = '<tr><td width="10%" align="center"><input value='+"'"+pk_carrier+"'"+' name="carr_check" type="checkbox" onClick="getCarByCarrier(this);"/></td>'
										  +'<td width="40%" color = "black">'+carrInfo.carr_name+'</td>'
										  +'<td center width="20%"><div style="margin-left:7px;">'+carrInfo.car_avallable_account+'</div></td>'
										  +'<td width="15%">'+carrInfo.ent_count+'</td>'
										  +'<td width="10%">'+carrInfo.accid_count+'</td>'
										  +'<td width="10%">'+carrInfo.price+'</td>'
										  +'<td width="5">'+carrInfo.kpi+'</td></tr>';
						carrStr += htmlUnit;	
					}
					
				}
				carrStr += '</table>';
			}
			document.getElementById('carrierInfoList').innerHTML = carrStr;
		}
		
		function getCarrierInMemory(key){
			if(carrMemory && carrMemory.length > 0){
				var checked_carrier = $("input[name='carr_check']:checked").val() || '';
				htmlStr = '<table width="240" style="TABLE-LAYOUT:fixed;WORD-BREAK:break-all">';
				for(var i=0; i<carrMemory.length;i++){
					var carrInfo = carrMemory[i];
    				var carr_name = carrInfo.carr_name;
    				var pk_carrier = carrInfo.pk_carrier;
    				//key为空则显示全部，否则显示key对应的承运商
    				if(key != ''){
    					if(carr_name.indexOf(key) == -1){
    						continue;//输入了关键字，但是这个承运商不在搜索范围内，跳过
    					}
    				}
    				if(pk_carrier == checked_carrier){
    					var htmlUnit = '<tr><td width="10%" align="center"><input value='+"'"+pk_carrier+"'"+' checked name="carr_check" type="checkbox" onClick="getCarByCarrier(this);"/></td>'
				    					  +'<td width="40%" color = "black">'+carrInfo.carr_name+'</td>'
				    					  +'<td center width="20%"><div style="margin-left:7px;">'+carrInfo.car_avallable_account+'</div></td>'
										  +'<td width="15%">'+carrInfo.ent_count+'</td>'
										  +'<td width="10%">'+carrInfo.accid_count+'</td>'
										  +'<td width="10%">'+carrInfo.price+'</td>'
										  +'<td width="5">'+carrInfo.kpi+'</td></tr>';
						htmlStr += htmlUnit;
						break;
    				}
    					
				}
				for(var i=0; i<carrMemory.length;i++){
					var carrInfo = carrMemory[i];
    				var carr_name = carrInfo.carr_name;
    				var pk_carrier = carrInfo.pk_carrier;
    				//key为空则显示全部，否则显示key对应的承运商
    				if(key != ''){
    					if(carr_name.indexOf(key) == -1){
    						continue;//输入了关键字，但是这个承运商不在搜索范围内，跳过
    					}
    				}
    				if(pk_carrier != checked_carrier){
    					var htmlUnit = '<tr><td width="10%" align="center"><input value='+"'"+pk_carrier+"'"+' name="carr_check" type="checkbox" onClick="getCarByCarrier(this);"/></td>'
				    					  +'<td width="40%" color = "black">'+carrInfo.carr_name+'</td>'
				    					  +'<td center width="20%"><div style="margin-left:7px;">'+carrInfo.car_avallable_account+'</div></td>'
										  +'<td width="15%">'+carrInfo.ent_count+'</td>'
										  +'<td width="10%">'+carrInfo.accid_count+'</td>'
										  +'<td width="10%">'+carrInfo.price+'</td>'
										  +'<td width="5">'+carrInfo.kpi+'</td></tr>';
						htmlStr += htmlUnit;
    				}
    					
				}
				htmlStr += '</table>';
				document.getElementById('carrierInfoList').innerHTML = htmlStr;
			}
		}
		
		//当点击车辆的时候，只会有满载率产生变化，所以只需要抓取满载率数据就可以了。
		function getFullWithCar(Obj){
			var select_car = Obj.value;
			var cars = document.getElementsByName("car_check");
			for(var i=0;i<cars.length;i++){
				if(cars[i].value == select_car){//当前车辆
					if(cars[i].checked == true){
						cars[i].checked = true;
					}else{
						cars[i].checked = false;
					}
				}else{
					cars[i].checked = false;
				}
            } 
			var pk_car = $("input[name='car_check']:checked").val() || '';
			$.ajax({
				type : "GET",
				url : 'loadProcDatas.json',  
				data : { vbillos : seg_billnos, pk_car : pk_car },
				dataType: 'JSON',
				success : function(data) {
					var full_load = data.full_load;
					if(full_load && full_load.weight_rate && full_load.volume_rate){//计算满载率
						document.getElementById('weightRate').innerHTML = full_load.weight_rate;
						document.getElementById('volumeRate').innerHTML = full_load.volume_rate;
					}else{
						document.getElementById('weightRate').innerHTML = "0.00%";
						document.getElementById('volumeRate').innerHTML = "0.00%";
					}
				}
			}); 
		}
		
		function doProc(){
			var checked_car = $("input[name='car_check']:checked").val() || '';
			$.ajax({
				type : "GET",
				url : 'loadProcDatas.json',  
				data : { vbillos : seg_billnos, pk_car : checked_car },
				dataType: 'JSON',
				success : function(data) {
					var merge_msg = data.merge_msg;
					var carriers = data.carriers;
					var cars = data.cars;
					var full_load = data.full_load;
					
					if(merge_msg && merge_msg.length > 0){
						uft.Utils.showErrorMsg(merge_msg);
					}
					createCarrier(carriers);//创建承运商
					createCar(cars);//创建车辆
					
					if(full_load && full_load.weight_rate && full_load.volume_rate){//计算满载率
						document.getElementById('weightRate').innerHTML = full_load.weight_rate;
						document.getElementById('volumeRate').innerHTML = full_load.volume_rate;
					}else{
						document.getElementById('weightRate').innerHTML = "0.00%";
						document.getElementById('volumeRate').innerHTML = "0.00%";
					}
					
				}
			}); 
		}
		

		var carrPanel =  new Ext.Panel({
			region : 'center',
			id : 'carrPanel',
			title : '承运商',
			tbar: [{
				xtype:'textfield',
				id:'carrKeyWord',
				width:'200px',
				emptyText:"关键字查询",//默认是null
				enableKeyEvents : true,
				scope:this,
			    listeners: {'keyup': {
			    	fn: function(o, evt){
			    		//从缓存中读取承运商信息，当关键字为空时，显示全部缓存信息
			    		var key = Ext.getCmp("carrKeyWord").getValue();
			    		getCarrierInMemory(key);
			    	},scope: this }
					}
				}],
			border : true,
			split : true,
			autoScroll:true,
			html : '<div id = "carrierInfoList"></div>'
		}); 
		
		//路线树
		var lineTree = new uft.extend.tree.CheckboxTree({
			id : 'lineTree',
			border : true,
			split : true,
			dataUrl : 'getLineTree.json'
		});
		
		var carPanel =  new Ext.Panel({
			region : 'center',
			id : 'carPanel',
			tbar: [{
				xtype:'textfield',
				id:'carKeyWord',
				width:'200px',
				emptyText:"关键字查询",//默认是null
				enableKeyEvents : true,
				scope:this,
			    listeners: {'keyup': {
			    	fn: function(o, evt){
			    		var key = Ext.getCmp("carKeyWord").getValue();
			    		getCarInMemory(key);
			    	},scope: this }
					}
				}],
			border : true,
			split : true,
			autoScroll:true,
			html : '<div id = "carInfoList"></div>'
		});
		
		carAndLineTableTab = new Ext.TabPanel({
			region : 'south',
			height : 200,
			activeTab : 0,
			deferredRender : false,
			items : [{
				title : '车辆',
				layout : 'fit',
				items : [carPanel]
			},{
				title : '线路',
				layout : 'fit',
				items : [lineTree]
			}]
		});
		
		
		//选择树节点事件
		lineTree.on('check', function(node,event){
			var ids = lineTree.getCheckedNodesId();
			var hg = app.headerGrid;
			var store = hg.getStore();
			store.baseParams["lineId"] = ids;
			store.baseParams["PUB_PARAMS"] = app.topQueryForm.getFormParams();
			
			var options={};
			options.params={};
			options.params["lineId"]=ids;
			if(ids && ids.length > 0){
				options.params['fromLineTree'] = true;//后面会用到
			}
			options.params[store.defaultParamNames.start]=0;
			options.params[store.defaultParamNames.limit]=hg.pageSize;
			store.reload(options);
		},this);
		
		var leftPanel = new Ext.Panel({
			region : 'west',
			layout : 'border',
			collapseMode: 'mini',
			split:true,
			width : 205,
			split : true,
			items : [totalPanel,carrPanel,carAndLineTableTab]
		});
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_ref);
				btns.push(new uft.extend.Button({
					text : '拆段',
					iconCls : 'btnSeg',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						//分段页面只需比较最大提货时间和最小的到货时间即可
						var ids=[],max_req_deli_date,min_req_arri_date;
						for(var i=0;i<records.length;i++){
							var record = records[i];
							ids.push(record.get(this.app.getHeaderPkField()));
							var req_deli_date = record.get('req_deli_date');
							var req_arri_date = record.get('req_arri_date');
							if(!(req_deli_date instanceof Date)){
								req_deli_date = Date.parseDate(req_deli_date,uft.jf.Constants.DATETIME_FORMAT);
							}
							if(!(req_arri_date instanceof Date)){
								req_arri_date = Date.parseDate(req_arri_date,uft.jf.Constants.DATETIME_FORMAT);
							}
							if(!max_req_deli_date){
								max_req_deli_date = req_deli_date;
							}
							if(!min_req_arri_date){
								min_req_arri_date = req_arri_date;
							}

							if(req_deli_date > max_req_deli_date){
								max_req_deli_date = req_deli_date;
							}
							if(req_arri_date < min_req_arri_date){
								min_req_arri_date = req_arri_date;
							}
						}
						new uft.tp.SegmentWindow({
							pk_segment:ids,
							max_req_deli_date:max_req_deli_date,
							min_req_arri_date:min_req_arri_date,
							grid:this.app.headerGrid
						}).show();
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销拆段',
					iconCls : 'btnCanSeg',
					scope : this,
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_segment:ids},
					    	url : 'cancelSection.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '拆量',
					iconCls : 'btnWei',
					scope : this,
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						} 
						if(ids && ids.length != 1){
							uft.extend.tip.Tip.msg('warn','有且只能选择一条记录！');
							return;
						}
						new uft.tp.QuantityWindow({pk_segment:ids[0],grid:this.app.headerGrid}).show();
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销拆量',
					iconCls : 'btnCanWei',
					scope : this,
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_segment:ids},
					    	url : 'cancelQuantity.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '排单',
					iconCls : 'btnSto',
					scope : this,
					handler : function(){
						var url = ctxPath + '/tp/pz/index.html?funCode=<%=com.tms.constants.FunConst.SEG_PZ_CODE%>';
						//所选运段
						var ids = [];
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records){
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
						//获取最早的提货时间，和最晚的到货时间，作为表头的提到货时间。
						var req_deli_date_temp = records[0].get('req_deli_date');//要求离开日期
						var req_arri_date_temp = records[0].get('req_arri_date');//要求到达日期
						
						for(var i=0;i<records.length;i++){
							var vbillstatus = records[i].get('vbillstatus');
							if(vbillstatus != uft.jf.bizStatus.SEG_WPLAN){
								//不是待调度的记录
								uft.Utils.showWarnMsg('必须选择状态为[待调度]的单据！');
								return;
							}
							var req_deli_date = records[i].get('req_deli_date');//要求离开日期
							if(req_deli_date < req_deli_date_temp){
								req_deli_date_temp = req_deli_date;
							}
							var req_arri_date = records[i].get('req_arri_date');//要求到达日期
							if(req_arri_date > req_arri_date_temp){
								req_arri_date_temp = req_arri_date;
							}
							ids.push(records[i].get(this.app.getHeaderPkField()));
						}
						url+="&pk_segmentAry="+Ext.encode(ids); //这里加上这个参数是为了弹出窗口读取方便
						for(var i=0;i<ids.length;i++){
							url +="&pk_segment="+ids[i];
						}
						if($("input[name='car_check']:checked").val()){
							url += "&pk_car=" + $("input[name='car_check']:checked").val();
						}
						if($("input[name='carr_check']:checked").val()){
							url += "&pk_carrier=" + $("input[name='carr_check']:checked").val();
						}
						url += "&req_deli_date="+req_deli_date_temp;
						url += "&req_arri_date="+req_arri_date_temp;
						openDialog(url,ids);
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '批量排单',
					iconCls : 'btnSto',
					scope : this,
					handler : function(){
						var url = ctxPath + '/tp/pz/index.html?funCode=<%=com.tms.constants.FunConst.SEG_BATCH_PZ_CODE%>';
						//所选运段
						var ids = [];
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records){
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
						var inv_vbillnos =  uft.Utils.getSelectedRecordIds(this.app.headerGrid,"invoice_vbillno");
						//对于多点运输，我们需要将所有的运段都显示出来
						var extraDatas = Utils.doSyncRequest('getSegments.json',{ids:inv_vbillnos});
						//如果返回空数据，说明没有额外的运段数据需要处理
						if(extraDatas && extraDatas.length > 0){
							for(var i=0;i<extraDatas.length;i++){
								var data = extraDatas[i];
								var tempRecord = {};
								uft.Utils.cloneJsonObject(records[0],tempRecord);
								tempRecord.data = data;
								tempRecord.json = data;
								records.push(tempRecord);
							}
						}
						 
						//获取最早的提货时间，和最晚的到货时间，作为表头的提到货时间。
						var req_deli_date_temp = records[0].get('req_deli_date');//要求离开日期
						var req_arri_date_temp = records[0].get('req_arri_date');//要求到达日期
						for(var i=0;i<records.length;i++){
							var vbillstatus = records[i].get('vbillstatus');
							if(vbillstatus != uft.jf.bizStatus.SEG_WPLAN){
								//不是待调度的记录
								uft.Utils.showWarnMsg('必须选择状态为[待调度]的单据！');
								return;
							}
							var req_deli_date = records[i].get('req_deli_date');//要求离开日期
							if(req_deli_date < req_deli_date_temp){
								req_deli_date_temp = req_deli_date;
							}
							var req_arri_date = records[i].get('req_arri_date');//要求到达日期
							if(req_arri_date > req_arri_date_temp){
								req_arri_date_temp = req_arri_date;
							}
							ids.push(records[i].get(this.app.getHeaderPkField()));
						}
						url+="&pk_segmentAry="+Ext.encode(ids); //这里加上这个参数是为了弹出窗口读取方便
						for(var i=0;i<ids.length;i++){
							url +="&pk_segment="+ids[i];
						}
						if($("input[name='car_check']:checked").val()){
							url += "&pk_car=" + $("input[name='car_check']:checked").val();
						}
						if($("input[name='carr_check']:checked").val()){
							url += "&pk_carrier=" + $("input[name='carr_check']:checked").val();
						}
						url += "&req_deli_date="+req_deli_date_temp;
						url += "&req_arri_date="+req_arri_date_temp;
						var newWindow = openDialog(url,ids);
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '委派',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						new uft.tp.Delegate({records:records,app:this.app}).show();
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '撤销委派',
					scope : this,
					handler : function(){
						var app = this.app;
						var ids = uft.Utils.getSelectedRecordIds(app.headerGrid,app.headerPkField);
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						var records = uft.Utils.getSelectedRecords(app.headerGrid);
						var params = app.newAjaxParams();
						params['billId'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'POST',
					    	url : 'cancelDelegate.json',
					    	params : params,
					    	success : function(values){
					    		if(values.datas && values.datas.length > 0){
					    			this.app.setHeaderValues(records,values.datas);
					    		}
					    		if(values.append){
					    			uft.Utils.showWarnMsg(values.append);
					    		}
					    	}
						});
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(this.btn_export);
				
				btns.push(new uft.extend.Button({
					text : '同步订单',
					scope : this,
					handler : function(){
						var app = this.app;
						var ids = uft.Utils.getSelectedRecordIds(app.headerGrid,app.headerPkField);
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						var records = uft.Utils.getSelectedRecords(app.headerGrid);
						var params = app.newAjaxParams();
						params['billId'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'POST',
					    	url : 'syncOrders.json',
					    	params : params,
					    	success : function(values){
					    		if(values.datas && values.datas.length > 0){
					    			this.app.setHeaderValues(records,values.datas);
					    		}
					    		if(values.append){
					    			uft.Utils.showWarnMsg(values.append);
					    		}
					    	}
						});
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				
				btns.push(new uft.extend.Button({
					text : '合并运段',
					scope : this,
					handler : function(){
						var app = this.app;
						var ids = uft.Utils.getSelectedRecordIds(app.headerGrid,app.headerPkField);
						if(!ids || ids.length < 2){
							uft.extend.tip.Tip.msg('warn','请至少选择两条记录！');
							return;
						}
						var records = uft.Utils.getSelectedRecords(app.headerGrid);
						var params = app.newAjaxParams();
						params['billId'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'POST',
					    	url : 'mergeSegments.json',
					    	params : params,
					    	success : function(values){
					    		if(values.success = true){
					    			this.app.headerGrid.getStore().reload();
					    		}
					    		/* if(values.datas && values.datas.length > 0){
					    			this.app.setHeaderValues(records,values.datas);
					    		}
					    		if(values.append){
					    			uft.Utils.showWarnMsg(values.append);
					    		} */
					    	}
						});
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '委派承运商',
					scope : this,
					handler : function(){
						var app = this.app;
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						var ids = uft.Utils.getSelectedRecordIds(app.headerGrid,app.headerPkField);
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						var checked_carrier = $("input[name='carr_check']:checked").val();
						new uft.tp.DelegateCarrier({records:records,carrier:checked_carrier,ids:ids,grid:this.app.headerGrid,app:this.app}).show();
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(new uft.extend.Button({
					text : '调度',
					scope : this,
					iconCls : 'btnSto',
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择记录！');
							return;
						}
						var checked_carrier = $("input[name='carr_check']:checked").val();
						var checked_car = $("input[name='car_check']:checked").val();
						if(!checked_carrier && !checked_car){
							uft.extend.tip.Tip.msg('warn','请先选择承运商或者车辆！');
							return;
						}
						var inv_vbillnos =  uft.Utils.getSelectedRecordIds(this.app.headerGrid,"invoice_vbillno");
						var params = this.app.newAjaxParams();
						params['checked_car'] = checked_car;
						params['checked_carrier'] = checked_carrier;
						params['inv_vbillnos'] = inv_vbillnos;
						params['ids'] = ids;
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'POST',
					    	url : 'dispatch.json',
					    	params : params,
					    	success : function(values){
					    		if(values.success = true){
					    			this.app.headerGrid.getStore().reload();
					    		}
					    	}
						});
						
					},
					enabledStatus : [uft.jf.pageStatus.OP_NOTEDIT_LIST],
					enabledBizStatus : [uft.jf.bizStatus.SEG_WPLAN]
				}));
				btns.push(this.btn_import);
				return btns;
			}
		});
		${moduleName}.appUiConfig.leftPanel = leftPanel;
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		var store = app.headerGrid.getStore();
		//全选表格中的记录
		store.on('load',function(store,records,options){
			if(options && options.params && options.params['fromLineTree']){ //这个参数是点击lineTree后设置上去的
				//从选择路线后加载的表格数据
				app.headerGrid.getSelectionModel().selectAll();
			}
			app.statusMgr.setPageStatus(app.initPageStatus);
			app.statusMgr.updateStatus();
		});	
		
		//展开运段明细
		function expandSegment(){
			var record = uft.Utils.getSelectedRecord(app.headerGrid);
			if(record){
				var pk_segment = record.get(app.getHeaderPkField());
				var url = ctxPath+'${segPackUrl}';
				url += "&pk_segment="+pk_segment;
				openDialog(url,null);
			}
		}
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='expandSegment()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(app.headerGrid,'_processor');
		if(processorColumn){
			processorColumn.renderer = processImg;	
		}
		//打开对话框
		function openDialog(url,params){
			var wparams = "dialogWidth:900px"
				+";dialogHeight:520px"
				+";dialogLeft:"+(window.screen.availWidth-900)/2+"px"
				+";dialogTop:"+(window.screen.availHeight-520)/2+"px"
				+";status:no;scroll:no;resizable:no;help:no;center:yes";
			if(Ext.isChrome){//chrome 从37版本开始不支持showModalDialog方法
				window.open(url,params,wparams);
			}else{
				window.showModalDialog(url,params,wparams);
			}
		}
		//定时刷新下表格
		var refreshPeriod = '<%=org.nw.utils.ParameterHelper.getPlanRefreshPeriod()%>';
		setUftInterval(function(){
			var grid = app.headerGrid;
			var options={};
			options.params={};
			options.params[grid.getStore().defaultParamNames.start]=0;
			options.params[grid.getStore().defaultParamNames.limit]=grid.pageSize;
			grid.getStore().reload(options);
		},this,parseInt(refreshPeriod));
		
		//打开车辆档案信息
		function openCarDoc(pk_car){
			var funCode = '<%=com.tms.constants.FunConst.CAR_CODE%>';
			uft.Utils.openNode(pk_car,'车辆档案',ctxPath+'/base/car/index.html?funCode='+funCode+'&nodeKey=view&billId='+pk_car);
		}
		
		//当选择了运段后，合计
		var grid = app.headerGrid;
		function setTotal(){
			var numCount=0,weightCount=0,volumeCount=0;
			var rs = uft.Utils.getSelectedRecords(grid);
			if(rs){
				for(var i=0;i<rs.length;i++){
					var num_count = uft.Utils.getNumberColumnValue(rs[i],'num_count');
					var weight_count = uft.Utils.getNumberColumnValue(rs[i],'weight_count');
					var volume_count = uft.Utils.getNumberColumnValue(rs[i],'volume_count');
					//获取ID
					numCount+= num_count;
					weightCount+= weight_count;
					volumeCount+= volume_count;
				}
			}
			_weight_count = weightCount.toFixed(0);
			_volume_count = volumeCount.toFixed(0)
			document.getElementById('numCount').innerHTML = numCount;
			document.getElementById('weightCount').innerHTML = weightCount.toFixed(0);
			document.getElementById('volumeCount').innerHTML = volumeCount.toFixed(0);
		}
		
		//满载率计算
		var timeOutFlag = true;
		function getRate(){
			if(!timeOutFlag){
				return;
			}else{
				timeOutFlag = false;
				var ids = [];
				setUftTimeout(function(){
					var rs = uft.Utils.getSelectedRecords(grid);
					if(rs){
						for(var i=0;i<rs.length;i++){
							if(rs[i].get('vbillstatus') == 10
									|| rs[i].get('vbillstatus') == '10'){//只需要待调度运段
								ids.push(rs[i].get('vbillno'));//运段号
							}
							
						}
					}
					seg_billnos = ids;
					doProc();
					timeOutFlag = true;
				},this,100);
			}
			
		}
		grid.getSelectionModel().on('rowselect',function(sm,rowIndex,r){
			setTotal();
			getRate();
		},this);
		grid.getSelectionModel().on('rowdeselect',function(sm,rowIndex,r){
			setTotal();
			getRate();
		},this);

		//打开跟踪地图
		function openCurrentTracking(carno){
			var win = Ext.getCmp('CurrentTracking');
			if(!win){
				win = new uft.tp.CurrentTracking({carno:carno});
			}
			win.show();
		}
		
		function refreshTrees(){
			doProc();
			//统计选择的订单信息
			var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
			var singleMap = {};
			var numMap = {};
			var weightMap = {};
			var volumeMap = {};
		
			for(var i=0;i<records.length;i++){
				var line_code = records[i].get('pz_line');
				
				var num = records[i].get('num_count');
				var weight = records[i].get('weight_count');
				var volume = records[i].get('volume_count');
				
				var single = singleMap[line_code];
				if(typeof(single) == 'undefined'){
					singleMap[line_code] = 1;
				}else{
					singleMap[line_code] = singleMap[line_code] + 1;
				}
				
				var num_count = numMap[line_code];
				if(typeof(num_count) == 'undefined'){
					numMap[line_code] = num;
				}else{
					numMap[line_code] = numMap[line_code] + num;
				}
				
				var weight_count = weightMap[line_code];
				if(typeof(weight_count) == 'undefined'){
					weightMap[line_code] = weight;
				}else{
					weightMap[line_code] = weightMap[line_code] + weight;
				}
				
				var volume_count = volumeMap[line_code];
				if(typeof(volume_count) == 'undefined'){
					volumeMap[line_code] = volume;
				}else{
					volumeMap[line_code] = volumeMap[line_code] + volume;
				}
			}
			//修改线路里的订单数量
			var checkedNodes = lineTree.getRootNode().childNodes;
			for(var line_code in singleMap){
				for(var i=0;i<checkedNodes.length;i++){
					var node = checkedNodes[i];
					var text = node.text;
					var info =  text.split(' ')[1].split('|');
					var code = text.split(' ')[0];
					if(code == line_code){
						var single = singleMap[line_code];
						var num = numMap[line_code];
						var weight = weightMap[line_code];
						var volume =volumeMap[line_code];
						info[0] = info[0]*1-single;
						info[1] = info[1]*1-num;
						info[2] = info[2]*1-weight;
						info[3] = info[3]*1-volume;
						text = code + ' ' + info.join('|');
						node.setText(text);
					}
				}
			}
		}
	</script>
<%@ include file="/common/footer.jsp"%>
</html>

