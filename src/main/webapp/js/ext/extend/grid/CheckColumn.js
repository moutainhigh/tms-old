Ext.namespace('uft.extend.grid');

/**
 * checkbox选择列，
 * 1、对于不可编辑表格，不能编辑；
 * 2、对于可编辑表格，判断该列是否可编辑
 * @class uft.extend.grid.CheckColumn
 * @extends Ext.grid.Column
 */
uft.extend.grid.CheckColumn = Ext.extend(Ext.grid.Column, {
	intervalID : null,
    constructor: function(cfg) {
        uft.extend.grid.CheckColumn.superclass.constructor.call(this, cfg);
        //FIXME 可以自定义renderer
        if(!cfg.renderer){//这里不能判断this.renderer,因为默认已经有一个
	        var field= null;
	        if(this.editor){
	        	field = this.editor.field ? this.editor.field : this.editor;
	        }
	        this.renderer = uft.extend.grid.CheckRenderer(field);
        }
    },
    processEvent : function(name, e, grid, rowIndex, colIndex){
    	if(!(grid instanceof Ext.grid.EditorGridPanel)){
    		//不是可编辑表格
    		return;
    	}
		//当column定义了editable：true时
		if(grid.originalColumns[colIndex].editable==false){
			return;
		}
        if (name == 'mousedown' || name == 'click') {
			if(this.intervalID!=null){
				clearInterval(this.intervalID);
			}
			this.intervalID = setUftInterval(this.doCheck,this,200,e,grid,rowIndex,colIndex); //注意这里第二个参数this非常关键,它是执行函数的scope
        } else {
            return Ext.grid.ActionColumn.superclass.processEvent.apply(this, arguments);
        }
    },
    doCheck : function(e, grid, rowIndex, colIndex){
    	if(grid.afterEditComplete !== false){
    		grid.stopEditing();
			clearInterval(this.intervalID);
			
	    	var record = grid.store.getAt(rowIndex);
	    	var field = grid.colModel.getDataIndex(colIndex);
	    	var v = record.data[this.dataIndex];
	        var checked = !(v === true || v === 'true' || v=='Y' || v==1 );
	        var value = checked?"Y":"N";
	    	var e = {
	    		grid : grid,
	    		row : rowIndex,
	    		column : colIndex,
	    		record : record,
	    		field : field,
	    		value : value
	    	};
	    	if(grid.afterEditComplete !== false){
		    	if(grid.fireEvent("beforeedit", e) !== false){
		            record.set(this.dataIndex, value);
		        	grid.fireEvent("afteredit", e);
		    	}
	    	}
	    	return false; // Cancel row selection.
    	}else{
    		return;
    	}
    }
});
uft.extend.grid.CheckRenderer = function(field){
	return function(value,meta,record,rowIndex, colIndex, store){
		meta.css += ' x-grid3-check-col-td';
        var checked = (value === true || value === 'true' || value=='Y' || value==1 );
        return String.format('<div class="x-grid3-check-col{0}">&#160;</div>', checked ? '-on' : '');		
	};
};
Ext.apply(Ext.grid.Column.types, {
    checkcolumn: uft.extend.grid.CheckColumn
});