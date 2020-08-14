/**
 * 定义一个null 的field，只是为了在使用Ext.getCmp(id)返回Component的时候，如果是undefined的时候，可以直接返回这个对象，这样不至于报错
 */
uft.extend.form.NullField = Ext.extend(uft.extend.form.Hidden, {
	onRender : Ext.emptyFn,
	initEvents : Ext.emptyFn,
	/**
	 * 方法来源:Ext.form.Field
	 */
	getName : Ext.emptyFn,
	getId : Ext.emptyFn,
	getValue : Ext.emptyFn,
	setValue : Ext.emptyFn
});
Ext.reg('nullfield', uft.extend.form.NullField);