package org.nrg.xnatx.plugins.transporter.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
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
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    @ApiOperation(value = "Accepts and processes the DataSnap payload.")
    public ResponseEntity<String> postSnap(final @RequestBody DataSnap dataSnap) {
        Boolean success = transporterService.storeDataSnap(getUser(), dataSnap);
        return success ?
                ResponseEntity.ok("DataSnap successfully received and processed!") :
                ResponseEntity.status(500).body("DataSnap failed to be processed.");
    }

    // REST Endpoint to respond to request for available snapshots
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public List<DataSnap> getSnaps() {
        return transporterService.getDataSnaps(getUser());
    }

    // REST Endpoint to GET a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public DataSnap getSnap(final @PathVariable String id,
                            @RequestParam(required = false, defaultValue = "false") boolean resolved) {
        return resolved ?
                transporterService.getResolvedDataSnap(getUser(), id) :
                transporterService.getDataSnap(getUser(), id);
    }

    // REST Endpoint to DELETE a particular snapshot for a given user
    @XapiRequestMapping(restrictTo = AccessLevel.Authenticated, value = {"/datasnap/{id}"}, method = DELETE)
    @ApiOperation(value = "Delete snapshot.")
    public ResponseEntity<Void> delete(final @PathVariable String id) throws NotFoundException, UnauthorizedException {
        transporterService.deleteDataSnap(getUser(), String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    // REST Endpoint to set transporter path mapping
    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/path-mapping"}, method = POST)
    @ApiOperation(value = "Set transporter path mapping.")
    public ResponseEntity setMapping(@RequestParam final String xnatRootPath, @RequestParam final String serverRootPath,
            @RequestParam final String reason) throws ConfigServiceException, JsonProcessingException {
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
    public ResponseEntity mirror(final @PathVariable String id)
            throws NotFoundException, RuntimeException, UnauthorizedException, IOException {
        transporterService.mirrorDataSnap(getUser(), id);
        return ResponseEntity.ok().build();
    }

    private UserI getUser() {
        return XDAT.getUserDetails();
    }

}
