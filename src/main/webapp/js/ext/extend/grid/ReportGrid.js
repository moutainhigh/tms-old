Ext.namespace('uft.extend.grid');
/**
 * 报表grid
 * @class uft.extend.grid.ReportGrid
 * @extends uft.extend.grid.BasicGrid
 */
uft.extend.grid.ReportGrid = Ext.extend(uft.extend.grid.BasicGrid, {
	isAddBbar : true,
	immediatelyLoad : false,
	sortable : true,
	isAddPageSizePlugin :false,
	isCheckboxSelectionModel : false,
	singleSelect : true,
	pageSize : 20
});