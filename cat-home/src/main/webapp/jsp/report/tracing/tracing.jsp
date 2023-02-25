<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true" %>
<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.logview.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.logview.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.tracing.Model" scope="request"/>

<a:application>
    <res:useCss value="${res.css.local.logview_css}" target="head-css"/>
    <res:useJs value="${res.js.local.logview_js}" target="head-js"/>

    <script type="text/javascript">
        function downloadCurrentPage(aHtml) {
            aHtml.href = 'data:text/html;charset=UTF-8,' + encodeURIComponent(document.documentElement.outerHTML);
            aHtml.download = "cat-" + $("#traceId").val() + ".html";
        }

        function downloadAsImage(aHtml) {
            html2canvas(document.querySelector("html")).then(canvas => {
                let pageData = canvas.toDataURL('image/jpeg', 1.0);
                console.log(pageData)
                saveFile(pageData.replace("image/jpeg", "image/octet-stream"), $("#traceId").val() + new Date().getTime() + ".jpeg");
            });

            let saveFile = function (data, filename) {
                let save_link = document.createElementNS('http://www.w3.org/1999/xhtml', 'a');
                save_link.href = data;
                save_link.download = filename;
                let event = document.createEvent('MouseEvents');
                event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
                save_link.dispatchEvent(event);
            };

            html2canvas(document.body, {
                onrendered: function (canvas) {

                }
            })
        }

        $(document).ready(function () {
            $("#submitBtn").click(function () {
                let traceId = $("#traceId").val();
                if (traceId.length <= 0) {
                    alert("请输入链路ID");
                    return;
                }

                // 抓取数据
                let allText = "";
                let domain = $("#domain").val();
                let waterfall = $('input[name="waterfall"]:checked').val();
                $.ajax({
                    type: "get",
                    url: "/cat/r/m/" + traceId + "?domain=" + domain + "&waterfall=" + waterfall,
                    cache: false,
                    async: false,
                    data: {},
                    success: function (data) {
                        if (data.indexOf('抱歉，消息可能是丢失或者已存档。') >= 0) {
                            $("#detail").html('<div class="report">' +
                                '<h3 class="text-center error">链路ID [' + traceId + '] 无效或者数据已被归档</h3>' +
                                '</div>');
                            return;
                        }
                        let start = data.indexOf('<table class="logview">');
                        let end = data.indexOf('</table>');
                        data = data.substr(start, end - start + 8);
                        console.log(data);
                        // data = data.replaceAll('<a href="', '<a href="/cat/r/m/');
                        allText = data;
                    },
                    error: function (request, status, ex) {
                        console.error(status + ";" + ex);
                        debugger
                        $("#detail").html('<div class="report">' +
                            '<h3 class="text-center error">链路ID [' + traceId + '] 查询失败，服务器状态：'
                            + status + '</h3></div>');
                    }
                });

                if (allText.length > 0) {
                    allText = escapeHtml(allText);
                    $("#detail").html(allText);
                }
            });

            function escapeHtml(allText) {
                let re = new RegExp("&lt;", "g");
                allText = allText.replace(re, "<");
                re = new RegExp("&gt;", "g");
                allText = allText.replace(re, ">");
                return allText;
            }

            let QueryString = {
                data: {},
                Initial: function () {
                    let aPairs, aTmp;
                    let queryString = new String(window.location.search);
                    queryString = queryString.substr(1, queryString.length);
                    aPairs = queryString.split("&");
                    for (let i = 0; i < aPairs.length; i++) {
                        aTmp = aPairs[i].split("=");
                        this.data[aTmp[0]] = aTmp[1];
                    }
                },
                GetValue: function (key) {
                    return this.data[key];
                }
            };

            QueryString.Initial();

            $("#domain").val(QueryString.GetValue('domain'));
        });
    </script>
    <input id="domain" type="hidden" />
    <table style="width:100%;">
        <tr>
            <th>
                <div class="navbar-header pull-left position">
                    <form id="wrap_search" style="margin-bottom: 0;">
                        <div class="input-group">&nbsp;
                            <input type="text" name="traceId" id="traceId" style="width: 300px;height: 35px;"
                                   value="" placeholder="请输入链路ID"/>
                            <span class="input-group-btn">
                                <button type="button" id="submitBtn" class="btn btn-pink btn-sm"
                                        style="margin-top: 0px; height: 35px">Go</button>
                            </span>
                            &nbsp;&nbsp;
                            <div class="radio radio-inline" style="margin-top:-1px">
                                <label>
                                    <input type="radio" name="waterfall" value="true">
                                    图表
                                </label>
                                &nbsp;&nbsp;
                                <label>
                                    <input type="radio" name="waterfall" value="false" checked="checked">
                                    文本
                                </label>
                            </div>
                        </div>
                    </form>
                </div>
            </th>
        </tr>
    </table>
    <div id="detail">
        <h4 class="text-center" style="margin-top: 50px;">- -！链路ID从哪里来？监控大盘报错时，根据线索追踪，从调用链路上下文获取 traceId 信息</h4>
    </div>
</a:application>