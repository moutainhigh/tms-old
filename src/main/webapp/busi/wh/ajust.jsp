<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" headerGridPageSize="20"/>				
	</body>
	<script type="text/javascript">
		MyToolbar = Ext.extend(uft.jf.BillToolbar, {
			constructor : function(config){
				Ext.apply(this,config);
				MyToolbar.superclass.constructor.call(this);
			},
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
				btns.push(this.btn_confirm);
				return btns;
			},
			btn_add_handler : function(){
				openDialog(ctxPath + '/wh/ajustref/index.html?funCode=t1216',null);
				this.app.headerGrid.getStore().reload();
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.BillPanel(${moduleName}.appUiConfig);
		
		//打开对话框
		function openDialog(url,params){
			var wparams = "dialogWidth:900px"
				+";dialogHeight:520px"
				+";dialogLeft:"+(window.screen.availWidth-900)/2+"px"
				+";dialogTop:"+(window.screen.availHeight-520)/2+"px"
				+";status:no;scroll:no;resizable:no;help:no;center:yes";
			if(Ext.isChrome){//chrome 从37版本开始不支持showModalDialog方法
				window.open(url,params,wparams);
			}else{
				window.showModalDialog(url,params,wparams);
			}
		}
		
		afterEditBody = function(e){
			var f = e.field,r = e.record;
			if(f == 'ajust_num'){//编辑调整数量
				var ajust_num = uft.Utils.getNumberColumnValue(r,'ajust_num');
				var available_num = uft.Utils.getNumberColumnValue(r,'available_num');
				if(ajust_num <=0 || ajust_num > available_num){
					uft.Utils.showWarnMsg('调整数量必须大于0，小于等于可用数量！');
					r.set('ajust_num',null);
					return;
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
