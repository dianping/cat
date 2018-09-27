
var $_netgraph = { 

	NS: 'http://www.w3.org/2000/svg',

    setting: {
        'sw_width':120,
        'sw_height':40,
        'sw_bgcolor':{
            'normal':'rgb(0,255,0)',
            'warnning':'rgb(255,255,0)',
            'serious':'rgb(255,0,0)',
        },
        'sw_border_color':'rgb(200,200,200)',
        'sw_border_width':1,
        'sw_corner':2,
        'sw_text_size':16,
        'sw_text_color':'rgb(100,100,100)',
        'sw_text_width':100,

        'conn_width':10,
        'conn_color':{
            'normal':'rgb(0,255,0)',
            'warnning':'rgb(255,255,0)',
            'serious':'rgb(255,0,0)',
        },
        'conn_tooltip_offset':20,

        'anchor_text_size':16,
        'anchor_text_color':'rgb(100,100,100)',

        'focus_scale':2,

        'tooltip_width':275,
        'tooltip_td_height': 20,
        'tooltip_td_width': 80,
    },

    topos: {
        "example": {
            data: null,
            content: null,
            svg: null,
            tooltip: null,
        },
    },

	build: function(wrapper, data) {
        var wrapperEle = document.getElementById(wrapper);
        wrapperEle.innerHTML = '';

        this.topos[wrapper] = {};
        var topo = this.topos[wrapper];

        topo.data = data;
        this.builddata(wrapper);

        topo.svg = document.createElementNS(this.NS, 'svg');
        this.setAttrValues(topo.svg, {'width':500,'height':800});
        wrapperEle.appendChild(topo.svg);

        topo.content = document.createElementNS(this.NS, 'g');
        topo.content.setAttribute('id', 'svg-content');
        topo.svg.appendChild(topo.content);

        topo.tooltip = document.createElementNS(this.NS, 'g');
        topo.tooltip.setAttribute('id', wrapper+'-svg-tooltip');
        topo.svg.appendChild(topo.tooltip);

        this.render(wrapper);
	},

    builddata: function(wrapper) {
        var topo = this.topos[wrapper];
        var conn = {};
        var id;
        var x1, y1, x2, y2;
        var f_anchor, t_anchor;
        var switchs = topo.data['switchs'];
        var anchors = topo.data['anchors'];

        sw = {};
        for(var i in switchs) {
            if (switchs[i].name != undefined) {
                sw[switchs[i].name] = switchs[i];
            }
        }
        topo.data['switchs'] = sw;

        an = {};
        for(var i in anchors) {
            if (anchors[i].name != undefined) {
                an[anchors[i].name] = anchors[i];
            }
        }
        topo.data['anchors'] = an;

        for(var k in sw) {
            sw[k]['conn'] = [];
            if (sw[k]['state'] == 3) {
                sw[k]['fill'] = this.setting.sw_bgcolor.serious;
            }
            else if (sw[k]['state'] == 2) {
                sw[k]['fill'] = this.setting.sw_bgcolor.warnning;
            }
            else {
                sw[k]['fill'] = this.setting.sw_bgcolor.normal;
            }
        }

        var conns = topo.data.connections;
        for(var i in conns) {
            var cn = conns[i];

            var from = cn['from'];
            var to = cn['to'];
            if (!sw[from] || (!sw[to] && !an[to])) {
                continue;
            }

            var id = from + '=>' + to;
            if(id in conn) {
                continue;
            }

            if(sw[from]['group'] == undefined && cn['interfaces'][0] != undefined) {
                sw[from]['group'] = cn['interfaces'][0]['group'];
                sw[from]['domain'] = cn['interfaces'][0]['domain'];
            }

            var inData, outData, instate, outstate, infill, outfill, indiscards, outdiscards, inerrors, outerrors;
            inData = cn['insum'];
            outData = cn['outsum'];
            instate = cn['instate'];
            outstate = cn['outstate'];
            indiscards = cn['inDiscards'];
            outdiscards = cn['outDiscards'];
            indiscardsstate = cn['inDiscardsState'];
            outdiscardsstate = cn['outDiscardsState'];
            inerrors = cn['inErrors'];
            outerrors = cn['outErrors'];
            inerrorsstate = cn['inErrorsState'];
            outerrorsstate = cn['outErrorsState'];
            if (inData == undefined) {
                inData = 0;
            }
            if (outData == undefined) {
                outData = 0;
            }
            if (instate == 3 || indiscardsstate == 3 || inerrorsstate == 3) {
                infill = this.setting.conn_color.serious;
            }
            else if (instate == 2 || indiscardsstate == 2 || inerrorsstate == 2) {
                infill = this.setting.conn_color.warnning;
            }
            else {
                infill = this.setting.conn_color.normal;
            }
            if (outstate == 3 || outdiscardsstate == 3 || outerrorsstate == 3) {
                outfill = this.setting.conn_color.serious;
            }
            else if (outstate == 2 || outdiscardsstate == 2 || outerrorsstate == 2) {
                outfill = this.setting.conn_color.warnning;
            }
            else {
                outfill = this.setting.conn_color.normal;
            }
            if (indiscards == undefined) {
                indiscards = 0;
            }
            if (outdiscards == undefined) {
                outdiscards = 0;
            }
            if (inerrors == undefined) {
                inerrors = 0;
            }
            if (outerrors == undefined) {
                outerrors = 0;
            }

            var f_anchor = 0;
            var t_anchor = 0;
            var x1, y1, x2, y2;
            if(from in sw) {
                x1 = sw[from].x;
                y1 = sw[from].y;
            }
            else {
                x1 = an[from].x;
                y1 = an[from].y;
                f_anchor = 1;
            }
            if(to in sw) {
                x2 = sw[to].x;
                y2 = sw[to].y;
            }
            else {
                x2 = an[to].x;
                y2 = an[to].y;
                t_anchor = 1;
            }

            if(!f_anchor && !t_anchor) {
                if (y1 < y2)
                {
                    conn[id+'-in'] = {'type':'in','x1':x1-20,'y1':y1,'x2':(x1+x2-40)/2,'y2':(y1+y2)/2,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1+20,'y1':y1,'x2':(x1+x2+40)/2,'y2':(y1+y2)/2,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
                else if (y1 > y2)
                {
                    conn[id+'-in'] = {'type':'in','x1':x1+20,'y1':y1,'x2':(x1+x2+40)/2,'y2':(y1+y2)/2,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1-20,'y1':y1,'x2':(x1+x2-40)/2,'y2':(y1+y2)/2,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
                else if (x1 < x2)
                {
                    conn[id+'-in'] = {'type':'in','x1':x1,'y1':y1-10,'x2':(x1+x2)/2,'y2':(y1+y2-20)/2,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1,'y1':y1+10,'x2':(x1+x2)/2,'y2':(y1+y2+20)/2,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
                else
                {
                    conn[id+'-in'] = {'type':'in','x1':x1,'y1':y1+10,'x2':(x1+x2)/2,'y2':(y1+y2+20)/2,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1,'y1':y1-10,'x2':(x1+x2)/2,'y2':(y1+y2-20)/2,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
            }
            else if(!f_anchor) {
                if (y1 != y2)
                {
                    conn[id+'-in'] = {'type':'in','x1':x1-20,'y1':y1,'x2':(x1+x2*2-60)/3,'y2':(y1+y2*2)/3,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1+20,'y1':y1,'x2':(x1+x2*2+60)/3,'y2':(y1+y2*2)/3,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
                else if (x1 != x2)
                {
                    conn[id+'-in'] = {'type':'in','x1':x1,'y1':y1-10,'x2':(x1+x2*2)/3,'y2':(y1+y2*2-30)/3,'data':[inData,indiscards,inerrors],'name':to,'fill':infill,'state':instate,'discardsstate':indiscardsstate,'errorsstate':inerrorsstate};
                    sw[from]['conn'].push(id+'-in');
                    conn[id+'-out'] = {'type':'out','x1':x1,'y1':y1+10,'x2':(x1+x2*2)/3,'y2':(y1+y2*2+30)/3,'data':[outData,outdiscards,outerrors],'name':to,'fill':outfill,'state':outstate,'discardsstate':outdiscardsstate,'errorsstate':outerrorsstate};
                    sw[from]['conn'].push(id+'-out');
                }
            }
        }

        topo.data.conn = conn;
    },

	conn_mousein: function(wrapper, id) {
        var topo = this.topos[wrapper];
		var d = topo.data['conn'][id];
        var mx = d.x1;
        var my = d.y1;
        var x, y;
        if (mx <= this.setting.tooltip_width)
            x = mx + this.setting.conn_tooltip_offset;
        else
            x = mx - this.setting.tooltip_width - this.setting.conn_tooltip_offset;
        y = my - this.setting.conn_tooltip_offset;
        var data = [d['name'], d['type'], this.decorateNumber(d['data'][0]), this.decorateNumber(d['data'][1]), this.decorateNumber(d['data'][2]), d['state'], d['discardsstate'], d['errorsstate']];
		this.show_tooltip_table_conn(wrapper,x,y,data);

        var conn = document.getElementById(wrapper+'-'+id);
        var w = Math.round(conn.getAttribute('stroke-width'))+this.setting.focus_scale;
        this.setAttrValues(conn, {'stroke-width':w});
	},

	sw_mousein: function(wrapper, id) {
        var topo = this.topos[wrapper];
		var x = topo.data['switchs'][id].x;
		var y = topo.data['switchs'][id].y;
        if (x <= 300)
            x = x - this.setting.sw_width/2 - 2;
        else
            x = x + this.setting.sw_width/2 - this.setting.tooltip_width - 2;
        y = y + this.setting.sw_height/2;
        var conn;
        var data = {};
        for (var i in topo.data['switchs'][id].conn) {
            conn = topo.data['conn'][topo.data['switchs'][id].conn[i]];
            var to = topo.data['switchs'][id].conn[i].split('=>')[1].split('-'+conn['type'])[0];
            if (data[to] == undefined)
                data[to] = {};
            data[to][conn['type']] = [this.decorateNumber(conn['data'][0]), this.decorateNumber(conn['data'][1]), this.decorateNumber(conn['data'][2]), conn['state'], conn['discardsstate'], conn['errorsstate']];
        }
        this.show_tooltip_table(wrapper, x, y, data);

        var sw = document.getElementById(wrapper+'-'+id).firstChild;
        var w = Math.round(sw.getAttribute('stroke-width'))+this.setting.focus_scale;
        this.setAttrValues(sw, {'stroke-width':w});
	},

    conn_mouseout: function(wrapper,id) {
        this.tooltip_hide(wrapper,id);

        var conn = document.getElementById(wrapper+'-'+id);
        var w = Math.round(conn.getAttribute('stroke-width'))-this.setting.focus_scale;
        this.setAttrValues(conn, {'stroke-width':w});
    },

    sw_mouseout: function(wrapper,id) {
        this.tooltip_hide(wrapper,id);

        var sw = document.getElementById(wrapper+'-'+id).firstChild;
        var w = Math.round(sw.getAttribute('stroke-width'))-this.setting.focus_scale;
        this.setAttrValues(sw, {'stroke-width':w});
    },

	tooltip_hide: function(wrapper,id) {
		var g = document.getElementById(wrapper+'-svg-tooltip');
        this.setAttrValues(g, {'visibility':'hidden','opacity':0,'data-cur':''});
	},

    show_tooltip_table_conn: function(wrapper, x, y, data) {
		var g = document.getElementById(wrapper+"-svg-tooltip");
		var childs = g.childNodes;
		for(var i = childs.length-1; i >= 0; i--) {
			g.removeChild(childs[i]);
		}

        var height = this.setting.tooltip_td_height * 2 + 20;
		var rect1 = document.createElementNS(this.NS, "rect");
		var rect2 = document.createElementNS(this.NS, "rect");
		var rect3 = document.createElementNS(this.NS, "rect");
		var rect4 = document.createElementNS(this.NS, "rect");
		this.setAttrValues([rect1,rect2,rect3,rect4], {
					'rx':3,
					'ry':3,
					'fill':'none',
					'x':0.5,
					'y':0.5,
					'width':this.setting.tooltip_width,
					'height':height,
					'fill-opacity':0.85,
					'isShadow':'true',
					'stroke':'black',
					'stroke-width':1,
					'transform':'translate(1, 1)'
				});
		this.setAttrValues(rect1, {'stroke-opacity':0.05,'stroke-width':5});
		this.setAttrValues(rect2, {'stroke-opacity':0.1,'stroke-width':2});
		this.setAttrValues(rect3, {'stroke-opacity':0.15});
		this.setAttrValues(rect4, {'stroke':'#2f7ed8','fill':'rgb(255,255,255)'});
		g.appendChild(rect1);
		g.appendChild(rect2);
		g.appendChild(rect3);
		g.appendChild(rect4);


        var ty = 0;
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 120,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'flow');
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 160,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'd');
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 200,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'e');

        ty += 30;
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10,
                  ty + this.setting.tooltip_td_height,
                  this.setting.tooltip_td_width,
                  this.setting.tooltip_td_height, data[0]);

        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 60,
                       ty + this.setting.tooltip_td_height,
                       60, this.setting.tooltip_td_height, data[1]);

        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 120,
                       ty + this.setting.tooltip_td_height,
                       40, this.setting.tooltip_td_height, data[2], data[5]);
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 160,
                       ty + this.setting.tooltip_td_height,
                       40, this.setting.tooltip_td_height, data[3], data[6]);
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 200,
                       ty + this.setting.tooltip_td_height,
                       40, this.setting.tooltip_td_height, data[4], data[7]);

        if (y > 400) {
            y -= this.setting.sw_height + height + 4;
        }

		this.setAttrValues(g, {'visibility':'visible','transform':'translate('+x+','+y+')','opacity':1});
    },

    show_tooltip_table: function(wrapper, x, y, data) {
		var g = document.getElementById(wrapper+"-svg-tooltip");
		var childs = g.childNodes;
		for(var i = childs.length-1; i >= 0; i--) {
			g.removeChild(childs[i]);
		}

        var length = 0;
        for (var to in data) {
            length++;
        }
        var height = length * (this.setting.tooltip_td_height * 2 + 10) + 30;
        if (height < 30) {
            height = 30;
        }
		var rect1 = document.createElementNS(this.NS, "rect");
		var rect2 = document.createElementNS(this.NS, "rect");
		var rect3 = document.createElementNS(this.NS, "rect");
		var rect4 = document.createElementNS(this.NS, "rect");
		this.setAttrValues([rect1,rect2,rect3,rect4], {
					'rx':3,
					'ry':3,
					'fill':'none',
					'x':0.5,
					'y':0.5,
					'width':this.setting.tooltip_width,
					'height':height,
					'fill-opacity':0.85,
					'isShadow':'true',
					'stroke':'black',
					'stroke-width':1,
					'transform':'translate(1, 1)'
				});
		this.setAttrValues(rect1, {'stroke-opacity':0.05,'stroke-width':5});
		this.setAttrValues(rect2, {'stroke-opacity':0.1,'stroke-width':2});
		this.setAttrValues(rect3, {'stroke-opacity':0.15});
		this.setAttrValues(rect4, {'stroke':'#2f7ed8','fill':'rgb(255,255,255)'});
		g.appendChild(rect1);
		g.appendChild(rect2);
		g.appendChild(rect3);
		g.appendChild(rect4);


        var ty = 0;
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 120,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'flow');
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 160,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'd');
        this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 200,
                       ty + 24,
                       60, this.setting.tooltip_td_height, 'e');
        ty += 20;
        for (var to in data) {
            this.show_text(g, this.setting.tooltip_td_width / 2 + 10,
                      ty + this.setting.tooltip_td_height * 2,
                      this.setting.tooltip_td_width,
                      this.setting.tooltip_td_height, to);

            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 60,
                           ty + this.setting.tooltip_td_height + 10,
                           60, this.setting.tooltip_td_height, 'in');

            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 60,
                           ty + this.setting.tooltip_td_height * 2 + 10,
                           60, this.setting.tooltip_td_height, 'out');

            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 120,
                           ty + this.setting.tooltip_td_height + 10,
                           40, this.setting.tooltip_td_height, data[to]['in'][0], data[to]['in'][3]);
            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 160,
                           ty + this.setting.tooltip_td_height + 10,
                           40, this.setting.tooltip_td_height, data[to]['in'][1], data[to]['in'][4]);
            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 200,
                           ty + this.setting.tooltip_td_height + 10,
                           40, this.setting.tooltip_td_height, data[to]['in'][2], data[to]['in'][5]);

            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 120,
                           ty + this.setting.tooltip_td_height * 2 + 10,
                           40, this.setting.tooltip_td_height, data[to]['out'][0], data[to]['out'][3]);
            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 160,
                           ty + this.setting.tooltip_td_height * 2 + 10,
                           40, this.setting.tooltip_td_height, data[to]['out'][1], data[to]['out'][4]);
            this.show_text(g, this.setting.tooltip_td_width / 2 + 10 + 200,
                           ty + this.setting.tooltip_td_height * 2 + 10,
                           40, this.setting.tooltip_td_height, data[to]['out'][2], data[to]['out'][5]);

            ty += this.setting.tooltip_td_height * 2 + 10;
        }

        if (y > 400) {
            y -= this.setting.sw_height + height + 4;
        }

		this.setAttrValues(g, {'visibility':'visible','transform':'translate('+x+','+y+')','opacity':1});
    },

    show_text: function(g, x, y, w, h, text, state) {
        var color = 'black';
        if (state == '3')
            color = 'red';
        var textNode = document.createElementNS(this.NS, "text");
        this.setAttrValues(textNode, {
            'x': x,
            'y': y,
            'rx': 3,
            'ry': 3,
            'width': w,
            'height': h,
            'text-anchor':'middle',
            'fill': color,
            'opacity': '1',
        });
        textString = document.createTextNode(text);
        textNode.appendChild(textString);
        g.appendChild(textNode);
    },

	show_tooltip: function(wrapper,x,y,text) {
		var g = document.getElementById(wrapper+"-svg-tooltip");
		var childs = g.childNodes;
		for(var i = childs.length-1; i >= 0; i--) {
			g.removeChild(childs[i]);
		}

        var height = text.length * 20 + 10;
        if (height < 30) {
            height = 30;
        }
		var rect1 = document.createElementNS(this.NS, "rect");
		var rect2 = document.createElementNS(this.NS, "rect");
		var rect3 = document.createElementNS(this.NS, "rect");
		var rect4 = document.createElementNS(this.NS, "rect");
		this.setAttrValues([rect1,rect2,rect3,rect4], {
					'rx':3,
					'ry':3,
					'fill':'none',
					'x':0.5,
					'y':0.5,
					'width':this.setting.tooltip_width,
					'height':height,
					'fill-opacity':0.85,
					'isShadow':'true',
					'stroke':'black',
					'stroke-width':1,
					'transform':'translate(1, 1)'
				});
		this.setAttrValues(rect1, {'stroke-opacity':0.05,'stroke-width':5});
		this.setAttrValues(rect2, {'stroke-opacity':0.1,'stroke-width':2});
		this.setAttrValues(rect3, {'stroke-opacity':0.15});
		this.setAttrValues(rect4, {'stroke':'#2f7ed8','fill':'rgb(255,255,255)'});

		g.appendChild(rect1);
		g.appendChild(rect2);
		g.appendChild(rect3);
		g.appendChild(rect4);

        var t, textString;
        var tx = 8;
        var ty = 20;
        for(var i in text) {
            t = document.createElementNS(this.NS, "text");
            this.setAttrValues(t, {
                        'x':tx,
                        'y':ty,
                        'style':'font-family:Lucida Grande;font-size:12px;color:#333333;fill:#333333;font-weight:bold;'
                    });
            textString = document.createTextNode(text[i]);
            t.appendChild(textString);
            g.appendChild(t);
            ty += 20;
        }

        if (y > height) {
            y -= height;
        }

		this.setAttrValues(g, {'visibility':'visible','transform':'translate('+x+','+y+')','opacity':1});
	},

    sw_detail: function(topo, id) {
        var sw = topo.data['switchs'][id];
        var group = sw['group'];
        var domain = sw['domain'];
        window.open('/cat/r/server?op=view&category=network&group=traffic&endPoint='+group, '_blank');
    },

	render: function(wrapper) {
        var topo = this.topos[wrapper];
		for (var id in topo.data['conn']) {
			var d = topo.data['conn'][id];
            var flow = d.data[0];
            if (flow < d.data[1]) {
                flow = d.data[1];
            }
			this.draw_conn(wrapper, d.x1, d.y1, d.x2, d.y2, id, d.fill, flow);
		}

		for (var id in topo.data['switchs']) {
			this.draw_sw(wrapper, topo.data['switchs'][id].x, topo.data['switchs'][id].y, id, topo.data['switchs'][id].fill);
		}

		for (var id in topo.data['anchors']) {
			this.draw_anchor(wrapper, topo.data['anchors'][id].x, topo.data['anchors'][id].y, id);
		}
	},

	draw_anchor: function(wrapper, x,y,id) {
        var topo = this.topos[wrapper];
		var text = document.createElementNS(this.NS,"text");
        this.setAttrValues(text, {
            'id':wrapper+'-'+id,
            'x':x,
            'y':y+this.setting.anchor_text_size/2,
            'font-size':this.setting.anchor_text_size,
            'fill':this.setting.anchor_text_color,
            'text-anchor':'middle',
        });
		var textString = document.createTextNode(id);
		text.appendChild(textString);
		topo.content.appendChild(text);
	},

	draw_sw: function(wrapper, x,y,id,fill) {
        var topo = this.topos[wrapper];
		var g = document.createElementNS(this.NS, "g");
		topo.content.appendChild(g);

		var sw = document.createElementNS(this.NS,"rect");
        this.setAttrValues(sw, {
            'x':x-this.setting.sw_width/2-this.setting.sw_border_width,
            'y':y-this.setting.sw_height/2-this.setting.sw_border_width,
            'rx':this.setting.sw_corner,
            'ry':this.setting.sw_corner,
            'width':this.setting.sw_width,
            'height':this.setting.sw_height,
            'fill':fill,
            'stroke':this.setting.sw_border_color,
            'stroke-width':this.setting.sw_border_width,
        });
        var sw_detail_ = this.sw_detail;
        sw.addEventListener("click", function(){sw_detail_(topo, id)}, false);
		g.appendChild(sw);

		var text = document.createElementNS(this.NS,"text");
        this.setAttrValues(text, {
            'x':x,
            'y':y+this.setting.sw_text_size/2,
            'textLength':this.setting.sw_text_width,
            'font-size':this.setting.sw_text_size,
            'fill':this.setting.sw_text_color,
            'text-anchor':'middle',
        });
        text.addEventListener("click", function(){sw_detail_(topo, id)}, false);
		var textString = document.createTextNode(id);
		text.appendChild(textString);
		g.appendChild(text);
		
        this.setAttrValues(g, {
            'id':wrapper+'-'+id,
            'onmouseover':"$_netgraph.sw_mousein('"+wrapper+"','"+id+"');",
            'onmouseout':"$_netgraph.sw_mouseout('"+wrapper+"','"+id+"');",
        });
	},

	draw_conn: function(wrapper, x1, y1, x2, y2, id, fill, flow) {
        var topo = this.topos[wrapper];

        var width = flow / 1000000000 * 8;
        if (width < 2 && width > 0) {
            width = 3;
        } 
        else if (width <= 0) {
            width = 1;
        }
        else if (width > 20) {
            width = 20;
        }

		var line = document.createElementNS(this.NS, "line");
		this.setAttrValues(line, {
                    'id':wrapper+'-'+id,
					'x1':x1,
					'y1':y1,
					'x2':x2,
					'y2':y2,
					'stroke':fill,
					'stroke-width':width,
                    'onmouseover':'$_netgraph.conn_mousein("'+wrapper+'","'+id+'");',
                    'onmouseout':'$_netgraph.conn_mouseout("'+wrapper+'","'+id+'");',
                });
		topo.content.appendChild(line);
	},

    setAttrValues: function(ele, map) {
		if(!(ele instanceof Array)) {
			ele = [ele];
		}

		for(i in ele) {
			for(k in map) {
				ele[i].setAttribute(k, map[k]);
			}
		}
	},

    decorateNumber: function(num) {
        if (num < 1024) {
            num = Math.round(num * 100) / 100;
        }
        else if (num < 1024 * 1024) {
            num = Math.round(num / 1024 * 100) / 100 + 'K';
        }
        else if (num < 1024 * 1024 * 1024) {
            num = Math.round(num / 1024 / 1024 * 100) / 100 + 'M';
        }
        else if (num < 1024 * 1024 * 1024 * 1024) {
            num = Math.round(num / 1024 / 1024 / 1024 * 100) / 100 + 'G';
        }
        else {
            num = (num / 1024 / 1024 / 1024 / 1024).toFixed(3) + 'T';
        }
        return num;
    }
};
