Ext.namespace('uft.extend.grid');

/**
 * 日期列模型，在EditorGrid中使用，当列处于编辑模式时，打开的是日期选择框
 * 原有的Ext.grid.DateTimeColumn在IE下有问题
 * @class uft.extend.grid.DateTimeColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.DateTimeColumn = Ext.extend(Ext.grid.Column, {
	format : 'Y-m-d H:i', //默认格式
    constructor: function(cfg) {
        uft.extend.grid.DateTimeColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.DateTimeRenderer(field); 	
        }
    }
});
uft.extend.grid.DateTimeRenderer = function(dateTimeField){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(value==undefined || value=='')
			return "";
		if(typeof value != "string"){
			value = value.dateFormat(this.format); 
  		} 
  		
  		if(value.length > 16){
			value = value.substring(0,16);
		}
		return value;
	};
};
Ext.apply(Ext.grid.Column.types, {
    datetimecolumn: uft.extend.grid.DateTimeColumn
});