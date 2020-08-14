Ext.ns('uft.extend.form');
/**
 * 点击大文本参照后打开的窗口，用于输入大文本
 * 供uft.extend.form.FormulaField调用，不能单独使用
 * @class uft.extend.form.BigTextWindow
 * @extends uft.extend.form.BigTextWindow
 */
uft.extend.form.FormulaWindow= Ext.extend(uft.extend.form.BigTextWindow, {
	shadow : false,
	width :600,
	height :400,
	textValue : null,
	returnValue : null,
	constructor : function(config) {
		Ext.apply(this,config);
		var extendItem = [new Ext.Panel({
			html : '<div style="color:#8E8E8E!important;"><b>帮助：</b><br/>1、getColValue(tablename,fieldname,pkfield,pkvalue)根据主键从数据库查询特定字段的值,其功能类似SQL语句:select fieldname from tablename where pkfield = pkvalue 从这条SQL语句可以看出各个参数的含义；'+
				   '<br/>2、fieldname1,fieldname2->getColsValue("tablename","fieldname1","fieldname2","pkfield",pkvalue)根据主键从数据库查询多个字段的值,左边待赋值的字段要用逗号分割,注意里面的字段，表名要用双引号扩起来。' +
				   '<br/>3、公式名称不区分大小写，但是公式有执行先后顺序。</div>'
		})];
		
		this.textArea = new Ext.form.TextArea({
			height:336
		});
		this.textArea.setValue(this.textValue);
		this.formPanel = new Ext.Panel({
			region : 'center',
			layout : 'fit',
			autoHeight:true,
			width : 360,
			frame : false,
			border : false,
			items :[this.textArea]
		});
		var h = 100;
		this.textArea.setHeight(336-h);
		this.southPanel = new Ext.Panel({
			region : 'south',
			layout : 'fit',
			height : h,
			items : extendItem
		});
		
		var btns = [{
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : this.process
		}, {
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		}];
		//XXX 注意这里调用的是Ext.Window的构造方法
		uft.extend.form.BigTextWindow.superclass.constructor.call(this, {
			title : '请录入公式',
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
			layout : 'border',
			items : [this.formPanel,this.southPanel],
			buttons : btns,
			defaultButton : 0,
			listeners: {"show": function() {
					//设置焦点,不能直接使用focus(),可能在show之后还需要执行其他的resize操作,导致焦点丢失
	                this.textArea.focus.defer(10, this.textArea);
	            },scope:this
        	}
	    });
	}
});
