(function($) {
    $.fn.resizable = function(_32, _33) {
        if (typeof _32 == "string") {
            return $.fn.resizable.methods[_32](this, _33);
        }
        function _34(e) {
            var _35 = e.data;
            var _36 = $.data(_35.target, "resizable").options;
            if (_35.dir.indexOf("e") != -1) {
                var _37 = _35.startWidth + e.pageX - _35.startX;
                _37 = Math.min(Math.max(_37, _36.minWidth), _36.maxWidth);
                _35.width = _37;
            }
            if (_35.dir.indexOf("s") != -1) {
                var _38 = _35.startHeight + e.pageY - _35.startY;
                _38 = Math.min(Math.max(_38, _36.minHeight), _36.maxHeight);
                _35.height = _38;
            }
            if (_35.dir.indexOf("w") != -1) {
                _35.width = _35.startWidth - e.pageX + _35.startX;
                if (_35.width >= _36.minWidth && _35.width <= _36.maxWidth) {
                    _35.left = _35.startLeft + e.pageX - _35.startX;
                }
            }
            if (_35.dir.indexOf("n") != -1) {
                _35.height = _35.startHeight - e.pageY + _35.startY;
                if (_35.height >= _36.minHeight && _35.height <= _36.maxHeight) {
                    _35.top = _35.startTop + e.pageY - _35.startY;
                }
            }
        };
        function _39(e) {
            var _3a = e.data;
            var _3b = _3a.target;
            if ($.boxModel == true) {
                $(_3b).css({
                    width: _3a.width - _3a.deltaWidth,
                    height: _3a.height - _3a.deltaHeight,
                    left: _3a.left,
                    top: _3a.top
                });
            } else {
                $(_3b).css({
                    width: _3a.width,
                    height: _3a.height,
                    left: _3a.left,
                    top: _3a.top
                });
            }
        };
        function _3c(e) {
            $.data(e.data.target, "resizable").options.onStartResize.call(e.data.target, e);
            return false;
        };
        function _3d(e) {
            _34(e);
            if ($.data(e.data.target, "resizable").options.onResize.call(e.data.target, e) != false) {
                _39(e);
            }
            return false;
        };
        function _3e(e) {
            _34(e, true);
            _39(e);
            $(document).unbind(".resizable");
            $.data(e.data.target, "resizable").options.onStopResize.call(e.data.target, e);
            return false;
        };
        return this.each(function() {
            var _3f = null;
            var _40 = $.data(this, "resizable");
            if (_40) {
                $(this).unbind(".resizable");
                _3f = $.extend(_40.options, _32 || {});
            } else {
                _3f = $.extend({},
                $.fn.resizable.defaults, _32 || {});
            }
            if (_3f.disabled == true) {
                return;
            }
            $.data(this, "resizable", {
                options: _3f
            }); 
            function _46(css) {
                var val = parseInt($(_41).css(css));
                if (isNaN(val)) {
                    return 0;
                } else {
                    return val;
                }
            };
        });
    };
    $.fn.resizable.methods = {};
    $.fn.resizable.defaults = {
        disabled: false,
        handles: "n, e, s, w, ne, se, sw, nw, all",
        minWidth: 10,
        minHeight: 10,
        maxWidth: 10000,
        maxHeight: 10000,
        edge: 5,
        onStartResize: function(e) {},
        onResize: function(e) {},
        onStopResize: function(e) {}
    };
})(jQuery);(function($) {
    $.parser = {
        auto: true,
        onComplete: function(_153) {},
        plugins: ["linkbutton", "menu", "menubutton", "splitbutton", "tree", "combobox", "combotree", "numberbox", "validatebox", "numberspinner", "timespinner", "calendar", "datebox", "datetimebox", "layout", "panel", "datagrid", "tabs", "accordion", "window", "dialog"],
        parse: function(_154) {
            var aa = [];
            for (var i = 0; i < $.parser.plugins.length; i++) {
                var name = $.parser.plugins[i];
                var r = $(".easyui-" + name, _154);
                if (r.length) {
                    if (r[name]) {
                        r[name]();
                    } else {
                        aa.push({
                            name: name,
                            jq: r
                        });
                    }
                }
            }
            if (aa.length && window.easyloader) {
                var _155 = [];
                for (var i = 0; i < aa.length; i++) {
                    _155.push(aa[i].name);
                }
                easyloader.load(_155,
                function() {
                    for (var i = 0; i < aa.length; i++) {
                        var name = aa[i].name;
                        var jq = aa[i].jq;
                        jq[name]();
                    }
                    $.parser.onComplete.call($.parser, _154);
                });
            } else {
                $.parser.onComplete.call($.parser, _154);
            }
        }
    };
    $(function() {
        if (!window.easyloader && $.parser.auto) {
            $.parser.parse();
        }
    });
})(jQuery); (function($) {
    function _156(node) {
        node.each(function() {
            $(this).remove();
            if ($.browser.msie) {
                this.outerHTML = "";
            }
        });
    };
    function _157(_158, _159) {
        var opts = $.data(_158, "panel").options;
        var _15a = $.data(_158, "panel").panel;
        var _15b = _15a.children("div.panel-header");
        var _15c = _15a.children("div.panel-body");
        if (_159) {
            if (_159.width) {
                opts.width = _159.width;
            }
            if (_159.height) {
                opts.height = _159.height;
            }
            if (_159.left != null) {
                opts.left = _159.left;
            }
            if (_159.top != null) {
                opts.top = _159.top;
            }
        }
        if (opts.fit == true) {
            var p = _15a.parent();
            opts.width = p.width();
            opts.height = p.height();
        }
        _15a.css({
            left: opts.left,
            top: opts.top
        });
        if (!isNaN(opts.width)) {
            if ($.boxModel == true) {
                _15a.width(opts.width - (_15a.outerWidth() - _15a.width()));
            } else {
                _15a.width(opts.width);
            }
        } else {
            _15a.width("auto");
        }
        if ($.boxModel == true) {
            _15b.width(_15a.width() - (_15b.outerWidth() - _15b.width()));
            _15c.width(_15a.width() - (_15c.outerWidth() - _15c.width()));
        } else {
            _15b.width(_15a.width());
            _15c.width(_15a.width());
        }
        if (!isNaN(opts.height)) {
            if ($.boxModel == true) {
                _15a.height(opts.height - (_15a.outerHeight() - _15a.height()));
                _15c.height(_15a.height() - _15b.outerHeight() - (_15c.outerHeight() - _15c.height()));
            } else {
                _15a.height(opts.height);
                _15c.height(_15a.height() - _15b.outerHeight());
            }
        } else {
            _15c.height("auto");
        }
        _15a.css("height", "");
        opts.onResize.apply(_158, [opts.width , opts.height]);
        _15a.find(">div.panel-body>div").triggerHandler("_resize");
    };
    function _15d(_15e, _15f) {
        var opts = $.data(_15e, "panel").options;
        var _160 = $.data(_15e, "panel").panel;
        if (_15f) {
            if (_15f.left != null) {
                opts.left = _15f.left;
            }
            if (_15f.top != null) {
                opts.top = _15f.top;
            }
        }
        _160.css({
            left: opts.left,
            top: opts.top
        });
        opts.onMove.apply(_15e, [opts.left, opts.top]);
    };
    function _161(_162) {
        var _163 = $(_162).addClass("panel-body").wrap("<div class=\"panel\"></div>").parent();
        _163.bind("_resize",
        function() {
            var opts = $.data(_162, "panel").options;
            if (opts.fit == true) {
                _157(_162);
            }
            return false;
        });
        return _163;
    };
    function _164(_165) {
        var opts = $.data(_165, "panel").options;
        var _166 = $.data(_165, "panel").panel;
        _156(_166.find(">div.panel-header"));
        if (opts.title && !opts.noheader) {
            var _167=$("<div class=\"panel-header\"><div class=\"panel-title\"><span class=\"i-all\"></span>"+opts.title+"</div></div>").prependTo(_166);
            var tool = $("<div class=\"panel-tool\"></div>").appendTo(_167);
			if(opts.closable){
				$("<div class=\"panel-tool-close\"></div>").appendTo(tool).bind("click",_168);
				}
				if(opts.maximizable){
				$("<div class=\"panel-tool-max\"></div>").appendTo(tool).bind("click",_169);
				}
				if(opts.minimizable){
				$("<div class=\"panel-tool-min\"></div>").appendTo(tool).bind("click",_16a);
				}
				if(opts.collapsible){
				$("<div class=\"panel-tool-collapse\"></div>").appendTo(tool).bind("click",_16b);
			}
            if (opts.tools) {
                for (var i = opts.tools.length - 1; i >= 0; i--) {
                    var t = $("<div></div>").addClass(opts.tools[i].iconCls).appendTo(tool);
                    if (opts.tools[i].handler) {
                        t.bind("click", eval(opts.tools[i].handler));
                    }
                }
            }
            tool.find("div").hover(function() {
                $(this).addClass("panel-tool-over");
            },
            function() {
                $(this).removeClass("panel-tool-over");
            }); 
        } 
        function _16b() {
            if (opts.collapsed == true) {
                _183(_165, true);
            } else {
                _178(_165, true);
            }
            return false;
        };
        function _16a() {
            _189(_165);
            return false;
        };
        function _169() {
            if (opts.maximized == true) {
                _18c(_165);
            } else {
                _177(_165);
            }
            return false;
        };
        function _168() {
            _16c(_165);
            return false;
        };
    };
    function _16d(_16e) {
        var _16f = $.data(_16e, "panel");
        if (_16f.options.href && (!_16f.isLoaded || !_16f.options.cache)) {
            _16f.isLoaded = false;
            var _170 = _16f.panel.find(">div.panel-body");
            _170.html($("<div class=\"panel-loading\"></div>").html(_16f.options.loadingMessage));
            $.ajax({
                url: _16f.options.href,
                cache: false,
                success: function(data) {
                    _170.html(data);
                    if ($.parser) {
                        $.parser.parse(_170);
                    }
                    _16f.options.onLoad.apply(_16e, arguments);
                    _16f.isLoaded = true;
                }
            });
        }
    };
    function _171(_172) {
        $(_172).find("div.panel:visible,div.accordion:visible,div.tabs-container:visible,div.layout:visible").each(function() {
            $(this).triggerHandler("_resize", [true]);
        });
    };
    function _173(_174, _175) {
        var opts = $.data(_174, "panel").options;
        var _176 = $.data(_174, "panel").panel;
        if (_175 != true) {
            if (opts.onBeforeOpen.call(_174) == false) {
                return;
            }
        }
        _176.show();
        opts.closed = false;
        opts.minimized = false;
        opts.onOpen.call(_174);
        if (opts.maximized == true) {
            opts.maximized = false;
            _177(_174);
        }
        if (opts.collapsed == true) {
            opts.collapsed = false;
            _178(_174);
        }
        if (!opts.collapsed) {
            _16d(_174);
            _171(_174);
        }
    };
    function _16c(_179, _17a) {
        var opts = $.data(_179, "panel").options;
        var _17b = $.data(_179, "panel").panel;
        if (_17a != true) {
            if (opts.onBeforeClose.call(_179) == false) {
                return;
            }
        }
        _17b.hide();
        opts.closed = true;
        opts.onClose.call(_179);
    };
    function _17c(_17d, _17e) {
        var opts = $.data(_17d, "panel").options;
        var _17f = $.data(_17d, "panel").panel;
        if (_17e != true) {
            if (opts.onBeforeDestroy.call(_17d) == false) {
                return;
            }
        }
        _156(_17f);
        opts.onDestroy.call(_17d);
    };
    function _178(_180, _181) {
        var opts = $.data(_180, "panel").options;
        var _182 = $.data(_180, "panel").panel;
        var body = _182.children("div.panel-body");
        if (opts.collapsed == true) {
            return;
        }
        body.stop(true, true);
        if (opts.onBeforeCollapse.call(_180) == false) {
            return;
        }
    };
    function _190(_191) {
        var opts = $.data(_191, "panel").options;
        var _192 = $.data(_191, "panel").panel;
        _192.css(opts.style);
        _192.addClass(opts.cls);
        _192.children("div.panel-header").addClass(opts.headerCls);
        _192.children("div.panel-body").addClass(opts.bodyCls);
    };
    function _193(_194, _195) {
        $.data(_194, "panel").options.title = _195;
        $(_194).panel("header").find("div.panel-title").html(_195);
    };
    var TO = false;
    var _196 = true;
    $(window).unbind(".panel").bind("resize.panel",
    function() {
        if (!_196) {
            return;
        }
        if (TO !== false) {
            clearTimeout(TO);
        }
        TO = setTimeout(function() {
            _196 = false;
            var _197 = $("body.layout");
            if (_197.length) {
                _197.layout("resize");
            } else {
                $("body>div.panel").triggerHandler("_resize");
            }
            _196 = true;
            TO = false;
        },
        0);
    });
    $.fn.panel = function(_198, _199) {
        if (typeof _198 == "string") {
            return $.fn.panel.methods[_198](this, _199);
        }
        _198 = _198 || {};
        return this.each(function() {
            var _19a = $.data(this, "panel");
            var opts;
            if (_19a) {
                opts = $.extend(_19a.options, _198);
            } else {
                opts = $.extend({},
                $.fn.panel.defaults, $.fn.panel.parseOptions(this), _198);
                $(this).attr("title", "");
                _19a = $.data(this, "panel", {
                    options: opts,
                    panel: _161(this),
                    isLoaded: false
                });
            }
            if (opts.content) {
                $(this).html(opts.content);
                if ($.parser) {
                    $.parser.parse(this);
                }
            }
            _164(this);
            _190(this);
            if (opts.doSize == true) {
                _19a.panel.css("display", "block");
                _157(this);
            }
            if (opts.closed == true || opts.minimized == true) {
                _19a.panel.hide();
            } else {
                _173(this);
            }
        });
    };
    $.fn.panel.methods = {
        options: function(jq) {
            return $.data(jq[0], "panel").options;
        },
        panel: function(jq) {
            return $.data(jq[0], "panel").panel;
        },
        header: function(jq) {
            return $.data(jq[0], "panel").panel.find(">div.panel-header");
        },
        body: function(jq) {
            return $.data(jq[0], "panel").panel.find(">div.panel-body");
        },
        setTitle: function(jq, _19b) {
            return jq.each(function() {
                _193(this, _19b);
            });
        },
        open: function(jq, _19c) {
            return jq.each(function() {
                _173(this, _19c);
            });
        },
        close: function(jq, _19d) {
            return jq.each(function() {

                _16c(this, _19d);
            });
        },
        destroy: function(jq, _19e) {
            return jq.each(function() {
                _17c(this, _19e);
            });
        },
        refresh: function(jq, href) {
            return jq.each(function() {
                $.data(this, "panel").isLoaded = false;
                if (href) {
                    $.data(this, "panel").options.href = href;
                }
                _16d(this);
            });
        },
        resize: function(jq, _19f) {
            return jq.each(function() {
                _157(this, _19f);
            });
        },
        move: function(jq, _1a0) {
            return jq.each(function() {
                _15d(this, _1a0);
            });
        },
        maximize: function(jq) {
            return jq.each(function() {
                _177(this);
            });
        },
        minimize: function(jq) {
            return jq.each(function() {
                _189(this);
            });
        },
        restore: function(jq) {
            return jq.each(function() {
                _18c(this);
            });
        },
        collapse: function(jq, _1a1) {
            return jq.each(function() {
                _178(this, _1a1);
            });
        },
        expand: function(jq, _1a2) {
            return jq.each(function() {
                _183(this, _1a2);
            });
        }
    };
    $.fn.panel.parseOptions = function(_1a3) {
        var t = $(_1a3);
        return {
            width: (parseInt(_1a3.style.width) || undefined),
            height: (parseInt(_1a3.style.height) || undefined),
            left: (parseInt(_1a3.style.left) || undefined),
            top: (parseInt(_1a3.style.top) || undefined),
            title: (t.attr("title") || undefined),
            iconCls: (t.attr("iconCls") || t.attr("icon")),
            cls: t.attr("cls"),
            headerCls: t.attr("headerCls"),
            bodyCls: t.attr("bodyCls"),
            href: t.attr("href"),
            cache: (t.attr("cache") ? t.attr("cache") == "true": undefined),
            fit: (t.attr("fit") ? t.attr("fit") == "true": undefined),
            border: (t.attr("border") ? t.attr("border") == "true": undefined),
            noheader: (t.attr("noheader") ? t.attr("noheader") == "true": undefined),
            collapsible: (t.attr("collapsible") ? t.attr("collapsible") == "true": undefined),
            minimizable: (t.attr("minimizable") ? t.attr("minimizable") == "true": undefined),
            maximizable: (t.attr("maximizable") ? t.attr("maximizable") == "true": undefined),
            closable: (t.attr("closable") ? t.attr("closable") == "true": undefined),
            collapsed: (t.attr("collapsed") ? t.attr("collapsed") == "true": undefined),
            minimized: (t.attr("minimized") ? t.attr("minimized") == "true": undefined),
            maximized: (t.attr("maximized") ? t.attr("maximized") == "true": undefined),
            closed: (t.attr("closed") ? t.attr("closed") == "true": undefined)
        };
    };
    $.fn.panel.defaults = {
        title: null,
        iconCls: null,
        width: "auto",
        height: "auto",
        left: null,
        top: null,
        cls: null,
        headerCls: null,
        bodyCls: null,
        style: {},
        href: null,
        cache: true,
        fit: false,
        border: true,
        doSize: true,
        noheader: false,
        content: null,
        collapsible: false,
        minimizable: false,
        maximizable: false,
        closable: false,
        collapsed: false,
        minimized: false,
        maximized: false,
        closed: false,
        tools: [],
        href: null,
        loadingMessage: "Loading...",
        onLoad: function() {},
        onBeforeOpen: function() {},
        onOpen: function() {},
        onBeforeClose: function() {},
        onClose: function() {},
        onBeforeDestroy: function() {},
        onDestroy: function() {},
        onResize: function(_1a4, _1a5) {},
        onMove: function(left, top) {},
        onMaximize: function() {},
        onRestore: function() {},
        onMinimize: function() {},
        onBeforeCollapse: function() {},
        onBeforeExpand: function() {},
        onCollapse: function() {},
        onExpand: function() {}
    };
})(jQuery); (function($) {
    function _220(_221) {
        var _222 = $(">div.tabs-header", _221);
        var _223 = 0;
        $("ul.tabs li", _222).each(function() {
            _223 += $(this).outerWidth(true);
        });
        var _224 = $("div.tabs-wrap", _222).width();
        var _225 = parseInt($("ul.tabs", _222).css("padding-left"));
        return _223 - _224 + _225;
    };
    function _226(_227) {
        var opts = $.data(_227, "tabs").options;
        var _228 = $(_227).children("div.tabs-header");
        var tool = _228.children("div.tabs-tool");
        var _229 = _228.children("div.tabs-scroller-left");
        var _22a = _228.children("div.tabs-scroller-right");
        var wrap = _228.children("div.tabs-wrap");
        var _22b = ($.boxModel == true ? (_228.outerHeight() - (tool.outerHeight() - tool.height())) : _228.outerHeight());
        if (opts.plain) {
            _22b -= 2;
        }
        tool.height(_22b);
        var _22c = 0;
        $("ul.tabs li", _228).each(function() {
            _22c += $(this).outerWidth(true);
        });
        var _22d = _228.width() - tool.outerWidth();
        if (_22c > _22d) {
            _229.show();
            _22a.show();
            tool.css("right", _22a.outerWidth());
            wrap.css({
                marginLeft: _229.outerWidth(),
                marginRight: _22a.outerWidth() + tool.outerWidth(),
                left: 0,
                width: _22d - _229.outerWidth() - _22a.outerWidth()
            });
        } else {
            _229.hide();
            _22a.hide();
            tool.css("right", 0);
            wrap.css({
                marginLeft: 0,
                marginRight: tool.outerWidth(),
                left: 0,
                width: _22d
            });
            wrap.scrollLeft(0);
        }
    };
    function _22e(_22f) {
        var opts = $.data(_22f, "tabs").options;
        var _230 = $(_22f).children("div.tabs-header");
        var _231 = _230.children("div.tabs-tool");
        _231.remove();
        if (opts.tools) {
            _231 = $("<div class=\"tabs-tool\"></div>").appendTo(_230);
            for (var i = 0; i < opts.tools.length; i++) {
                var tool = $("<a></a>").appendTo(_231);
                tool[0].onclick = eval(opts.tools[i].handler ||
                function() {});
                tool.linkbutton($.extend({},
                opts.tools[i], {
                    plain: true
                }));
            }
        }
    };
    function _232(_233) {
        var opts = $.data(_233, "tabs").options;
        var cc = $(_233);
        if (opts.fit == true) {
            var p = cc.parent();
            opts.width = p.width() -5
            opts.height = p.height();
        }
        cc.width(opts.width).height(opts.height);
        var _234 = $(">div.tabs-header", _233);
        if ($.boxModel == true) {
            _234.width(opts.width - (_234.outerWidth() - _234.width()));
        } else {
            _234.width(opts.width);
        }
        _226(_233);
        var _235 = $(">div.tabs-panels", _233);
        var _236 = opts.height;
        if (!isNaN(_236)) {
            if ($.boxModel == true) {
                var _237 = _235.outerHeight() - _235.height();
                _235.css("height", (_236 - _234.outerHeight() - _237) || "auto");
            } else {
                _235.css("height", _236 - _234.outerHeight());
            }
        } else {
            _235.height("auto");
        }
        var _238 = opts.width;
        if (!isNaN(_238)) {
            if ($.boxModel == true) {
                _235.width(_238 - (_235.outerWidth() - _235.width()));
            } else {
                _235.width(_238);
            }
        } else {
            _235.width("auto");
        }
    };
    function _239(_23a) {
        var opts = $.data(_23a, "tabs").options;
        var tab = _23b(_23a);
        if (tab) {
            var _23c = $(_23a).find(">div.tabs-panels");
            var _23d = opts.width == "auto" ? "auto": _23c.width();
            var _23e = opts.height == "auto" ? "auto": _23c.height();
            tab.panel("resize", {
                width: _23d,
                height: _23e
            });
        }
    };
    function _23f(_240) {
        var cc = $(_240);
        cc.addClass("tabs-container");
        cc.wrapInner("<div class=\"tabs-panels\"/>");
        $("<div class=\"tabs-header\">" + "<div class=\"tabs-scroller-left\"></div>" + "<div class=\"tabs-scroller-right\"></div>" + "<div class=\"tabs-wrap\">" + "<ul class=\"tabs\"></ul>" + "</div>" + "</div>").prependTo(_240);
        var tabs = [];
        var _241 = $(">div.tabs-header", _240);
        $(">div.tabs-panels>div", _240).each(function() {
            var pp = $(this);
            tabs.push(pp);
            _24a(_240, pp);
        });
        $(".tabs-scroller-left", _241).hover(function() {
            $(this).addClass("tabs-left-over");
        },
        function() {
            $(this).removeClass("tabs-left-over");
        });
		$(".tabs-scroller-right", _241).hover(function() {
            $(this).addClass("tabs-right-over");
        },
        function() {
            $(this).removeClass("tabs-right-over");
        });
        cc.bind("_resize",
        function(e, _242) {
            var opts = $.data(_240, "tabs").options;
            if (opts.fit == true || _242) {
                _232(_240);
                _239(_240);
            }
            return false;
        });
        return tabs;
    };
    function _243(_244) {
        var opts = $.data(_244, "tabs").options;
        var _245 = $(">div.tabs-header", _244);
        var _246 = $(">div.tabs-panels", _244);
        $(".tabs-scroller-left", _245).unbind(".tabs").bind("click.tabs",
        function() {
            var wrap = $(".tabs-wrap", _245);
            var pos = wrap.scrollLeft() - opts.scrollIncrement;
            wrap.animate({
                scrollLeft: pos
            },
            opts.scrollDuration);
        });
        $(".tabs-scroller-right", _245).unbind(".tabs").bind("click.tabs",
        function() {
            var wrap = $(".tabs-wrap", _245);
            var pos = Math.min(wrap.scrollLeft() + opts.scrollIncrement, _220(_244));
            wrap.animate({
                scrollLeft: pos
            },
            opts.scrollDuration);
        });
        var tabs = $.data(_244, "tabs").tabs;
        for (var i = 0,
        len = tabs.length; i < len; i++) {
            var _247 = tabs[i];
            var tab = _247.panel("options").tab;
            var _248 = _247.panel("options").title;
            tab.unbind(".tabs").bind("click.tabs", {
                title: _248
            },
            function(e) {
                _254(_244, e.data.title);
            }).bind("contextmenu.tabs", {
                title: _248
            },
            function(e) {
                opts.onContextMenu.call(_244, e, e.data.title);
            });
            tab.find("a.tabs-close").unbind(".tabs").bind("click.tabs", {
                title: _248
            },
            function(e) {
                _249(_244, e.data.title);
                return false;
            });
        }
    };
    function _24a(_24b, pp, _24c) {
        _24c = _24c || {};
        pp.panel($.extend({},
        {
            selected: pp.attr("selected") == "true"
        },
        _24c, {
            border: false,
            noheader: true,
            closed: true,
            doSize: false,
            iconCls: (_24c.icon ? _24c.icon: undefined),
            onLoad: function() {
                $.data(_24b, "tabs").options.onLoad.call(_24b, pp);
            }
        }));
        var opts = pp.panel("options");
        var _24d = $(">div.tabs-header", _24b);
        var tabs = $("ul.tabs", _24d);
        var tab = $("<li></li>").appendTo(tabs);
        var _24e = $("<a class=\"tabs-inner\"></a>").appendTo(tab);
        var _24f = $("<span class=\"tabs-title\"></span>").html(opts.title).appendTo(_24e);
        if (opts.closable) {
            _24f.addClass("tabs-closable");
            $("<a class=\"tabs-close\"></a>").appendTo(tab);
        }
		_24f.attr("title",opts.title);
        opts.tab = tab;
    };
    function _251(_252, _253) {
        var opts = $.data(_252, "tabs").options;
        var tabs = $.data(_252, "tabs").tabs;
        var pp = $("<div></div>").appendTo($(">div.tabs-panels", _252));
        tabs.push(pp);
        _24a(_252, pp, _253);
        opts.onAdd.call(_252, _253.title);
        _226(_252);
        _243(_252);
        _254(_252, _253.title);
    };
    function _255(_256, _257) {
        var _258 = $.data(_256, "tabs").selectHis;
        var pp = _257.tab;
        var _259 = pp.panel("options").title;
        pp.panel($.extend({},
        _257.options, {
            iconCls: (_257.options.icon ? _257.options.icon: undefined)
        }));
        var opts = pp.panel("options");
        var tab = opts.tab;
        tab.find("span.tabs-icon").attr("class", "tabs-icon");
        tab.find("a.tabs-close").remove();
        tab.find("span.tabs-title").html(opts.title);
        if (opts.closable) {
            tab.find("span.tabs-title").addClass("tabs-closable");
            $("<a class=\"tabs-close\"></a>").appendTo(tab);
        } else {
            tab.find("span.tabs-title").removeClass("tabs-closable");
        }
        if (_259 != opts.title) {
            for (var i = 0; i < _258.length; i++) {
                if (_258[i] == _259) {
                    _258[i] = opts.title;
                }
            }
        }
        _243(_256);
        $.data(_256, "tabs").options.onUpdate.call(_256, opts.title);
    };
    function _249(_25a, _25b) {
        var opts = $.data(_25a, "tabs").options;
        var tabs = $.data(_25a, "tabs").tabs;
        var _25c = $.data(_25a, "tabs").selectHis;
        if (!_25d(_25a, _25b)) {
            return;
        }
        if (opts.onBeforeClose.call(_25a, _25b) == false) {
            return;
        }
        var tab = _25e(_25a, _25b, true);
        tab.panel("options").tab.remove();
        tab.panel("destroy");
        opts.onClose.call(_25a, _25b);
        _226(_25a);
        for (var i = 0; i < _25c.length; i++) {
            if (_25c[i] == _25b) {
                _25c.splice(i, 1);
                i--;
            }
        }
        var _25f = _25c.pop();
        if (_25f) {
            _254(_25a, _25f);
        } else {
            if (tabs.length) {
                _254(_25a, tabs[0].panel("options").title);
            }
        }
    };
    function _25e(_260, _261, _262) {
        var tabs = $.data(_260, "tabs").tabs;
        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            if (tab.panel("options").title == _261) {
                if (_262) {
                    tabs.splice(i, 1);
                }
                return tab;
            }
        }
        return null;
    };
    function _23b(_263) {
        var tabs = $.data(_263, "tabs").tabs;
        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            if (tab.panel("options").closed == false) {
                return tab;
            }
        }
        return null;
    };
    function _264(_265) {
        var tabs = $.data(_265, "tabs").tabs;
        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            if (tab.panel("options").selected) {
                _254(_265, tab.panel("options").title);
                return;
            }
        }
        if (tabs.length) {
            _254(_265, tabs[0].panel("options").title);
        }
    };
    function _254(_266, _267) {
        var opts = $.data(_266, "tabs").options;
        var tabs = $.data(_266, "tabs").tabs;
        var _268 = $.data(_266, "tabs").selectHis;
        if (tabs.length == 0) {
            return;
        }
        var _269 = _25e(_266, _267);
        if (!_269) {
            return;
        }
        var _26a = _23b(_266);
        if (_26a) {
            _26a.panel("close");
            _26a.panel("options").tab.removeClass("tabs-selected");
        }
        _269.panel("open");
        var tab = _269.panel("options").tab;
        tab.addClass("tabs-selected");
        var wrap = $(_266).find(">div.tabs-header div.tabs-wrap");
        var _26b = tab.position().left + wrap.scrollLeft();
        var left = _26b - wrap.scrollLeft();
        var _26c = left + tab.outerWidth();
        if (left < 0 || _26c > wrap.innerWidth()) {
            var pos = Math.min(_26b - (wrap.width() - tab.width()) / 2, _220(_266));
            wrap.animate({
                scrollLeft: pos
            },
            opts.scrollDuration);
        } else {
            var pos = Math.min(wrap.scrollLeft(), _220(_266));
            wrap.animate({
                scrollLeft: pos
            },
            opts.scrollDuration);
        }
        _239(_266);
        _268.push(_267);
        opts.onSelect.call(_266, _267);
    };
    function _25d(_26d, _26e) {
        return _25e(_26d, _26e) != null;
    };
    $.fn.tabs = function(_26f, _270) {
        if (typeof _26f == "string") {
            return $.fn.tabs.methods[_26f](this, _270);
        }
        _26f = _26f || {};
        return this.each(function() {
            var _271 = $.data(this, "tabs");
            var opts;
            if (_271) {
                opts = $.extend(_271.options, _26f);
                _271.options = opts;
            } else {
                $.data(this, "tabs", {
                    options: $.extend({},
                    $.fn.tabs.defaults, $.fn.tabs.parseOptions(this), _26f),
                    tabs: _23f(this),
                    selectHis: []
                });
            }
            _22e(this);
            _243(this);
            _232(this);
            var _272 = this;
            setTimeout(function() {
                _264(_272);
            },
            0);
        });
    };
    $.fn.tabs.methods = {
        options: function(jq) {
            return $.data(jq[0], "tabs").options;
        },
        tabs: function(jq) {
            return $.data(jq[0], "tabs").tabs;
        },
        resize: function(jq) {
            return jq.each(function() {
                _232(this);
                _239(this);
            });
        },
        add: function(jq, _273) {
            return jq.each(function() {
                _251(this, _273);
            });
        },
        close: function(jq, _274) {
            return jq.each(function() {
                _249(this, _274);
            });
        },
        getTab: function(jq, _275) {
            return _25e(jq[0], _275);
        },
        getSelected: function(jq) {
            return _23b(jq[0]);
        },
        select: function(jq, _276) {
            return jq.each(function() {
                _254(this, _276);
            });
        },
        exists: function(jq, _277) {
            return _25d(jq[0], _277);
        },
        update: function(jq, _278) {
            return jq.each(function() {
                _255(this, _278);
            });
        }
    };
    $.fn.tabs.parseOptions = function(_279) {
        var t = $(_279);
        return {
            width: (parseInt(_279.style.width) || undefined),
            height: (parseInt(_279.style.height) || undefined),
            fit: (t.attr("fit") ? t.attr("fit") == "true": undefined),
            border: (t.attr("border") ? t.attr("border") == "true": undefined),
            plain: (t.attr("plain") ? t.attr("plain") == "true": undefined)
        };
    };
    $.fn.tabs.defaults = {
        width: "auto",
        height: "auto",
        plain: false,
        fit: false,
        border: true,
        tools: null,
        scrollIncrement: 100,
        scrollDuration: 400,
        onLoad: function(_27a) {},
        onSelect: function(_27b) {},
        onBeforeClose: function(_27c) {},
        onClose: function(_27d) {},
        onAdd: function(_27e) {},
        onUpdate: function(_27f) {},
        onContextMenu: function(e, _280) {}
    };
})(jQuery); (function($) {
    var _281 = false;
    function _282(_283) {
        var opts = $.data(_283, "layout").options;
        var _284 = $.data(_283, "layout").panels;
        var cc = $(_283);
        if (opts.fit == true) {
            var p = cc.parent();
            cc.width(p.width()).height(p.height());
        }
        var cpos = {
            top: 0,
            left: 0,
            width: cc.width(),
            height: cc.height()
        };
        function _285(pp) {
            if (pp.length == 0) {
                return;
            }
            pp.panel("resize", {
                width: cc.width(),
                height: pp.panel("options").height,
                left: 0,
                top: 0
            });
            cpos.top += pp.panel("options").height;/*top header height*/
            cpos.height -= pp.panel("options").height;
        };
        if (_289(_284.expandNorth)) {
            _285(_284.expandNorth);
        } else {
            _285(_284.north);
        }
        function _286(pp) {
            if (pp.length == 0) {
                return;
            }
            pp.panel("resize", {
                width: cc.width(),
                height: pp.panel("options").height,
                left: 0,
                top: cc.height() - pp.panel("options").height
            });
            cpos.height -= pp.panel("options").height;
        };
        if (_289(_284.expandSouth)) {
            _286(_284.expandSouth);
        } else {
            _286(_284.south);
        }
        function _287(pp) {
            if (pp.length == 0) {
                return;
            }
            pp.panel("resize", {
                width: pp.panel("options").width,
                height: cpos.height,
                left: cc.width() - pp.panel("options").width,
                top: cpos.top
            });
            cpos.width -= pp.panel("options").width;
        };
        if (_289(_284.expandEast)) {
            _287(_284.expandEast);
        } else {
            _287(_284.east);
        }
        function _288(pp) {
            if (pp.length == 0) {
                return;
            }
            pp.panel("resize", {
                width: pp.panel("options").width,
                height: cpos.height,
                left: 0,
                top: cpos.top
            });
            cpos.left += pp.panel("options").width;
            cpos.width -= pp.panel("options").width;
        };
        if (_289(_284.expandWest)) {
            _288(_284.expandWest);
        } else {
            _288(_284.west);
        }
        _284.center.panel("resize", cpos);
    };
    function init(_28a) {
        var cc = $(_28a);
        if (cc[0].tagName == "BODY") {
            $("html").css({
                height: "100%",
                overflow: "hidden"
            });
            $("body").css({
                height: "100%",
                overflow: "hidden",
                border: "none"
            });
        }
        cc.addClass("layout");
        cc.css({
            margin: 0,
            padding: 0
        });
        function _28b(dir) {
            var pp = $(">div[region=" + dir + "]", _28a).addClass("layout-body");
            var _28c = null;
            if (dir == "north") {
                _28c = "layout-button-up";
            } else {
                if (dir == "south") {
                    _28c = "layout-button-down";
                } else {
                    if (dir == "east") {
                        _28c = "layout-button-right";
                    } else {
                        if (dir == "west") {
                            _28c = "layout-button-left";
                        }
                    }
                }
            }
            var cls = "layout-panel";
            pp.panel({
                cls: cls,
                doSize: false,
                border: (pp.attr("border") == "false" ? false: true),
                width: (pp.length ? parseInt(pp[0].style.width) || pp.outerWidth() : "auto"),
                height: (pp.length ? parseInt(pp[0].style.height) || pp.outerHeight() : "auto"),
                tools: [{
                    iconCls: _28c,
                    handler: function() {
                        _295(_28a, dir);
                    }
                }]
            });
            if (pp.attr("split") == "true") {
                var _28d = pp.panel("panel");
                var _28e = "";
                if (dir == "north") {
                    _28e = "s";
                }
                if (dir == "south") {
                    _28e = "n";
                }
                if (dir == "east") {
                    _28e = "w";
                }
                if (dir == "west") {
                    _28e = "e";
                }
                _28d.resizable({
                    handles: _28e,
                    onStartResize: function(e) {
                        _281 = true;
                        var top = 0,
                        left = 0,
                        _290 = 0,
                        _291 = 0;
                        var pos = {
                            display: "block"
                        };
                        //_28f.css(pos);
                        $("<div class=\"layout-mask\"></div>").css({
                            left: 0,
                            top: 0,
                            width: cc.width(),
                            height: cc.height()
                        }).appendTo(cc);
                    },
                    onResize: function(e) {
                        return false;
                    },
                    onStopResize: function() {
                        var opts = pp.panel("options");
                        opts.width = _28d.outerWidth();
                        opts.height = _28d.outerHeight();
                        opts.left = _28d.css("left");
                        opts.top = _28d.css("top");
                        pp.panel("resize");
                        _282(_28a);
                        _281 = false;
                        cc.find(">div.layout-mask").remove();
                    }
                });
            }
            return pp;
        };
        var _293 = {
            center: _28b("center")
        };
        _293.north = _28b("north");
        _293.south = _28b("south");
        _293.east = _28b("east");
        _293.west = _28b("west");
        $(_28a).bind("_resize",
        function(e, _294) {
            var opts = $.data(_28a, "layout").options;
            if (opts.fit == true || _294) {
                _282(_28a);
            }
            return false;
        });
        return _293;
    };
    function _295(_296, _297) {
        var _298 = $.data(_296, "layout").panels;
        var cc = $(_296);
        function _299(dir) {
            var icon;
            if (dir == "east") {
                icon = "layout-button-left";
            } else {
                if (dir == "west") {
                    icon = "layout-button-right";
                } else {
                    if (dir == "north") {
                        icon = "layout-button-down";
                    } else {
                        if (dir == "south") {
                            icon = "layout-button-up";
                        }
                    }
                }
            }
			var expand="layout-expand layout-"+dir;
            var p = $("<div></div>").appendTo(cc).panel({
                cls: expand,
                title: "&nbsp;",
                closed: true,
                doSize: false,
                tools: [{
                    iconCls: icon,
                    handler: function() {
                        _29a(_296, _297);
                    }
                }]
            });
            p.panel("panel").hover(function() {
                $(this).addClass("layout-expand-over");
            },
            function() {
                $(this).removeClass("layout-expand-over");
            });
            return p;
        };
        if (_297 == "east") {
            if (_298.east.panel("options").onBeforeCollapse.call(_298.east) == false) {
                return;
            }
            _298.center.panel("resize", {
                width: _298.center.panel("options").width + _298.east.panel("options").width - 28
				
            });
            _298.east.panel("panel").animate({
                left: cc.width()
            },0.01,
            function() {
                _298.east.panel("close");
                _298.expandEast.panel("open").panel("resize", {
                    top: _298.east.panel("options").top,
                    left: cc.width() - 28,
                    width: 28,
                    height: _298.east.panel("options").height
                });
                _298.east.panel("options").onCollapse.call(_298.east);
            });
            if (!_298.expandEast) {
                _298.expandEast = _299("east");
                _298.expandEast.panel("panel").click(function() {
                    _298.east.panel("open").panel("resize", {
                        left: cc.width()
                    });
                    _298.east.panel("panel").animate({
                        left: cc.width() - _298.east.panel("options").width
                    },0.01);
                    return false;
                });
            }
        } else {
            if (_297 == "west") {
                if (_298.west.panel("options").onBeforeCollapse.call(_298.west) == false) {
                    return;
                }
                _298.center.panel("resize", {
                    width: _298.center.panel("options").width + _298.west.panel("options").width -7 ,
                    left: 7
                });
                _298.west.panel("panel").animate({
                    left: -_298.west.panel("options").width
                },0.001,
                function() {
                    _298.west.panel("close");
                    _298.expandWest.panel("open").panel("resize", {
                        top: _298.west.panel("options").top,
                        left: 0,
                        width: 7,
                        height: _298.west.panel("options").height
                    },1);
                    _298.west.panel("options").onCollapse.call(_298.west);
                });
                if (!_298.expandWest) {
                    _298.expandWest = _299("west");
                    _298.expandWest.panel("panel").click(function() {    
                        return false;
                    });
                }
            } else {
                if (_297 == "north") {
                    if (_298.north.panel("options").onBeforeCollapse.call(_298.north) == false) {
                        return;
                    }
                    var hh = cc.height() - 7;
                    if (_289(_298.expandSouth)) {
                        hh -= _298.expandSouth.panel("options").height;
                    } else {
                        if (_289(_298.south)) {
                            hh -= _298.south.panel("options").height;
                        }
                    }
                    _298.center.panel("resize", {
                        top: 7,
                        height: hh
                    });
                    _298.east.panel("resize", {
                        top: 7,
                        height: hh
                    });
                    _298.west.panel("resize", {
                        top: 7,
                        height: hh
                    });
                    if (_289(_298.expandEast)) {
                        _298.expandEast.panel("resize", {
                            top: 7,
                            height: hh
                        });
                    }
                    if (_289(_298.expandWest)) {
                        _298.expandWest.panel("resize", {
                            top: 7,
                            height: hh
                        });
                    }
                    _298.north.panel("panel").animate({
                        top: -_298.north.panel("options").height
                    },0.01,
                    function() {
                        _298.north.panel("close");
                        _298.expandNorth.panel("open").panel("resize", {
                            top: 0,
                            left: 0,
                            width: cc.width(),
                            height: 7
                        });
                        _298.north.panel("options").onCollapse.call(_298.north);
                    });
                    if (!_298.expandNorth) {
                        _298.expandNorth = _299("north");
                        _298.expandNorth.panel("panel").click(function() {
                            _298.north.panel("open").panel("resize", {
                                top: -_298.north.panel("options").height
                            });
                            _298.north.panel("panel").animate({
                                top: 0
                            },0.01);
                            return false;
                        });
                    }
                } else {
                    if (_297 == "south") {
                        if (_298.south.panel("options").onBeforeCollapse.call(_298.south) == false) {
                            return;
                        }
                        var hh = cc.height() - 28;
                        if (_289(_298.expandNorth)) {
                            hh -= _298.expandNorth.panel("options").height;
                        } else {
                            if (_289(_298.north)) {
                                hh -= _298.north.panel("options").height;
                            }
                        }
                        _298.center.panel("resize", {
                            height: hh
                        });
                        _298.east.panel("resize", {
                            height: hh
                        });
                        _298.west.panel("resize", {
                            height: hh
                        });
                        if (_289(_298.expandEast)) {
                            _298.expandEast.panel("resize", {
                                height: hh
                            });
                        }
                        if (_289(_298.expandWest)) {
                            _298.expandWest.panel("resize", {
                                height: hh
                            });
                        }
                        _298.south.panel("panel").animate({
                            top: cc.height()
                        },0.01,
                        function() {
                            _298.south.panel("close");
                            _298.expandSouth.panel("open").panel("resize", {
                                top: cc.height() - 28,
                                left: 0,
                                width: cc.width(),
                                height: 28
                            });
                            _298.south.panel("options").onCollapse.call(_298.south);
                        });
                        if (!_298.expandSouth) {
                            _298.expandSouth = _299("south");
                            _298.expandSouth.panel("panel").click(function() {
                                _298.south.panel("open").panel("resize", {
                                    top: cc.height()
                                });
                                _298.south.panel("panel").animate({
                                    top: cc.height() - _298.south.panel("options").height
                                },0.01);
                                return false;
                            });
                        }
                    }
                }
            }
        }
    };
    function _29a(_29b, _29c) {
        var _29d = $.data(_29b, "layout").panels;
        var cc = $(_29b);
        if (_29c == "east" && _29d.expandEast) {
            if (_29d.east.panel("options").onBeforeExpand.call(_29d.east) == false) {
                return;
            }
            _29d.expandEast.panel("close");
            _29d.east.panel("panel").stop(true, true);
            _29d.east.panel("open").panel("resize", {
                left: cc.width()
            });
            _29d.east.panel("panel").animate({
                left: cc.width() - _29d.east.panel("options").width
            },0.01,
            function() {
                _282(_29b);
                _29d.east.panel("options").onExpand.call(_29d.east);
            });
        } else {
            if (_29c == "west" && _29d.expandWest) {
                if (_29d.west.panel("options").onBeforeExpand.call(_29d.west) == false) {
                    return;
                }
                _29d.expandWest.panel("close");
                _29d.west.panel("panel").stop(true, true);
                _29d.west.panel("open").panel("resize", {
                    left: -_29d.west.panel("options").width
                });
                _29d.west.panel("panel").animate({
                    left: 0
                },0.01,
                function() {
                    _282(_29b);
                    _29d.west.panel("options").onExpand.call(_29d.west);
                });
            } else {
                if (_29c == "north" && _29d.expandNorth) {
                    if (_29d.north.panel("options").onBeforeExpand.call(_29d.north) == false) {
                        return;
                    }
                    _29d.expandNorth.panel("close");
                    _29d.north.panel("panel").stop(true, true);
                    _29d.north.panel("open").panel("resize", {
                        top: -_29d.north.panel("options").height
                    });
                    _29d.north.panel("panel").animate({
                        top: 0
                    },0.01,
                    function() {
                        _282(_29b);
                        _29d.north.panel("options").onExpand.call(_29d.north);
                    });
                } else {
                    if (_29c == "south" && _29d.expandSouth) {
                        if (_29d.south.panel("options").onBeforeExpand.call(_29d.south) == false) {
                            return;
                        }
                        _29d.expandSouth.panel("close");
                        _29d.south.panel("panel").stop(true, true);
                        _29d.south.panel("open").panel("resize", {
                            top: cc.height()
                        });
                        _29d.south.panel("panel").animate({
                            top: cc.height() - _29d.south.panel("options").height
                        },0.01,
                        function() {
                            _282(_29b);
                            _29d.south.panel("options").onExpand.call(_29d.south);
                        });
                    }
                }
            }
        }
    };
    function _29e(_29f) {
        var _2a0 = $.data(_29f, "layout").panels;
        var cc = $(_29f);
        if (_2a0.east.length) {
            _2a0.east.panel("panel").bind("mouseover", "east", _295);
        }
        if (_2a0.west.length) {
            _2a0.west.panel("panel").bind("mouseover", "west", _295);
        }
        if (_2a0.north.length) {
            _2a0.north.panel("panel").bind("mouseover", "north", _295);
        }
        if (_2a0.south.length) {
            _2a0.south.panel("panel").bind("mouseover", "south", _295);
        }
        _2a0.center.panel("panel").bind("mouseover", "center", _295);
        function _295(e) {
            if (_281 == true) {
                return;
            }
            if (e.data != "east" && _289(_2a0.east) && _289(_2a0.expandEast)) {
                _2a0.east.panel("panel").animate({
                    left: cc.width()
                },0.01,
                function() {
                    _2a0.east.panel("close");
                });
            }
            if (e.data != "west" && _289(_2a0.west) && _289(_2a0.expandWest)) {
                _2a0.west.panel("panel").animate({
                    left: -_2a0.west.panel("options").width
                },0.01,
                function() {
                    _2a0.west.panel("close");
                });
            }
            if (e.data != "north" && _289(_2a0.north) && _289(_2a0.expandNorth)) {
                _2a0.north.panel("panel").animate({
                    top: -_2a0.north.panel("options").height
                },0.01,
                function() {
                    _2a0.north.panel("close");
                });
            }
            if (e.data != "south" && _289(_2a0.south) && _289(_2a0.expandSouth)) {
                _2a0.south.panel("panel").animate({
                    top: cc.height()
                },0.01,
                function() {
                    _2a0.south.panel("close");
                });
            }
            return false;
        };
    };
    function _289(pp) {
        if (!pp) {
            return false;
        }
        if (pp.length) {
            return pp.panel("panel").is(":visible");
        } else {
            return false;
        }
    };
    $.fn.layout = function(_2a1, _2a2) {
        if (typeof _2a1 == "string") {
            return $.fn.layout.methods[_2a1](this, _2a2);
        }
        return this.each(function() {
            var _2a3 = $.data(this, "layout");
            if (!_2a3) {
                var opts = $.extend({},
                {
                    fit: $(this).attr("fit") == "true"
                });
                $.data(this, "layout", {
                    options: opts,
                    panels: init(this)
                });
                _29e(this);
            }
            _282(this);
        });
    };
    $.fn.layout.methods = {
        resize: function(jq) {
            return jq.each(function() {
                _282(this);
            });
        },
        panel: function(jq, _2a4) {
            return $.data(jq[0], "layout").panels[_2a4];
        },
        collapse: function(jq, _2a5) {
            return jq.each(function() {
                _295(this, _2a5);
            });
        },
        expand: function(jq, _2a6) {
            return jq.each(function() {
                _29a(this, _2a6);
            });
        }
    };
})(jQuery); 