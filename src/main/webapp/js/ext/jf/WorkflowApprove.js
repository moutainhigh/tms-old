Ext.namespace('uft.jf');
/**
 * 审批窗口
 * billId,单据ID
 * billType，单据类型
 * app，application对象
 * record,所选记录
 */
uft.jf.WorkflowApprove= function(config){
	this.shadow=false;
	Ext.apply(this, config);
	this.modal = (config.modal == undefined)?true:config.modal;
	this.width = (config.width == undefined)?640:config.width;
	this.height = (config.height == undefined)?450:config.height;
	var no_str = '不批准';
	var ok_str = '批准';
	
	
	var columns = [{
	   header : 'pk_checkflow',
	   hidden : true,
       dataIndex: 'pk_checkflow'
    },{
        header: '审批人',
        dataIndex: 'checkmen_name',
        sortable : true,
        align : 'center'
	},{
         header: '审批意见',
         dataIndex: 'approvestatus_name',
         sortable : true,
         align : 'center'
	},{
       header: '审批时间',
       dataIndex: 'dealdate',
       format : 'y-M-d',
       align : 'center',
       width:140,
       sortable : true
    },{
       header: '批语',
       dataIndex: 'checknote',
       sortable : true,
       align : 'center'
    }];
	var recordType =[
        {name: 'pk_checkflow', type: 'string'},
        {name: 'dealdate', type: 'string'},
        {name: 'lasttime', type: 'string'},
        {name: 'ischeck', type: 'string'},
        {name: 'approvestatus', type: 'string'},
        {name: 'checknote', type: 'string'},
        {name: 'checkman', type: 'string'},
        {name: 'senderman_name', type: 'string',sortName:'senderman'},
        {name: 'checkmen_name', type: 'string',sortName:'checkman'},
        {name: 'ischeck_name', type: 'string',sortName:'ischeck'},
        {name: 'approvestatus_name', type: 'string',sortName:'approvestatus'}
	];
	
	var topPanel = new Ext.Panel({
		region : 'north',
		layout : 'fit',
		border : false,
		height : 30,
		frame : false,
		html : '<div style="height:20px;line-height:20px;"><center><h1>流程历史信息</h1></center></div>'
	});
		
	var grid = new uft.extend.grid.BasicGrid({
		autoExpandColumn : 5,
		dataUrl:'getWorkflowNote.json?billId='+this.billId+'&funCode='+this.funCode+"&billType="+this.billType+'&_dc='+(new Date()).getTime(),
		immediatelyLoad : 'true',
		recordType : recordType,
		columns : columns
	});
	var northPanel = new Ext.Panel({
		layout : 'fit',
		region : 'north',
		border : false,
		height : 220,
		items : [grid]
	});
	this.centerPanel = new Ext.FormPanel({
		region : 'center',
		border : false,
		frame : false,
		bodyStyle : '',
		items : [{
			xtype :'fieldset',
			title : '审批意见（250字以内）',
			layout : 'column',
			autoHeight : true,
			border : true,
			items : [{
				columnWidth : .33,
				border : false,
				items :  [new Ext.form.Radio({
					name : 'approveAdvice',
					boxLabel : '<span style="font-size: 12px;">'+ok_str+'</span>',
					inputValue : 'approve',
					checked : true,
					listeners:{
						'check' : {
							fn:function(radio,isChecked) {
								if (isChecked) { 
									this.textArea.setValue(ok_str);
			    				}
			    			},
			    			scope:this
			    		}
					}
				})]
			},{
				columnWidth : .33,
				border : false,
				items :  [new Ext.form.Radio({
					name : 'approveAdvice',  
					boxLabel : '<span style="font-size: 12px;">'+no_str+'</span>',
					inputValue : 'unapprove',
					listeners:{
						'check' : {
							fn:function(radio,isChecked) {
								if (isChecked) { 
									this.textArea.setValue(no_str);
								}
							},
							scope:this
						}
					}
				})]
			},{
				columnWidth : .33,
				border : false,
				items :  [new Ext.form.Radio({
					name : 'approveAdvice',  
					boxLabel : '<span style="font-size: 12px;">驳回制单人</span>',
					labelStyle : 'font-size: 12px;',
					inputValue : 'backward',
					listeners:{
						'check' : {
							fn:function(radio,isChecked) {
								if (isChecked) { 
									this.textArea.setValue('驳回制单人');
								}
							},
							scope:this
				        }
					}
				})]
			}]
		}]
	});
	
	this.textArea = new Ext.form.TextArea({
		value : ok_str,
		height:80
	});
	var btmPanel = new Ext.FormPanel({
		region : 'south',
		layout : 'fit',
		height : 80,
		frame : false,
		border : false,
		cls:'checkNote',
		items : [this.textArea]
	});
	
	var mainSubPanel = new Ext.Panel({
		layout : 'border',
		region : 'center',
		border : false,
		split:true,
		items : [northPanel,this.centerPanel,btmPanel]
	});	
	
	var btns = [{
		xtype : 'button',
		text : '确&nbsp;&nbsp;定',
		scope : this,
		handler : function() {
			this.doApprove();
		}
	},{
		xtype : 'button',
		text : '取&nbsp;&nbsp;消',
		scope : this,
		handler : function() {
			this.destroy();
		}
	},{
		xtype : 'button',
		text : '流&nbsp;&nbsp;程&gt;&gt;',
		scope : this,
		handler : function() {
			this.viewApproveinfo();
		}
	}];	
	
	uft.jf.WorkflowApprove.superclass.constructor.call(this, {
		title : '审批处理情况',
		width : this.width,
		height : this.height,
		collapsible : false,
		shim : true,
		frame : true,
		closable : true,
		maximizable : true, //最大化
		border : false,
		modal : true,
		border : false,
		layout : 'border',
		items : [topPanel,mainSubPanel],
		buttons : [btns]
    });
};
Ext.extend(uft.jf.WorkflowApprove,Ext.Window, {
	afterRender : function(){
		uft.jf.WorkflowApprove.superclass.afterRender.call(this);
	},
	//执行审批动作
	doApprove : function(){
		var params = this.app.newAjaxParams();
		if(this.record){
			//对于轻量级页面可能没有record对象，但是此时的billId已经在params里了
			params[this.app.getBillIdField()]=this.record.data[this.app.headerPkField];
		}else if(this.billId){
			//从ulw页面打开的
			params[this.app.getBillIdField()] = this.billId;
		}
		
		params["approveAdvice"]=this.centerPanel.form.findField('approveAdvice').getGroupValue();
		params["checkNote"]=this.textArea.getValue();
		params["assignUsersPKs"]=this.assignUsersPKs;
		if(typeof(this.doBeforeApprove) == 'function'){
			if(this.doBeforeApprove(params) == false){
				return;
			}
		}
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	isTip : false,
	    	actionType:"审批",
	    	url : 'approve.json',
	    	success : function(values){
	    		this.doAfterApprove(values);
	    	}
	    });
	},
	doAfterApprove : function(values) {
		//请注意AbstractBaseController中对返回值的封装
		if(this.records){//对于轻量级页面可能没有records对象
    		//toolbar需要从BillToolbar中传入
    		this.app.setHeaderValues(this.records,values.datas);
		}
		// 审批后刷新表体审批页签
		var ap = Ext.getCmp(uft.jf.Constants.WORKFLOWPREDICT);
		if(ap) {
			this.app.reloadWorkflowPredict(ap); 
		}
		if(values.datas&&values.datas.length>0){
			if(this.app.ulw){
				//保存到已更新列表,ulw需要根据该数据同步到列表页
				var headerPk = values.datas[0].HEADER[this.app.headerPkField];
				if(this.app.updateBillIdAry.indexOf(headerPk) == -1){
					this.app.updateBillIdAry.push(headerPk);
					this.app.updateTsAry.push(values.datas[0].HEADER['ts']);
				}
			}
			this.app.statusMgr.setBizStatus(values.datas[0].HEADER[this.app.getBillStatusField()]);
		}
		//保持原有页面状态不变
		this.app.statusMgr.updateStatus();
		if(values.appendMsg){
			uft.Utils.showWarnMsg(values.appendMsg);
		}
		if(values.datas&&values.datas.length>0){
			if(this.app != window){
				//对于轻量级页面可能没有toolbar对象
				this.toolbar.fireEvent('approve',this,values.datas,values);
			}
		}				
		this.destroy();
		//modify by chengw 增加审批后是否返回首页参数控制
		var isReturnIndex = Utils.getRequestParam('isReturnIndex'); 
		if(isReturnIndex=="true"){
			window.top.open('','_parent','');
			window.top.close();
			// var url = window.top.location.href;
			// if(url.indexOf("?")!=-1){
				// url = url.substring(0,url.indexOf("?"));
			// }
			// window.top.location.href = url;
		}
	},
	viewApproveinfo : function(){
		var params = this.app.newAjaxParams();
		if(this.record){
			//对于轻量级页面可能没有record对象
			params[this.app.getBillIdField()]=this.record.data[this.app.headerPkField];
		}
		new uft.jf.WorkflowViewer(params).show();
	}
});