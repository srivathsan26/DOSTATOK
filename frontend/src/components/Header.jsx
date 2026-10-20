import React, { useEffect, useRef, useState } from 'react';
import { Moon, Sun, LogOut } from 'lucide-react';
import coinLogo from '../assets/dostatok-coin.png';

export default function Header({ serverOnline, profile, onLogout, onProfileClick, theme, onToggleTheme }) {
  const [showSignOutConfirm, setShowSignOutConfirm] = useState(false);
  const signOutRef = useRef(null);
  const initials = (profile?.name || 'A')
    .trim()
    .split(/\s+/)
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  useEffect(() => {
    if (!showSignOutConfirm) return undefined;
    function handlePointerDown(event) {
      if (!signOutRef.current?.contains(event.target)) setShowSignOutConfirm(false);
    }
    function handleKeyDown(event) {
      if (event.key === 'Escape') setShowSignOutConfirm(false);
    }
    document.addEventListener('pointerdown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [showSignOutConfirm]);

  return (
    <header className="app-header">
      <a
        className="brand-lockup brand-link"
        href="https://srivathsan-v-portfolio.ai.studio/"
        target="_blank"
        rel="noopener noreferrer"
        aria-label="DOSTATOK — open Srivathsan's portfolio in a new tab"
        title="Visit my portfolio"
      >
        <img className="brand-logo-icon" src={coinLogo} alt="" aria-hidden="true" />
        <span className="brand-text-block">
          <span className="brand-title">DOSTATOK</span>
          <span className="brand-subtitle">BUILD YOUR HORIZON</span>
        </span>
      </a>

      <div className="header-actions">
        <div className={`health-badge ${serverOnline ? 'is-online' : 'is-offline'}`} role="status">
          <span className="health-dot" aria-hidden="true" />
          <span>{serverOnline ? 'Local API ready' : 'Offline'}</span>
        </div>
        <button
          className="icon-button"
          type="button"
          onClick={onToggleTheme}
          aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}
          title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}
        >
          {theme === 'dark' ? <Sun size={18} aria-hidden="true" /> : <Moon size={18} aria-hidden="true" />}
        </button>
        <div className="user-profile-badge">
          <div className="user-avatar" aria-hidden="true">{initials}</div>
          <button className="profile-name profile-name-button" type="button" onClick={onProfileClick} aria-label="Open profile">
            {profile?.name || 'Account'}
          </button>
          <div className="sign-out-wrap" ref={signOutRef}>
            <button className="sign-out-button" type="button" onClick={() => setShowSignOutConfirm((open) => !open)} aria-label="Sign out">
              <LogOut size={15} aria-hidden="true" />
              <span>Sign out</span>
            </button>
            {showSignOutConfirm && (
              <div className="logout-confirm-popover" role="alertdialog" aria-label="Confirm sign out">
                <strong>Sign out?</strong>
                <p>Are you sure you want to sign out?</p>
                <div className="logout-confirm-actions">
                  <button className="btn btn-secondary" type="button" onClick={() => setShowSignOutConfirm(false)}>Cancel</button>
                  <button className="btn btn-danger logout-confirm-yes" type="button" onClick={() => { setShowSignOutConfirm(false); onLogout(); }}>Yes</button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
