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
				btns.push(new uft.extend.Button({
					text : '库存更新',
					scope : this,
					handler : function(){
						MyToolbar.superclass.btn_edit_handler.call(this);
						//所有字段都不能编辑，除了库存
						this.app.headerCard.getForm().items.each(function(f) { 
							if(f.id != 'stock_num'){
								f.setReadOnly(true);
							}
						});
					}
				}));
				return btns;
			},
			btn_add_handler : function(){
				MyToolbar.superclass.btn_add_handler.call(this);
				var stock_num = uft.Utils.getField('stock_num');
				stock_num.setReadOnly(false);
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var code = uft.Utils.getField('code');
				code.setReadOnly(true);
				//编辑时不能编辑库存数量
				var stock_num = uft.Utils.getField('stock_num');
				stock_num.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
