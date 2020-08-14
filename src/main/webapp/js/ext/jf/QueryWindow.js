Ext.namespace('uft.jf');
/**
 * 查询window
 * 实际是组装查询条件窗口的条件，设置到主Grid的查询条件中，并重新加载主Grid的数据
 * 
 * 参数列表
 * app panel对象
 * fromRefbill 是否来源于参照制单，应该保证参照制单的组件id不与默认的组件冲突
 */
uft.jf.QueryWindow= function(config){
	this.shadow=false;
	this.isBuildLeftTree=true;
	this.addImg = true; //是否加入删除小图标
	
	this.loadCondsUrl = "loadConds.json";
	this.getItemUrl = "getItem.json";
	this.getDefaultUrl = "getDefault.json";
	this.saveCondsUrl = "saveConds.json";
	if(config.fromRefbill === true){
		if(!config.targetApp){
			//如果不存在targetApp，则使用当前window，如在ulw页面中不需要传入该对象
			config.targetApp = window;
		}
//		uft.Utils.assert(config.targetApp, "目标单据对象不能为空！");
		//参照制单的查询模板
		this.loadCondsUrl = "loadRefbillConds.json";
		this.getItemUrl = "getRefbillItem.json";
		this.getDefaultUrl = "getRefbillDefault.json";
		this.refbillUrl = "refbill.html";
	}
	Ext.apply(this, config);//可以覆盖以上参数
	this.modal = (config.modal == undefined)?true:config.modal; //默认使用模态窗口
	this.width = (config.width == undefined)?600:config.width;
	this.height = (config.height == undefined)?400:config.height;
	
	this.dataUrl = this.loadCondsUrl+'?funCode=' + this.funCode + '&nodeKey=' + this.nodeKey+"&billType="+this.billType+"&ulw="+this.ulw;
	
	//此处使用同步请求查询模板的数据，因为在构造该页面的时候需要使用模板数据
	//用于构造查询窗口的左边树
	this.conds = Utils.doSyncRequest(this.dataUrl);
	if(this.conds==undefined ||this.conds.datas==undefined){
		//没有返回正确数据时
//		uft.Utils.showErrorMsg('查询条件数据不正确！');
		return;
	}
	
	//删除组件的图标
	this.imgPath = resourceCtxPath+'/theme/'+Constants.csstheme+'/images/default/btn/delete.gif';
	//组件序号
	this.compNumMap = {}; //现有条件中，组件的名称和数量的对照关系
	for(var i=0;i<this.conds.datas.length;i++){
		var obj = this.conds.datas[i];
		this.compNumMap[obj.id] = 0;
	}
	
	this.mainForm = new uft.extend.form.FormPanel({
		layout : 'form',
		border : false,
		frame : false,
		autoScroll :true,
		items : []
	});	
	
	if(this.isBuildLeftTree){
		var root = new Ext.tree.TreeNode({
			text : '选择字段',
			id :Ext.id(),
			expanded : true
		});
	}
	
	//默认组件id，需要加上前缀，comp-,del-
	//默认条件其实就是当前的条件
	this.defaultCIDs=[];
	this.defaultPKs = [];	
	for(var i=0; i<this.conds.datas.length; i++){
		var obj = this.conds.datas[i];
        if(obj.if_immobility=='Y' || obj.if_default=='Y'){
        	//默认及锁定查询条件
        	var idx = this.compNumMap[obj.id];
        	var cid = this._getComponentId(obj.field_code,idx);
			this.defaultCIDs.push(cid); //将组件加入默认组件数组
			this.defaultPKs.push(obj.id);
        	this.compNumMap[obj.id] = idx +1; //更新组件数量，这些组件将在afterRender后添加
        }
        if(this.isBuildLeftTree && obj.if_immobility!='Y'){
        	//固定条件不要加到树结构中
			var node = new Ext.tree.TreeNode({
				id : obj.id,
				code : obj.field_code,
		        text : obj.field_name,
		        leaf : true
			});
			root.appendChild(node);    
        }
	};	

	//整个查询窗口的panel对象集合
	var queryPanelItems=[];
	if(this.isBuildLeftTree){
		var fieldTree = new Ext.tree.TreePanel({
			border : false,
			lines : true,
			autoScroll : true,
			enableDD : false,
			root : root
		});
		
		fieldTree.on('dblclick', function(node){ //双击时增加条件
			if(!node.leaf){
				return;
			}
			/**
			 * 一个条件对应一个panel，
			 * 添加条件时，增加该panel
			 */
			var i;
			var currentCond = this.getCondByPK(node.id);
			var idx = this.compNumMap[node.id];
	        var cid = this._getComponentId(node.attributes['code'],idx);
	        var nodeObj = {id:node.id, code:node.attributes['code'], text:node.text};
	        var oneCondPanel=this.getOneCondPanel(currentCond,nodeObj,idx);
			this.mainForm.add(oneCondPanel);
			this.mainForm.doLayout();
			
			this.defaultPKs.push(node.id); //加入默认的条件中
			this.defaultCIDs.push(cid); 
			
			if(this.addImg){
				//绑定删除小图标的事件
				this.registerImgClickEvent(cid);
			}
			this.compNumMap[node.id] = idx + 1;//更新组件数量
		},this);	
		
		var leftPanel = new Ext.Panel({
			region : 'west',
			layout : 'fit',
			split:true,
			width:150,
			minSize:100, //可拉伸的最小宽度
			maxSize:200, //可拉伸的最大宽度		
			frame : false,
			border : true,
			items : [fieldTree]
		});
		queryPanelItems.push(leftPanel);
	}
	
	var main = new Ext.Panel({
		region : 'center',
		layout : 'fit',
		border : true,
		bodyStyle : 'padding : 0 0 0 5px',
		items : [this.mainForm]
	});
	queryPanelItems.push(main);
	
	this.queryBtn = new uft.extend.Button({
		text : '查&nbsp;&nbsp;询',
		actiontype : 'submit',
		scope : this,
		disabled : true,
		handler : this.queryHandler		
	});
	var btns = [];
	btns.push({
		xtype : 'uftbutton',
		text : '保存条件',
		tooltip : '记住当前的查询条件，下次直接使用',
		scope : this,
		handler : function() {
			this.saveConds();
		}
	},'->',this.queryBtn,{
		xtype : 'uftbutton',
		text : '重&nbsp;&nbsp;置',
		scope:this,
		handler : function(){
		    this.mainForm.getForm().items.each(function(field) {
		    	var fieldId = field.id;
		    	//只能重置最后的value域
		    	if(!field.readOnly 
		    		&& fieldId.indexOf('_field_') == -1 
		    		&& fieldId.indexOf('field_') == -1 
		    		&& fieldId.indexOf('_cond_') == -1){
		    		field.reset();
		    	}
		    });
		}
	},{
		xtype : 'uftbutton',
		text : '取&nbsp;&nbsp;消',
		scope : this,
		handler : function() {
			this.hide();
		}
	});
	
	uft.jf.QueryWindow.superclass.constructor.call(this, {
		title : this.title || '查询条件[双击增加查询条件]',
		width : this.width,
		height : this.height,
		collapsible : false,
		shim : true,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		border : false,
		modal : this.modal, 
		border : false,
		layout : 'border',
		buttonAlign : 'left',
		buttons : [btns],
		items : queryPanelItems
    });
    //生成默认查询条件
    this.genDefaultCondPanels();
};
Ext.extend(uft.jf.QueryWindow,Ext.Window, {
	getCondByPK : function(pk){
		//不需要判定conds，如果不存在已经会提示了，这里不可能不存在
		for(i=0;i<this.conds.datas.length; i++){
			if(this.conds.datas[i].id == pk){
				return this.conds.datas[i];
			}
		}
		return null;
	},
	//保存查询条件，从当前的界面上读取查询条件信息
	saveConds : function(){
		if(!this.defaultCIDs || this.defaultCIDs.length == 0 || !this.defaultPKs || this.defaultPKs.length == 0){
			uft.Utils.showErrorMsg('没有任何查询条件，不能保存！');
			return;
		}
        var params = {
        	cids:this.defaultCIDs,
        	pks:this.defaultPKs,
        	funCode:this.funCode,
        	nodeKey:this.nodeKey,
        	billType:this.billType,
        	ulw : this.ulw
        };
        //使用异步查询，提升速度
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	url : this.saveCondsUrl,
	    	method : 'GET',//默认使用get方式，防止超时
	    	isTip : true
	    }); 
	},
	//返回查询条件的条件下拉框的数据源，一个查询条件对应一个数据源
	getCondStore : function(cond){
		var operaCodes = cond.opera_code.split('@');
		var operaNames = cond.opera_name.split('@');
		var data = new Array();
		for(var j=0; j<operaCodes.length; j++){
			if(operaCodes[j].trim().length == 0)
				continue;
			var arr = new Array();
			arr[0] = operaNames[j];
			arr[1] = operaCodes[j];
			data[j] = arr;
		}
		var store = new Ext.data.SimpleStore({  
             fields : ['text', 'value'],
             data : data
        });	
        return store;
	},
	//生成查询条件的面板，一个查询条件对应一个panel
	genDefaultCondPanels : function(){
		var condPanels = new Array();
        var params = {
        	cids:this.defaultCIDs,
        	pks:this.defaultPKs,
        	funCode:this.funCode,
        	nodeKey:this.nodeKey,
        	billType:this.billType,
        	ulw : this.ulw
        };
        //使用异步查询，提升速度
	    uft.Utils.doAjax({
	    	scope : this,
	    	params : params,
	    	url : this.getDefaultUrl,
	    	method : 'GET',//默认使用get方式，防止超时
	    	isTip : false,
	    	success : this.addDefaultConds
	    });        
	},
	/**
	 * 返回默认查询条件的Panel，加入查询窗口中
	 * 回调函数
	 */
	addDefaultConds : function(values){
		if(values.append){
			//当前条件的pks，这里重新设置值，可能是从上次保存的条件中读取的
			this.defaultPKs = values.append.pks;
			this.defaultCIDs = values.append.cids;
			for(var key in this.compNumMap){
				this.compNumMap[key] = 0; //重置组件数量
			}
			//这里的cids可能并不是连续的，比如增加了2个单据号的查询条件，
			//此时再把第一个单据号条件删除，保存到后台的cid=vbillstatus_1,前台我们还是认为组建被庄家了2次,这是第一次被删除了
			for(var i=0;i<this.defaultPKs.length;i++){
				var index = this.defaultCIDs[i].lastIndexOf("_");
				var num = parseInt(this.defaultCIDs[i].substring(index+1)); //组件的最后一个数字,这个数字用来标示组件被创建了几次
				if((num+1) > this.compNumMap[this.defaultPKs[i]]){
					this.compNumMap[this.defaultPKs[i]] = num+1;
				}
			}
		}
		if(values.datas){
	         for(var i=0;i<values.datas.length;i++){
	         	var item = values.datas[i];
	         	var cid = this.defaultCIDs[i];
	         	var pk = this.defaultPKs[i];
	         	var obj = this.getCondByPK(pk);
	         	if(!obj){
	         		//条件已经不在查询模板中,就不再创建了
	         		this.defaultCIDs.remove(cid);
	         		this.defaultPKs.remove(pk);
	         		continue;
	         	}
	         	var node = {id:obj.id, code:obj.field_code, text:obj.field_name};
	         	var store = this.getCondStore(obj);
	         	var oneCondPanel = this.genOneCondPanel(item,cid,node,store);
		    	this.mainForm.add(oneCondPanel);//添加默认查询组件
	        }
	        this.mainForm.doLayout(); 
	        //注册删除图标的事件，必须在doLayout之后
	        for(var i=0;i<this.defaultCIDs.length;i++){
	        	var cid = this.defaultCIDs[i];
	        	this.registerImgClickEvent(cid);
	        }
	        
			//注册查询条件的enter事件
		    this.mainForm.getForm().items.each(function(f) { 
		    	f.on('specialkey', function(f,e){
					if(e.getKey()==13){ //将enter键转化为tab键，支持使用enter键切换field焦点
						if(Ext.isGecko){
						}else{
							//其他浏览器的实现,IE
							e.keyCode=9;
		            		e.browserEvent.keyCode=9;
						}
					} 
		        });
		    });	  
		}
		this.queryBtn.enable();
	},
	//返回一个查询条件panel
	getOneCondPanel : function(cond,node,idx){
		var store = this.getCondStore(cond);
		//用于生成value field的id
        var cid = this._getComponentId(node.code,idx);
        var params = {
			cid:cid,
			pk:cond.id,
			funCode:this.funCode,
			nodeKey:this.nodeKey,
			billType:this.billType,
			ulw : this.ulw
        };
        var obj = Utils.doSyncRequest(this.getItemUrl,params,'GET');
        //从后台传回的对象可能是个Array，此时的标识使用的是datas
        var data;
        if(obj && obj.data){
        	data = obj.data;
        }else if(obj && obj.datas){
        	data = obj.datas;
        }
		return this.genOneCondPanel(data,cid,node,store);
	},
	//private
	genOneCondPanel : function(item,cid,node,store){
		var innerItem = [];
		var readOnly = false;
		var allowBlank = true;
		if(item instanceof Array){
			for(var i=0;i<item.length;i++){
				if(item[i].readOnly===true){
					readOnly=true;
				}
				if(item[i].allowBlank===false){
					allowBlank=false;
				}
			}			
			if(item.length > 1){
				//包含多个组件,肯定只有2个组件
				var nItemAry = [];
				for(var i=0;i<item.length;i++){
					nItemAry.push({
			        	border : false,
			            columnWidth : .5,
			            layout : 'form',
			            defaults :{
			            	hideLabel : true,
			            	anchor: '97%'
			            },
			            items : [item[i]]
					});
				}
				innerItem.push({
		            layout : 'column',
		            border : false,
		            padding : '2px 0 0',
					items : nItemAry
				});
			}else{
				innerItem = item;
			}
		}else{
			innerItem.push(item);
			if(item.readOnly===true){
				readOnly=true;
			}
			if(item.allowBlank === false){
				allowBlank = false;
			}
		}
		var columnItem = [];
		var imgHtml = '<img id="del_'+cid+'" src="'+this.imgPath+'" width="16" border="0" class="h_img" title="删除" />';
		if(readOnly || !allowBlank){
			imgHtml = '<div style="width:16px">&nbsp;</div>';
		}
		if(this.addImg){
			columnItem.push({
            	border : false,
                columnWidth : .05,
                layout : 'form',
                items : [{border:false,
					   frame:false,
					   html:imgHtml}]
			});
		}
		columnItem.push({
        	border : false,
            columnWidth : .27,
            layout : 'form',
            items : [{
				xtype : 'textfield',
				id : '_field_'+cid,
				value : node.text,
				hideLabel : true,
				width : 105,
				readOnly : true
			},{
				xtype : 'textfield',
				id : 'field_'+cid,
				value : node.code,
				hideLabel : true,
				hidden : true
			}]
		});
		columnItem.push({
        	border : false,
            columnWidth : .2,
            layout : 'form',
            items : [{
            	xtype : 'localcombo',
				id : '_cond_' + cid,
				name : '_cond_' + cid,
				hiddenName : 'cond_'+cid,
				hideLabel : true,
				editable : false, 
				readOnly : readOnly, //鏄惁閿佸畾
				width : 80,
				anchor: '90%',
				store : store
			}]
		});
		columnItem.push({
        	border : false,
            columnWidth : .48,
            layout : 'form',
            defaults :{
            	hideLabel : true,
            	anchor: '98%'
            },
            items : innerItem
		});
		var oneCondPanel = new Ext.Panel({
			id : 'comp_'+cid,
			autoWidth : true,
			height : 30,
			border : false,
			frame : false,
			autoScroll : false,
			items : [{
	            layout : 'column',
	            border : false,
	            padding : '2px 0 0',
				items : [columnItem]
			}]
		});
		//设置默认的条件是第一个
		Ext.getCmp('_cond_' + cid).addListener({'afterRender':function(combo){
			if(combo.store.data.items.length>0){
				var firstValue = combo.store.data.items[0].data.value;//这个数值未免变态了
				combo.setValue(firstValue);
			}
		},scope:this});
		return oneCondPanel;
	},
	//查询事件
	queryHandler : function(){
		var mf = this.mainForm;
		var errors = mf.getErrors();
		if(errors.length==0) {
			var values = mf.getForm().getFieldValues(false);
			var params = new Array();
			//组装成[{fieldName:'appCode',condition:'=',value:'1'},{}]格式发送到后台
			for(key in values){
				var index = key.indexOf('field_');
				if(index == 0){
					key = key.substring(index + 6);
					var param = {};
					param.fieldName = values['field_'+key]; //对应condition.fieldName
					if(values['cond_'+key]==undefined||values['cond_'+key].length==0){
						continue;
					}					
					param.condition = values['cond_'+key];
					if(values[key]==undefined||values[key].length == 0){
						var preAry = ['_s_','_e_'];
						for(var i=0;i<preAry.length;i++){
							var nKey = preAry[i]+key;
							if(values[nKey] && String(values[nKey]).length>0){
								var aParam = {};
								aParam.fieldName = preAry[i]+param.fieldName;
								aParam.condition = param.condition;
								aParam.value = values[nKey];
								params.push(aParam);
							}
						}
					}else{
						param.value = values[key];
						params.push(param);
					}
				}
			}
			if(this.grid){
				//表格对象存在
				//uft.Utils.assert(this.grid, "主表格参数不能为空！");
				this.reloadGrid(Ext.encode(params));
			}
			this.hide();
			this.fireEvent('query',this,params);
		}else{
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
			return;
		}
	},
	//private 
	reloadGrid : function(params){
		if(this.grid){
			//存在表格对象，则重新加载表格的数据
			//将参数加入baseParams，这样刷新的时候才会带入这些参数
			var ds = this.grid.getStore();
			ds.baseParams[uft.jf.Constants.PUB_PARAMS] = params;
			ds.baseParams['billId'] = null;//对于联查时直接打开单据的情况，重新查询的时候不再将id作为条件
			var options={};
			options.params={};
			options.params[uft.jf.Constants.PUB_PARAMS]=params;
			options.params[ds.defaultParamNames.start]=0;//将起始记录重置为0，重新从第一页开始计算
			options.params[ds.defaultParamNames.limit]=this.grid.pageSize;
			
			ds.reload(options);
		}
	},
	//覆盖默认的close方法
	close : function(){
		this.hide();
	},
	//注册删除小图标的点击事件
	registerImgClickEvent : function(cid){
		var delCmp = Ext.get('del_'+cid);
		if(delCmp){
			delCmp.on('click',function(event){
				var targetId = event.getTarget().id;
				var compId = 'comp_'+targetId.substring(4);
				var comp = Ext.getCmp(compId);
				if(comp){
					this.mainForm.remove(comp, true); //auto destory
					this.mainForm.doLayout();
				}
				
				//将该组件从默认的组件集合中删除
				var index = this.defaultCIDs.indexOf(cid);//确定cid在集合中的位置，这个位置与pk在this.defaultPKs的位置相同
				this.defaultCIDs.remove(cid); //这里的remove方法是Ext修改Array的prototype方法
				this.defaultPKs.remove(this.defaultPKs[index]);//删除PK集合中的对应数据
			},this);
		}
	},
	/**
	 * 返回每个组件的id
	 * 对于参照制单的查询窗口，其idx都从1000开始，这样避免与单据的查询窗口的ID重复
	 * @param {} prefix
	 * 				ID前缀,该参数是必须的
	 * @param {} idx
	 * 				主键的标识号，该参数是必须的
	 */
	_getComponentId : function(prefix,idx){
		if(this.fromRefbill){
			idx = idx+1000;
		}
		return prefix+"_"+idx;
	},
	/**
	 * @deprecated
	 * @return {}
	 */
	getQueryPanel : function(){
		//该方法目前不使用了，为了效率，减少了一层panel嵌套
		return this;
	},
	/**
	 * 打开参照制单窗口,并执行返回值
	 * @param {} secondary
	 * 			是否二次参照
	 * @param {} refbillModel
	 * 			参照制单窗口的模式，目前支持两种模式：1、主子表，2、平铺
	 * @param {} srcFunCode
	 * 			源单据功能节点
	 * @param {} srcNodeKey
	 * 			源单据节点标识
	 * @param {} srcBillType
	 * 			源单据单据类型
	 * @param {} srcBodyTabnamePkFieldMap
	 * 			源单据表体pk，当使用平铺方式时需要使用	
	 * @param {} params
				查询窗口的参数
	 */
	openBillDialog : function(secondary,refbillModel,srcFunCode,srcNodeKey,srcBillType,srcBodyTabnamePkFieldMap,params){
		var url = this.refbillUrl+"?refbillModel="+refbillModel+"&funCode="+srcFunCode+"&nodeKey="+srcNodeKey+"&billType="+srcBillType;
		var args = new Array();
		if(secondary){
			//二次参照
			var cardParams = this.targetApp.getHeaderCard().getForm().getFieldValues(false);
			if(this.targetApp.footCard){
				//如果存在表尾，将表尾的数据也传入
				Ext.apply(cardParams, this.targetApp.footCard.getForm().getFieldValues(false));
			}
			var firstBodyGrid = this.targetApp.getBodyGrids()[0];//出现二次参照一般也就有且只有一个表体
			var vlastbillrowids = new Array();
			for(var i=0;i<firstBodyGrid.getStore().getCount();i++){
				var record = firstBodyGrid.getStore().getAt(i);
				vlastbillrowids.push(record.get('vlastbillrowid'));
			}
			Ext.apply(cardParams,{vlastbillrowids:vlastbillrowids}); //加入表体的识别参数
			args.push(Ext.encode(cardParams)); //目标单据的参数
		}else{
			args.push("");
		}
		args.push(srcFunCode);
		args.push(srcNodeKey);
		args.push(srcBillType);
		args.push(srcBodyTabnamePkFieldMap);
		if(this.targetApp == window){
			//从ulw页面中打开的查询窗口
			args.push(this.targetApp.getFunCode());
			args.push(this.targetApp.getNodeKey());
			args.push(this.targetApp.getBillType());
			args.push(this.targetApp.getTemplateID());
			args.push(this.targetApp.getHeaderTabCode());
		}else{
			args.push(this.targetApp.context.getFunCode());
			args.push(this.targetApp.context.getNodeKey());
			args.push(this.targetApp.context.getBillType());
			args.push(this.targetApp.context.getTemplateID());
			args.push(this.targetApp.context.getHeaderTabCode());
		}
		args.push(params);//查询窗口的参数
		var wparams = "dialogWidth:820px"
			+";dialogHeight:620px"
			+";dialogLeft:"+(window.screen.availWidth-820)/2+"px"
			+";dialogTop:"+(window.screen.availHeight-620)/2+"px"
			+";status:no;scroll:no;resizable:no;help:no;center:yes";		
		var returnValue = window.showModalDialog(url,args,wparams);
		this.targetApp.doRefbill(Ext.decode(returnValue),secondary);
	},
	afterRender : function(){
		uft.jf.QueryWindow.superclass.afterRender.call(this);
		this.el.on('paste',function(e,target){
			stopDefault(e);
			if(target && target.type == 'text'){
				var id = target.id;
				if(id.indexOf('_cond') == 0){//过滤条件的选择框
					return;
				}
				var cmp = Ext.getCmp(id);
				if(!cmp || cmp.readOnly){
					return;
				}
				if(Ext.isChrome){//针对chrome的特殊处理
					if(e && e.browserEvent && e.browserEvent.clipboardData){
						var items = e.browserEvent.clipboardData.items;
						var data = '';
						for(var i=0; i<items.length;i++){
							if(items[i].type && items[i].type == 'text/plain'){
								items[i].getAsString(function(str) {
									if(str){
										data += str;
						            	data += ',';
									}
						        });
							}
						}
						//上面是异步调用的方法，这里使用一个时间差
						setUftTimeout(function(){
							cmp.setValue(uft.Utils.escapeChar(data));
						},this,50);
					}
				}else{
					var data = uft.Utils.getClipboardData(e);
					if(data){
						// 输入框
						cmp.setValue(uft.Utils.escapeChar(data));
					}
				}
			}
		},this);
	}
});