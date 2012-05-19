/**
 * Copyright 2010-2011 Sun Ning <classicning@gmail.com>
 * Copyright 2011 Ni Huajie <lbt05@gmail.com>
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

function HeatCanvasOverlayView(map, options){
	options = options || {};
	this.setMap(map);
	this.map=map;
	this.heatmap = null;
	this._div=undefined;
	/*
	this.step = options.step || 1;
	this.degree = options.degree || HeatCanvas.LINEAR;
	this.colorscheme = options.colorscheme || null;
	*/
	this.opacity = options.opacity || 0.6;     //0.6
	this.rangemin= options.min;
	this.rangemax= options.max;

	this.data = [];
	this.dataOrdered={};

	this.interval={
		lat:0.0025,
		lng:0.0025
	};//add default

	this.zoom=undefined;
	this.pCenter={};

	this.zoomOrigin=undefined;
	this.pCenterOrigin={};

	this.centerOrigin=undefined;
	
	this.marker;
	
	this.testchars;
	
	var self=this;
	
	this.pDragStart;
	this.dist;
	this.timeDragStart;
	this.pMouseMove;
	this.mouseMoveLatLng;
	

	//google.maps.event.addListener(self.map,'dragend',function(){self.draw();});
	//google.maps.event.addListener(self.map,'dblclick',function(){self.draw();});
	//google.maps.event.addListener(self.map,'zoom_changed',function(){self.draw();});
	google.maps.event.addListener(self.map,'mousemove',function(e){
		var proj = self.getProjection();
		if(!e.LatLng) return;
		self.pMouseMove=proj.fromLatLngToContainerPixel(e.latLng);
		self.mouseMoveLatLng=e.latLng;
		if(typeof self.pDragStart !== 'undefined'){
			var d=Math.sqrt(Math.pow(self.pMouseMove.x-self.pDragStart.x,2)+Math.pow(self.pMouseMove.y-self.pDragStart.y,2));
			if(d>self.dist){self.dist=d;}
		}
		
		/*
		var p=self.pMouseMove;
		var element = document.getElementById("info2");
		element.textContent="lat and lng: "+e.latLng.toUrlValue()+" x:"+p.x+" y:"+p.y;
		*/
		
	});
	google.maps.event.addListener(self.map,'dragstart',function(){
		self.timeDragStart=+new Date();
		//var locElement = document.getElementById("loc");
		self.pDragStart=self.pMouseMove;
		self.dist=0;
	});
	google.maps.event.addListener(self.map,'dragend',function(e){
		var tInterval=+new Date()-self.timeDragStart;
		var p=self.pMouseMove;
		var distance=Math.sqrt(Math.pow(p.x-self.pDragStart.x,2)+Math.pow(p.y-self.pDragStart.y,2));
		if(distance>10 
			|| self.dist>15
			//|| tInterval>500
			){
			return;
		}
		self.pDragStart= undefined;
		
		var proj = self.getProjection();
		var latLng=self.mouseMoveLatLng;
		//loc
		function getPrice(){
			var lat=latLng.lat();
			var lon=latLng.lng();
			var y=lat/self.interval.lat;
			var x=lon/self.interval.lng;
	
			//bottom-left 00  ;  top-right 11;
			var test00=(Math.floor(y)+','+Math.floor(x)).toString();
			var v00=self.dataOrdered[(Math.floor(x)+','+Math.floor(y)).toString()] || 0;
			var v01=self.dataOrdered[(Math.floor(x)+','+Math.ceil(y)).toString()] || 0;
			var v10=self.dataOrdered[(Math.ceil(x)+','+Math.floor(y)).toString()] || 0;
			var v11=self.dataOrdered[(Math.ceil(x)+','+Math.ceil(y)).toString()] || 0;
	
	
			var lat0=Math.floor(y)*self.interval.lat;
			var lat1=Math.ceil(y)*self.interval.lat;
			var lon0=Math.floor(x)*self.interval.lng;
			var lon1=Math.ceil(x)*self.interval.lng;
	
			var P00=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat0, lon0));
			var P01=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat1, lon0));
			var P10=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat0, lon1));
			var P11=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat1, lon1));
	
			var pxleftRatio=(p.x-(P00.x+P01.x)/2)/((P10.x+P11.x)/2-(P00.x+P01.x)/2);
			var pybottomRatio=(p.y-(P00.y+P10.y)/2)/((P01.y+P11.y)/2-(P00.y+P10.y)/2);
			var value=(1-pxleftRatio)*(1-pybottomRatio)*v00 +pxleftRatio*(1-pybottomRatio)*v10 +(1-pxleftRatio)*pybottomRatio*v01 +pxleftRatio*pybottomRatio*v11;
			
			return Math.floor(value/10)*10;
		}
		slider.indicator(getPrice());

		self.marker.visible=true;
		self.marker.setPosition(latLng);
		
		

	});
		
	google.maps.event.addListener(self.map,'bounds_changed',function(){
		//self.testchars+='+';
			self.draw();
	});
	
	google.maps.event.addListener(self.map,'click',function(e){
		var proj = self.getProjection();
		var p=proj.fromLatLngToContainerPixel(e.latLng);
		

		//loc
		function getPrice(){
			var lat=e.latLng.lat();
			var lon=e.latLng.lng();
			var y=lat/self.interval.lat;
			var x=lon/self.interval.lng;
	
			//bottom-left 00  ;  top-right 11;
			var test00=(Math.floor(y)+','+Math.floor(x)).toString();
			var v00=self.dataOrdered[(Math.floor(x)+','+Math.floor(y)).toString()] || 0;
			var v01=self.dataOrdered[(Math.floor(x)+','+Math.ceil(y)).toString()] || 0;
			var v10=self.dataOrdered[(Math.ceil(x)+','+Math.floor(y)).toString()] || 0;
			var v11=self.dataOrdered[(Math.ceil(x)+','+Math.ceil(y)).toString()] || 0;
	
	
			var lat0=Math.floor(y)*self.interval.lat;
			var lat1=Math.ceil(y)*self.interval.lat;
			var lon0=Math.floor(x)*self.interval.lng;
			var lon1=Math.ceil(x)*self.interval.lng;
	
			var P00=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat0, lon0));
			var P01=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat1, lon0));
			var P10=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat0, lon1));
			var P11=proj.fromLatLngToContainerPixel(new google.maps.LatLng(lat1, lon1));
	
			var pxleftRatio=(p.x-(P00.x+P01.x)/2)/((P10.x+P11.x)/2-(P00.x+P01.x)/2);
			var pybottomRatio=(p.y-(P00.y+P10.y)/2)/((P01.y+P11.y)/2-(P00.y+P10.y)/2);
			var value=(1-pxleftRatio)*(1-pybottomRatio)*v00 +pxleftRatio*(1-pybottomRatio)*v10 +(1-pxleftRatio)*pybottomRatio*v01 +pxleftRatio*pybottomRatio*v11;
			
						return Math.floor(value/10)*10;
		}
		slider.indicator(getPrice());

		self.marker.visible=true;
		self.marker.setPosition(e.latLng);

	});
	
}

