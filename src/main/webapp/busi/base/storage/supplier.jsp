<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_export);
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var code = uft.Utils.getField('supp_code');
				code.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		afterEditBody = function(e){
			var grid = e.grid,ds = grid.getStore(),count = ds.getCount(),record = e.record;
			if(grid.id == 'ts_supp_addr'){
				if(e.field == 'addr_code' || e.field == 'addr_name'){
					var pk_address = record.get('pk_address');
					for(var i=0;i<count;i++){
						var r = ds.getAt(i);
						if(r.id == record.id){
							continue;
						}
						var pk = r.get('pk_address');
						if(pk == pk_address){
							uft.Utils.showWarnMsg("您选择的地址已经存在，请重新选择！");
							record.beginEdit();
							uft.Utils.setColumnValue(record,"pk_address",null);
							uft.Utils.setColumnValue(record,"addrcode",null);
							uft.Utils.setColumnValue(record,"addrname",null);
							record.endEdit();
							return;
						}
					}
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
