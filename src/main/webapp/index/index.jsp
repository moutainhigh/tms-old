<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.nw.json.JacksonUtils"%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
	<head>
		<meta name="renderer" content="webkit">
		<title><%=org.nw.Global.productName %></title>
		<%@ include file="/common/header_lite.jsp"%>
		<%if(debug){ %>
			<script type="text/javascript" src="<c:url value="/index/Index.js"/>"></script>
			<script type="text/javascript" src="<c:url value="/index/Workspace.js"/>"></script>
			<script type="text/javascript" src="<c:url value="/index/QuickLink.js"/>"></script>
			<script type="text/javascript" src="<c:url value="/index/ShortcutPanel.js"/>"></script>
			<script type="text/javascript" src='<c:url value="/js/ext/jf/PopUp.js?v=${version}" />'></script>
			<script src="http://pv.sohu.com/cityjson?ie=utf-8"></script>  
		<%}else{ %>
			<script type="text/javascript" src="<%=request.getContextPath() %>/index/index-min.js?v=<%=version %>"></script>
		<%} %>
		<link href="<%=request.getContextPath() %>/css/index.css" rel="stylesheet" type="text/css"></link>
	</head>
	<body>
	</body>
	<%@ include file="/common/footer.jsp"%>
	<script>
	var indexPage=null;
	function logout(){
		window.location.href="<c:url value="/logout.do"/>";
	}
	//编辑密码
	function openEditPasswordWin(){
		openNode("changPwd","修改密码",ctxPath+"/password/index.html",true,true);
	}
	
	function openSms(){
		openNode("t020","站内信",ctxPath+"/common/sms/index.html?funCode=t020",true,true);
	}
	/**
	 * 打开功能节点
	 * 
	 * @nodeId		节点id
	 * @title		标题 
	 * @url			iframe地址
	 * @isActive	是否设置为当前活动tab页签
	 * @isCloseable	是否可关闭
	 */
	function openNode(nodeId,title,url,isActive,isCloseable){
		var tab = new Ext.ux.IFrameComponent({
    		id : nodeId,
   			title : title,
   			tabTip : title,
   			closable: isCloseable,
   			border : false,
            autoScroll: true,
			url : url,
			layout : 'fit'
		});
   		indexPage.mainPanel.add(tab);
   		if(isActive){
   			indexPage.mainPanel.setActiveTab(tab);
   		}
	}
	/*使用触发左边菜单树打开功能节点*/
	function fireTreeNode(funCode){
		if(funCode.length <= 2){
			uft.Utils.showWarnMsg('打开的节点编码不正确！');	
			return;
		}
		var tree = Ext.getCmp(funCode.substring(0,2));
		var node = tree.getNodeById(funCode);
		if(node==undefined){
			uft.Utils.showWarnMsg('打开的节点不存在！');	
			return;
		}
		tree.fireEvent('click', node);
	}
	
	/**
	* 关闭功能节点
	*/
	function closeNode(nodeId){
		var comp = getNode(nodeId);
		if(comp){
			comp.destroy();
		}
	}
	
	/**
	* 返回功能节点所在地tabPanel
	* 子页面可能需要通过该方法得到tabPanel，再进行相应的操作，如注册事件。
	*/
	function getNode(nodeId){
		return Ext.getCmp(nodeId);
	}
	
	/**
	 * 页面初始化
	 */
	Ext.onReady(function(){
		Ext.QuickTips.init();
		Ext.BLANK_IMAGE_URL='<%=request.getContextPath()%>/theme/<%=theme%>/images/default/s.gif';
		var ip = "";
		var city = "";
		if(typeof(returnCitySN) != 'undefined'){
			ip = returnCitySN["cip"];
			city = returnCitySN["cname"];
		}
		indexPage=new uft.index.Index({funVOs:Ext.decode('${funVOs}'),
			corpName : '<%=loginInfo.getCorp_name() %>',
			userCode : '<%=loginInfo.getUser_code() %>',
			userName : '<%=loginInfo.getUser_name() %>',
			language : '<%=loginInfo.getLanguage() %>',
			ip : ip,
			city : city
		});
		
		var pk_user = '<%=loginInfo.getPk_user() %>';
		/* var href = window.location.href;//完整路径 http://localhost:8081/tms/index.html
		var host = window.location.host;//主机名 + 端口 localhost:8081
		var port = window.location.port;//端口 8081
		var url = 'ws:' + href.substring(7,href.length-11) + '/socket';
		alert(url)
		var webSocket = new WebSocket(url);
		alert(webSocket.readyState);
		webSocket.onopen = function(){
			webSocket.send("Message to send");
			alert("webSocket onopen... ");
		}
		webSocket.onmessage  = function(e){
			var message = e.data;
			alert("Message is received... :" + message);
		}
		webSocket.onclose = function(){
			var message = e.data;
			alert("Connection is closed...");
		} */
		/* var popUp;
		var popUpTimeout;
		function getPopUp(){
			var popInfo = Utils.doSyncRequest('getPopup.json',{pk_user:pk_user},'POST');
			if(popInfo && popInfo.length > 0){
				if(popUp  && (popUp.hidden == false || popUp.hidden == 'false')){//页面不存在的时候，才弹窗。
					popUp.close();
				}
				popUp = new uft.jf.PopUp({popInfos:popInfo}).show();
				//弹出框一分钟后关闭
				//重新设置定时关闭
				window.clearTimeout(popUpTimeout); 
				popUpTimeout = window.setTimeout(function() {
					popUp.close();
		        },60*1000);
			}
		}
		getPopUp();
		//定时获取
		window.setInterval(function(){
			getPopUp();
		},10*1000); */
		
		//默认打开页面
		openNode("default","我的桌面","<c:url value="/default.html"/>",true,false);
		//打开工作台，这里工作台数量是预设好的，以后要是修改工作台，或者添加工作台，需要在dafaultController里添加相应的工作台代码
		var workbenchs = <%=JacksonUtils.writeValueAsString(loginInfo.getWorkbenchs()) %>
		if(workbenchs && workbenchs.length > 0){
			for(var i=0;i< workbenchs.length;i++){
				var workbenchVO = workbenchs[i];
				var nodeId = workbenchVO.pk_workbench;
				var title = workbenchVO.workbench_name;
				var page = workbenchVO.page_name;
				openNode(nodeId,title,"<c:url value='/"+page+"'/>",false,true);
			}
		}
		
		window.collapseLeft = function(){
			var left = Ext.getCmp('leftPanel');
			if(left){
		 		if(!left.collapsed){
		 			left.collapse();
		 		}else{
		 			left.expand();
		 		}
	 		}
			var header = Ext.getCmp('headerPanel');
			if(header){
				if(!header.collapsed){
					header.collapse();
		 		}else{
		 			header.expand();
		 		}
			}
		};
		
		//autoComplete快捷入口
		var quickEntry = new uft.extend.form.Combox({
			renderTo : 'quickEntry',
			id : 'fun_name',
			minChars : 1,
			editable : true,
			emptyText : '快捷入口...',
			dataUrl : 'quickEntry.json',
			listConfig : {  
	            emptyText : '<div style="line-height:22px;padding:2px 10px">没有找到匹配的数据</div>'
	        }
		});
		quickEntry.trigger.setDisplayed(false);
		//当选择了以后，类似于点击左边的树节点
		
		quickEntry.on('select',function(combo,record){
			var url = record.data.value;
			var title = record.data.text;
			openNode(title,title,url,true,true);
		});
	});
	
	function keepSessionAlive(){
		Utils.doAsyncRequest('keepSessionAlive.json');
	}
	//每半个小时请求一次
	setUftInterval(keepSessionAlive,this,1800000);
	setUftTimeout(function(){
	    uft.Utils.doAjax({
	    	scope : this,
	    	url : 'checkLicense.json',
	    	isTip : false
	    });
	},this,100);
	
	</script>
</html>
