Ext.namespace('uft.extend.grid');
/**
 * 表头的右键菜单
 * @class uft.extend.grid.HeaderContextMenu
 * @extends Ext.menu.Menu
 */
uft.extend.grid.HeaderContextMenu = Ext.extend(Ext.menu.Menu, {
	constructor : function (config){
		Ext.apply(this, config);
		if(!this.grid){
			uft.Utils.showWarnMsg('表格参数是必须的！')
			return false;
		}
		if(!this.col){
			uft.Utils.showWarnMsg('列参数是必须的！')
			return false;
		}
		uft.extend.grid.HeaderContextMenu.superclass.constructor.call(this,{
	    	items : [
	    	    {
			        text: '定位...',
			        scope : this,
			        handler : function() {
			        	var w = Ext.getCmp('rl_win');
			        	if(!w){
			        		w = new uft.extend.grid.RowLocationWindow({grid:this.grid,col:this.col});
			        	}
			        	w.show();
			        }
			    }
		    ]
		});
	}
});
/**
 * 表体行定位
 * @class uft.extend.grid.RowLocationWindow
 * @extends Ext.Window
 */
uft.extend.grid.RowLocationWindow =  Ext.extend(Ext.Window, {
	grid : null,
	col : null,//当前点击的列
	constructor : function(config) {
		Ext.apply(this,config);
		var grid=this.grid,col=this.col,keyword='', cd = [],cm = grid.getColumnModel(),cs = cm.columns;
		var c = cm.getColumnAt(col);
		var r = this.getCurrentRow();
		if(r){
			keyword = r.get(c.dataIndex);
			//如果是下拉类型，需要转换成具体是值
			if(c.xtype == 'combocolumn'){
				var combo = c.editor;
				if(!combo){
					return keyword;
				}
				var record = combo.findRecord(combo.valueField, keyword);
				keyword = record ? record.get(combo.displayField) : combo.valueNotFoundText;
			}
		}
		if(cs && cs.length > 0){
			for(var i=0;i<cs.length;i++){
				if(!cs[i].hidden){
					var header = cs[i].header;//这里的header一般会包括一些html标签，这里过滤这些标签
					if(!header || !cs[i].dataIndex){
						continue;
					}
					header = Utils.removeHTMLTag(header);
					cd.push([header,cs[i].dataIndex]);
				}
			}
		}
		
		this.mainPanel = new Ext.FormPanel({
			padding : '5px 5px 0',
			defaults:{
				anchor: '98%'
			},
			frame : true,
			labelWidth : 60,
			layout : 'form',
			items : [{id : 'rl_keyword',name : 'keyword',xtype:'textfield',fieldLabel:'查找目标',value:keyword,cls:'x-form-text-border'},
					 {cls:'x-form-text-border',value:c.dataIndex,allowBlank:false,id:'rl_col',name:"col",fieldLabel:"查找列",xtype:"localcombo",
						store:{"fields":["text","value"],"data":cd,"xtype":"arraystore"}}]
		});
    	Ext.getCmp('rl_keyword').on('specialkey', function(f,e){
			if(e.getKey()==13){
				//执行查询
				this.findRow();
			} 
        },this);
		
		var btnAry = [];
		btnAry.push(new Ext.Button({
			text : '查&nbsp;&nbsp;找',
			scope : this,
			handler : this.findRow
		}));
		btnAry.push(new Ext.Button({
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.close();
			}
		}));
		uft.extend.grid.RowLocationWindow.superclass.constructor.call(this, {
			id : 'rl_win',
			title : '查找',
			width : 250 ,
			height : 150,
			frame : true,
			closable : true,
			draggable : true,
			modal : false,
			layout : 'fit',
			items : [this.mainPanel],
			buttonAlign : 'center',
			buttons : btnAry
	    });
	},
	/**
	 * 定位到下一行
	 */
	findRow : function(){
		var keyword = Ext.getCmp('rl_keyword').getValue();
		var dataIndex = Ext.getCmp('rl_col').getValue();
		if(keyword && dataIndex){
			var grid=this.grid,view = grid.getView(),ds = grid.getStore(),count = ds.getCount();
			//当前选中的行，从这一行开始往下查找
			var from = 0;
			var r = this.getCurrentRow();
			if(r){
				from = ds.indexOf(r);
			}
			//当前要查询的列的列号
			var col = grid.getColumnModel().findColumnIndex(dataIndex);
			for(var i=from+1;i<count;i++){
				var r = ds.getAt(i);
				var col_val = r.get(dataIndex);
				if(col_val.indexOf(keyword) != -1){
					var sm = grid.getSelectionModel();
					if(sm instanceof Ext.grid.CellSelectionModel){
						//选中单元格
						try{
							sm.select(i,col);//对于bufferView模式，这里会触发渲染
						}catch(e){
							setUftTimeout(sm.select,sm,100,i,col);//避免因为渲染没有完成而无法选中的问题
						}
					}else{
						//选中行
						sm.selectRow(i);
					}
					//找到以后退出
					break;
				}
			}
		}
	},
	getCurrentRow : function(){
		var grid = this.grid;
		if(grid instanceof uft.extend.grid.EditorGrid){
			r = grid.getSelectedRow();
		}else{
			r = uft.Utils.getSelectedRecord(grid);
		}
		return r;
	},
	show : function(){
		uft.extend.grid.RowLocationWindow.superclass.show.call(this);
		//鼠标焦点放到keyword上
		var c = Ext.getCmp('rl_keyword');
		c.focus.defer(100, c);
	}
});