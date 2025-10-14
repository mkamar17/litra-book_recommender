import { useEffect, useState } from 'react'
import { testConnection } from './api'

import './App.css'

function App() {
  const [data, setData] = useState(null)

  useEffect(() => {
    testConnection().then(result => setData(result))
  }, [])

  return (
    <>
      <div style={{ padding: '2rem' }}>
      <h1>Spring Boot Connection Test</h1>
      {data ? (
        <pre>{JSON.stringify(data, null, 2)}</pre>
      ) : (
        <p>Loading data from backend...</p>
      )}
    </div>

    </>
  )
}

export default App
