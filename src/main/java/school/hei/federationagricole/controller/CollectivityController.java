package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.entity.dto.CollectivityIdentificationRequest;
import school.hei.federationagricole.entity.dto.CollectivityResponse;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.entity.dto.CreateMembershipFee;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.CollectivityAlreadyIdentifiedException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.CollectivityService;
import school.hei.federationagricole.service.FinancialAccountService;
import school.hei.federationagricole.service.MembershipFeeService;
import school.hei.federationagricole.service.TransactionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService    collectivityService;
    private final MembershipFeeService   membershipFeeService;
    private final TransactionService     transactionService;
    private final FinancialAccountService financialAccountService;

    @PostMapping
    public ResponseEntity<?> createCollectivities(
            @RequestBody(required = false) List<CreateCollectivity> body) {
        try {
            if (body == null || body.isEmpty()) {
                return ResponseEntity.badRequest().body("Request body must be a non-empty array.");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(collectivityService.createCollectivities(body));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/informations")
    public ResponseEntity<?> identifyCollectivity(
            @PathVariable Integer id,
            @RequestBody(required = false) CollectivityIdentificationRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body("Request body with 'number' and 'name' is required.");
            }
            CollectivityResponse response = collectivityService.identifyCollectivity(id, request);
            return ResponseEntity.ok(response);
        } catch (CollectivityAlreadyIdentifiedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(membershipFeeService.getByCollectivity(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(
            @PathVariable Integer id,
            @RequestBody(required = false) List<CreateMembershipFee> body) {
        try {
            if (body == null || body.isEmpty()) {
                return ResponseEntity.badRequest().body("Request body must be a non-empty array.");
            }
            return ResponseEntity.ok(membershipFeeService.create(id, body));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity.ok(transactionService.getTransactions(id, from, to));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // Feature D complement: GET /collectivities/{id}/financialAccounts
    @GetMapping("/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate at) {
        try {
            return ResponseEntity.ok(financialAccountService.getByCollectivity(id, at));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // Feature A complement: GET /collectivities/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivity(@PathVariable Integer id) {
        try {
            CollectivityResponse response = collectivityService.getById(id);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}