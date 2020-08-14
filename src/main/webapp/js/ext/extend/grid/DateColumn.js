Ext.namespace('uft.extend.grid');

/**
 * 日期列模型，在EditorGrid中使用，当列处于编辑模式时，打开的是日期选择框
 * 原有的Ext.grid.DateColumn在IE下有问题
 * @class uft.extend.grid.DateColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.DateColumn = Ext.extend(Ext.grid.Column, {
	format : 'Y-m-d', //默认格式
    constructor: function(cfg) {
        uft.extend.grid.DateColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.DateRenderer(field);	
        }
    }
});
uft.extend.grid.DateRenderer = function(dateField){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(value==undefined || value=='')
			return "";
		if(typeof value == "string"){
			if(value.length > 10){
				value = value.substring(0,10); //截取日期就行
			}
   			return value; 
  		} 
  		return value.dateFormat(this.format); 
	};
};
Ext.apply(Ext.grid.Column.types, {
    datecolumn: uft.extend.grid.DateColumn
});