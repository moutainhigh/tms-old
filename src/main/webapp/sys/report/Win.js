Ext.ns('uft.report');
/**
 * 排序小窗口
 * @class uft.report.NewTabWin
 * @extends Ext.Window
 */
uft.report.OrderWin = Ext.extend(Ext.Window, {
	rightButtonWidth : 50,
	constructor : function(config){
		Ext.apply(this, config);
		var grid = this.grid;
		
		var s = grid.getStore(),count = s.getCount();
		var children = [];
		for(var i=0;i<count;i++){
			var record = s.getAt(i);
			var itemkey = record.get('itemkey');
			var text = record.get('defaultshowname');
			if(itemkey && itemkey.length > 0 && text && text.length > 0){
				children.push({id:itemkey,text:text,leaf:true});
			}
		}
		var root = {
			text : 'root',
			expended : true,
			children : children
		};
		this.tree = new Ext.tree.TreePanel({
			width : 275,
			height : 200,
			rootVisible : false,
			autoScroll: true,
			enableDD: true,
			root : root
		});		
		this.form = new uft.extend.form.FormPanel({
	        border : false,
	        autoWidth : true,
			autoHeight : true,
			frame : false,
			layout : 'table',
			layoutConfig : {
				columns : 2
			},
			items : [{
				border : false,
				layout : 'fit',
				items : [this.tree]
			},{
				border : false,
				padding : '8px 0 0 5px',
				baseCls: 'x-plain',
				xtype : 'buttongroup',
				width : 50,
				layoutConfig:{
	        		columns:1
	        	},
	        	items : [{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls:'top',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var node = this.tree.getSelectionModel().getSelectedNode();
		        		if(!node){
		        			return;
		        		}
		        		var parent = node.parentNode;
		        		var first = parent.firstChild;
		        		parent.insertBefore(node,first);
		        		this.tree.getSelectionModel().select(node);
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'up',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var node = this.tree.getSelectionModel().getSelectedNode();
		        		if(!node){
		        			return;
		        		}
		        		var parent = node.parentNode;
		        		var prev = node.previousSibling;
		        		parent.insertBefore(node,prev);
		        		this.tree.getSelectionModel().select(node);
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'down',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var node = this.tree.getSelectionModel().getSelectedNode();
		        		if(!node){
		        			return;
		        		}
		        		var parent = node.parentNode;
		        		var next = node.nextSibling;
		        		if(!next){
		        			next = parent.firstChild;
		        		}else{
		        			next = next.nextSibling;
		        		}
		        		parent.insertBefore(node,next);
		        		this.tree.getSelectionModel().select(node);
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'bottom',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var node = this.tree.getSelectionModel().getSelectedNode();
		        		if(!node){
		        			return;
		        		}
		        		var parent = node.parentNode;
		        		parent.appendChild(node);
		        		this.tree.getSelectionModel().select(node);
		        	}
		        }]
			}]
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		uft.report.OrderWin.superclass.constructor.call(this, {
			title : '项目移动重排',
			width : 350,
			height : 250,
			autoHeight : true,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.form],
			buttons : btns
		});
	},
	saveHandler : function(){
		var root = this.tree.getRootNode();
		var children = root.childNodes;
		var itemkeyAry = []; 
		for(var i=0;i<children.length;i++){
			itemkeyAry.push(children[i].id);
		}
		this.fireEvent('ok',this,itemkeyAry);
		this.close();
	}
});
/**
 * 结构调整
 * @class uft.report.AdjustWin
 * @extends Ext.Window
 */
