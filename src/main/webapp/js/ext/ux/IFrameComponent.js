Ext.namespace('Ext.ux'); 
Ext.ux.IFrameComponent = Ext.extend(Ext.BoxComponent, {
	onRender : function(ct, position) {
		this.el = ct.createChild( {
			tag : 'iframe',
			id : 'iframe_' + this.id,
			tabTip : this.tabTip,
			frameBorder : 0,
			src : this.url
		});
	}
});