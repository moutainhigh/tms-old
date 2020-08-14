Ext.namespace('uft.extend.form');
/**
 * 大文本参照，只用于可编辑表体，表头的大文本可以使用textArea
 * 与uft.extend.form.BigTextWindow结合使用
 * 
 * @class uft.extend.form.BigTextField
 * @extends Ext.form.TriggerField
 */
uft.extend.form.BigTextField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	triggerClass : 'x-form-search-trigger',
	isFocusAfterSubmit : false,
	constructor : function(config) {
		Ext.apply(this, config);
		uft.extend.form.BigTextField.superclass.constructor.call(this);
		//监听Enter按钮
		this.on('specialkey', function (me,e){
			if(this.isValid()){
		        if (e.getKey() == 13 || e.getKey() == 9){
		           this.setValue(this.el.dom.value);
		        }
			}
	    }, this);
	},
    onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
        this.triggerBlur(); //点击后调用失去焦点动作,Ext.Window不是真正的modal窗口
        //每次都新建一个窗口，然后colse掉，因为每个单元格的对象都会不一样，跟参照不同
		var bigTextWin = new uft.extend.form.BigTextWindow({
			srcField : this,
			textValue : this.gridEditor.startValue
		}); 
		bigTextWin.on('aftersubmit',function(){ //大文本输入窗口点击确定后触发的事件
			//只有处于编辑状态，下面的setValue才有效
			if(this.gridEditor){
				var grid = this.gridEditor.grid;
				grid.startEditing(this.gridEditor.row, this.gridEditor.col);
			}
            this.setValueAfterSubmit(bigTextWin.returnValue);
	    },this);
    	bigTextWin.show();
    },

    getValue : function(){
        return Ext.isDefined(this.value) ? this.value : '';
    },

    clearValue : function(){
        this.setRawValue('');
        this.value = '';
    },
    setValue : function(value){
        uft.extend.form.BigTextField.superclass.setValue.call(this, value);
        this.value = value;
        return this;
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
    },
    setValueAfterSubmit : function(value){
    	this.setValue(value);
    	if(!this.isFocusAfterSubmit){
    		if(this.gridEditor){
				this.gridEditor.grid.stopEditing();
    		}
    	}
    }
});
Ext.reg("bigtextfield",uft.extend.form.BigTextField);