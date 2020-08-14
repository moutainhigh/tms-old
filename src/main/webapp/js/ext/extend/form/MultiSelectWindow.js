Ext.ns('uft.extend.form');
/**
 * 点击下拉参照后，弹出的下拉选择窗口，支持多选
 * 当点击取消、或者选择后点击确定，此时是把窗口销毁掉。因为每次打开的时候都要重新设置值。这个与参照窗口不一样
 * 参数:
 * items checkbox选项
 * 供uft.extend.form.MultiSelectField调用，不能单独使用
 * @class uft.extend.form.MultiSelectWindow
 * @extends Ext.Window
 */
uft.extend.form.MultiSelectWindow= Ext.extend(Ext.Window, {
	shadow : false,
	width:250,
	height:300,
	returnValue : null,
	srcField : null,
	resetValue : true, //是否重新设置值，该组件目前支持单独使用，可能不需要重新设置值
	constructor : function(config) {
		Ext.apply(this,config);
		this.revertStatus(this.items);
		this.formPanel = new uft.extend.form.FormPanel({
			autoScroll:true,
			labelWidth:5,
			frame:true,
			border:false,
			autoWidth:true,
			items : this.items
		});
		
		var saBox = new uft.extend.form.Checkbox({
			inputValue : -999,
			boxLabel : '全选'
		});
		saBox.on('check',function(checkbox,value){
			if(value == 'Y'){
				//全选
				this.formPanel.items.each(function(f) {
					f.setValue(true);
				});
			}else{
				//全部取消
				this.formPanel.items.each(function(f) {
					f.setValue(false);
				});
			}
		},this);
		var btns = [saBox,'->',{
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
				this.hide();
			}
		}];
		
		uft.extend.form.MultiSelectWindow.superclass.constructor.call(this, {
			id : this.srcField.refWinId,
			title : this.srcField.fieldLabel || '请选择',
			width :300,
			height :350,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			modal : true,
			resizable : false,
			border : false,
			layout : 'fit',
			items : [this.formPanel],
			buttonAlign : 'left',
			buttons : btns
	    });
	},
	initComponent : function() {
        this.addEvents(
            /**
             * 提交表单前触发
             * @event expand
             * @param {uft.extend.form.MultiSelectWindow} 
             */
            'beforesubmit',
            /**
             * 提交表单后触发
             * @event expand
             * @param {uft.extend.form.MultiSelectWindow} 
             */
            'submit'
        );
        uft.extend.form.MultiSelectWindow.superclass.initComponent.call(this);
    },
	//点击确定按钮
	process : function(){
		this.fireEvent('beforesubmit',this);
		var values = this.formPanel.getForm().getValues();
		var valueArr=[];
		for(key in values){
			valueArr.push(values[key]);
		}
		if(valueArr.length>0){
			this.returnValue='['+valueArr.toString()+']';
		}else{
			this.returnValue='';
		}
		this.fireEvent('submit',this);
		this.hide();
	},
	show : function(){
		uft.extend.form.MultiSelectWindow.superclass.show.call(this);
		this.revertStatus(this.formPanel.items.items);
	},
	//还原items的状态，this.items会在window创建后被重置，所以这里作为参数
	revertStatus : function(items){
		if(!items){
			return;
		}
		if(this.resetValue !== false){
			//设置默认值
			for(var j=0;j<items.length;j++){
				items[j].checked=false;
			}		
			this.value = this.srcField.getValue();
			if(this.value){
		    	if(this.value.indexOf('[')!=0){
		    		this.value = '[' + this.value + ']';
		    	}
		    	this.value = this.value.substring(1,this.value.length-1); //过滤前后两个[]
			    var arr = this.value.split(",");
			    for(var i=0;i<arr.length;i++){
					for(var j=0;j<items.length;j++){
						if(items[j].inputValue == arr[i]){
							items[j].checked=true;
							break;
						}
					}
			    }
			}else{
				//没有值，那么取消打钩
				for(var j=0;j<items.length;j++){
					if(items[j].setValue){//如果还不存在，说明还没实例化
						items[j].setValue(false);
					}
				}
			}
		}
	}
});
