Ext.namespace('uft.tp');
/**
 * 分量界面,
 * 传入的参数：选择的运段记录
 * @class uft.tp.QuantityWindow
 * @extends Ext.Window
 */
uft.tp.QuantityWindow = Ext.extend(Ext.Window, {
	pk_segment : null, //传入的运段参数，必须的,可能包含多个值
	grid : null, //关联的grid
	constructor : function(config) {
		Ext.apply(this,config);
		var headerColumns = [{
		   header : 'pk_seg_pack_b',
		   hidden : true,
	       dataIndex: 'pk_seg_pack_b'
	    },{
		   header : 'pk_segment',
		   hidden : true,
	       dataIndex: 'pk_segment'
	    },{
		   header : 'pk_goods',
		   hidden : true,
	       dataIndex: 'pk_goods'
	    },{
		   header : 'unit_weight',
		   hidden : true,
	       dataIndex: 'unit_weight'
	    },{
		   header : 'unit_volume',
		   hidden : true,
	       dataIndex: 'unit_volume'
	    },{
	        header: '<span class="uft-grid-header-column-not-edit">货品编码</span>',
	        dataIndex: 'goods_code'
		},{
	        header: '<span class="uft-grid-header-column-not-edit">货品名称</span>',
	        dataIndex: 'goods_name'
		},{
	        header: '<span class="uft-grid-header-column-not-edit"><center>件数</center></span>',
	        dataIndex: 'num',
	        width : 80,
	        align : 'right'
		},{
	       header: '<span class="uft-grid-header-column-not-edit"><center>重量</center></span>',
	       dataIndex: 'weight',
	       width : 80,
	       align : 'right'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit"><center>体积</center></span>',
	       dataIndex: 'volume',
	       width : 80,
	       align : 'right'
	    },{
	        header: '单位数量',
	        hidden : true,
	        dataIndex: 'pack_num'
		},{
	        header: '<span class="uft-grid-header-column"><center>拆分数量</center></span>',
	        dataIndex: 'dist_pack_num_count',
	        width : 80,
	        align : 'right',
	        editor:{"xtype":"uftnumberfield","allowBlank":true,"maxLength":200,decimalPrecision:0}
		},{
	       header: '<span class="uft-grid-header-column"><center>拆分件数</center></span>',
	       dataIndex: 'dist_num',
	       width : 80,
	       align : 'right',
	       editor:{"xtype":"uftnumberfield","allowBlank":true,"maxLength":200,decimalPrecision:0},
	       beforeRenderer : function(value,meta,record){
				meta.css='css11';
	       }
		},{
	       header: '<span class="uft-grid-header-column"><center>拆分重量</center></span>',
	       dataIndex: 'dist_weight',
	       width : 80,
	       align : 'right',
	       editor:{"xtype":"uftnumberfield","allowBlank":true,"maxLength":200,decimalPrecision:2}
	    },{
	       header: '<span class="uft-grid-header-column"><center>拆分体积</center></span>',
	       dataIndex: 'dist_volume',
	       align : 'right',
	       width : 80,
	       editor:{"xtype":"uftnumberfield","allowBlank":true,"maxLength":200,decimalPrecision:2}
	    }];
		var headerRecordType =[
	        {name: 'pk_seg_pack_b', type: 'string'},
	        {name: 'pk_segment', type: 'string'},
	        {name: 'pk_goods', type: 'string'},
	        {name: 'unit_weight', type: 'float'},
	        {name: 'unit_volume', type: 'float'},
	        {name: 'goods_code', type: 'string'},
	        {name: 'goods_name', type: 'string'},
	        {name: 'pack_num', type: 'int'},
	        {name: 'dist_pack_num_count', type: 'int'},
	        {name: 'num', type: 'int'},
	        {name: 'weight', type: 'float'},
	        {name: 'volume', type: 'float'},
	        {name: 'dist_num', type: 'float'},
	        {name: 'dist_weight', type: 'float'},
	        {name: 'dist_volume', type: 'float'}
		];
		this.headerGrid = new uft.extend.grid.EditorGrid({
			isAddBbar : false,//不分页
			immediatelyLoad : true,
			dataUrl : 'loadSegPackByParent.json',
			params : {pk_segment:this.pk_segment},
			recordType : headerRecordType,
			columns : headerColumns
		});
		var headerPanel = new Ext.Panel({
			border : false,
			region : 'north',
			layout : 'fit',
			items : [this.headerGrid]
		});
		
		//定义按钮
		var btnAry = [new Ext.Button({
			iconCls : 'btnYes',
			text : '保&nbsp;&nbsp;存',
			actiontype : 'submit',
			scope : this,
			handler : this.saveAction
		}),new Ext.Button({
			iconCls : 'btnCancel',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.destroy();
			}
		})];
		uft.tp.QuantityWindow.superclass.constructor.call(this, {
			title : '拆量',
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
			buttons : btnAry
	    });
	    this.registerAfterEditEvent();
	},
	//注册编辑后事件，包括：1、选择地址编码后带出城市、详细地址
	registerAfterEditEvent : function(){
		this.headerGrid.on('afteredit',function(e){
			var record=e.record;
			var fieldName=e.field;
			if(fieldName == 'dist_num'){
				//编辑拆分件数
				var num = uft.Utils.getNumberColumnValue(record,'num');//件数
				var dist_num = uft.Utils.getNumberColumnValue(record,'dist_num');
				var unit_weight = uft.Utils.getNumberColumnValue(record,'unit_weight'); //单位重
				var unit_volume = uft.Utils.getNumberColumnValue(record,'unit_volume'); //单位体积
				if(dist_num < 0 || dist_num >num){
					//不能小于等于0，也不能大于等于件数
					uft.Utils.showWarnMsg('拆分件数必须大于等于0并且小于等于总件数！');
					uft.Utils.setColumnValue(record,'dist_num',0);
					return;
				}
				record.beginEdit();
				if(dist_num == num){
					record.set('dist_weight',uft.Utils.getNumberColumnValue(record,'weight'));
					record.set('dist_volume',uft.Utils.getNumberColumnValue(record,'volume'));
				}else{
					record.set('dist_weight',dist_num*unit_weight);
					record.set('dist_volume',dist_num*unit_volume);
				}
				
				//数量=件数×包装的数量
				var pack_num = uft.Utils.getNumberColumnValue(record,'pack_num');
				uft.Utils.setColumnValue(record,'dist_pack_num_count',pack_num*dist_num);
				record.endEdit();
			}else if(fieldName == 'dist_weight'){
				var dist_weight = uft.Utils.getNumberColumnValue(record,'dist_weight'); //拆分重量
				var weight = uft.Utils.getNumberColumnValue(record,'weight');
				if(dist_weight > weight){
					uft.Utils.showWarnMsg('拆分重量不能大于总重量！');
					uft.Utils.setColumnValue(record,'dist_weight',0);
					return;
				}
			}else if(fieldName == 'dist_volume'){
				var dist_volume = uft.Utils.getNumberColumnValue(record,'dist_volume'); //拆分体积
				var volume = uft.Utils.getNumberColumnValue(record,'volume');
				if(dist_volume > volume){
					uft.Utils.showWarnMsg('拆分体积不能大于总体积！');
					uft.Utils.setColumnValue(record,'dist_volume',0);
					return;
				}
			}
		},this);
		//表格数据加载后事件,如果记录为0，提示下没有可拆分的货品包装明细
		var store = this.headerGrid.getStore();
		store.on('load',function(e){
			if(store.getCount() == 0){
				Ext.Msg.show({
					title:'提示',
					msg: '该运段没有可拆分的货品包装明细！',
					buttons : Ext.Msg.OK,
					icon : Ext.Msg.INFO
				});
			}
		},this);
	},	
	//保存按钮的事件
	saveAction : function(){
		this.headerGrid.stopEditing();
		var store = this.headerGrid.getStore(),len = store.getCount();
		var allZero = true;
		var pk_seg_pack_bAry=[],dist_pack_num_countAry=[],dist_numAry=[],dist_weightAry=[],dist_volumeAry=[];
		for(var i=0;i<len;i++){
			var record = store.getAt(i);
			var pk_seg_pack_b = uft.Utils.getColumnValue(record,'pk_seg_pack_b');
			var dist_pack_num_count = uft.Utils.getNumberColumnValue(record,'dist_pack_num_count');//拆分数量
			var dist_num = uft.Utils.getNumberColumnValue(record,'dist_num');//拆分件数
			var dist_weight = uft.Utils.getNumberColumnValue(record,'dist_weight'); //拆分重量
			var dist_volume = uft.Utils.getNumberColumnValue(record,'dist_volume'); //拆分体积
			var num = uft.Utils.getNumberColumnValue(record,'num');
			var weight = uft.Utils.getNumberColumnValue(record,'weight');
			var volume = uft.Utils.getNumberColumnValue(record,'volume');
			if(dist_num < 0 || dist_num >num){
				//不能小于0，也不能大于等于件数
				if(!typeof(langague) == 'undefined' && langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Resolution number must be greater than or equal to 0 and less than or equal to the total number!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的拆分件数必须大于等于0，并且小于等于总件数！');
				}
				uft.Utils.setColumnValue(record,'dist_num',0);
				return;
			}
			if(len == 1){
				if(dist_num == 0 || dist_num == num){
					uft.Utils.showWarnMsg('拆分件数必须不等于0，并且不等于总件数！');
					uft.Utils.setColumnValue(record,'dist_num',0);
					return;
				}
			}
			
			if(dist_weight > weight){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Split weight can not be greater than the total weight!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的拆分重量不能大于总重量！');
				}
				return;
			}
			if(dist_volume > volume){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Split volume can not be greater than the total volume!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的拆分体积不能大于总体积！');
				}
				
				return;
			}
			if(dist_num != 0 || dist_weight != 0 || dist_volume != 0){
				allZero = false;
			}
			pk_seg_pack_bAry.push(pk_seg_pack_b);
			dist_pack_num_countAry.push(dist_pack_num_count);
			dist_numAry.push(dist_num);
			dist_weightAry.push(dist_weight);
			dist_volumeAry.push(dist_volume);
		}
		if(allZero){
			uft.Utils.showWarnMsg('拆分件数、拆分重量和拆分体积不能全部为0！');
			return;
		}
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : {pk_segment:this.pk_segment,pk_seg_pack_b:pk_seg_pack_bAry,dist_pack_num_count:dist_pack_num_countAry,
	    			  dist_num:dist_numAry,dist_weight:dist_weightAry,dist_volume:dist_volumeAry},
	    	url : 'distQuantity.json',
	    	success : function(values){
	    		//重新加载数据
	    		if(this.grid){
	    			this.grid.getStore().reload();
	    		}
	    		this.close(); //关闭该窗口
	    	}
	    });			
	}
});
