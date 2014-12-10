/**
 <b>Auto Container</b> Adds .container when window size is above 1140px.
 In Bootstrap you should stick with fixed width breakpoints.
 You can use this feature to enable fixed container only when window size is above 1140px
*/
ace.auto_container = function($) {
 $(window).on('resize.auto_container', function() {
	var enable = ace.vars.window['width'] > 1140;
	try {
		ace.settings.main_container_fixed(enable, false, false);
	} catch(e) {
		if(enable) $('.main-container,.navbar-container').addClass('container');
		else $('.main-container,.navbar-container').removeClass('container');
		$(document).trigger('settings.ace', ['main_container_fixed' , enable]);
	}
 }).triggerHandler('resize.auto_container');
}