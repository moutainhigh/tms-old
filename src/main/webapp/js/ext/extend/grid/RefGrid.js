Ext.namespace('uft.extend.grid');
/**
 * 参照所使用的表格，参照的窗口比较小，一般的表格的PagingToolbar比较长，不适合
 * 参照的表格增加一个顶部工具栏，用于查询表格使用
 * @param {} config
 */
uft.extend.grid.RefGrid = Ext.extend(uft.extend.grid.BasicGrid, {
	addGridSearch : true,
	addCellcontextmenu : false, //不启用右键菜单
	constructor : function (config){ 
		Ext.apply(this, config);
		var disableIndexes=[];
		//hidden=true的字段不加入排序
		for(var i=0;i<this.columns.length;i++){
			if(this.columns[i].hidden){
				disableIndexes.push(this.columns[i].dataIndex);
			}else{
				//有些时候可能存在多个相同的dataIndex，其中有些是隐藏有些是显示的，如参照类中奖pk，code，name设置成相同字段的情况
				//此时如果有一个字段是显示的，那么就需要把该字段作为查询字段，而不应该加到disableIndexs中
				if(disableIndexes.indexOf(this.columns[i].dataIndex) != -1){
					disableIndexes.remove(this.columns[i].dataIndex);
				}
			}
		}
		if(this.addGridSearch){
			this.tbar = [],//如果要使用gridSearch，那么需要定义toolbar
			this.gridSearch = new Ext.ux.grid.Search({
				position:'top'//将查询框放在
				,mode:'remote'
				,minChars:2
				,minLength:2
				,disableIndexes : disableIndexes
			}),	
			this.plugins=this.gridSearch;
		}
		
		uft.extend.grid.RefGrid.superclass.constructor.call(this,{
			rowNumberer : new Ext.grid.RowNumberer({width:25}),
			enableHdMenu : false,
			enableColumnMove : false, //列不能移动
			trackMouseOver : false, //mouseover事件			
			pageSize:10 //参照表格的页大小固定
		});
		if(this.bottomToolbar){
			this.bottomToolbar.addListener('beforechange',function(bbar,params){
				this.setGridSearchParams();
			},this);
		}
	},
	/**
	 * 设置查询工具栏的值到store参数中
	 */
    setGridSearchParams : function(){
    	var val = this.gridSearch.field.getValue();
    	var data = this.gridSearch.initData();
		var fields = data.fields;
		var fieldsType = data.fieldsType;
    	this.store.baseParams[this.gridSearch.paramNames.fields] = Ext.encode(fields);
		this.store.baseParams[this.gridSearch.paramNames.fieldsType] = Ext.encode(fieldsType);
		this.store.baseParams[this.gridSearch.paramNames.query] = val;
    }	
});
