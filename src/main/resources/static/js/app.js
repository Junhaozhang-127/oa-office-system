/* ================================================================
 * OA协同办公平台 — 交互脚本
 * ================================================================ */
var currentYear  = new Date().getFullYear();
var currentMonth = new Date().getMonth() + 1;

/* ---------- 面板切换 ---------- */
function switchTab(n, el) {
  // 离开数据看板时销毁图表
  if (n !== 2) disposeAllCharts();

  document.querySelectorAll('.panel').forEach(function(p, i) { p.classList.toggle('active', i === n); });
  document.querySelectorAll('.nav-item').forEach(function(i) { i.classList.remove('active'); });
  if (el) el.classList.add('active');
  var titles = ['工作台', '考勤日历', '数据看板', '审批流', '通知中心', '会议预约', '请假申请', '加班申请', '我的申请', '报销申请', '公告列表', '公告管理'];
  document.getElementById('crumbText').textContent = titles[n];
  // 离开数据看板时销毁图表
  if (n !== 2) disposeAllCharts();

  if (n === 0) loadEmployeeList();
  if (n === 1) { loadCalendar(); loadCalendarStats(); }
  if (n === 2) loadDashboard();
  if (n === 3) { loadApprovalPage(); }
  if (n === 4) loadNotifications();
  if (n === 5) { loadMeetingRoomList(); loadMyReservations(); loadScheduleSelects(); }
  if (n === 6) { populateLeaveEmpSelect(); }
  if (n === 7) { populateOvertimeEmpSelect(); }
  if (n === 8) { populateAppEmpSelect(); loadMyApplications(); }
  if (n === 9) { populateExpEmpSelect(); }
  if (n === 10) loadAnnouncementList();
  if (n === 11) loadAnnounceManageList();
  refreshUnreadBadge();
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
        // 同步到会议预约和申请模块的员工下拉框
        var bookingSelect = document.getElementById('bookingEmpSelect');
        var myMeetingSelect = document.getElementById('myMeetingEmpSelect');
        var leaveEmpSelect = document.getElementById('leaveEmpId');
        var overtimeEmpSelect = document.getElementById('overtimeEmpId');
        var myAppEmpSelect = document.getElementById('myAppEmpId');
        if (bookingSelect) bookingSelect.innerHTML = html;
        if (myMeetingSelect) myMeetingSelect.innerHTML = html;
        if (leaveEmpSelect) leaveEmpSelect.innerHTML = html;
        if (overtimeEmpSelect) overtimeEmpSelect.innerHTML = html;
        if (myAppEmpSelect) myAppEmpSelect.innerHTML = html;
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

/* ================================================================
 * 会议预约模块
 * ================================================================ */

/* ---------- 会议室列表加载 ---------- */
function loadMeetingRoomList() {
  var container = document.getElementById('meetingRoomList');
  if (!container) return;
  container.innerHTML = '<div class="cal-loading">加载中...</div>';

  fetch('/api/meeting-room/list')
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderMeetingRoomCards(res.data);
        document.getElementById('statRoomCount').textContent = res.data.length;
        document.getElementById('meetingRoomCountHint').textContent = res.data.length + '间';
        populateBookingRoomSelect(res.data);
      } else {
        container.innerHTML = '<div class="cal-error">加载失败：' + res.msg + '</div>';
      }
    })
    .catch(function(e) {
      container.innerHTML = '<div class="cal-error">网络请求失败：' + e.message + '</div>';
    });
}

function renderMeetingRoomCards(rooms) {
  var html = '';
  for (var i = 0; i < rooms.length; i++) {
    var r = rooms[i];
    html += '<div class="room-card">'
      + '<div class="room-card-header">'
      + '<span class="room-card-name">' + escapeHtml(r.roomName) + '</span>'
      + '<span class="room-card-code">' + escapeHtml(r.roomCode) + '</span>'
      + '</div>'
      + '<div class="room-card-body">'
      + '<div class="room-card-info">'
      + '<span class="room-card-icon">&#x1f465;</span> 容纳 ' + r.capacity + ' 人'
      + '</div>'
      + '<div class="room-card-info">'
      + '<span class="room-card-icon">&#x1f4cd;</span> ' + escapeHtml(r.location || '未设置')
      + '</div>'
      + '</div>'
      + '<div class="room-card-footer">'
      + '<button class="btn btn-sm" onclick="selectBookingRoom(' + r.id + ',\'' + escapeHtml(r.roomName) + '\')">预约此会议室</button>'
      + '</div>'
      + '</div>';
  }
  if (rooms.length === 0) {
    html = '<div style="text-align:center;padding:40px;color:var(--gray-400);">暂无可用的会议室</div>';
  }
  document.getElementById('meetingRoomList').innerHTML = html;
}

function populateBookingRoomSelect(rooms) {
  var select = document.getElementById('bookingRoomSelect');
  var scheduleSelect = document.getElementById('scheduleRoomSelect');
  var html = '<option value="">请选择会议室</option>';
  for (var i = 0; i < rooms.length; i++) {
    html += '<option value="' + rooms[i].id + '">' + escapeHtml(rooms[i].roomName) + ' (' + escapeHtml(rooms[i].roomCode) + ')</option>';
  }
  if (select) select.innerHTML = html;
  if (scheduleSelect) scheduleSelect.innerHTML = html;
}

function selectBookingRoom(roomId, roomName) {
  var select = document.getElementById('bookingRoomSelect');
  if (select) {
    select.value = roomId;
    select.scrollIntoView({ behavior: 'smooth' });
  }
}

/* ---------- 预约表单提交 ---------- */
function submitReservation() {
  var msgEl = document.getElementById('bookingFormMsg');
  msgEl.style.display = 'none';

  var roomId = document.getElementById('bookingRoomSelect').value;
  var empId = document.getElementById('bookingEmpSelect').value;
  var dateVal = document.getElementById('bookingDate').value;
  var startTimeVal = document.getElementById('bookingStartTime').value;
  var endTimeVal = document.getElementById('bookingEndTime').value;
  var title = document.getElementById('bookingTitle').value.trim();
  var desc = document.getElementById('bookingDesc').value.trim();

  if (!roomId)  { showFormMsg('请选择会议室', 'error'); return; }
  if (!empId)   { showFormMsg('请选择预约人', 'error'); return; }
  if (!dateVal) { showFormMsg('请选择预约日期', 'error'); return; }
  if (!startTimeVal) { showFormMsg('请选择开始时间', 'error'); return; }
  if (!endTimeVal)   { showFormMsg('请选择结束时间', 'error'); return; }
  if (!title)   { showFormMsg('请输入会议主题', 'error'); return; }

  if (endTimeVal <= startTimeVal) {
    showFormMsg('结束时间必须大于开始时间', 'error');
    return;
  }

  var startTime = dateVal + 'T' + startTimeVal + ':00';
  var endTime = dateVal + 'T' + endTimeVal + ':00';

  var formData = new URLSearchParams();
  formData.append('roomId', roomId);
  formData.append('empId', empId);
  formData.append('meetingTitle', title);
  formData.append('startTime', startTime);
  formData.append('endTime', endTime);
  formData.append('description', desc);

  fetch('/api/meeting-reservation/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString()
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        showFormMsg('预约成功！单号：' + res.data.reservationNo, 'success');
        loadMyReservations();
        loadRoomSchedule();
        loadRoomStats();
      } else {
        showFormMsg(res.msg, 'error');
      }
    })
    .catch(function(e) {
      showFormMsg('网络请求失败：' + e.message, 'error');
    });
}

function showFormMsg(msg, type) {
  var el = document.getElementById('bookingFormMsg');
  el.style.display = 'block';
  el.className = 'form-msg form-msg-' + type;
  el.textContent = msg;
}

/* ---------- 预约视图选择器 ---------- */
function loadScheduleSelects() {
  var today = new Date().toISOString().split('T')[0];
  var dateInput = document.getElementById('scheduleDate');
  if (dateInput && !dateInput.value) dateInput.value = today;
}

/* ---------- 会议室今日预约时间线 ---------- */
function loadRoomSchedule() {
  var roomId = document.getElementById('scheduleRoomSelect').value;
  var dateVal = document.getElementById('scheduleDate').value;
  var container = document.getElementById('scheduleTimeline');

  if (!roomId) {
    container.innerHTML = '<div class="cal-placeholder">请选择会议室查看预约情况</div>';
    return;
  }

  container.innerHTML = '<div class="cal-loading">加载中...</div>';

  fetch('/api/meeting-reservation/room-schedule?roomId=' + roomId + '&date=' + dateVal)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderScheduleTimeline(res.data);
      } else {
        container.innerHTML = '<div class="cal-error">加载失败：' + res.msg + '</div>';
      }
    })
    .catch(function(e) {
      container.innerHTML = '<div class="cal-error">网络请求失败：' + e.message + '</div>';
    });
}

