//供各个autoLoad页面调用的方法，可以打开具体的功能节点
function _openNode(funCode,title,url,billId){
	if(billId){
		if(url.indexOf('?') == -1){
			url += "?";
		}else{
			url += "&";
		}		
		url += 'billId='+billId;
	}
	if(parent && parent != window){
		parent.openNode(billId||funCode,title,url,true,true);
	}else{
		window.open(url);
	}
}
/**
 * portlet组件中，打开一条明细的待办或者打开列表
 * @param {} billId
 */
function openPortletNode(pk_portlet,fun_code,fun_name,class_name,billId){
	var url = ctxPath+class_name;
	if(url.indexOf('?') == -1){
		//url中没有参数
		url+='?';
	}else{
		url+='&';
	}
	url+='funCode='+fun_code;
	url+='&pk_portlet='+pk_portlet;//加入这个参数
	if(billId){
		url+='&billId='+billId;
		if(fun_code == 't036'){
			//alarm
			_openNode(billId,'待办事项',ctxPath+'/common/alarm/goTodo.html?pk_alarm='+billId);
		}else{
			_openNode(billId,'待办事项',url);
		}
	}else{
		_openNode(fun_code,fun_name,url);
	}
}
/**
 * 点击'更多'超链接时的方法
 * @param pk_portlet
 * @param fun_code
 * @param fun_name
 * @param class_name
 * @param billIds
 * @returns
 */
function openPortletNodeWithBillIds(pk_portlet,fun_code,fun_name,class_name,billIds){
	var url = ctxPath+class_name;
	if(url.indexOf('?') == -1){
		//url中没有参数
		url+='?';
	}else{
		url+='&';
	}
	url+='funCode='+fun_code;
	url+='&pk_portlet='+pk_portlet;//加入这个参数
	url+='&billIds='+billIds;
	_openNode(fun_code,fun_name,url);
}

var tools = [{
	id : 'refresh',
	handler : function(e, target, panel) {
		if(panel.autoLoad){
			panel.getUpdater().update(panel.autoLoad);
		}
	}
}];
//默认portal类
DefaultPortal = function(config) {
	Ext.apply(this,config);
	var firstCol = new Ext.ux.PortalColumn({
		columnWidth : .33,
		style : 'padding:5px 0 5px 5px'
	});
	var secondCol = new Ext.ux.PortalColumn({
		columnWidth : .33,
		style : 'padding:5px 0 5px 5px'
	});
	var thirdCol = new Ext.ux.PortalColumn({
		columnWidth : .33,
		style : 'padding:5px 0 5px 5px'
	});
	
	//用户的门户配置数据
	var pcVOs = this.pcVOs;
	if(pcVOs){
		for(var i=0;i<pcVOs.length;i++){
			var pcVO = pcVOs[i];
			var portlet_code=pcVO['portlet_code'];
			var portlet = Ext.getCmp(portlet_code);
			if(!portlet){
				var title = pcVO['portlet_name'];
				if(pcVO['query_sql']){
					title += "（<a href=\"javascript:openPortletNode('"+pcVO['pk_portlet']+"','"+pcVO['fun_code']+"','"+pcVO['fun_name']+"','"+pcVO['class_name']+"');\">"+pcVO['num_count']+"</a>）";
				}
				portlet = new Ext.ux.Portlet({
					id : portlet_code,
					title : title,
					frame : false,
					tools : tools,
					height : 230,
					autoLoad : 'getLatestTodo.html?pk_portlet='+pcVO['pk_portlet']
				});
			}
			this.portletCounter.push(portlet);
			if(pcVO['column_index'] == 1){
				//放入第一列
				firstCol.add(portlet);
			}else if(pcVO['column_index'] == 2){
				//放入第二列
				secondCol.add(portlet);
			}else{
				//放入第三列
				thirdCol.add(portlet);
			}
		}
	}
	
	var portalContainer = new Ext.ux.Portal({
		margins : '0 0 0 0',
		items : [firstCol, secondCol,thirdCol]
	});
	return new Ext.Viewport({
		layout : 'fit',
		items : [portalContainer]
	});
};
