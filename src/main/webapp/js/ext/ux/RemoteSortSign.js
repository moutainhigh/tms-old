Ext.namespace('Ext.ux.Andrie');

/**
 * @class Ext.ux.RemoteSortSign
 * @extends Ext.PagingToolbar
 * @author xuqc
 * @version 0.1
 */
Ext.ux.RemoteSortSign = function(config){
	Ext.apply(this, config);
};

Ext.extend(Ext.ux.RemoteSortSign, Ext.util.Observable, {
	addBefore: '-',
	/**
	 * @cfg {String} beforeText
	 * Text to display before the comboBox
	 */
	boxLabel: '后台排序',
	position : 13,
	
	init: function(pagingToolbar){
		this.pagingToolbar = pagingToolbar;
		this.pagingToolbar.on('render', this.onRender, this);
	},

	//private
	onRender: function(){
		this.checkbox = new Ext.form.Checkbox({
			name : 'isRemoteSort',
			boxLabel : this.boxLabel
		});
		if (this.addBefore){
			this.pagingToolbar.insert(this.position,this.addBefore);
			this.position++;
		}
		this.pagingToolbar.insert(this.position,this.checkbox);
		this.position++;
	},
	getCheckbox : function(){
		return this.checkbox;
	}
})