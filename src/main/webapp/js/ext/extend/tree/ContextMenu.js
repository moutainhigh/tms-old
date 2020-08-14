Ext.ns("uft.extend.tree");
/**
 * 树的默认右键菜单
 * 避免在一个页面中重复的id，故所有按钮都不定义id
 * @param {} config
 */
uft.extend.tree.ContextMenu = function(config){
	this.onlyShowAtLeaf=false;//是否只在叶子节点显示该菜单
	Ext.apply(this, config);
	
	uft.extend.tree.ContextMenu.superclass.constructor.call(this,{
    	items : [
    	    {
		        text: '刷新',
		        iconCls : 'btnRef',
		        scope : this,
		        handler : function() {
		        	if(this.contextNode.hasChildNodes())
		        		this.contextNode.reload();
		        	else
		        		this.contextNode.parentNode.reload();
		        }
		    },{
		        text: '展开',
		        iconCls : 'btnExpand',
		        scope : this,
		        handler : function() {
		        	this.contextNode.expand();
		        }
		    },{
		        text: '折叠',
		        iconCls : 'btnCollapse',
		        scope : this,
		        handler : function() {
		        	this.contextNode.collapse();
		        }
		    }
	    ]
	});

};
Ext.extend(uft.extend.tree.ContextMenu, Ext.menu.Menu, {
	
});