uft.report.AdjustWin = Ext.extend(Ext.Window, {
	rightButtonWidth : 60,
	constructor : function(config){
		Ext.apply(this, config);
		var grid = this.grid;
		
		var s = grid.getStore(),count = s.getCount();
		var children = [];
		for(var i=0;i<count;i++){
			var record = s.getAt(i);
			var itemkey = record.get('itemkey');
			var text = record.get('defaultshowname');
			if(itemkey && itemkey.length > 0 && text && text.length > 0){
				children.push({id:itemkey,text:text,leaf:true});
			}
		}
		var root = {
			text : '列字段',
			expended : true,
			children : children
		};
		this.tree = new Ext.tree.TreePanel({
			width : 400,
			height : 285,
			rootVisible : false,
			autoScroll: true,
			enableDD: true,
			selModel : new Ext.tree.MultiSelectionModel(),
			root : root
		});
		this.tree.on('click',function(node,e){
			if(node.parentNode.isRoot){
				//第一级节点，可以合并，如果有子节点，那么可以拆分
				mergeBtn.enable();
				if(node.childNodes && node.childNodes.length > 0){
					splitBtn.enable();	
				}else{
					splitBtn.disable();
				}
			}else{
				//不能合并也不能拆分
				mergeBtn.disable();
				splitBtn.disable();
			}
		},this);
		var mergeBtn = new Ext.Button({
        	width : this.rightButtonWidth,
        	text : '合并',
        	cls : 'x-btn-padd',
        	disabled : true,
        	scope : this,
        	handler : function(){
        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
        		if(!nodeAry || nodeAry.length == 0 || nodeAry.length == 1){
        			uft.Utils.showWarnMsg('请至少选择两个节点！');
        			return;
        		}
        		var win = new uft.report.NewNodeWin({tree:this.tree});
        		win.on('ok',function(win,parentName){
        			//重新组织节点
        			var parentNode = new Ext.tree.TreeNode({
        				id : parentName,
        				text : parentName
        			});
        			var last = nodeAry[nodeAry.length-1];
        			var next = last.nextSibling;
        			if(next){
        				next.parentNode.insertBefore(parentNode,next);
        			}else{
        				last.parentNode.appendChild(parentNode);
        			}
        			for(var i=0;i<nodeAry.length;i++){
        				parentNode.appendChild(nodeAry[i]);
        			}
        			parentNode.expand();
        		},this);
        		win.show();
        	}
        });
        var splitBtn = new Ext.Button({
        	width : this.rightButtonWidth,
        	text : '拆分',
        	cls : 'x-btn-padd',
        	disabled : true,
        	scope : this,
        	handler : function(){
        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
        		if(!nodeAry || nodeAry.length == 0){
        			return;
        		}
        		var ds = this.grid.getStore(),count = ds.getCount();
        		for(var i=0;i<nodeAry.length;i++){
        			var parentNode = nodeAry[i].parentNode;
        			var next = nodeAry[i].nextSibling;
        			var childNodes = nodeAry[i].childNodes;
        			if(childNodes){
        				var len = childNodes.length;
        				for(var j=0;j<len;j++){//注意，这里childNodes被移动后，每次都lenth都会减小
        					var itemkey = childNodes[0].id;
		        			if(next){
		        				parentNode.insertBefore(childNodes[0],next);
		        			}else{
		        				parentNode.appendChild(childNodes[0]);
		        			}
		        			for(var k=0;k<count;k++){
	        					var r = ds.getAt(k);
	        					if(r.get('itemkey') == itemkey){
	        						r.set('options',null);//清空options的值
	        						break;
	        					}
	        				}
        				}
        			}
        			parentNode.removeChild(nodeAry[i]);
        		}
        	}	
        });
		this.form = new uft.extend.form.FormPanel({
	        border : false,
			frame : false,
			layout : 'table',
			layoutConfig : {
				columns : 2
			},
			items : [{
				border : false,
				layout : 'fit',
				items : [this.tree]
			},{
				border : false,
				padding : '8px 0 0 5px',
				baseCls: 'x-plain',
				xtype : 'buttongroup',
				width : 75,
				layoutConfig:{
	        		columns:1
	        	},
	        	items : [{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls:'top',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
		        		if(!nodeAry || nodeAry.length == 0){
		        			return;
		        		}
		        		for(var i=0;i<nodeAry.length;i++){
		        			var node = nodeAry[i];
			        		var parent = node.parentNode;
			        		var first = parent.firstChild;
			        		parent.insertBefore(node,first);
			        		this.tree.getSelectionModel().select(node);
		        		}
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'up',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
		        		if(!nodeAry || nodeAry.length == 0){
		        			return;
		        		}
		        		for(var i=0;i<nodeAry.length;i++){
		        			var node = nodeAry[i];
			        		var parent = node.parentNode;
			        		var prev = node.previousSibling;
			        		parent.insertBefore(node,prev);
			        		this.tree.getSelectionModel().select(node);
		        		}
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'down',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
		        		if(!nodeAry || nodeAry.length == 0){
		        			return;
		        		}
		        		for(var i=0;i<nodeAry.length;i++){
		        			var node = nodeAry[i];
			        		var parent = node.parentNode;
			        		var next = node.nextSibling;
			        		if(!next){
			        			next = parent.firstChild;
			        		}else{
			        			next = next.nextSibling;
			        		}
			        		parent.insertBefore(node,next);
			        		this.tree.getSelectionModel().select(node);
		        		}
		        	}
		        },{
		        	xtype : 'button',
		        	width : this.rightButtonWidth,
		        	iconCls : 'bottom',
		        	cls : 'x-btn-padd',
		        	scope : this,
		        	handler : function(){
		        		var nodeAry = this.tree.getSelectionModel().getSelectedNodes();
		        		if(!nodeAry || nodeAry.length == 0){
		        			return;
		        		}
		        		for(var i=0;i<nodeAry.length;i++){
		        			var node = nodeAry[i];
			        		var parent = node.parentNode;
			        		parent.appendChild(node);
			        		this.tree.getSelectionModel().select(node);
		        		}
		        	}
		        },mergeBtn,splitBtn]
			}]
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		uft.report.AdjustWin.superclass.constructor.call(this, {
			title : '设置树结构',
			width : 500,
			height : 350,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.form],
			buttons : btns
		});
	},
	afterRender : function(){
		uft.report.AdjustWin.superclass.afterRender.call(this);
		this.resovleOptions();
	},
	saveHandler : function(){
		var root = this.tree.getRootNode();
		root.cascade(function(node){
			var mulicolhead = this.getMulicolhead(node);
			if(mulicolhead && mulicolhead.length > 0){
				var itemkey = node.id;
				//根据关键字，定位好grid的行
				var ds = this.grid.getStore(),count = ds.getCount();
				var record,tabcode = this.grid.id,showflag,listshowflag;
				for(var i=0;i<count;i++){
					var r = ds.getAt(i);
					if(r.get('itemkey') == itemkey){
						record = r;
						break;
					}
				}
				
				var options = this.buildOptions(tabcode,mulicolhead);
				record.set('options',options);
			}				
		},this);
		this.fireEvent('ok',this);
		this.close();
	},
	/**
	 * 得到多表头的信息text
	 * 如果是叶子节点，并且其父节点不是root（经过合并列处理），那么设置该字段的options值（xml）
	 */
	getMulicolhead : function(node){
		if(!node.leaf){
			return '';
		}
		var text = '';
		node = node.parentNode;
		while(node && !node.isRoot){
			var obj = node.id;
			if(text.length > 0){
				text = obj + '_' + text;
			}else{
				text = obj;
			}
			node = node.parentNode;
		}
		return text;
	},
	/**
	 * 生成xml String，作为多表头的配置信息
	 */
	buildOptions : function(tabcode,mulicolhead){
		var str = '<root><tab code="{tabcode}" showflag="Y" listshowflag="Y" mulicolhead="{mulicolhead}" /></root>';
		str = str.replace('{tabcode}',tabcode)
				.replace('{mulicolhead}',mulicolhead);
		//特殊字符替换
		str = str.replace(/</g,'&lt;');
		str = str.replace(/>/g,'&gt;');
		return str;		
	},
	/**
	 * options存在的情况下，识别值，构建树结构
	 */
	resovleOptions : function(){
		var ds = this.grid.getStore(),count = ds.getCount();
		for(var i=0;i<count;i++){
			var r = ds.getAt(i);
			var options = r.get('options');
			var itemkey = r.get('itemkey');
			if(options && options.length > 0){
				var index = options.indexOf('mulicolhead="');
				if(index > 0){
					options = options.substring(index+13);
					index = options.indexOf('"');
					//处理父节点
					var parentNode = this.tree.getRootNode();
					var node = this.tree.getNodeById(itemkey);//当前节点，肯定存在
					var next = node.nextSibling;//下一个节点
					var mulicolhead = options.substring(0,index);
					mulicolhead = mulicolhead.split('_');
					for(var j=0;j<mulicolhead.length;j++){
						//检查是否存在节点，如果不存在，加入一个新节点
						var newNode = this.tree.getNodeById(mulicolhead[j]);
						if(!newNode){
							newNode =new Ext.tree.TreeNode({
								id : mulicolhead[j],
								text : mulicolhead[j],
								expanded : true
							}); 
							if(j == 0){//第一级的节点，如果存在next，需要插入具体的位置中
								parentNode = this.tree.getRootNode();
								if(next){
									parentNode.insertBefore(newNode,next);
								}else{
									parentNode.appendChild(newNode);
								}
		        			}else{//子节点append即可
		        				parentNode.appendChild(newNode);
		        			}
						}
						parentNode = newNode;
					}
					//将该子节点加入当前的父节点中
					parentNode.appendChild(node);
				}
			}
		}
		this.tree.doLayout();
	}
});
/**
 * 合并节点时打开的窗口
 * @class uft.report.NewNodeWin
 * @extends Ext.Window
 */