HeatCanvasOverlayView.prototype = new google.maps.OverlayView();


HeatCanvasOverlayView.prototype.pushData = function(lat, lon, value) {
    this.data.push({"lon":lon, "lat":lat, "v":value});
    var loc=Math.round(lon/this.interval.lng)+','+Math.round(lat/this.interval.lat);
    this.dataOrdered[loc]=value;
}

HeatCanvasOverlayView.prototype.onAdd = function(){
    var proj = this.getProjection();
    var sw = proj.fromLatLngToDivPixel(this.getMap().getBounds().getSouthWest());
    var ne = proj.fromLatLngToDivPixel(this.getMap().getBounds().getNorthEast());

    var container = document.createElement("div");
    container.style.cssText = "position:absolute;top:0;left:0;border:0";
    container.style.width = "100%";
    container.style.height = "100%";
    var canvas = document.createElement("canvas");

    canvas.style.width = ne.x-sw.x+"px";
    canvas.style.height = sw.y-ne.y+"px";
    canvas.width = parseInt(canvas.style.width);
    canvas.height = parseInt(canvas.style.height);
    canvas.style.opacity = this.opacity;
    container.appendChild(canvas);

    this.heatmap = new HeatCanvas(canvas,this);
    
    var panes = this.getPanes();
    panes.overlayLayer.appendChild(container);
    this._div = container;
    
    initCanvas2Image(); //a function in googlemap.html
    
    //init params
    this.zoom=this.zoomOrigin;
    this.centerOrigin=this.getMap().getCenter();
    this.pCenterOrigin=proj.fromLatLngToContainerPixel(this.centerOrigin);
      
}

HeatCanvasOverlayView.prototype.onRemove = function() {
	//google.maps.event.clearInstanceListeners(this.map);
	this.heatmap.clear();
	this.data = {};
	$(this._div).remove();
	if(this.callback){
		this.callback.call(this);
	}
	console.log('clear');
	
}

