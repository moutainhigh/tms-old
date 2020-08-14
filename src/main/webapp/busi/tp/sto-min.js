Ext.namespace("uft.tp");
uft.tp.SegmentWindow = Ext
		.extend(
				Ext.Window,
				{
					pk_segment : null,
					max_req_deli_date : null,
					min_req_arri_date : null,
					grid : null,
					DATETIME_FORMAT : uft.jf.Constants.DATETIME_FORMAT,
					DATE_FORMAT : uft.jf.Constants.DATE_FORMAT,
					constructor : function(d) {
						Ext.apply(this, d);
						var c = [
								{
									header : "pk_address",
									hidden : true,
									dataIndex : "pk_address"
								},
								{
									header : "pk_city",
									hidden : true,
									dataIndex : "pk_city"
								},
								{
									xtype : "refcolumn",
									header : '<span class="uft-grid-header-column">地址编码</span>',
									dataIndex : "addr_code",
									editor : {
										xtype : "bodyreffield",
										refName : "地址档案-web",
										idcolname : "pk_address",
										refWindow : {
											model : 1,
											leafflag : false,
											gridDataUrl : ctxPath
													+ "/ref/common/addr/load4Grid.json",
											extGridColumnDescns : [ {
												type : "string",
												width : 120,
												dataIndex : "pk_address",
												xtype : "gridcolumn",
												hidden : true
											}, {
												header : "地址编码",
												type : "string",
												width : 120,
												dataIndex : "addr_code",
												sortable : true,
												xtype : "gridcolumn"
											}, {
												header : "地址名称",
												type : "string",
												width : 120,
												dataIndex : "addr_name",
												sortable : true,
												xtype : "gridcolumn"
											}, {
												header : "地址类型",
												type : "string",
												width : 120,
												dataIndex : "addr_type_name",
												sortable : true,
												xtype : "gridcolumn"
											}, {
												header : "备注",
												type : "string",
												width : 120,
												dataIndex : "memo",
												sortable : true,
												xtype : "gridcolumn"
											}, {
												type : "string",
												width : 120,
												dataIndex : "addr_type",
												sortable : true,
												xtype : "gridcolumn",
												hidden : true
											} ]
										},
										pkField : "pk_address",
										codeField : "addr_code",
										nameField : "addr_name",
										showCodeOnBlur : true,
										getByPkUrl : ctxPath
												+ "/ref/common/addr/getByPk.do",
										getByCodeUrl : ctxPath
												+ "/ref/common/addr/getByCode.do"
									}
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">地址名称</span>',
									dataIndex : "addr_name",
									editable : false
								},
								{
									xtype : "refcolumn",
									header : '<span class="uft-grid-header-column-not-edit">城市</span>',
									dataIndex : "city_name",
									editable : false
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">详细地址</span>',
									dataIndex : "detail_addr",
									editable : false
								},
								{
									xtype : "datetimecolumn",
									width : 130,
									header : '<span class="uft-grid-header-column-not-null">要求到达日期</span>',
									dataIndex : "req_arri_date",
									editor : {
										id : "_req_arri_date",
										xtype : "datetimefield",
										maxLength : 200
									}
								}, {
									xtype : "datetimecolumn",
									width : 130,
									header : "要求离开日期",
									dataIndex : "req_deli_date",
									editor : {
										xtype : "datetimefield",
										maxLength : 200
									}
								} ];
						var g = [ {
							name : "pk_address",
							type : "string"
						}, {
							name : "addr_code",
							type : "string"
						}, {
							name : "pk_city",
							type : "string"
						}, {
							name : "detail_addr",
							type : "string"
						}, {
							name : "req_arri_date",
							type : "string"
						}, {
							name : "req_deli_date",
							type : "string"
						} ];
						this.headerGrid = new uft.extend.grid.EditorGrid({
							isAddBbar : false,
							dragDropRowOrder : true,
							autoExpandColumn : 6,
							recordType : g,
							columns : c
						});
						var h = new Ext.Panel(
								{
									border : false,
									region : "north",
									layout : "fit",
									height : 150,
									items : [ this.headerGrid ],
									tbar : new Ext.Toolbar(
											{
												items : [
														{
															xtype : "button",
															text : "增加节点",
															iconCls : "btnAdd",
															scope : this,
															handler : function() {
																var i = this.headerGrid;
																i.stopEditing();
																i.addRow();
															}
														},
														{
															xtype : "button",
															text : "删除节点",
															iconCls : "btnDel",
															scope : this,
															handler : function() {
																var k = this.headerGrid, j = k
																		.getStore();
																var i = uft.Utils
																		.getSelectedRecord(k);
																if (i) {
																	k
																			.stopEditing();
																	var n = j
																			.indexOf(i);
																	var m = j
																			.getAt(n + 1);
																	var l = j
																			.getAt(n - 1);
																	if (m) {
																		k
																				.getSelectionModel()
																				.selectRow(
																						n + 1);
																	} else {
																		if (l) {
																			k
																					.getSelectionModel()
																					.selectRow(
																							n - 1);
																		}
																	}
																	j.remove(i);
																} else {
																	uft.Utils
																			.showWarnMsg("请先选中要删除的记录！");
																	return false;
																}
															}
														} ]
											})
								});
						var f = [
								{
									header : "pk_segment",
									hidden : true,
									dataIndex : "pk_segment"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">发货单号</span>',
									dataIndex : "invoice_vbillno"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">运段号</span>',
									dataIndex : "vbillno"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">起始地</span>',
									hidden : true,
									dataIndex : "pk_delivery"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">起始地</span>',
									dataIndex : "pk_delivery_name"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">起始地城市</span>',
									dataIndex : "deli_city"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">目的地</span>',
									hidden : true,
									dataIndex : "pk_arrival"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">目的地</span>',
									dataIndex : "pk_arrival_name"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">目的地城市</span>',
									dataIndex : "arri_city"
								},
								{
									xtype : "datetimecolumn",
									header : '<span class="uft-grid-header-column-not-edit">要求提货日期</span>',
									width : 125,
									dataIndex : "req_deli_date"
								},
								{
									xtype : "datetimecolumn",
									header : '<span class="uft-grid-header-column-not-edit">要求到货日期</span>',
									width : 125,
									dataIndex : "req_arri_date"
								} ];
						var b = [ {
							name : "pk_segment",
							type : "string"
						}, {
							name : "invoice_vbillno",
							type : "string"
						}, {
							name : "vbillno",
							type : "string"
						}, {
							name : "pk_delivery",
							type : "string"
						}, {
							name : "pk_delivery_name",
							type : "string"
						}, {
							name : "deli_city",
							type : "string"
						}, {
							name : "pk_arrival",
							type : "string"
						}, {
							name : "pk_arrival_name",
							type : "string"
						}, {
							name : "arri_city",
							type : "string"
						}, {
							name : "req_deli_date",
							type : "string"
						}, {
							name : "req_arri_date",
							type : "string"
						} ];
						this.bodyGrid = new uft.extend.grid.BasicGrid({
							isAddBbar : false,
							immediatelyLoad : true,
							dataUrl : "loadByPKs.json",
							params : {
								pk_segment : this.pk_segment
							},
							recordType : b,
							columns : f
						});
						var a = new Ext.TabPanel({
							border : false,
							region : "center",
							activeTab : 0,
							items : [ {
								layout : "fit",
								title : "所选订单",
								items : [ this.bodyGrid ]
							} ]
						});
						var e = [ new Ext.Button({
							iconCls : "btnYes",
							text : "保&nbsp;&nbsp;存",
							actiontype : "submit",
							scope : this,
							handler : this.saveAction
						}), new Ext.Button({
							iconCls : "btnCancel",
							text : "取&nbsp;&nbsp;消",
							scope : this,
							handler : function() {
								this.close();
							}
						}) ];
						uft.tp.SegmentWindow.superclass.constructor.call(this,
								{
									title : "拆段",
									width : 900,
									height : 400,
									collapsible : false,
									frame : true,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "border",
									items : [ h, a ],
									buttons : e
								});
						this.registerAfterEditEvent();
					},
					onRender : function(b, a) {
						uft.tp.SegmentWindow.superclass.onRender.call(this, b,
								a);
						this.headerGrid.addRow();
					},
					registerAfterEditEvent : function() {
						this.headerGrid
								.addListener(
										"afteredit",
										function(h) {
											var g = h.record;
											var o = h.field;
											if (o == "addr_code") {
												var b = g.get("pk_address");
												var c = this.bodyGrid
														.getStore();
												for ( var f = 0; f < c
														.getCount(); f++) {
													var q = c.getAt(f);
													var p = q
															.get("pk_delivery");
													var k = q.get("pk_arrival");
													if (b == p || b == k) {
														g.set("pk_address",
																null);
														g
																.set(
																		"addr_code",
																		null);
														g
																.set(
																		"addr_name",
																		null);
														uft.Utils
																.showWarnMsg("拆段节点不能是运段的提货方或者收货方！");
														return;
													}
												}
												var n = Utils
														.doSyncRequest(
																"afterEditAddrCode.json",
																{
																	pk_address : b
																}, "POST");
												if (n) {
													for ( var m in n) {
														g.beginEdit();
														g.set(m.trim(), n[m]);
														g.endEdit();
													}
												}
											} else {
												if (o == "req_deli_date") {
													var d = g
															.get("req_deli_date");
													var l = g
															.get("req_arri_date");
													if (d) {
														if (!d instanceof Date) {
															d = Date
																	.parseDate(
																			d,
																			this.DATETIME_FORMAT);
														}
														var j = d
																.dateFormat(this.DATETIME_FORMAT);
														j = Date
																.parseDate(
																		j
																				.substring(
																						0,
																						10),
																		this.DATE_FORMAT);
														if (j < this.max_req_deli_date
																.clearTime()) {
															g
																	.set(
																			"req_deli_date",
																			null);
															uft.Utils
																	.showWarnMsg("要求离开日期必须大于等于您选择的所有运段中最大的提货日期！");
															return;
														} else {
															if (j > this.min_req_arri_date
																	.clearTime()) {
																g
																		.set(
																				"req_deli_date",
																				null);
																uft.Utils
																		.showWarnMsg("要求离开日期必须小于等于您选择的所有运段中最小的到货日期！");
																return;
															}
														}
														if (l) {
															if (!l instanceof Date) {
																l = Date
																		.parseDate(
																				l,
																				this.DATETIME_FORMAT);
															}
															if (d < l) {
																g
																		.set(
																				"req_deli_date",
																				null);
																uft.Utils
																		.showWarnMsg("要求离开日期必须大于等于要求到达日期！");
																return;
															}
														}
													}
												} else {
													if (o == "req_arri_date") {
														var l = g
																.get("req_arri_date");
														var d = g
																.get("req_deli_date");
														if (l) {
															if (!l instanceof Date) {
																l = Date
																		.parseDate(
																				l,
																				this.DATETIME_FORMAT);
															}
															var j = l
																	.dateFormat(this.DATETIME_FORMAT);
															j = Date
																	.parseDate(
																			j
																					.substring(
																							0,
																							10),
																			this.DATE_FORMAT);
															if (j > this.min_req_arri_date
																	.clearTime()) {
																g
																		.set(
																				"req_arri_date",
																				null);
																uft.Utils
																		.showWarnMsg("要求到达日期必须小于您选择的所有运段中最小的到货日期！");
																return;
															} else {
																if (j < this.max_req_deli_date
																		.clearTime()) {
																	g
																			.set(
																					"req_arri_date",
																					null);
																	uft.Utils
																			.showWarnMsg("要求到达日期必须大于您选择的所有运段中最大的提货日期！");
																	return;
																}
															}
															if (d) {
																if (!d instanceof Date) {
																	d = Date
																			.parseDate(
																					d,
																					this.DATETIME_FORMAT);
																}
																if (d < l) {
																	g
																			.set(
																					"req_arri_date",
																					null);
																	uft.Utils
																			.showWarnMsg("要求离开日期必须大于等于要求到达日期！");
																	return;
																}
															}
														}
													}
												}
											}
										}, this);
					},
					saveAction : function() {
						this.headerGrid.stopEditing();
						var l = this.headerGrid.getStore(), j = l.getCount();
						var c = [], e = [], a = [];
						for ( var g = 0; g < j; g++) {
							var h = l.getAt(g);
							var b = h.get("pk_address");
							var k = uft.Utils
									.getColumnValue(h, "req_arri_date");
							var d = uft.Utils
									.getColumnValue(h, "req_deli_date");
							if (!b) {
								uft.Utils.showWarnMsg("第" + (g + 1)
										+ "行的地址编码不能为空！");
								return;
							}
							if (!k) {
								uft.Utils.showWarnMsg("第" + (g + 1)
										+ "行的要求到货日期不能为空！");
								return;
							}
							if (!d) {
								d = k;
							}
							c.push(b);
							if (k instanceof Date) {
								k = k.dateFormat(this.DATETIME_FORMAT);
							}
							if (d instanceof Date) {
								d = d.dateFormat(this.DATETIME_FORMAT);
							}
							e.push(k);
							a.push(d);
						}
						if (c.length == 0) {
							uft.Utils.showWarnMsg("请先增加节点！");
							return;
						}
						l = this.bodyGrid.getStore();
						j = l.getCount();
						var f = [];
						for ( var g = 0; g < j; g++) {
							var h = l.getAt(g);
							f.push(h.get("pk_segment"));
						}
						if (f.length == 0) {
							uft.Utils.showWarnMsg("你没有选择任何运段！");
							return;
						}
						uft.Utils.doAjax({
							scope : this,
							params : {
								HEADER : c,
								BODY : f,
								req_arri_date : e,
								req_deli_date : a
							},
							url : "distSection.json",
							success : function(i) {
								if (this.grid) {
									this.grid.getStore().reload();
								}
								this.close();
							}
						});
					}
				});
