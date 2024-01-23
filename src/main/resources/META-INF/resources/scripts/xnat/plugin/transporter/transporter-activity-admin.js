/*!
 * Transporter admin functions
 */

console.debug('transporter-activity-admin.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.transporter = getObject(XNAT.plugin.transporter || {});

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {

    console.log('transporter-activity-admin.js');
    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    var undefined,
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
        csrfUrl = XNAT.url.csrfUrl;

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error: ' + title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': ' + e.statusText + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function () {
                        if (closeAll) {
                            xmodal.closeAll();
                            XNAT.ui.dialog.closeAll();
                        }
                    }
                }
            ]
        });
    }

    /* ================= *
     * TransportActivity *
     * ================= */

    var activityTable, transportActivity, wrapperList;

    XNAT.plugin.transporter.activityTable = activityTable =
        getObject(XNAT.plugin.transporter.activityTable || {});

    XNAT.plugin.transporter.transportActivity = transportActivity =
        getObject(XNAT.plugin.transporter.transportActivity || {});

    //must exist within a div.tab-container and have class data-table-container
    const activityTableContainerId = 'transporter-activity-container';

    function viewActivityDialog(e, onclose) {
        e.preventDefault();
        var activityId = $(this).data('id') || $(this).closest('tr').prop('title');
        XNAT.plugin.transporter.activityTable.viewActivity(activityId);
    }

    const labelMap = {
        // id:             {label: 'ID', op: 'eq', type: 'number', show: false},
        timestamp:  {label: 'Date', column: 'timestamp', show: true},
        sessionId:    {label: 'Transport ID', column: 'sessionId', show: true},
        username:       {label: 'User', column: 'username', show: true},
        snapshotId:   {label: 'Snapshot ID', column: 'snapshotId', show: true},
        //actions:     {label: 'Actions', column: 'actions', show: true}
    };

    function activityTableObject() {
        return {
            table: {
                classes: "compact fixed-header selectable scrollable-table",
                style: "width: auto;",
                on: [
                    ['click', 'a.view-activity', viewActivityDialog]
                ]
            },
            //before: {
                //TODO - fix this for transporter page
                //filterCss: {
                //    tag: 'style|type=text/css',
                //    content:
                //        '#' + activityTableContainerId + ' .DATE { width: 160px !important; } \n' +
                //        '#' + activityTableContainerId + ' .command { width: 210px !important; }  \n' +
                //        '#' + activityTableContainerId + ' .username { width: 120px !important; }  \n' +
                //        '#' + activityTableContainerId + ' .ROOTELEMENT {width: 180px !important; }' +
                //        '#' + activityTableContainerId + ' .status { width: 130px !important; }  \n'
                //}
            //},
            sortable: 'timestamp, sessionId, username, snapshotId',
            filter: 'sessionId, username, snapshotId',
            items: {
                // id: {
                //     th: {className: 'id'},
                //     label: labelMap.id['label'],
                //     apply: function(){
                //         return this.id.toString();
                //     }
                // },
                timestamp: {
                    label: labelMap.timestamp['label'],
                    th: {className: 'DATE'},
                    apply: function () {
                        let timestamp = this['timestamp'];
                        let dateString = '';
                        if (timestamp) {
                            timestamp = timestamp.replace(/-/g, '/'); // include date format hack for Safari
                            if (timestamp.indexOf('UTC') < 0) {
                                timestamp = timestamp.trim() + ' UTC';
                            }
                            dateString = (new Date(timestamp)).toLocaleString();
                            dateString = dateString.replace(', ','<br>');

                        } else {
                            dateString = 'N/A';
                        }
                        return dateString;
                    }
                },
                sessionId: {
                    th: {className: 'sessionId'},
                    label: labelMap.sessionId['label'],
                    apply: function () {
                        return this['sessionId']
                    }
                    //apply: function () {
                    //    var message = '';
                    //    if (isObject(this['trigger']) && this['trigger']) {
                    //        message = message + '<br>Trigger: ' + ((this['trigger']['event-name'] && this['trigger']['event-name'] == "Scheduled Event")
                    //            ? this['trigger']['event-name'] : this['trigger']['label']);
                    //    }
                    //    return spawn('!',[
                    //        spawn('a.view-activity-detail', {
                    //            href: '#!',
                    //            title: 'View event details',
                    //            data: {'id': this.id},
                    //            style: { wordWrap: 'break-word', fontWeight: 'bold' },
                    //            html: this['sessionId']['name']
                    //        }),
                    //        spawn('span',message)
                    //    ]);
                    //},
                },
                username: {
                    th: {className: 'username'},
                    label: labelMap.username['label'],
                    apply: function () {
                        return this['username']
                    }
                },
                snapshotId: {
                    th: {className: 'snapshotId'},
                    td: {className: 'snapshotId word-wrapped'},
                    label: labelMap.snapshotId['label'],
                    apply: function(){
                        return this['label'];
                    }
                }
            }
        }
    }

    //activityTable.workflowModal = function(workflowIdOrEvent) {
    //    var workflowId;
    //    if (workflowIdOrEvent.hasOwnProperty("data")) {
    //        // this is an event
    //        workflowId = workflowIdOrEvent.data.wfid;
    //    } else {
    //        workflowId = workflowIdOrEvent;
    //    }
    //    // rptModal in xdat.js
    //    rptModal.call(this, workflowId, "wrk:workflowData", "wrk:workflowData.wrk_workflowData_id");
    //};

    var containerModalId = function(containerId, logFile) {
        return 'container-'+containerId+'-log-'+logFile;
    };

    //var checkContinueLiveLog = function(containerId, logFile, refreshLogSince, bytesRead) {
    //    // This will stop making ajax requests until the user clicks "continue"
    //    // thus allowing the session timeout to handle an expiring session
    //    XNAT.dialog.open({
    //        width: 360,
    //        content: '' +
    //            '<div style="font-size:14px;">' +
    //            'Are you still watching this log?' +
    //            '<br><br>'+
    //            'Click <b>"Continue"</b> to continue tailing the log ' +
    //            'or <b>"Close"</b> to close it.' +
    //            '</div>',
    //        buttons: [
    //            {
    //                label: 'Close',
    //                close: true,
    //                action: function(){
    //                    XNAT.dialog.closeAll();
    //                }
    //            },
    //            {
    //                label: 'Continue',
    //                isDefault: true,
    //                close: true,
    //                action: function(){
    //                    refreshLog(containerId, logFile, refreshLogSince, bytesRead);
    //                }
    //            }
    //        ]
    //    });
    //};

    activityTable.$loadAllBtn = false;
    //activityTable.refreshLog = refreshLog = function(containerId, logFile, refreshLogSince, bytesRead, loadAll, startTime) {
    //    var fullWait;
    //    var refreshPrm = {};
    //    if (refreshLogSince) refreshPrm.since = refreshLogSince;
    //    if (bytesRead) refreshPrm.bytesRead = bytesRead;
    //    if (loadAll) {
    //        fullWait = XNAT.ui.dialog.static.wait('Fetching log, please wait.');
    //        refreshPrm.loadAll = loadAll;
    //    }
