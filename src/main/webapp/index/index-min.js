Ext.ns("uft.index");uft.index.Index=Ext.extend(Ext.Panel,{constructor:function(b){Ext.apply(this,b);var h=function(o,n){if(o.leaf){var i=o.attributes.url;i=ctxPath+i;if(i.indexOf("?")==-1){i+="?";}else{i+="&";}i+="funCode="+o.attributes.code;var m=new Ext.ux.IFrameComponent({id:o.attributes.code,title:o.text,tabTip:o.text,closable:true,border:false,autoScroll:true,url:i,layout:"fit"});this.mainPanel.add(m);this.mainPanel.setActiveTab(m);}};var l=[new uft.index.ShortcutPanel()];this.treeAry=[];var j=this.funVOs;for(var f=0;f<j.length;f++){var e=new uft.extend.tree.ContextMenu();e.add({text:"加入快捷方式",iconCls:"btnAdd",scope:e,handler:function(m,p){if(!this.contextNode.leaf){uft.Utils.showWarnMsg("只有叶子节点才能加入快捷方式!");return;}else{var n=this.contextNode.id;var q=this.contextNode.text;var i=this.contextNode.attributes.code;var o=this.contextNode.attributes.url;new uft.index.QuickLink({pk_fun:n,fun_name:q,fun_code:i,class_name:o}).show();}}},{text:"新窗口打开",scope:e,iconCls:"btnSetting",handler:function(m,o){var n=this.contextNode;if(n.leaf){var i=n.attributes.url;i=ctxPath+i;if(i.indexOf("?")==-1){i+="?";}else{i+="&";}i+="funCode="+n.attributes.code;window.open(i);}else{uft.Utils.showWarnMsg("只能打开叶子节点!");return;}}});var g=new uft.extend.tree.Tree({id:j[f].pk_fun,title:j[f].fun_name,iconCls:"btnModule",dataUrl:ctxPath+"/getFunTree.json",params:{parent_id:j[f].pk_fun},border:true,rootVisible:false,contextMenu:e});g.on("click",h,this);l.push(g);this.treeAry.push(g);}var c=new Ext.Panel({id:"leftPanel",title:"导航",iconCls:"btnNav",region:"west",layout:"accordion",split:true,width:200,minWidth:200,maxWidth:200,margins:"0 0 0 3",autoScroll:true,collapseMode:"mini",tools:[{id:"refresh",qtip:"刷新",on:{click:function(){for(var m=0;m<l.length;m++){if(l[m] instanceof Ext.tree.TreePanel){l[m].getRootNode().reload();}else{l[m].reload();}}}}}],items:l});var d="";if(this.corpName&&this.corpName!="null"){d=this.corpName+"，"+this.userName+" 您好!";}else{d=this.userName+" 您好!";}var k=new Ext.Panel({region:"north",height:67,id:"headerPanel",html:'<div id="header" class="bodybg">	<div class="header">		<h1 id="logo_h" ondblclick="collapseLeft();"></h1>			<ul>				<li>'+d+'</li>				<li><a href="javascript:openEditPasswordWin();" class="amod">修改密码</a><a href="javascript:logout();" class="aout">注销</a></li>			</ul>			<span id="quickEntry"></span>	</div></div>'});this.mainPanel=new uft.index.Workspace({leftPanel:c,headerPanel:k});this.mainPanel.on("beforeremove",function(i,m){var n=m.el.dom.contentWindow.app;if(n&&n.statusMgr&&(n.statusMgr.getCurrentPageStatus()==1||n.statusMgr.getCurrentPageStatus()==4||n.statusMgr.getCurrentPageStatus()==6||n.statusMgr.getCurrentPageStatus()==7)){return window.confirm("有未保存的数据,确定离开么?");}});var a=new Ext.Viewport({layout:"border",items:[k,c,this.mainPanel]});}});Ext.namespace("uft.index");uft.index.Workspace=function(a){leftPanel=null;Ext.apply(this,a);uft.index.Workspace.superclass.constructor.call(this,{id:a.id,region:"center",activeTab:0,border:false,enableTabScroll:true,autoScroll:false,layoutOnTabChange:true,defaults:{hideMode:"nosize",style:{position:!Ext.isIE?"absolute":"relative"}},initEvents:function(){Ext.TabPanel.superclass.initEvents.call(this);this.mon(this.strip,{scope:this,mousedown:this.onStripMouseDown,contextmenu:this.onStripContextMenu});if(this.enableTabScroll){this.mon(this.strip,"mousewheel",this.onWheel,this);}this.mon(this.strip,"dblclick",this.onTitleDbClick,this);},plugins:new Ext.ux.TabCloseMenu(),items:[]});};Ext.extend(uft.index.Workspace,Ext.TabPanel,{onTitleDbClick:function(b,a,c){if(this.leftPanel){if(!this.leftPanel.collapsed){this.leftPanel.collapse();}else{this.leftPanel.expand();}}if(this.headerPanel){if(!this.headerPanel.collapsed){this.headerPanel.collapse();}else{this.headerPanel.expand();}}},activeTab:function(b,a,c){this.setActiveTab(a);}});Ext.namespace("uft.index");uft.index.QuickLink=Ext.extend(Ext.Window,{constructor:function(a){Ext.apply(this,a);this.quickLinkForm=new uft.extend.form.FormPanel({labelWidth:80,border:false,autoWidth:true,frame:true,items:[{xtype:"textfield",fieldLabel:"显示名称",name:"fun_name",value:this.fun_name},{xtype:"hidden",name:"fun_code",value:this.fun_code},{xtype:"hidden",name:"class_name",value:this.class_name},{xtype:"hidden",name:"pk_fun",value:this.pk_fun}]});var b=[];b.push({xtype:"button",text:"保&nbsp;&nbsp;存",actiontype:"submit",scope:this,handler:this.saveHandler},{xtype:"button",text:"关&nbsp;&nbsp;闭",scope:this,handler:function(){this.destroy();}});uft.index.QuickLink.superclass.constructor.call(this,{title:this.editFlag?"修改快捷方式":"增加快捷方式",width:300,height:159,layout:"fit",shim:true,frame:true,closable:true,draggable:true,border:false,modal:true,items:[this.quickLinkForm],buttons:b});this.quickLinkForm.getForm().items.each(function(c){c.on("specialkey",function(d,g){if(g.getKey()==13){this.saveHandler();}else{if(Ext.isIE||Ext.isChrome){if(g.getKey()==9){this.saveHandler();}}}},this);},this);},saveHandler:function(){if(this.quickLinkForm.getForm().isValid()){var a=this.quickLinkForm.getForm().getFieldValues(false);Ext.apply(a,{});if(this.editFlag){uft.Utils.doAjax({scope:this,url:ctxPath+"/common/quick/edit.json",params:a,isTip:true,success:function(){var c="btn_"+this.pk_fun;var b=Ext.getCmp(c);b.setText(a.fun_name);this.destroy();}});}else{uft.Utils.doAjax({scope:this,url:ctxPath+"/common/quick/add.json",params:a,isTip:true,success:function(){var b=Ext.getCmp("shortcutPanel");if(b){b.addQuickLink(a);}this.fireEvent("add",this);this.destroy();}});}}}});Ext.ns("uft.index");uft.index.ShortcutPanel=Ext.extend(Ext.Panel,{constructor:function(b){Ext.apply(this,b);var a=[];this.quickLinkPanel=new Ext.Panel({layout:"table",border:false,defaultType:"button",buttonAlign:"center",autoScroll:true,layoutConfig:{tableAttrs:{style:{padding:"5px 3px 5px 5px",margin:"auto"}},columns:2},items:[]});this.loadQuickLink();a.push(this.quickLinkPanel);uft.index.ShortcutPanel.superclass.constructor.call(this,{id:"shortcutPanel",title:"快捷菜单",iconCls:"btnShortcut",layout:"fit",autoScroll:true,border:false,items:a});},loadQuickLink:function(){Utils.request({scope:this,url:ctxPath+"/common/quick/getQuickLinks.json",onSuccess:function(a){if(a){a=Ext.decode(a);}if(a.datas){this.addQuickLink(a.datas);}}});},addQuickLink:function(f){if(!f){return;}var a;this.btnIds=new Array();if(!(f instanceof Array)){a=new Array();a.push(f);}else{a=f;}for(var d=0;d<a.length;d++){var c=a[d];var g="btn_"+c.pk_fun;this.btnIds.push(g);var b=new uft.extend.Button({id:g,text:c.fun_name,iconCls:"btnQuickLink",width:70,height:70,cls:"tableFixed",fun_code:c.fun_code,class_name:c.class_name,scale:"large",iconAlign:"top",margins:"3 0 3 0",scope:this,handler:function(i,j){var h=ctxPath+i.class_name||"";if(h.indexOf("?")==-1){h+="?";}else{h+="&";}h+="funCode="+i.fun_code;openNode(i.fun_code,i.text,h,true,true);}});this.quickLinkPanel.add(b);}this.quickLinkPanel.doLayout();for(var d=0;d<this.btnIds.length;d++){var e=Ext.get(this.btnIds[d]);if(e){e.addListener("contextmenu",this.showContextmenu,this,this.btnIds[d]);}}},reload:function(a){this.quickLinkPanel.removeAll();this.loadQuickLink(a);},showContextmenu:function(b,a,d){this.stopDefault(b);var c=new Ext.menu.Menu({items:[{text:"修改",iconCls:"btnEdit",scope:this,handler:function(){var f=d.substring(4);var e=Ext.getCmp(d);new uft.index.QuickLink({editFlag:true,pk_fun:f,fun_name:e.text,fun_code:e.fun_code,class_name:e.class_name}).show();}},{text:"删除",iconCls:"btnDel",scope:this,handler:function(){var e=d.substring(4);uft.Utils.doAjax({scope:this,url:ctxPath+"/common/quick/remove.json",params:{pk_fun:e},isTip:true,success:function(f){var g=Ext.getCmp(d);if(g){this.quickLinkPanel.remove(g);this.quickLinkPanel.doLayout();}}});}},{text:"新窗口打开",scope:this,iconCls:"btnSetting",handler:function(){var f=Ext.getCmp(d);var e=ctxPath+f.class_name||"";if(e.indexOf("?")==-1){e+="?";}else{e+="&";}e+="funCode="+f.fun_code;window.open(e);}}]});c.showAt(b.getXY());},stopDefault:function(a){if(a&&a.preventDefault){a.keyCode=0;a.preventDefault();a.stopPropagation();}else{window.event.keyCode=0;window.event.returnValue=false;}return false;}});