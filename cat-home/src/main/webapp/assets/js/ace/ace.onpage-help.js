jQuery(function($) {
	var help = null;//Onpage_Help instance

	
	//for ace demo pages, we temporarily disable fixed navbar, etc ... when help is enabled
	//because when an element is fixed, its highlighted help section should also become fixed?!
	var page_settings = {}
	var before_enable_help = function() {
		$('#btn-scroll-up').css('z-index', 1000000);//bring btn-scroll-up  higher , to be over our help area

		//save our current state of navbar, sidebar and breadcrumbs before enabling help
		page_settings['navbar'] = ace.settings.is('navbar', 'fixed')
		page_settings['sidebar'] = ace.settings.is('sidebar', 'fixed')
		page_settings['breadcrumbs'] = ace.settings.is('breadcrumbs', 'fixed')
		
		ace.settings.navbar_fixed(false , false);//now disable fixed navbar, which automatically disabled fixed sidebar and breadcrumbs
	}
	var after_disable_help = function() {
		$('#btn-scroll-up').css('z-index', '');

		//restore fixed state of navbar, sidebar, etc
		if( page_settings['breadcrumbs'] ) ace.settings.breadcrumbs_fixed(true, false, false);
		if( page_settings['sidebar'] ) ace.settings.sidebar_fixed(true, false, false);
		if( page_settings['navbar'] ) ace.settings.navbar_fixed(true, false, false);
	}
	
	var get_file_url = function(url, language) {
		//function that return the real path to a file which is being loaded
		return this.settings.base + '/' + url;
	}
	var get_section_url = function(section_name) {
		//according to a section_name such as `basics/navbar.toggle` return the file url which contains help content
		section_name = section_name || '';

		//for example convert `basic/navbar.layout.brand` to `basic/navbar`
		//because 'layout.brand' section is inside `basic/navbar.html` file
		var url = section_name.replace(/\..*$/g, '');

		var parts = url.split('/');
		if(parts.length == 1) {
			//for example convert `changes` to `changes/index.html`
			if(url.length == 0) url = 'intro';//or convert `empty string` to `intro/index.html`
			url = url + '/index.html';
		}
		else if(parts.length > 1) {
			//for example convert `basics/navbar.layout` to `basics/navbar.html`
			url = url + '.html';
		}
		return this.settings.base + '/docs/sections/' + url;
	}
	var get_img_url = function(src) {
		return this.settings.base + '/docs/' +src;
	}

	/**
	var code_highlight = function(e, language) {
		//'this' refers to 'Onpage_Help' object invoking this function
		if(typeof e === 'string') {
			if(typeof language === 'string') {
				//called when a file (html,css,etc) is loaded
				//'e' is a piece of code
				//maybe highlight the syntax it according to `language` and return result
			}
			else {
				//called before new help content is being displayed
				//'e' is a string which may contain <pre>...</pre> code sections
				//which you may want to highlight the code, or for example convert them < to &lt; and > to &gt;
				//and return the result
			}
		}
		else if(typeof e === 'object') {
			//called when new help content is displayed
			//'e' is an html element which may have "pre" children that you can syntax-highlight
		}
	}
	*/


	function startHelp() {
		if(help !== null) return;//already created?

		help = new Onpage_Help({
			'include_all': false,
			'base': ace.vars['base'] || '../..',
			'file_url': get_file_url,
			'section_url': get_section_url,
			
			'img_url': get_img_url,
			
			'before_enable': before_enable_help,
			'after_disable': after_disable_help
			
			//,'code_highlight': code_highlight
		})

		
		
		var help_container = $('#onpage-help-container');
		//add a custom button to enable/disable help
		help_container.append('<div class="ace-settings-container onpage-help-toggle-container">\
			<div id="onpage-help-toggle-btn" class="btn btn-app btn-xs btn-info ace-settings-btn onpage-help-toggle-btn">\
				<i class="onpage-help-toggle-text ace-icon fa fa-question bigger-150"></i>\
			</div>\
		</div>');

		$('#onpage-help-toggle-btn').on('click', function(e) {
			e.preventDefault();
			toggleHelp();
		})
		
		//add .container class to help container div when our content is put inside a ".container"
		$(document).on('settings.ace.help', function(ev, event_name, fixed) {
		   if(event_name == 'main_container_fixed') {
			  if(fixed) help_container.addClass('container');
			  else help_container.removeClass('container');
		   }
		}).triggerHandler('settings.ace.help', ['main_container_fixed', $('.main-container').hasClass('container')])
		
		//in ajax mode when a content is loaded via ajax, we may want to update help sections
		$(document).on('ajaxloadcomplete.ace.help', function() {
			help.update_sections();
		});
	}
	
	function toggleHelp() {
		help.toggle();
		
		var toggle_btn = $('#onpage-help-toggle-btn');
		toggle_btn.find('.onpage-help-toggle-text').removeClass('onpage-help-toggle-text');
		toggle_btn.toggleClass('btn-grey btn-info').parent().toggleClass('active');
	}


	$(window).on('hashchange.start_help', function(e) {
		if(help == null && window.location.hash == '#help') {
			startHelp();

			//add #help tag to sidebar links to enable help when navigating to the page
			$(document).on('click.start_help', '.sidebar .nav-list a', function() {
				var href = $(this).attr('href');
				if( !href.match(/\#help$/) ) $(this).attr('href', href+'#help');
			});
		}
	}).triggerHandler('hashchange.start_help');


	//some buttons inside demo pages that launch a help section
	$(document).on('click', '.btn-display-help', function(e) {
		e.preventDefault();

		startHelp();
		if( !help.is_active() ) toggleHelp();

		var section = $(this).attr('href');
		help.show_section_help(section);
	});
});
