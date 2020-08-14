Ext.namespace('uft.index');
uft.index.Workspace = function(config) {
	leftPanel = null;
	Ext.apply(this, config);
	
	/**
	 * 当TabPanel中存在applet，Embed等标签时，当active该tab时，中firefox会重新加载该页面，
	 * 使用该插件修复该问题。
	 * Ext升级到3.3版本后在IE下也会出现该问题，且在插件也无法修复，
	 * 修改ext-all.css的83行，IE下visibility必须设置为0，而其他浏览器是hidden。
	 */
//	var V = new Ext.ux.plugin.VisibilityMode({ bubble : false }) ;
	uft.index.Workspace.superclass.constructor.call(this, {
		id : config.id,
		region : 'center',
		activeTab:0,
		border : false,
		enableTabScroll : true,
		autoScroll : false,
		layoutOnTabChange:true,
		defaults: {
//          plugins: V,
          hideMode : 'nosize',
          style:{position:!Ext.isIE?'absolute':'relative'}
        },
	    initEvents : function(){
	        Ext.TabPanel.superclass.initEvents.call(this);
	        this.mon(this.strip, {
	            scope: this,
	            mousedown: this.onStripMouseDown,
	            contextmenu: this.onStripContextMenu
	        });
	        if(this.enableTabScroll){
	            this.mon(this.strip, 'mousewheel', this.onWheel, this);
	        }
	        this.mon(this.strip,'dblclick',this.onTitleDbClick,this);  
	    },        
        plugins: new Ext.ux.TabCloseMenu(),
		items : []
	});
};
Ext.extend(uft.index.Workspace, Ext.TabPanel, {
	//双击最大化
 	onTitleDbClick:function(e,target,o){  
 		if(this.leftPanel){
	 		if(!this.leftPanel.collapsed){
	 			this.leftPanel.collapse();
	 		}else{
	 			this.leftPanel.expand();
	 		}
 		}
 		if(this.headerPanel){
	 		if(!this.headerPanel.collapsed){
	 			this.headerPanel.collapse();
	 		}else{
	 			this.headerPanel.expand();
	 		}
 		}
    },
    activeTab:function(e, target, o){
    	this.setActiveTab(target);
    }
});