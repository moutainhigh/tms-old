<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" bodyGridsCheckboxSelectionModel="true" bodyGridsSingleSelect="false" bodyGridsImmediatelyLoad="true"/>
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
				btns.push('-');
				btns.push(new Ext.Button({
					text : '执行',
					iconCls : 'btnSetting',
					scope : this,
					handler : function(){
						//执行压缩动作
						var grid = app.getBodyGrids()[0];
						var ids = uft.Utils.getSelectedRecordIds(grid,grid.pkFieldName);
						if(ids && ids.length > 0){
						    uft.Utils.doAjax({
						    	scope : this,
						    	params : {pk_merge:ids},
						    	url : 'execute.json',
						    	success : function(values){
						    		
						    	}
						    });
						}else{
							uft.Utils.showWarnMsg('请先选择记录！');
							return;
						}
					}
				}));
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
