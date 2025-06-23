/**
 * Checks if the user is authenticated by making a request to the server.
 * If not authenticated, redirects to the login page with a redirect parameter.
 * Alerts the user if authentication is required for the given action.
 *
 * @async
 * @param {string} actionName - The name of the action requiring authentication (used in alert message).
 * @returns {Promise<boolean>} - Resolves to true if authenticated, false otherwise.
 */

/**
 * Sets up click event handlers on all elements with the [data-protected] attribute.
 * When clicked, checks authentication before allowing the action to proceed.
 * Prevents default action and propagation if not authenticated.
 */
 
/**
 * Wraps a callback function with an authentication check.
 * Executes the callback only if the user is authenticated.
 *
 * @async
 * @param {string} actionName - The name of the action requiring authentication (used in alert message).
 * @param {Function} callback - The function to execute if authenticated.
 * @returns {Promise<void>}
 */
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
    alert(`You need to be logged in to ${actionName}`);
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
