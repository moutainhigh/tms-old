/**
 * 带checkbox的树
 * 该文件引入了第三方的CheckNodeUI,引用该文件需要同时引入TreeCheckNodeUI.js
 */
Ext.namespace('uft.extend.tree'); 
uft.extend.tree.CheckboxTree=Ext.extend(Ext.tree.TreePanel,{
	dataUrl : '', //默认数据来源
	params : null,
	//@Deprecated,使用isRoot代替
	treeRootNodeId:'__root',       
	treeRootNodeText:'ROOT', //默认根节点名称
	rootVisible : false,  //是否显示根节点
	animate:false,  //是否动画效果
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
		var root=null;
		if(!config.root){
			root = new Ext.tree.AsyncTreeNode({
				text : this.treeRootNodeText
//				,id :this.treeRootNodeId
			});
		}
		
		var treeLoader = new Ext.tree.TreeLoader({
			dataUrl : this.dataUrl,
			baseAttrs: { uiProvider: Ext.ux.TreeCheckNodeUI } //添加 uiProvider 属性
		});
		treeLoader.on("beforeload", function(treeLoader, node) {
			var isRoot = false;
			if(this.getRootNode().id == node.id)
				isRoot = true;
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
			checkModel: config.checkModel || 'childCascade',   //对树的级联多选
	        onlyLeafCheckable: false,//对树所有结点都可选
			lines : true,
			autoScroll : true,
			enableDD : false,
			root : root,
			rootVisible:false,
			tbar : this.treeToolbar,//工具条
			selModel : new Ext.tree.MultiSelectionModel(), //多选模式
			contextMenu : config.contextMenu||new uft.extend.tree.ContextMenu(), //若没有传入右键菜单，则定义一个默认的右键菜单
			listeners: {
		        contextmenu: function(node, e) {
		            node.select();
		            var c = node.getOwnerTree().contextMenu;
		            c.contextNode = node;
		            c.showAt(e.getXY());
		        },
				click : function(node,e){
					node.getUI().check(!node.getUI().checkbox.checked);
				}
		    },			
			loader : treeLoader
		};
		Ext.applyIf(this,treeConfig);
		
		uft.extend.tree.CheckboxTree.superclass.constructor.call(this);
		if(!config.root){
			//如果树已经配置了根结点
			root.expand(false,/* no anim */false);
		}
	}
	//树节点的过滤
	,onTreeFilter : function(){
		var value = this.treeFilterField.getValue();
		if(this.isRemoteFilter){
			//后台查询
			var node = this.getRootNode();
			this.loader.baseParams['TREE_QUERY_KEYWORD'] = value;
			this.loader.load(node);
			this.loader.baseParams['TREE_QUERY_KEYWORD'] = null;
		} else {
			if(value && value.trim().length>0){
				//如果value不为空，表示用户希望查询数据，此时需要循环所有节点，如果不执行expand，则隐藏的节点属于为渲染的，是懒加载的，不会被查询
				this.expandAll();
			}
			this.treeFilter.filterBy(this.filterFn,this);
		}
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
	,reload:function(){
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
	 * 判断按钮是否是根节点
	 * @param {} node
	 * @return {}
	 */
	isRoot : function(node){
		var root = this.getRootNode();
		return root.id == node.id;
	},	
	/**
	 * 返回所选中的节点，数组中是选择的节点对象
	 * @param {} node
	 * 			父节点对象
	 */
	getCheckedNodes : function(node){
		return this.getChecked(node,1);
	},
	/**
	 * 返回所选中的节点，数组中是选择的节点的id
	 * @param {} node
	 * 			父节点对象
	 */
	getCheckedNodesId : function(node){
		return this.getChecked(node,0);
	},
	/**
	 * 返回所有选中的节点
	 * @param {} node
	 * 				父节点对象
	 * @param {} storeMedia 
	 * 				存储介质，默认是存储id，
	 * @return {}
	 */
    getChecked : function(node, storeMedia){
		var checked = [], i;
		var root = false;
		if( typeof node == 'undefined' ) {
			node = this.getRootNode();
			root = true;
		}
		
		if( node.checked || (node.attributes && node.attributes.checked)) { //叶子节点可能只有checked属性，而没有attributes属性
			if (!root){
				if(storeMedia&&storeMedia==1){
					//存储整个node对象
					checked.push(node);
				}else{
					//只存储id
					checked.push(node.id);
				}
			}
		}
		//另人很无语的东西，后台返回的ExtJsonTree对象是相同的，
		//Ext在build tree 的时候属性确大不相同。
		if(node.childNodes && node.childNodes.length > 0){
			for( i = 0; i < node.childNodes.length; i++ ) {                    //树初始化的root节点没有attributes属性
				checked = checked.concat( this.getChecked(node.childNodes[i],storeMedia) );
			}
		}
		else if(node.children && node.children.length > 0){                            //是否存在children属性
			for( i = 0; i < node.children.length; i++ ) {
				checked = checked.concat( this.getChecked(node.children[i],storeMedia));
			}
		}
		else if(node.attributes && node.attributes.children && node.attributes.children.length > 0){  //是否存在children属性
			for( i = 0; i < node.attributes.children.length; i++ ) {
				checked = checked.concat( this.getChecked(node.attributes.children[i],storeMedia));
			}
		}
		return checked;
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
     },
    /**
     * 取消选择所有节点
     * 这个方法放在这边有点奇怪，本来应该是this.getUI().uncheckAll,但是此时的getUI()返回的是一个节点的UI
     */
    uncheckAll : function(parent){
    	if(!parent){
    		parent = this.getRootNode();
    	}
		parent.cascade(function(node){
			if(node && node.getUI() instanceof Ext.ux.TreeCheckNodeUI){
				node.getUI().check(false);
			}
		});
    }     
});
Ext.reg('checkboxtree', uft.extend.tree.CheckboxTree);
