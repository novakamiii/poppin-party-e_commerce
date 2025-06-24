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