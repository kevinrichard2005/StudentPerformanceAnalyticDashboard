/**
 * Student Management System — Core Front-End Runtime
 * Theme manager · Shared SaaS shell · Auth & RBAC · API client · Accessible Menus, Modals & Toasts · Chart tokens
 * Zero inline style attributes; all colors come from CSS custom properties.
 */

const Theme = {
    KEY: 'sms_theme',

    resolve() {
        try {
            const saved = localStorage.getItem(this.KEY);
            if (saved === 'light' || saved === 'dark') return saved;
        } catch (e) { /* storage unavailable */ }
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return 'dark';
        }
        return 'light';
    },

    current() {
        return document.documentElement.getAttribute('data-theme') || this.resolve();
    },

    apply(theme, persist = false) {
        const next = theme === 'dark' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', next);
        if (persist) {
            try { localStorage.setItem(this.KEY, next); } catch (e) { /* ignore */ }
        }
        this.syncButtons(next);
        window.dispatchEvent(new CustomEvent('sms:themechange', { detail: { theme: next } }));
    },

    toggle() {
        const next = this.current() === 'dark' ? 'light' : 'dark';
        this.apply(next, true);
    },

    syncButtons(theme = this.current()) {
        const isDark = theme === 'dark';
        document.querySelectorAll('[data-theme-toggle]').forEach(btn => {
            btn.setAttribute('aria-label', isDark ? 'Switch to light theme' : 'Switch to dark theme');
            btn.setAttribute('title', isDark ? 'Switch to light theme' : 'Switch to dark theme');
            btn.innerHTML = `<i data-lucide="${isDark ? 'sun' : 'moon'}"></i>`;
        });
        if (typeof lucide !== 'undefined') lucide.createIcons();
    },

    token(name) {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }
};

// Apply theme immediately before DOMContentLoaded
Theme.apply(Theme.resolve(), false);

/**
 * Chart.js token helper — reads live CSS custom properties so charts adapt to light/dark
 */
const ChartTheme = {
    tokens() {
        return {
            text: Theme.token('--text') || '#111827',
            muted: Theme.token('--text-muted') || '#4B5563',
            subtle: Theme.token('--text-subtle') || '#5B6472',
            border: Theme.token('--border') || '#E5E7EB',
            surface: Theme.token('--surface') || '#FFFFFF',
            brand: Theme.token('--brand') || '#4F46E5',
            c1: Theme.token('--chart-1') || '#4F46E5',
            c2: Theme.token('--chart-2') || '#6366F1',
            c3: Theme.token('--chart-3') || '#818CF8',
            c4: Theme.token('--chart-4') || '#A5B4FC',
            c5: Theme.token('--chart-5') || '#9CA3AF',
            grid: Theme.token('--chart-grid') || '#E5E7EB',
            success: Theme.token('--success-solid') || '#15803D',
            danger: Theme.token('--danger-solid') || '#B91C1C',
            warning: Theme.token('--warning-solid') || '#B45309'
        };
    },

    applyDefaults() {
        if (typeof Chart === 'undefined') return;
        const t = this.tokens();
        Chart.defaults.color = t.muted;
        Chart.defaults.font.family = '"Inter", ui-sans-serif, system-ui, -apple-system, sans-serif';
        Chart.defaults.font.size = 12;
        Chart.defaults.borderColor = t.grid;
        Chart.defaults.plugins.tooltip.backgroundColor = t.surface;
        Chart.defaults.plugins.tooltip.titleColor = t.text;
        Chart.defaults.plugins.tooltip.bodyColor = t.muted;
        Chart.defaults.plugins.tooltip.borderColor = t.border;
        Chart.defaults.plugins.tooltip.borderWidth = 1;
        Chart.defaults.plugins.tooltip.padding = 10;
        Chart.defaults.plugins.tooltip.cornerRadius = 8;
    }
};

/**
 * Role-Based Authentication & Session Manager (Student, Teacher, Admin)
 */
