<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" bodyGridsPagination="true" bodyGridsImmediatelyLoad="true"/>
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BodyToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_ref);
				btns.push('-');
				btns.push(this.btn_row_add);
				btns.push(this.btn_row_del);
				btns.push(this.btn_row_ins);
				return btns;
			},
			getAppParams : function(config){
				if(!config){
					config = {};
				}
				config.bodyGridOnlyModify = false;//将所有数据发到后台，包括没有改变的记录，方便校验
				return MyToolbar.superclass.getAppParams.call(this,config);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
