package com.raileasy.repository;

import com.raileasy.entity.Booking;
import com.raileasy.enums.BookingStatus;
import com.raileasy.enums.TravelClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByScheduleIdAndTravelClassAndStatus(UUID scheduleId,
                                                          TravelClass travelClass,
                                                          BookingStatus status);

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
