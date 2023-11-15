package org.nrg.xnatx.plugins.transporter.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.*;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
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
    @ApiOperation(value = "Accepts and processes a DataSnap.")
    public ResponseEntity<String> postSnap(final @RequestBody DataSnap dataSnap,
                                           @RequestParam(required = false, defaultValue = "true") boolean resolve) throws Exception {
        Optional<DataSnap> result = transporterService.storeDataSnap(getUser(), dataSnap, resolve);
        if (result.isPresent()) {
            return ResponseEntity.ok("DataSnap " + result.get().getLabel() + " successfully received and processed!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store DataSnap");
        }
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}/reader"}, method = POST)
    @ApiOperation(value = "Add a reader to a DataSnap. Restricted to DataSnap owner.")
    public ResponseEntity<DataSnap> addReader(final @PathVariable String id,
                                             @RequestParam(required = true) String userLogin) throws Exception {
        DataSnap dataSnap = transporterService.addDataSnapReader(getUser(), id, userLogin);
        return ResponseEntity.ok(dataSnap);
    }

    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}/editor"}, method = POST)
    @ApiOperation(value = "Add a editor to a DataSnap. Restricted to DataSnap owner.")
    public ResponseEntity<DataSnap> addEditor(final @PathVariable String id,
                                              @RequestParam(required = true) String userLogin) throws Exception {
        DataSnap dataSnap = transporterService.addDataSnapEditor(getUser(), id, userLogin);
        return ResponseEntity.ok(dataSnap);
    }

    // REST Endpoint to respond to request for available snapshots
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnaps"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public List<DataSnap> getSnaps() {
        return transporterService.getDataSnaps(getUser());
    }

    // REST Endpoint to GET a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = GET)
    @ApiOperation(value = "Get snapshot by id.")
    @ResponseBody
    public DataSnap getSnap(final @PathVariable String id) throws UnauthorizedException, NotFoundException {
        return transporterService.getDataSnap(getUser(), id);
    }

    // REST Endpoint to DELETE a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = DELETE)
    @ApiOperation(value = "Delete snapshot.")
    public ResponseEntity<Void> delete(final @PathVariable String id) throws NotFoundException, UnauthorizedException {
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

    // REST Endpoint to set transporter path mapping
    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/path-mapping"}, method = POST)
    @ApiOperation(value = "Set transporter path mapping.")
    public ResponseEntity setMapping(@RequestParam final String xnatRootPath,
                                     @RequestParam final String serverRootPath,
                                     @RequestParam final String reason)
            throws ConfigServiceException, JsonProcessingException {
        transporterConfigService.setTransporterPathMapping(getUser().getLogin(), reason, xnatRootPath, serverRootPath);
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

    private UserI getUser() {
        return XDAT.getUserDetails();
    }

}
