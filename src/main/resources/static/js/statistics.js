let currentUserId = 1;
let currentDays = 7;
let allCharts = {};

const userNames = {
    1: '张三',
    2: '李四',
    3: '王五',
    4: '赵六'
};

const chartTheme = {
    ink: '#10201f',
    soft: '#4c6462',
    muted: '#839795',
    line: '#dce8e6',
    primary: '#0f9f8f',
    blue: '#7197ee',
    amber: '#f1a95d',
    red: '#e3665d',
    green: '#28a66e'
};

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    bindEvents();
    loadAllData();
});

function bindEvents() {
    document.querySelectorAll('.time-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.time-tab').forEach(item => item.classList.remove('active'));
            tab.classList.add('active');
            currentDays = Number(tab.dataset.days || 7);
            loadAllData();
        });
    });

    const userSelect = document.getElementById('userSelect');
    userSelect.addEventListener('change', () => {
        currentUserId = Number(userSelect.value);
        loadAllData();
    });

    window.addEventListener('resize', resizeCharts);
}

function initCharts() {
    allCharts.trend = echarts.init(document.getElementById('trendChart'));
    allCharts.radar = echarts.init(document.getElementById('radarChart'));
    allCharts.drugRank = echarts.init(document.getElementById('drugRankChart'));

    allCharts.trend.setOption(getTrendBaseOption());
    allCharts.radar.setOption(getRadarBaseOption());
    allCharts.drugRank.setOption(getDrugRankBaseOption());
}