const Auth = {
    TOKEN_KEY: 'sms_auth_token',
    USER_KEY: 'sms_auth_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        try {
            const raw = localStorage.getItem(this.USER_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    },

    getRole() {
        return this.getUser()?.role || null;
    },

    isLoggedIn() {
        return Boolean(this.getToken() && this.getUser());
    },

    isAdmin() { return this.getRole() === 'ADMIN'; },
    isTeacher() { return this.getRole() === 'TEACHER'; },
    isStudent() { return this.getRole() === 'STUDENT'; },

    canEdit() { return this.isAdmin() || this.isTeacher(); },
    canDelete() { return this.isAdmin(); },

    setSession(authData) {
        if (!authData || !authData.token || !authData.user) return;
        localStorage.setItem(this.TOKEN_KEY, authData.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(authData.user));
    },

    clearSession() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    async logout() {
        try {
            await API.logout();
        } catch (e) { /* ignore network errors */ }
        this.clearSession();
        window.location.href = '/pages/login.html';
    },

    isAuthPage() {
        const page = window.location.pathname.split('/').pop() || 'index.html';
        return page === 'login.html' || page === 'register.html';
    },

    enforcePageAccess() {
        if (this.isAuthPage()) return true;

        if (!this.isLoggedIn()) {
            window.location.href = '/pages/login.html';
            return false;
        }

        const page = window.location.pathname.split('/').pop() || 'index.html';
        if (this.isStudent() && (page === 'add-student.html' || page === 'edit-student.html')) {
            window.location.href = '/index.html';
            return false;
        }
        return true;
    },

    applyRoleUI() {
        const user = this.getUser();
        if (!user) return;

        const role = (user.role || 'STUDENT').toUpperCase();
        document.body.classList.remove('role-admin', 'role-teacher', 'role-student');
        document.body.classList.add('role-' + role.toLowerCase());

        const displayName = localStorage.getItem('sms_adminName') || user.fullName;
        document.querySelectorAll('.profile-name').forEach(el => {
            el.textContent = displayName;
        });
        document.querySelectorAll('.profile-avatar').forEach(el => {
            el.textContent = Utils.getInitials(displayName);
        });
        document.querySelectorAll('.profile-role').forEach(el => {
            el.innerHTML = `<span class="role-badge role-${role.toLowerCase()}">${Utils.escapeHtml(role)}</span>`;
        });

        // Student sidebar shortcut to their own record
        if (role === 'STUDENT' && user.studentRecordId) {
            const nav = document.querySelector('.sidebar-nav');
            if (nav && !document.getElementById('myProfileNavItem')) {
                const section = nav.querySelector('.nav-section');
                if (section) {
                    const link = document.createElement('a');
                    link.id = 'myProfileNavItem';
                    link.href = `/pages/student-details.html?id=${user.studentRecordId}`;
                    link.className = 'nav-item';
                    link.setAttribute('data-page', 'student-details.html');
                    link.innerHTML = '<i data-lucide="id-card"></i><span>My Academic Profile</span>';
                    section.appendChild(link);
                }
            }
        }
    }
};

/**
 * Keyboard-accessible floating Menu / Popover manager (user menu, notifications, row "⋯" actions)
 */
