package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import school.hei.federationagricole.entity.dto.CreateMember;
import school.hei.federationagricole.exception.InsufficientSponsorCount;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.exception.PaymentException;
import school.hei.federationagricole.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService service;

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody List<CreateMember> members) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(service.createMembers(members));

        } catch (PaymentException | InsufficientSponsorCount ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());

        } catch (NotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        } catch (RuntimeException ex) {
            String detail = ex.getMessage();
            if (ex.getCause() != null) detail += " | cause: " + ex.getCause().getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + detail);
        }
    }
}