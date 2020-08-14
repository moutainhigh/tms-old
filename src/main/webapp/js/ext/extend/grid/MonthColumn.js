Ext.namespace('uft.extend.grid');

/**
 * 日期列模型，在EditorGrid中使用，当列处于编辑模式时，打开的是日期选择框
 * @class uft.extend.grid.MonthColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.MonthColumn = Ext.extend(Ext.grid.Column, {
    constructor: function(cfg) {
        uft.extend.grid.MonthColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.MonthRenderer(field);        	
        }
    }
});

uft.extend.grid.MonthRenderer = function(monthField){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(value==undefined){
			return '';
		}
  		if(value instanceof Date){
  			return value.dateFormat("Y-m");
  		}else{
  			if(value.length>7){
  				return value.substring(0,7);
  			}
  		}
  		return value;
	};
};
Ext.apply(Ext.grid.Column.types, {
    monthcolumn: uft.extend.grid.MonthColumn
});

