package com.mercury.mercury.reporting.controller;

import com.mercury.mercury.reporting.dto.SettlementStatisticsResponse;
import com.mercury.mercury.reporting.dto.TopEntityResponse;
import com.mercury.mercury.reporting.dto.TradingDashboardResponse;
import com.mercury.mercury.reporting.service.TradingReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Trading Reporting & Analytics Dashboard", description = "Operations reporting dashboard components providing transactional analysis analytics indicators.")
@PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
public class TradingReportingController {

    private final TradingReportingService reportingService;

    @GetMapping("/dashboard")
    @Operation(summary = "GET Dashboard Lifecycle Count Summaries", description = "Aggregates overall trade record instances structured across their transactional state parameters.")
    public ResponseEntity<TradingDashboardResponse> getDashboardSummary() {
        return ResponseEntity.ok(reportingService.getDashboardMetrics());
    }

    @GetMapping("/top-clients")
    @Operation(summary = "GET Top 5 Value-Generating Client Accounts", description = "Identifies the top accounts ordering risk concentration volumes down across calculated total values.")
    public ResponseEntity<List<TopEntityResponse>> getTopClientsReport() {
        return ResponseEntity.ok(reportingService.getTopClients());
    }

    @GetMapping("/top-instruments")
    @Operation(summary = "GET Top 5 Most Traded Market Symbols", description = "Aggregates book liquidity metrics tracking high-volume asset allocation fields.")
    public ResponseEntity<List<TopEntityResponse>> getTopInstrumentsReport() {
        return ResponseEntity.ok(reportingService.getTopInstruments());
    }

    @GetMapping("/settlement/statistics")
    @Operation(summary = "GET Operational Clearing KPI Metrics", description = "Evaluates lifecycle intervals to audit straight-through-processing clearing efficiency metrics.")
    public ResponseEntity<SettlementStatisticsResponse> getSettlementPerformanceReport() {
        return ResponseEntity.ok(reportingService.getSettlementStats());
    }
}
