/**
 <b>Select a different skin</b>. It's good for demo only.
 You should hard code skin-specific changes inside your HTML/server-side code.
 Please refer to documentation for more info.
*/

ace.settings_skin = function($) {
  try {
	$('#skin-colorpicker').ace_colorpicker();
  } catch(e) {}

  $('#skin-colorpicker').on('change', function(){
	var skin_class = $(this).find('option:selected').data('skin');
	//skin cookie tip

	var body = $(document.body);
	body.removeClass('no-skin skin-1 skin-2 skin-3');

	//if(skin_class != 'skin-0') {
		body.addClass(skin_class);
		ace.data.set('skin', skin_class);
		//save the selected skin to cookies
		//which can later be used by your server side app to set the skin
		//for example: <body class="<?php echo $_COOKIE['ace_skin']; ?>"
	//} else ace.data.remove('skin');
	
	var skin3_colors = ['red', 'blue', 'green', ''];
	
	
		//undo skin-1
		$('.ace-nav > li.grey').removeClass('dark');
		
		//undo skin-2
		$('.ace-nav > li').removeClass('no-border margin-1');
		$('.ace-nav > li:not(:last-child)').removeClass('light-pink').find('> a > '+ace.vars['.icon']).removeClass('pink').end().eq(0).find('.badge').removeClass('badge-warning');
		$('.sidebar-shortcuts .btn')
		.removeClass('btn-pink btn-white')
		.find(ace.vars['.icon']).removeClass('white');
		
		//undo skin-3
		$('.ace-nav > li.grey').removeClass('red').find('.badge').removeClass('badge-yellow');
		$('.sidebar-shortcuts .btn').removeClass('btn-primary btn-white')
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).find(ace.vars['.icon']).removeClass(skin3_colors[i++]);
		})
	
	

	var skin0_buttons = ['btn-success', 'btn-info', 'btn-warning', 'btn-danger'];
	if(skin_class == 'no-skin') {
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).attr('class', 'btn ' + skin0_buttons[i++%4]);
		})
	}

	else if(skin_class == 'skin-1') {
		$('.ace-nav > li.grey').addClass('dark');
		var i = 0;
		$('.sidebar-shortcuts')
		.find('.btn').each(function() {
			$(this).attr('class', 'btn ' + skin0_buttons[i++%4]);
		})
	}

	else if(skin_class == 'skin-2') {
		$('.ace-nav > li').addClass('no-border margin-1');
		$('.ace-nav > li:not(:last-child)').addClass('light-pink').find('> a > '+ace.vars['.icon']).addClass('pink').end().eq(0).find('.badge').addClass('badge-warning');
		
		$('.sidebar-shortcuts .btn').attr('class', 'btn btn-white btn-pink')
		.find(ace.vars['.icon']).addClass('white');
	}

	//skin-3
	//change shortcut buttons classes, this should be hard-coded if you want to choose this skin
	else if(skin_class == 'skin-3') {
		body.addClass('no-skin');//because skin-3 has many parts of no-skin as well
		
		$('.ace-nav > li.grey').addClass('red').find('.badge').addClass('badge-yellow');
		
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).attr('class', 'btn btn-primary btn-white');
			$(this).find(ace.vars['.icon']).addClass(skin3_colors[i++]);
		})

	}

	//some sizing differences may be there in skins, so reset scrollbar size
	if('sidebar_scroll' in ace.helper) ace.helper.sidebar_scroll.reset();

 });
}