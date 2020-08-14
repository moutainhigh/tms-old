Ext.ns('uft.bill');
/**
 * 数字类型设置对话框
 * 供uft.bill.DecimaltypeField调用，不能单独使用
 * @class uft.bill.DecimaltypeWindow
 * @extends Ext.Window
 */
uft.bill.DecimaltypeWindow= Ext.extend(Ext.Window, {
	shadow : false,
	width :400,
	height :200,
	srcField : null,//关联的输入框
	constructor : function(config) {
		Ext.apply(this,config);
		var value = this.srcField.getValue();
		//解析这个value，将值赋给各个表单域
		var number_precision=2,thousands_format=false,zeroToNull=false
		if(value){
			//2,,,,,Y,Y
			var arr = value.split(',');
			number_precision = arr[0];
			if(arr.length > 5){//arr[5]是千分位
				thousands_format = arr[5]=='Y';
			}
			if(arr.length > 6){
				zeroToNull = arr[6]== 'Y';
			}
		}
		this.formPanel = new uft.extend.form.FormPanel({
			labelWidth : 100,
			layout : 'form',
			border : true,
			frame : true,
			defaults:{
				autoHeight:true, 
				bodyStyle:'padding:10px'
			},
			items : [{
						xtype : 'uftnumberfield',
						decimalPrecision:0,
						id : 'number_precision',
						style:"text-align: right",
						fieldLabel : '自定义精度',
						value : number_precision
					},{
						xtype : 'uftcheckbox',
						id : 'thousands_format',
						fieldLabel : '显示千分位',
						checked : thousands_format
					},{
						xtype : 'uftcheckbox',
						id : 'zeroToNull',
						fieldLabel : '零显示为空',
						checked : zeroToNull
					}]
		});
		
		var btns = [{
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : function(){
				//得到各个表单域的值，拼接成字符串
				var number_precision = Ext.getCmp('number_precision').getValue();
				var thousands_format = Ext.getCmp('thousands_format').getValue();
				var zeroToNull = Ext.getCmp('zeroToNull').getValue();
				//2,,,,,Y,Y
				var value = number_precision;
				if(thousands_format == 'Y'){
					value += ',,,,,Y,';
				}
				if(zeroToNull == 'Y'){
					if(thousands_format == 'Y'){
						value += 'Y';
					}else{
						value += ',,,,,,Y';
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
		uft.bill.DecimaltypeWindow.superclass.constructor.call(this, {
			title : '类型设置',
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
