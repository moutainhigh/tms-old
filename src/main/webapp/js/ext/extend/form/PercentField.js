/**
 * 显示百分比的输入框，当获得焦点时，显示去掉百分号的数值，如98%，显示98，提交的数据转换成小数，比如98%，提交的是0.98
 * @class uft.extend.form.PercentField
 * @extends Ext.form.NumberField
 */
uft.extend.form.PercentField = Ext.extend(Ext.form.NumberField,  {
    getValue : function() {
    	var v = Ext.form.NumberField.superclass.getValue.call(this);
    	if(!v){
    		return null;
    	}
    	if(typeof(v)== 'string'){
    		if(v.indexOf('%') != -1){
    			v = v.substring(0,v.length-1);//去掉最后一个百分号
    		}
    		v = parseFloat(v);
    	}
        return this.fixPrecision(v/100);
    },
    //这里设置的值是0.98，实际就是98%
    setValue : function(v){
    	var v = this._getFormatValue(v);
    	Ext.form.NumberField.superclass.setValue.call(this,v);
    },
    _getFormatValue : function(v){
    	if(!v){
    		return null;
    	}
    	if(typeof(v) == 'string'){
    		if(v.indexOf('%') == -1){
    			//转成百分数
    			v = parseFloat(v);
    			v = v*100 + "%";
    		}
    	}else{
    		v =v*100 + "%";
    	}
    	return v;
    },
    getRawValue :function(){
    	var v = this.rendered ? this.el.getValue() : Ext.value(this.value, '');
    	if(v === this.emptyText){
			v = 0;
		}
    	if(v && typeof(v)== 'string'){
    		if(v.indexOf('%') != -1){
    			v = v.substring(0,v.length-1);//去掉最后一个百分号
    		}
    		v = parseFloat(v);
    	}
		return this.fixPrecision(v/100)
    },    
    preFocus : function(){
    	var v = this.getValue();
    	Ext.form.NumberField.superclass.setValue.call(this, v);
    	Ext.form.NumberField.superclass.preFocus.call(this);
    },
    beforeBlur : function(){
    	var v = Ext.form.NumberField.superclass.getValue.call(this);
    	v = this._getFormatValue(v);
    	Ext.form.NumberField.superclass.setValue.call(this, v);
    	Ext.form.NumberField.superclass.beforeBlur.call(this);
    }    
});
Ext.reg('percentfield', uft.extend.form.PercentField);