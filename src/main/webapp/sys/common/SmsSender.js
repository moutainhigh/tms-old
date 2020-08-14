//站内信息窗口
Ext.ns('nw.sys');
nw.sys.SmsSender = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		
		var sender_name='',receiver_name='',title='',content='';
		if(this.pk_sms){
			//显示使用，根据pk加载数据
			var values = Utils.doSyncRequest('common/sms/getByPK.json',{pk_sms:this.pk_sms},'POST');
			if(values && values.data){
				var smsVO = values.data;
				sender_name = smsVO.sender_name;
				receiver_name = smsVO.receiver_name;
				title = smsVO.title;
				content = smsVO.content;
			}
		}
		
		var items = [];
		if(this.isAdd){
			items.push({"refName":"用户档案-web","itemCls":"uft-form-label-not-null","xtype":"headerreffield","name":"receiver","fieldLabel":"接收人",allowBlank : false,isMulti:true,
					"refWindow":{"model":1,"leafflag":false,"gridDataUrl":ctxPath+"/ref/common/user/load4Grid.json",
					"extGridColumnDescns":[{"type":"string","width":120,"dataIndex":"pk_user","xtype":"gridcolumn","hidden":true},
											{"header":"用户编码","type":"string","width":120,"dataIndex":"user_code","xtype":"gridcolumn"},
											{"header":"用户名称","type":"string","width":120,"dataIndex":"user_name","xtype":"gridcolumn"}]},
											"pkField":"pk_user","codeField":"user_code","nameField":"user_name","showCodeOnBlur":false,
											"getByPkUrl":ctxPath+"/ref/common/user/getByPk.do","getByCodeUrl":ctxPath+"/ref/common/user/getByCode.do"});			
		}else{
			items.push({fieldLabel : '发送人',name : 'sender_name',value : sender_name});
			items.push({fieldLabel : '接收人',name : 'receiver_name',value : receiver_name});
		}
		items.push({fieldLabel : '标题',name : 'title',"itemCls":"uft-form-label-not-null",allowBlank:false,value:title});	
		items.push({fieldLabel : '内容',name : 'content',xtype:'textarea',value:content});
		if(!this.isAdd){
			//设置所有输入框只读
			for(var i=0;i<items.length;i++){
				items[i].readOnly = true;
			}
		}
		
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
		var btns = [];
		if(this.isAdd){
			btns.push({
				xtype : 'button',
				text : '发&nbsp;&nbsp;送',
				scope : this,
				handler : function() {
					var form = this.form.getForm();
					if(form.isValid()){
						var values = form.getFieldValues(false);
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
				}
			});
		}
		btns.push({
			xtype : 'button',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		nw.sys.SmsSender.superclass.constructor.call(this, {
			title : this.isAdd?'发送站内信':'站内信详细',
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