/*!
 * Transporter admin functions
 */

console.debug('transporter-snapshot-admin.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.transporter = getObject(XNAT.plugin.transporter || {});
XNAT.plugin.transporter.snapshot = getObject(XNAT.plugin.transporter.snapshot || {});

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

    let snapshotUrl = XNAT.plugin.transporter.snapshotUrl = function() {
        let url = '/xapi/transporter/snapshot'
        return restUrl(url)
    }

    XNAT.plugin.transporter.getSnapshots = XNAT.plugin.transporter.snapshot.getSnapshots = async function() {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.getSnapshot');

        const response = await fetch(restUrl('/xapi/transporter/datasnaps'), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error('HTTP error getting transporter snapshots: ${response.status}');
        }

        return await response.json();
    }


    XNAT.plugin.transporter.snapshot.table = function(snapshotTableContainerId) {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.table');

        // initialize the table
        const snapshotTable = XNAT.table({
            className: 'snapshot xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        snapshotTable.tr()
            .th({addClass: 'left', html: '<b>ID</b>'})
            .th('<b>Label</b>')
            .th('<b>Description</b>')
            .th('<b>Root Key</b>')
            .th('<b>Build State</b>')
            .th('<b>Actions</b>')



        function mirrorSnapshotButton(snapshotId, force) {
            return spawn('button.btn.sm', {
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>Creating mirror of snapshot to prepare for Transporter access.</p>",
                        okAction: function() {
                            XNAT.plugin.jupyterhub.servers.stopServer(username, servername, eventTrackingId).then(() => {
                                const delay = (time) => new Promise(resolve => setTimeout(resolve, time));
                                delay(500).then(() => XNAT.plugin.jupyterhub.users.activity.refresh(activityTableContainerId));
                            }).catch(error => {
                                console.error(error);
                                XNAT.dialog.alert(`Failed to stop Jupyter server: ${error}`)
                            });
                        }
                    })
                }
            }, force === false ?  'Mirror' : 'Force Mirror');
        }

        XNAT.plugin.transporter.getSnapshots().then(item => {
            let noSnapshots = true;
            
            item.forEach(item => {
                let id = item['id'];
                let label = item['label'];
                let description = item['description'];
                let rootKey = item['path-root-key'];
                let buildState = item['build-state'];
                let isMirrored = buildState === 'MIRRORED';

                noSnapshots = false;
                snapshotTable.tr()
                          .td([spawn('div.left', [id])])
                          .td([spawn('div.center', [label])])
                          .td([spawn('div.center', [description])])
                          .td([spawn('div.center', [rootKey])])
                          .td([spawn('div.center', [buildState])])
                          .td([spawn('div.center', [isMirrored ?
                              mirrorSnapshotButton(id, false) :
                              mirrorSnapshotButton(id, true)])]);
            })
            
            if (noSnapshots) {
                snapshotTable.tr()
                          .td([spawn('div.left', ["No snapshots found."])]);
            }
        }).catch(e => {
            console.error("Unable to fetch transporter snapshots.", e);

            snapshotTable.tr()
                .td([spawn('div.left', ["Unable to fetch transporter snapshots."])]);
        })

        return snapshotTable.table;
    }

    XNAT.plugin.transporter.snapshot.refresh = function(snapshotTableContainerId) {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.refresh');

        // Create snapshot table
        let snapshotTable = XNAT.plugin.transporter.snapshot.table(snapshotTableContainerId)

        // Clear container and insert snapshot table
        let containerEl = document.getElementById(snapshotTableContainerId);
        if (containerEl && snapshotTable) {
            containerEl.innerHTML = "";
            containerEl.append(snapshotTable);
        }
    }

    XNAT.plugin.transporter.snapshot.init = function(snapshotTableContainerId = 'transporter-snapshot-table') {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.init');

        let containerEl = document.getElementById(snapshotTableContainerId);
        let footerEl = containerEl.parentElement.parentElement.querySelector(".panel-footer")

        XNAT.plugin.transporter.snapshot.refresh(snapshotTableContainerId);

        const refreshButton = spawn('button.btn.btn-sm', {
            html: 'Refresh',
            onclick: function() {
                XNAT.plugin.transporter.snapshot.refresh(snapshotTableContainerId)
            }
        });

        // add the 'refresh' button to the panel footer
        footerEl.append(spawn('div.pull-right', [refreshButton]));
        footerEl.append(spawn('div.clear.clearFix'));
    }


}));