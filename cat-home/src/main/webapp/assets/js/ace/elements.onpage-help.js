window.Onpage_Help = function(options) {
	var $ = window.jQuery || null;
	if($ == null) return;

	options = options || {}
	var defaults = {
		include_all: true,
		icon_1: 'fa fa-question',
		icon_2: 'fa fa-lightbulb-o',
		base: '',
		code_highlight: (!!window.Rainbow ? 'rainbow' : (!!window.Prism ? 'prism' : null)),

		add_panels: true,
		panel_content_selector: '.info-section',
		panel_content_title: '.info-title'
	}
	this.settings = $.extend({}, defaults, options);
	

	var $base = this.settings['base'];
	var ie_fix = document.all && !window.atob;//ie9 and below need a little fix

	var section_start = {};
	var section_end = {};
	var section_rect = {};
	var section_count = 0;
	
	var created = false;
	var active = false;
	
	var self = this, _ = this;
	var ovfx = '';
	var help_container = null;
	
	var body_h, body_w;
	
	var captureFocus = function() {
		if(!help_container) return;
		var scroll = -1;
		//like bootstrap modal
		$(document)
		.off('focusin.ace.help') //remove any previously attached handler
		.on('focusin.ace.help', function (e) {
			if (!( help_container[0] == e.target || $.contains(help_container[0], e.target) )) {
			  help_container.focus();
			}

			if(e.target == document && scroll > -1) {
				//when window regains focus and container is focused, it scrolls to bottom
				//so we put it back to its place
				$('body,html').scrollTop(scroll);
				scroll = -1;
			}
		})

		$(window).on('blur.ace.help', function(){
			scroll = $(window).scrollTop();
		});
	}
	var releaseFocus = function() {
		$(document).off('focusin.ace.help');
		$(window).off('blur.ace.help');
	}


	this.toggle = function() {
		if(active) {
			self.disable();
		}
		else {
			self.enable();
		}
	}

	this.enable = function() {
		if(active) return;
		if(typeof _.settings.before_enable === 'function' && _.settings.before_enable.call(self) === false) return;
		////

		//if( !created ) this.init();
		active = true;
		
		$('.onpage-help-backdrop, .onpage-help-section').removeClass('hidden');
		
		ovfx = document.body.style.overflowX;
		document.body.style.overflowX = 'hidden';//hide body:overflow-x
		
		display_help_sections();
		captureFocus();

		////
		if(typeof _.settings.after_enable === 'function') _.settings.after_enable.call(self);
	}
	
	this.disable = function() {
		if(!active) return;
		if(typeof _.settings.before_disable === 'function' && _.settings.before_disable.call(self)) return;
		////
				
		active = false;
		$('.onpage-help-backdrop, .onpage-help-section').addClass('hidden');

		document.body.style.overflowX = ovfx;//restore body:overflow-x
		releaseFocus();
		
		////
		if(typeof _.settings.after_disable === 'function') _.settings.after_disable.call(self);
	}
	
	this.is_active = function() {
		return active;
	}
	this.show_section_help = function(section) {
		launch_help_modal(section, true);
	}
	
	
	this.init = function() {
		if( created ) return;
	
		help_container = 
		$('<div class="onpage-help-container" id="onpage-help-container" tabindex="-1" />')
		.appendTo('body');

		help_container.append('<div class="onpage-help-backdrop hidden" />')

		//update to correct position and size
		$(window).on('resize.onpage_help', function() {
			if(!active) return;
			display_help_sections();
			
			if( help_modal != null && help_modal.hasClass('in') ) {
				setBodyHeight();				
				disableBodyScroll();
			}
		})

		created = true;
	}
	this.init();//create once at first
	

	///////////////////////////
	this.update_sections = function() {
		save_sections(true);//reset sections, maybe because of new elements and comments inserted into DOM
	}


	function display_help_sections() {
		if(!active) return;

		save_sections();//finds comments and relevant help sections

		body_h = document.body.scrollHeight - 2;
		body_w = document.body.scrollWidth - 2;

		//we first calculate all positions
		//because if we calculate one position and then make changes to DOM,
		//next position calculation will become slow on Webkit, because it tries to re-calculate layout changes and things
		//i.e. we batch call all and save offsets and scrollWidth, etc and then use them later in highlight_section
		//Firefox doesn't have such issue
		for(var name in section_start) {
			if(section_start.hasOwnProperty(name)) {
				save_section_offset(name);
			}
		}
		for(var name in section_start) {
			if(section_start.hasOwnProperty(name)) {
				highlight_section(name);
			}
		}
	}

	
	//finds comments and relevant help sections
	function save_sections(reset) {
		if( !(reset === true || section_count == 0) ) return;//no need to re-calculate sections, then return
		if(reset === true) help_container.find('.onpage-help-section').remove();

		section_start = {};
		section_end = {};
		section_count = 0;
		
		var count1 = 0, count2 = 0;
		
		//find all relevant comments
		var comments = $('*').contents().filter(function(){ return this.nodeType == 8/**Node.COMMENT_NODE;*/ })
		$(comments).each(function() {
			var match
			if( (match = $.trim(this.data).match(/#section\s*:\s*([\w\d\-\.\/]+)/i)) ) {
				var section_name = match[1];
				if( !(section_name in section_start) ) 	section_start[ section_name ] = this;
			}
			if( (match = $.trim(this.data).match(/\/section\s*:\s*([\w\d\-\.\/]+)/i)) ) {
				var section_name = match[1];
				if( !(section_name in section_end) && (section_name in section_start) ) {
					section_end[ section_name ] = this;
					section_count++;
				}
			}
		})
	}


	
	function save_section_offset(name) {
		if( !(name in section_start) || !(name in section_end) ) return;
		
		var x1 = 1000000, y1 = 1000000, x2 = -1000000, y2 = -1000000;
		var visible = false;

		
		var elements = [];
		
		var start = section_start[name];
		var end = section_end[name];
		while(start != end) {
			start = start.nextSibling;
			if(start == null) break;
			else if(start.nodeType == 1 /**Node.ELEMENT_NODE*/) elements.push(start);
		}

		var elen = elements.length;
		if(elen > 0 && !_.settings['include_all']) {
			//calculate dimension of only first and last element
			elements = elen == 1 ? [elements[0]] : [elements[0], elements[elen - 1]]
		}

		$(elements).each(function() {
			var $this = $(this);
			if( $this.is(':hidden') ) return;
			
			var off = $this.offset();
			var w = $this.outerWidth();
			var h = $this.outerHeight();
			
			if( !off || !w || !h ) return;
			
			visible = true;
			if(off.left < x1) x1 = off.left;
			if(off.left + w > x2) x2 = off.left + w;
			
			if(off.top < y1) y1 = off.top;
			if(off.top + h > y2) y2 = off.top + h;
		});
		
					
		if( !visible ) {
			section_rect[name] = {is_hidden: true}
			return;
		}
		
		x1 -= 1;
		y1 -= 1;
		x2 += 1;
		y2 += 1;
		
		
		var width = x2 - x1, height = y2 - y1;
		//section_rect is out of window ???
		if(x1 + width < 2 || x1 > body_w || y1 + height < 2 || y1 > body_h ) {
			section_rect[name] = {is_hidden: true}
			return;
		}

		section_rect[name] = {
			'left': parseInt(x1),
			'top': parseInt(y1),
			'width': parseInt(width),
			'height': parseInt(height)
		}
	}


	function highlight_section(name) {
		if( !(name in section_rect) || !help_container ) return;

		//div is the highlighted box above each section
		var div = help_container.find('.onpage-help-section[data-section="'+name+'"]').eq(0);
		if(div.length == 0)	{
			div = $('<a class="onpage-help-section" href="#" />').appendTo(help_container);
			if(ie_fix) div.append('<span class="ie-hover-fix" />');
			
			if(_.settings.icon_1) div.append('<i class="help-icon-1 '+_.settings.icon_1+'"></i>');
			if(_.settings.icon_2) div.append('<i class="help-icon-2 '+_.settings.icon_2+'"></i>');
			
			div.attr('data-section', name);

			div.on('click', function(e) {
				e.preventDefault();
				launch_help_modal(name);
			});
		}

		var rect = section_rect[name];
		if(rect['is_hidden'] === true) {
			div.addClass('hidden');
			return;
		}
		
		div.css({
			left: rect.left,
			top: rect.top,
			width: rect.width,
			height: rect.height
		});
		

		div.removeClass('hidden');
		div.removeClass('help-section-small help-section-smaller');
		if(rect.height < 55 || rect.width < 55) {
			div.addClass('help-section-smaller');
		}
		else if(rect.height < 75 || rect.width < 75) {
			div.addClass('help-section-small');
		}
	}
	

	var nav_list = [];
	var nav_pos = -1;
	var mbody = null;
	var maxh = 0;
	var help_modal = null;

	//disable body scroll, when modal content has no scrollbars or reached end of scrolling
	function disableBodyScroll() {
		if (!mbody) return;
		
		var body = mbody[0];
		var disableScroll = body.scrollHeight <= body.clientHeight;

		mbody.parent()
		.off('mousewheel.help DOMMouseScroll.help')
		.on('mousewheel.help DOMMouseScroll.help', function(event) {
			if(disableScroll) event.preventDefault();
			else {
				var delta = event.originalEvent.detail < 0 || event.originalEvent.wheelDelta > 0 ? 1 : -1

				if(delta == -1 && body.scrollTop + body.clientHeight >= body.scrollHeight) event.preventDefault();
				else if(delta == 1 && body.scrollTop <= 0) event.preventDefault();
			}
		});
	}
	
	function setBodyHeight() {
		if (!mbody) return;
		
		var diff = parseInt(help_modal.find('.modal-dialog').css('margin-top'));
		diff = diff + 110 + parseInt(diff / 2);
		maxh = parseInt( $(window).innerHeight() - diff + 40 );
		mbody.css({'max-height': maxh});
	}


	function launch_help_modal(section_name, save_to_list) {
		if(help_modal == null) {
			help_modal = $('<div id="onpage-help-modal" class="modal onpage-help-modal" tabindex="-1" role="dialog" aria-labelledby="HelpModalDialog" aria-hidden="true">\
			  <div class="modal-dialog modal-lg">\
				<div class="modal-content">\
					<div class="modal-header">\
					  <div class="pull-right onpage-help-modal-buttons">\
						<button aria-hidden="true" data-navdir="up" type="button" class="disabled btn btn-white btn-success btn-sm"><i class="ace-icon fa fa-level-up fa-flip-horizontal bigger-125 icon-only"></i></button>\
						&nbsp;\
						<button aria-hidden="true" data-navdir="back" type="button" class="disabled btn btn-white btn-info btn-sm"><i class="ace-icon fa fa-arrow-left icon-only"></i></button>\
						<button aria-hidden="true" data-navdir="forward" type="button" class="disabled btn btn-white btn-info  btn-sm"><i class="ace-icon fa fa-arrow-right icon-only"></i></button>\
						&nbsp;\
						<button aria-hidden="true" data-dismiss="modal" class="btn btn-white btn-danger btn-sm" type="button"><i class="ace-icon fa fa-times icon-only"></i></button>\
					  </div>\
					  <h4 class="modal-title">Help Dialog</h4>\
					</div>\
					<div class="modal-body"><div class="onpage-help-content"></div></div>\
				</div>\
			  </div>\
			</div>').appendTo('body');
		
			mbody = help_modal.find('.modal-body');
			mbody.css({'overflow-y': 'auto', 'overflow-x': 'hidden'});
			
			help_modal.css({'overflow' : 'hidden'})
			.on('show.bs.modal', function() {
				releaseFocus();
			})
			.on('hidden.bs.modal', function() {
				captureFocus();
			})
			
			help_modal.find('.onpage-help-modal-buttons').on('click', 'button[data-navdir]', function() {
				var dir = $(this).attr('data-navdir');
				if(dir == 'back') {
					if(nav_pos > 0) {
						nav_pos--;
						launch_help_modal(nav_list[nav_pos], false);
					}
				}
				else if(dir == 'forward') {
					if(nav_pos < nav_list.length - 1) {
						nav_pos++;
						launch_help_modal(nav_list[nav_pos], false);//don't save to history list, already in the list
					}
				}
				else if(dir == 'up') {
					var $this = $(this), url;
					if( $this.hasClass('disabled') || !(url = $this.attr('data-url')) ) return;
					
					launch_help_modal(url , true);//add to history list
				}
			});
		}


		if( !help_modal.hasClass('in') ) {
			if( document.body.lastChild != help_modal[0] ) $(document.body).append(help_modal);//move it to become the last child of body
			help_modal.modal('show');
			
			setBodyHeight();
		}

		help_modal.find('.modal-title').wrapInner("<span class='hidden' />").append('<i class="fa fa-spinner fa-spin blue bigger-125"></i>');
		var content = $('.onpage-help-content');
		content.addClass('hidden')
		
		$(document.body).removeClass('modal-open');//modal by default hides body scrollbars, but we don't want to do so, because on modal hide, a winow resize is triggered
		
		var parts = section_name.match(/file\:(.*?)\:(.+)/i);
		if(parts && parts.length == 3) {
			display_codeview(parts[2], parts[1], false);
			return;
		}

		section_name = section_name.replace(/^#/g, '');
		if(typeof _.settings.section_url === 'function') url = _.settings.section_url.call(self, section_name);

		$.ajax({url: url, dataType: 'text'})
		.done(function(result) {
			//find the title for this dialog by looking for a tag that has data-id attribute
			var title = '', excerpt = '';
			
			if(typeof _.settings.section_title === 'function') title = _.settings.section_title.call(self, result, section_name, url);
			else {
				var escapeSpecialChars = function(name) {
					return name.replace(/[\-\.\(\)\=\"\'\\\/]/g, function(a,b){return "\\"+a;})
				}
				var tname = section_name;
				while(title.length == 0) {	
					var reg_str = '\\<([a-z][a-z0-9]*)(?:\\s+)(?:[^\\<\\>]+?)data\\-id\\=\\"\\#'+escapeSpecialChars(tname)+'\\"(?:[^\\>]*)\\>([\\s\\S]*?)</\\1>';
					
					var regexp = new RegExp(reg_str , "im");
					var arr = result.match(reg_str);
					if(arr && arr[2]) {
						title = arr[2];
						break;
					}

					//if no "#something.part" was not found try looking for "#something" instead
					var tpos
					if((tpos = tname.lastIndexOf('.')) > -1) {
						tname = tname.substr(0, tpos);
					} else break;
				}
			}

			help_modal.find('.modal-title').html( $.trim(title) || '&nbsp;' );

			if(typeof _.settings.section_content === 'function') excerpt = _.settings.section_content.call(self, result, section_name, url);
			else {
				var find1 = '<!-- #section:'+section_name+' -->';
				var pos1 = result.indexOf(find1);
				var pos2 = result.indexOf('<!-- /section:'+section_name+' -->', pos1);

				if(pos1 == -1 || pos2 == -1) {
					help_modal.find('.modal-title').html( '&nbsp;' );
					return;
				}

				excerpt = result.substring(pos1 + find1.length + 1, pos2);
			}

			
			//convert `<` and `>` to `&lt;` and `&gt;` inside code snippets
			if(typeof _.settings.code_highlight === 'function') {
				excerpt = _.settings.code_highlight.call(self, excerpt);
			}
			else {
				//find prism & rainbow style pre tags and replace < > characters with &lt; &gt;
				excerpt =
				excerpt.replace(/\<pre((?:(?:.*?)(?:data\-language=["'](?:[\w\d]+)["'])(?:.*?))|(?:(?:.*?)(?:class=["'](?:.*?)language\-(?:[\w\d]+)(?:.*?)["'])(?:.*?)))\>([\s\S]+?)\<\/pre\>/ig, function(a, b, c){
					return '<pre'+(b)+'>'+c.replace(/\</g , '&lt;').replace(/\>/g , '&gt;')+'</pre>';
				});
			}


			//modify image paths if needed!
			if(typeof _.settings.img_url === 'function') {
				excerpt = excerpt.replace(/\<img(?:(?:.*?)src=["']([^"']+)["'])/ig, function(img, src) {
					var new_src = _.settings.img_url.call(self, src);
					return img.replace(src, new_src)
				});
			}


			//now update content area
			content.empty().append(excerpt);
			if(typeof _.settings.code_highlight === 'function') {
				_.settings.code_highlight.call(self, content);
			}
			else if(_.settings.code_highlight === 'rainbow') {
				try {
					Rainbow.color(content[0]);
				} catch(e) {}
			}
			else if(_.settings.code_highlight === 'prism') {
				try {
					content.find('pre[class*="language-"],code[class*="language-"]').each(function() {
						Prism.highlightElement(this);
					})
				} catch(e) {}
			}


			//wrap titles and contents inside panels
			if(_.settings.add_panels) {
				content
				.find(_.settings.panel_content_selector).each(function() {
					var header = $(this).prevAll(_.settings.panel_content_title);
					if(header.length == 0) return false;
					
					header =
					header.attr('class', 'panel-title')
					.wrapInner('<a class="help-panel-toggle" href="#" data-parent="#" data-toggle="collapse" />')
					.wrap('<div class="panel-heading" />')
					.closest('.panel-heading');

					$(this).wrap('<div class="panel panel-default panel-help"><div class="panel-collapse collapse"><div class="panel-body"></div></div></div>');
					$(this).closest('.panel').prepend(header);
				})
							
				var group_count = $('.panel-group').length;
				content.find('.panel').each(function() {
					if( $(this).parent().hasClass('panel-group') ) return;

					var group_id = 'panel-group-help-'+ (++group_count);
					var group = $('<div class="panel-group" />').insertBefore(this);
					group.attr('id', group_id);
					
					var panel_id = 0;
					group.siblings('.panel').appendTo(group);
					group.find('.help-panel-toggle')
					.append('<i class="pull-right ace-icon fa fa-plus" data-icon-show="ace-icon fa fa-plus" data-icon-hide="ace-icon fa fa-minus"></i>')
					.attr('data-parent', '#'+group_id)
					.each(function() {
						panel_id++;
						$(this).attr('data-target', '#'+group_id+'-'+panel_id);
						$(this).closest('.panel-heading').siblings('.panel-collapse').attr('id', group_id+'-'+panel_id);
					});
				});
				$(document).off('click.help-panel-toggle', '.help-panel-toggle').on('click.help-panel-toggle', '.help-panel-toggle', function(e) {
					e.preventDefault();
				});
			}


			///////////////////////////////////////////

			content.removeClass('hidden')

			var images = content.find('img:visible');
			if(images.length > 0) {
				//handle scrollbars when all images are loaded
				var ev_count = 0;
				images.off('.help_body_scroll').on('load.help_body_scroll error.help_body_scroll', function() {
					$(this).off('.help_body_scroll');
					ev_count++;
					if(ev_count >= images.length) disableBodyScroll();
				});
			}

			disableBodyScroll();
			content.find('.panel > .panel-collapse').on('shown.bs.collapse hidden.bs.collapse', function() {
				disableBodyScroll();
			});


			//save history list
			add_to_nav_list(section_name, save_to_list);
		
			var pos = -1;
			if((pos = section_name.lastIndexOf('.')) > -1) {
				section_name = section_name.substr(0, pos);
				help_modal.find('button[data-navdir=up]').removeClass('disabled').attr('data-url', section_name);
			}
			else {
				help_modal.find('button[data-navdir=up]').addClass('disabled').removeAttr('data-url').blur();
			}
		})
		.fail(function() {
			help_modal.find('.modal-title').find('.fa-spin').remove().end().find('.hidden').children().unwrap();
		});
	}//launch_help_modal


	$(document).on('click', '.onpage-help-modal a[href^="http"]', function() {
		$(this).attr('target', '_blank');
	});
	
	$(document).on('click', '.help-more', function(e) {
		e.preventDefault();
		var href = $(this).attr('href');
		launch_help_modal(href);
	});
	

	
	function add_to_nav_list(section_name, save_to_list) {
		if(save_to_list !== false) {
			if(nav_list.length > 0) {
				nav_list = nav_list.slice(0, nav_pos + 1);
			}
			if(nav_list[nav_list.length - 1] != section_name) {
				nav_list.push(section_name);
				nav_pos = nav_list.length - 1;
			}
		}
		
		if(nav_pos == 0){
			help_modal.find('button[data-navdir=back]').addClass('disabled').blur();
		}
		else {
			help_modal.find('button[data-navdir=back]').removeClass('disabled');
		}
		
		if(nav_pos == nav_list.length - 1){
			help_modal.find('button[data-navdir=forward]').addClass('disabled').blur();
		}
		else {
			help_modal.find('button[data-navdir=forward]').removeClass('disabled');
		}
	}

	
	$(document).on('click', '.open-file[data-open-file]', function() {
		help_modal.find('.modal-title').wrapInner("<span class='hidden' />").append('<i class="fa fa-spinner fa-spin blue bigger-125"></i>');
		$('.onpage-help-content').addClass('hidden')

		var url = $(this).attr('data-path') || $(this).text();
		var language = $(this).attr('data-open-file');
		display_codeview(url, language, true);
	});
	
	
	function display_codeview(url, language, save_to_list) {
		var $url = url;
	
		if(typeof _.settings.file_url === 'function') url = _.settings.file_url.call(self, url, language);
		$.ajax({url: url, dataType:'text'})
		.done(function(result) {

			add_to_nav_list('file:'+language+':'+$url, save_to_list);
			
			help_modal.find('button[data-navdir=up]').addClass('disabled').blur();
			help_modal.find('.modal-title').html($url).wrapInner('<code />');

			if(language != 'json') {
				if(language != 'css') {
					//replace each tab character with two spaces (only those that start at a new line)
					result = result.replace(/\n[\t]{1,}/g, function(p, q) {
						return p.replace(/\t/g, "  ");
					});
				} else {
					result = result.replace(/\t/g , "  ")
				}
			}
			else {
				language = 'javascript';
				result = JSON.stringify(JSON.parse(result), null, 2);//add spacing and somehow beautification
			}
			
			result = result.replace(/\>/g, '&gt;').replace(/\</g, '&lt;');

			var content = $('.onpage-help-content');
			content.removeClass('hidden').empty();

			if(typeof _.settings.code_highlight === 'function') {
				result = _.settings.code_highlight.call(self, result, language);
				content.html(result);
			}
			else if(_.settings.code_highlight === 'rainbow') {
				try {
					Rainbow.color(result, language, function(highlighted_code) {
						content.html(highlighted_code).wrapInner('<pre data-language="'+language+'" />');
					});
				} catch(e){}
			}
			else if(_.settings.code_highlight === 'prism') {
				try {
					result = Prism.highlight(result, Prism.languages[language] , language);
					content.html(result).wrapInner('<pre class="language-'+language+'" />');
				} catch(e){}
			}

		});
	}

}