async function loadAllData() {
    showLoading();
    try {
        const response = await fetch(`/api/statistics/all?userId=${currentUserId}&trendDays=${currentDays}&topDrugLimit=6`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();
        if (!result.success) {
            throw new Error(result.message || '统计接口返回失败');
        }

        const data = result.data.allStatistics || result.data;
        updateDashboard(data);
        renderCharts(data);
    } catch (error) {
        console.error('加载统计数据失败:', error);
        renderLoadError();
    } finally {
        hideLoading();
        resizeCharts();
    }
}

function updateDashboard(data) {
    const summary = data.summary || {};
    const patientName = userNames[currentUserId] || `用户 ${currentUserId}`;
    const checkInRate = toPercent(summary.checkInRate);
    const complianceRate = toPercent(summary.complianceRate);
    const missedRate = toPercent(summary.missedRate);
    const todayPlan = Number(summary.todayPlanCount || 0);
    const todayDone = Number(summary.todayDoneCount || 0);
    const todayMissed = Number(summary.todayMissedCount || 0);
    const pending = Math.max(0, todayPlan - todayDone - todayMissed);

    setText('patientTitle', `${patientName}的今日用药概览`);
    setText('activePlanText', `${data.activePlanCount || 0} 个`);
    setText('pendingText', `${pending + todayMissed} 项`);
    setText('healthStateText', getLevelText(complianceRate, todayPlan));

    animateNumber('heroScore', todayPlan > 0 ? Math.round(complianceRate) : 0);
    setText('heroGradeText', getLevelText(complianceRate, todayPlan));

    animateNumber('todayPlan', todayPlan);
    animateNumber('todayDone', todayDone);
    animateNumber('todayMissed', todayMissed);
    animateNumber('metricCheckIn', checkInRate, '%', 1);
    animateNumber('metricCompliance', complianceRate, '%', 1);
    animateNumber('metricMissed', missedRate, '%', 1);
    animateNumber('metricConsecutive', Number(summary.consecutiveDays || 0), ' 天');
    setText('metricLongest', `历史最长 ${summary.longestStreak || 0} 天`);

    updatePlanList(data.todayPlans || []);
    updateRecordTimeline(data.todayRecords || []);
    updateAlertList(data.missedAlerts || []);

    if (window.lucide) {
        lucide.createIcons();
    }
}

function updatePlanList(plans) {
    const container = document.getElementById('planList');
    if (!plans.length) {
        container.innerHTML = '<div class="empty">暂无今日处方计划</div>';
        return;
    }

    container.innerHTML = plans.slice(0, 5).map(plan => {
        const dose = [plan.dosage, plan.unit].filter(Boolean).join('');
        const description = dose || plan.drugSpecification || '剂量未填写';
        return `
            <article class="plan-item">
                <div class="metric-icon blue"><i data-lucide="pill"></i></div>
                <div class="plan-main">
                    <strong>${escapeHtml(plan.drugName || '未知药品')}</strong>
                    <span>${escapeHtml(description)} · ${escapeHtml(plan.frequency || '按医嘱')}</span>
                </div>
                <div class="time-chip">${escapeHtml(formatTime(plan.scheduledTime) || getSlotText(plan.timeSlot))}</div>
            </article>
        `;
    }).join('');
}

function updateRecordTimeline(records) {
    const container = document.getElementById('recordTimeline');
    if (!records.length) {
        container.innerHTML = '<div class="empty">暂无今日服药记录</div>';
        return;
    }

    container.innerHTML = records.slice(0, 8).map(record => {
        const status = getRecordStatus(record.status);
        const lateText = record.lateMinutes ? `延迟 ${record.lateMinutes} 分钟` : (record.isOnTime === 1 ? '按时完成' : '等待更新');
        return `
            <article class="timeline-item">
                <div class="status-dot ${status.className}"><i data-lucide="${status.icon}"></i></div>
                <div class="timeline-main">
                    <strong>${escapeHtml(record.drugName || '未知药品')}</strong>
                    <span>${escapeHtml(formatDateTime(record.scheduledTime))} · ${escapeHtml(lateText)}</span>
                </div>
                <div class="badge">${escapeHtml(status.text)}</div>
            </article>
        `;
    }).join('');
}

function updateAlertList(alerts) {
    const container = document.getElementById('alertList');
    if (!alerts.length) {
        container.innerHTML = '<div class="empty">暂无漏服记录</div>';
        return;
    }

    container.innerHTML = alerts.slice(0, 6).map(alert => {
        const action = alert.suggestAction || '请联系医生确认补服方案';
        const severity = getAlertSeverity(action);
        const reason = alert.missedReason ? `原因：${alert.missedReason}` : '未填写漏服原因';
        return `
            <article class="alert-card">
                <div class="metric-icon ${severity.color}">
                    <i data-lucide="${severity.icon}"></i>
                </div>
                <div>
                    <div class="alert-title">${escapeHtml(alert.drugName || '未知药品')}</div>
                    <div class="alert-meta">
                        <span>${escapeHtml(formatDateTime(alert.planTime))}</span>
                        <span class="badge">${escapeHtml(severity.text)}</span>
                    </div>
                    <div class="alert-meta"><span>${escapeHtml(reason)}</span></div>
                    <div class="alert-meta"><strong>${escapeHtml(action)}</strong></div>
                </div>
            </article>
        `;
    }).join('');
}

function renderCharts(data) {
    renderTrendChart(data.trend || []);
    renderRadarChart((data.summary || {}).timeSlotStats || {});
    renderDrugRankChart(data.topDrugs || []);
}

function getTrendBaseOption() {
    return {
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255,255,255,.96)',
            borderColor: chartTheme.line,
            textStyle: { color: chartTheme.ink },
            extraCssText: 'border-radius:14px;box-shadow:0 12px 30px rgba(19,73,67,.12);'
        },
        grid: { left: 42, right: 28, top: 34, bottom: 36 },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: [],
            axisTick: { show: false },
            axisLine: { lineStyle: { color: chartTheme.line } },
            axisLabel: { color: chartTheme.muted, fontWeight: 700 }
        },
        yAxis: {
            type: 'value',
            min: 0,
            max: 100,
            axisLabel: { formatter: '{value}%', color: chartTheme.muted, fontWeight: 700 },
            splitLine: { lineStyle: { color: '#eee8de' } }
        },
        series: [
            {
                name: '完成率',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                data: [],
                lineStyle: { width: 4, color: chartTheme.primary },
                itemStyle: { color: chartTheme.primary, borderColor: '#fff', borderWidth: 3 },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(15,159,143,.24)' },
                        { offset: 1, color: 'rgba(15,159,143,0)' }
                    ])
                },
                markLine: {
                    silent: true,
                    symbol: 'none',
                    lineStyle: { color: 'rgba(40,166,110,.45)', type: 'dashed' },
                    label: { formatter: '达标线 80%', color: chartTheme.green },
                    data: [{ yAxis: 80 }]
                }
            },
            {
                name: '7 日均线',
                type: 'line',
                smooth: true,
                symbol: 'none',
                data: [],
                lineStyle: { width: 2, color: chartTheme.blue, type: 'dashed' }
            }
        ]
    };
}

