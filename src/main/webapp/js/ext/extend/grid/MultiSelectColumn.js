Ext.namespace('uft.extend.grid');

/**
 * combox列模型，在EditorGrid中使用，该列处于编辑模式时打开的是一个combox,对应的是MultiSelectField
 * @class uft.extend.grid.ComboColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.MultiSelectColumn = Ext.extend(Ext.grid.Column, {
    constructor: function(cfg) {
        uft.extend.grid.MultiSelectColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
			var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.MultiSelectColumnRenderer(field);
        }
    }
});

uft.extend.grid.MultiSelectColumnRenderer = function(multiSelect){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(!multiSelect){
			if(!this.dataIndex){
				//对于bufferView的行，此时还没有渲染到
				return null;
			}
			multiSelect = Ext.getCmp(this.dataIndex);
		}
		var labels = multiSelect.findLabels(value);
		return labels;
	};
};
Ext.apply(Ext.grid.Column.types, {
    multiselectcolumn: uft.extend.grid.MultiSelectColumn
});
