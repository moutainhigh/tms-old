/**
 * 修复对于刚好是整数时没有出现小数点的问题，比如10，那么最终显示的是10.00
 * @class uft.extend.form.NumberField
 * @extends Ext.form.NumberField
 */
uft.extend.form.NumberField = Ext.extend(Ext.form.NumberField,  {
	style : "text-align: right",
	validationEvent : 'keyup',
	constructor : function(config){
		Ext.apply(this, config);
		if(this.decimalPrecision == 0){
			//整数,不允许输入小数部分
			this.allowDecimals = false;
		}
		uft.extend.form.NumberField.superclass.constructor.call(this);
	},	
    setValue : function(v) {
    	v = this._getFormatValue(v);
        return Ext.form.NumberField.superclass.setValue.call(this, v);
    },
    getValue : function() {
    	var v = this.getActualValue();
        return this.fixPrecision(this.parseValue(v));
    },
    getActualValue : function(){
    	var v = Ext.form.NumberField.superclass.getValue.call(this);
    	if(v && typeof(v)== 'string'){
    		v = v.replaceAll(',','');
    		v = parseFloat(v);
    	}
    	return v;
    },
    preFocus : function(){
    	var v = this.getActualValue();
    	Ext.form.NumberField.superclass.setValue.call(this, v);
    	Ext.form.NumberField.superclass.preFocus.call(this);
    },
    beforeBlur : function(){
    	var v = Ext.form.NumberField.superclass.getValue.call(this);
    	v = this._getFormatValue(v);
    	Ext.form.NumberField.superclass.setValue.call(this, v);
    	Ext.form.NumberField.superclass.beforeBlur.call(this);
    },
    getRawValue :function(){
    	var v = this.rendered ? this.el.getValue() : Ext.value(this.value, '');
    	if(v === this.emptyText){
			v = '';
		}
    	if(v && typeof(v)== 'string'){
    		v = v.replaceAll(',','');
    		v = parseFloat(v);
    	}
		return v;
    },
    _getFormatValue : function(v){
    	if(this.format){
    		v = Ext.util.Format.number(v,this.format);
    	}else{
	        v = this.fixPrecision(v);
	    	v = Ext.isNumber(v) ? v : parseFloat(String(v).replace(this.decimalSeparator, "."));
	    	v = v.toFixed(this.decimalPrecision); //补充两位小数
	        v = isNaN(v) ? '' : String(v).replace(".", this.decimalSeparator);
    	}
    	return v;
    } 
});
Ext.reg('uftnumberfield', uft.extend.form.NumberField);