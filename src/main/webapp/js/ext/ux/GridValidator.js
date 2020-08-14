Ext.namespace('Ext.ux', 'Ext.ux.plugins');

/**
 * EditorGrid validation plugin
 * Adds validation functions to the grid
 *
 * @author  Jozef Sakalos, aka Saki
 * @version 0.1
 *
 * Usage: 
 * grid = new Ext.grid.EditorGrid({plugins:new Ext.ux.plugins.GridValidator(), ...})
 */
Ext.ux.plugins.GridValidator = function(config) {
    this.init = function(grid) {
        Ext.apply(grid, {
            /**
             * Checks if a grid cell is valid
             * @param {Integer} col Cell column index
             * @param {Integer} row Cell row index
             * @return {Boolean} true = valid, false = invalid
             */
            isCellValid:function(col, row) {
                if(!this.colModel.isCellEditable(col, row)) {
                    return true;
                }
                var ed = this.colModel.getCellEditor(col, row);
                if(!ed) {
                    return true;
                }
                var record = this.store.getAt(row);
                if(!record) {
                    return true;
                }
                var field = this.colModel.getDataIndex(col);
                ed.field.setValue(record.data[field]);
                return ed.field.isValid(true);
            }

            /**
             * Checks if grid has valid data
             * @param {Boolean} editInvalid true to automatically start editing of the first invalid cell
             * @return {Boolean} true = valid, false = invalid
             */
            ,isValid:function(editInvalid) {
                var cols = this.colModel.getColumnCount();
                var rows = this.store.getCount();
                var r, c;
                var valid = true;
                if(this.onlyValidateSelected){
                	try{
                		var rs = this.getSelectionModel().getSelections();//行选中模式
                		rows = rs.length;
                		for(var i=0;i<rows;i++){
                			r = this.store.indexOf(rs[i]);//行号
		                    for(c = 0; c < cols; c++) {
		                        valid = this.isCellValid(c, r);
		                        if(!valid) {
		                            break;
		                        }
		                    }
		                    if(!valid) {
		                        break;
		                    }
                		}
                	}catch(e){//必须是行选中模式，如果是单元格选中模式则不支持
                	}
                }else{
	                for(r = 0; r < rows; r++) {
	                    for(c = 0; c < cols; c++) {
	                        valid = this.isCellValid(c, r);
	                        if(!valid) {
	                            break;
	                        }
	                    }
	                    if(!valid) {
	                        break;
	                    }
	                }
                }
                if(editInvalid && !valid) {
                    this.startEditing(r, c);
                }
                return valid;
            }
            /**
             * 返回单元格的错误信息
             */
            ,getFieldErrors:function(row,col) {
            	var errors = new Array();
                if(!this.colModel.isCellEditable(col, row)) {
                    return errors;
                }
                var ed = this.colModel.getCellEditor(col, row);
                if(!ed) {
                    return errors;
                }
                var record = this.store.getAt(row);
                if(!record) {
                    return errors;
                }
                var field = this.colModel.getDataIndex(col);
                ed.field.setValue(record.data[field]);
                return ed.field.getErrors();
            }
            /**
             * 返回表格的所有错误信息
             */
            ,getAllErrors : function(){
            	var errors = new Array();
                var cols = this.colModel.getColumnCount();
                var rows = this.store.getCount();
                var r, c;
                var valid = true;
                if(this.onlyValidateSelected){
                	try{
                		var rs = this.getSelectionModel().getSelections();//行选中模式
                		rows = rs.length;
		                for(var i = 0; i < rows; i++) {
		                    for(c = 0; c < cols; c++) {
		                    	r = this.store.indexOf(rs[i]);//行号
		                    	var error = this._getErrors(r,c);
		                		if(error){
		                			errors.push(error);	
		                		}
		                    }
		                }
                	}catch(e){//必须是行选中模式，如果是单元格选中模式则不支持
                	}
                }else{
	                for(r = 0; r < rows; r++) {
	                    for(c = 0; c < cols; c++) {
	                		var error = this._getErrors(r,c);
	                		if(error){
	                			errors.push(error);	
	                		}
	                    }
	                }                	
                }
                return errors;
            }
            ,_getErrors : function(r,c){
            	var ed = this.colModel.getCellEditor(c, r);
            	if(!ed){
            		return null;
            	}
            	var record = this.store.getAt(r);
                if(!record) {
                    return null;
                }
                var field = this.colModel.getDataIndex(c);
        		ed.field.setValue(record.data[field]);
        		var errors1=ed.field.getErrors();
        		if(errors1.length>0){
        			return '第'+(r+1)+'行['+this.colModel.columns[c].header+'],'+errors1[0]+'<br/>';
        		}
        		return null;
            }
        });
    };
};