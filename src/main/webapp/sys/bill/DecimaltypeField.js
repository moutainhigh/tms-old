Ext.namespace('uft.bill');
/**
 * 单据模板初始化页面，当数据类型选择小数时，打开数字类型设置
 * 与uft.bill.DecimaltypeWindow结合使用
 * 
 * @class uft.bill.DecimaltypeField
 * @extends Ext.form.TriggerField
 */
uft.bill.DecimaltypeField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	triggerClass : 'x-form-search-trigger',
	constructor : function(config) {
		Ext.apply(this, config);
		uft.bill.DecimaltypeField.superclass.constructor.call(this);
	},
    onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
        this.triggerBlur(); //点击后调用失去焦点动作,Ext.Window不是真正的modal窗口
        //每次都新建一个窗口，然后colse掉，因为每个单元格的对象都会不一样，跟参照不同
		var decimaltypeWin = new uft.bill.DecimaltypeWindow({
			srcField : this
		}); 
		decimaltypeWin.on('aftersubmit',function(value){ //大文本输入窗口点击确定后触发的事件
			//只有处于编辑状态，下面的setValue才有效
			if(this.gridEditor){
				var grid = this.gridEditor.grid;
				grid.startEditing(this.gridEditor.row, this.gridEditor.col);
			}
            this.setValueAfterSubmit(value);
	    },this);
    	decimaltypeWin.show();
    },

    getValue : function(){
        return Ext.isDefined(this.value) ? this.value : '';
    },

    clearValue : function(){
        this.setRawValue('');
        this.value = '';
    },
    setValue : function(value){
        uft.bill.DecimaltypeField.superclass.setValue.call(this, value);
        this.value = value;
        return this;
    },
    setValueAfterSubmit : function(value){
    	this.setValue(value);
    	if(!this.isFocusAfterSubmit){
    		if(this.gridEditor){
				this.gridEditor.grid.stopEditing();
    		}
    	}
    },
    setValueOnBlur : function(){
    	this.setValue(this.el.dom.value);
    },
    /**
     * 继承该方法，不抛出change事件
     */
    triggerBlur : function(){
        this.mimicing = false;
        this.doc.un('mousedown', this.mimicBlur, this);
        if(this.monitorTab && this.el){
            this.un('specialkey', this.checkTab, this);
        }
        this.beforeBlur();
        if(this.focusClass){
            this.el.removeClass(this.focusClass);
        }
        this.hasFocus = false;
        if(this.validationEvent !== false && (this.validateOnBlur || this.validationEvent == 'blur')){
            this.validate();
        }
        this.setValueOnBlur();
        this.fireEvent('blur', this);
        this.postBlur();
        if(this.wrap){
            this.wrap.removeClass(this.wrapFocusClass);
        }
    }
});
Ext.reg("decimaltypefield",uft.bill.DecimaltypeField);