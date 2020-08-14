Ext.ns('uft.jf');
/**
 * 将该类定义为类似java中的抽象类，因为该toolbar不包含任何按钮。
 * 子类可以覆盖getBtnArray方法，返回一个包含按钮的toolbar
 * @class uft.jf.BillToolbar
 * @extends uft.base.ToftToolbar
 */
uft.jf.BillToolbar = function(config){
	Ext.apply(this, config);
	this.btn_confirm = {
		variable : 'btn_confirm',
		text : '确认', 
		iconCls : 'btnYes',
		handler : this.btn_confirm_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
		enabledBizStatus : [uft.jf.bizStatus.NEW]
	};
	this.btn_unconfirm = {
		variable : 'btn_unconfirm',
		text : '反确认', 
		iconCls : 'btnCancel',
		handler : this.btn_unconfirm_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD],
		enabledBizStatus : [uft.jf.bizStatus.COMMIT]
	};
	this.btn_revise = {
		variable : 'btn_revise',
		text : '强制修订',
		iconCls : 'btnYes',
		handler : this.btn_revise_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
		,enabledBizStatus : [uft.jf.bizStatus.COMMIT]
	};	
	uft.jf.BillToolbar.superclass.constructor.call(this);
	//给默认按钮增加业务状态，toftToolbar中没有有审批的状态,故在这里定义
	this.btn_edit.enabledBizStatus=[uft.jf.bizStatus.NEW];
	this.btn_del.enabledBizStatus=[uft.jf.bizStatus.NEW];
	this.btn_print.enabledBizStatus='ALL';
	this.btn_copy.enabledBizStatus='ALL';
	this.btn_prev.enabledBizStatus='ALL';
	this.btn_next.enabledBizStatus='ALL';
};
Ext.extend(uft.jf.BillToolbar,uft.jf.ToftToolbar, {
	/**
	 * 提交时检查，返回true/false
	 */
	checkBeforeConfirm : function(showMsg){
		var app = this.app;
		if(!Ext.getCmp(app.headerPkField).getValue()){
			//卡片有值以后才需要进行校验,这里使用pk值进行校验即可
			return true;
		}
		if(app.headerCard){
			var errors = app.headerCard.getErrors();
			if(errors.length > 0){
				if(showMsg !== false){
					uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				}
				return false;
			}
		}
		if(app.footCard){
			var errors = app.footCard.getErrors();
			if(errors.length > 0){
				if(showMsg !== false){
					uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				}
				return false;
			}
		}
		var bodyGrids = app.getBodyGrids();
		if(bodyGrids){
			for(var i = 0; i < bodyGrids.length; i++) {
				//是否使用第三方插件进行验证每个单元格
				if(!bodyGrids[i].isValid()) {//这里使用第三方插件进行验证
					var errors = bodyGrids[i].getAllErrors();
					if(errors.length > 0){
						if(showMsg !== false){
							uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
						}
						return false;
					}
				}
			}
		}
		return true;
	},
	btn_confirm_handler : function(){
		//提交的时候需要检查哪些是必输项，需要做提示
		var bol = this.checkBeforeConfirm();
		if(!bol){//检测不通过
			return;
		}
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		
		//判断选中的记录状态是否一致
		if(records.length > 0){
			var statusfirst = records[0].data['vbillstatus'];
			for(var i =1 ; i < records.length;i++){
				var statusnext = records[i].data['vbillstatus'];
				if(statusfirst != statusnext){
					uft.Utils.showWarnMsg('选择的记录状态不一致，请确认!');
					return;
				}
			}
		}
					
		if(ids.length > 0){
			if(this.fireEvent('beforeconfirm',this,ids) !== false){
				var params=this.app.newAjaxParams();
				params[this.app.getBillIdField()]=ids;
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	isTip : true,
			    	method : 'GET',
			    	url : 'batchConfirm.json',
			    	success : function(values){
			    		this.app.setHeaderValues(records,values.datas);
			    		if(values.datas&&values.datas.length>0){
			    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
			    		}
			    		this.app.statusMgr.updateStatus();
			    		if(values.append){
			    			uft.Utils.showWarnMsg(values.append);
			    		}
			    		this.fireEvent('confirm',this,values.datas,values);
			    	}
			    });
			}
		}else{
			uft.Utils.showWarnMsg('请先选择记录！');
			return;
		}
	},
	//反确认
	btn_unconfirm_handler : function(){
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid,this.app.headerPkField);
		var ids = uft.Utils.getSelectedRecordIds(this.app.headerGrid,this.app.headerPkField);
		
		//判断选中的记录状态是否一致
		if(records.length > 0){
			var statusfirst = records[0].data['vbillstatus'];
			for(var i =1 ; i < records.length;i++){
				var statusnext = records[i].data['vbillstatus'];
				if(statusfirst != statusnext){
					uft.Utils.showWarnMsg('选择的记录状态不一致，请确认!');
					return;
				}
			}
		}
		
		if(ids.length > 0){
			//根据后台配置来决定是否弹出反确认详细信息的页面
			if(!this.simpleUnConfirm){
				new uft.cm.UnConfirm({app:this.app,record:records}).show();
			}else{
				if(this.fireEvent('beforeunconfirm',this,ids) !== false){
					var params=this.app.newAjaxParams();
					params[this.app.getBillIdField()]=ids;
				    uft.Utils.doAjax({
				    	scope : this,
				    	params : params,
				    	isTip : true,
				    	method : 'GET',
				    	url : 'batchUnconfirm.json',
				    	success : function(values){
				    		this.app.setHeaderValues(records,values.datas);
				    		if(values.datas&&values.datas.length>0){
				    			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
				    		}
				    		this.app.statusMgr.updateStatus();
				    		if(values.append){
				    			uft.Utils.showWarnMsg(values.append);
				    		}
				    		this.fireEvent('unconfirm',this,values.datas,values);
				    	}
				    });
				}
			}
		}else{
			uft.Utils.showWarnMsg('请先选择记录！');
			return;
		}
	},
	/**
	 * 修订按钮操作
	 * 增加了修订状态，只有可修订的域才能修改
	 */
	btn_revise_handler : function() {
		var records = uft.Utils.getSelectedRecords(this.app.headerGrid);
		var record = null;
		if(records){
			if(records.length > 1){
				uft.Utils.showWarnMsg('只能选择一条记录进行修订！');
				return false;
			}
			record = records[0];			
		}
		if(this.fireEvent('beforerevise',this,record) !== false){
			this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REVISE);
			this.app.statusMgr.updateStatus();
			var billId = record.get(this.app.headerPkField);
			var appBufferData = this.app.cacheMgr.getEntity(billId);
			if(appBufferData){
				//缓存已存在
				this.app.setAppValues(appBufferData);
				this.fireEvent('revise',this,appBufferData);
			}else{
				this.app.loadAppValues(billId,function(values){
		    		if(values && values.data){
		    			this.app.setAppValues(values.data,{saveToCache:true});
						this.fireEvent('revise',this,values.data,values);
		    		}
		    	},this);
			}
		}			
	},	
	btn_edit_handler : function(){
		if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_LIST){
			//若是参照add的列表状态，重新定义
			var records = uft.Utils.getSelectedRecords(this.app.getHeaderGrid());
			if(records){
				if(records.length > 1){
					uft.Utils.showWarnMsg('只能选择一条记录进行操作！');
					return false;
				}
				var record = records[0];
				var index = this.app.getHeaderGrid().getStore().indexOf(record);
				var refBufferData = this.app.getRefBufferData();
				if(refBufferData && refBufferData[index]){
					this.app.setCardValues(refBufferData[index].HEADER,false);
					//表体的值在选择行的时候已经加载了，这里不需要再加载了
				}
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_CARD);
				this.app.statusMgr.updateStatus();
			}
		}else{
			uft.jf.BillToolbar.superclass.btn_edit_handler.call(this);
		}
	},
	/**
	 * 保存成功后执行的脚本
	 */
	doAfterSave : function(values){
		if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_CARD){
			//参照制单保存后的处理
			var refBufferData = this.app.getRefBufferData();//读取缓存中的数据
			var record = uft.Utils.getSelectedRecord(this.app.getHeaderGrid());
			if(record){
				var index = this.app.getHeaderGrid().getStore().indexOf(record);
				if(refBufferData){
					refBufferData.remove(refBufferData[index]);//删除缓存中的对应元素
				}
				if(refBufferData.length > 0){
					//同时参照多条记录，移出已经参照的纪录
					this.app.getHeaderGrid().getStore().remove(record);//移除列表中的该记录
					this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_LIST);
					this.app.clearBodyGrids(); //移除表体的记录,开始保存下一条记录
			        this.app.selectHeaderGridRowOnBO(index);
				}else{
					//只有一条参照制单
			        this.app.setCardValues(values.data.HEADER);//设置表头和表尾数据
			        //更新表头列表
			        this.app.setHeaderRecordValues(record,values.data.HEADER);
	    			if(this.app.hasBodyGrid()) {
	    				//加载所有表体的数据
	    				this.app.loadBodyGrids(values.data.HEADER[this.app.headerPkField]);
	    			}
			        this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
				}				
			}else{
				//FIXME 暂时不处理，参照制单都必须有headerGrid
			}
			this.app.statusMgr.updateStatus();
			this.fireEvent('save',this,values.data,values);
		}else{
			uft.jf.BillToolbar.superclass.doAfterSave.call(this,values);
		}
	},
	btn_can_handler : function(){
		if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_LIST || this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_CARD){
			//若是参照制单的Add状态，重新定义
			var record = uft.Utils.getSelectedRecord(this.app.getHeaderGrid());
			if(record){
				Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('确定将失去未保存的记录？'), function(btn) {
					if (btn == 'yes') {
						this.app.resetCardValues();//重置卡片值
						var index = this.app.getHeaderGrid().getStore().indexOf(record);
						this.app.getHeaderGrid().getStore().remove(record);//移除列表中的该记录
						this.app.clearBodyGrids(); //移除表体的记录
						var values = this.app.getRefBufferData();//读取缓存中的数据
						var toBeRemove = null;
						if(values){
							toBeRemove = values[index];
							values.remove(toBeRemove);//删除缓存中的对应元素
						}
						if(this.app.getHeaderGrid().getStore().getCount() > 0){
							this.app.selectHeaderGridRowOnBO(0);//选择行,但是不触发onRowselect事件
							this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_LIST);
						}else{
							//若此时已经没有记录了，则返回初始化状态
							this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
						}
						this.app.statusMgr.setBizStatus(null);
						this.app.statusMgr.updateStatus();
						this.fireEvent('can',this,toBeRemove);
					}
				},this);
			}
		}else{
			uft.jf.BillToolbar.superclass.btn_can_handler.call(this);
		}
	},
	/**
	 * 打开参照制单的查询窗口
	 * 
	 * @param {} queryWinId
	 * 				查询窗口的ID，一般使用Ext.id()生成即可，该参数是必须的
	 * @param {} srcFunCode
	 * 				参照单据(源单据)的funCode
	 * @param {} srcNodeKey
	 * 				参照单据(源单据)的nodeKey
	 * @param {} srcBillType
	 * 				参照单据(源单据)的单据类型
	 * @param {} bodyTabnamePkFieldMap 
	 * 				源单据的子表的tabname与pkfield的对照，多页签使用,号分隔
	 * @param {} targetApp
	 * 				目标单据的app对象
	 * 				
	 */
	showRefbillQuery : function(config){
		var win = Ext.getCmp(config.queryWinId);
		if(win){
			Ext.apply(win,config); //将参数重新设置，因为可能这时候打开的是二次参照窗口
			win.show();
		}else{
			win = new uft.jf.QueryWindow({
				modal : true,
				fromRefbill : true,
				id : config.queryWinId,
				funCode : config.srcFunCode,
				nodeKey : config.srcNodeKey,
				billType : config.srcBillType,
				bodyTabnamePkFieldMap : config.srcBodyTabnamePkFieldMap,
				targetApp : config.targetApp,
				refbillModel : config.refbillModel || 0, //默认使用模式0，即主子表模式
				secondary : config.secondary
			}).show();	
		}		
	}
});