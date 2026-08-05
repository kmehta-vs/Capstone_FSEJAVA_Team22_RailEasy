package com.raileasy.train;

import com.raileasy.common.ConflictException;
import com.raileasy.common.NotFoundException;
import com.raileasy.train.dto.TrainRequest;
import com.raileasy.train.dto.TrainResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read + admin CRUD operations for trains.
 */
@Service
public class TrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    public List<TrainResponse> findAll() {
        return trainRepository.findAll().stream()
                .map(TrainService::toResponse)
                .toList();
    }

    @Transactional
    public TrainResponse create(TrainRequest request) {
        if (trainRepository.existsByTrainNumber(request.trainNumber())) {
            throw new ConflictException("Train number already exists: " + request.trainNumber());
        }
        Train train = new Train(request.trainNumber(), request.trainName(), request.seatsPerClass());
        return toResponse(trainRepository.save(train));
    }

    @Transactional
    public TrainResponse update(UUID id, TrainRequest request) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Train not found: " + id));
        trainRepository.findByTrainNumber(request.trainNumber())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Train number already exists: " + request.trainNumber());
                });
        train.setTrainNumber(request.trainNumber());
        train.setTrainName(request.trainName());
        train.setSeatsPerClass(request.seatsPerClass());
        return toResponse(trainRepository.save(train));
    }

    @Transactional
    public void delete(UUID id) {
        if (!trainRepository.existsById(id)) {
            throw new NotFoundException("Train not found: " + id);
        }
        trainRepository.deleteById(id);
    }

    static TrainResponse toResponse(Train train) {
        return new TrainResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getTrainName(),
                train.getSeatsPerClass()
        );
    }
}
