package school.hei.federationagricole.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.federationagricole.controller.dto.CreateCollectivity;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.service.CollectivityService;

import java.util.List;

@RestController
@RequestMapping("/collectivities")
public class CollectivityController {
    private final CollectivityService collectivityService;

    public CollectivityController(CollectivityService collectivityService) {
        this.collectivityService = collectivityService;
    }

    @PostMapping
    public ResponseEntity<List<Collectivity>> createCollectivities(@RequestBody List<CreateCollectivity> collectivities) {
        return new ResponseEntity<>(collectivityService.createCollectivities(collectivities), HttpStatus.CREATED);
    }
}