uft.report.NewNodeWin = Ext.extend(Ext.Window, {
	constructor : function(config){
		Ext.apply(this, config);
		
		var items = [];
		items.push({fieldLabel : '合并后名称',name : 'parentName',"itemCls":"uft-form-label-not-null",allowBlank:false});
		
		this.form = new uft.extend.form.FormPanel({
	        labelWidth : 80,
	        border : false,
	        autoWidth : true,
			autoHeight : true,
			frame : true,
			defaults : {
				xtype : 'textfield',
				allowBlank : false,
				anchor : '85%',
				width : 200
			},
			monitorValid : true,
			items : items
		});
		var btns = [];
		btns.push({
			xtype : 'button',
			text : '确&nbsp;&nbsp;定',
			actiontype : 'submit',
			scope : this,
			handler : this.saveHandler
		},{
			xtype : 'button',
			text : '取&nbsp;&nbsp;消',
			scope : this,
			handler : function() {
				this.close();
			}
		});		
		uft.report.NewNodeWin.superclass.constructor.call(this, {
			title : '请输入合并后的名称',
			width : 300,
			layout : 'fit',
			shim : true,
			frame : true,
			closable : true,
			draggable : true,
			border : false,
			modal : true,
			items : [this.form],
			buttons : btns
		});
	},
	saveHandler : function(){
		if(this.form.getForm().isValid()){
			var values = this.form.getForm().getFieldValues(false);
			var parentName = values['parentName'];
			var root = this.tree.getRootNode();
			root.cascade(function(node){
				var obj = node.attributes['code'];
				if(obj){
					if(obj==parentName){
						uft.Utils.showWarnMsg('节点名称已经存在，请换一个！');
						return;
					}
				}
			});
			//检查名称是否已经存在
			this.fireEvent('ok',this,parentName);
			this.close();
		}
	}
});
