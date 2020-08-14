/**
 * 工具类,与Ext没有联系，可以单独应用
 */
Utils={
	//模仿StringUtils
	isBlank : function(obj){
		if(typeof(obj) == 'string'){
			return (obj==undefined || obj==null || obj.trim()=='');
		}else{
			return (obj==undefined || obj==null);
		}
	},
	isNotBlank : function(obj){
		if(typeof(obj) == 'string'){
			return (obj != undefined && obj != null && obj.trim() != '');
		}else{
			return (obj != undefined && obj != null);
		}
	},
	StringBuffer:function(){  
		this._strings_ = [];
		this.append = function(str){
			this._strings_.push(str);
			return this;
		};
		this.toString = function(){
			return this._strings_.join("");
		};
	},
	UrlAppend : function (url, s) {
		if (Utils.isNotBlank(url)) {
			return url + (url.indexOf('?') === -1 ? '?' : '&') + s;
		}
		return url;
	},    
	
	DisableUrlCaching : function (url) {
		if (Utils.isNotBlank(url)) {
			Utils.UrlAppend(url, String.format('{0}={1}','_dc', new Date().getTime()));
		}
		return url;
	},
	
	newGuid : function (){ 
	    var guid = ""; 
	    for (var i = 1; i <= 32; i++){ 
	        var n = Math.floor(Math.random()*16.0).toString(16); 
	        guid += n; 
	        if((i==8)||(i==12)||(i==16)||(i==20)) 
	            guid += "-"; 
	    } 
	    return guid; 
	},
    createXhrObject : function() {
        var http;
//        var activeX = ['MSXML2.XMLHTTP.5.0','MSXML2.XMLHTTP.4.0','MSXML2.XMLHTTP.3.0', 'MSXML2.XMLHTTP', 'Microsoft.XMLHTTP'];
        var activeX = ['Msxml2.XMLHTTP.6.0','Msxml2.XMLHTTP.3.0','Msxml2.XMLHTTP'];//Ext使用这个

        try {
            http = new XMLHttpRequest();
        } catch (e) {
            for (var i = 0,len=activeX.length; i < len; ++i) {
                try {
                    http = new ActiveXObject(activeX[i]);
                    break;
                } catch (e) { }
            }
        } finally {
            return http;
        }
    },
	/**
	 * ajax请求
	 * 默认使用GET方式提交
	 * @param {} url
	 * @param {} params
	 * @param {} method post/get
	 * @param {} type 同步/异步
	 * @param {} onSuccess 当异步请求成功时，执行的callback
	 * @param {} onFailure 当异步请求失败时，执行的callback
	 * @param {} onBeforeSend 当异步请求时,在发送请求之前执行的callback
	 * @param {} onFailure 当异步请求时,请求完成时执行的callback
	 * @param {} returnType 返回数据的格式，默认是json
	 */
	request : function(config){
		//为url增加一个时间戳
		var URL = config.url;
    	if(URL.indexOf("?")==-1){
    		URL+="?";
    	}else{
    		URL+="&";
    	}
    	URL+="_dc="+(new Date()).getTime();
		var params 		 = config.params;
		var method 		 = config.method||'POST';
		var type=true;
		if(config.type===false){
			type=false;
		}
		var onSuccess 	 = config.onSuccess;
		var onFailure 	 = config.onFailure;
		var onBeforeSend = config.onBeforeSend;
		var onComplete 	 = config.onComplete;
		var scope 		 = config.scope || self;
	    var paramStr=null;
	    if(method.toUpperCase()=='POST'){
	    	if(params){
	    		paramStr='';
		    	for(var key in params){
		    		//如果值为空,则不传参数
		    		if(params[key] == undefined || params[key] == null){
		    			continue;
		    		}
		    		if(params[key] instanceof Array){
		    			//如果是数组
		    			for(var i=0;i<params[key].length;i++){
		    				paramStr += key + "="+encodeURI(params[key][i]);
		    				paramStr +="&";
		    			}
		    		}else{
		    			paramStr += key + "="+encodeURI(params[key]);
		    			paramStr +="&";
		    		}
				}
				paramStr = paramStr.substring(0,paramStr.length-1);
	    	}	 
	    }else{
	    	method='GET';//默认使用GET发送数据
	    	// 将params加入url
		    if(params){
		    	for(var key in params){
		    		//如果值为空,则不传参数
		    		if(params[key] == undefined || params[key] == null){
		    			continue;
		    		}
		    		if(params[key] instanceof Array){
		    			//如果是数组
		    			for(var i=0;i<params[key].length;i++){
		    				URL+= '&';
		    				URL += key + "="+encodeURI(params[key][i]);
		    			}
		    		}else{
		    			URL+= '&';
		    			URL += key + "="+encodeURI(params[key]);
		    		}
				}
		    }
	    }
		if(typeof(onBeforeSend)=='function'){
			onBeforeSend.call(scope);
		}
	    var conn = Utils.createXhrObject();
    	//URL=encodeURI(URL);
	    conn.open(method, URL, type);
	    if(method.toUpperCase()=='POST'){
	    	conn.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
	    }
	    conn.send(paramStr);
	    if(type){
	    	//异步请求
	        conn.onreadystatechange = function (){
	          	if (conn.readyState == 4){
		          	if(conn.status==200){
			       		var result='';
			       		if(Utils.isNotBlank(conn.responseText)){
			       			result=conn.responseText;
			       		}
			       		//这里不处理result的格式，在callback中根据要求再做处理
						if(typeof(onSuccess)=='function'){
							onSuccess.call(scope,result);
						}else{
							//默认处理
							try{
			    				var json=JSON.parse(result);
			    				if(json.success==false){
			    					alert(json.msg);
			    					return;
			    				}
			    			}catch(e){
			    				//不需要处理异常，可能本身就不想返回json格式
			    			}
						}
		          	}else{
		          		if(typeof(onFailure)=='function'){
							onFailure.call(scope,result);
						}else{
							alert("请求失败！XmlHttpRequest status: [" + conn.status + "].");
						}
		          	}
			        if(typeof(onComplete)=='function'){
						onComplete.call(scope);
					}		          	
	          	}
	        };	
	    }else{
	    	//同步请求
		    if (Utils.isNotBlank(conn.responseText)) {
		    	try{
		    		var json = JSON.parse(conn.responseText); //调用json2.js中的方法
		        	if(json.success==false || json.success=='false'){
		        		if(json.msg){
		        			alert(json.msg); //提示框
		        		}else{
		        			alert("操作失败！");
		        		}
		        		return;
		        	}
		        	return json;
		    	}catch(e){
					//以json结尾的请求一般都要返回json字符串，否则请不要以json结尾
					//此处判断如果以json结尾的url没有返回json，这种情况可能是登录超时。
					if(URL.indexOf(".json")>-1){
						alert("服务器没有返回正确的数据，可能登录已超时！");
						return;
					}
		    	}
		    	return conn.responseText;
		    }
		    return null;	    	
	    }
	},
	/**
	 * 异步请求
	 * @param {} url
	 * @param {} params
	 * @param {} method
	 * @param {} onSuccess 当异步请求成功时，执行的callback
	 * @param {} onFailure 当异步请求失败时，执行的callback
	 */
	doAsyncRequest : function(url,params,method,onSuccess,onFailure){
		Utils.request({
			url : url,
			params : params,
			method : method,
			type : true,
			onSuccess : onSuccess,
			onFailure : onFailure
		});
	},
	
	/**
	 * 同步请求
	 * @param url
	 * @param params
	 * @method post/get
	 * 
	 * */
	doSyncRequest : function(url,params,method) {
		return Utils.request({
			url : url,
			params : params,
			method : method,
			type : false
		});
	},
	/**
	 * 格式化日期，
	 * @param pattern 格式化后的日期格式
	 * @param date 待格式化的日期对象
	 */
	formatDate:function(pattern,date){
	    function formatNumber(data,format){// 3
	        format = format.length;
	        data = data || 0;
	        // return format == 1 ? data :
			// String(Math.pow(10,format)+data).substr(-format);//该死的IE6！！！
	        return format == 1 ? data : (data=String(Math.pow(10,format)+data)).substr(data.length-format);
	    }
	    return pattern.replace(/([YMDhsm])\1*/g,function(format){
	        switch(format.charAt()){
	        case 'Y' :
	            return formatNumber(date.getFullYear(),format);
	        case 'M' :
	            return formatNumber(date.getMonth()+1,format);
	        case 'D' :
	            return formatNumber(date.getDate(),format);
	        case 'w' :
	            return date.getDay()+1;
	        case 'h' :
	            return formatNumber(date.getHours(),format);
	        case 'm' :
	            return formatNumber(date.getMinutes(),format);
	        case 's' :
	            return formatNumber(date.getSeconds(),format);
	        }
	    });
	},
	/**
	 * 将array对象转换为string类型
	 * @param {} array 待转换的array对象
	 * @param {} split 在每个元素之间插入的符号
	 * @return {}
	 */
	arrayToString : function(array,split,prefix){  
		if(split==undefined){
			split=',';
		}
		var str='';
		for(var i=0,len=array.length;i<len;i++){
			if(Utils.isNotBlank(prefix)){
				str+=prefix;
			}
			str+=array[i];
			if(Utils.isNotBlank(prefix)){
				str+=prefix;
			}
			str+=split;
		}
		return str.substring(0,str.length-split.length);
	},
	/**
	 * 格式化数字，保留decimal位小数，四舍五入
	 * @param {} digit 待格式化的数
	 * @param {} decimal 保留的小数位数
	 * @return {}
	 */
	formatDigit :function(digit,decimal){    
		if(!decimal){
			decimal=0;
		}
   		digit  =  Math.round(digit*Math.pow(10,decimal))/Math.pow(10,decimal); 
   		return digit;    
	},
	/**
	 * 从一段代码中读取所有的script脚本
	 * 这个通常会是用于ajax调用中返回一段html代码（包括script脚本）
	 * @param {} data
	 * @return {}
	 */
	getJavaScript : function(data) {
		var startTag = "<script";
		var endTag = "</script>";
		var scripts = [];
		var startIndex = data.indexOf(startTag);
		while(startIndex > -1){
			data = data.substring(startIndex);
			data = data.substring(data.indexOf('>')+1); //脚本数据开始
			var endIndex = data.indexOf(endTag);
			scripts.push(data.substring(0,endIndex));
			data = data.substring(endIndex+9); //endTag的长度是9
			startIndex = data.indexOf(startTag);
		}
		return scripts;
	},	
	removeHTMLTag : function(str) {
        str = str.replace(/<\/?[^>]*>/g,''); //去除HTML tag
        str = str.replace(/[ | ]*\n/g,'\n'); //去除行尾空白
        //str = str.replace(/\n[\s| | ]*\r/g,'\n'); //去除多余空行
        str=str.replace(/&nbsp;/ig,'');//去掉&nbsp;
        return str;
    },
    /**
     * <p>Returns true if the passed value is empty.</p>
     * <p>The value is deemed to be empty if it is<div class="mdetail-params"><ul>
     * <li>null</li>
     * <li>undefined</li>
     * <li>an empty array</li>
     * <li>a zero length string (Unless the <tt>allowBlank</tt> parameter is <tt>true</tt>)</li>
     * </ul></div>
     * @param {Mixed} value The value to test
     * @param {Boolean} allowBlank (optional) true to allow empty strings (defaults to false)
     * @return {Boolean}
     */
    isEmpty : function(v, allowBlank){
        return v === null || v === undefined || ((Utils.isArray(v) && !v.length)) || (!allowBlank ? v === '' : false);
    },

    /**
     * Returns true if the passed value is a JavaScript array, otherwise false.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isArray : function(v){
        return Object.prototype.toString.call(v) === '[object Array]';
    },

    /**
     * Returns true if the passed object is a JavaScript date object, otherwise false.
     * @param {Object} object The object to test
     * @return {Boolean}
     */
    isDate : function(v){
        return Object.prototype.toString.call(v) === '[object Date]';
    },

    /**
     * Returns true if the passed value is a JavaScript Object, otherwise false.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isObject : function(v){
        return !!v && Object.prototype.toString.call(v) === '[object Object]';
    },

    /**
     * Returns true if the passed value is a JavaScript Function, otherwise false.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isFunction : function(v){
        return Object.prototype.toString.call(v) === '[object Function]';
    },

    /**
     * Returns true if the passed value is a number. Returns false for non-finite numbers.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isNumber : function(v){
        return typeof v === 'number' && isFinite(v);
    },

    /**
     * Returns true if the passed value is a string.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isString : function(v){
        return typeof v === 'string';
    },

    /**
     * Returns true if the passed value is a boolean.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    isBoolean : function(v){
        return typeof v === 'boolean';
    },
	/**
	 * 从url中读取参数值，类似java中的getParameter(String)
	 * @param {} name
	 * @return {}
	 */
	getParameter : function(name) {
		var URLParams = {};
		var aParams = document.location.search.substr(1).split('&');
		for (i = 0; i < aParams.length; i++) {
			var aParam = aParams[i].split('=');
			URLParams[aParam[0]] = aParam[1];
		}
		return URLParams[name];
	},
	//setTimeout()方法会返回一个唯一id用来以后清除定时器，这里使用这个id
	getUUID : function() {
        var id = setTimeout('0');
        clearTimeout(id);
        return id;
	},
	//打开节点，如果存在父框架，那么调用父框架的openNode方法
	openNode : function(title,url){
		if(parent && parent != window){
			parent.openNode(Utils.getUUID(),title,url,true,true);
		}else{
			window.open(url);
		}
	},
	/**
	 * 获取request中的参数
	 */
	getRequestParam : function(strParame) { 
		var args = new Object( ); 
		var arr = [];//url 中含有多个billId
		var query = location.search.substring(1);
		var pairs = query.split("&"); // Break at ampersand 
		for(var i = 0; i < pairs.length; i++) { 
			var pos = pairs[i].indexOf('='); 
			if (pos == -1) continue; 
			var argname = pairs[i].substring(0,pos); 
			var value = pairs[i].substring(pos+1); 
			value = decodeURIComponent(value); 
			args[argname] = value; 
			if(strParame == argname) {
				arr.push(value);
			}
		} 
//		if(!args[strParame]) {
//			var url = args['url'] || window.location.href;
//			if(url) {
//				var arr = url.split('?');
//				var paramArr = [];
//				if(arr.length > 1 && arr[1]) {
//					 paramArr = arr[1].split('&');
//				}
//				for(var i = 0; i < paramArr.length; i++) { 
//					var pos = paramArr[i].indexOf('='); 
//					if (pos == -1) continue; 
//					var argname = paramArr[i].substring(0, pos); 
//					args[argname] = paramArr[i].substring(pos+1);
//				} 
//			}
//		}
		if(arr.length > 1) {
			return arr;
		}
		return args[strParame]; 
	}
};

