import { useEffect, useState } from 'react';

export default function App() {
  const [status, setStatus] = useState('Loading health status...');

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
      })
      .then((data) => setStatus(data.status))
      .catch((error) => setStatus(`Error: ${error.message}`));
  }, []);

  return <main>{status}</main>;
}