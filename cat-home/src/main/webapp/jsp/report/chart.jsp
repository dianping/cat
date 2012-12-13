<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<res:bean id="res"/>
<!DOCTYPE html><html>
  <head>
    <style type="text/css">
      body {
        margin: 0px;
        padding: 0px;
      }
      #container {
        width : 600px;
        height: 384px;
        margin: 8px auto;
      }
    </style>
  </head>
  <body>
    <div id="container"></div>
    <res:useJs value="${res.js.local.flotr2_js}" target="bottom-js" />
   <res:useJs value="${res.js.local.example_js}" target="bottom-js" />
     </body>
</html>