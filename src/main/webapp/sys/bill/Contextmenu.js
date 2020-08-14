Ext.ns('uft.bill');
/**
 * 表格的右键菜单
 * @class uft.bill.GridContextMenu
 * @extends Ext.menu.Menu
 */
uft.bill.GridContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		if(!this.grid){
			uft.Utils.showWarnMsg('表格参数是必须的！')
			return false;
		}
		var et = this.et;
		var grid = this.grid;
		uft.bill.GridContextMenu.superclass.constructor.call(this,{
	    	items : [
	    	    {
			        text: '增加自定义项目...',
			        scope : this,
			        handler : function() {
			        	et.addNullField(grid);
			        }
			    },{
			        text: '增加自定义项目到新页签...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.NewTabWin({et:et});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		et.addNullFieldToNewTab(grid,values);
			        	},this);
			        }
			    },'-',{
			        text: '项目重新排序...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.OrderWin({et:et,grid:grid});
			        	win.on('ok',function(win,itemkeyAry){
							et.reOrderField(grid,itemkeyAry);
			        	},this);
			        	win.show();
			        }
			    },{
			    	text : '项目结构调整...',
			    	scope : this,
			    	handler : function(){
			    		var win = new uft.bill.AdjustWin({et:et,grid:grid});
			    		win.show();
			    	}
			    },{
			        text: '删除页签...',
			        scope : this,
			        handler : function() {
			        	Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定删除吗？'), function(btn) {
			        		if(btn == 'yes'){
			        			et.deleteTab(grid);
			        		}
			        	},this);
			        }
			    }
		    ]
		});
	}
});
/**
 * 行的
 * @class uft.bill.RowContextMenu
 * @extends Ext.menu.Menu
 */
uft.bill.RowContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		var et = this.et;
		var grid = this.grid;
		var record = this.record;
		uft.bill.RowContextMenu.superclass.constructor.call(this,{
	    	items : [
	    		{
			        text: '增加自定义项目...',
			        scope : this,
			        handler : function() {
			        	et.addNullField(grid);
			        }
			    },{
			        text: '增加自定义项目到新页签...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.NewTabWin({et:et});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		et.addNullFieldToNewTab(this.grid,values);
			        	},this);
			        }
			    },'-',{
			        text: '删除项目',
			        scope : this,
			        handler : function() {
			        	grid.getStore().remove(record);
			        }
			    },{
			        text: '移动项目到...',
			        scope : this,
			        handler : function() {
			        	var tabs = et.getTabs(grid);
			        	if(tabs.length == 1){
			        		uft.Utils.showWarnMsg('不能选择同一个页签！');
			        		return;
			        	}
			        	var win = new uft.bill.TabWin({et:et,grid:grid});
			        	win.show();
			        	win.on('ok',function(win,tabcode){
			        		et.moveFieldTo(grid,record,tabcode);
			        	},this);
			        }
			    },{
			        text: '移动项目到新页签...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.NewTabWin({et:et});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		et.moveFieldToNewTab(grid,record,values);
			        	},this);
			        }
			    },'-',{
			        text: '项目重新排序...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.OrderWin({et:et,grid:grid});
			        	win.on('ok',function(win,itemkeyAry){
			        		et.reOrderField(grid,itemkeyAry);
			        	},this);
			        	win.show();
			        }
			    },{
			    	text : '项目结构调整...',
			    	scope : this,
			    	handler : function(){
			    		var win = new uft.bill.AdjustWin({et:et,grid:grid});
			    		win.show();
			    	}
			    },{
			        text: '删除页签...',
			        scope : this,
			        handler : function() {
			        	Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定删除吗？'), function(btn) {
			        		if(btn == 'yes'){
			        			et.deleteTab(grid);
			        		}
			        	},this);
			        }
			    }
		    ]
		});
	}
});
/**
 * 表格的右键菜单
 * @class uft.bill.TableContextMenu
 * @extends Ext.menu.Menu
 */
uft.bill.TableContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		var et = this.et;
		uft.bill.TableContextMenu.superclass.constructor.call(this,{
	    	items : [
	    	    {
			        text: '添加到表头...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	et.addTableTo(null,tableName,true);
			        }
			    },{
			        text: '添加到表体...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	et.addTableTo(null,tableName,false);
			        }
			    },'-',{
			        text: '增加到表头新页签...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	var win = new uft.bill.NewTabWin({et:et,tabcode:tableName});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		values.pos = 0;
			        		values.tabindex=et.getNewTabIndex(true);
			        		et.addTableTo(null,tableName,true,true,values);
			        	},this);
			        }
			    },{
			        text: '增加到表体新页签...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	var win = new uft.bill.NewTabWin({et:et,tabcode:tableName});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		values.pos = 1;
			        		values.tabindex=et.getNewTabIndex(false);
			        		et.addTableTo(null,tableName,false,true,values);
			        	},this);
			        }
			    }
		    ]
		});
	},
	getTableName : function(){
		return Ext.getCmp(this.et.headerTableMultiSelectID).getValue();
	}
});
/**
 * 字段的右键菜单
 * @class uft.bill.FieldContextMenu
 * @extends Ext.menu.Menu
 */
uft.bill.FieldContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		var et = this.et;
		uft.bill.FieldContextMenu.superclass.constructor.call(this,{
	    	items : [
	    	    {
			        text: '添加到表头...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	var fields = this.getFields();
			        	et.addFieldTo(null,tableName,fields,true);
			        }
			    },{
			        text: '添加到表体...',
			        scope : this,
			        handler : function() {
			        	var tableName = this.getTableName();
			        	var fields = this.getFields();
			        	et.addFieldTo(null,tableName,fields,false);
			        }
			    },'-',{
			        text: '增加到表头新页签...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.NewTabWin({et:et});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		var tableName = this.getTableName();
			        		var fields = this.getFields();
			        		values.pos = 0;
			        		values.tabindex=et.getNewTabIndex(true);
			        		et.addFieldTo(null,tableName,fields,true,true,values);
			        	},this);
			        }
			    },{
			        text: '增加到表体新页签...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.bill.NewTabWin({et:et});
			        	win.show();
			        	win.on('ok',function(win,values){
			        		var tableName = this.getTableName();
			        		var fields = this.getFields();
			        		values.pos = 1;
			        		values.tabindex=et.getNewTabIndex(false);
			        		et.addFieldTo(null,tableName,fields,false,true,values);
			        	},this);
			        }
			    }
		    ]
		});
	},
	getTableName : function(){
		return Ext.getCmp(this.et.headerTableMultiSelectID).getValue();
	},
	getFields : function(){
		var records = Ext.getCmp(this.et.headerTableFieldMultiSelectID).view.getSelectedRecords();
		return records[0].data;
	}
});
