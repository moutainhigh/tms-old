Ext.ns('uft.jf');
/**
 * 将该类定义为类似java中的抽象类，因为该toolbar不包含任何按钮。
 * 子类可以覆盖getBtnArray方法，返回一个包含按钮的toolbar
 * @class uft.jf.ToftToolbar
 * @extends uft.base.TopBar
 */
uft.jf.ToftToolbar = Ext.extend(uft.base.UIToolbar, {
	app : null,	//application对象
	bodyAssistToolbar : null, //body辅助工具栏对象
	intervalID : null,
	/**
	 * Ext生成唯一的主键ID，用来记录当前queryWindow的id
	 * @type 
	 */
	queryWindowId : null,
	constructor : function (config){ 
		Ext.apply(this, config);
		uft.jf.ToftToolbar.superclass.constructor.call(this);
		this.queryWindowId = Ext.id();
	},
	/**
	 * 返回按钮
	 */
	btn_back_handler : function(){
		if(this.fireEvent('beforeback',this) !== false){
			if(this.app.ulw){
				window.parent.switchToULW();
			}else{
				//保存完以后使用setRecordValues()方法刷新了
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
				this.app.statusMgr.updateStatus();
				this.fireEvent('back',this);
			}
			this.fireEvent('back',this);
		}
	},	
	btn_query_handler : function(){
		var win = Ext.getCmp(this.queryWindowId);
		if(!win){
			win=new uft.jf.QueryWindow({
				id : this.queryWindowId,
				funCode : this.app.context.getFunCode(),
				nodeKey : this.app.context.getNodeKey(),
				billType: this.app.context.getBillType(),
				grid : this.app.headerGrid
			});
			//执行查询后的触发事件
			//切换到列表状态
			//查询按钮只会在列表下可见，故不需要切换了
			win.on('query',function(){
				//清空子表的数据
				if(this.app.hasBodyGrid()){
					this.app.clearBodyGrids();
				}
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
				this.app.statusMgr.updateStatus();
			},this);
			win.on('show',function(){
				this.setDisabled(); //当打开查询框时，禁用按钮
			},this);
			win.on('hide',function(){
				this.reStoreDisabled();//隐藏查询框时，还原按钮
			},this);
		}
		win.show();
	},
	/**
	 * 增加按钮操作
	 * config.useTransitionStatus 是否使用过度状态，在切换成OP_ADD时，默认会先转成OP_NOTEDIT_CARD
	 * 使用中转状态是为了让用户在视觉上觉得更快
	 * @Override
	 */
	btn_add_handler : function(config){
		var params = {};
		if(this.app.leftTree){
			if(!this.app.leftTree.getSelectedNode()){
				uft.Utils.showWarnMsg('请先选中左边的树节点！');
				return;
			}
		}
		if(this.fireEvent('beforeadd',this) !==false){
			//清空表体的数据
			if(this.app.hasBodyGrid()){
				this.app.clearBodyGrids();
			}
			
			if(config && config.useTransitionStatus !== false){//是否使用中转状态
				if(this.app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_NOTEDIT_CARD){
					//这是一个过渡状态，让效果看起来先切换到卡片也
					this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
					this.app.statusMgr.updateStatus();	
				}
			}
			
			//检测缓存是否存在
			if(!this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW)){
				//加载默认值
				//yaojiie 2016 5 16 新增是为啥要加载缓存？？？？？？
				//this.app.loadDefaultValue();
			}
			var appBufferData = this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW);
			if(this.app.leftTree){
				//加入树节点的值
				appBufferData.HEADER[this.app.getTreePkField()] = this.app.leftTree.getSelectedNode().id;
			}
			this.app.setCardValues(appBufferData.HEADER);
			//设置页面状态
			if(typeof(this.app.getBillStatusField) == 'function'){
	    		this.app.statusMgr.setBizStatus(appBufferData.HEADER[this.app.getBillStatusField()]);
	    	}
	    	//上面已经执行完切换到卡片页，这里不需要再执行afterCallback的操作
	    	if(config && config.useTransitionStatus !== false){
		    	this.app.statusMgr.removeBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
				this.app.statusMgr.removeAfterUpdateCallback(this.app.setHeaderHeight,this.app);
	    	}
			this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_ADD);
			this.app.statusMgr.updateStatus();
			if(config && config.useTransitionStatus !== false){
				this.app.statusMgr.addBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
				this.app.statusMgr.addAfterUpdateCallback(this.app.setHeaderHeight,this.app);
			}
			
			//这里将values也作为一个参数，有些需要使用values进行判断，如在values中设置了一些标识性的数据
			//但是通常只需要处理data数据
			var values = this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW_EXTRA);
			this.fireEvent('add',this,appBufferData.HEADER,values);
		}
	},
	/**
	 * 处理新增后返回的表体数据
	 * @private
	 * @param {} bodyData
	 */
	processBodyRowAdd : function(bodyData){
		//处理表体的新增行
		if(this.bodyAssistToolbar){
			if(bodyData){
				var bodyGrids = this.app.getBodyGrids();
				for(var key in bodyData){
					if(this.app.showBodyTabHeader === true){
						//对于多页签
						var index = 0;
						for(;index<bodyGrids.length;index++){
							if(bodyGrids[index].id == key){
								break;
							}
						}
						this.app.getBodyTabPanel().setActiveTab(index);
					}
					if(bodyData[key] && Ext.isArray(bodyData[key])){
						for(var i=0;i<bodyData[key].length;i++){
							//这里更加规范的做法是对每个appBufferData.BODY的纪录进行增行,但是通常每行记录的默认值都是相同的,所以可以这么处理
							//而且这么处理可以兼容子类继承了btn_row_add_handler的情况,否则需要对btn_row_add_handler函数需要做特殊处理
							this.bodyAssistToolbar.btn_row_add_handler();
						}
					}
				}
			}
		}
	},
	/**
	 * 修改按钮操作
	 * @param billId
	 * 			可能是被其他方法调用的，此时可能会传入billId，否则默认参数是event对象
	 * @Override
	 */
	btn_edit_handler : function() {
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		var record = null;
		if(records){
			if(records.length > 1){
				uft.Utils.showWarnMsg('只能选择一条记录进行修改！');
				return false;
			}
			record = records[0];
			this.app.currentBillId = record.data[this.app.getHeaderPkField()];
			this.app.currentTs = record.data['ts'];
		}
		if(this.app.currentBillId){
			if(!record){
				//如果是直接传入billId，此时的record对象为空，构造一个,以免监听事件时需要使用到record对象
				record = new Ext.data.Record();
				record.set(this.app.getHeaderPkField(),this.app.currentBillId);
			}
			if(this.fireEvent('beforeedit',this,record) !== false){
				//先切换到卡片显示，再仔细设置数据的动作，这考虑到有些表格可能还没有渲染，如果直接增加数据会报js错误
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_EDIT);
				this.app.statusMgr.updateStatus();
				var appBufferData = this.app.cacheMgr.getEntity(this.app.currentBillId,this.app.currentTs);
				if(appBufferData){
					//存在缓存
		    		this.app.setAppValues(appBufferData);
		    		//XXX 如果表体使用分页显示，那么后台返回的整个单据VO不会包括该表体，这里需要单独对该表体进行查询操作
	    			this.app.loadBodyGrids(this.app.currentBillId,true);
					this.fireEvent('edit',this,appBufferData);
				}else{
					this.app.loadAppValues(this.app.currentBillId,function(values){
			    		if(values && values.data){
			    			this.app.setAppValues(values.data,{saveToCache:true});
							this.fireEvent('edit',this,values.data,values);
			    		}
			    	},this);					
				}
			}
		}else{
			uft.Utils.showWarnMsg('请选择一条记录进行修改！');
				return false;
		}
	},
	/**
	 * 删除按钮操作
	 * @Override
	 */
	btn_del_handler : function(btn,event) {
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		var ids = [];
		if(records) {
			ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		}else{
			if(this.app.currentBillId){
				records = [];
				ids.push(this.app.currentBillId);
				//如果是直接传入billId，此时的record对象为空，构造一个,以免监听事件时需要使用到record对象
				var record = new Ext.data.Record();
				record.set(this.app.getHeaderPkField(),this.app.currentBillId);
				records.push(record);
			}
		}
		if(ids.length > 0){
			//删除前事件
			var msg = (btn && btn.confirmMsg)||'确认要删除所选记录吗？';
			if(this.fireEvent('beforedel',this,records) !== false){
				Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText(msg), function(btn) {
					var params = this.app.newAjaxParams();
					params[this.app.getBillIdField()]=ids;
					if (btn == 'yes') {
						uft.Utils.doAjax({
					    	scope : this,
					    	method : 'GET',
					    	url : 'delete.json',
					    	actionType : '删除',
					    	params : params,
					    	success : function(values){
					    		//更新当前的currentBillId,保证currentBillId对应的是当前的单据
					    		this.app.currentBillId = null; //当前的单据已被删除
					    		//动态更新本地数据集
					    		var grid = this.app.headerGrid;
					    		if(grid){
					    			for(var i=0;i<ids.length;i++){
					    				var _id=ids[i];
								        if(grid.getStore().pks){//pks可能不存在，使用reload可以清空pks
							        		//删除pk值
							        		grid.getStore().pks.remove(_id);//remove方法在ext中被定义了。
								        }
						        		var record = null;//待删除的记录
						        		for(var j=0;j<grid.getStore().getCount();j++){
						        			var _record = grid.getStore().getAt(j);
						        			if(_id==_record.get(grid.getPkFieldName())){
						        				record = _record;
						        				break;
						        			}
						        		}
								        grid.getStore().remove(record);//删除记录
								        grid.getStore().totalLength=grid.getStore().getTotalCount()-1;							        
								        //更新底部工具栏的显示信息
								        grid.updateTbarInfo();	
								        //删除卡片中缓存的值
								        this.app.cacheMgr.removeEntity(_id);
					    			}
					    		}

					    		this.app.resetCardValues();//重置卡片值
	
					    		if(this.app.hasBodyGrid()){ //清空bodyGrids的记录
					    			this.app.clearBodyGrids();
					    		}
					    		this.app.statusMgr.setBizStatus(null);
								this.app.statusMgr.updateStatus();
					    		if(values.append){
					    			//存在data数据的时候表示删除出现异常，显示异常信息
						    		uft.Utils.showWarnMsg(values.append);
					    		}
					    		this.fireEvent('del',this,records,values);//删除后事件
					    	}
					    });
					}
				},this);
			}
		}else{
			uft.Utils.showWarnMsg('请先选择一行记录！');
			return;
		}
	},	
	/**
	 * 保存按钮操作，与暂存按钮使用同一个脚本
	 * @Override
	 */
	btn_save_handler : function() {
		this.saveAction();
	},
	/**
	 * 取消按钮操作
	 * @Override
	 */
	btn_can_handler : function() {
		var app = this.app, sm = app.statusMgr;
		var record = uft.Utils.getSelectedRecord(app.headerGrid,false);
		if(record){
			app.currentBillId = record.data[app.getHeaderPkField()];
			app.currentTs = record.data['ts'];
		}else{
			//如果是直接传入billId，此时的record对象为空，构造一个,以免监听事件时需要使用到record对象
			record = new Ext.data.Record();
			record.set(app.getHeaderPkField(),app.currentBillId);				
		}
		if(app.currentBillId){
			if(this.fireEvent('beforecan',this,record) !== false){
				var appBufferData = app.cacheMgr.getEntity(app.currentBillId,app.currentTs);
				if(appBufferData){
					app.setAppValues(appBufferData);//恢复表单值
					//XXX 如果表体使用分页显示，那么后台返回的整个单据VO不会包括该表体，这里需要单独对该表体进行查询操作
	    			app.loadBodyGrids(app.currentBillId,true);
					sm.removeBeforeUpdateCallback(app.backupHistoryHeight,app);
					sm.removeAfterUpdateCallback(app.setHeaderHeight,app);
			        if(typeof(app.getBillStatusField)=='function'){
						sm.setBizStatus(appBufferData.HEADER[app.getBillStatusField()]);
					}
					sm.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
					sm.updateStatus();
					sm.addBeforeUpdateCallback(app.backupHistoryHeight,app);
					sm.addAfterUpdateCallback(app.setHeaderHeight,app);
					this.fireEvent('can',this,appBufferData);
				}else{
					app.loadAppValues(app.currentBillId,function(values){
			    		if(values && values.data){
			    			app.setAppValues(values.data,{saveToCache:true});
			    			
			    			sm.removeBeforeUpdateCallback(app.backupHistoryHeight,app);
							sm.removeAfterUpdateCallback(app.setHeaderHeight,app);
			    			if(typeof(app.getBillStatusField) == 'function'){
				    			sm.setBizStatus(values.data.HEADER[app.getBillStatusField()]);
				    		}
				    		sm.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
							sm.updateStatus();
							sm.addBeforeUpdateCallback(app.backupHistoryHeight,app);
							sm.addAfterUpdateCallback(app.setHeaderHeight,app);
			    			this.fireEvent('can',this,values.data,values);
			    		}
			    	},this);
				}
			}
		}else{
			if(this.fireEvent('beforecan',this,record) !== false){
				//没有选择记录，清空表头、表体、表尾数据
				app.resetCardValues();
				if(app.hasBodyGrid()){
					app.clearBodyGrids();
				}
				sm.removeBeforeUpdateCallback(app.backupHistoryHeight,app);
				sm.removeAfterUpdateCallback(app.setHeaderHeight,app);
				sm.setBizStatus(null);
				sm.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
				sm.updateStatus();
				sm.addBeforeUpdateCallback(app.backupHistoryHeight,app);
				sm.addAfterUpdateCallback(app.setHeaderHeight,app);
				this.fireEvent('can',this,appBufferData);
			}	
		}
	},
	/**
	 * 复制按钮，将单据复制后直接切换到新增状态
	 * @param billId
	 * 			可能是其他方法调用的，此时可能会传入billId，否则是event对象
	 */
	btn_copy_handler : function(){
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		var record = null;
		if(records){
			record = records[0];
			if(records.length > 1){
				uft.Utils.showWarnMsg('只能选择一条记录进行复制！');
				return false;
			}
			this.app.currentBillId = record.data[this.app.getHeaderPkField()];
		}
		if(this.app.currentBillId){
			if(!record){
				//如果是直接传入billId，此时的record对象为空，构造一个,以免监听事件时需要使用到record对象
				record = new Ext.data.Record();
				record.set(this.app.getHeaderPkField(),this.app.currentBillId);
			}
			if(this.fireEvent('beforecopy',this,record) !== false){
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_ADD);
				this.app.statusMgr.updateStatus();
				//这里不要使用缓存，一个是无法使用pk作为key，如果使用缓存，可能读取到单据的缓存数据，而复制可能需要对数据进行处理
				var params = this.app.newAjaxParams();
				params[this.app.getBillIdField()]=this.app.currentBillId;
				uft.Utils.doAjax({
			    	scope : this,
			    	method : 'GET',
			    	url : 'copy.json',
			    	isTip : false,
			    	params : params,
			    	success : function(values){
			    		if(values && values.data){
				    		//设置表头、表尾数据
				    		this.app.setAppValues(values.data);
							if(typeof(this.app.getBillStatusField) == 'function'){
				    			this.app.statusMgr.setBizStatus(uft.jf.bizStatus.FREE);
				    		}
//							this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_ADD);
//							this.app.statusMgr.updateStatus();							
							this.fireEvent('copy',this,values.data,values);
			    		}
			    	}
			    });
			}
		}
	},
	/**
	 * 刷新操作
	 * 1、列表显示，只刷新表头的数据，表体的数据清空。
	 * 2、卡片显示，卡片数据刷新，同时刷新表体数据。
	 */
	btn_ref_handler : function(){
		if(this.fireEvent('beforeref',this) !== false){
			if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_NOTEDIT_LIST 
				|| this.app.statusMgr.getCurrentPageStatus()==uft.jf.pageStatus.OP_INIT){
				//列表显示
				if(this.app.getHeaderGrid()){
					//从第一条开始查询，与翻页栏的刷新一致 
					var o = {};
					o[this.app.getHeaderGrid().getStore().paramNames.start] = 0;
					o[this.app.getHeaderGrid().getStore().paramNames.limit] = this.app.getHeaderGrid().pageSize;
					this.app.getHeaderGrid().getStore().reload({params:o});
				}
				//清空表头数据
				this.app.resetCardValues();
				//清空表体的数据
				if(this.app.hasBodyGrid()){
					this.app.clearBodyGrids();
				}
				
		    	this.app.statusMgr.removeBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
				this.app.statusMgr.removeAfterUpdateCallback(this.app.setHeaderHeight,this.app);
				this.app.statusMgr.setBizStatus(null);
				this.app.statusMgr.updateStatus();
				this.app.statusMgr.addBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
				this.app.statusMgr.addAfterUpdateCallback(this.app.setHeaderHeight,this.app);
				this.fireEvent('ref',this);
			}else if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_NOTEDIT_CARD){
				if(!this.app.currentBillId){
					this.app.currentBillId = uft.Utils.getSelectedRecordId(this.app.headerGrid,this.app.headerPkField);
				}
				if(this.app.currentBillId){
					this.app.loadAppValues(this.app.currentBillId,function(values){
			    		if(values && values.data){
			    			this.app.setAppValues(values.data,{saveToCache:true});
			    			//这里是刷新数据而已，如果没有更改单据状态，根本不需要updateStatus
							if(typeof(this.app.getBillStatusField) == 'function'){
						    	this.app.statusMgr.removeBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
								this.app.statusMgr.removeAfterUpdateCallback(this.app.setHeaderHeight,this.app);
			    				this.app.statusMgr.setBizStatus(values.data.HEADER[this.app.getBillStatusField()]);
			    				this.app.statusMgr.updateStatus();
								this.app.statusMgr.addBeforeUpdateCallback(this.app.backupHistoryHeight,this.app);
								this.app.statusMgr.addAfterUpdateCallback(this.app.setHeaderHeight,this.app);
			    			}
			    		}
			    		this.fireEvent('ref',this);
			    	},this);
				}
			}
		}
	},
	/**
	 * 列表显示按钮操作
	 * @Override
	 */
	btn_list_handler : function(){
		if(this.fireEvent('beforelist',this) !== false){
			//保存完以后使用setRecordValues()方法刷新了
			this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_LIST);
			this.app.statusMgr.updateStatus();
			this.fireEvent('list',this);
		}
	},
	/**
	 * 卡片显示按钮操作
	 * @Override
	 */
	btn_card_handler : function (){
		if(this.fireEvent('beforecard',this) !== false){
			//当没有数据时，不抓取数据，当还是切换状态
			this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
			this.app.statusMgr.updateStatus();			
			var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
			var bizStatus = null;
			var toDoBillId = null,toDoTs = null;
			if(record) {
				toDoBillId = record.data[this.app.getHeaderPkField()];
				toDoTs = record.data['ts'];
				if(typeof(this.app.getBillStatusField) == 'function'){
					bizStatus = record.data[this.app.getBillStatusField()];
				}
			}else if(this.app.currentBillId){
				toDoBillId = this.app.currentBillId;
			}
			if(toDoBillId){
				this.app.currentBillId = toDoBillId;
				var appBufferData = this.app.cacheMgr.getEntity(toDoBillId,toDoTs);
				if(appBufferData){
					if(typeof(this.app.getBillStatusField) == 'function'){
						bizStatus = appBufferData.HEADER[this.app.getBillStatusField()];
						this.app.statusMgr.setBizStatus(bizStatus);
						this.app.statusMgr.updateStatus();	
					}
					this.app.setAppValues(appBufferData);
					//XXX 如果表体使用分页显示，那么后台返回的整个单据VO不会包括该表体，这里需要单独对该表体进行查询操作
	    			this.app.loadBodyGrids(this.app.currentBillId,true);
				}else{
					this.app.loadAppValues(toDoBillId,function(values){
			    		if(values && values.data){
			    			if(typeof(this.app.getBillStatusField) == 'function'){
								bizStatus = values.data.HEADER[this.app.getBillStatusField()]
								this.app.statusMgr.setBizStatus(bizStatus);
								this.app.statusMgr.updateStatus();	
							}
			    			this.app.setAppValues(values.data,{saveToCache:true});
			    		}
			    	},this);
				}
			}
			this.fireEvent('card',this,record);
		}
	},
	/**
	 * 打印按钮操作,这里只打印一条单据
	 * @Override
	 */
	btn_print_handler : function(){
		var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
		if(record) {
			var id = record.get(this.app.headerPkField);
			var params = this.app.newAjaxParams();
			//支持批量打印
			params[this.app.getBillIdField()]=id;
			var url="print.do";
			if(params){
		    	if(url.indexOf('?') == -1){
		    		url +='?';
		    	}
		    	var index=0;
		    	for(key in params){
		    		if(index > 0){
		    			url+= '&';
		    		}
		    		url += key + "="+params[key];
		    		index++;
				}
		    }
			if(this.fireEvent('beforeprint',this,record,params)===false){
				return false;
			}
			window.open(url);
		}else{
			uft.Utils.showWarnMsg('请先选择记录进行打印！');
			return false;
		}
	},
	/**
	 * config :
	 * 			viewOnly,true/false是否只读
	 * @return {Boolean}
	 */
	btn_attach_handler : function(config){
		var pk_bill,billtype;
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		if(records) {
			if(records.length > 1){
				uft.Utils.showWarnMsg('只能选择一条记录！');
				return false;
			}
			pk_bill = records[0].get(this.app.headerPkField);
		}else{
			if(this.app.currentBillId){
				//存在只有一条记录的情况，并且没有列表页，只有卡片页
				pk_bill = this.app.currentBillId;
			}else{
				uft.Utils.showWarnMsg('请先选择记录！');
				return false;
			}
		}
		var billtype = this.app.context.getBillType();
		//alert(billtype);
		if(!billtype){//对于档案类型，billtype直接使用funCode就好了
			billtype = this.app.context.getFunCode();
		}
		var cfg = {
			app : this.app,
			pk_bill : pk_bill,
			billtype : billtype,
			funCode : this.app.context.getFunCode()
		};
		if(!(config instanceof uft.extend.Button)){
			Ext.apply(cfg,config);
		}
		new uft.jf.FileManager(cfg).show();
	},	
	
	btn_sms_handler : function(config){
		var billids="", billnos="", fun_code;
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.getHeaderPkField());
		var vbillnos = uft.Utils.getSelectedRecordIds(this.app.headerGrid,'vbillno');
		if(!vbillnos || 'undefined'== typeof(value) || value== undefined ){
			var codeField = Utils.doSyncRequest('getCodeField.json','POST');
			//如果vbillnos不存在说明根据vbillno获取不到对应的单号，这时候使用code再获取一次。
			var vbillnos = uft.Utils.getSelectedRecordIds(this.app.headerGrid,codeField);
		}
		
		var fun_code = this.app.context.getFunCode();
		if(!records) {
			uft.Utils.showWarnMsg('请选择记录！');
		}else{
			if(ids && ids.length > 0){
				for(var i=0;i<ids.length;i++){
					billids +=(ids[i] + ',');
				}
				billids = billids.substring(0,billids.length-1);
			}
			if(vbillnos && vbillnos.length > 0){
				for(var i=0;i<vbillnos.length;i++){
					billnos +=(vbillnos[i] + ',');
				}
				billnos = billnos.substring(0,billnos.length-1);
			}
			if(this.app.currentBillId){
				//存在只有一条记录的情况，并且没有列表页，只有卡片页
				billids = this.app.currentBillId;
			}
		}
		var cfg = {
			app : this.app,
			billids : billids,
			billnos : billnos,
			fun_code : fun_code
		};
		if(!(config instanceof uft.extend.Button)){
			Ext.apply(cfg,config);
		}
		new uft.jf.SmsSender(cfg).show();
	},
	/**
	 * 保存动作，不直接使用handler，因为需要一些参数，子类可以根据实际情况加入参数
	 * @param {} config
	 */
	saveAction : function(config){
		if(!config){
			config={};
		}
		//可编辑表格结束编辑，否则当编辑表格处于编辑状态，数据会没有提交。
		if(this.app.hasBodyGrid()){
			var grid=this.app.getActiveBodyGrid();
			if(grid instanceof Ext.grid.EditorGridPanel){
				if(this.intervalID!=null){
					clearInterval(this.intervalID);
				}
				this.intervalID = setUftInterval(this.doSaveAction,this,300,config); //注意这里第二个参数this非常关键,它是执行函数的scope
			}			
		}else{
			this.doSaveAction(config);
		}
	},
	doSaveAction : function(config){
		var continueFlag = true;
		if(this.intervalID){
			var grid=this.app.getActiveBodyGrid();
			if(grid.afterEditComplete !== false){
				grid.stopEditing();
				clearInterval(this.intervalID);
			}else{
				continueFlag = false;
			}
		}
		if(continueFlag){
			var errors = this.app.headerCard.getErrors();
			if(errors.length==0) {
				var params=this.app.newAjaxParams();
				var appPostData = this.getAppParams(config);
				if(appPostData !== false){
					params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
					if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REVISE){
						params['reviseflag'] = true;
					}
					if(this.fireEvent('beforesave',this,params) !== false){//抛出beforesave事件，比如报销单在传入后台前进行金额校验
						uft.Utils.doAjax({
					    	scope : this,
					    	url : config.saveUrl||'save.json',
					    	actionType : '保存',
					    	params : params,
					    	success : function(values){
								this.doAfterSave(values);
					    	}
					    });
					}
				}
			}else{
				uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				return;
			}
		}
	},
	/**
	 * 返回保存之前的数据,
	 * config参数是一个对象，包括
	 * config.firstBodyGridNotNull 第一个表体是否可以为空
	 * config.bodyGridValidate	是否对表体进行校验
	 * config.bodyGridOnlyModify 是否只返回修改过的记录
	 */
	getAppParams : function(config){
		var appPostData =  {};
		var values = this.app.headerCard.getForm().getFieldValues(false);
		if(this.app.footCard){
			var errors = this.app.footCard.getErrors();
			if(errors.length==0){
				//用户自定义校验
				Ext.apply(values, this.app.footCard.getForm().getFieldValues(false));
			}else{
				uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				return false;
			}
		}
		//日期的格式化已经在getFieldValues中已做了默认处理
		//加入主表数据
		appPostData[uft.jf.Constants.HEADER] = values;
		if(this.app.hasBodyGrid()) {
			//是否校验第一个表体
			if(config.firstBodyGridNotNull === true){
				var firstBodyGrid=this.app.getBodyGrids()[0];
				if(firstBodyGrid && (!this.app.hideBodyGrid || firstBodyGrid.allColumnHide !== true)){//如果第一个页签的所有字段都是隐藏的，那么不再校验
					if(typeof(firstBodyGrid.getAvailableCount) == "function"){
						if(firstBodyGrid.getAvailableCount()==0){
							uft.Utils.showWarnMsg('表体不能没有记录！');
							return false;
						}
					}
				}					
			}

			var bodyGridData={};
			var tabCodes=this.app.context.getBodyTabCode().split(',');//bodyGrids与tabCodes的长度肯定相同
			var bodyGrids = this.app.getBodyGrids();
			for(var i = 0; i < bodyGrids.length; i++) {
				if(typeof(bodyGrids[i].getModifyValue) == "function"){
					//是否使用第三方插件进行验证每个单元格
					var bodyGridValidate = true;
					if(Ext.isArray(config.bodyGridValidate)){
						bodyGridValidate = config.bodyGridValidate[i];
					}else{
						bodyGridValidate = config.bodyGridValidate;
					}
					if(bodyGridValidate !== false){
						if(!bodyGrids[i].isValid()) {//这里使用第三方插件进行验证
							errors = bodyGrids[i].getAllErrors();
							if(errors.length > 0){
								uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
								return false;
							}
						}
					}
					var bodyGridOnlyModify = true;
					if(Ext.isArray(config.bodyGridOnlyModify)){
						//如果是个数组,多页签的情况
						bodyGridOnlyModify = config.bodyGridOnlyModify[i];
					}else{
						bodyGridOnlyModify = config.bodyGridOnlyModify;
					}
					
					if(bodyGridOnlyModify !== false){
						bodyGridData[tabCodes[i]] = bodyGrids[i].getModifyValue();
					}else{
						bodyGridData[tabCodes[i]] = bodyGrids[i].getAllRecordValue();
					}
				}
			}
			//加入子表数据
			appPostData[uft.jf.Constants.BODY]=bodyGridData;
		}
		return appPostData;
	},
	/**
	 * 保存成功后的执行脚本，这里单独出来是考虑到子类可能需要覆盖
	 * @param {} values
	 */
	doAfterSave : function(values){
		if(values && values.data){
			//从缓存中移出
			this.app.cacheMgr.removeEntity(values.data.HEADER[this.app.headerPkField]);
			//清楚各个子表的缓存
			if(this.app.hasBodyGrid()){
				var bGrids = this.app.getBodyGrids();
				for(var i=0;i<bGrids.length;i++){
					this.app.cacheMgr.removeEntity(bGrids[i].id+values.data.HEADER[this.app.headerPkField]);
				}
			}
			//更新当前的currentBillId，保证currentBillId对应的是当前的数据,一般也只有新增状态下需要修改该billId，但是其他状态下重新赋值也没问题
			var headerPk = values.data.HEADER[this.app.headerPkField];
			this.app.currentBillId = headerPk;
			this.app.setCardValues(values.data.HEADER);//设置表头和表尾数据
			var grid=this.app.headerGrid;
			if(this.app.statusMgr.getCurrentPageStatus()==uft.jf.pageStatus.OP_ADD){
				//当新增记录，保存时，设置默认的业务状态
				if(grid){
					var recordType = grid.getStore().recordType;
			        var record = new recordType();
			        this.app.setHeaderRecordValues(record,values.data.HEADER);
			        var index = grid.getStore().getCount();
			        grid.getStore().insert(index, record);
			        this.app.selectHeaderGridRowOnBO(index);//选择行,但是不触发onRowselect事件
			        
			        //更新底部工具栏的显示信息
			        grid.getStore().totalLength=grid.getStore().getTotalCount()+1;
			        if(grid.getStore().pks){//pks可能不存在，使用reload可以清空pks
			        	grid.getStore().pks.push(record.get(grid.getPkFieldName()));
			        }
			        grid.updateTbarInfo();
				}
				
				this.processBodyAfterSave(headerPk);
			}else{
				if(grid){
					var record=uft.Utils.getSelectedRecord(grid);
					this.app.setHeaderRecordValues(record,values.data.HEADER);						
				}
				
				this.processBodyAfterSave(headerPk);
			}

			//当新增记录，保存时，设置默认的业务状态
	        //不一定是自由态，可能是保存直接提交
	        if(typeof(this.app.getBillStatusField)=='function'){
    			this.app.statusMgr.setBizStatus(values.data.HEADER[this.app.getBillStatusField()]);
    		}
    		if(values.append){
    			uft.Utils.showWarnMsg(values.append);
    		}	
		}
		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
		this.app.statusMgr.updateStatus();
		this.fireEvent('save',this,values.data,values);
	},
	//保存完成后对表体的处理，默认重新加载表体的数据
	processBodyAfterSave : function(headerPk){
		if(this.app.hasBodyGrid()) {
			//加载所有表体的数据
			this.app.loadBodyGrids(headerPk);
		}
	},
	//导出
	btn_export_handler : function(){
		var app = this.app;
		var context = app.context;
		var config = {};
		config.funCode = context.getFunCode();
		config.nodeKey = context.getNodeKey();
		config.templateID = context.getTemplateID();
		config.tabCode = context.getHeaderTabCode();
		config.isBody = false;
		
		//得到查询框的查询参数
		if(app.topQueryForm){
			var params = app.topQueryForm.getFormParams();
			//存在查询参数
			config.PUB_PARAMS = params;//这里不需要Ext.encode了，doExport的encodeURI已经会处理
		}
		var headerGrid = app.getHeaderGrid();
		headerGrid.doExport(config);
	},
	/**
	 * 导出表体的数据
	 * @param tabcode，要导出的表体的tabcode
	 */
	exportBodyGrid : function(tabcode){
		var config = {};
		var context = this.app.context;
		config.funCode = context.getFunCode();
		config.nodeKey = context.getNodeKey();
		config.templateID = context.getTemplateID();
		config.tabCode = tabcode||context.getBodyTabCode();
		config.isBody = true;
		var headerGrid = this.app.getHeaderGrid();
		config[this.app.getHeaderPkField()] = uft.Utils.getSelectedRecordId(headerGrid,this.app.getHeaderPkField());
		var url="exportExcel.do";
		var index = 0;
		for(var key in config){
			if(index == 0){
				url += "?";
			}else{
				url += "&";
			}
			url += key + "=" + config[key];
			index++;
		}
		window.open(encodeURI(encodeURI(url)));		
	},
	btn_import_handler : function(){
		var win = Ext.getCmp('importWin');
		if(!win){
			win = new uft.extend.UploadWindow({
				id:'importWin',
				params : this.app.newAjaxParams(),
				isLog : true,
				fun_code : this.app.context.getFunCode(),
				permitted_extensions : ['xls','xlsx']
			});
		}
		win.show();		
	},
	/**
	 * 上一张单据,从待办事项中读取上一张单据
	 */
	btn_prev_handler : function(){
		this._prevOrNextAction({url:'prev.json',msg:'已经是第一条了！'});
	},
	/**
	 * 下一张单据，从待办事项中读取下一张单据
	 */
	btn_next_handler : function(){
		this._prevOrNextAction({url:'next.json',msg:'已经是最后一条了！'});
	},
	_prevOrNextAction : function(config){
		var params=this.app.newAjaxParams();
		var billId = this.app.currentBillId;
		if(!billId){
			var record = uft.Utils.getSelectedRecord(this.app.headerGrid);
			if(!record){
				uft.Utils.showWarnMsg('请选择一条记录！');
				return;
			}else{
				billId = record.get(this.app.headerPkField);
			}
		}
		params[this.app.getBillIdField()] = billId;
		if(this.app.headerGrid){
			var store = this.app.headerGrid.getStore();
			params[uft.jf.Constants.PUB_PARAMS] = store.baseParams.PUB_PARAMS;
			var tree = this.app.leftTree;
			if(tree && tree.getSelectedNode()){
				//存在左边树，也是一个查询条件
				params[this.app.getTreePkField()] = tree.getSelectedNode().id;
			}
			//处理排序
			var sortInfo = store.sortInfo;
			if(sortInfo){
				params.sort = sortInfo.field;
				params.dir = sortInfo.direction;
			}
		}
		uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	isTip : false,
	    	url : config.url,
	    	success : function(values){
	    		if(values.data){
	    			var billId = values.data.HEADER[this.app.getHeaderPkField()];
	    			if(this.billContext){
	    				this.app.billContext.setBillId(billId);
	    			}
	    			this.app.currentBillId = billId;
		    		this.app.setAppValues(values.data);
		    		//列表选中这条记录
		    		var grid = this.app.headerGrid;
		    		if(grid){
		    			var ds = grid.getStore();
		    			for(var i=0;i<ds.getCount();i++){
		    				var r = ds.getAt(i);
		    				if(r.get(this.app.getHeaderPkField()) == billId){
		    					this.app.selectHeaderGridRowOnBO(i);
		    					break;
		    				}
		    			}
		    		}		    		
		    		if(typeof(this.app.getBillStatusField) == 'function'){
		    			this.app.statusMgr.setBizStatus(values.data.HEADER[this.app.getBillStatusField()]);
		    		}
	    			this.app.statusMgr.updateStatus();
	    		}else{
	    			uft.extend.tip.Tip.msg('info', config.msg);
	    			return;
	    		}
	    	}
	    });	
	}
});