function renderScheduleTimeline(reservations) {
  if (reservations.length === 0) {
    document.getElementById('scheduleTimeline').innerHTML = '<div style="text-align:center;padding:32px;color:var(--gray-400);">该会议室当日暂无预约</div>';
    return;
  }

  var html = '<div class="tl-wrap">';
  for (var i = 0; i < reservations.length; i++) {
    var r = reservations[i];
    var startTime = formatDateTime(r.startTime);
    var endTime = formatDateTime(r.endTime);
    html += '<div class="tl-item done">'
      + '<div class="tl-time">' + startTime + ' — ' + endTime + '</div>'
      + '<div class="tl-text"><strong>' + escapeHtml(r.meetingTitle) + '</strong> · 预约人ID：' + r.empId + '</div>'
      + '</div>';
  }
  html += '</div>';
  document.getElementById('scheduleTimeline').innerHTML = html;
}

/* ---------- 我的会议列表 ---------- */
function loadMyReservations() {
  var select = document.getElementById('myMeetingEmpSelect');
  var empId = select ? select.value : '';
  if (!empId) {
    var empSelectMain = document.getElementById('empSelect');
    if (empSelectMain) empId = empSelectMain.value;
  }
  if (!empId) {
    document.getElementById('myReservationTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">请选择员工后查看会议列表</td></tr>';
    return;
  }

  document.getElementById('myReservationTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  fetch('/api/meeting-reservation/my-list?empId=' + empId)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderMyReservations(res.data);
        document.getElementById('statMyReservationCount').textContent = res.data.length;
      } else {
        document.getElementById('myReservationTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + res.msg + '</td></tr>';
      }
    })
    .catch(function(e) {
      document.getElementById('myReservationTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">网络请求失败：' + e.message + '</td></tr>';
    });
}

function renderMyReservations(reservations) {
  if (reservations.length === 0) {
    document.getElementById('myReservationTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">暂无会议预约记录</td></tr>';
    return;
  }

  var html = '';
  for (var i = 0; i < reservations.length; i++) {
    var r = reservations[i];
    var statusTag = r.status === 1 ? '<span class="tag t-green">有效</span>' : '<span class="tag t-gray">已取消</span>';
    var cancelBtn = r.status === 1
      ? '<button class="btn btn-danger btn-xs" onclick="cancelReservation(' + r.id + ')">取消</button>'
      : '<span style="font-size:11px;color:var(--gray-400);">已取消</span>';

    html += '<tr>'
      + '<td>' + (r.reservationNo || '-') + '</td>'
      + '<td>会议室' + r.roomId + '</td>'
      + '<td>' + escapeHtml(r.meetingTitle) + '</td>'
      + '<td>' + formatDateTime(r.startTime) + '</td>'
      + '<td>' + formatDateTime(r.endTime) + '</td>'
      + '<td>' + statusTag + '</td>'
      + '<td class="txt-center">' + cancelBtn + '</td>'
      + '</tr>';
  }
  document.getElementById('myReservationTableBody').innerHTML = html;
}

/* ---------- 取消预约 ---------- */
function cancelReservation(id) {
  if (!confirm('确认要取消此预约吗？')) return;

  var formData = new URLSearchParams();
  formData.append('id', id);

  fetch('/api/meeting-reservation/cancel', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString()
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        loadMyReservations();
        loadRoomSchedule();
        loadRoomStats();
      } else {
        alert(res.msg);
      }
    })
    .catch(function(e) {
      alert('网络请求失败：' + e.message);
    });
}

/* ---------- 统计刷新 ---------- */
function loadRoomStats() {
  var today = new Date().toISOString().split('T')[0];
  var roomId = document.getElementById('scheduleRoomSelect').value;
  if (roomId) {
    fetch('/api/meeting-reservation/room-schedule?roomId=' + roomId + '&date=' + today)
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (res.code === 200) {
          document.getElementById('statTodayReservationCount').textContent = res.data.length;
          var upcoming = 0;
          var now = new Date();
          for (var i = 0; i < res.data.length; i++) {
            if (new Date(res.data[i].startTime) > now) upcoming++;
          }
          document.getElementById('statUpcomingCount').textContent = upcoming;
        }
      })
      .catch(function() {});
  }
}

/* ---------- 工具函数 ---------- */
function formatDateTime(dtStr) {
  if (!dtStr) return '-';
  var dt = dtStr.replace('T', ' ').substring(0, 16);
  return dt;
}

function escapeHtml(str) {
  if (!str) return '';
  var div = document.createElement('div');
  div.appendChild(document.createTextNode(str));
  return div.innerHTML;
}

/* ================================================================
 * 申请中心模块（请假/加班/我的申请）
 * ================================================================ */

/* ---------- 申请下拉框占位函数 ---------- */
function populateLeaveEmpSelect() {}
function populateOvertimeEmpSelect() {}
function populateAppEmpSelect() {}

/* ---------- 请假申请：自动计算天数 ---------- */
function setupLeaveCalc() {
  var startEl = document.getElementById('leaveStartDate');
  var endEl = document.getElementById('leaveEndDate');
  if (!startEl || !endEl) return;
  var handler = function() {
    var start = startEl.value, end = endEl.value;
    if (start && end) {
      var s = new Date(start), e = new Date(end);
      var diff = Math.round((e - s) / (1000 * 60 * 60 * 24)) + 1;
      if (diff > 0) {
        document.getElementById('leaveDays').textContent = diff + ' 天';
      } else {
        document.getElementById('leaveDays').textContent = '日期无效';
      }
    }
  };
  startEl.addEventListener('change', handler);
  endEl.addEventListener('change', handler);
}

/* ---------- 加班申请：自动计算小时数 ---------- */
function setupOvertimeCalc() {
  var startEl = document.getElementById('overtimeStartTime');
  var endEl = document.getElementById('overtimeEndTime');
  if (!startEl || !endEl) return;
  var handler = function() {
    var start = startEl.value, end = endEl.value;
    if (start && end) {
      var s = new Date(start), e = new Date(end);
      var diffHours = (e - s) / (1000 * 60 * 60);
      if (diffHours > 0) {
        document.getElementById('overtimeHours').textContent = diffHours.toFixed(1) + ' 小时';
      } else {
        document.getElementById('overtimeHours').textContent = '时间无效';
      }
    }
  };
  startEl.addEventListener('change', handler);
  endEl.addEventListener('change', handler);
}

document.addEventListener('DOMContentLoaded', function() {
  setupLeaveCalc();
  setupOvertimeCalc();
});

/* ---------- 请假申请：提交 ---------- */
function submitLeave() {
  var msgEl = document.getElementById('leaveMsg');

  var empId = parseInt(document.getElementById('leaveEmpId').value) || null;
  var leaveType = parseInt(document.getElementById('leaveType').value) || null;
  var startDate = document.getElementById('leaveStartDate').value;
  var endDate = document.getElementById('leaveEndDate').value;
  var reason = document.getElementById('leaveReason').value.trim();

  if (!empId) { showLeaveMsg('请选择申请人', 'error'); return; }
  if (!leaveType) { showLeaveMsg('请选择请假类型', 'error'); return; }
  if (!startDate) { showLeaveMsg('请选择开始日期', 'error'); return; }
  if (!endDate) { showLeaveMsg('请选择结束日期', 'error'); return; }
  if (new Date(endDate) < new Date(startDate)) { showLeaveMsg('结束日期必须晚于开始日期', 'error'); return; }
  if (!reason) { showLeaveMsg('请输入申请原因', 'error'); return; }

  var payload = {
    empId: empId,
    leaveType: leaveType,
    startDate: startDate,
    endDate: endDate,
    reason: reason
  };

  fetch('/api/leave-requests', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        showLeaveMsg('提交成功！单号：' + res.data.leaveNo + '，天数：' + res.data.days + '，状态：' + res.data.statusText, 'success');
      } else {
        showLeaveMsg(res.msg, 'error');
      }
    })
    .catch(function(e) {
      showLeaveMsg('网络请求失败：' + e.message, 'error');
    });
}

