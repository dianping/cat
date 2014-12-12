/*
 * jQuery Hotkeys Plugin
 * Copyright 2010, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * Based upon the plugin by Tzury Bar Yochay:
 * http://github.com/tzuryby/hotkeys
 *
 * Original idea by:
 * Binny V A, http://www.openjs.com/scripts/events/keyboard_shortcuts/
*/
(function(b){b.hotkeys={version:"0.8",specialKeys:{8:"backspace",9:"tab",10:"return",13:"return",16:"shift",17:"ctrl",18:"alt",19:"pause",20:"capslock",27:"esc",32:"space",33:"pageup",34:"pagedown",35:"end",36:"home",37:"left",38:"up",39:"right",40:"down",45:"insert",46:"del",96:"0",97:"1",98:"2",99:"3",100:"4",101:"5",102:"6",103:"7",104:"8",105:"9",106:"*",107:"+",109:"-",110:".",111:"/",112:"f1",113:"f2",114:"f3",115:"f4",116:"f5",117:"f6",118:"f7",119:"f8",120:"f9",121:"f10",122:"f11",123:"f12",144:"numlock",145:"scroll",186:";",191:"/",220:"\\",222:"'",224:"meta"},shiftNums:{"`":"~","1":"!","2":"@","3":"#","4":"$","5":"%","6":"^","7":"&","8":"*","9":"(","0":")","-":"_","=":"+",";":": ","'":'"',",":"<",".":">","/":"?","\\":"|"}};function a(d){if(typeof d.data=="undefined"){return}if(typeof d.data==="string"){d.data={keys:d.data}}if(!d.data||!d.data.keys||typeof d.data.keys!=="string"){return}var c=d.handler,e=d.data.keys.toLowerCase().split(" "),f=["text","password","number","email","url","range","date","month","week","time","datetime","datetime-local","search","color","tel"];d.handler=function(k){if(this!==k.target&&(/textarea|select/i.test(k.target.nodeName)||b.inArray(k.target.type,f)>-1)){return}var h=b.hotkeys.specialKeys[k.keyCode],m=(k.type==="keydown"||k.type==="keypress")&&String.fromCharCode(k.which).toLowerCase();modif="",possible={};if(k.altKey&&h!=="alt"){modif+="alt+"}if(k.ctrlKey&&h!=="ctrl"){modif+="ctrl+"}if(k.metaKey&&!k.ctrlKey&&h!=="meta"){modif+="meta+"}if(k.shiftKey&&h!=="shift"){modif+="shift+"}if(h){possible[modif+h]=true}if(m){possible[modif+m]=true;possible[modif+b.hotkeys.shiftNums[m]]=true;if(modif==="shift+"){possible[b.hotkeys.shiftNums[m]]=true}}for(var j=0,g=e.length;j<g;j++){if(possible[e[j]]){return c.apply(this,arguments)}}}}b.each(["keydown","keyup","keypress"],function(){b.event.special[this]={add:a}})})(this.jQuery);