//
    //    var firstRun = $.isEmptyObject(refreshPrm);
//
    //    if (!startTime) {
    //        startTime = new Date();
    //    } else {
    //        // Check uptime
    //        var maxUptime = 900; //sec
    //        var uptime = Math.round((new Date() - startTime)/1000);
    //        if (uptime >= maxUptime) {
    //            checkContinueLiveLog(containerId, logFile, refreshLogSince, bytesRead);
    //            return;
    //        }
    //    }
//
    //    var $container = activityTable.logModal.content$.parent();
//
    //    // Functions for adding log content to modal
    //    function appendContent(content, clear) {
    //        if (firstRun || clear) activityTable.logModal.content$.empty();
    //        var lines = content.split('\n').filter(function(line){return line;}); // remove empty lines
    //        if (lines.length > 0) {
    //            activityTable.logModal.content$.append(spawn('pre',
    //                {'style': {'font-size':'12px','margin':'0', 'white-space':'pre-wrap'}}, lines.join('<br/>')));
    //        }
    //    }

        //function addLiveLoggingContent(dataJson) {
        //    // We're live logging
        //    var currentScrollPos = $container.scrollTop(),
        //        containerHeight = $container[0].scrollHeight,
        //        autoScroll = $container.height() + currentScrollPos >= containerHeight; //user has not scrolled