Ext.namespace("uft.tp");
uft.tp.QuantityWindow = Ext
		.extend(
				Ext.Window,
				{
					pk_segment : null,
					grid : null,
					constructor : function(b) {
						Ext.apply(this, b);
						var a = [
								{
									header : "pk_seg_pack_b",
									hidden : true,
									dataIndex : "pk_seg_pack_b"
								},
								{
									header : "pk_segment",
									hidden : true,
									dataIndex : "pk_segment"
								},
								{
									header : "pk_goods",
									hidden : true,
									dataIndex : "pk_goods"
								},
								{
									header : "unit_weight",
									hidden : true,
									dataIndex : "unit_weight"
								},
								{
									header : "unit_volume",
									hidden : true,
									dataIndex : "unit_volume"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">货品编码</span>',
									dataIndex : "goods_code"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit">货品名称</span>',
									dataIndex : "goods_name"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit"><center>件数</center></span>',
									dataIndex : "num",
									width : 80,
									align : "right"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit"><center>重量</center></span>',
									dataIndex : "weight",
									width : 80,
									align : "right"
								},
								{
									header : '<span class="uft-grid-header-column-not-edit"><center>体积</center></span>',
									dataIndex : "volume",
									width : 80,
									align : "right"
								},
								{
									header : "单位数量",
									hidden : true,
									dataIndex : "pack_num"
								},
								{
									header : '<span class="uft-grid-header-column"><center>拆分数量</center></span>',
									dataIndex : "dist_pack_num_count",
									width : 80,
									align : "right",
									editor : {
										xtype : "uftnumberfield",
										allowBlank : true,
										maxLength : 200,
										decimalPrecision : 0
									}
								},
								{
									header : '<span class="uft-grid-header-column"><center>拆分件数</center></span>',
									dataIndex : "dist_num",
									width : 80,
									align : "right",
									editor : {
										xtype : "uftnumberfield",
										allowBlank : true,
										maxLength : 200,
										decimalPrecision : 0
									},
									beforeRenderer : function(g, h, f) {
										h.css = "css11";
									}
								},
								{
									header : '<span class="uft-grid-header-column"><center>拆分重量</center></span>',
									dataIndex : "dist_weight",
									width : 80,
									align : "right",
									editor : {
										xtype : "uftnumberfield",
										allowBlank : true,
										maxLength : 200,
										decimalPrecision : 2
									}
								},
								{
									header : '<span class="uft-grid-header-column"><center>拆分体积</center></span>',
									dataIndex : "dist_volume",
									align : "right",
									width : 80,
									editor : {
										xtype : "uftnumberfield",
										allowBlank : true,
										maxLength : 200,
										decimalPrecision : 2
									}
								} ];
						var d = [ {
							name : "pk_seg_pack_b",
							type : "string"
						}, {
							name : "pk_segment",
							type : "string"
						}, {
							name : "pk_goods",
							type : "string"
						}, {
							name : "unit_weight",
							type : "float"
						}, {
							name : "unit_volume",
							type : "float"
						}, {
							name : "goods_code",
							type : "string"
						}, {
							name : "goods_name",
							type : "string"
						}, {
							name : "pack_num",
							type : "int"
						}, {
							name : "dist_pack_num_count",
							type : "int"
						}, {
							name : "num",
							type : "int"
						}, {
							name : "weight",
							type : "float"
						}, {
							name : "volume",
							type : "float"
						}, {
							name : "dist_num",
							type : "float"
						}, {
							name : "dist_weight",
							type : "float"
						}, {
							name : "dist_volume",
							type : "float"
						} ];
						this.headerGrid = new uft.extend.grid.EditorGrid({
							isAddBbar : false,
							immediatelyLoad : true,
							dataUrl : "loadSegPackByParent.json",
							params : {
								pk_segment : this.pk_segment
							},
							recordType : d,
							columns : a
						});
						var e = new Ext.Panel({
							border : false,
							region : "north",
							layout : "fit",
							items : [ this.headerGrid ]
						});
						var c = [ new Ext.Button({
							iconCls : "btnYes",
							text : "保&nbsp;&nbsp;存",
							actiontype : "submit",
							scope : this,
							handler : this.saveAction
						}), new Ext.Button({
							iconCls : "btnCancel",
							text : "取&nbsp;&nbsp;消",
							scope : this,
							handler : function() {
								this.destroy();
							}
						}) ];
						uft.tp.QuantityWindow.superclass.constructor.call(this,
								{
									title : "拆量",
									width : 850,
									height : 400,
									collapsible : false,
									frame : true,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "fit",
									items : [ e ],
									buttons : c
								});
						this.registerAfterEditEvent();
					},
					registerAfterEditEvent : function() {
						this.headerGrid
								.on(
										"afteredit",
										function(j) {
											var i = j.record;
											var m = j.field;
											if (m == "dist_num") {
												var h = uft.Utils
														.getNumberColumnValue(
																i, "num");
												var n = uft.Utils
														.getNumberColumnValue(
																i, "dist_num");
												var b = uft.Utils
														.getNumberColumnValue(
																i,
																"unit_weight");
												var c = uft.Utils
														.getNumberColumnValue(
																i,
																"unit_volume");
												if (n < 0 || n > h) {
													uft.Utils
															.showWarnMsg("拆分件数必须大于等于0并且小于等于总件数！");
													uft.Utils.setColumnValue(i,
															"dist_num", 0);
													return;
												}
												i.beginEdit();
												i.set("dist_weight", n * b);
												i.set("dist_volume", n * c);
												var d = uft.Utils
														.getNumberColumnValue(
																i, "pack_num");
												uft.Utils.setColumnValue(i,
														"dist_pack_num_count",
														d * n);
												i.endEdit();
											} else {
												if (m == "dist_weight") {
													var l = uft.Utils
															.getNumberColumnValue(
																	i,
																	"dist_weight");
													var g = uft.Utils
															.getNumberColumnValue(
																	i, "weight");
													if (l > g) {
														uft.Utils
																.showWarnMsg("拆分重量不能大于总重量！");
														uft.Utils
																.setColumnValue(
																		i,
																		"dist_weight",
																		0);
														return;
													}
												} else {
													if (m == "dist_volume") {
														var k = uft.Utils
																.getNumberColumnValue(
																		i,
																		"dist_volume");
														var f = uft.Utils
																.getNumberColumnValue(
																		i,
																		"volume");
														if (k > f) {
															uft.Utils
																	.showWarnMsg("拆分体积不能大于总体积！");
															uft.Utils
																	.setColumnValue(
																			i,
																			"dist_volume",
																			0);
															return;
														}
													}
												}
											}
										}, this);
						var a = this.headerGrid.getStore();
						a.on("load", function(b) {
							if (a.getCount() == 0) {
								Ext.Msg.show({
									title : "提示",
									msg : "该运段没有可拆分的货品包装明细！",
									buttons : Ext.Msg.OK,
									icon : Ext.Msg.INFO
								});
							}
						}, this);
					},
					saveAction : function() {
						this.headerGrid.stopEditing();
						var f = this.headerGrid.getStore(), p = f.getCount();
						var g = true;
						var a = [], j = [], k = [], e = [], d = [];
						for ( var o = 0; o < p; o++) {
							var c = f.getAt(o);
							var q = uft.Utils
									.getColumnValue(c, "pk_seg_pack_b");
							var m = uft.Utils.getNumberColumnValue(c,
									"dist_pack_num_count");
							var r = uft.Utils.getNumberColumnValue(c,
									"dist_num");
							var s = uft.Utils.getNumberColumnValue(c,
									"dist_weight");
							var b = uft.Utils.getNumberColumnValue(c,
									"dist_volume");
							var h = uft.Utils.getNumberColumnValue(c, "num");
							var l = uft.Utils.getNumberColumnValue(c, "weight");
							var n = uft.Utils.getNumberColumnValue(c, "volume");
							if (r < 0 || r > h) {
								uft.Utils.showWarnMsg("第" + (o + 1)
										+ "行的拆分件数必须大于等于0，并且小于等于总件数！");
								uft.Utils.setColumnValue(c, "dist_num", 0);
								return;
							}
							if (p == 1) {
								if (r == 0 || r == h) {
									uft.Utils
											.showWarnMsg("拆分件数必须不等于0，并且不等于总件数！");
									uft.Utils.setColumnValue(c, "dist_num", 0);
									return;
								}
							}
							if (s > l) {
								uft.Utils.showWarnMsg("第" + (o + 1)
										+ "行的拆分重量不能大于总重量！");
								return;
							}
							if (b > n) {
								uft.Utils.showWarnMsg("第" + (o + 1)
										+ "行的拆分体积不能大于总体积！");
								return;
							}
							if (r != 0 || s != 0 || b != 0) {
								g = false;
							}
							a.push(q);
							j.push(m);
							k.push(r);
							e.push(s);
							d.push(b);
						}
						if (g) {
							uft.Utils.showWarnMsg("拆分件数、拆分重量和拆分体积不能全部为0！");
							return;
						}
						uft.Utils.doAjax({
							scope : this,
							params : {
								pk_segment : this.pk_segment,
								pk_seg_pack_b : a,
								dist_pack_num_count : j,
								dist_num : k,
								dist_weight : e,
								dist_volume : d
							},
							url : "distQuantity.json",
							success : function(i) {
								if (this.grid) {
									this.grid.getStore().reload();
								}
								this.close();
							}
						});
					}
				});
