Ext.namespace('Ext.ux.InlineToolbarTabPanel');
Ext.ux.InlineToolbarTabPanel = Ext.extend(Ext.TabPanel, {
  hideToolbar : false //是否隐藏工具栏
  ,toolbar: null  //嵌入tabpanel的toolbar,已经是一个toolbar对象
  ,headerToolbar: true
  ,tabToolbars: false
  ,shadowTabs: false
  ,border : true
  ,withinForm: false//是否布局在表单中
  ,onRender: function(ct, position) {
    if(!this.itemTpl){
      var tt;

      if (this.tabToolbars) {
        tt = new Ext.Template(
          '<li class="{cls}" id="{id}"><a class="x-tab-strip-close" onclick="return false;"></a>',
          '<a class="x-tab-right" href="#" onclick="return false;"><em class="x-tab-left">',
          '<table cellpadding="1" cellspacing="0" class="x-tab-strip-inner"><tr><td>',
          '<span class="x-tab-strip-text {iconCls}">{text}</span></td><td>&nbsp;</td>',
          '<td style="padding-top:1px;"><div id="{tabTB}" style="position:relative;"></div></td></tr></table></em></a></li>'
        );
      } else {
        tt = new Ext.Template(
          '<li class="{cls}" id="{id}"><a class="x-tab-strip-close" onclick="return false;"></a>',
          '<a class="x-tab-right" href="#" onclick="return false;"><em class="x-tab-left">',
          '<span class="x-tab-strip-inner"><span class="x-tab-strip-text {iconCls}">{text}</span></span>',
          '</em></a></li>'
        );
      }
      tt.disableFormats = true;
      tt.compile();
      this.itemTpl = tt;
    }
    Ext.ux.InlineToolbarTabPanel.superclass.onRender.call(this, ct, position);
  }
  ,afterRender: function() {
    Ext.ux.InlineToolbarTabPanel.superclass.afterRender.call(this);
    if (!Ext.isEmpty(this.toolbar)&& !this.hideToolbar) {
      this.setToolbar(this.toolbar);
    }
  }
  ,onResize: function() {
    Ext.ux.InlineToolbarTabPanel.superclass.onResize.apply(this, arguments);
    if (Ext.isEmpty(this.toolbar)) return;

    var tbEl = this.toolbar.getEl();
    if(!tbEl){
    	return;
    }
    var tbWidth = tbEl.dom.offsetWidth;
    if(tbWidth == 0){
    	//此时恢复原来的宽度，这个宽度可能在隐藏toolbar时变成0了
    	if(!this.backupTbWidth){
    		tbWidth = 142;//定义一个默认的宽度
    	}else{
    		tbWidth = this.backupTbWidth;
    	}
    }else{
    	//备份该高度
    	this.backupTbWidth = tbWidth;
    }
    var w;
    var headerOffsetWidth = this.header.dom.offsetWidth;
    if(headerOffsetWidth == 0){
    	//header隐藏了,取页面的宽度
    	headerOffsetWidth = document.body.offsetWidth;
    }
    w = headerOffsetWidth-tbWidth-(this.headerToolbar?0:10);//重新设置header的宽度，等于原始宽度减去辅助工具栏宽度
    //如果放在form中，实际上是把它作为表单的一个域，所以整个宽度需要减去form中表单域的缩进
    this.header.setWidth((Ext.isIE6)?(w-2):(Ext.isIE7 || Ext.isGecko3)?w-4:w-(this.withinForm?5:0));
    this.tbHeader.setWidth(tbWidth + (this.headerToolbar?4:10)); 
//    this.stripWrap.setWidth(w-(this.withinForm?4:0));
//    this.toolbar.setWidth(tbWidth);
    
    this.header.setHeight(28);
    this.tbHeader.setHeight(28);
//    this.stripWrap.setHeight(28);
//    this.tbWrap.setHeight(28);
    this.strip.setHeight(28);

//    this.tbHeader.alignTo(this.header, 'tr', (Ext.isGecko && !Ext.isGecko3)?[-1,-1]:[0,0]);
    this.tbHeader.alignTo(this.header, 'tr',[0,0]);
    this.delegateUpdates();
  }

  ,getToolbar: function() {
    return this.toolbar;
  }

  ,setToolbar: function(obj) {
    var cls = 'x-tab-panel-header';
    var tbStyle = {style: 'border-width:0px;' +
      (this.headerToolbar? 'padding:0px;background:transparent none;': '')};

    if (this.headerToolbar)
      cls += (this.border? '':
        ' x-tab-panel-noborder x-tab-panel-header-noborder');
    else
      cls += ' x-tab-strip-wrap x-tab-strip-top';

    this.tbHeader = this.header.insertSibling({
      id:"tbHeader",
      cls:"x-tab-panel-header x-unselectable",
      style: 'position:absolute;right:0px;' + (Ext.isIE6? 'width:0px;' : '')+(uft.Utils.isIE?'':'top:-0.5px;')
    });

    this.tbWrap = this.tbHeader.createChild({
      id:'tbWrap'
      ,style: 'border-left:0px none;border-left-width:0px;'
      ,cls: cls
    });

    var _style='';//border-left:0px solid #8DB2E3;border-top:0px none;margin-top:-2px
    if(Ext.isIE8){
    	//_style='';//border-left:0px solid #8DB2E3;border-top:0px none;margin-top:-1px	
    }
    this.tbContainer = this.tbWrap.createChild({
      id:'tbContainer',
      style: _style
      , tag: this.headerToolbar? 'ul': 'div'
      , cls: this.headerToolbar? 'x-tab-strip-top': 'x-tab-right x-tab-panel-header'
    });
    
    this.header.setStyle('border-right', '0px none');

    Ext.apply(this.toolbar, tbStyle);

    if (!this.headerToolbar) {
      this.toolbar.removeClass('x-toolbar');
      this.toolbar.addClass('x-tab-strip-inner');
    }
    this.toolbar.render(this.tbContainer);
    if(Ext.isIE7||Ext.isChrome){
//    	var tbEl = this.toolbar.getEl();
//	    var tbWidth = tbEl.dom.offsetWidth;//IE7和chrome这个宽度是整个toolbar的宽度
	    this.tbHeader.setWidth(176 + (this.headerToolbar?4:10));
    }

    if (this.toolbar != obj) {
      this.onResize(this.getSize().width);
      this.toolbar = obj;
    }
  }

  ,initTab: function(item, index){
    var before = this.strip.dom.childNodes[index];
    var cls = item.closable ? 'x-tab-strip-closable' : '';
    if(item.disabled){
      cls += ' x-item-disabled';
    }
    if(item.iconCls){
      cls += ' x-tab-with-icon';
    }
    if(item.tabCls){
      cls += ' ' + item.tabCls;
    }

    cls += this.shadowTabs? ' x-tab-strip-disabled': '';

    var tbID = Ext.id();
    var p = {
      id: this.id + this.idDelimiter + item.getItemId(),
      text: item.title,
      cls: cls,
      tabTB: tbID,
      iconCls: item.iconCls || ''
    };

    var el = before ?
             this.itemTpl.insertBefore(before, p) :
             this.itemTpl.append(this.strip, p);

    Ext.fly(el).addClassOnOver('x-tab-strip-over');

    if (this.tabToolbars && !Ext.isEmpty(item.tabToolbar)) {
    item.tabToolbar = new Ext.Toolbar(item.tabToolbar);
    item.tabToolbar.render(tbID);
      item.tabToolbar.removeClass('x-toolbar');
    }

    if(item.tabTip){
      Ext.fly(el).child('span.x-tab-strip-text', true).qtip = item.tabTip;
    }
	item.tabEl = el;//晕死，没加这个，测试怎么通过的？
    item.on('disable', this.onItemDisabled, this);
    item.on('enable', this.onItemEnabled, this);
    item.on('titlechange', this.onItemTitleChanged, this);
    item.on('beforeshow', this.onBeforeShowItem, this);
  }

  ,setActiveTab: function(item){
    item = this.getComponent(item);
    if (!item || this.fireEvent('beforetabchange', this, item, this.activeTab) === false) {
      return;
    }
    if (!this.rendered) {
      this.activeTab = item;
      return;
    }
    if (this.activeTab != item) {
      if (this.activeTab) {
        var oldEl = this.getTabEl(this.activeTab);
        if(oldEl) {
          Ext.fly(oldEl).removeClass('x-tab-strip-active');
//          Ext.fly(oldEl).setStyle('padding-top', '0px');
          if (this.shadowTabs) {
            Ext.fly(oldEl).addClass('x-tab-strip-disabled');
          }
        }
        this.activeTab.fireEvent('deactivate', this.activeTab);
      }
      var el = this.getTabEl(item);
      Ext.fly(el).addClass('x-tab-strip-active');
//      Ext.fly(el).setStyle('padding-top', '1px');
      if (this.shadowTabs) {
        Ext.fly(el).removeClass('x-tab-strip-disabled');
      }

      this.activeTab = item;
      this.stack.add(item);

      this.layout.setActiveItem(item);
      if (this.layoutOnTabChange && item.doLayout) {
        item.doLayout();
      }
      if (this.scrolling) {
        this.scrollToTab(item, this.animScroll);
      }

      item.fireEvent('activate', item);
      this.fireEvent('tabchange', this, item);
    }
  }

  ,createScrollers: function(){
    var h = this.strip.dom.offsetHeight;

    // left
    var sl = this.header.insertFirst({
      cls:'x-tab-scroller-left'
    });
    sl.setHeight(h);
    sl.addClassOnOver('x-tab-scroller-left-over');
    this.leftRepeater = new Ext.util.ClickRepeater(sl, {
      interval : this.scrollRepeatInterval,
      handler: this.onScrollLeft,
      scope: this
    });
    this.scrollLeft = sl;

    // right
    var sr = this.header.insertFirst({
        cls:'x-tab-scroller-right'
    });
    sr.setHeight(h);
    sr.addClassOnOver('x-tab-scroller-right-over');
    this.rightRepeater = new Ext.util.ClickRepeater(sr, {
      interval : this.scrollRepeatInterval,
      handler: this.onScrollRight,
      scope: this
    });
    this.scrollRight = sr;
  }
});