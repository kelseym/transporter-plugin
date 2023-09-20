package org.nrg.xnatx.plugins.transporter.rest;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.*;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("deprecation")
@Slf4j
@Api("API for the XNAT Transporter Service")
@XapiRestController
public class TransporterRestApi extends AbstractXapiRestController {

    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;
    private static final DataSnap DATASNAP = DataSnap.builder()
            .id(1L)
            .name("Test Snap")
            .path("/tmp")
            .description("Test data snap.")
            .build();

    private static final DataSnap DATASNAP2 = DataSnap.builder()
            .id(2L)
            .name("Test Snap2")
            .path("/tmp2")
            .description("2nd test data snap.")
            .build();


    @Autowired
    public TransporterRestApi(UserManagementServiceI userManagementService,
                                 RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    // REST Endpoint to verify availability of transporter API
    @XapiRequestMapping(value = {"/ping"}, method = GET)
    @ApiOperation(value = "Ping the XNAT Transporter API.", notes = "Returns \"OK\" on success.")
    @ResponseBody
    public String ping() {
        return HttpStatus.OK.toString();
    }

    // REST Endpoint to respond to request for available snapshots
    @XapiRequestMapping(value = {"/snapshots"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public List<DataSnap> getSnapshots() {
        // Test code - return a single DataSnap object
        return Arrays.asList(DATASNAP, DATASNAP2);
    }

    // REST Endpoint to GET a particular snapshot for a given user
    @XapiRequestMapping(value = {"/snapshots/{id}"}, method = GET)
    @ApiOperation(value = "Get available snapshots.")
    @ResponseBody
    public DataSnap getSnapshot(final String id) {
        // Test code - return a single DataSnap object from the list
        return Arrays.asList(DATASNAP, DATASNAP2).
                stream().filter(ds -> ds.getId().equals(Long.valueOf(id))).findFirst().get();
    }




}