const Menu = {
    activeMenu: null,
    activeTrigger: null,

    close() {
        if (this.activeMenu) {
            this.activeMenu.remove();
            this.activeMenu = null;
        }
        if (this.activeTrigger) {
            this.activeTrigger.setAttribute('aria-expanded', 'false');
            this.activeTrigger = null;
        }
    },

    open(trigger, html, options = {}) {
        if (this.activeTrigger === trigger) {
            this.close();
            return null;
        }
        this.close();

        const menu = document.createElement('div');
        menu.className = 'menu';
        menu.setAttribute('role', options.role || 'menu');
        menu.innerHTML = html;
        document.body.appendChild(menu);

        trigger.setAttribute('aria-expanded', 'true');
        this.activeMenu = menu;
        this.activeTrigger = trigger;

        if (typeof lucide !== 'undefined') lucide.createIcons();

        const rect = trigger.getBoundingClientRect();
        const menuRect = menu.getBoundingClientRect();
        let top = rect.bottom + 6;
        let left = options.align === 'left' ? rect.left : rect.right - menuRect.width;

        if (left + menuRect.width > window.innerWidth - 12) {
            left = window.innerWidth - menuRect.width - 12;
        }
        if (left < 12) left = 12;
        if (top + menuRect.height > window.innerHeight - 12) {
            top = Math.max(12, rect.top - menuRect.height - 6);
        }

        menu.style.top = `${Math.round(top)}px`;
        menu.style.left = `${Math.round(left)}px`;

        requestAnimationFrame(() => menu.classList.add('open'));

        const items = Array.from(menu.querySelectorAll('.menu-item'));
        if (items.length > 0 && options.focusFirst) {
            items[0].focus();
        }

        menu.addEventListener('keydown', e => {
            const focusable = Array.from(menu.querySelectorAll('.menu-item'));
            const idx = focusable.indexOf(document.activeElement);
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (focusable.length) focusable[(idx + 1) % focusable.length].focus();
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (focusable.length) focusable[(idx - 1 + focusable.length) % focusable.length].focus();
            } else if (e.key === 'Home') {
                e.preventDefault();
                if (focusable.length) focusable[0].focus();
            } else if (e.key === 'End') {
                e.preventDefault();
                if (focusable.length) focusable[focusable.length - 1].focus();
            } else if (e.key === 'Escape') {
                e.preventDefault();
                const t = this.activeTrigger;
                this.close();
                if (t) t.focus();
            }
        });

        return menu;
    }
};

document.addEventListener('click', e => {
    if (!Menu.activeMenu) return;
    if (Menu.activeMenu.contains(e.target)) return;
    if (Menu.activeTrigger && Menu.activeTrigger.contains(e.target)) return;
    Menu.close();
});

window.addEventListener('resize', () => Menu.close());
window.addEventListener('scroll', () => Menu.close(), true);

/**
 * Shared SaaS Application Shell (Sidebar + Sticky Header + Search + Theme Toggle + Notifications + User Dropdown)
 */
