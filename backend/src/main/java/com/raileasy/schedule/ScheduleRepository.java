package com.raileasy.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    /**
     * Case-insensitive search by from/to station and journey date.
     */
    @Query("""
            SELECT s FROM Schedule s
            WHERE LOWER(s.fromStation) = LOWER(:from)
              AND LOWER(s.toStation) = LOWER(:to)
              AND s.journeyDate = :date
            ORDER BY s.departureTime ASC
            """)
    List<Schedule> search(@Param("from") String from,
                          @Param("to") String to,
                          @Param("date") LocalDate date);
}
