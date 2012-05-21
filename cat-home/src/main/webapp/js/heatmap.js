  var HeatOverlay;
  var slider, renderMap;
  var min,max;
  
  function init() {
	if (!document.createElement('canvas').getContext) {		
		$('#wait_logo').hide();
		document.getElementById("map").innerHTML="<p>Your browser does not support canvas. Please use <a id=\"a\" target=\"blank\" href=\"https://www.google.com/chrome\">Chrome</a>  <a id=\"b\" target=\"blank\" href=\"http://www.firefox.com\">Firefox</a>  <a id=\"c\" target=\"blank\" href=\"http://www.apple.com/safari/\">Safari</a>  <a id=\"d\" target=\"blank\" href=\"http://www.opera.com\">Opera</a> to view this site.</p>                        <p>你的浏览器不支持canvas. 请使用 <a id=\"a\" target=\"blank\" href=\"https://www.google.com/chrome\">Chrome</a>  <a id=\"b\" target=\"blank\" href=\"http://www.firefox.com\">Firefox</a>  <a id=\"c\" target=\"blank\" href=\"http://www.apple.com/safari/\">Safari</a>  <a id=\"d\" target=\"blank\" href=\"http://www.opera.com\">Opera</a>等浏览器访问这个站点。</p>";
		return;
	}
	
  	
    var latlng = new google.maps.LatLng(31.230393,121.473704);
    var myoptions = {
        zoom: 13,
        center: latlng,
        maxZoom: 18,
        minZoom: 4,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        streetViewControl: false,
       // navigationControlOptions: {style: google.maps.NavigationControlStyle.SMALL},
	    mapTypeControl: true,
	    mapTypeControlOptions: {
	        style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
	        position: google.maps.ControlPosition.LEFT_TOP
	    },
	    navigationControl: true,
	    navigationControlOptions: {
	        style: google.maps.NavigationControlStyle.ZOOM_PAN,
	        position: google.maps.ControlPosition.LEFT
	    },
	    scaleControl: true,
	    scaleControlOptions: {
	        position: google.maps.ControlPosition.LEFT_BOTTOM
	    } 
	};
    //var map = new google.maps.Map(document.getElementById("map"), myoptions);
    
    var ne, sw, width=$('#map').width(), height = $('#map').height();
   	var t, ti = 200;
   	var interval = {
			lat: 0.01,
			lng: 0.01
		};
   	var map = $('#map').gmap3({
    	action: 'init',
    	options: myoptions,
    	events:{
    		'bounds_changed':function(){	// 地图位置变化，获取数据并重新渲染热图
    			if(HeatOverlay){
    				HeatOverlay.setMap(null);
    				HeatOverlay = null;
    			}
    			$('#wait_logo').show();

    			var _t = +new Date;
    			if(t && (_t - t < ti)) return;
    			t = _t;
    			var bounds = map.getBounds(), ne = bounds.getNorthEast(),sw = bounds.getSouthWest(), data = {
    				lat2:ne.lat(),
    				lat1:sw.lat(),
    				lng2:ne.lng(),
    				lng1:sw.lng(),
    				width: width,
    				height: height,
    				cb: 'renderMap',
    				op:'jsonp',
    				unit:60
    			}, url = urlPrefix+"&"+decodeURIComponent($.param(data));
    			//url = "data_sh.js";
    			$.getScript(url);
    		},'zoom_changed': function(){
    			// 12,0.02,13 0.01, 14,0.005, 15 0.0025
    			var p = Math.pow(2, 13 - map.getZoom());
    			interval.lat = 0.01 * p;
    			interval.lng = 0.01 * p;
    		} 
    	}
    }).gmap3('get');		
    	
		// autocomplete
	$('#address').autocomplete({
		source: function () {
			$('#map').gmap3({
				action: 'getAddress',
				address: $(this).val(),
				callback: function (results) {
					if (!results) return;
					$('#address').autocomplete('display', results, false);
				}
			});
		},
		cb: {
			cast: function (item) {
				return item.formatted_address;
			},
			select: function (item) {
				map.setCenter(item.geometry.location);					
				//map.setZoom(13);
			}
		}
	});
	// 热门城市
	$('#hotcities a').click(function(e){
		e.preventDefault();
		var latlng = $(this).attr('data-latlng').split(',');
		map.setCenter(new google.maps.LatLng(latlng[0],latlng[1]));
	});

 
    renderMap = function(_data){
    	console.log(_data);
    	min = _data.min || 0;
    	max = _data.max || 10000;
    	var data = _data.data;
		if(data.length == 0) return;

    	var heatmap = new HeatCanvasOverlayView(map, {'opacity':1.0, 'min':min, 'max':max,});//'opacity':0.8
    	
   	 	HeatOverlay=heatmap;
      
		
		heatmap.interval=interval;
		        
		for(var i=0,l=data.length; i<l; i++) {
          heatmap.pushData(data[i][0], data[i][1], data[i][2]);
        }
        
        //slider
		setTimeout(function(){
					slider.setValues(min,max,min,max);


				},100);
		}     
        var mainCanvas=document.createElement("canvas");
				var rootElement=document.getElementById("map");
				//rootElement.appendChild(mainCanvas);
				slider=new HeatSlider(mainCanvas, rootElement);
				setTimeout(function(){
					slider.setValues(min,max,min,max);
				},800);
				slider.stop=function() { 
						slider.disable();
						//HeatOverlay.heatmap.wait();//
						setTimeout(function(){
							HeatOverlay.heatmap._adjust(slider.values[0],slider.values[1]);
							//HeatOverlay.heatmap.endWait();			
						},400);
				}
				slider.enable();
				map.controls[google.maps.ControlPosition.RIGHT].push(mainCanvas);

		}
      
      //debug div function
		function initCanvas2Image(){
      }//end of initCanvas2Image