Ext.namespace('uft.kpi');

uft.kpi.KPI = Ext.extend(Ext.Window, {
	constructor : function(config) {
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请先选中记录！');
		return false;
	}
	var panel = new Ext.Panel({
		layout : 'fit',
		html : '<div id="container" style="width:100%;height:100%;"></div>'
	});	
	
//	var myChart = echarts.init(panel);
//	var option = {
//		    title : {
//		        text: '罗纳尔多',
//		        subtext: '完全实况球员数据'
//		    },
//		    tooltip : {
//		        trigger: 'axis'
//		    },
//		    legend: {
//		        x : 'center',
//		        data:['罗纳尔多']
//		    },
//		    toolbox: {
//		        show : true,
//		        feature : {
//		            mark : {show: true},
//		            dataView : {show: true, readOnly: false},
//		            restore : {show: true},
//		            saveAsImage : {show: true}
//		        }
//		    },
//		    calculable : true,
//		    polar : [
//		        {
//		            indicator : [
//		                {text : '进攻', max  : 100},
//		                {text : '防守', max  : 100},
//		                {text : '体能', max  : 100},
//		                {text : '速度', max  : 100},
//		                {text : '力量', max  : 100},
//		                {text : '技巧', max  : 100}
//		            ],
//		            radius : 130
//		        }
//		    ],
//		    series : [
//		        {
//		            name: '完全实况球员数据',
//		            type: 'radar',
//		            itemStyle: {
//		                normal: {
//		                    areaStyle: {
//		                        type: 'default'
//		                    }
//		                }
//		            },
//		            data : [
//		               
//		                {
//		                    value : [97, 32, 74, 95, 88, 92],
//		                    name : '罗纳尔多'
//		                }
//		            ]
//		        }
//		    ]
//		};
//	    // 为echarts对象加载数据 
//	    myChart.setOption(option);
	    
		uft.kpi.KPI.superclass.constructor.call(this, {
			id : 'KPI',
			title : '承运商KPI展示',
			width : 1000,
			height : 520,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [panel],
			buttons : [new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();
					}
			})],
	    });
	},

});

