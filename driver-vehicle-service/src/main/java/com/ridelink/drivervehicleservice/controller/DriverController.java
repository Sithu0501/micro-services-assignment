package com.ridelink.drivervehicleservice.controller;

import com.ridelink.drivervehicleservice.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicleservice.dto.response.ApiResponse;
import com.ridelink.drivervehicleservice.dto.response.DriverResponse;
import com.ridelink.drivervehicleservice.dto.response.EligibleDriverResponse;
import com.ridelink.drivervehicleservice.exception.ErrorResponse;
import com.ridelink.drivervehicleservice.security.UserPrincipal;
import com.ridelink.drivervehicleservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing REST endpoints for driver operational profiles, availability toggling,
 * simulated GPS location tracking, and eligible driver discovery.
 */
@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Driver Management", description = "Endpoints for driver profiles, availability status, simulated location, and ride dispatch eligibility")
@SecurityRequirement(name = "BearerAuth")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    private String extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return authentication != null ? authentication.getName() : null;
    }

    // =========================================================================
    // DRIVER PROFILE ENDPOINTS
    // =========================================================================

    @PostMapping
    @Operation(summary = "Create driver operational profile", description = "Initializes an operational profile for the authenticated driver account.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Driver profile created successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error on input fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Profile or license already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> createProfile(
            @Valid @RequestBody CreateDriverRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        DriverResponse response = driverService.createDriverProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver operational profile created successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current driver profile", description = "Retrieves the operational profile of the authenticated driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Driver profile not created yet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> getMyProfile(Authentication authentication) {
        String userId = extractUserId(authentication);
        DriverResponse response = driverService.getDriverProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Driver profile retrieved successfully", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current driver profile", description = "Updates operational contact, license expiry, or service area for the authenticated driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Driver profile updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Driver profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> updateMyProfile(
            @Valid @RequestBody UpdateDriverRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        DriverResponse response = driverService.updateDriverProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Driver profile updated successfully", response));
    }

    @PatchMapping("/me/availability")
    @Operation(summary = "Update driver availability status", description = "Toggles driver availability between AVAILABLE, UNAVAILABLE, and ON_TRIP.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Availability status updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Driver has no registered vehicles",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Driver profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> updateAvailability(
            @Valid @RequestBody UpdateAvailabilityRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        DriverResponse response = driverService.updateAvailabilityStatus(userId, request.status());
        return ResponseEntity.ok(ApiResponse.success("Availability status updated successfully", response));
    }

    @PutMapping("/me/location")
    @Operation(summary = "Update simulated GPS location", description = "Updates the driver's current simulated latitude and longitude.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Location updated successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid latitude or longitude",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Driver profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        DriverResponse response = driverService.updateCurrentLocation(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", response));
    }

    // =========================================================================
    // DISCOVERY & INTER-SERVICE ENDPOINTS
    // =========================================================================

    @GetMapping("/eligible")
    @Operation(summary = "Retrieve eligible available drivers", description = "Endpoint used by Ride Management Service to find eligible drivers based on availability, service area, and proximity.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Eligible drivers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EligibleDriverResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<EligibleDriverResponse>>> getEligibleDrivers(
            @Parameter(description = "Filter by service area name (e.g., Colombo)")
            @RequestParam(required = false) String serviceArea,
            @Parameter(description = "Pickup latitude for proximity search")
            @RequestParam(required = false) Double latitude,
            @Parameter(description = "Pickup longitude for proximity search")
            @RequestParam(required = false) Double longitude,
            @Parameter(description = "Maximum search radius in kilometers (default: 10.0)")
            @RequestParam(required = false, defaultValue = "10.0") Double radius
    ) {
        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(serviceArea, latitude, longitude, radius);
        return ResponseEntity.ok(ApiResponse.success("Eligible drivers retrieved successfully", eligible));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver profile by ID", description = "Direct lookup by internal driver ID, utilized for inter-service communication.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Driver retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DriverResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(
            @PathVariable String id
    ) {
        DriverResponse response = driverService.getDriverProfileById(id);
        return ResponseEntity.ok(ApiResponse.success("Driver retrieved successfully", response));
    }
}
