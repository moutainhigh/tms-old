Ext.ns('uft.extend.form');
/**
 * 点击大文本参照后打开的窗口，用于输入大文本
 * 供uft.extend.form.BigTextField调用，不能单独使用
 * @class uft.extend.form.BigTextWindow
 * @extends Ext.Window
 */
uft.extend.form.BigTextWindow= Ext.extend(Ext.Window, {
	shadow : false,
	width :600,
	height :400,
	textValue : null,
	returnValue : null,
	constructor : function(config) {
		Ext.apply(this,config);
		var srcField = this.srcField;
		if(!this.extendItem){
			this.extendItem = srcField.extendItem;
		}
		
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
		if(this.extendItem){
			var h = this.extendItemHeight||100;
			this.textArea.setHeight(336-h);
			this.southPanel = new Ext.Panel({
				region : 'south',
				layout : 'fit',
				height : h,
				items : this.extendItem
			});
		}
		
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
		uft.extend.form.BigTextWindow.superclass.constructor.call(this, {
			title : '文本显示',
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
			layout : this.extendItem?'border':'fit',
			items : this.extendItem?[this.formPanel,this.southPanel]:[this.formPanel],
			buttons : btns,
			defaultButton : 0,
			listeners: {"show": function() {
					//设置焦点,不能直接使用focus(),可能在show之后还需要执行其他的resize操作,导致焦点丢失
	                this.textArea.focus.defer(10, this.textArea);
	            },scope:this
        	}
	    });
	},
	//点击确定按钮
	process : function(){
		this.fireEvent('beforesubmit',this);
		this.returnValue = this.textArea.getValue();
		this.fireEvent('aftersubmit',this);
		//大文本的处理比较简单，而且每个单元格的文本都不一样，不能使用同一个对象，
		//这个跟参照不同，这里直接关闭掉
		this.close();
	},
	close : function(){
		uft.extend.form.BigTextWindow.superclass.close.call(this);
		if(this.extendItem){
			for(var i=0;i<this.extendItem.length;i++){
				this.extendItem[i].rendered = false;
			}
		}
	}
});
