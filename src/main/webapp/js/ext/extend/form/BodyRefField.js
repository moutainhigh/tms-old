Ext.namespace('uft.extend.form');
/**
 * 参照打开的窗口类型包括四种，目前只支持三种，如下：
 * 树(model:0)、表格(model:1)、左边树右边表格(model:2)
 * 
 * refName : 参照名称
 * refWindow;该参数名称与BaseRefModel.java中定义的变量同名
 * idcolname;
 * pkBilltempletB;
 * pkField;
 * codeField;
 * 
 * @param {} config
 */
uft.extend.form.BodyRefField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	refWin : null,
	isUseCache : true, //是否使用cache
	triggerClass : 'x-form-search-trigger',
	selectOnFocus : true,
	//如果isFocusAfterSubmit为true，那么showCodeOnFocus必须为true,否则都为false
	showCodeOnBlur : false,
	showCodeOnFocus : false,
	isFocusAfterSubmit:false,
	//是否多选,目前多选只用在报表查询中，自己定义参照
	//多选需要设置默认值的话只能设置整个对象，不支持只设置pk，然后根据pk去查询
	isMulti : false,
	constructor : function(config) {
		Ext.apply(this, config);
		this.refWinId = Ext.id();
		this.cache = {}; //cache对象
		this.currentObjMap = {}; //当前对象的map，<record.id，currentObj>存储,currentObj至少包括pk,name,code三个值，因为可编辑表格的一列的所有行共用一个对象
		this.extendParams = {}; //额外参数对象		
		this.leftTreeExtendParams = {}; //左树右表参照，左边树的额外参数
        this.treeOnly = config.refWindow.model=='0';
		uft.extend.form.BodyRefField.superclass.constructor.call(this);
	},
	initEvents : function(){
		uft.extend.form.BodyRefField.superclass.initEvents.call(this);
        this.keyNav = new Ext.KeyNav(this.el, {
            "down": function(e) {
                this.onTriggerClick();
            },
            scope: this,
            forceKeyDown: true
        });		
	},
    onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
		this.openRefWindow();
		//点击参照那个小按钮触发的blur事件和点击其他任何地方执行的触发事件
		this.fromTriggerClick = true;		
		//窗口打开后执行失去焦点动作，这样方便当前窗口获得焦点，否则点击一次才会失去焦点，这样会出现树需要选择两次才能选择到一个节点
		this.triggerBlur();
    },
    /**
     * 打开参照框
     */
    openRefWindow : function(config){
        //对于参照，在打开参照窗口前出发的事件
        if(this.fireEvent('beforeshow',this) !== false){
	        var refWin = Ext.getCmp(this.refWinId);
	    	if(!refWin){
	    		//解决打开参照窗口后，参照域失去焦点不能赋值的问题
	    		//1、使用window.ModalDialog可以解决
	    		//2、在EditorGrid中将grid传入gridEditor中
	    		Ext.apply(this.refWindow,{srcField:this});
	    		refWin = new uft.extend.form.RefWindow(Ext.apply(this.refWindow,{record:this.getEditorRecord()})); 
				refWin.on('submit',function(){
					var obj = refWin.getReturnValue(); //{pk:1,code:02,name:a}
					if(this.gridEditor){
						//参照处于可编辑表格中
						var grid = this.gridEditor.grid;
						if(!grid.colModel.isCellEditable(this.gridEditor.col, this.gridEditor.row)){
							return;
						}
						grid.startEditing(this.gridEditor.row, this.gridEditor.col);
					}
					this.setValueAfterSubmit(obj);
				},this);
	    	}
	    	refWin.show(this.getEditorRecord());
	    	refWin.processAfterShow(config);//打开后的一些动作，如切换activeTab，设置keyword
	    	return refWin;
        }
        return null;
    },
    /**
     * 返回参照窗口
     * @return {}
     */
    getRefWindow : function(){
    	return Ext.getCmp(this.refWinId);
    },
    getValue : function(){
    	if(this.fillinable === false){
	    	//若pk域没有值，则当前参照域的值是dirty的
	    	if(this.getIdcolnameValue()==undefined||this.getIdcolnameValue()==''){
	    		return "";
	    	}
    	}
        return uft.extend.form.BodyRefField.superclass.getValue.call(this);
    },
    setValue : function(value){
        uft.extend.form.BodyRefField.superclass.setValue.call(this, value);
        return this;
    },    
    /**
     * 设置参照的值，传入的是一个object，其中pk是必须的
     */
    setObjectValue : function(obj){
    	if(!obj){
    		return;
    	}
    	if(typeof(obj) == 'string'){
    		if(obj.trim() == '' || obj.trim() == 'null'){
    			return;
    		}
    		try{
    			obj = Ext.decode(obj);
    		}catch(e){
    			//FIXME 某些情况下，这里的obj实际上就是一个pk，这里兼容这种方式
    			var pk = obj;
    			if(this.isUseCache){
		    		obj = this.cache[pk];
		    	}
		    	if(!obj){
    				obj = this.getByPk(pk);
		    	}
    		}
    	}
    	if(Ext.isArray(obj)){
    		this.isMulti = true;
    	}
    	var r = this.getEditorRecord();
    	if(this.isMulti){
    		var aryObj = obj;
    		if(!Ext.isArray(obj)){
				aryObj = [];
				aryObj.push(obj);
    		}
    		this._orginalPk = this.getPk(); //存储原值
    		if(this.gridEditor){
    			this.currentObjMap[r.id] = aryObj;
    		}
    		this.setIdcolnameValue(this.getPk());
	    	//判断此时鼠标的焦点在哪里，如果是已经是在自身，那么强制显示code
			if(this.hasFocus){
				uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getCode());
			}else{
		    	if(this.showCodeOnBlur){
					uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getCode()||"");
					this._startValue = this.getCode();
					if(this.isMulti && this.el){
						this.el.dom.title = this.getCode(); //设置title属性
					}
				}else{
					uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getText()||"");
					this._startValue = this.getText();
					if(this.isMulti && this.el){
						this.el.dom.title = this.getText(); //设置title属性
					}
				}
			}
			if(this.isUseCache){
	    		this.cache[this.getPk()] = aryObj;
	    		this.cache[this.getCode()] = aryObj;
	    	}
    		return;
    	}
    	this._orginalPk = this.getPk(); //存储原值
    	this.currentObjMap[r.id] = obj;
    	if(this.showCodeOnBlur){
			uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getCode()||"");
		}else{
			uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getText()||"");
		}
		if(this.isUseCache){
    		this.cache[this.getPk()] = obj;
    	}
    },    
    clearValue : function(){
    	var record = this.getEditorRecord();
    	this.currentObjMap[record.id] = null;
        this.setRawValue('');
        this.setIdcolnameValue('');
        this.setValue('');
        if(record){
			//可编辑表格中清空
        	record.beginEdit();
			record.set(this.getName(),'');
			record.set(this.idcolname,'');
			record.endEdit();
        }
    },
    onFocus : function(){
    	uft.extend.form.BodyRefField.superclass.onFocus.call(this);
    	if(this.readOnly === true){
    		return;
    	}    
    	if(this.showCodeOnFocus === false){
    		return;
    	}
    	this.setValueOnFocus();
    },
    setValueOnFocus : function(){
        //当获得焦点时，显示编码
        var pk = this.getIdcolnameValue();
        if(pk==undefined || pk.trim()==''){
        	return ;
        }
        var obj;
        if(this.isUseCache){
        	obj=this.cache[pk];
        	if(obj){
        		uft.extend.form.BodyRefField.superclass.setValue.call(this, obj.code||"");
				if(this.selectOnFocus){
		            this.el.dom.select();
		        }         		
        		return;
        	}
        }
        obj=this.getByPk(pk);
        if(!obj){
        	this.clearValue();
        	return;
        }
        uft.extend.form.BodyRefField.superclass.setValue.call(this, obj.code||"");
        if(this.isUseCache){
        	if(obj)
        		this.cache[pk] = obj;
        }
        if(this.selectOnFocus){
            this.el.dom.select();
        }        
    },
    onBlur : Ext.emptyFn,
	//当鼠标失去焦点时，
    //检查该code是否改变，若改变则根据code取得pk，再发生执行公式请求    
    //**如果值没有改变，则不再需要执行设置值的动作。**!important
    setValueOnBlur : function(){
    	if(this.readOnly === true){
    		return;
    	}     	
//		var orginalPk;//上一次的pk值，用于判定值是否改变，若改变，需要触发change事件
		if(this.bTriggerBlur ===true){
			this.orginalPk = this._orginalPk;
		}else{
			this.orginalPk = this.getIdcolnameValue();
		}    	
		
		var code = this.el.dom.value; 
    	if(code==undefined || code.trim()==''){ //没有输入值,空格也不行
    		if(this.orginalPk==undefined||this.orginalPk==''){
    			this.clearValue();
    			return;
    		}
    		if(this.fireEvent('beforesetvalue','',this.orginalPk)===false){
    			return;
    		}
    		//从有值到没有值
    		this.clearValue();
    		this.fireEvent('change', this,'',this.orginalPk);
    		return;
    	}		
    	var r = this.getEditorRecord();
    	var obj; //数据对象
		//上一次的值，为了比较值是否发生改变
		var startValue = '';
		if(this.gridEditor){//该组件可能使用在其他地方，不一定是可编辑表格中
			//得到原始值
			startValue = this.gridEditor.startValue;
		}
		if(this.showCodeOnFocus !== false){
			//鼠标聚焦的时候显示code值
		}else if(code == startValue){
			//这种情况是用户选择了参照以后(还有可能复制的情况)，又将鼠标聚焦在参照上,此时参照值没有改变
			var pk = this.getIdcolnameValue();
			if(pk){
				//类似复制的情况
				if(this.isUseCache){
		    		obj = this.cache[pk];
		    	}
		    	if(!obj){
		    		//根据pk去后台读取,肯定会存在,否则这条数据怎么来的!
		    		obj=this.getByPk(pk);
		    	}
			}else{
				code = this.getCode();
			}
		}else if(!this.currentObjMap[r.id]){
			//这种情况肯定是用户自己输入code，希望返回期望值
		}
    	
    	if(!obj){
	    	if(this.isUseCache){
	    		obj = this.cache[code];
	    	} 
    	}
    	if(!obj){
    		obj=this.getByCode(code);
			if(!obj){
		    	if(this.fillinable === true){
		    		//TODO 需要注意的是，可能不触发beforesetvalue,change事件，因为是否触发是以pk来判定的
					if(!obj){
		    			obj = {pk:'',code:code,name:code};
		    			this.setIdcolnameValue(obj.pk);
		    		}
		    	}else{
					this.clearValue();
					return;
		    	}
			}    		
    	}
    	if(this.isMulti){
    		//多选
    		if(Ext.isArray(obj)){
    			this.currentObjMap[r.id] = obj;
    		}else{
    			this.currentObjMap[r.id] = [obj];
    		}
    	}else{
    		this.currentObjMap[r.id] = obj;//指向当前对象
    	}	
		if(this.isUseCache){
			this.cache[this.getCode()] = obj;
			this.cache[this.getPk()] = obj;
		}     	
    	this.currentObjMap[r.id] = obj;//指向当前对象

    	if(this.orginalPk!=this.getPk()){
    		//若值发生改变，则触发change事件
	    	if(this.bTriggerBlur !== true && this.fireEvent('beforesetvalue',this.getPk(),this.orginalPk) === false){
				return;
			}
			this.setIdcolnameValue(this.getPk());
			this.fireEvent('change', this,this.getPk(),this.orginalPk );
    	}
		
		if(!this.showCodeOnBlur){
			//是否只显示code
			var retObj = {};
			retObj[this.getName()] = this.getText();
			this.setRetObj(retObj);
		}
		this.bTriggerBlur =false;
    },
    /**
     * 继承该方法主要是为了不在父类抛出change事件
     */
    triggerBlur : function(){
        this.mimicing = false;
        this.doc.un('mousedown', this.mimicBlur, this);
        if(this.monitorTab && this.el){
            this.un('specialkey', this.checkTab, this);
        }
        this.beforeBlur();
        if(this.focusClass){
            this.el.removeClass(this.focusClass);
        }
        this.hasFocus = false;
        if(this.validationEvent !== false && (this.validateOnBlur || this.validationEvent == 'blur')){
            this.validate();
        }
//        var v = this.getValue();
//        if(String(v) !== String(this.startValue)){
//            this.fireEvent('change', this, v, this.startValue);
//        }
        this.setValueOnBlur();
        delete this.fromTriggerClick;//删除该变量，标识已经用完了
        this.fireEvent('blur', this);
        this.postBlur();
        if(this.wrap){
            this.wrap.removeClass(this.wrapFocusClass);
        }
    },    
    setValueAfterSubmit : function(obj){
    	var r = this.getEditorRecord();
    	if(this.isMulti){
    		//多选，此时obj是一个Array
    		this.bTriggerBlur =true;
    		this._orginalPk=this.getPk(); //备份原始的pk值
    		this.currentObjMap[r.id] = obj;
    		this.setIdcolnameValue(this.getPk());
	    	if(this.showCodeOnBlur){
				uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getCode()||"");
				this._startValue = this.getCode();
				if(this.isMulti && this.el){
					this.el.dom.title = this.getCode(); //设置title属性
				}
			}else{
				uft.extend.form.BodyRefField.superclass.setValue.call(this, this.getText()||"");
				this._startValue = this.getText();
				if(this.isMulti && this.el){
					this.el.dom.title = this.getText(); //设置title属性
				}
			}
	    	if(this.isUseCache){
	    		this.cache[this.getPk()] = obj;
	    		this.cache[this.getCode()] = obj;
	    	}
	    	if(!this.isFocusAfterSubmit){
				this.gridEditor.grid.stopEditing();
				this.bTriggerBlur =false;
	    	}
	    	return;
    	}    	
    	if(this.getIdcolnameValue()==obj.pk){
    		//当前已存在pk值，选择的对象就是原对象
	    	if(!this.isFocusAfterSubmit){
				this.gridEditor.grid.stopEditing();
	    	}    		
    		return;
    	}
    	//肯定是有不同的选项，必须触发onBlur
    	this.bTriggerBlur =true;       	
    	//存储上一次的值
    	this._orginalPk=this.getIdcolnameValue();
    	if(this.fireEvent('beforesetvalue',obj.pk,this._orginalPk)===false){
    		return;
    	}
    	//设置为当前对象
    	this.currentObjMap[r.id] = obj;
    	
    	this.setIdcolnameValue(obj.pk);
    	if(this.showCodeOnBlur){
    		this.setValue(obj.code);
    	}else{
    		this.setValue(obj.name);
    	}
    	
    	//设置cache
    	if(this.isUseCache){
    		this.cache[obj.pk] = obj;
    		this.cache[obj.code] = obj;
    	}
		//若是可编辑表格，此时单元格已得到焦点
    	if(!this.isFocusAfterSubmit){
			this.gridEditor.grid.stopEditing();
			this.bTriggerBlur =false;
    	}
    	return;
    },
	//返回参照所在地行数据
	getEditorRecord : function(){
		if(this.gridEditor){
			//参照处于可编辑表格中
			return this.gridEditor.record;
		}
		return null;
	},
	getRetObj : function(data){
		if(data){
			if(Ext.isArray(data)){
				var objs = [];
				for(var i=0;i<data.length;i++){
					var obj = {};
					obj['pk'] = data[i][this.pkField];
					obj['code'] = data[i][this.codeField];
					obj['name'] = data[i][this.nameField];
					objs.push(obj);
				}
				return objs;
			}else{
				data['pk'] = data[this.pkField];
				data['code'] = data[this.codeField];
				data['name'] = data[this.nameField];
				return data;
			}
		}else{
			return null;
		}		
	},
	//将执行编辑公式返回的值回填到表单或者表格中
	setRetObj : function(retObj){
		if(!retObj){
			this.clearValue();
			return;
		}
		var record = this.getEditorRecord();
		if(record){
			record.beginEdit();
			for(var key in retObj){
				//模板中的字段可能会有空格,设置值的时候先去掉前后空格
				////////这句设置值在form的情况下起作用
				if(key.trim() == this.getName()){
					//当前参照域使用该方式设置值，否则会出现偶尔没有刷新页面的情况
					this.setValue(retObj[key]);
					this.el.dom.value=retObj[key];
				}
				record.set(key.trim(),retObj[key]);
			}
			record.endEdit();
		}else{
			for(var key in retObj){
				//模板中的字段可能会有空格,设置值的时候先去掉前后空格
				//2011-06-10，同时必须使用该方法赋值，在页面上才能够通过Ext.getCmp('').getValue()取值
				//2011-06-28，出现这个问题可能是以前id出现重复引起的，现在先注释掉
				//若直接使用该方法赋值，text却不能显示出来。所以结合下面的赋值
				var c = Ext.getCmp(key.trim());
				if(c){
					c.setValue(retObj[key]);					
				}
			}
		}
	},
	/**
	 * 返回的数据格式为{pk:pk,code:code,name:name}
	 * @param {} code
	 * @return {String}
	 */
	getByCode : function(code){
		if (!code || code.trim() == ''){
			return null;
		}
		var refWin = Ext.getCmp(this.refWinId);
		if(this.treeOnly){
			var node;
			if(refWin){
				node = refWin.tree.getNodeByAttr(refWin.tree.getRootNode(),'code', code);
			}
			if(node){
				return this.getTreeRetObj(node);
			}else{
				//从数据库中查询
				if(this.getByCodeUrl && this.getByCodeUrl.length > 0 && code.trim().length>0){
					var url=this.getByCodeUrl;
					var finalParams = {code:code};
					if(this.refWindow.params){ //可能code会重复，如封存数据等，加入自定义条件
						var _params = this.refWindow.params;
						var arr = _params.split(";");
						for(var i=0;i<arr.length;i++){
							var arr1=arr[i].split("=");
							arr1[1]=uft.Utils.resolveExpression(arr1[1]);
							finalParams[arr1[0]] = arr1[1];
						}						
					}
					if(this.extendParams){
						Ext.apply(finalParams,this.extendParams);
					}
					
					var values = Utils.doSyncRequest(url,finalParams,'POST'); //使用post是为了支持中文
					return this.getRetObj(values.data);
				}
			}
		}else{
			var record,flag=false;
			if(refWin){
				var ds = refWin.grid.getStore();
				for(var i=0;i<ds.getCount();i++){
					record = ds.getAt(i);
					if(record.data[this.codeField] == code){
						flag=true;
						break;
					}
				}
			}
			if(flag){
				return this.getRetObj(record.data);
			}else{
				//从数据库中查询
				if(this.getByCodeUrl && this.getByCodeUrl.length > 0 && code.trim().length>0){
					var url=this.getByCodeUrl;
					var finalParams = {code:code};
					if(this.refWindow.params){//可能code会重复，如封存数据等，加入自定义条件
						var _params = this.refWindow.params;
						var arr = _params.split(";");
						for(var i=0;i<arr.length;i++){
							var arr1=arr[i].split("=");
							arr1[1]=uft.Utils.resolveExpression(arr1[1]);
							finalParams[arr1[0]] = arr1[1];
						}	
					}			
					if(this.extendParams){
						Ext.apply(finalParams,this.extendParams);
					}			
					var values = Utils.doSyncRequest(url,finalParams,'POST');
					var valueAry=[];
					if(values.data){
						valueAry = [this.getRetObj(values.data)];
					}else if(values.datas){
						if(this.fillinable===true){//可直接输入的模式
				    		//TODO 需要注意的是，可能不触发beforesetvalue,change事件，因为是否触发是以pk来判定的
				    		valueAry = [{pk:'',code:code,name:code}];
				    	}else if(values.datas.length == 1){
				    		//返回一条记录
				    		valueAry = [this.getRetObj(values.datas[0])];
				    	}else{
				    		valueAry = values.datas;
				    	}
					}
					if(this.fromTriggerClick || valueAry.length >1){
						this.openRefWindowAfterTriggerClick(code,valueAry);
					}else{
						if(valueAry.length > 0){
							return valueAry[0];
						}
					}					
				}
			}
		}
		return null;
	},
	/**
	 * 输入字符，点击trigger后的操作
	 * @param {} keyword
	 * @param {} datas
	 */
	openRefWindowAfterTriggerClick : function(keyword,valueAry){
		//返回多条记录
		this.immediatelyLoad = false;
		this.openRefWindow({keyword:keyword}); //打开参照窗口
		var refWin = Ext.getCmp(this.refWinId);
		if(refWin && refWin.grid){
			refWin.grid.getStore().removeAll();
			//能够返回多条记录，肯定是表格参照
			refWin.grid.addRecords(valueAry);
			refWin.grid.updateTbarInfo();
			if(refWin.grid.bottomToolbar){
				refWin.grid.bottomToolbar.updateTbarInfo({total:valueAry.length,activePage:1,pages:1});
			}
		}		
	},	
    /**
     * 根据树的节点返回参照所需要的对象
     * @param {} node
     * 			树的节点对象
     */
    getTreeRetObj : function(node){
    	var name=node.attributes['hiddenText']?node.attributes['hiddenText']:node.text;
		return {pk:node.id,code:node.attributes['code'],name:name};
    },	
	getByPk : function(pk){
		if (!pk || pk.trim() == ''){
			return null;
		}
		if(!pk.success&&pk.msg){
			//如果登录超时或者没权限
			alert(pk.msg);
			return null;
		}
		var refWin = Ext.getCmp(this.refWinId);
		if(this.treeOnly){
			var obj;
			if(refWin){
				if(this.isMulti){
					//多选的话返回数组，如果返回的是空数组，则从数据库中读取
					obj = [];
					var pkAry = pk.split(',');
					for(var i=0;i<pkAry.length;i++){
						var node = refWin.tree.getNodeById(pkAry[i]);
						obj.push(this.getTreeRetObj(node));
					}
				}else{
					var node = refWin.tree.getNodeById(pk);	
					obj = this.getTreeRetObj(node);
				}
			}
			if(obj && obj.length > 0){
				return obj;
			}else{
				//从数据库中查询
				if(this.getByPkUrl && this.getByPkUrl.length > 0 && pk.trim().length>0){
					var values = Utils.doSyncRequest(this.getByPkUrl,{pk:pk},'POST');
					return this.getRetObj(values.data);
				}
				//从数据库中查询
				if(this.getByPkUrl && this.getByPkUrl.length > 0 && pk.trim().length>0){
					//考虑到效率问题，这里使用异步调用
					Utils.request({
						url : this.getByPkUrl,
						params : {pk:pk},
						method : 'POST',
						type : true,
						scope : this,
						onSuccess : function(values){
							var json = Ext.decode(values);
							//FIXME 支持多选
							var obj = null;
							if(json.data){
								obj = this.getRetObj(json.data);
							}else if(json.datas){
								obj = this.getRetObj(json.datas);
							}
							if(obj){
					    		this.setObjectValue(obj);
					    	}							
						}
					});	
				}				
			}
		}else{
			var obj;
			if(refWin){
				var ds = refWin.grid.getStore();
				if(this.isMulti){
					obj = [];
					var pkAry = pk.split(',');
					for(var i=0;i<pkAry.length;i++){
						for(var j=0;j<ds.getCount();j++){
							var record = ds.getAt(j);
							if(record.data[this.pkField] == pkAry[i]){
								obj.push(record.data); 
								break;
							}
						}
					}
				}else{
					for(var i=0;i<ds.getCount();i++){
						var record = ds.getAt(i);
						if(record.data[this.pkField] == pk){
							obj = record.data;
							break;
						}
					}
				}
			}
			if(obj && obj.length > 0){
				return this.getRetObj(obj);
			}else{
				//从数据库中查询
				if(this.getByPkUrl && this.getByPkUrl.length > 0 && pk.trim().length>0){
					//考虑到效率问题，这里使用异步调用
					Utils.request({
						url : this.getByPkUrl,
						params : {pk:pk},
						method : 'POST',
						type : true,
						scope : this,
						onSuccess : function(values){
							var json = Ext.decode(values);
							//FIXME 支持多选
							var obj = null;
							if(json.data){
								obj = this.getRetObj(json.data);
							}else if(json.datas){
								obj = this.getRetObj(json.datas);
							}
							if(obj){
					    		this.setObjectValue(obj);
					    	}							
						}
					});	
				}
			}
		}
		return null;
	},		
	//比较两个json的值是否相等
	compareTo : function(_old,_new){
		for(key in _old){
			if(_old[key] != _new[key]){
				return false;
			}
		}
		return true;
	},
	//读取主键域的值
	getIdcolnameValue : function(){
		var record = this.getEditorRecord();
		if(record){
			//表格中的参照
			if(this.idcolname==null||this.idcolname==''){
				//若没有关键字，则使用自身作为关键字
				return record.data[this.getName()];
			}else{
				return record.data[this.idcolname];
			}
		}
	},
	//设置主键域的值
	setIdcolnameValue : function(value){
		var record = this.getEditorRecord();
		if(record){
			if(this.idcolname==null || this.idcolname==''){
				//若没有关键字，则使用自身作为关键字
				record.set(this.getName(),value);
			}else{
				record.set(this.idcolname,value);
			}			
		}
	}
	//可能需要与其他表单域关联,当其他表单域改变时，需要同时改变参照显示值
	//提供增加表格额外参数，将参数加入refWindow配置中,
	//参数的格式如：{_cond: "name like 'abc'"}
	,addExtendParams : function(newParams){
		for(key in newParams){
			this.extendParams[key] = newParams[key];
		}		
	},
	//对于左树右表的参照，可以设置左边树的参数
	addLeftTreeExtendParams : function(newParams){
		for(key in newParams){
			this.leftTreeExtendParams[key] = newParams[key];
		}		
	},
    onDestroy : function(){
        Ext.destroy(Ext.getCmp(this.refWinId),this.keyNav,this.cache,this.extendParams);
        uft.extend.form.BodyRefField.superclass.onDestroy.call(this);
    },
    /**
     * 返回参照的pk值
     */
    getPk : function(){
    	if(this.gridEditor){
	    	var currentObj = this.currentObjMap[this.getEditorRecord().id];
	    	if(currentObj){
		    	if(Ext.isArray(currentObj)){
	    			var pks = '';
	    			for(var i=0;i<currentObj.length;i++){
	    				pks += currentObj[i].pk+",";
	    			}
	    			return pks.substring(0,pks.length-1); //多个pk以逗号分隔
		    	}else{
			    	return currentObj.pk;
		    	}
	    	}
    	}
    	return this.getIdcolnameValue();
    },
    /**
     * 返回参照的code值
     */
    getCode : function(){
    	if(this.gridEditor){
	    	var currentObj = this.currentObjMap[this.getEditorRecord().id];
	    	if(currentObj){
		    	if(Ext.isArray(currentObj)){
		    			var codes = '';
		    			for(var i=0;i<currentObj.length;i++){
		    				codes += currentObj[i].code+",";
		    			}
		    			return codes.substring(0,codes.length-1); //多个pk以逗号分隔    			
		    	}else{
			    	return currentObj.code;
		    	}
	    	}
    	}
    	if(this.hasFocus){
    		return this.getValue();
    	}
    	return "";
    },
    /**
     * 返回参照的text值
     */
    getText : function(){
    	if(this.gridEditor){
    		var currentObj = this.currentObjMap[this.getEditorRecord().id];
	    	if(currentObj){
		    	if(Ext.isArray(currentObj)){
	    			var names = '';
	    			for(var i=0;i<currentObj.length;i++){
	    				names += currentObj[i].name+",";
	    			}
	    			return names.substring(0,names.length-1); //多个pk以逗号分隔    			
		    	}else{
			    	return currentObj.name;
		    	}
	    	}
    	}
    	if(this.hasFocus){
    		return "";
    	}
    	return this.getValue();
    },
    /**
     * 返回当前参照所对应的行的列值,对于树参照，columnName只能是id,code,name
     * @param {} columnName
     */
    getRefValue : function(columnName){
    	if(!columnName){
    		return null;
    	}
    	if(this.currentObjMap[this.getEditorRecord().id]){
    		return this.currentObjMap[this.getEditorRecord().id][columnName];
    	}else{
    		return null;
    	}
    }
});
Ext.reg("bodyreffield",uft.extend.form.BodyRefField);