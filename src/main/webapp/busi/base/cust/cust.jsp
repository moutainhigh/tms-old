<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
		<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/jquery/ajaxfileupload.js"/>"></script>
	</head>
	<body>
	<nw:Bill templetVO="${templetVO}" headerGridImmediatelyLoad="true" 
		bodyGridsPagination="false,false" useFieldSetInHeader="true"  headerGridPageSize="20"
		bodyGridsDataUrl="loadData.json,loadData.json" headerGridCheckboxSelectionModel="true" headerGridSingleSelect="false"/>				
	</body>
	<script type="text/javascript">
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
				var cust_code = uft.Utils.getField('cust_code');
				cust_code.setReadOnly(true);
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
		
		
		app.statusMgr.addAfterUpdateCallback(function(){
			var grid = Ext.getCmp('ts_cust_rate');
			setBodyAssistToolbarEnable(grid);
		});
		
		app.getBodyTabPanel().addListener('tabchange',function(){
			var grid = app.getActiveBodyGrid();
			if(grid.id == 'ts_cust_rate'){
				setBodyAssistToolbarEnable(grid);
			}
			
		},this);
		
		function setBodyAssistToolbarEnable(grid){
			var bat = app.bodyAssistToolbar;
			var pageStatus = app.statusMgr.getCurrentPageStatus();
			if(pageStatus == uft.jf.pageStatus.OP_EDIT || pageStatus == uft.jf.pageStatus.OP_ADD){
				bat.setOperatorEnabled(grid,{'add':true,'cop':false,'del':true});
			}else{
				bat.setOperatorEnabled(grid,{'add':false,'cop':false,'del':false});
			}
		}
		
		var  opp_grid = Ext.getCmp("ts_cust_op_project_b");
		if(opp_grid){
			opp_grid.addListener('beforeedit',function(e){
				var record = e.record;
				var fieldName=e.field;
				var pk_customer = Ext.getCmp("pk_customer").getValue()
				//alert(pk_customer);
				//alert(fieldName);
				if(fieldName == 'op_project_code'){
					var opp_editor = uft.Utils.getColumnEditor(opp_grid,'op_project_code');
					opp_editor.addExtendParams({pk_customer:pk_customer});
				}
			});
		}
		
		//检测当前填入的地址是否已经存在
		function afterEditAddrcodeOrAddrname(record){
			var curPk_address = record.get("pk_address");
			if(!curPk_address || curPk_address == ''){
				return;
			}
			var grid = Ext.getCmp("ts_cust_addr");
			if(grid){
				for(var i=0;i<grid.getStore().getCount();i++){
					var _record = grid.getStore().getAt(i);
					if(_record.id != record.id && _record.get("pk_address") == curPk_address){
						//不能比较本编辑行
						uft.Utils.showWarnMsg("您选择的地址已经存在，请重新选择！");
						record.beginEdit();
						uft.Utils.setColumnValue(record,"pk_address",null);
						uft.Utils.setColumnValue(record,"addrcode",null);
						uft.Utils.setColumnValue(record,"addrname",null);
						record.endEdit();
						return;
					}
				}
			}
		}
		
		
		afterEditBody = function(e){
			var grid = e.grid,ds = grid.getStore(),count = ds.getCount(),record = e.record;
			if(e.grid.id == 'ts_cust_rate'){//体积重换算比
				if(e.field == 'trans_type_name'){
					var pk_trans_type = record.get('pk_trans_type');
					var start_area = record.get('start_area');
					var end_area = record.get('end_area');
					var key1 = pk_trans_type + start_area + end_area
					for(var i=0;i<count;i++){
						var r = ds.getAt(i);
						if(r.id != record.id){
							var pk = r.get('pk_trans_type');
							var start = r.get('start_area');
							var end = r.get('end_area');
							var key2 = pk + start + end
							if(key2 == key1){
								uft.Utils.showWarnMsg('您选择的换算比已经存在，不能重复录入！');
								record.set('pk_trans_type',null);
								record.set('trans_type_name',null);
								record.set('start_area',null);
								record.set('start_area_name',null);
								record.set('end_area',null);
								record.set('end_area_name',null);
								return false;
							}
						}
					}
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
