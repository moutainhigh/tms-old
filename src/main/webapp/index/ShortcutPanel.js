Ext.ns('uft.index');
uft.index.ShortcutPanel = Ext.extend(Ext.Panel, {
	constructor:function(config) {
		Ext.apply(this, config);
		var items = [];
		this.quickLinkPanel = new Ext.Panel({
			layout : 'table',
			border : false,
		    defaultType: 'button',
		    buttonAlign : 'center',
		    autoScroll:true,
		    layoutConfig: {
		    	tableAttrs : {
		            style: {
		                padding : '5px 3px 5px 5px',
		                margin:'auto'
		            }
		    	},
		        columns:2
		    },
		    items : []
		});
		this.loadQuickLink();
		items.push(this.quickLinkPanel);
		uft.index.ShortcutPanel.superclass.constructor.call(this,{
			id : 'shortcutPanel',
			title : '快捷菜单',
			iconCls : 'btnShortcut',
			layout : 'fit',
			autoScroll:true,
			border : false,
			items : items
		});
	},
	/**
	 * 返回快捷菜单的数据,并添加到快捷panel中
	 */
	loadQuickLink : function(){
		Utils.request({
			scope : this,
			url : ctxPath+'/common/quick/getQuickLinks.json',
			onSuccess : function(values){
				//动态加入buttonPanel中
				if(values){
					values = Ext.decode(values);
				}
				if(values.datas){
					this.addQuickLink(values.datas);
				}
			}
		});
	},
	//动态增加快捷按钮
	//private
	addQuickLink : function(data){
		if(!data){
			return;
		}
		var arr ;
		this.btnIds = new Array();
		if(!(data instanceof Array)){
			arr = new Array();
			arr.push(data);
		}else{
			arr = data;
		}
		for(var i=0;i<arr.length;i++){
			var btnConfig = arr[i];
			var btnId = 'btn_'+btnConfig.pk_fun;
			this.btnIds.push(btnId); //存储btnId 
			var btn = new uft.extend.Button({
				id : btnId,
				text : btnConfig.fun_name,
				iconCls:'btnQuickLink',
				width : 70,
				height:70,
				cls : 'tableFixed',
				fun_code : btnConfig.fun_code,
				class_name:btnConfig.class_name,
				scale: 'large',
				iconAlign: 'top',
				margins : '3 0 3 0',
				scope : this,
				handler : function(btn,e){
					var url = ctxPath + btn.class_name||'';
			    	if(url.indexOf("?")==-1){
			    		url+="?";
			    	}else{
			    		url+="&";
			    	}
			    	url+="funCode="+btn.fun_code;					
					openNode(btn.fun_code,btn.text,url,true,true);
				}
			});
			
			this.quickLinkPanel.add(btn);
		}
		this.quickLinkPanel.doLayout();
		//加入右键菜单
		for(var i=0;i<this.btnIds.length;i++){
			var ele = Ext.get(this.btnIds[i]);
			if(ele){
				ele.addListener('contextmenu',this.showContextmenu,this,this.btnIds[i]);
			}
		}		
	},
	//重新加载快捷面板，可能是iframe或者默认的面板
	reload : function(data){
		//先移除所有组件
		this.quickLinkPanel.removeAll();
		//使用默认的panel
		this.loadQuickLink(data);
	},
	//显示右键菜单,btnId是在增加listener时传入的
	showContextmenu : function(e,btnEl,btnId){
		this.stopDefault(e);
		var menu = new Ext.menu.Menu({
			items :[{
		        text: '修改',
		        iconCls : 'btnEdit',
		        scope : this,
		        handler : function() {
		        	var pk_fun=btnId.substring(4);
		        	var btn = Ext.getCmp(btnId);
	            	new uft.index.QuickLink({
	            		editFlag : true,
	            		pk_fun:pk_fun,
	            		fun_name:btn.text,
	            		fun_code:btn.fun_code,
	            		class_name:btn.class_name
	            	}).show();		        	
		        }
		    },{
		        text: '删除',
		        iconCls : 'btnDel',
		        scope : this,
		        handler : function() {
		        	var pk_fun=btnId.substring(4);
					uft.Utils.doAjax({
						scope : this,
						url : ctxPath+'/common/quick/remove.json',
						params : {pk_fun:pk_fun},
						isTip : true,
						success : function(values) {
							//从buttonPanel中移除
							var cmp = Ext.getCmp(btnId);
							if(cmp){
								this.quickLinkPanel.remove(cmp);
		        				this.quickLinkPanel.doLayout();
							}
						}
					});
		        }
		    },{
				text : '新窗口打开',
				scope : this,
				iconCls : 'btnSetting',
				handler : function(){
		        	var btn = Ext.getCmp(btnId);
					var url = ctxPath + btn.class_name||'';
			    	if(url.indexOf("?")==-1){
			    		url+="?";
			    	}else{
			    		url+="&";
			    	}
			    	url+="funCode="+btn.fun_code;
			    	window.open(url);
				}
			}]
		});
	    menu.showAt(e.getXY());
	},
	//阻止浏览器默认行为
    stopDefault : function(e) {
	    if (e && e.preventDefault){
	    	e.keyCode=0;
	    	e.preventDefault();
	    	e.stopPropagation();
	    }else{
	    	window.event.keyCode=0;
	    	window.event.returnValue = false;
	    }
	    return false;
	} 	
});
