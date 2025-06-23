/**
 * Handles dashboard navigation and tab switching logic for the account page.
 *
 * - Adds click event listeners to sidebar menu links to show/hide dashboard sections.
 * - Updates the active state of sidebar links and dashboard sections.
 * - When the "Order Status" section is activated, initializes the "to-ship" tab as active and sets up tab switching logic.
 * - Ensures only one order status tab and its corresponding content are active at a time.
 * - On initial page load, shows the "profile" section by default.
 * - If the URL hash is "#order-status", initializes the "to-ship" tab as active.
 *
 * Elements involved:
 *   - `.dashboard-menu a`: Sidebar navigation links.
 *   - `.dashboard-section`: Main dashboard content sections.
 *   - `#order-status .status-tab`: Tabs within the order status section.
 *   - `#order-status .order-item-list`: Content lists for each order status tab.
 *
 * Assumes:
 *   - Each sidebar link's `href` attribute matches the `id` of its target section.
 *   - Order status tabs use a `data-tab` attribute matching the `id` of their content.
 */
const dashboardMenuLinks = document.querySelectorAll('.dashboard-menu a');
        const dashboardSections = document.querySelectorAll('.dashboard-section');
        const orderStatusTabs = document.querySelectorAll('#order-status .status-tab');
        const orderItemLists = document.querySelectorAll('#order-status .order-item-list');

        dashboardMenuLinks.forEach(link => {
            link.addEventListener('click', function(event) {
                event.preventDefault();
                const targetId = this.getAttribute('href').substring(1); // Remove the '#'

                // Deactivate all sections
                dashboardSections.forEach(section => {
                    section.classList.remove('active');
                });

                // Activate the target section
                const targetSection = document.getElementById(targetId);
                if (targetSection) {
                    targetSection.classList.add('active');
                }

                // Update active link in the sidebar (optional)
                dashboardMenuLinks.forEach(item => item.classList.remove('active'));
                this.classList.add('active');

                // If Order Status is activated, ensure the correct tab content is shown
                if (targetId === 'order-status') {
                    // Initially show 'to-ship' and make its tab active
                    orderStatusTabs.forEach(tab => tab.classList.remove('active'));
                    orderItemLists.forEach(list => list.style.display = 'none');

                    const initialTab = document.querySelector('#order-status .status-tab[data-tab="to-ship"]');
                    const initialContent = document.getElementById('to-ship');

                    if (initialTab && initialContent) {
                        initialTab.classList.add('active');
                        initialContent.style.display = 'block';
                    }

                    // Add event listeners for order status tabs (if not already present)
                    orderStatusTabs.forEach(tab => {
                        tab.addEventListener('click', () => {
                            orderStatusTabs.forEach(t => t.classList.remove('active'));
                            orderItemLists.forEach(content => content.style.display = 'none');

                            tab.classList.add('active');
                            const targetTab = tab.getAttribute('data-tab');
                            const targetContent = document.getElementById(targetTab);
                            if (targetContent) {
                                targetContent.style.display = 'block';
                            }
                        });
                    });
                }
            });
        });

        // Initially show the 'profile' section
        const initialSection = document.getElementById('profile');
        if (initialSection) {
            initialSection.classList.add('active');
        }

        // Initialize the 'to-ship' tab as active on load (if on order status)
        if (window.location.hash === '#order-status') {
            const initialTab = document.querySelector('#order-status .status-tab[data-tab="to-ship"]');
            const initialContent = document.getElementById('to-ship');
            if (initialTab && initialContent) {
                initialTab.classList.add('active');
                initialContent.style.display = 'block';
            }
        }