package school.hei.federationagricole.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.federationagricole.entity.dto.CollectivityLocalStatistics;
import school.hei.federationagricole.entity.dto.CollectivityOverallStatistics;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.service.StatisticsService;

import java.time.LocalDate;
import java.util.List;

/**
 * Statistics controller.
 *
 * NOTE: The OpenAPI spec (v0.0.5) uses the path prefix "/collectivites" (without the "i"
 * before "tes"), which differs from the "/collectivities" prefix used everywhere else.
 * This controller faithfully mirrors the spec paths:
 *   - GET /collectivites/{id}/statistics   (local statistics – feature G)
 *   - GET /collectivites/statistics        (overall statistics – feature H)
 */
@RestController
@AllArgsConstructor
@RequestMapping("/collectivites")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * GET /collectivites/{id}/statistics
     *
     * Returns per-member statistics for a specific collectivity:
     * – earnedAmount : total payments made by each active member during [from, to]
     * – unpaidAmount : potential unpaid amount based on ACTIVE membership fees
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getLocalStatistics(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest()
                        .body("'from' date must be before or equal to 'to' date.");
            }
            List<CollectivityLocalStatistics> stats =
                    statisticsService.getLocalStatistics(id, from, to);
            return ResponseEntity.ok(stats);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * GET /collectivites/statistics
     *
     * Returns cross-collectivity statistics for all collectivities:
     * – newMembersNumber                 : members who joined each collectivity during [from, to]
     * – overallMemberCurrentDuePercentage: % of members up-to-date on their active fees
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOverallStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest()
                        .body("'from' date must be before or equal to 'to' date.");
            }
            List<CollectivityOverallStatistics> stats =
                    statisticsService.getOverallStatistics(from, to);
            return ResponseEntity.ok(stats);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}