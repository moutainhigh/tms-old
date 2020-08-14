Ext.namespace('uft.extend.grid');

/**
 * combox列模型，在EditorGrid中使用，该列处于编辑模式时打开的是一个combox
 * @class uft.extend.grid.ComboColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.ComboColumn = Ext.extend(Ext.grid.Column, {
    constructor: function(cfg) {
        uft.extend.grid.ComboColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.ComboRenderer(field);        	
        }
    }
});

uft.extend.grid.ComboRenderer = function(combo){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(!combo){
			combo = this.editor;
		}
		if(!combo){
			if(!this.dataIndex){
				//对于bufferView的行，此时还没有渲染到
				return null;
			}
			combo = Ext.getCmp(this.dataIndex);
			if(!combo){
				//卡片页上没有该字段，或者根本没有生成卡片页
				return value;
			}
		}
		var record = combo.findRecord(combo.valueField, value);
		var result = record ? record.get(combo.displayField) : combo.valueNotFoundText;
		if(!result && combo.editable){
			//2013-8-19可以直接输入的 下拉框
			return value;
		}
		return result;
	};
};
Ext.apply(Ext.grid.Column.types, {
    combocolumn: uft.extend.grid.ComboColumn
});
