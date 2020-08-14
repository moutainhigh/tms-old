<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/jquery/ajaxfileupload.js"/>"></script>
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
				btns.push(this.btn_add);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
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
						window.location.href=ctxPath+"/doc/download.do?pk_document="+records[0].data['pk_document'];
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
						window.location.href=ctxPath+"/doc/zipDownload.do?pk_document="+idStr;
					},
					enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
				});
				return btns;
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		var funCode = app.context.getFunCode();
		
		var permitted_extensions = 'doc,docx,xls,xlsx,ppt,pptx,pdf';
		var userfile = Ext.getCmp('userfile');
		if(userfile){
			userfile.on('fileselected',function(field,fileName){
				if(!this.check(fileName)){
					field.setValue(null);
					uft.Utils.showErrorMsg('上传文件不合法，必须是[doc,docx,xls,xlsx,ppt,pptx,pdf]类型的文件！');
					return false;
				}
				var body = Ext.getBody();
				body.mask(uft.jf.Constants.UPLOADING_MSG);
				  $.ajaxFileUpload({
	                url:'upload.json?funCode='+funCode,
	                secureuri:false,
	                fileElementId:'userfile-file',
	                referTarget : field,
	                dataType: 'json',
	                success: function (result, status){
	                	body.unmask();
	                    if(result.success){
	                    	var data = result.data;
	                    	if(data){
		                    	field.setValue(data.realFileName);//上传后的文件的真实名称
		                    	Ext.getCmp('file_name').setValue(data.fileName);
	                    	}
	                    	field.onBlur(); //触发鼠标移开事件,这样能刷新图片
	                    }else{
	                    	field.setValue(null);
	                    	uft.Utils.showErrorMsg(result.msg);
	                    	return;
	                    }
	                }
	          	});
			},this);
		}

		/**
		 * 检查文件名是否合法
		*/
		function check(fileName){
			var index = fileName.indexOf(".");
			if(index == -1){
				return false;
			}
			var suffix = fileName.substring(index+1);
			suffix = suffix.toLowerCase();
			if(permitted_extensions.indexOf(suffix) != -1){
				return true;
			}
			return false;
		};	
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