//
        //    //append content
        //    appendContent(dataJson.content);
//
        //    //scroll to bottom
        //    if (autoScroll) $container.scrollTop($container[0].scrollHeight);
//
        //    if (dataJson.timestamp !== -1) {
        //        // Container is still running, check for more!
        //        refreshLog(containerId, logFile, dataJson.timestamp, false, false, startTime);
        //    }
        //}

        //function removeLoadAllBtn() {
        //    if (activityTable.$loadAllBtn) {
        //        activityTable.$loadAllBtn.remove();
        //        activityTable.$loadAllBtn = false;
        //    }
        //}
//
        //function addLoadAllBtn(curBytesRead) {
        //    removeLoadAllBtn();
        //    activityTable.$loadAllBtn = $('<button class="button btn" id="load-log">Load entire log file</button>');
        //    activityTable.$loadAllBtn.appendTo(activityTable.logModal.footerButtons$);
        //    activityTable.$loadAllBtn.click(function(){
        //        $container.off("scroll");
        //        $container.scrollTop($container[0].scrollHeight);
        //        refreshLog(containerId, logFile, false, curBytesRead, true);
        //    });
        //}
//
        //function startScrolling(curBytesRead) {
        //    $container.scroll(function() {
        //        if ($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
        //            $container.off("scroll");
        //            addLoadAllBtn(curBytesRead);
        //            refreshLog(containerId, logFile, false, curBytesRead);
        //        }
        //    });
        //}

        //function addFileContent(dataJson, clear) {
        //    appendContent(dataJson.content, clear);
        //    if (dataJson.bytesRead === -1) {
        //        // File read in its entirety
        //        removeLoadAllBtn();
        //    } else {
        //        startScrolling(dataJson.bytesRead);
        //    }
        //}

        //var $waitElement = $('<span class="spinner text-info"><i class="fa fa-spinner fa-spin"></i> Loading...</span>');
        //XNAT.xhr.getJSON({
        //    url: rootUrl('/xapi/containers/' + containerId + '/logSince/' + logFile),
        //    data: refreshPrm,
        //    beforeSend: function () {
        //        if (firstRun || bytesRead) $waitElement.appendTo(activityTable.logModal.content$);
        //    },
        //    success: function (dataJson) {
        //        if (firstRun || bytesRead) $waitElement.remove();
        //        if (fullWait) {
        //            fullWait.close();
        //        }
//
        //        // Ensure that user didn't close modal
        //        if ($container.length === 0 || $container.is(':hidden')) {
        //            return;
        //        }
