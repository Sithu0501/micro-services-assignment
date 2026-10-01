/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.Valid
 *  org.springframework.data.domain.Page
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.GrantedAuthority
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.dto.request.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideStatusRequest;
import com.ridelink.ridemanagement.dto.response.ApiResponse;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.dto.response.RideSummaryResponse;
import com.ridelink.ridemanagement.exception.ErrorResponse;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.security.UserPrincipal;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/v1/rides"})
@Tag(name="Ride Management", description="Endpoints for ride requests, lifecycle state management, driver dispatch, and ride history")
@SecurityRequirement(name="BearerAuth")
public class RideController {
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    private UserPrincipal extractPrincipal(Authentication authentication) {
        Object object;
        if (authentication != null && (object = authentication.getPrincipal()) instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal)object;
            return principal;
        }
        if (authentication != null && authentication.getName() != null) {
            String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_PASSENGER").replace("ROLE_", "");
            return new UserPrincipal(authentication.getName(), authentication.getName(), role);
        }
        throw new AccessDeniedException("Full authentication is required to access this resource");
    }

    private String extractToken(Authentication authentication) {
        Object object;
        if (authentication != null && (object = authentication.getCredentials()) instanceof String) {
            String token = (String)object;
            return token;
        }
        return null;
    }

    @PostMapping
    @PreAuthorize(value="hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary="Request a new ride", description="Initializes a new ride request with pickup and destination coordinates. Automatically sets status to REQUESTED.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Ride created successfully", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - Validation error", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - Missing or invalid token", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Passenger not found in Account Service", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Passenger already has an active ride", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503", description="Service Unavailable - Account Service unreachable", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> createRide(@Valid @RequestBody CreateRideRequest request, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        String token = this.extractToken(authentication);
        RideResponse response = this.rideService.createRide(request, principal, token);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(ApiResponse.success("Ride requested successfully", response));
    }

    @GetMapping(value={"/{id}"})
    @Operation(summary="Get ride by ID", description="Retrieves full details of a specific ride. Accessible by the owning passenger, assigned driver, or ADMIN.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride retrieved successfully", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - Missing or invalid token", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not permitted to view another user's ride", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride ID does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> getRideById(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.getRideById(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary="List rides", description="Lists rides with optional filtering by status, passenger, or driver. ADMIN can list all; non-admins view only their own records.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Rides retrieved successfully", content={@Content(schema=@Schema(implementation=RideSummaryResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - Missing or invalid token", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<Page<RideSummaryResponse>>> listRides(@Parameter(description="Filter by ride lifecycle status") @RequestParam(required=false) RideStatus status, @Parameter(description="Filter by passenger user ID (Admin only)") @RequestParam(required=false) String passengerId, @Parameter(description="Filter by driver profile ID (Admin only)") @RequestParam(required=false) String driverId, @Parameter(description="Page index (0-based, default: 0)") @RequestParam(required=false, defaultValue="0") int page, @Parameter(description="Page size (default: 20, max: 100)") @RequestParam(required=false, defaultValue="20") int size, @Parameter(description="Sort direction by requested time: ASC or DESC (default: DESC)") @RequestParam(required=false, defaultValue="DESC") String sort, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        Page<RideSummaryResponse> response = this.rideService.listRides(status, passengerId, driverId, page, size, sort, principal);
        return ResponseEntity.ok(ApiResponse.success("Rides retrieved successfully", response));
    }

    @PutMapping(value={"/{id}"})
    @Operation(summary="Update editable ride locations", description="Updates pickup and destination coordinates. Only permitted while ride is in REQUESTED status.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride updated successfully", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - Validation error", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not ride owner", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Ride is no longer in editable REQUESTED status", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> updateRide(@PathVariable String id, @Valid @RequestBody UpdateRideRequest request, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.updateRide(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride updated successfully", response));
    }

    @DeleteMapping(value={"/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    @Operation(summary="Delete ride record (Admin only)", description="Permanently deletes a ride document from 'ridelink_ride_db'. Restricted to ADMIN role.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Ride deleted successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Requires ADMIN role", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<Void> deleteRide(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        this.rideService.deleteRide(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value={"/{id}/assign-driver"})
    @PreAuthorize(value="hasRole('ADMIN')")
    @Operation(summary="Manually assign driver to ride (Admin only)", description="Assigns an available driver and optional vehicle to a REQUESTED ride.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Driver assigned successfully", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - Validation error", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Requires ADMIN role", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride or driver does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Driver unavailable, busy on active ride, or ride not in REQUESTED status", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503", description="Service Unavailable - Driver Service unreachable", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> assignDriver(@PathVariable String id, @Valid @RequestBody AssignDriverRequest request, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        String token = this.extractToken(authentication);
        RideResponse response = this.rideService.assignDriver(id, request, principal, token);
        return ResponseEntity.ok(ApiResponse.success("Driver assigned successfully", response));
    }

    @PostMapping(value={"/{id}/auto-assign"})
    @Operation(summary="Automatically assign closest available driver", description="Dispatches the nearest eligible available driver via Driver & Vehicle Service discovery.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Driver automatically assigned", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Unauthorized caller", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - No eligible drivers available or ride not in REQUESTED status", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="503", description="Service Unavailable - Driver Service unreachable", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> autoAssignDriver(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        String token = this.extractToken(authentication);
        RideResponse response = this.rideService.autoAssignDriver(id, principal, token);
        return ResponseEntity.ok(ApiResponse.success("Driver automatically assigned successfully", response));
    }

    @PatchMapping(value={"/{id}/accept"})
    @PreAuthorize(value="hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary="Driver accepts ride (ASSIGNED -> ACCEPTED)", description="Invoked by the assigned driver to accept the dispatch and initiate pickup.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride accepted", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not assigned driver", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Illegal status transition (must be ASSIGNED)", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.acceptRide(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride accepted successfully", response));
    }

    @PatchMapping(value={"/{id}/start"})
    @PreAuthorize(value="hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary="Driver starts ride (ACCEPTED -> IN_PROGRESS)", description="Invoked by the assigned driver when the passenger has been boarded and trip begins.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride started", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not assigned driver", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Illegal status transition (must be ACCEPTED)", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> startRide(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.startRide(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride started successfully", response));
    }

    @PatchMapping(value={"/{id}/complete"})
    @PreAuthorize(value="hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary="Driver completes ride (IN_PROGRESS -> COMPLETED)", description="Invoked by the assigned driver upon reaching the destination. Terminal state.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride completed", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not assigned driver", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Illegal status transition (must be IN_PROGRESS)", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> completeRide(@PathVariable String id, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.completeRide(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride completed successfully", response));
    }

    @PatchMapping(value={"/{id}/status"})
    @Operation(summary="Update ride lifecycle status", description="Generic endpoint for transitioning ride through lifecycle states.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Status updated", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - Validation error", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Unauthorized role for this transition", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Illegal status transition", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> updateRideStatus(@PathVariable String id, @Valid @RequestBody UpdateRideStatusRequest request, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.updateRideStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride status updated successfully", response));
    }

    @PatchMapping(value={"/{id}/cancel"})
    @Operation(summary="Cancel ride", description="Cancels a ride in REQUESTED, ASSIGNED, or ACCEPTED status with a documented reason. Sets status to CANCELLED.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Ride cancelled successfully", content={@Content(schema=@Schema(implementation=RideResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - Missing or invalid cancellation reason", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Not authorized to cancel this ride", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Not Found - Ride does not exist", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - Cannot cancel ride in IN_PROGRESS or terminal state", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<RideResponse>> cancelRide(@PathVariable String id, @Valid @RequestBody CancelRideRequest request, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        RideResponse response = this.rideService.cancelRide(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Ride cancelled successfully", response));
    }

    @GetMapping(value={"/passenger/{passengerId}"})
    @Operation(summary="Get passenger ride history", description="Retrieves all rides requested by the specified passenger. Accessible by the passenger or ADMIN.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Passenger ride history retrieved", content={@Content(schema=@Schema(implementation=RideSummaryResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Cannot access another passenger's history", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<Page<RideSummaryResponse>>> getPassengerRideHistory(@PathVariable String passengerId, @Parameter(description="Page index (default: 0)") @RequestParam(required=false, defaultValue="0") int page, @Parameter(description="Page size (default: 20)") @RequestParam(required=false, defaultValue="20") int size, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        Page<RideSummaryResponse> response = this.rideService.getPassengerRideHistory(passengerId, page, size, principal);
        return ResponseEntity.ok(ApiResponse.success("Passenger ride history retrieved successfully", response));
    }

    @GetMapping(value={"/driver/{driverId}"})
    @Operation(summary="Get driver ride history", description="Retrieves all rides assigned to the specified driver. Accessible by the driver or ADMIN.")
    @ApiResponses(value={@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Driver ride history retrieved", content={@Content(schema=@Schema(implementation=RideSummaryResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized", content={@Content(schema=@Schema(implementation=ErrorResponse.class))}), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - Cannot access another driver's history", content={@Content(schema=@Schema(implementation=ErrorResponse.class))})})
    public ResponseEntity<ApiResponse<Page<RideSummaryResponse>>> getDriverRideHistory(@PathVariable String driverId, @Parameter(description="Page index (default: 0)") @RequestParam(required=false, defaultValue="0") int page, @Parameter(description="Page size (default: 20)") @RequestParam(required=false, defaultValue="20") int size, Authentication authentication) {
        UserPrincipal principal = this.extractPrincipal(authentication);
        Page<RideSummaryResponse> response = this.rideService.getDriverRideHistory(driverId, page, size, principal);
        return ResponseEntity.ok(ApiResponse.success("Driver ride history retrieved successfully", response));
    }
}

