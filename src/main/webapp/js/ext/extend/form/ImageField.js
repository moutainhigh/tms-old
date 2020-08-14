Ext.namespace('uft.extend.form');
/**
 * 显示图片的field
 * 
 * @class uft.extend.form.ImageField
 * @extends Ext.form.DisplayField
 */
uft.extend.form.ImageField = Ext.extend(Ext.form.DisplayField, {
	//这个高度和宽度与缩略图的高度和宽度一致
	defaultAutoCreate : {tag: 'img', src: Ext.BLANK_IMAGE_URL,width : 170, height:170,style:'cursor:pointer;'},
	hideLabel : true, //默认隐藏fieldLabel
	prefix : 'viewImage.do', //url前缀
	fileName : null, //图片文件名
	constructor : function(config) {
		Ext.apply(this, config);
		this.rowspan = 8; //占用多少行
		uft.extend.form.ImageField.superclass.constructor.call(this);
	},
	onRender : function(ct, position){
		uft.extend.form.ImageField.superclass.onRender.call(this,ct,position);
		this.bindListeners();
	},
	bindListeners : function(){
		//绑定事件
		if(this.el){
			this.el.on({scope:this,click : function(){
				//打开原图
				var imageUrl = this._buildImageUrl({fileName:this.fileName});
				window.open(imageUrl);
			},error : function(){
				//当图片打不开时，使用默认的图片
				this.el.dom.src= ctxPath +'/images/unsee.gif'
			}});
		}
	},
	setValue : function(value){
		if(!value){
			//清空当前的image url
			if(this.el){
				this.el.dom.src = Ext.BLANK_IMAGE_URL;
			}
			return;
		}
		this.fileName = value;
		var imageUrl = this._buildImageUrl({fileName:value,thumb:true});
		//重新加载图片
		if(this.el){
			this.el.dom.src = imageUrl;
		}
	},
	//private
	_buildImageUrl : function(config){
		var url = this.prefix;
		if(config.thumb){
			url += "?thumb=true"; //读取缩略图
		}
		if(config.fileName){
			if(url.indexOf('?') == -1){
				url += '?';
			}else{
				url += '&';
			}			
			url += "fileName="+config.fileName;
		}else{
			return Ext.BLANK_IMAGE_URL;
		}
		return url;
	}
});
Ext.reg("imagefield",uft.extend.form.ImageField);