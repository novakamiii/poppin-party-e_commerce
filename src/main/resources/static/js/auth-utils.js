export const checkAuth = async (actionName) => {
  try {
    const response = await fetch('/api/auth/check', {
      credentials: 'include',
      headers: { 'Accept': 'application/json' }
    });

    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      if (!data.authenticated) {
        alert(`You need to be logged in to ${actionName}!`);
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
      }
      return false;
    }

    const data = await response.json();
    return data.authenticated === true;
  } catch (error) {
    console.error("Auth check failed:", error);
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