const Shell = {
    PAGE_TITLES: {
        'index.html': { section: 'Overview', title: 'Dashboard' },
        'students.html': { section: 'Students', title: 'All Students' },
        'add-student.html': { section: 'Students', title: 'Add Student' },
        'edit-student.html': { section: 'Students', title: 'Edit Student' },
        'student-details.html': { section: 'Students', title: 'Student Profile' },
        'analytics.html': { section: 'Reports', title: 'Analytics' },
        'settings.html': { section: 'Workspace', title: 'Settings' }
    },

    init() {
        const container = document.querySelector('.app-container');
        if (!container || Auth.isAuthPage()) return;

        const currentPage = window.location.pathname.split('/').pop() || 'index.html';
        const meta = this.PAGE_TITLES[currentPage] || { section: 'Workspace', title: document.title.split('—')[0].trim() };

        this.renderSidebar(container);
        this.renderHeader(container, meta);
        Sidebar.init();
        Auth.applyRoleUI();
        Theme.syncButtons();
    },

    renderSidebar(container) {
        let sidebar = document.getElementById('sidebar');
        if (!sidebar) {
            sidebar = document.createElement('aside');
            sidebar.id = 'sidebar';
            sidebar.className = 'sidebar';
            container.prepend(sidebar);
        }
        sidebar.setAttribute('aria-label', 'Primary navigation');
        sidebar.innerHTML = `
            <div class="sidebar-header">
                <div class="sidebar-logo" aria-hidden="true"><i data-lucide="graduation-cap"></i></div>
                <div class="sidebar-brand">
                    <h2>StudentMS</h2>
                    <span>Academic Workspace</span>
                </div>
            </div>
            <nav class="sidebar-nav" aria-label="Main sections">
                <div class="nav-section">
                    <div class="nav-section-title">Overview</div>
                    <a href="/index.html" class="nav-item" data-page="index.html">
                        <i data-lucide="layout-dashboard"></i><span>Dashboard</span>
                    </a>
                </div>
                <div class="nav-section">
                    <div class="nav-section-title">Directory</div>
                    <a href="/pages/students.html" class="nav-item" data-page="students.html">
                        <i data-lucide="users"></i><span>All Students</span>
                    </a>
                    <a href="/pages/add-student.html" class="nav-item" data-page="add-student.html">
                        <i data-lucide="user-plus"></i><span>Add Student</span>
                    </a>
                </div>
                <div class="nav-section">
                    <div class="nav-section-title">Insights</div>
                    <a href="/pages/analytics.html" class="nav-item" data-page="analytics.html">
                        <i data-lucide="bar-chart-3"></i><span>Analytics</span>
                    </a>
                </div>
            </nav>
            <div class="sidebar-footer">
                <a href="/pages/settings.html" class="nav-item" data-page="settings.html">
                    <i data-lucide="settings"></i><span>Settings</span>
                </a>
                <button type="button" class="nav-item nav-danger" id="sidebarLogoutBtn">
                    <i data-lucide="log-out"></i><span>Sign Out</span>
                </button>
            </div>
        `;

        let overlay = document.getElementById('sidebarOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'sidebarOverlay';
            overlay.className = 'sidebar-overlay';
            sidebar.after(overlay);
        }

        const logoutBtn = sidebar.querySelector('#sidebarLogoutBtn');
        if (logoutBtn) logoutBtn.addEventListener('click', () => Auth.logout());
    },

    renderHeader(container, meta) {
        let header = container.querySelector('.header');
        if (!header) {
            const wrapper = container.querySelector('.main-wrapper');
            header = document.createElement('header');
            header.className = 'header';
            if (wrapper) wrapper.prepend(header);
        }

        const user = Auth.getUser();
        const name = localStorage.getItem('sms_adminName') || user?.fullName || 'Administrator';
        const role = (user?.role || 'ADMIN').toUpperCase();

        header.innerHTML = `
            <div class="header-left">
                <button type="button" class="icon-btn sidebar-toggle" id="sidebarToggle" aria-label="Open navigation menu">
                    <i data-lucide="menu"></i>
                </button>
                <nav class="breadcrumb" aria-label="Breadcrumb">
                    <span class="breadcrumb-parent">${Utils.escapeHtml(meta.section)}</span>
                    <span class="breadcrumb-sep" aria-hidden="true">/</span>
                    <span class="breadcrumb-current header-title"><h1>${Utils.escapeHtml(meta.title)}</h1></span>
                </nav>
            </div>
            <div class="header-right">
                <div class="header-search">
                    <i data-lucide="search"></i>
                    <input type="search" id="globalSearchInput" placeholder="Search students..." aria-label="Search students by name, ID, or department">
                    <span class="kbd" aria-hidden="true">/</span>
                </div>
                <button type="button" class="icon-btn" id="themeToggleBtn" data-theme-toggle aria-label="Toggle color theme">
                    <i data-lucide="moon"></i>
                </button>
                <button type="button" class="header-icon-btn" id="notifyBtn" aria-label="Notifications" aria-haspopup="menu" aria-expanded="false">
                    <i data-lucide="bell"></i>
                    <span class="badge" aria-hidden="true"></span>
                </button>
                <button type="button" class="header-profile" id="userMenuBtn" aria-label="Account menu" aria-haspopup="menu" aria-expanded="false">
                    <span class="profile-avatar">${Utils.escapeHtml(Utils.getInitials(name))}</span>
                    <span class="profile-info">
                        <span class="profile-name">${Utils.escapeHtml(name)}</span>
                        <span class="profile-role"><span class="role-badge role-${role.toLowerCase()}">${Utils.escapeHtml(role)}</span></span>
                    </span>
                    <i data-lucide="chevron-down"></i>
                </button>
            </div>
        `;

        const themeBtn = header.querySelector('#themeToggleBtn');
        if (themeBtn) themeBtn.addEventListener('click', () => Theme.toggle());

        const notifyBtn = header.querySelector('#notifyBtn');
        if (notifyBtn) notifyBtn.addEventListener('click', () => this.openNotificationsMenu(notifyBtn));

        const userMenuBtn = header.querySelector('#userMenuBtn');
        if (userMenuBtn) userMenuBtn.addEventListener('click', () => this.openUserMenu(userMenuBtn));
    },

    async openNotificationsMenu(trigger) {
        try {
            const res = await API.getStats();
            const s = res.data || {};
            const html = `
                <div class="menu-header">
                    <strong>Workspace Summary</strong>
                    <span>Live academic snapshot</span>
                </div>
                <a href="/pages/students.html" class="menu-note" role="menuitem">
                    <i data-lucide="users" class="tone-brand"></i>
                    <div>
                        <div class="note-title">${s.totalStudents ?? 0} enrolled students</div>
                        <div class="note-sub">Class average: ${s.averageMarks ?? 0} marks</div>
                    </div>
                </a>
                <a href="/pages/analytics.html" class="menu-note" role="menuitem">
                    <i data-lucide="check-circle-2" class="tone-success"></i>
                    <div>
                        <div class="note-title">${s.passPercentage ?? 0}% pass rate (${s.passedStudents ?? 0} passed)</div>
                        <div class="note-sub">${s.failedStudents ?? 0} student(s) currently below 40 marks</div>
                    </div>
                </a>
            `;
            Menu.open(trigger, html);
        } catch (err) {
            Menu.open(trigger, `<div class="menu-empty">Unable to load notifications</div>`);
        }
    },

    openUserMenu(trigger) {
        const user = Auth.getUser() || {};
        const name = localStorage.getItem('sms_adminName') || user.fullName || 'Administrator';
        const isDark = Theme.current() === 'dark';
        const profileLink = (user.role === 'STUDENT' && user.studentRecordId)
            ? `<a href="/pages/student-details.html?id=${user.studentRecordId}" class="menu-item" role="menuitem"><i data-lucide="id-card"></i><span>My Academic Profile</span></a>`
            : '';

        const html = `
            <div class="menu-header">
                <strong>${Utils.escapeHtml(name)}</strong>
                <span>${Utils.escapeHtml(user.email || '')}</span>
            </div>
            ${profileLink}
            <a href="/pages/settings.html" class="menu-item" role="menuitem">
                <i data-lucide="settings"></i><span>Settings</span>
            </a>
            <button type="button" class="menu-item" data-menu-theme role="menuitem">
                <i data-lucide="${isDark ? 'sun' : 'moon'}"></i><span>${isDark ? 'Light theme' : 'Dark theme'}</span>
            </button>
            <div class="menu-sep" role="separator"></div>
            <button type="button" class="menu-item is-danger" data-menu-logout role="menuitem">
                <i data-lucide="log-out"></i><span>Sign Out</span>
            </button>
        `;
        const menu = Menu.open(trigger, html, { focusFirst: true });
        if (!menu) return;
        const themeItem = menu.querySelector('[data-menu-theme]');
        if (themeItem) themeItem.addEventListener('click', () => { Theme.toggle(); Menu.close(); });
        const logoutItem = menu.querySelector('[data-menu-logout]');
        if (logoutItem) logoutItem.addEventListener('click', () => { Menu.close(); Auth.logout(); });
    }
};

