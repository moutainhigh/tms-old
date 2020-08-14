<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="true" bodyGridsSingleSelect="true" bodyGridsCheckboxSelectionModel="true"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BodyToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_edit.text='库内移动';
			},
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				return btns;
			},
			btn_edit_handler : function(){
				var ts_lot_qty = Ext.getCmp('ts_lot_qty');
				var count = ts_lot_qty.getStore().getCount();
				if(count == 0){
					uft.Utils.showWarnMsg('请先查询库存信息！');
					return false;
				}
				MyToolbar.superclass.btn_edit_handler.call(this);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.registerTopPanelStatusChange = function(){};//默认当切换到修改状态时，会隐藏查询框
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		
		var ts_lot_qty = Ext.getCmp('ts_lot_qty');
		Ext.apply(ts_lot_qty,{isValid:function(editInvalid) {
            var cols = this.colModel.getColumnCount();
            var rs = this.getSelectionModel().getSelections();
            var rows = rs.length;
            var r, c;
            var valid = true;
            for(r = 0; r < rows; r++) {
                for(c = 0; c < cols; c++) {
                    valid = this.isCellValid(c, r);
                    if(!valid) {
                        break;
                    }
                }
                if(!valid) {
                    break;
                }
            }
            if(editInvalid && !valid) {
                this.startEditing(r, c);
            }
            return valid;
        },getAllErrors:function(){
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
		
		afterEditBody = function(field,value,oriValue){
			if(field.id == 'move_num'){//编辑移动数量
				var available_num = uft.Utils.getNumberFieldValue('available_num');//可用数量
				var move_num = uft.Utils.getNumberFieldValue('move_num');//移动数量
				if(move_num <=0 || move_num > available_num){
					uft.Utils.showWarnMsg('移动数量必须大于0并且小于等于可用数量！');
					uft.Utils.setFieldValue('move_num',null);
					return;
				}
			}
		}
		${moduleName}.appUiConfig.toolbar.on('save',function(){
			//保存后，刷新
			ts_lot_qty.getStore().reload();
		},this);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
