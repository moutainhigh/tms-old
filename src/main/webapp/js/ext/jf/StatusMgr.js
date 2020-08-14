Ext.namespace('uft.jf');
/**
 * 页面状态
 * 初始化状态			OP_INIT = 0;
 * 新增状态				OP_ADD = 1;
 * 非编辑列表			OP_NOTEDIT_LIST=2;		
 * 非编辑卡片			OP_NOTEDIT_CARD=3;
 * 编辑状态				OP_EDIT=4;
 * 修订状态				OP_REVISE=5;
 * 参照制单新增-列表态	OP_REFADD_LIST:6;
 * 参照制单新增-卡片态	OP_REFADD_CARD:6;
 * @type 
 */
uft.jf.pageStatus={OP_INIT:0,OP_ADD:1,OP_NOTEDIT_LIST:2,OP_NOTEDIT_CARD:3,OP_EDIT:4,OP_REVISE:5,OP_REFADD_LIST:6,OP_REFADD_CARD:7};

/**
 * 业务状态
	int NEW = 0; // 新建
	int CONFIRM = 1; // 已确认
	int DELIVERY = 2;// 已提货
	int ARRIVAL = 3; // 已到货
	int SIGN = 4;// 已签收
	int PART_SIGN = 5; // 部分签收
	int BACK = 6;// 已回单
	int CLOSE = 6;// 关闭
 * @type 
 */
uft.jf.bizStatus={NEW:0,COMMIT:1,
//发货单状态
INV_CONFIRM:1,INV_DELIVERY:2,INV_ARRIVAL:3,INV_SIGN:4,INV_PART_SIGN:5,INV_BACK:6,INV_PART_DELIVERY:7,INV_PART_ARRIVAL:8,INV_CLOSE:9,
//运段状态
SEG_WPLAN:10,SEG_DISPATCH:11,SEG_DELIVERY:12,SEG_ARRIVAL:13,
//委托单
ENT_UNCONFIRM:0,ENT_CONFIRM:21,ENT_DELIVERY:22,ENT_ARRIVAL:23,ENT_VENT:24,
//应收明细
RD_CONFIRM:31,RD_CHECK:32,RD_PART_CAVLOAN:33,RD_CAVLOAN:34,RD_CONFIRMING:35,RD_CLOSE:36,
//应收对账
RCS_CONFIRM:51,RCS_PART_CAVLOAN:53,RCS_CAVLOAN:54,
//应付明细
PD_CONFIRM:41,PD_CHECK:42,PD_PART_CAVLOAN:43,PD_CAVLOAN:44,PD_CONFIRMING:45,PD_CLOSE:46,
//应付对账
PCS_CONFIRM:61,PCS_PART_CAVLOAN:63,PCS_CAVLOAN:64,
//异常故障
EA_NEW:0,EA_WHANDLE:71,EA_HANDLING:72,EA_HANDLED:73,EA_CLOSED:74,
//辅助工具管理
ATM_OPEN:0,ATM_APPROVE:81,ATM_GRANT:82,ATM_RETURN:83,
//入库单
INSTO_NEW:0,INSTO_PART_REC:92,INSTO_ALL_REC:94,INSTO_ADDED:96,INSTO_CLOSED:98,
//出库单
OUTSTO_NEW:0,OUTSTO_PART_PICK:102,OUTSTO_PICKED:104,OUTSTO_PART_SHIP:106,OUTSTO_SHIPED:108,OUTSTO_CLOSED:110,
AJUST_NEW:0,AJUST_CONFIRM:112,
//车队管理 ：
//用车管理
YCGL_NEW:0,YCGL_CONFIRMING:128,YCGL_CONFIRM:130,YCGL_REFUSE:132,YCGL_SEND:134,YCGL_DIS:136,YCGL_RET:138,
//报账管理
REM_NEW:0,REM_CONFIRM:116,
//路桥费管理
TOLL_NEW:0,TOLL_CONFIRM:118,
//保养管理
MAT_NEW:0,MAT_CONFIRM:120,
//保险管理
INS_NEW:0,INS_CONFIRM:122,
// 维修记录管理
REP_NEW:0,REP_CONFIRM:124,
//加油管理
REF_NEW:0,REF_CONFIRM:126,
// 年审管理
ANN_NEW:0,ANN_CONFIRM:140,
//违章管理
VIO_NEW:0,VIO_CONFIRM:142,
//司机培训管理
SJPX_NEW:0,SJPX_CONFIRM:144,
//轮胎管理
TYRENEW:0,TYRE_CONFIRM:146,

};

/**
 * 状态管理器<br>
 * 该类写的并不严密，私有成员没有隐藏，存在被直接操作的风险，以后可修改隐藏私有成员和私有方法。
 * 
 * param:传入状态管理器的所有受管状态(可选，不传则用uft.jf.status)
 */