function resetLeaveForm() {
  document.getElementById('leaveType').value = '';
  document.getElementById('leaveStartDate').value = '';
  document.getElementById('leaveEndDate').value = '';
  document.getElementById('leaveReason').value = '';
  document.getElementById('leaveDays').textContent = '0 天';
  var msgEl = document.getElementById('leaveMsg');
  msgEl.style.display = 'none';
  msgEl.textContent = '';
}

function showLeaveMsg(text, type) {
  var msgEl = document.getElementById('leaveMsg');
  msgEl.textContent = text;
  msgEl.style.display = 'block';
  msgEl.className = 'form-msg form-msg-' + type;
}

/* ---------- 加班申请：提交 ---------- */
function submitOvertime() {
  var msgEl = document.getElementById('overtimeMsg');

  var empId = parseInt(document.getElementById('overtimeEmpId').value) || null;
  var overtimeType = parseInt(document.getElementById('overtimeType').value) || null;
  var startTime = document.getElementById('overtimeStartTime').value;
  var endTime = document.getElementById('overtimeEndTime').value;
  var reason = document.getElementById('overtimeReason').value.trim();

  if (!empId) { showOvertimeMsg('请选择申请人', 'error'); return; }
  if (!overtimeType) { showOvertimeMsg('请选择加班类型', 'error'); return; }
  if (!startTime) { showOvertimeMsg('请选择开始时间', 'error'); return; }
  if (!endTime) { showOvertimeMsg('请选择结束时间', 'error'); return; }
  if (new Date(endTime) < new Date(startTime)) { showOvertimeMsg('结束时间必须晚于开始时间', 'error'); return; }
  if (!reason) { showOvertimeMsg('请输入加班原因', 'error'); return; }

  var payload = {
    empId: empId,
    overtimeType: overtimeType,
    startTime: startTime,
    endTime: endTime,
    reason: reason
  };

  fetch('/api/overtime-requests', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        showOvertimeMsg('提交成功！单号：' + res.data.overtimeNo + '，小时数：' + res.data.hours + '，状态：' + res.data.statusText, 'success');
      } else {
        showOvertimeMsg(res.msg, 'error');
      }
    })
    .catch(function(e) {
      showOvertimeMsg('网络请求失败：' + e.message, 'error');
    });
}

function resetOvertimeForm() {
  document.getElementById('overtimeType').value = '';
  document.getElementById('overtimeStartTime').value = '';
  document.getElementById('overtimeEndTime').value = '';
  document.getElementById('overtimeReason').value = '';
  document.getElementById('overtimeHours').textContent = '0 小时';
  var msgEl = document.getElementById('overtimeMsg');
  msgEl.style.display = 'none';
  msgEl.textContent = '';
}

function showOvertimeMsg(text, type) {
  var msgEl = document.getElementById('overtimeMsg');
  msgEl.textContent = text;
  msgEl.style.display = 'block';
  msgEl.className = 'form-msg form-msg-' + type;
}

/* ---------- 我的申请：加载列表 ---------- */
function loadMyApplications() {
  var empId = document.getElementById('myAppEmpId').value;
  if (!empId) return;

  var tbody = document.getElementById('appTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  fetch('/api/applications/my?empId=' + empId)
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      if (res.code === 200) {
        renderApplicationTable(res.data.rows);
      } else {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + res.msg + '</td></tr>';
      }
    })
    .catch(function(e) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:30px;">网络请求失败：' + e.message + '</td></tr>';
    });
}

function renderApplicationTable(rows) {
  var tbody = document.getElementById('appTableBody');
  if (!rows || rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--gray-400);padding:30px;">暂无申请记录</td></tr>';
    return;
  }
  var html = '';
  for (var i = 0; i < rows.length; i++) {
    var app = rows[i];
    html += '<tr>'
      + '<td><span class="tag ' + getAppTypeClass(app.applicationType) + '">' + (app.applicationTypeText || '-') + '</span></td>'
      + '<td>' + (app.leaveNo || app.overtimeNo || '-') + '</td>'
      + '<td>' + (app.timeRange || '-') + '</td>'
      + '<td>' + (app.amount != null ? app.amount : '-') + ' ' + (app.amountUnit || '') + '</td>'
      + '<td style="max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">' + (app.reason || '-') + '</td>'
      + '<td><span class="tag ' + getProcessStatusClass(app.status) + '">' + getProcessStatusText(app.status) + '</span></td>'
      + '<td>' + (formatDateTime(app.createTime) || '-') + '</td>'
      + '</tr>';
  }
  tbody.innerHTML = html;
}

/* ---------- 申请中心：状态/类型映射 ---------- */
function getProcessStatusText(status) {
  switch (status) {
    case 'PENDING': return '待审批';
    case 'MANAGER_APPROVED': return '经理已审批';
    case 'FINANCE_APPROVED': return '财务已审批';
    case 'REJECTED': return '已驳回';
    case 'COMPLETED': return '已完成';
    default: return status || '未知';
  }
}

function getProcessStatusClass(status) {
  switch (status) {
    case 'PENDING': return 't-yellow';
    case 'MANAGER_APPROVED': return 't-blue';
    case 'FINANCE_APPROVED': return 't-blue';
    case 'REJECTED': return 't-red';
    case 'COMPLETED': return 't-green';
    default: return 't-gray';
  }
}

function getAppTypeClass(type) {
  if (type === 'LEAVE') return 't-blue';
  if (type === 'OVERTIME') return 't-green';
  return 't-gray';
}

function formatDateTime(dt) {
  if (!dt) return null;
  if (typeof dt === 'string' && dt.indexOf('T') > -1) {
    return dt.replace('T', ' ').substring(0, 19);
  }
  return dt;
}

/* ================================================================
 * 数据看板模块 — ECharts图表 + 统计卡片 + 报表导出
 * ================================================================ */
var dashCharts = {};

/** 图表颜色体系（与CSS变量对齐） */
var CHART_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#ec4899', '#06b6d4'];

/** 初始化日期筛选默认值（本月） */
function initDashboardDates() {
  var now = new Date();
  var year = now.getFullYear();
  var month = String(now.getMonth() + 1).padStart(2, '0');
  var firstDay = year + '-' + month + '-01';
  var lastDay = new Date(year, now.getMonth() + 1, 0);
  var endStr = lastDay.getFullYear() + '-' + String(lastDay.getMonth() + 1).padStart(2, '0') + '-' + String(lastDay.getDate()).padStart(2, '0');

  var startEl = document.getElementById('dashStartDate');
  var endEl = document.getElementById('dashEndDate');
  if (startEl && !startEl.value) startEl.value = firstDay;
  if (endEl && !endEl.value) endEl.value = endStr;
}

/** 加载所有看板数据 */
function loadDashboard() {
  initDashboardDates();
  loadDeptBarChart();
  loadLeavePieChart();
  loadExpenseLineChart();
  loadAnomalyGaugeChart();
}

/** 刷新看板 */
function refreshDashboard() {
  disposeAllCharts();
  loadDashboard();
}

/** 部门人数柱状图 */
function loadDeptBarChart() {
  var dom = document.getElementById('chartDeptBar');
  if (!dom) return;
  if (dashCharts.deptBar) dashCharts.deptBar.dispose();

  var chart = echarts.init(dom);
  dashCharts.deptBar = chart;
  chart.showLoading({ text: '加载中...', color: '#3b82f6', maskColor: 'rgba(255,255,255,0.8)' });

  fetch('/api/dashboard/department-count')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      chart.hideLoading();
      if (res.code === 200 && res.data && res.data.length > 0) {
        var names = [], values = [];
        for (var i = 0; i < res.data.length; i++) {
          names.push(res.data[i].departmentName);
          values.push(res.data[i].employeeCount);
        }
        chart.setOption({
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '10%', top: '5%', containLabel: true },
          xAxis: { type: 'category', data: names, axisLabel: { fontSize: 11, color: '#64748b' } },
          yAxis: { type: 'value', name: '人数', nameTextStyle: { fontSize: 11, color: '#94a3b8' },
            axisLabel: { fontSize: 11, color: '#64748b' }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
          series: [{ type: 'bar', data: values, itemStyle: { color: '#3b82f6', borderRadius: [6, 6, 0, 0] },
            barWidth: '50%', emphasis: { itemStyle: { color: '#2563eb' } } }]
        });
      } else {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center',
          textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 400 } } });
      }
    })
    .catch(function(e) {
      chart.hideLoading();
      console.error('部门人数图表加载失败:', e);
      chart.setOption({ title: { text: '加载失败', left: 'center', top: 'center',
        textStyle: { color: '#ef4444', fontSize: 14 } } });
    });
}

