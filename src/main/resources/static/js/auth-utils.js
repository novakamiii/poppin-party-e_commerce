export const checkAuth = async (actionName) => {
  try {
    const response = await fetch('/api/auth/check', {
      credentials: 'include'
    });

    if (!response.ok) {
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('text/html')) {
        // Server returned HTML (login page) instead of JSON
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
        return false;
      }
      throw new Error('Not authenticated');
    }
    return true;
  } catch (error) {
    alert(`You need to be logged in to ${actionName}!`);
    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    return false;
  }
};

export const setupAuthProtectedElements = () => {
  // Add click handlers to all protected elements
  document.querySelectorAll('[data-protected]').forEach(element => {
    element.addEventListener('click', async (e) => {
      const actionName = element.dataset.protectedName || 'this feature';
      const isAuthenticated = await checkAuth(actionName);

      if (!isAuthenticated) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });
};

export const authWrapper = async (actionName, callback) => {
  const isAuthenticated = await checkAuth(actionName);
  if (isAuthenticated) {
    callback();
  }
};
