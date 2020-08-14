<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<script>
		window.checkLeave = false;
		</script>
		<%@ include file="/common/header.jsp"%>
		<title>查询库存</title>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" isShowTab="true" useFieldSetInHeader="true" isBuildHeaderGrid="false" bodyGridsPagination="true" bodyGridsImmediatelyLoad="false" bodyGridsCheckboxSelectionModel="true" bodyGridsSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				return btns;
			},
			btn_can_handler : function(){
				window.close();
			},
			btn_save_handler : function(){
				var ts_storage_ajust_b = Ext.getCmp('ts_storage_ajust_b');
				var records = uft.Utils.getSelectedRecords(ts_storage_ajust_b);
				if(records == null || records.length == 0){
					uft.Utils.showWarnMsg('请先选择库存记录！');
					return false;
				}
				MyToolbar.superclass.btn_save_handler.call(this);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bottomBar = true;
		${moduleName}.appUiConfig.headerHeight = 110;
		${moduleName}.appUiConfig.showBodyTabHeader = false;
		${moduleName}.appUiConfig.showbodyAssistToolbar = false;
		${moduleName}.appUiConfig.initPageStatus=uft.jf.pageStatus.OP_EDIT;
		${moduleName}.appUiConfig.registerTopPanelStatusChange = function(){};//默认当切换到修改状态时，会隐藏查询框
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		var tqf = app.topQueryForm.getForm();//查询区域
		var pk_customer = tqf.findField('ts_lot_qty.pk_customer');
		pk_customer.on('change',function(field,value,originalValue){
			Ext.getCmp('src_customer').setValue(value);
		},this);
		
		var ts_storage_ajust_b = Ext.getCmp('ts_storage_ajust_b');
		Ext.apply(ts_storage_ajust_b,{getAllErrors:function(){
        	var errors = new Array();
            var cols = this.colModel.getColumnCount();
            var rs = this.getSelectionModel().getSelections();
            var r, c;
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
		
		afterEditBody = function(e){
			var f = e.field,r = e.record;
			if(f == 'ajust_num'){//编辑调整数量
				var ajust_num = uft.Utils.getNumberColumnValue(r,'ajust_num');
				var available_num = uft.Utils.getNumberColumnValue(r,'available_num');
				if(ajust_num <=0 || ajust_num > available_num){
					uft.Utils.showWarnMsg('调整数量必须大于0，小于等于可用数量！');
					r.set('ajust_num',null);
					return;
				}
			}
		}
		
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