/** 请假类型饼图 */
function loadLeavePieChart() {
  var dom = document.getElementById('chartLeavePie');
  if (!dom) return;
  if (dashCharts.leavePie) dashCharts.leavePie.dispose();

  var chart = echarts.init(dom);
  dashCharts.leavePie = chart;
  chart.showLoading({ text: '加载中...', color: '#3b82f6', maskColor: 'rgba(255,255,255,0.8)' });

  fetch('/api/dashboard/leave-type-stats')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      chart.hideLoading();
      if (res.code === 200 && res.data && res.data.length > 0) {
        var pieData = [];
        for (var i = 0; i < res.data.length; i++) {
          pieData.push({ name: res.data[i].leaveTypeName, value: res.data[i].count });
        }
        chart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
          legend: { orient: 'horizontal', bottom: 0, textStyle: { fontSize: 11, color: '#64748b' } },
          series: [{
            type: 'pie', radius: ['45%', '72%'], center: ['50%', '48%'],
            itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
            label: { show: true, fontSize: 10, color: '#64748b' },
            emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
            data: pieData
          }]
        });
      } else {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center',
          textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 400 } } });
      }
    })
    .catch(function(e) {
      chart.hideLoading();
      console.error('请假类型图表加载失败:', e);
      chart.setOption({ title: { text: '加载失败', left: 'center', top: 'center',
        textStyle: { color: '#ef4444', fontSize: 14 } } });
    });
}

/** 报销趋势折线图 */
function loadExpenseLineChart() {
  var dom = document.getElementById('chartExpenseLine');
  if (!dom) return;
  if (dashCharts.expenseLine) dashCharts.expenseLine.dispose();

  var chart = echarts.init(dom);
  dashCharts.expenseLine = chart;
  chart.showLoading({ text: '加载中...', color: '#3b82f6', maskColor: 'rgba(255,255,255,0.8)' });

  var startDate = document.getElementById('dashStartDate').value;
  var endDate = document.getElementById('dashEndDate').value;
  var params = '';
  if (startDate) params += '&startDate=' + startDate;
  if (endDate) params += '&endDate=' + endDate;

  fetch('/api/dashboard/reimbursement-trend?' + params.substring(1))
    .then(function(r) { return r.json(); })
    .then(function(res) {
      chart.hideLoading();
      if (res.code === 200 && res.data && res.data.length > 0) {
        var dates = [], amounts = [], counts = [];
        for (var i = 0; i < res.data.length; i++) {
          dates.push(res.data[i].date);
          amounts.push(Number(res.data[i].amount));
          counts.push(res.data[i].count);
        }
        chart.setOption({
          tooltip: { trigger: 'axis', formatter: function(params) {
            var d = params[0]; return d.axisValue + '<br/>金额: ¥' + d.value.toLocaleString() + '<br/>单数: ' + counts[d.dataIndex];
          }},
          grid: { left: '3%', right: '4%', bottom: '10%', top: '5%', containLabel: true },
          xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10, color: '#64748b', rotate: 30 } },
          yAxis: { type: 'value', name: '金额(元)', nameTextStyle: { fontSize: 11, color: '#94a3b8' },
            axisLabel: { fontSize: 11, color: '#64748b' }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
          series: [{
            type: 'line', data: amounts, smooth: true, symbol: 'circle', symbolSize: 6,
            lineStyle: { color: '#3b82f6', width: 2.5 },
            itemStyle: { color: '#3b82f6' },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1,
              [{ offset: 0, color: 'rgba(59,130,246,0.25)' }, { offset: 1, color: 'rgba(59,130,246,0.02)' }]) }
          }]
        });
      } else {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center',
          textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 400 } } });
      }
    })
    .catch(function(e) {
      chart.hideLoading();
      console.error('报销趋势图表加载失败:', e);
      chart.setOption({ title: { text: '加载失败', left: 'center', top: 'center',
        textStyle: { color: '#ef4444', fontSize: 14 } } });
    });
}

/** 考勤异常率仪表盘 */
function loadAnomalyGaugeChart() {
  var dom = document.getElementById('chartAnomalyGauge');
  if (!dom) return;
  if (dashCharts.anomalyGauge) dashCharts.anomalyGauge.dispose();

  var chart = echarts.init(dom);
  dashCharts.anomalyGauge = chart;
  chart.showLoading({ text: '加载中...', color: '#3b82f6', maskColor: 'rgba(255,255,255,0.8)' });

  var startDate = document.getElementById('dashStartDate').value;
  var endDate = document.getElementById('dashEndDate').value;
  var params = '';
  if (startDate) params += '&startDate=' + startDate;
  if (endDate) params += '&endDate=' + endDate;

  fetch('/api/dashboard/attendance-anomaly?' + params.substring(1))
    .then(function(r) { return r.json(); })
    .then(function(res) {
      chart.hideLoading();
      if (res.code === 200 && res.data) {
        var d = res.data;
        var rate = d.abnormalRate != null ? Number(d.abnormalRate) : 0;
        chart.setOption({
          series: [{
            type: 'gauge', startAngle: 210, endAngle: -30, center: ['50%', '55%'], radius: '85%',
            min: 0, max: 100, splitNumber: 10,
            axisLine: { lineStyle: { width: 14,
              color: [[0.3, '#10b981'], [0.6, '#f59e0b'], [1, '#ef4444']] } },
            pointer: { length: '60%', width: 6, itemStyle: { color: '#334155' } },
            axisTick: { distance: -14, length: 6, lineStyle: { width: 1.5, color: '#94a3b8' } },
            splitLine: { distance: -18, length: 14, lineStyle: { width: 2.5, color: '#94a3b8' } },
            axisLabel: { color: '#64748b', fontSize: 10, distance: 20 },
            anchor: { show: true, showAbove: true, size: 18, itemStyle: { borderWidth: 1.5 } },
            title: { offsetCenter: [0, '75%'], fontSize: 12, color: '#64748b' },
            detail: {
              valueAnimation: true, fontSize: 18, fontWeight: 700,
              offsetCenter: [0, '55%'], color: '#1e293b',
              formatter: function(v) { return v.toFixed(1) + '%'; }
            },
            data: [{ value: rate, name: '异常率' }]
          }],
          graphic: [
            { type: 'text', left: 'center', bottom: 14,
              style: { text: '正常 ' + (d.normalCount || 0) + ' | 异常 ' + (d.abnormalCount || 0) + ' | 总计 ' + (d.totalCount || 0),
                fontSize: 11, fill: '#94a3b8', fontWeight: 500 } }
          ]
        });
      } else {
        chart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center',
          textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 400 } } });
      }
    })
    .catch(function(e) {
      chart.hideLoading();
      console.error('考勤异常率图表加载失败:', e);
      chart.setOption({ title: { text: '加载失败', left: 'center', top: 'center',
        textStyle: { color: '#ef4444', fontSize: 14 } } });
    });
}

/** 销毁所有图表实例 */
function disposeAllCharts() {
  Object.keys(dashCharts).forEach(function(key) {
    if (dashCharts[key]) {
      dashCharts[key].dispose();
      delete dashCharts[key];
    }
  });
}

/** 打开导出选择弹窗 */
function exportReport() {
  document.getElementById('exportModal').style.display = 'flex';
}

/** 关闭导出弹窗 */
function closeExportModal() {
  document.getElementById('exportModal').style.display = 'none';
}

/** 全选/取消全选 */
function toggleAllChecks(checked) {
  document.querySelectorAll('.export-check').forEach(function(cb) { cb.checked = checked; });
}

/** 执行导出 */
function doExport() {
  var checks = document.querySelectorAll('.export-check:checked');
  var sheets = [];
  checks.forEach(function(cb) { sheets.push(cb.value); });
  if (sheets.length === 0) { alert('请至少选择一个导出模块'); return; }

  var startDate = document.getElementById('dashStartDate').value;
  var endDate = document.getElementById('dashEndDate').value;

  var btn = document.getElementById('exportConfirmBtn');
  if (btn) { btn.disabled = true; btn.textContent = '⏳ 导出中...'; }

  fetch('/api/reports/export', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sheets: sheets, startDate: startDate, endDate: endDate })
  })
    .then(function(r) {
      if (!r.ok) throw new Error('导出失败: HTTP ' + r.status);
      var disposition = r.headers.get('Content-Disposition');
      var fileName = 'OA统计报表.xlsx';
      if (disposition) {
        var match = disposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/);
        if (match) fileName = decodeURIComponent(match[1].replace(/"/g, ''));
      }
      return r.blob().then(function(blob) { return { blob: blob, fileName: fileName }; });
    })
    .then(function(result) {
      closeExportModal();
      var url = window.URL.createObjectURL(result.blob);
      var a = document.createElement('a');
      a.href = url;
      a.download = result.fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      if (btn) { btn.disabled = false; btn.textContent = '📥 导出Excel'; }
    })
    .catch(function(e) {
      console.error('报表导出失败:', e);
      alert('报表导出失败：' + e.message);
      if (btn) { btn.disabled = false; btn.textContent = '📥 导出Excel'; }
    });
}

