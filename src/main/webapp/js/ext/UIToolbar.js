Ext.namespace('uft.base');
/**
 * 工具栏基类，包括工具栏按钮，
 * 之类可复用这些按钮，并重写按钮事件
 * 
 * Ext 无法使用相同的快捷键绑定不同的按钮，
 * 也就是每个按钮需要定义不同的快捷键
 * @param {} config
 */
uft.base.UIToolbar = function(config){
	this.cls="uft-toolbar-title";
	Ext.apply(this, config);
	this.btn_back = {
		variable : 'btn_back',
		text : '返回', 
		tooltip : 'Ctrl+B',
		keyBinding: {
			key : 'b',
			Ctrl : true
        },
        iconCls : 'btnBack',
		handler : this.btn_back_handler,
		enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_query = {
		variable : 'btn_query',
		text : '查询', 
		tooltip : 'F3',
		keyBinding: {
            key: Ext.EventObject.F3
        },
        iconCls : 'btnQuery',
		handler : this.btn_query_handler,
		enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_add = {
		variable : 'btn_add',
		text : '新增', 
		tooltip : 'Ctrl+N',
		keyBinding: {
            key: 'n',
            ctrl : true
        },
        iconCls : 'btnAdd',
        handler : this.btn_add_handler,
        enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};	
	this.btn_copy = {
		variable : 'btn_back',
		text: '复制',
		tooltip : 'Ctrl+Alt+C',
		keyBinding: {
            key: 'c',
            ctrl : true,
            alt : true
        },
        iconCls : 'btnCopy',
        handler : this.btn_copy_handler,
        enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_edit = {
		variable : 'btn_edit',
		text : '修改', 
		tooltip : 'Ctrl+E',
		keyBinding: {
            key: 'e',
            ctrl : true
        },
        iconCls : 'btnEdit',
		handler : this.btn_edit_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_REFADD_LIST]
		//业务状态放在BillToolbar.js中设置，它只与单据有关
	};
	this.btn_del = {
		variable : 'btn_del',
		text : '删除', 
		tooltip : 'Ctrl+D',
		keyBinding: {
            key: 'd',
            ctrl : true
        },
        iconCls : 'btnDel',
		handler : this.btn_del_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};	
	this.btn_save = {
		variable : 'btn_save',
		text : '保存', 
		tooltip : 'Ctrl+S',
		keyBinding: {
            key: 's',
            ctrl : true
        },
        iconCls : 'btnSave',
		handler : this.btn_save_handler,
		enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE,uft.jf.pageStatus.OP_REFADD_CARD]
	};
	this.btn_can = {
		variable : 'btn_can',
		text : '取消', 
		tooltip : 'Ctrl+Q',
		keyBinding: {
            key: 'q',
            ctrl : true
        },
        iconCls : 'btnCancel',
		handler : this.btn_can_handler,
		enabledStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE,uft.jf.pageStatus.OP_REFADD_LIST,uft.jf.pageStatus.OP_REFADD_CARD]
	};

	this.btn_ref = {
		variable : 'btn_ref',
		text : '刷新', 
		tooltip : 'Ctrl+R',
		keyBinding: {
            key: 'r',
            ctrl : true
        },
        iconCls : 'btnRef',
		handler : this.btn_ref_handler,
		enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_list = {
		variable : 'btn_list',
		text : '列表显示', 
		tooltip : 'Alt+Z',
		keyBinding: {
            key: 'z',
            alt : true
        },
		handler : this.btn_list_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD],
		visibleStatus:[uft.jf.pageStatus.OP_ADD,uft.jf.pageStatus.OP_EDIT,uft.jf.pageStatus.OP_REVISE,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_card = {
		variable : 'btn_card',
		text : '卡片显示', 
		tooltip : 'Alt+Z',
		keyBinding: {
            key: 'z',
            alt : true
        },
		handler : this.btn_card_handler,
		visibleStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
	};
	this.btn_attach = {
		variable : 'btn_attach',
		text : '附件管理', 
		tooltip : 'Alt+A',
		iconCls : 'btnAttach',
		keyBinding: {
            key: 'a',
            alt : true
        },
		handler : this.btn_attach_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST]
	};	
	this.btn_print = {
		variable : 'btn_print',
		text : '打印', 
		tooltip : 'Crtl+P',
		iconCls : 'btnPrint',
		keyBinding: {
            key: 'p',
            ctrl : true
        },
		handler : this.btn_print_handler,
		enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_export = {
		variable : 'btn_export',
		text : '导出', 
		tooltip : 'Ctrl+O',
		iconCls : 'btnExport',
		keyBinding: {
            key: 'o',
            ctrl : true
        },
		handler : this.btn_export_handler,
		enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
	};
	this.btn_import = {
		variable : 'btn_import',
		text : '导入', 
		tooltip : 'Ctrl+I',
		iconCls : 'btnImport',
		keyBinding: {
            key: 'i',
            ctrl : true
        },
		handler : this.btn_import_handler,
		enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_LIST]
	};	
	this.btn_sms = {
			variable : 'btn_import',
			text : '站内信', 
			iconCls : 'btnSms',
			handler : this.btn_sms_handler,
			enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD,uft.jf.pageStatus.OP_NOTEDIT_LIST]
	};	
	this.btn_prev = {
		variable : 'btn_prev',
		text : '上一条', 
		handler : this.btn_prev_handler,
		visibleStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};
	this.btn_next = {
		variable : 'btn_next',
		text : '下一条', 
		handler : this.btn_next_handler,
		visibleStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD]
	};	
	uft.base.UIToolbar.superclass.constructor.call(this,{
		autoHeight : true,
		defaults : {//这里会确保按钮的scope都属于app，但是如果改成下拉按钮，那么需要在下拉按钮中设置
			scope : this
		},
		plugins:new Ext.ux.ToolbarKeyMap(),
		items : this.getInstantiatedBtnArray()
	});
};
Ext.extend(uft.base.UIToolbar,Ext.Toolbar, {
	/**
	 * 禁用所有按钮，同时存储状态
	 */
	setDisabled : function(){
		this.storeDisabled();
		var arr = this.items.items;
		for(var i=0; i<arr.length; i++){
			arr[i].disable();
		}
	},
	storeDisabled : function(){
		var bakUp = {};
		var arr = this.items.items;
		for(var i=0; i<arr.length; i++){
			var disabled = arr[i].disabled;
			if(!disabled)
				bakUp[arr[i].id] = false;
			else
				bakUp[arr[i].id] = true;	
		}
		this.bakUp = bakUp;
	},
	reStoreDisabled : function(){
		if(!this.bakUp){
			this.storeDisabled();
		}
		var bakUp = this.bakUp;
		var arr = this.items.items;
		for(var i=0; i<arr.length; i++){
			arr[i].setDisabled(bakUp[arr[i].id]);
		}
	},
	getInstantiatedBtnArray : function(){
		var instantiatedBtnArray = [];
		var btnArray = this.getAvailableBtnArray();
		for(var i=0;i<btnArray.length;i++){
			if(btnArray[i].variable){
				var btn = new uft.extend.Button(btnArray[i]);
				instantiatedBtnArray.push(btn);
				eval('this.'+btnArray[i].variable+'=btn');
			}else{
				//可能是自定义按钮
				instantiatedBtnArray.push(btnArray[i]);
			}
		}
		delete btnArray;
		return instantiatedBtnArray;
	},
	//返回可用的工具栏按钮，该函数会过滤掉NC功能注册中注册了不可用的按钮
	/**
	 * 从功能注册中读取了按钮信息，这里根据功能注册中定义的按钮，根据按钮名称匹配到已经到toolbar的按钮，达到控制按钮显示或隐藏，以及更改按钮名称的目的
	 * @return {}
	 */
	getAvailableBtnArray :function(){
		var defineBtnArray = this.getBtnArray();
		if(this.btnArray && this.btnArray != ''){
			var array;
			if(Ext.isArray(this.btnArray)){
				array = this.btnArray;
			}else{
				array = Ext.decode(this.btnArray)
			}
			if(array.length > 0){
				//如果array中的bill_type字段有值，那么认为这个是自定义按钮，并且这个字段的值是它要调用的存储过程名字
				for(var j=0;j<array.length;j++){
					var proc = array[j].bill_type;
					if(proc && proc.length > 0){
						var text = array[j].fun_name;
						if(language && language == 'en_US'){
							text = array[j].fun_en_name;
						}
						//自定义一个按钮
						defineBtnArray.push(new uft.extend.Button({
							text : text,
							scope : this,
							handler : getHandler(array[j]),
							enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST,uft.jf.pageStatus.OP_NOTEDIT_CARD]
						}));
					}
				}
				//已经定义了禁用的按钮
				var newBtnArray = []; //最终返回的按钮集合
				for(var i=0;i<defineBtnArray.length;i++){
					var dba = defineBtnArray[i];
					if(dba.menu){//下拉菜单的情况
						var items = dba.menu.items;
						if(items && items.length > 0){
							for(var k=0;k<items.length;k++){
								var item = items[k];
								if(!item.scope){
									item.scope = this;//2015-3-14 下拉按钮的默认scope也是app
								}
								var lockedFlag = false;
								for(var j=0;j<array.length;j++){
									if(item.text == array[j].fun_name){//以菜单管理中的节点名称和按钮的text进行对应
										if(array[j].locked_flag == 'Y' || array[j].auth == 'N'){
											//锁定的按钮
											lockedFlag = true;
										}else{
											//设置新的按钮名称
											var text = array[j].help_name;
											if(language && language == 'en_US'){
												text = array[j].help_en_name;
											}
											if(text){//这里实际上是页面设置的“请求后跳转的文件”
												item.text = text;
											}
										}
										break;
									}
								}
								if(lockedFlag){//被锁定的下级菜单，那么移除
									items.remove(item);
									k--;
								}
							}
						}
						newBtnArray.push(dba);//对于下拉菜单，第一级菜单肯定需要加上
					}else{
						var lockedFlag = false;
						for(var j=0;j<array.length;j++){
							if(dba.text == array[j].fun_name){//以菜单管理中的节点名称和按钮的text进行对应
								if(array[j].locked_flag == 'Y' || array[j].auth == 'N'){
									//锁定的按钮
									lockedFlag = true;
								}else{
									var text = array[j].help_name;
									if(language && language == 'en_US'){
										text = array[j].help_en_name;
									}
									//设置新的按钮名称
									if(text){//这里实际上是页面设置的“请求后跳转的文件”
										dba.text = text;
									}
								}
								break;
							}
						}
						if(!lockedFlag){
							newBtnArray.push(dba);
						}
					}
				}
				delete defineBtnArray;
				delete array;
				delete this.btnArray;
				return newBtnArray;
			}
		}
		return defineBtnArray;
		
		function getHandler(funVO) {
		    return function () {
		    	this.customBtnHandler(funVO);
		    }
		}
	},
	/**
	 * 返回工具栏的按钮数组
	 */
	getBtnArray : function(){
	},
	btn_back_handler : function(){
		this.showMsg('btn_back_handler');
	},
	btn_query_handler : function(){
		this.showMsg('btn_query_handler');
	},
	btn_add_handler : function(){
		this.showMsg('btn_add_handler');
	},
	btn_edit_handler : function(){
		this.showMsg('btn_edit_handler');
	},
	btn_revise_handler : function(){
		this.showMsg('btn_revise_handler');
	},	
	btn_del_handler : function(){
		this.showMsg('btn_del_handler');
	},	
	btn_save_handler : function(){
		this.showMsg('btn_can_handler');
	},
	btn_can_handler : function(){ 
		this.showMsg('btn_can_handler');
	},
	btn_copy_handler : function(){ 
		this.showMsg('btn_copy_handler');
	},	
	btn_ref_handler : function(){
		window.location.reload();
	},
	btn_list_handler : function(){
		this.showMsg('btn_list_handler');
	},
	btn_card_handler : function(){
		this.showMsg('btn_card_handler');
	},
	btn_print_handler : function(){
		this.showMsg('btn_print_handler');
	},
	btn_export_handler : function(){
		this.showMsg('btn_export_handler');
	},
	btn_import_handler : function(){
		this.showMsg('btn_import_handler');
	},
	btn_attach_handler : function(){
		this.showMsg('btn_attach_handler');
	},
	btn_sms_handler : function(){
		this.showMsg('btn_sms_handler');
	},
	showMsg : function(handler){
		Ext.Msg.show({
			title : '操作提示',
			msg : '该方法['+handler+']一般需要被继承！',
			buttons : Ext.Msg.OK,
			icon : Ext.Msg.WARNING
		});
	},
	/**
	 * 自定义按钮的事件
	 */
	customBtnHandler : function(funVO){
		var app = this.app,grid = app.headerGrid;
		if(!grid){
			uft.Utils.showWarnMsg('表头的headerGrid是必须的！');
			return false;
		}
		var ids = uft.Utils.getSelectedRecordIds(grid,app.headerPkField);
		if(!ids || ids.length == 0){
			uft.Utils.showWarnMsg('请先选择记录！');
			return false;
		}
		//查询按钮是否分配了模板，如果分配了，那么打开模板窗口，否则直接请求
		uft.Utils.doAjax({
			url : ctxPath +'/common/loadCustomBtnTemplet.json',
			params : {pk_fun:funVO.pk_fun},
			scope : this,
			isTip : false,
			success : function(values){
				if(values && values.data){
					//打开窗口
					new uft.CustomBtnWindow({billId:ids,funVO:funVO,pk_templet:values.data}).show();
				}else{
					var params = app.newAjaxParams();
					params['billId'] = ids;
					params['pk_fun'] = funVO.pk_fun;
					uft.Utils.doAjax({
				    	scope : this,
				    	params : params,
				    	url : ctxPath + funVO.class_name,
				    	isTip : true,
				    	actionType:funVO.fun_name,
				    	success : function(){
				    		if(funVO.help_name){//请求后跳转的文件，作为回调函数
				    			funVO.help_name.call(app,ids);
				    		}
				    	}
				    });
				}
			}
		});
	}
});
