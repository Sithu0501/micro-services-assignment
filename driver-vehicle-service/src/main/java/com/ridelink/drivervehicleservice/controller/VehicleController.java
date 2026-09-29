package com.ridelink.drivervehicleservice.controller;

import com.ridelink.drivervehicleservice.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.response.ApiResponse;
import com.ridelink.drivervehicleservice.dto.response.VehicleResponse;
import com.ridelink.drivervehicleservice.exception.ErrorResponse;
import com.ridelink.drivervehicleservice.security.UserPrincipal;
import com.ridelink.drivervehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller exposing REST endpoints for driver vehicle registration, updates,
 * retrieval, and deletion.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for registering, viewing, modifying, and removing vehicles")
@SecurityRequirement(name = "BearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    private String extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle", description = "Associates a new vehicle with the authenticated driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Vehicle registered successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
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
                    responseCode = "404",
                    description = "Not Found - Driver profile does not exist yet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Vehicle registration plate already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        VehicleResponse response = vehicleService.registerVehicle(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get vehicles for authenticated driver", description = "Retrieves all vehicles registered under the current driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicles retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMyVehicles(Authentication authentication) {
        String userId = extractUserId(authentication);
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Direct vehicle lookup by internal ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable String id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Updates details of a vehicle owned by the authenticated driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Driver does not own this vehicle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody UpdateVehicleRequest request,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        VehicleResponse response = vehicleService.updateVehicle(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Removes a vehicle owned by the authenticated driver.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Driver does not own this vehicle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = extractUserId(authentication);
        vehicleService.deleteVehicle(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }
}
