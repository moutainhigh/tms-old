/*!
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
/**
 * @class Ext.grid.EditorGridPanel
 * @extends Ext.grid.GridPanel
 * <p>This class extends the {@link Ext.grid.GridPanel GridPanel Class} to provide cell editing
 * on selected {@link Ext.grid.Column columns}. The editable columns are specified by providing
 * an {@link Ext.grid.ColumnModel#editor editor} in the {@link Ext.grid.Column column configuration}.</p>
 * <p>Editability of columns may be controlled programatically by inserting an implementation
 * of {@link Ext.grid.ColumnModel#isCellEditable isCellEditable} into the
 * {@link Ext.grid.ColumnModel ColumnModel}.</p>
 * <p>Editing is performed on the value of the <i>field</i> specified by the column's
 * <tt>{@link Ext.grid.ColumnModel#dataIndex dataIndex}</tt> in the backing {@link Ext.data.Store Store}
 * (so if you are using a {@link Ext.grid.ColumnModel#setRenderer renderer} in order to display
 * transformed data, this must be accounted for).</p>
 * <p>If a value-to-description mapping is used to render a column, then a {@link Ext.form.Field#ComboBox ComboBox}
 * which uses the same {@link Ext.form.Field#valueField value}-to-{@link Ext.form.Field#displayFieldField description}
 * mapping would be an appropriate editor.</p>
 * If there is a more complex mismatch between the visible data in the grid, and the editable data in
 * the {@link Edt.data.Store Store}, then code to transform the data both before and after editing can be
 * injected using the {@link #beforeedit} and {@link #afteredit} events.
 * @constructor
 * @param {Object} config The config object
 * @xtype editorgrid
 */
