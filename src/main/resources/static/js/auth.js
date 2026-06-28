/**
 * OA认证与权限模块
 * 提供Token管理、API请求拦截、权限判断、菜单过滤
 * 依赖：无（纯原生JS，直接操作localStorage）
 */
var Auth = (function() {
    'use strict';

    var TOKEN_KEY = 'oa_token';
    var USER_KEY = 'oa_user';
    var MENUS_KEY = 'oa_menus';
    var PERMS_KEY = 'oa_permissions';

    // ============ Token管理 ============

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function removeToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    // ============ 用户信息 ============

    function getUser() {
        var raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    }

    function setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }

    function removeUser() {
        localStorage.removeItem(USER_KEY);
    }

    // ============ 菜单和权限 ============

    function getMenus() {
        var raw = localStorage.getItem(MENUS_KEY);
        return raw ? JSON.parse(raw) : [];
    }

    function setMenus(menus) {
        localStorage.setItem(MENUS_KEY, JSON.stringify(menus));
    }

    function getPermissions() {
        var raw = localStorage.getItem(PERMS_KEY);
        return raw ? JSON.parse(raw) : [];
    }

    function setPermissions(perms) {
        localStorage.setItem(PERMS_KEY, JSON.stringify(perms));
    }

    // ============ 权限判断 ============

    /**
     * 检查当前用户是否拥有指定权限
     * @param {string} permCode 权限编码（如 employee:add）
     * @returns {boolean}
     */
    function hasPermission(permCode) {
        if (!permCode) return true;
        var user = getUser();
        if (!user) return false;
        var roles = user.roles || [];
        // 管理员拥有所有权限
        if (roles.indexOf('admin') !== -1) return true;
        var perms = getPermissions();
        return perms.indexOf(permCode) !== -1;
    }

    /**
     * 检查是否已登录
     * @returns {boolean}
     */
    function isLoggedIn() {
        return !!getToken();
    }

    /**
     * 检查是否拥有指定角色
     * @param {string} roleCode 角色编码
     * @returns {boolean}
     */
    function hasRole(roleCode) {
        var user = getUser();
        if (!user) return false;
        var roles = user.roles || [];
        return roles.indexOf(roleCode) !== -1;
    }

    // ============ API请求封装 ============

    /**
     * 带认证的统一fetch封装
     * 自动携带Authorization头、处理401/403
     * @param {string} url 请求URL
     * @param {object} options fetch选项
     * @returns {Promise}
     */
    function apiFetch(url, options) {
        options = options || {};
        var headers = options.headers || {};
        var token = getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        options.headers = headers;

        return fetch(url, options).then(function(r) {
            if (r.status === 401) {
                logout();
                if (window.location.pathname.indexOf('login.html') === -1) {
                    window.location.href = 'pages/login.html';
                }
                throw new Error('未登录或Token已过期');
            }
            if (r.status === 403) {
                throw new Error('权限不足');
            }
            return r;
        });
    }

    /**
     * apiFetch的JSON版本，自动解析res.code
     * @param {string} url
     * @param {object} options
     * @returns {Promise}
     */
    function apiFetchJSON(url, options) {
        return apiFetch(url, options).then(function(r) {
            if (!r.ok && r.status !== 200) {
                throw new Error('HTTP ' + r.status);
            }
            return r.json();
        }).then(function(res) {
            if (res.code === 401) {
                logout();
                if (window.location.pathname.indexOf('login.html') === -1) {
                    window.location.href = 'pages/login.html';
                }
            }
            return res;
        });
    }

    // ============ 登录/登出 ============

    /**
     * 登录
     * @param {string} username
     * @param {string} password
     * @returns {Promise}
     */
    function login(username, password) {
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username, password: password })
        }).then(function(r) {
            return r.json();
        }).then(function(res) {
            if (res.code === 200) {
                var data = res.data;
                setToken(data.token);
                setUser({
                    userId: data.userId,
                    username: data.username,
                    empId: data.empId,
                    roles: data.roles || []
                });
                setMenus(data.menus || []);
                setPermissions(data.permissions || []);
                return data;
            }
            throw new Error(res.msg || '登录失败');
        });
    }

    /**
     * 退出登录
     */
    function logout() {
        removeToken();
        removeUser();
        localStorage.removeItem(MENUS_KEY);
        localStorage.removeItem(PERMS_KEY);
        // 尝试通知后端
        var token = getToken();
        if (!token) {
            fetch('/api/auth/logout', { method: 'POST' }).catch(function() {});
        }
    }

    /**
     * 从Token恢复用户信息（刷新页面后调用）
     * @returns {Promise}
     */
    function restoreSession() {
        if (!isLoggedIn()) {
            return Promise.reject(new Error('未登录'));
        }
        return apiFetchJSON('/api/auth/me').then(function(res) {
            if (res.code === 200) {
                var data = res.data;
                setUser({
                    userId: data.userId,
                    username: data.username,
                    empId: data.empId,
                    roles: data.roles || []
                });
                setMenus(data.menus || []);
                setPermissions(data.permissions || []);
                return data;
            }
            throw new Error(res.msg);
        });
    }

    /**
     * 获取当前用户的菜单ID集合（用于侧边栏过滤）
     * 根据后端返回的sys_menu数据，提取menu_name映射
     * @returns {Array} 菜单名称数组
     */
    function getAccessibleMenuNames() {
        var menus = getMenus();
        var names = [];
        for (var i = 0; i < menus.length; i++) {
            if (menus[i].menuName) {
                names.push(menus[i].menuName);
            }
        }
        return names;
    }

    /**
     * 判断菜单名称是否在当前用户可访问范围内
     * @param {string} menuName
     * @returns {boolean}
     */
    function canAccessMenu(menuName) {
        var user = getUser();
        if (!user) return false;
        var roles = user.roles || [];
        // 管理员拥有全部菜单
        if (roles.indexOf('admin') !== -1) return true;
        var names = getAccessibleMenuNames();
        return names.indexOf(menuName) !== -1;
    }

    // ============ 初始化 ============

    /**
     * 页面加载时检查登录状态
     * 未登录则跳转到登录页
     * @returns {boolean} 是否已登录
     */
    function requireAuth() {
        if (!isLoggedIn()) {
            window.location.href = 'pages/login.html';
            return false;
        }
        return true;
    }

    /**
     * 安装全局fetch拦截器，自动为/api/请求添加Authorization头
     * 确保已有代码中所有fetch调用自动携带Token
     */
    function setupFetchInterceptor() {
        var originalFetch = window.fetch;
        window.fetch = function(url, options) {
            options = options || {};
            var urlStr = typeof url === 'string' ? url : url.url;
            // 仅对同源/api/请求添加Token
            if (urlStr && urlStr.indexOf('/api/') !== -1 && urlStr.indexOf('http') !== 0) {
                var token = getToken();
                if (token) {
                    var headers = options.headers || {};
                    // 支持Headers对象和普通对象
                    if (headers instanceof Headers) {
                        if (!headers.has('Authorization')) {
                            headers.set('Authorization', 'Bearer ' + token);
                        }
                    } else {
                        if (!headers['Authorization']) {
                            headers['Authorization'] = 'Bearer ' + token;
                        }
                    }
                    options.headers = headers;
                }
            }
            return originalFetch.call(window, url, options).then(function(r) {
                if (r.status === 401) {
                    logout();
                    if (window.location.pathname.indexOf('login.html') === -1) {
                        window.location.href = 'pages/login.html';
                    }
                }
                return r;
            });
        };
    }

    // 自动安装拦截器
    setupFetchInterceptor();

    // 公开API
    return {
        getToken: getToken,
        setToken: setToken,
        removeToken: removeToken,
        getUser: getUser,
        setUser: setUser,
        getMenus: getMenus,
        setMenus: setMenus,
        getPermissions: getPermissions,
        setPermissions: setPermissions,
        hasPermission: hasPermission,
        isLoggedIn: isLoggedIn,
        hasRole: hasRole,
        apiFetch: apiFetch,
        apiFetchJSON: apiFetchJSON,
        login: login,
        logout: logout,
        restoreSession: restoreSession,
        requireAuth: requireAuth,
        canAccessMenu: canAccessMenu,
        getAccessibleMenuNames: getAccessibleMenuNames
    };
})();
