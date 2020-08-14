uft.extend.form.Hidden = Ext.extend(Ext.form.Hidden, {
    hidden : true, //hidden组件默认hidden为true，Ext默认的组件是false
    editable : false,//hidden组件默认editable为false，Ext默认的组件是undefined
    /**
     * 以下方法是参照域的方法，定义这些方法是因为当模板中配置为隐藏域后，
     * 系统直接识别为ufthidden，这样jsp可能使用了以下方法，这样不至于引起js错误
     * 方法来源:HeaderRefField,BodyRefField
     * @type 
     */
    addExtendParams : Ext.emptyFn,
    getPk : function(){
    	//getPk目前是在参照中使用，而且跟getValue返回相同的值
    	return this.getValue();
    },
    getText : Ext.emptyFn,
    getCode : Ext.emptyFn,
    getRefValue : Ext.emptyFn,
    
    /**
     * 方法来源:DateField
     * @type 
     */
    getValueAsString : Ext.emptyFn
});
Ext.reg('ufthidden', uft.extend.form.Hidden);