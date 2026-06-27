/* ================================================================
 * OA协同办公平台 — 交互脚本
 * ================================================================ */
var currentYear  = new Date().getFullYear();
var currentMonth = new Date().getMonth() + 1;

/* ---------- 面板切换 ---------- */
function switchTab(n, el) {
  document.querySelectorAll('.panel').forEach(function(p, i) { p.classList.toggle('active', i === n); });
  document.querySelectorAll('.nav-item').forEach(function(i) { i.classList.remove('active'); });
  if (el) el.classList.add('active');
  var titles = ['工作台', '考勤日历', '数据看板', '审批流', '通知中心'];
  document.getElementById('crumbText').textContent = titles[n];
  if (n === 0) loadEmployeeList();
  if (n === 1) { loadCalendar(); loadCalendarStats(); }
}

/* ---------- 考勤日历模块（日历网格与月度统计独立） ---------- */
function loadCalendar() {
  var empId = document.getElementById('empSelect').value;
  if (!empId) { document.getElementById('calGrid').innerHTML = '<div class="cal-loading">正在加载员工数据...</div>'; return; }
  document.getElementById('calMonthLabel').textContent = currentYear + '年' + currentMonth + '月';
  document.getElementById('calGrid').innerHTML = '<div class="cal-loading">加载中...</div>';

  fetch('/api/attendance/calendar?empId=' + empId + '&year=' + currentYear + '&month=' + currentMonth)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderCalendar(res.data);
      } else {
        document.getElementById('calGrid').innerHTML = '<div class="cal-error">数据加载失败：' + res.msg + '</div>';
      }
    })
    .catch(function(e) {
      document.getElementById('calGrid').innerHTML = '<div class="cal-error">网络请求失败，请确认后端服务已启动。<br>错误信息：' + e.message + '</div>';
    });
}

function loadCalendarStats() {
  var empId = document.getElementById('empSelect').value;
  if (!empId) return;

  fetch('/api/attendance/stats?empId=' + empId + '&year=' + currentYear + '&month=' + currentMonth)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        updateStats(res.data);
      }
    })
    .catch(function() {
      // 统计加载失败不影响日历展示，保持原数值
    });
}

function renderCalendar(data) {
  var days = data.calendarDays, html = '';
  for (var i = 0; i < days.length; i++) {
    var d = days[i];
    if (d === null) { html += '<div class="cal-day placeholder"></div>'; continue; }
    var cls;
    switch (d.status) {
      case 1: cls = 'd-normal'; break;
      case 2: cls = 'd-late'; break;
      case 3: cls = 'd-absent'; break;
      case 4: cls = 'd-leave'; break;
      case 0: cls = 'd-weekend'; break;
      default: cls = 'd-norecord';
    }
    html += '<div class="cal-day"><div class="d-num">' + d.day + '</div><div class="d-status ' + cls + '">' + (d.statusText || '') + '</div></div>';
  }
  document.getElementById('calGrid').innerHTML = html;
}

function updateStats(s) {
  document.getElementById('statAttendanceDays').textContent = s.attendance_days || 0;
  document.getElementById('statLateDays').textContent = s.late_days || 0;
  document.getElementById('statMissingDays').textContent = s.missing_days || 0;
  document.getElementById('statLeaveDays').textContent = s.leave_days || 0;
}

function prevMonth() {
  if (currentMonth === 1) { currentMonth = 12; currentYear--; }
  else { currentMonth--; }
  loadCalendar();
  loadCalendarStats();
}

function nextMonth() {
  if (currentMonth === 12) { currentMonth = 1; currentYear++; }
  else { currentMonth++; }
  loadCalendar();
  loadCalendarStats();
}

/* ---------- 员工列表数据加载 ---------- */
function loadEmployeeList() {
  var tbody = document.getElementById('empTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  fetch('/api/employee/list')
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderEmployeeTable(res.data.rows);
        updateEmployeeCount(res.data.total);
        updateEmpRecordCount(res.data.rows.length);
      } else {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + res.msg + '</td></tr>';
      }
    })
    .catch(function(e) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">网络请求失败：' + e.message + '</td></tr>';
    });
}

function renderEmployeeTable(rows) {
  var html = '';
  for (var i = 0; i < rows.length; i++) {
    var emp = rows[i];
    html += '<tr>'
      + '<td>' + (emp.empNo || '-') + '</td>'
      + '<td>' + (emp.name || '-') + '</td>'
      + '<td>' + (emp.deptName || '-') + '</td>'
      + '<td>' + (emp.position || '-') + '</td>'
      + '<td>' + (emp.entryDate || '-') + '</td>'
      + '<td><span class="tag ' + getEmpStatusClass(emp.status) + '">' + getEmpStatusText(emp.status) + '</span></td>'
      + '<td class="txt-center"><button class="btn btn-xs" onclick="viewEmployeeDetail(' + emp.id + ')">详情</button></td>'
      + '</tr>';
  }
  document.getElementById('empTableBody').innerHTML = html;
}