/** 窗口大小变化时resize所有图表 */
window.addEventListener('resize', function() {
  Object.keys(dashCharts).forEach(function(key) {
    if (dashCharts[key]) dashCharts[key].resize();
  });
});

/* ================================================================
   报销申请模块
   ================================================================ */

/** 加载报销人下拉框 */
function populateExpEmpSelect() {
  var sel = document.getElementById('expEmpId');
  if (!sel) return;
  if (sel.options.length > 1) return;
  fetch('/api/employee/list')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200 && res.data && res.data.rows) {
        sel.innerHTML = '<option value="">请选择员工</option>';
        res.data.rows.forEach(function(emp) {
          sel.innerHTML += '<option value="' + emp.id + '">' + emp.name + ' (' + emp.empNo + ')</option>';
        });
      }
    });
}

/** 发票上传处理 */
function handleInvoiceUpload(input) {
  var file = input.files[0];
  if (!file) return;
  if (!file.type.startsWith('image/')) { alert('请选择图片文件'); return; }
  if (file.size > 10 * 1024 * 1024) { alert('文件大小不能超过10MB'); return; }

  var formData = new FormData();
  formData.append('file', file);

  document.getElementById('uploadLoading').style.display = 'block';
  document.getElementById('uploadPlaceholder').style.display = 'none';
  document.getElementById('uploadPreview').style.display = 'none';

  fetch('/api/expense-report/upload', { method: 'POST', body: formData })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      document.getElementById('uploadLoading').style.display = 'none';
      if (res.code === 200) {
        document.getElementById('expInvoiceUrl').value = res.data.url;
        document.getElementById('invoiceThumb').src = res.data.url;
        document.getElementById('uploadPreview').style.display = 'block';
      } else {
        alert('上传失败：' + res.msg);
        document.getElementById('uploadPlaceholder').style.display = 'block';
      }
    })
    .catch(function(e) {
      document.getElementById('uploadLoading').style.display = 'none';
      document.getElementById('uploadPlaceholder').style.display = 'block';
      alert('上传失败：' + e.message);
    });
}

/** 移除已上传的发票 */
function removeInvoice() {
  document.getElementById('expInvoiceUrl').value = '';
  document.getElementById('invoiceFileInput').value = '';
  document.getElementById('uploadPreview').style.display = 'none';
  document.getElementById('uploadPlaceholder').style.display = 'block';
}

/** 添加明细行 */
function addExpenseItem() {
  var tbody = document.getElementById('expItemTableBody');
  var rowCount = tbody.querySelectorAll('.expense-item-row').length;
  var tr = document.createElement('tr');
  tr.className = 'expense-item-row';
  tr.innerHTML = '<td class="item-seq">' + (rowCount + 1) + '</td>'
    + '<td><input type="text" class="form-input item-name" placeholder="如：往返高铁票"></td>'
    + '<td><input type="number" class="form-input item-amount txt-right" placeholder="0.00" step="0.01" min="0" onchange="calcExpTotal()"></td>'
    + '<td><input type="date" class="form-input item-date"></td>'
    + '<td><input type="text" class="form-input item-remark" placeholder="备注"></td>'
    + '<td class="txt-center"><button class="btn btn-danger btn-xs" onclick="removeExpenseItem(this)">删除</button></td>';
  tbody.appendChild(tr);
  document.getElementById('expItemEmpty').style.display = 'none';
}

/** 删除明细行 */
function removeExpenseItem(btn) {
  var tr = btn.closest('tr');
  tr.parentNode.removeChild(tr);
  // 重新编号
  var rows = document.querySelectorAll('#expItemTableBody .expense-item-row');
  rows.forEach(function(row, i) { row.querySelector('.item-seq').textContent = i + 1; });
  if (rows.length === 0) {
    document.getElementById('expItemEmpty').style.display = '';
  }
  calcExpTotal();
}

/** 计算报销总额（从明细行累加） */
function calcExpTotal() {
  var total = 0;
  document.querySelectorAll('.item-amount').forEach(function(input) {
    var v = parseFloat(input.value);
    if (!isNaN(v) && v > 0) total += v;
  });
  document.getElementById('expTotalAmount').value = total.toFixed(2);
}

/** 提交报销单 */
function submitExpenseReport() {
  var empId = document.getElementById('expEmpId').value;
  if (!empId) { alert('请选择报销人'); return; }

  var expenseType = document.getElementById('expType').value;
  var description = document.getElementById('expDesc').value;
  var invoiceUrl = document.getElementById('expInvoiceUrl').value;

  // 收集明细
  var items = [];
  var rows = document.querySelectorAll('#expItemTableBody .expense-item-row');
  rows.forEach(function(row) {
    var name = row.querySelector('.item-name').value.trim();
    var amount = parseFloat(row.querySelector('.item-amount').value);
    var date = row.querySelector('.item-date').value;
    var remark = row.querySelector('.item-remark').value.trim();

    if (!name || isNaN(amount) || amount <= 0) return;
    items.push({
      itemName: name,
      amount: amount,
      expenseDate: date || null,
      remark: remark
    });
  });

  if (items.length === 0) { alert('请至少添加一条费用明细'); return; }

  var totalAmount = 0;
  items.forEach(function(it) { totalAmount += it.amount; });

  var body = {
    empId: parseInt(empId),
    expenseType: expenseType,
    totalAmount: totalAmount.toFixed(2),
    description: description,
    invoiceUrl: invoiceUrl,
    items: items
  };

  fetch('/api/expense-report/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        alert('报销单提交成功！单号：' + res.data.reportNo);
        resetExpenseForm();
      } else {
        alert('提交失败：' + res.msg);
      }
    })
    .catch(function(e) { alert('提交失败：' + e.message); });
}

/** 重置报销表单 */
function resetExpenseForm() {
  document.getElementById('expDesc').value = '';
  document.getElementById('expTotalAmount').value = '';
  document.getElementById('expInvoiceUrl').value = '';
  document.getElementById('invoiceFileInput').value = '';
  document.getElementById('uploadPreview').style.display = 'none';
  document.getElementById('uploadPlaceholder').style.display = 'block';
  var tbody = document.getElementById('expItemTableBody');
  tbody.innerHTML = '';
  document.getElementById('expItemEmpty').style.display = '';
  // 添加一个空行
  addExpenseItem();
}

// 上传区域点击和拖拽事件
document.addEventListener('DOMContentLoaded', function() {
  var zone = document.getElementById('invoiceUploadZone');
  if (!zone) return;
  zone.addEventListener('click', function() {
    document.getElementById('invoiceFileInput').click();
  });
  zone.addEventListener('dragover', function(e) {
    e.preventDefault();
    zone.classList.add('dragover');
  });
  zone.addEventListener('dragleave', function() {
    zone.classList.remove('dragover');
  });
  zone.addEventListener('drop', function(e) {
    e.preventDefault();
    zone.classList.remove('dragover');
    var files = e.dataTransfer.files;
    if (files.length > 0) {
      document.getElementById('invoiceFileInput').files = files;
      handleInvoiceUpload(document.getElementById('invoiceFileInput'));
    }
  });

  // 初始化明细空行提示状态
  var rows = document.querySelectorAll('#expItemTableBody .expense-item-row');
  var empty = document.getElementById('expItemEmpty');
  if (empty && rows.length > 0) empty.style.display = 'none';
});

/* ================================================================
   审批流模块
   ================================================================ */

var approvalPendingItem = null; // 当前正在审批的单据

/** 审批页初始化 */
function loadApprovalPage() {
  loadApprovalList();
  document.getElementById('approvalFlowCard').style.display = 'none';
  document.getElementById('approvalTimelineCard').style.display = 'none';
}

