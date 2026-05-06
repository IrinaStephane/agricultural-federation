package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.StatisticsService;

import java.time.LocalDate;


@RestController
@AllArgsConstructor
@RequestMapping("/collectivites")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getLocalStatistics(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            if (from == null || to == null) {
                return ResponseEntity.badRequest()
                        .body("Query parameters 'from' and 'to' are required.");
            }
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest()
                        .body("'from' date must not be after 'to' date.");
            }
            return ResponseEntity.ok(statisticsService.getLocalStatistics(id, from, to));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getOverallStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            if (from == null || to == null) {
                return ResponseEntity.badRequest()
                        .body("Query parameters 'from' and 'to' are required.");
            }
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest()
                        .body("'from' date must not be after 'to' date.");
            }
            return ResponseEntity.ok(statisticsService.getOverallStatistics(from, to));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}