HeatCanvasOverlayView.prototype.draw = function() {

	if(!this.getMap() || !this.getMap().getBounds()) return;
	
	  var proj = this.getProjection();
	  // fit current viewport
	  var sw = proj.fromLatLngToDivPixel(this.getMap().getBounds().getSouthWest());
    var ne = proj.fromLatLngToDivPixel(this.getMap().getBounds().getNorthEast());
		// Resize the image's DIV to fit the indicated dimensions.
	if(typeof(this._div) !== 'undefined'){
		var div = this._div;
		div.style.left = sw.x + 'px';
		div.style.top = ne.y + 'px';
		div.style.width = (ne.x - sw.x) + 'px';
		div.style.height = (sw.y - ne.y) + 'px';	  
	}
	  	  

			
	  
		if (this.data.length > 0) {//this.data.length will be set as 0 later , exec only once;

	
			
			//calculate interval  add
			var llc=this.getMap().getCenter();
			var llc2=new google.maps.LatLng(llc.lat()+this.interval.lat,llc.lng()+this.interval.lng);
			var p00=proj.fromLatLngToContainerPixel(llc) ;
			var p11=proj.fromLatLngToContainerPixel(llc2) ;
			this.heatmap.interval.x=Math.abs(p11.x-p00.x);
			this.heatmap.interval.y=Math.abs(p11.y-p00.y);
	
			/*
			var info2Element = document.getElementById("info2");
			info2Element.textContent="info2: "+Math.abs(p11.x-p00.x)+" "+Math.abs(p11.y-p00.y);
			var info3Element = document.getElementById("info3");
			info3Element.textContent="info3: "+llc.lat()+" "+llc.lng();  
			*/
			
			this.heatmap.clear();

			for (var i=0, l=this.data.length; i<l; i++) {
				latlon = new google.maps.LatLng(this.data[i].lat, this.data[i].lon);
				localXY = proj.fromLatLngToContainerPixel(latlon);
				this.heatmap.push(
			    Math.floor(localXY.x), //	localXY.x,
			    Math.floor(localXY.y), //	localXY.y,
			    this.data[i].v);
			}
			this.data={}; //this.data.length==0
			

			//var count=0;
			//
			
			var self=this;
			//google.maps.event.addListener(self.map,'dragend',function(){self.draw();});
			//google.maps.event.addListener(self.map,'dblclick',function(){self.draw();});
			//google.maps.event.addListener(self.map,'zoom_changed',function(){self.draw();});
			//google.maps.event.trigger(self.map,'dblclick');
			//this.getMap().setZoom(10);	
			
					
			redraw();
			//these commands will be executed when redraw's worker is running or when redraw's setTimeout() ;

			setTimeout(function(){
				//self.getMap().setZoom(13);
				redraw();
				resetDiv();
			},2000);
			/*self.zoom=13;
			self.pCenterOrigin=proj.fromLatLngToContainerPixel(self.centerOrigin);
			*/

			return;
		}
		/*
		var info5Element = document.getElementById("info5");
		info5Element.textContent="heatmap.data.length: "+this.data.length; 
		*/
		redraw();	
		
		//var self=this;
		function redraw(){
			//position params to render image
			var self=window.HeatOverlay;
			var proj = self.getProjection();
		  // fit current viewport
		  var sw = proj.fromLatLngToDivPixel(self.getMap().getBounds().getSouthWest());
	    var ne = proj.fromLatLngToDivPixel(self.getMap().getBounds().getNorthEast());
			// Resize the image's DIV to fit the indicated dimensions.
			var div = self._div;
			div.style.left = sw.x + 'px';
			div.style.top = ne.y + 'px';
			div.style.width = (ne.x - sw.x) + 'px';
			div.style.height = (sw.y - ne.y) + 'px';	 		
	
			//self.testchars+=" |"+ne.y+"|";
			
			self.zoom=self.getMap().getZoom();
			self.pCenterOrigin=proj.fromLatLngToContainerPixel(self.centerOrigin);
			self.heatmap.render();
			//setTimeout(redraw, 300);
		}
		
		
		function resetDiv(){
			//count++;
			//position params to render image
			var self=window.HeatOverlay;
			if(!proj.getProjection) return;
			var proj = self.getProjection();
			if(!self.getMap() || !self.getMap().getBounds()) return;
		  // fit current viewport
		  var sw = proj.fromLatLngToDivPixel(self.getMap().getBounds().getSouthWest());
	    var ne = proj.fromLatLngToDivPixel(self.getMap().getBounds().getNorthEast());
			// Resize the image's DIV to fit the indicated dimensions.
			var div = self._div;
			var x=div.style.borderLeft;
			div.style.left = sw.x +0.12344555666677777+'px'; //0.12344 is needed to force browser redraw
			div.style.top = ne.y + 0.123123123412414+ 'px'; //0.12344 is needed to force browser redraw
			div.style.width = (ne.x - sw.x) + 'px';
			div.style.height = (sw.y - ne.y) + 'px';
			div.style.marginLeft=-1+'px';
			div.style.marginLeft=0+'px';
			//setTimeout(resetDiv, 200); 	
		}
		

		
		/*
		this.zoom=this.getMap().getZoom();
		this.pCenterOrigin=proj.fromLatLngToContainerPixel(this.centerOrigin);
		this.heatmap.render();
		*/
}

