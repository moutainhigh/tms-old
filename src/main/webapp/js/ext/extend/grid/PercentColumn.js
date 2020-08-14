Ext.namespace('uft.extend.grid');
/**
 * 百分数的模型
 * @class uft.extend.grid.PercentColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.PercentColumn = Ext.extend(Ext.grid.Column, {
    constructor: function(cfg) {
        uft.extend.grid.PercentColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.PercentRenderer(field); 	
        }
    }
});
uft.extend.grid.PercentRenderer = function(field){
	return function(value,meta,record,rowIndex, colIndex, store){
		if(!value)
			return '0%';
		if(value && typeof(value) == 'string'){
    		if(value.indexOf('%') == -1){
    			//转成百分数
    			value = parseFloat(value);
    		}else{
    			return value;
    		}
    	}
    	return value*100 + "%";
	};
};
Ext.apply(Ext.grid.Column.types, {
    percentcolumn: uft.extend.grid.PercentColumn
});