package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.entity.dto.CollectivityIdentificationRequest;
import school.hei.federationagricole.entity.dto.CollectivityResponse;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.CollectivityAlreadyIdentifiedException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.CollectivityService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService service;

    // ── POST /collectivities  (Feature A) ────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createCollectivities(
            @RequestBody(required = false) List<CreateCollectivity> createCollectivities) {
        try {
            if (createCollectivities == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Mandatory body not provided");
            }
            List<CollectivityResponse> collectivities = service.createCollectivities(createCollectivities);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ── PUT /collectivities/{id}  (Feature J) ────────────────────────────────

    /**
     * The federation attributes a unique number and name to a collectivity.
     * Once assigned, number and name are immutable → returns 409 if an attempt
     * is made to change them.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> identifyCollectivity(
            @PathVariable Integer id,
            @RequestBody(required = false) CollectivityIdentificationRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Request body with 'number' and 'name' is required.");
            }
            CollectivityResponse response = service.identifyCollectivity(id, request);
            return ResponseEntity.ok(response);
        } catch (CollectivityAlreadyIdentifiedException e) {
            // 409 Conflict: number or name was already assigned and is immutable
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}