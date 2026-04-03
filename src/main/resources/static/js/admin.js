/* Admin Dashboard JS */
document.addEventListener('DOMContentLoaded', function() {

    // Sidebar active link
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-link').forEach(function(link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

    // Inventory search
    const searchInput = document.getElementById('inventorySearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const query = this.value.toLowerCase();
            document.querySelectorAll('.inventory-row').forEach(function(row) {
                const data = row.getAttribute('data-search') || '';
                row.style.display = data.includes(query) ? '' : 'none';
            });
        });
    }

    // ── Notification Bell ─────────────────────────────────────────────
    initNotificationBell();
});

/* ── CSRF helper — đọc token từ meta tag, gửi kèm mọi fetch POST ── */
function getCsrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');
    if (token && header) {
        return { [header.content]: token.content };
    }
    return {};
}

function initNotificationBell() {
    const btn      = document.getElementById('notifBellBtn');
    const popover  = document.getElementById('notifPopover');
    const badge    = document.getElementById('notifBadge');
    const markAll  = document.getElementById('notifMarkAllBtn');
    const listEl   = document.getElementById('notifList');

    if (!btn || !popover) return;

    // ── Toggle open/close ──────────────────────────────
    btn.addEventListener('click', function(e) {
        e.stopPropagation();
        const isOpen = popover.classList.contains('open');
        if (!isOpen) {
            openPopover();
        } else {
            closePopover();
        }
    });

    // Close when clicking outside
    document.addEventListener('click', function(e) {
        if (!popover.contains(e.target) && e.target !== btn) {
            closePopover();
        }
    });

    function openPopover() {
        popover.classList.add('open');
        btn.classList.add('active');
        popover.setAttribute('aria-hidden', 'false');
        fetchNotifications();
    }

    function closePopover() {
        popover.classList.remove('open');
        btn.classList.remove('active');
        popover.setAttribute('aria-hidden', 'true');
    }

    // ── Fetch from API ─────────────────────────────────
    function fetchNotifications() {
        listEl.innerHTML = '<div class="notif-loading">Đang tải...</div>';
        fetch('/admin/api/notifications')
            .then(res => res.json())
            .then(data => {
                renderNotifications(data.notifications || []);
                updateBadge(data.unreadCount || 0);
            })
            .catch(() => {
                listEl.innerHTML = '<div class="notif-empty">Không thể tải thông báo.</div>';
            });
    }

    // Update badge on load immediately
    fetch('/admin/api/notifications')
        .then(res => res.json())
        .then(data => updateBadge(data.unreadCount || 0))
        .catch(() => {});

    // ── Update badge ───────────────────────────────────
    function updateBadge(count) {
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }

    // ── Render list ────────────────────────────────────
    function renderNotifications(notifications) {
        if (!notifications || notifications.length === 0) {
            listEl.innerHTML = '<div class="notif-empty">Không có thông báo nào.</div>';
            return;
        }

        listEl.innerHTML = '';
        notifications.forEach(n => {
            const isOrder = n.type.startsWith('ORDER');
            // Admin: order → trang orders chung, stock → trang books chung
            const href = isOrder ? '/admin/orders' : '/admin/books';

            const item = document.createElement('a');
            item.className = 'notif-item' + (!n.isRead ? ' unread' : '');
            item.href = '#';
            item.setAttribute('data-id', n.id);
            item.setAttribute('data-href', href);

            item.innerHTML = `
                <div class="notif-item-icon ${isOrder ? 'notif-icon-order' : 'notif-icon-stock'}">
                    ${isOrder ? orderIcon() : stockIcon()}
                </div>
                <div class="notif-item-body">
                    <div class="notif-item-title">${escHtml(n.title)}</div>
                    <div class="notif-item-msg">${escHtml(n.message)}</div>
                    <div class="notif-item-time">${formatTime(n.createdAt)}</div>
                </div>
                ${!n.isRead ? '<div class="notif-unread-dot"></div>' : ''}
            `;

            // Click: mark read then navigate
            item.addEventListener('click', function(e) {
                e.preventDefault();
                const notifId  = this.getAttribute('data-id');
                const target   = this.getAttribute('data-href');
                markAsRead(notifId, () => {
                    if (target && target !== '#') {
                        window.location.href = target;
                    }
                });
            });

            listEl.appendChild(item);
        });
    }

    // ── Mark single read ───────────────────────────────
    function markAsRead(id, callback) {
        fetch('/admin/api/notifications/' + id + '/read', {
            method: 'POST',
            headers: getCsrfHeaders()
        })
            .then(() => { if (callback) callback(); })
            .catch(() => { if (callback) callback(); });
    }

    // ── Mark all read ──────────────────────────────────
    if (markAll) {
        markAll.addEventListener('click', function() {
            fetch('/admin/api/notifications/mark-all-read', {
                method: 'POST',
                headers: getCsrfHeaders()
            })
                .then(res => res.json())
                .then(() => {
                    // Update UI
                    document.querySelectorAll('.notif-item.unread').forEach(el => {
                        el.classList.remove('unread');
                        const dot = el.querySelector('.notif-unread-dot');
                        if (dot) dot.remove();
                    });
                    updateBadge(0);
                })
                .catch(() => {});
        });
    }

    // ── Helper: SVG icons ──────────────────────────────
    function orderIcon() {
        return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                    <line x1="3" y1="6" x2="21" y2="6"/>
                    <path d="M16 10a4 4 0 0 1-8 0"/>
                </svg>`;
    }
    function stockIcon() {
        return `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
                </svg>`;
    }

    // ── Helper: escape HTML ────────────────────────────
    function escHtml(str) {
        const d = document.createElement('div');
        d.appendChild(document.createTextNode(str || ''));
        return d.innerHTML;
    }

    // ── Helper: relative time ──────────────────────────
    function formatTime(isoStr) {
        if (!isoStr) return '';
        const date = new Date(isoStr);
        const diffMs  = Date.now() - date.getTime();
        const diffMin = Math.floor(diffMs / 60000);
        if (diffMin < 1)   return 'Vừa xong';
        if (diffMin < 60)  return diffMin + ' phút trước';
        const diffH = Math.floor(diffMin / 60);
        if (diffH < 24)    return diffH + ' giờ trước';
        const diffD = Math.floor(diffH / 24);
        if (diffD < 7)     return diffD + ' ngày trước';
        return date.toLocaleDateString('vi-VN');
    }
}
