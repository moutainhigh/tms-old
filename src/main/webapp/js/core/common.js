/**
 * note:在定义prototype方法时，为了不与其他框架的方法产生冲突，
 * 方法的命名规范统一前面加入下划线
 * String中定义的方法目前暂时不修改，但是以后都必须参考上面的命名规范
 * 如Array中定义的_removeByIndex,_indexOf
 * 这个教训太严重了
 */
if(window.Constants == undefined){
	window.Constants = {};
	window.Constants.csstheme = 'default';//默认样式
}
/**
 * 引入JS或CSS文件
 * 
 * @param path
 *            js或者css文件路径
 * @param type
 *            类型，如果为css，那么引入css文件；否则引入javascript文件 注意：引用js 和
 *            css只支持widgets目录下的css和js文件，对于非widgets目录下的文件，请使用原始声明方式
 * @param id css时，作为link标签的id
 * @param charset 字符集，默认为gbk
 */
function $import(path, type, id, charset){
	var doc = document;
	if(charset==null){
		charset="UTF-8";
	}
	var i = 0, base = getResourceCtxPath();
	if(!path){
		return ;
	}
	//对于从外部引入的js处理
	var abPath;
	if(path.indexOf('http://') == 0){
		abPath = path;
	}else{
		abPath = base + path;
	}
	try {// 添加js，css文件的引用
		if (type == "css") {
			if (id&&id !=""){
				id ='id="'+id+'"';
			}else{
				id="";
			}
			doc.writeln('<li'+'nk '+id+' href="' + abPath + '" rel="stylesheet" type="text/css" charset="'+charset+'"></li'+'nk>');
		} else {
			doc.writeln('<scr'+'ipt src="' + abPath + '" type="text/javascript" charset="'+charset+'"></scr'+'ipt>');
		}
	} catch (e) {// 如果异常，则创建相应的元素
		if( type == "css"){
			var linkEle = doc.createElement("link");
			linkEle.href = base + path +  ".css" ;
			
			doc.getElementsByTagName("head")[0].appendChild( linkEle );
		}else {
			var script = doc.createElement("script");
			script.src = base + path +  ".js" ;
			
			doc.getElementsByTagName("head")[0].appendChild( script );
		}
	}
}

/**
 * 获取文件的所在路径,如： ca/company/
 */
function getBasePath(jsFileName){
	if(jsFileName == '') {
		alert( 'import.getBasePath error:js file name cannot be null');
		return ;
	}
	var src = jsFileName ;
	var scripts = document.getElementsByTagName( "script"); 
    var i=0, len = scripts.length;
	for ( ; i < len; i++) {
		if (scripts[i].src.match(src)) {			 
			return scripts[i].src.replace(src, "");
		}
	}
	return "" ;
}

/**
 * 返回上下文路径
 */
function getResourceCtxPath(){
	// resourceCtxPath在header.jsp中已定义
	return resourceCtxPath;
}
function getCtxPath(){
	// ctxPath在header.jsp中已定义
	return ctxPath;
}
/**
 * 阻止浏览器的默认行为
 * @param {} e
 * @return {Boolean}
 */
function stopDefault(e) {
    if (e && e.preventDefault)
        e.preventDefault();
    else
    	//IE
        window.event.returnValue = false;
    return false;
} 

/**
    功能：修改 window.setTimeout，使之可以传递参数和对象参数   
    使用方法： setTimeout(回调函数,回调函数的执行scope,时间,参数1,,参数n)   
*/
var __sto = setTimeout;    
window.setUftTimeout = function(callback,scope,timeout,param){    
    var args = Array.prototype.slice.call(arguments,3);    
    var _cb = function(){    
        callback.apply(scope,args);    
    };
    return __sto(_cb,timeout); 
};
/**
    功能：修改 window.setInterval，使之可以传递参数和对象参数   
    使用方法： setInterval(回调函数,回调函数的执行scope,时间,参数1,,参数n)   
*/
var __sto1 = setInterval;    
window.setUftInterval = function(callback,scope,timeout,param){    
    var args = Array.prototype.slice.call(arguments,3);    
    var _cb = function(){    
        callback.apply(scope,args);    
    };
    return __sto1(_cb,timeout); 
};

/**
 * 定义类似java的replaceAll方法
 * @param {} source
 * @param {} replaceWith
 * @param {} ignoreCase
 * @return {}
 */
String.prototype.replaceAll = function(source, replaceWith, ignoreCase) {
    if (!RegExp.prototype.isPrototypeOf(source)) {
        return this.replace(new RegExp(source, (ignoreCase ? "gi": "g")), replaceWith);
    } else {
        return this.replace(source, replaceWith);
    }
};
/**
 * 判断一个字符串是否以某个子串开头
 * @param {} str
 * @return {}
 */
String.prototype.startWith=function(str){  
  var reg=new RegExp("^"+str);  
  return reg.test(this);     
};
/**
 * 判断一个字符串是否以某个子串结尾
 * @param {} str
 * @return {}
 */
String.prototype.endWith=function(str){  
  var reg=new RegExp(str+"$");  
  return reg.test(this);     
};
/**
 * 对于单据页面，禁用页面的回退
 * @param {} e
 * @return {Boolean}
 */
function stopBackSpace(e){
	var ev = e || window.event;//获取event对象  
    var obj = ev.target || ev.srcElement;//获取事件源  
    var t = obj.type || obj.getAttribute('type');//获取事件源类型  
    if(ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea"){  
        return false;  
    }
}
/**
 * 两个参数，一个是cookie的名子，一个是值
 * @param {} name
 * @param {} value
 */
function setCookie(name,value){
    var Days = 30;//此 cookie 将被保存 30 天
    var exp  = new Date();//new Date("December 31, 9998");
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
/**
 * 根据名称读取cookie
 * @param {} name
 * @return {}
 */
function getCookie(name){
	var arr = document.cookie.match(new RegExp("(^| )"+name+"=([^;]*)(;|$)"));
	if(arr != null) return unescape(arr[2]); return null;
}
/**
 * 根据名称删除cookie
 * @param {} name
 * @return {}
 */
function delCookie(name){
	try{
		var exp = new Date();
		exp.setTime(exp.getTime() - 1);
		var cval=getCookie(name);
		if(cval!=null) document.cookie= name + "="+cval+";expires="+exp.toGMTString();
	}catch(ex){
	
	}
}
