/**
* jQuery ligerUI  1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{
    var l = $.ligerui;

    $.fn.ligerGrid = function (options)
    {
        return $.ligerui.run.call(this, "ligerGrid", arguments);
    };

    $.fn.ligerGetGridManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetGridManager", arguments);
    };

    $.ligerDefaults.Grid = {
        title: null,
        width: 'auto',                          //宽度值
        height: 'auto',                          //宽度值
        columnWidth: null,                      //默认列宽度
        resizable: true,                        //table是否可伸缩
        url: false,                             //ajax url
        usePager: true,                         //是否分页
        page: 1,                                //默认当前页 
        pageSize: 10,                           //每页默认的结果数
        pageSizeOptions: [10, 20, 30, 40, 50],  //可选择设定的每页结果数
        parms: [],                         //提交到服务器的参数
        columns: [],                          //数据源
        minColToggle: 1,                        //最小显示的列
        dataType: 'server',                     //数据源：本地(local)或(server),本地是将读取p.data。不需要配置，取决于设置了data或是url
        dataAction: 'server',                    //提交数据的方式：本地(local)或(server),选择本地方式时将在客服端分页、排序。 
        showTableToggleBtn: false,              //是否显示'显示隐藏Grid'按钮 
        switchPageSizeApplyComboBox: false,     //切换每页记录数是否应用ligerComboBox
        allowAdjustColWidth: true,              //是否允许调整列宽     
        checkbox: false,                         //是否显示复选框
        allowHideColumn: true,                 //是否显示'切换列层'按钮
        enabledEdit: false,                      //是否允许编辑
        isScroll: true,                         //是否滚动
        onDragCol: null,                       //拖动列事件
        onToggleCol: null,                     //切换列事件
        onChangeSort: null,                    //改变排序事件
        onSuccess: null,                       //成功获取服务器数据的事件
        onDblClickRow: null,                     //双击行事件
        onSelectRow: null,                    //选择行事件
        onUnSelectRow: null,                   //取消选择行事件
        onBeforeCheckRow: null,                 //选择前事件，可以通过return false阻止操作(复选框)
        onCheckRow: null,                    //选择事件(复选框) 
        onBeforeCheckAllRow: null,              //选择前事件，可以通过return false阻止操作(复选框 全选/全不选)
        onCheckAllRow: null,                    //选择事件(复选框 全选/全不选)
        onBeforeShowData: null,                  //显示数据前事件，可以通过reutrn false阻止操作
        onAfterShowData: null,                 //显示完数据事件
        onError: null,                         //错误事件
        onSubmit: null,                         //提交前事件
        dateFormat: 'yyyy-MM-dd',              //默认时间显示格式
        InWindow: true,                        //是否以窗口的高度为准 height设置为百分比时可用
        statusName: '__status',                    //状态名
        method: 'post',                         //提交方式
        async: false,
        fixedCellHeight: true,                       //是否固定单元格的高度
        heightDiff: 0,                         //高度补差,当设置height:100%时，可能会有高度的误差，可以通过这个属性调整
        cssClass: null,                    //类名
        root: 'Rows',                       //数据源字段名
        record: 'Total',                     //数据源记录数字段名
        pageParmName: 'page',               //页索引参数名，(提交给服务器)
        pagesizeParmName: 'pagesize',        //页记录数参数名，(提交给服务器)
        sortnameParmName: 'sortname',        //页排序列名(提交给服务器)
        sortorderParmName: 'sortorder',      //页排序方向(提交给服务器)
        onReload: null,                    //刷新事件，可以通过return false来阻止操作
        onToFirst: null,                     //第一页，可以通过return false来阻止操作
        onToPrev: null,                      //上一页，可以通过return false来阻止操作
        onToNext: null,                      //下一页，可以通过return false来阻止操作
        onToLast: null,                      //最后一页，可以通过return false来阻止操作
        allowUnSelectRow: false,           //是否允许反选行 
        alternatingRow: true,           //间隔行效果
        mouseoverRowCssClass: 'l-grid-row-over',
        enabledSort: true,                      //是否允许排序
        rowAttrRender: null,                  //行自定义属性渲染器(包括style，也可以定义)
        groupColumnName: null,                 //分组 - 列名
        groupColumnDisplay: '分组',             //分组 - 列显示名字
        groupRender: null,                     //分组 - 渲染器
        totalRender: null,                       //统计行(全部数据)
        delayLoad: false,                        //初始化时是否不加载
        where: null,                           //数据过滤查询函数,(参数一 data item，参数二 data item index)
        selectRowButtonOnly: false,            //复选框模式时，是否只允许点击复选框才能选择行
        onAfterAddRow: null,                     //增加行后事件
        onBeforeEdit: null,                      //编辑前事件
        onBeforeSubmitEdit: null,               //验证编辑器结果是否通过
        onAfterEdit: null,                       //结束编辑后事件
        onLoading: null,                        //加载时函数
        onLoaded: null,                          //加载完函数
        onContextmenu: null,                   //右击事件
        whenRClickToSelect: false,                //右击行时是否选中
        contentType: null,                     //Ajax contentType参数
        checkboxColWidth: 27,                  //复选框列宽度
        detailColWidth: 29,                     //明细列宽度
        clickToEdit: true,                      //是否点击单元格的时候就编辑
        detailToEdit: false,                     //是否点击明细的时候进入编辑
        onEndEdit: null,
        minColumnWidth: 80,
        tree: null,                            //treeGrid模式
        isChecked: null,                       //复选框 初始化函数
        frozen: true,                          //是否固定列
        frozenDetail: false,                    //明细按钮是否在固定列中
        frozenCheckbox: true,                  //复选框按钮是否在固定列中
        detailHeight: 260,
        rownumbers: false,                         //是否显示行序号
        frozenRownumbers: true,                  //行序号是否在固定列中
        rownumbersColWidth: 26,
        colDraggable: false,                       //是否允许表头拖拽
        rowDraggable: false,                         //是否允许行拖拽
        rowDraggingRender: null,
        autoCheckChildren: true,                  //是否自动选中子节点
        onRowDragDrop: null                    //行拖拽事件
    };
    $.ligerDefaults.GridString = {
        errorMessage: '发生错误',
        pageStatMessage: '显示从{from}到{to}，总 {total} 条 。每页显示：{pagesize}',
        pageTextMessage: 'Page',
        loadingMessage: '加载中...',
        findTextMessage: '查找',
        noRecordMessage: '没有符合条件的记录存在',
        isContinueByDataChanged: '数据已经改变,如果继续将丢失数据,是否继续?',
        cancelMessage: '取消',
        saveMessage: '保存',
        applyMessage: '应用',
        draggingMessage: '{count}行'
    };
    //接口方法扩展
    $.ligerMethos.Grid = $.ligerMethos.Grid || {};

    //排序器扩展
    $.ligerDefaults.Grid.sorters = $.ligerDefaults.Grid.sorters || {};

    //格式化器扩展
    $.ligerDefaults.Grid.formatters = $.ligerDefaults.Grid.formatters || {};

    //编辑器扩展
    $.ligerDefaults.Grid.editors = $.ligerDefaults.Grid.editors || {};


    $.ligerDefaults.Grid.sorters['date'] = function (val1, val2)
    {
        return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['int'] = function (val1, val2)
    {
        return parseInt(val1) < parseInt(val2) ? -1 : parseInt(val1) > parseInt(val2) ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['float'] = function (val1, val2)
    {
        return parseFloat(val1) < parseFloat(val2) ? -1 : parseFloat(val1) > parseFloat(val2) ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['string'] = function (val1, val2)
    {
        return val1.localeCompare(val2);
    };


    $.ligerDefaults.Grid.formatters['date'] = function (value, column)
    {
        function getFormatDate(date, dateformat)
        {
            var g = this, p = this.options;
            if (isNaN(date)) return null;
            var format = dateformat;
            var o = {
                "M+": date.getMonth() + 1,
                "d+": date.getDate(),
                "h+": date.getHours(),
                "m+": date.getMinutes(),
                "s+": date.getSeconds(),
                "q+": Math.floor((date.getMonth() + 3) / 3),
                "S": date.getMilliseconds()
            }
            if (/(y+)/.test(format))
            {
                format = format.replace(RegExp.$1, (date.getFullYear() + "")
            .substr(4 - RegExp.$1.length));
            }
            for (var k in o)
            {
                if (new RegExp("(" + k + ")").test(format))
                {
                    format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
                : ("00" + o[k]).substr(("" + o[k]).length));
                }
            }
            return format;
        }
        if (!value) return "";
        if (value instanceof Date)
        {
            var format = column.format || this.options.dateFormat || "yyyy-MM-dd";
            return getFormatDate(value, format);
        }
        else
        {
            return value.toString();
        }
    }


    $.ligerDefaults.Grid.editors['date'] =
    {
        create: function (container, editParm)
        {
            var column = editParm.column;
            var input = $("<input type='text'/>");
            container.append(input);
            var options = {};
            var ext = column.editor.p || column.editor.ext;
            if (ext)
            {
                var tmp = typeof (ext) == 'function' ?
                 ext(editParm.record, editParm.rowindex, editParm.value, column) : ext;
                $.extend(options, tmp);
            }
            input.ligerDateEditor(options);
            return input;
        },
        getValue: function (input, editParm)
        {
            return input.liger('option', 'value');
        },
        setValue: function (input, value, editParm)
        {
            input.liger('option', 'value', value);
        },
        resize: function (input, width, height, editParm)
        {
            input.liger('option', 'width', width);
            input.liger('option', 'height', height);
        },
        destroy: function (input, editParm)
        {
            input.liger('destroy');
        }
    };

    $.ligerDefaults.Grid.editors['select'] =
     $.ligerDefaults.Grid.editors['combobox'] =
     {
         create: function (container, editParm)
         {
             var column = editParm.column;
             var input = $("<input type='text'/>");
             container.append(input);
             var options = {
                 data: column.editor.data,
                 slide: false,
                 valueField: column.editor.valueField || column.editor.valueColumnName,
                 textField: column.editor.textField || column.editor.displayColumnName
             };
             var ext = column.editor.p || column.editor.ext;
             if (ext)
             {
                 var tmp = typeof (ext) == 'function' ?
                 ext(editParm.record, editParm.rowindex, editParm.value, column) : ext;
                 $.extend(options, tmp);
             }
             input.ligerComboBox(options);
             return input;
         },
         getValue: function (input, editParm)
         {
             return input.liger('option', 'value');
         },
         setValue: function (input, value, editParm)
         {
             input.liger('option', 'value', value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };

    $.ligerDefaults.Grid.editors['int'] =
     $.ligerDefaults.Grid.editors['float'] =
     $.ligerDefaults.Grid.editors['spinner'] =
     {
         create: function (container, editParm)
         {
             var column = editParm.column;
             var input = $("<input type='text'/>");
             container.append(input);
             input.css({ border: '#6E90BE' })
             var options = {
                 type: column.editor.type == 'float' ? 'float' : 'int'
             };
             if (column.editor.minValue != undefined) options.minValue = column.editor.minValue;
             if (column.editor.maxValue != undefined) options.maxValue = column.editor.maxValue;
             input.ligerSpinner(options);
             return input;
         },
         getValue: function (input, editParm)
         {
             var column = editParm.column;
             var isInt = column.editor.type == "int";
             if (isInt)
                 return parseInt(input.val(), 10);
             else
                 return parseFloat(input.val());
         },
         setValue: function (input, value, editParm)
         {
             input.val(value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };


    $.ligerDefaults.Grid.editors['string'] =
     $.ligerDefaults.Grid.editors['text'] = {
         create: function (container, editParm)
         {
             var input = $("<input type='text' class='l-text-editing'/>");
             container.append(input);
             input.ligerTextBox();
             return input;
         },
         getValue: function (input, editParm)
         {
             return input.val();
         },
         setValue: function (input, value, editParm)
         {
             input.val(value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };

    $.ligerDefaults.Grid.editors['chk'] = $.ligerDefaults.Grid.editors['checkbox'] = {
        create: function (container, editParm)
        {
            var input = $("<input type='checkbox' />");
            container.append(input);
            input.ligerCheckBox();
            return input;
        },
        getValue: function (input, editParm)
        {
            return input[0].checked ? 1 : 0;
        },
        setValue: function (input, value, editParm)
        {
            input.val(value ? true : false);
        },
        resize: function (input, width, height, editParm)
        {
            input.liger('option', 'width', width);
            input.liger('option', 'height', height);
        },
        destroy: function (input, editParm)
        {
            input.liger('destroy');
        }
    };

    $.ligerui.controls.Grid = function (element, options)
    {
        $.ligerui.controls.Grid.base.constructor.call(this, element, options);
    };

    $.ligerui.controls.Grid.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return '$.ligerui.controls.Grid';
        },
        __idPrev: function ()
        {
            return 'grid';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Grid;
        },
        _init: function ()
        {
            $.ligerui.controls.Grid.base._init.call(this);
            var g = this, p = this.options;
            p.dataType = p.url ? "server" : "local";
            if (p.dataType == "local")
            {
                p.data = p.data || [];
                p.dataAction = "local";
            }
            if (p.isScroll == false)
            {
                p.height = 'auto';
            }
            if (!p.frozen)
            {
                p.frozenCheckbox = false;
                p.frozenDetail = false;
                p.frozenRownumbers = false;
            }
            if (p.detailToEdit)
            {
                p.enabledEdit = true;
                p.clickToEdit = false;
                p.detail = {
                    height: 'auto',
                    onShowDetail: function (record, container, callback)
                    {
                        $(container).addClass("l-grid-detailpanel-edit");
                        g.beginEdit(record, function (rowdata, column)
                        {
                            var editContainer = $("<div class='l-editbox'></div>");
                            editContainer.width(120).height(23);
                            editContainer.appendTo(container);
                            return editContainer;
                        });
                        function removeRow()
                        {
                            $(container).parent().parent().remove();
                            g.collapseDetail(record);
                        }
                        $("<div class='l-clear'></div>").appendTo(container);
                        $("<div class='l-button'>" + p.saveMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.endEdit(record);
                            removeRow();
                        });
                        $("<div class='l-button'>" + p.applyMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.submitEdit(record);
                        });
                        $("<div class='l-button'>" + p.cancelMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.cancelEdit(record);
                            removeRow();
                        });
                    }
                };
            }
            if (p.tree)//启用分页模式
            {
                p.tree.childrenName = p.tree.childrenName || "children";
                p.tree.isParent = p.tree.isParent || function (rowData)
                {
                    var exist = p.tree.childrenName in rowData;
                    return exist;
                };
                p.tree.isExtend = p.tree.isExtend || function (rowData)
                {
                    if ('isextend' in rowData && rowData['isextend'] == false)
                        return false;
                    return true;
                };
            }
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.grid = $(g.element);
            g.grid.addClass("l-panel");
            var gridhtmlarr = [];
            gridhtmlarr.push("        <div class='l-panel-header'><span class='l-panel-header-text'></span></div>");
            gridhtmlarr.push("                    <div class='l-grid-loading'></div>");
            gridhtmlarr.push("        <div class='l-panel-bwarp'>");
            gridhtmlarr.push("            <div class='l-panel-body'>");
            gridhtmlarr.push("                <div class='l-grid'>");
            gridhtmlarr.push("                    <div class='l-grid-dragging-line'></div>");
            gridhtmlarr.push("                    <div class='l-grid-popup'><table cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");

            gridhtmlarr.push("                  <div class='l-grid1'>");
            gridhtmlarr.push("                      <div class='l-grid-header l-grid-header1'>");
            gridhtmlarr.push("                          <div class='l-grid-header-inner'><table class='l-grid-header-table' cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                      <div class='l-grid-body l-grid-body1'>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                  </div>");

            gridhtmlarr.push("                  <div class='l-grid2'>");
            gridhtmlarr.push("                      <div class='l-grid-header l-grid-header2'>");
            gridhtmlarr.push("                          <div class='l-grid-header-inner'><table class='l-grid-header-table' cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                      <div class='l-grid-body l-grid-body2 l-scroll'>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                  </div>");


            gridhtmlarr.push("                 </div>");
            gridhtmlarr.push("              </div>");
            gridhtmlarr.push("         </div>");
            gridhtmlarr.push("         <div class='l-panel-bar'>");
            gridhtmlarr.push("            <div class='l-panel-bbar-inner'>");
            gridhtmlarr.push("                <div class='l-bar-group  l-bar-message'><span class='l-bar-text'></span></div>");
            gridhtmlarr.push("            <div class='l-bar-group l-bar-selectpagesize'></div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnfirst'><span></span></div>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnprev'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'><span class='pcontrol'> <input type='text' size='4' value='1' style='width:20px' maxlength='3' /> / <span></span></span></div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                     <div class='l-bar-button l-bar-btnnext'><span></span></div>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnlast'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                     <div class='l-bar-button l-bar-btnload'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");

            gridhtmlarr.push("                <div class='l-clear'></div>");
            gridhtmlarr.push("            </div>");
            gridhtmlarr.push("         </div>");
            g.grid.html(gridhtmlarr.join(''));
            //头部
            g.header = $(".l-panel-header:first", g.grid);
            //主体
            g.body = $(".l-panel-body:first", g.grid);
            //底部工具条         
            g.toolbar = $(".l-panel-bar:first", g.grid);
            //显示/隐藏列      
            g.popup = $(".l-grid-popup:first", g.grid);
            //加载中
            g.gridloading = $(".l-grid-loading:first", g.grid);
            //调整列宽层 
            g.draggingline = $(".l-grid-dragging-line", g.grid);

            g.gridview = $(".l-grid:first", g.grid);
            g.gridview.attr("id", g.id + "grid");
            g.gridview1 = $(".l-grid1:first", g.gridview);
            g.gridview2 = $(".l-grid2:first", g.gridview);
            //表头     
            g.gridheader = $(".l-grid-header:first", g.gridview2);
            //表主体     
            g.gridbody = $(".l-grid-body:first", g.gridview2);

            //frozen
            g.f = {};
            //表头     
            g.f.gridheader = $(".l-grid-header:first", g.gridview1);
            //表主体     
            g.f.gridbody = $(".l-grid-body:first", g.gridview1);

            g.currentData = null;
            g.changedCells = {};
            g.editors = {};                 //多编辑器同时存在
            g.editor = { editing: false };  //单编辑器,配置clickToEdit
            if (p.height == "auto")
            {
                g.bind("SysGridHeightChanged", function ()
                {
                    if (g.enabledFrozen())
                        g.gridview.height(Math.max(g.gridview1.height(), g.gridview2.height()));
                });
            }

            var pc = $.extend({}, p);

            this._bulid();
            this._setColumns(p.columns);

            delete pc['columns'];
            delete pc['data'];
            delete pc['url'];
            g.set(pc);
            if (!p.delayLoad)
            {
                if(p.url)
                   g.set({ url: p.url});
                else if(p.data)
                   g.set({data:p.data});
            }   
        },
        _setFrozen: function (frozen)
        {
            if (frozen)
                this.grid.addClass("l-frozen");
            else
                this.grid.removeClass("l-frozen");
        },
        _setCssClass: function (value)
        {
            this.grid.addClass(value);
        },
        _setLoadingMessage: function (value)
        {
            this.gridloading.html(value);
        },
        _setHeight: function (h)
        {
            var g = this, p = this.options;
            g.unbind("SysGridHeightChanged");
            if (h == "auto")
            {
                g.bind("SysGridHeightChanged", function ()
                {
                    if (g.enabledFrozen())
                        g.gridview.height(Math.max(g.gridview1.height(), g.gridview2.height()));
                });
                return;
            }
            if (typeof h == "string" && h.indexOf('%') > 0)
            {
                if (p.inWindow)
                    h = $(window).height() * parseFloat(h) * 0.01;
                else
                    h = g.grid.parent().height() * parseFloat(h) * 0.01;
            }
            if (p.title) h -= 24;
            if (p.usePager) h -= 32;
            if (p.totalRender) h -= 25;
            var gridHeaderHeight = 23 * (g._columnMaxLevel - 1) + 22;
            h -= gridHeaderHeight;
            if (h > 0)
            {
                g.gridbody.height(h);
                if (h > 18) g.f.gridbody.height(h - 18);
                g.gridview.height(h + gridHeaderHeight);
            }
        },
        _updateFrozenWidth: function ()
        {
            var g = this, p = this.options;
            if (g.enabledFrozen())
            {
                g.gridview1.width(g.f.gridtablewidth);
                var view2width = g.gridview.width() - g.f.gridtablewidth;
                g.gridview2.css({ left: g.f.gridtablewidth });
                if (view2width > 0) g.gridview2.css({ width: view2width });
            }
        },
        _setWidth: function (value)
        {
            var g = this, p = this.options;
            if (g.enabledFrozen()) g._onResize();
        },
        _setUrl: function (value)
        {
            if (value)
            {
                this.options.dataType = "server";
                this.loadData(true);
            }
            else
            {
                this.options.dataType = "local";
            }
        },
        _setData: function (value) { 
            this.loadData(this.options.data);
        },
        //刷新数据
        loadData: function (loadDataParm)
        {
            var g = this, p = this.options;
            g.loading = true;
            var clause = null;
            var loadServer = true;
            if (typeof (loadDataParm) == "function")
            {
                clause = loadDataParm;
                loadServer = false;
            }
            else if (typeof (loadDataParm) == "boolean")
            {
                loadServer = loadDataParm;
            }
            else if (typeof (loadDataParm) == "object" && loadDataParm)
            {
                loadServer = false;
                p.dataType = "local";
                p.data = loadDataParm;
            }
            //参数初始化
            if (!p.newPage) p.newPage = 1;
            if (p.dataAction == "server")
            {
                if (!p.sortOrder) p.sortOrder = "asc";
            }
            var param = [];
            if (p.parms)
            {
                if (p.parms.length)
                {
                    $(p.parms).each(function ()
                    {
                        param.push({ name: this.name, value: this.value });
                    });
                }
                else if (typeof p.parms == "string")
                {
                    for (var name in p.parms)
                    {
                        param.push({ name: name, value: p.parms[name] });
                    }
                }
            }
            if (p.dataAction == "server")
            {
                if (p.usePager)
                {
                    param.push({ name: p.pageParmName, value: p.newPage });
                    param.push({ name: p.pagesizeParmName, value: p.pageSize });
                }
                if (p.sortName)
                {
                    param.push({ name: p.sortnameParmName, value: p.sortName });
                    param.push({ name: p.sortorderParmName, value: p.sortOrder });
                }
            };
            $(".l-bar-btnload span", g.toolbar).addClass("l-disabled");
            if (p.dataType == "local") {
                
                debugger
                g.filteredData = g.data = p.data;
                if (clause)
                    g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                if (p.usePager)
                    g.currentData = g._getCurrentPageData(g.filteredData);
                else
                {
                    g.currentData = g.filteredData;
                }
                g._showData();
            }
            else if (p.dataAction == "local" && !loadServer)
            {
                if (g.data && g.data[p.root])
                {
                    g.filteredData = g.data;
                    if (clause)
                        g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                    g.currentData = g._getCurrentPageData(g.filteredData);
                    g._showData();
                }
            }
            else
            {
                if (g.hasBind('loading'))
                {
                    g.trigger('loading');
                }
                else
                {
                    g.toggleLoading.ligerDefer(g, 10, [true]);
                }
                g.loadServerData.ligerDefer(g, 10, [param, clause]);
            }
            g.loading = false;
        },
        loadServerData: function (param, clause)
        {
            var g = this, p = this.options;
            var ajaxOptions = {
                type: p.method,
                url: p.url,
                data: param,
                async: p.async,
                dataType: 'json',
                beforeSend: function ()
                {
                },
                success: function (data)
                {
                    g.trigger('success', [data, g]);
                    if (!data || !data[p.root] || !data[p.root].length)
                    {
                        g.currentData = g.data = {};
                        g.currentData[p.root] = g.data[p.root] = [];
                        g.currentData[p.record] = g.data[p.record] = 0;
                        g._showData();
                        return;
                    }
                    g.data = data;
                    if (p.dataAction == "server")
                    {
                        g.currentData = g.data;
                    }
                    else
                    {
                        g.filteredData = g.data;
                        if (clause) g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                        if (p.usePager)
                            g.currentData = g._getCurrentPageData(g.filteredData);
                        else
                            g.currentData = g.filteredData;
                    }
                    g._showData.ligerDefer(g, 10, [g.currentData]);
                },
                complete: function ()
                {
                    if (g.hasBind('loaded'))
                    {
                        g.trigger('loaded', [g]);
                    }
                    else
                    {
                        g.toggleLoading.ligerDefer(g, 10, [false]);
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown)
                {
                    g.currentData = g.data = {};
                    g.currentData[p.root] = g.data[p.root] = [];
                    g.currentData[p.record] = g.data[p.record] = 0;
                    g.toggleLoading.ligerDefer(g, 10, [false]);
                    $(".l-bar-btnload span", g.toolbar).removeClass("l-disabled");
                    g.trigger('error', [XMLHttpRequest, textStatus, errorThrown]);
                }
            };
            if (p.contentType) ajaxOptions.contentType = p.contentType;
            $.ajax(ajaxOptions);
        },
        toggleLoading: function (show)
        {
            this.gridloading[show ? 'show' : 'hide']();
        },
        _createEditor: function (editor, container, editParm, width, height)
        {
            var editorInput = editor.create(container, editParm);
            if (editor.setValue) editor.setValue(editorInput, editParm.value, editParm);
            if (editor.resize) editor.resize(editorInput, width, height, editParm);
            return editorInput;
        },
        /*
        @description 使一行进入编辑状态
        @param  {rowParm} rowindex或者rowdata
        @param {containerBulider} 编辑器填充层构造器
        */
        beginEdit: function (rowParm, containerBulider)
        {
            var g = this, p = this.options;
            if (!p.enabledEdit || p.clickToEdit) return;
            var rowdata = g.getRow(rowParm);
            if (rowdata._editing) return;
            g.editors[rowdata['__id']] = {};
            rowdata._editing = true;
            g.reRender({ rowdata: rowdata });
            containerBulider = containerBulider || function (rowdata, column)
            {
                var cellobj = g.getCellObj(rowdata, column);
                var container = $(cellobj).html("");
                g.setCellEditing(rowdata, column, true);
                return container;
            };
            if (g.trigger('beginEdit', { record: rowdata, rowindex: rowdata['__index'] }) == false) return;
            for (var i = 0, l = g.columns.length; i < l; i++)
            {
                var column = g.columns[i];
                if (!column.name || !column.editor || !column.editor.type || !$.ligerDefaults.Grid.editors[column.editor.type]) continue;
                var editor = $.ligerDefaults.Grid.editors[column.editor.type];
                var editParm = { record: rowdata, value: rowdata[column.name], column: column, rowindex: rowdata['__index'] };
                var container = containerBulider(rowdata, column);
                var width = container.width(), height = container.height();
                var editorInput = g._createEditor(editor, container, editParm, width, height);
                g.editors[rowdata['__id']][column['__id']] = { editor: editor, input: editorInput, editParm: editParm, container: container };
            }
            g.trigger('afterBeginEdit', { record: rowdata, rowindex: rowdata['__index'] });

        },
        cancelEdit: function (rowParm)
        {
            var g = this;
            if (rowParm == undefined)
            {
                for (var rowid in g.editors)
                {
                    g.cancelEdit(rowid);
                }
            }
            else
            {
                var rowdata = g.getRow(rowParm);
                if (!g.editors[rowdata['__id']]) return;
                if (g.trigger('cancelEdit', { record: rowdata, rowindex: rowdata['__index'] }) == false) return;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                }
                delete g.editors[rowdata['__id']];
                delete rowdata['_editing'];
                g.reRender({ rowdata: rowdata });
            }
        },
        addEditRow: function (rowdata)
        {
            var g = this;
            rowdata = g.add(rowdata);
            g.beginEdit(rowdata);
        },
        submitEdit: function (rowParm)
        {
            var g = this, p = this.options;
            if (rowParm == undefined)
            {
                for (var rowid in g.editors)
                {
                    g.submitEdit(rowid);
                }
            }
            else
            {
                var rowdata = g.getRow(rowParm);
                var newdata = {};
                if (!g.editors[rowdata['__id']]) return;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    var column = o.editParm.column;
                    if (column.name)
                        newdata[column.name] = o.editor.getValue(o.input, o.editParm);
                }
                if (g.trigger('beforeSubmitEdit', { record: rowdata, rowindex: rowdata['__index'], newdata: newdata }) == false)
                    return false;
                g.updateRow(rowdata, newdata);
                g.trigger('afterSubmitEdit', { record: rowdata, rowindex: rowdata['__index'], newdata: newdata });
            }
        },
        endEdit: function (rowParm)
        {
            var g = this, p = this.options;
            if (g.editor.editing)
            {
                var o = g.editor;
                if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                g.editor.container.remove();
                g.trigger('sysEndEdit', [g.editor.editParm]);
                g.trigger('endEdit', [g.editor.editParm]);
                g.editor.container;
                g.reRender({ rowdata: g.editor.editParm.record, column: g.editor.editParm.column });
                g.trigger('afterEdit', [g.editor.editParm]);
                g.editor = { editing: false };
            }
            else if (rowParm != undefined)
            {
                var rowdata = g.getRow(rowParm);
                if (!g.editors[rowdata['__id']]) return;
                if (g.submitEdit(rowParm) == false) return false;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                }
                delete g.editors[rowdata['__id']];
                delete rowdata['_editing'];
                g.trigger('afterEdit', { record: rowdata, rowindex: rowdata['__index'] });
            }
            else
            {
                for (var rowid in g.editors)
                {
                    g.endEdit(rowid);
                }
            }
        },
        setWidth: function (w)
        {
            return this._setWidth(w);
        },
        setHeight: function (h)
        {
            return this._setHeight(h);
        },
        //是否启用复选框列
        enabledCheckbox: function ()
        {
            return this.options.checkbox ? true : false;
        },
        //是否固定列
        enabledFrozen: function ()
        {
            var g = this, p = this.options;
            if (!p.frozen) return false;
            var cols = g.columns || [];
            if (g.enabledDetail() && p.frozenDetail || g.enabledCheckbox() && p.frozenCheckbox
            || p.frozenRownumbers && p.rownumbers) return true;
            for (var i = 0, l = cols.length; i < l; i++)
            {
                if (cols[i].frozen)
                {
                    return true;
                }
            }
            this._setFrozen(false);
            return false;
        },
        //是否启用明细编辑
        enabledDetailEdit: function ()
        {
            if (!this.enabledDetail()) return false;
            return this.options.detailToEdit ? true : false;
        },
        //是否启用明细列
        enabledDetail: function ()
        {
            if (this.options.detail && this.options.detail.onShowDetail) return true;
            return false;
        },
        //是否启用分组
        enabledGroup: function ()
        {
            return this.options.groupColumnName ? true : false;
        },
        deleteSelectedRow: function ()
        {
            if (!this.selected) return;
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    this._deleteData.ligerDefer(this, 10, [o]);
            }
            this.reRender.ligerDefer(this, 20);
        },
        removeRange: function (rowArr)
        {
            var g = this, p = this.options;
            $.each(rowArr, function ()
            {
                g._removeData(this);
            });
            g.reRender();
        },
        remove: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            g._removeData(rowParm);
            g.reRender();
        },
        deleteRange: function (rowArr)
        {
            var g = this, p = this.options;
            $.each(rowArr, function ()
            {
                g._deleteData(this);
            });
            g.reRender();
        },
        deleteRow: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            g._deleteData(rowdata);
            g.reRender();
            g.isDataChanged = true;
        },
        _deleteData: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            rowdata[p.statusName] = 'delete';
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        children[i][p.statusName] = 'delete';
                    }
                }
            }
            g.deletedRows = g.deletedRows || [];
            g.deletedRows.push(rowdata);
            g._removeSelected(rowdata);
        },
        /*
        @param  {arg} column index、column name、column、单元格
        @param  {value} 值
        @param  {rowParm} rowindex或者rowdata
        */
        updateCell: function (arg, value, rowParm)
        {
            var g = this, p = this.options;
            var column, cellObj, rowdata;
            if (typeof (arg) == "string") //column name
            {
                for (var i = 0, l = g.columns.length; i < l; i++)
                {
                    if (g.columns[i].name == arg)
                    {
                        g.updateCell(i, value, rowParm);
                    }
                }
                return;
            }
            if (typeof (arg) == "number")
            {
                column = g.columns[arg];
                rowdata = g.getRow(rowParm);
                cellObj = g.getCellObj(rowdata, column);
            }
            else if (typeof (arg) == "object" && arg['__id'])
            {
                column = arg;
                rowdata = g.getRow(rowParm);
                cellObj = g.getCellObj(rowdata, column);
            }
            else
            {
                cellObj = arg;
                var ids = cellObj.id.split('|');
                var columnid = ids[ids.length - 1];
                column = g._columns[columnid];
                var row = $(cellObj).parent();
                rowdata = rowdata || g.getRow(row[0]);
            }
            if (value != null && column.name)
            {
                rowdata[column.name] = value;
                if (rowdata[p.statusName] != 'add')
                    rowdata[p.statusName] = 'update';
                g.isDataChanged = true;
            }
            g.reRender({ rowdata: rowdata, column: column });
        },
        addRows: function (rowdataArr, neardata, isBefore, parentRowData)
        {
            var g = this, p = this.options;
            $(rowdataArr).each(function ()
            {
                g.addRow(this, neardata, isBefore, parentRowData);
            });
        },
        _createRowid: function ()
        {
            return "r" + (1000 + this.recordNumber);
        },
        _isRowId: function (str)
        {
            return (str in this.records);
        },
        _addNewRecord: function (o, previd, pid)
        {
            var g = this, p = this.options;
            g.recordNumber++;
            o['__id'] = g._createRowid();
            o['__previd'] = previd;
            if (previd && previd != -1)
            {
                var prev = g.records[previd];
                if (prev['__nextid'] && prev['__nextid'] != -1)
                {
                    var prevOldNext = g.records[prev['__nextid']];
                    if (prevOldNext)
                        prevOldNext['__previd'] = o['__id'];
                }
                prev['__nextid'] = o['__id'];
                o['__index'] = prev['__index'] + 1;
            }
            else
            {
                o['__index'] = 0;
            }
            if (p.tree)
            {
                if (pid && pid != -1)
                {
                    var parent = g.records[pid];
                    o['__pid'] = pid;
                    o['__level'] = parent['__level'] + 1;
                }
                else
                {
                    o['__pid'] = -1;
                    o['__level'] = 1;
                }
                o['__hasChildren'] = o[p.tree.childrenName] ? true : false;
            }
            if (o[p.statusName] != "add")
                o[p.statusName] = "nochanged";
            g.rows[o['__index']] = o;
            g.records[o['__id']] = o;
            return o;
        },
        //将原始的数据转换成适合 grid的行数据 
        _getRows: function (data)
        {
            var g = this, p = this.options;
            var targetData = [];
            function load(data)
            {
                if (!data || !data.length) return;
                for (var i = 0, l = data.length; i < l; i++)
                {
                    var o = data[i];
                    targetData.push(o);
                    if (o[p.tree.childrenName])
                    {
                        load(o[p.tree.childrenName]);
                    }
                }
            }
            load(data);
            return targetData;
        },
        _updateGridData: function ()
        {
            var g = this, p = this.options;
            g.recordNumber = 0;
            g.rows = [];
            g.records = {};
            var previd = -1;
            function load(data, pid)
            {
                if (!data || !data.length) return;
                for (var i = 0, l = data.length; i < l; i++)
                {
                    var o = data[i];
                    g.formatRecord(o);
                    if (o[p.statusName] == "delete") continue;
                    g._addNewRecord(o, previd, pid);
                    previd = o['__id'];
                    if (o['__hasChildren'])
                    {
                        load(o[p.tree.childrenName], o['__id']);
                    }
                }
            }
            load(g.currentData[p.root], -1);
            return g.rows;
        },
        _moveData: function (from, to, isAfter)
        {
            var g = this, p = this.options;
            var fromRow = g.getRow(from);
            var toRow = g.getRow(to);
            var fromIndex, toIndex;
            var listdata = g._getParentChildren(fromRow);
            fromIndex = $.inArray(fromRow, listdata);
            listdata.splice(fromIndex, 1);
            listdata = g._getParentChildren(toRow);
            toIndex = $.inArray(toRow, listdata);
            listdata.splice(toIndex + (isAfter ? 1 : 0), 0, fromRow);
        },
        move: function (from, to, isAfter)
        {
            this._moveData(from, to, isAfter);
            this.reRender();
        },
        moveRange: function (rows, to, isAfter)
        {
            for (var i in rows)
            {
                this._moveData(rows[i], to, isAfter);
            }
            this.reRender();
        },
        up: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index == -1 || index == 0) return;
            g.move(rowdata, listdata[index - 1], false);
        },
        down: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index == -1 || index == listdata.length - 1) return;
            g.move(rowdata, listdata[index + 1], true);
        },
        addRow: function (rowdata, neardata, isBefore, parentRowData)
        {
            var g = this, p = this.options;
            rowdata = rowdata || {};
            g._addData(rowdata, parentRowData, neardata, isBefore);
            g.reRender();
            //标识状态
            rowdata[p.statusName] = 'add';
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        children[i][p.statusName] = 'add';
                    }
                }
            }
            g.isDataChanged = true;
            p.total = p.total ? (p.total + 1) : 1;
            p.pageCount = Math.ceil(p.total / p.pageSize);
            g._buildPager();
            g.trigger('SysGridHeightChanged');
            g.trigger('afterAddRow', [rowdata]);
            return rowdata;
        },
        updateRow: function (rowDom, newRowData)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowDom);
            //标识状态
            g.isDataChanged = true;
            $.extend(rowdata, newRowData || {});
            if (rowdata[p.statusName] != 'add')
                rowdata[p.statusName] = 'update';
            g.reRender.ligerDefer(g, 10, [{ rowdata: rowdata}]);
            return rowdata;
        },
        setCellEditing: function (rowdata, column, editing)
        {
            var g = this, p = this.options;
            var cell = g.getCellObj(rowdata, column);
            var methodName = editing ? 'addClass' : 'removeClass';
            $(cell)[methodName]("l-grid-row-cell-editing");
            if (rowdata['__id'] != 0)
            {
                var prevrowobj = $(g.getRowObj(rowdata['__id'])).prev();
                if (!prevrowobj.length) return;
                var prevrow = g.getRow(prevrowobj[0]);
                var cellprev = g.getCellObj(prevrow, column);
                if (!cellprev) return;
                $(cellprev)[methodName]("l-grid-row-cell-editing-topcell");
            }
            if (column['__previd'] != -1 && column['__previd'] != null)
            {
                var cellprev = $(g.getCellObj(rowdata, column)).prev();
                $(cellprev)[methodName]("l-grid-row-cell-editing-leftcell");
            }
        },
        reRender: function (e)
        {
            var g = this, p = this.options;
            e = e || {};
            var rowdata = e.rowdata, column = e.column;
            if (column && (column.isdetail || column.ischeckbox)) return;
            if (rowdata && rowdata[p.statusName] == "delete") return;
            if (rowdata && column)
            {
                var cell = g.getCellObj(rowdata, column);
                $(cell).html(g._getCellHtml(rowdata, column));
                if (!column.issystem)
                    g.setCellEditing(rowdata, column, false);
            }
            else if (rowdata)
            {
                $(g.columns).each(function () { g.reRender({ rowdata: rowdata, column: this }); });
            }
            else if (column)
            {
                for (var rowid in g.records) { g.reRender({ rowdata: g.records[rowid], column: column }); }
                for (var i = 0; i < g.totalNumber; i++)
                {
                    var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                    $("div:first", tobj).html(g._getTotalCellContent(column, g.groups && g.groups[i] ? g.groups[i] : g.currentData[p.root]));
                }
            }
            else
            {
                g._showData();
            }
        },
        getData: function (status, removeStatus)
        {
            var g = this, p = this.options;
            var data = [];
            for (var rowid in g.records)
            {
                var o = $.extend({}, g.records[rowid]);
                if (o[p.statusName] == status || status == undefined)
                {
                    data.push(g.formatRecord(o, removeStatus));
                }
            }
            return data;
        },
        //格式化数据
        formatRecord: function (o, removeStatus)
        {
            delete o['__id'];
            delete o['__previd'];
            delete o['__nextid'];
            delete o['__index'];
            if (this.options.tree)
            {
                delete o['__pid'];
                delete o['__level'];
                delete o['__hasChildren'];
            }
            if (removeStatus) delete o[this.options.statusName];
            return o;
        },
        getUpdated: function ()
        {
            return this.getData('update', true);
        },
        getDeleted: function ()
        {
            return this.deletedRows;
        },
        getAdded: function ()
        {
            return this.getData('add', true);
        },
        getColumn: function (columnParm)
        {
            var g = this, p = this.options;
            if (typeof columnParm == "string") // column id
            {
                if (g._isColumnId(columnParm))
                    return g._columns[columnParm];
                else
                    return g.columns[parseInt(columnParm)];
            }
            else if (typeof (columnParm) == "number") //column index
            {
                return g.columns[columnParm];
            }
            else if (typeof columnParm == "object" && columnParm.nodeType == 1) //column header cell
            {
                var ids = columnParm.id.split('|');
                var columnid = ids[ids.length - 1];
                return g._columns[columnid];
            }
            return columnParm;
        },
        getColumnType: function (columnname)
        {
            var g = this, p = this.options;
            for (i = 0; i < g.columns.length; i++)
            {
                if (g.columns[i].name == columnname)
                {
                    if (g.columns[i].type) return g.columns[i].type;
                    return "string";
                }
            }
            return null;
        },
        //是否包含汇总
        isTotalSummary: function ()
        {
            var g = this, p = this.options;
            for (var i = 0; i < g.columns.length; i++)
            {
                if (g.columns[i].totalSummary) return true;
            }
            return false;
        },
        //根据层次获取列集合
        //如果columnLevel为空，获取叶节点集合
        getColumns: function (columnLevel)
        {
            var g = this, p = this.options;
            var columns = [];
            for (var id in g._columns)
            {
                var col = g._columns[id];
                if (columnLevel != undefined)
                {
                    if (col['__level'] == columnLevel) columns.push(col);
                }
                else
                {
                    if (col['__leaf']) columns.push(col);
                }
            }
            return columns;
        },
        //改变排序
        changeSort: function (columnName, sortOrder)
        {
            var g = this, p = this.options;
            if (g.loading) return true;
            if (p.dataAction == "local")
            {
                var columnType = g.getColumnType(columnName);
                if (!g.sortedData)
                    g.sortedData = g.filteredData;
                if (p.sortName == columnName)
                {
                    g.sortedData[p.root].reverse();
                } else
                {
                    g.sortedData[p.root].sort(function (data1, data2)
                    {
                        return g._compareData(data1, data2, columnName, columnType);
                    });
                }
                if (p.usePager)
                    g.currentData = g._getCurrentPageData(g.sortedData);
                else
                    g.currentData = g.sortedData;
                g._showData();
            }
            p.sortName = columnName;
            p.sortOrder = sortOrder;
            if (p.dataAction == "server")
            {
                g.loadData(p.where);
            }
        },
        //改变分页
        changePage: function (ctype)
        {
            var g = this, p = this.options;
            if (g.loading) return true;
            if (p.dataAction != "local" && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                return false;
            p.pageCount = parseInt($(".pcontrol span", g.toolbar).html());
            switch (ctype)
            {
                case 'first': if (p.page == 1) return; p.newPage = 1; break;
                case 'prev': if (p.page == 1) return; if (p.page > 1) p.newPage = parseInt(p.page) - 1; break;
                case 'next': if (p.page >= p.pageCount) return; p.newPage = parseInt(p.page) + 1; break;
                case 'last': if (p.page >= p.pageCount) return; p.newPage = p.pageCount; break;
                case 'input':
                    var nv = parseInt($('.pcontrol input', g.toolbar).val());
                    if (isNaN(nv)) nv = 1;
                    if (nv < 1) nv = 1;
                    else if (nv > p.pageCount) nv = p.pageCount;
                    $('.pcontrol input', g.toolbar).val(nv);
                    p.newPage = nv;
                    break;
            }
            if (p.newPage == p.page) return false;
            if (p.newPage == 1)
            {
                $(".l-bar-btnfirst span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).addClass("l-disabled");
            }
            else
            {
                $(".l-bar-btnfirst span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).removeClass("l-disabled");
            }
            if (p.newPage == p.pageCount)
            {
                $(".l-bar-btnlast span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).addClass("l-disabled");
            }
            else
            {
                $(".l-bar-btnlast span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).removeClass("l-disabled");
            }
            g.trigger('changePage', [p.newPage]);
            if (p.dataAction == "server")
            {
                g.loadData(p.where);
            }
            else {
                debugger
                g.currentData = g._getCurrentPageData(g.filteredData);
                g._showData();
            }
        },
        getSelectedRow: function ()
        {
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    return o;
            }
            return null;
        },
        getSelectedRows: function ()
        {
            var arr = [];
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    arr.push(o);
            }
            return arr;
        },
        getSelectedRowObj: function ()
        {
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    return this.getRowObj(o);
            }
            return null;
        },
        getSelectedRowObjs: function ()
        {
            var arr = [];
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    arr.push(this.getRowObj(o));
            }
            return arr;
        },
        getCellObj: function (rowParm, column)
        {
            var rowdata = this.getRow(rowParm);
            column = this.getColumn(column);
            return document.getElementById(this._getCellDomId(rowdata, column));
        },
        getRowObj: function (rowParm, frozen)
        {
            var g = this, p = this.options;
            if (rowParm == null) return null;
            if (typeof (rowParm) == "string")
            {
                if (g._isRowId(rowParm))
                    return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + rowParm);
                else
                    return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + g.rows[parseInt(rowParm)]['__id']);
            }
            else if (typeof (rowParm) == "number")
            {
                return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + g.rows[rowParm]['__id']);
            }
            else if (typeof (rowParm) == "object" && rowParm['__id']) //rowdata
            {
                return g.getRowObj(rowParm['__id'], frozen);
            }
            return rowParm;
        },
        getRow: function (rowParm)
        {
            var g = this, p = this.options;
            if (rowParm == null) return null;
            if (typeof (rowParm) == "string")
            {
                if (g._isRowId(rowParm))
                    return g.records[rowParm];
                else
                    return g.rows[parseInt(rowParm)];
            }
            else if (typeof (rowParm) == "number")
            {
                return g.rows[parseInt(rowParm)];
            }
            else if (typeof (rowParm) == "object" && rowParm.nodeType == 1 && !rowParm['__id']) //dom对象
            {
                return g._getRowByDomId(rowParm.id);
            }
            return rowParm;
        },
        _setColumnVisible: function (column, hide)
        {
            var g = this, p = this.options;
            if (!hide)  //显示
            {
                column._hide = false;
                document.getElementById(column['__domid']).style.display = "";
                //判断分组列是否隐藏,如果隐藏了则显示出来
                if (column['__pid'] != -1)
                {
                    var pcol = g._columns[column['__pid']];
                    if (pcol._hide)
                    {
                        document.getElementById(pcol['__domid']).style.display = "";
                        this._setColumnVisible(pcol, hide);
                    }
                }
            }
            else //隐藏
            {
                column._hide = true;
                document.getElementById(column['__domid']).style.display = "none";
                //判断同分组的列是否都隐藏,如果是则隐藏分组列
                if (column['__pid'] != -1)
                {
                    var hideall = true;
                    var pcol = this._columns[column['__pid']];
                    for (var i = 0; pcol && i < pcol.columns.length; i++)
                    {
                        if (!pcol.columns[i]._hide)
                        {
                            hideall = false;
                            break;
                        }
                    }
                    if (hideall)
                    {
                        pcol._hide = true;
                        document.getElementById(pcol['__domid']).style.display = "none";
                        this._setColumnVisible(pcol, hide);
                    }
                }
            }
        },
        //显示隐藏列
        toggleCol: function (columnparm, visible, toggleByPopup)
        {
            var g = this, p = this.options;
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.toggleCol(this, visible, toggleByPopup);
                    });
                    return;
                }
            }
            if (!column) return;
            var columnindex = column['__leafindex'];
            var headercell = document.getElementById(column['__domid']);
            if (!headercell) return;
            headercell = $(headercell);
            var cells = [];
            for (var i in g.rows)
            {
                var obj = g.getCellObj(g.rows[i], column);
                if (obj) cells.push(obj);
            }
            for (var i = 0; i < g.totalNumber; i++)
            {
                var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                if (tobj) cells.push(tobj);
            }
            var colwidth = column._width;
            //显示列
            if (visible && column._hide)
            {
                if (column.frozen)
                    g.f.gridtablewidth += (parseInt(colwidth) + 1);
                else
                    g.gridtablewidth += (parseInt(colwidth) + 1);
                g._setColumnVisible(column, false);
                $(cells).show();
            }
            //隐藏列
            else if (!visible && !column._hide)
            {
                if (column.frozen)
                    g.f.gridtablewidth -= (parseInt(colwidth) + 1);
                else
                    g.gridtablewidth -= (parseInt(colwidth) + 1);
                g._setColumnVisible(column, true);
                $(cells).hide();
            }
            if (column.frozen)
            {
                $("div:first", g.f.gridheader).width(g.f.gridtablewidth);
                $("div:first", g.f.gridbody).width(g.f.gridtablewidth);
            }
            else
            {
                $("div:first", g.gridheader).width(g.gridtablewidth + 40);
                $("div:first", g.gridbody).width(g.gridtablewidth);
            }
            g._updateFrozenWidth();
            if (!toggleByPopup)
            {
                $(':checkbox[columnindex=' + columnindex + "]", g.popup).each(function ()
                {
                    this.checked = visible;
                    if ($.fn.ligerCheckBox)
                    {
                        var checkboxmanager = $(this).ligerGetCheckBoxManager();
                        if (checkboxmanager) checkboxmanager.updateStyle();
                    }
                });
            }
        },
        //设置列宽
        setColumnWidth: function (columnparm, newwidth)
        {
            var g = this, p = this.options;
            if (!newwidth) return;
            newwidth = parseInt(newwidth, 10);
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.setColumnWidth(this, newwidth);
                    });
                    return;
                }
            }
            if (!column) return;
            var mincolumnwidth = p.minColumnWidth;
            if (column.minWidth) mincolumnwidth = column.minWidth;
            newwidth = newwidth < mincolumnwidth ? mincolumnwidth : newwidth;
            var diff = newwidth - column._width;
            if (g.trigger('beforeChangeColumnWidth', [column, newwidth]) == false) return;
            column._width = newwidth;
            if (column.frozen)
            {
                g.f.gridtablewidth += diff;
                $("div:first", g.f.gridheader).width(g.f.gridtablewidth);
                $("div:first", g.f.gridbody).width(g.f.gridtablewidth);
            }
            else
            {
                g.gridtablewidth += diff;
                $("div:first", g.gridheader).width(g.gridtablewidth + 40);
                $("div:first", g.gridbody).width(g.gridtablewidth);
            }
            $(document.getElementById(column['__domid'])).css('width', newwidth);
            var cells = [];
            for (var rowid in g.records)
            {
                var obj = g.getCellObj(g.records[rowid], column);
                if (obj) cells.push(obj);

                if (!g.enabledDetailEdit() && g.editors[rowid] && g.editors[rowid][column['__id']])
                {
                    var o = g.editors[rowid][column['__id']];
                    if (o.editor.resize) o.editor.resize(o.input, newwidth, o.container.height(), o.editParm);
                }
            }
            for (var i = 0; i < g.totalNumber; i++)
            {
                var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                if (tobj) cells.push(tobj);
            }
            $(cells).css('width', newwidth).find("> div.l-grid-row-cell-inner:first").css('width', newwidth - 8);

            g._updateFrozenWidth();


            g.trigger('afterChangeColumnWidth', [column, newwidth]);
        },
        //改变列表头内容
        changeHeaderText: function (columnparm, headerText)
        {
            var g = this, p = this.options;
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.changeHeaderText(this, headerText);
                    });
                    return;
                }
            }
            if (!column) return;
            var columnindex = column['__leafindex'];
            var headercell = document.getElementById(column['__domid']);
            $(".l-grid-hd-cell-text", headercell).html(headerText);
            if (p.allowHideColumn)
            {
                $(':checkbox[columnindex=' + columnindex + "]", g.popup).parent().next().html(headerText);
            }
        },
        //改变列的位置
        changeCol: function (from, to, isAfter)
        {
            var g = this, p = this.options;
            if (!from || !to) return;
            var fromCol = g.getColumn(from);
            var toCol = g.getColumn(to);
            fromCol.frozen = toCol.frozen;
            var fromColIndex, toColIndex;
            var fromColumns = fromCol['__pid'] == -1 ? p.columns : g._columns[fromCol['__pid']].columns;
            var toColumns = toCol['__pid'] == -1 ? p.columns : g._columns[toCol['__pid']].columns;
            fromColIndex = $.inArray(fromCol, fromColumns);
            toColIndex = $.inArray(toCol, toColumns);
            var sameParent = fromColumns == toColumns;
            var sameLevel = fromCol['__level'] == toCol['__level'];
            toColumns.splice(toColIndex + (isAfter ? 1 : 0), 0, fromCol);
            if (!sameParent)
            {
                fromColumns.splice(fromColIndex, 1);
            }
            else
            {
                if (isAfter) fromColumns.splice(fromColIndex, 1);
                else fromColumns.splice(fromColIndex + 1, 1);
            }
            g._setColumns(p.columns);
            g.reRender();
        },


        collapseDetail: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            for (var i = 0, l = g.columns.length; i < l; i++)
            {
                if (g.columns[i].isdetail)
                {
                    var row = g.getRowObj(rowdata);
                    var cell = g.getCellObj(rowdata, g.columns[i]);
                    $(row).next("tr.l-grid-detailpanel").hide();
                    $(".l-grid-row-cell-detailbtn:first", cell).removeClass("l-open");
                    g.trigger('SysGridHeightChanged');
                    return;
                }
            }
        },
        extendDetail: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            for (var i = 0, l = g.columns; i < l; i++)
            {
                if (g.columns[i].isdetail)
                {
                    var row = g.getRowObj(rowdata);
                    var cell = g.getCellObj(rowdata, g.columns[i]);
                    $(row).next("tr.l-grid-detailpanel").show();
                    $(".l-grid-row-cell-detailbtn:first", cell).addClass("l-open");
                    g.trigger('SysGridHeightChanged');
                    return;
                }
            }
        },
        getParent: function (rowParm)
        {
            var g = this, p = this.options;
            if (!p.tree) return null;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return null;
            if (rowdata['__pid'] in g.records) return g.records[rowdata['__pid']];
            else return null;
        },
        getChildren: function (rowParm, deep)
        {
            var g = this, p = this.options;
            if (!p.tree) return null;
            var rowData = g.getRow(rowParm);
            if (!rowData) return null;
            var arr = [];
            function loadChildren(data)
            {
                if (data[p.tree.childrenName])
                {
                    for (var i = 0, l = data[p.tree.childrenName].length; i < l; i++)
                    {
                        var o = data[p.tree.childrenName][i];
                        if (o['__status'] == 'delete') continue;
                        arr.push(o);
                        if (deep)
                            loadChildren(o);
                    }
                }
            }
            loadChildren(rowData);
            return arr;
        },
        isLeaf: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            return rowdata['__hasChildren'] ? false : true;
        },
        hasChildren: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = this.getRow(rowParm);
            if (!rowdata) return;
            return (rowdata[p.tree.childrenName] && rowdata[p.tree.childrenName].length) ? true : false;
        },
        existRecord: function (record)
        {
            for (var rowid in this.records)
            {
                if (this.records[rowid] == record) return true;
            }
            return false;
        },
        _removeSelected: function (rowdata)
        {
            var g = this, p = this.options;
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        var index2 = $.inArray(children[i], g.selected);
                        if (index2 != -1) g.selected.splice(index2, 1);
                    }
                }
            }
            var index = $.inArray(rowdata, g.selected);
            if (index != -1) g.selected.splice(index, 1);
        },
        _getParentChildren: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata;
            if (p.tree && g.existRecord(rowdata) && rowdata['__pid'] in g.records)
            {
                listdata = g.records[rowdata['__pid']][p.tree.childrenName];
            }
            else
            {
                listdata = g.currentData[p.root];
            }
            return listdata;
        },
        _removeData: function (rowdata)
        {
            var g = this, p = this.options;
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index != -1)
            {
                listdata.splice(index, 1);
            }
            g._removeSelected(rowdata);
        },
        _addData: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            var listdata = g.currentData[p.root];
            if (neardata)
            {
                if (p.tree)
                {
                    if (parentdata)
                        listdata = parentdata[p.tree.childrenName];
                    else if (neardata['__pid'] in g.records)
                        listdata = g.records[neardata['__pid']][p.tree.childrenName];
                }
                var index = $.inArray(neardata, listdata);
                listdata.splice(index == -1 ? -1 : index + (isBefore ? 0 : 1), 0, rowdata);
            }
            else
            {
                if (p.tree && parentdata)
                {
                    listdata = parentdata[p.tree.childrenName];
                }
                listdata.push(rowdata);
            }
        },
        //移动数据(树)
        //@parm [parentdata] 附加到哪一个节点下级
        //@parm [neardata] 附加到哪一个节点的上方/下方
        //@parm [isBefore] 是否附加到上方
        _appendData: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            rowdata[p.statusName] = "update";
            g._removeData(rowdata);
            g._addData(rowdata, parentdata, neardata, isBefore);
        },
        appendRange: function (rows, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            var toRender = false;
            $.each(rows, function (i, item)
            {
                if (item['__id'] && g.existRecord(item))
                {
                    if (g.isLeaf(parentdata)) g.upgrade(parentdata);
                    g._appendData(item, parentdata, neardata, isBefore);
                    toRender = true;
                }
                else
                {
                    g.appendRow(item, parentdata, neardata, isBefore);
                }
            });
            if (toRender) g.reRender();

        },
        appendRow: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            if ($.isArray(rowdata))
            {
                g.appendRange(rowdata, parentdata, neardata, isBefore);
                return;
            }
            if (rowdata['__id'] && g.existRecord(rowdata))
            {
                g._appendData(rowdata, parentdata, neardata, isBefore);
                g.reRender();
                return;
            }
            if (parentdata && g.isLeaf(parentdata)) g.upgrade(parentdata);
            g.addRow(rowdata, neardata, isBefore ? true : false, parentdata);
        },
        upgrade: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata || !p.tree) return;
            rowdata[p.tree.childrenName] = rowdata[p.tree.childrenName] || [];
            rowdata['__hasChildren'] = true;
            var rowobjs = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) rowobjs.push(g.getRowObj(rowdata, true));
            $("> td > div > .l-grid-tree-space:last", rowobjs).addClass("l-grid-tree-link l-grid-tree-link-open");
        },
        demotion: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata || !p.tree) return;
            var rowobjs = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) rowobjs.push(g.getRowObj(rowdata, true));
            $("> td > div > .l-grid-tree-space:last", rowobjs).removeClass("l-grid-tree-link l-grid-tree-link-open l-grid-tree-link-close");
            if (g.hasChildren(rowdata))
            {
                var children = g.getChildren(rowdata);
                for (var i = 0, l = children.length; i < l; i++)
                {
                    g.deleteRow(children[i]);
                }
            }
            rowdata['__hasChildren'] = false;
        },
        collapse: function (rowParm)
        {
            var g = this, p = this.options;
            var targetRowObj = g.getRowObj(rowParm);
            var linkbtn = $(".l-grid-tree-link", targetRowObj);
            if (linkbtn.hasClass("l-grid-tree-link-close")) return;
            g.toggle(rowParm);
        },
        expand: function (rowParm)
        {
            var g = this, p = this.options;
            var targetRowObj = g.getRowObj(rowParm);
            var linkbtn = $(".l-grid-tree-link", targetRowObj);
            if (linkbtn.hasClass("l-grid-tree-link-open")) return;
            g.toggle(rowParm);
        },
        toggle: function (rowParm)
        {
            if (!rowParm) return;
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var targetRowObj = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) targetRowObj.push(g.getRowObj(rowdata, true));
            var level = rowdata['__level'], indexInCollapsedRows;
            var linkbtn = $(".l-grid-tree-link:first", targetRowObj);
            var opening = true;
            g.collapsedRows = g.collapsedRows || [];
            if (linkbtn.hasClass("l-grid-tree-link-close")) //收缩
            {
                linkbtn.removeClass("l-grid-tree-link-close").addClass("l-grid-tree-link-open");
                indexInCollapsedRows = $.inArray(rowdata, g.collapsedRows);
                if (indexInCollapsedRows != -1) g.collapsedRows.splice(indexInCollapsedRows, 1);
            }
            else //折叠
            {
                opening = false;
                linkbtn.addClass("l-grid-tree-link-close").removeClass("l-grid-tree-link-open");
                indexInCollapsedRows = $.inArray(rowdata, g.collapsedRows);
                if (indexInCollapsedRows == -1) g.collapsedRows.push(rowdata);
            }
            var children = g.getChildren(rowdata, true);
            for (var i = 0, l = children.length; i < l; i++)
            {
                var o = children[i];
                var currentRow = $([g.getRowObj(o['__id'])]);
                if (g.enabledFrozen()) currentRow = currentRow.add(g.getRowObj(o['__id'], true));
                if (opening)
                {
                    $(".l-grid-tree-link", currentRow).removeClass("l-grid-tree-link-close").addClass("l-grid-tree-link-open");
                    currentRow.show();
                }
                else
                {
                    $(".l-grid-tree-link", currentRow).removeClass("l-grid-tree-link-open").addClass("l-grid-tree-link-close");
                    currentRow.hide();
                }
            }
        },
        _bulid: function ()
        {
            var g = this;
            g._clearGrid();
            //创建头部
            g._initBuildHeader();
            //宽度高度初始化
            g._initHeight();
            //创建底部工具条
            g._initFootbar();
            //创建分页
            g._buildPager();
            //创建事件
            g._setEvent();
        },
        _setColumns: function (columns)
        {
            var g = this;
            //初始化列
            g._initColumns();
            //创建表头
            g._initBuildGridHeader();
            //创建 显示/隐藏 列 列表
            g._initBuildPopup();
        },
        _initBuildHeader: function ()
        {
            var g = this, p = this.options;
            if (p.title)
                $(".l-panel-header-text", g.header).html(p.title);
            else
                g.header.hide();
        },
        _createColumnId: function (column)
        {
            if (column.id != null) return column.id.toString();
            return "c" + (100 + this._columnCount);
        },
        _isColumnId: function (str)
        {
            return (str in this._columns);
        },
        _initColumns: function ()
        {
            var g = this, p = this.options;
            g._columns = {};             //全部列的信息  
            g._columnCount = 0;
            g._columnLeafCount = 0;
            g._columnMaxLevel = 1;
            if (!p.columns) return;
            function removeProp(column, props)
            {
                for (var i in props)
                {
                    if (props[i] in column)
                        delete column[props[i]];
                }
            }
            //设置id、pid、level、leaf，返回叶节点数,如果是叶节点，返回1
            function setColumn(column, level, pid, previd)
            {
                removeProp(column, ['__id', '__pid', '__previd', '__nextid', '__domid', '__leaf', '__leafindex', '__level', '__colSpan', '__rowSpan']);
                if (level > g._columnMaxLevel) g._columnMaxLevel = level;
                g._columnCount++;
                column['__id'] = g._createColumnId(column);
                column['__domid'] = g.id + "|hcell|" + column['__id'];
                g._columns[column['__id']] = column;
                if (!column.columns || !column.columns.length)
                    column['__leafindex'] = g._columnLeafCount++;
                column['__level'] = level;
                column['__pid'] = pid;
                column['__previd'] = previd;
                if (!column.columns || !column.columns.length)
                {
                    column['__leaf'] = true;
                    return 1;
                }
                var leafcount = 0;
                var newid = -1;
                for (var i = 0, l = column.columns.length; i < l; i++)
                {
                    var col = column.columns[i];
                    leafcount += setColumn(col, level + 1, column['__id'], newid);
                    newid = col['__id'];
                }
                column['__leafcount'] = leafcount;
                return leafcount;
            }
            var lastid = -1;
            //行序号
            if (p.rownumbers)
            {
                var frozenRownumbers = g.enabledGroup() ? false : p.frozen && p.frozenRownumbers;
                var col = { isrownumber: true, issystem: true, width: p.rownumbersColWidth, frozen: frozenRownumbers };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //明细列
            if (g.enabledDetail())
            {
                var frozenDetail = g.enabledGroup() ? false : p.frozen && p.frozenDetail;
                var col = { isdetail: true, issystem: true, width: p.detailColWidth, frozen: frozenDetail };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //复选框列
            if (g.enabledCheckbox())
            {
                var frozenCheckbox = g.enabledGroup() ? false : p.frozen && p.frozenCheckbox;
                var col = { ischeckbox: true, issystem: true, width: p.detailColWidth, frozen: frozenCheckbox };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            for (var i = 0, l = p.columns.length; i < l; i++)
            {
                var col = p.columns[i];
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //设置colSpan和rowSpan
            for (var id in g._columns)
            {
                var col = g._columns[id];
                if (col['__leafcount'] > 1)
                {
                    col['__colSpan'] = col['__leafcount'];
                }
                if (col['__leaf'] && col['__level'] != g._columnMaxLevel)
                {
                    col['__rowSpan'] = g._columnMaxLevel - col['__level'] + 1;
                }
            }
            //叶级别列的信息  
            g.columns = g.getColumns();
            $(g.columns).each(function (i, column)
            {
                column.columnname = column.name;
                column.columnindex = i;
                column.type = column.type || "string";
                column.islast = i == g.columns.length - 1;
                column.isSort = column.isSort == false ? false : true;
                column.frozen = column.frozen ? true : false;
                column._width = g._getColumnWidth(column);
                column._hide = column.hide ? true : false;
            });
        },
        _getColumnWidth: function (column)
        {
            var g = this, p = this.options;
            if (column._width) return column._width;
            var colwidth;
            if (column.width)
            {
                colwidth = column.width;
            }
            else if (p.columnWidth)
            {
                colwidth = p.columnWidth;
            }
            if (!colwidth)
            {
                var lwidth = 4;
                if (g.enabledCheckbox()) lwidth += p.checkboxColWidth;
                if (g.enabledDetail()) lwidth += p.detailColWidth;
                colwidth = parseInt((g.grid.width() - lwidth) / g.columns.length);
            }
            if (typeof (colwidth) == "string" && colwidth.indexOf('%') > 0)
            {
                column._width = colwidth = parseInt(parseInt(colwidth) * 0.01 * (g.grid.width() - g.columns.length));
            }
            if (column.minWidth && colwidth < column.minWidth) colwidth = column.minWidth;
            if (column.maxWidth && colwidth > column.maxWidth) colwidth = column.maxWidth;
            column._width = colwidth;
            return colwidth;
        },
        _createHeaderCell: function (column)
        {
            var g = this, p = this.options;
            var jcell = $("<td class='l-grid-hd-cell'><div class='l-grid-hd-cell-inner'><span class='l-grid-hd-cell-text'></span></div></td>");
            jcell.attr("id", column['__domid']);
            if (!column['__leaf'])
                jcell.addClass("l-grid-hd-cell-mul");
            if (column.columnindex == g.columns.length - 1)
            {
                jcell.addClass("l-grid-hd-cell-last");
            }
            if (column.isrownumber)
            {
                jcell.addClass("l-grid-hd-cell-rownumbers");
                jcell.html("<div class='l-grid-hd-cell-inner'></div>");
            }
            if (column.ischeckbox)
            {
                jcell.addClass("l-grid-hd-cell-checkbox");
                jcell.html("<div class='l-grid-hd-cell-inner'><div class='l-grid-hd-cell-text l-grid-hd-cell-btn-checkbox'></div></div>");
            }
            if (column.isdetail)
            {
                jcell.addClass("l-grid-hd-cell-detail");
                jcell.html("<div class='l-grid-hd-cell-inner'><div class='l-grid-hd-cell-text l-grid-hd-cell-btn-detail'></div></div>");
            }
            if (column['__colSpan']) jcell.attr("colSpan", column['__colSpan']);
            if (column['__rowSpan'])
            {
                jcell.attr("rowSpan", column['__rowSpan']);
                jcell.height(23 * column['__rowSpan']);
            } else
            {
                jcell.height(23);
            }
            if (column['__leaf'])
            {
                jcell.width(column['_width']);
                jcell.attr("columnindex", column['__leafindex']);
            }
            if (column._hide) jcell.hide();
            if (column.name) jcell.attr({ columnname: column.name });
            var headerText = "";
            if (column.display && column.display != "")
                headerText = column.display;
            else if (column.headerRender)
                headerText = column.headerRender(column);
            else
                headerText = "&nbsp;";
            $(".l-grid-hd-cell-text:first", jcell).html(headerText);
            if (!column.issystem && column['__leaf'] && column.resizable !== false && $.fn.ligerResizable)
            {
                g.colResizable[column['__id']] = jcell.ligerResizable({ handles: 'e',
                    onStartResize: function (e, ev)
                    {
                        this.proxy.hide();
                        g.draggingline.css({ height: g.body.height(), top: 0, left: ev.pageX - g.grid.offset().left + parseInt(g.body[0].scrollLeft) }).show();
                    },
                    onResize: function (e, ev)
                    {
                        g.colresizing = true;
                        g.draggingline.css({ left: ev.pageX - g.grid.offset().left + parseInt(g.body[0].scrollLeft) });
                        $('body').add(jcell).css('cursor', 'e-resize');
                    },
                    onStopResize: function (e)
                    {
                        g.colresizing = false;
                        $('body').add(jcell).css('cursor', 'default');
                        g.draggingline.hide();
                        g.setColumnWidth(column, column._width + e.diffX);
                        return false;
                    }
                });
            }
            return jcell;
        },
        _initBuildGridHeader: function ()
        {
            var g = this, p = this.options;
            g.gridtablewidth = 0;
            g.f.gridtablewidth = 0;
            if (g.colResizable)
            {
                for (var i in g.colResizable)
                {
                    g.colResizable[i].destroy();
                }
                g.colResizable = null;
            }
            g.colResizable = {};
            $("tbody:first", g.gridheader).html("");
            $("tbody:first", g.f.gridheader).html("");
            for (var level = 1; level <= g._columnMaxLevel; level++)
            {
                var columns = g.getColumns(level);           //获取level层次的列集合
                var islast = level == g._columnMaxLevel;     //是否最末级
                var tr = $("<tr class='l-grid-hd-row'></tr>");
                var trf = $("<tr class='l-grid-hd-row'></tr>");
                if (!islast) tr.add(trf).addClass("l-grid-hd-mul");
                $("tbody:first", g.gridheader).append(tr);
                $("tbody:first", g.f.gridheader).append(trf);
                $(columns).each(function (i, column)
                {
                    (column.frozen ? trf : tr).append(g._createHeaderCell(column));
                    if (column['__leaf'])
                    {
                        var colwidth = column['_width'];
                        if (!column.frozen)
                            g.gridtablewidth += (parseInt(colwidth) ? parseInt(colwidth) : 0) + 1;
                        else
                            g.f.gridtablewidth += (parseInt(colwidth) ? parseInt(colwidth) : 0) + 1;
                    }
                });
            }
            if (g._columnMaxLevel > 0)
            {
                var h = 23 * g._columnMaxLevel;
                g.gridheader.add(g.f.gridheader).height(h);
                if (p.rownumbers && p.frozenRownumbers) g.f.gridheader.find("td:first").height(h);
            }
            g._updateFrozenWidth();
            $("div:first", g.gridheader).width(g.gridtablewidth + 40);
        },
        _initBuildPopup: function ()
        {
            var g = this, p = this.options;
            $(':checkbox', g.popup).unbind();
            $('tbody tr', g.popup).remove();
            $(g.columns).each(function (i, column)
            {
                if (column.issystem) return;
                if (column.isAllowHide == false) return;
                var chk = 'checked="checked"';
                if (column._hide) chk = '';
                var header = column.display;
                $('tbody', g.popup).append('<tr><td class="l-column-left"><input type="checkbox" ' + chk + ' class="l-checkbox" columnindex="' + i + '"/></td><td class="l-column-right">' + header + '</td></tr>');
            });
            if ($.fn.ligerCheckBox)
            {
                $('input:checkbox', g.popup).ligerCheckBox(
                {
                    onBeforeClick: function (obj)
                    {
                        if (!obj.checked) return true;
                        if ($('input:checked', g.popup).length <= p.minColToggle)
                            return false;
                        return true;
                    }
                });
            }
            //表头 - 显示/隐藏'列控制'按钮事件
            if (p.allowHideColumn)
            {
                $('tr', g.popup).hover(function ()
                {
                    $(this).addClass('l-popup-row-over');
                },
                function ()
                {
                    $(this).removeClass('l-popup-row-over');
                });
                var onPopupCheckboxChange = function ()
                {
                    if ($('input:checked', g.popup).length + 1 <= p.minColToggle)
                    {
                        return false;
                    }
                    g.toggleCol(parseInt($(this).attr("columnindex")), this.checked, true);
                };
                if ($.fn.ligerCheckBox)
                    $(':checkbox', g.popup).bind('change', onPopupCheckboxChange);
                else
                    $(':checkbox', g.popup).bind('click', onPopupCheckboxChange);
            }
        },
        _initHeight: function ()
        {
            var g = this, p = this.options;
            if (p.height == 'auto')
            {
                g.gridbody.height('auto');
                g.f.gridbody.height('auto');
            }
            if (p.width)
            {
                g.grid.width(p.width);
            }
            g._onResize.call(g);
        },
        _initFootbar: function ()
        {
            var g = this, p = this.options;
            if (p.usePager)
            {
                //创建底部工具条 - 选择每页显示记录数
                var optStr = "";
                var selectedIndex = -1;
                $(p.pageSizeOptions).each(function (i, item)
                {
                    var selectedStr = "";
                    if (p.pageSize == item) selectedIndex = i;
                    optStr += "<option value='" + item + "' " + selectedStr + " >" + item + "</option>";
                });

                $('.l-bar-selectpagesize', g.toolbar).append("<select name='rp'>" + optStr + "</select>");
                if (selectedIndex != -1) $('.l-bar-selectpagesize select', g.toolbar)[0].selectedIndex = selectedIndex;
                if (p.switchPageSizeApplyComboBox && $.fn.ligerComboBox)
                {
                    $(".l-bar-selectpagesize select", g.toolbar).ligerComboBox(
                    {
                        onBeforeSelect: function ()
                        {
                            if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                                return false;
                            return true;
                        },
                        width: 45
                    });
                }
            }
            else
            {
                g.toolbar.hide();
            }
        },
        _searchData: function (data, clause)
        {
            var g = this, p = this.options;
            var newData = new Array();
            for (var i = 0; i < data.length; i++)
            {
                if (clause(data[i], i))
                {
                    newData[newData.length] = data[i];
                }
            }
            return newData;
        },
        _clearGrid: function ()
        {
            var g = this, p = this.options;
            for (var i in g.rows)
            {
                var rowobj = $(g.getRowObj(g.rows[i]));
                if (g.enabledFrozen())
                    rowobj = rowobj.add(g.getRowObj(g.rows[i], true));
                rowobj.unbind();
            }
            //清空数据
            g.gridbody.html("");
            g.f.gridbody.html("");
            g.recordNumber = 0;
            g.records = {};
            g.rows = [];
            if (!g.selected)
                g.selected = [];
            g.totalNumber = 0;
        },
        _fillGridBody: function (data, frozen)
        {
            var g = this, p = this.options;
            //加载数据 
            var gridhtmlarr = ['<div class="l-grid-body-inner"><table class="l-grid-body-table" cellpadding=0 cellspacing=0><tbody>'];
            if (g.enabledGroup()) //启用分组模式
            {
                var groups = []; //分组列名数组
                var groupsdata = []; //切成几块后的数据
                g.groups = groupsdata;
                for (var rowparm in data)
                {
                    var item = data[rowparm];
                    var groupColumnValue = item[p.groupColumnName];
                    var valueIndex = $.inArray(groupColumnValue, groups);
                    if (valueIndex == -1)
                    {
                        groups.push(groupColumnValue);
                        valueIndex = groups.length - 1;
                        groupsdata.push([]);
                    }
                    groupsdata[valueIndex].push(item);
                }
                $(groupsdata).each(function (i, item)
                {
                    if (groupsdata.length == 1)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-last l-grid-grouprow-first"');
                    if (i == groupsdata.length - 1)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-last"');
                    else if (i == 0)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-first"');
                    else
                        gridhtmlarr.push('<tr class="l-grid-grouprow"');
                    gridhtmlarr.push(' groupindex"=' + i + '" >');
                    gridhtmlarr.push('<td colSpan="' + g.columns.length + '" class="l-grid-grouprow-cell">');
                    gridhtmlarr.push('<span class="l-grid-group-togglebtn">&nbsp;&nbsp;&nbsp;&nbsp;</span>');
                    if (p.groupRender)
                        gridhtmlarr.push(p.groupRender(groups[i], p.groupColumnDisplay));
                    else
                        gridhtmlarr.push(p.groupColumnDisplay + ':' + groups[i]);


                    gridhtmlarr.push('</td>');
                    gridhtmlarr.push('</tr>');

                    gridhtmlarr.push(g._getHtmlFromData(item, frozen));
                    //汇总
                    if (g.isTotalSummary())
                        gridhtmlarr.push(g._getTotalSummaryHtml(item, "l-grid-totalsummary-group", frozen));
                });
            }
            else
            {
                gridhtmlarr.push(g._getHtmlFromData(data, frozen));
            }
            gridhtmlarr.push('</tbody></table></div>');
            (frozen ? g.f.gridbody : g.gridbody).html(gridhtmlarr.join(''));
            //分组时不需要            
            if (!g.enabledGroup())
            {
                //创建汇总行
                g._bulidTotalSummary(frozen);
            }
            $("> div:first", g.gridbody).width(g.gridtablewidth);
            g._onResize();
        },
        _showData: function ()
        {
            var g = this, p = this.options;
            var data = g.currentData[p.root];
            if (p.usePager)
            {
                //更新总记录数 
                if (g.data && g.data[p.record] && p.dataAction == "server")
                    p.total = g.data[p.record];
                else if (g.filteredData && g.filteredData[p.root])
                    p.total = g.filteredData[p.root].length;
                else if (g.data && g.data[p.root])
                    p.total = g.data[p.root].length;
                else if (data)
                    p.total = data.length;

                p.page = p.newPage;
                if (!p.total) p.total = 0;
                if (!p.page) p.page = 1;
                p.pageCount = Math.ceil(p.total / p.pageSize);
                if (!p.pageCount) p.pageCount = 1;
                //更新分页
                g._buildPager();
            }
            //加载中
            $('.l-bar-btnloading:first', g.toolbar).removeClass('l-bar-btnloading');
            if (g.trigger('beforeShowData', [g.currentData]) == false) return;
            g._clearGrid();
            g.isDataChanged = false;
            if (!data) return;
            $(".l-bar-btnload:first span", g.toolbar).removeClass("l-disabled");
            g._updateGridData();
            if (g.enabledFrozen())
                g._fillGridBody(g.rows, true);
            g._fillGridBody(g.rows, false);
            g.trigger('SysGridHeightChanged');
            if (p.totalRender)
            {
                $(".l-panel-bar-total", g.element).remove();
                $(".l-panel-bar", g.element).before('<div class="l-panel-bar-total">' + p.totalRender(g.data, g.filteredData) + '</div>');
            }
            if (p.mouseoverRowCssClass)
            {
                for (var i in g.rows)
                {
                    var rowobj = $(g.getRowObj(g.rows[i]));
                    if (g.enabledFrozen())
                        rowobj = rowobj.add(g.getRowObj(g.rows[i], true));
                    rowobj.bind('mouseover.gridrow', function ()
                    {
                        g._onRowOver(this, true);
                    }).bind('mouseout.gridrow', function ()
                    {
                        g._onRowOver(this, false);
                    });
                }
            }
            g.gridbody.trigger('scroll.grid');
            g.trigger('afterShowData', [g.currentData]);
        },
        _getRowDomId: function (rowdata, frozen)
        {
            return this.id + "|" + (frozen ? "1" : "2") + "|" + rowdata['__id'];
        },
        _getCellDomId: function (rowdata, column)
        {
            return this._getRowDomId(rowdata, column.frozen) + "|" + column['__id'];
        },
        _getHtmlFromData: function (data, frozen)
        {
            if (!data) return "";
            var g = this, p = this.options;
            var gridhtmlarr = [];
            for (var rowparm in data)
            {
                var item = data[rowparm];
                var rowid = item['__id'];
                if (!item) continue;
                gridhtmlarr.push('<tr');
                gridhtmlarr.push(' id="' + g._getRowDomId(item, frozen) + '"');
                gridhtmlarr.push(' class="l-grid-row'); //class start 
                if (g.enabledCheckbox() && p.isChecked && p.isChecked(item))
                {
                    g.select(item);
                    gridhtmlarr.push(' l-selected');
                }
                if (g.isSelected(item))
                {
                    gridhtmlarr.push(' l-selected');
                }
                if (rowid % 2 == 1 && p.alternatingRow)
                    gridhtmlarr.push(' l-grid-row-alt');
                gridhtmlarr.push('" ');  //class end
                if (p.rowAttrRender) gridhtmlarr.push(p.rowAttrRender(item, rowid));
                if (p.tree && g.collapsedRows && g.collapsedRows.length)
                {
                    var isHide = function ()
                    {
                        var pitem = g.getParent(item);
                        while (pitem)
                        {
                            if ($.inArray(pitem, g.collapsedRows) != -1) return true;
                            pitem = g.getParent(pitem);
                        }
                        return false;
                    };
                    if (isHide()) gridhtmlarr.push(' style="display:none;" ');
                }
                gridhtmlarr.push('>');
                $(g.columns).each(function (columnindex, column)
                {
                    if (frozen != column.frozen) return;
                    gridhtmlarr.push('<td');
                    gridhtmlarr.push(' id="' + g._getCellDomId(item, this) + '"');
                    //如果是行序号(系统列)
                    if (this.isrownumber)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-rownumbers" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner">' + (parseInt(item['__index']) + 1) + '</div></td>');
                        return;
                    }
                    //如果是复选框(系统列)
                    if (this.ischeckbox)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-checkbox" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner"><span class="l-grid-row-cell-btn-checkbox"></span></div></td>');
                        return;
                    }
                    //如果是明细列(系统列)
                    else if (this.isdetail)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-detail" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner"><span class="l-grid-row-cell-detailbtn"></span></div></td>');
                        return;
                    }
                    var colwidth = this._width;
                    gridhtmlarr.push(' class="l-grid-row-cell ');
                    if (g.changedCells[rowid + "_" + this['__id']]) gridhtmlarr.push("l-grid-row-cell-edited ");
                    if (this.islast)
                        gridhtmlarr.push('l-grid-row-cell-last ');
                    gridhtmlarr.push('"');
                    //if (this.columnname) gridhtmlarr.push('columnname="' + this.columnname + '"');
                    gridhtmlarr.push(' style = "');
                    gridhtmlarr.push('width:' + colwidth + 'px; ');
                    if (column._hide)
                    {
                        gridhtmlarr.push('display:none;');
                    }
                    gridhtmlarr.push(' ">');
                    gridhtmlarr.push(g._getCellHtml(item, column));
                    gridhtmlarr.push('</td>');
                });
                gridhtmlarr.push('</tr>');
            }
            return gridhtmlarr.join('');
        },
        _getCellHtml: function (rowdata, column)
        {
            var g = this, p = this.options;
            if (column.isrownumber)
                return '<div class="l-grid-row-cell-inner">' + (parseInt(rowdata['__index']) + 1) + '</div>';
            var htmlarr = [];
            htmlarr.push('<div class="l-grid-row-cell-inner"');
            //htmlarr.push('<div');
            htmlarr.push(' style = "width:' + parseInt(column._width - 8) + 'px; ');
            if (p.fixedCellHeight) htmlarr.push('height:' + 22 + 'px;');
            if (column.align) htmlarr.push('text-align:' + column.align + ';');
            var content = g._getCellContent(rowdata, column);
            htmlarr.push('">' + content + '</div>');
            return htmlarr.join('');
        },
        _getCellContent: function (rowdata, column)
        {
            if (!rowdata || !column) return "";
            if (column.isrownumber) return parseInt(rowdata['__index']) + 1;
            var rowid = rowdata['__id'];
            var rowindex = rowdata['__index'];
            var value = column.name ? rowdata[column.name] : null;
            var g = this, p = this.options;
            var content = "";
            if (column.render)
            {
                content = column.render.call(g, rowdata, rowindex, value, column);
            }
            else if ($.ligerDefaults.Grid.formatters[column.type])
            {
                content = $.ligerDefaults.Grid.formatters[column.type].call(g, value, column);
            }
            else if (value != null)
            {
                content = value.toString();
            }
            if (p.tree && (p.tree.columnName != null && p.tree.columnName == column.name || p.tree.columnId != null && p.tree.columnId == column.id))
            {
                content = g._getTreeCellHtml(content, rowdata);
            }
            return content || "";
        },
        _getTreeCellHtml: function (oldContent, rowdata)
        {
            var level = rowdata['__level'];
            var g = this, p = this.options;
            //var isExtend = p.tree.isExtend(rowdata);
            var isExtend = $.inArray(rowdata, g.collapsedRows || []) == -1;
            var isParent = p.tree.isParent(rowdata);
            var content = "";
            level = parseInt(level) || 1;
            for (var i = 1; i < level; i++)
            {
                content += "<div class='l-grid-tree-space'></div>";
            }
            if (isExtend && isParent)
                content += "<div class='l-grid-tree-space l-grid-tree-link l-grid-tree-link-open'></div>";
            else if (isParent)
                content += "<div class='l-grid-tree-space l-grid-tree-link l-grid-tree-link-close'></div>";
            else
                content += "<div class='l-grid-tree-space'></div>";
            content += "<span class='l-grid-tree-content'>" + oldContent + "</span>";
            return content;
        },
        _applyEditor: function (obj)
        {
            var g = this, p = this.options;
            var rowcell = obj;
            var ids = rowcell.id.split('|');
            var columnid = ids[ids.length - 1];
            var column = g._columns[columnid];
            var row = $(rowcell).parent();
            var rowdata = g.getRow(row[0]);
            var rowid = rowdata['__id'];
            var rowindex = rowdata['__index'];
            if (!column || !column.editor) return;
            var columnname = column.name;
            var columnindex = column.columnindex;
            if (column.editor.type && $.ligerDefaults.Grid.editors[column.editor.type])
            {
                var currentdata = rowdata[columnname];
                var editParm = { record: rowdata, value: currentdata, column: column, rowindex: rowindex };
                if (g.trigger('beforeEdit', [editParm]) == false) return false;
                var editor = $.ligerDefaults.Grid.editors[column.editor.type];
                var jcell = $(rowcell), offset = $(rowcell).offset();
                jcell.html("");
                g.setCellEditing(rowdata, column, true);
                var width = $(rowcell).width(), height = $(rowcell).height();
                var container = $("<div class='l-grid-editor'></div>").appendTo('body');
                if ($.browser.mozilla)
                    container.css({ left: offset.left, top: offset.top }).show();
                else
                    container.css({ left: offset.left + 1, top: offset.top + 1 }).show();
                var editorInput = g._createEditor(editor, container, editParm, width, height);
                g.editor = { editing: true, editor: editor, input: editorInput, editParm: editParm, container: container };
                g.unbind('sysEndEdit');
                g.bind('sysEndEdit', function ()
                {
                    var newValue = editor.getValue(editorInput, editParm);
                    if (newValue != currentdata)
                    {
                        $(rowcell).addClass("l-grid-row-cell-edited");
                        g.changedCells[rowid + "_" + column['__id']] = true;
                        if (column.editor.onChange) column.editor.onChange(rowcell, newValue);
                        editParm.value = newValue;
                        if (g._checkEditAndUpdateCell(editParm))
                        {
                            if (column.editor.onChanged) column.editor.onChanged(rowcell, newValue);
                        }
                    }
                });
            }
        },
        _checkEditAndUpdateCell: function (editParm)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeSubmitEdit', [editParm]) == false) return false;
            if (g.editor.editor && g.editor.editor.destroy)
                g.editor.editor.destroy(g.editor.input, g.editor.editParm);
            g.updateCell(editParm.column, editParm.value, editParm.record);
            if (editParm.column.render || g.enabledTotal()) g.reRender({ column: editParm.column });
            g.reRender({ rowdata: editParm.record });
            return true;
        },
        _getCurrentPageData: function (source)
        {
            var g = this, p = this.options;
            var data = {};
            data[p.root] = [];
            if (!source || !source[p.root] || !source[p.root].length)
            {
                data[p.record] = 0;
                return data;
            }
            data[p.record] = source[p.root].length;
            if (!p.newPage) p.newPage = 1;
            for (i = (p.newPage - 1) * p.pageSize; i < source[p.root].length && i < p.newPage * p.pageSize; i++)
            {
                data[p.root].push(source[p.root][i]);
            }
            return data;
        },
        //比较某一列两个数据
        _compareData: function (data1, data2, columnName, columnType)
        {
            var g = this, p = this.options;
            var val1 = data1[columnName], val2 = data2[columnName];
            if (val1 == null && val2 != null) return 1;
            else if (val1 == null && val2 == null) return 0;
            else if (val1 != null && val2 == null) return -1;
            if ($.ligerDefaults.Grid.sorters[columnType])
                return $.ligerDefaults.Grid.sorters[columnType].call(g, val1, val2);
            else
                return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
        },
        _getTotalCellContent: function (column, data)
        {
            var g = this, p = this.options;
            var totalsummaryArr = [];
            if (column.totalSummary)
            {
                var isExist = function (type)
                {
                    for (var i = 0; i < types.length; i++)
                        if (types[i].toLowerCase() == type.toLowerCase()) return true;
                    return false;
                };
                var sum = 0, count = 0, avg = 0;
                var max = parseFloat(data[0][column.name]);
                var min = parseFloat(data[0][column.name]);
                for (var i = 0; i < data.length; i++)
                {
                    count += 1;
                    var value = parseFloat(data[i][column.name]);
                    if (!value) continue;
                    sum += value;
                    if (value > max) max = value;
                    if (value < min) min = value;
                }
                avg = sum * 1.0 / data.length;
                if (column.totalSummary.render)
                {
                    var renderhtml = column.totalSummary.render({
                        sum: sum,
                        count: count,
                        avg: avg,
                        min: min,
                        max: max
                    }, column, g.data);
                    totalsummaryArr.push(renderhtml);
                }
                else if (column.totalSummary.type)
                {
                    var types = column.totalSummary.type.split(',');
                    if (isExist('sum'))
                        totalsummaryArr.push("<div>Sum=" + sum.toFixed(2) + "</div>");
                    if (isExist('count'))
                        totalsummaryArr.push("<div>Count=" + count + "</div>");
                    if (isExist('max'))
                        totalsummaryArr.push("<div>Max=" + max.toFixed(2) + "</div>");
                    if (isExist('min'))
                        totalsummaryArr.push("<div>Min=" + min.toFixed(2) + "</div>");
                    if (isExist('avg'))
                        totalsummaryArr.push("<div>Avg=" + avg.toFixed(2) + "</div>");
                }
            }
            return totalsummaryArr.join('');
        },
        _getTotalSummaryHtml: function (data, classCssName, frozen)
        {
            var g = this, p = this.options;
            var totalsummaryArr = [];
            if (classCssName)
                totalsummaryArr.push('<tr class="l-grid-totalsummary ' + classCssName + '">');
            else
                totalsummaryArr.push('<tr class="l-grid-totalsummary">');
            $(g.columns).each(function (columnindex, column)
            {
                if (this.frozen != frozen) return;
                //如果是行序号(系统列)
                if (this.isrownumber)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-rownumbers" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                //如果是复选框(系统列)
                if (this.ischeckbox)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-checkbox" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                //如果是明细列(系统列)
                else if (this.isdetail)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-detail" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                totalsummaryArr.push('<td class="l-grid-totalsummary-cell');
                if (this.islast)
                    totalsummaryArr.push(" l-grid-totalsummary-cell-last");
                totalsummaryArr.push('" ');
                totalsummaryArr.push('id="' + g.id + "|total" + g.totalNumber + "|" + column.__id + '" ');
                totalsummaryArr.push('width="' + this._width + '" ');
                columnname = this.columnname;
                if (columnname)
                {
                    totalsummaryArr.push('columnname="' + columnname + '" ');
                }
                totalsummaryArr.push('columnindex="' + columnindex + '" ');
                totalsummaryArr.push('><div class="l-grid-totalsummary-cell-inner"');
                if (column.align)
                    totalsummaryArr.push(' style="text-Align:' + column.align + ';"');
                totalsummaryArr.push('>');
                totalsummaryArr.push(g._getTotalCellContent(column, data));
                totalsummaryArr.push('</div></td>');
            });
            totalsummaryArr.push('</tr>');
            if (!frozen) g.totalNumber++;
            return totalsummaryArr.join('');
        },
        _bulidTotalSummary: function (frozen)
        {
            var g = this, p = this.options;
            if (!g.isTotalSummary()) return false;
            if (!g.currentData || g.currentData[p.root].length == 0) return false;
            var totalRow = $(g._getTotalSummaryHtml(g.currentData[p.root], null, frozen));
            $("tbody:first", frozen ? g.f.gridbody : g.gridbody).append(totalRow);
        },
        _buildPager: function ()
        {
            var g = this, p = this.options;
            $('.pcontrol input', g.toolbar).val(p.page);
            if (!p.pageCount) p.pageCount = 1;
            $('.pcontrol span', g.toolbar).html(p.pageCount);
            var r1 = parseInt((p.page - 1) * p.pageSize) + 1.0;
            var r2 = parseInt(r1) + parseInt(p.pageSize) - 1;
            if (!p.total) p.total = 0;
            if (p.total < r2) r2 = p.total;
            if (!p.total) r1 = r2 = 0;
            if (r1 < 0) r1 = 0;
            if (r2 < 0) r2 = 0;
            var stat = p.pageStatMessage;
            stat = stat.replace(/{from}/, r1);
            stat = stat.replace(/{to}/, r2);
            stat = stat.replace(/{total}/, p.total);
            stat = stat.replace(/{pagesize}/, p.pageSize);
            $('.l-bar-text', g.toolbar).html(stat);
            if (!p.total)
            {
                $(".l-bar-btnfirst span,.l-bar-btnprev span,.l-bar-btnnext span,.l-bar-btnlast span", g.toolbar)
                    .addClass("l-disabled");
            }
            if (p.page == 1)
            {
                $(".l-bar-btnfirst span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).addClass("l-disabled");
            }
            else if (p.page > p.pageCount && p.pageCount > 0)
            {
                $(".l-bar-btnfirst span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).removeClass("l-disabled");
            }
            if (p.page == p.pageCount)
            {
                $(".l-bar-btnlast span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).addClass("l-disabled");
            }
            else if (p.page < p.pageCount && p.pageCount > 0)
            {
                $(".l-bar-btnlast span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).removeClass("l-disabled");
            }
        },
        _getRowIdByDomId: function (domid)
        {
            var ids = domid.split('|');
            var rowid = ids[2];
            return rowid;
        },
        _getRowByDomId: function (domid)
        {
            return this.records[this._getRowIdByDomId(domid)];
        },
        _getSrcElementByEvent: function (e)
        {
            var g = this;
            var obj = (e.target || e.srcElement);
            var jobjs = $(obj).parents().add(obj);
            var fn = function (parm)
            {
                for (var i = 0, l = jobjs.length; i < l; i++)
                {
                    if (typeof parm == "string")
                    {
                        if ($(jobjs[i]).hasClass(parm)) return jobjs[i];
                    }
                    else if (typeof parm == "object")
                    {
                        if (jobjs[i] == parm) return jobjs[i];
                    }
                }
                return null;
            };
            if (fn("l-grid-editor")) return { editing: true, editor: fn("l-grid-editor") };
            if (jobjs.index(this.element) == -1) return { out: true };
            var indetail = false;
            if (jobjs.hasClass("l-grid-detailpanel") && g.detailrows)
            {
                for (var i = 0, l = g.detailrows.length; i < l; i++)
                {
                    if (jobjs.index(g.detailrows[i]) != -1)
                    {
                        indetail = true;
                        break;
                    }
                }
            }
            var r = {
                grid: fn("l-panel"),
                indetail: indetail,
                frozen: fn(g.gridview1[0]) ? true : false,
                header: fn("l-panel-header"), //标题
                gridheader: fn("l-grid-header"), //表格头 
                gridbody: fn("l-grid-body"),
                total: fn("l-panel-bar-total"), //总汇总 
                popup: fn("l-grid-popup"),
                toolbar: fn("l-panel-bar")
            };
            if (r.gridheader)
            {
                r.hrow = fn("l-grid-hd-row");
                r.hcell = fn("l-grid-hd-cell");
                r.hcelltext = fn("l-grid-hd-cell-text");
                r.checkboxall = fn("l-grid-hd-cell-checkbox");
                if (r.hcell)
                {
                    var columnid = r.hcell.id.split('|')[2];
                    r.column = g._columns[columnid];
                }
            }
            if (r.gridbody)
            {
                r.row = fn("l-grid-row");
                r.cell = fn("l-grid-row-cell");
                r.checkbox = fn("l-grid-row-cell-btn-checkbox");
                r.groupbtn = fn("l-grid-group-togglebtn");
                r.grouprow = fn("l-grid-grouprow");
                r.detailbtn = fn("l-grid-row-cell-detailbtn");
                r.detailrow = fn("l-grid-detailpanel");
                r.totalrow = fn("l-grid-totalsummary");
                r.totalcell = fn("l-grid-totalsummary-cell");
                r.rownumberscell = $(r.cell).hasClass("l-grid-row-cell-rownumbers") ? r.cell : null;
                r.detailcell = $(r.cell).hasClass("l-grid-row-cell-detail") ? r.cell : null;
                r.checkboxcell = $(r.cell).hasClass("l-grid-row-cell-checkbox") ? r.cell : null;
                r.treelink = fn("l-grid-tree-link");
                r.editor = fn("l-grid-editor");
                if (r.row) r.data = this._getRowByDomId(r.row.id);
                if (r.cell) r.editing = $(r.cell).hasClass("l-grid-row-cell-editing");
                if (r.editor) r.editing = true;
                if (r.editing) r.out = false;
            }
            if (r.toolbar)
            {
                r.first = fn("l-bar-btnfirst");
                r.last = fn("l-bar-btnlast");
                r.next = fn("l-bar-btnnext");
                r.prev = fn("l-bar-btnprev");
                r.load = fn("l-bar-btnload");
                r.button = fn("l-bar-button");
            }

            return r;
        },
        _setEvent: function ()
        {
            var g = this, p = this.options;
            g.grid.bind("mousedown.grid", function (e)
            {
                g._onMouseDown.call(g, e);
            });
            g.grid.bind("dblclick.grid", function (e)
            {
                g._onDblClick.call(g, e);
            });
            g.grid.bind("contextmenu.grid", function (e)
            {
                return g._onContextmenu.call(g, e);
            });
            $(document).bind("mouseup.grid", function (e)
            {
                g._onMouseUp.call(g, e);
            });
            $(document).bind("click.grid", function (e)
            {
                g._onClick.call(g, e);
            });
            $(window).bind("resize.grid", function (e)
            {
                g._onResize.call(g);
            });
            $(document).bind("keydown.grid", function (e)
            {
                if (e.ctrlKey) g.ctrlKey = true;
            });
            $(document).bind("keyup.grid", function (e)
            {
                delete g.ctrlKey;
            });
            //表体 - 滚动联动事件 
            g.gridbody.bind('scroll.grid', function ()
            {
                var scrollLeft = g.gridbody.scrollLeft();
                var scrollTop = g.gridbody.scrollTop();
                if (scrollLeft != null)
                    g.gridheader[0].scrollLeft = scrollLeft;
                if (scrollTop != null)
                    g.f.gridbody[0].scrollTop = scrollTop;
                g.trigger('SysGridHeightChanged');
            });
            //工具条 - 切换每页记录数事件
            $('select', g.toolbar).change(function ()
            {
                if (g.isDataChanged && !confirm(p.isContinueByDataChanged))
                    return false;
                p.newPage = 1;
                p.pageSize = this.value;
                g.loadData(p.where);
            });
            //工具条 - 切换当前页事件
            $('span.pcontrol :text', g.toolbar).blur(function (e)
            {
                g.changePage('input');
            });
            $("div.l-bar-button", g.toolbar).hover(function ()
            {
                $(this).addClass("l-bar-button-over");
            }, function ()
            {
                $(this).removeClass("l-bar-button-over");
            });
            //列拖拽支持
            if ($.fn.ligerDrag && p.colDraggable)
            {
                g.colDroptip = $("<div class='l-drag-coldroptip' style='display:none'><div class='l-drop-move-up'></div><div class='l-drop-move-down'></div></div>").appendTo('body');
                g.gridheader.add(g.f.gridheader).ligerDrag({ revert: true, animate: false,
                    proxyX: 0, proxyY: 0,
                    proxy: function (draggable, e)
                    {
                        var src = g._getSrcElementByEvent(e);
                        if (src.hcell && src.column)
                        {
                            var content = $(".l-grid-hd-cell-text:first", src.hcell).html();
                            var proxy = $("<div class='l-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div></div>").appendTo('body');
                            proxy.append(content);
                            return proxy;
                        }
                    },
                    onRevert: function () { return false; },
                    onRendered: function ()
                    {
                        this.set('cursor', 'default');
                        g.children[this.id] = this;
                    },
                    onStartDrag: function (current, e)
                    {
                        if (e.button == 2) return false;
                        if (g.colresizing) return false;
                        this.set('cursor', 'default');
                        var src = g._getSrcElementByEvent(e);
                        if (!src.hcell || !src.column || src.column.issystem || src.hcelltext) return false;
                        if ($(src.hcell).css('cursor').indexOf('resize') != -1) return false;
                        this.draggingColumn = src.column;
                        g.coldragging = true;

                        var gridOffset = g.grid.offset();
                        this.validRange = {
                            top: gridOffset.top,
                            bottom: gridOffset.top + g.gridheader.height(),
                            left: gridOffset.left - 10,
                            right: gridOffset.left + g.grid.width() + 10
                        };
                    },
                    onDrag: function (current, e)
                    {
                        this.set('cursor', 'default');
                        var column = this.draggingColumn;
                        if (!column) return false;
                        if (g.colresizing) return false;
                        if (g.colDropIn == null)
                            g.colDropIn = -1;
                        var pageX = e.pageX;
                        var pageY = e.pageY;
                        var visit = false;
                        var gridOffset = g.grid.offset();
                        var validRange = this.validRange;
                        if (pageX < validRange.left || pageX > validRange.right
                            || pageY > validRange.bottom || pageY < validRange.top)
                        {
                            g.colDropIn = -1;
                            g.colDroptip.hide();
                            this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes").addClass("l-drop-no");
                            return;
                        }
                        for (var colid in g._columns)
                        {
                            var col = g._columns[colid];
                            if (column == col)
                            {
                                visit = true;
                                continue;
                            }
                            if (col.issystem) continue;
                            var sameLevel = col['__level'] == column['__level'];
                            var isAfter = !sameLevel ? false : visit ? true : false;
                            if (column.frozen != col.frozen) isAfter = col.frozen ? false : true;
                            if (g.colDropIn != -1 && g.colDropIn != colid) continue;
                            var cell = document.getElementById(col['__domid']);
                            var offset = $(cell).offset();
                            var range = {
                                top: offset.top,
                                bottom: offset.top + $(cell).height(),
                                left: offset.left - 10,
                                right: offset.left + 10
                            };
                            if (isAfter)
                            {
                                var cellwidth = $(cell).width();
                                range.left += cellwidth;
                                range.right += cellwidth;
                            }
                            if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                            {
                                var height = 23;
                                if (col['__rowSpan']) height *= col['__rowSpan'];
                                g.colDroptip.css({
                                    left: range.left + 5,
                                    top: range.top - 9,
                                    height: height + 9 * 2
                                }).show();
                                g.colDropIn = colid;
                                g.colDropDir = isAfter ? "right" : "left";
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no").addClass("l-drop-yes");
                                break;
                            }
                            else if (g.colDropIn != -1)
                            {
                                g.colDropIn = -1;
                                g.colDroptip.hide();
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes").addClass("l-drop-no");
                            }
                        }
                    },
                    onStopDrag: function (current, e)
                    {
                        var column = this.draggingColumn;
                        g.coldragging = false;
                        if (g.colDropIn != -1)
                        {
                            g.changeCol.ligerDefer(g, 0, [column, g.colDropIn, g.colDropDir == "right"]);
                            g.colDropIn = -1;
                        }
                        g.colDroptip.hide();
                        this.set('cursor', 'default');
                    }
                });
            }
            //行拖拽支持
            if ($.fn.ligerDrag && p.rowDraggable)
            {
                g.rowDroptip = $("<div class='l-drag-rowdroptip' style='display:none'></div>").appendTo('body');
                g.gridbody.add(g.f.gridbody).ligerDrag({ revert: true, animate: false,
                    proxyX: 0, proxyY: 0,
                    proxy: function (draggable, e)
                    {
                        var src = g._getSrcElementByEvent(e);
                        if (src.row)
                        {
                            var content = p.draggingMessage.replace(/{count}/, draggable.draggingRows ? draggable.draggingRows.length : 1);
                            if (p.rowDraggingRender)
                            {
                                content = p.rowDraggingRender(draggable.draggingRows, draggable, g);
                            }
                            var proxy = $("<div class='l-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div>" + content + "</div>").appendTo('body');
                            return proxy;
                        }
                    },
                    onRevert: function () { return false; },
                    onRendered: function ()
                    {
                        this.set('cursor', 'default');
                        g.children[this.id] = this;
                    },
                    onStartDrag: function (current, e)
                    {
                        if (e.button == 2) return false;
                        if (g.colresizing) return false;
                        if (!g.columns.length) return false;
                        this.set('cursor', 'default');
                        var src = g._getSrcElementByEvent(e);
                        if (!src.cell || !src.data || src.checkbox) return false;
                        var ids = src.cell.id.split('|');
                        var column = g._columns[ids[ids.length - 1]];
                        if (src.rownumberscell || src.detailcell || src.checkboxcell || column == g.columns[0])
                        {
                            if (g.enabledCheckbox())
                            {
                                this.draggingRows = g.getSelecteds();
                                if (!this.draggingRows || !this.draggingRows.length) return false;
                            }
                            else
                            {
                                this.draggingRows = [src.data];
                            }
                            this.draggingRow = src.data;
                            this.set('cursor', 'move');
                            g.rowdragging = true;
                            this.validRange = {
                                top: g.gridbody.offset().top,
                                bottom: g.gridbody.offset().top + g.gridbody.height(),
                                left: g.grid.offset().left - 10,
                                right: g.grid.offset().left + g.grid.width() + 10
                            };
                        }
                        else
                        {
                            return false;
                        }
                    },
                    onDrag: function (current, e)
                    {
                        var rowdata = this.draggingRow;
                        if (!rowdata) return false;
                        var rows = this.draggingRows ? this.draggingRows : [rowdata];
                        if (g.colresizing) return false;
                        if (g.rowDropIn == null) g.rowDropIn = -1;
                        var pageX = e.pageX;
                        var pageY = e.pageY;
                        var visit = false;
                        var validRange = this.validRange;
                        if (pageX < validRange.left || pageX > validRange.right
                            || pageY > validRange.bottom || pageY < validRange.top)
                        {
                            g.rowDropIn = -1;
                            g.rowDroptip.hide();
                            this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes l-drop-add").addClass("l-drop-no");
                            return;
                        }
                        for (var i in g.rows)
                        {
                            var rd = g.rows[i];
                            var rowid = rd['__id'];
                            if (rowdata == rd) visit = true;
                            if ($.inArray(rd, rows) != -1) continue;
                            var isAfter = visit ? true : false;
                            if (g.rowDropIn != -1 && g.rowDropIn != rowid) continue;
                            var rowobj = g.getRowObj(rowid);
                            var offset = $(rowobj).offset();
                            var range = {
                                top: offset.top - 4,
                                bottom: offset.top + $(rowobj).height() + 4,
                                left: g.grid.offset().left,
                                right: g.grid.offset().left + g.grid.width()
                            };
                            if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                            {
                                var lineTop = offset.top;
                                if (isAfter) lineTop += $(rowobj).height();
                                g.rowDroptip.css({
                                    left: range.left,
                                    top: lineTop,
                                    width: range.right - range.left
                                }).show();
                                g.rowDropIn = rowid;
                                g.rowDropDir = isAfter ? "bottom" : "top";
                                if (p.tree && pageY > range.top + 5 && pageY < range.bottom - 5)
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-yes").addClass("l-drop-add");
                                    g.rowDroptip.hide();
                                    g.rowDropInParent = true;
                                }
                                else
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-add").addClass("l-drop-yes");
                                    g.rowDroptip.show();
                                    g.rowDropInParent = false;
                                }
                                break;
                            }
                            else if (g.rowDropIn != -1)
                            {
                                g.rowDropIn = -1;
                                g.rowDropInParent = false;
                                g.rowDroptip.hide();
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes  l-drop-add").addClass("l-drop-no");
                            }
                        }
                    },
                    onStopDrag: function (current, e)
                    {
                        var rows = this.draggingRows;
                        g.rowdragging = false;
                        for (var i = 0; i < rows.length; i++)
                        {
                            var children = rows[i].children;
                            if (children)
                            {
                                rows = $.grep(rows, function (node, i)
                                {
                                    var isIn = $.inArray(node, children) == -1;
                                    return isIn;
                                });
                            }
                        }
                        if (g.rowDropIn != -1)
                        {
                            if (p.tree)
                            {
                                var neardata, prow;
                                if (g.rowDropInParent)
                                {
                                    prow = g.getRow(g.rowDropIn);
                                }
                                else
                                {
                                    neardata = g.getRow(g.rowDropIn);
                                    prow = g.getParent(neardata);
                                }
                                g.appendRange(rows, prow, neardata, g.rowDropDir != "bottom");
                                g.trigger('rowDragDrop', {
                                    rows: rows,
                                    parent: prow,
                                    near: neardata,
                                    after: g.rowDropDir == "bottom"
                                });
                            }
                            else
                            {
                                g.moveRange(rows, g.rowDropIn, g.rowDropDir == "bottom");
                                g.trigger('rowDragDrop', {
                                    rows: rows,
                                    parent: prow,
                                    near: g.getRow(g.rowDropIn),
                                    after: g.rowDropDir == "bottom"
                                });
                            }

                            g.rowDropIn = -1;
                        }
                        g.rowDroptip.hide();
                        this.set('cursor', 'default');
                    }
                });
            }
        },
        _onRowOver: function (rowParm, over)
        {
            if (l.draggable.dragging) return;
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var methodName = over ? "addClass" : "removeClass";
            if (g.enabledFrozen())
                $(g.getRowObj(rowdata, true))[methodName](p.mouseoverRowCssClass);
            $(g.getRowObj(rowdata, false))[methodName](p.mouseoverRowCssClass);
        },
        _onMouseUp: function (e)
        {
            var g = this, p = this.options;
            if (l.draggable.dragging)
            {
                var src = g._getSrcElementByEvent(e);

                //drop in header cell
                if (src.hcell && src.column)
                {
                    g.trigger('dragdrop', [{ type: 'header', column: src.column, cell: src.hcell }, e]);
                }
                else if (src.row)
                {
                    g.trigger('dragdrop', [{ type: 'row', record: src.data, row: src.row }, e]);
                }
            }
        },
        _onMouseDown: function (e)
        {
            var g = this, p = this.options;
        },
        _onContextmenu: function (e)
        {
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.row)
            {
                if (p.whenRClickToSelect)
                    g.select(src.data);
                if (g.hasBind('contextmenu'))
                {
                    return g.trigger('contextmenu', [{ data: src.data, rowindex: src.data['__index'], row: src.row }, e]);
                }
            }
            else if (src.hcell)
            {
                if (!p.allowHideColumn) return true;
                var columnindex = $(src.hcell).attr("columnindex");
                if (columnindex == undefined) return true;
                var left = (e.pageX - g.body.offset().left + parseInt(g.body[0].scrollLeft));
                if (columnindex == g.columns.length - 1) left -= 50;
                g.popup.css({ left: left, top: g.gridheader.height() + 1 });
                g.popup.toggle();
                return false;
            }
        },
        _onDblClick: function (e)
        {
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.row)
            {
                g.trigger('dblClickRow', [src.data, src.data['__id'], src.row]);
            }
        },
        _onClick: function (e)
        {
            var obj = (e.target || e.srcElement);
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.out)
            {
                if (g.editor.editing && !$.ligerui.win.masking) g.endEdit();
                if (p.allowHideColumn) g.popup.hide();
                return;
            }
            if (src.indetail || src.editing)
            {
                return;
            }
            if (g.editor.editing)
            {
                g.endEdit();
            }
            if (p.allowHideColumn)
            {
                if (!src.popup)
                {
                    g.popup.hide();
                }
            }
            if (src.checkboxall) //复选框全选
            {
                var row = $(src.hrow);
                var uncheck = row.hasClass("l-checked");
                if (g.trigger('beforeCheckAllRow', [!uncheck, g.element]) == false) return false;
                if (uncheck)
                {
                    row.removeClass("l-checked");
                }
                else
                {
                    row.addClass("l-checked");
                }
                g.selected = [];
                for (var rowid in g.records)
                {
                    if (uncheck)
                        g.unselect(g.records[rowid]);
                    else
                        g.select(g.records[rowid]);
                }
                g.trigger('checkAllRow', [!uncheck, g.element]);
            }
            else if (src.hcelltext) //排序
            {
                var hcell = $(src.hcelltext).parent().parent();
                if (!p.enabledSort || !src.column) return;
                if (src.column.isSort == false) return;
                if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged)) return;
                var sort = $(".l-grid-hd-cell-sort:first", hcell);
                var columnName = src.column.name;
                if (!columnName) return;
                if (sort.length > 0)
                {
                    if (sort.hasClass("l-grid-hd-cell-sort-asc"))
                    {
                        sort.removeClass("l-grid-hd-cell-sort-asc").addClass("l-grid-hd-cell-sort-desc");
                        hcell.removeClass("l-grid-hd-cell-asc").addClass("l-grid-hd-cell-desc");
                        g.changeSort(columnName, 'desc');
                    }
                    else if (sort.hasClass("l-grid-hd-cell-sort-desc"))
                    {
                        sort.removeClass("l-grid-hd-cell-sort-desc").addClass("l-grid-hd-cell-sort-asc");
                        hcell.removeClass("l-grid-hd-cell-desc").addClass("l-grid-hd-cell-asc");
                        g.changeSort(columnName, 'asc');
                    }
                }
                else
                {
                    hcell.removeClass("l-grid-hd-cell-desc").addClass("l-grid-hd-cell-asc");
                    $(src.hcelltext).after("<span class='l-grid-hd-cell-sort l-grid-hd-cell-sort-asc'>&nbsp;&nbsp;</span>");
                    g.changeSort(columnName, 'asc');
                }
                $(".l-grid-hd-cell-sort", g.gridheader).add($(".l-grid-hd-cell-sort", g.f.gridheader)).not($(".l-grid-hd-cell-sort:first", hcell)).remove();
            }
            //明细
            else if (src.detailbtn && p.detail)
            {
                var item = src.data;
                var row = $([g.getRowObj(item, false)]);
                if (g.enabledFrozen()) row = row.add(g.getRowObj(item, true));
                var rowid = item['__id'];
                if ($(src.detailbtn).hasClass("l-open"))
                {
                    if (p.detail.onCollapse)
                        p.detail.onCollapse(item, $(".l-grid-detailpanel-inner:first", nextrow)[0]);
                    row.next("tr.l-grid-detailpanel").hide();
                    $(src.detailbtn).removeClass("l-open");
                }
                else
                {
                    var nextrow = row.next("tr.l-grid-detailpanel");
                    if (nextrow.length > 0)
                    {
                        nextrow.show();
                        if (p.detail.onExtend)
                            p.detail.onExtend(item, $(".l-grid-detailpanel-inner:first", nextrow)[0]);
                        $(src.detailbtn).addClass("l-open");
                        g.trigger('SysGridHeightChanged');
                        return;
                    }
                    $(src.detailbtn).addClass("l-open");
                    var frozenColNum = 0;
                    for (var i = 0; i < g.columns.length; i++)
                        if (g.columns[i].frozen) frozenColNum++;
                    var detailRow = $("<tr class='l-grid-detailpanel'><td><div class='l-grid-detailpanel-inner' style='display:none'></div></td></tr>");
                    var detailFrozenRow = $("<tr class='l-grid-detailpanel'><td><div class='l-grid-detailpanel-inner' style='display:none'></div></td></tr>");
                    detailRow.attr("id", g.id + "|detail|" + rowid);
                    g.detailrows = g.detailrows || [];
                    g.detailrows.push(detailRow[0]);
                    g.detailrows.push(detailFrozenRow[0]);
                    var detailRowInner = $("div:first", detailRow);
                    detailRowInner.parent().attr("colSpan", g.columns.length - frozenColNum);
                    row.eq(0).after(detailRow);
                    if (frozenColNum > 0)
                    {
                        detailFrozenRow.find("td:first").attr("colSpan", frozenColNum);
                        row.eq(1).after(detailFrozenRow);
                    }
                    if (p.detail.onShowDetail)
                    {
                        p.detail.onShowDetail(item, detailRowInner[0], function ()
                        {
                            g.trigger('SysGridHeightChanged');
                        });
                        $("div:first", detailFrozenRow).add(detailRowInner).show().height(p.detail.height || p.detailHeight);
                    }
                    else if (p.detail.render)
                    {
                        detailRowInner.append(p.detail.render());
                        detailRowInner.show();
                    }
                    g.trigger('SysGridHeightChanged');
                }
            }
            else if (src.groupbtn)
            {
                var grouprow = $(src.grouprow);
                var opening = true;
                if ($(src.groupbtn).hasClass("l-grid-group-togglebtn-close"))
                {
                    $(src.groupbtn).removeClass("l-grid-group-togglebtn-close");

                    if (grouprow.hasClass("l-grid-grouprow-last"))
                    {
                        $("td:first", grouprow).width('auto');
                    }
                }
                else
                {
                    opening = false;
                    $(src.groupbtn).addClass("l-grid-group-togglebtn-close");
                    if (grouprow.hasClass("l-grid-grouprow-last"))
                    {
                        $("td:first", grouprow).width(g.gridtablewidth);
                    }
                }
                var currentRow = grouprow.next(".l-grid-row,.l-grid-totalsummary-group,.l-grid-detailpanel");
                while (true)
                {
                    if (currentRow.length == 0) break;
                    if (opening)
                    {
                        currentRow.show();
                        //如果是明细展开的行，并且之前的状态已经是关闭的，隐藏之
                        if (currentRow.hasClass("l-grid-detailpanel") && !currentRow.prev().find("td.l-grid-row-cell-detail:first span.l-grid-row-cell-detailbtn:first").hasClass("l-open"))
                        {
                            currentRow.hide();
                        }
                    }
                    else
                    {
                        currentRow.hide();
                    }
                    currentRow = currentRow.next(".l-grid-row,.l-grid-totalsummary-group,.l-grid-detailpanel");
                }
                g.trigger('SysGridHeightChanged');
            }
            //树 - 伸展/收缩节点
            else if (src.treelink)
            {
                g.toggle(src.data);
            }
            else if (src.row && g.enabledCheckbox()) //复选框选择行
            {
                //复选框
                var selectRowButtonOnly = p.selectRowButtonOnly ? true : false;
                if (p.enabledEdit) selectRowButtonOnly = true;
                if (src.checkbox || !selectRowButtonOnly)
                {
                    var row = $(src.row);
                    var uncheck = row.hasClass("l-selected");
                    if (g.trigger('beforeCheckRow', [!uncheck, src.data, src.data['__id'], src.row]) == false)
                        return false;
                    var met = uncheck ? 'unselect' : 'select';
                    g[met](src.data);
                    if (p.tree && p.autoCheckChildren)
                    {
                        var children = g.getChildren(src.data, true);
                        for (var i = 0, l = children.length; i < l; i++)
                        {
                            g[met](children[i]);
                        }
                    }
                    g.trigger('checkRow', [!uncheck, src.data, src.data['__id'], src.row]);
                }
                if (!src.checkbox && src.cell && p.enabledEdit && p.clickToEdit)
                {
                    g._applyEditor(src.cell);
                }
            }
            else if (src.row && !g.enabledCheckbox())
            {
                if (src.cell && p.enabledEdit && p.clickToEdit)
                {
                    g._applyEditor(src.cell);
                }

                //选择行
                if ($(src.row).hasClass("l-selected"))
                {
                    if (!p.allowUnSelectRow)
                    {
                        $(src.row).addClass("l-selected-again");
                        return;
                    }
                    g.unselect(src.data);
                }
                else
                {
                    g.select(src.data);
                }
            }
            else if (src.toolbar)
            {
                if (src.first)
                {
                    if (g.trigger('toFirst', [g.element]) == false) return false;
                    g.changePage('first');
                }
                else if (src.prev)
                {
                    if (g.trigger('toPrev', [g.element]) == false) return false;
                    g.changePage('prev');
                }
                else if (src.next)
                {
                    if (g.trigger('toNext', [g.element]) == false) return false;
                    g.changePage('next');
                }
                else if (src.last)
                {
                    if (g.trigger('toLast', [g.element]) == false) return false;
                    g.changePage('last');
                }
                else if (src.load)
                {
                    if ($("span", src.load).hasClass("l-disabled")) return false;
                    if (g.trigger('reload', [g.element]) == false) return false;
                    if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                        return false;
                    g.loadData(p.where);
                }
            }
        },
        select: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var rowid = rowdata['__id'];
            var rowobj = g.getRowObj(rowid);
            var rowobj1 = g.getRowObj(rowid, true);
            if (!g.enabledCheckbox() && !g.ctrlKey) //单选
            {
                for (var i in g.selected)
                {
                    var o = g.selected[i];
                    if (o['__id'] in g.records)
                    {
                        $(g.getRowObj(o)).removeClass("l-selected l-selected-again");
                        if (g.enabledFrozen())
                            $(g.getRowObj(o, true)).removeClass("l-selected l-selected-again");
                    }
                }
                g.selected = [];
            }
            if (rowobj) $(rowobj).addClass("l-selected");
            if (rowobj1) $(rowobj1).addClass("l-selected");
            g.selected[g.selected.length] = rowdata;
            g.trigger('selectRow', [rowdata, rowid, rowobj]);
        },
        unselect: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var rowid = rowdata['__id'];
            var rowobj = g.getRowObj(rowid);
            var rowobj1 = g.getRowObj(rowid, true);
            $(rowobj).removeClass("l-selected l-selected-again");
            if (g.enabledFrozen())
                $(rowobj1).removeClass("l-selected l-selected-again");
            g._removeSelected(rowdata);
            g.trigger('unSelectRow', [rowdata, rowid, rowobj]);
        },
        isSelected: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            for (var i in g.selected)
            {
                if (g.selected[i] == rowdata) return true;
            }
            return false;
        },
        _onResize: function ()
        {
            var g = this, p = this.options;
            if (p.height && p.height != 'auto')
            {
                var windowHeight = $(window).height();
                //if(g.windowHeight != undefined && g.windowHeight == windowHeight) return;

                var h = 0;
                var parentHeight = null;
                if (typeof (p.height) == "string" && p.height.indexOf('%') > 0)
                {
                    var gridparent = g.grid.parent();
                    if (p.InWindow)
                    {
                        parentHeight = windowHeight;
                        parentHeight -= parseInt($('body').css('paddingTop'));
                        parentHeight -= parseInt($('body').css('paddingBottom'));
                    }
                    else
                    {
                        parentHeight = gridparent.height();
                    }
                    h = parentHeight * parseFloat(p.height) * 0.01;
                    if (p.InWindow || gridparent[0].tagName.toLowerCase() == "body")
                        h -= (g.grid.offset().top - parseInt($('body').css('paddingTop')));
                }
                else
                {
                    h = parseInt(p.height);
                }

                h += p.heightDiff;
                g.windowHeight = windowHeight;
                g._setHeight(h);
            }
            if (g.enabledFrozen())
            {
                var gridView1Width = g.gridview1.width();
                var gridViewWidth = g.gridview.width()
                g.gridview2.css({
                    width: gridViewWidth - gridView1Width
                });
            }
            g.trigger('SysGridHeightChanged');
        }
    });

    $.ligerui.controls.Grid.prototype.enabledTotal = $.ligerui.controls.Grid.prototype.isTotalSummary;
    $.ligerui.controls.Grid.prototype.add = $.ligerui.controls.Grid.prototype.addRow;
    $.ligerui.controls.Grid.prototype.update = $.ligerui.controls.Grid.prototype.updateRow;
    $.ligerui.controls.Grid.prototype.append = $.ligerui.controls.Grid.prototype.appendRow;
    $.ligerui.controls.Grid.prototype.getSelected = $.ligerui.controls.Grid.prototype.getSelectedRow;
    $.ligerui.controls.Grid.prototype.getSelecteds = $.ligerui.controls.Grid.prototype.getSelectedRows;
    $.ligerui.controls.Grid.prototype.getCheckedRows = $.ligerui.controls.Grid.prototype.getSelectedRows;
    $.ligerui.controls.Grid.prototype.getCheckedRowObjs = $.ligerui.controls.Grid.prototype.getSelectedRowObjs;
    $.ligerui.controls.Grid.prototype.setOptions = $.ligerui.controls.Grid.prototype.set;

})(jQuery);