/** 查询审批人角色并加载待审批列表 */
function loadApprovalList() {
  var userId = document.getElementById('approvalUserId').value;
  document.getElementById('approvalTableBody').innerHTML =
    '<tr><td colspan="8" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  // 获取用户角色
  fetch('/api/approval/user-roles?userId=' + userId)
    .then(function(r) { return r.json(); })
    .then(function(res) {
      var roles = (res.code === 200 && res.data) ? res.data : [];
      var roleLabel = roles.length > 0 ? '角色：' + roles.join(', ') : '无审批权限';
      document.getElementById('approvalRoleLabel').textContent = roleLabel;

      // 加载待审批列表
      return fetch('/api/approval/pending-list?userId=' + userId);
    })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      var tbody = document.getElementById('approvalTableBody');
      if (res.code !== 200 || !res.data || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--gray-400);padding:30px;">暂无待审批单据</td></tr>';
        return;
      }
      var html = '';
      res.data.forEach(function(item) {
        var statusTag = getStatusTag(item.status, item.businessType);
        var amount = item.amount != null ? Number(item.amount).toLocaleString() : '0';
        var bizTypeName = item.businessType === 'EXPENSE' ? '报销' : item.businessType === 'LEAVE' ? '请假' : '加班';
        var typeLabel = item.expenseType || bizTypeName;
        html += '<tr>';
        html += '<td>' + (item.reportNo || '') + '</td>';
        html += '<td><span class="tag t-blue">' + bizTypeName + '</span></td>';
        html += '<td>' + (item.empName || '') + '</td>';
        html += '<td>' + (item.deptName || '') + '</td>';
        html += '<td class="txt-right">¥' + amount + '</td>';
        html += '<td style="max-width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">' + (item.description || '') + '</td>';
        html += '<td>' + statusTag + '</td>';
        html += '<td class="txt-center">';
        html += '<button class="btn btn-primary btn-xs" onclick="openApprovalModal(\'' + item.businessType + '\',' + item.id + ',\'' + (item.reportNo || '') + '\',1)">通过</button> ';
        html += '<button class="btn btn-danger btn-xs" onclick="openApprovalModal(\'' + item.businessType + '\',' + item.id + ',\'' + (item.reportNo || '') + '\',2)">驳回</button>';
        html += '</td></tr>';
      });
      tbody.innerHTML = html;
    })
    .catch(function(e) {
      document.getElementById('approvalTableBody').innerHTML =
        '<tr><td colspan="8" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + e.message + '</td></tr>';
    });
}

/** 获取状态标签HTML */
function getStatusTag(status, bizType) {
  if (status === 'PENDING') return '<span class="tag t-yellow">待审批</span>';
  if (status === 'MANAGER_APPROVED') return '<span class="tag t-blue">待财务审批</span>';
  if (status === 'FINANCE_APPROVED') return '<span class="tag t-blue">待打款</span>';
  if (status === 'COMPLETED') return '<span class="tag t-green">已完成</span>';
  if (status === 'REJECTED') return '<span class="tag t-red">已驳回</span>';
  return '<span class="tag">' + status + '</span>';
}

/** 打开审批弹窗 */
function openApprovalModal(bizType, bizId, reportNo, result) {
  approvalPendingItem = { businessType: bizType, businessId: bizId, reportNo: reportNo, result: result };
  document.getElementById('approvalOpinion').value = '';
  var title = result === 1 ? '审批通过 - ' + reportNo : '驳回 - ' + reportNo;
  document.getElementById('approvalModalTitle').textContent = title;
  document.getElementById('approvalApproveBtn').style.display = result === 1 ? '' : 'none';
  document.getElementById('approvalRejectBtn').style.display = result === 2 ? '' : 'none';
  document.getElementById('approvalModal').style.display = 'flex';
}

/** 关闭审批弹窗 */
function closeApprovalModal() {
  document.getElementById('approvalModal').style.display = 'none';
  approvalPendingItem = null;
}

/** 执行审批 */
function executeApproval(result) {
  if (!approvalPendingItem) return;
  var opinion = document.getElementById('approvalOpinion').value;
  var userId = document.getElementById('approvalUserId').value;

  var body = {
    businessType: approvalPendingItem.businessType,
    businessId: approvalPendingItem.businessId,
    approverId: parseInt(userId),
    result: result,
    opinion: opinion
  };

  fetch('/api/approval/execute', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      closeApprovalModal();
      if (res.code === 200) {
        alert(res.data.message || '操作成功');
        loadApprovalList();
        // 自动加载时间轴
        loadApprovalTimeline(approvalPendingItem.businessType, approvalPendingItem.businessId, approvalPendingItem.reportNo);
        loadApprovalFlow(approvalPendingItem.businessType, approvalPendingItem.businessId, approvalPendingItem.reportNo);
      } else {
        alert('操作失败：' + res.msg);
      }
    })
    .catch(function(e) { alert('操作失败：' + e.message); });
}

/** 加载审批时间轴 */
function loadApprovalTimeline(bizType, bizId, reportNo) {
  fetch('/api/approval/timeline?businessType=' + bizType + '&businessId=' + bizId)
    .then(function(r) { return r.json(); })
    .then(function(res) {
      var card = document.getElementById('approvalTimelineCard');
      var tl = document.getElementById('approvalTimeline');
      document.getElementById('approvalTimelineTitle').textContent = (reportNo || '') + ' 审批记录';

      if (res.code !== 200 || !res.data || res.data.length === 0) {
        tl.innerHTML = '<div style="color:var(--gray-400);">暂无审批记录</div>';
        card.style.display = '';
        return;
      }

      var html = '';
      res.data.forEach(function(record) {
        var action = record.approvalResult === 1 ? '审批通过' : '驳回';
        var icon = record.approvalResult === 1 ? '✅' : '❌';
        html += '<div class="tl-item done">';
        html += '<div class="tl-time">' + (record.approvalTime || record.createTime || '') + '</div>';
        html += '<div class="tl-text">' + icon + ' ' + (record.approverName || '审批人') + ' ' + action;
        if (record.approvalOpinion) html += '："' + record.approvalOpinion + '"';
        html += '</div></div>';
      });
      tl.innerHTML = html;
      card.style.display = '';
    });
}

/** 加载审批进度条 */
function loadApprovalFlow(bizType, bizId, reportNo) {
  // 查询单据状态
  var urlMap = {
    'EXPENSE': '/api/expense-report/' + bizId,
    'LEAVE': '/api/leave/' + bizId,
    'OVERTIME': '/api/overtime/' + bizId
  };
  // 对于LEAVE和OVERTIME，通过审批列表查询状态
  // 简化处理：根据bizType构建flow
  var card = document.getElementById('approvalFlowCard');
  document.getElementById('approvalFlowTitle').textContent = (reportNo || '') + ' 审批进度';

  var status = 'PENDING';
  // 获取当前状态
  if (bizType === 'EXPENSE') {
    fetch('/api/expense-report/' + bizId)
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (res.code === 200 && res.data && res.data.report) {
          renderFlowLine(bizType, res.data.report.status);
          document.getElementById('approvalFlowTag').textContent = getStatusText(res.data.report.status);
        }
      });
  } else {
    renderFlowLine(bizType, 'PENDING');
  }
  card.style.display = '';
}

function renderFlowLine(bizType, status) {
  var nodes;
  if (bizType === 'EXPENSE') {
    nodes = [
      { label: '提交申请', state: 'done' },
      { label: '经理审批', state: status === 'PENDING' ? 'cur' : (status === 'REJECTED' && getApprovalStep(status, '经理审批') === 'reject' ? 'reject' : 'done') },
      { label: '财务审批', state: getFlowState(status, 'MANAGER_APPROVED', 'FINANCE_APPROVED') },
      { label: '完成', state: status === 'COMPLETED' ? 'done' : '' }
    ];
  } else {
    nodes = [
      { label: '提交申请', state: 'done' },
      { label: '经理审批', state: getFlowState(status, 'PENDING', 'COMPLETED') },
      { label: '完成', state: status === 'COMPLETED' ? 'done' : '' }
    ];
  }

  var html = '';
  nodes.forEach(function(node, i) {
    if (i > 0) html += '<span class="flow-arrow">→</span>';
    html += '<span class="flow-node ' + node.state + '">' + node.label + '</span>';
  });
  document.getElementById('approvalFlowLine').innerHTML = html;
}

function getFlowState(status, pendingStatus, doneStatus) {
  if (status === 'REJECTED') return 'reject';
  if (status === doneStatus || status === 'COMPLETED') return 'done';
  if (status === pendingStatus || status === 'PENDING') return '';
  return 'done';
}

function getApprovalStep(status, step) {
  return status === 'REJECTED' ? 'reject' : '';
}

