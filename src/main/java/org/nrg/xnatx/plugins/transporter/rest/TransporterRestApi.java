package org.nrg.xnatx.plugins.transporter.rest;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.*;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
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

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Api("API for the XNAT Transporter Service")
@XapiRestController
@RequestMapping(value = "/transporter")
public class TransporterRestApi extends AbstractXapiRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;

    private TransporterService transporterService;

    @Autowired
    public TransporterRestApi(TransporterService transporterService,
                              UserManagementServiceI userManagementService,
                                 RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.transporterService = transporterService;
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
    public DataSnap getSnap(final @PathVariable String id, @RequestParam(required = false, defaultValue = "false") boolean resolved) {
        return resolved ?
                transporterService.getDataSnap(getUser(), id) :
                transporterService.getResolvedDataSnap(getUser(), id);
    }


    private UserI getUser() {
        return XDAT.getUserDetails();
    }

}