(function(){
	ua = navigator.userAgent.toLowerCase(),
	check = function(r){
	    return r.test(ua);
	},
	DOC = document,
	docMode = DOC.documentMode,
	isStrict = DOC.compatMode == "CSS1Compat",
	isOpera = check(/opera/),
	isChrome = check(/\bchrome\b/),
	isWebKit = check(/webkit/),
	isSafari = !isChrome && check(/safari/),
	isSafari2 = isSafari && check(/applewebkit\/4/), // unique to Safari 2
	isSafari3 = isSafari && check(/version\/3/),
	isSafari4 = isSafari && check(/version\/4/),
	isIE = !isOpera && check(/msie/),
	isIE7 = isIE && (check(/msie 7/) || docMode == 7),
	isIE8 = isIE && ((check(/msie 8/) && docMode != 7)||(check(/msie 9/) && docMode == 8)),
	isIE9 = isIE && (check(/msie 9/) && docMode == 9),
	isIE6 = isIE && !isIE7 && !isIE8 && !isIE9,
	isGecko = !isWebKit && check(/gecko/),
	isGecko2 = isGecko && check(/rv:1\.8/),
	isGecko3 = isGecko && check(/rv:1\.9/),
	isBorderBox = isIE && !isStrict,
	isWindows = check(/windows|win32/),
	isMac = check(/macintosh|mac os x/),
	isAir = check(/adobeair/),
	isLinux = check(/linux/),
	isSecure = /^https/i.test(window.location.protocol);
	
	Utils.isIE =isIE;
	Utils.isIE6=isIE6;
	Utils.isIE7=isIE7;
	Utils.isIE8=isIE8;
	Utils.isIE9=isIE9;
	Utils.isSecure=isSecure;
})();
