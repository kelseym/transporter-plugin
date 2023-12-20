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

    let activityUrl = XNAT.plugin.transporter.activityUrl = function() {
        let url = '/xapi/transporter/activity'
        return restUrl(url)
    }

    XNAT.plugin.transporter.getActivity = XNAT.plugin.transporter.activity.getAll = async function() {
        console.debug('transporter-activity-admin.js: XNAT.plugin.transporter.activity.getActivity');

        const response = await fetch(restUrl('/xapi/transporter/activity'), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error('HTTP error getting transporter activity: ${response.status}');
        }

        return await response.json();
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
            .th('<b>Event</b>')
            .th('<b>Timestamp</b>')



        XNAT.plugin.transporter.getActivity().then(item => {
            let noActivity = true;
            
            item.forEach(item => {
                let sessionId = item['sessionId'];
                let user = item['username'];
                let snapshotId = item['snapshotId'];
                let event = item['event'];
                let timestamp = item['timestamp'];

                noActivity = false;
                activityTable.tr()
                          .td([spawn('div.left', [sessionId])])
                          .td([spawn('div.center', [username])])
                          .td([spawn('div.center', [snapshotId])])
                          .td([spawn('div.center', [event])])
                          .td([spawn('div.center', [timestamp.toLocaleString()])]);
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