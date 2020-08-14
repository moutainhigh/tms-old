Ext.ns('uft.report');
/**
 * 表格的右键菜单
 * @class uft.report.GridContextMenu
 * @extends Ext.menu.Menu
 */
uft.report.GridContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		if(!this.grid){
			uft.Utils.showWarnMsg('表格参数是必须的！')
			return false;
		}
		var app = this.app;
		var grid = this.grid;
		uft.report.GridContextMenu.superclass.constructor.call(this,{
	    	items : [
	    	    {
			        text: '项目重新排序...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.report.OrderWin({grid:grid});
			        	win.on('ok',function(win,itemkeyAry){
							reOrderField(grid,itemkeyAry);
			        	},this);
			        	win.show();
			        }
			    },{
			    	text : '项目结构调整...',
			    	scope : this,
			    	handler : function(){
			    		var win = new uft.report.AdjustWin({grid:grid});
			    		win.show();
			    	}
			    }
		    ]
		});
	}
});
/**
 * 行的
 * @class uft.report.RowContextMenu
 * @extends Ext.menu.Menu
 */
uft.report.RowContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		var app = this.app;
		var grid = this.grid;
		var record = this.record;
		uft.report.RowContextMenu.superclass.constructor.call(this,{
	    	items : [
	    		{
			        text: '项目重新排序...',
			        scope : this,
			        handler : function() {
			        	var win = new uft.report.OrderWin({grid:grid});
			        	win.on('ok',function(win,itemkeyAry){
			        		reOrderField(grid,itemkeyAry);
			        	},this);
			        	win.show();
			        }
			    },{
			    	text : '项目结构调整...',
			    	scope : this,
			    	handler : function(){
			    		var win = new uft.report.AdjustWin({grid:grid});
			    		win.show();
			    	}
			    }
		    ]
		});
	}
});
