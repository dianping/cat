
function TabManager() {
	this.htmlTypeRegex = new RegExp("^text/html.*");
	this.tabId2Tid = {};
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
					console.log("got server mappinag " + this.serverMapping);
				} else {
					console.error("error get server mapping from " + mappingUrl);
				}
			}
			xhr.send();
		}
	};
	
	this.iconClicked = function(tabId) {
		var rootId = this.tabId2Tid["" + tabId].rootId;
		var server = this.tabId2Tid["" + tabId].server;
		server = server.split(",")[0];
		var newServer = this.serverMapping[server];
		if(newServer) {
			server = newServer;
		}
		
		chrome.tabs.create({url: "http://" + server + "/cat/r/m/" + rootId});
	};
	
	this.hasCatRootId = function(tabId, headers) {
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
			this.tabId2Tid["" + tabId] = {rootId: rootId, server: server};
			this.updateServerMapping(server);
			return true;
		} else {
			return false;
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
	
	this.headersReceived = function(tabId, headers) {
		if(this.tabId2Tid["" + tabId]) {
			chrome.pageAction.show(tabId);
		} else {
			if(this.hasCatRootId(tabId, headers)) {
				chrome.pageAction.show(tabId);
			}
		}
	};
	
	this.headersWillBeSent = function(tabId, headers) {
		headers.push({name: "X-CAT-TRACING", value: "true"});
		return {requestHeaders: headers};
	};
	
	this.tabRemoved = function(tabId) {
		delete this.tabId2Tid["" + tabId];
	};
	
	this.tabUrlUpdated = this.tabRemoved;

}

var tabMgr = new TabManager();
var responseCallback = function(details) {
	var headers = details.responseHeaders;
	var tabId = details.tabId;
	tabMgr.headersReceived(tabId, headers);
};
var requestCallback = function(details) {
	var headers = details.requestHeaders;
	var tabId = details.tabId;
	return tabMgr.headersWillBeSent(tabId, headers);
};
var filter = {urls: ["*://*/*"]};

chrome.webRequest.onHeadersReceived.addListener(responseCallback, filter, ["responseHeaders"]);
//chrome.webRequest.onBeforeSendHeaders.addListener(requestCallback, filter, ["blocking", "requestHeaders"]);

chrome.pageAction.onClicked.addListener(function(tab) {
	//alert(localStorage["favorite_color"]);
	tabMgr.iconClicked(tab.id);
});

chrome.tabs.onRemoved.addListener(function(tabId, removeInfo) {
	tabMgr.tabRemoved(tabId);	
});

chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
	if(changeInfo.url) {
		tabMgr.tabUrlUpdated(tabId);		
	}	
});
