Ext.namespace('uft.jf');
/**
 * 基础panel类,定义了一个与单据无关的基础Panel布局，
 * 包括的widgets有：leftTree,headerGrid,headerCard,footCard,bodyGrids,toolbar,
 * 所有widgets都是可选的，但肯定必须包含一项
 * 需要注意的是bodyGrids是一个
 * 
 * 目前panel支持3种模式，一般模式，treeForm和bodyGrid模式
 * 这个模式是根据传入的toolbar对象的不同来区分的。
 * module: 0 一般模式
 * 		   1 只有表体的模式
 * 		   2 左树右表单的模式
 */
uft.jf.ToftPanel = Ext.extend(uft.jf.UIPanel, {
	context : null,	//与模板相关的信息
	toolbar : null,			//整个panel的工具栏
	/**
	 * 左边树的工具栏，可能需要做一个过滤条件等，一般只有一个过滤条件，因为TreePanel宽度只有200
	 * 只定义这个接口，用户自己定义工具栏内容，以及与tree的联动
	 * @type 
	 */
	treeToolbar : null,		
	bodyAssistToolbar : null,  //表体的工具栏,正在考虑将表体的行操作放在表体工具栏中
	leftTree : null, 	 	//左边树，放在panel的west region
	headerGrid : null, 		//放入表头的表格 ，必须 
	headerCard : null, 		//放入表头的form，必须
	bodyGrids   : null, 	//放入表体的表格定义 ，可能有多个表格，使用数组存储
	footCard : null, 		//表尾
	treePkField : null, 	//当存在左边树时，左边树与表头的关联字段
	treeParentPkField : null, //当左树又表单的模式,即编辑树型菜单的模式,需要设置改参数
	headerPkField : null,  	//当存在表头和表体时，表头与表体的关联字段
	//private
	module : 0,				//当前Panel的模式，支持3中模式
	initPageStatus : uft.jf.pageStatus.OP_INIT,  //页面初始化状态
	clientHeight : document.documentElement.clientHeight,//使用一个成员变量，避免每次操作document
	leftPanelWidth : 200,   //左边树所在的panel的宽度
	toolbarHeight : 26, 	//工具栏的高度
	headerHeight : 242,		//表头高度
	footHeight : 58,		//表尾高度
	waterfallScene : false,	//使用瀑布流来展现表体
	bodyWaterfallScene : false,//表体使用流式布局，当为true时，自动设置waterfallScene为true。
	/**
	 * 使用瀑布流方式展现的情况下，表体的高度。
	 * 当计算的高度小于该值时使用该值
	 * @type Number
	 */
	bodyHeight : 300, 		
	showBodyTabHeader : true, //对于一些简单的主子表(单子表)，用户希望页面简洁些，不想看到页签信息，可以使用该选项
	showbodyAssistToolbar : true,//是否显示表体辅助工具栏
	showBodyInList : false, //是否在列表页面显示表体内容,目前只有在主子的参照制单上有用
	hideBodyGrid : false,   //是否隐藏表体,当表体的所有字段(所有页签的)都是隐藏的时,隐藏bodyTabPanel
	headerSplit : true,	//表体的Panel是否可以拖拽
	
	appValues : null, //整个面板的值，数据结构见通用数据结构,如果appValues存在，则设置到面板中
	
	//private
	headerPanelHasHeaderCard : false, //表头Panel是否包含HeaderCard，当加入HeaderCard的时候会置为true
	ulw : false, //是否是从ulw中链接过来的
	constructor : function (config){
		Ext.apply(this, config); //页面上可以将该值覆盖，
		if(this.bodyWaterfallScene){
			//如果表体使用流式布局，那么整个页面使用流式布局，不可能表体使用了流式布局，表尾不使用
			this.waterfallScene = true;
		}
		this.waterfallSceneGridAry = [];//定义了使用流式布局的表体页签
		if(this.hasBodyGrid()){
			//如果bodyGrid自己定义了使用waterfallScene，那么剩余的bodyGrids则还是使用流式布局
			for(var i=0;i<this.bodyGrids.length;i++){
				var bg = this.bodyGrids[i];
//				if(this.waterfallScene){
//					Ext.apply(bg,{autoHeight:true});
//				}
				if(bg.waterfallScene){//若该表格使用了流式布局,将其放入waterfallSceneGridAry
					this.waterfallScene = true;//只要某个表格使用了流式布局，那么整个页面使用流式布局
					this.waterfallSceneGridAry.push(bg);
					this.bodyGrids.remove(bg);
					this.bodyTitles.remove(this.bodyTitles[i]);//标题与表格是对应的
					i--;
				}
			}	
		}
		//设置页面初始化状态
		if(this.hasBillId()){
			this.currentBillId = this.context.getBillId();
			if(!this.initPageStatus){
				//如果存在billId,并且没有定义初始化状态，那么使用非编辑卡片作为默认状态，也许定义了编辑为初始化状态，这时候就没必要设置默认状态了
				this.initPageStatus = uft.jf.pageStatus.OP_NOTEDIT_CARD;
			}
		}	
		//设置当前Panel的模式
		if(this.toolbar){
			if(this.toolbar instanceof uft.jf.TreeFormToolbar){
				this.module = 2;
				this.initPageStatus=uft.jf.pageStatus.OP_NOTEDIT_CARD;
			}else if(this.toolbar instanceof uft.jf.BodyToolbar){
				//只有表体的情况
				this.module = 1;
				this.showBodyInList = true;//显示表体
				this.showBodyTabHeader = false;//不显示tabHeader 
			}
		}
		if(this.hasBodyGrid() && this.module != 1){//如果是单表体，不需要bodyAssistToolbar
			//存在表体的时候，才需要创建辅助工具栏
			if(!this.bodyAssistToolbar){
				this.bodyAssistToolbar = new uft.jf.BodyAssistToolbar();
			}
			Ext.apply(this.bodyAssistToolbar, {app : this});
			//将body的工具栏传入，可能上下工具栏需要联动操作
			Ext.apply(this.toolbar, {bodyAssistToolbar:this.bodyAssistToolbar});
			//默认都等于1
			if(Utils.getParameter('bbar') != 2){//xuqc 2015-1-13默认使用简洁的辅助工具栏
				this.showbodyAssistToolbar = false;
				for(var i=0;i<this.bodyGrids.length;i++){
					var operator = uft.Utils.getColumn(this.getBodyGrids()[i],'_operator');
					if(operator){
						operator.header = this.bodyAssistToolbar.getOperatorHeader();
						operator.renderer = this.bodyAssistToolbar.getOperatorRenderer;
					}
				}
			}
		}
		Ext.apply(this.toolbar, {app : this});
		//锁定查询条件的高度
		if(this.topQueryForm){
			this.topPanelHeight = this.topQueryForm.getDefineHeight();
		}
		
		//构建leftTree,headerGrid,headerCard,footCard,bodyGrids的布局
		//若存在树，则将树加入west区域
		var mainItem=[]; //整个面板的所有子面板
		if(this.leftPanel){
			//已经定义了左边的Panel
			mainItem.push(this.leftPanel);
		}else if(this.leftTree){
			//不需要创建一个leftPanel了，减少panel的数量
			Ext.apply(this.leftTree,{
				region : 'west',
				layout : 'fit',
				width : this.leftPanelWidth, 
				split : true,
				border : true,
				tbar : this.treeToolbar
			});
			this.leftPanel = this.leftTree;//兼容以前的版本，保留leftPanel变量
			mainItem.push(this.leftPanel);
		}
		//若存在表头部分
		if(this.headerGrid || this.headerCard){
			this.headerPanel = new Ext.Panel({
				region : this.waterfallScene?'center':(this.hasBodyGrid()?'north':'center'), //若存在表体部分，则将header放入布局的north区域，否则放入center区域
				split : this.headerSplit,//2011-08-31 新报销单 重新设置成可拉伸
				autoScroll : Ext.isChrome?false:true, //当卡片页显示时会切换scroll状态
				height : this.getHeaderPanelHeight(),
				border : false,
				layout : 'fit',
				items : this.getHeaderItem()
			});
			mainItem.push(this.headerPanel);
		}

		//若存在表体
		if(this.hasBodyGrid()){
			uft.Utils.assert(this.headerPkField, 'uft.jf.ToftPanel - 缺少参数 headerPkField');
			if(this.bodyWaterfallScene !== false){
				//表体使用流式布局
				this.bodyTabPanel = this.bodyGrids[0];//bodyTabPanel设置为第一个表格
				for(var i=0;i<this.bodyGrids.length;i++){
					var bg = this.bodyGrids[i];
					Ext.apply(bg,this.getWaterfallSceneStyle());
					if(bg.allColumnHide){//所有列都隐藏的话，那么隐藏表格
						bg.hide();
					}
					this.addItemToHeader(bg);
				}
			}else{
				//是否隐藏表体TabPanel,当表体的所有字段都是隐藏时,因此bodyTabPanel
				var hideBodyTabPanel = this.hideBodyGrid;
				if(!hideBodyTabPanel){
					//这个选项会造成的情况是:当初始化成其他状态的时候,此时tabpanel是hidden的,当其他组件已经渲染完成以后,才show出来,
			        //这样导致渲染上面的一点问题,即底部的横线会没有掉.实际上如果初始化状态不时OP_INIT时，不要渲染成hidden
					hideBodyTabPanel = (this.module == 0 && this.initPageStatus==uft.jf.pageStatus.OP_INIT && this.showBodyInList===false)?true:false;
				}
				//如果是瀑布流模式，使用一个最低的高度
				var bodyHeight;
				if(this.waterfallScene){
					var sHeight = this.clientHeight-this.toolbarHeight-(this.footCard?this.footHeight:0)-this.headerHeight;
					if(sHeight < 300){
						sHeight = 300;
					}
					bodyHeight = this.bodyHeight || sHeight;
				}
				if(this.showBodyTabHeader !== false){//是否显示表体的tab title，如果只有一个页签，才能不显示
					for(var i=0;i<this.bodyGrids.length;i++){
						Ext.apply(this.bodyGrids[i],{title : this.bodyTitles[i]});
						//XXX 2013-3-26 如果所有页签都是隐藏的，那么使用hideBodyTabPanel来控制，如果只有一个页签隐藏，则使用allColumnHide来判断
						if(this.bodyGrids[i].allColumnHide){
							Ext.apply(this.bodyGrids[i],{tabCls : 'hideHeader'});
						}
					}
					delete this.bodyTitles;
					var tabPanelConfig = {
						deferredRender : false,//这里不能延迟加载，如加载数据时，必须为所有表体设置数据，但是该表体可能还没有渲染，这样会报错
				        resizeTabs:false,
						region : 'center',
						split : false,
						activeTab:0,//当前激活标签
			        	frame : false,
			        	border : true,
			        	withinForm : this.waterfallScene,
			        	bodyStyle : this.waterfallScene?'margin-bottom:10px;':'',
			        	items : this.bodyGrids
			        	,hidden : hideBodyTabPanel 
					};
					if(this.waterfallScene){
						Ext.apply(tabPanelConfig,{height: bodyHeight});
					}
					if(this.showbodyAssistToolbar){
						Ext.apply(tabPanelConfig,{toolbar: this.bodyAssistToolbar});
						this.bodyTabPanel = new Ext.ux.InlineToolbarTabPanel(tabPanelConfig);
					}else{
						this.bodyTabPanel = new Ext.TabPanel(tabPanelConfig);
					}
				}else{
					this.bodyTabPanel = this.bodyGrids[0];
					Ext.apply(this.bodyTabPanel,{
						region : 'center',
						layout : 'fit',
						border : true,
						autoScroll : false
						,hidden : hideBodyTabPanel
					});
					if(this.waterfallScene){
						Ext.apply(this.bodyTabPanel,{height: bodyHeight});
					}
				}
				if(this.waterfallScene){
					this.addItemToHeader(this.bodyTabPanel);
				}else{
					mainItem.push(this.bodyTabPanel);
				}
			}
			//处理使用流式布局的表体
			for(var i=0;i<this.waterfallSceneGridAry.length;i++){
				Ext.apply(this.waterfallSceneGridAry[i],this.getWaterfallSceneStyle());
				if(this.waterfallSceneGridAry[i].allColumnHide){
					this.waterfallSceneGridAry[i].hide();
				}
				this.addItemToHeader(this.waterfallSceneGridAry[i]);
				this.bodyGrids.push(this.waterfallSceneGridAry[i]);//将拆分的bodyGrid重新进行合并
			}
			delete this.waterfallSceneGridAry;			
		}
		//若存在表尾
		if(this.footCard) {
			//不需要创建footPanel了，减少panel嵌套，提高效率
			Ext.apply(this.footCard,{
				region : 'south', 
				split : false,
				border : false,
				hidden : this.initPageStatus==uft.jf.pageStatus.OP_INIT,
				height : this.footHeight
			});				
			this.footPanel = this.footCard;//兼容旧版本，保留footPanel对象			
			if(this.waterfallScene){
				var firstItem = this.headerCard.items.items[0];
				var footItems = this.footCard.items.items;
				var len = footItems.length;
				if(firstItem.xtype && firstItem.xtype == 'tabpanel'){
					//表头包含多页签
					mainItem.push(this.footPanel);//在IE8及以下版本formPanel中无法直接加入formPanel对象，这里不能add(this.footCard);
				}else{
					for(var i=0;i<len;i++){
						this.headerCard.add(footItems[0]);
					}
					delete this.footCard;//瀑布流模式相当于把footCard的字段加入表头
				}
			}else{
				mainItem.push(this.footPanel);
			}
		}
		var appItem = mainItem;
		if(this.topQueryForm){//简单查询框
			appItem = [];
			Ext.apply(this.topQueryForm,{
				region : 'north'
			});
			this.topPanel = this.topQueryForm;
			if(this.leftPanel){
				//这里为了达到简单查询框只在headerPanel的上面，而leftPanel占满整个高度
				appItem.push(this.leftPanel);
				mainItem.shift(); //删除leftPanel
				appItem.push({
					region : 'center',
					layout : 'border',
					split : false,
					border : false,
					items : [this.topPanel,{
						region : 'center',
						layout : 'border',
						split : false,
						border : false,
						items : mainItem
					}]
				});
			}else{
				appItem.push(this.topPanel);
				appItem.push({
					region : 'center',
					layout : 'border',
					split : false,
					border : false,
					items : mainItem
				});
			}
		}else if(this.leftPanel){
			appItem = [];
			//这里为了达到简单查询框只在headerPanel的上面，而leftPanel占满整个高度
			appItem.push(this.leftPanel);
			mainItem.shift(); //删除leftPanel
			appItem.push({
				region : 'center',
				layout : 'border',
				split : false,
				border : false,
				items : [{
					region : 'center',
					layout : 'border',
					split : false,
					border : false,
					items : mainItem
				}]
			});
		}
		var nodeKey = this.context.getNodeKey();
		var cfg = {
			layout:'border',
			border : false,
			items : appItem
		};
		if(this.bottomBar){
			var btns = this.toolbar.getInstantiatedBtnArray();
			for(var i=0;i<btns.length;i++){
				Ext.apply(btns[i],{scope:this.toolbar});
			}
			Ext.apply(cfg,{buttons : btns});
		}else{
			//这里是一个特殊处理情况，如果使用view的模板，那么不要显示toolbar
			Ext.apply(cfg,{tbar : (nodeKey == 'view' || nodeKey == 'VIEW')?undefined:this.toolbar});
		}
		//如果autoRender为false，这个panel可能作为一个单独的panel要渲染在其他容器中
		if(this.autoRender !== false){
			Ext.apply(cfg,{renderTo : document.body,height : this.clientHeight});
		}
		uft.jf.ToftPanel.superclass.constructor.call(this, cfg);
		//集中注册事件
		this.internalRegisterEvent();
		this.init();
		//首页近来以后不进行调整高度的动作，只有第二次执行到updateStatus的时候才执行
		this.statusMgr.addBeforeUpdateCallback(this.backupHistoryHeight,this);
		this.statusMgr.addAfterUpdateCallback(this.setHeaderHeight,this);
		
		//针对不同的业务状态，单据状态做一些处理
		this.onAppReady();
	},
	//private 流式布局时，组件的style，目前是grid组件
	getWaterfallSceneStyle : function(){
		return {autoHeight:true,border:true,bodyStyle:'margin-bottom:10px;',collapsible:true};
	},	
	//private,流式布局时，将组建添加到表头
	addItemToHeader : function(item){
		var firstItem = this.headerCard.items.items[0];
		var columns;
		if(firstItem.xtype && firstItem.xtype == 'tabpanel'){
			//表头包含多页签
			var firstForm = firstItem.items.items[0];
			columns = firstForm.layoutConfig.columns;
			firstForm.add(Ext.apply(item,{colspan : columns}));
		}else{
			if(firstItem.xtype && firstItem.xtype=='fieldset'){
				columns = firstItem.layoutConfig.columns;
			}else{
				columns = this.headerCard.layoutConfig.columns;
			}
			this.headerCard.add(Ext.apply(item,{colspan : columns}));
		}		
	},	
	getHeaderItem : function(){
		var headerItem=[];
		if(this.headerGrid && this.module==0){
			headerItem = [this.headerGrid];
			if(this.initPageStatus != uft.jf.pageStatus.OP_INIT){
				this.headerGrid.hidden = true;
			}
			if(this.headerCard){
				//这里对于列表状态只判定OP_INIT,初始化状态一般不要设置成OP_NOTEDIT_LIST
				if(this.initPageStatus==uft.jf.pageStatus.OP_INIT){
					this.headerCard.hidden = true;//渲染成hidden，速度更快
				}else{
					this.headerGrid.hidden = true;
				}
			}
		}
		
		if(this.headerCard){
			if(this.initPageStatus != uft.jf.pageStatus.OP_INIT){
				//如果是非初始化状态，则加入headerCard，这种一般出现在有数据的情况，直接打开卡片页，或者直接进来就新增状态
				headerItem.push(this.headerCard);
				this.headerPanelHasHeaderCard = true;
			}
		}
		return headerItem;
	},
	internalRegisterEvent : function(){
		this.registerTreeclickEvent();
		this.registerRowdblclickEvent();
		this.registerRowselectEvent();
		this.registerStatusChange(); 
		this.registerBodyGridAftereditEvent();
		this.registerBodyGridBeforeeditEvent();
		this.registerFieldChangeEvent();
		this.registerReffieldBeforeshowEvent();
		
		//如果表头卡片页包括多页签，则切换多页签的时候重新设置下高度
		//FIXME XUQC 2013-7-31太影响效率了
//		if(this.headerCard){
//			var headerItem = this.headerCard.items.items[0];
//			if(headerItem.xtype=='tabpanel'){
//				headerItem.addListener('tabchange',function(){
//					this.setHeaderHeight();
//				},this);
//			}
//		}		
	},
	/**
	 * 是否包含billId
	 * @return {}
	 */
	hasBillId : function(){
		return (this.context.getBillId() != null && this.context.getBillId().trim().length > 0);
	},		
	/**
	 * 初始化应用
	 */
	init : function(){
		//将页面状态置为初始化状态
		if(this.appValues){
			this.setAppValues(this.appValues);
			var HEADER = this.appValues.HEADER;
			if(typeof(this.getBillStatusField) == 'function'){
				if(!HEADER || !HEADER[this.getBillStatusField()]){
					this.statusMgr.setBizStatus(uft.jf.bizStatus.FREE);//默认是自由态
				}else{
					this.statusMgr.setBizStatus(HEADER[this.getBillStatusField()]);
				}
			}else{
				this.statusMgr.setBizStatus(null);
			}
			this.statusMgr.setPageStatus(this.initPageStatus);
			this.statusMgr.updateStatus();
			if(HEADER){
				this.currentBillId = HEADER[this.getHeaderPkField()];//设置当前pk
			}
		}else if(!this.hasBillId()){
			if(typeof(this.getBillStatusField) == 'function'){
				this.statusMgr.setBizStatus(this.initBizStatus);
			}
			this.statusMgr.setPageStatus(this.initPageStatus);
			this.statusMgr.updateStatus();
		}else{
			this.statusMgr.setPageStatus(this.initPageStatus);
			this.statusMgr.updateStatus();
			this.loadAppValues(null,function(values){
	    		if(values.data){
		    		this.setAppValues(values.data,{saveToCache:true,addToHeaderGrid:true});
		    		if(typeof(this.getBillStatusField) == 'function'){
		    			this.statusMgr.setBizStatus(values.data.HEADER[this.getBillStatusField()]);
		    		}
	    			this.statusMgr.updateStatus();
	    		}
	    	});			
		}
	},	
	backupHistoryHeight : function(){
		if(this.headerPanel){
			if(this.headerCard && this.headerCard.hidden===false){
				this.headerCardHeight = this.headerPanel.getHeight();
			}
			if(!this.headerCardHeight || this.headerCardHeight==0){
				this.headerCardHeight=Math.ceil(this.headerHeight);
			}
		}
	},
	//private
	//耗时操作，调用需谨慎
	setHeaderHeight : function(){
		if(this.headerPanel){
			//打开多个页签后，headerHeight高度可能变为0，可能是浏览器渲染的原因。
			if(this.headerGrid && this.headerGrid.hidden===false){
				if(Ext.isChrome){
					//避免在列表页出现滚动条
					this.headerPanel.setAutoScroll(false);
				}
				//headerGrid的高度始终是这个,对于参照制单的情况,列表页也显示表体,但是不会调用到该方法
				this.headerGridHeight = this.clientHeight-this.toolbarHeight;
				if(this.topQueryForm){
					this.headerGridHeight = this.headerGridHeight -this.topPanelHeight;
				}
				//列表显示
				this.headerGrid.setHeight(this.headerGridHeight);//这里grid也必须设置下高度,否则当存在BillId时,返回列表页高度出现问题
				this.headerPanel.setHeight(this.headerGridHeight);
				if(this.footPanel){
					this.footPanel.setHeight(0);
				}
			}else{
				if(Ext.isChrome){
					//卡片页下需要滚动条，否则表单域会显示不完全
					this.headerPanel.setAutoScroll(true);
				}
				//卡片显示
				this.headerPanel.setHeight(this.headerCardHeight);
				if(this.footPanel){
					this.footPanel.setHeight(this.footHeight);
				}
			}
			this.doLayout();//设置完高度需要重新布局
		}
	},
	//private 
	getHeaderPanelHeight : function(){
		var headerPanelHeight;
		if(this.initPageStatus==uft.jf.pageStatus.OP_INIT){
			if(this.showBodyInList === false){
				headerPanelHeight = this.clientHeight-this.toolbarHeight;
				if(this.topQueryForm){
					//6是伸缩栏的高度
					headerPanelHeight = headerPanelHeight -this.topPanelHeight;
				}
			}else{
				headerPanelHeight = this.headerHeight-this.toolbarHeight;
			}
		}else{
			headerPanelHeight = this.headerHeight-this.toolbarHeight;
		}	
		return headerPanelHeight;
	},
	/**
	 * 这里前台页面已经展现了，在不影响页面展现的情况下，可以为后续操作做一些准备工作,
	 * 这些准备工作必须在页面已经加载完成的情况下，比如调用btn_add_handler时需要调用到子类定义的函数等
	 */
	onAppReady : function(){
		Ext.onReady(function(){
			if(!this.appValues){//如果存在appValues，其本身就是想作为值初始化到app中，此时不需要调用add方法
				if(this.ajaxLoadDefaultValue !== false){
					//1、这里异步加载该单据的默认值
					this.loadDefaultValue(true);
				}
				//2、针对不同的状态做处理
				if(this.initPageStatus == uft.jf.pageStatus.OP_ADD){
					//新增状态
					if(this.defaultValue){
						if(this.defaultValue.data){
							this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW, this.defaultValue.data);
							//通常不需要缓存该内容，但是目前发现表体可能需要使用到append的内容
							this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW_EXTRA, this.defaultValue);
						}
					}
				}else if(this.initPageStatus == uft.jf.pageStatus.OP_EDIT){
					//编辑状态
				}
			}
			//根据页面状态更新操作列按钮禁用启用状态
			if(Utils.getParameter('bbar')!=2){
				var bat = this.bodyAssistToolbar;
				if(bat){
					bat.updateOperatorStatus();
					this.statusMgr.addAfterUpdateCallback(bat.updateOperatorStatus, bat);
				}
			}
		},this);
	},
	/**
	 * 主表的主键
	 * @return {String}
	 */
	getHeaderPkField : function(){
		return this.headerPkField;
	},
	/**
	 * 单据ID字段名，用于存储表头的主键值，
	 * 正常是使用headerPkField进行存储，但是为了后台取数据方便，统一使用该字段存储
	 * @return {String}
	 */
	getBillIdField : function(){
		return "billId";
	},
	/**
	 * 返回左边树的主键
	 * @return {}
	 */
	getTreePkField : function(){
		return this.treePkField;
	},
	/**
	 * 返回树菜单的父级字段的名称
	 * @return {}
	 */
	getTreeParentPkField : function(){
		return this.treeParentPkField;
	},
	/**
	 * 返回左边的Panel，用于存放树
	 */
	getLeftPanel : function(){
		return this.leftPanel;
	},
	/**
	 * 返回HeaderGrid
	 * @return {}
	 */
	getHeaderGrid : function(){
		return this.headerGrid;
	},
	/**
	 * 返回headerCard
	 * @return {}
	 */
	getHeaderCard : function(){
		return this.headerCard;
	},
	/**
	 * 返回表体tab页
	 * 请注意，当showBodyTabHeader===false时，此时实际上返回的是一个Panel，而不是真正的TabPanel
	 * @return {}
	 */
	getBodyTabPanel : function(){
		return this.bodyTabPanel;
	},
	/**
	 * 返回当前活动的表体Grid
	 * @return {}
	 */
	getActiveBodyGrid : function(){
		if(this.showBodyTabHeader!==false){
			return this.bodyTabPanel.getActiveTab();
		}else{
			return this.bodyTabPanel;
		}
	},	
	/**
	 * 设置表体的激活tab
	 * @param {} item
	 */
	setBodyActiveTab : function(item){
		if(this.showBodyTabHeader!==false){
			this.bodyTabPanel.setActiveTab(item);
		}
	},
	/**
	 * 是否包含bodyGrid
	 * @return {Boolean}
	 */
	hasBodyGrid : function(){
		if(this.bodyGrids && this.bodyGrids.length > 0)
			return true;
		return false;
	},	
	/**
	 * 返回表体的所有表格
	 * @return {}
	 */
	getBodyGrids : function(){
		return this.bodyGrids;
	},	
	/**
	 * 返回toolbar
	 * @return {}
	 */
	getToolbar : function(){
		return this.toolbar;
	},
	getBtmPanel : function(){
		return this.btmPanel;
	},
	/**
	 * 重新加载表体的数据
	 */
	reloadBodyGrids : function(){
		for(var i = 0; i < this.bodyGrids.length; i++) {
			this.bodyGrids[i].getStore().reload();
		}
	},
	/**
	 * 清空表体数据
	 */
	clearBodyGrids : function(){
		var len = this.bodyGrids.length;
		for(var i = 0;this.bodyGrids!=null && i < len; i++) {
			//需要判断该表格是否已经被渲染 了，tabPanel中的grid，如果没有经过渲染，调用以下函数会报错。
			var grid = this.bodyGrids[i];
			if(grid.getView().cm){
				//使用该方式判断是否已经被渲染，可能有更好的方式
				grid.getStore().removeAll();
				if(grid.currentRecord){
					grid.currentRecord = null;
				}
				grid.updateTbarInfo();
			}
		}	
	},
	/**
	 * 重新加载当前Tab的grid数据
	 */
	reloadActiveBodyGrid : function(){
		var grid = this.getActiveBodyGrid();
		grid.getStore().reload();
	},
	/**
	 * 重新加载headerGrid的数据
	 */
	reloadHeaderGrid : function(){
		this.headerGrid.getStore().reload();
	},
	/**
	 * 加载指定表格的数据，该表格属于表体，
	 * @param {} grid
	 * @param {} headerPk 关联主表的pk
	 */
	loadBodyGrid : function(grid,headerPk){
		var ds = grid.getStore();
		ds.setBaseParam(this.headerPkField, headerPk);
		ds.load();
	},
	/**
	 * 加载表体的数据,是否只重新加载存在分页的表体
	 * @param {} headerPk
	 */
	loadBodyGrids : function(headerPk,paginationOnly){
		if(this.hasBodyGrid()){
			for(var i = 0; i < this.bodyGrids.length; i++) {
				var grid = this.bodyGrids[i];
				if(paginationOnly && !grid.isAddBbar){
					continue;
				}
				this.loadBodyGrid(grid,headerPk);
			}
		}
	},
	/**
	 * 设置表体的值，值的格式是固定的,可能是多表体
	 * 格式如：{TABNAME1:[],TABNAME2:[]}
	 * @param {} bodyValues
	 */
	setBodyValues : function(values){
		if(values){
			for(var key in values){
				var bodyGrid = Ext.getCmp(key);
				if(bodyGrid){
					bodyGrid.addRecords(values[key]);
    				bodyGrid.updateTbarInfo();
				}
			}
		}
	},
	/**
	 * 设置表头的数据,这里只返回表头的数据,故需要删除缓存中的数据
	 * 与setHeaderValues唯一的不同是,数据没有放到以HEADER作为key的map中
	 */
	_setHeaderValues : function(records,datas){
		if(datas){
			for(var i=0;i<datas.length;i++){
				var key = datas[i][this.getHeaderPkField()];
				this.cacheMgr.removeEntity(key);
			}
			if(records.length==1){
				//提交的是单行记录
				this.setCardValues(datas[0]);//设置表头和表尾的值
				this.setHeaderRecordValues(records[0],datas[0]);//设置行数据
			}else{
				//提交的是多行记录
    			for(var i=0;i<datas.length;i++){
    				var data=datas[i];
    				for(var j=0;j<records.length;j++){
    					var record=records[j];
    					if(record.data[this.getHeaderPkField()]==data[this.getHeaderPkField()]){
    						this.setHeaderRecordValues(record,data);//设置行数据
    					}
    				}
    			}
			}
		}
	},	
	/**
	 * 设置表头的数据,这里只返回表头的数据,故需要删除缓存中的数据
	 */
	setHeaderValues : function(records,datas){
		if(datas){
			for(var i=0;i<datas.length;i++){
				if(datas[i].HEADER){
					var key = datas[i].HEADER[this.getHeaderPkField()];
					this.cacheMgr.removeEntity(key);
				}
			}
			if(records.length==1){
				//提交的是单行记录
				this.setCardValues(datas[0].HEADER);//设置表头和表尾的值
				this.setHeaderRecordValues(records[0],datas[0].HEADER);//设置行数据
			}else{
				//提交的是多行记录
    			for(var i=0;i<datas.length;i++){
    				var data=datas[i];
    				for(var j=0;j<records.length;j++){
    					var record=records[j];
    					if(record.data[this.getHeaderPkField()]==data.HEADER[this.getHeaderPkField()]){
    						this.setHeaderRecordValues(record,data.HEADER);//设置行数据
    					}
    				}
    			}
			}
		}
	},
	/**
	 * 填充Form表单值,包括headerCard和footCard
	 * @param {} values json对象
	 */
	setCardValues : function(values) {
		if(!values){
			return;
		}
		//可能返回nc的表达式,这里需要解析下
		for(var key in values){
			this.resolveNCExpression(key.trim(),values[key]);
		}		
		if(this.headerCard){
			this.headerCard.getForm().reset();
			this.headerCard.getForm().setValues(values);
		}
		if(this.footCard) {
			this.footCard.getForm().reset();
			this.footCard.getForm().setValues(values);
		}
	},
	/**
	 * 返回卡片页的值
	 * @return {}
	 */
	getCardValues : function(){
		var values = {};
		if(app.headerCard){
			Ext.apply(values, app.headerCard.getForm().getFieldValues(false));
		}
		if(app.footCard){
			Ext.apply(values, app.footCard.getForm().getFieldValues(false));
		}
		return values;
	},
	/**
	 * 重置表单值
	 */
	resetCardValues : function(){
		if(this.headerCard){
			this.headerCard.getForm().reset();
		}
		if(this.footCard) {
			this.footCard.getForm().reset();
		}
	},
	/**
	 * 将values中的值设置到表头grid中的某行
	 * @param {} record 行数据
	 * @param {} values 
	 */
	setHeaderRecordValues : function(record,values){
		if(!record){
			return;
		}
		record.beginEdit();
		for(var key in values){
			if(values[key] && values[key].pk){
				//这属于参照类型的返回值
				record.set(key,values[key].pk);
			}else{
				record.set(key,values[key]);
			}
		}
		record.endEdit();
	},
	/**
	 * 加载整个app的value，通常对应一个单据的vo
	 */
	loadAppValues : function(headerPk,func,scope){
		var params=this.newAjaxParams();
		if(headerPk){
			params[this.getBillIdField()]=headerPk;
		}
		if(!func){
			func = function(values){
	    		if(values && values.data){
	    			this.setAppValues(values.data,{saveToCache:true});
	    			this.fireEvent('show',this,values.data);
	    		}
	    	}
		}
	    uft.Utils.doAjax({
	    	scope : scope || this,
	    	params : params,
	    	url : 'show.json',
	    	isTip : false,
	    	success : func
	    });
	    //XXX 如果表体使用分页显示，那么后台返回的整个单据VO不会包括该表体，这里需要单独对该表体进行查询操作
	    this.loadBodyGrids(params[this.getBillIdField()],true);
	},
	/**
	 * 设置整个Panel的值，包括表头、表尾、表体
	 * 注意表体可能是多页签,不管是当页签还是多页签，都必须加上页签作为key
	 * 默认将字表的记录增加到表体，如果有重复的记录，那么请直接刷新表体，
	 * 系统不能自动识别记录是增加还是更新，更新哪一条
	 * @param {} values
	 * 			values的格式必须如下：{HEADER:{aa:'123'},BODY:{tabName1:[{fieldName:'123'}],tabName2:[{fieldName:'456'}]}}
	 * @param {} config
	 * 			配置信息，包括：
	 * 			saveToCache,是否加入缓存管理器,默认为false
	 * 			addToHeaderGrid,增加到表头列表，默认为false
	 * 			updateToHeaderGrid,更新表头的相应记录,默认为false
	 * 			updateRecord,需要更新的行，如果没有传入，那么使用当前选中的行
	 * 			removeAllBodyGrid,是否先将表体清空，默认为true,多个页签使用数组方式存储
	 * 			config.cavloan,是否是核销调用的，核销的情况比较特殊，做了一些特殊处理
	 * 			updateBody,是否更新表体
	 */
	setAppValues : function(values,config){
		if(!config){
			config = {};
		}
		if(values.HEADER){
			if(config.saveToCache === true){
				var key = values.HEADER[this.getHeaderPkField()];
				if(!key){
					//如果没有主键，可能是新增的情况
					key = uft.jf.Constants.PK_NEW;
				}
				this.cacheMgr.addEntity(key,values);
			}
			//存在表头、表尾数据,此时一般是在编辑状态，表头的列表不需要更新
			this.setCardValues(values.HEADER);
			if(this.headerGrid){
				if(config.addToHeaderGrid === true){
					//当新增记录，保存时，设置默认的业务状态
					var store = this.headerGrid.getStore();
					var recordType = store.recordType;
			        var record = new recordType();
			        this.setHeaderRecordValues(record,values.HEADER);
			        store.removeAll();//先移除当前数据
			        store.insert(0, record);
			        this.selectHeaderGridRowOnBO(0);//选择行,但是不触发onRowselect事件
			        
			        //更新底部工具栏的显示信息
			        store.totalLength=store.getTotalCount()+1;
			        if(store.pks){//pks可能不存在，使用reload可以清空pks
			        	store.pks.push(record.get(this.headerGrid.getPkFieldName()));
			        }
			        this.headerGrid.updateTbarInfo();
				}else if(config.updateToHeaderGrid === true){
					//更新表头所选行
					var r = config.updateRecord;
					if(!r){
						r=uft.Utils.getSelectedRecord(this.headerGrid);
					}
					this.setHeaderRecordValues(r,values.HEADER);
				}
			}
		}
		if(values.BODY && config.updateBody !== false){
			var index = 0;
			for(var key in values.BODY){
				//key表示的是表体的tab，也是grid的id
				var bodyGrid = Ext.getCmp(key);
				if(bodyGrid){
					//这里的调用方式与Store的loadRecords一样
					var length = bodyGrid.getStore().getCount();
					var removeAllBodyGrid = true;
					if(Ext.isArray(config.removeAllBodyGrid)){
						//如果是数组则根据页签编号来取
						removeAllBodyGrid = config.removeAllBodyGrid[index];
					}else{
						//可能是单表体情况
						removeAllBodyGrid = config.removeAllBodyGrid;
					}
					var deleted = []; //用于保存已经删除的记录，这些记录因为某些原因要保存起来，并且合并到新的modified中
					if(removeAllBodyGrid !== false){
						if(config.cavloan === true){
							//核销情况，核销情况比较特殊，做特殊处理
							var modified = bodyGrid.getStore().modified;
							for(var i=0;i<modified.length;i++){
								if(!modified[i].store){
									//不是删除的数据,从modified中移除
									//这里有一种情况，当表体中存在记录，此时把记录删除，然后增加一条记录，点击核销，然后点击保存，此时必须把刚才删除的记录发送到后台真正删除，否则改记录在保存后会重新恢复
									deleted.push(modified[i]);
								}
							}
						}
						//清空原有的数据,默认执行此操作
						bodyGrid.getStore().removeAll();
						bodyGrid.getStore().modified=[];//清空当前改变的值
						length = 0;
					}

					var datas = values.BODY[key];
					if(datas){
						var records = [],store=bodyGrid.getStore();
						var recordType = store.recordType;
						for(var i=0;i<datas.length;i++){
							var isExist = false;
							for(var j=0;j<store.getCount();j++){
								var oldRecord = store.getAt(j);
								if(oldRecord.get(bodyGrid.pkFieldName) == datas[i][bodyGrid.pkFieldName]){
									//记录已经存在，更新，这种情况出现在上面的config.removeAllBodyGrid=false
									isExist = true;
									//会自动标记为已修改的纪录,放在modified中
									oldRecord.beginEdit();
									for(var dataKey in datas[i]){
										oldRecord.set(dataKey,datas[i][dataKey]);
									} 
									oldRecord.endEdit();
								}
							}
							if(!isExist){
						        var record = new recordType();
						        record.beginEdit();
								for(var dataKey in datas[i]){
									//这里情况比较特殊，当值为null时，使用record.set是设置不进去的，但是目前遇到一个问题，核销的时候需要将核销字段设置进去（该值可能是null）
									if(config.cavloan === true){
										//FIXME XUQC 目前只对核销做特殊处理
										if(datas[i][dataKey]){
											record.set(dataKey,datas[i][dataKey]);
										}else{
											record.data[dataKey] = datas[i][dataKey];
										}
									}else{
										record.set(dataKey,datas[i][dataKey]);
									}
								}  
								record.endEdit();
								record.join(store);
								records.push(record);
								if(config.cavloan === true){
									//核销情况比较特殊，做特殊处理，对于核销后的返回记录，都加入modified中
									store.modified.push(record);
								}else if(!record.get(bodyGrid.pkFieldName)){
				            		//没有pk值的记录,这可能是使用load的数据，但是用户希望把他们作为已修改的数据
				            		store.modified.push(record);
				            	}
							}
						}						
			            store.data.addAll(records);
			            store.totalLength = length + records.length;
			            bodyGrid.updateTbarInfo();
			            store.fireEvent('datachanged', store);
			            delete records;
			            
			            //将之前保存的删除的记录加入新的修改的集合，这种情况只有在核销时才出现
						for(var i=0;i<deleted.length;i++){
							store.modified.push(deleted[i]);
						}
					}
				}
				index++;
			}
		}
	},	
	//注册左边树的点击事件
	registerTreeclickEvent : function(){
		if(this.leftTree) {
			//若存在左边树时，增加树点击事件
			this.leftTree.on('click', function(node, e){
				this.treeClickHandler(node);
			}, this);
		}
	},
	//点击树和选择树时候的处理函数
	treeClickHandler : function(node){
		var nid = node.id;
		if(this.module==2){
			var appValues = this.cacheMgr.getEntity(nid);
			if(appValues){
				this.setAppValues(appValues);
			}else{
				//只包含卡片页的情况,
				var params=this.newAjaxParams();
				params[this.getTreePkField()]=nid;
				params['isRoot']=this.leftTree.getRootNode().id==nid; //是否是根节点
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	url : 'show.json',
			    	isTip : false,
			    	success : function(values){
			    		if(values&&values.data){
			    			this.cacheMgr.addEntity(nid,values.data);
			    			//把树的节点id作为key
			    			this.setAppValues(values.data);
			    		}
			    	}
			    });
			}
		}else if(this.headerGrid){
			this.currentBillId = null;//2015-03-15 左树右表的情况，选择了左树，那么刷新右表，此时列表已经没有选中一行记录了，那么清空当前的billId
			//tree & headerGrid的情况
			var ds = this.headerGrid.getStore();
			//如果此时已经使用了查询窗口，那么需要清空查询窗口的参数
			delete ds.baseParams[uft.jf.Constants.PUB_PARAMS];
			//如果是第一次查询，此时没有lastOptions对象
			if(ds.lastOptions && ds.lastOptions.params){
				delete ds.lastOptions.params[uft.jf.Constants.PUB_PARAMS];
			}
			
			//将树形参数加入到基础参数中，这样在翻页的时候就会带上该参数
			ds.baseParams[this.getTreePkField()] = nid;
			var options = {};
			options.params = {};
			options.params[this.getTreePkField()] = nid;
			ds.reload(options);
			
			//当存在bodyGrids时，清除脏数据
			if(this.hasBodyGrid()){
				for(var i = 0; i < this.bodyGrids.length; i++) {
					var ds = this.bodyGrids[i].getStore();
					ds.removeAll();
				}
			}
			//如果这时候不卡片页状态，则切换到列表页状态
			//FIXME 选中树怎么会切换页面状态呢？
			//左树右表的情况下,如果此时是卡片状态，需要切换到列表
			this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
			this.statusMgr.updateStatus();
		}else if(this.hasBodyGrid()){
			//tree & bodyGrid的情况
			var ds = this.getActiveBodyGrid().getStore();
			ds.baseParams[this.getTreePkField()] = nid;
			var options = {};
			options.params = {};
			options.params[this.getTreePkField()] = nid;
			ds.reload(options);
			this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
			this.statusMgr.updateStatus();
		}
	},
	//注册表头的行双击事件
	registerRowdblclickEvent : function(){
		if(this.headerGrid&& this.headerCard){
			//双击headerGrid的某一行时，切换到卡片状态
			this.headerGrid.addListener({
				'rowdblclick' : this.onRowdblclick,scope : this
			});	
		}
	},
	/**
	 * 双击行动作
	 */
	onRowdblclick : function(grid,rowIndex,e){
		this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
		this.statusMgr.updateStatus();
		var record = this.headerGrid.getStore().getAt(rowIndex);
		var headerPk = record.data[this.getHeaderPkField()];
		var ts = record.data['ts'];
		if(record) {
			this.currentBillId = headerPk;
			var appBufferData = this.cacheMgr.getEntity(headerPk,ts);
			if(appBufferData){
				//缓存中已存在
				this.setAppValues(appBufferData);
				//XXX 如果表体使用分页显示，那么后台返回的整个单据VO不会包括该表体，这里需要单独对该表体进行查询操作
	    		this.loadBodyGrids(headerPk,true);
			}else{
				this.loadAppValues(headerPk);
			}						
		}
	},
	//注册表头的行选择事件
	registerRowselectEvent : function(){
		if(this.headerGrid){
			//增加headerGrid的行选择事件
			this.headerGrid.getSelectionModel().addListener({
				'rowselect' : this.onRowselect,scope : this
			});	
		}
	},
	//行选择事件,可被重写
	onRowselect : function(sm, rowIndex, record) {
		this.statusMgr.removeBeforeUpdateCallback(this.backupHistoryHeight,this);
		this.statusMgr.removeAfterUpdateCallback(this.setHeaderHeight,this);
		this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
		this.statusMgr.updateStatus();
		this.statusMgr.addBeforeUpdateCallback(this.backupHistoryHeight,this);
		this.statusMgr.addAfterUpdateCallback(this.setHeaderHeight,this);
		if(this.showBodyInList !== false && this.hasBodyGrid()){
			var appBufferData = this.cacheMgr.getEntity(record.data[this.headerPkField],record.data['ts']);
			if(appBufferData){
				//缓存中已经存在
				this.setAppValues(appBufferData);
			}else{
				//在timeout的时间内多次点击时，只触发最后一次（指的是子表抓取，状态切换是立即的）
				if(this.timeoutID!=null){
					clearTimeout(this.timeoutID);
				}
				this.timeoutID=setUftTimeout(this.loadBodyGridsForEvent,null,this.timeout,this.getBodyGrids(),record,this.headerPkField);
			}
		}		
		this.headerGrid.fireEvent('afterrowselect',record,rowIndex);
	},
	//选择表头的某行,但不触发rowselect事件,主要是该事件会将pageStatus改掉,但有些情况不需要更改
	selectHeaderGridRowOnBO : function(row){
		this.headerGrid.getSelectionModel().un('rowselect',this.onRowselect,this);
		this.headerGrid.getSelectionModel().selectRow(row);
		this.headerGrid.getSelectionModel().on('rowselect',this.onRowselect,this);
	},
	//表头的rowSelect事件
	//注意这个函数内部的this对象,此时this指向页面对象
	loadBodyGridsForEvent : function(bodyGrids, record,headerPkField) {
		if(bodyGrids){
			for(var i = 0; i < bodyGrids.length; i++) {
				var ds = bodyGrids[i].getStore();
				ds.setBaseParam(headerPkField, record.data[headerPkField]);
				ds.load();
			}
		}
	},	
	//注册表体的afteredit事件，用于执行模板中设置的js代码
	registerBodyGridAftereditEvent : function(){
		if(this.hasBodyGrid()){
			var bodyGrids = this.getBodyGrids();
			for(var i=0;i<bodyGrids.length;i++){
				var grid = bodyGrids[i];
				grid.addListener('afteredit',function(e){
					/**
					 * 定义如下变量，方便使用
					 */
					var record=e.record;
					var row = e.row;
					var grid = e.grid;
					var column = grid.originalColumns[e.column];//列对象
					var fieldName=e.field;
					var value=e.value;
					var originalValue=e.originalValue;		
					//用于执行公式的参数
					var params = record.data;
					var format = 'Y-m-d';
					if(column.editor){
						if(column.editor.format){
							format = column.editor.format;
						}
						if(column.editor.xtype=='bodyreffield'){
							//对于参照，其值使用pk域的值
							value = record.get(column.editor.idcolname);
							originalValue = column.editor.orginalPk;
						}
					}
					if(value instanceof Date){
						value = value.dateFormat(format);
					}
					if(originalValue instanceof Date){
						originalValue = originalValue.dateFormat(format);
					}
					//有些自定义1中的脚本的参数直接使用的是event，一般推荐使用具体的值，如value，originalValue
					e.originalValue = originalValue;
					e.value = value;
					
					//是否执行了同步请求
					var syncRequest = false;
					//这里不需要判断值是否改变了，在抛出的时候已经判断了
					//1、执行编辑公式
					if(column.hasEditformula===true){
						//若是表格，此时传入的参数包括所在行的所有字段值
						params['pkBilltemplet']=column.pkBilltemplet;
						params['pkBilltempletB']=column.pkBilltempletB;	
						var cacheKey=Ext.encode(params);
						var retObj = this.cacheMgr.getEntity(cacheKey);
						if(!retObj){
							Ext.getBody().mask(uft.jf.Constants.PROCESS_MSG);//显示操作提示
							var values = Utils.doSyncRequest('execFormula.json',params,'POST');
							this.cacheMgr.addEntity(cacheKey,values);
							//设置执行公式后的值
							this.setRetObj(column.editor,record,values);	
							Ext.getBody().unmask();
							syncRequest = true;
						}else{
							//设置执行公式后的值
							this.setRetObj(column.editor,record,retObj);
						}						
					}
					if(typeof(afterEditBody) == 'function'){
						afterEditBody(e);
					}else{
						//2、若该列存在js脚本,则执行之
						if(column.script){
							eval(column.script);//执行模板【自定义1】中的脚本
						}
					}
					//3、如果此时【保存】按钮获得焦点，并且执行了同步请求，判定用户实际上是直接点击了【保存】按钮，但是因为执行了同步请求，导致点击不生效
					if(syncRequest && this.toolbar.btn_save && this.toolbar.btn_save.getEl().hasClass('x-btn-click')){
						//手动执行保存动作
						this.toolbar.btn_save_handler();
					}
				},this);
			}
		}
	},
	/**
	 * 注册表体的编辑前事件
	 */
	registerBodyGridBeforeeditEvent : function(){
		if(this.hasBodyGrid()){
			var bodyGrids = this.getBodyGrids();
			for(var i=0;i<bodyGrids.length;i++){
				var grid = bodyGrids[i];
				grid.addListener('beforeedit',function(e){
					var returnValue;
					if(typeof(beforeEditBody) == 'function'){
						returnValue = beforeEditBody(e);
					}else if(e.grid.originalColumns&&Utils.isNotBlank(e.grid.originalColumns[e.column].beforeEditScript)){
						//若该列存在js脚本,则执行之
						var record=e.record;
						var row = e.row;
						var grid = e.grid;
						var column = grid.originalColumns[e.column];//列对象
						var fieldName=e.field;
						var value=e.value;
						var format = 'Y-m-d';
						if(column.editor){
							if(column.editor.format){
								format = column.editor.format;
							}
							if(column.editor.xtype=='bodyreffield'){
								//对于参照，其值使用pk域的值
								value = record.get(column.editor.idcolname);
							}
						}
						if(value instanceof Date){
							value = value.dateFormat(format);
						}
						//有些自定义1中的脚本的参数直接使用的是event，一般推荐使用具体的值，如value
						e.value = value;
						returnValue = eval(column.beforeEditScript);//执行模板【自定义2】中的脚本
					}
					if(returnValue===false){
						//这么写是为了兼容以前，在没有返回true的情况下，也继续执行。
						return false;
					}
				},this);
			}
		}
	},	
	//注册form中field的change事件，用于执行模板中设置的js代码
	registerFieldChangeEvent : function(){
		if(this.getHeaderCard()){
			this.getHeaderCard().getForm().items.each(function(field){ 
				if(!field.hidden && !field.readOnly && field.xtype!='hidden'){
					field.addListener('change',function(field,value,originalValue){
						if(value instanceof Date){
							value = value.dateFormat(field.format||'Y-m-d');
						}	
						if(originalValue instanceof Date){
							originalValue = originalValue.dateFormat(field.format||'Y-m-d');
						}
						
						//是否执行了同步请求
						var syncRequest = false;
						//1、执行编辑公式
						if(field.hasEditformula===true){
							// modify by xuqc 2012-05-16 执行公式时，把整个表头的值都传过去。
							var params = this.getHeaderCard().getForm().getFieldValues(false);
//							var params = {};
//							var colName=field.idcolname==null?field.id:field.idcolname;
//							params[colName]=value;
							params['pkBilltemplet']=field.pkBilltemplet;
							params['pkBilltempletB']=field.pkBilltempletB;							
							var cacheKey=Ext.encode(params);
							var retObj = this.cacheMgr.getEntity(cacheKey);
							if(!retObj){
								var body = Ext.getBody();
								body.mask(uft.jf.Constants.PROCESS_MSG);//显示操作提示
								//不要使用异步方法,以免点击保存后还出现没有执行完
								var values = Utils.doSyncRequest('execFormula.json',params,'POST');
								this.cacheMgr.addEntity(cacheKey,values);
								//设置执行公式后的值
								this.setRetObj(field,null,values);		
								body.unmask();
								syncRequest = true;
							}else{
								//设置执行公式后的值
								this.setRetObj(field,null,retObj);								
							}
						}
						if(typeof(afterEditHead) == 'function'){
							afterEditHead(field,value,originalValue);
						}else{
							//2、若存在脚本，则执行之
							if(Utils.isNotBlank(field.script)){
								eval(field.script);//执行模板【自定义1】中的脚本
							}
						}
						//3、如果此时【保存】按钮获得焦点，并且执行了同步请求，判定用户实际上是直接点击了【保存】按钮，但是因为执行了同步请求，导致点击不生效
						if(syncRequest && this.toolbar.btn_save && this.toolbar.btn_save.getEl().hasClass('x-btn-click')){
							//手动执行保存动作
							this.toolbar.btn_save_handler();
						}
					},this);
				}
			},this);
		}
	},	
	//注册表头参照域的beforeshow事件，执行模板中设置的js代码。
	//注意与表体的beforeedit执行的代码一样
	registerReffieldBeforeshowEvent : function(){
		if(this.getHeaderCard()){
			this.getHeaderCard().getForm().items.each(function(field){ 
				if(!field.hidden && !field.readOnly && field.xtype=='headerreffield'){
					field.addListener('beforeshow',function(field){
						var returnValue;
						if(typeof(beforeEditHead) == 'function'){
							returnValue = beforeEditHead(field);
						}else{
							if(Utils.isNotBlank(field.beforeEditScript)){
								returnValue = eval(field.beforeEditScript);//执行模板【自定义2】中的脚本
							}
						}
						if(returnValue === false){
							return false;
						}
					},this);
				}
			});
		}
	},		
	/**
	 * 设置执行公式后的返回值
	 * @param {} field
	 * 				参照域对象
	 * @param {} record
	 * 				如果是在表格中，则表示所在行
	 * @param {} retObj
	 * 				执行编辑公式后的返回值，以键值对存储
	 */
	setRetObj : function(field,record,retObj){
		if(!retObj){
			return;
		}
		if(!Ext.isIE){
			if(retObj instanceof Document){
				return;
			}
		}
		if(record){
			record.beginEdit();
			for(var key in retObj){
				//解析nc表达式
				//FIXME 这里的处理还不完整，可编辑表格的处理会有些不同，只是现在还没遇到这种情况
				this.resolveNCExpression(key.trim(),retObj[key]);
				record.set(key.trim(),retObj[key]);
			}
			record.endEdit();
		}else{
			for(var key in retObj){
				//解析nc表达式
				this.resolveNCExpression(key.trim(),retObj[key]);
				var c = Ext.getCmp(key.trim());
				if(c){
					//FIXME,2011-09-28,有些参照可能会返回自身，那么会造成死循环
					//var originalValue = c.getValue();
					c.setValue(retObj[key]);
					//c.fireEvent('change',c,retObj[key],originalValue); //当值改变的时候抛出事件
				}
			}
		}
	},	
	//解析NC中返回的一些公式
	resolveNCExpression : function(key,value){
		//兼容nc中$Editable[vreceivername]公式，表示vreceivername可编辑
		if(key.indexOf('$Editable')==0){
			var start = key.indexOf('[');
			var end = key.indexOf(']');
			var id = key.substring(start+1,end);
			var cmp = Ext.getCmp(id);
			if(cmp && typeof(cmp.setDisabled) == 'function'){
				if(value == 'Y'){
					cmp.setReadOnly(false);
				}else{
					cmp.setReadOnly(true);
				}
			}
		}
	},	
	//注册各个组件的onstatuschange属性，与页面状态和业务状态相关		
	registerStatusChange : function(){
		this.registerToolbarStatusChange();
		this.registerLeftTreeStatusChange();
		this.registerTreeToolbarStatusChange();
		this.registerTopPanelStatusChange();
		this.registerHeaderGridStatusChange();
		this.registerCardStatusChange();
		this.registerBodyGridsStatusChange();
	},	
	//注册toolbar,bodyAssistToolbar的onstatuschange属性
	registerToolbarStatusChange : function(){
		if(this.toolbar){
			var items = this.toolbar.items.items;
			for(var i=0;i<items.length;i++){
				if(items[i].menu&&items[i].menu.items){
					//处理下拉菜单
					for(var j=0;j<items[i].menu.items.items.length;j++){
						this.statusMgr.addListener(items[i].menu.items.items[j],items[i].menu.items.items[j].onstatuschange);
						this.statusMgr.addBizListener(items[i].menu.items.items[j],items[i].menu.items.items[j].onbizstatuschange);
					}
				}
				this.statusMgr.addListener(items[i],items[i].onstatuschange);
				this.statusMgr.addBizListener(items[i],items[i].onbizstatuschange);
			}
		}
		if(this.bodyAssistToolbar){
			var items = this.bodyAssistToolbar.items.items;
			for(var i=0;i<items.length;i++){
				this.statusMgr.addListener(items[i],items[i].onstatuschange);
				this.statusMgr.addBizListener(items[i],items[i].onbizstatuschange);
			}
		}
	},
	//注册leftTree的onstatuschange属性
	registerLeftTreeStatusChange : function(){
		if(this.leftTree){
			this.leftTree.onstatuschange = function(tree, status){
				if(status==uft.jf.pageStatus.OP_INIT){
					tree.enable();
				}else if(status==uft.jf.pageStatus.OP_ADD){
					tree.disable();
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
					tree.enable();
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
					tree.enable();
				}else if(status == uft.jf.pageStatus.OP_EDIT){
					tree.disable();
				}else if(status == uft.jf.pageStatus.OP_REVISE){
					tree.disable();
				}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
					tree.disable();
				}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
					tree.disable();
				}
			};
			this.statusMgr.addListener(this.leftTree,this.leftTree.onstatuschange);
		}
	},
	/**
	 * 注册TreePanel的toobar的状态
	 */
	registerTreeToolbarStatusChange : function(){
		if(this.treeToolbar){
			this.treeToolbar.onstatuschange = function(treeToolbar, status){
				if(status==uft.jf.pageStatus.OP_INIT){
					treeToolbar.setDisabled(false);
				}else if(status==uft.jf.pageStatus.OP_ADD){
					treeToolbar.setDisabled(true);
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
					treeToolbar.setDisabled(false);
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
					treeToolbar.setDisabled(false);
				}else if(status == uft.jf.pageStatus.OP_EDIT){
					treeToolbar.setDisabled(true);
				}else if(status == uft.jf.pageStatus.OP_REVISE){
					treeToolbar.setDisabled(true);
				}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
					treeToolbar.setDisabled(true);
				}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
					treeToolbar.setDisabled(true);
				}
			};
			this.statusMgr.addListener(this.treeToolbar,this.treeToolbar.onstatuschange);
		}
	},	
	registerTopPanelStatusChange : function(){
		if(this.topPanel){
			this.topPanel.onstatuschange = function(panel, status){
				if(status==uft.jf.pageStatus.OP_INIT){
					panel.setHeight(this.topPanelHeight);
				}else if(status==uft.jf.pageStatus.OP_ADD){
					panel.setHeight(0);
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
					panel.setHeight(this.topPanelHeight);
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
					panel.setHeight(0);
				}else if(status == uft.jf.pageStatus.OP_EDIT){
					panel.setHeight(0);
				}else if(status == uft.jf.pageStatus.OP_REVISE){
					panel.setHeight(0);
				}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
					panel.setHeight(this.topPanelHeight);
				}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
					panel.setHeight(0);
				}
			};
			this.statusMgr.addListener(this.topPanel,this.topPanel.onstatuschange,this);
		}
	},	
	//注册headerGrid的onstatuschange属性
	registerHeaderGridStatusChange : function(){
		if(this.headerGrid){
			this.headerGrid.onstatuschange = function(headerGrid, status){
				if(status==uft.jf.pageStatus.OP_INIT){
					if(headerGrid.hidden === true){
						headerGrid.show();	
					}
				}else if(status==uft.jf.pageStatus.OP_ADD){
					if(headerGrid.hidden === false){
						headerGrid.hide();
					}
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
					if(headerGrid.hidden === true){
						headerGrid.show();
					}
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
					if(headerGrid.hidden === false){
						headerGrid.hide();
					}
				}else if(status == uft.jf.pageStatus.OP_EDIT){
					if(headerGrid.hidden === false){
						headerGrid.hide();
					}
				}else if(status == uft.jf.pageStatus.OP_REVISE){
					if(headerGrid.hidden === false){
						headerGrid.hide();
					}
				}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
					if(headerGrid.hidden === true){
						headerGrid.show();
					}
				}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
					if(headerGrid.hidden === false){
						headerGrid.hide();
					}
				}
			};
			this.statusMgr.addListener(this.headerGrid,this.headerGrid.onstatuschange, this);
		}
	},
	//注册headerCard & footCard的onstatuschange属性
	registerCardStatusChange : function(){
		if(this.headerCard){
			this.headerCard.onstatuschange = function(headerCard, status){
				if(status==uft.jf.pageStatus.OP_INIT){
					if(headerCard && this.headerGrid){
						if(headerCard.hidden === false){
							headerCard.hide();
						}
						headerCard.setReadOnly();
					}else if(headerCard && !this.headerGrid){
						if(!this.headerPanelHasHeaderCard){
							this.headerPanel.add(headerCard);
							this.headerPanelHasHeaderCard = true;
						}
						if(headerCard.hidden === true){
							headerCard.show();
						}
						headerCard.setReadOnly();
					}
					if(this.footCard && this.headerGrid){
						if(this.footCard.hidden === false){
							this.footCard.hide();
						}
						this.footCard.setReadOnly();
					}else if(this.footCard && !this.headerGrid){
						if(this.footCard.hidden === true){
							this.footCard.show();
						}
						this.footCard.setReadOnly();
					}
				}else if(status==uft.jf.pageStatus.OP_ADD){
					this._setHeaderCardStatus(false,true);
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
					this._setHeaderListStatus();
				}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
					this._setHeaderCardStatus(false,false);
				}else if(status == uft.jf.pageStatus.OP_EDIT){
					this._setHeaderCardStatus(false,true,headerCard.editableFields);
				}else if(status == uft.jf.pageStatus.OP_REVISE){
					this._setHeaderCardStatus(true,false);
				}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
					this._setHeaderListStatus();
				}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
					this._setHeaderCardStatus(false,true);					
				}
			};
			this.statusMgr.addListener(this.headerCard,this.headerCard.onstatuschange, this);
		}
	},
	/**
	 * 设置列表状态下各个表单的状态
	 */
	_setHeaderListStatus : function(){
		if(this.headerCard){
			if(this.headerCard.hidden === false){
				this.headerCard.hide();
			}
			this.headerCard.setReadOnly();
		}
		if(this.footCard){
			if(this.footCard.hidden === false){
				this.footCard.hide();
			}
			this.footCard.setReadOnly();
		}
	},
	/**
	 * 设置卡片状态下表单的状态，方便重复调用
	 * @param {} reviseFlag 是否是修订
	 * @param {} editable	表单是否是可编辑的状态
	 * @param {} editableFields 自定义了哪些可编辑的字段，用于审核修改中
	 */
	_setHeaderCardStatus : function(reviseFlag,editable,editableFields){
		if(this.headerCard){
			if(!this.headerPanelHasHeaderCard){
				this.headerPanel.add(this.headerCard);
				this.headerPanelHasHeaderCard = true;
			}
			if(this.headerCard.hidden === true){
				this.headerCard.show();
			}
			if(reviseFlag){
				this.headerCard.enableRevise();
			}else{
				if(editable){
					this.headerCard.reStoreReadOnly(editableFields);
				}else{
					this.headerCard.setReadOnly();
				}				
			}
		}
		if(this.footCard){
			if(this.footCard.hidden === true){
				this.footCard.show();
			}
			if(reviseFlag){
				this.footCard.enableRevise();
			}else{
				if(editable){
					this.footCard.reStoreReadOnly();
				}else{
					this.footCard.setReadOnly();
				}
			}
		}		
	},
	//注册bodyGrids的onstatusChange属性
	registerBodyGridsStatusChange : function(){
		if(this.showBodyInList === true && this.module == 0){
			//在列表页显示表体,这种情况只出现在参照制单,不需要注册该事件
			//这里要注意，如果是单表体的情况，还是需要注册的
			return;
		}
		if(this.bodyGrids && !this.hideBodyGrid){
			for(var i=0; i<this.bodyGrids.length; i++){
				this.bodyGrids[i].onstatuschange = function(bodyGrid, status){
					if(status==uft.jf.pageStatus.OP_INIT){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.setDisabled();
						}
						if(this.bodyTabPanel.hidden === false && this.module == 0){
							//只有模式1才有显示和隐藏表体的情况
							this.bodyTabPanel.hide();
						}
					}else if(status==uft.jf.pageStatus.OP_ADD){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.reStoreDisabled();
						}
						if(this.bodyTabPanel.hidden === true && this.module == 0){
							this.bodyTabPanel.show();
						}
					}else if(status == uft.jf.pageStatus.OP_NOTEDIT_LIST){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.setDisabled();
						}
						if(this.bodyTabPanel.hidden === false && this.module == 0){
							this.bodyTabPanel.hide();
						}
					}else if(status == uft.jf.pageStatus.OP_NOTEDIT_CARD){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.setDisabled();
						}
						if(this.bodyTabPanel.hidden === true && this.module == 0){
							this.bodyTabPanel.show();
						}
					}else if(status == uft.jf.pageStatus.OP_EDIT){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							if(this.bodyGrids.editableColumns){
								var editableColumns = this.bodyGrids.editableColumns[bodyGrid.id];
								bodyGrid.reStoreDisabled(editableColumns);
							}else{
								bodyGrid.reStoreDisabled();
							}
						}
						if(this.bodyTabPanel.hidden === true && this.module == 0){
							this.bodyTabPanel.show();
						}
					}else if(status == uft.jf.pageStatus.OP_REVISE){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.enableRevise();
						}
						if(this.bodyTabPanel.hidden === true && this.module == 0){
							this.bodyTabPanel.show();
						}
					}else if(status == uft.jf.pageStatus.OP_REFADD_LIST){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.setDisabled();
						}
						if(this.bodyTabPanel.hidden === false && this.module == 0){
							this.bodyTabPanel.hide();
						}
					}else if(status == uft.jf.pageStatus.OP_REFADD_CARD){
						if(bodyGrid instanceof uft.extend.grid.EditorGrid){
							bodyGrid.reStoreDisabled();
						}
						if(this.bodyTabPanel.hidden === true && this.module == 0){
							this.bodyTabPanel.show();
						}
					}
				};
				this.statusMgr.addListener(this.bodyGrids[i],this.bodyGrids[i].onstatuschange, this);
			}
		}
	},
	/**
	 * 注册资产编码的多选择事件
	 * @param {} e
	 */
	registerAfterEditMultiColumn : function(e){
		var col = e.grid.colModel.columns[e.column];
		var editor = col.editor;
		var refWin = editor.getRefWindow();
		if(!refWin || refWin.hasListener('process')){//避免重复注册事件
			return;
		}
		refWin.on('process',function(win,values){
			var objs = win.returnValue;//[{pk:'',code:'',name:''},{}]
			this.afterBatchEditMultiColumn(col,objs,1);
		},this);
	},
	/**
	 * 选择多行时，其他行的默认值，有些时候可能会将基准行的数据也带到其他行上
	 */
	getBatchEditMultiColumnRowDefaultValue : function(grid){
		var bodyAssistToolbar = app.bodyAssistToolbar;
		var rdv = bodyAssistToolbar.getRowDefaultValues(grid.id);
		return rdv;
	},
	/**
	 * 
	 * @param {} col equipCode列对象
	 * @param {} objs 选中的值，数组，可以选择多个
	 * @param {} fromIndex 从哪个对象开始添加，如果是多选时带入的那么从第二个开始处理，因为第一个参照会默认处理了
	 */
	afterBatchEditMultiColumn : function(col,objs,fromIndex){
		if(!fromIndex){
			formIndex = 0;
		}
		var grid = this.getActiveBodyGrid();
		var currentRow = grid.getSelectedRow();
		var store = grid.getStore();
		var column = grid.colModel.findColumnIndex(col.dataIndex);//列号
		var editor = col.editor;
		var idcolname = editor.idcolname;
		if(!idcolname){
			idcolname = col.dataIndex;
		}
		
		var rdv = this.getBatchEditMultiColumnRowDefaultValue(grid);
		var newrdv = {};//拷贝一份数据，避免去修改到缓存
		for(var key in rdv){
			newrdv[key] = rdv[key];
		}
		if(objs.length < 1){
			return;
		}
		for(var i=fromIndex ; i < objs.length; i++){//这里从第二行开始
			var newRecord = {};
			  //新增行，通过values重新赋值
			newRecord[idcolname] = objs[i].pk;
			newRecord[editor.codeField] = objs[i].code;
			newRecord[editor.nameField] = objs[i].name;
			//这里必须有参数，否则code,name没法得到
			if(col.hasEditformula===true){
				var params = this.newAjaxParams();
				params[idcolname]= objs[i].pk;
				params['pkBilltemplet']= col.pkBilltemplet;
				params['pkBilltempletB']= col.pkBilltempletB;
				if(Utils.getRequestParam("ef1") == 1) {
				    var headerValues = this.getCardValues();
				    var key;
				    for(key in headerValues) {
				    	params["H."+key] = headerValues[key];
				    }
				}
				var values = Utils.doSyncRequest('execFormula.json',params,'POST');
				Ext.apply(newrdv,values);
			}
			Ext.apply(newrdv,newRecord);
			grid.addRow(newrdv);
			var newRow = store.getCount()-1;//指向当前增加的行
			var r = store.getAt(newRow);
			//对于多选的参照，设置idcolname对应的值，基本上就是pk，但是对于code或者name，如果objs的字段和参照字段是对应的，那么不需要设置，
			//否则会出现没有显示name或者code的情况，这时候可以通过公式去显示，比如默认使用name去对应到当前列，这样也是会有问题，比如参照设置在code字段呢？
			if(col.idcolname){
				r.set(col.idcolname,objs[i].pk);
			}
			
			if(typeof(afterEditBody) == 'function'){
				afterEditBody({ grid : grid,
					  record : r,
					  field : col.dataIndex,
					  originalValue : '',
					  value : newrdv[idcolname],
					  row : newRow,
					  column : column,
					  cancel : false 
				});
			}else{ 
				//若该列存在js脚本,则执行之
				if(col.script){
					eval(col.script);//执行模板【自定义1】中的脚本
				}
			}
		}
		this.fireEvent('afterbatcheditmulticolumn',col,objs,fromIndex);
	},
	/**
	 * 返回一张单据的默认值，以后【新增】，【增行】等都先从这里读取默认值
	 */
	loadDefaultValue : function(async){
		var values = this.cacheMgr.getEntity(uft.jf.Constants.PK_NEW);
		if(values){
			//已经存在缓存值了
			return;
		}
		var params = this.newAjaxParams();
		if(async){
			//异步请求
		    Utils.request({
		    	scope : this,
		    	params : params,
		    	method : 'POST',
		    	url : 'getDefaultValue.json',
		    	onSuccess : function(values){
		    		values = Ext.decode(values);
		    		if(values && values.data){
		    			this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW, values.data);
						//通常不需要缓存该内容，但是目前发现表体可能需要使用到append的内容
						this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW_EXTRA, values);
		    		}
		    	}
		    });
		}else{
			if(!Ext.isIE){
				//IE下显示不出效果
				Ext.getBody().mask(uft.jf.Constants.PROCESS_MSG);
			}
			//这里不能使用异步请求，其他地方需要等待该默认值已经加载
			var values = Utils.doSyncRequest('getDefaultValue.json',params,'POST');
			if(values && values.data){
				this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW, values.data);
				//通常不需要缓存该内容，但是目前发现表体可能需要使用到append的内容
				this.cacheMgr.addEntity(uft.jf.Constants.PK_NEW_EXTRA, values);
			}
			if(!Ext.isIE){
				Ext.getBody().unmask();
			}
		}
	},
	newAjaxParams : function(param){
		var p = uft.jf.ToftPanel.superclass.newAjaxParams.call(this);
		var c = this.context;
		p['templateID']=c.getTemplateID();
		p['tabCode']=c.getHeaderTabCode();
		p['headerTabCode'] = c.getHeaderTabCode();
		p['bodyTabCode'] = c.getBodyTabCode();
		p['funCode']=c.getFunCode();
		p['nodeKey']=c.getNodeKey();
		p['billType']=c.getBillType();
		p[this.getBillIdField()]=c.getBillId();
		return p;
	}
});
