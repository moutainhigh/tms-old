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
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true"  headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
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
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var driver_code = uft.Utils.getField('driver_code');
				driver_code.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		
		var photo = Ext.getCmp('photo');
		if(photo){
			photo.on('fileselected',function(field,fileName){
				//检查后缀
				if(!this.check(fileName)){
					field.setValue(null);
					uft.Utils.showErrorMsg('上传文件不合法，必须是png,jpeg,jpg类型的文件');
					return false;
				}
				//上传
				var body = Ext.getBody();
				body.mask(uft.jf.Constants.UPLOADING_MSG);
				  $.ajaxFileUpload({
	                url:'uploadImage.json',
	                secureuri:false,
	                fileElementId:'photo-file',
	                referTarget : field,
	                dataType: 'json',
	                success: function (result, status){
	                	body.unmask();
	                    if(result.success){
	                    	field.setValue(result.data); //使用新的文件名
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
			var permissionSuffix = "png,jpeg,jpg";
			var index = fileName.indexOf(".");
			if(index == -1){
				return false;
			}
			//扩展名
			var suffix = fileName.substring(index+1);
			suffix = suffix.toLowerCase();
			if(permissionSuffix.indexOf(suffix) != -1){
				return true;
			}
			return false;
		};			
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
