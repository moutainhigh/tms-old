//站内信息窗口
Ext.ns('uft.jf');
uft.jf.SmsSender = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		var billids=config.billids,billnos=config.billnos,fun_code=config.fun_code,upload_flag = 'false';
		var pk_attas=[];
		var items = [];
		items.push({"refName":"用户档案-web","itemCls":"uft-form-label-not-null","xtype":"headerreffield","name":"receiver","fieldLabel":"接收人",allowBlank : false,isMulti:true,
			"refWindow":{"model":1,"leafflag":false,"gridDataUrl":ctxPath+"/ref/common/user/load4Grid.json",
			"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_user","xtype":"gridcolumn","hidden":true},
									{"header":"用户编码","type":"string","width":120,"dataIndex":"user_code","xtype":"gridcolumn"},
									{"header":"用户名称","type":"string","width":120,"dataIndex":"user_name","xtype":"gridcolumn"}]},
									"pkField":"pk_user","codeField":"user_code","nameField":"user_name","showCodeOnBlur":false,
									"getByPkUrl":ctxPath+"/ref/common/user/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/user/getByCode.do"});			
		items.push({fieldLabel : '标题',name : 'title',"itemCls":"uft-form-label-not-null",allowBlank:false});	
		items.push({fieldLabel : '附件',id:'pk_filesystem1',name : 'pk_filesystem1',xtype:'fileuploadfield',permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png']});
		items.push({fieldLabel : '附件',id:'pk_filesystem2',name : 'pk_filesystem2',xtype:'fileuploadfield',permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png']});
		items.push({fieldLabel : '附件',id:'pk_filesystem3',name : 'pk_filesystem3',xtype:'fileuploadfield',permitted_extensions:['doc','xdoc','xls','xlsx','pdf','jpg','jpeg','png']});
		items.push({fieldLabel : '内容',name : 'content',xtype:'textarea'});
		
		this.form = new uft.extend.form.FormPanel({
	        labelWidth : 80,
	        border : false,
	        autoWidth : true,
			autoHeight : true,
			frame : true,
			defaults : {
				xtype : 'textfield',
				allowBlank : false,
				anchor : '85%',
				width : 200
			},
			items : items
		});
		
		
		this.form.items.each(function(field){
			if(field.name == 'pk_filesystem1'){
				field.on('fileselected',function(field,fileName){
					//上传
					var body = Ext.getBody();
					body.mask(uft.jf.Constants.UPLOADING_MSG);
					  $.ajaxFileUpload({
		                url:ctxPath + '/common/sms/uploadSmsAttach.json',
		                secureuri:false,
		                fileElementId:'pk_filesystem1-file',
		                referTarget : field,
		                dataType: 'json',
		                success: function (result, status){
		                	body.unmask();
		                    if(result.success){
		                    	uft.Utils.showInfoMsg('文件上传成功！');
		                    	upload_flag = 'true';
		                    	pk_attas.push(result.data['pk_filesystem1'])
		                    	field.setValue(result.data['pk_filesystem1']); //这里实际上是文件的pk
		                    }else{
		                    	field.setValue(null);
		                    	uft.Utils.showErrorMsg(result.msg);
		                    	return;
		                    }
		                }
		          	});
				},this);
				}else if(field.name == 'pk_filesystem2'){
					field.on('fileselected',function(field,fileName){
						//上传
						var body = Ext.getBody();
						body.mask(uft.jf.Constants.UPLOADING_MSG);
						  $.ajaxFileUpload({
							url:ctxPath + '/common/sms/uploadSmsAttach.json',
			                secureuri:false,
			                fileElementId:'pk_filesystem2-file',
			                referTarget : field,
			                dataType: 'json',
			                success: function (result, status){
			                	body.unmask();
			                    if(result.success){
			                    	uft.Utils.showInfoMsg('文件上传成功！');
			                    	upload_flag = 'true';
			                    	pk_attas.push(result.data['pk_filesystem2'])
			                    	field.setValue(result.data['pk_filesystem2']); //这里实际上是文件的pk
			                    }else{
			                    	field.setValue(null);
			                    	uft.Utils.showErrorMsg(result.msg);
			                    	return;
			                    }
			                }
			          	});
					},this);
				}else if(field.name == 'pk_filesystem3'){
					field.on('fileselected',function(field,fileName){
						//上传
						var body = Ext.getBody();
						body.mask(uft.jf.Constants.UPLOADING_MSG);
						  $.ajaxFileUpload({
							url:ctxPath + '/common/sms/uploadSmsAttach.json',
			                secureuri:false,
			                fileElementId:'pk_filesystem3-file',
			                referTarget : field,
			                dataType: 'json',
			                success: function (result, status){
			                	body.unmask();
			                    if(result.success){
			                    	uft.Utils.showInfoMsg('文件上传成功！');
			                    	upload_flag = 'true';
			                    	pk_attas.push(result.data['pk_filesystem3'])
			                    	field.setValue(result.data['pk_filesystem3']); //这里实际上是文件的pk
			                    }else{
			                    	field.setValue(null);
			                    	uft.Utils.showErrorMsg(result.msg);
			                    	return;
			                    }
			                }
			          	});
					},this);
				}
		},this);
		

		var btns = [];
		btns.push({
			xtype : 'button',
			text : '发&nbsp;&nbsp;送',
			scope : this,
			handler : function() {
				var form = this.form.getForm();
				var values = form.getFieldValues(false);
				values.billids = billids;
				values.billnos = billnos;
				values.fun_code = fun_code;
				values.pk_attas = pk_attas;
				values.upload_flag = upload_flag;
				uft.Utils.doAjax({
			    	scope : this,
			    	url : ctxPath + '/common/sms/send.json',
			    	actionType : '发送',
			    	params : values,
			    	success : function(values){
						this.close();
			    	}
			    });
			}
		});
		btns.push({
			xtype : 'button',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		uft.jf.SmsSender.superclass.constructor.call(this, {
			title : '发送站内信',
			width : 500,
			autoHeight : true,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.form],
			buttons : btns
		});
	}
});