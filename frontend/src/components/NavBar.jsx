import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import Avatar from './Avatar.jsx'

function NavBar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="flex items-center gap-4 text-sm">
      {user ? (
        <>
          <NavItem to="/feed">Feed</NavItem>
          <NavItem to="/browse">Browse</NavItem>
          <NavItem to="/friends">Friends</NavItem>
          <Link
            to={`/users/${user.id}`}
            className="flex items-center gap-2 rounded-full px-2 py-1 transition hover:bg-slate-100"
            title="Profile"
          >
            <Avatar userId={user.id} email={user.email} size="sm" />
            <span className="hidden text-xs font-medium text-slate-700 sm:inline">Profile</span>
          </Link>
          <NavItem to="/playdates">Playdates</NavItem>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-full border border-pink-500 px-3 py-1 text-xs font-medium text-pink-600 hover:bg-pink-50"
          >
            Logout
          </button>
        </>
      ) : (
        <>
          <NavItem to="/login">Login</NavItem>
          <NavItem to="/register">Register</NavItem>
        </>
      )}
    </nav>
  )
}

function NavItem({ to, children }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        [
          'rounded-full px-3 py-1 text-xs font-medium transition-colors',
          isActive
            ? 'bg-pink-500 text-white shadow-sm'
            : 'text-slate-600 hover:bg-slate-100',
        ].join(' ')
      }
    >
      {children}
    </NavLink>
  )
}

export default NavBar