Ext.namespace("uft.tp");
uft.tp.BatchPZ = Ext
		.extend(
				Ext.Window,
				{
					constructor : function(a) {
						Ext.apply(this, a);
						if (!this.records) {
							uft.Utils.showErrorMsg("请先选中记录！");
							return false;
						}
						this.formPanel = new uft.extend.form.FormPanel(
								{
									height : 225,
									labelWidth : 80,
									autoScroll : true,
									border : false,
									items : [ {
										layout : "tableform",
										layoutConfig : {
											columns : 3
										},
										border : false,
										padding : "5px 5px 0",
										defaults : {
											anchor : "95%",
											xtype : "textfield"
										},
										items : [
												{
													refName : "车辆档案-web",
													xtype : "headerreffield",
													name : "carno",
													fieldLabel : "车牌号",
													colspan : 1,
													pkBilltemplet : "0001A410000000000O0Y",
													pkBilltempletB : "0001A410000000000O0Z",
													hasEditformula : true,
													refWindow : {
														model : 1,
														leafflag : false,
														params : "refName:'车辆档案-web'",
														gridDataUrl : ctxPath
																+ "/ref/common/car/load4Grid.json",
														extGridColumnDescns : [
																{
																	type : "string",
																	width : 120,
																	dataIndex : "carno",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	header : "车牌号",
																	type : "string",
																	width : 120,
																	dataIndex : "carno",
																	xtype : "gridcolumn"
																},
																{
																	header : "车辆类型",
																	type : "string",
																	width : 120,
																	dataIndex : "car_type_name",
																	xtype : "gridcolumn"
																},
																{
																	header : "车辆性质",
																	type : "string",
																	width : 120,
																	dataIndex : "car_prop_name",
																	xtype : "gridcolumn"
																},
																{
																	type : "string",
																	width : 120,
																	dataIndex : "pk_car_type",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	type : "string",
																	width : 120,
																	dataIndex : "car_prop",
																	xtype : "gridcolumn",
																	hidden : true
																} ]
													},
													pkField : "carno",
													codeField : "carno",
													nameField : "carno",
													fillinable : true,
													showCodeOnBlur : false,
													getByPkUrl : ctxPath
															+ "/ref/common/car/getByPk.do",
													getByCodeUrl : ctxPath
															+ "/ref/common/car/getByCode.do"
												},
												{
													refName : "承运商档案-web",
													allowBlank : false,
													xtype : "headerreffield",
													name : "pk_carrier",
													fieldLabel : "承运商",
													itemCls : "uft-form-label-not-null",
													colspan : 1,
													script : "refreshPayDetail();alert(1)",
													hasEditformula : true,
													refWindow : {
														model : 1,
														leafflag : false,
														params : "refName:'承运商档案-web'",
														gridDataUrl : ctxPath
																+ "/ref/common/carr/load4Grid.json",
														extGridColumnDescns : [
																{
																	type : "string",
																	width : 120,
																	dataIndex : "pk_carrier",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	header : "承运商编码",
																	type : "string",
																	width : 120,
																	dataIndex : "carr_code",
																	xtype : "gridcolumn"
																},
																{
																	header : "承运商名称",
																	type : "string",
																	width : 120,
																	dataIndex : "carr_name",
																	xtype : "gridcolumn"
																},
																{
																	header : "承运商类型",
																	type : "string",
																	width : 120,
																	dataIndex : "carr_type",
																	xtype : "gridcolumn"
																},
																{
																	header : "备注",
																	type : "string",
																	width : 120,
																	dataIndex : "memo",
																	xtype : "gridcolumn"
																} ]
													},
													pkField : "pk_carrier",
													codeField : "carr_code",
													nameField : "carr_name",
													showCodeOnBlur : false,
													getByPkUrl : ctxPath
															+ "/ref/common/carr/getByPk.do",
													getByCodeUrl : ctxPath
															+ "/ref/common/carr/getByCode.do"
												},
												{
													refName : "司机档案-web",
													xtype : "headerreffield",
													name : "pk_driver",
													fieldLabel : "司机",
													colspan : 1,
													refWindow : {
														model : 1,
														leafflag : false,
														params : "refName:'司机档案-web'",
														gridDataUrl : ctxPath
																+ "/ref/common/driver2/load4Grid.json",
														extGridColumnDescns : [
																{
																	type : "string",
																	width : 120,
																	dataIndex : "pk_driver",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	header : "司机编码",
																	type : "string",
																	width : 120,
																	dataIndex : "driver_code",
																	xtype : "gridcolumn"
																},
																{
																	header : "司机名称",
																	type : "string",
																	width : 120,
																	dataIndex : "driver_name",
																	xtype : "gridcolumn"
																},
																{
																	header : "备注",
																	type : "string",
																	width : 120,
																	dataIndex : "memo",
																	xtype : "gridcolumn"
																} ]
													},
													pkField : "driver_name",
													codeField : "driver_name",
													nameField : "driver_name",
													fillinable : true,
													showCodeOnBlur : false,
													getByPkUrl : ctxPath
															+ "/ref/common/driver2/getByPk.do",
													getByCodeUrl : ctxPath
															+ "/ref/common/driver2/getByCode.do"
												},
												{
													refName : "车辆类型-web",
													xtype : "headerreffield",
													name : "pk_car_type",
													fieldLabel : "车型",
													colspan : 1,
													script : "refreshPayDetail()",
													refWindow : {
														model : 1,
														leafflag : false,
														params : "refName:'车辆类型-web'",
														gridDataUrl : ctxPath
																+ "/ref/common/cartype/load4Grid.json",
														extGridColumnDescns : [
																{
																	type : "string",
																	width : 120,
																	dataIndex : "pk_car_type",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	header : "类型编码",
																	type : "string",
																	width : 120,
																	dataIndex : "code",
																	xtype : "gridcolumn"
																},
																{
																	header : "类型名称",
																	type : "string",
																	width : 120,
																	dataIndex : "name",
																	xtype : "gridcolumn"
																},
																{
																	header : "备注",
																	type : "string",
																	width : 120,
																	dataIndex : "memo",
																	xtype : "gridcolumn"
																} ]
													},
													pkField : "pk_car_type",
													codeField : "code",
													nameField : "name",
													showCodeOnBlur : false,
													getByPkUrl : ctxPath
															+ "/ref/common/cartype/getByPk.do",
													getByCodeUrl : ctxPath
															+ "/ref/common/cartype/getByCode.do"
												},
												{
													refName : "运输方式-web",
													allowBlank : false,
													xtype : "headerreffield",
													name : "pk_trans_type",
													fieldLabel : "运输方式",
													itemCls : "uft-form-label-not-null",
													colspan : 1,
													script : "updateHeaderFeeWeightCount();refreshPayDetail()",
													refWindow : {
														model : 1,
														leafflag : false,
														params : "refName:'运输方式-web'",
														gridDataUrl : ctxPath
																+ "/ref/common/transtype/load4Grid.json",
														extGridColumnDescns : [
																{
																	type : "string",
																	width : 120,
																	dataIndex : "pk_trans_type",
																	xtype : "gridcolumn",
																	hidden : true
																},
																{
																	header : "运输方式编码",
																	type : "string",
																	width : 120,
																	dataIndex : "code",
																	xtype : "gridcolumn"
																},
																{
																	header : "运输方式名称",
																	type : "string",
																	width : 120,
																	dataIndex : "name",
																	xtype : "gridcolumn"
																},
																{
																	header : "备注",
																	type : "string",
																	width : 120,
																	dataIndex : "memo",
																	xtype : "gridcolumn"
																} ]
													},
													pkField : "pk_trans_type",
													codeField : "code",
													nameField : "name",
													showCodeOnBlur : false,
													getByPkUrl : ctxPath
															+ "/ref/common/transtype/getByPk.do",
													getByCodeUrl : ctxPath
															+ "/ref/common/transtype/getByCode.do"
												},
												{
													name : "balatype",
													fieldLabel : "结算方式",
													xtype : "localcombo",
													colspan : 1,
													hidden : false,
													maxLength : 200,
													store : {
														fields : [ "text",
																"value" ],
														data : [
																[ "&nbsp;", "" ],
																[ "月结", "0" ],
																[ "类型2", "1" ] ],
														xtype : "arraystore"
													}
												}, {
													name : "gps_id",
													fieldLabel : "GPS_ID",
													xtype : "textfield",
													colspan : 1,
													maxLength : 50
												}, {
													name : "memo",
													fieldLabel : "备注",
													xtype : "textfield",
													colspan : 4,
													maxLength : 200
												} ]
									} ]
								});
						this.fieldMap = {};
						this.formPanel.getForm().items.each(function(b) {
							alert(1);
							this.fieldMap[b.name] = b;
							b.on("change", function(g, f, d) {
								alert(2);
								if (g.hasEditformula === true) {
									var h = this.formPanel.getForm()
											.getFieldValues(false);
									h.pkBilltemplet = g.pkBilltemplet;
									h.pkBilltempletB = g.pkBilltempletB;
									var c = Ext.getBody();
									c.mask(uft.jf.Constants.PROCESS_MSG);
									var e = Utils.doSyncRequest(
											"execFormula.json", h, "POST");
									this.setRetObj(e);
									c.unmask();
								}
							}, this);
						}, this);
						uft.tp.BatchPZ.superclass.constructor.call(this, {
							title : "批量排单",
							width : 700,
							height : 300,
							closable : true,
							draggable : true,
							resizable : true,
							modal : true,
							border : false,
							layout : "fit",
							items : [ this.formPanel ],
							buttons : [ {
								xtype : "button",
								text : "保存",
								iconCls : "btnSave",
								scope : this,
								handler : this.batchPZ
							}, new Ext.Button({
								iconCls : "btnCancel",
								text : "关&nbsp;&nbsp;闭",
								scope : this,
								handler : function() {
									this.close();
								}
							}) ]
						});
					},
					setRetObj : function(b) {
						var a;
						for (a in b) {
							var c = this.fieldMap[a];
							if (c) {
								c.setValue(b[a]);
							}
						}
					},
					batchPZ : function() {
						var e = this.formPanel.getForm();
						var g = this.formPanel.getErrors();
						if (g.length != 0) {
							uft.Utils.showWarnMsg(Utils.arrayToString(g, ""));
							return;
						}
						var c = e.getFieldValues(false);
						var a = [], b = this.records;
						for ( var d = 0; d < b.length; d++) {
							a.push(b[d].get("vbillno"));
						}
						Ext.apply(c, {
							vbillno : a
						});
						var f = this.app.newAjaxParams();
						Ext.apply(f, c);
						uft.Utils.doAjax({
							scope : this,
							method : "POST",
							url : ctxPath + "/tp/pz/batchSave.json",
							params : f,
							success : function(h) {
								this.app.toReload = true;
								this.close();
							}
						});
					}
				});
