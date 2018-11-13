<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
    <res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js"/>
    <res:useJs value="${res.js.local['editor.js']}" target="head-js"/>
    <script src='${model.webapp}/assets/js/editor/ace.js'></script>

    <style>
        .cat-alarm-tip {
            padding: 1em;
        }

        .cat-alarm-tip-container {
            padding: 0.5em;
            border: 1px solid #ffeeba;
            background-color: #fff3cd;
            color: #856404;
        }

        .cat-alarm-tip-container h4 {
            font-size: 1.5em;
            padding: 0;
            margin: 0 0 0.3em;
        }

        .cat-alarm-tip-container p {
            margin-bottom: 0.3em;
        }

        .cat-alarm-tip-container button {
            margin-right: 0.5em;
            padding: 0.2em 0.5em;
        }

        .cat-alarm-tip-container a.question {
            margin-right: 0.5em;
            cursor: not-allowed;
            color: #CCC;
        }
    </style>

    <div class="cat-alarm-tip">
        <div class="cat-alarm-tip-container">
            <h4>配置说明：</h4>
            <p>* local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;</p>
            <p>* hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；</p>
            <p>* job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为 false；</p>
            <p>* alarm-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为 false；</p>
            <p>* storage : 定义数据存储配置信息</p>
            <p>* local-report-storage-time : 定义本地报告存放时长，单位为（天）</p>
            <p>* local-logivew-storage-time : 定义本地日志存放时长，单位为（天）</p>
            <p>* local-base-dir : 定义本地数据存储目录</p>
            <p>* hdfs : 定义HDFS配置信息，便于直接登录系统</p>
            <p>* server-uri : 定义HDFS服务地址</p>
            <p>* remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）</p>
        </div>
    </div>

    <form name="serverConfigUpdate" id="form" method="post"
          action="${model.pageUri}?op=serverConfigUpdate">
        <table class="table table-striped table-condensed  table-hover">
            <tr>
                <td>
                    <input id="content" name="content" value="" type="hidden"/>
                    <div id="editor" class="editor">${model.content}</div>
                </td>
            </tr>
            <tr>
                <td style="text-align:center"><input class='btn btn-primary' id="serverConfigUpdate"
                                                     type="submit" name="submit" value="提交"/></td>
            </tr>
        </table>
    </form>
    <h4 class="text-center text-danger" id="state">&nbsp;</h4>
</a:config>
<script type="text/javascript">
    $(document).ready(function () {
        $('#overall_config').addClass('active open');
        $('#serverConfigUpdate').addClass('active');
        var state = '${model.opState}';
        if (state == 'Success') {
            $('#state').html('操作成功');
        } else {
            $('#state').html('操作失败');
        }
        setInterval(function () {
            $('#state').html('&nbsp;');
        }, 3000);
    });
</script>