function renderTrendChart(trend) {
    const dates = trend.map(item => String(item.date || '').substring(5));
    const rates = trend.map(item => Number(toPercent(item.completionRate).toFixed(1)));
    const movingAverage = rates.map((_, index) => {
        const start = Math.max(0, index - 6);
        const slice = rates.slice(start, index + 1);
        const value = slice.reduce((sum, item) => sum + Number(item), 0) / Math.max(slice.length, 1);
        return Number(value.toFixed(1));
    });

    allCharts.trend.setOption({
        xAxis: { data: dates },
        series: [
            { data: rates },
            { data: movingAverage }
        ]
    });
}

function getRadarBaseOption() {
    return {
        tooltip: {
            backgroundColor: 'rgba(255,255,255,.96)',
            borderColor: chartTheme.line,
            textStyle: { color: chartTheme.ink }
        },
        radar: {
            indicator: [
                { name: '早间', max: 100 },
                { name: '午后', max: 100 },
                { name: '晚间', max: 100 }
            ],
            radius: '62%',
            center: ['50%', '54%'],
            axisName: { color: chartTheme.soft, fontWeight: 800 },
            splitLine: { lineStyle: { color: '#deebe9' } },
            axisLine: { lineStyle: { color: '#deebe9' } },
            splitArea: { areaStyle: { color: ['rgba(255,255,255,.45)', 'rgba(255,255,255,.20)'] } }
        },
        series: [
            {
                type: 'radar',
                data: [
                    {
                        value: [0, 0, 0],
                        name: '实际完成率',
                        symbol: 'circle',
                        symbolSize: 6,
                        lineStyle: { color: chartTheme.primary, width: 3 },
                        itemStyle: { color: chartTheme.primary },
                        areaStyle: { color: 'rgba(15,159,143,.20)' }
                    },
                    {
                        value: [100, 100, 100],
                        name: '理想状态',
                        symbol: 'none',
                        lineStyle: { color: 'rgba(113,151,238,.36)', type: 'dashed' },
                        areaStyle: { color: 'rgba(113,151,238,.04)' }
                    }
                ]
            }
        ]
    };
}

function renderRadarChart(timeSlotStats) {
    allCharts.radar.setOption({
        series: [
            {
                data: [
                    {
                        value: [
                            Number(toPercent(timeSlotStats.morning).toFixed(1)),
                            Number(toPercent(timeSlotStats.afternoon).toFixed(1)),
                            Number(toPercent(timeSlotStats.evening).toFixed(1))
                        ],
                        name: '实际完成率'
                    },
                    { value: [100, 100, 100], name: '理想状态' }
                ]
            }
        ]
    });
}

