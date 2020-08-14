Ext.ns('Ext.ux.grid');
/**
 * 双击表头header，自动扩展列宽
 * http://www.sencha.com/forum/showthread.php?82965-Dblclick-to-autosize-grid-columns
 */
(function () {
    var cursorRe = /^(?:col|e|w)-resize$/;
    Ext.ux.grid.AutoSizeColumns = Ext.extend(Object, {
        cellPadding: 8,
        constructor: function (config) {
            Ext.apply(this, config);
        },
        init: function (grid) {
            var view = grid.getView();
            view.onHeaderClick = view.onHeaderClick.createInterceptor(this.onHeaderClick);
            grid.on('headerdblclick', this.onHeaderDblClick.createDelegate(view, [this.cellPadding], 3));
        },
        onHeaderClick: function (grid, colIndex) {
            var el = this.getHeaderCell(colIndex);
            if (cursorRe.test(el.style.cursor)) {
                return false;
            }
        },
        onHeaderDblClick: function (grid, colIndex, e, cellPadding) {
            var ua = navigator.userAgent.toLowerCase();
            var el = this.getHeaderCell(colIndex), width, rowIndex, count;
            if (!cursorRe.test(el.style.cursor)) {
                return;
            }
            if (e.getXY()[0] - Ext.lib.Dom.getXY(el)[0] <= 5) {
                colIndex--;
                el = this.getHeaderCell(colIndex);
            }
            if (this.cm.isFixed(colIndex) || this.cm.isHidden(colIndex)) {
                return;
            }
            el = el.firstChild;
            el.style.width = '0px';
            if(Ext.isChrome || Ext.isSafari) {
            	 width = Ext.util.TextMetrics.measure(el, el.innerHTML).width;
            } else {
            	width = el.scrollWidth;
            }
            el.style.width = 'auto';
            for (rowIndex = 0, count = this.ds.getCount(); rowIndex < count; rowIndex++) {
                el = this.getCell(rowIndex, colIndex).firstChild;
                el.style.width = '0px';
                if(Ext.isChrome || Ext.isSafari) {
                	width = Math.max(width, Ext.util.TextMetrics.measure(el, el.innerHTML).width);
                } else {
                	width = Math.max(width, el.scrollWidth);
                }
                el.style.width = 'auto';
            }
            this.onColumnSplitterMoved(colIndex, width + cellPadding);
        }
    });
})();
Ext.preg('autosizecolumns', Ext.ux.grid.AutoSizeColumns);