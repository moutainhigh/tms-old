Ext.namespace('uft.te');
/**
 * 节点到货
 * @class uft.te.NodeArrivalWindow
 * @extends Ext.Window
 */
uft.te.NodeArrivalWindow = Ext.extend(Ext.Window, {
	currentRecord : null,//表头表格所选中的行
	pk_entrust : null, //传入委托单
	DATETIME_FORMAT : "Y-m-d H:i:s",
	disabledRowClass : "x-grid3-row-selected-disable",
	isMulti : false,  //是否多选
	isBodyMulti : false, //是否是表体参照的多选,该参数在srcField中，因为脱离了srcField，则该参数没有作用
	constructor : function(config) {
		Ext.apply(this,config);
		
		selectItems = [];
		com_tms_jf_ts_ent_line_b_recordType = [];
		com_tms_jf_ts_ent_line_b_columns = [];
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_address","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"operate_name","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"operate_type","type":"int"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"addr_code","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"addr_name","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_city","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"city_name","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_province","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"province_name","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_area","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"area_name","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"detail_addr","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"act_arri_date","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"act_leav_date","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"memo","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"req_arri_date","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"req_leav_date","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"dr","type":"int"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_ent_line_b","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_entrust","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"serialno","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"ts","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"dr","type":"int"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"ts","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"addr_flag","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_segment","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"arrival_flag","type":"string"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"segment_node","type":"string"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址</span>","width":100,"dataIndex":"pk_address","xtype":"gridcolumn","hidden":true,"editable":false,editor:{"xtype":"textfield","maxLength":50}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>实际到达时间</span>","width":120,"dataIndex":"act_arri_date","xtype":"datetimecolumn",editor:{"xtype":"datetimefield",allowBlank:false,"maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>实际离开时间</span>","width":120,"dataIndex":"act_leav_date","xtype":"datetimecolumn",editor:{"xtype":"datetimefield","maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-null'>要求到达时间</span>","width":120,"dataIndex":"req_arri_date","xtype":"datetimecolumn","hidden":true});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-null'>要求离开时间</span>","width":120,"dataIndex":"req_leav_date","xtype":"datetimecolumn","hidden":true});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>备注</span>","width":200,"dataIndex":"memo","xtype":"gridcolumn",editor:{"xtype":"textfield","maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>操作类型</span>","width":100,"dataIndex":"operate_name","xtype":"gridcolumn"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>操作类型</span>","width":100,"dataIndex":"operate_type","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>地址编码</span>","width":100,"dataIndex":"addr_code","xtype":"refcolumn"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址名称</span>","width":100,"dataIndex":"addr_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>城市</span>","width":100,"dataIndex":"pk_city","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>城市</span>","width":100,"dataIndex":"city_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>省份</span>","width":100,"dataIndex":"pk_province","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>省份</span>","width":100,"dataIndex":"province_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>区域</span>","width":100,"dataIndex":"pk_area","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>区域</span>","width":100,"dataIndex":"area_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>详细地址</span>","width":200,"dataIndex":"detail_addr","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'><center>删除标识</center></span>","width":100,"dataIndex":"dr","xtype":"gridcolumn","hidden":true,"editable":false,"align":"right"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>pk_ent_line_b</span>","width":100,"dataIndex":"pk_ent_line_b","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>委托单</span>","width":100,"dataIndex":"pk_entrust","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>序号</span>","width":100,"dataIndex":"serialno","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>更新时间戳</span>","width":100,"dataIndex":"ts","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'><center>dr</center></span>","width":0,"dataIndex":"dr","xtype":"gridcolumn","hidden":true,"editable":false,"align":"right"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>ts</span>","width":0,"dataIndex":"ts","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>addr_flag</span>","width":0,"dataIndex":"addr_flag","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>pk_segment</span>","width":0,"dataIndex":"pk_segment","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>是否到货</span>","width":100,"dataIndex":"arrival_flag","xtype":"checkcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>是否系统节点</span>","width":100,"dataIndex":"segment_node","xtype":"checkcolumn","editable":false});
		
		com_tms_jf_ts_ent_line_b_detail_recordType = [];
		com_tms_jf_ts_ent_line_b_detail_columns = [];
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"pk_ent_line_b","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"addr_code","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"addr_name","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"pk_entrust","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"pk_ent_line_pack_b","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"addr_code","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"addr_name","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"goods_code","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"goods_name","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"num","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"volume","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"pack_num_count","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"plan_num","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"plan_pack_num_count","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"pack","type":"string"});
		com_tms_jf_ts_ent_line_b_detail_recordType.push({"name":"weight","type":"string"});
		
		

		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>pk_ent_line_b</span>","width":100,"dataIndex":"pk_ent_line_b","xtype":"gridcolumn","hidden":true,"editable":false,editor:{"xtype":"textfield","maxLength":50}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址编码</span>","width":100,"dataIndex":"addr_code","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址名称</span>","width":100,"dataIndex":"addr_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>委托单</span>","width":100,"dataIndex":"pk_entrust","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>pk_ent_line_pack_b</span>","width":100,"dataIndex":"pk_ent_line_pack_b","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>货品编码</span>","width":100,"dataIndex":"goods_code","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>货品名称</span>","width":200,"dataIndex":"goods_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>件数</span>","width":50,"dataIndex":"num","xtype":"gridcolumn",editor:{"xtype":"numberfield","maxLength":20}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>数量</span>","width":50,"dataIndex":"pack_num_count","xtype":"gridcolumn",editor:{"xtype":"numberfield","maxLength":20}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>包装</span>","width":50,"dataIndex":"pack","xtype":"gridcolumn",editor:{"xtype":"numberfield","maxLength":20}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>重量</span>","width":50,"dataIndex":"weight","xtype":"gridcolumn",editor:{"xtype":"numberfield","maxLength":20}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column'>体积</span>","width":50,"dataIndex":"volume","xtype":"gridcolumn",editor:{"xtype":"numberfield","maxLength":20}});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>计划件数</span>","width":100,"dataIndex":"plan_num","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_detail_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>计划数量</span>","width":100,"dataIndex":"plan_pack_num_count","xtype":"gridcolumn","editable":false});
		
		var entLineGrid = new uft.extend.grid.EditorGrid({
			id : 'ts_ent_line_b_1',
			pkFieldName : 'pk_ent_line_b',
			dataUrl : 'loadEntLineB.json',
			params : {pk_entrust:this.pk_entrust},
			border : true,
			isCheckboxSelectionModel : true,
			sm : new Ext.grid.RowSelectionModel(),
			singleSelect : false,
			isAddBbar : false,
			immediatelyLoad : true,
			loadMask : false,
			onlyValidateSelected:true,
			plugins : [new Ext.ux.plugins.GridValidator()],
			recordType : com_tms_jf_ts_ent_line_b_recordType,
			columns : com_tms_jf_ts_ent_line_b_columns
		});
	
		com_tms_jf_ts_ent_line_b_detail_recordType.unshift({name:'_processor',type:'string'});
		com_tms_jf_ts_ent_line_b_detail_columns.unshift({header:'操作',dataIndex:'_processor',hidden:true,width:30,renderer : this.processorRender});
		this.gridDetail = new uft.extend.grid.EditorGrid({
			id : 'ts_ent_line_pack_b',
			pkFieldName : 'pk_ent_line_pack_b',
			dataUrl : 'loadEntLinePackB.json',
			params : {pk_entrust:this.pk_entrust},
			border : true,
			sm : new Ext.grid.RowSelectionModel(),
			singleSelect : false,
			isAddBbar : false,
			loadMask : false,
			immediatelyLoad : false,
			onlyValidateSelected:true,
			plugins : [new Ext.ux.plugins.GridValidator()],
			recordType : com_tms_jf_ts_ent_line_b_detail_recordType,
			columns : com_tms_jf_ts_ent_line_b_detail_columns
		});
		
		var gridPanel = new Ext.Panel({
			layout : 'fit',
			region : 'center',
			frame : false,
			maskDisabled : true,
			border : false,
			items : [entLineGrid]
		});
		var items=[];
		if(gridPanel)
			items.push(gridPanel);
		var main = new Ext.Panel({
			layout : 'border',
			frame : false,
			border : false,
			maskDisabled : true,
			items : items
		});	
	
		if(entLineGrid){
			//存在已选表格，此时需要使用tabPanel
			this.tabPanel = new Ext.TabPanel({
				deferredRender : false,
				resizeTabs:false,
				border : false,
				maskDisabled : true,
				activeTab:0,
			    frame : false
			});
			this.tabPanel.add({
				title : "节点信息",
				border : false,
				frame : false,
				layout : 'fit',
				items : [main]
			});				
			//加入已选数据的tab
			this.tabPanel.add({
				title : "明细表",
				border : false,
				frame : false,
				layout : 'fit',
				items : [this.gridDetail]
			});
			if(entLineGrid){
				//如果存在grid，注册grid的选中和未选中事件
				this.registerGridRowEvent(entLineGrid);
			}
		}
		//注册编辑后事件
		entLineGrid.on({'afteredit':function(e){
			if(e.field == 'act_arri_date'){
				this.afterEditAct_arri_date(e.record,e.row);
			}else if(e.field == 'act_leav_date'){
				this.afterEditAct_leav_date(e.record,e.row);				
			}
		},'beforeedit':function(e){
			var arrival_flag = e.record.get('arrival_flag');
			if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){ //节点到货的行，不能编辑
				return false;
			}
		},scope:this});
		//注册编辑后事件
		this.gridDetail.on({'afteredit':function(e){
			if(e.field == 'act_arri_date'){
			//	this.afterEditAct_arri_date(e.record,e.row);
			//}else if(e.field == 'act_leav_date'){
			//	this.afterEditAct_leav_date(e.record,e.row);				
			}
		},'beforeedit':function(e){
			var arrival_flag = e.record.get('arrival_flag');
			if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){ //节点到货的行，不能编辑
				return false;
			}
		},scope:this});
		entLineGrid.getStore().on({'load':function(store,records){
			var len = records.length;
			for(var i=0;i<len;i++){
				var r = records[i];
				var arrival_flag = r.get('arrival_flag');
				if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){ //节点到货的行，加一个样式
					entLineGrid.getView().addRowClass(i, this.disabledRowClass);//增加一个样式，看起来是禁用的样式，表示已到货，不可编辑
				}
				var act_arri_date = r.get('act_arri_date');
				if(Utils.isBlank(act_arri_date)){
					r.set('act_arri_date',r.get('req_arri_date'));
				}
			}
			//第一个节点的‘实际到达时间、实际离开时间’默认等于委托单的‘要求提货时间’
			//最后一个节点的‘实际到达时间、实际离开时间’默认等于委托单的‘要求到货时间’
			//XXX 2014-10-7 节点的实际到达时间默认等于要求到达时间
//			var req_deli_date = this.currentRecord.get('req_deli_date');
//			var req_arri_date = this.currentRecord.get('req_arri_date');
//			if(len > 1){
//				if(Utils.isBlank(records[0].get('act_arri_date'))){
//					records[0].set('act_arri_date',req_deli_date);
//				}
////				if(Utils.isBlank(records[0].get('act_leav_date'))){
////					records[0].set('act_leav_date',req_deli_date);
////				}
//				if(Utils.isBlank(records[len-1].get('act_arri_date'))){
//					records[len-1].set('act_arri_date',req_arri_date);	
//				}
////				if(Utils.isBlank(records[len-1].get('act_leav_date'))){
////					records[len-1].set('act_leav_date',req_arri_date);
////				}
//			}
		},scope:this});
		
		uft.te.NodeArrivalWindow.superclass.constructor.call(this, {
			title : this.title||'节点到货',
			width : 850,
			height : 450,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [this.tabPanel],
			buttons : [{
					xtype : 'button',
					text : '确认',
					iconCls : 'btnYes',
					scope : this,
					handler : this.confirmArrival
				},{
					xtype : 'button',
					text : '反确认',
					iconCls : 'btnCancel',
					scope : this,
					handler:this.unconfirmArrival
				},new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();
					}
			})]
	    });
	},
	//编辑实际离开时间，必须是介于第一行的要求到达时间和第二行的要求到达时间之间
	afterEditAct_leav_date : function(record,row){
		//2014-09-18同一行的时间不比较了
//		var act_arri_date = record.get('act_arri_date');
//		if(typeof(act_arri_date) == 'string'){
//			act_arri_date = Date.parseDate(act_arri_date,this.DATETIME_FORMAT);
//		}
	//	var act_leav_date = record.get('act_leav_date');
//	if(!act_leav_date){
//			return;
//		}
//		if(typeof(act_leav_date) == 'string'){
//			act_leav_date = Date.parseDate(act_leav_date,this.DATETIME_FORMAT);
//		}
//		if(act_leav_date < act_arri_date){
//			uft.Utils.showWarnMsg('实际离开时间必须大于等于实际到达时间！');
//			record.set('act_leav_date',null);
//			return;
//		}
	},
	//编辑实际到达时间，如果存在要求离开时间，那么要求到达时间必须小于要求离开时间
	afterEditAct_arri_date : function(record,row){
		//2014-09-18同一行的时间不比较了
//		var act_arri_date = record.get('act_arri_date');
//		if(typeof(act_arri_date) == 'string'){
//			act_arri_date = Date.parseDate(act_arri_date,this.DATETIME_FORMAT);
//		}
//		var act_leav_date = record.get('act_leav_date');
//		if(typeof(act_leav_date) == 'string'){
//			act_leav_date = Date.parseDate(act_leav_date,this.DATETIME_FORMAT);
//		}
//		if(act_leav_date && act_leav_date < act_arri_date){
//			uft.Utils.showWarnMsg('实际到达时间必须小于等于实际离开时间！');
//			record.set('act_arri_date',null);
//			return;
//		}
	},
	/**
	 * 确认节点到货
	 */
	confirmArrival : function (){
		var params = app.newAjaxParams();
		//必须存在实际到货时间和实际离开时间
		var grid = Ext.getCmp('ts_ent_line_b_1');
		grid.stopEditing();
		var records = uft.Utils.getSelectedRecords(grid);
		if(!records){
			uft.Utils.showWarnMsg('请先选择记录！');
			return;
		}		
		if(!grid.isValid()) {//这里使用第三方插件进行验证
			errors = grid.getAllErrors();
			uft.Utils.showWarnMsg(errors);
			return;
		}
		var len = records.length,ds = grid.getStore();
		var datas = [];
		var dataGoods = [];
		for(var i=0;i<ds.getCount();i++){
			var record = ds.getAt(i);
			var frontRecord = null;
			if((i-1) >= 0){
				frontRecord = ds.getAt(i-1);//上一行
			}
			var checked = false,frCheck = false;
			for(var j=0;j<len;j++){
				if(record.id == records[j].id){
					checked = true;
				}
				if(frontRecord){
					if(records[j].id == frontRecord.id){
						frCheck = true;//上一行也是选中状态
					}
				}
			}
			if(!checked){
				//不是选中的行
				continue;
			}else{
				if(frontRecord && !frCheck){//选中的行，判断上一行是否是未选中并且是未确认的
					var arrival_flag = frontRecord.get('arrival_flag');
					if(String(arrival_flag)!='true' && String(arrival_flag)!='Y'){ //上一个节点还为到货
						uft.Utils.showWarnMsg('请先确认上一个节点！');
						return;
					}
				}
			}
			
			//将该行的数据发送到后台，这里需要对日期做特殊处理
			var act_arri_date = record.get('act_arri_date');
			if(act_arri_date instanceof Date){
				record.set('act_arri_date',act_arri_date.dateFormat(this.DATETIME_FORMAT));
			}
			var act_leav_date = record.get('act_leav_date');
			if(act_leav_date){
				if(act_leav_date instanceof Date){
					record.set('act_leav_date',act_leav_date.dateFormat(this.DATETIME_FORMAT));
				}
			}					
			if(frontRecord){
				//实际到达时间必须大于等于上一个节点的实际到达时间
				var f_act_arri_date = frontRecord.get('act_arri_date');//上一个节点的实际离开时间
				if(f_act_arri_date){
					if(typeof(f_act_arri_date) == 'string'){
						f_act_arri_date = Date.parseDate(f_act_arri_date,this.DATETIME_FORMAT);
					}
					var act_arri_date = record.get('act_arri_date');
					if(typeof(act_arri_date) == 'string'){
						act_arri_date = Date.parseDate(act_arri_date,this.DATETIME_FORMAT);
					}
					if(act_arri_date < f_act_arri_date){
						if(langague && langague == 'en_US'){
							uft.Utils.showWarnMsg('Line '+i+1+',Actual arrival time must be greater than or equal to the actual time of arrival of a node!');
						}else{
							uft.Utils.showWarnMsg('第'+(i+1)+'行的实际到达时间必须大于等于上一个节点的实际到达时间！');
						}
						
						return;
					}
				}
			}
			datas.push(record.data);
		}
		
		//增加货品明细数据
		var linePackgrid = Ext.getCmp('ts_ent_line_pack_b');
		linePackgrid.stopEditing();
		    var linePkgds = linePackgrid.getStore();
		    for(var i=0;i<linePkgds.getCount();i++){
			var record = linePkgds.getAt(i);
			dataGoods.push(record.data);
		    }
		 
		params['APP_POST_DATA'] = Ext.encode(datas);
		params['LINE_GOODS_DATA'] = Ext.encode(dataGoods);
		
		uft.Utils.doAjax({
			scope : this,
			params : params,
			url : ctxPath + '/te/tracking/confirmArrival.json',
			success : function(values){
				//更新行信息
				if(values && values.datas){
					var datas = values.datas;
					for(var i=0;i<datas.length;i++){
						var data = datas[i];//这里实际上还是数组，包括2个对象，第一个是委托单ＶＯ，第二个是EntLineBVO
						var entVO = data[0];
//						var entLineBVO = data[1];
//						for(var j=0;j<ds.getCount();j++){
//							var record = ds.getAt(j);
//							if(record.get('pk_ent_line_b') == entLineBVO['pk_ent_line_b']){
//								//找到这条记录
//								record.beginEdit();
//								var key;
//								for(key in entLineBVO){
//									record.set(key,entLineBVO[key]);
//								}
//								record.endEdit();
//								grid.getView().addRowClass(j, this.disabledRowClass);//增加一个样式，看起来是禁用的样式，表示已到货，不可编辑
//								break;
//							}
//						}
						var r = this.currentRecord;
						r.beginEdit();
						//委托单模块
						r.set('vbillstatus',entVO.vbillstatus);
						//在途跟踪模块
						r.set('tracking_status',entVO.tracking_status);
						r.set('act_deli_date',entVO.act_deli_date);
						r.set('act_arri_date',entVO.act_arri_date);
						r.endEdit();
					}
					grid.getStore().reload();
				}
			}
		});
	},
	/**
	 * 反确认
	 */
	unconfirmArrival : function (){
		//必须存在实际到货时间和实际离开时间
		var grid = Ext.getCmp('ts_ent_line_b_1');
		var records = uft.Utils.getSelectedRecords(grid);
		if(!records){
			uft.Utils.showWarnMsg('请先选择记录！');
			return;
		}		
		var len = records.length,ds = grid.getStore();
		var datas = [];
		for(var i=0;i<ds.getCount();i++){
			var record = ds.getAt(i);
			var nextRecord = null;
			if((i+1) < ds.getCount()){
				nextRecord = ds.getAt(i+1);//下一行
			}
			var checked = false,nrCheck = false;
			for(var j=0;j<len;j++){
				if(record.id == records[j].id){
					checked = true;
				}
				if(nextRecord){
					if(records[j].id == nextRecord.id){
						nrCheck = true;//下一行也是选中状态
					}
				}
			}
			if(!checked){
				//不是选中的行
				continue;
			}else{
				if(nextRecord && !nrCheck){//判断下一行是否是未选中并且是未确认的
					var arrival_flag = nextRecord.get('arrival_flag');
					if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){ //下一个节点还没有反确认
						uft.Utils.showWarnMsg('请先反确认下一个节点！');
						return;
					}
				}
			}
			
			//将该行的数据发送到后台，这里需要对日期做特殊处理
			var act_arri_date = record.get('act_arri_date');
			if(act_arri_date instanceof Date){
				record.set('act_arri_date',act_arri_date.dateFormat(this.DATETIME_FORMAT));
			}
			var act_leav_date = record.get('act_leav_date');
			if(act_leav_date){
				if(act_leav_date instanceof Date){
					record.set('act_leav_date',act_leav_date.dateFormat(this.DATETIME_FORMAT));
				}
			}					
			datas.push(record.data);
		}
		datas = datas.reverse();//反确认需要从后面的元素开始，这里颠倒顺序一下
		uft.Utils.doAjax({
			scope : this,
			params : {APP_POST_DATA : Ext.encode(datas)},
			url : ctxPath + '/te/tracking/unconfirmArrival.json',
			success : function(values){
				//更新行信息
				if(values && values.datas){
					var datas = values.datas;
					for(var i=0;i<datas.length;i++){
						var data = datas[i];//这里实际上还是数组，包括2个对象，第一个是委托单ＶＯ，第二个是EntLineBVO
						var entVO = data[0];
//						var entLineBVO = data[1];
//						for(var j=0;j<ds.getCount();j++){
//							var record = ds.getAt(j);
//							if(record.get('pk_ent_line_b') == entLineBVO['pk_ent_line_b']){
//								//找到这条记录
//								record.beginEdit();
//								var key;
//								for(key in entLineBVO){
//									record.set(key,entLineBVO[key]);
//								}
//								//重新设置默认值
//								var req_deli_date = this.currentRecord.get('req_deli_date');
//								var req_arri_date = this.currentRecord.get('req_arri_date');
//								if(j == 0){
//									//第一条记录
//									record.set('act_arri_date',req_deli_date);
//									record.set('act_leav_date',req_deli_date);
//								}else if(j == ds.getCount()-1){
//									//最后一条记录
//									record.set('act_arri_date',req_arri_date);
//									record.set('act_leav_date',req_arri_date);
//								}
//								record.endEdit();
//								grid.getView().removeRowClass(j, this.disabledRowClass);//删除之前加入的样式
//								break;
//							}
//						}
						var r = this.currentRecord;
						r.beginEdit();
						//委托单模块
						r.set('vbillstatus',entVO.vbillstatus);
						//在途跟踪模块
						r.set('tracking_status',entVO.tracking_status);
						r.set('act_deli_date',entVO.act_deli_date);
						r.set('act_arri_date',entVO.act_arri_date);
						r.endEdit();
					}
					grid.getStore().reload();
				}				
			}
		});
	},
	
	   //当存在已选数据Tab时，需要注册表格的行选择事件
    registerGridRowEvent : function(grid){
    	grid.getSelectionModel().on('rowselect',this._onGridRowSelect,this);
    	//反选
    	grid.getSelectionModel().on('rowdeselect',this._onGridRowDeselect,this);
    },
    
    //存在已选数据tab的情况下，选择行的事件
    _onGridRowSelect : function(sm, rowIndex, record){
    	//判断record是否已经存在
    	var ifExist = false;
    	var pk = record.get('pk_ent_line_b');
    	for(var i = 0;i< selectItems.length;i++){
    		selrecord = selectItems[i];
    		if(pk == selrecord.get('pk_ent_line_b')){
    			//记录已存在
				ifExist = true;
				break;
    		}
    	}
	
		if(!ifExist){
			selectItems.push(record);
    	//	store.add(record);
    		this._loadEntLinePackb(record);
    		this.registerImgClickEvent(record.id);
		}
    },
    //存在已选数据tab的情况下，反选行的事件
    _onGridRowDeselect : function(sm, rowIndex, record){
    	//判断record是否已经存在
    	var ifExist = false;
    	var pk =  record.get('pk_ent_line_b');
    	
    	for(var i = 0;i< selectItems.length;i++){
    		selrecord = selectItems[i];
    		if(pk == selrecord.get('pk_ent_line_b')){
    			//记录已存在
				ifExist = true;
				selectItems.remove(selrecord);
				break;
    		}
    	}
    	
		//删除加载的数据
		  var grid = Ext.getCmp('ts_ent_line_pack_b');
		  var store = grid.getStore();
		  for(var i=0;i<store.getCount();i++){
			var _curRecord = store.getAt(i);
			if(pk == _curRecord.get('pk_ent_line_b')){
				//记录已存在
				store.remove(_curRecord);
				i--;
			}
		}
		grid.getStore().reload();
    },
  
    //已选数据表格的删除行图标
    processorRender : function(value,meta,record){
    	var imgPath = resourceCtxPath+'/theme/'+Constants.csstheme+'/images/default/btn/delete.gif';
    	return "<div align='center'><img id='del_"+record.id+"' src='"+imgPath+"' width='16' border='0' class='h_img' title='删除' /></div>";
    },
    //注册删除小图标的事件
    registerImgClickEvent : function(id){
		var delCmp = Ext.get('del_'+id);
		if(delCmp){
			delCmp.on('click',function(event){
				for(var i=0;i<this.gridDetail.getStore().getCount();i++){
					var record = this.gridDetail.getStore().getAt(i);
					if(record.id == id){
						//同步更新其他表格的选中状态
						this.syncGridSelectedStatus(record);
						this.gridDetail.getStore().remove(record);
						//更新行号信息
						return;
					}
				}
			},this);
		}
    },
    //当删除已选表格的记录时，同步其他表格的选中状态
    syncGridSelectedStatus : function(record){
    	if(entLineGrid){
    		for(var i=0;i<entLineGrid.getStore().getCount();i++){
    			var _record =entLineGrid.getStore().getAt(i);
    			if(_record.get('pk_entrust') == record.get('pk_entrust')){
    				entLineGrid.getSelectionModel().deselectRow(i);
    				break;
    			}
    		}
    	}
    },
    //更新已选tab的title
    _loadEntLinePackb : function(record){
    	var params= app.newAjaxParams();
    	var pk_ent_line_b_value= record.get('pk_ent_line_b')
    	var addr_code_value = record.get('addr_code');
    	var addr_name_value = record.get('addr_name');
    	params['pk_ent_line_b'] =pk_ent_line_b_value;
    	
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	isTip : false,
	    	mask : false,
	    	url : 'loadEntLinePackB.json',
	    	success : function(values){
	    		if(values && values.records){
					var datas = values.records;
	    		    var grid = Ext.getCmp('ts_ent_line_pack_b');
	    		    var store = grid.getStore();
	    		    
	    		    for(var i=0;i<datas.length;i++){
	    		    var p = new Ext.data.Record({
	    		    	"pk_ent_line_b":pk_ent_line_b_value,"addr_code": addr_code_value,"addr_name": addr_name_value,"pk_entrust":datas[i].pk_entrust,
	    		    	"pk_ent_line_pack_b":datas[i].pk_ent_line_pack_b,"goods_code":datas[i].goods_code,"goods_name":datas[i].goods_name,"num":datas[i].num,
	    		     	"pack_num_count":datas[i].pack_num_count,"plan_num":datas[i].plan_num,"plan_pack_num_count":datas[i].plan_pack_num_count,
	    		     	"pack":datas[i].pack,"weight":datas[i].weight,"volume":datas[i].volume
	    	                });

	        			store.insert(i,p);
	        		}
	    		    grid.getStore().reload();
	    		}
	    	}   
	    });
    },    
});
