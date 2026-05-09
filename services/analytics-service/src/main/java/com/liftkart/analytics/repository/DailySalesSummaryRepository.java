package com.liftkart.analytics.repository;

import com.liftkart.analytics.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailySalesSummaryRepository
        extends JpaRepository<DailySalesSummary, UUID> {

    Optional<DailySalesSummary> findByVendorIdAndSummaryDate(
            UUID vendorId, LocalDate date);

    Optional<DailySalesSummary> findByVendorIdIsNullAndSummaryDate(
            LocalDate date);

    List<DailySalesSummary> findByVendorIdAndSummaryDateBetweenOrderBySummaryDateAsc(
            UUID vendorId, LocalDate from, LocalDate to);

    @Query("SELECT d FROM DailySalesSummary d " +
            "WHERE d.vendorId IS NULL " +
            "AND d.summaryDate BETWEEN :from AND :to " +
            "ORDER BY d.summaryDate ASC")
    List<DailySalesSummary> findPlatformSummary(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}