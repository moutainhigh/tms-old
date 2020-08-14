Ext.namespace('uft');
uft.Internal={
		
		getText : function(text){
			if(language == 'en_US'){
				if(this.en_US[text]){
					return this.en_US[text];
				}
			}
			return text;
		},
		
		en_US : {
			'一键配载':'XXX'
		}
		
}