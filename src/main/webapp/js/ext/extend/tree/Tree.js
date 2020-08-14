Ext.namespace('uft.extend.tree');

/**
 * 封装了一颗简单的树
 * @class uft.extend.tree.Tree
 * @extends Ext.tree.TreePanel
 */
uft.extend.tree.Tree=Ext.extend(Ext.tree.TreePanel,{
	dataUrl : '', //默认数据来源
	params : {},
	//@Deprecated,使用isRoot代替
	treeRootNodeText:'ROOT', //默认根节点名称
	rootVisible : false,
	animate:false,   //是否动画效果
	//FIXME 2011-09-28
	trackMouseOver : false, //鼠标移过的时候不使用效果，为效率上的考虑
	//过滤工具条
	isTreeFilter : false,
	isRemoteFilter : false,//是否后台过虑
	/**
	 * 当页面状态改变时触发的事件，一般的application中将该属性注册到状态管理器中
	 * @type 
	 */
	onstatuschange : null,
	
	constructor:function(config){
		Ext.apply(this, config);
		if(this.params==undefined || this.params==''){
			//保证params是一个对象
			this.params={};
		}
		var root=null;
		if(!config.root){
			root = new Ext.tree.AsyncTreeNode({
				 text : this.treeRootNodeText
			});
		}
		
		var treeLoader = new Ext.tree.TreeLoader({
			requestMethod : 'POST',//如果是远程查询，可能发送汉字，避免后台转码
			dataUrl : this.dataUrl
		});
		treeLoader.on("beforeload", function(treeLoader, node) {
			var isRoot = false;
			if(this.getRootNode().id == node.id){
				isRoot = true;
			}
			treeLoader.baseParams['isRoot']=isRoot;
			Ext.apply(treeLoader.baseParams,this.params);
	    }, this);
	    
		if(this.isTreeFilter){
			this.treeFilterField =new uft.extend.form.FilterField({
				cls : 'x-form-field-ext',
				name : '_treeFilterField',
				emptyText : '按回车过滤',
				enableKeyEvents : true
			});
			//关联键盘enter事件
			this.treeFilterField.on('render', function() {
				var map = new Ext.KeyMap(this.treeFilterField.el, [{
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : this.onTreeFilter
				}]);
				map.stopEvent = true;
			}, this);
			this.treeFilterField.on('clear',function(field){
				if(field.getValue() ==''){
					//清空了当前的值,执行下查询
					this.onTreeFilter();
				}
			},this);
			this.treeToolbar=new Ext.Toolbar({
				items : [this.treeFilterField/*,new Ext.Button({
					text : '过滤',
					iconCls:'btnZoom',
					scope : this,
					handler : this.onTreeFilter
				})*/]
			});
			//过滤器
			this.treeFilter = new Ext.tree.TreeFilter(this,{
				clearBlank : true,
				autoClear : true
			});
		}		    
	    
		var treeConfig={
			border : false,
			lines : true,
			autoScroll : true,
			enableDD : false,
			root : root,
			rootVisible:false,
			tbar : this.treeToolbar,//工具条
//			maskDisabled : false,
			contextMenu : config.contextMenu||new uft.extend.tree.ContextMenu(), //若没有传入右键菜单，则定义一个默认的右键菜单
			listeners: {
		        contextmenu: function(node, e) {
		        	var c = node.getOwnerTree().contextMenu;
		        	if(c.onlyShowAtLeaf===true&&!node.leaf){//是否只在叶子节点显示
		        		return;
		        	}
		            node.select();
		            c.contextNode = node;
		            c.showAt(e.getXY());
		        },
		        'beforeclick' : function(node, e) {
					this.selectNode = node;
				}
		    },
			loader : treeLoader
		};
		Ext.applyIf(this,treeConfig);
		uft.extend.tree.Tree.superclass.constructor.call(this,{
			listeners : this.listeners || {
				'beforeclick' : function(node, e) {
					this.selectNode = node;
				}
			}
		});
		if(!config.root){
			//如果树已经配置了根结点
			root.expand(false,/* no anim */false);
		}
	}
	//树节点的过滤
	,onTreeFilter : function(){
		var value = this.treeFilterField.getValue();
		if(this.isRemoteFilter) {
			//后台查询
			var node = this.getRootNode();
			this.loader.baseParams['TREE_QUERY_KEYWORD'] = value;
			this.loader.load(node);
			this.loader.baseParams['TREE_QUERY_KEYWORD'] = null;
		}else{
			if(value && value.trim().length>0){
				//如果value不为空，表示用户希望查询数据，此时需要循环所有节点，如果不执行expand，则隐藏的节点属于为渲染的，是懒加载的，不会被查询
				this.expandAll();
			}
		}
		this.treeFilter.filterBy(this.filterFn,this);
	}
	,filterFn : function(n){
		var re = new RegExp(Ext.escapeRe(this.treeFilterField.getValue()), 'i');
		if(!n.isLeaf()){
			//非叶子节点，此时需要检查其子节点是否符合条件，只要有一个子节点符合，则不能过滤该节点，否则才过滤掉
			var flag = re.test(n.text);
			if(!flag){
				var cs = n.childNodes;
				for(var i = 0, len = cs.length; i < len; i++) {
					var flag1 = this.filterFn(cs[i]);
					if(flag1){
						flag = flag1;
						break;
					}
				}
			}
			return flag;
		}else{
			//叶子节点，直接比较
			return re.test(n.text);
		}
	}
	/**
	 * 重新加载树
	 */
	,reload:function(){
		this.selectNode = null;
		var root=this.getRootNode();
		this.loader.load(root);
		root.expand();
	},
	/**
	 * 绑定事件,如果已经处于选中状态,不再触发
	 */
	bindEvent : function(event, func){
		this.on(event, function(node, e){
			if(this.getSelectedNode().id != node.id) {
				func.call(this);
			}
		});
	},
	/**
	 * 判断一个节点是否是根节点
	 * @param {} node
	 * @return {}
	 */
	isRoot : function(node){
		var root = this.getRootNode();
		return root.id == node.id;
	},
	
	/**
	 * 获选选中节点
	 * @return {}
	 */
	getSelectedNode : function() {
		return this.getSelectionModel().getSelectedNode();
//		return this.selectNode;
	},
	/**
	 * 设置当前所选节点
	 */
	setSelectedNode : function(newNode){
		this.selectNode = newNode;
	},
	/**
	 * 根据节点的某个属性值查询，遇到第一个节点就返回
	 */
	getNodeByAttr : function(parent,attr,attrValue){
		var findNode = null;
		parent.cascade(function(node){  
			if(node.attributes[attr]==attrValue){
				findNode = node;
			}
		});
		return findNode;
     },
     /**
      * 返回节点的编码，
      * @param {} node
      */
     getCode : function(node){
     	return node.attributes['code'];
     },
     getText : function(node){
     	if(node.attributes['hiddenText']){
     		return node.attributes['hiddenText'];
     	}else{
     		return node.text;
     	}     	
     }
});
Ext.reg('ufttree', uft.extend.tree.Tree);
