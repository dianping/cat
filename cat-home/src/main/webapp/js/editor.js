$(document).ready(function() {
	var editor = ace.edit("editor");
	editor.setTheme("ace/theme/dawn");
	editor.getSession().setMode("ace/mode/xml");
	editor.setFontSize(13);
	
	$("#form").submit(function( event ) {
		var editor = ace.edit("editor");
		var content = editor.session.getValue();
		$('#content').val(content);
		return;
	});
});
