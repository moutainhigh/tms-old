<script type="text/javascript">
var _tmpDiv =null;
__RemoveNode=function(n){
	if(isIE){
        if(n&&n.tagName!='BODY'){
            _tmpDiv = _tmpDiv || document.createElement('div');
            _tmpDiv.appendChild(n);
            _tmpDiv.innerHTML = '';
        }
    }else{
        if(n&&n.parentNode&&n.tagName!='BODY'){
            n.parentNode.removeChild(n);
        }
    }
};
if(window.jQuery){
	$(document).ready(function(){
		__RemoveNode(document.getElementById("loading-beforehtml"));
	});
}else{
	Ext.onReady(function(){
		__RemoveNode(document.getElementById("loading-beforehtml"));
	});
}
</script>