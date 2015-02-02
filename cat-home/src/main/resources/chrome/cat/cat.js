
function TabManager() {
	this.htmlTypeRegex = new RegExp("^text/html.*");
	this.tabId2Txid = {};
	this.serverMapping = {};
	this.serverMappingUpdated = false;

	this.updateServerMapping = function(server) {
		if(!this.serverMappingUpdated) {
			this.serverMappingUpdated = true;
			var xhr = new XMLHttpRequest();
			var mappingUrl = "http://" + server + "/cat/s/plugin/chrome?mapping=true"
			xhr.open("GET", mappingUrl, true);
			xhr.onreadystatechange = function() {
				if (xhr.readyState == 4) {	// 4 = DONE
					this.serverMapping = JSON.parse(xhr.responseText);
					console.debug("got server mappinag " + this.serverMapping);
				}
			}
			xhr.send();
		}
	};
	
	this.iconClicked = function(tabId) {
		var rootId = this.tabId2Txid["" + tabId].rootId;
		var server = this.tabId2Txid["" + tabId].server;
		server = server.split(",")[0];
		var newServer = this.serverMapping[server];
		if(newServer) {
			server = newServer;
		}
		
		chrome.tabs.create({url: "http://" + server + "/cat/r/m/" + rootId});
	};
	
	this.inspectAndRecordCatRootId = function(tabId, headers, url) {
		for (var i = 0; i < headers.length; i++) {
			var name = headers[i]["name"];
			var value = headers[i]["value"];
			if(name === "X-CAT-ROOT-ID") {
				var rootId = value;
			} else if(name ==="X-CAT-SERVER") {
				var server = value;
			}
		}
		
		if(rootId && server) {
			this.tabId2Txid["" + tabId] = {rootId: rootId, server: server, url: url};
			console.debug(tabId + " recognized as cat enabled with rootId " + rootId);
			this.updateServerMapping(server);
		}
	};
	
	this.isHtmlResponse = function(headers) {
		for (var i = 0; i < headers.length; i++) {
			var name = headers[i]["name"];
			var value = headers[i]["value"];
			if(name == "Content-Type" && this.htmlTypeRegex.test(value)) {
				return true;
			}
		}
		return false;
	};
	
	this.headersReceived = function(tabId, headers, url) {
		console.debug("header for " + tabId + " with url " + url);
		for (var i = 0; i < headers.length; i++) {
			var name = headers[i]["name"];
			var value = headers[i]["value"];
			console.debug("\t" + name + " : " + value);
		}
		if(tabId <= 0) {	
			return;
		}

		this.inspectAndRecordCatRootId(tabId, headers, url);
	};
	
	this.headersWillBeSent = function(tabId, headers) {
		headers.push({name: "X-CAT-TRACE-MODE", value: "true"});
		return {requestHeaders: headers};
	};
	
	this.tabRemoved = function(tabId) {
		delete this.tabId2Txid["" + tabId];
	};
	
	this.tabUrlUpdated = function(tabId, url) {
		var tabCatInfo = this.tabId2Txid["" + tabId];
		if(tabCatInfo && tabCatInfo["url"] != url) {
			console.debug(tabId + " url updated, reset cat info");
			delete this.tabId2Txid["" + tabId];
		}
	};
	
	this.tabCompleted = function(tabId) {
		if(this.tabId2Txid["" + tabId]) {
			this.showIcon(tabId);
		}
	};
	
	this.tabReplaced = function(addedTabId, removedTabId) {
		console.debug("replacing " + removedTabId + " with " + addedTabId);
		if(this.tabId2Txid["" + addedTabId]) {
			this.showIcon(addedTabId);
		}
	};
	
	this.showIcon = function(tabId) {
		console.debug("show pageAction for " + tabId);
		chrome.pageAction.show(tabId);
	};
}

var tabMgr = new TabManager();
var responseCallback = function(details) {
	if("main_frame" == details.type){
		var headers = details.responseHeaders;
		var tabId = details.tabId;
		var url = details.url;
		tabMgr.headersReceived(tabId, headers, url);
	}
};
var requestCallback = function(details) {
	var headers = details.requestHeaders;
	var tabId = details.tabId;
	return tabMgr.headersWillBeSent(tabId, headers);
};
var filter = {urls: ["*://*/*"]};

chrome.webRequest.onHeadersReceived.addListener(responseCallback, filter, ["responseHeaders"]);
chrome.webRequest.onBeforeSendHeaders.addListener(requestCallback, filter, ["blocking", "requestHeaders"]);

chrome.pageAction.onClicked.addListener(function(tab) {
	//alert(localStorage["favorite_color"]);
	tabMgr.iconClicked(tab.id);
});

chrome.tabs.onRemoved.addListener(function(tabId, removeInfo) {
	console.debug(tabId + " removed");
	tabMgr.tabRemoved(tabId);	
});

chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
	console.debug(tabId + " updated");
	for(var x in changeInfo) {
		console.debug("\t" + x + " : " + changeInfo[x]);
	}
	var url = changeInfo.url;
	if(url) {
		console.debug(tabId + "'s url has changed");
		tabMgr.tabUrlUpdated(tabId, url);		
	} else if(changeInfo["status"] === "complete") {
		tabMgr.tabCompleted(tabId);	
	}	
});

chrome.tabs.onReplaced.addListener(function(addedTabId, removedTabId) {
	tabMgr.tabReplaced(addedTabId, removedTabId);
});
