<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<title>批量排单</title>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" isBuildHeaderGrid="false" bodyGridsPagination="false,false" 
	bodyGridsDataUrl="loadSegmentByPKs.json,loadEntTransbilityB.json" 
	bodyGridsDragDropRowOrder="true,false" bodyGridsSortable="true,true" />	
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				return btns;
			},
			btn_can_handler : function(){
				window.close();
			},
			//btn_save_handler : function(){
			//	window.close();
			//},
			getAppParams : function(config){
				//发送所有数据，而不是只有改变的
				return MyToolbar.superclass.getAppParams.call(this,{bodyGridOnlyModify:false})
			}
		});
		//辅助工具栏
		MyBodyAssistToolbar = Ext.extend(uft.jf.BodyAssistToolbar, {
			btn_row_add_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_add_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_del_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_del_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_ins_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_ins_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			btn_row_pas_handler : function(){
				MyBodyAssistToolbar.superclass.btn_row_pas_handler.call(this);
				afterEditBodyAssistToolbar();
			},
			getRowPasDefaultValue : function(gridId,selectRecordValue){
				var value = MyBodyAssistToolbar.superclass.getRowPasDefaultValue.call(this,gridId,selectRecordValue);
				value['segment_node'] = null;
				value['addr_flag'] = null;
				value['pk_segment'] = null;
				value['serialno'] = null;
				return value;
			}
		});		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bodyAssistToolbar = new MyBodyAssistToolbar();
		${moduleName}.appUiConfig.initPageStatus = uft.jf.pageStatus.OP_EDIT;
		${moduleName}.appUiConfig.ajaxLoadDefaultValue = false;//不需要去读取默认值
		${moduleName}.appUiConfig.bottomBar = true;
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		//设置表头的默认值，如选择了车辆，那么需要带出车辆的信息
		var headerMap = Ext.decode('${headerMap}');
		if(headerMap){
			var key;
			for(key in headerMap){
				uft.Utils.getField(key).setValue(headerMap[key]);
			}
		}
		
		var args = ${param.pk_segmentAry}; //这里是所选运段,从url中读取
		var segmentGrid = Ext.getCmp('ts_segment'); //这里实际上是表体第一个页签
		//加载运段表格的数据
		var options={};
		options.params={};
		options.params['pk_segment']=args;
		segmentGrid.getStore().reload(options);
		var entTransbilityGrid = Ext.getCmp('ts_ent_transbility_b');
		entTransbilityGrid.getStore().reload(options);
		
		//编辑要求提货日期，比较下提货日期和收货日期
		function afterChangeReq_deli_date(field,value,originalValue){
			var value_date = Date.parseDate(value,"Y-m-d H:i:s");
			var req_arri_date = Ext.getCmp('req_arri_date').getValue();
			if(value_date && req_arri_date){
				if(value_date > req_arri_date){
					uft.Utils.showWarnMsg('要求提货日期不能大于要求收货日期！');
					field.setValue(originalValue);
					return;
				}
			}
			//将所有包装明细的的时间都改掉
			var grid = Ext.getCmp('ts_segment'),store = grid.getStore(),count = store.getCount();
			for(var i=0;i<count;i++){
				var record = store.getAt(i);
				uft.Utils.setColumnValue(record,"req_deli_date",value);
			}
		}
		//编辑要求收货日期，比较下提货日期和收货日期
		function afterChangeReq_arri_date(field,value,originalValue){
			var value_date = Date.parseDate(value,"Y-m-d H:i:s");
			var req_deli_date = Ext.getCmp('req_deli_date').getValue();
			if(value_date && req_deli_date){
				if(value_date < req_deli_date){
					uft.Utils.showWarnMsg('要求收货日期不能小于要求提货日期！');
					field.setValue(originalValue);
					return;
				}
			}
			//将所有包装明细的的时间都改掉
			var grid = Ext.getCmp('ts_segment'),store = grid.getStore(),count = store.getCount();
			for(var i=0;i<count;i++){
				var record = store.getAt(i);
				uft.Utils.setColumnValue(record,"req_arri_date",value);
			}
		}
		
		
		function afterChangeCarno(field,value,originalValue){
			//这个获取方式和前台模板相关联，所以模板字段顺序不能随便更改
			//this.app.headerCard.items.items[2].extendParams.carno = value;//司机档案
		}
			
		function afterChangeCarrier(field,value,originalValue){
			//这个获取方式和前台模板相关联，所以模板字段顺序不能随便更改
			this.app.headerCard.items.items[0].extendParams.pk_carrier = value;//车辆档案
			this.app.headerCard.items.items[2].extendParams.pk_carrier = value;//司机档案
		}
		
		${moduleName}.appUiConfig.toolbar.on({
			'save':function(toolbar,datas){
				if(window.opener && window.opener.app){
					window.opener.app.headerGrid.getStore().reload();
					window.opener.refreshTrees();
				}
				//保存成功后,关闭窗口
				window.close();
			},
			'beforesave':function(toolbar,params){
				//保存之前校验
				//校验相同发货单的不同运段（发货单做了分段或者分量）不能一起配载
				var grid = Ext.getCmp('ts_segment'),ds = grid.getStore(),count = ds.getCount();
				var vs = [];
				for(var i=0;i<count;i++){
					var r = ds.getAt(i);
					var invoice_vbillno = r.get('invoice_vbillno');
					var exist = false;
					for(var j=0;j<vs.length;j++){
						if(vs[j] == invoice_vbillno){
							exist = true;
							break;
						}
					}
					if(!exist){
						vs.push(invoice_vbillno);
					}else{
						var ifMergeSameInvoice = '<%=org.nw.utils.ParameterHelper.getIfMergeSameInvoice()%>';
						if(ifMergeSameInvoice && ifMergeSameInvoice == 'N'){
							uft.Utils.showWarnMsg('相同发货单的运段不能一起排单！');
							return false;
						}
					}
				}
				//return checkEntLineB();
				return true;
		},scope:this});
		
		//当切换到货物信息tab时，禁用辅助工具栏
		app.bodyAssistToolbar.setDisabled(true); //默认打开的就是货物信息tab，所以默认禁用辅助工具栏
		var tabPanel = app.getBodyTabPanel();
		tabPanel.addListener('tabchange',function(tabPanel,activePanel){
			if(activePanel.id=='ts_segment'){ //货物信息tab
				app.bodyAssistToolbar.setDisabled(true);
			}else{
				app.bodyAssistToolbar.setDisabled(false);
			}
		});
		
		//展开运段明细
		function expandSegment(){
			var record = segmentGrid.getSelectedRow(); //注意表体的选中行的读取方式
			if(record){
				var pk_segment = record.get(segmentGrid.pkFieldName);
				var url = ctxPath+'${segPackUrl}';
				url += "&pk_segment="+pk_segment;
				openDialog(url,null);
			}
		}
		//设置表体第一列的渲染函数
		function processImg(){
			return "<div align='center'><img src='"+ctxPath+"/images/expand.gif' border=0 onclick='expandSegment()' style='cursor:pointer'>";
		};
		var processorColumn = uft.Utils.getColumn(segmentGrid,'_processor');
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
		var transFeeCode = 'ET10';
		
		//监听关闭事件，如果关闭，那么父窗口的表格重新刷新
		//2015-05-29只有点击保存以后才进行刷新
// 		window.onunload = function(){
// 			if(window.opener && window.opener.app){
// 				window.opener.app.headerGrid.getStore().reload();
// 			}
// 		}
	</script>
	<!-- <script type="text/javascript" src='<c:url value="/busi/te/EntrustEdit.js?v=${version}" />'></script> -->
	<%@ include file="/common/footer.jsp"%>
</html>