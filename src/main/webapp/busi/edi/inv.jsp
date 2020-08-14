<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="false"  headerGridPageSizePlugin="true"		bodyGridsPagination="false,false,false" 
		bodyGridsDataUrl="loadData.json,loadData.json,refreshReceDetail.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>			
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
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
					text : '同步', 
					iconCls : 'btnYes',
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择发货单！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_invoice:ids},
					    	url : 'sync.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					//enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				
				btns.push(new uft.extend.Button({
					text : '取消', 
					iconCls : 'btnCancel',
					handler : function(){
						var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
						if(!ids || ids.length == 0){
							uft.extend.tip.Tip.msg('warn','请先选择发货单！');
							return;
						}
					    uft.Utils.doAjax({
					    	scope : this,
					    	params : {pk_invoice:ids},
					    	url : 'cancel.json',
					    	success : function(values){
					    		//重新加载数据
					    		this.app.headerGrid.getStore().reload();
					    	}
					    });	
					},
					//enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
				}));
				btns.push(this.btn_export);
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
