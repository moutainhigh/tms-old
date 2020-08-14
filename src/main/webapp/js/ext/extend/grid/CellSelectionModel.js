Ext.ns('uft.extend.grid');

/**
 * 单元格选择模式，在定义Ext Grid中使用
 * 支持使用Enter快捷键横向切换单元格
 * @class uft.extend.grid.CellSelectionModel
 * @extends Ext.grid.CellSelectionModel
 */
uft.extend.grid.CellSelectionModel = Ext.extend(Ext.grid.CellSelectionModel, {
	newRowWhenWalkInLastCell : false,//在最后一个单元格中点击Enter时，是否自动增加一行
    handleMouseDown : function(g, row, cell, e){
        if(e.button !== 0 || this.isLocked()){
            return;
        }
        this.select(row, cell);
        //鼠标点击的时候直接编辑,但是：若加在这边，在同一个列的两个参照进行切换时会出现没有触发onBlur事件的情况
        //这种方式是不好的，但是目前没有更好的解决方案，如果不加这段，客户觉得操作比较麻烦，因为有时候需要多次点击才能进入编辑。
//        if(g.isEditor && g.editing){
//        	g.startEditing(row, cell); 
//        }
    },	
    onEditorKey: function(field, e){
        if(e.getKey() == e.TAB || e.getKey() == e.ENTER){
            this.handleKeyDown(e);
        }
    },

    /** @ignore */
    handleKeyDown : function(e){
    	//FIXME 这里不阻止其他按键的录入，便于如果选择了一个单元格，按字符和数字键直接进入编辑
//        if(!e.isNavKeyPress()){
//            return;
//        }
        
        var k = e.getKey(),
            g = this.grid,
            s = this.selection,
            sm = this,
            walk = function(row, col, step){
                return g.walkCells(
                    row,
                    col,
                    step,
                    g.isEditor && g.editing ? sm.acceptsNav : sm.isSelectable, // *** handle tabbing while editorgrid is in edit mode
                    sm
                );
            },
            cell, newCell, r, c, ae;
            
        switch(k){
            case e.ESC:
            case e.PAGE_UP:
            case e.PAGE_DOWN:
                // do nothing
                break;
            default:
            	//2012-10-25,表格支持Ctrl+C来复制text，所以如果有且只有这2个按键，那么不阻止浏览器默认行为
            	if(e.CTRL==17 && e.keyCode == 67){
            	}else{
            		// *** call e.stopEvent() only for non ESC, PAGE UP/DOWN KEYS
                	e.stopEvent();
            	}
                break;
        }

        if(!s){
            cell = walk(0, 0, 1); // *** use private walk() function defined above
            if(cell){
                this.select(cell[0], cell[1]);
            }
            return;
        }

        cell = s.cell;  // currently selected cell
        r = cell[0];    // current row
        c = cell[1];    // current column
        
        switch(k){
            case e.TAB:
                if(e.shiftKey){
                    newCell = walk(r, c - 1, -1);
                }else{
                    newCell = walk(r, c + 1, 1);
	                //最后一行，再点击TAB键，自动增加一行
                    if(this.newRowWhenWalkInLastCell !== false){
		                if(!newCell&&r==g.getStore().getCount()-1){
		                	if(g.editable){
			                	g.addRow();
				                newCell = walk(r, c + 1, 1);
		                	}
		                }        
                    }
                }
                break;
            case e.DOWN:
                newCell = walk(r + 1, c, 1);
                break;
            case e.UP:
                newCell = walk(r - 1, c, -1);
                break;
            case e.RIGHT:
                newCell = walk(r, c + 1, 1);
                break;
            case e.LEFT:
                newCell = walk(r, c - 1, -1);
                break;
            case e.ENTER:
                if(e.shiftKey){
                    newCell = walk(r, c - 1, -1);
                }else{
                    newCell = walk(r, c + 1, 1);
                    if(this.newRowWhenWalkInLastCell !== false){
		                //最后一行，再点击ENTER键，自动增加一行
		                if(!newCell&&r==g.getStore().getCount()-1){
		                	if(g.editable){
		                		g.addRow();
			                	newCell = walk(r, c + 1, 1);
		                	}
		                }                     	
                    }
                }
                break;
        }
        //数字键和字母键,直接切换到录入状态，跟Excel类似
        if(e.shiftKey || e.ctrlKey){
        	//此时同时按住了ctrl或shift键，这属于功能键的范围了。
        	return;
        }
        var initValue = null;
        if((k > 47 && k < 58) || (k > 64 && k < 91)){
        	newCell = cell;
        	initValue = String.fromCharCode(k).toLowerCase();
        }

        if(newCell){
            // *** reassign r & c variables to newly-selected cell's row and column
            r = newCell[0];
            c = newCell[1];

            this.select(r, c); // *** highlight newly-selected cell and update selection

            if(g.isEditor && g.editing){
                ae = g.activeEditor;
                if(ae && ae.field.triggerBlur){
                    ae.field.triggerBlur();
                }
            }
            if(initValue){
	            g.startEditing(r, c);//进入编辑状态
	            var ed = g.colModel.getCellEditor(c, r);//单元格的编辑器
	            if(ed && ed.field && ed.field.el){
	            	ed.field.el.dom.value=initValue;
	            }   	            
            }
        }
    }  
})

