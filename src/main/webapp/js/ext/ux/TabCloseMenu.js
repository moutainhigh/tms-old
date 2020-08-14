/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * @class Ext.ux.TabCloseMenu
 * @extends Object 
 * Plugin (ptype = 'tabclosemenu') for adding a close context menu to tabs. Note that the menu respects
 * the closable configuration on the tab. As such, commands like remove others and remove all will not
 * remove items that are not closable.
 * 
 * @constructor
 * @param {Object} config The configuration options
 * @ptype tabclosemenu
 */
//Ext.ns("Ext.ux");
Ext.ux.TabCloseMenu = Ext.extend(Object, {
    /**
     * @cfg {String} closeTabText
     * The text for closing the current tab. Defaults to <tt>'Close Tab'</tt>.
     */
    closeTabText: '关闭页签',

    /**
     * @cfg {String} closeOtherTabsText
     * The text for closing all tabs except the current one. Defaults to <tt>'Close Other Tabs'</tt>.
     */
    closeOtherTabsText: '关闭其它',
    
    /**
     * @cfg {Boolean} showCloseAll
     * Indicates whether to show the 'Close All' option. Defaults to <tt>true</tt>. 
     */
    showCloseAll: true,
    
    /**
     * @cfg {String} closeAllTabsText
     * <p>The text for closing all tabs. Defaults to <tt>'Close All Tabs'</tt>.
     */
    closeAllTabsText: '关闭所有',
    
    constructor : function(config){
        Ext.apply(this, config || {});
    },

    //public
    init : function(tabs){
        this.tabs = tabs;
        tabs.on({
            scope: this,
            contextmenu: this.onContextMenu,
            destroy: this.destroy
        });
    },
    
    destroy : function(){
        Ext.destroy(this.menu);
        delete this.menu;
        delete this.tabs;
        delete this.active;    
    },

    // private
    onContextMenu : function(tabs, item, e){
    	var disableRefresh=false;
    	if(tabs.activeTab!=item){
    		//非当前活动tab不允许刷新,否则ext布局会出问题
    		disableRefresh=true;
    	}
        this.active = item;
        var m = this.createMenu(),
            disableAll = true,
            disableOthers = true,
            closeAll = m.getComponent('closeall');
        
        m.getComponent('close').setDisabled(!item.closable);
        tabs.items.each(function(){
            if(this.closable){
                disableAll = false;
                if(this != item){
                    disableOthers = false;
                    return false;
                }
            }
        });
        m.getComponent('closeothers').setDisabled(disableOthers);
        m.getComponent('refresh').setDisabled(disableRefresh);
        if(closeAll){
            closeAll.setDisabled(disableAll);
        }
        
        e.stopEvent();
        m.showAt(e.getPoint());
    },
    
    createMenu : function(){
        if(!this.menu){
            var items = [];
            items.push({
                itemId: 'refresh',
                text: '刷新页签',
                scope: this,
                handler: this.onRefresh
            });
            items.push({
                itemId: 'close',
                text: this.closeTabText,
                scope: this,
                handler: this.onClose
            });
            items.push({
                itemId: 'openWin',
                text: '新窗口',
                scope: this,
                handler: this.onOpenWin
            });
            if(this.showCloseAll){
                items.push('-');
            }
            items.push({
                itemId: 'closeothers',
                text: this.closeOtherTabsText,
                scope: this,
                handler: this.onCloseOthers
            });
            if(this.showCloseAll){
                items.push({
                    itemId: 'closeall',
                    text: this.closeAllTabsText,
                    scope: this,
                    handler: this.onCloseAll
                });
            }
            this.menu = new Ext.menu.Menu({
                items: items
            });
        }
        return this.menu;
    },
    
    onRefresh:function(param){
    	try{
	    	if(this.active&&this.active.getEl()&&this.active.getEl().dom&&this.active.getEl().dom.src){
	    		this.active.getEl().dom.src=this.active.getEl().dom.src;
			}else{
				Ext.Msg.show({
					title : '错误提示',
					msg : '非Iframe模式不能刷新.',
					buttons : Ext.Msg.OK,
					icon : Ext.Msg.ERROR
				});
			}
    	}catch(ex){
			alert(ex.message);
		}
    },
    
    /**
     * 把页签的页面在新窗口打开
     */
    onOpenWin:function(){
    	try{
	    	if(this.active&&this.active.getEl()&&this.active.getEl().dom&&this.active.getEl().dom.src){
	    		var _url=this.active.getEl().dom.src;
	    		window.open(_url);
	    		//window.open(_url,this.active.getEl().dom.id);
			}else{
				Ext.Msg.show({
					title : '错误提示',
					msg : '非Iframe模式不能弹出新窗口.',
					buttons : Ext.Msg.OK,
					icon : Ext.Msg.ERROR
				});
			}
    	}catch(ex){
			alert(ex.message);
		}
    },
    
    onClose : function(){
        this.tabs.remove(this.active);
    },
    
    onCloseOthers : function(){
        this.doClose(true);
    },
    
    onCloseAll : function(){
        this.doClose(false);
    },
    
    doClose : function(excludeActive){
        var items = [];
        this.tabs.items.each(function(item){
            if(item.closable){
                if(!excludeActive || item != this.active){
                    items.push(item);
                }    
            }
        }, this);
        Ext.each(items, function(item){
            this.tabs.remove(item);
        }, this);
    }
});

Ext.preg('tabclosemenu', Ext.ux.TabCloseMenu);