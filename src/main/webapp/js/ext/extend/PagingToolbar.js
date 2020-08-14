Ext.ns('uft.extend');
(function() {
var T = Ext.Toolbar;
uft.extend.PagingToolbar = Ext.extend(Ext.Toolbar, {
    /**
     * @cfg {Ext.data.Store} store
     * The {@link Ext.data.Store} the paging toolbar should use as its data source (required).
     */	
	cls:'uft-paging-toolbar',
	pageSize : 20,//每页记录数
	// 当前页之前的页数，默认为4。  
    beforeNum : 4,  
    // 当前页之后的页数，默认为5。  
    afterNum : 4,  
    prevText : '上一页',
    disabledPrevText : '<div class="page-bbar-btn-prev-disabled">上一页</div>',
    nextText : '下一页',
    disabledNextText : '<div class="page-bbar-btn-next-disabled">下一页</div>',
    
    pageNumText : '<div class="page-bbar-yema">页码：</div>',
	
	totalPageText : '<div class="page-bbar-totalPage-text">共{0}页</div>',
	totalNumText : '<div class="page-bbar-totalNum-text">共{0}条</div>',
	jumpPage : false, //是否加入直接跳转到第几页的输入框
	totalPage : false, //是否加入总页数
	totalNum : true, //是否加入总记录数
	refresh : true, //是否加入刷新按钮
	/**
	 * 工具栏分两部分，一部分是需要重新生成按钮，一部分不需要重新生成，以splitFlag分隔
	 * @cfg splitItemHiddenValue
	 */
	beforeJumpPageText : '到第',
	afterJumpPageText: '页',
	jumpPageText : '确定',
	refreshText : '刷新',
	buttonAlign : 'left',
	initComponent : function(){
		this.items=[];
		//显示总页数
		if(this.totalPage){
			this.totalPageItem = new T.TextItem({
				hiddenValue : -1, //该值不能与其他按钮相同
				text: String.format(this.totalPageText, 1)
			});
	        this.items.push(this.totalPageItem);
	        this.splitItemHiddenValue=this.totalPageItem.hiddenValue;
		}

        //加入页面直接跳转框
        if(this.jumpPage){
	        this.beforeJumpPageItem = new T.TextItem({
	        	hiddenValue:-2,
	        	text:this.beforeJumpPageText
	        });
	        if(!this.splitItemHiddenValue){
	        	this.splitItemHiddenValue = this.beforeJumpPageItem.hiddenValue;
	        }
	        
        	this.items.push(this.beforeJumpPageItem);
			//页码输入框
			this.inputItem = new Ext.form.NumberField({
	            cls: 'page-bbar-input-number',
	            allowDecimals: false,
	            allowNegative: false,
	            enableKeyEvents: true,
	            selectOnFocus: true,
	            submitValue: false
	        });	  
        	this.items.push(this.inputItem);
        	this.items.push(new T.TextItem({text:this.afterJumpPageText}));
			this.btn_jumpto=new uft.extend.Button({
				text : this.jumpPageText,
				cls : 'page-bbar-btn',
				scope : this,
				handler : this.jumpto
			});
			this.items.push(this.btn_jumpto);
        }
        //加入总记录数
        if(this.totalNum){
			this.totalNumItem = new T.TextItem({
				hiddenValue : -3, //该值不能与其他按钮相同
				text: String.format(this.totalNumText, 0)
			});
	        this.items.push(this.totalNumItem);
	        this.splitItemHiddenValue=this.totalNumItem.hiddenValue;
        }
        
        uft.extend.PagingToolbar.superclass.initComponent.call(this);
        this.addEvents(
            /**
             * @event change
             * Fires after the active page has been changed.
             * @param {Ext.PagingToolbar} this
             * @param {Object} pageData An object that has these properties:<ul>
             * <li><code>total</code> : Number <div class="sub-desc">The total number of records in the dataset as
             * returned by the server</div></li>
             * <li><code>activePage</code> : Number <div class="sub-desc">The current page number</div></li>
             * <li><code>pages</code> : Number <div class="sub-desc">The total number of pages (calculated from
             * the total number of records in the dataset as returned by the server and the current {@link #pageSize})</div></li>
             * </ul>
             */
            'change',
            /**
             * @event beforechange
             * Fires just before the active page is changed.
             * Return false to prevent the active page from being changed.
             * @param {Ext.PagingToolbar} this
             * @param {Object} params An object hash of the parameters which the PagingToolbar will send when
             * loading the required page. This will contain:<ul>
             * <li><code>start</code> : Number <div class="sub-desc">The starting row number for the next page of records to
             * be retrieved from the server</div></li>
             * <li><code>limit</code> : Number <div class="sub-desc">The number of records to be retrieved from the server</div></li>
             * </ul>
             * <p>(note: the names of the <b>start</b> and <b>limit</b> properties are determined
             * by the store's {@link Ext.data.Store#paramNames paramNames} property.)</p>
             * <p>Parameters may be added as required in the event handler.</p>
             */
            'beforechange'
        );
        this.on('afterlayout', this.onFirstLayout, this, {single: true});
        this.cursor = 0;
        this.bindStore(this.store, true);        
    },
    //清空上一页和下一页之间的按钮
    clearBtns : function(){
        var len=this.items.items.length;
        for(var i=0;i<len;i++){ //上一页处在第一项
        	var item = this.items.items[i];
        	if(item){
	        	if(item.hiddenValue&&item.hiddenValue==this.splitItemHiddenValue){//若使用固定id进行比较的话，会限制一个页面不能有多个工具栏
	        		break;
	        	}else{
	        		this.remove(item,true); //auDestroy
	        		i--;//减少一个item后，i也必须减少
	        	}
        	}
        }
    },
    
    //写入当前页码的前面beforeNum个页码，
    //1、当前页码小于等于beforeNum,则插入当前页码之前的所有页码；
    //2、当前面的页码大于beforeNum,则插入第一、二、以及最靠近当前页码的(beforeNum-2)个页码，中间插入...
    addBeforeBtns : function(ap){
    	if(ap<=this.beforeNum){
    		for(i=ap-1;i>0;i--){
    			this.insertButton(0,this.buildBtn(i)); //从上一页按钮后面开始插入
    		}
    	}else{
    		for(var i=ap-1;i>=ap-(this.beforeNum-2);i--){
				this.insertButton(0,this.buildBtn(i));
    		}
    		
    		var btn_first = this.buildBtn(1);
    		var btn_second = this.buildBtn(2);
    		if(ap-(this.beforeNum-2)!=3){
				this.insertButton(0,new T.TextItem({text:'...'}));
    		}
			this.insertButton(0,btn_second);
			this.insertButton(0,btn_first);
    	}
    },
    
    //写入当前页码的后afterNum个页码
    //1、总页码小于等于(当前页码+afterNum)，则显示所有页码；
    //2、总页码大于(当前页码+afterNum)，则显示当前页码的后(afterNum-2)个页码和最后两个页码
    addAfterBtns : function(ap,ps){
    	if(ps<=ap+this.afterNum){
    		for(var i=ps;i>ap;i--){
				this.insertButton(0,this.buildBtn(i));
    		}
    	}else{
    		var btn_last = this.buildBtn(ps);
    		var btn_second_last = this.buildBtn(ps-1);
			this.insertButton(0,btn_last);
			this.insertButton(0,btn_second_last);
    		if(ap+this.afterNum<ps){
				this.insertButton(0,new T.TextItem({text:'...'}));
    		}
    		for(var i=ap+this.afterNum-2;i>ap;i--){
				this.insertButton(0,this.buildBtn(i));
    		}
    	}
    },
    
    //加入当前页码的按钮，注意样式有些不一样
    addCurrentBtn : function(ap){
//    	var btn_curr = new uft.extend.Button({
//    		autoWidth : false,
//			text : ap,
//			cls : 'page-bbar-btn-disabled'
//		});	
//		this.insertButton(0,btn_curr);
//		btn_curr.setDisabled(true);
		this.insertButton(0,new T.TextItem({text:'<div class=page-bbar-btn-disabled>'+ap+'</div>'}));
    },
    
    //加入上一页按钮，如果当前页小于2，则插入的是TextItem
    addPrevBtn : function(ap){
        if(ap<2){
        	//若当前页小于2，则禁用上一页按钮
    		this.insertButton(0,new T.TextItem({text:this.disabledPrevText}));
    	}else{
			this.btn_prev=new uft.extend.Button({
				text : this.prevText,
				cls : 'page-bbar-btn-prev',
				scope : this,
				handler : this.movePrevious
			});     
			this.insertButton(0,this.btn_prev);
    	}
    },
    
    //若总页数小于等于当前页码，则插入的是TextItem
    addNextBtn : function(ap,ps){
    	if(ps<=ap){
    		this.insertButton(0,new T.TextItem({text:this.disabledNextText}));
    	}else{
			this.btn_next=new uft.extend.Button({
				text : this.nextText, //不要固定id，否则一个页面只能有一个下一页的按钮
				cls : 'page-bbar-btn-next',
				scope : this,
				handler : this.moveNext
			});
			this.insertButton(0,this.btn_next);
    	}

    },
    
    //刷新按钮
    addRefreshBtn : function(){
		this.refreshBtn = new T.Button({
            tooltip: this.refreshText,
            overflowText: this.refreshText,
            iconCls: 'x-tbar-loading',
            handler: this.doRefresh,
            scope: this
        });
        this.insertButton(0,this.refreshBtn);
    },    
    
    /**
     * 增加页码text，“页码：”
     */
    addPageNumText : function(){
    	this.insertButton(0,new T.TextItem({text:this.pageNumText}));
    },
    
    addOneSpacer : function(){
    	//暂时不加间隔
    	this.insertButton(0,new T.TextItem({text:'&nbsp;&nbsp;'}));
    },
    buildBtn : function(num){
		return new uft.extend.Button({
			hiddenValue  :num, //监听事件会读取该值
			autoWidth : false,
			text : num,//text==num是必须的，监听事件会读取该text
			cls : 'page-bbar-btn',
			scope : this,
			handler : this.goPage
		});
    },
    
    // private
    onFirstLayout : function(){
        if(this.dsLoaded){
            this.onLoad.apply(this, this.dsLoaded);
        }
    },
    //更新总记录数的信息
    updateInfo : function(){
		if(this.totalNum){
        	//更新总记录数
        	this.totalNumItem.setText(String.format(this.totalNumText, this.store.getTotalCount()));
        }
    },    

    // private
    onLoad : function(store, r, o){
        if(!this.rendered){
            this.dsLoaded = [store, r, o];
            return;
        }
        var p = this.getParams();
        this.cursor = (o.params && o.params[p.start]) ? o.params[p.start] : 0;
        var d = this.getPageData();
		this.updateTbarInfo(d);
        this.fireEvent('change', this, d);
    },
    /**
     * 区分父类的updateInfo方法
     * d对象包括的元素：total，activePage，pages
     */
    updateTbarInfo : function(d){
    	if(!d){
    		d = this.getPageData();
    	}
        var ap = d.activePage, ps = d.pages,total=d.total;
        this.clearBtns();
    	//先清空上一页和下一页之间的按钮，再根据算法插入不同的页码
		this.addAfterBtns(ap,ps); //请注意这三个的顺序不能修改
		this.addCurrentBtn(ap);
		this.addBeforeBtns(ap);
		this.addPageNumText();
		if(this.refresh){
			this.addRefreshBtn();
		}
		this.addNextBtn(ap,ps);
		this.addPrevBtn(ap);
        
		if(this.totalPage){
			//更新总页数
       	 	this.totalPageItem.setText(String.format(this.totalPageText, d.pages));
		}
        if(this.jumpPage){
	        //更新输入框
	        this.inputItem.setValue(ap);
        }
        if(this.totalNum){
        	//更新总记录数
        	this.totalNumItem.setText(String.format(this.totalNumText, total));
        }
        this.doLayout();    	
    },

    // private
    getPageData : function(){
        var total = this.store.getTotalCount();
        return {
            total : total,
            activePage : Math.ceil((this.cursor+this.pageSize)/this.pageSize),
            pages :  total < this.pageSize ? 1 : Math.ceil(total/this.pageSize)
        };
    },

    /**
     * Change the active page
     * @param {Integer} page The page to display
     */
    //private 直接跳转到某页
    changePage : function(page){
        this.doLoad(((page-1) * this.pageSize).constrain(0, this.store.getTotalCount()));
    },
    
    /**
     * 页面中的页码的跳转函数
     */
    goPage : function(btn,e){
    	this.changePage(parseInt(btn.hiddenValue));
    },
    
    /**
     * 页面中点击确定，跳转函数
     */
    jumpto : function(){
    	var d = this.getPageData(), pageNum;
		var pageNum = this.readPage(d);
		if(pageNum !== false){
			pageNum = Math.min(Math.max(1, pageNum), d.pages);
			this.doLoad(pageNum * this.pageSize);
		}     	
    	this.changePage(pageNum);
    },

    doRefresh : function(){
//        this.doLoad(this.cursor);
    	//最好刷新后回到第一页，否则如果当前页已经没了，就会出问题
    	this.doLoad(0);
    },
    // private
    onLoadError : function(){
        if(!this.rendered){
            return;
        }
    },

    // private
    readPage : function(d){
        var v = this.inputItem.getValue(), pageNum;
        if (!v || isNaN(pageNum = parseInt(v, 10))) {
            this.inputItem.setValue(d.activePage);
            return false;
        }
        return pageNum;
    },

    // private
    getParams : function(){
        //retain backwards compat, allow params on the toolbar itself, if they exist.
        return this.paramNames || this.store.paramNames;
    },

    // private
    beforeLoad : function(){
    	
    },

    // private
    doLoad : function(start){
        var o = {}, pn = this.getParams();
        o[pn.start] = start;
        o[pn.limit] = this.pageSize;
        if(this.fireEvent('beforechange', this, o) !== false){
            this.store.load({params:o});
        }
    },

    /**
     * Move to the first page, has the same effect as clicking the 'first' button.
     */
    moveFirst : function(){
        this.doLoad(0);
    },

    /**
     * Move to the previous page, has the same effect as clicking the 'previous' button.
     */
    movePrevious : function(){
        this.doLoad(Math.max(0, this.cursor-this.pageSize));
    },

    /**
     * Move to the next page, has the same effect as clicking the 'next' button.
     */
    moveNext : function(){
        this.doLoad(this.cursor+this.pageSize);
    },

    /**
     * Move to the last page, has the same effect as clicking the 'last' button.
     */
    moveLast : function(){
        var total = this.store.getTotalCount(),
            extra = total % this.pageSize;

        this.doLoad(extra ? (total - extra) : total - this.pageSize);
    },
    
    /**
     * Binds the paging toolbar to the specified {@link Ext.data.Store}
     * @param {Store} store The store to bind to this toolbar
     * @param {Boolean} initial (Optional) true to not remove listeners
     */
    bindStore : function(store, initial){
        var doLoad;
        if(!initial && this.store){
            if(store !== this.store && this.store.autoDestroy){
                this.store.destroy();
            }else{
                this.store.un('beforeload', this.beforeLoad, this);
                this.store.un('load', this.onLoad, this);
                this.store.un('exception', this.onLoadError, this);
            }
            if(!store){
                this.store = null;
            }
        }
        if(store){
            store = Ext.StoreMgr.lookup(store);
            store.on({
                scope: this,
                beforeload: this.beforeLoad,
                load: this.onLoad,
                exception: this.onLoadError
            });
            doLoad = true;
        }
        this.store = store;
        if(doLoad){
            this.onLoad(store, null, {});
        }
    },

    /**
     * Unbinds the paging toolbar from the specified {@link Ext.data.Store} <b>(deprecated)</b>
     * @param {Ext.data.Store} store The data store to unbind
     */
    unbind : function(store){
        this.bindStore(null);
    },

    /**
     * Binds the paging toolbar to the specified {@link Ext.data.Store} <b>(deprecated)</b>
     * @param {Ext.data.Store} store The data store to bind
     */
    bind : function(store){
        this.bindStore(store);
    },

    // private
    onDestroy : function(){
        this.bindStore(null);
        uft.extend.PagingToolbar.superclass.onDestroy.call(this);
    }    
});
})();
Ext.reg('uftpaging', uft.extend.PagingToolbar);