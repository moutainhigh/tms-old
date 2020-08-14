<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" 
		bodyGridsPagination="false,false"  headerGridPageSize="20"
		bodyGridsDataUrl="loadData.json,loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">

		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
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
				btns.push(this.btn_import);
				btns.push(this.btn_export);
				btns.push(new uft.extend.Button({
					text : '经纬度获取',
					scope : this,
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择地址！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_address:ids},
					    	url : 'getGeocoderLatitude.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
				},
				enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				}));
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				addr_code = uft.Utils.getField('addr_code');
				addr_code.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
