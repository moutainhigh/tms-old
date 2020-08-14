/*****************定义各种portlet开始***********************/
var smsPortlet = new Ext.ux.Portlet({
	id : 'sms',
	title : '站内信',
	frame : false,
	tools : [{
		id : 'gear',
		qtip : '发布站内信',
		handler : function(){
			new nw.sys.SmsSender({isAdd:true}).show();
		}
	}].concat(tools),
	height : 230,
	autoLoad : 'c/sms/getTop5.html'
});
//查询站内信的总数，并更新到title中
uft.Utils.doAjax({
	scope : this,
	isTip : false,
	url : 'c/sms/getCount.json',
	success : function(values){
		if(values){
    		smsPortlet.setTitle('站内信（<a href="javascript:openSmsNode()">'+values.data+'</a>）');
		}
	}
});		

var tzggPortlet = new Ext.ux.Portlet({
	id : 'tzgg',
	title : '通知公告',
	frame : false,
	tools : tools,
	height : 230,
	autoLoad : 'c/bulletin/getTop5.html?type=0'
});
var newsPortlet = new Ext.ux.Portlet({
	id : 'news',
	title : '新闻中心',
	frame : false,
	tools : tools,
	height : 230,
	autoLoad : 'c/news/getTop5.html'
});
var wdglPortlet = new Ext.ux.Portlet({
	id : 'wdgl',
	title : '文档管理',
	frame : false,
	tools : tools,
	height : 230,
	autoLoad : 'c/doc/getTop5.html'
});	

/*****************定义各种portlet结束***********************/
/*****************autoLoad页面所需要使用的方法***************/
/**
 * 打开通知公告
 * @param {} billId
 */
function openBulletinNode(billId){
	_openNode('t022','通知公告',ctxPath+'/c/bulletin/index.html?funCode=t022',billId);
}
function openDocumentNode(billId){
	if(billId){
		//直接下载文件
		location.href=ctxPath + '/doc/download.do?pk_document='+billId;
	}else{
		_openNode('t03002','政策文件',ctxPath+'/c/doc/index.html?funCode=t03002',billId);
	}
}

/**
 * 打开新闻中心
 * @param {} billId
 */
function openNewsNode(billId){
	var url;
	if(billId){
		url = ctxPath+'/c/news/detail.html?pk_news='+billId;
	}else{
		url = ctxPath+'/c/news/viewList.html';
	}
	_openNode('t024','新闻中心',url);
}
/**
 * 打开站内信节点
 */
function openSmsNode(){
	_openNode('t020','站内信',ctxPath+'/common/sms/index.html?funCode=t020');
}
