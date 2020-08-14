Ext.namespace('Ext.ux.form.HtmlEditor');
/**
 * @author Shea Frederick - http://www.vinylfox.com
 * @class Ext.ux.form.HtmlEditor.Link
 * @extends Ext.util.Observable
 * <p>A plugin that creates a button on the HtmlEditor for inserting a link.</p>
 */
Ext.ux.form.HtmlEditor.Link = Ext.extend(Ext.util.Observable, {
    // Link language text
    langTitle   : '插入链接',
    langInsert  : '插入',
    langCancel  : '取消',
    langTarget  : '目标',
    langURL     : '链接地址',
    langText    : '显示文本',
    // private
    linkTargetOptions: [['_self', '默认'],['_blank', '新窗口']],
    init: function(cmp){
        cmp.enableLinks = false;
        this.cmp = cmp;
        this.cmp.on('render', this.onRender, this);
    },
    onRender: function(){
        var cmp = this.cmp;
        var btn = this.cmp.getToolbar().addButton({
            iconCls: 'x-edit-createlink',
            handler: function(){
                var sel = this.cmp.getSelectedText();
                    new Ext.Window({
                    	id : '_linkWin',
                        title: this.langTitle,
                        width: 260,
                        height: 160,
                        items: [{
                            xtype: 'form',
                            itemId: 'insert-link',
                            border: false,
                            plain: true,
                            bodyStyle: 'padding: 10px;',
                            labelWidth: 60,
                            labelAlign: 'right',
                            items: [{
                                xtype: 'textfield',
                                fieldLabel: this.langText,
                                name: 'text',
                                anchor: '100%',
                                value: '' 
                            }, {
                                xtype: 'textfield',
                                fieldLabel: this.langURL,
                                vtype: 'url',
                                name: 'url',
                                anchor: '100%',
                                value: 'http://'
                            }, {
                                xtype: 'combo',
                                fieldLabel: this.langTarget,
                                name: 'target',
                                forceSelection: true,
                                mode: 'local',
                                store: new Ext.data.ArrayStore({
                                    autoDestroy: true,
                                    fields: ['spec', 'val'],
                                    data: this.linkTargetOptions
                                }),
                                triggerAction: 'all',
                                value: '_blank',
                                displayField: 'val',
                                valueField: 'spec',
                                anchor: '100%'
                            }]
                        }],
                        buttons: [{
                            text: this.langInsert,
                            handler: function(){
                                var frm = Ext.getCmp('_linkWin').getComponent('insert-link').getForm();
                                if (frm.isValid()) {
                                    var afterSpace = '', sel = this.cmp.getSelectedText(true), text = frm.findField('text').getValue(), url = frm.findField('url').getValue(), target = frm.findField('target').getValue();
                                    if (text.length && text[text.length - 1] == ' ') {
                                        text = text.substr(0, text.length - 1);
                                        afterSpace = ' ';
                                    }
                                    if (sel.hasHTML) {
                                        text = sel.html;
                                    }
                                    var html = '<a href="' + url + '" target="' + target + '">' + text + '</a>' + afterSpace;
                                    this.cmp.insertAtCursor(html);
                                    Ext.getCmp('_linkWin').close();
                                } else {
                                    if (!frm.findField('url').isValid()) {
                                        frm.findField('url').getEl().frame();
                                    } else if (!frm.findField('target').isValid()) {
                                        frm.findField('target').getEl().frame();
                                    }
                                }
                                
                            },
                            scope: this
                        }, {
                            text: this.langCancel,
                            handler: function(){
                                Ext.getCmp('_linkWin').close();
                            },
                            scope: this
                        }],
                        listeners: {
                            show: {
                                fn: function(){
                                    var frm = Ext.getCmp('_linkWin').getComponent('insert-link').getForm();
                                    frm.findField('text').setValue(sel.textContent).setDisabled(sel.hasHTML);
//                                    frm.findField('url').reset().focus(true, 50);
                                },
                                scope: this,
                                defer: 350
                            }
                        }
                    }).show();
            },
            scope: this,
            tooltip: this.langTitle
        });
    }
});
