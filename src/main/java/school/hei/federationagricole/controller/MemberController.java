package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.entity.dto.CreateMember;
import school.hei.federationagricole.entity.dto.CreateMemberPayment;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.InsufficientSponsorCount;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.exception.PaymentException;
import school.hei.federationagricole.service.MemberService;
import school.hei.federationagricole.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService  memberService;
    private final PaymentService paymentService;

    // ── POST /members  (Feature B) ────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createMembers(
            @RequestBody(required = false) List<CreateMember> body) {
        try {
            if (body == null || body.isEmpty()) {
                return ResponseEntity.badRequest().body("Request body must be a non-empty array.");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(memberService.createMembers(body));
        } catch (PaymentException | InsufficientSponsorCount e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // ── POST /members/{id}/payments  (Feature v0.0.3) ────────────────────────

    @PostMapping("/{id}/payments")
    public ResponseEntity<?> createPayments(
            @PathVariable Integer id,
            @RequestBody(required = false) List<CreateMemberPayment> body) {
        try {
            if (body == null || body.isEmpty()) {
                return ResponseEntity.badRequest().body("Request body must be a non-empty array.");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paymentService.createPayments(id, body));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}
