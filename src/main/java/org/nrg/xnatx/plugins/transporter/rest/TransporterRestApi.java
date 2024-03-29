package org.nrg.xnatx.plugins.transporter.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.*;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotValidationException;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@Api("API for the XNAT Transporter Service")
@XapiRestController
@RequestMapping(value = "/transporter")
public class TransporterRestApi extends AbstractXapiRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;

    private TransporterService transporterService;
    private TransporterConfigService transporterConfigService;

    @Autowired
    public TransporterRestApi(TransporterService transporterService,
                              TransporterConfigService transporterConfigService,
                              UserManagementServiceI userManagementService,
                                 RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.transporterService = transporterService;
        this.transporterConfigService = transporterConfigService;
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/datasnap"}, method = POST, consumes = JSON)
    @ApiOperation(value = "Accepts and processes a DataSnap before adding it to the database")
    public ResponseEntity<String> addSnap(final @RequestBody DataSnap dataSnap,
                                           @RequestParam(required = false, defaultValue = "true") boolean resolve) throws Exception {
        Optional<DataSnap> result = transporterService.storeDataSnap(getUser(), dataSnap, resolve);
        if (result.isPresent()) {
            return ResponseEntity.ok("DataSnap " + result.get().getLabel() + " successfully received and processed!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store DataSnap");
        }
    }

    // REST Endpoint to respond to request for available snapshots
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnaps"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public List<DataSnap> getSnaps() {
        List<DataSnap> dataSnaps = transporterService.getDataSnaps(isAdmin(getUser()) ? null : getUser());
        dataSnaps.sort((Comparator.comparing(DataSnap::getId)));
        return dataSnaps;
    }

    // REST Endpoint to GET a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = GET)
    @ApiOperation(value = "Get snapshot by id.")
    @ResponseBody
    public DataSnap getSnap(final @PathVariable String id) throws UnauthorizedException, NotFoundException {
        return transporterService.getDataSnap(getUser(), id);
    }

    // REST Endpoint to GET a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = PUT, consumes = JSON)
    @ApiOperation(value = "Update snapshot label, description and content.")
    @ResponseBody
    public DataSnap updateSnap(final @PathVariable(required = true) String id,
                               final @RequestBody DataSnap updatedDataSnap)
            throws UnauthorizedException, NotFoundException, SnapshotValidationException {
        return transporterService.updateDataSnap(getUser(), id, updatedDataSnap);
    }

    // REST Endpoint to DELETE a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = DELETE)
    @ApiOperation(value = "Delete snapshot.")
    public ResponseEntity<Void> deleteSnap(final @PathVariable String id) throws NotFoundException, UnauthorizedException {
        transporterService.deleteDataSnap(getUser(), String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    // REST Endpoint to GET payload by label for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/payload/{label}"}, method = GET)
    @ApiOperation(value = "Get payload by label.")
    @ResponseBody
    public ResponseEntity<Payload> getPayloadByLabel(final @PathVariable(required = true) String label)
            throws Exception {

        return ResponseEntity.ok(transporterService.createPayload(getUser(), label));
    }

    // REST Endpoint to GET all payload labels for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/payloads"}, method = GET)
    @ApiOperation(value = "Get payload summaries by user. Available payloads correspond to mirrored snapshots.")
    @ResponseBody
    public ResponseEntity<List<Payload>> getPayloads()
            throws Exception {

        return ResponseEntity.ok(transporterService.getAvailablePayloads(getUser()));
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}/reader"}, method = POST)
    @ApiOperation(value = "Add a reader to a DataSnap. Restricted to DataSnap owner.")
    public ResponseEntity<DataSnap> addSnapReader(final @PathVariable String id,
                                                  @RequestParam(required = true) String userLogin) throws Exception {
        DataSnap dataSnap = transporterService.addDataSnapReader(getUser(), id, userLogin);
        return ResponseEntity.ok(dataSnap);
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}/editor"}, method = POST)
    @ApiOperation(value = "Add a editor to a DataSnap. Restricted to DataSnap owner.")
    public ResponseEntity<DataSnap> addSnapEditor(final @PathVariable String id,
                                                  @RequestParam(required = true) String userLogin) throws Exception {
        DataSnap dataSnap = transporterService.addDataSnapEditor(getUser(), id, userLogin);
        return ResponseEntity.ok(dataSnap);
    }

    // REST Endpoint to set transporter path mapping
    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/path-mapping"}, method = POST, consumes = JSON)
    @ApiOperation(value = "Set transporter path mapping.")
    public ResponseEntity setMappingJson(@RequestBody final TransporterPathMapping transporterPathMapping)
            throws ConfigServiceException, JsonProcessingException {
        transporterConfigService.setTransporterPathMapping(getUser().getLogin(), null, transporterPathMapping.getXnatRootPath(), transporterPathMapping.getServerRootPath());
        return ResponseEntity.ok().build();
    }

    // REST Endpoint to get transporter path mapping
    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/path-mapping"}, method = GET)
    @ApiOperation(value = "Get transporter path mapping.")
    public ResponseEntity<TransporterPathMapping> getMapping() throws JsonProcessingException {
        return ResponseEntity.ok(transporterConfigService.getTransporterPathMapping());
    }

    // REST Endpoint to request data mirroring
    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/mirror/{id}"}, method = POST)
    @ApiOperation(value = "Mirror snapshot data.")
    public ResponseEntity mirror(final @PathVariable String id,
                                 @RequestParam(required = false, defaultValue = "false") boolean force)
            throws Exception {
        transporterService.mirrorDataSnap(getUser(), id, force);
        return ResponseEntity.ok().build();
    }


    // ** REMOTE APPLICATION ENDPOINTS ** //


    // REST Endpoints to manage remote application status
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/heartbeat"}, method = POST)
    @ApiOperation(value = "Update remote application status.")
    public ResponseEntity updateRemoteHeartbeat(@RequestBody(required = true) RemoteAppHeartbeat heartbeat)
            throws Exception {
        log.debug("Received heartbeat from " + heartbeat.getRemoteHost());
        transporterService.updateRemoteApplicationStatus(heartbeat);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/heartbeat"}, method = GET)
    @ApiOperation(value = "Get remote application status.")
    public ResponseEntity<List<RemoteAppHeartbeat>> getRemoteHeartbeat() throws Exception {
        return ResponseEntity.ok(transporterService.getRemoteApplicationStatus());
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/heartbeat/{remoteAppId}"}, method = GET)
    @ApiOperation(value = "Get remote application status by remoteAppId.")
    public ResponseEntity<RemoteAppHeartbeat> getRemoteHeartbeat(
            @PathVariable(required = true) String remoteAppId) throws Exception {
        //TODO: check if user is authorized to get this remoteAppId
        return ResponseEntity.ok(transporterService.getRemoteApplicationStatus(remoteAppId));
    }

    // REST endpoints to collect transfer activity from remote application
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/activity"}, method = POST)
    @ApiOperation(value = "Update remote application activity.")
    public ResponseEntity updateActivity(
                @RequestBody(required = true) TransportActivity.TransportActivityMessage activityMessage,
                @RequestParam(name = "message_id", required = false) String messageId)
            throws Exception {
        if (!getUser().getLogin().equals(activityMessage.getUsername())){
            throw new UnauthorizedException("User " + getUser().getLogin() + " is not authorized to update activity for user " + activityMessage.getUsername());
        }
        log.debug("Received activity from " + activityMessage);
        transporterService.updateRemoteApplicationActivity(activityMessage);
        return ResponseEntity.ok().build();
    }

    // REST endpoint to get remote application transfer activity
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/activity"}, method = GET)
    @ApiOperation(value = "Get remote application activity by user.")
    public ResponseEntity<List<TransportActivity>> getActivity(@RequestParam(required = false) String snapshotId)
            throws Exception {
        return ResponseEntity.ok(transporterService.getRemoteApplicationActivity(null, getUser(), snapshotId));
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/activity/{sessionId}"}, method = DELETE)
    @ApiOperation(value = "Delete remote application activity by sessionId.")
    public ResponseEntity deleteActivity(@PathVariable(required = true) String sessionId) throws Exception {
        transporterService.deleteRemoteApplicationActivity(sessionId);
        return ResponseEntity.ok().build();
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/activity/all"}, method = GET)
    @ApiOperation(value = "Get all remote application activity.")
    public ResponseEntity<List<TransportActivity>> getAllActivity(@RequestParam(required = false) String snapshotId,
                                                                  @RequestParam(required = false) String sessionId)
            throws Exception {
        return ResponseEntity.ok(transporterService.getRemoteApplicationActivity(sessionId, null, snapshotId));
    }

    private UserI getUser() {
        return XDAT.getUserDetails();
    }

    private boolean isAdmin(final UserI user) {
        return getRoleHolder().isSiteAdmin(user);
    }

    private boolean isOwner(final UserI user, final List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return false;
        }
        final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
        for (String pid : projectIds) {
            if (!userHelperService.isOwner(pid)) {
                return false;
            }
        }
        return true;
    }
}
