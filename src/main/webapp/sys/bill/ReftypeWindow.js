Ext.ns('uft.bill');
/**
 * 参照选择对话框
 * 供uft.bill.ReftypeField调用，不能单独使用
 * @class uft.bill.ReftypeWindow
 * @extends Ext.Window
 */
uft.bill.ReftypeWindow= Ext.extend(Ext.Window, {
	shadow : false,
	width :600,
	height :300,
	srcField : null,//关联的输入框
	constructor : function(config) {
		Ext.apply(this,config);
		var value = this.srcField.getValue();
		var userdefine=false,displaynamewhenblur = true,isleafcanselect = true,systemref='',userdefineref='';
		if(value){
			value = value.replace('&lt;','<');
			value = value.replace('&gt;','>');
			value = value.split(',');
			if(value[0].indexOf('<') == 0){
				userdefine = true;
				userdefineref = value[0];
			}else{
				systemref = value[0];
			}
			for(var i=0;i<value.length;i++){
				if(value[i] && value[i].indexOf('code=') != -1){
					//code
					var v = value[i].substring(value[i].indexOf('=')+1);
					displaynamewhenblur = v=='Y'?false:true;
				}else if(value[i] && value[i].indexOf('nl=') != -1){
					var v = value[i].substring(value[i].indexOf('=')+1);
					isleafcanselect = v=='Y'?true:false;
				}
			}
		}
		this.formPanel = new uft.extend.form.FormPanel({
			region : 'north',
			labelWidth : 135,
			layout : 'fit',
			border : true,
			items : [{
					layout : 'tableform',
					layoutConfig: {columns:2},
					border : true,
					padding : '5px 5px 0',
					defaults:{
						anchor: '98%',
						xtype : 'textfield'
					},      
					items : [{
						xtype : 'uftcheckbox',
						id : 'userdefine',
						name : 'userdefine',
						fieldLabel : '是否自定义参照',
						checked : userdefine,
						colspan : 2
					},{
						xtype : 'uftcombo',
						id : 'systemref',
						name : 'systemref',
						fieldLabel : '参照选择',
						dataUrl : 'loadSystemRef.json',
						readOnly : userdefine,
						value : systemref,
						colspan : 1
					},{
						id : 'userdefineref',
						name : 'userdefineref',
						fieldLabel : '自定义参照类名称',
						realyOnly : userdefine?false:true,
						"itemCls":"uft-form-label-not-edit",
						value : userdefineref,
						colspan : 1
					},{
						xtype : 'uftcheckbox',
						id : 'isleafcanselect',
						name : 'isleafcanselect',
						fieldLabel : '是否可以选择非叶子节点',
						checked : isleafcanselect,
						colspan : 1
					},{
						xtype : 'uftcheckbox',
						id : 'displaynamewhenblur',
						name : 'displaynamewhenblur',
						fieldLabel : '焦点离开后参照显示名称',
						checked : displaynamewhenblur,
						colspan : 1
					}]
				}]
		});
		var userdefine = Ext.getCmp('userdefine');//是否自定义参照
		userdefine.on('check',function(cb,value){
			if(value == 'Y'){
				//使用自定义参照
				Ext.getCmp('userdefineref').setReadOnly(false);
				Ext.getCmp('systemref').setReadOnly(true);
			}else{
				Ext.getCmp('userdefineref').setReadOnly(true);
				Ext.getCmp('systemref').setReadOnly(false);
			}
		},this);
		
		var btns = [{
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : function(){
				//运输方式-web,code=Y,nl=N
				var value = '';
				var userdefine = Ext.getCmp('userdefine').getValue();
				if(userdefine && userdefine == 'Y'){
					//自定义参照
					value = Ext.getCmp('userdefineref').getValue();
					value = value.replace('<','&lt;');
					value = value.replace('>','&gt;');
				}else{
					value = Ext.getCmp('systemref').getValue();
				}
				if(value){
					var displaynamewhenblur = Ext.getCmp('displaynamewhenblur').getValue();
					if(displaynamewhenblur == 'N'){
						value += ',';
						value += 'code=Y';
					}
					var isleafcanselect = Ext.getCmp('isleafcanselect').getValue();
					if(isleafcanselect == 'N'){//是否能够
						value +=',';
						value +='nl=N';
					}
				}
				this.fireEvent('aftersubmit',value);
				this.close();
			}
		}, {
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		}];
		uft.bill.ReftypeWindow.superclass.constructor.call(this, {
			title : '参照选择定制对话框',
			width : this.width ,
			height : this.height,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true,
			draggable : false,
			modal : true,
			resizable : true,
			border : false,
			layout : 'fit',
			items : [this.formPanel],
			buttonAlign : 'center',
			buttons : btns
	    });
	}
});
