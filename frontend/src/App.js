import React, { useState } from 'react';
import Header from './components/Header';
import ClientList from './components/ClientList';
import AppointmentList from './components/AppointmentList';
import AppointmentForm from './components/AppointmentForm';

const App = () => {
  const [refreshKey, setRefreshKey] = useState(0);

  const handleAppointmentCreated = () => {
    // Trigger refresh of appointment list
    setRefreshKey(prev => prev + 1);
  };

  const appStyle = {
    minHeight: '100vh',
    backgroundColor: '#f8fafc',
  };

  const mainStyle = {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '2rem 1rem',
  };

  const gridStyle = {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '2rem',
    marginTop: '2rem',
  };

  const responsiveGridStyle = {
    ...gridStyle,
    '@media (max-width: 768px)': {
      gridTemplateColumns: '1fr',
    },
  };

  return (
    <div style={appStyle}>
      <Header />
      <main style={mainStyle}>
        <AppointmentForm onAppointmentCreated={handleAppointmentCreated} />
        <div style={gridStyle}>
          <ClientList />
          <AppointmentList key={refreshKey} />
        </div>
      </main>
    </div>
  );
};

export default App;
