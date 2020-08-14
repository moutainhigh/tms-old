Ext.ns('uft');
window.__bindings = [];
if (window.addEventListener){
	window.__eventName = "keydown";
}else{
	window.__eventName = "onkeydown";
}  
/**
 * 该类参考了默认的Ext keyMap类，将事件注册在window上，Ext的keyMap类是将事件注册到具体的某个元素上，
 * 这样只有在该元素获得焦点时才能激活快捷键。
 * @class uft.KeyMap
 * @extends Object
 */
uft.KeyMap = Ext.extend(Object, {
	constructor : function(config){
	    if(config){
	        this.addBinding(config);
	    }
	    this.enable();
	},
	addBinding : function(config) {
		if (Ext.isArray(config)) {
			Ext.each(config, function(c) {
						this.addBinding(c);
					}, this);
			return;
		}
		var keyCode = config.key, fn = config.fn || config.handler, scope = config.scope;

		if (typeof keyCode == "string") {
			var ks = [];
			var keyString = keyCode.toUpperCase();
			for (var j = 0, len = keyString.length; j < len; j++) {
				ks.push(keyString.charCodeAt(j));
			}
			keyCode = ks;
		}
		var keyArray = Ext.isArray(keyCode);

		var handler = function(e) {
			var flag = true;
			var val, key, keys = ['shift', 'ctrl', 'alt'];
	        for (var i = 0, len = keys.length; i < len; ++i){
	            key = keys[i];
	            val = config[key];
	            if(!(val === undefined || (val === e[key + 'Key']))){
	                flag = false;
	            }
	        }
	        if(flag === true){
	            if(keyArray){
	                for(var i = 0, len = keyCode.length; i < len; i++){
	                    if(keyCode[i] == e.keyCode){
	                      	fn.call(scope || window, e.keyCode, e);
//	                      return;//这里不再return，若注册了多个相同的快捷键，则会执行所有匹配的项。
	                    }
	                }
	            }else{
	                if(e.keyCode == keyCode){
	                    fn.call(scope || window, e.keyCode, e);
//	                    return;
	                }
	            }
	        }
		};
		window.__bindings.push(handler);
	},	
	handleKeyDown : function(e) {
//		this.stopDefault(e);
		window.afterOneTrigger=false;//在具体执行该handler时，会判断该变量，如果已经执行过一次了，那么不再需要执行第二次。
		var b = window.__bindings;
		var ee = e;
		for (var i = 0, len = b.length; i < len; i++) {
			b[i].call(this, ee);
		}
	},	
	//阻止浏览器默认行为
    stopDefault : function(e) {
	    if (e && e.preventDefault){
	    	e.keyCode=0;
	    	e.preventDefault();
	    	e.stopPropagation();
	    }else{
	    	window.event.keyCode=0;
	    	window.event.returnValue = false;
	    }
	    return false;
	} ,	
	enable: function(){
		if (window.addEventListener){
//			window.removeEventListener(window.__eventName, this.handleKeyDown, false);//先移除已经定义的event
			window.addEventListener(window.__eventName, this.handleKeyDown, false);
		}else{
//			document.detachEvent(window.__eventName, this.handleKeyDown);//先移除已经定义的event
			document.attachEvent(window.__eventName, this.handleKeyDown);
		}
	}
});