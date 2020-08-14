Ext.namespace('uft.extend.data');
/**
 * 使用该类替代Ext.data.Store来创建数据源，前台传入的分页参数还是start,limit
 * 但是后台的接收变量变为EXT_PAGE_PARAM_START,EXT_PAGE_PARAM_LIMIT
 */
uft.extend.data.Store = function() {
	this.isAddBbar=true;//从BasicGrid,EditorGrid中传入的参数，表示该表格是否包含底部工具栏
	this.defaultParamNames={
		start : 'PAGE_PARAM_START',
		limit : 'PAGE_PARAM_LIMIT',
		sort : 'sort',
		dir : 'dir'
	};		
	uft.extend.data.Store.superclass.constructor.apply(this, arguments);
};

Ext.extend(uft.extend.data.Store, Ext.data.Store, {
    createCallback : function(action, rs, batch) {
        var actions = Ext.data.Api.actions;
        return (action == 'read') ? this.loadRecords : function(data, response, success) {
            // calls: onCreateRecords | onUpdateRecords | onDestroyRecords
            this['on' + Ext.util.Format.capitalize(action) + 'Records'](success, rs, [].concat(data));
            // If success === false here, exception will have been called in DataProxy
            if (success === true) {
                this.fireEvent('write', this, action, data, response, rs);
            }
            this.removeFromBatch(batch, action, data);
        };
    },	
    loadRecords : function(o, options, success){
        var i;
        if (this.isDestroyed === true) {
            return;
        }
        if(!o || success === false){
        	//当请求失败时，弹出提示框，目前使用在当用户没有权限时提示
            if(this.reader.jsonData != undefined){
		        if(this.reader.jsonData.success != undefined){
			        if(this.reader.jsonData.success == "false" || !this.reader.jsonData.success){
						Ext.Msg.show({
							title : '操作失败',
							msg : this.reader.jsonData.msg,
							buttons : Ext.Msg.OK,
							icon : Ext.Msg.WARNING
						});
						Ext.WindowMgr.zseed=9999;
		       		}
		        }
            }               	
            if(options.callback){
                options.callback.call(options.scope || this, [], options, false, o);
            }
            return;
        }
        //FIXME 2013-4-26,统计行
    	this.summaryRow = o.summaryRow||{};
    	
        var r = o.records,t;
        t = o.totalRecords || r.length;
        if(!options || options.add !== true){
        	// FIXME 重新加载数据的时候肯定要清空当前改变的记录集
//            if(this.pruneModifiedRecords){
                this.modified = [];
//            }
            for(i = 0, len = r.length; i < len; i++){
            	if(!r[i].get(this.pkFieldName)){
            		//没有pk值的记录,这可能是使用load的数据，但是用户希望把他们作为已修改的数据
            		this.modified.push(r[i]);
            	}
                r[i].join(this);
            }
            if(this.snapshot){
                this.data = this.snapshot;
                delete this.snapshot;
            }
            this.clearData();
            this.data.addAll(r);
            this.totalLength = t;
            this.applySort();
            this.fireEvent('datachanged', this);
        }else{
            var toAdd = [],
                rec,
                cnt = 0;
            for(i = 0, len = r.length; i < len; ++i){
                rec = r[i];
                if(this.indexOfId(rec.id) > -1){
                    this.doUpdate(rec);
                }else{
                    toAdd.push(rec);
                    ++cnt;
                }
            }
            this.totalLength = Math.max(t, this.data.length + cnt);
            this.add(toAdd);
        }
	    this.fireEvent('load', this, r, options);
	    if(options.callback){
	        options.callback.call(options.scope || this, r, options, true);
	    }             
    },    
    reload : function(options){
        this.load(Ext.applyIf(options||{}, this.lastOptions));
    },    
    /**
     * 使用新的条件加载数据，分页时的下一页、上一页使用该方法
     * @param {} options
     * 				查询条件
     * @param {} storeOptions
     * 				是否将options查询条件加入最近的查询条件中，有时候查询条件只希望用一次，不希望被记住
     * @return {}
     */
	load : function(options,storeOptions) {
        options = Ext.apply({}, options);
        if(storeOptions!==false){
	        this.storeOptions(options);
        }
        if(this.sortInfo && this.remoteSort){
            var pn = this.paramNames;
            options.params = Ext.apply({}, options.params);
            options.params[pn.sort] = this.sortInfo.field;
            options.params[pn.dir] = this.sortInfo.direction;
        }
        try {
            return this.execute('read', null, options); // <-- null represents rs.  No rs for load actions.
        } catch(e) {
            this.handleException(e);
            return false;
        }
    },
    execute : function(action, rs, options, /* private */ batch) {
        // blow up if action not Ext.data.CREATE, READ, UPDATE, DESTROY
        if (!Ext.data.Api.isAction(action)) {
            throw new Ext.data.Api.Error('execute', action);
        }
        // make sure options has a fresh, new params hash
        options = Ext.applyIf(options||{}, {
            params: {}
        });
        if(batch !== undefined){
            this.addToBatch(batch);
        }
        // have to separate before-events since load has a different signature than create,destroy and save events since load does not
        // include the rs (record resultset) parameter.  Capture return values from the beforeaction into doRequest flag.
        var doRequest = true;

        if (action === 'read') {
            doRequest = this.fireEvent('beforeload', this, options);
            Ext.applyIf(options.params, this.baseParams);
            if(this.isAddBbar){ //是否使用分页
				this.setStartParam(options);
     			this.setLimitParam(options);
            }else{
            	//如果没有分页，则删除这两个翻页参数
            	delete options.params[this.paramNames.start];
            	delete options.params[this.paramNames.limit];
            }
            delete options.params.start;
	        delete options.params.limit;
        }
        else {
            // if Writer is configured as listful, force single-record rs to be [{}] instead of {}
            // TODO Move listful rendering into DataWriter where the @cfg is defined.  Should be easy now.
            if (this.writer.listful === true && this.restful !== true) {
                rs = (Ext.isArray(rs)) ? rs : [rs];
            }
            // if rs has just a single record, shift it off so that Writer writes data as '{}' rather than '[{}]'
            else if (Ext.isArray(rs) && rs.length == 1) {
                rs = rs.shift();
            }
            // Write the action to options.params
            if ((doRequest = this.fireEvent('beforewrite', this, action, rs, options)) !== false) {
                this.writer.apply(options.params, this.baseParams, action, rs);
            }
        }
        
        if (doRequest !== false) {
            // Send request to proxy.
            if (this.writer && this.proxy.url && !this.proxy.restful && !Ext.data.Api.hasUniqueUrl(this.proxy, action)) {
                options.params.xaction = action;    // <-- really old, probaby unecessary.
            }
            // Note:  Up until this point we've been dealing with 'action' as a key from Ext.data.Api.actions.
            // We'll flip it now and send the value into DataProxy#request, since it's the value which maps to
            // the user's configured DataProxy#api
            // TODO Refactor all Proxies to accept an instance of Ext.data.Request (not yet defined) instead of this looooooong list
            // of params.  This method is an artifact from Ext2.
            this.proxy.request(Ext.data.Api.actions[action], rs, options.params, this.reader, this.createCallback(action, rs, batch), this, options);
        }
        return doRequest;
    },
    /**
     * 判断一条record是否被修改过
     */
    isModify : function(record){
    	return this.modified.indexOf(record)>-1;
    },
    /**
     * Sorts the store contents by a single field and direction. This is called internally by {@link sort} and would
     * not usually be called manually
     * @param {String} fieldName The name of the field to sort by.
     * @param {String} dir (optional) The sort order, 'ASC' or 'DESC' (case-sensitive, defaults to <tt>'ASC'</tt>)
     */
    singleSort: function(fieldName, dir) {
        var field = this.fields.get(fieldName);
        if (!field) {
            return false;
        }

        var name       = field.name,
            sortInfo   = this.sortInfo || null,
            sortToggle = this.sortToggle ? this.sortToggle[name] : null;

        if (!dir) {
            if (sortInfo && sortInfo.field == name) { // toggle sort dir
                dir = (this.sortToggle[name] || 'ASC').toggle('ASC', 'DESC');
            } else {
                dir = field.sortDir;
            }
        }

        this.sortToggle[name] = dir;
        this.sortInfo = {field: name, direction: dir};
        if(this.remoteSort){
        	if(field.sortName){//如果自定义了sortName，则优先使用
        		this.sortInfo.field = field.sortName;
        	}
        }
       
        this.hasMultiSort = false;

        if (this.remoteSort) {
            if (!this.load(this.lastOptions)) {
                if (sortToggle) {
                    this.sortToggle[name] = sortToggle;
                }
                if (sortInfo) {
                    this.sortInfo = sortInfo;
                }
            }
        } else {
            this.applySort();
            this.fireEvent('datachanged', this);
        }
        return true;
    },
    /**
     * 返回开始记录
     * @param {} options
     * @return {}
     */
    getStart : function(options){
    	return options.params[this.paramNames.start];
    },
	/**
	 * 返回每页记录数
	 * @param {} options
	 * @return {}
	 */
    getLimit : function(options){
		return options.params[this.paramNames.limit];
    },
    /**
     * 设置开始记录参数
     * @param {} options
     */
    setStartParam : function(options){
    	var params = options.params;
        if(typeof(params[this.paramNames.start]) == 'undefined'){
        	if(params.start){
        		params[this.paramNames.start]=params.start;
        	}else{
            	if(this.baseParams.start==undefined){
            		this.baseParams.start=0; //默认起始记录为0，当起始值为-1时，后台不进行分页查询
            	}
            	params[this.paramNames.start] = this.baseParams.start;  //options.params['EXT_PAGE_PARAM_START']=0
            	delete this.baseParams.start;
        	}
        }    
    },
    /**
     * 设置每页记录数参数
     * @param {} options
     */
    setLimitParam : function(options){
    	var params = options.params;
    	if(typeof(params[this.paramNames.limit]) == 'undefined'){
    		if(params.limit){
    			params[this.paramNames.limit]=params.limit;
    		}else{
        		params[this.paramNames.limit] = this.baseParams.limit;  //options.params['EXT_PAGE_PARAM_LIMIT']=3
        		delete this.baseParams.limit;
    		}
    	}       	
    },
    remove : function(record){
    	if(!record){
    		//2012-05-10如果record不存在，null或者undefined，不需要执行
    		return;
    	}
        if(Ext.isArray(record)){
            Ext.each(record, function(r){
                this.remove(r);
            }, this);
            return;
        }
        var index = this.data.indexOf(record);
        if(index > -1){
            record.join(null);
            this.data.removeAt(index);
        }
//        if(this.pruneModifiedRecords){
//            this.modified.remove(record);
//        }
        //将删除的记录加入改变的数组
		if(this.modified.indexOf(record) == -1){ 
			this.modified.push(record);
		}
        if(this.snapshot){
            this.snapshot.remove(record);
        }
        if(index > -1){
            this.fireEvent('remove', this, record, index);
        }
    },
    removeAll : function(silent){
        var items = [];
        this.each(function(rec){
            items.push(rec);
        });
        //加入修改的记录中
        for(var i=0;i<this.getCount();i++){
        	var record = this.getAt(i);
        	if(this.modified.indexOf(record) == -1){ 
				this.modified.push(record);
			}
        }
        this.clearData();
        if(this.totalLength){
        	this.totalLength = 0;
        }
        if(this.snapshot){
            this.snapshot.clear();
        }
        if(this.pruneModifiedRecords){
            this.modified = [];
        }
        if (silent !== true) {  // <-- prevents write-actions when we just want to clear a store.
            this.fireEvent('clear', this, items);
        }
    }    
});