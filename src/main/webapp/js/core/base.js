$(function(){
    registerTrListener();
});
function stopBubble(e){
	//如果传入了事件对象.那么就是非IE浏览器
	if(e && e.stopPropagation){
	//因此它支持W3C的stopPropation()方法
		e.stopPropagation();
	}else{
		//否则,我们得使用IE的方式来取消事件冒泡
		window.event.cancelBubble = true;
	}
}
/**
 * 注册tr的事件，行单击，鼠标经过事件
 */
_trHoverHandler = function(event){
   	if(!$(this).hasClass("tclick")){
       	$(this).addClass("thover");
   	}
};
_trHoverMouseOut = function(event){
   	if($(this).hasClass("thover")){
       	$(this).removeClass("thover");
    }
};
_trClickHandler = function(event){
	// 设置当前行
	$(this).removeClass("thover");
	$(this).addClass("tclick");
	
	var chkArr = $('input:checkbox'); // checkbox数组
	/* checkbox操作 */
	if(event.target && event.target.type== 'checkbox'){
		//点击在checkbox上
		var chk = event.target;
		if(!chk.checked){
			$(this).removeClass('tclick');
		}
	}else if(event.target && event.target.children.length > 0 && event.target.children[0].type == 'checkbox'){
		//点击在checkbox单元格上
		var checkbox = event.target.children[0];
		checkbox.checked = !checkbox.checked;
		if(!checkbox.checked){
			$(this).removeClass('tclick');
		}
	}else if(event.target && event.target.tagName== 'A'){
		// 取消其他行样式
		$(this).siblings().removeClass('tclick');
		//点击在链接上,此时肯定选择该行				
		var chkObj=$('input:checkbox',this); // 当前选择的行
		if(chkObj && chkObj.length > 0){
			for(var i=0;i<chkArr.length;i++){
				if(chkArr[i].id == chkObj[0].id){
					chkObj[0].checked = true;
					if(!chkArr[i].checked){
						$(this).removeClass('tclick');
					}
				}else{
					chkArr[i].checked = false;
				}
			}
		}
	}else {
		// 取消其他行样式
		$(this).siblings().removeClass('tclick');
		// 设置当前行的checkbox为选定
		var chkObj=$('input:checkbox',this); // 当前选择的行
		if(chkObj && chkObj.length > 0){
			var chkObjChecked = chkObj[0].checked;
			for(var i=0;i<chkArr.length;i++){
				if(chkArr[i].id == chkObj[0].id){
					chkArr[i].checked=!chkObjChecked;
					if(!chkArr[i].checked){
						$(this).removeClass('tclick');
					}
				}else{
					chkArr[i].checked = false;
				}
			}
		}
	}
};
registerTrListener = function(){
	/**
	 * 鼠标经过行时，加入背景色
	 */
	$("tbody tr").unbind('hover',_trHoverHandler);
	$("tbody tr").hover(_trHoverHandler,_trHoverMouseOut);
	
	/**
	 * 行点击事件
	 */
	$("tbody tr").unbind('click',_trClickHandler);//先移除该事件
	$("tbody tr").click(_trClickHandler);	
};
/**
 * 翻页需要调用的函数
 * @param {} pageIndex
 * @param {} pageSize
 */
_ChangePage = function(pageIndex,pageSize){
	document.form.PAGE_PARAM_START.value=(pageIndex-1)*pageSize;
	document.form.PAGE_PARAM_LIMIT.value = pageSize;
	document.form.submit();
};
/**
 * 全选
 * @param {} checkbox
 */
selectAll = function(checkbox){
	var chkArr = $('td input:checkbox');
	for(var i=0;i<chkArr.length;i++){
		chkArr[i].checked = checkbox.checked;
	}
};
/**
 * 返回选中的行
 * @return {}
 */
getSelectedIds = function(attr_name){
	var ids = [];
	var chkArr = $('td input:checkbox');
	for(var i=0;i<chkArr.length;i++){
		if(chkArr[i].checked){
			ids.push(chkArr[i].attributes[attr_name].value);
		}
	}
	return ids;
}