function getDrugRankBaseOption() {
    return {
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: 'rgba(255,255,255,.96)',
            borderColor: chartTheme.line,
            textStyle: { color: chartTheme.ink }
        },
        grid: { left: 36, right: 16, top: 28, bottom: 56 },
        xAxis: {
            type: 'category',
            data: [],
            axisLabel: {
                color: chartTheme.soft,
                fontWeight: 800,
                fontSize: 11,
                interval: 0
            },
            axisLine: { show: false },
            axisTick: { show: false }
        },
        yAxis: {
            type: 'value',
            max: 100,
            axisLabel: { formatter: '{value}%', color: chartTheme.muted, fontWeight: 700, fontSize: 9 },
            splitLine: { lineStyle: { color: '#eee8de' } },
            axisLine: { show: false },
            axisTick: { show: false }
        },
        series: [
            {
                name: '依从性',
                type: 'bar',
                data: [],
                barWidth: 16,
                label: {
                    show: true,
                    position: 'top',
                    formatter: '{c}%',
                    color: chartTheme.soft,
                    fontWeight: 800,
                    fontSize: 10
                },
                itemStyle: {
                    borderRadius: [8, 8, 0, 0],
                    borderColor: 'transparent',
                    borderWidth: 0
                }
            },
            {
                type: 'bar',
                data: [],
                barWidth: 16,
                barGap: '-100%',
                silent: true,
                tooltip: { show: false },
                itemStyle: { color: '#efe8dc', borderRadius: [8, 8, 0, 0] }
            }
        ]
    };
}

function renderDrugRankChart(topDrugs) {
    if (!topDrugs.length) {
        allCharts.drugRank.setOption({
            xAxis: { data: ['暂无数据'] },
            series: [{ data: [0] }, { data: [100] }]
        });
        return;
    }

    const values = topDrugs.map(item => Number(toPercent(item.usageRate).toFixed(1)));

    const drugNames = topDrugs.map(item => item.drugName || '未知药品');
    const coloredData = values.map(v => ({
        value: v,
        itemStyle: {
            color: drugColorForValue(v),
            borderColor: 'transparent',
            borderWidth: 0
        }
    }));

    allCharts.drugRank.setOption({
        xAxis: { data: drugNames },
        series: [
            { data: coloredData },
            { data: topDrugs.map(() => 100) }
        ]
    });
}

function drugColorForValue(value) {
    const v = Number(value || 0);
    if (v >= 80) return chartTheme.green;
    if (v >= 60) return chartTheme.amber;
    return chartTheme.red;
}

function animateNumber(id, end, suffix = '', decimals = 0) {
    const element = document.getElementById(id);
    if (!element) return;

    const start = Number.parseFloat(element.dataset.value || '0');
    const target = Number(end || 0);
    const startTime = performance.now();
    const duration = 620;

    element.dataset.value = String(target);

    function tick(now) {
        const progress = Math.min((now - startTime) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        const value = start + (target - start) * eased;
        element.textContent = `${value.toFixed(decimals)}${suffix}`;
        if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
}

function renderLoadError() {
    setText('healthStateText', '连接失败');
    setText('heroGradeText', '连接失败');
    document.getElementById('alertList').innerHTML = '<div class="empty">后端接口暂时无法连接</div>';
    document.getElementById('planList').innerHTML = '<div class="empty">处方计划暂时无法加载</div>';
    document.getElementById('recordTimeline').innerHTML = '<div class="empty">用药时间线暂时无法加载</div>';
}

function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'grid';
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

function resizeCharts() {
    Object.values(allCharts).forEach(chart => chart.resize());
}

function toPercent(value) {
    return Number(value || 0) * 100;
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}

function getLevelText(rate, total) {
    if (!total) return '暂无计划';
    if (rate >= 80) return '达标良好';
    if (rate >= 60) return '需要观察';
    return '需要干预';
}

function getAlertSeverity(action) {
    if (action.includes('尽快') || action.includes('补服')) {
        return { text: '高优先级', color: 'red', icon: 'alarm-clock' };
    }
    if (action.includes('跳过') || action.includes('下次')) {
        return { text: '需确认', color: 'amber', icon: 'circle-alert' };
    }
    return { text: '提醒', color: 'blue', icon: 'bell' };
}

function getRecordStatus(status) {
    switch (Number(status)) {
        case 1:
            return { text: '已服', className: 'done', icon: 'check' };
        case 2:
            return { text: '漏服', className: 'missed', icon: 'x' };
        case 3:
            return { text: '补服', className: 'supplement', icon: 'rotate-ccw' };
        default:
            return { text: '待服', className: 'pending', icon: 'clock-3' };
    }
}

function getSlotText(slot) {
    if (slot === 'morning') return '早间';
    if (slot === 'afternoon') return '午后';
    if (slot === 'evening') return '晚间';
    return '按时段';
}

function formatTime(value) {
    if (!value) return '';
    return String(value).substring(0, 5);
}

function formatDateTime(value) {
    if (!value) return '计划时间：--';
    return `计划时间：${String(value).replace('T', ' ').substring(0, 16)}`;
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ========== 多视图切换 ==========

let activeView = 'dashboard';

document.querySelectorAll('.rail-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const title = btn.getAttribute('title') || '';
        let targetView = 'dashboard';
        if (title === '计划') targetView = 'plan';
        else if (title === '药品') targetView = 'drug';
        else if (title === '提醒') targetView = 'alert';
        else if (title === '档案') targetView = 'profile';

        switchView(targetView);
    });
});

