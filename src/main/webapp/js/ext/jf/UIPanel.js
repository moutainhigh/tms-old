Ext.ns('uft.jf');
uft.jf.UIPanel = Ext.extend(Ext.Panel, {
	statusMgr:null, 	//状态管理器，统一管理页面状态和业务状态
	cacheMgr : null, 	//缓存管理器
	
	timeout : 300,			//表头的选择事件中，如果用户在该时间内多次选择，则只请求最后一次
	timeoutID : null,		//setTimeout的ID
	intervalID: null,		//setInterval的ID	
	constructor : function (config){
		Ext.apply(this, config);
		//初始化状态管理器
		this.statusMgr = new uft.jf.StatusMgr();
		//实例化缓存管理器
		this.cacheMgr = new uft.jf.CacheMgr();
		uft.jf.UIPanel.superclass.constructor.call(this);
	},
    initComponent : function() {
        uft.jf.UIPanel.superclass.initComponent.call(this);
        Ext.EventManager.onWindowResize(this.fireResize, this);
    },
    fireResize : function(w, h){
        this.fireEvent('resize', this, w, h, w, h);
        this.setWidth(w);
        this.setHeight(h);
        this.doLayout();
    },
	//Application在执行ajax调用时说需的参数，可被覆盖
	newAjaxParams : function(param){
		var args = Array.prototype.slice.call(arguments);
		var params = {};
		for(var i=0;i<args.length/2;i++){
			params[args[i]] = args[i+1];
		}
		return params;
	}    
});