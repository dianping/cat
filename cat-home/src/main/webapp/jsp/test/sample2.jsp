<%@ page contentType="image/svg+xml; charset=utf-8"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg version="1.1" width="800" height="500" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
  <title>Duration</title>
  <script type="text/javascript">
<![CDATA[
function v(obj) {
	var str = "";
	var o = 0;
	
	for (var i in obj) {
		str+=(i+":"+obj[i]);
	
		if (o) {
			o=0;
			str+="\n";
		} else {
			o=1;
			str+=", ";
		}
	}
	
	alert(str);
}

function show(evt, msg) {
   var svgDocument = evt.view.document;
   var target = svgDocument.getElementById("title");
   var newText = svgDocument.createTextNode(msg);

   target.replaceChild(newText,target.firstChild);
}
]]></script>

  <g stroke="#003f7f" fill="white">
  	<text id="title" x="20" y="20" font-size="18" stroke="black" fill="black">Title</text>
  	
	<line x1="200" y1="200" x2="600" y2="200" stroke-width="8" stroke="black" stroke-opacity="0.5"/>
	<line x1="200" y1="200" x2="400" y2="60" stroke-width="2" stroke-opacity="0.5"/>
	<line x1="600" y1="200" x2="400" y2="60" stroke-width="12" stroke-opacity="0.5" onmouseover="show(evt, 'First to 10.1.6.37');"/>
	
	<image xlink:href="/cat/images/box.png" x="169" y="158" width="62" height="84"/>
	<text x="200" y="250" font-size="18" alignment-baseline="central" text-anchor="middle" stroke="black" fill="black">10.1.6.48</text>
	
	<image xlink:href="/cat/images/box.png" x="569" y="158" width="62" height="84"/>
	<text x="600" y="250" font-size="18" alignment-baseline="central" text-anchor="middle" stroke="black" fill="black">10.1.6.37</text>

	<circle r="30" cx="400" cy="60" fill="red"/>
	<circle r="50" cx="400" cy="60" fill-opacity="0.5" fill="#ddd" onmouseover="show(evt, 'First');"/>
	<text x="400" y="60" font-size="18" alignment-baseline="central" text-anchor="middle" stroke="black" fill="black">First</text>
	
	<circle r="30" cx="200" cy="400" fill="blue"/>
	<circle r="50" cx="200" cy="400" fill-opacity="0.5" fill="#eee"/>
	<text x="200" y="400" font-size="18" alignment-baseline="central" text-anchor="middle" stroke="black" fill="black">Second</text>
	
	<circle r="30" cx="600" cy="400" fill="green"/>
	<circle r="50" cx="600" cy="400" fill-opacity="0.5" fill="yellow"/>
	<text x="600" y="400" font-size="18" alignment-baseline="central" text-anchor="middle" stroke="black" fill="black">Third</text>
  </g>
</svg>