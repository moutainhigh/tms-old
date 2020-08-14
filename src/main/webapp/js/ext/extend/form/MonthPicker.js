Ext.namespace('uft.extend.form'); 
/**
 * 年月选择框
 * @class uft.extend.form.MonthPicker
 * @extends Ext.form.DateField
 */
uft.extend.form.MonthPicker = Ext.extend(Ext.form.DateField, {
	format : 'Y-m',
	constructor : function(config) {
		Ext.apply(this, config);
		uft.extend.form.MonthPicker.superclass.constructor.call(this,{
			id: this.id,
	        fieldLabel: this.fieldLabel,
	        plugins: 'monthPickerPlugin',
	        format: this.format,
	        editable: false
		});
	}
});
Ext.reg('monthpicker', uft.extend.form.MonthPicker);  