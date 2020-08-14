if(window.Constants == undefined){
	window.Constants = {};
	window.Constants.csstheme = 'default';//默认样式
}
/**
 * Ext.onReady可以同时多处使用，不会被覆盖
 */
 //定义该URL，否则默认从官网上下载该文件
Ext.BLANK_IMAGE_URL=ctxPath+"/theme/"+Constants.csstheme+"/images/default/s.gif";
Ext.onReady(function(){
	focus();//打开该页面时将焦点放在该页面上，否则快捷键不能直接使用
    Ext.QuickTips.init();
	Ext.Ajax.on('requestcomplete',checkSessionStatus, this);
});
function checkSessionStatus(conn,response,options){
	//Ext重新封装了response对象
	if(options['url']&&options['url'].indexOf('.json')>-1){
		//ajax请求
		var data=response.responseText;
		if(data){
			if(data.indexOf('{')!=0&&data.indexOf("[")!=0){ //使用这种方式判断返回的数据是否是json格式串
				alert('服务器没有返回正确的数据，可能登录已超时！');
				return;
			}
		}
	}
};
