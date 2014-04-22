<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<svg version="1.1" width="980" height="380" xmlns="http://www.w3.org/2000/svg">
  ${model.graph1}
  ${model.graph2}
  ${model.graph3}
  ${model.graph4}
</svg>
