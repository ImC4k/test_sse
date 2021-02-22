import { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [sseMessage, setSseMessage] = useState('');
  
  useEffect(() => {
    var sse = new EventSource('http://localhost:8080/test-sse?id=123');
    sse.onmessage = (event) => {
      setSseMessage(event.data);
    }
    sse.onerror = (e) => {
      setSseMessage(`error: ${JSON.stringify(e, null, 4)}`);
      sse.close();
    }
    return () => {
      sse.close()
    }
  }, []);

  return (
    <div className="App">
      message is {sseMessage || "empty"}
    </div>
  );
}

export default App;
