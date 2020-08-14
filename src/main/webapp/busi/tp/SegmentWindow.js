Ext.namespace('uft.tp');
/**
 * 分段界面,
 * 传入的参数：选择的运段记录
 * @class uft.tp.SegmentWindow
 * @extends Ext.Window
 */
uft.tp.SegmentWindow = Ext.extend(Ext.Window, {
	pk_segment : null, //传入的运段参数，必须的,可能包含多个值
	max_req_deli_date : null, //最大的要求提货日期
	min_req_arri_date : null, //最小的要求到货日期
	grid : null,//窗口所依赖的表格
	DATETIME_FORMAT : uft.jf.Constants.DATETIME_FORMAT,
	DATE_FORMAT : uft.jf.Constants.DATE_FORMAT,
	constructor : function(config) {
		Ext.apply(this,config);
		var headerColumns = [{
			header : 'pk_address',
		   	hidden : true,
	       	dataIndex: 'pk_address'
	    },{
			header : 'pk_city',
		   	hidden : true,
	       	dataIndex: 'pk_city'
	    },{
	    	"xtype":"refcolumn",
	        header: '<span class="uft-grid-header-column">地址编码</span>',
	        dataIndex: 'addr_code',
	        editor:{"xtype":"bodyreffield",
	        		refName:'地址档案-web',
	        		"idcolname":"pk_address","refWindow":{"model":1,"leafflag":false,
		        		"gridDataUrl":ctxPath+"/ref/common/addr/load4Grid.json",
		        		"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_address","xtype":"gridcolumn","hidden":true},
		        							   {"header":"地址编码","type":"string","width":120,"dataIndex":"addr_code","sortable":true,"xtype":"gridcolumn"},
		        							   {"header":"地址名称","type":"string","width":120,"dataIndex":"addr_name","sortable":true,"xtype":"gridcolumn"},
		        							   {"header":"地址类型","type":"string","width":120,"dataIndex":"addr_type_name","sortable":true,"xtype":"gridcolumn"},
		        							   {"header":"备注","type":"string","width":120,"dataIndex":"memo","sortable":true,"xtype":"gridcolumn"},
		        							   {"type":"string","width":120,"dataIndex":"addr_type","sortable":true,"xtype":"gridcolumn","hidden":true}]},
	        		"pkField":"pk_address","codeField":"addr_code","nameField":"addr_name","showCodeOnBlur":true,"getByPkUrl":ctxPath+"/ref/common/addr/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/addr/getByCode.do"}
		},{
	       	header: '<span class="uft-grid-header-column-not-edit">地址名称</span>',
	       	dataIndex: 'addr_name',
	       	editable : false
	    },{
			"xtype":"refcolumn",
	         header: '<span class="uft-grid-header-column-not-edit">城市</span>',
	         dataIndex: 'city_name',
	         editable : false
		},{
	       	header: '<span class="uft-grid-header-column-not-edit">详细地址</span>',
	       	dataIndex: 'detail_addr',
	       	editable : false
	    },{
	    	xtype :'datetimecolumn',
	       	width : 130,
	       	header: '<span class="uft-grid-header-column-not-null">要求到达日期</span>',
	       	dataIndex: 'req_arri_date',
	       	editor:{id:'_req_arri_date',"xtype":"datetimefield","maxLength":200}
	    },{
	    	xtype :'datetimecolumn',
	       	width : 130,
	       	header: '要求离开日期',
	       	dataIndex: 'req_deli_date',
	       	editor:{"xtype":"datetimefield","maxLength":200}
	    }];
		var headerRecordType =[
	        {name: 'pk_address', type: 'string'},
	        {name: 'addr_code', type: 'string'},
	        {name: 'pk_city', type: 'string'},
	        {name: 'detail_addr', type: 'string'},
	        {name: 'req_arri_date', type: 'string'},
	        {name: 'req_deli_date', type: 'string'}
		];
		this.headerGrid = new uft.extend.grid.EditorGrid({
			isAddBbar : false,//不分页
			dragDropRowOrder : true, //使用拖放调整顺序
			autoExpandColumn : 6,
			recordType : headerRecordType,
			columns : headerColumns
		});
		var headerPanel = new Ext.Panel({
			border : false,
			region : 'north',
			layout : 'fit',
			height : 150,
			items : [this.headerGrid],
			tbar : new Ext.Toolbar({
				items : [{
					xtype : 'button',
					text : '增加节点',
					iconCls : 'btnAdd',
					scope : this,
					handler : function(){
						var grid = this.headerGrid;
						grid.stopEditing();
						grid.addRow();
					}
				},{
					xtype : 'button',
					text : '删除节点',
					iconCls : 'btnDel',
					scope : this,
					handler : function(){
						var grid = this.headerGrid,store = grid.getStore();
						var record = uft.Utils.getSelectedRecord(grid);
						if(record){
							grid.stopEditing();
							var rowIndex = store.indexOf(record);
							var nextRecord = store.getAt(rowIndex+1);
							var frontRecord = store.getAt(rowIndex-1);
							//更新底部工具栏的显示信息
							if(nextRecord){
								grid.getSelectionModel().selectRow(rowIndex+1);
							}else{
								if(frontRecord){
									grid.getSelectionModel().selectRow(rowIndex-1);
								}
							}
							store.remove(record);
						}else{
							uft.Utils.showWarnMsg('请先选中要删除的记录！');
							return false;
						}
					}
				}]
			})
		});
		
		var bodyColumns = [{
		   header : 'pk_segment',
		   hidden : true,
	       dataIndex: 'pk_segment'
	    },{
	        header: '<span class="uft-grid-header-column-not-edit">发货单号</span>',
	        dataIndex: 'invoice_vbillno'
		},{
	         header: '<span class="uft-grid-header-column-not-edit">运段号</span>',
	         dataIndex: 'vbillno'
		},{
	       header: '<span class="uft-grid-header-column-not-edit">起始地</span>',
	       hidden : true,
	       dataIndex: 'pk_delivery'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit">起始地</span>',
	       dataIndex: 'pk_delivery_name'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit">起始地城市</span>',
	       dataIndex: 'deli_city'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit">目的地</span>',
	        hidden : true,
	       dataIndex: 'pk_arrival'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit">目的地</span>',
	       dataIndex: 'pk_arrival_name'
	    },{
	       header: '<span class="uft-grid-header-column-not-edit">目的地城市</span>',
	       dataIndex: 'arri_city'
	    },{
	    	xtype :'datetimecolumn',
	       header: '<span class="uft-grid-header-column-not-edit">要求提货日期</span>',
	       width : 125,
	       dataIndex: 'req_deli_date'
	    },{
	    	xtype :'datetimecolumn',
	       header: '<span class="uft-grid-header-column-not-edit">要求到货日期</span>',
	       width : 125,
	       dataIndex: 'req_arri_date'
	    }];
		var bodyRecordType =[
	        {name: 'pk_segment', type: 'string'},
	        {name: 'invoice_vbillno', type: 'string'},
	        {name: 'vbillno', type: 'string'},
	        {name: 'pk_delivery', type: 'string'},
	        {name: 'pk_delivery_name', type: 'string'},
	        {name: 'deli_city', type: 'string'},
	        {name: 'pk_arrival', type: 'string'},
	        {name: 'pk_arrival_name', type: 'string'},
	        {name: 'arri_city', type: 'string'},
	        {name: 'req_deli_date', type: 'string'},
	        {name: 'req_arri_date', type: 'string'}
		];
		this.bodyGrid = new uft.extend.grid.BasicGrid({
			isAddBbar : false,//不分页
			immediatelyLoad : true,
			dataUrl : 'loadByPKs.json',
			params : {pk_segment:this.pk_segment},
			recordType : bodyRecordType,
			columns : bodyColumns
		});	
		var bodyTabPanel = new Ext.TabPanel({
			border : false,
			region : 'center',
			activeTab: 0,
			items : [{
				layout : 'fit',
				title : '所选订单',
				items : [this.bodyGrid]
			}]
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
				this.close();
			}
		})];
		uft.tp.SegmentWindow.superclass.constructor.call(this, {
			title : '拆段',
			width : 900,
			height : 400,
			collapsible : false,
			frame : true,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'border',
			items : [headerPanel,bodyTabPanel],
			buttons : btnAry
	    });
	    //注册事件
	    this.registerAfterEditEvent();
	},
	onRender : function(ct, position){
		uft.tp.SegmentWindow.superclass.onRender.call(this,ct, position);
		this.headerGrid.addRow();//自动增加一行
	},
	//注册编辑后事件，包括：1、选择地址编码后带出城市、详细地址
	registerAfterEditEvent : function(){
		this.headerGrid.addListener('afteredit',function(e){
			var record=e.record;
			var fieldName=e.field;
			if(fieldName == 'addr_code'){
				//编辑地址编码
				var pk_address = record.get('pk_address');
				//这个地址不能是提货方或者收货方
				var ds = this.bodyGrid.getStore();
				for(var i=0;i<ds.getCount();i++){
					var _r = ds.getAt(i);
					var _pk_delivery = _r.get('pk_delivery');
					var _pk_arrival = _r.get('pk_arrival');
					if(pk_address == _pk_delivery || pk_address == _pk_arrival){
						record.set('pk_address',null);
						record.set('addr_code',null);
						record.set('addr_name',null);
						uft.Utils.showWarnMsg('拆段节点不能是运段的提货方或者收货方！');
						return;
					}
				}
				var values = Utils.doSyncRequest('afterEditAddrCode.json',{pk_address:pk_address},'POST');
				if(values){
					for(var key in values){
						record.beginEdit();
						record.set(key.trim(),values[key]);
						record.endEdit();
					}
				}
			}else if(fieldName == 'req_deli_date'){
				var req_deli_date = record.get('req_deli_date');//要求离开日期
				var req_arri_date = record.get('req_arri_date');//要求到达日期
				if(req_deli_date){
					if(!req_deli_date instanceof Date){
						req_deli_date = Date.parseDate(req_deli_date,this.DATETIME_FORMAT);
					}
					//XXX 千万注意，这里不能直接修改req_deli_date变量，否则造成原始数据的修改
					var a = req_deli_date.dateFormat(this.DATETIME_FORMAT);
					a = Date.parseDate(a.substring(0,10),this.DATE_FORMAT);
					if(a < this.max_req_deli_date.clearTime()){//2014-09-18 不比较时间了，直接比较日期就可以了
						record.set('req_deli_date',null);
						uft.Utils.showWarnMsg('要求离开日期必须大于等于您选择的所有运段中最大的提货日期！');
						return;
					}else if(a > this.min_req_arri_date.clearTime()){
						record.set('req_deli_date',null);
						uft.Utils.showWarnMsg('要求离开日期必须小于等于您选择的所有运段中最小的到货日期！');
						return;
					}
					if(req_arri_date){
						if(!req_arri_date instanceof Date){
							req_arri_date = Date.parseDate(req_arri_date,this.DATETIME_FORMAT);
						}
						if(req_deli_date < req_arri_date){//对于同一个节点，比较时间
							record.set('req_deli_date',null);
							uft.Utils.showWarnMsg('要求离开日期必须大于等于要求到达日期！');
							return;
						}
					}
				}
			}else if(fieldName == 'req_arri_date'){
				var req_arri_date = record.get('req_arri_date');//要求到达日期
				var req_deli_date = record.get('req_deli_date');//要求离开日期
				if(req_arri_date){
					if(!req_arri_date instanceof Date){
						req_arri_date = Date.parseDate(req_arri_date,this.DATETIME_FORMAT);
					}
					var a = req_arri_date.dateFormat(this.DATETIME_FORMAT);
					a = Date.parseDate(a.substring(0,10),this.DATE_FORMAT);
					if(a > this.min_req_arri_date.clearTime()){//2014-09-18 不比较时间了，直接比较日期就可以了
						record.set('req_arri_date',null);
						uft.Utils.showWarnMsg('要求到达日期必须小于您选择的所有运段中最小的到货日期！');
						return;
					}else if(a < this.max_req_deli_date.clearTime()){
						record.set('req_arri_date',null);
						uft.Utils.showWarnMsg('要求到达日期必须大于您选择的所有运段中最大的提货日期！');
						return;
					}
					if(req_deli_date){
						if(!req_deli_date instanceof Date){
							req_deli_date = Date.parseDate(req_deli_date,this.DATETIME_FORMAT);
						}
						if(req_deli_date < req_arri_date){//对于同一个节点，比较时间
							record.set('req_arri_date',null);
							uft.Utils.showWarnMsg('要求离开日期必须大于等于要求到达日期！');
							return;
						}
					}
				}
			}
		},this);
	},
	//保存按钮的事件,执行分段
	saveAction : function(){
		this.headerGrid.stopEditing();
		var store = this.headerGrid.getStore(),len = store.getCount();
		var HEADER = [],req_arri_dateAry = [],req_deli_dateAry = [];
		for(var i=0;i<len;i++){
			var record = store.getAt(i);
			//地址主键以及包括要求到货日期，要求离开日期
			var pk_address = record.get('pk_address');
			var req_arri_date = uft.Utils.getColumnValue(record,'req_arri_date');
			var req_deli_date = uft.Utils.getColumnValue(record,'req_deli_date');
			if(!pk_address){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Address code cannot be empty!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的地址编码不能为空！');
				}
				return;
			}
			if(!req_arri_date){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Requested delivery date can not be empty!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的要求到货日期不能为空！');
				}
				
				return;
			}
			if(!req_deli_date){
				//2014-09-18 如果要求离开日期不存在，那么默认等于要求到货日期
				req_deli_date = req_arri_date;
//				uft.Utils.showWarnMsg('第'+(i+1)+'行的要求离开日期不能为空！');
//				return;
			}
			HEADER.push(pk_address);
			if(req_arri_date instanceof Date){
				req_arri_date = req_arri_date.dateFormat(this.DATETIME_FORMAT);
			}
			if(req_deli_date instanceof Date){
				req_deli_date = req_deli_date.dateFormat(this.DATETIME_FORMAT);
			}
			req_arri_dateAry.push(req_arri_date);
			req_deli_dateAry.push(req_deli_date);
		}
		if(HEADER.length == 0){
			uft.Utils.showWarnMsg('请先增加节点！');
			return;
		}
		store = this.bodyGrid.getStore();
		len = store.getCount();
		var BODY = [];
		for(var i=0;i<len;i++){
			var record = store.getAt(i);
			BODY.push(record.get('pk_segment'));
		}
		if(BODY.length == 0){
			uft.Utils.showWarnMsg('你没有选择任何运段！');
			return;
		}
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : {HEADER:HEADER,BODY:BODY,req_arri_date:req_arri_dateAry,req_deli_date:req_deli_dateAry},
	    	url : 'distSection.json',
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
