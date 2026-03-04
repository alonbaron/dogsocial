import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'

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
          <NavItem to={`/users/${user.id}`}>Profile</NavItem>
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

