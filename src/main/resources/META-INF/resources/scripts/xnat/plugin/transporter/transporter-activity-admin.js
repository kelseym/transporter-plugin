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

    var activityTable, transportActivity;

    XNAT.plugin.transporter.activityTable = activityTable =
        getObject(XNAT.plugin.transporter.activityTable || {});

    XNAT.plugin.transporter.transportActivity = transportActivity =
        getObject(XNAT.plugin.transporter.transportActivity || {});

    //must exist within a div.tab-container and have class data-table-container
    const activityTableContainerId = 'transporter-activity-container';

    const activityLabelMap = {
        // id:             {label: 'ID', op: 'eq', type: 'number', show: false},
        timestamp:  {label: 'Date', column: 'timestamp', show: true},
        sessionId:    {label: 'Transport ID', column: 'sessionId', show: true},
        username:       {label: 'User', column: 'username', show: true},
        snapshotId:   {label: 'Snapshot ID', column: 'snapshotId', show: true},
    };

    function activityTableObject() {
        return {
            table: {
                classes: "compact fixed-header selectable scrollable-table",
                style: "width: auto;",
            },
            sortable: 'timestamp, sessionId, username, snapshotId',
            filter: 'sessionId, username, snapshotId',
            items: {
                // id: {
                //     th: {className: 'id'},
                //     label: activityLabelMap.id['label'],
                //     apply: function(){
                //         return this.id.toString();
                //     }
                // },
                timestamp: {
                    label: activityLabelMap.timestamp['label'],
                    th: {className: 'DATE'},
                    apply: function () {
                        let timestamp = this['timestamp'];
                        let dateString = '';
                        if (timestamp) {
                            //timestamp = timestamp.replace(/-/g, '/'); // include date format hack for Safari
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
                    label: activityLabelMap.sessionId['label'],
                    apply: function () {
                        return this['sessionId']
                    }
                },
                username: {
                    th: {className: 'username'},
                    label: activityLabelMap.username['label'],
                    apply: function () {
                        return this['username']
                    }
                },
                snapshotId: {
                    th: {className: 'snapshotId'},
                    td: {className: 'snapshotId word-wrapped'},
                    label: activityLabelMap.snapshotId['label'],
                    apply: function(){
                        return this['label'];
                    }
                }
            }
        }
    }



    activityTable.init = activityTable.refresh = function (context) {
        console.log('transporter-activityTable.init()');
        if (context) {
            activityTable.context = context;
        }
        function activitySetupParams() {
            if (context) {
                XNAT.ui.ajaxTable.filters = XNAT.ui.ajaxTable.filters || {};
            }
        }

        $('#' + activityTableContainerId).empty();
        XNAT.plugin.transporter.activityTable.activityData =
            XNAT.ui.ajaxTable.AjaxTable(XNAT.url.restUrl('/xapi/transporter/activity/all'),
            'transporter-activity-table', activityTableContainerId, 'Activity', 'All activity',
            activityTableObject(), activitySetupParams, null, activityDataLoadCallback, null, activityLabelMap);

        XNAT.plugin.transporter.activityTable.activityData.loading = false;
        XNAT.plugin.transporter.activityTable.activityData.load();
    };

    function activityDataLoadCallback(data) {
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