<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.nw.Global"%>
<%
	boolean debug = Boolean.valueOf(Global.getPropertyValue("debug")).booleanValue();
	String theme = Global.getPropertyValue("theme");
	String version = Global.getResourceVersion();
%>
<!DOCTYPE html PUBLIC "">
<html xmlns="http://www.w3.org/1999/xhtml" >
<head>
    <title><%=Global.productName %>-登录</title>
	<div id="loading-beforehtml">
		<div style="position: absolute; z-index: 20000; left: 45%; top: 40%;">
			<img src="<c:url value="/images/loading.gif"/>" style="margin-right:8px;float:left;">
			<span style="font: normal 12px arial;">加载中，请稍候...</span>
		</div>
	</div>
	<script type="text/javascript" >
		Constants = {};
		Constants.DEBUG = <%=debug%>;
		Constants.timeOut = 90000;
		Constants.csstheme='<%=theme%>';
		ctxPath = '<%=request.getContextPath()%>';
		resourceCtxPath = ctxPath;
	</script>
	<%if(debug){ %>
		<script type="text/javascript" src="<c:url value="/js/core/common.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/ext/import/ext.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/js/base.js"/>"></script>
		<link href="<%=request.getContextPath() %>/theme/<%=theme%>/css/xtheme.css?v=<%=version %>" rel="stylesheet" type="text/css"></link>
	<%}else{ %>
		<script type="text/javascript" src="<%=request.getContextPath() %>/js/base-min.js?v=<%=version %>"></script>
		<link href="<%=request.getContextPath() %>/theme/<%=theme%>/css/base-min.css?v=<%=version %>" rel="stylesheet" type="text/css"></link>
	<%} %>
    <style type="text/css">
	    #login-logo .x-plain-body{
	        background:transparent url('images/logo.png') center center no-repeat;
	    }
	    .locked{
	        background-image: url(images/lock.png) !important;
	    }
	    .user_code{
	        background-image: url(images/user.png);
	        background-repeat: no-repeat;
	        padding-left: 20px;
	    }
	    .user_password{
	        background-image: url(images/lock.png);
	        background-repeat: no-repeat;
	        padding-left: 20px;
	    }
    </style>
    <script type="text/javascript" src="<c:url value="/VerifyImage.js"/>"></script>
    <script>
  		//是否使用验证码
    	var useVerifyCode = '<%=Global.getPropertyValue("login.useVerifyCode")%>'=='true';
		var logoPanel = new Ext.Panel({
	        baseCls : 'x-plain',
	        id : 'login-logo',
	        region : 'center'
	    });
		var user_code = new Ext.form.TextField({
	        fieldLabel : '登录名',
	        name : 'user_code',
	        cls : 'user_code',
	        blankText : '登录名不能为空',
	        validateOnBlur : true,
	        validationEvent : 'keyup',
	        allowBlank : false
	    });
		var user_password = new Ext.form.TextField({
	        inputType : 'password',
	        name : 'user_password',
	        cls : 'user_password',
	        blankText : '密码不能为空',
	        fieldLabel : '密&nbsp;&nbsp;&nbsp;码',
	        validateOnBlur : true,
	        validationEvent : 'keyup',
	        allowBlank : false
		});
		var service = new Ext.form.Hidden({
			name : 'service',
			value : '${param.service}'
		});
		user_code.on('specialkey', function(textfield, e){
			if(user_code.isValid()){
		        if (e.getKey() == 13){
		        	user_password.focus();
		        }
			}
	    }, this);
		user_password.on('specialkey', function(textfield, e){
			if(user_password.isValid()){
				if (e.getKey() == 13){
					onLogin();
				}
			}
	    },this);
		var items = [user_code,user_password,service];
		if(useVerifyCode){
			var verify_code = new Ext.form.TextField({
				name : 'verify_code',
		        blankText : '验证码不能为空',
		        fieldLabel : '验证码',
		        validateOnBlur : true,
		        validationEvent : 'keyup',
		        allowBlank : false
			});	
			verify_code.on('specialkey', function(textfield, e){
				if(verify_code.isValid()){
			        if (e.getKey() == 13){
			        	hiddenVerify();
			        	onLogin();
			        }
				}
		    }, this);
			verify_code.on('focus', function (textfield){
		       focusGetVerify(textfield.el.dom,'verifyCodeGenerator.do');
		    }, this);
			verify_code.on('blur', function (textfield, e){
		       hiddenVerify();
		    }, this);
			items.push(verify_code);
		}
		var loginForm = new Ext.form.FormPanel({
	        border : false,
	        bodyStyle : useVerifyCode?"padding: 8px":"padding: 15px",
	        waitMsgTarget : true,
	        labelWidth : 60,
	        defaults : {
	            width : 280
	        },
	        items : items
	    });
		var username = new Ext.form.TextField({
	        fieldLabel : '登录名',
	        name : 'username',
	        cls : 'username',
	        blankText : '登录名不能为空',
	        validateOnBlur : true,
	        validationEvent : 'keyup',
	        allowBlank : false
	    });		
		var verifyCode = new Ext.form.TextField({
			name : 'verifyCode',
	        blankText : '验证码不能为空',
	        fieldLabel : '验证码',
	        validateOnBlur : true,
	        validationEvent : 'keyup',
	        allowBlank : false
		});	
		username.on('specialkey', function(textfield, e){
			if(username.isValid()){
		        if (e.getKey() == 13){
		        	verifyCode.focus();
		        }
			}
	    }, this);
		verifyCode.on('specialkey', function(textfield, e){
			if(verifyCode.isValid()){
		        if (e.getKey() == 13){
		        	onReset();
		        }
			}
	    }, this);
		verifyCode.on('focus', function (textfield){
	       focusGetVerify(textfield.el.dom,'verifyCodeGenerator.do');
	    }, this);
		verifyCode.on('blur', function (textfield, e){
	       hiddenVerify();
	    }, this);		
		var resetPasswordForm = new Ext.form.FormPanel({
	        border : false,
	        bodyStyle : "padding: 15px",
	        baseCls : 'x-plain',
	        waitMsgTarget : true,
	        labelWidth : 60,
	        defaults : {
	            width : 280
	        },
	        items : [username,verifyCode]
	    });		
		var tabPanel = new Ext.TabPanel({
			region : 'south',
			deferredRender : false,
			resizeTabs:false,
			border : false,
			activeTab:0,
			height : 115,
		    frame : false,
		    items : [{
		    	id : 'loginTab',
				title : '登&nbsp;&nbsp;&nbsp;&nbsp;录',
				border : false,
				frame : false,
				layout : 'fit',
				items : [loginForm]
		    },{
				title : '忘记密码',
				border : false,
				frame : false,
				layout : 'fit',
				items : [resetPasswordForm]
		    }]
		});
		tabPanel.on('tabchange',function(tabPanel,activeTab){
			var loginBtn = Ext.getCmp('loginBtn');
			var resetBtn = Ext.getCmp('resetBtn');
			if(activeTab.id == 'loginTab'){
				//登录tab，则设置登录按钮可见，隐藏重置密码按钮
				loginBtn.show();
				resetBtn.hide();
			}else{
				//登录tab，则设置重置密码按钮可见，隐藏登录按钮
				loginBtn.hide();
				resetBtn.show();
			}
		});
		//登录
		onLogin = function(){
	    	var body = Ext.getBody();
	    	if(loginForm.getForm().isValid()){
	    		body.mask('登录中，请稍候...');
	    		Ext.Ajax.request({
	    			scope : this,
	    			url : 'ajaxLogin.json',
	    			params : loginForm.getForm().getFieldValues(false),
	    			method : 'POST',
	    			timeout : 90000,
	    			success : function(response){
	    				body.unmask();
	    				var result = Ext.decode(response.responseText);
	    				if(result.success){
	    					body.mask('登录成功，正在跳转...');
	    					//设置cookie
			            	setCookie("user_code",user_code.getValue());
	    					var service = "index.html";
	    					if(result.data && result.data.service){
	    						service = result.data.service;
	    					}
	    					loginWin.destroy();
	    					location.href = service;
	    				}else{
	    					loginForm.getForm().reset();
	    					user_code.focus();
	    					uft.Utils.showErrorMsg(result.msg);
	    				}
	    			},
	    			failure : function(response) {
	    				body.unmask();
	    				uft.Utils.showErrorMsg('服务器处理失败，请重试，若还有问题，请联系管理员！');
	    			}
	    		})
	    	}
		};
		//重置密码 
		onReset = function(){
			var body = Ext.getBody();
	    	if(resetPasswordForm.getForm().isValid()){
	    		body.mask('处理中，请稍候...');
	    		Ext.Ajax.request({
	    			scope : this,
	    			url : 'resetPassword.json',
	    			params : resetPasswordForm.getForm().getFieldValues(false),
	    			method : 'POST',
	    			timeout : 90000,
	    			success : function(response){
	    				body.unmask();
	    				var result = Ext.decode(response.responseText);
	    				if(result.success){
	    					uft.Utils.showInfoMsg(result.msg);
	    				}else{
	    					uft.Utils.showErrorMsg(result.msg);
	    				}
    					resetPasswordForm.getForm().reset();
	    			},
	    			failure : function(response) {
	    				body.unmask();
	    				uft.Utils.showErrorMsg('服务器处理失败，请重试，若还有问题，请联系管理员！');
	    			}
	    		})
	    	}
		}
	    var loginWin = new Ext.Window({
	        title : 'TMS登录',
	        iconCls : 'locked',
	        width : 429,
	        height : 280,
	        resizable : false,
	        draggable : true,
	        modal : false,
	        closable : false,
	        layout : 'border',
	        bodyStyle : 'padding:5px;',
	        plain : false,
	        items : [logoPanel, tabPanel],
	        buttonAlign : 'right',
	        buttons : [{
	        	id : 'loginBtn',
	            text : '登录',
	            cls : "x-btn-text-icon",
	            icon : "images/lock_open.png",
	            handler : onLogin
	        },{
	        	id : 'resetBtn',
	            text : '重置密码',
	            iconCls : 'btnReset',
	            hidden : true,
	            handler : onReset
	        }]
	    }).show();    
		Ext.onReady(function() {
		    if(getCookie("user_code") != null && getCookie("user_code") != ""){
		    	user_code.setValue(getCookie("user_code"));
		    	user_password.focus();
		    }else{
		    	user_code.focus();
		    }
// 		    user_code.setValue('administrator');
// 		    user_password.setValue('143306');
// 		    onLogin();
	    });
    </script>
</head>
<body>
<%@ include file="/common/footer.jsp"%>
</body>
</html>