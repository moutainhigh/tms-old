Ext.ns('uft.jf');
/**
 * 放在列表头的简易查询面板
 * 成员变量：
 * isDynReport是否是动态报表
 * context上下文参数
 * @param {}
 *            config
 */
uft.jf.QueryFormPanel = function(config){
	Ext.apply(this,config);
	if(!this.grid){
		uft.Utils.showErrorMsg('表格对象是必须的，否则无法执行查询！');
		return;
	}
	if(!this.queryItems){
		uft.Utils.showErrorMsg('查询项不能为空！');
		return;
	}
	//对查询项是数组的进行处理，将他们作为一个分组
	for(var i=0;i<this.queryItems.length;i++){
		var item = this.queryItems[i];
		if(Ext.isArray(item)){
			var nItemAry = [],label = item[0].fieldLabel;
			for(var j=0;j<item.length;j++){
				nItemAry.push({
		        	border : false,
		            columnWidth : 0.5,
		            layout : 'form',
		            defaults :{
		            	hideLabel : true,
		            	anchor: '99%'
		            },
		            items : [item[j]]
				});
			}
			this.queryItems[i] = {
				fieldLabel : label,
	            layout : 'column',
	            border : false,
				items : nItemAry
			}
		}
	}
	if(!this.cols){
		this.cols = 5;
	}
	this.queryItems.push({
		xtype:'buttongroup',
		frame : false,
		defaults : {
			scale : 'small'
		},
		items:[{
                text : '查询',
				scope : this,
				iconCls : 'btnQuery',
				handler : this.queryHandler
            },{
            	text : '重置',
				scope : this,
				iconCls : 'btnCancel',
				handler : this.resetHandler
            }]
	});
	var len = this.queryItems.length;
	this.dHeight = (parseInt(len/this.cols) + (len%this.cols==0?0:1))*29+5;
// this.dHeight = this.queryItems.length>4?60:35;
	uft.jf.QueryFormPanel.superclass.constructor.call(this,{
		height : this.dHeight,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:this.cols},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '97%'
				},      
				items : this.queryItems
			}]
	});
};
Ext.extend(uft.jf.QueryFormPanel,uft.extend.form.FormPanel, {
	registerSpecialKey : function(){
	    this.getForm().items.each(function(f) {
	    	f.on('specialkey', function(f,e){
				if(e.getKey()==13){ // enter键
					this.queryHandler();
				}else if(Ext.isIE || Ext.isChrome){
					if(e.getKey()==9){
						this.queryHandler();
					}
				}
	        },this);
	    },this);
	},
	resetHandler : function(){
		this.doReset();
	},
	doReset : function(){
		var items = this.getForm().items.items;
		for(var i=0;i<items.length;i++){
			if(!items[i].readOnly){//如果是固定的条件，那么不能清空
				items[i].reset();
			}
		}
	},
	queryHandler : function(){
		this.doQuery();
	},
	//返回当前查询框的查询参数
	getFormParams : function(){
		if(this.getForm().isValid()){
			var values = this.getForm().getFieldValues(false);
			var params = [];
			// 组装成[{fieldName:'appCode',condition:'=',value:'1'},{}]格式发送到后台
			for(var key in values){
				var value = values[key];
				if(!value || String(value).trim().length == 0){
					continue;
				}
				var param = {};
				param.fieldName = key;
				param.condition = '';// 不传，后面使用默认的
				param.value = value;
				params.push(param);
			}		
			return Ext.encode(params);
		}else{
			var errors = this.getErrors();
			if(errors.length!=0){
				uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
				return false;
			}
		}
	},
	doQuery : function(){
		var params = this.getFormParams();
		this.reloadGrid(params);
	},
	reloadGrid : function(params){
		if(this.isDynReport){//如果是动态报表
			var options=this.newAjaxParams();
			options[uft.jf.Constants.PUB_PARAMS]=params;
			uft.Utils.doAjax({
				scope : this,
				url : 'loadData.json',
				params : options,
				isTip : false,
		    	success : function(results){
		    		//查询成功后，抛出事件，重新绘制表格
		    		this.fireEvent('rebuild',this,results);
		    	}
			});
		}else{
			var ds = this.grid.getStore();
			ds.baseParams[uft.jf.Constants.PUB_PARAMS] = params;
			ds.baseParams['billId'] = null;// 对于联查时直接打开单据的情况，重新查询的时候不再将id作为条件
			var options={};
			options.params={};
			options.params[uft.jf.Constants.PUB_PARAMS]=params;
			options.params[ds.defaultParamNames.start]=0;// 将起始记录重置为0，重新从第一页开始计算
			options.params[ds.defaultParamNames.limit]=this.grid.pageSize;
			ds.reload(options);
		}
	},
	/**
	 * 返回定义的高度
	 * 
	 * @return {}
	 */
	getDefineHeight : function(){
		return this.dHeight;
	},
	afterRender : function(){
		uft.jf.QueryFormPanel.superclass.afterRender.call(this);
		this.el.on('paste',function(e,target){
			stopDefault(e);
			if(target && target.type == 'text'){
				var cmp = Ext.getCmp(target.id);
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
	},
	/**
	 * 重新设置查询依赖的表格
	 * @param {} grid
	 */
	setGrid : function(grid){
		this.grid = grid;
	},
	newAjaxParams : function(param){
		var params = {};
		if(this.context){
			params['templateID']=this.context.getTemplateID();
			params['funCode']=this.context.getFunCode();
			params['nodeKey']=this.context.getNodeKey();
		}
		return params;
	}
});
