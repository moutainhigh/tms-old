<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type='text/javascript' src='<c:url value="/sys/common/SmsSender.js?v=${version}"/>'></script>
		<script type='text/javascript' src='<c:url value="/sys/common/SmsAttach.js?v=${version}"/>'></script>
		<script type="text/javascript">
			function attaRenderer(value,meta,record){
				if(value==undefined || value=='' || value == 'N' || !value){
					return "";
				}else{
					return "<div align='center'><img src='"+ctxPath+"/theme/default/images/default/btn/attach.png' border=0 onclick='openAttr("+'"'+record.data.pk_sms+'"'+")' style='cursor:pointer'>";
				}
			}
			function openAttr(pk_sms){
				new nw.sys.SmsAttach({app:this.app,pk_sms:pk_sms}).show();
			}
		
		
			function billnosRenderer(value,meta,record){
				var billids = uft.Utils.getColumnValue(record,'billids');
				var fun_code = uft.Utils.getColumnValue(record,'fun_code');
				if(value==undefined || value=='' || fun_code==undefined || fun_code=='')
					return value;
				
				var funVO = Utils.doSyncRequest('getFunVO.json',{'fun_code':fun_code},'POST');
				if(!funVO){
					return value;
				}
				return "<a href='javascript:openNodeWithBillIds("+JSON.stringify(funVO)+","+'"'+billids+'"'+");'>"+value+"</a>"
			}
			
			function openNodeWithBillIds(funVO,billIds){
				var url = ctxPath+funVO.class_name;
				if(url.indexOf('?') == -1){
					//url中没有参数
					url+='?';
				}else{
					url+='&';
				}
				url+='funCode='+funVO.fun_code;
				url+='&billIds='+billIds;
				
				if(parent && parent != window){
					parent.openNode(funVO.fun_code,funVO.fun_name,url,true,true);
				}else{
					window.open(url);
				}
			}
			</script>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false" headerGridImmediatelyLoad="true" />
	</body>
	<script type="text/javascript" defer>
		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_prev);
				btns.push(this.btn_next);
				btns.push({
					xtype : 'button',
					text : '发送站内信',
					scope : this,
					handler : function(){
						new nw.sys.SmsSender({isAdd:true}).show();
					}
				});
				return btns;
			}
		});
		
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
