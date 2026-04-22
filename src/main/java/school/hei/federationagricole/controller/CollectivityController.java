package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.entity.CollectivityTransaction;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.dto.CollectivityIdentificationRequest;
import school.hei.federationagricole.entity.dto.CollectivityResponse;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.entity.dto.CreateMembershipFee;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.CollectivityAlreadyIdentifiedException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.CollectivityService;
import school.hei.federationagricole.service.MembershipFeeService;
import school.hei.federationagricole.service.TransactionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService collectivityService;
    private final MembershipFeeService membershipFeeService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createCollectivities(
            @RequestBody(required = false) List<CreateCollectivity> createCollectivities) {
        try {
            if (createCollectivities == null || createCollectivities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Mandatory body not provided");
            }

            List<CollectivityResponse> collectivities =
                    collectivityService.createCollectivities(createCollectivities);

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

    @PutMapping("/{id}/informations")
    public ResponseEntity<?> identifyCollectivity(
            @PathVariable Integer id,
            @RequestBody(required = false) CollectivityIdentificationRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Request body with 'number' and 'name' is required.");
            }

            CollectivityResponse response = collectivityService.identifyCollectivity(id, request);
            return ResponseEntity.ok(response);

        } catch (CollectivityAlreadyIdentifiedException e) {
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

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable Integer id) {
        try {
            List<MembershipFee> fees = membershipFeeService.getByCollectivity(id);
            return ResponseEntity.ok(fees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(
            @PathVariable Integer id,
            @RequestBody(required = false) List<CreateMembershipFee> dtos) {
        try {
            if (dtos == null || dtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Mandatory body not provided");
            }

            List<MembershipFee> created = membershipFeeService.create(id, dtos);
            return ResponseEntity.ok(created);

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable Integer id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        try {
            List<CollectivityTransaction> transactions =
                    transactionService.getTransactions(id, from, to);
            return ResponseEntity.ok(transactions);

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}