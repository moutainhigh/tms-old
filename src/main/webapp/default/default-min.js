/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ux.Portal=Ext.extend(Ext.Panel,{layout:"column",autoScroll:true,cls:"x-portal",defaultType:"portalcolumn",border:false,initComponent:function(){Ext.ux.Portal.superclass.initComponent.call(this);this.addEvents({validatedrop:true,beforedragover:true,dragover:true,beforedrop:true,drop:true});},initEvents:function(){Ext.ux.Portal.superclass.initEvents.call(this);this.dd=new Ext.ux.Portal.DropZone(this,this.dropConfig);},beforeDestroy:function(){if(this.dd){this.dd.unreg();}Ext.ux.Portal.superclass.beforeDestroy.call(this);}});Ext.reg("portal",Ext.ux.Portal);Ext.ux.Portal.DropZone=Ext.extend(Ext.dd.DropTarget,{constructor:function(a,b){this.portal=a;Ext.dd.ScrollManager.register(a.body);Ext.ux.Portal.DropZone.superclass.constructor.call(this,a.bwrap.dom,b);a.body.ddScrollConfig=this.ddScrollConfig;},ddScrollConfig:{vthresh:50,hthresh:-1,animate:true,increment:200},createEvent:function(a,f,d,b,h,g){return{portal:this.portal,panel:d.panel,columnIndex:b,column:h,position:g,data:d,source:a,rawEvent:f,status:this.dropAllowed};},notifyOver:function(v,t,w){var f=t.getXY(),a=this.portal,n=v.proxy;if(!this.grid){this.grid=this.getGrid();}var b=a.body.dom.clientWidth;if(!this.lastCW){this.lastCW=b;}else{if(this.lastCW!=b){this.lastCW=b;a.doLayout();this.grid=this.getGrid();}}var d=0,l=this.grid.columnX,m=false;for(var s=l.length;d<s;d++){if(f[0]<(l[d].x+l[d].w)){m=true;break;}}if(!m){d--;}var q,k=false,i=0,u=a.items.itemAt(d),o=u.items.items,j=false;for(var s=o.length;i<s;i++){q=o[i];var r=q.el.getHeight();if(r===0){j=true;}else{if((q.el.getY()+(r/2))>f[1]){k=true;break;}}}i=(k&&q?i:u.items.getCount())+(j?-1:0);var g=this.createEvent(v,t,w,d,u,i);if(a.fireEvent("validatedrop",g)!==false&&a.fireEvent("beforedragover",g)!==false){n.getProxy().setWidth("auto");if(q){n.moveProxy(q.el.dom.parentNode,k?q.el.dom:null);}else{n.moveProxy(u.el.dom,null);}this.lastPos={c:u,col:d,p:j||(k&&q)?i:false};this.scrollPos=a.body.getScroll();a.fireEvent("dragover",g);return g.status;}else{return g.status;}},notifyOut:function(){delete this.grid;},notifyDrop:function(l,h,g){delete this.grid;if(!this.lastPos){return;}var j=this.lastPos.c,f=this.lastPos.col,k=this.lastPos.p,a=l.panel,b=this.createEvent(l,h,g,f,j,k!==false?k:j.items.getCount());if(this.portal.fireEvent("validatedrop",b)!==false&&this.portal.fireEvent("beforedrop",b)!==false){l.proxy.getProxy().remove();a.el.dom.parentNode.removeChild(l.panel.el.dom);if(k!==false){j.insert(k,a);}else{j.add(a);}j.doLayout();this.portal.fireEvent("drop",b);var m=this.scrollPos.top;if(m){var i=this.portal.body.dom;setTimeout(function(){i.scrollTop=m;},10);}}delete this.lastPos;},getGrid:function(){var a=this.portal.bwrap.getBox();a.columnX=[];this.portal.items.each(function(b){a.columnX.push({x:b.el.getX(),w:b.el.getWidth()});});return a;},unreg:function(){Ext.dd.ScrollManager.unregister(this.portal.body);Ext.ux.Portal.DropZone.superclass.unreg.call(this);}});/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ux.PortalColumn=Ext.extend(Ext.Container,{layout:"anchor",defaultType:"portlet",cls:"x-portal-column"});Ext.reg("portalcolumn",Ext.ux.PortalColumn);/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ux.Portlet=Ext.extend(Ext.Panel,{anchor:"100%",frame:true,collapsible:false,draggable:true,cls:"x-portlet"});Ext.reg("portlet",Ext.ux.Portlet);function _openNode(a,c,b,d){if(d){if(b.indexOf("?")==-1){b+="?";}else{b+="&";}b+="billId="+d;}if(parent&&parent!=window){parent.openNode(d||a,c,b,true,true);}else{window.open(b);}}function openPortletNode(c,a,e,d,f){var b=ctxPath+d;if(b.indexOf("?")==-1){b+="?";}else{b+="&";}b+="funCode="+a;b+="&pk_portlet="+c;if(f){b+="&billId="+f;if(a=="t036"){_openNode(f,"待办事项",ctxPath+"/common/alarm/goTodo.html?pk_alarm="+f);}else{_openNode(f,"待办事项",b);}}else{_openNode(a,e,b);}}var tools=[{id:"refresh",handler:function(c,b,a){if(a.autoLoad){a.getUpdater().update(a.autoLoad);}}}];DefaultPortal=function(b){Ext.apply(this,b);var e=new Ext.ux.PortalColumn({columnWidth:0.33,style:"padding:5px 0 5px 5px"});var l=new Ext.ux.PortalColumn({columnWidth:0.33,style:"padding:5px 0 5px 5px"});var a=new Ext.ux.PortalColumn({columnWidth:0.33,style:"padding:5px 0 5px 5px"});var f=this.pcVOs;if(f){for(var c=0;c<f.length;c++){var k=f[c];var j=k.portlet_code;var h=Ext.getCmp(j);if(!h){var g=k.portlet_name;if(k.query_sql){g+="（<a href=\"javascript:openPortletNode('"+k.pk_portlet+"','"+k.fun_code+"','"+k.fun_name+"','"+k.class_name+"');\">"+k.num_count+"</a>）";}h=new Ext.ux.Portlet({id:j,title:g,frame:false,tools:tools,height:230,autoLoad:"getLatestTodo.html?pk_portlet="+k.pk_portlet});}this.portletCounter.push(h);if(k.column_index==1){e.add(h);}else{if(k.column_index==2){l.add(h);}else{a.add(h);}}}}var d=new Ext.ux.Portal({margins:"0 0 0 0",items:[e,l,a]});return new Ext.Viewport({layout:"fit",items:[d]});};var smsPortlet=new Ext.ux.Portlet({id:"sms",title:"站内信",frame:false,tools:[{id:"gear",qtip:"发布站内信",handler:function(){new nw.sys.SmsSender({isAdd:true}).show();}}].concat(tools),height:230,autoLoad:"c/sms/getTop5.html"});uft.Utils.doAjax({scope:this,isTip:false,url:"c/sms/getCount.json",success:function(a){if(a){smsPortlet.setTitle('站内信（<a href="javascript:openSmsNode()">'+a.data+"</a>）");}}});var tzggPortlet=new Ext.ux.Portlet({id:"tzgg",title:"通知公告",frame:false,tools:tools,height:230,autoLoad:"c/bulletin/getTop5.html?type=0"});var newsPortlet=new Ext.ux.Portlet({id:"news",title:"新闻中心",frame:false,tools:tools,height:230,autoLoad:"c/news/getTop5.html"});var wdglPortlet=new Ext.ux.Portlet({id:"wdgl",title:"文档管理",frame:false,tools:tools,height:230,autoLoad:"c/doc/getTop5.html"});function openBulletinNode(a){_openNode("t022","通知公告",ctxPath+"/c/bulletin/index.html?funCode=t022",a);}function openDocumentNode(a){if(a){location.href=ctxPath+"/doc/download.do?pk_document="+a;}else{_openNode("t03002","政策文件",ctxPath+"/c/doc/index.html?funCode=t03002",a);}}function openNewsNode(b){var a;if(b){a=ctxPath+"/c/news/detail.html?pk_news="+b;}else{a=ctxPath+"/c/news/viewList.html";}_openNode("t024","新闻中心",a);}function openSmsNode(){_openNode("t020","站内信",ctxPath+"/common/sms/index.html?funCode=t020");}