// API utility functions for communicating with backend services

//const CLIENT_SERVICE_URL = 'http://localhost:8081';
//const APPOINTMENT_SERVICE_URL = 'http://localhost:8082';

const CLIENT_SERVICE_URL = 'https://client-service-app-582adc026b78.herokuapp.com';
const APPOINTMENT_SERVICE_URL = 'https://appointment-service-app-dc5b59d8a050.herokuapp.com';

// Generic API call function
async function apiCall(url, options = {}) {
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    // Handle empty responses
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    
    return await response.text();
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
}

// Client API functions
export async function fetchClients() {
  return apiCall(`${CLIENT_SERVICE_URL}/clients`);
}

export async function fetchClientById(id) {
  return apiCall(`${CLIENT_SERVICE_URL}/clients/${id}`);
}

export async function searchClients(name) {
  return apiCall(`${CLIENT_SERVICE_URL}/clients/search?name=${encodeURIComponent(name)}`);
}

export async function createClient(clientData) {
  return apiCall(`${CLIENT_SERVICE_URL}/clients`, {
    method: 'POST',
    body: JSON.stringify(clientData),
  });
}

export async function updateClient(id, clientData) {
  return apiCall(`${CLIENT_SERVICE_URL}/clients/${id}`, {
    method: 'PUT',
    body: JSON.stringify(clientData),
  });
}

export async function deleteClient(id) {
  return apiCall(`${CLIENT_SERVICE_URL}/clients/${id}`, {
    method: 'DELETE',
  });
}

export async function syncClients() {
  return apiCall(`${CLIENT_SERVICE_URL}/clients/sync`, {
    method: 'POST',
  });
}

// Appointment API functions
export async function fetchAppointments() {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments`);
}

export async function fetchUpcomingAppointments() {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/upcoming`);
}

export async function fetchTodaysAppointments() {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/today`);
}

export async function fetchAppointmentById(id) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/${id}`);
}

export async function fetchAppointmentsByClientId(clientId) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/client/${clientId}`);
}

export async function fetchAppointmentsByStatus(status) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/status/${status}`);
}

export async function fetchAppointmentsBetween(startTime, endTime) {
  const params = new URLSearchParams({
    startTime: startTime,
    endTime: endTime,
  });
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/range?${params}`);
}

export async function createAppointment(appointmentData) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments`, {
    method: 'POST',
    body: JSON.stringify(appointmentData),
  });
}

export async function updateAppointment(id, appointmentData) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/${id}`, {
    method: 'PUT',
    body: JSON.stringify(appointmentData),
  });
}

export async function cancelAppointment(id) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/${id}/cancel`, {
    method: 'PATCH',
  });
}

export async function deleteAppointment(id) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/${id}`, {
    method: 'DELETE',
  });
}

export async function syncAppointments() {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/sync`, {
    method: 'POST',
  });
}

export async function getAppointmentCountByStatus(status) {
  return apiCall(`${APPOINTMENT_SERVICE_URL}/appointments/stats/count/${status}`);
}

// Utility functions
export function formatDateTime(dateTimeString) {
  if (!dateTimeString) return '';
  
  try {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  } catch (error) {
    console.error('Error formatting date:', error);
    return dateTimeString;
  }
}

export function formatDate(dateString) {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  } catch (error) {
    console.error('Error formatting date:', error);
    return dateString;
  }
}

export function formatTime(timeString) {
  if (!timeString) return '';
  
  try {
    const date = new Date(timeString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  } catch (error) {
    console.error('Error formatting time:', error);
    return timeString;
  }
}
