<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript">
		//不同的单据状态使用不同的颜色
		function vbillstatusBeforeRenderer(value,meta,record){
			meta.css = 'css'+value;
			if(value ==96 || value == 98){
				meta.style+='color:#fff;';
			}
		}
		function est_arri_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus < 1){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		function act_arri_dateBeforeRenderer(value,meta,record){
			if(value==undefined || value=='')
				return "";
			var date = Date.parseDate(value,"Y-m-d H:i:s");
			var vbillstatus = uft.Utils.getNumberColumnValue(record,'vbillstatus');
			var curDate = new Date();
			if(date < curDate && vbillstatus <= 92){
				meta.css='cssRed';
				meta.style+='color:#fff;';
			}
		}
		</script>
		<style type="text/css">
			.css0{
				background-color: #92D050;
			}
			.css92{
				background-color: #FAC090;
			}
			.css94{
				background-color: #EBD6D6;
			}
			.css96{
				background-color: #7030A0;
			}
			.css98{
				background-color: #44964C;
			}
			.cssRed{
				background-color: #FF0000;
			}
		</style>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" headerGridPageSizePlugin="true"
		bodyGridsPagination="false" 
		bodyGridsDataUrl="loadData.json" bodyGridsCheckboxSelectionModel="true,false" bodyGridsSingleSelect="false,true" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_edit.text = '收货';
				this.btn_edit.enabledBizStatus=[uft.jf.bizStatus.NEW,uft.jf.bizStatus.INSTO_PART_REC];
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_edit);
				btns.push(new uft.extend.Button({
					text : '全部收货',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
						if(records){
							var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
							var params=this.app.newAjaxParams();
							params[this.app.getBillIdField()]=ids;
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : params,
						    	isTip : true,
						    	method : 'GET',
						    	url : 'receiptAll.json',
						    	success : function(values){
						    		//全部收货后，刷新交易表
						    		this.app.reloadBodyGrids();
						    		
						    		this.app.setHeaderValues(records,values.datas);
						    		if(values.datas&&values.datas.length>0){
						    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
						    		}
						    		this.app.statusMgr.updateStatus();
						    		if(values.append){
						    			uft.Utils.showWarnMsg(values.append);
						    		}
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
					enabledBizStatus:[uft.jf.bizStatus.NEW,uft.jf.bizStatus.INSTO_PART_REC]
				}));
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_export);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		//表体的编辑后事件
		function afterEditBody(e){
			var f = e.field,r = e.record,row=e.row;
			if(f == 'accept_count'){
				afterEditAccept_count(r,row);
			}else if(f == 'volume'){
				afterEditVolume(r);
			}else if(f == 'weight'){
				afterEditWeight(r);
			}else if(f == 'produce_date'){
				afterEditProduce_date(r,e.value);
			}
		}
		//更新表头的包装类别的统计信息，包括总件数、总重量、总体积
		function updateHeaderSummary(){
			var resultMap = uft.Utils.getGridSumValueMap('ts_instorage_b',['weight','volume'])
			uft.Utils.getField('weight_count').setValue(resultMap['weight']);
			uft.Utils.getField('volume_count').setValue(resultMap['volume']);
		}	
		//更新体积时，更新汇总信息
		function afterEditVolume(record){
			updateHeaderSummary();
		}
		//更新重时，更新汇总信息
		function afterEditWeight(record){
			updateHeaderSummary();
		}
		//编辑接收量时，更新汇总信息
		function afterEditAccept_count(record,row){
			var order_count = uft.Utils.getNumberColumnValue(record,'order_count');
			var accepted_count = uft.Utils.getNumberColumnValue(record,'accepted_count');
			var accept_count = uft.Utils.getNumberColumnValue(record,'accept_count');
			if(accept_count <=0){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',The amount received cannot be less than or equal to 0!');
				}else{
					uft.Utils.showWarnMsg('第' + (row+1) + '行的接收量不能小于等于0！');
				}
				record.set('accept_count',null);
				return;
			}
			if(accept_count > (order_count-accepted_count)){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',The amount received is not greater than the order quantity minus the received amount!');
				}else{
					uft.Utils.showWarnMsg('第' + (row+1) + '行的接收量不能大于订单量减去已接收量！');
				}
				
				record.set('accept_count',null);
				return;
			}
		}
		//编辑生产日期，自动计算失效日期
		function afterEditProduce_date(record,value){
			var pk_goods = record.get('pk_goods');
			if(pk_goods){
				uft.Utils.doAjax({
			    	scope : this,
			    	method : 'POST',
			    	url : 'getExpireDate.json',
			    	isTip : false,
			    	params : {pk_goods:pk_goods,produce_date:value},
			    	success : function(values){
			    		if(values && values.data){
			    			record.set('expire_date',values.data);
			    		}
			    	}
				});
			}
		}
		
		//如果没有选中这一行，那么不需要检测这一行的必输项，这里重写验证方法
		var ts_instorage_b = Ext.getCmp('ts_instorage_b');
		Ext.apply(ts_instorage_b,{getAllErrors:function(){
        	var errors = new Array();
            var cols = this.colModel.getColumnCount();
            var rs = this.getSelectionModel().getSelections();
            var r, c;
            var valid = true;
            for(r = 0; r < rs.length; r++) {
            	var record = rs[r];
            	var index = this.store.indexOf(record);
                for(c = 0; c < cols; c++) {
                	var ed = this.colModel.getCellEditor(c, r);
                	if(!ed){
                		continue;
                	}
	                if(!record) {
	                    continue;
	                }
	                var field = this.colModel.getDataIndex(c);
            		ed.field.setValue(record.data[field]);
            		var errors1=ed.field.getErrors();
            		if(errors1.length>0){
            			errors.push('第'+(index+1)+'行['+this.colModel.columns[c].header+'],'+errors1[0]+'<br/>')
            		}
                }
            }
            return errors;
        },getModifyValue : function(){
       		var toUpt = new Array();
       		var toDel = new Array();
       		var modified = this.getStore().modified;
       		var rs = this.getSelectionModel().getSelections();
       		if(modified){
       			for(var i=0;i<modified.length;i++){
       				var r = modified[i];
       				//如果这一行没有选中，那么不加入最后要修改的数据里边
       				var exist = false;//判断这一行是否在选中的行里边
       				for(var j=0;j<rs.length;j++){
       					if(r.id == rs[j].id){
       						exist = true;
       						break;
       					}
       				}
       				if(exist){
           				//格式化日期
           	    		for(var key in modified[i].data){
           	    			if(modified[i].data[key] instanceof Date){
           	    				//目前系统支持日期+时间格式的控件，可能会有不同的日期格式了，不能定死了
           	    				var column = uft.Utils.getColumn(this,key);
           	    				var value = modified[i].data[key].dateFormat(column.format||'Y-m-d');
           	    				modified[i].set(key,value);
           	    			}
           	    		}
           				if(modified[i].store){
           					//修改的记录
           					toUpt.push(modified[i].data);
           				}else{
           					//删除的记录，但是必须是存在pk的才能记入真正被删除的
           					if(modified[i].get(this.getPkFieldName())){
           						toDel.push(modified[i].data);
           					}
           				}
       				}
       			}
       		}
           	return {"delete":toDel,"update":toUpt};
        }});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
