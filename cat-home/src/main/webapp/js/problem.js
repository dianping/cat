$(function() {
	$('.minute').click(function(e) {
		e.preventDefault();
	});
});

$(function() {
	$('.error').click(function(e) {
		e.preventDefault();
	});

});

$(function() {
	$('.failure').click(function(e) {
		e.preventDefault();
	});

});

$(function() {
	$('.long-url').click(function(e) {
		e.preventDefault();
	});

});

function show(anchor) {
	$.ajax({
		type : "get",
		url : anchor.href,
		success : function(data, textStatus) {
			alertWin("Problem Detail", data, 500, 250)
		}
	});
}

function alertWin(title, msg, w, h) {
	var lastMsgObj = document.getElementById("msgObjRef");
	if (lastMsgObj != null) {
		document.body.removeChild(lastMsgObj);
	}
	var titleheight = "22px";
	var bordercolor = "#666699";
	var titlecolor = "#FFFFFF";
	var titlebgcolor = "#06C";
	var bgcolor = "#FFFFFF";
	var iWidth = document.documentElement.clientWidth;
	var iHeight = document.documentElement.clientHeight;
	if (iHeight < 600)
		iHeight = 600;
	if (iWidth < 1000)
		iWidth = 1000;
	var top_ = 160 + document.documentElement.scrollTop
			+ document.body.scrollTop;
	/*
	 * if (iHeight > h) top_ = (iHeight - h) / 2 - 50 + document.body.scrollTop +
	 * document.documentElement.scrollTop-50;
	 */
	var msgObj = document.createElement("div");
	msgObj.setAttribute("id", "msgObjRef");
	msgObj.style.cssText = "position:absolute;top:" + top_ + "px;left:400"
			+ "px;width:auto;height:auto;text-align:center;border:1px solid "
			+ bordercolor + ";background-color:" + bgcolor
			+ ";padding:1px;line-height:22px;z-index:102;";
	document.body.appendChild(msgObj);
	var table = document.createElement("table");
	msgObj.appendChild(table);
	table.style.cssText = "margin:0px;border:0px;padding:0px;";
	table.cellSpacing = 0;
	var tr = table.insertRow(-1);
	var titleBar = tr.insertCell(-1);
	// var titlewid = w - 20;
	// titlewidth = titlewid + 'px';
	titleBar.style.cssText = "height:" + titleheight
			+ "px;text-align:left;padding:3px;margin:0px;color:" + titlecolor
			+ ";border:1px solid " + bordercolor
			+ ";cursor:move;background-color:" + titlebgcolor;
	// titleBar.style.paddingLeft = "10px";
	titleBar.innerHTML = title;
	var moveX = 0;
	var moveY = 0;
	var moveTop = 0;
	var moveLeft = 0;
	var moveable = false;
	var docMouseMoveEvent = document.onmousemove;
	var docMouseUpEvent = document.onmouseup;
	titleBar.onmousedown = function() {
		var evt = getEvent();
		moveable = true;
		moveX = evt.clientX;
		moveY = evt.clientY;
		moveTop = parseInt(msgObj.style.top);
		moveLeft = parseInt(msgObj.style.left);

		document.onmousemove = function() {
			if (moveable) {
				var evt = getEvent();
				var x = moveLeft + evt.clientX - moveX;
				var y = moveTop + evt.clientY - moveY;
				if (x > 0 && (x + w < iWidth) && y > 0 && (y + h < iHeight)) {
					msgObj.style.left = x + "px";
					msgObj.style.top = y + "px";
				}
			}
		};
		document.onmouseup = function() {
			if (moveable) {
				document.onmousemove = docMouseMoveEvent;
				document.onmouseup = docMouseUpEvent;
				moveable = false;
				moveX = 0;
				moveY = 0;
				moveTop = 0;
				moveLeft = 0;
			}
		};
	};
	var closeBtn = tr.insertCell(-1);
	closeBtn.style.cssText = "cursor:pointer;background-color:" + titlebgcolor;
	closeBtn.innerHTML = "<span color:" + titlecolor + ";'>×</span>";
	closeBtn.onclick = function() {
		document.body.removeChild(msgObj);
	};
	var msgBox = table.insertRow(-1).insertCell(-1);
	msgBox.colSpan = 2;
	msgBox.innerHTML = msg;
	function getEvent() {
		return window.event || arguments.callee.caller.arguments[0];
	}
	$(function() {
		$('.minute').click(function(e) {
			e.preventDefault();
		});
	});
}