function switchView(view) {
    if (view === activeView) return;
    activeView = view;

    // 更新侧边栏高亮
    document.querySelectorAll('.rail-btn').forEach(b => b.classList.remove('active'));
    const titles = { dashboard: '驾驶舱', plan: '计划', drug: '药品', alert: '提醒', profile: '档案' };
    document.querySelectorAll('.rail-btn').forEach(b => {
        if (b.getAttribute('title') === titles[view]) b.classList.add('active');
    });

    const board = document.querySelector('.board');
    const topbar = document.querySelector('.topbar');
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));

    if (view === 'dashboard') {
        board.style.display = '';
        topbar.style.display = '';
    } else {
        board.style.display = 'none';
        topbar.style.display = 'none';
        const viewEl = document.getElementById(view + 'View');
        if (viewEl) viewEl.classList.add('active');
        loadViewData(view);
    }
}

function loadViewData(view) {
    switch (view) {
        case 'plan': loadPlanView(); break;
        case 'drug': loadDrugView(); break;
        case 'alert': loadAlertView(); break;
        case 'profile': loadProfileView(); break;
    }
}

async function loadPlanView() {
    const container = document.getElementById('planViewBody');
    try {
        const res = await fetch('/api/plans?userId=' + currentUserId);
        const json = await res.json();
        const plans = json.data || [];
        if (!plans.length) {
            container.innerHTML = '<div class="empty">暂无活跃处方计划</div>';
            return;
        }
        container.innerHTML = plans.map(p => {
            const dose = [p.dosage, p.unit].filter(Boolean).join('');
            return '<div class="plan-card">'
                + '<div><strong>' + escapeHtml(p.drugName || '未知药品') + '</strong>'
                + '<div class="plan-meta">'
                + '<span>' + escapeHtml(dose || '--') + '</span>'
                + '<span>' + escapeHtml(p.frequency || '--') + '</span>'
                + '<span>' + escapeHtml(getSlotText(p.timeSlot) || '--') + '</span>'
                + '</div></div>'
                + '<div class="plan-tag">' + escapeHtml(formatTime(p.scheduledTime) || '--') + '</div>'
                + '</div>';
        }).join('');
    } catch (e) {
        container.innerHTML = '<div class="empty">加载失败，请稍后重试</div>';
    }
    if (window.lucide) lucide.createIcons();
}

async function loadDrugView() {
    const container = document.getElementById('drugViewBody');
    try {
        const res = await fetch('/api/drugs');
        const json = await res.json();
        const drugs = json.data || [];
        if (!drugs.length) {
            container.innerHTML = '<div class="empty">暂无药品数据</div>';
            return;
        }
        container.innerHTML = drugs.map(d => {
            return '<div class="drug-card">'
                + '<div class="drug-name">' + escapeHtml(d.drugName || '--') + '</div>'
                + '<div class="drug-spec">' + escapeHtml(d.specification || '--') + '</div>'
                + '<div class="drug-row">'
                + '<span class="drug-chip">' + escapeHtml(d.manufacturer || '--') + '</span>'
                + '<span class="drug-chip">' + escapeHtml(d.category || '--') + '</span>'
                + (d.genericName ? '<span class="drug-chip">' + escapeHtml(d.genericName) + '</span>' : '')
                + '</div></div>';
        }).join('');
    } catch (e) {
        container.innerHTML = '<div class="empty">加载失败，请稍后重试</div>';
    }
}

