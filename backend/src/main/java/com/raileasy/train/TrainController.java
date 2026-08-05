package com.raileasy.train;

import com.raileasy.train.dto.TrainRequest;
import com.raileasy.train.dto.TrainResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trains")
@Tag(name = "Trains", description = "Train catalogue")
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping
    @Operation(summary = "List all trains")
    public List<TrainResponse> getTrains() {
        return trainService.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a train (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TrainResponse> create(@Valid @RequestBody TrainRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a train (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public TrainResponse update(@PathVariable("id") UUID id, @Valid @RequestBody TrainRequest request) {
        return trainService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a train (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        trainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