uft.jf.StatusMgr=function(config){
	if(!config){
		config = {};
	}
	//存储与业务状态相关的监听器
	this.bizListeners = new Array();
	/**
	 * 所有页面状态(目前这个没用)
	 */
	this.pageStatus=config.pageStatus || uft.jf.pageStatus;
	/**
	 * 所有业务状态（目前这个没用）
	 */
	this.bizStatus=config.bizStatus || uft.jf.bizStatus;
	uft.Utils.assert(uft.jf.pageStatus.OP_INIT, 'uft.jf.StatusMgr初始化错误，必须存在名为OP_INIT的状态！');
	//当前页面状态
	this.currentPageStatus=uft.jf.pageStatus.OP_INIT;
	/**
	 * 在调用setPageStatus时，此时页面还并没有改成当前设置的状态，
	 * 而是要调用updateStatus后才会修改，这个变量就是用来存储页面即将跳转到状态
	 */
	this.transitionPageStatus; 
	//当前业务状态
	this.currentBizStatus=null;
	//存储所有与页面状态相关的监听器
	this.listeners=new Array();
	
	//page状态管理器更新后的callback
	this.afterUpdateCallbacks = new Array();
	this.beforeUpdateCallbacks = new Array();
	
	/**
	 * 当前页面的锚点，当状态改变时，必须修改当前的锚点，这是为了与ULW适配
	 */
	this.currentAnchor = null;
	/**
	 * 设置当前锚点
	 */
	this.setCurrentAnchor = function(anchor){
		this.currentAnchor = anchor;
	};
	this.getCurrentAnchor = function(){
		return this.currentAnchor;
	};
	//添加状态更新后的callback
	this.addAfterUpdateCallback = function(func,scope){
		var o = {};
		o.func = func;
		o.scope=scope;
		this.afterUpdateCallbacks.push(o);
	};
	//移除状态更新后的callback
	this.removeAfterUpdateCallback = function(func,scope){
		for(var i=0;i<this.afterUpdateCallbacks.length;i++){
			var o=this.afterUpdateCallbacks[i];
			if(o.func == func && o.scope == scope){
				this.afterUpdateCallbacks.splice(i,1); //删除元素
				break;
			}
		}
	};
	//添加状态更新前的callback
	this.addBeforeUpdateCallback = function(func,scope){
		var o = {};
		o.func = func;
		o.scope=scope;
		this.beforeUpdateCallbacks.push(o);
	};
	//移除状态更新前的callback
	this.removeBeforeUpdateCallback = function(func,scope){
		for(var i=0;i<this.beforeUpdateCallbacks.length;i++){
			var o=this.beforeUpdateCallbacks[i];
			if(o.func == func && o.scope == scope){
				this.beforeUpdateCallbacks.splice(i,1); //删除元素
				break;
			}
		}
	};
	this.doAfterUpdateCallback = function(){
		for(var i=0;i<this.afterUpdateCallbacks.length;i++){
			var o=this.afterUpdateCallbacks[i];
			if(o.func){
				o.func.call(o.scope);	
			}
		}
	};
	/**
	 * 请注意，这个方法是在setPageStatus时调用的
	 */
	this.doBeforeUpdateCallback = function(){
		for(var i=0;i<this.beforeUpdateCallbacks.length;i++){
			var o=this.beforeUpdateCallbacks[i];
			if(o.func){
				o.func.call(o.scope);	
			}
		}
	};
	/**
	 * 添加与页面状态相关的监听器
	 * @param target 要执行监听事件的对象
	 * @param func 监听函数
	 * @scope 函数执行的作用域
	 */
	this.addListener = function(target,func,scope){ //this.statusManager.addListener(btn,btn.onstatuschange);
		var o = {};
		o.target = target;
		o.func = func;
		o.scope = scope;
		this.listeners.push(o);
	};
	/**
	 * 增加业务状态监听器
	 */
	this.addBizListener = function(target,func,scope){ 
		var o = {};
		o.target = target;
		o.func = func;
		o.scope = scope;
		this.bizListeners.push(o);
	};

	/**
	 * @deprecated
	 */
	this.changeBizStatus = function(bizStatus){
		alert("changeBizStatus已不再使用，请修改为setBizStatus！");
	};
	/**
	 * 返回当前页面状态
	 * @deprecated
	 * @see getCurrentPageStatus
	 */
	this.getCurrentStatus = function(){
		alert("getCurrentStatus已经废弃，请用getCurrentPageStatus！");
		return this.getCurrentPageStatus();
	};
	/**
	 * 切换页面状态
	 * @deprecated 
	 */
	this.changeStatus = function(pageStatus){
		alert("changeStatus已经废弃，请用updateStatus！");
		this.currentPageStatus=pageStatus;
		this.updateStatus();
	};
	/**
	 * 设置新的页面状态
	 */
	this.setPageStatus=function(pageStatus){
		this.transitionPageStatus = pageStatus;
	};
	/**
	 * 设置新的业务状态
	 */
	this.setBizStatus=function(bizStatus){
		this.currentBizStatus = bizStatus;
	};
	/**
	 * 返回当前业务状态
	 */
	this.getCurrentBizStatus = function(){
		return this.currentBizStatus;
	};
	/**
	 * 返回当前页面状态
	 */
	this.getCurrentPageStatus = function(){
		return this.currentPageStatus;
	};
	this.getTransitionPageStatus = function(){
		return this.transitionPageStatus;
	};
	/**
	 * 更新状态
	 */
	this.updateStatus=function(){
		this.doBeforeUpdateCallback();
		this.currentPageStatus = this.transitionPageStatus;//将过渡状态设置到当前状态中
		this.doPageStatusUpdate();
		this.doBizStatusUpdate();
		this.doAfterUpdateCallback();
		
		/**
		 * 清空当前锚点，为了与ULW适配
		 */
		this.currentAnchor = null;
	};
	/**
	 * 初始化组件状态，恢复为启用和显示
	 */
	this.initBtnStatus=function(cmp){
		if(cmp.disabled){
			cmp.enable();//启用
		}
		if(cmp.hidden){
			cmp.show();//显示
		}
		if(this.isULWButton(cmp)){
			//目前ulwbutton不处理显示和隐藏
			if(cmp[0].disabled){
				cmp.enable();
			}
		}
	};
	/**
	 * 是否是轻量级的button
	 */
	this.isULWButton = function(cmp){
		return (cmp.length && cmp.length > 0 && cmp[0].type=='button');
	};
	/**
	 * 分发执行已注册的监听器
	 */
	this.doPageStatusUpdate = function(){
		for(var i=0;i<this.listeners.length;i++){
			var o=this.listeners[i];
			if(o.target instanceof Ext.Button || o.target instanceof Ext.menu.Item || this.isULWButton(o.target)){
				//若是按钮类型的组件，先启用或显示按钮
				//可能是ulw中的轻量级按钮
				this.initBtnStatus(o.target);
			}
			if(o.func){
				o.func.call(o.scope || this, o.target, this.currentPageStatus);	
			}else{
				//default
				this.defaultStatusChangeEnableHandler(o.target,this.currentPageStatus);
				this.defaultStatusChangeVisibleHandler(o.target,this.currentPageStatus);
			}
		}
	};
	/**
	 * 分发执行各个已经注册的监听器
	 */
	this.doBizStatusUpdate = function(){
		for(var i=0;i<this.bizListeners.length;i++){
			var o=this.bizListeners[i];
			if(this.isULWButton(o.target)){
				if(o.target[0].disabled){
					continue;
				}
			}
			if(o.target.disabled){
				//如果已经禁用，则跳过
				continue;
			}
			if(o.func){
				o.func.call(o.scope || this, o.target, this.currentBizStatus);	
			}else{
				this.defaultBizStatusChangeEnableHandler(o.target,this.currentBizStatus);
			}
		}
	};
	
	/**
	 * 设置页面启用状态
	 */
	this.defaultStatusChangeEnableHandler=function(cmp,status){
		if(cmp.enabledStatus){
			if(status == undefined){
				return;
			}
			if(cmp.enabledStatus=="ALL"){
				cmp.enabledStatus = [];
				var key;
				for(key in this.pageStatus){
					cmp.enabledStatus.push(this.pageStatus[key]); 
				}
			}
			if(cmp.enabledStatus.length> 0){
				var isHas=false;
				for(var i=0; i<cmp.enabledStatus.length; i++){
					if(status == cmp.enabledStatus[i]){
						isHas=true;
						break;
					}
				}
				if(!isHas){
					//如果不在状态数组中，则禁用
					cmp.disable();
				}
			}else{
				//如果enabledStatus是个空数组、或者不存在，则不启用
				cmp.disable();
			}
		}
	};
	/**
	 * 设置业务启用状态
	 */
	this.defaultBizStatusChangeEnableHandler=function(cmp,status){
		if(cmp.enabledBizStatus){
			if(status == undefined){
				cmp.disable();
				return;
			}
			if(cmp.enabledBizStatus=="ALL"){
				cmp.enabledBizStatus = [];
				var key;
				for(key in this.bizStatus){
					cmp.enabledBizStatus.push(this.bizStatus[key]);
				}
			}
			if(cmp.enabledBizStatus.length> 0){
				var isHas=false;
				for(var i=0; i<cmp.enabledBizStatus.length ; i++){
					if(status == cmp.enabledBizStatus[i]){
						isHas=true;
						break;
					}
				}
				if(!isHas){
					//如果不在状态数组中，则禁用
					cmp.disable();
				}
			}else{
				//如果enabledBizStatus是个空数组、或者不存在，则不启用
				cmp.disable();
			}
		}
	};
	/**
	 * 设置显示状态
	 */
	this.defaultStatusChangeVisibleHandler=function(cmp,status){
		if(cmp.visibleStatus){
			if(cmp.visibleStatus.length> 0){
				var isHas=false;
				for(var i=0; i<cmp.visibleStatus.length ; i++){
					if(status == cmp.visibleStatus[i]){
						isHas=true;
						break;
					}
				}
				if(!isHas){
					//如果不在状态数组中，则隐藏
					cmp.hide();
				}
			}else{
				//如果visibleStatus是个空数组、或者不存在，则不显示
				cmp.hide();
			}
		}
	};
};