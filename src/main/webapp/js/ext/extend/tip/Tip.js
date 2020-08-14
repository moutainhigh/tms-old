Ext.ns('uft.extend.tip');
/**
 * 一个操作提示框
 */
uft.extend.tip.Tip = function(){
    var msgCt;

    function createBox(t, s){
    	var className = 'msg-success';
    	if(t == 'info'){
    		t = '提示';
    		className = 'msg-infor';
    	}else if(t == 'warn'){
    		t = '警告';
    		className = 'msg-warning';
    	}else if(t == 'error'){
    		t = '错误';
    		className = 'msg-error';
    	}
    	return ['<div class="message '+className+'">',
                '<h3>', t, '</h3>','<p>', s,'</p>','<div id="msg-close-div" class="msg-close"></div>',
                '</div>'].join('');
    }
    return {
    	/**
    	 * @title
    	 * @format,
    	 */
        msg : function(t, s){
            if(!msgCt){
                msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
            }
            /**
            alignTo( Mixed element, String position, [Array offsets], [Boolean/Object animate] ) : Ext.Element
			将el对齐到element,positon,指示对齐的位置,可选以下定义
			tl     左上角(默认)
			t      上居中
			tr     右上角
			l      左边界的中央
			c      居中
			r      右边界的中央
			bl     左下角
			b      下居中
			br     右下角
             */
            msgCt.alignTo(document, 'tr-tr');
            var m = Ext.DomHelper.append(msgCt, {html:createBox(t, s)}, true);
            //注册close事件
            var el = m.dom.children[0].children[2];//得到msg-close
            Ext.get(el).on('click',function(event,el){
            	var msgDiv = el.parentNode;
            	msgDiv.parentNode.removeChild(msgDiv);
            });
            m.slideIn('t').pause(5).ghost("t", {remove:true});
        },

        init : function(){
            var lb = Ext.get('lib-bar');
            if(lb){
                lb.show();
            }
        }
    };
}();