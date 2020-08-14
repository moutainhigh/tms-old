<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="true" bodyGridsImmediatelyLoad="false" bodyGridsCheckboxSelectionModel="true" bodyGridsSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BodyToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
				this.btn_save.enabledStatus=[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST];
				this.btn_can.enabledStatus=[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST];
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
			btn_save_handler : function() {
				if(this.app.hasBodyGrid()){
					var grid=this.app.getActiveBodyGrid();
					if(typeof(grid.stopEditing) == "function"){
						grid.stopEditing();
					}			
				}
		    	var params=this.app.newAjaxParams();
				params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(this.getAppParams());
				params['pk_outstorage_b'] = '${pk_outstorage_b}';
				uft.Utils.doAjax({
			    	scope : this,
			    	url : this.saveUrl||'save.json',
			    	params : params,
			    	success : function(values){
			    		this.doAfterSave(values);
			    	}
			    });
			},			
			doAfterSave : function(values){
				this.app.reloadBodyGrids();//重新刷新表体
				if(values.data){
					var d = values.data;
					setPickCount(d['picked_count']);
				}
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		${moduleName}.appUiConfig.bottomBar = true;
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		var childVO = Ext.decode('${childVO}');//查询到的出库单子表的一行记录
		var tqf = app.topQueryForm.getForm();//查询区域
		
		Ext.apply(app.topQueryForm,{
		doQuery : function(){
			if(this.getForm().isValid()){
				var values = this.getForm().getFieldValues(false);
				var params = [];
				//组装成[{fieldName:'appCode',condition:'=',value:'1'},{}]格式发送到后台
				for(var key in values){
					if(key == 'ts_lot_qty.order_count' || key == 'ts_lot_qty.picked_count' || key == 'ts_lot_qty.unpick_count'){
						continue;
					}
					var value = values[key];
					if(!value || String(value).trim().length == 0){
						continue;
					}
					var param = {};
					param.fieldName = key;
					param.condition = '=';
					param.value = value;
					params.push(param);
				}
				//加入客户条件
				var param = {fieldName:'ts_lot_qty.pk_customer',
							condition : '=',
							value : childVO.pk_customer};
				params.push(param);
				this.reloadGrid(Ext.encode(params));
			}
		},
		doReset : function(){
			var form = this.getForm();
			form.items.each(function(f){
				if(f.name == 'ts_lot_qty.pk_goods' || f.name == 'ts_lot_qty.pk_customer' || f.name == 'ts_lot_qty.order_count' 
					|| f.name == 'ts_lot_qty.picked_count' || f.name == 'ts_lot_qty.unpick_count'){
					
				}else{
					f.reset();
				}
	        });
	        return this;
		}});
		var ts_lot_qty = Ext.getCmp('ts_lot_qty');
		Ext.apply(ts_lot_qty,{getAllErrors:function(){
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
		
		afterEditBody = function(e){
			var f = e.field,r = e.record,row=e.row;
			if(f == 'pick_num'){//编辑分配数量
				var pick_num = uft.Utils.getNumberColumnValue(r,'pick_num');
				var available_num = uft.Utils.getNumberColumnValue(r,'available_num');
				var unpick_count = tqf.findField('ts_lot_qty.unpick_count').getValue()||0;
				if(unpick_count < available_num){
					if(pick_num <=0 || pick_num > unpick_count){
						uft.Utils.showWarnMsg('分配数量必须大于0，小于等于待分配数量！');
						r.set('pick_num',null);
						return;
					}
				}else{
					if(pick_num <=0 || pick_num > available_num){
						uft.Utils.showWarnMsg('分配数量必须大于0，小于等于可用数量！');
						r.set('pick_num',null);
						return;
					}
				}
			}
		}
		
		//设置分配数量列可编辑
		function setColumnEditable(){
			var pick_num = uft.Utils.getColumn(Ext.getCmp('ts_lot_qty'),'pick_num');
			pick_num.editable = true;
		}
		//设置分配数量和未分配数量
		function setPickCount(picked_count,shiped_count){
			var order_count = tqf.findField('ts_lot_qty.order_count').getValue()||0;
			tqf.findField('ts_lot_qty.picked_count').setValue(picked_count);
			var unpick_count = order_count-picked_count;
			if(shiped_count){
				unpick_count = order_count-picked_count-shiped_count;
			}
			tqf.findField('ts_lot_qty.unpick_count').setValue(unpick_count);
		}
		
		Ext.onReady(function(){
			//货品\订单数量、已分配数量、未分配数量
			var pk_goods = tqf.findField('ts_lot_qty.pk_goods'),order_count =tqf.findField('ts_lot_qty.order_count');
			pk_goods.setValue(childVO.pk_goods);
			order_count.setValue(childVO.order_count||0);
			setPickCount(childVO.picked_count||0,childVO.shiped_count||0);
			
			//货位、LPN
			tqf.findField('ts_lot_qty.pk_goods_allocation').setValue(childVO.pk_goods_allocation);
			tqf.findField('ts_lot_qty.lpn').setValue(childVO.lpn);
			tqf.findField('ts_lot.produce_date').setValue(childVO.produce_date);
			tqf.findField('ts_lot.expire_date').setValue(childVO.expire_date);
			tqf.findField('ts_lot.lot_attr1').setValue(childVO.lot_attr1);
			tqf.findField('ts_lot.lot_attr2').setValue(childVO.lot_attr2);
			tqf.findField('ts_lot.lot_attr3').setValue(childVO.lot_attr3);
			tqf.findField('ts_lot.lot_attr4').setValue(childVO.lot_attr4);
			tqf.findField('ts_lot.lot_attr5').setValue(childVO.lot_attr5);
			tqf.findField('ts_lot.lot_attr6').setValue(childVO.lot_attr6);
			tqf.findField('ts_lot.lot_attr7').setValue(childVO.lot_attr7);
			tqf.findField('ts_lot.lot_attr8').setValue(childVO.lot_attr8);
			tqf.findField('ts_lot.lot_attr9').setValue(childVO.lot_attr9);
			tqf.findField('ts_lot.lot_attr10').setValue(childVO.lot_attr10);
			tqf.findField('ts_lot.lot_attr11').setValue(childVO.lot_attr11);
			
			tqf.findField('ts_lot_qty.pk_customer').hide();
			
			//设置分配数量列可以编辑
			setColumnEditable();
			app.statusMgr.addAfterUpdateCallback(setColumnEditable);
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
