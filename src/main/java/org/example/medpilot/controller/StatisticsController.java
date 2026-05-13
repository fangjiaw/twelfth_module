package org.example.medpilot.controller;

import org.example.medpilot.dto.MissedAlert;
import org.example.medpilot.mapper.MedicationPlanMapper;
import org.example.medpilot.mapper.MedicationRecordMapper;
import org.example.medpilot.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationRecordMapper medicationRecordMapper;

    public StatisticsController(StatisticsService statisticsService,
                                MedicationPlanMapper medicationPlanMapper,
                                MedicationRecordMapper medicationRecordMapper) {
        this.statisticsService = statisticsService;
        this.medicationPlanMapper = medicationPlanMapper;
        this.medicationRecordMapper = medicationRecordMapper;
    }

    /**
     * 获取漏服提醒
     */
    @GetMapping("/missedAlerts")
    public Map<String, Object> getMissedAlerts(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MissedAlert> alerts = statisticsService.getMissedAlerts(userId, startDate, endDate);
        return buildSuccessResponse(alerts);
    }

    /**
     * 获取所有统计数据（一次性获取，用于图表页面）
     */
    @GetMapping("/all")
    public Map<String, Object> getAllStatistics(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") Integer trendDays,
            @RequestParam(defaultValue = "5") Integer topDrugLimit) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(trendDays - 1);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("summary", statisticsService.getStatisticsSummary(userId));
        result.put("trend", statisticsService.getDailyTrend(userId, trendDays));
        result.put("topDrugs", statisticsService.getTopDrugs(userId, topDrugLimit));
        result.put("missedAlerts", statisticsService.getMissedAlerts(userId, startDate, today));
        result.put("todayPlans", medicationPlanMapper.selectPlansByUserIdAndDate(userId, today));
        result.put("todayRecords", medicationRecordMapper.selectByUserIdAndDate(userId, today));
        result.put("activePlanCount", medicationPlanMapper.countActivePlans(userId));

        return buildSuccessResponse(result);
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
