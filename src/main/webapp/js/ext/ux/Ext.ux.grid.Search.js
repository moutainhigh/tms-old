/**
 * 该类是引用别人的，所以不改变其命名空间
 */
Ext.ns('Ext.ux.grid');

// Check RegExp.escape dependency
if('function' !== typeof RegExp.escape) {
	throw('RegExp.escape function is missing. Include Ext.ux.util.js file.');
}

/**
 * Creates new Search plugin
 * @constructor
 * @param {Object} A config object
 */
Ext.ux.grid.Search = function(config) {
	Ext.apply(this, config);
	Ext.ux.grid.Search.superclass.constructor.call(this);
}; // eo constructor

Ext.extend(Ext.ux.grid.Search, Ext.util.Observable, {
	/**
	 * @cfg {Boolean} autoFocus Try to focus the input field on each store load if set to true (defaults to undefined)
	 */

	/**
	 * @cfg {String} searchText Text to display on menu button
	 */
//	 searchText:'Search'
	searchText:'<span class="uft-grid-search-text">条件</span>'

	/**
	 * @cfg {String} searchTipText Text to display as input tooltip. Set to '' for no tooltip
	 */ 
	,searchTipText:'Type a text to search and press Enter'

	/**
	 * @cfg {String} selectAllText Text to display on menu item that selects all fields
	 */
//	,selectAllText:'Select All'
	,selectAllText:'所有字段'

	/**
	 * @cfg {String} position Where to display the search controls. Valid values are top and bottom
	 * Corresponding toolbar has to exist at least with mimimum configuration tbar:[] for position:top or bbar:[]
	 * for position bottom. Plugin does NOT create any toolbar.(defaults to "bottom")
	 */
	,position:'bottom'

	/**
	 * @cfg {String} iconCls Icon class for menu button (defaults to "icon-magnifier")
	 */
	,iconCls:'icon-magnifier'

	/**
	 * @cfg {String/Array} checkIndexes Which indexes to check by default. Can be either 'all' for all indexes
	 * or array of dataIndex names, e.g. ['persFirstName', 'persLastName'] (defaults to "all")
	 */
	,checkIndexes:'all'

	/**
	 * @cfg {Array} disableIndexes Array of index names to disable (not show in the menu), e.g. ['persTitle', 'persTitle2']
	 * (defaults to [] - empty array)
	 */
	,disableIndexes:[]
	,enableIndexes:[] //enableIndexs的优先级比disableIndexs高，若存在enableIndexes则不检查disableIndexes;

	/**
	 * Field containing search text (read-only)
	 * @property field
	 * @type {Ext.form.TwinTriggerField}
	 */

	/**
	 * @cfg {String} dateFormat How to format date values. If undefined (the default) 
	 * date is formatted as configured in colummn model
	 */

	/**
	 * @cfg {Boolean} showSelectAll Select All item is shown in menu if true (defaults to true)
	 */
	,showSelectAll:true

	/**
	 * Menu containing the column module fields menu with checkboxes (read-only)
	 * @property menu
	 * @type {Ext.menu.Menu}
	 */

	/**
	 * @cfg {String} menuStyle Valid values are 'checkbox' and 'radio'. If menuStyle is radio
	 * then only one field can be searched at a time and selectAll is automatically switched off. 
	 * (defaults to "checkbox")
	 */
	,menuStyle:'checkbox'

	/**
	 * @cfg {Number} minChars Minimum characters to type before the request is made. If undefined (the default)
	 * the trigger field shows magnifier icon and you need to click it or press enter for search to start. If it
	 * is defined and greater than 0 then maginfier is not shown and search starts after minChars are typed.
	 * (defaults to undefined)
	 */

	/**
	 * @cfg {String} minCharsTipText Tooltip to display if minChars is > 1
	 */
	,minCharsTipText:'Type at least {0} characters'

	/**
	 * @cfg {Array} readonlyIndexes Array of index names to disable (show in menu disabled), e.g. ['persTitle', 'persTitle2']
	 * (defaults to undefined)
	 */

	/**
	 * @cfg {Number} width Width of input field in pixels (defaults to 100)
	 */
	,width:150

	/**
	 * @cfg {String} xtype xtype is usually not used to instantiate this plugin but you have a chance to identify it
	 */
	,xtype:'gridsearch'
	
	/**
	 * @cfg {Object} paramNames Params name map (defaults to {fields:"fields", query:"query"}
	 */
	,paramNames: {
		 fields:'GRID_QUERY_FIELDS'
		 ,fieldsType : 'GRID_QUERY_FIELDS_TYPE' //字段类型，后台在构造查询语句的时候，如果没有传入class，则需要使用该字段进行判断
		,query:'GRID_QUERY_KEYWORD'
	}

	/**
	 * @cfg {String} shortcutKey Key to fucus the input field (defaults to r = Sea_r_ch). Empty string disables shortcut
	 */
	,shortcutKey:'r'

	/**
	 * @cfg {String} shortcutModifier Modifier for shortcutKey. Valid values: alt, ctrl, shift (defaults to "alt")
	 */
	,shortcutModifier:'alt'

	/**
	 * @cfg {String} align "left" or "right" (defaults to "left")
	 */

	/**
	 * @cfg {Number} minLength Force user to type this many character before he can make a search 
	 * (defaults to undefined)
	 */

	/**
	 * @cfg {Ext.Panel/String} toolbarContainer Panel (or id of the panel) which contains toolbar we want to render
	 * search controls to (defaults to this.grid, the grid this plugin is plugged-in into)
	 */
	
	/**
	 * @private
	 * @param {Ext.grid.GridPanel/Ext.grid.EditorGrid} grid reference to grid this plugin is used for
	 */
	,init:function(grid) {
		this.grid = grid;

		// setup toolbar container if id was given
		if('string' === typeof this.toolbarContainer) {
			this.toolbarContainer = Ext.getCmp(this.toolbarContainer);
		}

		// do our processing after grid render and reconfigure
		grid.onRender = grid.onRender.createSequence(this.onRender, this);
		grid.reconfigure = grid.reconfigure.createSequence(this.reconfigure, this);
	} // eo function init
	/**
	 * adds plugin controls to <b>existing</b> toolbar and calls reconfigure
	 * @private
	 */
	,onRender:function() {
		var panel = this.toolbarContainer || this.grid;
		var tb;
		if('bottom' === this.position){
			tb=panel.bottomToolbar;
			this.pos=tb.items.length-2;//表格的bbar通常都包含翻页按钮。
		}else{
			tb=panel.topToolbar;
		}
		
		this.field =new uft.extend.form.FilterField({ 
			 id : this.grid.id + '_textfield',
			 cls : 'x-form-field-ext',
			 emptyText : '按回车查询'
			,width:this.width
		});
		this.field.on('clear',function(field){
			if(field.getValue() ==''){
				//清空了当前的值,执行下查询
				this.onTriggerSearch();
			}
		},this);
		this.field.on('render', function() {
			var map = new Ext.KeyMap(this.field.el, [{
				 key : Ext.EventObject.ENTER,
				scope : this,
				fn : this.onTriggerSearch
			}, {
				key : Ext.EventObject.ESC,
				scope : this,
				fn : this.onTriggerClear
			}]);
			map.stopEvent = true;
		}, this);
		

		if('right' === this.align) {
			tb.addFill();
		}else {
			if(0 < tb.items.getCount()) {
				tb.addSeparator();
			}
		}
		if(this.pos){
			tb.insertButton(this.pos,this.field);			
		}else{
			tb.addButton(this.field);
		}
		// 查询按钮
//		tb.addButton(new Ext.Button({
//			text : '查询',
//			iconCls:'btnZoom',
//			scope : this,
//			handler : this.onTriggerSearch
//		}));

		// keyMap
		if(this.shortcutKey && this.shortcutModifier) {
			var shortcutEl = this.grid.getEl();
			var shortcutCfg = [{
				 key:this.shortcutKey
				,scope:this
				,stopEvent:true
				,fn:function() {
					this.field.focus();
				}
			}];
			shortcutCfg[0][this.shortcutModifier] = true;
			this.keymap = new Ext.KeyMap(shortcutEl, shortcutCfg);
		}

		if(true === this.autoFocus) {
			this.grid.store.on({scope:this, load:function(){this.field.focus();}});
		}
	} // eo function onRender
	,onSearchFieldKeydown:function(e){
		  var k = e.getKey();
		  //alert(k);
	}
	/**
	 * field el keypup event handler. Triggers the search
	 * @private
	 */
	,onKeyUp:function() {
		var length = this.field.getValue().toString().length;
		if(0 === length || this.minChars <= length) {
			this.onTriggerSearch();
		}
	} // eo function onKeyUp
	/**
	 * Clear Trigger click handler
	 * @private 
	 */
	,onTriggerClear:function() {
		if(this.field.getValue()) {
			this.field.setValue('');
			this.field.focus();
			this.onTriggerSearch();
		}
	} // eo function onTriggerClear
	/**
	 * Search Trigger click handler (executes the search, local or remote)
	 * @private 
	 */
	,onTriggerSearch:function() {
		if(!this.field.isValid()) {
			return;
		}
		var val = this.field.getValue();
		var store = this.grid.store;

			// clear start (necessary if we have paging)
			if(store.lastOptions && store.lastOptions.params) {
				store.lastOptions.params[store.paramNames.start] = 0;
			}

			// get fields to search array
			var data = this.initData();
			var fields = data.fields;
			var fieldsType = data.fieldsType;

			// add fields and query to baseParams of store
			delete(store.baseParams[this.paramNames.fields]);
			delete(store.baseParams[this.paramNames.fieldsType]);
			delete(store.baseParams[this.paramNames.query]);
			if (store.lastOptions && store.lastOptions.params) {
				delete(store.lastOptions.params[this.paramNames.fields]);
				delete(store.lastOptions.params[this.paramNames.fieldsType]);
				delete(store.lastOptions.params[this.paramNames.query]);
			}
			if(fields.length) {
				store.baseParams[this.paramNames.fields] = Ext.encode(fields);
				store.baseParams[this.paramNames.fieldsType] = Ext.encode(fieldsType);
				store.baseParams[this.paramNames.query] = val;
			}

			// reload store
			store.reload();

	} // eo function onTriggerSearch
	,initData : function(){
		var fields = [];
		var fieldsType = [];
		var cm = this.grid.colModel;
		Ext.each(cm.config, function(config) {
			var disable = false;
			var enable = false;
			if(config.header && config.dataIndex) {
				if(this.enableIndexes.length>0){
					Ext.each(this.enableIndexes, function(item) {
						enable = enable ? enable : item === config.dataIndex;
					});
					if(enable) {
						fields.push(config.dataIndex);
						fieldsType.push(this._getType(config.dataIndex));								
					}
				}else{
					Ext.each(this.disableIndexes, function(item) {
						disable = disable ? disable : item === config.dataIndex;
					});
					if(!disable) {
						fields.push(config.dataIndex);
						fieldsType.push(this._getType(config.dataIndex));						
					}
				}
			}
		}, this);	
		return {fields:fields,fieldsType:fieldsType};
	}
	/**
	 * @param {Boolean} true to disable search (TwinTriggerField), false to enable
	 */
	,setDisabled:function() {
		this.field.setDisabled.apply(this.field, arguments);
	} // eo function setDisabled
	/**
	 * Enable search (TwinTriggerField)
	 */
	,enable:function() {
		this.setDisabled(false);
	} // eo function enable
	/**
	 * Disable search (TwinTriggerField)
	 */
	,disable:function() {
		this.setDisabled(true);
	} // eo function disable

	,_getType : function(dataIndex){
		var type='string';
		var recordType = this.grid.recordType;
		if(recordType){
			for(var j=0;j<recordType.length;j++){
				if(recordType[j].name == dataIndex){
					type = recordType[j].type;
					break;
				}
			}
		}
		return type;
	}

}); // eo extend