Ext.namespace("uft.tp");
uft.tp.Delegate = Ext.extend(Ext.Window, {
	constructor : function(a) {
		Ext.apply(this, a);
		if (!this.records) {
			uft.Utils.showErrorMsg("请先选中记录！");
			return false;
		}
		this.formPanel = new uft.extend.form.FormPanel({
			layout : "form",
			frame : true,
			height : 80,
			labelWidth : 60,
			autoScroll : true,
			border : false,
			items : [ {
				refName : "公司目录-web",
				xtype : "headerreffield",
				allowBlank : false,
				name : "pk_corp",
				fieldLabel : "公司",
				width : 150,
				refWindow : {
					model : 0,
					leafflag : false,
					params : "refName:'公司目录-web'",
					treeDataUrl : ctxPath + "/ref/common/corp/load4Tree.json"
				},
				pkField : "pk_corp",
				codeField : "corp_code",
				nameField : "corp_name",
				showCodeOnBlur : false,
				getByPkUrl : ctxPath + "/ref/common/corp/getByPk.do",
				getByCodeUrl : ctxPath + "/ref/common/corp/getByCode.do"
			} ]
		});
		uft.tp.Delegate.superclass.constructor.call(this, {
			title : "委派公司",
			width : 300,
			height : 150,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : "fit",
			items : [ this.formPanel ],
			buttons : [ {
				xtype : "button",
				text : "保存",
				iconCls : "btnSave",
				scope : this,
				handler : this.delegate
			}, new Ext.Button({
				iconCls : "btnCancel",
				text : "关&nbsp;&nbsp;闭",
				scope : this,
				handler : function() {
					this.close();
				}
			}) ]
		});
	},
	delegate : function() {
		var d = this.formPanel.getForm();
		var g = this.formPanel.getErrors();
		if (g.length != 0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(g, ""));
			return;
		}
		var b = d.getFieldValues(false);
		var f = [], a = this.records;
		for ( var c = 0; c < a.length; c++) {
			f.push(a[c].get("pk_segment"));
		}
		Ext.apply(b, {
			billId : f
		});
		var e = this.app.newAjaxParams();
		Ext.apply(e, b);
		uft.Utils.doAjax({
			scope : this,
			method : "POST",
			url : "delegate.json",
			params : e,
			success : function(h) {
				this.app.setHeaderValues(this.records, h.datas);
				if (h.append) {
					uft.Utils.showWarnMsg(h.append);
				}
				this.close();
			}
		});
	}
});

