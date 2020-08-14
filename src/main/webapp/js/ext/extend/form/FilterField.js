/**
 * 用于过滤的field，目前只用在Tree中
 * @class uft.extend.form.FilterField
 * @extends Ext.form.TriggerField
 */
uft.extend.form.FilterField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "20", autocomplete: "off"},
	triggerClass : 'x-form-remove-trigger',
	constructor : function(config) {
		Ext.apply(this, config);
		uft.extend.form.FilterField.superclass.constructor.call(this);
		this.on('render', function() {
			var map = new Ext.KeyMap(this.el, [{
				key : Ext.EventObject.ENTER,
				scope : this.scope || this,
				fn : this.fn
			}]);
			map.stopEvent = true;
		}, this);
	},
	onTriggerClick : function(){
		if(this.fn){
			this.fn.call(this.scope||this);
		}else{
			this.setValue('');
		}
		this.fireEvent('clear',this);
	},
	onBlur : function(){
		var value = this.getValue();
		if(value && value.length > 0){
			if(this.fn){
				this.fn.call(this.scope||this);
			}
		}
	}
});
Ext.reg('filterfield', uft.extend.form.FilterField);