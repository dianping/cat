<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request"/>
<c:set var="report" value="${model.report}"/>

<a:report title="HeartBeat Report" navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}"
          timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
    <jsp:attribute
            name="subtitle">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
    <jsp:body>
        <div class="tabbable">
            <ul class="nav nav-tabs" style="height: 35px;">
                <c:forEach varStatus="status" var="ipPids" items="${model.ipPidMap}">
                    <li class="${fn:contains(payload.realIp, ipPids.key)?'active':''}"><a href="#tab${status.index}"
                                                                                          data-toggle="tab">${ipPids.key}</a>
                    </li>
                </c:forEach>
            </ul>
            <div class="tab-content">
                <c:forEach varStatus="status" var="ipPids" items="${model.ipPidMap}">
                    <div class="tab-pane ${fn:contains(payload.realIp, ipPids.key)?'active':''}"
                         id="tab${status.index}">
                        <c:forEach var="pid" items="${ipPids.value}">
                            <c:choose>
                                <c:when test="${fn:contains(payload.realIp,pid)}">
                                    ［${pid}］&nbsp
                                </c:when>
                                <c:otherwise>
                                    ［<a href="?domain=${model.domain}&ip=${ipPids.key}-${pid}&date=${model.date}">${pid}</a>］&nbsp
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                </c:forEach>
            </div>
        </div>


        <table>
            <c:forEach items="${model.extensionGraph}" var="entry">
                <tr>
                    <th><h5 class='text-error' style="padding-left:20px;">${entry.key} Info</h5></th>
                </tr>
                <tr>
                    <td>
                        <c:set var="size" value="${entry.value.height}"/>
                        <c:set var="extensionHeight" value="${size*190 }"/>

                        <svg version="1.1" width="1200" height="${extensionHeight}" xmlns="http://www.w3.org/2000/svg">
                            <c:forEach items="${entry.value.svgs}" var="kv">
                                ${kv.value}
                            </c:forEach>
                        </svg>
                    </td>
                </tr>
            </c:forEach>
        </table>
        </table>

        <script type="text/javascript" src="/cat/js/appendHostname.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                appendHostname(${model.ipToHostnameStr});
            });
        </script>
    </jsp:body>
</a:report>
