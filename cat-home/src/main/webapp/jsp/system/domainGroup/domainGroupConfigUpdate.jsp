<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
    <script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>
    <script type="text/javascript">
        function remove(row) {
            $('#row_' + row).remove();
        }

        function add() {
            var n = document.getElementsByClassName("group").length;
            n = n + 1;
            $("#content").append('<tr id=row_' + n + '><td width="10%"><input type="text" name="pars" class="group" id="group_"' + n + ' placeholder="Enter group ..."/></td>'
                + '<td> <input type="text" name="pars" id="tag_"' + n + ' class="myTag" placeholder="Enter ip ..."/></td>'
                + '<td><a href="javascript:remove(' + n + ');" class="btn btn-danger btn-sm" ><i class="ace-icon fa fa-trash-o bigger-120"></i></a></td></div>');
            var tag_input = $('.myTag');
            try {
                tag_input.tag(
                    {
                        placeholder: tag_input.attr('placeholder'),
                    }
                )
            }
            catch (e) {
                //display a textarea for old IE, because it doesn't support this plugin or another one I tried!
                tag_input.after('<textarea id="' + tag_input.attr('id') + '" name="' + tag_input.attr('name') + '" rows="3">' + tag_input.val() + '</textarea>').remove();
                //$('#form-field-tags').autosize({append: "\n"});
            }
        }

        function submit() {
            var domain = {};
            domain["id"] = $('#domain').val();
            var groups = {};
            domain["groups"] = groups;
            $('.group').each(function () {
                var name = $(this).val();
                var map = {};
                var iplst = [];
                groups[name] = map;
                map["id"] = name;
                map["ips"] = iplst;
                var id = $(this).attr('id');
                var index = id.split("_")[1];
                var ipstr = $("#tag_" + index).val();
                var ips = ipstr.split(",");
                for (var i in ips) {
                    console.log(ips[i]);
                    iplst.push(ips[i].trim());
                }
            });
            var content = JSON.stringify(domain);
            var domainstr = $('#domain').val();
            window.location.href = "?op=domainGroupConfigSubmit&domain=" + encodeURIComponent(domainstr) + "&content=" + encodeURIComponent(content);
        }

        $(document).ready(function () {
            $('#projects_config').addClass('active open');
            $('#domainGroupConfigUpdate').addClass('active');
            <c:forEach var="entry" items="${model.groupDomain.groups}" varStatus="status">
            var tag_input = $('#tag_${status.index}');
            try {
                tag_input.tag(
                    {
                        placeholder: tag_input.attr('placeholder'),
                    }
                )

                //programmatically add a new
                var $tag_obj = $('#tag_${status.index}').data('tag');
                <c:forEach var="item" items="${entry.value.ips}" varStatus="status">
                $tag_obj.add("${item}");
                </c:forEach>
            }
            catch (e) {
                //display a textarea for old IE, because it doesn't support this plugin or another one I tried!
                tag_input.after('<textarea id="' + tag_input.attr('id') + '" name="' + tag_input.attr('name') + '" rows="3">' + tag_input.val() + '</textarea>').remove();
                //$('#form-field-tags').autosize({append: "\n"});
            }
            </c:forEach>
        });
    </script>

    <h3 class="text-center text-success">编辑机器分组配置</h3>
    <table class="table table-striped table-condensed " id="content">
        <input type="hidden" name="op" value="domainGroupConfigSubmit"/>
        <tr>
            <th width="10%">项目组</th>
            <c:choose>
                <c:when test="${not empty model.groupDomain.id}">
                    <th><input type="text" id="domain" value="${model.groupDomain.id}" size="50" readonly/></th>
                </c:when>
                <c:otherwise>
                    <th><input type="text" id="domain" value="${model.groupDomain.id}" size="50"/></th>
                </c:otherwise>
            </c:choose>
            <th width="5%"><a href="javascript:add();" class="btn btn-primary btn-sm">
                <i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
        </tr>
        <c:forEach var="entry" items="${model.groupDomain.groups}" varStatus="status">
            <tr id="row_${status.index}">
                <td width="10%"><input type="text" class="group" id="group_${status.index}" value="${entry.value.id}"
                                       readonly/></td>
                <td>
                    <input type="text" name="pars" class="tag" id="tag_${status.index}" placeholder="Enter ip ..."/>
                </td>
                <td width="5%"><a href="javascript:remove(${status.index})" class="btn btn-danger btn-sm">
                    <i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
            </tr>
        </c:forEach>
    </table>
    <input class='btn btn-primary btn-sm' style="MARGIN-LEFT:45%" type="button" value="提交" onclick="submit();"/>
</a:config>

<style>
    .tags {
        width: 95%;
    }
</style>