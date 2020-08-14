Ext.namespace('uft.te');

uft.te.PackRecord = Ext.extend(Ext.Window, {
	pk_entrust : null, //传入的委托单参数
	grid : null, //关联的grid
	constructor : function(config) {
		Ext.apply(this,config);
		var headerColumns = [{
		   header : 'pk_entrust',
		   hidden : true,
	       dataIndex: 'pk_entrust'
	    },{
		   header : 'vbillno',
		   hidden : true,
	       dataIndex: 'vbillno'
	    },{
		   header : '<span class="uft-grid-header-column"><center>货品编码</center></span>',
		   width : 80,
		   align : 'left',
	       dataIndex: 'dd_goods_code'
	    },{
		   header : '<span class="uft-grid-header-column"><center>货品名称</center></span>',
		   width : 160,
		   align : 'left',
	       dataIndex: 'dd_goods_name'
	    },{
		   header : '<span class="uft-grid-header-column-not-edit"><center>原始件数</center></span>',
		   width : 80,
		   align : 'right',
	       dataIndex: 'dd_num',
	       beforeRenderer : function(value,meta,record){
				meta.css='css1';
	       }
	    },{
	        header: '<span class="uft-grid-header-column-not-edit"><center>原始重量</center></span>',
	        width : 80,
	        align : 'right',
	        dataIndex: 'dd_weight',
	        beforeRenderer : function(value,meta,record){
				meta.css='css1';
	       }
		},{
	        header: '<span class="uft-grid-header-column-not-edit"><center>原始体积</center></span>',
	        width : 80,
	        align : 'right',
	        dataIndex: 'dd_volume',
	        beforeRenderer : function(value,meta,record){
				meta.css='css1';
	       }
		},{
	        header: '<span class="uft-grid-header-column"><center>是否提货</center></span>',
	        hidden : true,
	        width : 80,
	        align : 'right',
	        dataIndex: 'th_arrival_flag'
		},{
	       header: '<span class="uft-grid-header-column"><center>提货件数</center></span>',
	       width : 80,
	       align : 'right',
	       dataIndex: 'th_num',
	       beforeRenderer : function(value,meta,record){
				meta.css='css2';
	       }
	    },{
	       header: '<span class="uft-grid-header-column"><center>提货重量</center></span>',
	       width : 80,
	       align : 'right',
	       dataIndex: 'th_weight',
	       beforeRenderer : function(value,meta,record){
				meta.css='css2';
	       }
	    },{
	        header: '<span class="uft-grid-header-column"><center>提货体积</center></span>',
	        width : 80,
	        align : 'right',
	        dataIndex: 'th_volume',
	        beforeRenderer : function(value,meta,record){
				meta.css='css2';
	       }
		},{
	        header: '<span class="uft-grid-header-column-not-edit"><center>是否到货</center></span>',
	        hidden : true,
	        width : 80,
	        align : 'right',
	        dataIndex: 'dh_arrival_flag'
		},{
	       header: '<span class="uft-grid-header-column-not-edit"><center>到货件数</center></span>',
	       width : 80,
	       align : 'right',
	       dataIndex: 'dh_num',
	       beforeRenderer : function(value,meta,record){
				meta.css='css3';
	       }
		},{
	       header: '<span class="uft-grid-header-column-not-edit"><center>到货重量</center></span>',
	       width : 80,
	       align : 'right',
	       dataIndex: 'dh_weight',
	       beforeRenderer : function(value,meta,record){
				meta.css='css3';
	       }
	    },{
	       header: '<span class="uft-grid-header-column-not-edit"><center>到货体积</center></span>',
	       width : 80,
	       align : 'right',
	       dataIndex: 'dh_volume',
	       beforeRenderer : function(value,meta,record){
				meta.css='css3';
	       }
	    },{
		   header : '<span class="uft-grid-header-column"><center>回单件数</center></span>',
		   width : 80,
		   align : 'right',
		   dataIndex: 'hd_num',
		   beforeRenderer : function(value,meta,record){
				meta.css='css4';
	       }
		},{
		   header : '<span class="uft-grid-header-column"><center>回单重量</center></span>',
		   width : 80,
		   align : 'right',
		   dataIndex: 'hd_weight',beforeRenderer : function(value,meta,record){
				meta.css='css4';
	       }
		
		},{
		   header : '<span class="uft-grid-header-column"><center>回单体积</center></span>',
		   width : 80,
		   align : 'right',
		   dataIndex: 'hd_volume',
		   beforeRenderer : function(value,meta,record){
				meta.css='css4';
	       }
		}];
		var headerRecordType =[
	        {name: 'pk_entrust', type: 'string'},
	        {name: 'vbillno', type: 'string'},
	        {name: 'dd_goods_code', type: 'string'},
	        {name: 'dd_goods_name', type: 'string'},
	        {name: 'dd_num', type: 'int'},
	        {name: 'dd_weight', type: 'float'},
	        {name: 'dd_volume', type: 'float'},
	        {name: 'th_arrival_flag', type: 'string'},
	        {name: 'th_num', type: 'int'},
	        {name: 'th_weight', type: 'float'},
	        {name: 'th_volume', type: 'float'},
	        {name: 'dh_arrival_flag', type: 'string'},
	        {name: 'dh_num', type: 'int'},
	        {name: 'dh_weight', type: 'float'},
	        {name: 'dh_volume', type: 'float'},
	        {name: 'hd_num', type: 'int'},
	        {name: 'hd_weight', type: 'float'},
	        {name: 'hd_volume', type: 'float'},
	        
		];
		this.headerGrid = new uft.extend.grid.EditorGrid({
			isAddBbar : false,//不分页
			immediatelyLoad : true,
			dataUrl : 'loadPackRecord.json',
			params : {pk_entrust:this.pk_entrust},
			recordType : headerRecordType,
			columns : headerColumns
		});
		var headerPanel = new Ext.Panel({
			border : false,
			region : 'north',
			layout : 'fit',
			items : [this.headerGrid]
		});
		
		uft.te.PackRecord.superclass.constructor.call(this, {
			title : '货品修改记录',
			width : 850,
			height : 400,
			collapsible : false,
			frame : true,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [headerPanel],
	    });
	}
});
