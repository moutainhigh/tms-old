Ext.namespace("uft.extend.form");
/**
 * 默认【年-月-日】格式的日期输入框，父类的默认格式是【年/月/日】
 * @class uft.extend.form.DateField
 * @extends Ext.form.DateField
 */
uft.extend.form.DateField = Ext.extend(Ext.form.DateField, {
	isFocusAfterSubmit : false, //选择后是否得到焦点
	format : 'Y-m-d',
	altFormats : "m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|ymd|Ymd|d|Y-m-d|n-j|n/j",
	constructor : function(config) {
		Ext.apply(this, config);
		uft.extend.form.DateField.superclass.constructor.call(this);
	},
	/**
	 * 一个一个返回日期字符串的方法
	 * @return {}
	 */
	getValueAsString : function(){
		var value = this.getValue();
		return value?value.dateFormat(this.format):''; 
	},
	onTriggerClick : function(){
		if(this.readOnly){
            return;
        }	
        uft.extend.form.DateField.superclass.onTriggerClick.call(this);
	},
    onSelect: function(m, d){
        this.setValue(d);
        this.fireEvent('select', this, d);
        this.menu.hide();
    	if(!this.isFocusAfterSubmit){
    		if(this.gridEditor){
    			//若是可编辑表格，此时单元格已得到焦点
    			this.gridEditor.grid.stopEditing();
    		}
    	}        
    },
    //从写这个方法是为了兼容日期时间格式
    setValue : function(date){
    	if(typeof(date) == 'string'){
    		if(date.length > 10){
				date = date.substring(0,10); //截取日期就行
			}
    	}
        return Ext.form.DateField.superclass.setValue.call(this, this.formatDate(this.parseDate(date)));
    }
});
Ext.reg('uftdatefield', uft.extend.form.DateField);