function getStatusText(status) {
  var map = {
    'PENDING': '⏳ 待审批', 'MANAGER_APPROVED': '⏳ 待财务审批',
    'FINANCE_APPROVED': '⏳ 待打款', 'COMPLETED': '✅ 已完成', 'REJECTED': '❌ 已驳回'
  };
  return map[status] || status;
}

function refreshUnreadBadge() {
  fetch('/api/notifications/unread-count')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        var count = res.data;
        var badge = document.getElementById('navBadge');
        if (badge) {
          if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'inline-flex';
          } else {
            badge.style.display = 'none';
          }
        }
      }
    })
    .catch(function() {});
}

/** 页面加载和定时刷新Badge */
document.addEventListener('DOMContentLoaded', function() {
  refreshUnreadBadge();
  setInterval(refreshUnreadBadge, 30000); // 每30秒刷新一次未读数
});

/* ---------- 通知中心 ---------- */
var notifyPage = 1, notifyTotalPages = 1;

function loadNotifications(action) {
  if (action === 'prev') notifyPage = Math.max(1, notifyPage - 1);
  else if (action === 'next') notifyPage = Math.min(notifyTotalPages, notifyPage + 1);
  else if (action === 'last') notifyPage = notifyTotalPages;
  else if (typeof action === 'number') notifyPage = action;

  var typeFilter = document.getElementById('notifyTypeFilter').value;
  var statusFilter = document.getElementById('notifyStatusFilter').value;

  var url = '/api/notifications/my?page=' + notifyPage + '&size=10';
  if (typeFilter) url += '&businessType=' + typeFilter;
  if (statusFilter) url += '&status=' + statusFilter;

  var container = document.getElementById('notificationList');
  container.innerHTML = '<div class="cal-loading">加载中...</div>';

  fetch(url)
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        renderNotificationList(res.data);
        notifyTotalPages = Math.ceil(res.data.total / res.data.size) || 1;
        notifyPage = res.data.page;
        document.getElementById('notifyPageInfo').textContent = '第' + notifyPage + '页/共' + notifyTotalPages + '页';
        document.getElementById('notifyPrevBtn').disabled = notifyPage <= 1;
        document.getElementById('notifyNextBtn').disabled = notifyPage >= notifyTotalPages;
        document.getElementById('notifyUnreadBadge').textContent = (res.data.total || 0) + '条消息';
      } else {
        container.innerHTML = '<div class="cal-error">加载失败：' + res.msg + '</div>';
      }
    })
    .catch(function(e) {
      container.innerHTML = '<div class="cal-error">网络请求失败：' + e.message + '</div>';
    });
}

function renderNotificationList(data) {
  var rows = data.rows, container = document.getElementById('notificationList');
  if (!rows || rows.length === 0) {
    container.innerHTML = '<div style="text-align:center;padding:40px;color:var(--gray-400);">暂无消息通知</div>';
    return;
  }
  var html = '';
  for (var i = 0; i < rows.length; i++) {
    var n = rows[i];
    var isUnread = n.status === 'UNREAD';
    var typeTag = getBusinessTypeTag(n.businessType);
    var timeAgo = getTimeAgo(n.createTime);
    html += '<div class="notice-item' + (isUnread ? ' unread' : '') + '" id="notifyItem' + n.id + '">'
      + '<div>'
      + '<div class="notice-title">' + (isUnread ? '<span class="notice-dot"></span>' : '') + typeTag + ' ' + escapeHtml(n.title) + '</div>'
      + '<div class="notice-meta">' + escapeHtml(n.content || '') + '</div>'
      + '</div>'
      + '<div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px;">'
      + '<div class="notice-time">' + timeAgo + '</div>'
      + (isUnread ? '<button class="btn btn-xs" onclick="markNotifyRead(' + n.id + ')">标记已读</button>' : '<span style="font-size:11px;color:var(--gray-400);">已读</span>')
      + '</div>'
      + '</div>';
  }
  container.innerHTML = html;
}

function markNotifyRead(id) {
  fetch('/api/notifications/' + id + '/read', { method: 'POST' })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        loadNotifications();
        refreshUnreadBadge();
      }
    });
}

function markAllNotificationsRead() {
  if (!confirm('确认将所有通知标记为已读？')) return;
  fetch('/api/notifications/read-all', { method: 'POST' })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        loadNotifications();
        refreshUnreadBadge();
      }
    });
}

function getBusinessTypeTag(type) {
  var map = {
    'ANNOUNCEMENT': '<span class="tag t-blue">公告</span>',
    'MEETING': '<span class="tag t-green">会议</span>',
    'APPROVAL': '<span class="tag t-yellow">审批</span>',
    'LEAVE': '<span class="tag t-blue">请假</span>',
    'OVERTIME': '<span class="tag t-green">加班</span>',
    'REIMBURSEMENT': '<span class="tag t-yellow">报销</span>'
  };
  return map[type] || '<span class="tag t-gray">' + (type || '未知') + '</span>';
}

function getTimeAgo(dateStr) {
  if (!dateStr) return '-';
  var date = new Date(dateStr.replace(' ', 'T'));
  var now = new Date();
  var diffMs = now - date;
  if (isNaN(diffMs)) return dateStr;
  var diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return diffMin + '分钟前';
  var diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return diffHour + '小时前';
  var diffDay = Math.floor(diffHour / 24);
  if (diffDay < 30) return diffDay + '天前';
  return dateStr.substring(0, 10);
}

/* ---------- 公告列表（面板9） ---------- */
var annPage = 1, annTotalPages = 1;

function loadAnnouncementList(action) {
  if (action === 'prev') annPage = Math.max(1, annPage - 1);
  else if (action === 'next') annPage = Math.min(annTotalPages, annPage + 1);
  else if (action === 'last') annPage = annTotalPages;
  else if (typeof action === 'number') annPage = action;

  var tbody = document.getElementById('announceTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  fetch('/api/notices/published?page=' + annPage + '&size=10&type=2')
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        renderAnnouncementList(res.data);
      } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + res.msg + '</td></tr>';
      }
    })
    .catch(function(e) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:30px;">网络请求失败：' + e.message + '</td></tr>';
    });
}

function renderAnnouncementList(data) {
  var rows = data.rows, tbody = document.getElementById('announceTableBody');
  annTotalPages = Math.ceil(data.total / data.size) || 1;
  annPage = data.page;
  document.getElementById('annPageInfo').textContent = '第' + annPage + '页/共' + annTotalPages + '页';
  document.getElementById('annPrevBtn').disabled = annPage <= 1;
  document.getElementById('annNextBtn').disabled = annPage >= annTotalPages;
  document.getElementById('announceCount').textContent = data.total + '条公告';

  if (!rows || rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--gray-400);padding:30px;">暂无已发布公告</td></tr>';
    return;
  }

  // 收集公告ID列表，查询已读状态
  var noticeIds = [];
  for (var i = 0; i < rows.length; i++) noticeIds.push(rows[i].id);

  // 先渲染基础数据，再异步更新已读状态
  var html = '';
  for (var i2 = 0; i2 < rows.length; i2++) {
    var ann = rows[i2];
    html += '<tr>'
      + '<td><strong>' + escapeHtml(ann.title) + '</strong></td>'
      + '<td><span class="tag t-blue">公告</span></td>'
      + '<td>' + formatDateTime(ann.createTime) + '</td>'
      + '<td id="annReadStatus' + ann.id + '"><span class="tag t-yellow">查询中...</span></td>'
      + '<td class="txt-center">'
      + '<button class="btn btn-xs" onclick="viewAnnounceDetail(' + ann.id + ')">查看</button> '
      + '<button class="btn btn-primary btn-xs" onclick="markAnnounceRead(' + ann.id + ')">标记已读</button>'
      + '</td>'
      + '</tr>';
  }
  tbody.innerHTML = html;

  // 查询已读状态
  fetch('/api/notices/read-status', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ noticeIds: noticeIds })
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        var readIds = res.data || [];
        for (var j = 0; j < rows.length; j++) {
          var cell = document.getElementById('annReadStatus' + rows[j].id);
          if (cell) {
            if (readIds.indexOf(rows[j].id) >= 0) {
              cell.innerHTML = '<span class="tag t-green">已读</span>';
            } else {
              cell.innerHTML = '<span class="tag t-red">未读</span>';
            }
          }
        }
      }
    });
}

function markAnnounceRead(noticeId) {
  fetch('/api/notices/' + noticeId + '/read', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({})
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        var cell = document.getElementById('annReadStatus' + noticeId);
        if (cell) cell.innerHTML = '<span class="tag t-green">已读</span>';
        refreshUnreadBadge();
      }
    });
}