Ext.grid.EditorGridPanel = Ext.extend(Ext.grid.GridPanel, {
    /**
     * @cfg {Number} clicksToEdit
     * <p>The number of clicks on a cell required to display the cell's editor (defaults to 2).</p>
     * <p>Setting this option to 'auto' means that mousedown <i>on the selected cell</i> starts
     * editing that cell.</p>
     */
    clicksToEdit: 2,

    /**
    * @cfg {Boolean} forceValidation
    * True to force validation even if the value is unmodified (defaults to false)
    */
    forceValidation: false,

    // private
    isEditor : true,
    // private
    detectEdit: false,

    /**
     * @cfg {Boolean} autoEncode
     * True to automatically HTML encode and decode values pre and post edit (defaults to false)
     */
    autoEncode : false,

    /**
     * @cfg {Boolean} trackMouseOver @hide
     */
    // private
    trackMouseOver: false, // causes very odd FF errors

    // private
    initComponent : function(){
        Ext.grid.EditorGridPanel.superclass.initComponent.call(this);

        if(!this.selModel){
            /**
             * @cfg {Object} selModel Any subclass of AbstractSelectionModel that will provide the selection model for
             * the grid (defaults to {@link Ext.grid.CellSelectionModel} if not specified).
             */
            this.selModel = new Ext.grid.CellSelectionModel();
        }

        this.activeEditor = null;

        this.addEvents(
            /**
             * @event beforeedit
             * Fires before cell editing is triggered. The edit event object has the following properties <br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>grid - This grid</li>
             * <li>record - The record being edited</li>
             * <li>field - The field name being edited</li>
             * <li>value - The value for the field being edited.</li>
             * <li>row - The grid row index</li>
             * <li>column - The grid column index</li>
             * <li>cancel - Set this to true to cancel the edit or return false from your handler.</li>
             * </ul>
             * @param {Object} e An edit event (see above for description)
             */
            "beforeedit",
            /**
             * @event afteredit
             * Fires after a cell is edited. The edit event object has the following properties <br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>grid - This grid</li>
             * <li>record - The record being edited</li>
             * <li>field - The field name being edited</li>
             * <li>value - The value being set</li>
             * <li>originalValue - The original value for the field, before the edit.</li>
             * <li>row - The grid row index</li>
             * <li>column - The grid column index</li>
             * </ul>
             *
             * <pre><code>
grid.on('afteredit', afterEdit, this );

function afterEdit(e) {
    // execute an XHR to send/commit data to the server, in callback do (if successful):
    e.record.commit();
};
             * </code></pre>
             * @param {Object} e An edit event (see above for description)
             */
            "afteredit",
            /**
             * @event validateedit
             * Fires after a cell is edited, but before the value is set in the record. Return false
             * to cancel the change. The edit event object has the following properties <br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>grid - This grid</li>
             * <li>record - The record being edited</li>
             * <li>field - The field name being edited</li>
             * <li>value - The value being set</li>
             * <li>originalValue - The original value for the field, before the edit.</li>
             * <li>row - The grid row index</li>
             * <li>column - The grid column index</li>
             * <li>cancel - Set this to true to cancel the edit or return false from your handler.</li>
             * </ul>
             * Usage example showing how to remove the red triangle (dirty record indicator) from some
             * records (not all).  By observing the grid's validateedit event, it can be cancelled if
             * the edit occurs on a targeted row (for example) and then setting the field's new value
             * in the Record directly:
             * <pre><code>
grid.on('validateedit', function(e) {
  var myTargetRow = 6;

  if (e.row == myTargetRow) {
    e.cancel = true;
    e.record.data[e.field] = e.value;
  }
});
             * </code></pre>
             * @param {Object} e An edit event (see above for description)
             */
            "validateedit"
        );
    },

    // private
    initEvents : function(){
        Ext.grid.EditorGridPanel.superclass.initEvents.call(this);

        this.getGridEl().on('mousewheel', this.stopEditing.createDelegate(this, [true]), this);
        this.on('columnresize', this.stopEditing, this, [true]);

        if(this.clicksToEdit == 1){
            this.on("cellclick", this.onCellDblClick, this);
        }else {
            var view = this.getView();
            if(this.clicksToEdit == 'auto' && view.mainBody){
                view.mainBody.on('mousedown', this.onAutoEditClick, this);
            }
            this.on('celldblclick', this.onCellDblClick, this);
        }
    },

    onResize : function(){
        Ext.grid.EditorGridPanel.superclass.onResize.apply(this, arguments);
        var ae = this.activeEditor;
        if(this.editing && ae){
            ae.realign(true);
        }
    },

    // private
    onCellDblClick : function(g, row, col){
        this.startEditing(row, col);
    },

    // private
    onAutoEditClick : function(e, t){
        if(e.button !== 0){
            return;
        }
        var row = this.view.findRowIndex(t),
            col = this.view.findCellIndex(t);
        if(row !== false && col !== false){
            this.stopEditing();
            if(this.selModel.getSelectedCell){ // cell sm
                var sc = this.selModel.getSelectedCell();
                if(sc && sc[0] === row && sc[1] === col){
                    this.startEditing(row, col);
                }
            }else{
                if(this.selModel.isSelected(row)){
                    this.startEditing(row, col);
                }
            }
        }
    },

    // private
    onEditComplete : function(ed, value, startValue){
        this.editing = false;
        this.lastActiveEditor = this.activeEditor;
        this.activeEditor = null;

        var r = ed.record,
            field = this.colModel.getDataIndex(ed.col);
        value = this.postEditValue(value, startValue, r, field);
        if(this.forceValidation === true || String(value) !== String(startValue)){
            var e = {
                grid: this,
                record: r,
                field: field,
                originalValue: startValue,
                value: value,
                row: ed.row,
                column: ed.col,
                cancel:false
            };
            if(this.fireEvent("validateedit", e) !== false && !e.cancel && String(value) !== String(startValue)){
                r.set(field, e.value);
                delete e.cancel;
                this.fireEvent("afteredit", e);
            }
        }
        this.view.focusCell(ed.row, ed.col);
    },

    /**
     * FIXME 将uft.extend.grid.EditorGrid中的实现放到这边，PropertyGrid也需要使用到
     * Starts editing the specified for the specified row/column
     * @param {Number} rowIndex
     * @param {Number} colIndex
     */
 	startEditing : function(row, col){
        this.stopEditing();
        if(this.colModel.isCellEditable(col, row)){
            this.view.ensureVisible(row, col, true);
            var r = this.store.getAt(row),
                field = this.colModel.getDataIndex(col),
                e = {
                    grid: this,
                    record: r,
                    field: field,
                    value: r.data[field],
                    row: row,
                    column: col,
                    cancel:false
                };
            if(this.fireEvent("beforeedit", e) !== false && !e.cancel){
                this.editing = true;
                var ed = this.colModel.getCellEditor(col, row);
                if(!ed){
                    return;
                }
	            //对于checkbox,不进入编辑模式
                //用鼠标单击的情况，在checkcolumn已经拦截，不会触发editing，但是对于使用tab键和enter键的情况，在这里拦截
	            if(ed.field.xtype=='uftcheckbox'||ed.field.xtype=='checkbox'){
	            	return;
	            }                 
                if(!ed.rendered){
                    ed.parentEl = this.view.getEditorParent(ed);
                    ed.on({
                        scope: this,
                        render: {
                            fn: function(c){
                                c.field.focus(false, true);
                            },
                            single: true,
                            scope: this
                        },
                        specialkey: function(field, e){
                            this.getSelectionModel().onEditorKey(field, e);
                        },
                        complete: this.onEditComplete,
                        canceledit: this.stopEditing.createDelegate(this, [true])
                    });
                    ed.on({
                    	scope : this,
                    	canceledit : {
                    		fn : function(ed,v,startValue){
                    			//当取消编辑时，不再恢复单元格中原来的值，这主要用于当删除必输项的值是，ext会默认恢复上一次的值，但是这是不必要的操作。
                    			ed.setValue(v);
                    			ed.record.set(this.colModel.columns[col].dataIndex,v);
                    			
                    			//对于删除数据后，同样抛出编辑后事件
					            if(this.fireEvent("validateedit", e) !== false && String(v) !== String(startValue)){
					            	if(v == '' && startValue == null){
					            		//目前null值和空串没有很好的解决，当第一次点击进参照时，此时startValue为null，鼠标移开的时候value为空串，此时不应该执行afteredit
					            		return;
					            	}
					            	field = this.colModel.getDataIndex(ed.col);
						            var e = {
						                grid: this,
						                record: ed.record,
						                field: field,
						                originalValue: startValue,
						                value: v,
						                row: ed.row,
						                column: ed.col
						            };
					                ed.record.set(field, v);
					                if(ed.field && ed.field.idcolname && (v == undefined||v == '')){
					                	//对于清空数据的情况，设置关键字所在的单元格也为空
					                	ed.record.set(ed.field.idcolname,'');
					                }
					                this.fireEvent("afteredit", e);
					            }                    			
                    		}
                    	}
                    });
                }
                Ext.apply(ed, {
                	grid: this, //增加这一行，是为了在参照中使用这个grid对象
                    row     : row,
                    col     : col,
                    record  : r
                });
                /*
                 * 这里解决window窗口被参照挡住的问题
                 */
//                if(ed.field.getXType()=='headerreffield' || ed.field.getXType()=='bodyreffield'){
                	Ext.apply(ed,{zIndex:8000});
//                }
                this.lastEdit = {
                    row: row,
                    col: col
                };
                this.activeEditor = ed;
                // Set the selectSameEditor flag if we are reusing the same editor again and
                // need to prevent the editor from firing onBlur on itself.
                ed.selectSameEditor = (this.activeEditor == this.lastActiveEditor);
                var v = this.preEditValue(r, field);
                ed.startEdit(this.view.getCell(row, col).firstChild, Ext.isDefined(v) ? v : '');
                // Clear the selectSameEditor flag
                (function(){
                    delete ed.selectSameEditor;
                }).defer(50);
            }
        }
    },

    // private
    preEditValue : function(r, field){
        var value = r.data[field];
        return this.autoEncode && Ext.isString(value) ? Ext.util.Format.htmlDecode(value) : value;
    },

    // private
    postEditValue : function(value, originalValue, r, field){
        return this.autoEncode && Ext.isString(value) ? Ext.util.Format.htmlEncode(value) : value;
    },

    /**
     * Stops any active editing
     * @param {Boolean} cancel (optional) True to cancel any changes
     */
    stopEditing : function(cancel){
        if(this.editing){
            // Store the lastActiveEditor to check if it is changing
            var ae = this.lastActiveEditor = this.activeEditor;
            if(ae){
                ae[cancel === true ? 'cancelEdit' : 'completeEdit']();
                this.view.focusCell(ae.row, ae.col);
            }
            this.activeEditor = null;
        }
        this.editing = false;
    }
});
Ext.reg('editorgrid', Ext.grid.EditorGridPanel);