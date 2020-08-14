Ext.namespace('uft.te');
/**
 * 快递记录
 * @class uft.te.Express
 * @extends Ext.Window
 */
uft.te.Express = Ext.extend(Ext.Window, {
	constructor : function(config) {
		Ext.apply(this,config);
		if(!this.values || this.values.length == 0){
			uft.Utils.showErrorMsg('没有返回信息！');
			return false;
		}
		var formPanelItems = [];
		var values = this.values;
		for(var i=0;i<this.values.length;i++){
			var id = this.values[i].id;
			var error = this.values[i].error;
			var express = this.values[i].express;//快递信息列表
			var expressItems = [];
			if(error){
				var errorItem = {
						name : "error",
						fieldLabel : "错误信息",
						xtype : "textfield",
						value : error,
						style : "color:red;",
						colspan : 3
					};
					expressItems.push(errorItem);
			}
			var splitItem = {
				name : "split",
				text : "快递记录",
				xtype : "label",
				newlineflag : true,
				colspan : 3,
				
				
			};
			expressItems.push(splitItem);
			
			if(express && express.length > 0){
				for(var i=0;i<express.length;i++){
					var expressItem = {
							name : "express",
							fieldLabel : express[i].time,
							labelStyle: 'width:40;',
							xtype : "textfield",
							value : express[i].context,
							newlineflag : true,
							colspan : 3
						};
						expressItems.push(expressItem);
				}
			}
			
			var formPanelItem = {
				layout : 'tableform',
				layoutConfig: {columns:3},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield'
				},      
				items : [{
					id : id,
					xtype : 'fieldset',
					title : id,
					collapsible : true,
					collapsed : false,
					layout : 'tableform',
					padding : '5px 5px 0',
					colspan : 3,
					autoScroll : false,
					layoutConfig: {columns:3},
					defaults:{
						anchor: '95%'
					},
					items : [expressItems]
				}]
			};
			formPanelItems.push(formPanelItem);
		}
		
		this.formPanel = new uft.extend.form.FormPanel({
			id : 'expressForm',
			region : 'north',
			height : 240,
			labelWidth : 80,
			bodyStyle:'overflow-y:auto;overflow-x:hidden',
			margins : '0px 5px 0px 5px',
			border : true,
			items : [formPanelItems]
		});
		
		uft.te.Express.superclass.constructor.call(this, {
			id : 'Express',
			title : '快递信息',
			width : 900,
			height : 500,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : 'fit',
			buttons : [new Ext.Button({
					iconCls : 'btnCancel',
					text : '关&nbsp;&nbsp;闭',
					scope : this,
					handler : function() {
						this.close();//多个地方使用地图，而地图渲染的id都是container，这个就不改了
					}
			})],
			items : [this.formPanel]
	    });
	},
	show : function(){

		uft.te.Express.superclass.show.call(this);
	},
	afterRender : function(){

		uft.te.Express.superclass.afterRender.call(this);
	}
});
