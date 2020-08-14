/*!
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
/**
 * @class Ext.grid.CheckboxSelectionModel
 * @extends Ext.grid.RowSelectionModel
 * A custom selection model that renders a column of checkboxes that can be toggled to select or deselect rows.
 * @constructor
 * @param {Object} config The configuration options
 */
Ext.grid.CheckboxSelectionModel = Ext.extend(Ext.grid.RowSelectionModel, {

    /**
     * @cfg {Boolean} checkOnly <tt>true</tt> if rows can only be selected by clicking on the
     * checkbox column (defaults to <tt>false</tt>).
     */
    /**
     * @cfg {String} header Any valid text or HTML fragment to display in the header cell for the
     * checkbox column.  Defaults to:<pre><code>
     * '&lt;div class="x-grid3-hd-checker">&#38;#160;&lt;/div>'</tt>
     * </code></pre>
     * The default CSS class of <tt>'x-grid3-hd-checker'</tt> displays a checkbox in the header
     * and provides support for automatic check all/none behavior on header click. This string
     * can be replaced by any valid HTML fragment, including a simple text string (e.g.,
     * <tt>'Select Rows'</tt>), but the automatic check all/none behavior will only work if the
     * <tt>'x-grid3-hd-checker'</tt> class is supplied.
     */
    header : '<div class="x-grid3-hd-checker">&#160;</div>',
    /**
     * @cfg {Number} width The default width in pixels of the checkbox column (defaults to <tt>20</tt>).
     */
    width : 20,
    /**
     * @cfg {Boolean} sortable <tt>true</tt> if the checkbox column is sortable (defaults to
     * <tt>false</tt>).
     */
    sortable : false,

    // private
    menuDisabled : true,
    fixed : true,
    hideable: false,
    dataIndex : '',
    id : 'checker',
    isColumn: true, // So that ColumnModel doesn't feed this through the Column constructor

    constructor : function(){
        Ext.grid.CheckboxSelectionModel.superclass.constructor.apply(this, arguments);
        if(this.checkOnly){
            this.handleMouseDown = Ext.emptyFn;
        }
    },

    // private
    initEvents : function(){
        Ext.grid.CheckboxSelectionModel.superclass.initEvents.call(this);
        this.grid.on('render', function(){
        	var view = this.grid.getView();
        	if(view.lockedInnerHd){
        		//锁定模式
        		Ext.fly(view.lockedInnerHd).on('mousedown', this.onHdMouseDown, this);
        	}else{
        		Ext.fly(view.innerHd).on('mousedown', this.onHdMouseDown, this);	
        	}
        }, this);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     */
    processEvent : function(name, e, grid, rowIndex, colIndex){
        if (name == 'mousedown') {
            this.onMouseDown(e, e.getTarget());
            return false;
        } else {
            return Ext.grid.Column.prototype.processEvent.apply(this, arguments);
        }
    },
    //鼠标点击行事件
    handleMouseDown : function(g, rowIndex, e){
    	//FIXME 这里定义一种行选择模型，专门给参照多选用的
    	if(this.refMultiSelectionModel){
	    	e.ctrlKey = true;
	    	e.shiftKey = false;
    	}
    	Ext.grid.CheckboxSelectionModel.superclass.handleMouseDown.call(this,g,rowIndex,e);
    },
    // private
    onMouseDown : function(e, t){
        if(e.button === 0 && t.className == 'x-grid3-row-checker'){ // Only fire if left-click
            e.stopEvent();
            var row = e.getTarget('.x-grid3-row');
            if(row){
                var index = row.rowIndex;
                if(this.isSelected(index)){
                    this.deselectRow(index);
                }else{
                    this.selectRow(index, true);
                    this.grid.getView().focusRow(index);
                }
            }
        }
    },

    // private
    onHdMouseDown : function(e, t) {
        if(t.className == 'x-grid3-hd-checker'){
            e.stopEvent();
            var hd = Ext.fly(t.parentNode);
            var isChecked = hd.hasClass('x-grid3-hd-checker-on');
            if(isChecked){
                hd.removeClass('x-grid3-hd-checker-on');
                this.clearSelections();
            }else{
                hd.addClass('x-grid3-hd-checker-on');
                this.selectAll();
            }
        }
    },

    // private
    renderer : function(v, p, record){
        return '<div class="x-grid3-row-checker">&#160;</div>';
    },
    //FIXME xuqc 将header的checkbox也取消打勾
    clearSelections : function(fast){
        Ext.grid.CheckboxSelectionModel.superclass.clearSelections.call(this,fast);
//        var hdEle = Ext.select('*[class=x-grid3-hd-checker]').elements[0];
//        if(hdEle){
//        	var hd = Ext.fly(hdEle.parentNode);
//        	hd.removeClass('x-grid3-hd-checker-on');
//        }
    },
    //FIXME xuqc 将header的checkbox也打勾
    selectAll : function(){
        Ext.grid.CheckboxSelectionModel.superclass.selectAll.call(this);
        //2015-1-13 列表下，如果打开参照，选择参照的全选按钮，此时会把表格的全选按钮也打钩了
//        var hdEle = Ext.select('*[class=x-grid3-hd-checker]').elements[0];
//        if(hdEle){
//        	var hd = Ext.fly(hdEle.parentNode);
//        	hd.addClass('x-grid3-hd-checker-on');
//        }
    },
    onEditorSelect: function(row, lastRow){
        if(lastRow != row && !this.checkOnly){
            this.selectRow(row); // *** highlight newly-selected cell and update selection
        }
    },
    //FIXME 当处于可编辑表格时,返回编辑器,实际上不需要编辑器，定义这个方法避免出现错误
    getCellEditor : function(col,row){
    	return null;
    }
});