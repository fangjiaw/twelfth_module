// 全局变量
let currentUserId = 1;
let currentDays = 7;
let allCharts = {};
let allData = {};

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    // 初始化图表实例
    initCharts();

    // 绑定时间切换事件
    document.querySelectorAll('.time-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            document.querySelectorAll('.time-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            currentDays = parseInt(this.dataset.days);
            loadAllData();
        });
    });

    // 绑定用户切换事件
    document.getElementById('userSelect').addEventListener('change', function() {
        currentUserId = parseInt(this.value);
        loadAllData();
    });

    // 加载初始数据
    loadAllData();
});

// 初始化所有图表
function initCharts() {
    // 打卡率环形图
    allCharts.checkIn = echarts.init(document.getElementById('checkInChart'));
    // 漏服率环形图
    allCharts.missed = echarts.init(document.getElementById('missedChart'));
    // 趋势折线图
    allCharts.trend = echarts.init(document.getElementById('trendChart'));
    // 时段分析柱状图
    allCharts.timeSlot = echarts.init(document.getElementById('timeSlotChart'));
    // 药品排行横向柱状图
    allCharts.drugRank = echarts.init(document.getElementById('drugRankChart'));

    // 响应式调整
    window.addEventListener('resize', function() {
        Object.values(allCharts).forEach(chart => chart.resize());
    });
}

// 加载所有数据
async function loadAllData() {
    try {
        const response = await fetch(`/api/statistics/all?userId=${currentUserId}&trendDays=${currentDays}&topDrugLimit=5`);
        const result = await response.json();

        if (result.success) {
            allData = result.data;
            updateUI(allData);
            renderCharts(allData);
        }
    } catch (error) {
        console.error('加载数据失败:', error);
    }
}

// 更新界面数据
function updateUI(data) {
    const summary = data.summary || {};
    const checkInRate = (summary.checkInRate || 0) * 100;
    const missedRate = (summary.missedRate || 0) * 100;
    const complianceRate = (summary.complianceRate || 0) * 100;

    // 更新统计卡片
    document.getElementById('checkInRate').textContent = checkInRate.toFixed(0) + '%';
    document.getElementById('missedRate').textContent = missedRate.toFixed(0) + '%';
    document.getElementById('complianceRate').textContent = complianceRate.toFixed(0) + '%';

    // 设置达标等级样式
    const complianceEl = document.getElementById('complianceRate');
    complianceEl.className = 'value ' + (summary.complianceLevel || 'good');

    const levelText = { excellent: '优秀', good: '良好', warning: '需改进' };
    document.getElementById('complianceLevel').innerHTML =
        `<span class="compliance-badge ${summary.complianceLevel || 'good'}">${levelText[summary.complianceLevel] || '未知'}</span>`;

    // 连续打卡
    document.getElementById('consecutiveDays').textContent = (summary.consecutiveDays || 0) + '天';
    document.getElementById('longestStreak').textContent = '历史最长 ' + (summary.longestStreak || 0) + ' 天';

    // 今日统计
    document.getElementById('todayPlan').textContent = summary.todayPlanCount || 0;
    document.getElementById('todayDone').textContent = summary.todayDoneCount || 0;

    const missedEl = document.getElementById('todayMissed');
    missedEl.textContent = summary.todayMissedCount || 0;
    missedEl.className = 'value' + ((summary.todayMissedCount || 0) > 0 ? ' warning' : '');

    // 时段统计
    const timeSlot = summary.timeSlotStats || {};
    document.getElementById('morningRate').textContent = ((timeSlot.morning || 0) * 100).toFixed(0);
    document.getElementById('afternoonRate').textContent = ((timeSlot.afternoon || 0) * 100).toFixed(0);
    document.getElementById('eveningRate').textContent = ((timeSlot.evening || 0) * 100).toFixed(0);

    // 更新漏服提醒列表
    updateAlertList(data.missedAlerts || []);
}

// 更新漏服提醒列表
function updateAlertList(alerts) {
    const container = document.getElementById('alertList');

    if (!alerts || alerts.length === 0) {
        container.innerHTML = '<div style="text-align:center;color:#999;padding:40px;">暂无漏服记录，继续保持！</div>';
        return;
    }

    container.innerHTML = alerts.map(alert => `
        <div class="alert-item">
            <div class="alert-info">
                <div class="drug-name">${alert.drugName}</div>
                <div class="plan-time">计划时间: ${formatDateTime(alert.planTime)}</div>
            </div>
            <div class="alert-action">${alert.suggestAction}</div>
        </div>
    `).join('');
}

