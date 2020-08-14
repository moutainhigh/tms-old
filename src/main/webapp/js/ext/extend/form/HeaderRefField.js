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
uft.extend.form.HeaderRefField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	refWin : null,
	isUseCache : true, //是否使用cache
	triggerClass : 'x-form-search-trigger',
	selectOnFocus : true,
	//如果isFocusAfterSubmit为true，那么showCodeOnFocus必须为true，否则都为false
	showCodeOnBlur : false,
	showCodeOnFocus : false,
	isFocusAfterSubmit:true, 
	//是否多选,目前多选只用在报表查询中，自己定义参照
	//多选需要设置默认值的话只能设置整个对象，不支持只设置pk，然后根据pk去查询
	isMulti : false,
	constructor : function(config) {
		Ext.apply(this, config);
		this.refWinId = Ext.id();
		this.cache = {}; //cache对象
		this.currentObj = null;//当前参照的值对象，针对单选，包括pk,code,name值
		this.extendParams = {}; //额外参数对象		
		this.leftTreeExtendParams = {}; //左树右表参照，左边树的额外参数
        this.treeOnly = config.refWindow.model=='0';
        
        //多选不需要录入
        this.currentObjs = null; //当前参照的值对象,针对多选，是个数组，每个数组对象包括pk,code,name值
		uft.extend.form.HeaderRefField.superclass.constructor.call(this);
		
	},
	initEvents : function(){
		uft.extend.form.HeaderRefField.superclass.initEvents.call(this);
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
     * 打开参照窗口,配置信息包括：
     * keyword:查询的初始化关键字
     */
    openRefWindow : function(config){
        //对于参照，在打开参照窗口前出发的事件
        if(this.fireEvent('beforeshow',this) !== false){
	        var refWin = Ext.getCmp(this.refWinId);
	    	if(!refWin){
	    		Ext.applyIf(this.refWindow,{srcField:this});
	    		refWin = new uft.extend.form.RefWindow(this.refWindow); 
				refWin.on('submit',function(){
					if(this.readOnly)//防止在不能编辑状态下打开的参照窗口设置值
						return;
					var obj = refWin.getReturnValue(); //{pk:1,code:02,name:a}
					this.setValueAfterSubmit(obj);					
				},this);
	    	}
	    	refWin.show();
	    	refWin.processAfterShow(config);//打开后的一些动作，如切换activeTab，设置keyword
	    	return refWin;
        }
        return null;
    },
    /**
     * 如果组件在render之前已经调用过setObjectValue,此时已经是有值的，
     * 此时this.value是name值，如果再调用setValue就错了
     */
    initValue : function(){
        if(this.value !== undefined){
        	if(this.currentObj){
        		uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.value);
        	}else{
        		this.setValue(this.value);
        	}
        }else if(!Ext.isEmpty(this.el.dom.value) && this.el.dom.value != this.emptyText){
        	if(this.currentObj){
        		uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.el.dom.value);
        	}else{
        		this.setValue(this.el.dom.value);
        	}
        }
        /**
         * The original value of the field as configured in the {@link #value} configuration, or
         * as loaded by the last form load operation if the form's {@link Ext.form.BasicForm#trackResetOnLoad trackResetOnLoad}
         * setting is <code>true</code>.
         * @type mixed
         * @property originalValue
         */
        this.originalValue = this.getValue();
        //FIXME 定义input框的maxLength，让浏览器自己去处理，Ext默认使用提示信息
		if (!isNaN(this.maxLength) && (this.maxLength *1) > 0 && (this.maxLength != Number.MAX_VALUE)) {
	        this.el.dom.maxLength = this.maxLength *1;
	    }  
    },    
    getValue : function(){
		if(this.returnType == 0){
    		return this.getCode();
    	}else if(this.returnType == 1){
    		return this.getText();
    	}
    	var pk = this.getPk();
    	if(!pk || pk == ''){
    		if(this.currentObj){
    			return this.currentObj.code;
    		}
    	}
    	return pk;
    },
    /**
     * 设置的是pk值，这里根据pk值返回包括pk/code/name的对象
     * 多选的情况,这里的pk是一个数组，并且数组中的每个对象都必须包括pk,code,name等值
     * @param {} value
     */
    setValue : function(pk){
    	//以{开头,并以}结尾的，认为是对象
    	if(pk && (typeof(pk) != 'string' 
    		|| (pk.indexOf('{') == 0 && pk.indexOf('}')==pk.length-1) 
    		|| (pk.indexOf('[') == 0 && pk.indexOf(']')==pk.length-1))){
    		this.setObjectValue(pk);	
    		return this;
    	}
    	if(!pk || pk.trim() == '' || pk.trim() == 'null'){
    		this.clearValue();
    		return;
    	}
    	var obj;
    	if(this.isUseCache){
    		obj = this.cache[pk];
    	}
    	if(!obj){
    		//FIXME 需要根据pk去数据库读取整个参照对象的情况，先设置pk进去，name，code等查询完返回后再设置
    		this.setObjectValue({pk:pk,name:'',code:''});
    		obj = this.getByPk(pk);
    	}
    	if(obj){
    		this.setObjectValue(obj);
    	}
    	return this;
    },
    /**
     * 设置参照的值，传入的是一个object，其中pk是必须的
     * 多选的情况obj是一个数组
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
    	if(this.isMulti){
    		var aryObj = obj;
    		if(!Ext.isArray(obj)){
				aryObj = [];
				aryObj.push(obj);
    		}
    		this._orginalPk = this.getPk(); //存储原值
    		this.currentObjs = aryObj;
	    	//判断此时鼠标的焦点在哪里，如果是已经是在自身，那么强制显示code
			if(this.hasFocus){
				uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getCode());
			}else{
		    	if(this.showCodeOnBlur){
					uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getCode()||"");
					this._startValue = this.getCode();
					if(this.isMulti && this.el){
						this.el.dom.title = this.getCode(); //设置title属性
					}
				}else{
					uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getText()||"");
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
    	if(!obj || !obj.pk){
    		return;
    	}
    	this._orginalPk = this.getPk(); //存储原值
    	this.currentObj = obj;
    	//判断此时鼠标的焦点在哪里，如果是已经是在自身，那么强制显示code
		if(this.hasFocus){
			uft.extend.form.HeaderRefField.superclass.setValue.call(this, obj.code);
		}else{
	    	if(this.showCodeOnBlur){
				uft.extend.form.HeaderRefField.superclass.setValue.call(this, obj.code||"");
				this._startValue = obj.code;
			}else{
				uft.extend.form.HeaderRefField.superclass.setValue.call(this, obj.name||"");
				this._startValue = obj.name;
			}
		}
		if(this.isUseCache){
    		this.cache[obj.pk] = obj;
    		this.cache[obj.code] = obj;
    	}
    	return this;
    },
    clearValue : function(){
    	if(this.isMulti){
    		this.currentObjs = null;
    	}else{
    		this.currentObj = null;
    	}
        this.setRawValue('');
        uft.extend.form.HeaderRefField.superclass.setValue.call(this, '');
        if(this.isMulti && this.el){
        	this.el.dom.title = '';
        }
    },
    onFocus : function(){
		//上一次的值，为了比较值是否发生改变，不能直接使用getValue,因为被重写了，
		//不能使用uft.extend.form.HeaderRefField.superclass.getValue.call(this); 因为得到的始终是当前值
    	this._startValue = this.el.dom.value;
    	uft.extend.form.HeaderRefField.superclass.onFocus.call(this);
    	if(this.readOnly === true){
    		return;
    	}    
    	if(this.showCodeOnFocus === false){
    		return;
    	}
        //当获得焦点时，显示编码
        if(!this.currentObj || this.currentObj.pk==undefined || this.currentObj.pk.trim()==''){
        	return ;
        }
        uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.currentObj.code||"");
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
    	
		var orginalPk;//上一次的pk值，用于判定值是否改变，若改变，需要触发change事件
		if(this.bTriggerBlur ===true){
			orginalPk = this._orginalPk;
		}else{
			orginalPk = this.getPk();
		}
		
		var code = this.el.dom.value;
    	if(code==undefined || code.trim()==''){ //没有输入值,空格也不行
    		if(orginalPk==undefined||orginalPk==''){
    			this.clearValue();
    			return;
    		}
    		if(this.fireEvent('beforesetvalue','',orginalPk)===false){
    			return;
    		}
    		//从有值到没有值
    		this.clearValue();
    		this.fireEvent('change', this,'',orginalPk);
    		return;
    	}	
    	
		if(this.showCodeOnFocus !== false){
			//鼠标聚焦的时候显示code值
		}else if(code == this._startValue){
			//数值没有改变，只是获得焦点而已
			code = this.getCode();
		}else if(!this.currentObj){
			//这种情况肯定是用户自己输入code，希望返回期望值
		}
    	
    	var obj;
    	if(this.isUseCache){
    		obj = this.cache[code];
    	}
    	
    	if(!obj){
    		obj=this.getByCode(code);
			if(!obj){
		    	if(this.fillinable===true){
		    		//TODO 需要注意的是，可能不触发beforesetvalue,change事件，因为是否触发是以pk来判定的
					if(!obj){
		    			obj = {pk:'',code:code,name:code};
		    		}
		    	}else{
					this.clearValue();
					return;
		    	}
			}     		
    		if(this.isUseCache){
    			this.cache[code] = obj;
    		}
    	}
    	if(this.isMulti){
    		//多选
    		if(Ext.isArray(obj)){
    			this.currentObjs = obj;
    		}else{
    			this.currentObjs = [obj];
    		}
    	}else{
    		this.currentObj = obj;//指向当前对象
    	}
		if(orginalPk!=this.getPk()){
    		//若值发生改变，则触发change事件
	    	if(this.bTriggerBlur!==true&&this.fireEvent('beforesetvalue',this.getPk(),orginalPk)===false){
				return;
			}
			this.fireEvent('change', this,this.getPk(),orginalPk );
    	}
    	if(!this.showCodeOnBlur){
			uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getText());
			this._startValue = this.getText();
			if(this.isMulti && this.el){
				this.el.dom.title = this.getText(); //设置title属性
			}
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
        this.setValueOnBlur();
        delete this.fromTriggerClick;//删除该变量，标识已经用完了
        this.fireEvent('blur', this);
        this.postBlur();
        if(this.wrap){
            this.wrap.removeClass(this.wrapFocusClass);
        }
    },    
    setValueAfterSubmit : function(obj){
    	if(this.fireEvent('beforesetvalueaftersubmit',this,obj) !== false){
	    	if(this.isMulti){
	    		//多选，此时obj是一个Array
	    		this.bTriggerBlur =true;
	    		this._orginalPk=this.getPk(); //备份原始的pk值
	    		this.currentObjs = obj;
		    	if(this.showCodeOnBlur){
					uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getCode()||"");
					this._startValue = this.getCode();
					if(this.isMulti && this.el){
						this.el.dom.title = this.getCode(); //设置title属性
					}
				}else{
					uft.extend.form.HeaderRefField.superclass.setValue.call(this, this.getText()||"");
					this._startValue = this.getText();
					if(this.isMulti && this.el){
						this.el.dom.title = this.getText(); //设置title属性
					}
				}
		    	if(this.isUseCache){
		    		this.cache[this.getPk()] = obj;
		    		this.cache[this.getCode()] = obj;
		    	}
		    	//默认已经失去焦点
		    	if(this.isFocusAfterSubmit){
		    		this.focus();//得到鼠标焦点
		    	}else{
			    	if(this._orginalPk != this.getPk()){
			    		//若值发生改变，则触发change事件
						this.fireEvent('change', this,this.getPk(),this._orginalPk );
			    	}
			    	this.bTriggerBlur = false;
		    	}
		    	return;
	    	}
	    	//可能返回的pk项没有值，但是其他项code，name等有值，这属于参照本身的错误
	    	if(!obj || !obj.pk || obj.pk.trim().length==0){
	    		this.clearValue();
	    		return;
	    	}    	
	    	if(this.currentObj && this.currentObj.pk==obj.pk){//当前已存在pk值，选择的对象就是原对象
		    	//默认已经失去焦点
		    	if(this.isFocusAfterSubmit){
		    		this.focus();//得到鼠标焦点
		    	}   		
	    		return;
	    	}
	    	
	    	//肯定是有不同的选项，必须触发onBlur
	    	this.bTriggerBlur =true;       	
	    	//存储上一次的值
	    	this._orginalPk=this.getPk();
	    	if(this.fireEvent('beforesetvalue',obj.pk,this._orginalPk)===false){
	    		return;
	    	}
	    	
	    	this.currentObj = obj;
	    	
	    	if(this.showCodeOnBlur){
	    		uft.extend.form.HeaderRefField.superclass.setValue.call(this, obj.code||"");
	    		this._startValue = obj.code;
	    	}else{
	    		uft.extend.form.HeaderRefField.superclass.setValue.call(this, obj.name||"");
	    		this._startValue = obj.name;
	    	}
	    	
	    	//设置cache
	    	if(this.isUseCache){
	    		this.cache[obj.pk] = obj;
	    		this.cache[obj.code] = obj;
	    	}
	    	//默认已经失去焦点
	    	if(this.isFocusAfterSubmit){
	    		this.focus();//得到鼠标焦点
	    	}else{
		    	if(this._orginalPk != obj.pk){
		    		//若值发生改变，则触发change事件
					this.fireEvent('change', this,obj.pk,this._orginalPk );
		    	}
				this.bTriggerBlur =false;
	    	}
	    	return;
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
	/**
	 * 返回的数据格式为{pk:pk,code:code,name:name}
	 * @param {} code
	 * @return {String}
	 */
	getByCode : function(code){
		if (!code || code.trim()==''){
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
				if(this.returnType == 0 || this.returnType == 1){
					//返回值是code或者name，这种情况只会在查询模板中出现
					return {pk:'',code:code,name:code};
				}
				//从数据库中查询
				if(this.getByCodeUrl && this.getByCodeUrl.length > 0 && code.trim().length>0){
					var url=this.getByCodeUrl;
					var finalParams = {code:code};
					//可能code会重复，如封存数据等，加入自定义条件
					if(this.refWindow.params){ 
						var _params = this.refWindow.params;
						var arr = _params.split(";");
						for(var i=0;i<arr.length;i++){
							var arr1;
							if(arr[i].indexOf(":")==-1){
								arr1=arr[i].split("=");
							}else{
								arr1=arr[i].split(":");
							}
							arr1[1]=uft.Utils.resolveExpression(arr1[1]);
							finalParams[arr1[0]] = arr1[1];
						}						
					}
					if(this.extendParams){
						Ext.apply(finalParams,this.extendParams);
					}
					var values = Utils.doSyncRequest(url,finalParams,'POST');
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
				if(this.returnType == 0 || this.returnType == 1){
					//返回值是code或者name，这种情况只会在查询模板中出现
					return {pk:'',code:code,name:code};
				}
				//从数据库中查询
				if(this.getByCodeUrl && this.getByCodeUrl.length > 0 && code.trim().length>0){
					var url=this.getByCodeUrl;
					var finalParams = {code:code};
					if(this.refWindow.params){//可能code会重复，如封存数据等，加入自定义条件
						var _params = this.refWindow.params;
						var arr = _params.split(";");
						for(var i=0;i<arr.length;i++){
							var arr1;
							if(arr[i].indexOf(":")==-1){
								arr1=arr[i].split("=");
							}else{
								arr1=arr[i].split(":");
							}
							arr1[1]=uft.Utils.resolveExpression(arr1[1]);
							finalParams[arr1[0]] = arr1[1];
						}	
					}			
					if(this.extendParams){
						Ext.apply(finalParams,this.extendParams);
					}			
					var values = Utils.doSyncRequest(url,finalParams,'POST'); //使用post是为了支持中文
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
	 * 
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
	getByPk : function(pk){
		if (!pk || pk.trim()==''){
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
	//将code与params作为key
	getCacheKey : function(code,params){
		return code+"_"+Ext.encode(params);
	},
	//可能需要与其他表单域关联,当其他表单域改变时，需要同时改变参照显示值
	//提供增加表格额外参数，将参数加入refWindow配置中,
	//参数的格式如：{_cond: "name like 'abc'"}
	addExtendParams : function(newParams){
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
        uft.extend.form.HeaderRefField.superclass.onDestroy.call(this);
    },
    /**
     * 返回参照的pk值
     */
    getPk : function(){
    	if(this.isMulti){
    		if(this.currentObjs){
    			var pks = '';
    			for(var i=0;i<this.currentObjs.length;i++){
    				pks += this.currentObjs[i].pk+",";
    			}
    			return pks.substring(0,pks.length-1); //多个pk以逗号分隔
    		}
    	}else{
	    	if(this.currentObj){
	    		return this.currentObj.pk;
	    	}
    	}
    	return null;
    },
    /**
     * 返回参照的code值
     */
    getCode : function(){
    	if(this.isMulti){
    		if(this.currentObjs){
    			var codes = '';
    			for(var i=0;i<this.currentObjs.length;i++){
    				codes += this.currentObjs[i].code+",";
    			}
    			return codes.substring(0,codes.length-1); //多个pk以逗号分隔    			
    		}
    	}else{
	    	if(this.currentObj){
	    		return this.currentObj.code;
	    	}
    	}
		if(this.hasFocus){
    		return this.el.dom.value;
    	}   
    	return null;
    },
    /**
     * 返回参照的text值
     */
    getText : function(){
    	if(this.isMulti){
    		if(this.currentObjs){
    			var names = '';
    			for(var i=0;i<this.currentObjs.length;i++){
    				names += this.currentObjs[i].name+",";
    			}
    			return names.substring(0,names.length-1); //多个pk以逗号分隔    			
    		}
    	}else{
	    	if(this.currentObj){
	    		return this.currentObj.name;
	    	}
    	}
    	if(this.hasFocus){
    		return null;
    	}
    	return this.el.dom.value;
    },
    /**
     * 返回当前参照所对应的行的列值,对于树参照，columnName只能是id,code,name
     * 对于多选，不要调用这个方法，否则会出现错误，目前也没有这样的需求，这里不实现
     * @param {} columnName
     */
    getRefValue : function(columnName){
    	if(!columnName){
    		return null;
    	}
    	if(this.currentObj){
    		return this.currentObj[columnName];
    	}else{
    		return null;
    	}
    }
});
Ext.reg("headerreffield",uft.extend.form.HeaderRefField);