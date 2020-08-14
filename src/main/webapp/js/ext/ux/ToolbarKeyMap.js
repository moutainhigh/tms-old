Ext.ns('Ext.ux');
Ext.ux.ToolbarKeyMap = Ext.extend(Object, (function() {
    var kb,
        owner,
    	mappings;

    function addKeyBinding(c) {
        if (kb = c.keyBinding) {
            delete c.keyBinding;
            if (!kb.fn && c.handler) {
                kb.fn = function(k, e) {
                	//只有在可以触发自定义行为的时候才阻止浏览器行为
                    if(c.disabled===false&&window.afterOneTrigger!==true){
                    	stopDefault(e);
                    	c.handler.call(c.scope, c, e);
                    	//在ie下使用以下语句可以阻止继续传播
                    	if(Ext.isIE){
                    		window.event.keyCode=0;
                    	}
                    	window.afterOneTrigger=true;
                    }
                }
            }
            mappings.push(kb);
            var t = [];
            if (kb.ctrl) t.push('Ctrl');
            if (kb.alt) t.push('Alt');
            if (kb.shift) t.push('Shift');
            if(typeof(kb.key) == 'string'){
            	t.push(kb.key.toUpperCase());
            }else{
            	t.push(String.fromCharCode(kb.key).toUpperCase());
            }
            c.hotKey = '('+t.join('+')+')';
            if (c instanceof Ext.menu.Item) {
                c.onRender = c.onRender.createSequence(addMenuItemHotKey);
            } else if ((c instanceof Ext.Button) && (c.showHotKey)) {
            	if(this.hasOwnerCt===true){
                	c.onRender = c.onRender.createSequence(addButtonHotKey);
            	}
            }
        }
        if ((c instanceof Ext.Button) && c.menu) {
            c.menu.cascade(addKeyBinding);
        }
    }

    function findKeyNavs() {
        delete this.onRender;
        if (owner = this.ownerCt) {
        	this.hasOwnerCt=true;
            mappings = [];
            this.cascade(addKeyBinding);
            if (!owner.menuKeyMap) {
            	owner.menuKeyMap = new uft.KeyMap(mappings);
                owner.el.dom.tabIndex = 0;
            } else {
                owner.menuKeyMap.addBinding(mappings);
            }
        }else{
        	//没有ownerCt的情况，目前BodyAssistToolbar会出现这种情况
        	this.hasOwnerCt=false;
        	mappings = [];
            this.cascade(addKeyBinding);
            new uft.KeyMap(mappings);//将快捷键加入window的快捷键库中
        }
    }

    function addMenuItemHotKey() {
        delete this.onRender;
        this.el.setStyle({
            overflow: 'hidden',
            zoom: 1
        });
        this.el.child('.x-menu-item-text').setStyle({
            'float': 'left'
        });
        this.el.createChild({
        	tag : 'span',
            style: {
                padding: '0px 0px 0px 15px'
                ,display:'inline-block'
                ,_position:'absolute' //for ie6
//                ,'float': 'right' //使用float在ie8下会出现换行,在ie7下没有自适应宽度
            },
            html: this.hotKey
        });
    }

    function addButtonHotKey() {
        delete this.onRender;
        var p = this.btnEl.up('');
        p.setStyle({
            overflow: 'hidden',
            zoom: 1
        });
        p.up('td').setStyle('text-align', 'left');
        this.btnEl.setStyle('.x-menu-item-text').setStyle({
            'float': 'left'
        });
        p = p.createChild({
                style: {
                padding: '0px 0px 0px 15px',
                'float': 'right',
                position: 'relative',
                bottom: Ext.isWebKit ? '-1px' : '-2px'
            },
            html: this.hotKey
        });
    }
    
    function stopDefault(e) {
	    if (e && e.preventDefault){
	    	e.preventDefault();
	    	e.stopPropagation();
	    }else{
	    	window.event.returnValue = false;
	    }
	    return false;
	} 
	
    return {
        init: function(toolbar) {
            toolbar.onRender = toolbar.onRender.createSequence(findKeyNavs,toolbar);
            toolbar.doLayout = toolbar.doLayout.createSequence(findKeyNavs,toolbar);
        }
    }
})());
