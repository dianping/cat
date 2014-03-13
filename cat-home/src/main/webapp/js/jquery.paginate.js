(function($) {
	$.fn.paginate = function(options) {
		var opts = $.extend({}, $.fn.paginate.defaults, options);
		return this.each(function() {
			$this = $(this);
			var o = $.meta ? $.extend({}, opts, $this.data()) : opts;
			var selectedpage = o.start;
			$.fn.draw(o,$this,selectedpage);	
		});
	};
	var outsidewidth_tmp = 0;
	var insidewidth 	 = 0;
	var bName = navigator.appName;
	var bVer = navigator.appVersion;
	if(bVer.indexOf('MSIE 7.0') > 0)
		var ver = "ie7";
	$.fn.paginate.defaults = {
		count 		: 5,
		start 		: 12,
		display  	: 5,
		border					: true,
		border_color			: '#fff',
		text_color  			: '#8cc59d',
		background_color    	: 'black',	
		border_hover_color		: '#fff',
		text_hover_color  		: '#fff',
		background_hover_color	: '#fff', 
		rotate      			: true,
		images					: true,
		mouse					: 'slide',
		onChange				: function(){return false;}
	};
	$.fn.draw = function(o,obj,selectedpage){
		if(o.display > o.count)
			o.display = o.count;
		$this.empty();
		if(o.images){
			var spreviousclass 	= 'jPag-sprevious-img';
			var previousclass 	= 'jPag-previous-img';
			var snextclass 		= 'jPag-snext-img';
			var nextclass 		= 'jPag-next-img';
		}
		else{
			var spreviousclass 	= 'jPag-sprevious';
			var previousclass 	= 'jPag-previous';
			var snextclass 		= 'jPag-snext';
			var nextclass 		= 'jPag-next';
		}
		var _first		= $(document.createElement('a')).addClass('jPag-first').html('First');
		
		if(o.rotate){
			if(o.images) var _rotleft	= $(document.createElement('span')).addClass(spreviousclass);
			else var _rotleft	= $(document.createElement('span')).addClass(spreviousclass).html('&laquo;');		
		}
		
		var _divwrapleft	= $(document.createElement('div')).addClass('jPag-control-back');
		_divwrapleft.append(_first).append(_rotleft);
		
		var _ulwrapdiv	= $(document.createElement('div')).css('overflow','hidden');
		var _ul			= $(document.createElement('ul')).addClass('jPag-pages')
		var c = (o.display - 1) / 2;
		var first = selectedpage - c;
		var selobj;
		for(var i = 0; i < o.count; i++){
			var val = i+1;
			if(val == selectedpage){
				var _obj = $(document.createElement('li')).html('<span class="jPag-current">'+val+'</span>');
				selobj = _obj;
				_ul.append(_obj);
			}	
			else{
				var _obj = $(document.createElement('li')).html('<a>'+ val +'</a>');
				_ul.append(_obj);
				}				
		}		
		_ulwrapdiv.append(_ul);
		
		if(o.rotate){
			if(o.images) var _rotright	= $(document.createElement('span')).addClass(snextclass);
			else var _rotright	= $(document.createElement('span')).addClass(snextclass).html('&raquo;');
		}
		
		var _last		= $(document.createElement('a')).addClass('jPag-last').html('Last');
		var _divwrapright	= $(document.createElement('div')).addClass('jPag-control-front');
		_divwrapright.append(_rotright).append(_last);
		
		//append all:
		$this.addClass('jPaginate').append(_divwrapleft).append(_ulwrapdiv).append(_divwrapright);
			
		if(!o.border){
			if(o.background_color == 'none') var a_css 				= {'color':o.text_color};
			else var a_css 											= {'color':o.text_color,'background-color':o.background_color};
			if(o.background_hover_color == 'none')	var hover_css 	= {'color':o.text_hover_color};
			else var hover_css 										= {'color':o.text_hover_color,'background-color':o.background_hover_color};	
		}	
		else{
			if(o.background_color == 'none') var a_css 				= {'color':o.text_color,'border':'1px solid '+o.border_color};
			else var a_css 											= {'color':o.text_color,'background-color':o.background_color,'border':'1px solid '+o.border_color};
			if(o.background_hover_color == 'none')	var hover_css 	= {'color':o.text_hover_color,'border':'1px solid '+o.border_hover_color};
			else var hover_css 										= {'color':o.text_hover_color,'background-color':o.background_hover_color,'border':'1px solid '+o.border_hover_color};
		}
		
		$.fn.applystyle(o,$this,a_css,hover_css,_first,_ul,_ulwrapdiv,_divwrapright);
		//calculate width of the ones displayed:
		var outsidewidth = outsidewidth_tmp - _first.parent().width() -3;
		if(ver == 'ie7'){
			_ulwrapdiv.css('width',outsidewidth+72+'px');
			_divwrapright.css('left',outsidewidth_tmp+6+72+'px');
		}
		else{
			_ulwrapdiv.css('width',outsidewidth+'px');
			_divwrapright.css('left',outsidewidth_tmp+6+'px');
		}
		
		if(o.rotate){
			_rotright.hover(
				function() {
				  thumbs_scroll_interval = setInterval(
					function() {
					  var left = _ulwrapdiv.scrollLeft() + 1;
					  _ulwrapdiv.scrollLeft(left);
					},
					20
				  );
				},
				function() {
				  clearInterval(thumbs_scroll_interval);
				}
			);
			_rotleft.hover(
				function() {
				  thumbs_scroll_interval = setInterval(
					function() {
					  var left = _ulwrapdiv.scrollLeft() - 1;
					  _ulwrapdiv.scrollLeft(left);
					},
					20
				  );
				},
				function() {
				  clearInterval(thumbs_scroll_interval);
				}
			);
			if(o.mouse == 'press'){
				_rotright.mousedown(
					function() {
					  thumbs_mouse_interval = setInterval(
						function() {
						  var left = _ulwrapdiv.scrollLeft() + 5;
						  _ulwrapdiv.scrollLeft(left);
						},
						20
					  );
					}
				).mouseup(
					function() {
					  clearInterval(thumbs_mouse_interval);
					}
				);
				_rotleft.mousedown(
					function() {
					  thumbs_mouse_interval = setInterval(
						function() {
						  var left = _ulwrapdiv.scrollLeft() - 5;
						  _ulwrapdiv.scrollLeft(left);
						},
						20
					  );
					}
				).mouseup(
					function() {
					  clearInterval(thumbs_mouse_interval);
					}
				);
			}
			else{
				_rotleft.click(function(e){
					var width = outsidewidth - 10;
					var left = _ulwrapdiv.scrollLeft() - width;
					_ulwrapdiv.animate({scrollLeft: left +'px'});
				});	
				
				_rotright.click(function(e){
					var width = outsidewidth - 10;
					var left = _ulwrapdiv.scrollLeft() + width;
					_ulwrapdiv.animate({scrollLeft: left +'px'});
				});
			}
		}
		
		//first and last:
		_first.click(function(e){
				_ulwrapdiv.animate({scrollLeft: '0px'});
				_ulwrapdiv.find('li').eq(0).click();
		});
		_last.click(function(e){
				_ulwrapdiv.animate({scrollLeft: insidewidth +'px'});
				_ulwrapdiv.find('li').eq(o.count - 1).click();
		});
		
		//click a page
		_ulwrapdiv.find('li').click(function(e){
			selobj.html('<a>'+selobj.find('.jPag-current').html()+'</a>'); 
			var currval = $(this).find('a').html();
			$(this).html('<span class="jPag-current">'+currval+'</span>');
			selobj = $(this);
			$.fn.applystyle(o,$(this).parent().parent().parent(),a_css,hover_css,_first,_ul,_ulwrapdiv,_divwrapright);	
			var left = (this.offsetLeft) / 2;
			var left2 = _ulwrapdiv.scrollLeft() + left;
			var tmp = left - (outsidewidth / 2);
			if(ver == 'ie7')
				_ulwrapdiv.animate({scrollLeft: left + tmp - _first.parent().width() + 52 + 'px'});	
			else
				_ulwrapdiv.animate({scrollLeft: left + tmp - _first.parent().width() + 'px'});	
			o.onChange(currval);	
		});
		
		var last = _ulwrapdiv.find('li').eq(o.start-1);
		last.attr('id','tmp');
		var left = document.getElementById('tmp').offsetLeft / 2;
		last.removeAttr('id');
		var tmp = left - (outsidewidth / 2);
		if(ver == 'ie7') _ulwrapdiv.animate({scrollLeft: left + tmp - _first.parent().width() + 52 + 'px'});	
		else _ulwrapdiv.animate({scrollLeft: left + tmp - _first.parent().width() + 'px'});	
	}
	
	$.fn.applystyle = function(o,obj,a_css,hover_css,_first,_ul,_ulwrapdiv,_divwrapright){
					obj.find('a').css(a_css);
					obj.find('span.jPag-current').css(hover_css);
					obj.find('a').hover(
					function(){
						$(this).css(hover_css);
					},
					function(){
						$(this).css(a_css);
					}
					);
					obj.css('padding-left',_first.parent().width() + 5 +'px');
					insidewidth = 0;
					
					obj.find('li').each(function(i,n){
						if(i == (o.display-1)){
							outsidewidth_tmp = this.offsetLeft + this.offsetWidth ;
						}
						insidewidth += this.offsetWidth;
					})
					_ul.css('width',insidewidth+'px');
	}
})(jQuery);