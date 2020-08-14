
function getPosition(obj) {
	var top=0;
	var left=0;
	var width=obj.offsetWidth;
	var height=obj.offsetHeight;
	while (obj.offsetParent) {
		top += obj.offsetTop;
		left += obj.offsetLeft;
		obj = obj.offsetParent;
	}
	return {"top":top,"left":left,"width":width,"height":height};
}
//验证码相关
var verifyCounter=[];

function VerifyImage(eleID,o,strSrc){
	var c=verifyCounter.length;
	this.timeout=null;
	this.ipt = null;
	if(!eleID)
		this.eleID=eleID="verifyIMG_"+c;
	if(!o)
		this.baseObj = document.body;
	else
		this.baseObj = o;
	this.od=document.createElement("div");
	with(this.od){
		style.position="absolute";
		style.backgroundColor="#93b0cc";
		style.border="solid 1px #545454";
		style.padding="3px";
		style.zIndex = 9999999;  // 防止mask挡住弹出的验证码图片
		id=eleID+"_anchor";
	}
	this.templete=('<img id="'+eleID+'" onclick="getVerify(\''+eleID+'\','+c+')" style="cursor:pointer;margin-bottom:5px" src="'+strSrc+'?sid={{_random_}}" alt="获取中..." title="点击更换" width="130" height="53" /><br /><span onclick="getVerify(\''+eleID+'\','+c+')" style="font-size:11px;color:white;cursor:pointer">看不清楚?换一个</span>');
	this.seed=Math.random();
	this.appended=false;

	verifyCounter[c]=this;
}

VerifyImage.prototype.showMe=function(x,y){
	//Browser check
	var Browser = new Object();

	Browser.ua = window.navigator.userAgent.toLowerCase();
	Browser.ie = /msie/.test(Browser.ua);
	Browser.moz = /gecko/.test(Browser.ua);
	with(this.od){
		style.top=(y+20)+"px";
		if(Browser.ie){
			style.left=(x+90)+"px";
		}else{
			style.left=(x+10)+"px";
		}
	}
	if(!this.appended){
		this.baseObj.appendChild(this.od);
		this.od.innerHTML=this.templete.replace(/\{\{_random_\}\}/g,this.seed);
		this.appended=true;
	}
	this.od.style.display="";
}

VerifyImage.prototype.killMe=function(){
	this.od.style.display="none";
}

VerifyImage.prototype.change=function(){
	var o=document.getElementById(this.eleID);
	this.seed=Math.random();
	o.src=o.src+"?sid="+this.seed;
}

//获取验证码
function getVerify(s,n){
	clearTimeout(verifyCounter[n].timeout);
	verifyCounter[n].change();
	if(verifyCounter[n].ipt){
		verifyCounter[n].ipt.value="";
		verifyCounter[n].ipt.focus();
	}
}

//input focus获取验证码
function focusGetVerify(o,strSrc){
	var pos=getPosition(o);
	var e=(verifyCounter.length>0)?(verifyCounter[0]):(new VerifyImage(false,document.getElementById('mbody'),strSrc));
	e.ipt = o;
	e.showMe(pos.left,pos.top);
	o.value="";
}
function freshVerify(){
	if(!verifyCounter[0])
		return;
	else
		verifyCounter[0].change();
}
//干掉验证码
function hiddenVerify(isImme){
	if(!verifyCounter[0])
		return;
	if(!isImme)
		verifyCounter[0].timeout=setTimeout(function(){verifyCounter[0].killMe()},200);
	else
		verifyCounter[0].killMe();
}
