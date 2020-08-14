Ext.namespace('uft.index');
uft.index.QuickLink = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		this.quickLinkForm = new uft.extend.form.FormPanel({
	        labelWidth : 80,
	        border : false,
	        autoWidth : true,
			frame : true,
			items : [{
				xtype : 'textfield',
				fieldLabel : '显示名称',
                name : 'fun_name',
                value : this.fun_name
            },{
				xtype : 'hidden',
                name : 'fun_code',
                value : this.fun_code
            },{
				xtype : 'hidden',
                name : 'class_name',
                value : this.class_name
            },{
				xtype : 'hidden',
                name : 'pk_fun',
                value : this.pk_fun
            }]
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '保&nbsp;&nbsp;存',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		});		
		uft.index.QuickLink.superclass.constructor.call(this, {
			title : this.editFlag?'修改快捷方式':'增加快捷方式',
			width : 300,
			height : 159,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.quickLinkForm],
			buttons : btns
		});
	    this.quickLinkForm.getForm().items.each(function(f) {
	    	f.on('specialkey', function(f,e){
				if(e.getKey()==13){ //enter键
					this.saveHandler();
				}else if(Ext.isIE || Ext.isChrome){
					if(e.getKey()==9){
						this.saveHandler();
					}
				}
	        },this);
	    },this);		
	},
	saveHandler : function(){
		if(this.quickLinkForm.getForm().isValid()) {
			var values = this.quickLinkForm.getForm().getFieldValues(false);
			Ext.apply(values,{});
			if(this.editFlag){
				uft.Utils.doAjax({
					scope : this,
					url : ctxPath+'/common/quick/edit.json',
					params : values,
					isTip : true,
					success : function() {
						//动态更新text值
						var btnId = 'btn_'+this.pk_fun;
						var btn = Ext.getCmp(btnId);
						btn.setText(values['fun_name']);
						this.destroy();
					}
				});
			}else{
				uft.Utils.doAjax({
			    	scope : this,
			    	url : ctxPath + '/common/quick/add.json',
			    	params : values,
			    	isTip : true,
			    	success : function(){
			    		//增加快捷方式后的处理,刷新快捷列表
			    		var cmp = Ext.getCmp('shortcutPanel'); //该Id在ShortcutPanel中定义
			    		if(cmp){
			    			cmp.addQuickLink(values);
			    		}
			    		this.fireEvent('add',this);
			    		this.destroy();
			    	}
			    });
			}
		}
	}
});