/**
 * REST API Service
 */
const API = {
    BASE_URL: '/api/students',
    AUTH_URL: '/api/auth',

    async request(url, options = {}) {
        const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
        const token = Auth.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        const config = { ...options, headers };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                if (response.status === 401 && !Auth.isAuthPage()) {
                    Auth.clearSession();
                    window.location.href = '/pages/login.html';
                }
                throw { status: response.status, data };
            }
            return data;
        } catch (error) {
            if (error.data) throw error;
            throw { status: 0, data: { success: false, message: 'Network error. Please check your connection.' } };
        }
    },

    async login(credentials) {
        return this.request(`${this.AUTH_URL}/login`, {
            method: 'POST',
            body: JSON.stringify(credentials),
        });
    },

    async register(payload) {
        return this.request(`${this.AUTH_URL}/register`, {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },

    async getMe() {
        return this.request(`${this.AUTH_URL}/me`);
    },

    async getMyStudentProfile() {
        return this.request(`${this.AUTH_URL}/my-student-profile`);
    },

    async getAllUsers() {
        return this.request(`${this.AUTH_URL}/users`);
    },

    async logout() {
        return this.request(`${this.AUTH_URL}/logout`, { method: 'POST' });
    },

    async createStudent(studentData) {
        return this.request(this.BASE_URL, {
            method: 'POST',
            body: JSON.stringify(studentData),
        });
    },

    async getAllStudents(page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') {
        const params = new URLSearchParams({ page, size, sortBy, sortDir });
        return this.request(`${this.BASE_URL}?${params}`);
    },

    async getStudentById(id) {
        return this.request(`${this.BASE_URL}/${id}`);
    },

    async updateStudent(id, studentData) {
        return this.request(`${this.BASE_URL}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(studentData),
        });
    },

    async deleteStudent(id) {
        return this.request(`${this.BASE_URL}/${id}`, {
            method: 'DELETE',
        });
    },

    async searchStudents(keyword, page = 0, size = 10) {
        const params = new URLSearchParams({ keyword, page, size });
        return this.request(`${this.BASE_URL}/search?${params}`);
    },

    async filterStudents({ keyword = '', department = '', grade = '', page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = {}) {
        const params = new URLSearchParams({ page, size, sortBy, sortDir });
        if (keyword) params.append('keyword', keyword);
        if (department) params.append('department', department);
        if (grade) params.append('grade', grade);
        return this.request(`${this.BASE_URL}/filter?${params}`);
    },

    async getStats() {
        return this.request(`${this.BASE_URL}/stats`);
    },

    async getRecentStudents() {
        return this.request(`${this.BASE_URL}/recent`);
    },
};

/**
 * Toast Notification System
 */
const Toast = {
    container: null,

    init() {
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            this.container.setAttribute('role', 'status');
            this.container.setAttribute('aria-live', 'polite');
            document.body.appendChild(this.container);
        }
    },

    show(type, title, message, duration = 4000) {
        this.init();

        const icons = {
            success: 'check-circle-2',
            error: 'alert-circle',
            warning: 'alert-triangle',
            info: 'info',
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i data-lucide="${icons[type] || 'info'}" class="toast-icon"></i>
            <div class="toast-content">
                <div class="toast-title">${Utils.escapeHtml(title)}</div>
                ${message ? `<div class="toast-message">${Utils.escapeHtml(message)}</div>` : ''}
            </div>
            <button type="button" class="toast-close" aria-label="Dismiss notification">
                <i data-lucide="x"></i>
            </button>
        `;

        this.container.appendChild(toast);
        if (typeof lucide !== 'undefined') lucide.createIcons();

        requestAnimationFrame(() => toast.classList.add('show'));

        const close = () => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 220);
        };

        toast.querySelector('.toast-close').addEventListener('click', close);
        if (duration > 0) setTimeout(close, duration);

        return toast;
    },

    success(title, message) { return this.show('success', title, message); },
    error(title, message) { return this.show('error', title, message); },
    warning(title, message) { return this.show('warning', title, message); },
    info(title, message) { return this.show('info', title, message); },
};

