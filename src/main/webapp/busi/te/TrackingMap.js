Ext.namespace('uft.te');
/**
 * 异常跟踪-维护运力信息-路线跟踪
 * @class uft.te.Tracking
 * @extends Ext.Window
 */
uft.te.TrackingMap = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.pk_entrust){
			uft.Utils.showErrorMsg('请先选中一条委托单！');
			return false;
		}
		if(!this.gps_id){
			uft.Utils.showErrorMsg('请先选中一个gps号！');
			return false;
		}
		
		var panel = new Ext.Panel({
			layout : 'fit',
			html : '<div id="container" style="width:100%;height:100%;"></div>'
		});		
		
		uft.te.TrackingMap.superclass.constructor.call(this, {
			id : 'TrackingMap',
			title : '路线跟踪',
			width : 900,
			height : 500,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			buttons : [new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();//多个地方使用地图，而地图渲染的id都是container，这个就不改了
					}
			})],
			items : [panel]
	    });
	},
	show : function(){
		if(this.map){
			this.map.clearOverlays();
		}
		//1、将提货方和收货方标注到地图上
		uft.Utils.doAjax({
	    	scope : this,
	    	params : {pk_entrust:this.pk_entrust},
	    	url : 'getVirtualTrack.json',
	    	isTip : false,
	    	success : function(values){
	    		if(values && values.datas){
	    			var addrs = values.datas;
	    			//三种驾车策略：最少时间，最短距离，避开高速
					var routePolicy = [BMAP_DRIVING_POLICY_LEAST_TIME,BMAP_DRIVING_POLICY_LEAST_DISTANCE,BMAP_DRIVING_POLICY_AVOID_HIGHWAYS];
	    			var drv = new BMap.DrivingRoute(this.map, {renderOptions:{map: this.map, autoViewport: true},policy:routePolicy[1]});
					drv.search(addrs[0], addrs[1]);
//	    			drv.search("厦门", "北京");
	    		}
	    	}
	    });
		//2、读取跟踪数据，并显示在地图上
		uft.Utils.doAjax({
	    	scope : this,
	    	params : {pk_entrust:this.pk_entrust,gps_id:this.gps_id},
	    	url : 'getGpsTrackVOs.json',
	    	isTip : false,
	    	success : function(rootVO){
	    		if(rootVO && rootVO.result){
	    			var ds = rootVO.dataset;
	    			if(ds){
	    				//将数据标注到地图上
	    				if(ds.length > 0){
	    					var ptAry = [],map = this.map,osp,esp;
		    				for(var i=0;i<ds.length;i++){
		    					var data = ds[i];
		    					var obj = {lng:data.longitude,lat:data.latitude,title : data.place_name+data.road_name};
		    					if(i == 0){
		    						osp = obj;
		    					}else if (i == ds.length-1){
		    						esp = obj;
		    					}else{
		    						ptAry.push(new BMap.Point(data.longitude, data.latitude));
		    					}
		    				}
		    				if(!esp){//如果只有起始点，那么终点也是起始点
		    					esp = osp;
		    				}
	    				
		    				var sp = new BMap.Point(osp.lng, osp.lat);//起始点
//		    				var sm = new BMap.Marker(sp); //按照地图点坐标生成标记  
//		    				sm.setTitle(osp.title);
//                        	map.addOverlay(sm);
		    				
		    				var ep = new BMap.Point(esp.lng, esp.lat);//终点
//                        	var em = new BMap.Marker(ep);
//                        	em.setTitle(esp.title);
//                        	map.addOverlay(em);
		    				
		    				var drv = new BMap.DrivingRoute(map);
							//从起点到终点
//							drv.search(sp, ep/*,{waypoints:ptAry}*/);//定义经过的点,最多支持10个
//							drv.setSearchCompleteCallback(function(res){
//								//FIXME 
//								res.getStart().title = osp.title;
//								res.getEnd().title = esp.title;
//								ptAry = drv.getResults().getPlan(0).getRoute(0).getPath();
//								//map.setViewport(ptAry);
//								map.addOverlay(new BMap.Polyline(ptAry, {strokeColor: "red", strokeWeight: 5, strokeOpacity: 1}));
//							});
	    				}
	    			}
	    		}
	    	}
	    });
		uft.te.TrackingMap.superclass.show.call(this);
	},
	afterRender : function(){
		uft.te.TrackingMap.superclass.afterRender.call(this);
		//创建地图
		this.map = new BMap.Map("container");          // 创建地图实例  
		this.map.addControl(new BMap.NavigationControl());    
		this.map.addControl(new BMap.ScaleControl());
		this.map.addControl(new BMap.OverviewMapControl());    
		this.map.addControl(new BMap.MapTypeControl());    
		this.map.enableScrollWheelZoom(true);//使用鼠标滚轮来缩放地图
		this.map.centerAndZoom("北京",5);
	}
});
