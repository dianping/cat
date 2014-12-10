/**
 <b>Auto content padding on fixed navbar &amp; breadcrumbs</b>.
 Navbar's content and height is often predictable and when navbar is fixed we can add appropriate padding to content area using CSS.
 
 But when navbar is fixed and its content size and height is not predictable we may need to add the necessary padding to content area dynamically using Javascript.

 It's not often needed and you can have good results using CSS3 media queries to add necessary paddings based on window size.
*/
ace.auto_padding = function($) {
	var navbar = $('.navbar').eq(0);
	var navbar_container = $('.navbar-container').eq(0);
	
	var sidebar = $('.sidebar').eq(0);
	
	var main_container = $('.main-container').get(0);
	
	var breadcrumbs = $('.breadcrumbs').eq(0);
	var page_content = $('.page-content').get(0);
	
	var default_padding = 8

	if(navbar.length > 0) {
	  $(window).on('resize.padding', function() {
		if( navbar.css('position') == 'fixed' ) {
			var padding1 = !ace.vars['nav_collapse'] ? navbar.outerHeight() : navbar_container.outerHeight();
			padding1 = parseInt(padding1);
			main_container.style.paddingTop = padding1 + 'px';
			
			if(ace.vars['non_auto_fixed'] && sidebar.length > 0) {
				if(sidebar.css('position') == 'fixed') {
					sidebar.get(0).style.top = padding1 + 'px';
				}
				else sidebar.get(0).style.top = '';
			}

			if( breadcrumbs.length > 0 ) {
				if(breadcrumbs.css('position') == 'fixed') {
					var padding2 = default_padding + breadcrumbs.outerHeight() + parseInt(breadcrumbs.css('margin-top'));
					padding2 = parseInt(padding2);
					page_content.style['paddingTop'] = padding2 + 'px';

					if(ace.vars['non_auto_fixed']) breadcrumbs.get(0).style.top = padding1 + 'px';
				} else {
					page_content.style.paddingTop = '';
					if(ace.vars['non_auto_fixed']) breadcrumbs.get(0).style.top = '';
				}
			}
		}
		else {
			main_container.style.paddingTop = '';
			page_content.style.paddingTop = '';
			
			if(ace.vars['non_auto_fixed']) {
				if(sidebar.length > 0) {
					sidebar.get(0).style.top = '';
				}
				if(breadcrumbs.length > 0) {
					breadcrumbs.get(0).style.top = '';
				}
			}
		}
	  }).triggerHandler('resize.padding');

	  $(document).on('settings.ace', function(ev, event_name, event_val) {
		if(event_name == 'navbar_fixed' || event_name == 'breadcrumbs_fixed') {
			if(ace.vars['webkit']) {
				//force new 'css position' values to kick in
				navbar.get(0).offsetHeight;
				if(breadcrumbs.length > 0) breadcrumbs.get(0).offsetHeight;
			}
			$(window).triggerHandler('resize.padding');
		}
	  });
	  
	  /**$('#skin-colorpicker').on('change', function() {
		$(window).triggerHandler('resize.padding');
	  });*/
	}

}