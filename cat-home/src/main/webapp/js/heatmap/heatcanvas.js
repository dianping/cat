/**
 * Copyright 2010-2011 Sun Ning <classicning@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Heatmap api based on canvas
 *
 */

var HeatCanvas = function(canvas,overlay){
	if (typeof(canvas) == "string") {
		this.canvas = document.getElementById(canvas);
	} else {
		this.canvas = canvas;
	}
	if(this.canvas == null){
		return null;
	}
	//this.bgcolor="rgba(255,255,255,0)";
	this.worker = new Worker(HeatCanvas.getPath()+'heatcanvas-worker.js');

	this.width = this.canvas.width;
	this.height = this.canvas.height;

	this.onRenderingStart = null;
	this.onRenderingEnd = function(){
	       $('#wait_logo').hide();
	};
	
	this.overlay=overlay;

	this.data = {};
	this.value={};
	this.valueOrigin=undefined;
	this.maxvalue=0;
	this.interval={
		x: 0,		
		y: 0
	};  
	
	this.rangemin=overlay.rangemin;
	this.rangemax=overlay.rangemax;
	
	this.canvasImg = new Image();
	this.canvasImg.zoomlevel=12;
	this.originZoomLevel=undefined;
};


HeatCanvas.prototype.push = function(x, y, data){
    // ignore all data out of extent
    intx=Math.ceil(this.interval.x);
    inty=Math.ceil(this.interval.y);
    if (x < -this.interval.x || x > this.width+this.interval.x) {//
        return ;
		}
    if (y < -this.interval.y || y > this.height+this.interval.y) {//
        return;
    }
    var id = (x+intx)+(y+inty)*(this.width+intx*2);//
    if(this.data[id]){
        this.data[id] = this.data[id] + data;           
    } else {
        this.data[id] = data;
    }
};

HeatCanvas.prototype.render = function(){
	var self = this;
	if(typeof(self.valueOrigin) !== 'undefined'){
		self._render();
	}
		
	this.worker.onmessage = function(e){
		self.value = e.data.value;
		if(typeof(self.valueOrigin) === 'undefined'){
			self.valueOrigin =e.data.value;
			self.canvasImg.zoomlevel=self.overlay.zoom;
			self.originZoomLevel=self.overlay.zoom;
			self._adjust(self.rangemin,self.rangemax);
		}
		self.data = {};
		if (self.onRenderingEnd){
			self.onRenderingEnd();
		}
	}
	var msg = {
		'data': self.data,
		'width': self.width,
		'height': self.height,
		'value': self.value,
		'interval': self.interval		//#add interval
	};

	if(typeof(self.valueOrigin) === 'undefined'){
		this.worker.postMessage(msg);
	}
	if (this.onRenderingStart){
		this.onRenderingStart();
	}
};


HeatCanvas.prototype._render = function(){
    var ctx = this.canvas.getContext("2d");
    ctx.clearRect(0, 0, this.width, this.height);

	var w=this.canvasImg.width*Math.pow(2,this.overlay.zoom-this.canvasImg.zoomlevel);
	var h=this.canvasImg.height*Math.pow(2,this.overlay.zoom-this.canvasImg.zoomlevel);
	ctx.drawImage(this.canvasImg, this.overlay.pCenterOrigin.x-w/2, this.overlay.pCenterOrigin.y-h/2, w, h);
};

HeatCanvas.prototype.wait = function(){
	document.body.style.cursor='wait';
	HeatOverlay.map.setOptions({ draggableCursor: 'wait' });
	return true;
};

HeatCanvas.prototype.endWait = function(){
	document.body.style.cursor='wait';
	HeatOverlay.map.setOptions({ draggableCursor: 'auto' });
};

HeatCanvas.prototype._adjust = function(rangemin,rangemax){
	this.rangemin= rangemin ;
 	this.rangemax= rangemax ;

	//var canv=this.canvas;			
	var canv = document.createElement('canvas');
	canv.width=this.width;
	canv.height=this.height;
	var ctx = canv.getContext("2d");
	ctx.clearRect(0, 0, this.width, this.height);
    
	for(var pos in this.valueOrigin){
		var x = Math.floor(pos%this.width);
		var y = Math.floor(pos/this.width);
		var color = this.defaultValue2Color(this.valueOrigin[pos]);
		ctx.fillStyle = color;
		ctx.fillRect(x, y, 1, 1);
	} 

	var img = new Image();
	var s= canv.toDataURL("image/png");
		img.onload=function(){
	}
	img.src = s;// src must be after onload in firefox
	img.zoomlevel=this.originZoomLevel;
	this.canvasImg=img;
		
	var self=this;
		  
	setTimeout(function(){
		var ctx = self.canvas.getContext("2d");
		ctx.clearRect(0, 0, self.width, self.height);
		var w=img.width*Math.pow(2,self.overlay.zoom-img.zoomlevel);
		var h=img.height*Math.pow(2,self.overlay.zoom-img.zoomlevel);
		ctx.drawImage(img, self.overlay.pCenterOrigin.x-w/2, self.overlay.pCenterOrigin.y-h/2, w, h);	//callback
		slider.enable();
	},500);
};

HeatCanvas.prototype.clear = function(){
	this.data = {};
	this.value = {};
	this.canvas.getContext("2d").clearRect(0, 0, this.width, this.height);
};

HeatCanvas.prototype.defaultValue2Color = function(){
	var value2Color= {};
	return function(value){
		var v;
		if(value<this.rangemin){
			v=0;
		}else if(value>this.rangemax){
			v=1;
		}else{
			v=(value-this.rangemin)/(this.rangemax-this.rangemin);
		}
		v=Math.floor(v*400)/400;
	
		if(value2Color.hasOwnProperty(v)){
			return value2Color[v];
		}else{
			var t=Math.sqrt(v);
			var hue = (1 - t) * 340;
			var saturation=t*100;
			var light = 60*(1-t)+40;
	
			var s="hsla("+hue+", "+saturation+"%, "+light+"%,"+Math.sqrt(t)*0.8+")";
			value2Color[v]=s;
			return s;
		}
	}
}();

HeatCanvas.LINEAR = 1;
HeatCanvas.QUAD = 2;
HeatCanvas.CUBIC = 3;

HeatCanvas.getPath = function() {
    var scriptTags = document.getElementsByTagName("script");
    for (var i=0; i<scriptTags.length; i++) {
        var src = scriptTags[i].src;
        var pos = src.indexOf("heatcanvas.js");
        if (pos > 0) {
            return src.substring(0, pos);
        }
    }
    return "";
}
