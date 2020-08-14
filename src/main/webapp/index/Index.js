Ext.ns('uft.index');
uft.index.Index = Ext.extend(Ext.Panel, {
	constructor:function(config) {
		Ext.apply(this, config);
		var language = this.language;
	    var treeClickHandler = function(node,event){
	    	if(node.leaf){
		    	var url = node.attributes['url'];
		    	url = ctxPath + url;
		    	if(url.indexOf("?")==-1){
		    		url+="?";
		    	}else{
		    		url+="&";
		    	}
		    	url+="funCode="+node.attributes['code'];
		    	url+="&ip="+this.ip;
		    	url+="&city=" + this.city;
	    		var tab = new Ext.ux.IFrameComponent({
		    		id : node.attributes['code'],
	       			title:node.text,
	       			tabTip : node.text,
	       			closable: true,
	       			border : false,
	                autoScroll: true,
					url : url,
					layout : 'fit'
				});
	       		this.mainPanel.add(tab);
				this.mainPanel.setActiveTab(tab);
	    	}
		};		
		//加入快捷菜单Panel
		var leftItems=[new uft.index.ShortcutPanel()];
		this.treeAry = [];//定义一个所有树的成员变量，方便定位树的节点，目前使用在快捷入口
		
		var funVOs=this.funVOs;//传进来时已经转了
		for(var i=0;i<funVOs.length;i++){
			var contextMenu = new uft.extend.tree.ContextMenu();//默认的右键菜单
			contextMenu.add({
	            text: '加入快捷方式',
	            iconCls : 'btnAdd',
	            scope : contextMenu, //注意这个上下文
	            handler : function(btn,e) {
	            	if(!this.contextNode.leaf){
	            		uft.Utils.showWarnMsg('只有叶子节点才能加入快捷方式!');
	            		return;
	            	}else{
		            	var pk_fun = this.contextNode.id;
		            	var fun_name = this.contextNode.text;
		            	var fun_code = this.contextNode.attributes['code'];
		            	var class_name = this.contextNode.attributes['url'];
		            	new uft.index.QuickLink({
		            		pk_fun:pk_fun,
		            		fun_name:fun_name,
		            		fun_code:fun_code,
		            		class_name:class_name
		            	}).show();
	            	}
	            }
			},{
				text : '新窗口打开',
				scope : contextMenu,
				iconCls : 'btnSetting',
				handler : function(btn,e){
					var node = this.contextNode;
					if(node.leaf){
						var url = node.attributes['url'];
				    	url = ctxPath + url;
				    	if(url.indexOf("?")==-1){
				    		url+="?";
				    	}else{
				    		url+="&";
				    	}
				    	url+="funCode="+node.attributes['code'];
				    	window.open(url);
					}else{
						uft.Utils.showWarnMsg('只能打开叶子节点!');
	            		return;
					}
				}
			});
			var resourceTreeTitle = funVOs[i].fun_name;
			if(language == 'en'){
				resourceTreeTitle = funVOs[i].fun_en_name;
			}
			var resourceTree = new uft.extend.tree.Tree({
				id : funVOs[i].pk_fun,
				title :resourceTreeTitle,
				iconCls : 'btnModule',
				dataUrl:ctxPath + '/getFunTree.json',
				params : {parent_id:funVOs[i].pk_fun},
				border : true,
				rootVisible:false,
				contextMenu : contextMenu
			});
			resourceTree.on('click', treeClickHandler,this);
			leftItems.push(resourceTree);
			this.treeAry.push(resourceTree);
		}
		
		var left = new Ext.Panel({
			id : 'leftPanel',
			title : '导航',
			iconCls : 'btnNav',
			region:'west',
			layout : 'accordion',
			split:true,
			width:200,
			minWidth: 200,
			maxWidth: 200, 
			margins:'0 0 0 3',
            autoScroll: true,
			collapseMode: 'mini',
			tools:[{ 
				id:'refresh', //该id是必须的，css样式会使用该id
                qtip:'刷新',
                on:{
                    click: function(){
                    	for(var i=0;i<leftItems.length;i++){
                    		if(leftItems[i] instanceof Ext.tree.TreePanel){
                    			leftItems[i].getRootNode().reload();
                    		}else{
                    			leftItems[i].reload();
                    		}
                    	}
                    }
                }
            }],
			items:leftItems
		});
		
		/**
		 * 需要在其他地方增加mainPanel的Tab页，所有定义为成员变量
		 */
		var welcomTitle = '';
		if(this.corpName && this.corpName != 'null'){
			welcomTitle = this.corpName+'，'+this.userName+' 您好!';
		}else{
			welcomTitle = this.userName+' 您好!';
		}
	
		var weather = Utils.doSyncRequest('getWeatherForecast.json');
		var html ='';
		if(weather){
			html ='<div style="height:35px; width:205px;padding-top:6px;">'+
					'<div style="position:absolute;left:0px;width:42px;height:30px;padding-top:3px;"><img src='+weather.pictureUrl+'></div>'+
					'<div style="position:absolute;left:42px;height:30px;font-size: 12px;">&nbsp;'+weather.date +'</br>&nbsp;' + weather.weather+' '+weather.wind+' '+weather.temperature+'</div>'+
				  '</div>';
		}
		
		window.setInterval(function(){
			weather = Utils.doSyncRequest('getWeatherForecast.json');
			if(weather){
				html ='<div style="height:35px; width:205px;padding-top:6px;">'+
						'<div style="position:absolute;left:0px;width:42px;height:30px;padding-top:3px;"><img src='+weather.pictureUrl+'></div>'+
						'<div style="position:absolute;left:42px;height:30px;font-size: 12px;">&nbsp;'+weather.date +'</br>&nbsp;' + weather.weather+' '+weather.wind+' '+weather.temperature+'</div>'+
					  '</div>';
			}
			var weatherNode = document.getElementById("weather");
			if(weatherNode){
				weatherNode.innerHTML = html;
			}
		},3600*1000);
		
		//论坛数量的判断,定时器定时去抓取
//		var sms = Utils.doSyncRequest(ctxPath+'/c/sms/getCount.json');
//		if(sms){
//			var smsCount = sms.data;
//			if(smsCount){
//				welcomTitle += "<a  style='color:red' id='smsCount' href='javascript:openSms();'>("+smsCount+")</a>";
//			}
//		}

//		window.setInterval(function(){
//			var sms = Utils.doSyncRequest(ctxPath+'/c/sms/getCount.json');
//			if(sms){
//				var smsCount = sms.data;
//				if(smsCount){
//					var smsCountNode = document.getElementById("smsCount");
//					if(smsCountNode){
//						smsCountNode.innerHTML = "("+smsCount+")";
//					}
//				}
//				
//			}
//			
//		},30*1000);
		
		
		var headerPanel = new Ext.Panel({region:'north', 
										 height:67,
										 id : 'headerPanel',
										 html : '<div id="header" class="bodybg">' +
										 		'	<div class="header">' +
										 		'		<h1 id="logo_h" ondblclick="collapseLeft();"></h1>' +
										 		'			<ul>' +
										 		'               <li id="welcomTitle">'+welcomTitle+'</li>' +			
											 	'				<li><a href="javascript:openEditPasswordWin();" class="amod">修改密码</a><a href="javascript:logout();" class="aout">注销</a></li>' +
											 	'			</ul>' +
												'			<span id="quickEntry"></span>' + 
											 	'	</div>' +
											 	'<div id="weather" style="position:relative; float:right; display:inline;margin-right:10px;">'+html+'</div>'+
											 	'</div>'});
		this.mainPanel = new uft.index.Workspace({leftPanel:left,headerPanel:headerPanel});			
		this.mainPanel.on('beforeremove',function(tab,cmp){
			var app = cmp.el.dom.contentWindow.app;
			if(app && app.statusMgr && (app.statusMgr.getCurrentPageStatus()==1 
						|| app.statusMgr.getCurrentPageStatus()==4
						|| app.statusMgr.getCurrentPageStatus()==6
						|| app.statusMgr.getCurrentPageStatus()==7)){
				return window.confirm("有未保存的数据,确定离开么?");
			}
		});		
		var vp = new Ext.Viewport({
			layout:'border',
			items:[headerPanel, left, this.mainPanel]
		});
	},
});
