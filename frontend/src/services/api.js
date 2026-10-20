/**
 * DOSTATOK API Service Client
 * Centralized fetch handler providing structured promises and standardized error parsing.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
let sessionExpiredNotified = false;

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const res = await fetch(url, config);
    const json = await res.json().catch(() => null);

    if (!res.ok) {
      if (res.status === 401 && endpoint !== '/auth/login' && endpoint !== '/auth/logout' && !sessionExpiredNotified) {
        sessionExpiredNotified = true;
        window.dispatchEvent(new Event('dostatok:session-expired'));
      }
      const errMsg = json?.error || `HTTP ${res.status}: ${res.statusText}`;
      const err = new Error(errMsg);
      err.statusCode = res.status;
      err.data = json;
      throw err;
    }

    if (endpoint === '/auth/login' || endpoint === '/auth/register') sessionExpiredNotified = false;

    return json?.data !== undefined ? json.data : json;
  } catch (error) {
    if (error.statusCode) {
      throw error;
    }
    throw new Error(`Server connection failed (${error.message}). Is DOSTATOK backend running on port 8080?`);
  }
}

export const api = {
  getMe: () => request('/auth/me'),
  updateProfile: (data) => request('/auth/profile', { method: 'PUT', body: JSON.stringify(data) }),
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  logout: () => request('/auth/logout', { method: 'POST' }),

  // Health
  getHealth: () => request('/health'),

  // Dashboard
  getDashboardSummary: () => request('/dashboard/summary'),

  // Accounts
  getAccounts: () => request('/accounts'),
  getAccount: (id) => request(`/accounts/${id}`),
  createAccount: (data) => request('/accounts', { method: 'POST', body: JSON.stringify(data) }),
  updateAccount: (id, data) => request(`/accounts/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteAccount: (id) => request(`/accounts/${id}`, { method: 'DELETE' }),
  deposit: (id, data) => request(`/accounts/${id}/deposit`, { method: 'POST', body: JSON.stringify(data) }),
  withdraw: (id, data) => request(`/accounts/${id}/withdraw`, { method: 'POST', body: JSON.stringify(data) }),
  transfer: (data) => request('/accounts/transfer', { method: 'POST', body: JSON.stringify(data) }),

  // Transactions
  getTransactions: () => request('/transactions'),
  getAccountTransactions: (id) => request(`/accounts/${id}/transactions`),

  // Income
  getIncome: () => request('/income'),
  createIncome: (data) => request('/income', { method: 'POST', body: JSON.stringify(data) }),
  updateIncome: (id, data) => request(`/income/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteIncome: (id) => request(`/income/${id}`, { method: 'DELETE' }),

  // Expenses
  getExpenses: () => request('/expenses'),
  createExpense: (data) => request('/expenses', { method: 'POST', body: JSON.stringify(data) }),
  updateExpense: (id, data) => request(`/expenses/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteExpense: (id) => request(`/expenses/${id}`, { method: 'DELETE' }),

  // Investments
  getInvestments: () => request('/investments'),
  createInvestment: (data) => request('/investments', { method: 'POST', body: JSON.stringify(data) }),
  updateInvestment: (id, data) => request(`/investments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteInvestment: (id) => request(`/investments/${id}`, { method: 'DELETE' }),

  // Assets
  getAssets: () => request('/assets'),
  createAsset: (data) => request('/assets', { method: 'POST', body: JSON.stringify(data) }),
  updateAsset: (id, data) => request(`/assets/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteAsset: (id) => request(`/assets/${id}`, { method: 'DELETE' }),

  // Loans
  getLoans: () => request('/loans'),
  createLoan: (data) => request('/loans', { method: 'POST', body: JSON.stringify(data) }),
  updateLoan: (id, data) => request(`/loans/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteLoan: (id) => request(`/loans/${id}`, { method: 'DELETE' }),
  repayLoan: (id, data) => request(`/loans/${id}/repay`, { method: 'POST', body: JSON.stringify(data) }),
  getLoanRepayments: (id) => request(`/loans/${id}/repayments`),

  // Financial Goals
  getGoals: () => request('/goals'),
  createGoal: (data) => request('/goals', { method: 'POST', body: JSON.stringify(data) }),
  updateGoal: (id, data) => request(`/goals/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteGoal: (id) => request(`/goals/${id}`, { method: 'DELETE' }),
  contributeToGoal: (id, data) => request(`/goals/${id}/contribute`, { method: 'POST', body: JSON.stringify(data) }),
};

