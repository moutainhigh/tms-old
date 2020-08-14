Ext.namespace('uft.tp');
/**
 *
 * 调度配载-点击车牌号，显示当前车辆的位置
 * @class uft.te.Tracking
 * @extends Ext.Window
 */

uft.tp.CurrentTracking = Ext.extend(Ext.Window, {
	interval : 60,//一分钟刷新下位置
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.carno){
			uft.Utils.showErrorMsg('请先选中一个车牌号或者司机！');
			return false;
		}
		
		var panel = new Ext.Panel({
			layout : 'fit',
			html : '<div id="container" style="width:100%;height:100%;"></div>'
		});		
		
		uft.tp.CurrentTracking.superclass.constructor.call(this, {
			id : 'CurrentTracking',
			title : '车辆跟踪',
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
		this.doTrack();
		uft.tp.CurrentTracking.superclass.show.call(this);
		//10s倒计时效果
		window.setUftInterval(function(){
			var secDiv = Ext.fly('secDiv');
			if(!secDiv){
				return;
			}
			var span = secDiv.select('#secDiv span').elements[0];
			var value = span.innerHTML;
			value = parseInt(value);
			if(value == 0){
				span.innerHTML = this.interval;
				this.doTrack();
			}else{
				//转成string，如果直接使用int的0，不会更新到界面
				span.innerHTML = String(value-1);
			}
		},this,1000);
	},
	doTrack : function(){
		if(this.map){
			this.map.clearOverlays();
		}
		//显示车辆目前的位置
		uft.Utils.doAjax({
	    	scope : this,
	    	params : {carno:this.carno},
	    	url : ctxPath + '/tp/sto/getCurrentTracking.json',
	    	isTip : false,
	    	mask : false,
	    	success : function(rootVO){
	    		if(rootVO && rootVO.result){
	    			var ds = rootVO.dataset;
	    			if(ds && ds.length > 0){
	    				//将数据标注到地图上
    					var data = ds[0];
    					var pt = new BMap.Point(data.longitude, data.latitude);
    					//FIXME 得到车辆照片
    					var myIcon = new BMap.Icon(ctxPath+"/busi/te/images/green_2.gif",new BMap.Size(28,28));
						var marker = new BMap.Marker(pt,{icon:myIcon});  // 创建标注
						this.map.addOverlay(marker);              // 将标注添加到地图中
						this.map.centerAndZoom(pt, 15);
						
						var ct_addr = Ext.fly('ct_addr');
						if(!ct_addr){
							return;
						}
						ct_addr.update(data.place_name+data.road_name);
						var html = '<ul><li>名称：'+data.gpsid+'</li>' +
								'<li>经度：'+data.longitude+'</li>' +
								'<li>纬度：'+data.latitude+'</li>' +
								'<li>速度：'+data.speed+'</li>' +
								'<li>里程：'+data.distance+'</li>' +
								'<li>地址：'+data.place_name + data.road_name+'</li>' +
								'<li>定位时间：'+data.gps_time+'</li>' +
								'<li>备注：'+data.memo+'</li>' +
								'<li>来源：'+rootVO.source+'</li>' +
								'</ul>';
						Ext.fly('gpsDetailDiv').update(html);
	    			}
	    		}
	    	}
	    });
	},
	afterRender : function(){
		uft.tp.CurrentTracking.superclass.afterRender.call(this);
		//创建地图
		this.map = new BMap.Map("container");          // 创建地图实例  
		this.map.addControl(new BMap.NavigationControl());    
		this.map.addControl(new BMap.ScaleControl());
		this.map.addControl(new BMap.OverviewMapControl());    
		this.map.addControl(new BMap.MapTypeControl());    
		this.map.enableScrollWheelZoom(true);//使用鼠标滚轮来缩放地图
		this.map.centerAndZoom("北京",5);
		
		var c = Ext.get("container"),helper = Ext.DomHelper;
		helper.append(c,"<div id='secDiv'><span>"+this.interval+"</span>秒后刷新    位置：<span id='ct_addr'></span></div>",true);
		helper.append(c,"<div id='gpsDiv'><div id='gpsDiv1'><h1 style='padding: 5px 0 0 5px;font-size: 14px;'>GPS</h1>" +
				"<HR style='position: inherit;' width='90%' color='#987cb9'/><div id='gpsDetailDiv' style='margin: 15px;line-height: 23px;'></div>" +
				"<div></div>",true);
		helper.append(Ext.get('gpsDiv'),'<div class="gpsDiv_om omBtnClosed" style="bottom: 0px; left: 0px; top: auto; right: auto;"></div>');
		Ext.query(".gpsDiv_om")[0].onclick = function(){
			var el = Ext.get(this);
			var bol = el.hasClass('omBtnClosed');
			if(bol){
				el.removeClass('omBtnClosed');
				el.addClass('omBtn');
			}else{
				el.removeClass('omBtn');
				el.addClass('omBtnClosed');
			}
			Ext.get('gpsDiv1').toggle();
		};
	}
});
