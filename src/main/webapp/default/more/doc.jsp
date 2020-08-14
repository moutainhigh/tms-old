<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false" headerGridImmediatelyLoad="true" />
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push({
					text : '下载',
					iconCls : 'btnDownload',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！');
							return false;
						}
						if(records.length > 1){
							uft.Utils.showWarnMsg('只能选择一条记录进行下载！');
							return false;
						}
						window.location.href=ctxPath+"/c/doc/download.do?pk_document="+records[0].data['pk_document'];
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				});
				btns.push({
					text : '批量下载',
					scope : this,
					handler : function(){
						var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
						if(!records || records.length == 0){
							uft.Utils.showWarnMsg('请选择记录！');
							return false;
						}
						var idStr = '';
						for(var i=0;i<records.length;i++){
							idStr += records[i].data['pk_document'];
							if(i != records.length-1){
								idStr += ',';
							}
						}
						window.location.href=ctxPath+"/c/doc/zipDownload.do?pk_document="+idStr;
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				});
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
