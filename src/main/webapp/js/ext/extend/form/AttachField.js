Ext.namespace('uft.extend.form');
/**
 * 显示附件
 * 
 * @class uft.extend.form.AttachField
 * @extends Ext.form.DisplayField
 */
uft.extend.form.AttachField = Ext.extend(Ext.form.DisplayField, {
	defaultAutoCreate : {tag: 'div', cls: 'x-attach-item-border'},
	hideLabel : true,
	rowspan : 5,//占用多少行,改动这个数字需要同时改动css中的高度，否则会出现行被撑高的情况
	_downLoadUrl : 'downLoadFile.do', //url前缀
	_getAttachNodesUrl : 'getAttachNodes.json', //得到所有文件节点的url
	_attachPathMap : null, //存储文件地址和文件名的对象
	_initValue : '<p>附件：</p>',
	constructor : function(config) {
		Ext.apply(this, config);
		uft.extend.form.AttachField.superclass.constructor.call(this);
	},
	setValue : function(value){
		if(!value || !value.billType){
			uft.extend.form.AttachField.superclass.setValue.call(this,this._initValue);
			return;
		}
		this.setObjectValue(value);
	},
	/**
	 * 参数config是一个对象，可能包括billId,billType,vbillno,其中billType是必须的
	 * @param {} config
	 */
	setObjectValue : function(config){
		if(!config || !config.billType){
			uft.extend.form.AttachField.superclass.setValue.call(this,this._initValue);
			return;
		}
		var attachPathMap = this._loadAttachPathMap(config);
		var value = this._getAttachValue(attachPathMap);
		uft.extend.form.AttachField.superclass.setValue.call(this,value);
	},
	_loadAttachPathMap : function(config){
		if(!config || !config.billType){
			return null;
		}
		//这里使用同步请求，因为调用reset以及setValue时都会调用该方法，导致调用多次
		var values = Utils.request({
			type : false,
			url : this._getAttachNodesUrl,
			params : {billId:config.billId,billType:config.billType,vbillno:config.vbillno}
		});
		if(!values){
			return null;
		}
		return values.data;
	},
	/**
	 * 返回要设置到value中的字符串，包括这些路径，以及超链接，当点击文件名后，下载文件
	 */
	_getAttachValue : function(attachPathMap){
		if(!attachPathMap){
			return this._initValue;
		}
		var str=this._initValue;
		for(var key in attachPathMap){
			//fix by wangxf 2012/9/18 21:45 将url地址编码,防止乱码问题
			str += '<p style="padding:0 0 2px 0"><a href="'+this._downLoadUrl+'?nodepath='+encodeURI(key)+'" target="_blank">'+attachPathMap[key]+'</a></p>';
		}
		return str;
	}
});
Ext.reg("attachfield",uft.extend.form.AttachField);