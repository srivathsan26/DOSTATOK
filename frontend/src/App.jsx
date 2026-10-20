import React, { useState, useEffect, useRef, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Modal from './components/Modal';
import DashboardPage from './pages/DashboardPage';
import AccountsPage from './pages/AccountsPage';
import CashflowPage from './pages/CashflowPage';
import InvestmentsPage from './pages/InvestmentsPage';
import AssetsLiabilitiesPage from './pages/AssetsLiabilitiesPage';
import GoalsPage from './pages/GoalsPage';
import ProfilePage from './pages/ProfilePage';
import coinLogo from './assets/dostatok-coin.png';
import { api } from './services/api';
import { formatINR } from './utils/formatters';
import { CheckCircle2, AlertCircle } from 'lucide-react';

function validateFormEvent(event) {
  const form = event.currentTarget instanceof HTMLFormElement
    ? event.currentTarget
    : event.currentTarget.closest('.modal-card')?.querySelector('form');
  return !form || form.reportValidity();
}

const screenTitles = {
  profile: ['Profile', 'Manage your personal account details.'],
  accounts: ['Accounts', 'Manage your bank accounts and review the latest 100 ledger records.'],
  cashflow: ['Income & expenses', 'Review recorded inflows and outflows by category.'],
  investments: ['Investments', 'Track current valuations and recorded cost basis.'],
  assets_liabilities: ['Assets & loans', 'Review physical assets and outstanding loan balances.'],
  goals: ['Financial goals', 'Track progress toward the targets you have set.'],
};

const TAB_PATHS = {
  profile: '/profile',
  dashboard: '/dashboard',
  accounts: '/accounts',
  cashflow: '/cashflow',
  investments: '/investments',
  assets_liabilities: '/assets-liabilities',
  goals: '/goals',
};
const PATH_TABS = Object.fromEntries(Object.entries(TAB_PATHS).map(([tab, path]) => [path, tab]));

function FinanceApp({ profile, onProfileUpdate, onLogout }) {
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem('dostatok-theme') === 'dark' ? 'dark' : 'light';
    } catch {
      return 'light';
    }
  });
  const [currentTab, setCurrentTab] = useState(() => profile?.profileComplete
    ? PATH_TABS[window.location.pathname] || 'dashboard'
    : 'profile');
  const [profileSaving, setProfileSaving] = useState(false);
  const [serverOnline, setServerOnline] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [toast, setToast] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const submittingLock = useRef(false);

  const runSubmission = async (handler, event) => {
    if (submittingLock.current || !validateFormEvent(event)) return;
    submittingLock.current = true;
    setSubmitting(true);
    try {
      await handler(event);
    } finally {
      submittingLock.current = false;
      setSubmitting(false);
    }
  };

  // Core Financial State
  const [summary, setSummary] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [incomeList, setIncomeList] = useState([]);
  const [expenseList, setExpenseList] = useState([]);
  const [investments, setInvestments] = useState([]);
  const [assets, setAssets] = useState([]);
  const [loans, setLoans] = useState([]);
  const [goals, setGoals] = useState([]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try { localStorage.setItem('dostatok-theme', theme); } catch { /* Theme still updates for this session. */ }
  }, [theme]);

  const navigate = (tab) => {
    if (!profile?.profileComplete && tab !== 'profile') tab = 'profile';
    const nextPath = TAB_PATHS[tab] || TAB_PATHS.dashboard;
    if (window.location.pathname !== nextPath) window.history.pushState({ tab }, '', nextPath);
    setCurrentTab(TAB_PATHS[tab] ? tab : 'dashboard');
  };

  useEffect(() => {
    const syncRoute = () => {
      const tab = profile?.profileComplete ? PATH_TABS[window.location.pathname] : 'profile';
      if (tab) {
        setCurrentTab(tab);
      } else {
        window.history.replaceState({ tab: 'dashboard' }, '', TAB_PATHS.dashboard);
        setCurrentTab('dashboard');
      }
    };
    window.addEventListener('popstate', syncRoute);
    if (!profile?.profileComplete) {
      window.history.replaceState({ tab: 'profile' }, '', TAB_PATHS.profile);
      setCurrentTab('profile');
    } else if (!PATH_TABS[window.location.pathname]) {
      window.history.replaceState({ tab: 'dashboard' }, '', TAB_PATHS.dashboard);
      setCurrentTab('dashboard');
    }
    return () => window.removeEventListener('popstate', syncRoute);
  }, [profile?.profileComplete]);

  useEffect(() => {
    const handleExpiredSession = () => onLogout();
    window.addEventListener('dostatok:session-expired', handleExpiredSession);
    return () => window.removeEventListener('dostatok:session-expired', handleExpiredSession);
  }, [onLogout]);

  // Modal States
  const [depositModal, setDepositModal] = useState({ open: false, accountId: '', amount: '', category: 'Deposit', description: '' });
  const [withdrawModal, setWithdrawModal] = useState({ open: false, accountId: '', amount: '', category: 'Withdrawal', description: '' });
  const [createAccountModal, setCreateAccountModal] = useState({ open: false, accountNumber: '', accountName: '', accountType: 'SAVINGS', bankName: '', initialBalance: '', minimumBalance: '1000', overdraftLimit: '50000', interestRate: '3.5' });
  const [transferModal, setTransferModal] = useState({ open: false, fromAccountId: '', toAccountId: '', amount: '', description: '' });
  const [addIncomeModal, setAddIncomeModal] = useState({ open: false, title: '', amount: '', category: 'Salary', source: '', incomeDate: '', accountId: '', updateAccount: true, notes: '' });
  const [addExpenseModal, setAddExpenseModal] = useState({ open: false, title: '', amount: '', category: 'Food', paymentMethod: 'UPI', expenseDate: '', accountId: '', deductFromAccount: true, notes: '' });
  const [addInvestmentModal, setAddInvestmentModal] = useState({ open: false, name: '', investmentType: 'Mutual Fund', investedAmount: '', currentValue: '', purchaseDate: '', notes: '' });
  const [addAssetModal, setAddAssetModal] = useState({ open: false, name: '', assetCategory: 'MOVABLE', purchaseValue: '', currentValue: '', acquisitionDate: '', notes: '' });
  const [addLoanModal, setAddLoanModal] = useState({ open: false, loanName: '', lender: '', principalAmount: '', interestRate: '8.5', tenureMonths: '60', startDate: '' });
  const [repayLoanModal, setRepayLoanModal] = useState({ open: false, loan: null, amount: '', accountId: '', notes: 'Monthly EMI installment' });
  const [createGoalModal, setCreateGoalModal] = useState({ open: false, title: '', targetAmount: '', targetDate: '', category: 'Savings' });
  const [contributeGoalModal, setContributeGoalModal] = useState({ open: false, goal: null, amount: '', accountId: '' });

  // Show Toast Helper
  const showToast = useCallback((type, message) => {
    setToast({ type, message });
  }, []);

  useEffect(() => {
    if (!toast) return undefined;
    const timer = window.setTimeout(() => setToast(null), 4500);
    return () => window.clearTimeout(timer);
  }, [toast]);

  // Refresh all application data from backend
  const refreshAllData = useCallback(async () => {
    try {
      const [sumData, accData, txData, incData, expData, invData, assData, loanData, goalData] = await Promise.all([
        api.getDashboardSummary(),
        api.getAccounts(),
        api.getTransactions(),
        api.getIncome(),
        api.getExpenses(),
        api.getInvestments(),
        api.getAssets(),
        api.getLoans(),
        api.getGoals()
      ]);

      setSummary(sumData);
      setAccounts(accData || []);
      setTransactions(txData || []);
      setIncomeList(incData || []);
      setExpenseList(expData || []);
      setInvestments(invData || []);
      setAssets(assData || []);
      setLoans(loanData || []);
      setGoals(goalData || []);
      setServerOnline(true);
      setLoadError('');
    } catch (err) {
      console.error('Failed to load financial data:', err);
      setServerOnline(false);
      if (err.statusCode === 401) return;
      setLoadError(err.message || 'Could not load your financial information.');
      showToast('error', err.message);
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    // This starts independent API reads; state updates happen after their network responses.
    // oxlint-disable-next-line react/set-state-in-effect
    refreshAllData();
  }, [refreshAllData]);

  // Set default account when modal opens
  const openDeposit = (accId = null) => {
    const id = accId || (accounts.length > 0 ? accounts[0].id : '');
    setDepositModal({ open: true, accountId: id, amount: '', category: 'Deposit', description: '' });
  };

  const openWithdraw = (accId = null) => {
    const id = accId || (accounts.length > 0 ? accounts[0].id : '');
    setWithdrawModal({ open: true, accountId: id, amount: '', category: 'Withdrawal', description: '' });
  };

  // Deposit Submit
  const handleDepositSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    if (!depositModal.accountId || !depositModal.amount || Number(depositModal.amount) <= 0) {
      showToast('error', 'Please enter a valid positive deposit amount.');
      return;
    }
    try {
      await api.deposit(depositModal.accountId, {
        amount: Number(depositModal.amount),
        category: depositModal.category,
        description: depositModal.description || 'Bank deposit'
      });
      showToast('success', `Deposited ${formatINR(depositModal.amount)} successfully!`);
      setDepositModal({ ...depositModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Withdraw Submit
  const handleWithdrawSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    if (!withdrawModal.accountId || !withdrawModal.amount || Number(withdrawModal.amount) <= 0) {
      showToast('error', 'Please enter a valid positive withdrawal amount.');
      return;
    }
    try {
      await api.withdraw(withdrawModal.accountId, {
        amount: Number(withdrawModal.amount),
        category: withdrawModal.category,
        description: withdrawModal.description || 'Bank withdrawal'
      });
      showToast('success', `Withdrew ${formatINR(withdrawModal.amount)} successfully!`);
      setWithdrawModal({ ...withdrawModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Create Account Submit
  const handleCreateAccountSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createAccount({
        accountNumber: createAccountModal.accountNumber,
        accountName: createAccountModal.accountName,
        accountType: createAccountModal.accountType,
        bankName: createAccountModal.bankName,
        initialBalance: Number(createAccountModal.initialBalance || 0),
        minimumBalance: Number(createAccountModal.minimumBalance || 0),
        overdraftLimit: Number(createAccountModal.overdraftLimit || 0),
        interestRate: Number(createAccountModal.interestRate || 0)
      });
      showToast('success', 'Account created successfully!');
      setCreateAccountModal({ ...createAccountModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Delete Account
  const handleDeleteAccount = async (id, name, balance) => {
    if (Number(balance) > 0) {
      showToast('error', `Cannot delete account with positive balance (${formatINR(balance)}). Withdraw funds first.`);
      return;
    }
    if (!window.confirm(`Are you sure you want to delete "${name}"?`)) return;
    try {
      await api.deleteAccount(id);
      showToast('success', 'Account deleted successfully.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Transfer Submit
  const handleTransferSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    if (transferModal.fromAccountId === transferModal.toAccountId) {
      showToast('error', 'Source and destination accounts must be different.');
      return;
    }
    try {
      await api.transfer({
        fromAccountId: Number(transferModal.fromAccountId),
        toAccountId: Number(transferModal.toAccountId),
        amount: Number(transferModal.amount),
        description: transferModal.description
      });
      showToast('success', `Transferred ${formatINR(transferModal.amount)} successfully!`);
      setTransferModal({ ...transferModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Add Income Submit
  const handleAddIncomeSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createIncome({
        title: addIncomeModal.title,
        amount: Number(addIncomeModal.amount),
        category: addIncomeModal.category,
        source: addIncomeModal.source,
        incomeDate: addIncomeModal.incomeDate || new Date().toISOString().split('T')[0],
        accountId: addIncomeModal.accountId ? Number(addIncomeModal.accountId) : null,
        updateAccount: addIncomeModal.updateAccount,
        notes: addIncomeModal.notes
      });
      showToast('success', 'Income record saved successfully!');
      setAddIncomeModal({ ...addIncomeModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Add Expense Submit
  const handleAddExpenseSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createExpense({
        title: addExpenseModal.title,
        amount: Number(addExpenseModal.amount),
        category: addExpenseModal.category,
        paymentMethod: addExpenseModal.paymentMethod,
        expenseDate: addExpenseModal.expenseDate || new Date().toISOString().split('T')[0],
        accountId: addExpenseModal.accountId ? Number(addExpenseModal.accountId) : null,
        deductFromAccount: addExpenseModal.deductFromAccount,
        notes: addExpenseModal.notes
      });
      showToast('success', 'Expense record recorded successfully!');
      setAddExpenseModal({ ...addExpenseModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Add Investment Submit
  const handleAddInvestmentSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createInvestment({
        name: addInvestmentModal.name,
        investmentType: addInvestmentModal.investmentType,
        investedAmount: Number(addInvestmentModal.investedAmount),
        currentValue: Number(addInvestmentModal.currentValue || addInvestmentModal.investedAmount),
        purchaseDate: addInvestmentModal.purchaseDate || new Date().toISOString().split('T')[0],
        notes: addInvestmentModal.notes
      });
      showToast('success', 'Investment holding recorded!');
      setAddInvestmentModal({ ...addInvestmentModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Add Asset Submit
  const handleAddAssetSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createAsset({
        name: addAssetModal.name,
        assetCategory: addAssetModal.assetCategory,
        purchaseValue: Number(addAssetModal.purchaseValue),
        currentValue: Number(addAssetModal.currentValue || addAssetModal.purchaseValue),
        acquisitionDate: addAssetModal.acquisitionDate || new Date().toISOString().split('T')[0],
        notes: addAssetModal.notes
      });
      showToast('success', 'Physical capital asset recorded!');
      setAddAssetModal({ ...addAssetModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Add Loan Submit
  const handleAddLoanSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createLoan({
        loanName: addLoanModal.loanName,
        lender: addLoanModal.lender,
        principalAmount: Number(addLoanModal.principalAmount),
        interestRate: Number(addLoanModal.interestRate),
        tenureMonths: Number(addLoanModal.tenureMonths),
        startDate: addLoanModal.startDate || new Date().toISOString().split('T')[0]
      });
      showToast('success', 'Loan liability registered!');
      setAddLoanModal({ ...addLoanModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Repay Loan Submit
  const handleRepayLoanSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    if (!repayLoanModal.loan) return;
    try {
      await api.repayLoan(repayLoanModal.loan.id, {
        amount: Number(repayLoanModal.amount),
        accountId: repayLoanModal.accountId ? Number(repayLoanModal.accountId) : null,
        notes: repayLoanModal.notes
      });
      showToast('success', `Repayment of ${formatINR(repayLoanModal.amount)} recorded!`);
      setRepayLoanModal({ ...repayLoanModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Create Goal Submit
  const handleCreateGoalSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    try {
      await api.createGoal({
        title: createGoalModal.title,
        targetAmount: Number(createGoalModal.targetAmount),
        targetDate: createGoalModal.targetDate || new Date().toISOString().split('T')[0],
        category: createGoalModal.category
      });
      showToast('success', 'Financial goal created!');
      setCreateGoalModal({ ...createGoalModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Contribute Goal Submit
  const handleContributeGoalSubmit = async (e) => {
    e.preventDefault();
    if (!validateFormEvent(e)) return;
    if (!contributeGoalModal.goal) return;
    try {
      await api.contributeToGoal(contributeGoalModal.goal.id, {
        amount: Number(contributeGoalModal.amount),
        accountId: contributeGoalModal.accountId ? Number(contributeGoalModal.accountId) : null
      });
      showToast('success', `Contributed ${formatINR(contributeGoalModal.amount)} to goal!`);
      setContributeGoalModal({ ...contributeGoalModal, open: false });
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  // Generic Deletions
  const handleDeleteIncome = async (id, title) => {
    if (!window.confirm(`Delete income "${title}"?`)) return;
    try {
      await api.deleteIncome(id);
      showToast('success', 'Income record deleted.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleDeleteExpense = async (id, title) => {
    if (!window.confirm(`Delete expense "${title}"?`)) return;
    try {
      await api.deleteExpense(id);
      showToast('success', 'Expense record deleted.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleDeleteInvestment = async (id, name) => {
    if (!window.confirm(`Delete investment "${name}"?`)) return;
    try {
      await api.deleteInvestment(id);
      showToast('success', 'Investment holding removed.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleDeleteAsset = async (id, name) => {
    if (!window.confirm(`Delete asset "${name}"?`)) return;
    try {
      await api.deleteAsset(id);
      showToast('success', 'Asset record removed.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleDeleteLoan = async (id, name) => {
    if (!window.confirm(`Delete loan facility "${name}"?`)) return;
    try {
      await api.deleteLoan(id);
      showToast('success', 'Loan facility deleted.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleDeleteGoal = async (id, title) => {
    if (!window.confirm(`Delete goal "${title}"?`)) return;
    try {
      await api.deleteGoal(id);
      showToast('success', 'Goal deleted.');
      await refreshAllData();
    } catch (err) {
      showToast('error', err.message);
    }
  };

  return (
    <div className="app-container">
      {/* Toast Notification */}
      {toast && (
        <div className="toast-container" aria-live={toast.type === 'error' ? 'assertive' : 'polite'}>
          <div className={`toast ${toast.type === 'success' ? 'toast-success' : 'toast-error'}`} role={toast.type === 'error' ? 'alert' : 'status'}>
            {toast.type === 'success' ? <CheckCircle2 size={18} color="var(--color-positive)" /> : <AlertCircle size={18} color="var(--color-negative)" />}
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      {/* Sidebar */}
      <div className="main-content">
        <div className="header-shell">
        <Header
          serverOnline={serverOnline}
          profile={profile}
          onLogout={onLogout}
          onProfileClick={() => navigate('profile')}
          theme={theme}
          onToggleTheme={() => setTheme(theme === 'light' ? 'dark' : 'light')}
        />
        <Sidebar currentTab={currentTab} onSelectTab={navigate} />
        </div>

        <main className="page-body" id="main-content" tabIndex="-1">
          <div className="page-transition" key={currentTab}>
          {currentTab !== 'dashboard' && currentTab !== 'profile' && (
            <section className="screen-heading" aria-labelledby="screen-title">
              <p className="eyebrow">DOSTATOK · FINANCES</p>
              <h1 id="screen-title">{screenTitles[currentTab]?.[0]}</h1>
              <p>{screenTitles[currentTab]?.[1]}</p>
            </section>
          )}
          {currentTab === 'profile' && (
            <ProfilePage
              profile={profile}
              saving={profileSaving}
              onSave={async (data) => {
                setProfileSaving(true);
                try {
                  const updatedProfile = await api.updateProfile(data);
                  onProfileUpdate(updatedProfile);
                  showToast('success', 'Profile saved.');
                } finally {
                  setProfileSaving(false);
                }
              }}
            />
          )}
          {currentTab === 'dashboard' && (
            <DashboardPage
              profile={profile}
              summary={summary}
              loadError={loadError}
              onRetry={refreshAllData}
              hasAccounts={accounts.length > 0}
              incomeList={incomeList}
              expenseList={expenseList}
              goals={goals}
              loading={loading}
              onOpenDeposit={() => openDeposit()}
              onOpenWithdraw={() => openWithdraw()}
              onOpenAddIncome={() => setAddIncomeModal({ ...addIncomeModal, open: true, accountId: accounts[0]?.id || '' })}
              onOpenAddExpense={() => setAddExpenseModal({ ...addExpenseModal, open: true, accountId: accounts[0]?.id || '' })}
              onNavigate={navigate}
            />
          )}

          {currentTab === 'accounts' && (
            <AccountsPage
              accounts={accounts}
              transactions={transactions}
              loading={loading}
              onOpenCreateAccount={() => setCreateAccountModal({ ...createAccountModal, open: true })}
              onOpenDeposit={(id) => openDeposit(id)}
              onOpenWithdraw={(id) => openWithdraw(id)}
              onOpenTransfer={() => setTransferModal({
                ...transferModal,
                open: true,
                fromAccountId: accounts[0]?.id || '',
                toAccountId: accounts[1]?.id || accounts[0]?.id || ''
              })}
              onDeleteAccount={handleDeleteAccount}
            />
          )}

          {currentTab === 'cashflow' && (
            <CashflowPage
              incomeList={incomeList}
              expenseList={expenseList}
              loading={loading}
              onOpenAddIncome={() => setAddIncomeModal({ ...addIncomeModal, open: true, accountId: accounts[0]?.id || '' })}
              onOpenAddExpense={() => setAddExpenseModal({ ...addExpenseModal, open: true, accountId: accounts[0]?.id || '' })}
              onDeleteIncome={handleDeleteIncome}
              onDeleteExpense={handleDeleteExpense}
            />
          )}

          {currentTab === 'investments' && (
            <InvestmentsPage
              investments={investments}
              loading={loading}
              onOpenAddInvestment={() => setAddInvestmentModal({ ...addInvestmentModal, open: true })}
              onDeleteInvestment={handleDeleteInvestment}
            />
          )}

          {currentTab === 'assets_liabilities' && (
            <AssetsLiabilitiesPage
              assets={assets}
              loans={loans}
              loading={loading}
              onOpenAddAsset={() => setAddAssetModal({ ...addAssetModal, open: true })}
              onOpenAddLoan={() => setAddLoanModal({ ...addLoanModal, open: true })}
              onOpenRepayLoan={(loan) => setRepayLoanModal({
                open: true,
                loan,
                amount: loan.monthlyEmi || '',
                accountId: accounts[0]?.id || '',
                notes: `EMI Repayment for ${loan.loanName}`
              })}
              onDeleteAsset={handleDeleteAsset}
              onDeleteLoan={handleDeleteLoan}
            />
          )}

          {currentTab === 'goals' && (
            <GoalsPage
              goals={goals}
              loading={loading}
              onOpenCreateGoal={() => setCreateGoalModal({ ...createGoalModal, open: true })}
              onOpenContributeGoal={(goal) => setContributeGoalModal({
                open: true,
                goal,
                amount: '',
                accountId: accounts[0]?.id || ''
              })}
              onDeleteGoal={handleDeleteGoal}
            />
          )}

          </div>

        </main>
      </div>

      {/* ==================== MODALS ==================== */}

      {/* 1. Deposit Modal */}
      <Modal
        isOpen={depositModal.open}
        onClose={() => setDepositModal({ ...depositModal, open: false })}
        title="Record a bank deposit"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setDepositModal({ ...depositModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleDepositSubmit, event)}>Confirm Deposit</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleDepositSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Select Account</label>
            <select
              className="form-select"
              value={depositModal.accountId}
              onChange={(e) => setDepositModal({ ...depositModal, accountId: e.target.value })}
              required
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountName} ({a.bankName}) — Balance: {formatINR(a.balance)}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Deposit Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="1"
              className="form-input"
              placeholder="e.g. 25000"
              value={depositModal.amount}
              onChange={(e) => setDepositModal({ ...depositModal, amount: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Category</label>
            <input
              type="text"
              className="form-input"
              value={depositModal.category}
              onChange={(e) => setDepositModal({ ...depositModal, category: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Description / Note</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Salary, client fee, cash deposit"
              value={depositModal.description}
              onChange={(e) => setDepositModal({ ...depositModal, description: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 2. Withdrawal Modal */}
      <Modal
        isOpen={withdrawModal.open}
        onClose={() => setWithdrawModal({ ...withdrawModal, open: false })}
        title="Record a bank withdrawal"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setWithdrawModal({ ...withdrawModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleWithdrawSubmit, event)}>Confirm Withdrawal</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleWithdrawSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Select Account</label>
            <select
              className="form-select"
              value={withdrawModal.accountId}
              onChange={(e) => setWithdrawModal({ ...withdrawModal, accountId: e.target.value })}
              required
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountName} ({a.accountType}) — Balance: {formatINR(a.balance)}
                </option>
              ))}
            </select>
            <p className="form-hint">
              Savings accounts enforce minimum balance policy; Current accounts permit overdraft.
            </p>
          </div>
          <div className="form-group">
            <label className="form-label">Withdrawal Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="1"
              className="form-input"
              placeholder="e.g. 5000"
              value={withdrawModal.amount}
              onChange={(e) => setWithdrawModal({ ...withdrawModal, amount: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Category</label>
            <input
              type="text"
              className="form-input"
              value={withdrawModal.category}
              onChange={(e) => setWithdrawModal({ ...withdrawModal, category: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Description / Note</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. ATM withdrawal, bill payment"
              value={withdrawModal.description}
              onChange={(e) => setWithdrawModal({ ...withdrawModal, description: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 3. Open New Account Modal */}
      <Modal
        isOpen={createAccountModal.open}
        onClose={() => setCreateAccountModal({ ...createAccountModal, open: false })}
        title="Open New Bank Account"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setCreateAccountModal({ ...createAccountModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleCreateAccountSubmit, event)}>Create Account</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleCreateAccountSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Account Classification (Polymorphic Model)</label>
            <select
              className="form-select"
              value={createAccountModal.accountType}
              onChange={(e) => setCreateAccountModal({ ...createAccountModal, accountType: e.target.value })}
            >
              <option value="SAVINGS">Savings Account (Enforces Minimum Balance & Yields Interest)</option>
              <option value="CURRENT">Current Account (Business account with Overdraft Facility)</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Account Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Secondary Savings, Kotak Business"
              value={createAccountModal.accountName}
              onChange={(e) => setCreateAccountModal({ ...createAccountModal, accountName: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Bank Name</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. State Bank of India"
                value={createAccountModal.bankName}
                onChange={(e) => setCreateAccountModal({ ...createAccountModal, bankName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Account Number</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. SBIN8839201"
                value={createAccountModal.accountNumber}
                onChange={(e) => setCreateAccountModal({ ...createAccountModal, accountNumber: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Opening Balance (₹)</label>
            <input
              type="number"
              step="0.01"
              min="0"
              className="form-input"
              placeholder="0.00"
              value={createAccountModal.initialBalance}
              onChange={(e) => setCreateAccountModal({ ...createAccountModal, initialBalance: e.target.value })}
            />
          </div>

          {createAccountModal.accountType === 'SAVINGS' ? (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="form-group">
                <label className="form-label">Minimum Balance (₹)</label>
                <input
                  type="number"
                  step="0.01"
                  className="form-input"
                  value={createAccountModal.minimumBalance}
                  onChange={(e) => setCreateAccountModal({ ...createAccountModal, minimumBalance: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Interest Rate (% p.a.)</label>
                <input
                  type="number"
                  step="0.1"
                  className="form-input"
                  value={createAccountModal.interestRate}
                  onChange={(e) => setCreateAccountModal({ ...createAccountModal, interestRate: e.target.value })}
                />
              </div>
            </div>
          ) : (
            <div className="form-group">
              <label className="form-label">Overdraft Limit (₹)</label>
              <input
                type="number"
                step="0.01"
                className="form-input"
                value={createAccountModal.overdraftLimit}
                onChange={(e) => setCreateAccountModal({ ...createAccountModal, overdraftLimit: e.target.value })}
              />
            </div>
          )}
        </form>
      </Modal>

      {/* 4. Inter-Account Transfer Modal */}
      <Modal
        isOpen={transferModal.open}
        onClose={() => setTransferModal({ ...transferModal, open: false })}
        title="Inter-Account Transfer"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setTransferModal({ ...transferModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleTransferSubmit, event)}>Transfer Funds</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleTransferSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Source Account (Debit)</label>
            <select
              className="form-select"
              value={transferModal.fromAccountId}
              onChange={(e) => setTransferModal({ ...transferModal, fromAccountId: e.target.value })}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountName} — Available: {formatINR(a.balance)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Destination Account (Credit)</label>
            <select
              className="form-select"
              value={transferModal.toAccountId}
              onChange={(e) => setTransferModal({ ...transferModal, toAccountId: e.target.value })}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountName} — Balance: {formatINR(a.balance)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Transfer Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="1"
              className="form-input"
              placeholder="e.g. 15000"
              value={transferModal.amount}
              onChange={(e) => setTransferModal({ ...transferModal, amount: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Transfer Note</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Monthly savings allocation"
              value={transferModal.description}
              onChange={(e) => setTransferModal({ ...transferModal, description: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 5. Add Income Modal */}
      <Modal
        isOpen={addIncomeModal.open}
        onClose={() => setAddIncomeModal({ ...addIncomeModal, open: false })}
        title="Record New Income Stream"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setAddIncomeModal({ ...addIncomeModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleAddIncomeSubmit, event)}>Save Income</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleAddIncomeSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Income Title</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. October Consulting Fee"
              value={addIncomeModal.title}
              onChange={(e) => setAddIncomeModal({ ...addIncomeModal, title: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                min="1"
                className="form-input"
                placeholder="e.g. 50000"
                value={addIncomeModal.amount}
                onChange={(e) => setAddIncomeModal({ ...addIncomeModal, amount: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Category</label>
              <select
                className="form-select"
                value={addIncomeModal.category}
                onChange={(e) => setAddIncomeModal({ ...addIncomeModal, category: e.target.value })}
              >
                <option value="Salary">Salary</option>
                <option value="Consulting">Consulting / Freelance</option>
                <option value="Investments">Investments / Dividends</option>
                <option value="Rental">Rental Income</option>
                <option value="Bonus">Bonus / Allowance</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Source / Employer</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Client X / Corp"
                value={addIncomeModal.source}
                onChange={(e) => setAddIncomeModal({ ...addIncomeModal, source: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Date</label>
              <input
                type="date"
                className="form-input"
                value={addIncomeModal.incomeDate}
                onChange={(e) => setAddIncomeModal({ ...addIncomeModal, incomeDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Credit Into Account (Optional)</label>
            <select
              className="form-select"
              value={addIncomeModal.accountId}
              onChange={(e) => setAddIncomeModal({ ...addIncomeModal, accountId: e.target.value })}
            >
              <option value="">No Account (Tracking Only)</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.accountName} ({a.bankName})</option>
              ))}
            </select>
          </div>

          {addIncomeModal.accountId && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
              <input
                type="checkbox"
                id="updateAccountInc"
                checked={addIncomeModal.updateAccount}
                onChange={(e) => setAddIncomeModal({ ...addIncomeModal, updateAccount: e.target.checked })}
              />
              <label htmlFor="updateAccountInc" style={{ fontSize: '13px', cursor: 'pointer' }}>
                Automatically credit bank balance and record transaction entry
              </label>
            </div>
          )}
        </form>
      </Modal>

      {/* 6. Add Expense Modal */}
      <Modal
        isOpen={addExpenseModal.open}
        onClose={() => setAddExpenseModal({ ...addExpenseModal, open: false })}
        title="Record New Expenditure"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setAddExpenseModal({ ...addExpenseModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleAddExpenseSubmit, event)}>Record Expense</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleAddExpenseSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Expense Title</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Electric Vehicle Charging"
              value={addExpenseModal.title}
              onChange={(e) => setAddExpenseModal({ ...addExpenseModal, title: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                min="1"
                className="form-input"
                placeholder="e.g. 2400"
                value={addExpenseModal.amount}
                onChange={(e) => setAddExpenseModal({ ...addExpenseModal, amount: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Category</label>
              <select
                className="form-select"
                value={addExpenseModal.category}
                onChange={(e) => setAddExpenseModal({ ...addExpenseModal, category: e.target.value })}
              >
                <option value="Food">Food & Dining</option>
                <option value="Housing">Housing & Rent</option>
                <option value="Transportation">Transportation & Fuel</option>
                <option value="Utilities">Utilities & Internet</option>
                <option value="Healthcare">Healthcare & Medicine</option>
                <option value="Business">Business & Cloud</option>
                <option value="Entertainment">Entertainment & Travel</option>
                <option value="Shopping">Shopping & Lifestyle</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Payment Mode</label>
              <select
                className="form-select"
                value={addExpenseModal.paymentMethod}
                onChange={(e) => setAddExpenseModal({ ...addExpenseModal, paymentMethod: e.target.value })}
              >
                <option value="UPI">UPI / GPay / PhonePe</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="NET_BANKING">Net Banking</option>
                <option value="CASH">Cash</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date</label>
              <input
                type="date"
                className="form-input"
                value={addExpenseModal.expenseDate}
                onChange={(e) => setAddExpenseModal({ ...addExpenseModal, expenseDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Deduct From Account (Optional)</label>
            <select
              className="form-select"
              value={addExpenseModal.accountId}
              onChange={(e) => setAddExpenseModal({ ...addExpenseModal, accountId: e.target.value })}
            >
              <option value="">No Account (Standalone Entry)</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.accountName} — Available: {formatINR(a.balance)}</option>
              ))}
            </select>
          </div>

          {addExpenseModal.accountId && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
              <input
                type="checkbox"
                id="deductExp"
                checked={addExpenseModal.deductFromAccount}
                onChange={(e) => setAddExpenseModal({ ...addExpenseModal, deductFromAccount: e.target.checked })}
              />
              <label htmlFor="deductExp" style={{ fontSize: '13px', cursor: 'pointer' }}>
                Enforce bank withdrawal rules and deduct balance atomically
              </label>
            </div>
          )}
        </form>
      </Modal>

      {/* 7. Add Investment Modal */}
      <Modal
        isOpen={addInvestmentModal.open}
        onClose={() => setAddInvestmentModal({ ...addInvestmentModal, open: false })}
        title="Add Investment Holding"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setAddInvestmentModal({ ...addInvestmentModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleAddInvestmentSubmit, event)}>Add Holding</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleAddInvestmentSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Investment Name / Ticker</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Parag Parikh Flexi Cap Fund"
              value={addInvestmentModal.name}
              onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Asset Classification</label>
            <select
              className="form-select"
              value={addInvestmentModal.investmentType}
              onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, investmentType: e.target.value })}
            >
              <option value="Mutual Fund">Mutual Fund (SIP / Lump sum)</option>
              <option value="Stocks">Equities / Direct Stocks</option>
              <option value="Gold">Sovereign Gold Bond / Digital Gold</option>
              <option value="Fixed Deposit">Fixed Deposit (FD)</option>
              <option value="Crypto">Crypto Asset</option>
              <option value="Real Estate">REIT / Real Estate</option>
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Invested Principal (₹)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                placeholder="e.g. 100000"
                value={addInvestmentModal.investedAmount}
                onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, investedAmount: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Current Valuation (₹)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                placeholder="e.g. 125000"
                value={addInvestmentModal.currentValue}
                onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, currentValue: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Purchase Date</label>
            <input
              type="date"
              className="form-input"
              value={addInvestmentModal.purchaseDate}
              onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, purchaseDate: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Notes</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. 5-year investment horizon"
              value={addInvestmentModal.notes}
              onChange={(e) => setAddInvestmentModal({ ...addInvestmentModal, notes: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 8. Add Asset Modal */}
      <Modal
        isOpen={addAssetModal.open}
        onClose={() => setAddAssetModal({ ...addAssetModal, open: false })}
        title="Record Physical Capital Asset"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setAddAssetModal({ ...addAssetModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleAddAssetSubmit, event)}>Save Asset</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleAddAssetSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Asset Title</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Bangalore Urban Residential Plot"
              value={addAssetModal.name}
              onChange={(e) => setAddAssetModal({ ...addAssetModal, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Asset Classification</label>
            <select
              className="form-select"
              value={addAssetModal.assetCategory}
              onChange={(e) => setAddAssetModal({ ...addAssetModal, assetCategory: e.target.value })}
            >
              <option value="MOVABLE">Movable (Vehicle, Equipment, Tech Setup)</option>
              <option value="IMMOVABLE">Immovable (Residential Property, Land, Commercial)</option>
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Purchase Cost (₹)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                placeholder="e.g. 1500000"
                value={addAssetModal.purchaseValue}
                onChange={(e) => setAddAssetModal({ ...addAssetModal, purchaseValue: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Estimated Current Value (₹)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                placeholder="e.g. 2100000"
                value={addAssetModal.currentValue}
                onChange={(e) => setAddAssetModal({ ...addAssetModal, currentValue: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Acquisition Date</label>
            <input
              type="date"
              className="form-input"
              value={addAssetModal.acquisitionDate}
              onChange={(e) => setAddAssetModal({ ...addAssetModal, acquisitionDate: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 9. Add Loan Modal */}
      <Modal
        isOpen={addLoanModal.open}
        onClose={() => setAddLoanModal({ ...addLoanModal, open: false })}
        title="Register Liability / Loan Facility"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setAddLoanModal({ ...addLoanModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleAddLoanSubmit, event)}>Register Loan</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleAddLoanSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Loan Description</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Electric Vehicle Auto Loan"
              value={addLoanModal.loanName}
              onChange={(e) => setAddLoanModal({ ...addLoanModal, loanName: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Lender Institution</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. HDFC Bank Auto Financing"
              value={addLoanModal.lender}
              onChange={(e) => setAddLoanModal({ ...addLoanModal, lender: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Principal Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                min="1000"
                className="form-input"
                placeholder="e.g. 1200000"
                value={addLoanModal.principalAmount}
                onChange={(e) => setAddLoanModal({ ...addLoanModal, principalAmount: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Interest Rate (% p.a.)</label>
              <input
                type="number"
                step="0.01"
                className="form-input"
                value={addLoanModal.interestRate}
                onChange={(e) => setAddLoanModal({ ...addLoanModal, interestRate: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Tenure (Months)</label>
              <input
                type="number"
                min="1"
                className="form-input"
                value={addLoanModal.tenureMonths}
                onChange={(e) => setAddLoanModal({ ...addLoanModal, tenureMonths: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Start Date</label>
              <input
                type="date"
                className="form-input"
                value={addLoanModal.startDate}
                onChange={(e) => setAddLoanModal({ ...addLoanModal, startDate: e.target.value })}
              />
            </div>
          </div>
        </form>
      </Modal>

      {/* 10. Repay Loan Modal */}
      <Modal
        isOpen={repayLoanModal.open}
        onClose={() => setRepayLoanModal({ ...repayLoanModal, open: false })}
        title={`Record EMI Repayment — ${repayLoanModal.loan?.loanName || ''}`}
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setRepayLoanModal({ ...repayLoanModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleRepayLoanSubmit, event)}>Confirm Repayment</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleRepayLoanSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Outstanding Principal Balance</label>
            <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-negative)' }}>
              {formatINR(repayLoanModal.loan?.outstandingBalance)}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Repayment Installment Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="1"
              className="form-input"
              value={repayLoanModal.amount}
              onChange={(e) => setRepayLoanModal({ ...repayLoanModal, amount: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Deduct From Bank Account (Optional)</label>
            <select
              className="form-select"
              value={repayLoanModal.accountId}
              onChange={(e) => setRepayLoanModal({ ...repayLoanModal, accountId: e.target.value })}
            >
              <option value="">Do not deduct from bank balance</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.accountName} — Balance: {formatINR(a.balance)}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Payment Note</label>
            <input
              type="text"
              className="form-input"
              value={repayLoanModal.notes}
              onChange={(e) => setRepayLoanModal({ ...repayLoanModal, notes: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 11. Create Goal Modal */}
      <Modal
        isOpen={createGoalModal.open}
        onClose={() => setCreateGoalModal({ ...createGoalModal, open: false })}
        title="Establish Financial Goal Target"
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setCreateGoalModal({ ...createGoalModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleCreateGoalSubmit, event)}>Create Goal</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleCreateGoalSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Milestone Title</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Emergency 6-Month Liquid Reserve"
              value={createGoalModal.title}
              onChange={(e) => setCreateGoalModal({ ...createGoalModal, title: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Target Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                min="100"
                className="form-input"
                placeholder="e.g. 300000"
                value={createGoalModal.targetAmount}
                onChange={(e) => setCreateGoalModal({ ...createGoalModal, targetAmount: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Target Completion Date</label>
              <input
                type="date"
                className="form-input"
                value={createGoalModal.targetDate}
                onChange={(e) => setCreateGoalModal({ ...createGoalModal, targetDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Category</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Safety Net, Travel, Education"
              value={createGoalModal.category}
              onChange={(e) => setCreateGoalModal({ ...createGoalModal, category: e.target.value })}
            />
          </div>
        </form>
      </Modal>

      {/* 12. Contribute to Goal Modal */}
      <Modal
        isOpen={contributeGoalModal.open}
        onClose={() => setContributeGoalModal({ ...contributeGoalModal, open: false })}
        title={`Contribute Funds to ${contributeGoalModal.goal?.title || 'Goal'}`}
        footer={(
          <>
            <button className="btn btn-secondary" onClick={() => setContributeGoalModal({ ...contributeGoalModal, open: false })}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={(event) => runSubmission(handleContributeGoalSubmit, event)}>Apply Contribution</button>
          </>
        )}
      >
        <form onSubmit={(event) => runSubmission(handleContributeGoalSubmit, event)}>
          <div className="form-group">
            <label className="form-label">Current Goal Progress</label>
            <div style={{ fontSize: '15px', color: 'var(--color-text-muted)' }}>
              Saved: <strong>{formatINR(contributeGoalModal.goal?.currentAmount)}</strong> of {formatINR(contributeGoalModal.goal?.targetAmount)}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Contribution Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="1"
              className="form-input"
              placeholder="e.g. 15000"
              value={contributeGoalModal.amount}
              onChange={(e) => setContributeGoalModal({ ...contributeGoalModal, amount: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Deduct From Bank Account (Optional)</label>
            <select
              className="form-select"
              value={contributeGoalModal.accountId}
              onChange={(e) => setContributeGoalModal({ ...contributeGoalModal, accountId: e.target.value })}
            >
              <option value="">Do not deduct from bank balance</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.accountName} — Balance: {formatINR(a.balance)}</option>
              ))}
            </select>
          </div>
        </form>
      </Modal>
    </div>
  );
}

function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError('');
    if (mode === 'register' && password !== confirmation) { setError('Passwords do not match.'); return; }
    setBusy(true);
    try {
      const currentProfile = mode === 'register'
        ? await api.register({ name, email, password })
        : await api.login({ email, password });
      onAuthenticated(currentProfile);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  return (
    <main className="auth-page">
      <form className="auth-card" onSubmit={submit}>
        <a
          className="auth-brand-lockup auth-brand-link"
          href="https://srivathsan-v-portfolio.ai.studio/"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="DOSTATOK — open Srivathsan's portfolio in a new tab"
          title="Visit my portfolio"
        >
          <img src={coinLogo} alt="" aria-hidden="true" />
          <span className="auth-brand-text">
            <strong>DOSTATOK</strong>
            <span>BUILD YOUR HORIZON</span>
          </span>
        </a>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <p>Your financial information stays in your local DOSTATOK database.</p>
        {mode === 'register' && <label>Name<input className="form-input" autoComplete="name" value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} /></label>}
        <label>Email<input className="form-input" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Password<input className="form-input" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} value={password} onChange={(e) => setPassword(e.target.value)} minLength={12} maxLength={128} required /></label>
        {mode === 'register' && <label>Confirm password<input className="form-input" type="password" autoComplete="new-password" value={confirmation} onChange={(e) => setConfirmation(e.target.value)} required /></label>}
        {error && <div className="auth-error" role="alert">{error}</div>}
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
        <button className="auth-switch" type="button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
          {mode === 'login' ? 'New to DOSTATOK? Create an account' : 'Already registered? Sign in'}
        </button>
      </form>
    </main>
  );
}

export default function App() {
  const [profile, setProfile] = useState(null);
  const [checking, setChecking] = useState(true);
  useEffect(() => { api.getMe().then(setProfile).catch(() => setProfile(null)).finally(() => setChecking(false)); }, []);
  async function logout() { try { await api.logout(); } finally { setProfile(null); } }
  if (checking) return <main className="auth-page"><p>Connecting to your local DOSTATOK server…</p></main>;
  return profile ? <FinanceApp profile={profile} onProfileUpdate={setProfile} onLogout={logout} /> : <AuthScreen onAuthenticated={setProfile} />;
}
