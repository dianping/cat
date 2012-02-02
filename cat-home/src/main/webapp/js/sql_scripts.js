var hideRE=/\[:: hide ::\]/gi;
var showRE=/\[:: show ::\]/gi;


function toggleSection(name)
{
	var obj = document.getElementById(name);
	var ctl = document.getElementById(name + "_ctl");

	if (obj.style.display == "none") {
		obj.style.display = "inline";
		ctl.innerHTML = ctl.innerHTML.replace(showRE, "[:: hide ::]");
	} else {
		obj.style.display = "none";
		ctl.innerHTML = ctl.innerHTML.replace(hideRE, "[:: show ::]");
	}
	resizeIframe("iframe_" + name.substring(4));
	return false;
}

function computeHeight () {
    //window.console.log("db.oh:"+ document.body.offsetHeight);
    //window.console.log("d.h:"+ document.height);
    //window.console.log("db.sh:"+ document.body.scrollHeight);
    // msie does not define document.height
    if(document.height) {
	return document.body.offsetHeight+35;
    } else {
	return document.body.scrollHeight;
    }
  }

function resizeIframe(name) {
    var ifr = document.getElementById(name);
    ifr.style.height = ifr.contentWindow.computeHeight();
    if(window.parent != window) {
	window.parent.resizeIframe(window.frameElement.id);
    }
}

function toggleSectionHide(name)
{
	var obj = document.getElementById(name);
	var ctl = document.getElementById(name + "_ctl");

	if (obj.style.display == "inline") {
		obj.style.display = "none";
		ctl.innerHTML = ctl.innerHTML.replace(hideRE, "[:: show ::]");
	} else {
		obj.style.display = "inline";
		ctl.innerHTML = ctl.innerHTML.replace(showRE, "[:: hide ::]");
	}

	return false;
}

function toggleSQL(name)
{
	var obj = document.getElementById(name);

	if (obj.style.display == "none") {
		obj.style.display = "inline";
	} else {
		obj.style.display = "none";
	}

	return false;
}

function toggleSQLHide(name)
{
	var obj = document.getElementById(name);

	if (obj.style.display == "inline") {
		obj.style.display = "none";
	} else {
		obj.style.display = "inline";
	}

	return false;
}

function toggleSQL2(name)
{
	var obj = document.getElementById(name + "-1");

	if (obj.style.display == "none") {
		obj.style.display = "inline";
	} else {
		obj.style.display = "none";
	}

	obj = document.getElementById(name + "-2");

	if (obj.style.display == "none") {
		obj.style.display = "inline";
	} else {
		obj.style.display = "none";
	}   

	return false;
}
