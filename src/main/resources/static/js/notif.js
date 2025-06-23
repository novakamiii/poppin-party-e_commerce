document.addEventListener('DOMContentLoaded', () => {
  const notifButton = document.querySelector('.notif-button');
  const notifCounter = document.getElementById('notification-counter');
  const notifContent = document.getElementById('notif-content');
  const markAllReadBtn = document.getElementById('mark-all-read');

  // Toggle dropdown visibility
  notifButton?.addEventListener('click', (e) => {
    e.stopPropagation();
    const dropdown = notifButton.querySelector('.notif-dropdown');
    if (dropdown) {
      dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
    }
  });

  // Close dropdown when clicking outside
  document.addEventListener('click', () => {
    const dropdown = document.querySelector('.notif-dropdown');
    if (dropdown) dropdown.style.display = 'none';
  });

  // Load notifications
  async function loadNotifications() {
    try {
      const response = await fetch('/api/notifications', {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      updateNotificationCounter(data.unreadCount);
      renderNotifications(data.notifications);
    } catch (error) {
      console.error("Notification error:", error);
      showEmptyNotifications("Error loading notifications");
    }
  }

  // Show empty state for notifications
  function showEmptyNotifications(message = "No notifications yet") {
    notifContent.innerHTML = `
      <div class="no-notifications">
        <img src="/img/no-notifications.png" alt="No notifications" class="no-notifications-image">
        <p class="no-notifications-message">${message}</p>
      </div>
    `;

    // Hide "Mark all as read" button when empty
    if (markAllReadBtn) {
      markAllReadBtn.style.display = 'none';
    }
  }

  // Render notifications
  function renderNotifications(notifications) {
    if (!notifications || notifications.length === 0) {
      showEmptyNotifications();
      return;
    }

    // Show "Mark all as read" button when there are notifications
    if (markAllReadBtn) {
      markAllReadBtn.style.display = 'block';
    }

    notifContent.innerHTML = notifications.map(notif => `
      <div class="notification-item ${notif.read ? '' : 'unread'}" data-id="${notif.id}"
           data-order-id="${notif.orderId}" data-status="${notif.status}">
        <div class="notification-message">${notif.message || 'No message'}</div>
        ${notif.trackingNumber ? `
          <div class="notification-details">
            Tracking #: ${notif.trackingNumber}<br>
            Status: ${notif.status || 'N/A'}
          </div>
        ` : ''}
        <div class="notification-time">
          ${notif.createdAt ? new Date(notif.createdAt).toLocaleString() : ''}
        </div>
      </div>
    `).join('');
  }

  // Update counter
  function updateNotificationCounter(count) {
    if (!notifCounter) return;
    notifCounter.textContent = count;
    notifCounter.style.display = count > 0 ? 'block' : 'none';
  }

  // Mark notification as read
  async function markAsRead(notificationId) {
    try {
      await fetch(`/api/notifications/${notificationId}/read`, {
        method: 'POST',
        credentials: 'include'
      });
      loadNotifications();
    } catch (error) {
      console.error("Error marking as read:", error);
    }
  }

  // Event listeners
  notifContent?.addEventListener('click', (e) => {
    const notifItem = e.target.closest('.notification-item');
    if (notifItem) {
      const orderId = notifItem.dataset.orderId;
      const status = notifItem.dataset.status;

      // Mark as read
      markAsRead(notifItem.dataset.id);

      // Redirect to order status if orderId exists
      if (orderId) {
        window.location.href = `/order/status?order=${orderId}&status=${status}`;
      }
    }
  });

  markAllReadBtn?.addEventListener('click', async () => {
    try {
      await fetch('/api/notifications/mark-all-read', {
        method: 'POST',
        credentials: 'include'
      });
      loadNotifications();
    } catch (error) {
      console.error("Error marking all as read:", error);
    }
  });

  // Initial load
  loadNotifications();

  // Refresh every 30 seconds
  const pollInterval = setInterval(loadNotifications, 30000);

  // Cleanup on page navigation
  window.addEventListener('beforeunload', () => {
    clearInterval(pollInterval);
  });
});
