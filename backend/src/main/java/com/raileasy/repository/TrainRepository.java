package com.raileasy.repository;

import com.raileasy.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TrainRepository extends JpaRepository<Train, UUID> {
    Optional<Train> findByTrainNumber(String trainNumber);
}
