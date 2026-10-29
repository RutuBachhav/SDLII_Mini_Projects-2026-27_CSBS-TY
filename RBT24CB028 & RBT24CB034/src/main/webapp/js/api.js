/**
 * CampusCare - API Client Service
 */

const API_BASE = window.location.pathname.startsWith('/CampusCare')
    ? '/CampusCare/api'
    : '/api';

const Api = {
    getToken() {
        return localStorage.getItem('campuscare_token');
    },

    setToken(token) {
        if (token) {
            localStorage.setItem('campuscare_token', token);
        } else {
            localStorage.removeItem('campuscare_token');
        }
    },

    getUser() {
        const u = localStorage.getItem('campuscare_user');
        return u ? JSON.parse(u) : null;
    },

    setUser(user) {
        if (user) {
            localStorage.setItem('campuscare_user', JSON.stringify(user));
        } else {
            localStorage.removeItem('campuscare_user');
        }
    },

    logout() {
        this.setToken(null);
        this.setUser(null);
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const res = await fetch(url, { ...options, headers });
            // Server serialises dates as "2026-01-01T10:00:00.000Z[UTC]"; strip the zone-id suffix so Date() can parse it
            const raw = await res.text();
            let data = null;
            try { data = JSON.parse(raw.replace(/Z\[[^\]"]+\]/g, 'Z')); } catch (e) { data = null; }

            if (!res.ok) {
                const message = (data && data.message) ? data.message : `HTTP Error ${res.status}: ${res.statusText}`;
                if (res.status === 401) {
                    this.logout();
                }
                throw new Error(message);
            }

            return data;
        } catch (err) {
            console.error('API Request failed:', err);
            throw err;
        }
    },

    // Auth API
    login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    register(data) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    getCurrentUser() {
        return this.request('/auth/me', { method: 'GET' });
    },

    // Complaints API
    getComplaints(params = {}) {
        const query = new URLSearchParams();
        if (params.status) query.append('status', params.status);
        if (params.category) query.append('category', params.category);
        if (params.priority) query.append('priority', params.priority);
        if (params.departmentId) query.append('departmentId', params.departmentId);
        const qs = query.toString() ? `?${query.toString()}` : '';
        return this.request(`/complaints${qs}`, { method: 'GET' });
    },

    getComplaintById(id) {
        return this.request(`/complaints/${id}`, { method: 'GET' });
    },

    createComplaint(data) {
        return this.request('/complaints', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    assignComplaint(id, staffId, priority) {
        return this.request(`/complaints/${id}/assign`, {
            method: 'PUT',
            body: JSON.stringify({ staffId, priority })
        });
    },

    updateComplaintStatus(id, status, remarks) {
        return this.request(`/complaints/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status, remarks })
        });
    },

    addComplaintUpdate(id, message, newStatus) {
        return this.request(`/complaints/${id}/updates`, {
            method: 'POST',
            body: JSON.stringify({ message, newStatus })
        });
    },

    deleteComplaint(id) {
        return this.request(`/complaints/${id}`, { method: 'DELETE' });
    },

    // Department API
    getDepartments() {
        return this.request('/departments', { method: 'GET' });
    },

    createDepartment(data) {
        return this.request('/departments', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // Students & Staff
    getStudents() {
        return this.request('/students', { method: 'GET' });
    },

    getStaff() {
        return this.request('/staff', { method: 'GET' });
    },

    // Notifications
    getNotifications() {
        return this.request('/notifications', { method: 'GET' });
    },

    markNotificationRead(id) {
        return this.request(`/notifications/${id}/read`, { method: 'PUT' });
    },

    markAllNotificationsRead() {
        return this.request('/notifications/read-all', { method: 'PUT' });
    },

    // Dashboards
    getAdminDashboard() {
        return this.request('/dashboard/admin', { method: 'GET' });
    },

    getStaffDashboard() {
        return this.request('/dashboard/staff', { method: 'GET' });
    },

    getStudentDashboard() {
        return this.request('/dashboard/student', { method: 'GET' });
    }
};
