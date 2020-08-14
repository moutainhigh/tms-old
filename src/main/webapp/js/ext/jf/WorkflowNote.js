Ext.namespace('uft.jf');
/**
 * 审批情况window
 * billType,单据类型
 * billId,单据ID
 * funCode，节点编码
 */
uft.jf.WorkflowNote= function(config){
	this.shadow=false;
	Ext.apply(this, config);
	uft.Utils.assert(this.billId, "没有设置参数【billId】！");
	uft.Utils.assert(this.funCode, "没有设置参数【funCode】！");
	this.modal = (config.modal == undefined)?true:config.modal;
	this.width = (config.width == undefined)?640:config.width;
	this.height = (config.height == undefined)?450:config.height;
	//FIXME lisq 2015-01-26 审批流预测参数，同apprPredict 分开设置
	var apprPredict = Utils.getRequestParam("apprPredict")==1;
	var shortWorkflowPicUrl = 'getApprovePic.do';
	var shortWorkflowNoteUrl = 'getWorkflowNote.json';
	if(apprPredict){
		//出现审批状况的tab，此时使用新的请求url
		shortWorkflowPicUrl = 'getWorkflowPredictPic.do';
		shortWorkflowNoteUrl = 'getWorkflowPredict.json';
	}
	
	if(!this.urlPrefix){
		this.urlPrefix="";
	}
	var workflowPicUrl = this.urlPrefix +shortWorkflowPicUrl+'?billId='+this.billId+'&_dc='+(new Date()).getTime();
	var workflowNoteUrl = this.urlPrefix + shortWorkflowNoteUrl+'?billId='+this.billId+'&_dc='+(new Date()).getTime();
	if(this.funCode){
		workflowPicUrl += "&funCode="+this.funCode;
		workflowNoteUrl += "&funCode="+this.funCode;
	}
	if(this.billType){
		workflowPicUrl += "&billType="+this.billType;
		workflowNoteUrl += "&billType="+this.billType;
	}
	
	var columns,recordType;
	if(apprPredict){
		//如果是审批状况，则使用ajax请求返回单据模板的列信息
		var values = Utils.request({
			type : false,
			url : 'getWorkflowPredictTemplet.json'
		});
		if(values.data){
			columns = values.data['columns'];
			recordType = values.data['recordType'];
		}
	}else{
		columns = [{
		   header : 'pk_checkflow',
		   hidden : true,
	       dataIndex: 'pk_checkflow'
	    },{
	       header: '发送人',
	       dataIndex: 'senderman_name',
	       align : 'center'
	    },{
	       header: '发送日期',
	       dataIndex: 'senddate',
	       width:130,
	       align : 'center'
	    },{
	       header: '审批人',
	       dataIndex: 'checkmen_name',
	       align : 'center'
	    },{
	       header: '审核日期',
	       dataIndex: 'dealdate',
	       width:130,
	       align : 'center'
	    },{
	        header: '审批状况',
	        dataIndex: 'approvestatus_name',
	        align : 'center'
	     },{
	       header: '审批意见',
	       dataIndex: 'approveresult',
	       align : 'center'
	    },{
	       header: '批语',
	       dataIndex: 'checknote',
	       align : 'center'
	    },{
	       header: '说明',
	       dataIndex: 'messagenote',
	       width:350,
	       align : 'center'
	    },{
	    	xtype : 'numbercolumn',
	        header: apprPredict?'金额':'原币金额',
	        dataIndex: 'money',
	        align : 'right'
	     },{
	     	xtype : 'numbercolumn',
	        header: '辅币金额',
	        dataIndex: 'localmoney',
	        align : 'right',
	        hidden : apprPredict
	     },{
	     	xtype : 'numbercolumn',
	        header: '送审原币金额',
	        dataIndex: 'premoney',
	        align : 'right',
	        hidden : apprPredict
	     },{
	     	xtype : 'numbercolumn',
	        header: '送审辅币金额',
	        dataIndex: 'preassmoney',
	        align : 'right',
	        hidden : apprPredict
	     }];
		recordType =[
	        {name: 'pk_checkflow', type: 'string'},
	        {name: 'senderman', type: 'string'},
	        {name: 'senddate', type: 'string'},
	        {name: 'checkman', type: 'string'},
	        {name: 'dealdate', type: 'string'},
	        {name: 'lasttime', type: 'string'},
	        {name: 'ischeck', type: 'string'},
	        {name: 'approvestatus', type: 'string'},
	        {name: 'approveresult', type: 'string'},
	        {name: 'checknote', type: 'string'},
	        {name: 'messagenote', type: 'string'},
	        {name: 'localmoney', type: 'float',useNull:true},
	        {name: 'premoney', type: 'float',useNull:true},
	        {name: 'preassmoney', type: 'float',useNull:true},
	        {name: 'money', type: 'string',useNull:true},
	        {name: 'senderman_name', type: 'string',sortName:'senderman'},
	        {name: 'checkmen_name', type: 'string',sortName:'checkman'},
	        {name: 'ischeck_name', type: 'string',sortName:'ischeck'},
	        {name: 'approvestatus_name', type: 'string',sortName:'approvestatus'}
		];
	}
	
	var grid = new uft.extend.grid.BasicGrid({
		dataUrl:workflowNoteUrl,
		isAddBbar : false,
		immediatelyLoad : 'true',
		recordType : recordType,
		columns : columns
	});
	var wfTitle = apprPredict?"流程信息":"流程历史信息";
	var topPanel = new Ext.Panel({
		region : 'north',
		layout : 'fit',
		border : false,
		frame : false,
		height : 20,
		html : '<div style="height:20px;line-height:20px;"><center><h1>'+wfTitle+'</h1></center></div>'
	});	
	var centerPanel = new Ext.Panel({
		region : 'center',
		layout : 'fit',
		border : false,
		items : [grid]
	});
	this.btmPanel = new Ext.Panel({
		region : 'south',
		layout : 'fit',
		autoScroll : true,
		split:true,
		height : 200,
		bodyStyle : 'display:inline-block;vertical-align:middle;',
		html:'<div style="display:table-cell;vertical-align:middle;height:180px;*font-size:150px;line-height:180px;"><img src='+workflowPicUrl+' border=0 style="display:block;margin:0 auto;"/></div>'
	});
	
	var main = new Ext.Panel({
		layout : 'border',
		border : true,
		items : [topPanel,centerPanel,this.btmPanel]
	});

	var btns = [{
			xtype : 'button',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}
	];
	uft.jf.WorkflowNote.superclass.constructor.call(this, {
		title : '审批情况',
		width : this.width,
		height : this.height,
		collapsible : false,
		shim : true,
		frame : true,
		closable : true, 
		maximizable : true,
		border : false,
		modal : this.modal,
		border : false,
		layout : 'fit',
		items : [main],
		buttons : btns
    });
};
Ext.extend(uft.jf.WorkflowNote,Ext.Window, {
});