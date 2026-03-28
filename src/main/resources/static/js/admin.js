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
});