Ext.namespace("uft.tp");
uft.tp.CurrentTracking = Ext
		.extend(
				Ext.Window,
				{
					interval : 60,
					constructor : function(b) {
						Ext.apply(this, b);
						if (!this.carno) {
							uft.Utils.showErrorMsg("请先选中一个车牌号号！");
							return false;
						}
						var a = new Ext.Panel(
								{
									layout : "fit",
									html : '<div id="container" style="width:100%;height:100%;"></div>'
								});
						uft.tp.CurrentTracking.superclass.constructor.call(
								this, {
									id : "CurrentTracking",
									title : "车辆跟踪",
									width : 900,
									height : 500,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "fit",
									buttons : [ new Ext.Button({
										iconCls : "btnCancel",
										text : "关&nbsp;&nbsp;闭",
										scope : this,
										handler : function() {
											this.close();
										}
									}) ],
									items : [ a ]
								});
					},
					show : function() {
						this.doTrack();
						uft.tp.CurrentTracking.superclass.show.call(this);
						window.setUftInterval(function() {
							var a = Ext.fly("secDiv");
							if (!a) {
								return;
							}
							var b = a.select("#secDiv span").elements[0];
							var c = b.innerHTML;
							c = parseInt(c);
							if (c == 0) {
								b.innerHTML = this.interval;
								this.doTrack();
							} else {
								b.innerHTML = String(c - 1);
							}
						}, this, 1000);
					},
					doTrack : function() {
						if (this.map) {
							this.map.clearOverlays();
						}
						uft.Utils
								.doAjax({
									scope : this,
									params : {
										carno : this.carno
									},
									url : ctxPath
											+ "/tp/sto/getCurrentTracking.json",
									isTip : false,
									mask : false,
									success : function(b) {
										if (b && b.result) {
											var f = b.dataset;
											if (f && f.length > 0) {
												var e = f[0];
												var g = new BMap.Point(
														e.longitude, e.latitude);
												var d = new BMap.Icon(
														ctxPath
																+ "/busi/te/images/green_2.gif",
														new BMap.Size(28, 28));
												var a = new BMap.Marker(g, {
													icon : d
												});
												this.map.addOverlay(a);
												this.map.centerAndZoom(g, 15);
												var h = Ext.fly("ct_addr");
												if (!h) {
													return;
												}
												h.update(e.place_name
														+ e.road_name);
												var c = "<ul><li>名称：" + e.gpsid
														+ "</li><li>经度："
														+ e.longitude
														+ "</li><li>纬度："
														+ e.latitude
														+ "</li><li>速度："
														+ e.speed
														+ "</li><li>里程："
														+ e.distance
														+ "</li><li>地址："
														+ e.place_name
														+ e.road_name
														+ "</li><li>定位时间："
														+ e.gps_time
														+ "</li><li>备注："
														+ e.memo
														+ "</li><li>来源："
														+ b.source
														+ "</li></ul>";
												Ext.fly("gpsDetailDiv").update(
														c);
											}
										}
									}
								});
					},
					afterRender : function() {
						uft.tp.CurrentTracking.superclass.afterRender
								.call(this);
						this.map = new BMap.Map("container");
						this.map.addControl(new BMap.NavigationControl());
						this.map.addControl(new BMap.ScaleControl());
						this.map.addControl(new BMap.OverviewMapControl());
						this.map.addControl(new BMap.MapTypeControl());
						this.map.enableScrollWheelZoom(true);
						this.map.centerAndZoom("北京", 5);
						var b = Ext.get("container"), a = Ext.DomHelper;
						a
								.append(
										b,
										"<div id='secDiv'><span>"
												+ this.interval
												+ "</span>秒后刷新    位置：<span id='ct_addr'></span></div>",
										true);
						a
								.append(
										b,
										"<div id='gpsDiv'><div id='gpsDiv1'><h1 style='padding: 5px 0 0 5px;font-size: 14px;'>GPS</h1><HR style='position: inherit;' width='90%' color='#987cb9'/><div id='gpsDetailDiv' style='margin: 15px;line-height: 23px;'></div><div></div>",
										true);
						a
								.append(
										Ext.get("gpsDiv"),
										'<div class="gpsDiv_om omBtnClosed" style="bottom: 0px; left: 0px; top: auto; right: auto;"></div>');
						Ext.query(".gpsDiv_om")[0].onclick = function() {
							var c = Ext.get(this);
							var d = c.hasClass("omBtnClosed");
							if (d) {
								c.removeClass("omBtnClosed");
								c.addClass("omBtn");
							} else {
								c.removeClass("omBtn");
								c.addClass("omBtnClosed");
							}
							Ext.get("gpsDiv1").toggle();
						};
					}
				});