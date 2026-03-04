import { Outlet } from 'react-router-dom'
import Logo from './components/Logo.jsx'
import NavBar from './components/NavBar.jsx'

function App() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-slate-100">
      <header className="bg-white/90 shadow-sm">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <Logo />
          <NavBar />
        </div>
      </header>

      <main className="mx-auto flex max-w-5xl px-4 py-6">
        <div className="w-full">
          <Outlet />
        </div>
      </main>
    </div>
  )
}

export default App
