Ext.namespace('uft.jf');
/**
 * 报表的panel
 */
uft.jf.ReportPanel = Ext.extend(uft.jf.UIPanel, {
	context : null,	//与模板相关的信息
	headerGrid : null, 		//放入表头的表格 ，必须 
	clientHeight : document.documentElement.clientHeight,//使用一个成员变量，避免每次操作document
	constructor : function (config){
		Ext.apply(this, config); //页面上可以将该值覆盖，
		//若存在表头部分
		this.headerPanel = new Ext.Panel({
			region : 'center',
			autoScroll : Ext.isChrome?false:true, //当卡片页显示时会切换scroll状态
			border : false,
			layout : 'fit',
			items : [this.headerGrid]
		});
		//锁定查询条件的高度
		var itemAry = [this.headerPanel];
		if(this.topQueryForm){
			this.topPanelHeight = this.topQueryForm.getDefineHeight();
			Ext.apply(this.topQueryForm,{
				region : 'north',
				height : this.topPanelHeight
			});
			itemAry.push(this.topQueryForm);
		}

		uft.jf.ReportPanel.superclass.constructor.call(this, {
			layout:'border',
			border : false,
			renderTo : document.body,
			height : this.clientHeight,
			tbar : new uft.jf.ReportToolbar({app:this}),
			items : itemAry
		});
		this.onReady();
	},
	onReady : function(){
		Ext.onReady(function(){
			if(this.topQueryForm){
				this.topQueryForm.on('rebuild',function(tqf,results){
					if(results){
						this.reconfigureHeaderGrid(results);
					}
				},this);
			}
			
		},this);
	},
	reconfigureHeaderGrid : function(obj){
		//根据返回的数据，决定是否重新刷新表头
		var g = this.headerGrid,cm = g.colModel,ds = g.getStore(),ps = g.plugins,tbs = g.toolbars,hp = this.headerPanel;
		var headerVO = obj.headerVO;
		if(headerVO){
			//重新生成grid对象
			//如果存在groupVO，说明需要多表头
			var groupVOs = headerVO.groupVOs,cs = headerVO.columnAry,rs = headerVO.recordTypeAry;
			//FIXME 不重新创建表格，使用重新配置的方式，这个最好的方式，但是必须解决所有插件的兼容问题。
			//这个太难，特别是ColumnHeaderGroup,因为他使用的是一个gridView的方式
//					if(groupVOs && groupVOs.length > 0){
//						cm = new Ext.ux.grid.LockingColumnModel({columns : headerVO.columnAry});
//						g.view = new Ext.ux.grid.LockingHeaderGroupView({grows:groupVOs,cm : cm});
//						g.view.init(g);
//        				//g.view.render();
//					}else{
//						cm = new Ext.grid.ColumnModel({columns : headerVO.columnAry});
//					}
//					
//					for(var i=0;i<ps.length;i++){
//						if(ps[i] instanceof Ext.ux.grid.GridSummary){
//							ps[i].cm = cm;//重新设置cm
//						}
//						//ps[i].init(g);
//					}
//					if(tbs){
//						for(var i=0;i<tbs.length;i++){
//							tbs[i].bind(store);
//						}
//					}
//					g.reconfigure(store,cm);
			
			var plugins = null;
			if(groupVOs && groupVOs.length > 0){
				plugins = [new Ext.ux.grid.ColumnHeaderGroup({rows : headerVO.groupVOs})];
			}
			var newStore=new Ext.data.JsonStore({
			    data:{ "totalRecords": obj.totalRecords, "records":obj.records},
			    autoLoad:true,  
			    totalProperty:"totalRecords",
			    root:"records",  
			    fields:rs
			});
			//从后台返回的columnAry对象中，"renderer", "beforeRenderer", "summaryRenderer"这三个属性都是string类型，这里要转成函数
			if(cs){
				var arr = ["renderer", "beforeRenderer", "summaryRenderer"];
				for(var i=0;i<cs.length;i++){
					for(var j=0;j<arr.length;j++){
						var r = cs[i][arr[j]];
						if(r){
							if(Ext.isString(r)){//string类型
								cs[i].renderer = window[r];//转成window范围的一个方法
							}
						}
					}
				}
			}
				
			var newG = new uft.extend.grid.ReportGrid({
				isAddBbar : false,
				remoteSort : false,//这里是动态报表,没有分页,使用本地排序
				sortable : true,
				datastore : newStore,
				recordType : rs,
				columns : cs,
				bufferView : false,//对于动态报表,使用bufferview,避免数据太多的情况下,加载慢
				plugins : plugins
			});
			this.topQueryForm.setGrid(newG);//重新设置查询表格
			this.headerGrid = newG;
			hp.remove(g,true);
			hp.add(newG);
			hp.doLayout();
			return newG;
		}
		return null;
	},
	/**
	 * 返回HeaderGrid
	 * @return {}
	 */
	getHeaderGrid : function(){
		return this.headerGrid;
	},
	newAjaxParams : function(param){
		var params = uft.jf.ReportPanel.superclass.newAjaxParams.call(this);
		params['templateID']=this.context.getTemplateID();
		params['funCode']=this.context.getFunCode();
		params['nodeKey']=this.context.getNodeKey();
		return params;
	}
});