// 渲染所有图表
function renderCharts(data) {
    renderCheckInChart(data.summary);
    renderMissedChart(data.summary);
    renderTrendChart(data.trend);
    renderTimeSlotChart(data.summary);
    renderDrugRankChart(data.topDrugs);
}

// 渲染打卡率环形图
function renderCheckInChart(summary) {
    const rate = summary.checkInRate || 0;
    const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
        series: [{
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '50%'],
            label: { show: true, formatter: '{d}%', fontSize: 14 },
            data: [
                { value: (rate * 100).toFixed(0), name: '已打卡', itemStyle: { color: '#52c41a' } },
                { value: ((1 - rate) * 100).toFixed(0), name: '未打卡', itemStyle: { color: '#f0f0f0' } }
            ]
        }]
    };
    allCharts.checkIn.setOption(option);
}

// 渲染漏服率环形图
function renderMissedChart(summary) {
    const rate = summary.missedRate || 0;
    const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
        series: [{
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '50%'],
            label: { show: true, formatter: '{d}%', fontSize: 14 },
            data: [
                { value: (rate * 100).toFixed(0), name: '漏服', itemStyle: { color: '#f5222d' } },
                { value: ((1 - rate) * 100).toFixed(0), name: '正常', itemStyle: { color: '#f0f0f0' } }
            ]
        }]
    };
    allCharts.missed.setOption(option);
}

// 渲染趋势折线图
function renderTrendChart(trend) {
    if (!trend || trend.length === 0) return;

    const dates = trend.map(t => t.date.substring(5)); // MM-DD格式
    const rates = trend.map(t => (t.completionRate * 100).toFixed(1));

    const option = {
        tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>完成率: ${p[0].value}%` },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: dates, boundaryGap: false },
        yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
        series: [{
            type: 'line',
            smooth: true,
            data: rates,
            areaStyle: { color: 'rgba(102, 126, 234, 0.2)' },
            lineStyle: { color: '#667eea', width: 2 },
            itemStyle: { color: '#667eea' },
            markLine: {
                silent: true,
                data: [{ yAxis: 80, lineStyle: { color: '#52c41a' }, name: '达标线' }]
            }
        }]
    };
    allCharts.trend.setOption(option);
}

// 渲染时段分析柱状图
function renderTimeSlotChart(summary) {
    const timeSlot = summary.timeSlotStats || {};
    const option = {
        tooltip: { trigger: 'axis', formatter: p => `${p[0].name}: ${p[0].value}%` },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: ['早间\n6-12点', '下午\n12-18点', '晚间\n18-24点'] },
        yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
        series: [{
            type: 'bar',
            data: [
                { value: ((timeSlot.morning || 0) * 100).toFixed(1), itemStyle: { color: '#36cfc9' } },
                { value: ((timeSlot.afternoon || 0) * 100).toFixed(1), itemStyle: { color: '#597ef7' } },
                { value: ((timeSlot.evening || 0) * 100).toFixed(1), itemStyle: { color: '#b37feb' } }
            ],
            barWidth: '50%',
            label: { show: true, position: 'top', formatter: '{c}%' }
        }]
    };
    allCharts.timeSlot.setOption(option);
}

// 渲染药品排行图
function renderDrugRankChart(topDrugs) {
    if (!topDrugs || topDrugs.length === 0) {
        allCharts.drugRank.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } } });
        return;
    }

    const drugs = topDrugs.map(d => d.drugName);
    const counts = topDrugs.map(d => d.usageCount);

    const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: drugs.reverse(), axisLabel: { fontSize: 12 } },
        series: [{
            type: 'bar',
            data: counts.reverse(),
            itemStyle: {
                color: function(params) {
                    const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe'];
                    return colors[params.dataIndex % colors.length];
                }
            },
            label: { show: true, position: 'right', formatter: '{c} 次' }
        }]
    };
    allCharts.drugRank.setOption(option);
}

// 格式化日期时间
function formatDateTime(dateTime) {
    if (!dateTime) return '--';
    if (typeof dateTime === 'string') {
        return dateTime.replace('T', ' ').substring(0, 16);
    }
    return dateTime;
}
