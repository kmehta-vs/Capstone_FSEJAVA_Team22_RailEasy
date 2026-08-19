package com.raileasy.train;

import com.raileasy.common.ConflictException;
import com.raileasy.common.NotFoundException;
import com.raileasy.train.dto.TrainRequest;
import com.raileasy.train.dto.TrainResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainServiceTest {

    @Mock
    private TrainRepository trainRepository;

    @InjectMocks
    private TrainService trainService;

    private Train train;

    @BeforeEach
    void setUp() {
        train = new Train("12163", "Chennai Express", 64);
        // id is normally assigned by JPA on persist; set it manually for this unit test
        ReflectionTestUtils.setField(train, "id", UUID.randomUUID());
    }

    @Test
    void findAll_mapsAllTrainsToResponses() {
        when(trainRepository.findAll()).thenReturn(List.of(train));

        List<TrainResponse> result = trainService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trainNumber()).isEqualTo("12163");
        assertThat(result.get(0).trainName()).isEqualTo("Chennai Express");
        assertThat(result.get(0).seatsPerClass()).isEqualTo(64);
    }

    @Test
    void create_savesNewTrain_whenTrainNumberIsUnique() {
        TrainRequest request = new TrainRequest("22691", "Rajdhani Express", 64);
        when(trainRepository.existsByTrainNumber("22691")).thenReturn(false);
        when(trainRepository.save(any(Train.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainResponse response = trainService.create(request);

        assertThat(response.trainNumber()).isEqualTo("22691");
        assertThat(response.trainName()).isEqualTo("Rajdhani Express");
        verify(trainRepository).save(any(Train.class));
    }

    @Test
    void create_throwsConflict_whenTrainNumberAlreadyExists() {
        TrainRequest request = new TrainRequest("12163", "Chennai Express", 64);
        when(trainRepository.existsByTrainNumber("12163")).thenReturn(true);

        assertThatThrownBy(() -> trainService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("12163");
        verify(trainRepository, never()).save(any());
    }

    @Test
    void update_updatesFields_whenTrainExistsAndNumberUnchanged() {
        UUID id = train.getId();
        TrainRequest request = new TrainRequest("12163", "Chennai Superfast Express", 64);
        when(trainRepository.findById(id)).thenReturn(Optional.of(train));
        when(trainRepository.findByTrainNumber("12163")).thenReturn(Optional.of(train));
        when(trainRepository.save(any(Train.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainResponse response = trainService.update(id, request);

        assertThat(response.trainName()).isEqualTo("Chennai Superfast Express");
    }

    @Test
    void update_throwsNotFound_whenTrainDoesNotExist() {
        UUID id = UUID.randomUUID();
        TrainRequest request = new TrainRequest("12163", "Chennai Express", 64);
        when(trainRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.update(id, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throwsConflict_whenNewTrainNumberBelongsToAnotherTrain() {
        UUID id = UUID.randomUUID();
        Train other = new Train("22691", "Rajdhani Express", 64);
        ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        TrainRequest request = new TrainRequest("22691", "Chennai Express", 64);
        when(trainRepository.findById(id)).thenReturn(Optional.of(train));
        when(trainRepository.findByTrainNumber("22691")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> trainService.update(id, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void delete_removesTrain_whenItExists() {
        UUID id = UUID.randomUUID();
        when(trainRepository.existsById(id)).thenReturn(true);

        trainService.delete(id);

        verify(trainRepository, times(1)).deleteById(id);
    }

    @Test
    void delete_throwsNotFound_whenTrainDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(trainRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> trainService.delete(id))
                .isInstanceOf(NotFoundException.class);
        verify(trainRepository, never()).deleteById(any());
    }
}
