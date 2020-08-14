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
			btn_row_del_handler : function(){
				var grid = this.app.getBodyGrids()[0];
				var record = grid.getSelectedRow();
				if(record){
					var type = record.get('type');
					if(String(type) === '0'){
						uft.Utils.showWarnMsg('平台级参数不能删除！');
						return;
					}
				}
				MyToolbar.superclass.btn_row_del_handler.call(this);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		var grid = app.bodyGrids[0];
		grid.on('beforeedit',function(e){
			//不能修改其他公司的参数
			var currentCorp = '<%=loginInfo.getPk_corp()%>';
			var _pk_corp = e.record.get('pk_corp');
			if(currentCorp != '0001' && currentCorp != _pk_corp){
				return false;
			}
			return true;
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
