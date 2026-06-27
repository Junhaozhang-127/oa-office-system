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
  var titles = ['工作台', '考勤日历', '数据看板', '审批流', '通知中心', '会议预约'];
  document.getElementById('crumbText').textContent = titles[n];
  if (n === 0) loadEmployeeList();
  if (n === 1) { loadCalendar(); loadCalendarStats(); }
  if (n === 5) { loadMeetingRoomList(); loadMyReservations(); loadScheduleSelects(); }
}

/* ---------- 考勤日历模块（日历网格与月度统计独立） ---------- */
function loadCalendar() {
  var empId = document.getElementById('empSelect').value;
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
        // 同步到会议预约模块的员工下拉框
        var bookingSelect = document.getElementById('bookingEmpSelect');
        var myMeetingSelect = document.getElementById('myMeetingEmpSelect');
        if (bookingSelect) bookingSelect.innerHTML = html;
        if (myMeetingSelect) myMeetingSelect.innerHTML = html;
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

