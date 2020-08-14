/*!
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
/**
 * @class Ext.grid.RowNumberer
 * This is a utility class that can be passed into a {@link Ext.grid.ColumnModel} as a column config that provides
 * an automatic row numbering column.
 * <br>Usage:<br>
 <pre><code>
 // This is a typical column config with the first column providing row numbers
 var colModel = new Ext.grid.ColumnModel([
    new Ext.grid.RowNumberer(),
    {header: "Name", width: 80, sortable: true},
    {header: "Code", width: 50, sortable: true},
    {header: "Description", width: 200, sortable: true}
 ]);
 </code></pre>
 * @constructor
 * @param {Object} config The configuration options
 */
 //参考http://www.sencha.com/forum/showthread.php?84251-GridRowNumberer，将其改造成既是一个column对象又是一个插件
 //自动更新行号
Ext.grid.RowNumberer = Ext.extend(Object, {
    /**
     * @cfg {String} header Any valid text or HTML fragment to display in the header cell for the row
     * number column (defaults to '').
     */
    header: "",
    /**
     * @cfg {Number} width The default width in pixels of the row number column (defaults to 23).
     */
    width: 23,
    /**
     * @cfg {Boolean} sortable True if the row number column is sortable (defaults to false).
     * @hide
     */
    sortable: false,
    
    constructor : function(config){
        Ext.apply(this, config);
        if(this.rowspan){
            this.renderer = this.renderer.createDelegate(this);
        }
    },

    // private
    fixed:true,
    hideable: false,
    menuDisabled:true,
    dataIndex: '',
    id: 'numberer',
    rowspan: undefined,
    // private
    renderer : function(v, p, record, rowIndex){
        if(this.rowspan){
            p.cellAttr = 'rowspan="'+this.rowspan+'"';
        }
        return rowIndex+1;
    },
    //FIXME 改造成插件
    isColumn: false,
    init : function(grid){
        this.grid = grid;
        grid.mon(grid.getView(),{
            rowremoved: this.onRowChange,
            rowsinserted: this.onRowChange,
            scope: this
        });
    },
    onRowChange : function (view, rowIndex){
        var colIndex = this.grid.getColumnModel().getIndexById(this.id);
        var sel = "div.x-grid3-cell-inner.x-grid3-col-" + this.id;
        for (var i = rowIndex, n = view.getRows().length; i < n; ++i){
            var cell = Ext.fly(view.getCell(i, colIndex)).first(sel, true);
            cell.innerHTML = "" + (i+1);
        }
    }
});