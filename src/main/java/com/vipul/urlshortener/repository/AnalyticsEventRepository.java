package com.vipul.urlshortener.repository;

import com.vipul.urlshortener.entity.AnalyticsEvent;
import com.vipul.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    Long countByUrlMapping(UrlMapping urlMapping);

    Long countByUrlMappingAndOccurredAtBetween(UrlMapping urlMapping, LocalDateTime start, LocalDateTime end);

    List<AnalyticsEvent> findTop10ByUrlMappingOrderByOccurredAtDesc(UrlMapping urlMapping);

    @Query(value = "SELECT date_trunc('day', occurred_at) as day, count(*) as cnt FROM analytics_event WHERE url_id = :urlId AND occurred_at >= :start AND occurred_at < :end GROUP BY day ORDER BY day", nativeQuery = true)
    List<Object[]> countDaily(@Param("urlId") Long urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}