Ext.ns('uft.jf');
/**
 * 与模板相关的信息，结构类似javaBean
 */
uft.jf.Context=function(){
	var templateID=null;		//单据模板ID，作为参数发送到NCServer
	var headerTabCode=null;		//表头和表尾的tabCode肯定大于等于1个，多个使用,分隔
	var bodyTabCode=null;		//表体的tabCode，多个使用,分隔
	var funCode = null; 		//功能节点编码
	var nodeKey = null; 		//节点标识
	var billType = null; 		//单据类型
	this.getTemplateID = function(){
		return templateID;
	};
	this.setTemplateID = function(newTemplateID){
		templateID = newTemplateID;
	};
	this.getHeaderTabCode = function(){
		return headerTabCode;
	};
	this.setHeaderTabCode = function(newHeaderTabCode){
		headerTabCode = newHeaderTabCode;
	};
	this.getBodyTabCode = function(){
		return bodyTabCode;
	};
	this.setBodyTabCode = function(newBodyTabCode){
		bodyTabCode = newBodyTabCode;
	};
	this.getFunCode = function(){
		return funCode;
	};
	this.setFunCode = function(newFunCode){
		funCode = newFunCode;
	};
	this.getNodeKey = function(){
		return nodeKey;
	};
	this.setNodeKey = function(newNodeKey){
		nodeKey = newNodeKey;
	};
	this.getBillType = function(){
		return billType;
	};
	this.setBillType = function(newBillType){
		billType = newBillType;
	};
	
	var billId = null;			//单据ID，当billId存在时，直接打开卡片页
	this.getBillId = function(){
		return billId;
	};
	this.setBillId = function(newBillId){
		billId = newBillId;
	};
	
	var billIds = null;			// 待打开的单据数组，这里用","分割，当billIds存在时，直接加载列表页，并且加载对应的数据。
	this.getBillIds = function(){
		return billIds;
	};
	this.setBillIds = function(newBillIds){
		billIds = newBillIds;
	};
};