async function loadAlertView() {
    const container = document.getElementById('alertViewBody');
    try {
        const today = new Date().toISOString().split('T')[0];
        const past = new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0];
        const res = await fetch('/api/statistics/missedAlerts?userId=' + currentUserId + '&startDate=' + past + '&endDate=' + today);
        const json = await res.json();
        const alerts = json.data || [];
        if (!alerts.length) {
            container.innerHTML = '<div class="empty">暂无漏服记录</div>';
            return;
        }
        container.innerHTML = alerts.map(a => {
            const sev = getAlertSeverity(a.suggestAction || '');
            return '<div class="alert-item">'
                + '<div class="alert-icon" style="background:' + (sev.color === 'red' ? '#e3665d' : sev.color === 'amber' ? '#f1a95d' : '#7197ee') + ';color:#fff;display:grid;place-items:center;"><i data-lucide="' + sev.icon + '"></i></div>'
                + '<div class="alert-info"><strong>' + escapeHtml(a.drugName || '未知药品') + '</strong>'
                + '<div class="alert-detail">' + escapeHtml(formatDateTime(a.planTime)) + '</div>'
                + '<div class="alert-detail">' + escapeHtml(a.missedReason || '未填写原因') + '</div>'
                + '<div class="alert-detail" style="color:' + chartTheme.sage + '">' + escapeHtml(a.suggestAction || '请联系医生确认') + '</div>'
                + '</div>'
                + '<div class="alert-badge" style="background:' + (sev.color === 'red' ? '#fce4e4' : sev.color === 'amber' ? '#fef3e4' : '#e4ecfc') + ';color:' + (sev.color === 'red' ? '#c0392b' : sev.color === 'amber' ? '#b8731b' : '#2b5ec0') + '">' + escapeHtml(sev.text) + '</div>'
                + '</div>';
        }).join('');
    } catch (e) {
        container.innerHTML = '<div class="empty">加载失败，请稍后重试</div>';
    }
    if (window.lucide) lucide.createIcons();
}

async function loadProfileView() {
    const container = document.getElementById('profileViewBody');
    try {
        const res = await fetch('/api/users/' + currentUserId);
        const json = await res.json();
        const user = json.data || {};
        const genderText = user.gender === 1 ? '男' : user.gender === 0 ? '女' : '--';
        container.innerHTML = '<div class="profile-card">'
            + '<h3>' + escapeHtml(user.realName || '用户 #' + currentUserId) + '</h3>'
            + '<div style="font-size:13px;opacity:.7;font-weight:700;">' + escapeHtml(user.username || '--') + '</div>'
            + '<div class="profile-row">'
            + '<div class="profile-field"><label>年龄</label><span>' + (user.age || '--') + ' 岁</span></div>'
            + '<div class="profile-field"><label>性别</label><span>' + genderText + '</span></div>'
            + '<div class="profile-field"><label>手机</label><span>' + escapeHtml(user.phone || '--') + '</span></div>'
            + '<div class="profile-field"><label>状态</label><span>' + (user.status === 1 ? '正常' : '禁用') + '</span></div>'
            + '</div>'
            + '<div class="profile-tags">'
            + '<span class="profile-tag">过敏史：' + escapeHtml(user.allergyHistory || '无') + '</span>'
            + '<span class="profile-tag">病史：' + escapeHtml(user.medicalHistory || '无') + '</span>'
            + '</div></div>';
    } catch (e) {
        container.innerHTML = '<div class="empty">加载失败，请稍后重试</div>';
    }
}
