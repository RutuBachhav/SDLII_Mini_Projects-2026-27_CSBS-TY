/**
 * CampusCare - Main Application Controller
 */

const App = {
    currentUser: null,
    currentTab: 'dashboard',
    allDepartments: [],
    allStaffMembers: [],
    cachedComplaints: [],

    init() {
        this.currentUser = Api.getUser();
        this.renderNavbar();
        if (this.currentUser && Api.getToken()) {
            this.showPortal();
            this.loadNotifications();
            // Poll notifications every 30 seconds
            setInterval(() => {
                if (this.currentUser) this.loadNotifications();
            }, 30000);
        } else {
            this.showLogin();
        }
        this.loadDepartments();
    },

    // ================= Toast Alerts =================
    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${message}</span>`;
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    // ================= Common Data =================
    async loadDepartments() {
        try {
            const res = await Api.getDepartments();
            if (res && res.data) {
                this.allDepartments = res.data;
            }
        } catch (e) {
            console.error('Failed to load departments', e);
        }
    },

    async loadStaffMembers() {
        try {
            const res = await Api.getStaff();
            if (res && res.data) {
                this.allStaffMembers = res.data;
            }
        } catch (e) {
            console.error('Failed to load staff list', e);
        }
    },

    // ================= Navbar & Notifications =================
    renderNavbar() {
        const userBadge = document.getElementById('user-badge-container');
        const notifContainer = document.getElementById('notif-container');

        if (this.currentUser) {
            userBadge.innerHTML = `
                <div class="user-badge">
                    <span>${this.currentUser.name}</span>
                    <span class="role-tag">${this.currentUser.role}</span>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="App.logout()">Logout</button>
            `;
            notifContainer.style.display = 'block';
        } else {
            userBadge.innerHTML = '';
            notifContainer.style.display = 'none';
        }
    },

    async loadNotifications() {
        try {
            const res = await Api.getNotifications();
            const list = res.data || [];
            const unreadCount = list.filter(n => !n.read).length;
            const badge = document.getElementById('notif-badge');
            if (unreadCount > 0) {
                badge.innerText = unreadCount;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }

            const listEl = document.getElementById('notif-list');
            if (list.length === 0) {
                listEl.innerHTML = '<li class="notif-item" style="text-align:center;color:#64748b;">No notifications</li>';
            } else {
                listEl.innerHTML = list.map(n => `
                    <li class="notif-item ${n.read ? '' : 'unread'}" onclick="App.markNotifRead(${n.id})">
                        <div>${n.message}</div>
                        <div class="notif-item-time">${new Date(n.createdAt).toLocaleString()}</div>
                    </li>
                `).join('');
            }
        } catch (e) {
            console.error('Failed to fetch notifications', e);
        }
    },

    toggleNotifDropdown() {
        const dd = document.getElementById('notif-dropdown');
        dd.style.display = dd.style.display === 'block' ? 'none' : 'block';
    },

    async markNotifRead(id) {
        try {
            await Api.markNotificationRead(id);
            this.loadNotifications();
        } catch (e) {
            console.error(e);
        }
    },

    async markAllNotifsRead() {
        try {
            await Api.markAllNotificationsRead();
            this.loadNotifications();
            this.showToast('All notifications marked as read', 'success');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // ================= Authentication =================
    showLogin() {
        document.getElementById('portal-view').style.display = 'none';
        document.getElementById('auth-view').style.display = 'block';
        document.getElementById('login-box').style.display = 'block';
        document.getElementById('register-box').style.display = 'none';
    },

    showRegister() {
        document.getElementById('portal-view').style.display = 'none';
        document.getElementById('auth-view').style.display = 'block';
        document.getElementById('login-box').style.display = 'none';
        document.getElementById('register-box').style.display = 'block';
        this.populateRegisterDepartments();
    },

    populateRegisterDepartments() {
        const sel = document.getElementById('reg-department');
        if (!sel) return;
        sel.innerHTML = '<option value="">Select Department</option>' +
            this.allDepartments.map(d => `<option value="${d.id}">${d.departmentName}</option>`).join('');
    },

    onRoleChange() {
        const role = document.getElementById('reg-role').value;
        const studentFields = document.getElementById('student-extra-fields');
        const staffFields = document.getElementById('staff-extra-fields');

        if (role === 'STUDENT') {
            studentFields.style.display = 'block';
            staffFields.style.display = 'none';
        } else if (role === 'STAFF') {
            studentFields.style.display = 'none';
            staffFields.style.display = 'block';
        } else {
            studentFields.style.display = 'none';
            staffFields.style.display = 'none';
        }
    },

    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const pwd = document.getElementById('login-password').value;

        try {
            const res = await Api.login(email, pwd);
            Api.setToken(res.data.token);
            Api.setUser(res.data);
            this.currentUser = res.data;
            this.renderNavbar();
            this.showToast('Welcome back, ' + res.data.name + '!', 'success');
            this.showPortal();
            this.loadNotifications();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const role = document.getElementById('reg-role').value;
        const data = {
            name: document.getElementById('reg-name').value,
            email: document.getElementById('reg-email').value,
            password: document.getElementById('reg-password').value,
            phone: document.getElementById('reg-phone').value,
            role: role
        };

        if (role === 'STUDENT') {
            data.rollNumber = document.getElementById('reg-roll').value;
            data.branch = document.getElementById('reg-branch').value;
            data.year = document.getElementById('reg-year').value;
            data.division = document.getElementById('reg-division').value;
        } else if (role === 'STAFF') {
            const deptId = document.getElementById('reg-department').value;
            if (deptId) data.departmentId = parseInt(deptId);
            data.designation = document.getElementById('reg-designation').value;
        }

        try {
            const res = await Api.register(data);
            Api.setToken(res.data.token);
            Api.setUser(res.data);
            this.currentUser = res.data;
            this.renderNavbar();
            this.showToast('Account registered successfully!', 'success');
            this.showPortal();
            this.loadNotifications();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    demoLogin(role) {
        if (role === 'student') {
            document.getElementById('login-email').value = 'student@campuscare.com';
            document.getElementById('login-password').value = 'student123';
        } else if (role === 'staff') {
            document.getElementById('login-email').value = 'staff@campuscare.com';
            document.getElementById('login-password').value = 'staff123';
        } else if (role === 'admin') {
            document.getElementById('login-email').value = 'admin@campuscare.com';
            document.getElementById('login-password').value = 'admin123';
        }
        document.getElementById('login-form').dispatchEvent(new Event('submit'));
    },

    logout() {
        Api.logout();
        this.currentUser = null;
        this.renderNavbar();
        this.showLogin();
        this.showToast('Logged out successfully', 'info');
    },

    // ================= Portal Navigation =================
    showPortal() {
        document.getElementById('auth-view').style.display = 'none';
        document.getElementById('portal-view').style.display = 'flex';

        this.renderSidebar();
        this.navigateTo('dashboard');
    },

    renderSidebar() {
        const sidebar = document.getElementById('app-sidebar');
        const role = this.currentUser.role;
        const initials = (this.currentUser.name || 'CC').split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();

        let links = [
            { id: 'dashboard', label: 'Dashboard', role: 'ALL' }
        ];

        if (role === 'STUDENT') {
            links.push(
                { id: 'submit-complaint', label: 'Submit Complaint', role: 'STUDENT' },
                { id: 'my-complaints', label: 'My Complaints', role: 'STUDENT' },
                { id: 'profile', label: 'My Profile', role: 'STUDENT' }
            );
        } else if (role === 'STAFF') {
            links.push(
                { id: 'assigned-complaints', label: 'Assigned Complaints', role: 'STAFF' },
                { id: 'profile', label: 'My Profile', role: 'STAFF' }
            );
        } else if (role === 'ADMIN') {
            links.push(
                { id: 'all-complaints', label: 'All Complaints', role: 'ADMIN' },
                { id: 'manage-departments', label: 'Departments', role: 'ADMIN' },
                { id: 'manage-staff', label: 'Staff Members', role: 'ADMIN' },
                { id: 'manage-students', label: 'Students', role: 'ADMIN' },
                { id: 'profile', label: 'Admin Profile', role: 'ADMIN' }
            );
        }

        sidebar.innerHTML = `
            <div class="sidebar-user-card">
                <div class="sidebar-user-avatar">${initials}</div>
                <div class="sidebar-user-info">
                    <span class="sidebar-user-name" title="${this.currentUser.name}">${this.currentUser.name}</span>
                    <span class="sidebar-user-role">${this.currentUser.role}</span>
                </div>
            </div>
            <div class="sidebar-section-title">Navigation Menu</div>
            ${links.map(l => `
                <a class="nav-link ${this.currentTab === l.id ? 'active' : ''}" onclick="App.navigateTo('${l.id}')">
                    ${l.label}
                </a>
            `).join('')}
        `;
    },

    navigateTo(tab) {
        this.currentTab = tab;
        this.renderSidebar();

        const content = document.getElementById('main-view-content');
        content.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon"></div>
                <div class="empty-state-title">Loading View...</div>
                <div class="empty-state-desc">Fetching latest campus data from the server.</div>
            </div>
        `;

        if (tab === 'dashboard') {
            this.loadDashboardView();
        } else if (tab === 'submit-complaint') {
            this.loadSubmitComplaintView();
        } else if (tab === 'my-complaints') {
            this.loadComplaintsView('student');
        } else if (tab === 'assigned-complaints') {
            this.loadComplaintsView('staff');
        } else if (tab === 'all-complaints') {
            this.loadComplaintsView('admin');
        } else if (tab === 'manage-departments') {
            this.loadDepartmentsView();
        } else if (tab === 'manage-staff') {
            this.loadStaffView();
        } else if (tab === 'manage-students') {
            this.loadStudentsView();
        } else if (tab === 'profile') {
            this.loadProfileView();
        }
    },

    // ================= Dashboard Views =================
    async loadDashboardView() {
        const content = document.getElementById('main-view-content');
        const role = this.currentUser.role;

        try {
            let stats = null;
            if (role === 'ADMIN') {
                const res = await Api.getAdminDashboard();
                stats = res.data;
            } else if (role === 'STAFF') {
                const res = await Api.getStaffDashboard();
                stats = res.data;
            } else {
                const res = await Api.getStudentDashboard();
                stats = res.data;
            }

            let html = `
                <div class="page-header">
                    <div>
                        <h1 class="page-title">${role === 'ADMIN' ? 'Administrator Portal' : role === 'STAFF' ? 'Staff Workstation' : 'Student Dashboard'}</h1>
                        <p class="page-subtitle">Welcome back, <strong>${this.currentUser.name}</strong> • Here is what's happening on campus today.</p>
                    </div>
                    ${role === 'STUDENT' ? `
                        <button class="btn btn-primary" onclick="App.navigateTo('submit-complaint')">Lodge New Complaint</button>
                    ` : ''}
                </div>
            `;

            if (role === 'ADMIN') {
                html += `
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Total Complaints</span>
                                <div class="stat-icon-wrapper stat-icon-blue"></div>
                            </div>
                            <span class="stat-value" style="color:var(--primary);">${stats.totalComplaints}</span>
                            <span class="stat-subtext">All submitted tickets</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Pending Review</span>
                                <div class="stat-icon-wrapper stat-icon-amber"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-pending);">${stats.pendingComplaints}</span>
                            <span class="stat-subtext">Awaiting staff assignment</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Assigned</span>
                                <div class="stat-icon-wrapper stat-icon-purple"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-assigned);">${stats.assignedComplaints}</span>
                            <span class="stat-subtext">Staff assigned</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">In Progress</span>
                                <div class="stat-icon-wrapper stat-icon-cyan"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-inprogress);">${stats.inProgressComplaints}</span>
                            <span class="stat-subtext">Work ongoing</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Resolved</span>
                                <div class="stat-icon-wrapper stat-icon-green"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-resolved);">${stats.resolvedComplaints}</span>
                            <span class="stat-subtext">Work completed</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Closed</span>
                                <div class="stat-icon-wrapper stat-icon-slate"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-closed);">${stats.closedComplaints}</span>
                            <span class="stat-subtext">Verified & closed</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Registered Students</span>
                                <div class="stat-icon-wrapper stat-icon-blue"></div>
                            </div>
                            <span class="stat-value">${stats.totalStudents}</span>
                            <span class="stat-subtext">Active student accounts</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Campus Staff</span>
                                <div class="stat-icon-wrapper stat-icon-purple"></div>
                            </div>
                            <span class="stat-value">${stats.totalStaff}</span>
                            <span class="stat-subtext">Assigned faculty/staff</span>
                        </div>
                    </div>

                    <div class="card" style="margin-bottom:2rem;">
                        <div class="card-header">
                            <h3 class="card-title">Admin Quick Navigation</h3>
                        </div>
                        <div class="quick-actions-grid">
                            <div class="action-card" onclick="App.navigateTo('all-complaints')">
                                <div class="action-card-icon stat-icon-blue"></div>
                                <div>
                                    <div class="action-card-title">Manage Complaints</div>
                                    <div class="action-card-desc">Review, assign, and track all campus tickets</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('manage-departments')">
                                <div class="action-card-icon stat-icon-purple"></div>
                                <div>
                                    <div class="action-card-title">Campus Departments</div>
                                    <div class="action-card-desc">Add & configure college service departments</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('manage-staff')">
                                <div class="action-card-icon stat-icon-green"></div>
                                <div>
                                    <div class="action-card-title">Staff Directory</div>
                                    <div class="action-card-desc">View department personnel and assignment loads</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('manage-students')">
                                <div class="action-card-icon stat-icon-amber"></div>
                                <div>
                                    <div class="action-card-title">Student Records</div>
                                    <div class="action-card-desc">View registered students and complaints lodged</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(350px,1fr));gap:1.5rem;">
                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">Complaints by Category</h3>
                            </div>
                            <div class="table-container">
                                <table>
                                    <thead><tr><th>Category</th><th>Count</th></tr></thead>
                                    <tbody>
                                        ${Object.entries(stats.complaintsByCategory || {}).map(([cat, cnt]) => `
                                            <tr><td><strong>${cat}</strong></td><td><span class="badge badge-assigned">${cnt}</span></td></tr>
                                        `).join('') || '<tr><td colspan="2" style="text-align:center;padding:1.5rem;color:#64748b;">No categorical data available</td></tr>'}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">Complaints by Priority</h3>
                            </div>
                            <div class="table-container">
                                <table>
                                    <thead><tr><th>Priority</th><th>Count</th></tr></thead>
                                    <tbody>
                                        ${Object.entries(stats.complaintsByPriority || {}).map(([prio, cnt]) => `
                                            <tr>
                                                <td><span class="badge badge-prio-${prio.toLowerCase()}">${prio}</span></td>
                                                <td><strong>${cnt}</strong></td>
                                            </tr>
                                        `).join('') || '<tr><td colspan="2" style="text-align:center;padding:1.5rem;color:#64748b;">No priority data available</td></tr>'}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                `;
            } else if (role === 'STAFF') {
                // Fetch recent assigned complaints for staff preview
                let recentAssigned = [];
                try {
                    const cRes = await Api.getComplaints();
                    const allC = cRes.data || [];
                    recentAssigned = allC.filter(c => c.assignedStaffId === this.currentUser.staffId).slice(0, 5);
                } catch (e) { console.error(e); }

                html += `
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Assigned to Me</span>
                                <div class="stat-icon-wrapper stat-icon-blue"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-assigned);">${stats.assignedComplaints}</span>
                            <span class="stat-subtext">Active assignments awaiting action</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">In Progress</span>
                                <div class="stat-icon-wrapper stat-icon-purple"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-inprogress);">${stats.inProgressComplaints}</span>
                            <span class="stat-subtext">Currently under resolution</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Resolved by Me</span>
                                <div class="stat-icon-wrapper stat-icon-green"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-resolved);">${stats.resolvedComplaints}</span>
                            <span class="stat-subtext">Successfully completed tasks</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Lifetime Total</span>
                                <div class="stat-icon-wrapper stat-icon-slate"></div>
                            </div>
                            <span class="stat-value">${stats.totalComplaints}</span>
                            <span class="stat-subtext">Total tickets assigned over time</span>
                        </div>
                    </div>

                    <div class="card" style="margin-bottom:2rem;">
                        <div class="card-header">
                            <h3 class="card-title">Quick Actions</h3>
                        </div>
                        <div class="quick-actions-grid">
                            <div class="action-card" onclick="App.navigateTo('assigned-complaints')">
                                <div class="action-card-icon stat-icon-blue"></div>
                                <div>
                                    <div class="action-card-title">View Assigned Complaints</div>
                                    <div class="action-card-desc">Update resolution progress and post milestone notes</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('profile')">
                                <div class="action-card-icon stat-icon-purple"></div>
                                <div>
                                    <div class="action-card-title">Staff Profile</div>
                                    <div class="action-card-desc">View department affiliation and contact details</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">Recent Assigned Tasks</h3>
                            <button class="btn btn-secondary btn-sm" onclick="App.navigateTo('assigned-complaints')">View All</button>
                        </div>
                        <div class="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Title</th>
                                        <th>Category</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th>Date</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${recentAssigned.length > 0 ? recentAssigned.map(c => `
                                        <tr>
                                            <td>#${c.id}</td>
                                            <td><strong>${c.title}</strong></td>
                                            <td>${c.category}</td>
                                            <td><span class="badge badge-prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
                                            <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
                                            <td style="white-space:nowrap;font-size:0.86rem;">${new Date(c.createdAt).toLocaleDateString()} <span style="font-size:0.75rem;color:var(--text-light);display:block;">${new Date(c.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></td>
                                            <td>
                                                <div style="display:flex;gap:0.35rem;">
                                                    <button class="btn btn-secondary btn-sm" onclick="App.viewComplaintDetails(${c.id})">Details</button>
                                                    <button class="btn btn-primary btn-sm" onclick="App.openUpdateModal(${c.id})">Update</button>
                                                </div>
                                            </td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="7" style="text-align:center;padding:2rem;color:#64748b;">No active assignments at this time.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;
            } else {
                // Student Dashboard
                let recentComplaints = [];
                try {
                    const cRes = await Api.getComplaints();
                    const allC = cRes.data || [];
                    recentComplaints = allC.filter(c => c.studentId === this.currentUser.studentId).slice(0, 5);
                } catch (e) { console.error(e); }

                html += `
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Total Lodged</span>
                                <div class="stat-icon-wrapper stat-icon-blue"></div>
                            </div>
                            <span class="stat-value" style="color:var(--primary);">${stats.totalComplaints}</span>
                            <span class="stat-subtext">All complaints submitted by you</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Pending Review</span>
                                <div class="stat-icon-wrapper stat-icon-amber"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-pending);">${stats.pendingComplaints}</span>
                            <span class="stat-subtext">Awaiting admin review</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">In Progress</span>
                                <div class="stat-icon-wrapper stat-icon-purple"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-inprogress);">${stats.inProgressComplaints}</span>
                            <span class="stat-subtext">Staff actively working on resolution</span>
                        </div>
                        <div class="stat-card">
                            <div class="stat-card-top">
                                <span class="stat-label">Resolved</span>
                                <div class="stat-icon-wrapper stat-icon-green"></div>
                            </div>
                            <span class="stat-value" style="color:var(--status-resolved);">${stats.resolvedComplaints}</span>
                            <span class="stat-subtext">Resolved and ready for closure</span>
                        </div>
                    </div>

                    <div class="card" style="margin-bottom:2rem;">
                        <div class="card-header">
                            <h3 class="card-title">Quick Actions</h3>
                        </div>
                        <div class="quick-actions-grid">
                            <div class="action-card" onclick="App.navigateTo('submit-complaint')">
                                <div class="action-card-icon stat-icon-blue"></div>
                                <div>
                                    <div class="action-card-title">Submit New Complaint</div>
                                    <div class="action-card-desc">Lodge issues for Wi-Fi, Labs, Hostel, or Classrooms</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('my-complaints')">
                                <div class="action-card-icon stat-icon-purple"></div>
                                <div>
                                    <div class="action-card-title">Track My Complaints</div>
                                    <div class="action-card-desc">Check live timeline updates & staff comments</div>
                                </div>
                            </div>
                            <div class="action-card" onclick="App.navigateTo('profile')">
                                <div class="action-card-icon stat-icon-green"></div>
                                <div>
                                    <div class="action-card-title">Student Profile</div>
                                    <div class="action-card-desc">View roll number, department, and contact info</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">Recent Complaints</h3>
                            <button class="btn btn-secondary btn-sm" onclick="App.navigateTo('my-complaints')">View All</button>
                        </div>
                        <div class="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Title</th>
                                        <th>Category</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th>Assigned Staff</th>
                                        <th>Date</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${recentComplaints.length > 0 ? recentComplaints.map(c => `
                                        <tr>
                                            <td>#${c.id}</td>
                                            <td><strong>${c.title}</strong></td>
                                            <td>${c.category}</td>
                                            <td><span class="badge badge-prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
                                            <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
                                            <td>${c.assignedStaffName ? c.assignedStaffName : '<em style="color:#94a3b8;">Unassigned</em>'}</td>
                                            <td style="white-space:nowrap;font-size:0.86rem;">${new Date(c.createdAt).toLocaleDateString()} <span style="font-size:0.75rem;color:var(--text-light);display:block;">${new Date(c.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></td>
                                            <td>
                                                <button class="btn btn-secondary btn-sm" onclick="App.viewComplaintDetails(${c.id})">Details</button>
                                            </td>
                                        </tr>
                                    `).join('') : `
                                        <tr>
                                            <td colspan="8">
                                                <div class="empty-state">
                                                    <div class="empty-state-icon"></div>
                                                    <div class="empty-state-title">No Complaints Submitted Yet</div>
                                                    <div class="empty-state-desc">You haven't submitted any complaints or service requests yet. When you report a campus issue, you can track its real-time resolution here.</div>
                                                    <button class="btn btn-primary btn-sm" onclick="App.navigateTo('submit-complaint')">Lodge Your First Complaint</button>
                                                </div>
                                            </td>
                                        </tr>
                                    `}
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;
            }

            content.innerHTML = html;
        } catch (err) {
            content.innerHTML = `
                <div class="card" style="border-left:5px solid var(--prio-urgent);">
                    <h3 style="color:var(--prio-urgent);margin-bottom:0.5rem;">Unable to Load Dashboard</h3>
                    <p style="color:var(--text-muted);">${err.message}</p>
                    <button class="btn btn-secondary btn-sm" style="margin-top:1rem;" onclick="App.loadDashboardView()">Retry</button>
                </div>
            `;
        }
    },

    // ================= Submit Complaint View =================
    loadSubmitComplaintView() {
        const content = document.getElementById('main-view-content');
        content.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Submit a Complaint / Service Request</h1>
                    <p class="page-subtitle">Report classroom, laboratory, Wi-Fi, or hostel issues for fast administrative resolution.</p>
                </div>
            </div>

            <div class="form-info-box">
                <strong>CampusCare Service Commitment:</strong> All submitted requests are automatically logged into the central college repository. Administrators will route tickets to certified staff within 24 hours, and you will receive real-time notifications when progress updates are posted.
            </div>

            <div class="card" style="max-width:840px;margin:0 auto;">
                <div class="card-header">
                    <h2 class="card-title">Complaint Information</h2>
                </div>
                <form id="complaint-form" onsubmit="App.handleSubmitComplaint(event)">
                    <div class="form-group">
                        <label class="form-label" for="comp-title">Complaint Title <span class="required-mark">*</span></label>
                        <input type="text" id="comp-title" class="form-control" required placeholder="e.g. Projector in Lab 204 flickering intermittently" minlength="3" maxlength="150">
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="comp-category">Service Category <span class="required-mark">*</span></label>
                            <select id="comp-category" class="form-control" required>
                                <option value="">Select Service Category</option>
                                <option value="Infrastructure">Infrastructure (Building, Furniture, Lights)</option>
                                <option value="Computer/Lab">Computer/Lab (PC, Hardware, Peripherals)</option>
                                <option value="Electricity">Electricity & Power Outage</option>
                                <option value="Water">Water Supply & Plumbing</option>
                                <option value="Cleanliness">Cleanliness & Sanitation</option>
                                <option value="Library">Library Services</option>
                                <option value="Transportation">College Transportation & Bus</option>
                                <option value="Wi-Fi/Internet">Campus Wi-Fi & Internet</option>
                                <option value="Classroom">Classroom Equipment & AC</option>
                                <option value="Other">Other Miscellaneous</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="comp-priority">Priority Level <span class="required-mark">*</span></label>
                            <select id="comp-priority" class="form-control" required>
                                <option value="LOW">LOW – Minor inconvenience</option>
                                <option value="MEDIUM" selected>MEDIUM – Standard campus issue</option>
                                <option value="HIGH">HIGH – Academic / lab disruption</option>
                                <option value="URGENT">URGENT – Critical safety / exam blocker</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="comp-dept">Target Department <span style="font-size:0.8rem;color:var(--text-light);">(Optional)</span></label>
                        <select id="comp-dept" class="form-control">
                            <option value="">Let Administrator Route to Department Automatically</option>
                            ${this.allDepartments.map(d => `<option value="${d.id}">${d.departmentName}</option>`).join('')}
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="comp-desc">Detailed Description <span class="required-mark">*</span></label>
                        <textarea id="comp-desc" class="form-control" rows="5" required placeholder="Please describe the exact campus location (building, floor, room number), symptoms of the problem, and impact on students..." minlength="10" maxlength="2000"></textarea>
                    </div>

                    <div style="display:flex;justify-content:flex-end;gap:1rem;margin-top:1.5rem;">
                        <button type="button" class="btn btn-secondary" onclick="App.navigateTo('dashboard')">Cancel</button>
                        <button type="submit" class="btn btn-primary btn-lg">Submit Complaint</button>
                    </div>
                </form>
            </div>
        `;
    },

    async handleSubmitComplaint(e) {
        e.preventDefault();
        const data = {
            title: document.getElementById('comp-title').value,
            category: document.getElementById('comp-category').value,
            priority: document.getElementById('comp-priority').value,
            description: document.getElementById('comp-desc').value
        };
        const dept = document.getElementById('comp-dept').value;
        if (dept) data.departmentId = parseInt(dept);

        try {
            await Api.createComplaint(data);
            this.showToast('Complaint submitted successfully and queued for review!', 'success');
            this.loadNotifications();
            this.navigateTo('my-complaints');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ================= Complaints List View =================
    async loadComplaintsView(mode = 'admin') {
        const content = document.getElementById('main-view-content');
        await this.loadStaffMembers();

        try {
            const res = await Api.getComplaints();
            let complaints = res.data || [];

            // Filter for student / staff if applicable
            if (mode === 'student' && this.currentUser.studentId) {
                complaints = complaints.filter(c => c.studentId === this.currentUser.studentId);
            } else if (mode === 'staff' && this.currentUser.staffId) {
                complaints = complaints.filter(c => c.assignedStaffId === this.currentUser.staffId);
            }

            this.cachedComplaints = complaints;

            let title = 'All Campus Complaints';
            let subtitle = 'Comprehensive registry of all student complaints and service requests';
            if (mode === 'student') {
                title = 'My Submitted Complaints';
                subtitle = 'Track the live resolution milestones and staff updates for your complaints';
            }
            if (mode === 'staff') {
                title = 'My Assigned Complaints';
                subtitle = 'Manage and update resolution progress for tasks assigned to you';
            }

            let html = `
                <div class="page-header">
                    <div>
                        <h1 class="page-title">${title} <span class="badge badge-assigned" style="vertical-align:middle;font-size:0.85rem;">${complaints.length} Total</span></h1>
                        <p class="page-subtitle">${subtitle}</p>
                    </div>
                    ${mode === 'student' ? '<button class="btn btn-primary" onclick="App.navigateTo(\'submit-complaint\')">Lodge New Complaint</button>' : ''}
                </div>

                <div class="card">
                    <!-- Filter Toolbar -->
                    <div class="filter-bar">
                        <div class="filter-group">
                            <span class="filter-label">Search</span>
                            <input type="text" id="filter-search" class="form-control" placeholder="Search by title or ID..." onkeyup="App.filterComplaintsTable()">
                        </div>
                        <div class="filter-group">
                            <span class="filter-label">Status</span>
                            <select id="filter-status" class="form-control" onchange="App.filterComplaintsTable()">
                                <option value="">All Statuses</option>
                                <option value="PENDING">PENDING</option>
                                <option value="ASSIGNED">ASSIGNED</option>
                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                                <option value="RESOLVED">RESOLVED</option>
                                <option value="CLOSED">CLOSED</option>
                            </select>
                        </div>
                        <div class="filter-group">
                            <span class="filter-label">Category</span>
                            <select id="filter-category" class="form-control" onchange="App.filterComplaintsTable()">
                                <option value="">All Categories</option>
                                <option value="Infrastructure">Infrastructure</option>
                                <option value="Computer/Lab">Computer/Lab</option>
                                <option value="Electricity">Electricity</option>
                                <option value="Water">Water</option>
                                <option value="Cleanliness">Cleanliness</option>
                                <option value="Library">Library</option>
                                <option value="Transportation">Transportation</option>
                                <option value="Wi-Fi/Internet">Wi-Fi/Internet</option>
                                <option value="Classroom">Classroom</option>
                            </select>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width:70px;">ID</th>
                                    <th>Title</th>
                                    <th>Category</th>
                                    <th>Priority</th>
                                    <th>Status</th>
                                    <th>Student</th>
                                    <th>Assigned Staff</th>
                                    <th>Date</th>
                                    <th style="text-align:right;">Action</th>
                                </tr>
                            </thead>
                            <tbody id="complaints-tbody">
                                ${this.renderComplaintsRows(complaints, mode)}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            content.innerHTML = html;
        } catch (err) {
            content.innerHTML = `
                <div class="card" style="border-left:5px solid var(--prio-urgent);">
                    <h3 style="color:var(--prio-urgent);">Error Loading Complaints</h3>
                    <p style="color:var(--text-muted);">${err.message}</p>
                </div>
            `;
        }
    },

    renderComplaintsRows(complaints, mode) {
        if (!complaints || complaints.length === 0) {
            return `
                <tr>
                    <td colspan="9">
                        <div class="empty-state">
                            <div class="empty-state-icon"></div>
                            <div class="empty-state-title">No Matching Complaints</div>
                            <div class="empty-state-desc">No complaints match the selected filter criteria. Try adjusting your filters or search keywords.</div>
                        </div>
                    </td>
                </tr>
            `;
        }

        return complaints.map(c => `
            <tr>
                <td><strong>#${c.id}</strong></td>
                <td><strong>${c.title}</strong></td>
                <td><span style="color:var(--text-muted);">${c.category}</span></td>
                <td class="table-badge-cell"><span class="badge badge-prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
                <td class="table-badge-cell"><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
                <td>${c.studentName || 'Student'}</td>
                <td>${c.assignedStaffName ? `${c.assignedStaffName} <span style="font-size:0.78rem;color:var(--text-light);">(${c.assignedStaffDesignation || 'Staff'})</span>` : '<em style="color:#94a3b8;">Unassigned</em>'}</td>
                <td style="white-space:nowrap;font-size:0.86rem;">${new Date(c.createdAt).toLocaleDateString()} <span style="font-size:0.75rem;color:var(--text-light);display:block;">${new Date(c.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></td>
                <td class="table-actions-cell" style="text-align:right;">
                    <div style="display:inline-flex;gap:0.4rem;">
                        <button class="btn btn-secondary btn-sm" onclick="App.viewComplaintDetails(${c.id})">Details</button>
                        ${this.currentUser.role === 'ADMIN' ? `
                            <button class="btn btn-primary btn-sm" onclick="App.openAssignModal(${c.id})">Assign</button>
                        ` : ''}
                        ${this.currentUser.role === 'STAFF' && c.assignedStaffId === this.currentUser.staffId ? `
                            <button class="btn btn-primary btn-sm" onclick="App.openUpdateModal(${c.id})">Update</button>
                        ` : ''}
                        ${this.currentUser.role === 'STUDENT' && c.status === 'RESOLVED' ? `
                            <button class="btn btn-success btn-sm" onclick="App.closeResolvedComplaint(${c.id})">Close Ticket</button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    filterComplaintsTable() {
        const searchEl = document.getElementById('filter-search');
        const statusEl = document.getElementById('filter-status');
        const categoryEl = document.getElementById('filter-category');

        const search = searchEl ? searchEl.value.trim().toLowerCase() : '';
        const status = statusEl ? statusEl.value : '';
        const category = categoryEl ? categoryEl.value : '';

        let filtered = this.cachedComplaints;
        if (search) {
            filtered = filtered.filter(c =>
                c.title.toLowerCase().includes(search) ||
                c.id.toString().includes(search) ||
                (c.category && c.category.toLowerCase().includes(search))
            );
        }
        if (status) filtered = filtered.filter(c => c.status === status);
        if (category) filtered = filtered.filter(c => c.category === category);

        const tbody = document.getElementById('complaints-tbody');
        if (tbody) {
            tbody.innerHTML = this.renderComplaintsRows(filtered, this.currentUser.role.toLowerCase());
        }
    },

    // ================= Complaint Details Modal =================
    async viewComplaintDetails(id) {
        try {
            const res = await Api.getComplaintById(id);
            const c = res.data;

            const modal = document.getElementById('details-modal');
            const body = document.getElementById('details-modal-body');

            const updates = c.updates || [];

            body.innerHTML = `
                <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1.25rem;flex-wrap:wrap;gap:0.75rem;">
                    <div>
                        <h2 style="font-size:1.35rem;font-weight:800;color:var(--text-main);">#${c.id} - ${c.title}</h2>
                        <span style="color:var(--text-muted);font-size:0.86rem;">Submitted on ${new Date(c.createdAt).toLocaleString()}</span>
                    </div>
                    <div style="display:flex;gap:0.5rem;align-items:center;">
                        <span class="badge badge-${c.status.toLowerCase()}">${c.status}</span>
                        <span class="badge badge-prio-${c.priority.toLowerCase()}">${c.priority}</span>
                    </div>
                </div>

                <div class="card" style="padding:1.25rem;margin-bottom:1.5rem;background:#f8fafc;border:1.5px solid var(--border-color);">
                    <div class="form-row">
                        <div><strong style="color:var(--text-light);text-transform:uppercase;font-size:0.78rem;display:block;">Category</strong> <span style="font-weight:700;">${c.category}</span></div>
                        <div><strong style="color:var(--text-light);text-transform:uppercase;font-size:0.78rem;display:block;">Department</strong> <span style="font-weight:700;">${c.departmentName || 'Not assigned yet'}</span></div>
                    </div>
                    <div class="form-row" style="margin-top:1rem;">
                        <div><strong style="color:var(--text-light);text-transform:uppercase;font-size:0.78rem;display:block;">Student Submitter</strong> <span style="font-weight:700;">${c.studentName} ${c.rollNumber ? `(${c.rollNumber}, ${c.branch || ''})` : ''}</span></div>
                        <div><strong style="color:var(--text-light);text-transform:uppercase;font-size:0.78rem;display:block;">Assigned Staff</strong> <span style="font-weight:700;">${c.assignedStaffName ? `${c.assignedStaffName} (${c.assignedStaffDesignation || 'Staff'})` : 'Unassigned'}</span></div>
                    </div>
                </div>

                <div style="margin-bottom:1.75rem;">
                    <h4 style="margin-bottom:0.5rem;font-weight:800;font-size:1rem;">Issue Description</h4>
                    <div style="white-space:pre-wrap;background:#ffffff;padding:1rem 1.25rem;border-radius:var(--radius-md);border:1.5px solid var(--border-color);line-height:1.6;font-size:0.95rem;">${c.description}</div>
                </div>

                <div>
                    <h4 style="margin-bottom:0.5rem;font-weight:800;font-size:1rem;">Lifecycle & Resolution Milestones</h4>
                    ${updates.length > 0 ? `
                        <ul class="timeline">
                            ${updates.map(u => `
                                <li class="timeline-item">
                                    <span class="timeline-point"></span>
                                    <div class="timeline-content">
                                        <div class="timeline-meta">
                                            <span><strong>${u.staffName || 'System'}</strong></span>
                                            <span>${new Date(u.createdAt).toLocaleString()}</span>
                                        </div>
                                        <p style="margin-bottom:0.35rem;">${u.message}</p>
                                        ${u.status ? `<div><span class="badge badge-${u.status.toLowerCase()}">${u.status}</span></div>` : ''}
                                    </div>
                                </li>
                            `).join('')}
                        </ul>
                    ` : `
                        <p style="color:var(--text-muted);font-size:0.9rem;padding:0.75rem;background:#f8fafc;border-radius:var(--radius-md);border:1px solid var(--border-color);">No updates logged yet. Awaiting administrative assignment.</p>
                    `}
                </div>

                <div style="margin-top:2rem;display:flex;gap:0.75rem;justify-content:flex-end;">
                    <button type="button" class="btn btn-secondary" onclick="App.closeModal('details-modal')">Close Window</button>
                    ${this.currentUser.role === 'ADMIN' ? `
                        <button class="btn btn-primary" onclick="App.closeModal('details-modal');App.openAssignModal(${c.id})">Assign Staff</button>
                    ` : ''}
                    ${this.currentUser.role === 'STAFF' && c.assignedStaffId === this.currentUser.staffId ? `
                        <button class="btn btn-primary" onclick="App.closeModal('details-modal');App.openUpdateModal(${c.id})">Update Status</button>
                    ` : ''}
                    ${this.currentUser.role === 'STUDENT' && c.status === 'RESOLVED' ? `
                        <button class="btn btn-success" onclick="App.closeResolvedComplaint(${c.id})">Confirm Resolution & Close</button>
                    ` : ''}
                </div>
            `;

            modal.classList.add('active');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ================= Assign Staff Modal (Admin) =================
    openAssignModal(complaintId) {
        const modal = document.getElementById('assign-modal');
        document.getElementById('assign-complaint-id').value = complaintId;

        const sel = document.getElementById('assign-staff-select');
        sel.innerHTML = '<option value="">Select Staff Member</option>' +
            this.allStaffMembers.map(s => `
                <option value="${s.id}">${s.name} (${s.designation} - ${s.departmentName || 'General'})</option>
            `).join('');

        modal.classList.add('active');
    },

    async handleAssignComplaint(e) {
        e.preventDefault();
        const cid = document.getElementById('assign-complaint-id').value;
        const staffId = document.getElementById('assign-staff-select').value;
        const priority = document.getElementById('assign-priority-select').value;

        try {
            await Api.assignComplaint(cid, parseInt(staffId), priority);
            this.showToast(`Complaint #${cid} assigned successfully!`, 'success');
            this.closeModal('assign-modal');
            this.loadComplaintsView('admin');
            this.loadNotifications();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ================= Update Status Modal (Staff) =================
    openUpdateModal(complaintId) {
        const modal = document.getElementById('update-modal');
        document.getElementById('update-complaint-id').value = complaintId;
        modal.classList.add('active');
    },

    async handleUpdateComplaint(e) {
        e.preventDefault();
        const cid = document.getElementById('update-complaint-id').value;
        const status = document.getElementById('update-status-select').value;
        const msg = document.getElementById('update-message').value;

        try {
            await Api.addComplaintUpdate(cid, msg, status);
            this.showToast(`Complaint #${cid} updated to ${status}!`, 'success');
            this.closeModal('update-modal');
            if (this.currentUser.role === 'STAFF') {
                this.loadComplaintsView('staff');
            } else {
                this.loadComplaintsView('admin');
            }
            this.loadNotifications();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async closeResolvedComplaint(complaintId) {
        if (!confirm('Are you satisfied with the resolution and wish to mark this ticket as CLOSED?')) return;
        try {
            await Api.updateComplaintStatus(complaintId, 'CLOSED', 'Student confirmed resolution and closed ticket');
            this.showToast(`Complaint #${complaintId} closed. Thank you!`, 'success');
            this.closeModal('details-modal');
            this.loadComplaintsView('student');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ================= Departments Management =================
    async loadDepartmentsView() {
        const content = document.getElementById('main-view-content');
        await this.loadDepartments();

        content.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Campus Departments <span class="badge badge-assigned" style="vertical-align:middle;font-size:0.85rem;">${this.allDepartments.length} Departments</span></h1>
                    <p class="page-subtitle">Configure college service divisions responsible for handling student complaints.</p>
                </div>
                <button class="btn btn-primary" onclick="App.openNewDeptModal()">Add New Department</button>
            </div>

            <div class="card">
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th style="width:70px;">ID</th>
                                <th>Department Name</th>
                                <th>Description / Responsibilities</th>
                                <th>Staff Members</th>
                                <th>Complaints Handled</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${this.allDepartments.map(d => `
                                <tr>
                                    <td><strong>#${d.id}</strong></td>
                                    <td><strong>${d.departmentName}</strong></td>
                                    <td>${d.description || '<em style="color:#94a3b8;">No description provided</em>'}</td>
                                    <td><span class="badge badge-assigned">${d.staffCount} Staff</span></td>
                                    <td><span class="badge badge-prio-medium">${d.complaintCount} Tickets</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    },

    openNewDeptModal() {
        document.getElementById('dept-modal').classList.add('active');
    },

    async handleCreateDepartment(e) {
        e.preventDefault();
        const data = {
            departmentName: document.getElementById('new-dept-name').value,
            description: document.getElementById('new-dept-desc').value
        };

        try {
            await Api.createDepartment(data);
            this.showToast('Department created successfully!', 'success');
            this.closeModal('dept-modal');
            this.loadDepartmentsView();
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ================= Staff Management =================
    async loadStaffView() {
        const content = document.getElementById('main-view-content');
        await this.loadStaffMembers();

        content.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Faculty & Staff Members <span class="badge badge-assigned" style="vertical-align:middle;font-size:0.85rem;">${this.allStaffMembers.length} Staff</span></h1>
                    <p class="page-subtitle">Directory of registered college faculty and technical support personnel.</p>
                </div>
            </div>

            <div class="card">
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th style="width:70px;">ID</th>
                                <th>Staff Name</th>
                                <th>Email</th>
                                <th>Department</th>
                                <th>Designation</th>
                                <th>Phone</th>
                                <th>Assigned</th>
                                <th>Resolved</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${this.allStaffMembers.map(s => `
                                <tr>
                                    <td><strong>#${s.id}</strong></td>
                                    <td><strong>${s.name}</strong></td>
                                    <td>${s.email}</td>
                                    <td><span class="badge badge-assigned">${s.departmentName || 'General'}</span></td>
                                    <td>${s.designation}</td>
                                    <td>${s.phone || '-'}</td>
                                    <td><strong>${s.assignedCount}</strong></td>
                                    <td><strong style="color:var(--status-resolved);">${s.resolvedCount}</strong></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    },

    // ================= Students Management =================
    async loadStudentsView() {
        const content = document.getElementById('main-view-content');
        try {
            const res = await Api.getStudents();
            const students = res.data || [];

            content.innerHTML = `
                <div class="page-header">
                    <div>
                        <h1 class="page-title">Registered Students <span class="badge badge-assigned" style="vertical-align:middle;font-size:0.85rem;">${students.length} Students</span></h1>
                        <p class="page-subtitle">Directory of verified students enrolled in the CampusCare management system.</p>
                    </div>
                </div>

                <div class="card">
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width:70px;">ID</th>
                                    <th>Roll Number</th>
                                    <th>Student Name</th>
                                    <th>Email</th>
                                    <th>Branch</th>
                                    <th>Year / Div</th>
                                    <th>Phone</th>
                                    <th>Complaints</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${students.map(st => `
                                    <tr>
                                        <td><strong>#${st.id}</strong></td>
                                        <td><strong>${st.rollNumber}</strong></td>
                                        <td>${st.name}</td>
                                        <td>${st.email}</td>
                                        <td>${st.branch}</td>
                                        <td>${st.year} / ${st.division || '-'}</td>
                                        <td>${st.phone || '-'}</td>
                                        <td><span class="badge badge-assigned">${st.totalComplaints}</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            content.innerHTML = `<div class="card" style="color:red;">Error loading students: ${err.message}</div>`;
        }
    },

    // ================= Profile View =================
    async loadProfileView() {
        const content = document.getElementById('main-view-content');
        const role = this.currentUser.role;
        const initials = (this.currentUser.name || 'CC').split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();

        // Fetch fresh student/staff info if available
        let studentInfo = null;
        let staffInfo = null;

        if (role === 'STUDENT') {
            try {
                const sRes = await Api.getStudents();
                studentInfo = (sRes.data || []).find(s => s.email === this.currentUser.email);
            } catch (e) { console.error(e); }
        } else if (role === 'STAFF') {
            try {
                const sRes = await Api.getStaff();
                staffInfo = (sRes.data || []).find(s => s.email === this.currentUser.email);
            } catch (e) { console.error(e); }
        }

        content.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">University Account Profile</h1>
                    <p class="page-subtitle">Your personal account credentials and institutional identity records.</p>
                </div>
            </div>

            <div class="profile-card">
                <div class="profile-header-banner">
                    <div class="profile-avatar-circle">${initials}</div>
                    <div>
                        <h2 style="font-size:1.6rem;font-weight:800;line-height:1.2;">${this.currentUser.name}</h2>
                        <div style="margin-top:0.4rem;display:flex;gap:0.6rem;align-items:center;">
                            <span class="role-tag" style="background:#ffffff;color:var(--primary);">${this.currentUser.role}</span>
                            <span style="opacity:0.9;font-size:0.9rem;">${this.currentUser.email}</span>
                        </div>
                    </div>
                </div>

                <div class="profile-body">
                    <h3 style="font-size:1.15rem;font-weight:800;margin-bottom:1.25rem;color:var(--text-main);">Institutional Credentials</h3>
                    
                    <div class="profile-grid">
                        <div class="profile-item">
                            <div class="profile-label">Full Name</div>
                            <div class="profile-value">${this.currentUser.name}</div>
                        </div>
                        <div class="profile-item">
                            <div class="profile-label">Email Address</div>
                            <div class="profile-value">${this.currentUser.email}</div>
                        </div>
                        <div class="profile-item">
                            <div class="profile-label">Account Role</div>
                            <div class="profile-value">${this.currentUser.role}</div>
                        </div>
                        <div class="profile-item">
                            <div class="profile-label">Contact Phone</div>
                            <div class="profile-value">${this.currentUser.phone || studentInfo?.phone || staffInfo?.phone || 'Not provided'}</div>
                        </div>

                        ${role === 'STUDENT' ? `
                            <div class="profile-item">
                                <div class="profile-label">Roll Number</div>
                                <div class="profile-value">${studentInfo?.rollNumber || this.currentUser.rollNumber || 'CS-2024-001'}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Academic Branch</div>
                                <div class="profile-value">${studentInfo?.branch || this.currentUser.branch || 'Computer Engineering'}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Academic Year</div>
                                <div class="profile-value">${studentInfo?.year || this.currentUser.year || '3rd Year'}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Division</div>
                                <div class="profile-value">${studentInfo?.division || this.currentUser.division || 'A'}</div>
                            </div>
                        ` : ''}

                        ${role === 'STAFF' ? `
                            <div class="profile-item">
                                <div class="profile-label">Department</div>
                                <div class="profile-value">${staffInfo?.departmentName || 'Information Technology'}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Designation</div>
                                <div class="profile-value">${staffInfo?.designation || 'Faculty'}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Assigned Tickets</div>
                                <div class="profile-value">${staffInfo?.assignedCount || 0}</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Resolved Tickets</div>
                                <div class="profile-value">${staffInfo?.resolvedCount || 0}</div>
                            </div>
                        ` : ''}

                        ${role === 'ADMIN' ? `
                            <div class="profile-item">
                                <div class="profile-label">Administrative Privileges</div>
                                <div class="profile-value">Full System Access & Routing Control</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-label">Security Protocol</div>
                                <div class="profile-value">JWT Bearer Authentication & RBAC</div>
                            </div>
                        ` : ''}
                    </div>

                    <div style="margin-top:2rem;padding-top:1.5rem;border-top:1.5px solid var(--border-color);display:flex;justify-content:flex-end;">
                        <button class="btn btn-secondary" onclick="App.logout()">Log Out of Account</button>
                    </div>
                </div>
            </div>
        `;
    },

    closeModal(modalId) {
        const m = document.getElementById(modalId);
        if (m) m.classList.remove('active');
    }
};

window.addEventListener('DOMContentLoaded', () => {
    App.init();
});
