/**
 * 自定义Ext vtype
 */
Ext.apply(Ext.form.VTypes, {
	'password' : function(val, field) {
		if (field.initial) {
			var pwd = Ext.getCmp(field.initial);
			return (val == pwd.getValue());
		}
		return false;
	},
	'passwordText' : '两次输入的密码不一致！',

	'chinese' : function(val) {
		var reg = /^[\u4e00-\u9fa5]+$/i;
		return reg.test(val);
	},
	'chineseText' : '只能输入中文！',
	
	'alphanum' : function(val) {
		var alphanum = /^[a-zA-Z0-9_]+$/;
		return alphanum.test(val);
	},
	'alphanumText' : '只能输入英文字母、数字、下划线！',
	'positivenum' : function(val){
		var reg = /^[1-9]\d*$/;
		return reg.test(val);
	},
	'positivenumText' : '只能输入正数',
	'negativenum' : function(val){
		var reg = /^-[1-9]\d*$/;
		return reg.test(val);
	},
	'negativenumText' : '只能输入负数',

	'url' : function(val) {
		var url = /(((^https?)|(^ftp)):\/\/([\-\w]+\.)+\w{2,3}(\/[%\-\w]+(\.\w{2,})?)*(([\w\-\.\?\\\/+@&#;`~=%!]*)(\.\w{2,})?)*\/?)/i;
		return url.test(val);
	},
	'urlText' : '请输入有效的URL地址！',

	'ip' : function(val) {
		var ip = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
		return ip.test(val);
	},
	'ipText' : '请输入正确的IP地址！',

	'phone' : function(val) {
		var phone = /^((0[1-9]{3})?(0[12][0-9])?[-])?\d{6,8}$/;
		return phone.test(val);
	},
	'phoneText' : '请输入正确的电话号码,如:0920-29392929',

	'mobile' : function(val) {
		var mobile=/(^0?[1][34578][0-9]{9}$)/;
		return mobile.test(val);
	},
	'mobileText' : '请输入正确的手机号码！',

	'alpha' : function(val) {
		var alpha = /^[a-zA-Z_]+$/;
		return alpha.test(val);
	},
	'alphaText' : '请输入英文字母！',
	'idcard' : function(val){
		var idcard = /^(\d{15}$|^\d{18}$|^\d{17}(\d|X|x))$/;
		return idcard.test(val);
	}, 
	'idcardText' : '请输入正确的身份证号！'
});