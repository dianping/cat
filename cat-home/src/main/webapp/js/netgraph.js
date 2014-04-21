
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

        'tooltip_width':220,
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
        var verseid;
        var sw = topo.data['sw'];
        var anchor = topo.data['anchor'];
        var x1, y1, x2, y2;
        var f_anchor, t_anchor;

        for(var k in sw) {
            topo.data['sw'][k]['conn'] = [];
            if (topo.data['sw'][k]['state'] == 3) {
                topo.data['sw'][k]['fill'] = this.setting.sw_bgcolor.serious;
            }
            else if (topo.data['sw'][k]['state'] == 2) {
                topo.data['sw'][k]['fill'] = this.setting.sw_bgcolor.warnning;
            }
            else {
                topo.data['sw'][k]['fill'] = this.setting.sw_bgcolor.normal;
            }
        }

        for(var i in topo.data.conn) {
            var cn = topo.data.conn[i];

            var firstname = cn[0][0];
            var secondname = cn[1][0];
            if ((!sw[firstname] && !anchor[firstname]) || 
                    (!sw[secondname] && !anchor[secondname])) {
                continue;
            }

            var id = firstname + '=>' + secondname;
            var verseid = secondname + '=>' + firstname;
            if(id in conn) {
                continue;
            }

            var firstin, firstout, firststate, firstfill;
            firstin = cn[0][1];
            firstout = cn[0][2];
            firststate = cn[0][3];
            if (firstin == undefined) {
                firstin = 0;
            }
            if (firstout == undefined) {
                firstout = 0;
            }
            if (firststate == 3) {
                firstfill = this.setting.conn_color.serious;
            }
            else if (firststate == 2) {
                firstfill = this.setting.conn_color.warnning;
            }
            else {
                firstfill = this.setting.conn_color.normal;
            }

            var secondin, secondout, secondstate, secondfill;
            secondin = cn[1][1];
            secondout = cn[1][2];
            secondstate = cn[1][3];
            if (secondin == undefined) {
                secondin = 0;
            }
            if (secondout == undefined) {
                secondout = 0;
            }
            if (secondstate == 3) {
                secondfill = this.setting.conn_color.serious;
            }
            else if (secondstate == 2) {
                secondfill = this.setting.conn_color.warnning;
            }
            else {
                secondfill = this.setting.conn_color.normal;
            }

            var f_anchor = 0;
            var t_anchor = 0;
            var x1, y1, x2, y2;
            if(firstname in sw) {
                x1 = sw[firstname].x;
                y1 = sw[firstname].y;
            }
            else {
                x1 = anchor[firstname].x;
                y1 = anchor[firstname].y;
                f_anchor = 1;
            }
            if(secondname in sw) {
                x2 = sw[secondname].x;
                y2 = sw[secondname].y;
            }
            else {
                x2 = anchor[secondname].x;
                y2 = anchor[secondname].y;
                t_anchor = 1;
            }

            var name = id.split('=>')[1];
            var versename = verseid.split('=>')[1];
            if(!f_anchor && !t_anchor) {
                conn[id] = {'x1':x1,'y1':y1,'x2':(x1+x2)/2,'y2':(y1+y2)/2,'data':[firstin,firstout],'name':name,'fill':firstfill};
                conn[verseid] = {'x1':(x1+x2)/2,'y1':(y1+y2)/2,'x2':x2,'y2':y2,'data':[secondin,secondout],'name':versename,'fill':secondfill};
                topo.data.sw[firstname]['conn'].push(id);
                topo.data.sw[secondname]['conn'].push(verseid);
            }
            else if(!f_anchor) {
                conn[id] = {'x1':x1,'y1':y1,'x2':(x1+x2*2)/3,'y2':(y1+y2*2)/3,'data':[firstin,firstout],'name':name,'fill':firstfill};
                topo.data.sw[firstname]['conn'].push(id);
            }
            else if(!t_anchor) {
                conn[verseid] = {'x1':(x1*2+x2)/3,'y1':(y1*2+y2)/3,'x2':x2,'y2':y2,'data':[secondin,secondout],'name':versename,'fill':secondfill};
                topo.data.sw[secondname]['conn'].push(verseid);
            }
        }

        topo.data.conn = conn;
    },

	conn_mousein: function(wrapper, id) {
        var topo = this.topos[wrapper];
		var d = topo.data['conn'][id];
        var mx = (d.x1 + d.x2) / 2;
        var my = (d.y1 + d.y2) / 2;
        var x, y;
        if (mx <= 300)
            x = mx + this.setting.conn_tooltip_offset;
        else
            x = mx - this.setting.tooltip_width - this.setting.conn_tooltip_offset;
        y = my + this.setting.conn_tooltip_offset;
        var tip = [];
        tip.push('[from '+d['name'] + ']-[in]: ' + this.decorateNumber(d['data'][0]));
        tip.push('[to '+d['name'] + ']-[out]: ' + this.decorateNumber(d['data'][1]));
		this.show_tooltip(wrapper,x,y,tip);

        var conn = document.getElementById(wrapper+'-'+id);
        var w = Math.round(conn.getAttribute('stroke-width'))+this.setting.focus_scale;
        this.setAttrValues(conn, {'stroke-width':w});
	},

	sw_mousein: function(wrapper, id) {
        var topo = this.topos[wrapper];
		var x = topo.data['sw'][id].x;
		var y = topo.data['sw'][id].y;
        if (x <= 300)
            x = x + this.setting.sw_width/2 + this.setting.sw_border_width;
        else
            x = x - this.setting.sw_width/2 - this.setting.sw_border_width - this.setting.tooltip_width - 4;
        y = y - this.setting.sw_height/2-this.setting.sw_border_width;
        var conn;
        var tip = [];
        for(var i in topo.data['sw'][id].conn) {
            conn = topo.data['conn'][topo.data['sw'][id].conn[i]];
            tip.push('[from '+conn['name'] + ']-[in]: ' + this.decorateNumber(conn['data'][0]));
            tip.push('[to '+conn['name'] + ']-[out]: ' + this.decorateNumber(conn['data'][1]));
        }
		this.show_tooltip(wrapper,x,y,tip);

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

	show_tooltip: function(wrapper,x,y,text) {
		var g = document.getElementById(wrapper+"-svg-tooltip");
		var childs = g.childNodes;
		for(var i = childs.length-1; i >= 0; i--) {
			g.removeChild(childs[i]);
		}

        var height = text.length * 20 + 10;
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

		this.setAttrValues(g, {'visibility':'visible','transform':'translate('+x+','+y+')','opacity':1});
	},

	render: function(wrapper) {
        var topo = this.topos[wrapper];
		for (var id in topo.data['conn']) {
			var d = topo.data['conn'][id];
			this.draw_conn(wrapper, d.x1, d.y1, d.x2, d.y2, id, d.fill);
		}

		for (var id in topo.data['sw']) {
			this.draw_sw(wrapper, topo.data['sw'][id].x, topo.data['sw'][id].y, id, topo.data['sw'][id].fill);
		}

		for (var id in topo.data['anchor']) {
			this.draw_anchor(wrapper, topo.data['anchor'][id].x, topo.data['anchor'][id].y, id);
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
		var textString = document.createTextNode(id);
		text.appendChild(textString);
		g.appendChild(text);
		
        this.setAttrValues(g, {
            'id':wrapper+'-'+id,
            'onmouseover':"$_netgraph.sw_mousein('"+wrapper+"','"+id+"');",
            'onmouseout':"$_netgraph.sw_mouseout('"+wrapper+"','"+id+"');",
        });
	},

	draw_conn: function(wrapper, x1, y1, x2, y2, id, fill) {
        var topo = this.topos[wrapper];

		var line = document.createElementNS(this.NS, "line");
		this.setAttrValues(line, {
                    'id':wrapper+'-'+id,
					'x1':x1,
					'y1':y1,
					'x2':x2,
					'y2':y2,
					'stroke':fill,
					'stroke-width':this.setting.conn_width,
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
            num = Math.round(num);
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
