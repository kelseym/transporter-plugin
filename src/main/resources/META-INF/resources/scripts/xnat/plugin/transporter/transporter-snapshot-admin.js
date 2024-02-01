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

    let getSnapshotEditUrl = XNAT.plugin.transporter.getSnapshotEditUrl = function (snapshotId) {
        return restUrl(`/xapi/transporter/datasnap/${snapshotId}`);
    }

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll){
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    XNAT.plugin.transporter.snapshot.getSnapshots = async function() {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.getSnapshots');

        const response = await fetch(restUrl('/xapi/transporter/datasnaps'), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error('HTTP error getting transporter snapshots: ${response.status}');
        }
        return await response.json();
    }

    XNAT.plugin.transporter.snapshot.getSnapshot = function(id) {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.getSnapshot');
        return XNAT.xhr.get({
            url: getSnapshotEditUrl(id),
            dataType: 'json',
            success: function(data){
                if (data) {
                    return data;
                }
            }
        })
    };

    XNAT.plugin.transporter.snapshot.mirrorSnapshot = async function(snapshotId, force = false) {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.mirrorSnapshot');

        const response = await fetch(restUrl(`/xapi/transporter/mirror/${snapshotId}/?force=${force}`), {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error('HTTP error mirroring transporter snapshot: ${response.status}');
        }

        return await response.json();
    }

    XNAT.plugin.transporter.snapshot.deleteSnapshot = function(id,label){
        XNAT.ui.dialog.confirm({
            title: 'Delete Snapshot?',
            content: 'Are you sure you want to delete the snapshot <b>'+label+'</b>? This operation cannot be undone.',
            okAction: function(){
                XNAT.xhr.ajax({
                    url: restUrl('/xapi/transporter/datasnap/' + id),
                    method: 'DELETE',
                    success: function () {
                        XNAT.ui.banner.top(3000, 'Successfully Deleted Snapshot ' + label, 'success');
                        XNAT.plugin.transporter.snapshot.refresh('transporter-snapshot-table');
                    }
                })
            }
        })
    };

    XNAT.plugin.transporter.snapshot.table = function(snapshotTableContainerId) {
        console.debug('transporter-snapshot-admin.js: XNAT.plugin.transporter.snapshot.table');

        function mirrorSnapshotButton(snapshotId, force) {
            let message = force === false ? "Mirror snapshot?" : "Force mirror snapshot?";
            let dialogMessage = force === false ?
                "Create mirror of snapshot data to prepare for Transporter access." :
                "Re-mirror snapshot data to prepare for Transporter access.";
            return spawn('button.btn.btn-sm.mirror-snapshot-button', {
                html: "<i class='fa fa-refresh' title='"+message+"'></i>",
                data: {"snapshotId": snapshotId},
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>" + dialogMessage + "</p>",
                        okAction: function() {
                            XNAT.plugin.transporter.snapshot.mirrorSnapshot(snapshotId, force).then((response) => {
                                if (response.ok) {
                                    // Check for a successful HTTP response code (e.g., 200 OK)
                                    const delay = (time) => new Promise(resolve => setTimeout(resolve, time));
                                    delay(500).then(() => XNAT.plugin.transporter.snapshot.refresh());
                                } else {
                                    console.error(`Failed to mirror snapshot. HTTP response code: ${response.status}`);
                                    XNAT.dialog.alert(`Failed to mirror snapshot: ${response.message}`)
                                }
                            }).catch(error => {
                                console.error(error);
                                XNAT.dialog.alert(`Error encountered mirroring snapshot: ${error}`)
                            });
                        }
                    })
                }
            });
        }

        function viewSnapshotButton(snapshotId, label) {
            return spawn('button.btn.btn-sm.view-snapshot-button', {
                html: "<i class='fa fa-eye' title='View Snapshot'></i>",
                data: {"snapshotId": snapshotId},
                onclick: function () {
                    XNAT.xhr.getJSON({
                        url: getSnapshotEditUrl(snapshotId),
                        success: function (data) {
                            XNAT.dialog.open({
                                title: 'View Snapshot Data',
                                width: 900,
                                content: '<div id="snapshot-json"></div>',
                                beforeShow: function (obj) {
                                    var container = obj.$modal.find('div#snapshot-json');
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

        function editSnapshotButton(snapshotId){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    XNAT.plugin.transporter.snapshot.getSnapshot(snapshotId).done(function(snapshot){
                        XNAT.plugin.transporter.snapshot.dialog(snapshot);
                    });
                }
            }, '<i class="fa fa-pencil" title="Edit Command"></i>');
        }

        function deleteSnapshotButton(snapshotId, label) {
            return spawn('button.btn.btn-sm.delete-config-button', {
                html: "<i class='fa fa-trash-o' title='Delete Snapshot'></i>",
                data: {"snapshotId": snapshotId},
                onclick: function () {
                    XNAT.plugin.transporter.snapshot.deleteSnapshot(snapshotId,label)
                }
            })
        }

        // create a code editor dialog to view a snapshot definition
        XNAT.plugin.transporter.snapshot.dialog = function(snapshot){
            var _source,_editor;
            snapshot = snapshot || {};

            var dialogButtons = {
                update: {
                    label: 'Save',
                    isDefault: true,
                    action: function(){
                        var editorContent = _editor.getValue().code;

                        var url = getSnapshotEditUrl(snapshot.id);

                        XNAT.xhr.postJSON({
                            url: url,
                            dataType: 'json',
                            data: editorContent,
                            success: function(){
                                XNAT.plugin.transporter.snapshot.refresh();
                                xmodal.closeAll();
                                XNAT.ui.banner.top(2000, 'Snapshot updated.', 'success');
                            },
                            fail: function(e){
                                errorHandler(e, 'Could Not Update', false);
                            }
                        });
                    }
                },
                close: { label: 'Cancel' }
            };

            // sanitize the command definition so it can be updated
            var sanitizedVars = {};
            ['id', 'root-path', 'build-state'].forEach(function(v){
                sanitizedVars[v] = snapshot[v];
                delete snapshot[v];
            });

            _source = spawn ('textarea', JSON.stringify(snapshot, null, 4));

            _editor = XNAT.app.codeEditor.init(_source, {
                language: 'json'
            });

            _editor.openEditor({
                title: 'Edit Definition For ' + snapshot.name,
                classes: 'plugin-json',
                buttons: dialogButtons,
                height: 680,
                afterShow: function(dialog, obj){
                    obj.aceEditor.setReadOnly(false);
                    dialog.$modal.find('.body .inner').prepend(
                        spawn('div',[
                            spawn('p', 'Snapshot Name: '+snapshot['name']),
                        ])
                    );
                }
            });
        };


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
            .th('<b>Build State</b>')
            .th('<b>Actions</b>')

        XNAT.plugin.transporter.snapshot.getSnapshots().then(item => {
            let noSnapshots = true;
            
            item.forEach(item => {
                let id = item['id'];
                let label = item['label'];
                let description = item['description'];
                let buildState = item['build-state'];
                let isMirrored = buildState === 'MIRRORED';

                noSnapshots = false;
                snapshotTable.tr()
                          .td([spawn('div.left', [id])])
                          .td([spawn('div.center', [label])])
                          .td([spawn('div.center', [description])])
                          .td([spawn('div.center', [buildState])])
                          .td([['div.center',
                              [mirrorSnapshotButton(id, isMirrored), spacer(6), viewSnapshotButton(id, label),
                                  spacer(6), editSnapshotButton(id), spacer(6), deleteSnapshotButton(id, label)]]]);
            })
            
            if (noSnapshots) {
                snapshotTable.tr()
                            .td([spawn('div.left', ["No snapshots found."])])
                            .td([spawn('div.left', "")])
                            .td([spawn('div.left', "")])
                            .td([spawn('div.left', "")])
                            .td([spawn('div.left', "")])
            }
        }).catch(e => {
            console.error("Unable to fetch transporter snapshots.", e);

            snapshotTable.tr()
                .td([spawn('div.left', ["Unable to fetch transporter snapshots."])]);
        })

        return snapshotTable.table;
    }

    XNAT.plugin.transporter.snapshot.refresh = function(snapshotTableContainerId= 'transporter-snapshot-table') {
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