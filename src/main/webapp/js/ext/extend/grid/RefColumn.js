Ext.namespace('uft.extend.grid');

/**
 * 参照列模型，在EditorGrid中使用，当列处于编辑模式时，打开的是参照
 * @class uft.extend.grid.RefColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.RefColumn = Ext.extend(Ext.grid.Column, {
    constructor: function(cfg) {
        uft.extend.grid.RefColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.RefRenderer(field);         	
        }
    }
});

uft.extend.grid.RefRenderer = function(refField){
	return function(value,meta,record,rowIndex, colIndex, store){
		return value;
	};
};
Ext.apply(Ext.grid.Column.types, {
    refcolumn: uft.extend.grid.RefColumn
});
