package org.example.twelfth_module.service;

import org.example.twelfth_module.dto.DailyStats;
import org.example.twelfth_module.dto.DrugUsageStat;
import org.example.twelfth_module.dto.MissedAlert;
import org.example.twelfth_module.dto.StatisticsSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取打卡率
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡率 (0.0 - 1.0)
     */
    Double getCheckInRate(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取漏服率
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 漏服率 (0.0 - 1.0)
     */
    Double getMissedRate(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取服药达标率（按时服药才算达标）
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 达标率 (0.0 - 1.0)
     */
    Double getComplianceRate(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取当前连续打卡天数
     * @param userId 用户ID
     * @return 连续天数
     */
    Integer getConsecutiveDays(Long userId);

    /**
     * 获取历史最长连续打卡天数
     * @param userId 用户ID
     * @return 最长连续天数
     */
    Integer getLongestStreak(Long userId);

    /**
     * 获取时段统计（早/中/晚完成率）
     * @param userId 用户ID
     * @param date 日期
     * @return 时段完成率 Map
     */
    Map<String, Double> getTimeSlotStats(Long userId, LocalDate date);

    /**
     * 获取每日服药趋势
     * @param userId 用户ID
     * @param days 天数
     * @return 每日统计列表
     */
    List<DailyStats> getDailyTrend(Long userId, Integer days);

    /**
     * 获取药品使用排行
     * @param userId 用户ID
     * @param limit 返回条数
     * @return 药品使用统计列表
     */
    List<DrugUsageStat> getTopDrugs(Long userId, Integer limit);

    /**
     * 获取漏服提醒列表
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 漏服提醒列表
     */
    List<MissedAlert> getMissedAlerts(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取统计汇总
     * @param userId 用户ID
     * @return 统计汇总对象
     */
    StatisticsSummary getStatisticsSummary(Long userId);

    /**
     * 获取今日统计
     * @param userId 用户ID
     * @return 今日统计
     */
    DailyStats getTodayStats(Long userId);
}
