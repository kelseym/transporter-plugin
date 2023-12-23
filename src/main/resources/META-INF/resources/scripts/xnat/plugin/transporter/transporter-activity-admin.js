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
    function viewActivityButton(sessionId) {
        return spawn('button.btn.btn-sm.view-activity-button', {
            html: "<i class='fa fa-eye' title='View Transport Session Activity'></i>",
            data: {"sessionId": sessionId},
            onclick: function () {
                XNAT.xhr.getJSON({
                    url: restUrl(`/xapi/transporter/activity/all/?sessionId=${sessionId}`),
                    success: function (data) {
                        XNAT.dialog.open({
                            title: 'View Activity Events',
                            width: 900,
                            content: '<div id="activity-json"></div>',
                            beforeShow: function (obj) {
                                var container = obj.$modal.find('div#activity-json');
                                container.empty().append(spawn('pre',JSON.stringify(data,null,4)));
                            },
                            buttons: [
                                {
                                    label: 'OK',
                                    isDefault: true,
                                    close: true
                                }
                            ]
                        })
                    }
                })
            }
        });
    }
    function deleteActivityButton(sessionId, sessionIdShort) {
        return spawn('button.btn.btn-sm.delete-config-button', {
            html: "<i class='fa fa-trash-o' title='Delete Activity'></i>",
            data: {"sessionId": sessionId},
            onclick: function () {
                XNAT.plugin.transporter.activity.deleteActivity(sessionId, sessionIdShort)
            }
        })
    }

    XNAT.plugin.transporter.activity.table = function(activityTableContainerId) {
        console.debug('transporter-activity-admin.js: XNAT.plugin.transporter.activity.table');

        // initialize the table
        const activityTable = XNAT.table({
            className: 'activity xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        activityTable.tr()
            .th({addClass: 'left', html: '<b>Session</b>'})
            .th('<b>User</b>')
            .th('<b>Snapshot</b>')
            .th('<b>Timestamp</b>')
            .th('<b>Actions</b>')



        XNAT.plugin.transporter.getActivity().then(item => {
            let noActivity = true;
            
            item.forEach(item => {
                let sessionId = item['sessionId'];
                let sessionIdShort = item['session-id-short'];
                let username = item['username'];
                let snapshotId = item['snapshot-id-display'];
                let timestamp = item['formatted-timestamp'];
                let events = item['events'];

                noActivity = false;
                activityTable.tr()
                          .td([spawn('div.left', [sessionIdShort])])
                          .td([spawn('div.center', [username])])
                          .td([spawn('div.center', [snapshotId])])
                          .td([spawn('div.center', [timestamp])])
                          .td([['div.center',
                            [viewActivityButton(sessionId), spacer(6), deleteActivityButton(sessionId, sessionIdShort)]]]);
            })
            
            if (noActivity) {
                activityTable.tr()
                          .td([spawn('div.left', ["No transporter activity logged."])])
                          .td([spawn('div.center', [])])
                          .td([spawn('div.center', [])])
                          .td([spawn('div.center', [])])
                          .td([spawn('div.center', [])]);
            }
        }).catch(e => {
            console.error("Unable to fetch transporter activity.", e);

            activityTable.tr()
                .td([spawn('div.left', ["Unable to fetch transporter activity."])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])]);
        })

        return activityTable.table;
    }

    XNAT.plugin.transporter.activity.refresh = function(activityTableContainerId) {
        console.debug('transporter-activity-admin.js: XNAT.plugin.transporter.activity.refresh');

        // Create activity table
        let activityTable = XNAT.plugin.transporter.activity.table(activityTableContainerId)

        // Clear container and insert activity table
        let containerEl = document.getElementById(activityTableContainerId);
        if (containerEl && activityTable) {
            containerEl.innerHTML = "";
            containerEl.append(activityTable);
        }
    }

    XNAT.plugin.transporter.activity.init = function(activityTableContainerId = 'transporter-activity-table') {
        console.debug('transporter-activity-admin.js: XNAT.plugin.transporter.activity.init');

        let containerEl = document.getElementById(activityTableContainerId);
        let footerEl = containerEl.parentElement.parentElement.querySelector(".panel-footer")

        XNAT.plugin.transporter.activity.refresh(activityTableContainerId);

        const refreshButton = spawn('button.btn.btn-sm', {
            html: 'Refresh',
            onclick: function() {
                XNAT.plugin.transporter.activity.refresh(activityTableContainerId)
            }
        });

        // add the 'refresh' button to the panel footer
        footerEl.append(spawn('div.pull-right', [refreshButton]));
        footerEl.append(spawn('div.clear.clearFix'));
    }


}));