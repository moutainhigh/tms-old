Ext.namespace('uft.te');
/**
 * 节点到货
 * @class uft.te.BatchNodeArrivalWindow
 * @extends Ext.Window
 */
uft.te.BatchNodeArrivalWindow = Ext.extend(Ext.Window, {
	currentRecords : null,//表头表格所选中的行
	DATETIME_FORMAT : "Y-m-d H:i:s",
	disabledRowClass : "x-grid3-row-selected-disable",
	constructor : function(config) {
		Ext.apply(this,config);
		this.form = new uft.extend.form.FormPanel({
			border : true,
			autoScroll : true,
			padding : '5px 0 0 0',
			items: this.buildFieldset()
		});
		uft.te.BatchNodeArrivalWindow.superclass.constructor.call(this, {
			title : this.title||'批量节点到货',
			width : 850,
			height : 460,
			collapsible : false,
			frame : true,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			items : [this.form],
			buttons : [{
					xtype : 'button',
					text : '全部确认',
					iconCls : 'btnYes',
					scope : this,
					handler : this.confirmArrival
				},{
					xtype : 'button',
					text : '全部反确认',
					iconCls : 'btnCancel',
					scope : this,
					handler : this.unconfirmArrival
				},{
					xtype : 'button',
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();
					}
			}]
	    });
	},
	//根据选中的委托单记录，生成一个个fieldset
	buildFieldset : function(){
		var fsAry = [];
		var cr = this.currentRecords;
		for(var i=0;i<cr.length;i++){
			var record = cr[i],vbillno=record.get('vbillno');
			var fieldset = {
				height : 185,
				xtype : 'fieldset',
				collapsible : true,
				title : '对单据'+vbillno+'节点到货',
				layout : 'fit',
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%'
				},
				items : [this.buildGrid(record)]
			};
			fsAry.push(fieldset);
		}
		return fsAry;
	},
	buildGrid : function(_r){
		var vbillno=_r.get('vbillno'),pk_entrust=_r.get('pk_entrust')
		var gridId = 'ts_ent_line_b_'+vbillno;
		
		com_tms_jf_ts_ent_line_b_recordType = [];
		com_tms_jf_ts_ent_line_b_columns = [];
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"operate_type","type":"int"});
		com_tms_jf_ts_ent_line_b_recordType.push({"name":"pk_address","type":"string"});
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
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>操作类型</span>","width":100,"dataIndex":"operate_name","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址</span>","width":100,"dataIndex":"pk_address","xtype":"gridcolumn","hidden":true,"editable":false,editor:{"xtype":"textfield","maxLength":50}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-null'>实际到达时间</span>","width":110,"dataIndex":"act_arri_date","xtype":"datetimecolumn",editor:{"xtype":"datetimefield",allowBlank:false,"maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>实际离开时间</span>","width":110,"dataIndex":"act_leav_date","xtype":"datetimecolumn",editor:{"xtype":"datetimefield","maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-null'>要求到达时间</span>","width":120,"dataIndex":"req_arri_date","xtype":"datetimecolumn","hidden":true});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-null'>要求离开时间</span>","width":120,"dataIndex":"req_leav_date","xtype":"datetimecolumn","hidden":true});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>备注</span>","width":120,"dataIndex":"memo","xtype":"gridcolumn",editor:{"xtype":"textfield","maxLength":200}});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column'>地址编码</span>","width":60,"dataIndex":"addr_code","xtype":"refcolumn"});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>地址名称</span>","width":100,"dataIndex":"addr_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>城市</span>","width":80,"dataIndex":"pk_city","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>城市</span>","width":80,"dataIndex":"city_name","xtype":"gridcolumn","editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>省份</span>","width":60,"dataIndex":"pk_province","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>省份</span>","width":60,"dataIndex":"province_name","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>区域</span>","width":60,"dataIndex":"pk_area","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>区域</span>","width":60,"dataIndex":"area_name","xtype":"gridcolumn","hidden":true,"editable":false});
		com_tms_jf_ts_ent_line_b_columns.push({"header":"<span class='uft-grid-header-column-not-edit'>详细地址</span>","width":170,"dataIndex":"detail_addr","xtype":"gridcolumn","editable":false});
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

		var entLineGrid = new uft.extend.grid.EditorGrid({
			id : gridId,
			border : true,
			pkFieldName : 'pk_ent_line_b',
			dataUrl : 'loadEntLineB.json',
			params : {pk_entrust:pk_entrust},
			isCheckboxSelectionModel : false,
			sm : new Ext.grid.RowSelectionModel(),
			singleSelect : false,
			isAddBbar : false,
			immediatelyLoad : true,
			plugins : [new Ext.ux.plugins.GridValidator()],
			recordType : com_tms_jf_ts_ent_line_b_recordType,
			columns : com_tms_jf_ts_ent_line_b_columns
		});
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
//			var req_deli_date = _r.get('req_deli_date');
//			var req_arri_date = _r.get('req_arri_date');
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
		return entLineGrid;
	},
	//编辑实际离开时间，必须是介于第一行的要求到达时间和第二行的要求到达时间之间
	afterEditAct_leav_date : function(record,row){
		//2014-09-18同一行的时间不比较了
//		var act_arri_date = record.get('act_arri_date');
//		if(typeof(act_arri_date) == 'string'){
//			act_arri_date = Date.parseDate(act_arri_date,this.DATETIME_FORMAT);
//		}
//		var act_leav_date = record.get('act_leav_date');
//		if(!act_leav_date){
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
	getAllRecords : function(grid){
		var arr = [],ds = grid.getStore(),len=ds.getCount();
		for(var i=0;i<len;i++){
			arr.push(ds.getAt(i));
		}
		return arr;
	},
	/**
	 * 确认节点到货
	 */
	confirmArrival : function (){
		var _cr = this.currentRecords;
		var datas = [];
		for(var k=0;k<_cr.length;k++){
			var _r = _cr[k],vbillno=_r.get('vbillno');
			var grid = Ext.getCmp('ts_ent_line_b_'+vbillno);
			grid.stopEditing();
			//必须存在实际到货时间和实际离开时间
			var records = this.getAllRecords(grid);
			if(!records){
				continue;
			}	
			if(!grid.isValid()) {//这里使用第三方插件进行验证
				errors = grid.getAllErrors();
				uft.Utils.showWarnMsg(errors);
				return;
			}
			var len = records.length,ds = grid.getStore();
			for(var i=0;i<ds.getCount();i++){
				var record = ds.getAt(i);
				var frontRecord = null;
				if((i-1) >= 0){
					frontRecord = ds.getAt(i-1);//上一行
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
							if(!language && language == 'en_US'){
								uft.Utils.showWarnMsg('第'+(i+1)+'行的实际到达时间必须大于等于上一个节点的实际到达时间！');
							}else{
								uft.Utils.showWarnMsg('Line '+(i+1)+',Actual arrival time must be greater than or equal to the actual time of arrival of a node!');
							}
							return;
						}
					}
				}
				var arrival_flag = record.get('arrival_flag');
				if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){
				}else{
					//只加入未到货的节点
					datas.push(record.data);
				}
			}
		}
		if(datas.length == 0){
			uft.Utils.showWarnMsg('没有记录需要确认！');
			return;
		}
		Ext.Msg.confirm(uft.Internal.getText('询问'), uft.Internal.getText('你确定要[批量确认]吗？'), function(btn) {
			if (btn == 'yes') {
				uft.Utils.doAjax({
					scope : this,
					params : {APP_POST_DATA : Ext.encode(datas)},
					url : ctxPath + '/te/tracking/confirmArrival.json',
					success : function(values){
						if(values && values.append){
							//uft.Utils.showWarnMsg(values.append);
							Ext.MessageBox.show({
							    title:"警告",
							    msg:values.append,
							    icon:Ext.MessageBox.WARNING,
							    modal:true,
        						buttons:Ext.Msg.OK
							});	
						}
						var _cr = this.currentRecords;
						for(var k=0;k<_cr.length;k++){
							var _r = _cr[k],_vbillno=_r.get('vbillno');
							var grid = Ext.getCmp('ts_ent_line_b_'+_vbillno);
							grid.getStore().reload();
						}
//						if(values && values.datas){
//							var pkMap = {},pkMap1={};
//							for(var i=0;i<values.datas.length;i++){
//								var data = values.datas[i];
//								var entLineVO = data[1];//路线信息放在第二个
//								var pk_entrust = entLineVO['pk_entrust'];
//								var pk_ent_line_b = entLineVO['pk_ent_line_b'];
//								pkMap[pk_entrust+pk_ent_line_b] = pk_ent_line_b;//定位到委托单的某个节点
//								pkMap1[pk_entrust] = pk_ent_line_b;//定位到一条委托单记录
//							}							
//							for(var k=0;k<_cr.length;k++){
//								var _r = _cr[k],_vbillno=_r.get('vbillno'),_pk_entrust=_r.get('pk_entrust');
//								var pk = pkMap1[_pk_entrust];
//								if(pk){
//									var grid = Ext.getCmp('ts_ent_line_b_'+_vbillno);
//									var ds = grid.getStore(),len = ds.getCount();
//									for(var i=0;i<len;i++){
//										var record = ds.getAt(i);
//										var pk_ent_line_b = record.get('pk_ent_line_b');
//										var newPK = pkMap[_pk_entrust+pk_ent_line_b];
//										if(newPK == pk_ent_line_b){
//											record.set('arrival_flag','Y');
//											grid.getView().addRowClass(i, this.disabledRowClass);
//										}
//									}
//								}
//							}
//						}
					}
				});
			}
		},this);
	},
	/**
	 * 反确认
	 */
	unconfirmArrival : function (){
		var _cr = this.currentRecords;
		var datas = [];
		for(var k=0;k<_cr.length;k++){
			var _r = _cr[k],vbillno=_r.get('vbillno');
			var grid = Ext.getCmp('ts_ent_line_b_'+vbillno);
			//必须存在实际到货时间和实际离开时间
			var records = this.getAllRecords(grid);
			if(!records){
				continue;
			}
			var len = records.length,ds = grid.getStore();
			for(var i=0;i<ds.getCount();i++){
				var record = ds.getAt(i);
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
				var arrival_flag = record.get('arrival_flag');
				if(String(arrival_flag)=='true' || String(arrival_flag)=='Y'){
					datas.push(record.data);
				}
			}
		}
		if(datas.length == 0){
			uft.Utils.showWarnMsg('没有记录需要反确认！');
			return;
		}
		datas = datas.reverse();//反确认需要从后面的元素开始，这里颠倒顺序一下
		Ext.Msg.confirm(uft.Internal.getText('询问'), uft.Internal.getText('你确定要[批量反确认]吗？'), function(btn) {
			if (btn == 'yes') {
				uft.Utils.doAjax({
					scope : this,
					params : {APP_POST_DATA : Ext.encode(datas)},
					url : ctxPath + '/te/tracking/unconfirmArrival.json',
					success : function(values){
						if(values && values.append){
							Ext.MessageBox.show({
							    title:uft.Internal.getText('警告'),
							    msg:values.append,
							    icon:Ext.MessageBox.WARNING,
							    modal:true,
        						buttons:Ext.Msg.OK
							});							
						}
						var _cr = this.currentRecords;
						for(var k=0;k<_cr.length;k++){
							var _r = _cr[k],_vbillno=_r.get('vbillno');
							var grid = Ext.getCmp('ts_ent_line_b_'+_vbillno);
							grid.getStore().reload();
						}
//						if(values && values.datas){
//							var pkMap = {},pkMap1={};
//							for(var i=0;i<values.datas.length;i++){
//								var data = values.datas[i];
//								var entLineVO = data[1];//路线信息放在第二个
//								var pk_entrust = entLineVO['pk_entrust'];
//								var pk_ent_line_b = entLineVO['pk_ent_line_b'];
//								pkMap[pk_entrust+pk_ent_line_b] = pk_ent_line_b;//定位到委托单的某个节点
//								pkMap1[pk_entrust] = pk_ent_line_b;//定位到一条委托单记录
//							}							
//							for(var k=0;k<_cr.length;k++){
//								var _r = _cr[k],_vbillno=_r.get('vbillno'),_pk_entrust=_r.get('pk_entrust');
//								var pk = pkMap1[_pk_entrust];
//								if(pk){
//									var grid = Ext.getCmp('ts_ent_line_b_'+_vbillno);
//									var ds = grid.getStore(),len = ds.getCount();
//									for(var i=0;i<len;i++){
//										var record = ds.getAt(i);
//										var pk_ent_line_b = record.get('pk_ent_line_b');
//										var newPK = pkMap[_pk_entrust+pk_ent_line_b];
//										if(newPK == pk_ent_line_b){
//											record.set('arrival_flag','N');
//											grid.getView().removeRowClass(i, this.disabledRowClass);
//										}
//									}
//								}
//							}
//						}	
					}
				});
			}
		},this);
	}		
});
