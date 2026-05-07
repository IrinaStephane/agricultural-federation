package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.federationagricole.entity.Federation;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.FederationService;

@RestController
@RequestMapping("/federation")
@AllArgsConstructor
public class FederationController {
    private final FederationService federationService;

    @GetMapping
    public ResponseEntity<?> getFederation() {
        try {
            Federation federation = federationService.getFederation();
            return ResponseEntity.status(HttpStatus.OK).body(federation);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