//
        //        var fromFile = dataJson.fromFile;
        //        if (fromFile) {
        //            // file content
        //            var emptyFirst = false;
        //            if (firstRun) {
        //                activityTable.logModal.title$.text(activityTable.logModal.title$.text() + ' (from file)');
        //            } else if (refreshLogSince) {
        //                // We were live logging, but we swapped to reading a file, notify user since we're starting back from the top
        //                XNAT.ui.dialog.alert('Processing competed');
        //                activityTable.logModal.title$.text(
        //                    activityTable.logModal.title$.text().replace('(live)', '(from file)')
        //                );
        //                emptyFirst = true;
        //            }
        //            addFileContent(dataJson, emptyFirst);
        //        } else {
        //            // live logging content
        //            if (firstRun) {
        //                activityTable.logModal.title$.text(activityTable.logModal.title$.text() + ' (live)');
        //            }
        //            addLiveLoggingContent(dataJson);
        //        }
        //    },
        //    error: function (e) {
        //        if (e.status === 403) {
        //            errorHandler(e, 'Insufficient permissions to retrieve ' + logFile , true);
        //        } else {
        //            errorHandler(e, 'Cannot retrieve ' + logFile + '; container may have restarted.', true);
        //        }
        //    }
        //});
    //};

    //activityTable.viewLog = viewLog = function (containerId, logFile) {
    //    activityTable.logModal = XNAT.dialog.open({
    //        title: 'View ' + logFile,
    //        id: containerModalId(containerId, logFile),
    //        width: 850,
    //        header: true,
    //        maxBtn: true,
    //        beforeShow: function() {
    //            refreshLog(containerId, logFile);
    //        },
    //        buttons: [
    //            {
    //                label: 'Done',
    //                isDefault: true,
    //                close: true
    //            }
    //        ]
    //    });
    //};



    activityTable.viewActivityEntry = function(activityEntry) {
        var activityDialogButtons = [
            {
                label: 'Done',
                isDefault: true,
                close: true
            }
        ];

        // build nice-looking activity entry table
        var pheTable = XNAT.table({
            className: 'xnat-table compact',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        var allTables = [spawn('h3', 'Container information'), pheTable.table];

        for (var key in activityEntry) {
            var val = activityEntry[key], formattedVal = '', putInTable = true;

            if (Array.isArray(val) && val.length > 0) {
                // Display a table
                var columns = [];
                val.forEach(function (item) {
                    if (typeof item === 'object') {
                        Object.keys(item).forEach(function(itemKey){
                            if(columns.indexOf(itemKey)===-1){
                                columns.push(itemKey);
                            }
                        });
                    }
                });


                formattedVal="<table class='xnat-table'>";
                if (columns.length > 0) {
                    formattedVal+="<tr>";
                    columns.forEach(function(colName){
                        formattedVal+="<th>"+colName+"</th>";
                    });
                    formattedVal+="</tr>";

                    val.sort(function(obj1,obj2){
                        // Sort by time recorded (if we have it)
                        var date1 = Date.parse(obj1["time-recorded"]), date2 = Date.parse(obj2["time-recorded"]);
                        return date1 - date2;
                    });
                } else {
                    // skip header if we just have one column
                    // sort alphabetically
                    val.sort()
                }

                val.forEach(function (item) {
                    formattedVal+="<tr>";
                    if (typeof item === 'object') {
                        columns.forEach(function (itemKey) {
                            formattedVal += "<td nowrap>";
                            var temp = item[itemKey];
                            if (typeof temp === 'object') temp = JSON.stringify(temp);
                            formattedVal += temp;
                            formattedVal += "</td>";
                        });
                    } else {
                        formattedVal += "<td nowrap>";
                        formattedVal += item;
                        formattedVal += "</td>";
                    }
                    formattedVal+="</tr>";
                });
                formattedVal+="</table>";
                putInTable = false;
            } else if (typeof val === 'object') {
                formattedVal = spawn('code', JSON.stringify(val));
            } else if (!val) {
                formattedVal = spawn('code', 'false');
            } else if (key === 'workflow-id') {
                // Allow pulling up detailed workflow info (can contain addl info in details field)
                var curid = '#wfmodal' + val;
                formattedVal = spawn('a' + curid, {}, val);
                $(document).on('click', curid, {wfid: val}, activityTable.workflowModal);
            } else {
                formattedVal = spawn('code', val);
            }

            if (putInTable) {
                pheTable.tr()
                    .td('<b>' + key + '</b>')
                    .td([spawn('div', {style: {'word-break': 'break-all', 'max-width': '600px', 'overflow':'auto'}}, formattedVal)]);
            } else {
                allTables.push(
                    spawn('div', {style: {'word-break': 'break-all', 'overflow':'auto', 'margin-bottom': '10px', 'max-width': 'max-content'}},
                        [spawn('div.data-table-actionsrow', {}, spawn('strong', {class: "textlink-sm data-table-action"},
                            'Container ' + key)), formattedVal])
                );
            }

            // check logs and populate buttons at bottom of modal
            if (key === 'log-paths') {
                if (activityEntry.backend.toLowerCase() !== 'kubernetes') {
                    activityDialogButtons.push({
                        label: 'View StdOut.log',
                        close: false,
                        action: function(){
                            activityTable.viewLog(activityEntry.id, 'stdout')
                        }
                    });

                    activityDialogButtons.push({
                        label: 'View StdErr.log',
                        close: false,
                        action: function(){
                            activityTable.viewLog(activityEntry.id, 'stderr')
                        }
                    });
                }
                else {
                    // Container executions in Kubernetes do not publish a StdErr log
                    activityDialogButtons.push({
                        label: 'View Logs',
                        close: false,
                        action: function(){
                            activityTable.viewLog(activityEntry.id, 'stdout')
                        }
                    });
                }
            }
            if (key === 'setup-container-id') {
                activityDialogButtons.push({
                    label: 'View Setup Container',
                    close: true,
                    action: function () {
                        activityTable.viewHistory(activityEntry[key]);
                    }
                })
            }
            if (key === 'parent-database-id' && activityEntry[key]) {
                var parentId = activityEntry[key];
                activityDialogButtons.push({
                    label: 'View Parent Container',
                    close: true,
                    action: function () {
                        activityTable.viewHistory(parentId);
                    }
                })
            }
        }

        // display activity
        XNAT.ui.dialog.open({
            title: activityEntry['wrapper-name'],
            width: 800,
            scroll: true,
            content: spawn('div', allTables),
            buttons: activityDialogButtons,
            header: true,
            maxBtn: true
        });
    };

    activityTable.viewActivity = function (id) {
        if (XNAT.plugin.transporter.transportActivity.hasOwnProperty(id)) {
            //activityTable.viewActivityEntry(XNAT.plugin.transporter.transportActivity[id]);
        } else {
            console.log(id);
            XNAT.ui.dialog.open({
                content: 'Sorry, could not display this activity item.',
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true
                    }
                ]
            });
        }
    };


    activityTable.findById = function(e){
        e.preventDefault();

        var id = $('#transport-session-id-entry').val();
        if (!id) return false;

        XNAT.xhr.getJSON({
            url: restUrl('/xapi/transporter/activity/'+id),
            error: function(e){
                console.warn(e);
                XNAT.ui.dialog.message('Please enter a valid session ID');
                $('#transport-session-id-entry').focus();
            },
            success: function(data){
                $('#transport-session-id-entry').val('');
                activityTable.viewActivityEntry(data);
            }
        })
    };

    $(document).off('keyup').on('keyup','#transport-session-id-entry',function(e){
        var val = this.value;
        if (e.key === 'Enter' || e.keyCode === '13') {
            activityTable.findById(e);
        }
    });

    activityTable.init = activityTable.refresh = function (context) {
        console.log('transporter-activityTable.init()');
        if (context) {
            activityTable.context = context;
        }
        function setupParams() {
            if (context) {
                XNAT.ui.ajaxTable.filters = XNAT.ui.ajaxTable.filters || {};
            }
        }

        wrapperList = getObject(XNAT.plugin.transporter.wrapperList || {});

        $('#' + activityTableContainerId).empty();
        XNAT.plugin.transporter.activityTable.activityData =
            XNAT.ui.ajaxTable.AjaxTable(XNAT.url.restUrl('/xapi/transporter/activity/all'),
            'transporter-activity-table', activityTableContainerId, 'Activity', 'All activity',
            activityTableObject(), setupParams, null, dataLoadCallback, null, labelMap);

        XNAT.plugin.transporter.activityTable.activityData.load();

        // add a "find by ID" input field after the table renders
        var target = $('#'+activityTableContainerId),
            searchHistoryInput = spawn('input#transport-session-id-entry', {
                type:'text',
                name: 'findbyid',
                placeholder: 'Find By ID',
                size: 12,
                style: {'font-size':'12px' }}
            ),
            searchHistoryButton = spawn(
                'button.btn2.btn-sm',
                {
                    title: 'Find By ID',
                    onclick: XNAT.plugin.transporter.activityTable.findById
                },
                [ spawn('i.fa.fa-search') ]);
        target.prepend(spawn('div.pull-right',[
            searchHistoryInput,
            spacer(4),
            searchHistoryButton
        ]));

    };

    function dataLoadCallback(data) {
        data.forEach(function (activityEntry) {
            // data.filter(function(entry){ return entry.id === activityEntry.id })[0].context = activityTable.context;
            activityEntry.context = activityTable.context;
            transportActivity[activityEntry.id] = activityEntry;
        });
    }
}));

$(document).ready(function(){
    XNAT.plugin.transporter.activityTable.init();
});