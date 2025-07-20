import React from 'react';

const Header = () => {
  const headerStyle = {
    backgroundColor: '#ffffff',
    borderBottom: '1px solid #e2e8f0',
    padding: '1rem 2rem',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    position: 'sticky',
    top: 0,
    zIndex: 100,
  };

  const titleStyle = {
    fontSize: '1.875rem',
    fontWeight: '700',
    color: '#1e293b',
    margin: 0,
    fontFamily: 'Inter, sans-serif',
  };

  const subtitleStyle = {
    fontSize: '0.875rem',
    color: '#64748b',
    marginTop: '0.25rem',
    fontWeight: '400',
  };

  const containerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    maxWidth: '1200px',
    margin: '0 auto',
  };

  const statusStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    fontSize: '0.875rem',
    color: '#059669',
    fontWeight: '500',
  };

  const statusDotStyle = {
    width: '8px',
    height: '8px',
    backgroundColor: '#10b981',
    borderRadius: '50%',
  };

  return (
    <header style={headerStyle}>
      <div style={containerStyle}>
        <div>
          <h1 style={titleStyle}>Wellness Clinic Admin</h1>
          <p style={subtitleStyle}>Client & Appointment Management System</p>
        </div>
        <div style={statusStyle}>
          <div style={statusDotStyle}></div>
          System Online
        </div>
      </div>
    </header>
  );
};

export default Header;
