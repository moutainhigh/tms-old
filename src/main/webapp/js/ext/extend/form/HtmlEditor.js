Ext.namespace('uft.extend.form');
/**
 * 加入一些插件
 * 
 * @class uft.extend.form.HtmlEditor
 * @extends Ext.form.HtmlEditor
 */
uft.extend.form.HtmlEditor = Ext.extend(Ext.form.HtmlEditor, {
    fontFamilies : [
        'Arial',
        'Courier New',
        'Tahoma',
        'Times New Roman',
        'Verdana',
        '宋体'
    ],	
	plugins : [
        new Ext.ux.form.HtmlEditor.Link(),
        new Ext.ux.form.HtmlEditor.Image(),
        new Ext.ux.form.HtmlEditor.Table()
    ]
});
Ext.reg("htmleditor",uft.extend.form.HtmlEditor);