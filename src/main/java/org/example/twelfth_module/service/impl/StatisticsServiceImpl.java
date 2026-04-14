package org.example.twelfth_module.service.impl;

import org.example.twelfth_module.dto.DailyStats;
import org.example.twelfth_module.dto.DrugUsageStat;
import org.example.twelfth_module.dto.MissedAlert;
import org.example.twelfth_module.dto.StatisticsSummary;
import org.example.twelfth_module.entity.MedicationRecord;
import org.example.twelfth_module.mapper.MedicationPlanMapper;
import org.example.twelfth_module.mapper.MedicationRecordMapper;
import org.example.twelfth_module.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private MedicationRecordMapper recordMapper;

    @Autowired
    private MedicationPlanMapper planMapper;

    @Override
    public Double getCheckInRate(Long userId, LocalDate startDate, LocalDate endDate) {
        Integer total = recordMapper.countTotal(userId, startDate, endDate);
        if (total == null || total == 0) {
            return 0.0;
        }
        Integer completed = recordMapper.countCompleted(userId, startDate, endDate);
        return (double) completed / total;
    }

    @Override
    public Double getMissedRate(Long userId, LocalDate startDate, LocalDate endDate) {
        Integer total = recordMapper.countTotal(userId, startDate, endDate);
        if (total == null || total == 0) {
            return 0.0;
        }
        Integer missed = recordMapper.countMissed(userId, startDate, endDate);
        return (double) missed / total;
    }

    @Override
    public Double getComplianceRate(Long userId, LocalDate startDate, LocalDate endDate) {
        Integer total = recordMapper.countTotal(userId, startDate, endDate);
        if (total == null || total == 0) {
            return 0.0;
        }
        Integer onTime = recordMapper.countOnTime(userId, startDate, endDate);
        return (double) onTime / total;
    }

    @Override
    public Integer getConsecutiveDays(Long userId) {
        // 获取最近90天的打卡记录
        List<MedicationRecord> records = recordMapper.selectConsecutiveDays(userId, 90);
        if (records == null || records.isEmpty()) {
            return 0;
        }

        // 获取不重复的日期并排序
        Set<LocalDate> completedDates = records.stream()
                .map(MedicationRecord::getRecordDate)
                .collect(Collectors.toSet());

        List<LocalDate> sortedDates = new ArrayList<>(completedDates);
        Collections.sort(sortedDates);

        // 从今天往前计算连续天数
        int consecutiveDays = 0;
        LocalDate checkDate = LocalDate.now();

        // 如果今天没有记录，从昨天开始检查
        if (!completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1);
        }

        while (completedDates.contains(checkDate)) {
            consecutiveDays++;
            checkDate = checkDate.minusDays(1);
        }

        return consecutiveDays;
    }

    @Override
    public Integer getLongestStreak(Long userId) {
        // 获取最近365天的打卡记录
        List<MedicationRecord> records = recordMapper.selectConsecutiveDays(userId, 365);
        if (records == null || records.isEmpty()) {
            return 0;
        }

        // 获取不重复的日期并排序
        Set<LocalDate> completedDates = records.stream()
                .map(MedicationRecord::getRecordDate)
                .collect(Collectors.toSet());

        List<LocalDate> sortedDates = new ArrayList<>(completedDates);
        Collections.sort(sortedDates);

        // 计算最长连续天数
        int maxStreak = 0;
        int currentStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(sortedDates.get(i - 1), sortedDates.get(i));
            if (daysBetween == 1) {
                currentStreak++;
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = 1;
            }
        }
        maxStreak = Math.max(maxStreak, currentStreak);

        return maxStreak;
    }

    @Override
    public Map<String, Double> getTimeSlotStats(Long userId, LocalDate date) {
        List<MedicationRecord> records = recordMapper.selectTimeSlotStats(userId, date);

        Map<String, Integer> totalBySlot = new HashMap<>();
        Map<String, Integer> completedBySlot = new HashMap<>();

        for (MedicationRecord record : records) {
            String slot = getTimeSlot(record.getScheduledTime().toLocalTime());
            totalBySlot.merge(slot, 1, Integer::sum);
            if (record.getStatus() == 1 || record.getStatus() == 3) {
                completedBySlot.merge(slot, 1, Integer::sum);
            }
        }

        Map<String, Double> result = new HashMap<>();
        for (String slot : Arrays.asList("morning", "afternoon", "evening")) {
            int total = totalBySlot.getOrDefault(slot, 0);
            int completed = completedBySlot.getOrDefault(slot, 0);
            result.put(slot, total > 0 ? (double) completed / total : 0.0);
        }

        return result;
    }

    private String getTimeSlot(java.time.LocalTime time) {
        int hour = time.getHour();
        if (hour >= 6 && hour <= 11) {
            return "morning";
        } else if (hour >= 12 && hour <= 17) {
            return "afternoon";
        } else {
            return "evening";
        }
    }

    @Override
    public List<DailyStats> getDailyTrend(Long userId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<MedicationRecord> records = recordMapper.selectDailyStats(userId, startDate, endDate);

        // 按日期分组统计
        Map<LocalDate, List<MedicationRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(MedicationRecord::getRecordDate));

        List<DailyStats> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<MedicationRecord> dayRecords = byDate.getOrDefault(date, Collections.emptyList());

            int total = dayRecords.size();
            int done = (int) dayRecords.stream().filter(r -> r.getStatus() == 1 || r.getStatus() == 3).count();
            int missed = (int) dayRecords.stream().filter(r -> r.getStatus() == 2).count();
            int supplement = (int) dayRecords.stream().filter(r -> r.getStatus() == 3).count();

            result.add(DailyStats.builder()
                    .date(date)
                    .planCount(total)
                    .doneCount(done)
                    .missedCount(missed)
                    .supplementCount(supplement)
                    .completionRate(total > 0 ? (double) done / total : 0.0)
                    .build());
        }

        return result;
    }

    @Override
    public List<DrugUsageStat> getTopDrugs(Long userId, Integer limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        List<MedicationRecord> records = recordMapper.selectDrugUsageStats(userId, startDate, endDate);

        // 按药品分组统计
        Map<Long, DrugUsageStat> drugMap = new LinkedHashMap<>();
        for (MedicationRecord record : records) {
            DrugUsageStat stat = drugMap.computeIfAbsent(record.getDrugId(), id ->
                    DrugUsageStat.builder()
                            .drugId(record.getDrugId())
                            .drugName(record.getDrugName())
                            .usageCount(0)
                            .build());
            stat.setUsageCount(stat.getUsageCount() + 1);
        }

        // 计算总次数和占比
        int totalUsage = drugMap.values().stream().mapToInt(DrugUsageStat::getUsageCount).sum();

        return drugMap.values().stream()
                .sorted(Comparator.comparingInt(DrugUsageStat::getUsageCount).reversed())
                .limit(limit)
                .peek(stat -> stat.setUsageRate((double) stat.getUsageCount() / totalUsage))
                .collect(Collectors.toList());
    }

    @Override
    public List<MissedAlert> getMissedAlerts(Long userId, LocalDate startDate, LocalDate endDate) {
        List<MedicationRecord> records = recordMapper.selectMissedRecords(userId, startDate, endDate);

        return records.stream()
                .map(record -> MissedAlert.builder()
                        .recordId(record.getId())
                        .drugId(record.getDrugId())
                        .drugName(record.getDrugName())
                        .planTime(record.getScheduledTime())
                        .status(record.getStatus())
                        .statusText(getStatusText(record.getStatus()))
                        .suggestAction(record.getSupplementAction() != null ?
                                record.getSupplementAction() : generateSuggestAction(record))
                        .missedReason(record.getMissedReason())
                        .build())
                .collect(Collectors.toList());
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待服";
            case 1: return "已服";
            case 2: return "漏服";
            case 3: return "补服";
            default: return "未知";
        }
    }

    private String generateSuggestAction(MedicationRecord record) {
        // 根据时间判断补服建议
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = record.getScheduledTime();
        long hoursLate = ChronoUnit.HOURS.between(scheduled, now);

        if (hoursLate < 2) {
            return "尽快补服本次剂量";
        } else if (hoursLate < 4) {
            return "跳过本次，直接服用下次计划";
        } else {
            return "已超过补服窗口，跳过本次，等待下次服药时间";
        }
    }

    @Override
    public StatisticsSummary getStatisticsSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.minusDays(29);

        Double weekCheckInRate = getCheckInRate(userId, weekStart, today);
        Double weekMissedRate = getMissedRate(userId, weekStart, today);
        Double weekComplianceRate = getComplianceRate(userId, weekStart, today);
        Integer consecutiveDays = getConsecutiveDays(userId);
        Integer longestStreak = getLongestStreak(userId);
        Map<String, Double> timeSlotStats = getTimeSlotStats(userId, today);

        // 今日统计
        DailyStats todayStats = getTodayStats(userId);

        // 确定达标等级
        String complianceLevel;
        if (weekComplianceRate >= 0.8) {
            complianceLevel = "excellent";
        } else if (weekComplianceRate >= 0.6) {
            complianceLevel = "good";
        } else {
            complianceLevel = "warning";
        }

        return StatisticsSummary.builder()
                .checkInRate(weekCheckInRate)
                .missedRate(weekMissedRate)
                .complianceRate(weekComplianceRate)
                .consecutiveDays(consecutiveDays)
                .longestStreak(longestStreak)
                .todayPlanCount(todayStats.getPlanCount())
                .todayDoneCount(todayStats.getDoneCount())
                .todayMissedCount(todayStats.getMissedCount())
                .timeSlotStats(timeSlotStats)
                .complianceLevel(complianceLevel)
                .build();
    }

    @Override
    public DailyStats getTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        List<MedicationRecord> records = recordMapper.selectByUserIdAndDate(userId, today);

        int total = records.size();
        int done = (int) records.stream().filter(r -> r.getStatus() == 1 || r.getStatus() == 3).count();
        int missed = (int) records.stream().filter(r -> r.getStatus() == 2).count();

        return DailyStats.builder()
                .date(today)
                .planCount(total)
                .doneCount(done)
                .missedCount(missed)
                .completionRate(total > 0 ? (double) done / total : 0.0)
                .build();
    }
}
