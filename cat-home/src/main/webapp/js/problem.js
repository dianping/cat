function show(anchor) {
	$.ajax({
		type : "get",
		url : anchor.href,
		success : function(data, textStatus) {
			alertWin("Problem Detail", data);
		}
	});
	return false;
}

function requestGroupInfo(anchor){
	$.ajax({
		type : "get",
		url : anchor.href,
		success : function(data, textStatus) {
			document.getElementById('machineThreadGroupInfo').innerHTML=data;
		}
	});
	return false;
}

$(document).keypress(function(e) {
	if (e.which == 113) {
		var lastMsgObj = document.getElementById("msgObjRef");
		if (lastMsgObj != null) {
			document.body.removeChild(lastMsgObj);
		}
	}
});

function alertWin(title, msg) {
	var lastMsgObj = document.getElementById("msgObjRef");
	if (lastMsgObj != null) {
		document.body.removeChild(lastMsgObj);
	}
	var w = 500; // default width
	var h = 250; // defaylt hight
	var iWidth = document.documentElement.clientWidth;
	var iHeight = document.documentElement.clientHeight;
	if (iHeight < 600)
		iHeight = 600;
	if (iWidth < 1000)
		iWidth = 1000;
	var top_ = 150 + document.documentElement.scrollTop
			+ document.body.scrollTop;
	var left_ = 500 + document.documentElement.scrollLeft
			+ document.body.scrollLeft;
	var msgObj = document.createElement("div");
	msgObj.setAttribute("id", "msgObjRef");
	msgObj.style.cssText = "top:" + top_ + "px;left:" + left_ + "px";
	document.body.appendChild(msgObj);
	var table = document.createElement("table");
	msgObj.appendChild(table);
	var tr = table.insertRow(-1);
	var titleBar = tr.insertCell(-1);
	titleBar.setAttribute("id", "titleBar");
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
	closeBtn.setAttribute("id", "closeBtn");
	closeBtn.innerHTML = "×";
	closeBtn.onclick = function() {
		document.body.removeChild(msgObj);
	};
	var msgBox = table.insertRow(-1).insertCell(-1);
	msgBox.colSpan = 2;
	msgBox.innerHTML = msg;
	function getEvent() {
		return window.event || arguments.callee.caller.arguments[0];
	}
}