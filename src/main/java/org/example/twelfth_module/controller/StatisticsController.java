package org.example.twelfth_module.controller;

import org.example.twelfth_module.dto.DailyStats;
import org.example.twelfth_module.dto.DrugUsageStat;
import org.example.twelfth_module.dto.MissedAlert;
import org.example.twelfth_module.dto.StatisticsSummary;
import org.example.twelfth_module.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取打卡率
     */
    @GetMapping("/checkInRate")
    public Map<String, Object> getCheckInRate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double rate = statisticsService.getCheckInRate(userId, startDate, endDate);
        return buildSuccessResponse("checkInRate", rate);
    }

    /**
     * 获取漏服率
     */
    @GetMapping("/missedRate")
    public Map<String, Object> getMissedRate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double rate = statisticsService.getMissedRate(userId, startDate, endDate);
        return buildSuccessResponse("missedRate", rate);
    }

    /**
     * 获取达标率
     */
    @GetMapping("/complianceRate")
    public Map<String, Object> getComplianceRate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double rate = statisticsService.getComplianceRate(userId, startDate, endDate);
        return buildSuccessResponse("complianceRate", rate);
    }

    /**
     * 获取连续打卡天数
     */
    @GetMapping("/consecutiveDays")
    public Map<String, Object> getConsecutiveDays(@RequestParam Long userId) {
        Integer days = statisticsService.getConsecutiveDays(userId);
        return buildSuccessResponse("consecutiveDays", days);
    }

    /**
     * 获取最长连续天数
     */
    @GetMapping("/longestStreak")
    public Map<String, Object> getLongestStreak(@RequestParam Long userId) {
        Integer days = statisticsService.getLongestStreak(userId);
        return buildSuccessResponse("longestStreak", days);
    }

    /**
     * 获取时段统计
     */
    @GetMapping("/timeSlotStats")
    public Map<String, Object> getTimeSlotStats(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Double> stats = statisticsService.getTimeSlotStats(userId, date);
        return buildSuccessResponse("timeSlotStats", stats);
    }

    /**
     * 获取服药趋势
     */
    @GetMapping("/trend")
    public Map<String, Object> getTrend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") Integer days) {
        List<DailyStats> trend = statisticsService.getDailyTrend(userId, days);
        return buildSuccessResponse("trend", trend);
    }

    /**
     * 获取药品排行
     */
    @GetMapping("/topDrugs")
    public Map<String, Object> getTopDrugs(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        List<DrugUsageStat> topDrugs = statisticsService.getTopDrugs(userId, limit);
        return buildSuccessResponse("topDrugs", topDrugs);
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
        return buildSuccessResponse("missedAlerts", alerts);
    }

    /**
     * 获取统计汇总
     */
    @GetMapping("/summary")
    public Map<String, Object> getSummary(@RequestParam Long userId) {
        StatisticsSummary summary = statisticsService.getStatisticsSummary(userId);
        return buildSuccessResponse("summary", summary);
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayStats(@RequestParam Long userId) {
        DailyStats todayStats = statisticsService.getTodayStats(userId);
        return buildSuccessResponse("todayStats", todayStats);
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

        return buildSuccessResponse("allStatistics", result);
    }

    private Map<String, Object> buildSuccessResponse(String key, Object data) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
