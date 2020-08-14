Ext.ns('uft.jf');
/**
 * 左边树，右边表单的Panel的Toolbar，这中panel一般用来编辑树
 * @class uft.jf.TreeFormToolbar
 * @extends uft.base.UIToolbar
 */
uft.jf.TreeFormToolbar = Ext.extend(uft.base.UIToolbar, {
	app : null,
	constructor : function (config){ 
		Ext.apply(this, config);
		uft.jf.TreeFormToolbar.superclass.constructor.call(this);
	},
	getBtnArray : function(){
		var btns = new Array();
		btns.push(this.btn_add);
		btns.push(this.btn_edit);
		btns.push(this.btn_del);
		btns.push(this.btn_save);
		btns.push(this.btn_can);
		btns.push(this.btn_ref);
		return btns;
	},
	btn_add_handler : function() {
		var parentNode = this.app.leftTree.getSelectedNode();
		if(parentNode){
			if(this.fireEvent('beforeadd',this,parentNode.id) !==false){
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
				this.app.statusMgr.updateStatus();	
				var params = this.app.headerCard.getForm().getFieldValues(false);
				var _params=this.app.newAjaxParams();
				Ext.apply(params,_params);
				//检测缓存是否存在
				if(!this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW)){
					//加载默认值
					this.app.loadDefaultValue();
				}
				var appBufferData = this.app.cacheMgr.getEntity(uft.jf.Constants.PK_NEW);
				if(appBufferData){
					if(this.app.leftTree.getRootNode().id != parentNode.id){
						//不是根节点,则加入父级节点参数
						appBufferData.HEADER[this.app.getTreeParentPkField()] = parentNode.id;
					}else{
						delete appBufferData.HEADER[this.app.getTreeParentPkField()];
					}
					//这里调用setAppValues和setCardValues效果相同，因为肯定没有表体
					this.app.setCardValues(appBufferData.HEADER);
				}
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_ADD);
				this.app.statusMgr.updateStatus();
				this.fireEvent('add',this,appBufferData.HEADER);
			}
		}else{
			uft.Utils.showWarnMsg('请先选中父节点！');
		}
	},
	/**
	 * 修改按钮操作
	 * @Override
	 */
	btn_edit_handler : function() {
		var node = this.app.leftTree.getSelectedNode();
		if(!node){
			uft.Utils.showWarnMsg('请先选中要修改的节点！');
		}else{
			if(this.fireEvent('beforeedit',this,node.id) !==false){
				this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_EDIT);
				this.app.statusMgr.updateStatus();
				this.fireEvent('edit',this,node.id)
			}
		}
	},
	/**
	 * 保存可能是增加或修改,说传参数的数据结构与单据相同，这样后台可以复用json转aggVO方法
	 * @Override
	 */
	btn_save_handler : function() {
		var errors = this.app.headerCard.getErrors();
		if(errors.length==0) {
			var appPostData =  {};
			var params=this.app.newAjaxParams();
			var values = this.app.headerCard.getForm().getFieldValues(false);
			//date类型做特殊处理,此时的date已经是类似'yyyy-mm-dd HH:mm:ss'的格式了,参见DateField.js的定义
			for(key in values){
				if(values[key] instanceof Date){
					values[key] = values[key].dateFormat('Y-m-d');
				}
			}	
			//加入主表数据
			appPostData[uft.jf.Constants.HEADER] = values;
			params[uft.jf.Constants.APP_POST_DATA] = Ext.encode(appPostData);
			if(this.fireEvent('beforesave',this,params)){//抛出beforesave事件，可以通过该事件增加参数等
				uft.Utils.doAjax({
					scope : this,
					params : params,
					url : 'save.json',
					method : 'POST',
					success : function(values) {
						if(values.data){
							//从缓存中移出
							this.app.cacheMgr.removeEntity(values.data.HEADER[this.app.headerPkField]);
							this.app.setCardValues(values.data.HEADER);//设置表头和表尾数据
							var node = this.app.leftTree.getSelectedNode();
							if(this.app.statusMgr.getCurrentPageStatus() == uft.jf.pageStatus.OP_ADD){
								//展开当前节点
								node.expand();
								//增加树节点
								var newNode = new Ext.tree.TreeNode({
									id : values.data._id,
									text : values.data._text,
									code : values.data._code
								});
								node.leaf = false;//设置为非叶子节点，在判定hasChildNodes时，竟然还根据是否leaf来判断，参见Tree.js,341行
								node.appendChild(newNode);
								//选择新增加的节点
								this.app.leftTree.getSelectionModel().select(newNode);
							}else{
								//修改树节点
								node.setText(values.data._text);
								node.attributes['code'] = values.data._code;
							}
						}
						this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
						this.app.statusMgr.updateStatus();
						this.fireEvent('save',this,values.data,values);
					}
				});
			}
		}else{
			uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
		}
	},
	/**
	 * 删除按钮操作
	 * @Override
	 */
	btn_del_handler : function() {
		var node = this.app.leftTree.getSelectedNode();
		if(!node){
			uft.Utils.showWarnMsg('请先选中要删除的节点！');
		}else{
			if(this.fireEvent('beforedel',this,node.id) !== false){
				Ext.Msg.confirm(uft.Internal.getText('确认'), uft.Internal.getText('你确认要删除这条记录吗？'), function(btn) {
					if (btn == 'yes') {
						var params=this.app.newAjaxParams();
						var node=this.app.leftTree.getSelectedNode();
						params[this.app.getBillIdField()]=node.id;
						uft.Utils.doAjax({
							scope : this,
							params : params,
							url : 'delete.json',
							success : function(values) {
								node.parentNode.removeChild(node);
								this.app.resetCardValues();
								this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
								this.app.statusMgr.updateStatus();
								this.fireEvent('del',this,node.id,values);//删除后事件
							}
						});
					}
				}, this);
			}
		}
	},
	/**
	 * 取消按钮操作
	 * @Override
	 */	
	btn_can_handler : function() {
		this.app.statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
		this.app.statusMgr.updateStatus();
		var node = this.app.leftTree.getSelectedNode();
		if(node){
			if(this.fireEvent('beforecan',this,node.id) !== false){
				var cardValue = this.app.cacheMgr.getEntity(node.id);
				if(cardValue){
					this.app.setCardValues(cardValue.HEADER);
				}else{
					//对于无实际意义的节点，点击节点时数据返回null，也不加入缓存
					this.app.setCardValues(null);
				}
				this.fireEvent('can',this,cardValue);
			}
		}
		
	}
});