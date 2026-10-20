import React from 'react';
import {
  LayoutDashboard,
  Landmark,
  ArrowLeftRight,
  TrendingUp,
  Building2,
  Target,
} from 'lucide-react';

const menuItems = [
  { id: 'dashboard', label: 'Overview', icon: LayoutDashboard },
  { id: 'accounts', label: 'Accounts', icon: Landmark },
  { id: 'cashflow', label: 'Cash flow', icon: ArrowLeftRight },
  { id: 'investments', label: 'Investments', icon: TrendingUp },
  { id: 'assets_liabilities', label: 'Assets & loans', icon: Building2 },
  { id: 'goals', label: 'Goals', icon: Target },
];

export default function Sidebar({ currentTab, onSelectTab }) {
  return (
    <nav className="top-navigation" aria-label="Main navigation">
      <div className="top-navigation-inner">
        {menuItems.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            aria-current={currentTab === id ? 'page' : undefined}
            className={`nav-item ${currentTab === id ? 'active' : ''}`}
            onClick={() => onSelectTab(id)}
          >
            <Icon className="nav-item-icon" aria-hidden="true" />
            <span>{label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