function viewAnnounceDetail(noticeId) {
  window.location.href = 'pages/announcement-detail.html?noticeId=' + noticeId;
}

/* ---------- 公告管理（面板10） ---------- */
var annMPage = 1, annMTotalPages = 1;
var wangEditor = null;

function loadAnnounceManageList(action) {
  if (action === 'prev') annMPage = Math.max(1, annMPage - 1);
  else if (action === 'next') annMPage = Math.min(annMTotalPages, annMPage + 1);
  else if (action === 'last') annMPage = annMTotalPages;
  else if (typeof action === 'number') annMPage = action;

  var typeFilter = document.getElementById('annManageTypeFilter').value;
  var statusFilter = document.getElementById('annManageStatusFilter').value;
  var url = '/api/notices?page=' + annMPage + '&size=10';
  if (typeFilter) url += '&type=' + typeFilter;
  if (statusFilter) url += '&status=' + statusFilter;

  var tbody = document.getElementById('annManageTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--gray-400);padding:30px;">加载中...</td></tr>';

  fetch(url)
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        renderAnnounceManageList(res.data);
      } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:30px;">加载失败：' + res.msg + '</td></tr>';
      }
    })
    .catch(function(e) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--danger);padding:30px;">网络请求失败：' + e.message + '</td></tr>';
    });
}

function renderAnnounceManageList(data) {
  var rows = data.rows, tbody = document.getElementById('annManageTableBody');
  annMTotalPages = Math.ceil(data.total / data.size) || 1;
  annMPage = data.page;
  document.getElementById('annMPageInfo').textContent = '第' + annMPage + '页/共' + annMTotalPages + '页';
  document.getElementById('annMPrevBtn').disabled = annMPage <= 1;
  document.getElementById('annMNextBtn').disabled = annMPage >= annMTotalPages;

  if (!rows || rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--gray-400);padding:30px;">暂无公告</td></tr>';
    return;
  }
  var html = '';
  for (var i = 0; i < rows.length; i++) {
    var ann = rows[i];
    var statusTag = getNoticeStatusTag(ann.status);
    var typeTag = ann.type === 1 ? '<span class="tag t-blue">通知</span>' : '<span class="tag t-blue">公告</span>';
    var actions = '';
    if (ann.status === 0) {
      actions = '<button class="btn btn-xs" onclick="editAnnounce(' + ann.id + ')">编辑</button> '
        + '<button class="btn btn-primary btn-xs" onclick="publishAnnounce(' + ann.id + ')">发布</button>';
    } else if (ann.status === 1) {
      actions = '<button class="btn btn-xs" onclick="viewAnnounceDetail(' + ann.id + ')">查看</button> '
        + '<button class="btn btn-danger btn-xs" onclick="withdrawAnnounce(' + ann.id + ')">撤回</button>';
    } else {
      actions = '<button class="btn btn-xs" onclick="viewAnnounceDetail(' + ann.id + ')">查看</button>';
    }
    html += '<tr>'
      + '<td><strong>' + escapeHtml(ann.title) + '</strong></td>'
      + '<td>' + typeTag + '</td>'
      + '<td>' + statusTag + '</td>'
      + '<td>' + formatDateTime(ann.createTime) + '</td>'
      + '<td class="txt-center">' + actions + '</td>'
      + '</tr>';
  }
  tbody.innerHTML = html;
}

function getNoticeStatusTag(status) {
  if (status === 0) return '<span class="tag t-yellow">草稿</span>';
  if (status === 1) return '<span class="tag t-green">已发布</span>';
  if (status === 2) return '<span class="tag t-gray">已撤回</span>';
  return '<span class="tag t-gray">未知</span>';
}

/* ---------- 公告表单（WangEditor） ---------- */
var editingNoticeId = null;

function showAnnounceCreate() {
  editingNoticeId = null;
  document.getElementById('announceFormTitle').textContent = '新建公告';
  document.getElementById('announceTitle').value = '';
  document.getElementById('announceType').value = '2';
  document.getElementById('announceSubmitBtn').textContent = '保存草稿';
  document.getElementById('announceFormCard').style.display = '';
  initWangEditor('');
}

function editAnnounce(id) {
  fetch('/api/notices/' + id)
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        var ann = res.data;
        if (ann.status !== 0) { alert('仅草稿状态可编辑'); return; }
        editingNoticeId = ann.id;
        document.getElementById('announceFormTitle').textContent = '编辑公告';
        document.getElementById('announceTitle').value = ann.title || '';
        document.getElementById('announceType').value = ann.type || 2;
        document.getElementById('announceSubmitBtn').textContent = '保存修改';
        document.getElementById('announceFormCard').style.display = '';
        initWangEditor(ann.content || '');
      } else {
        alert(res.msg);
      }
    })
    .catch(function(e) { alert('加载公告失败：' + e.message); });
}

function hideAnnounceForm() {
  document.getElementById('announceFormCard').style.display = 'none';
  destroyWangEditor();
}

function initWangEditor(content) {
  destroyWangEditor();
  var container = document.getElementById('wangEditorContainer');
  container.innerHTML = '';
  try {
    if (typeof window.wangEditor !== 'undefined') {
      wangEditor = window.wangEditor.createEditor({
        selector: container,
        html: content || '<p></p>',
        config: { placeholder: '请输入公告内容...' }
      });
    } else {
      // 降级为textarea
      container.innerHTML = '<textarea id="announceContentFallback" class="form-input" rows="12" placeholder="请输入公告内容（富文本编辑器加载中...）" style="width:100%;"></textarea>';
      if (content) document.getElementById('announceContentFallback').value = content;
    }
  } catch (e) {
    console.warn('WangEditor初始化失败，使用降级方案：', e);
    container.innerHTML = '<textarea id="announceContentFallback" class="form-input" rows="12" placeholder="请输入公告内容..." style="width:100%;"></textarea>';
    if (content) document.getElementById('announceContentFallback').value = content;
  }
}

function destroyWangEditor() {
  if (wangEditor) {
    try { wangEditor.destroy(); } catch (e) {}
    wangEditor = null;
  }
}

function getEditorContent() {
  if (wangEditor) {
    return wangEditor.getHtml();
  }
  var fallback = document.getElementById('announceContentFallback');
  return fallback ? fallback.value : '';
}

function submitAnnounceForm() {
  var msgEl = document.getElementById('announceFormMsgEl');
  var title = document.getElementById('announceTitle').value.trim();
  var type = parseInt(document.getElementById('announceType').value);
  var content = getEditorContent();

  if (!title) { showFormMsgEl(msgEl, '请输入公告标题', 'error'); return; }
  if (!content || content === '<p></p>' || content === '<p><br></p>') {
    showFormMsgEl(msgEl, '请输入公告内容', 'error'); return;
  }

  var payload = { title: title, type: type, content: content };
  var url = '/api/notices';
  var method = 'POST';

  if (editingNoticeId) {
    url = '/api/notices/' + editingNoticeId;
    method = 'PUT';
  }

  fetch(url, {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        showFormMsgEl(msgEl, editingNoticeId ? '保存成功' : '创建成功（草稿状态）', 'success');
        if (!editingNoticeId) {
          editingNoticeId = res.data.id;
        }
        loadAnnounceManageList();
      } else {
        showFormMsgEl(msgEl, res.msg, 'error');
      }
    })
    .catch(function(e) {
      showFormMsgEl(msgEl, '网络请求失败：' + e.message, 'error');
    });
}

function publishAnnounce(id) {
  if (!confirm('确认发布此公告？发布后将通知所有用户。')) return;
  fetch('/api/notices/' + id + '/publish', { method: 'POST' })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        alert('发布成功');
        loadAnnounceManageList();
        refreshUnreadBadge();
      } else {
        alert(res.msg);
      }
    })
    .catch(function(e) { alert('发布失败：' + e.message); });
}

function withdrawAnnounce(id) {
  if (!confirm('确认撤回此公告？')) return;
  fetch('/api/notices/' + id + '/withdraw', { method: 'POST' })
    .then(function(r) { return r.json(); })
    .then(function(res) {
      if (res.code === 200) {
        alert('撤回成功');
        loadAnnounceManageList();
      } else {
        alert(res.msg);
      }
    })
    .catch(function(e) { alert('撤回失败：' + e.message); });
}

function showFormMsgEl(el, msg, type) {
  el.style.display = 'block';
  el.className = 'form-msg form-msg-' + type;
  el.textContent = msg;
}

