package org.nrg.xnatx.plugins.transporter.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotRequest;
import org.nrg.xnatx.plugins.transporter.services.SnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Api("API for the XNAT Transporter Service")
@XapiRestController
@RequestMapping(value = "/transporter/snapshot")
public class SnapshotRestApi  extends AbstractXapiRestController {

    private final SnapshotService snapshotService;

    protected SnapshotRestApi(UserManagementServiceI userManagementService, RoleHolder roleHolder, SnapshotService snapshotService) {
        super(userManagementService, roleHolder);
        this.snapshotService = snapshotService;
    }


    @XapiRequestMapping(restrictTo = AccessLevel.Admin, value = {"/"}, method = POST)//, consumes = JSON)
    @ApiOperation(value = "Create a new snapshot definition. Return a resolved snapshot manifest.")
    public ResponseEntity<ResolvedSnapshot> createSnapshot(@RequestBody SnapshotRequest snapshotRequest)
            throws Exception {

        return ResponseEntity.ok(snapshotService.createSnapshot(
                snapshotRequest.toBuilder().user(getUser()).build(),false));
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
