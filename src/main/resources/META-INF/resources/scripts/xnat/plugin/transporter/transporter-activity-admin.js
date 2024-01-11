/*!
 * Transporter admin functions
 */

console.debug('transporter-activity-admin.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.transporter = getObject(XNAT.plugin.transporter || {});
XNAT.plugin.transporter.activity = getObject(XNAT.plugin.transporter.activity || {});

(function(factory) {
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

    let restUrl = XNAT.url.restUrl;

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    XNAT.plugin.transporter.getActivity = XNAT.plugin.transporter.activity.getAll = async function(sessionId) {
        console.debug('transporter-activity-admin.js: XNAT.plugin.transporter.activity.getActivity');

        const url = sessionId
            ? restUrl(`/xapi/transporter/activity/all?sessionId=${sessionId}`)
            : restUrl('/xapi/transporter/activity/all');

        const response = await fetch(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error('HTTP error getting transporter activity: ${response.status}');
        }

        return await response.json();
    }

    XNAT.plugin.transporter.activity.deleteActivity = function(sessionId,label){
        XNAT.ui.dialog.confirm({
            title: 'Delete Activity History?',
            content: 'Are you sure you want to delete the activity history for remote session <b>'+label+'</b>? This operation cannot be undone.',
            okAction: function(){
                XNAT.xhr.ajax({
                    url: restUrl('/xapi/transporter/activity/' + sessionId),
                    method: 'DELETE',
                    success: function () {
                        XNAT.ui.banner.top(3000, 'Successfully Deleted ' + label + ' activity history.', 'success');
                        XNAT.plugin.transporter.activity.refresh('transporter-activity-table');
                    }
                })
            }
        })
    };
    //function viewActivityButton(sessionId) {
    //    return spawn('button.btn.btn-sm.view-activity-button', {
    //        html: "<i class='fa fa-eye' title='View Transport Session Activity'></i>",
    //        data: {"sessionId": sessionId},
    //        onclick: function () {
    //            XNAT.xhr.getJSON({
    //                url: restUrl(`/xapi/transporter/activity/all/?sessionId=${sessionId}`),
    //                success: function (data) {
    //                    XNAT.dialog.open({
    //                        title: 'View Activity Events',
    //                        width: 900,
    //                        content: '<div id="activity-json"></div>',
    //                        beforeShow: function (obj) {
    //                            var container = obj.$modal.find('div#activity-json');
    //                            container.empty().append(spawn('pre',JSON.stringify(data,null,4)));
    //                        },
    //                        buttons: [
    //                            {
    //                                label: 'OK',
    //                                isDefault: true,
    //                                close: true
    //                            }
    //                        ]
    //                    })
    //                }
    //            })
    //        }
    //    });
    //}
    //function deleteActivityButton(sessionId, sessionIdShort) {
    //    return spawn('button.btn.btn-sm.delete-config-button', {
    //        html: "<i class='fa fa-trash-o' title='Delete Activity'></i>",
    //        data: {"sessionId": sessionId},
    //        onclick: function () {
    //            XNAT.plugin.transporter.activity.deleteActivity(sessionId, sessionIdShort)
    //        }
    //    })
    //}


    /* ======================== *
    /* Paginated Activity Table *
    /* ======================== */

    var activityTable, activityData;

    XNAT.plugin.transporter.activity.activityTable = activityTable =
        getObject(XNAT.plugin.transporter.activity.activityTable || {});

    XNAT.plugin.transporter.activity.activityData = activityData =
        getObject(XNAT.plugin.transporter.activity.activityData || {});

    const activityTableContainerId = 'transporter-activity-container';

    function getActivityUrl(){
        return restUrl('/xapi/transporter/activity/all');
    }

    function viewActivityDialog(e, onclose) {
        e.preventDefault();
        //var historyId = $(this).data('id') || $(this).closest('tr').prop('title');
        //XNAT.admin.eventServicePanel.historyTable.viewHistory(historyId);
    }

    const labelMap = {
        // id:             {label: 'ID', op: 'eq', type: 'number', show: false},
        timestamp:  {label: 'Date', column: 'timestamp', show: true},
        session:    {label: 'Transport ID', column: 'session', show: true},
        user:       {label: 'Run As User', column: 'user', show: true},
        snapshot:   {label: 'Snapshot', column: 'snapshot', show: true},
        //actions:     {label: 'Actions', column: 'actions', show: true}
    };

    function activityTableObject() {
        return {
            table: {
                classes: "fixed-header selectable scrollable-table compact",
                style: "width: auto;",
                on: [
                    ['click', 'a.view-activity-detail', viewActivityDialog]
                ]
            },
            sortable: 'timestamp, session, user, snapshot',
            filter: 'session, user, snapshot, status',
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
                session: {
                    th: {className: 'session'},
                    label: labelMap.session['label'],
                    apply: function () {
                        return this['session']
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
                    //            html: this['session']['name']
                    //        }),
                    //        spawn('span',message)
                    //    ]);
                    //},
                },
                user: {
                    th: {className: 'user'},
                    label: labelMap.user['label'],
                    apply: function () {
                        return this['user']
                    }
                },
                snapshot: {
                    th: {className: 'snapshot'},
                    td: {className: 'snapshot word-wrapped'},
                    label: labelMap.snapshot['label'],
                    apply: function(){
                        return this['label'];
                    }
                }
                //,actions: {
                //    th: {className: 'actions'},
                //    td: {className: 'actions'},
                //    label: labelMap.actions['label'],
                //    apply: function () {
                //        return spawn('div.center', [
                //            viewActivityButton(this['session']),
                //            spacer(6),
                //            deleteActivityButton(this['session'], this['session-id-short'])
                //        ])
                //    }
                //}
            }
        }
    }

    XNAT.plugin.transporter.activity.refresh = XNAT.plugin.transporter.activity.init = function (context) {
        if (context) {
            activityTable.context = context;
        }
        function setupParams() {
            if (context) {
                XNAT.ui.ajaxTable.filters = XNAT.ui.ajaxTable.filters || {};
                // XNAT.ui.ajaxTable.filters['project'] = {operator: 'eq', value: context, backend: 'hibernate'};
            }
        }

        $('#' + activityTableContainerId).empty();
        XNAT.plugin.transporter.activity.activityTable.activtiyListing = XNAT.ui.ajaxTable.AjaxTable(getActivityUrl(),
            'transporter-activity-table', activityTableContainerId, 'Transporter Activity', 'All Activity',
            activityTableObject(), setupParams, null, dataLoadCallback, null, labelMap);

        XNAT.plugin.transporter.activity.activityTable.activtiyListing.load();

        // add a "find by ID" input field after the table renders
        //var target = $('#'+activityTableContainerId),
        //    searchHistoryInput = spawn('input#event-id-entry', {
        //        type:'text',
        //        name: 'findbyid',
        //        placeholder: 'Find By ID',
        //        size: 12,
        //        style: {'font-size':'12px' }}
        //    ),
        //    searchHistoryButton = spawn(
        //        'button.btn2.btn-sm',
        //        {
        //            title: 'Find By ID',
        //            onclick: XNAT.plugin.transporterActivityPanel.activityTable.findById
        //        },
        //        [ spawn('i.fa.fa-search') ]);
        //target.prepend(spawn('div.pull-right',[
        //    searchHistoryInput,
        //    spacer(4),
        //    searchHistoryButton
        //]));
//
    };

    function dataLoadCallback(data) {
        data.forEach(function (historyEntry) {
            // data.filter(function(entry){ return entry.id === historyEntry.id })[0].context = activityTable.context;
            historyEntry.context = activityTable.context;
            activityData[historyEntry.id] = historyEntry;
        });
    }

}));