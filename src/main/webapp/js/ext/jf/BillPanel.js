Ext.namespace('uft.jf');
/**
 * 单据Panel，基础ToftPanel，封装了与单据相关的操作
 */
uft.jf.BillPanel = Ext.extend(uft.jf.ToftPanel, {
	initBizStatus : null,
	/**
	 * 返回HeaderPanel的Item数组
	 * @return {}
	 */
	getHeaderItem : function(){
		var headerItem = uft.jf.BillPanel.superclass.getHeaderItem.call(this);
		if(this.headerGrid && this.hasBillId()){
			this.headerGrid.hidden = true;
			//颠倒下顺序，为了视觉上的效果
			headerItem.reverse();
		}
		return headerItem;
	},
	fireRowDblClick : function(app){
		if(app.headerGrid.getStore().getCount()==1){
			clearInterval(app.intervalID);
			app.headerGrid.getSelectionModel().selectFirstRow();
			app.headerGrid.fireEvent('rowdblclick',app.headerGrid,0);
		}
	},	
	/**
	 * 重写rowdblclick动作，可能处于OP_REFADD	状态
	 * @param {} grid
	 * @param {} rowIndex
	 * @param {} e
	 */
	onRowdblclick : function(grid,rowIndex,e){
		if(this.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_LIST){
			//若是参照add状态，重新定义
			var record = uft.Utils.getSelectedRecord(grid);
			if(record){
				var refBufferData = this.getRefBufferData();
				if(refBufferData && refBufferData[rowIndex]){
//					this.resetCardValues();
//					this.setCardValues(refBufferData[rowIndex].HEADER);
					//表体的值在选择行的时候已经加载了，这里不需要再加载了
					//注意：对于参照制单，行选择不再触发onRowselect事件了，因为如果触发，会导致页面状态改变
					this.setAppValues(refBufferData[rowIndex]);
				}
				this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_CARD);
				this.statusMgr.updateStatus();
			}
		}else{
			uft.jf.BillPanel.superclass.onRowdblclick.call(this,grid,rowIndex,e);
		}
	},
	//行选择事件
	onRowselect : function(sm, rowIndex, record) {
		if(this.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_REFADD_LIST){
			//如果此时是参照制单的列表页，选择行也不修改其状态
			return;
		}
		this.statusMgr.setBizStatus(record.data[this.getBillStatusField()]);
		if(this.context.getBillId() == null || this.context.getBillId()==''){
			uft.jf.BillPanel.superclass.onRowselect.call(this,sm,rowIndex,record);
			return;
		}else{
			this.statusMgr.removeBeforeUpdateCallback(this.backupHistoryHeight,this);
			this.statusMgr.removeAfterUpdateCallback(this.setHeaderHeight,this);
			this.statusMgr.updateStatus();
			this.statusMgr.addBeforeUpdateCallback(this.backupHistoryHeight,this);
			this.statusMgr.addAfterUpdateCallback(this.setHeaderHeight,this);			
		}
		this.headerGrid.fireEvent('afterrowselect',record,rowIndex);
	},	
	getBillStatusField : function(){
		return "vbillstatus";
	},
	newAjaxParams : function(param){
		var params = uft.jf.BillPanel.superclass.newAjaxParams.call(this);
		params[this.getBillIdField()]=this.context.getBillId();
		return params;
	},
	/**
	 * 将参照制单返回的数据设置到当前单据中，
	 * 该方法在QueryWindow.js中调用
	 * values如果是一个数组，此时是参照制单的情况，可能是一条记录和多条记录的情况大不相同，需要区分处理
	 * 可能是报销单生成还款单的情况，此时value不是数组
	 * @param returnValue
	 * 		参照制单返回的聚合vo数据
	 * @param secondary
	 * 		是否是二次参照,这个参数目前只对于平铺方式有效
	 */
	doRefbill : function(values,secondary){
		if(values){
			if(!this.getHeaderGrid()){
				uft.Utils.showErrorMsg('包含参照制单的页面必须存在表头列表对象[headerGrid]！');
				return false;
			}
			
			if(secondary === true){
				//如果是二次参照
				//设置表体数据
				if(Ext.isArray(values)){
					for(var i=0;i<values.length;i++){
						this.setBodyValues(values[i].BODY);
					}
				}else{
					this.setBodyValues(values.BODY);
				}
			}else{
				var flag = false;
				if(Ext.isArray(values)){
					if(values.length==1){
						values = values[0];
						flag = true;
					}
				}else{
					flag = true;
				}
				if(flag === true){
					this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
					this.statusMgr.updateStatus();
					
					//只有一条记录
					this.refBufferData = [values]; //参照制单的缓存数据,注意这里的values只是一个对象,不是数组
					if(values.HEADER){
						this.setCardValues(values.HEADER,false);//设置卡片页的值
					}
					//清空表头数据
					this.getHeaderGrid().getStore().removeAll();
					 //加入表头列表数据
					this.getHeaderGrid().addRecord(values.HEADER,this.getHeaderGrid().getStore().getCount());
					this.getHeaderGrid().updateTbarInfo();
					this.selectHeaderGridRowOnBO(0);//选择行,但是不触发onRowselect事件
					
					//清空表体的数据
					if(this.hasBodyGrid()){
						this.clearBodyGrids();
					}
					this.setBodyValues(values.BODY);
					
					this.statusMgr.removeBeforeUpdateCallback(this.backupHistoryHeight,this);
					this.statusMgr.removeAfterUpdateCallback(this.setHeaderHeight,this);
					this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_CARD);
					this.statusMgr.setBizStatus(uft.jf.bizStatus.NEW);
					this.statusMgr.updateStatus();
					this.statusMgr.addBeforeUpdateCallback(this.backupHistoryHeight,this);
					this.statusMgr.addAfterUpdateCallback(this.setHeaderHeight,this);
				}else{
					//多条记录
					this.refBufferData = values; //参照制单的缓存数据
					
					//清空表头数据
					this.getHeaderGrid().getStore().removeAll();
					for(var i=0;i<values.length;i++){
						if(values[i].HEADER){
							this.getHeaderGrid().addRecord(values[i].HEADER,i); //设置表头列表数据
						}
					}
					this.getHeaderGrid().updateTbarInfo();
					this.selectHeaderGridRowOnBO(0);//选择行,但是不触发onRowselect事件
					
					//清空表体的数据
					if(this.hasBodyGrid()){
						this.clearBodyGrids();
					}					
					//选择第一条记录,此时会检测当前的页面状态是否是OP_REFADD_LIST,若是，则会读取缓存数据，填充到表体
					this.statusMgr.setPageStatus(uft.jf.pageStatus.OP_REFADD_LIST);
					this.statusMgr.setBizStatus(uft.jf.bizStatus.NEW);		
					this.statusMgr.updateStatus();
				}
			}
		}
	},
	/**
	 * 返回参照制单的缓存数据,如果存在，该数据是一个Array
	 * @return {}
	 */
	getRefBufferData : function(){
		if(this.refBufferData){
			return this.refBufferData;
		}
		return null;
	}
});