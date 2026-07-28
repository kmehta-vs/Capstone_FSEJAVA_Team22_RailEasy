package com.raileasy.repository;

import com.raileasy.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("SELECT s FROM Schedule s WHERE " +
           "LOWER(s.fromStation) LIKE LOWER(CONCAT('%', :from, '%')) AND " +
           "LOWER(s.toStation) LIKE LOWER(CONCAT('%', :to, '%')) AND " +
           "s.journeyDate = :date")
    List<Schedule> searchSchedules(@Param("from") String from,
                                   @Param("to") String to,
                                   @Param("date") LocalDate date);
}
