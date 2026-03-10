import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import Avatar from './Avatar.jsx'

const NAV_ITEMS = [
  { to: '/feed',      label: 'Feed',      icon: '🏠' },
  { to: '/browse',    label: 'Browse',    icon: '🔍' },
  { to: '/friends',   label: 'Friends',   icon: '🐕' },
  { to: '/playdates', label: 'Playdates', icon: '📅' },
]

function NavBar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (!user) {
    return (
      <nav className="flex items-center gap-2">
        <NavLink
          to="/login"
          className={({ isActive }) =>
            `rounded-xl px-4 py-2 text-sm font-medium transition ${
              isActive ? 'bg-pink-500 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`
          }
        >
          Log in
        </NavLink>
        <NavLink
          to="/register"
          className="btn-primary btn-sm"
        >
          Sign up
        </NavLink>
      </nav>
    )
  }

  return (
    <nav className="flex items-center gap-1">
      {NAV_ITEMS.map(({ to, label, icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `hidden sm:flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm font-medium transition ${
              isActive
                ? 'bg-pink-50 text-pink-600'
                : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
            }`
          }
        >
          <span className="text-base leading-none">{icon}</span>
          <span>{label}</span>
        </NavLink>
      ))}

      {/* Mobile nav — icons only */}
      {NAV_ITEMS.map(({ to, icon, label }) => (
        <NavLink
          key={`m-${to}`}
          to={to}
          title={label}
          className={({ isActive }) =>
            `flex sm:hidden items-center justify-center w-9 h-9 rounded-xl text-lg transition ${
              isActive ? 'bg-pink-50 text-pink-600' : 'text-slate-500 hover:bg-slate-100'
            }`
          }
        >
          {icon}
        </NavLink>
      ))}

      <div className="ml-1 h-6 w-px bg-slate-200" />

      <Link
        to={`/users/${user.id}`}
        className="flex items-center gap-2 rounded-xl px-2 py-1.5 transition hover:bg-slate-100"
        title="My profile"
      >
        <Avatar userId={user.id} email={user.email} size="sm" />
        <span className="hidden md:block text-sm font-medium text-slate-700">
          {user.username ? `@${user.username}` : 'Profile'}
        </span>
      </Link>

      <button
        type="button"
        onClick={handleLogout}
        className="ml-1 rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-red-600 transition"
      >
        Logout
      </button>
    </nav>
  )
}

export default NavBar
