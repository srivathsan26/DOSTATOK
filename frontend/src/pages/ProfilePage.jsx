import React, { useEffect, useState } from 'react';

export default function ProfilePage({ profile, onSave, saving }) {
  const [age, setAge] = useState(profile?.age ?? '');
  const [occupation, setOccupation] = useState(profile?.occupation ?? '');
  const [address, setAddress] = useState(profile?.address ?? '');
  const [annualIncome, setAnnualIncome] = useState(profile?.annualIncome ?? '');
  const [error, setError] = useState('');

  useEffect(() => {
    setAge(profile?.age ?? '');
    setOccupation(profile?.occupation ?? '');
    setAddress(profile?.address ?? '');
    setAnnualIncome(profile?.annualIncome ?? '');
  }, [profile]);

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      await onSave({ age: Number(age), occupation, address, annualIncome });
    } catch (saveError) {
      setError(saveError.message || 'Could not save your profile.');
    }
  }

  return (
    <section className="profile-page" aria-labelledby="profile-page-title">
      <div className="profile-page-heading">
        <p className="eyebrow">YOUR ACCOUNT</p>
        <h1 id="profile-page-title">{profile?.profileComplete ? 'Your profile' : 'Complete your profile'}</h1>
        <p>{profile?.profileComplete ? 'Review or update your personal details.' : 'Add a few details to finish setting up your account.'}</p>
      </div>
      <form className="profile-form card" onSubmit={submit}>
        <div className="profile-identity">
          <div className="user-avatar" aria-hidden="true">{(profile?.name || 'A').trim().charAt(0).toUpperCase()}</div>
          <div><strong>{profile?.name}</strong><span>{profile?.email}</span></div>
        </div>
        <div className="profile-fields">
          <label className="form-label">Age
            <input className="form-input" type="number" min="13" max="120" value={age} onChange={(event) => setAge(event.target.value)} required />
          </label>
          <label className="form-label">Job / occupation
            <input className="form-input" type="text" maxLength="100" autoComplete="organization-title" value={occupation} onChange={(event) => setOccupation(event.target.value)} required />
          </label>
          <label className="form-label profile-address-field">Address
            <textarea className="form-textarea" rows="3" maxLength="500" autoComplete="street-address" value={address} onChange={(event) => setAddress(event.target.value)} required />
          </label>
          <label className="form-label">Annual income (₹)
            <input className="form-input" type="number" min="0" step="0.01" value={annualIncome} onChange={(event) => setAnnualIncome(event.target.value)} required />
          </label>
        </div>
        {error && <div className="auth-error" role="alert">{error}</div>}
        <div className="profile-form-actions">
          <p>These details are stored in your local DOSTATOK database.</p>
          <button className="btn btn-primary" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save profile'}</button>
        </div>
      </form>
    </section>
  );
}