/**
 * Accessible Modal System (zero inline styles)
 */
const Modal = {
    show(id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.classList.add('active');
            const closeBtn = modal.querySelector('.modal-close');
            if (closeBtn) closeBtn.focus();
        }
    },

    hide(id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.classList.remove('active');
        }
    },

    confirm(title, messageHtml, onConfirm) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay active';
        overlay.innerHTML = `
            <div class="modal modal-sm" role="alertdialog" aria-modal="true" aria-labelledby="confirmModalTitle">
                <div class="modal-header">
                    <h3 class="modal-title" id="confirmModalTitle">${Utils.escapeHtml(title)}</h3>
                    <button type="button" class="modal-close" aria-label="Close dialog">
                        <i data-lucide="x"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <p>${messageHtml}</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-ghost cancel-btn">Cancel</button>
                    <button type="button" class="btn btn-danger confirm-btn">
                        <i data-lucide="trash-2"></i><span>Delete</span>
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);
        if (typeof lucide !== 'undefined') lucide.createIcons();

        const confirmBtn = overlay.querySelector('.confirm-btn');
        if (confirmBtn) confirmBtn.focus();

        const close = () => {
            overlay.classList.remove('active');
            setTimeout(() => overlay.remove(), 220);
        };

        overlay.querySelector('.modal-close').addEventListener('click', close);
        overlay.querySelector('.cancel-btn').addEventListener('click', close);
        confirmBtn.addEventListener('click', () => {
            close();
            if (onConfirm) onConfirm();
        });
        overlay.addEventListener('click', e => {
            if (e.target === overlay) close();
        });
    },
};

/**
 * Sidebar Management
 */
const Sidebar = {
    init() {
        const toggle = document.getElementById('sidebarToggle');
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('sidebarOverlay');

        if (toggle && sidebar && overlay) {
            toggle.onclick = () => {
                sidebar.classList.toggle('open');
                overlay.classList.toggle('active');
            };
        }

        if (overlay && sidebar) {
            overlay.onclick = () => {
                sidebar.classList.remove('open');
                overlay.classList.remove('active');
            };
        }

        this.setActiveNav();
    },

    setActiveNav() {
        const currentPage = window.location.pathname.split('/').pop() || 'index.html';
        document.querySelectorAll('.nav-item[data-page]').forEach(item => {
            const target = item.getAttribute('data-page');
            item.classList.toggle('active', target === currentPage);
            if (target === currentPage) {
                item.setAttribute('aria-current', 'page');
            } else {
                item.removeAttribute('aria-current');
            }
        });
    },

    closeMobile() {
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('sidebarOverlay');
        if (sidebar) sidebar.classList.remove('open');
        if (overlay) overlay.classList.remove('active');
    },
};

/**
 * Form Validation
 */
const FormValidator = {
    validateField(input) {
        if (!input || input.disabled || input.type === 'hidden') return true;
        const value = input.value.trim();
        const name = input.name;
        let error = '';

        if (input.required && !value) {
            error = 'This field is required';
        } else if (name === 'fullName' && value && value.length < 2) {
            error = 'Name must be at least 2 characters';
        } else if (name === 'email' && value) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) error = 'Enter a valid email address';
        } else if (name === 'phone' && value) {
            const phoneRegex = /^[+]?[0-9]{7,15}$/;
            if (!phoneRegex.test(value)) error = 'Use 7–15 digits (optional leading +)';
        } else if (name === 'marks' && value !== '') {
            const marks = parseFloat(value);
            if (isNaN(marks) || marks < 0 || marks > 100) error = 'Marks must be between 0 and 100';
        } else if (name === 'yearOfStudy' && value !== '') {
            const yr = parseInt(value, 10);
            if (isNaN(yr) || yr < 1 || yr > 6) error = 'Year must be between 1 and 6';
        }

        this.showFieldError(input, error);
        return !error;
    },

    showFieldError(input, message) {
        if (!input) return;
        const group = input.closest('.form-group') || input.parentElement;
        const errorEl = group ? group.querySelector('.form-error') : null;
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.toggle('visible', Boolean(message));
        }
        input.classList.toggle('error', Boolean(message));
        if (message) {
            input.setAttribute('aria-invalid', 'true');
        } else {
            input.removeAttribute('aria-invalid');
        }
    },

    validateForm(form) {
        let valid = true;
        form.querySelectorAll('input, select, textarea').forEach(input => {
            if (!this.validateField(input)) valid = false;
        });
        return valid;
    },

    clearErrors(form) {
        form.querySelectorAll('.form-error').forEach(el => {
            el.classList.remove('visible');
            el.textContent = '';
        });
        form.querySelectorAll('.error').forEach(el => {
            el.classList.remove('error');
            el.removeAttribute('aria-invalid');
        });
    },
};

/**
 * Grade Utilities (preview only — backend is source of truth)
 */
const GradeUtil = {
    calculate(marks) {
        if (marks >= 90) return { grade: 'O', result: 'Excellent' };
        if (marks >= 80) return { grade: 'A+', result: 'Outstanding' };
        if (marks >= 70) return { grade: 'A', result: 'Very Good' };
        if (marks >= 60) return { grade: 'B+', result: 'Good' };
        if (marks >= 50) return { grade: 'B', result: 'Average' };
        if (marks >= 40) return { grade: 'C', result: 'Pass' };
        return { grade: 'F', result: 'Fail' };
    },

    getBadgeClass(grade) {
        const map = {
            'O': 'grade-o', 'A+': 'grade-a-plus', 'A': 'grade-a',
            'B+': 'grade-b-plus', 'B': 'grade-b', 'C': 'grade-c', 'F': 'grade-f'
        };
        return map[grade] || '';
    },

    getStatusClass(result) {
        return result === 'Fail' ? 'fail' : 'pass';
    },
};

/**
 * Utility Functions
 */
const Utils = {
    formatDate(dateStr) {
        if (!dateStr) return '—';
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    },

    formatDateTime(dateStr) {
        if (!dateStr) return '—';
        return new Date(dateStr).toLocaleString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },

    getInitials(name) {
        if (!name) return '?';
        return name.trim().split(/\s+/).map(w => w[0]).join('').toUpperCase().substring(0, 2);
    },

    debounce(fn, delay = 250) {
        let timer;
        return (...args) => {
            clearTimeout(timer);
            timer = setTimeout(() => fn(...args), delay);
        };
    },

    escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str == null ? '' : String(str);
        return div.innerHTML;
    },

    async exportStudentsCsv() {
        try {
            const res = await API.getAllStudents(0, 100, 'studentCode', 'asc');
            const rows = res.data?.content || [];
            if (rows.length === 0) {
                Toast.warning('Export', 'No student records to export.');
                return;
            }
            const headers = ['Student ID', 'Full Name', 'Email', 'Phone', 'Gender', 'DOB', 'Department', 'Course', 'Year', 'Semester', 'Marks', 'Grade', 'Result'];
            const csvEscape = v => `"${String(v ?? '').replace(/"/g, '""')}"`;
            const lines = [
                headers.join(','),
                ...rows.map(s => [
                    s.studentCode, s.fullName, s.email, s.phone, s.gender, s.dateOfBirth,
                    s.department, s.course, s.yearOfStudy, s.semester, s.marks, s.grade, s.result
                ].map(csvEscape).join(','))
            ];
            const blob = new Blob([lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `students-${new Date().toISOString().slice(0, 10)}.csv`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
            Toast.success('Export Complete', `Exported ${rows.length} student records to CSV.`);
        } catch (err) {
            Toast.error('Export Failed', err.data?.message || 'Unable to export student data.');
        }
    },
};

function navigateTo(page) {
    if (page.startsWith('/')) {
        window.location.href = page;
    } else {
        window.location.href = `/pages/${page}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!Auth.enforcePageAccess()) return;

    ChartTheme.applyDefaults();
    Shell.init();
    Theme.syncButtons();

    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // Header search shortcut: press Enter to search on the All Students page, or "/" to focus
    document.querySelectorAll('.header-search input').forEach(input => {
        input.addEventListener('keydown', e => {
            if (e.key === 'Enter' && input.value.trim()) {
                window.location.href = `/pages/students.html?q=${encodeURIComponent(input.value.trim())}`;
            }
        });
    });

    document.addEventListener('keydown', e => {
        const tag = document.activeElement?.tagName;
        if (e.key === '/' && tag !== 'INPUT' && tag !== 'TEXTAREA' && tag !== 'SELECT') {
            const search = document.getElementById('globalSearchInput') || document.getElementById('searchInput');
            if (search) {
                e.preventDefault();
                search.focus();
            }
        } else if (e.key === 'Escape') {
            Menu.close();
            document.querySelectorAll('.modal-overlay.active').forEach(m => {
                if (m.id) Modal.hide(m.id); else m.remove();
            });
            Sidebar.closeMobile();
        }
    });
});
