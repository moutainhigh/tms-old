/*
 * Bundle.js
 *
 * @author: elmasse(c) Maximiliano Fierro 2008
 * @TODO: Comment Me!!!!
 */


Ext.namespace('Ext.i18n');

Ext.i18n.Bundle = function(config){
	this.bundle = config.bundle;
	this.path = config.path;
	this.language = config.lang || this.guessLanguage(); 
	var url;
	if(this.path)
		url = this.path + '/';
	url+=this.bundle+'_'+this.language+this.resourceExt;
	
    Ext.i18n.Bundle.superclass.constructor.call(this, {
        proxy: new Ext.data.HttpProxy({
								url: url, 
								method: 'POST'
								}),
        reader: new Ext.data.PropertyReader()
    });

	this.load();
	this.on('loadexception', this.loadParent);
};

 Ext.extend(Ext.i18n.Bundle, Ext.data.Store,{ 
	defaultLanguage: 'en-US',
	loadFlag: false,
	resourceExt: '.properties',
	bundle: '',
	path: null,
	
	//private
	guessLanguage: function(){
		return (navigator.language || navigator.browserLanguage
				|| navigator.userLanguage || this.defaultLanguage);
	},
	
	getMsg: function(key){
		return this.getById(key)? Ext.util.Format.htmlDecode(this.getById(key).data) : key + 'undefined';
	},
	
	onReady: function(fn){
		this.readyFn = fn;
		this.on('load', this.readyFn);
	},
	
	loadParent: function(){
		if(!this.loadFlag){
			this.loadFlag=true;
			var url;
			if(this.path)
				url = this.path + '/';
			url+=this.bundle+this.resourceExt;
			this.proxy = new Ext.data.HttpProxy({
								url: url, 
								method: 'POST'
								});
			this.load();			
		}else{
			throw {message: 'Resource Bundle not found'};
		}	
	}

});

