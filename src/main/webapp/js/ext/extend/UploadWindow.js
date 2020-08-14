Ext.ns('uft.extend');
/**
 * 上传文件小窗口
 * @class UploadWindow
 * @extends Ext.Window
 */
uft.extend.UploadWindow = Ext.extend(Ext.Window, {
	fields : null,
	permitted_extensions:[],//空表示允许上传所有文件类型
	addCoverBtn : false,
	coverLabel : '是否覆盖',
	checkedCover : false,
	isLog : false, //是否加入可以查询日志功能
	submitUrl : "import.do",
	width : 320,
	params : {},//发送到后台的参数
	extendItems : [],
	closeAction: "close",
	constructor : function(config) {
		Ext.apply(this, config);
		if(this.permitted_extensions.length > 0){
			this.title = this.title||'请选择'+this.permitted_extensions.join(',')+'类型的文件';
		}else{
			this.title = this.title||'文件上传';
		}
		var items=[];
		items.push({fieldLabel:'选择文件',
					itemCls:'uft-form-label-not-null',
					id : '_up_file',
					name:'file',
					xtype:'fileuploadfield',
					allowBlank:false,
					buttonText: '浏览...',
					permitted_extensions:this.permitted_extensions});
		
//		var templet = {
//				layout : "column", // 从左往右的布局
//				items : [{
//							xtype : 'uftcombo',
//							name : 'pk_import_config',
//							allowBlank: false,
//							itemCls:'uft-form-label-not-null',
//							emptyText: "--请选择--",  
//							fieldLabel : '模板选择',
//							dataUrl : ctxPath + '/ic/getTemplet.json?fun_code='+this.fun_code,
//							resizable: true,//可以改变大小   
//							width: 36
//							//columnWidth : 0.1 
//					
//						},{
//							xtype : 'button',
//							text : '下载',
//							actiontype : 'submit',
//							scope : this,
//							handler : function() {
//								var values = this.form.getForm().getFieldValues()
//								var pk_import_config = values['pk_import_config'];
//								if(!pk_import_config || typeof(pk_import_config) == 'undefined'){
//									uft.Utils.showWarnMsg("请选择模板！");
//									return false;
//								}
//								var url = "downloadTemplet.do?pk_import_config=" + pk_import_config;
//								window.open(encodeURI(url));
//							}
//						}]
//		};
//		items.push(templet);
		
		items.push({
			xtype : 'uftcombo',
			name : 'pk_import_config',
			id : 'pk_import_config',
			allowBlank: false,
			itemCls:'uft-form-label-not-null',
			style : 'width:30px;',
			fieldLabel : '模板选择',
			dataUrl : ctxPath + '/ic/getTemplet.json?fun_code='+this.fun_code,
			width: 50
			//colspan : 1
		});
	
		if(this.addCoverBtn){
			items.push({fieldLabel : this.coverLabel,
						inputValue : 'true',
						checked : this.checkedCover,
						name : 'isCover',
						xtype:'uftcheckbox'});
		}
		for(var i=0;i<this.extendItems.length;i++){
			items.push(this.extendItems[i]);
		}
		if(this.isLog){
			items.push({xtype:'hidden',id:'log',name:'log'});
		}
		
		var btns = [];
//		if(this.isLog){
//			btns.push({
//				text : '查看日志',
//				scope : this,
//				handler : function() {
//					//查询导入的日志
//					new Ext.Window({
//						title : '导入日志',
//						html : uft.Utils.getField('log').getValue(),
//						height : 300,
//						width : 400,
//						maximizable : true,
//						modal : true,
//						autoScroll : true,
//						layout : 'fit'
//					}).show();
//				}
//			});
//		}
		btns.push({
			xtype : 'button',
			text : '下载',
			actiontype : 'submit',
			scope : this,
			handler : function() {
				var values = this.form.getForm().getFieldValues()
				var pk_import_config = values['pk_import_config'];
				if(!pk_import_config || typeof(pk_import_config) == 'undefined'){
					uft.Utils.showWarnMsg("请选择模板！");
					return false;
				}
				var url = "downloadTemplet.do?pk_import_config=" + pk_import_config;
				window.open(encodeURI(url));
			}
		});
		btns.push({
			text : '上传',
			formBind : true,
			scope : this,
			type : 'submit',
			handler : function() {
        		if(this.form.getForm().isValid()){
        			Ext.apply(this.params,this.form.getForm().getFieldValues());
            		this.form.getForm().submit({
	            		url : this.submitUrl,
	            		params : this.params,
	            		method : 'POST',
	            		waitTitle: '请稍后',   
                        waitMsg: '正在上传 ...',
                        scope : this,   
            			success:function(form,action){
	            			if(action && action.result) {
		            			var msg = "上传成功！";
		            			var result = action.result;
		            			if(result && result.msg){
									msg = result.msg;
			            		}
		            			uft.extend.tip.Tip.msg('info', msg);
		            			if(this.isLog){
		            				uft.Utils.getField('log').setValue(result.log);
		            			}
		            		}
//		            		var uf = Ext.getCmp('_up_file'),fileInput = uf.fileInput;
//		            		uf.setValue(null);
//		            		if(Ext.isIE){
//		            			//IE无法情况file输入框的值，只能先删除，重新创建一个
//		            			fileInput.parent().remove(fileInput)
//		            			uf.createFileInput();
//		            		}else{
//		            			fileInput.dom.value = '';
//		            		}
		            		this.fireEvent('aftersuccess',this,action?action.result:null);//抛出执行成功事件
            			},
            			failure:function(form, action){
            				var result = action.result;
		            		if(this.isLog){
			            		uft.Utils.getField('log').setValue(result.log); //将导入日志放入隐藏域
		            		}	            				
	            			var msg = "出现未知错误！";
	            			if(result && result.msg){
								msg = result.msg;
		            		}
            				uft.Utils.showErrorMsg(msg);
            				this.fireEvent('afterfailure',this,action?action.result:null);//抛出执行失败事件
            			}
            		});
        		}
			}
		});
		btns.push({
			text : '取消',
			scope : this,
			handler : function() {
				if(this.closeAction == 'hide'){
					this.hide();
				}else{
					this.close();
				}
				this.fireEvent('afterdestroy',this);//抛出关闭对话框事件
			}
		});
		this.form = new uft.extend.form.FormPanel({
			layout : 'form',
			fileUpload : true,
			target : '_self',
			labelWidth : 80,
	        border : false,
	        autoWidth : true,
			autoHeight : true,
			frame : true,
			defaults : {
				anchor : '95%'
			},
			items : items,
            buttons : btns
		});
		uft.extend.UploadWindow.superclass.constructor.call(this, {
			id : this.id,
			title : this.title,
			modal : true,
			frame : true,
			width : this.width,
			autoHeight : true,
			align : 'center',
			plain : false,
			layout : 'fit',
			items : [this.form]
		});
	}
});