function updateEmployeeCount(total) {
  var el = document.querySelector('.stat-card.blue .stat-num');
  if (el) el.textContent = total;
}

function updateEmpRecordCount(count) {
  var el = document.getElementById('empRecordCount');
  if (el) el.textContent = count + '条记录';
}

/* ---------- 员工选择下拉框动态加载 ---------- */
function loadEmployeeSelect() {
  var select = document.getElementById('empSelect');
  if (!select) return;
  fetch('/api/employee/list')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        var html = '';
        for (var i = 0; i < res.data.rows.length; i++) {
          var emp = res.data.rows[i];
          html += '<option value="' + emp.id + '">' + emp.name + ' (' + emp.empNo + ')</option>';
        }
        select.innerHTML = html;
        // 加载完员工列表后，初始化工作台员工表格和考勤日历
        loadEmployeeList();
        loadCalendar();
        loadCalendarStats();
      }
    })
    .catch(function() {
      // 加载失败保留原有选项，不影响日历功能
    });
}

/* ---------- 员工详情页跳转 ---------- */
function viewEmployeeDetail(empId) {
  window.location.href = 'pages/employee-detail.html?empId=' + empId;
}

/* ---------- 员工详情页数据加载 ---------- */
function loadEmployeeDetail() {
  var params = new URLSearchParams(window.location.search);
  var empId = params.get('empId');
  if (!empId) {
    document.getElementById('detailContent').innerHTML = '<div class="cal-error">参数错误：缺少员工ID</div>';
    return;
  }

  fetch('/api/employee/detail?empId=' + empId)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderEmployeeDetail(res.data);
      } else {
        document.getElementById('detailContent').innerHTML = '<div class="cal-error">' + res.msg + '</div>';
      }
    })
    .catch(function(e) {
      document.getElementById('detailContent').innerHTML = '<div class="cal-error">网络请求失败：' + e.message + '</div>';
    });
}

function renderEmployeeDetail(data) {
  var emp = data.employee;
  var stats = data.attendanceStats;
  var records = data.recentRecords;

  // 更新面包屑
  document.getElementById('detailCrumb').textContent = emp.name + ' (' + emp.empNo + ')';

  // 基本信息
  document.getElementById('infoEmpNo').textContent = emp.empNo || '-';
  document.getElementById('infoName').textContent = emp.name || '-';
  document.getElementById('infoGender').textContent = getGenderText(emp.gender);
  document.getElementById('infoDept').textContent = emp.deptName || '-';
  document.getElementById('infoPosition').textContent = emp.position || '-';
  document.getElementById('infoEntryDate').textContent = emp.entryDate || '-';
  document.getElementById('infoStatus').innerHTML = '<span class="tag ' + getEmpStatusClass(emp.status) + '">' + getEmpStatusText(emp.status) + '</span>';
  document.getElementById('infoPhone').textContent = emp.phone || '-';
  document.getElementById('infoEmail').textContent = emp.email || '-';

  // 考勤统计
  document.getElementById('statTotalRecords').textContent = stats.total_records || 0;
  document.getElementById('statNormalDays').textContent = stats.normal_days || 0;
  document.getElementById('statLateDays2').textContent = stats.late_days || 0;
  document.getElementById('statMissingDays2').textContent = stats.missing_days || 0;
  document.getElementById('statLeaveDays2').textContent = stats.leave_days || 0;
  document.getElementById('statAttendanceDays2').textContent = stats.attendance_days || 0;

  // 近期打卡记录
  if (records && records.length > 0) {
    var html = '';
    for (var i = 0; i < records.length; i++) {
      var r = records[i];
      html += '<tr>'
        + '<td>' + (r.checkDate || '-') + '</td>'
        + '<td>' + (r.checkInTime || '-') + '</td>'
        + '<td>' + (r.checkOutTime || '-') + '</td>'
        + '<td><span class="tag ' + getAttendanceStatusClass(r.status) + '">' + getAttendanceStatusText(r.status) + '</span></td>'
        + '</tr>';
    }
    document.getElementById('recentTableBody').innerHTML = html;
  } else {
    document.getElementById('recentTableBody').innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--gray-400);padding:30px;">暂无考勤记录</td></tr>';
  }
}

/* ---------- 工具函数 ---------- */
function getEmpStatusText(status) {
  if (status === 1) return '在职';
  if (status === 2) return '试用期';
  if (status === 3) return '离职';
  return '未知';
}

function getEmpStatusClass(status) {
  if (status === 1) return 't-green';
  if (status === 2) return 't-yellow';
  if (status === 3) return 't-red';
  return 't-gray';
}

function getGenderText(gender) {
  return gender === 1 ? '男' : '女';
}

function getAttendanceStatusText(status) {
  switch (status) {
    case 1: return '正常';
    case 2: return '迟到';
    case 3: return '缺卡';
    case 4: return '请假';
    default: return '未打卡';
  }
}

function getAttendanceStatusClass(status) {
  switch (status) {
    case 1: return 't-green';
    case 2: return 't-yellow';
    case 3: return 't-red';
    case 4: return 't-blue';
    default: return 't-gray';
  }
}
