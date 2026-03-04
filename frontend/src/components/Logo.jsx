import { Link } from 'react-router-dom'
import pawpalsLogo from '../assets/pawpals-logo.png'

function Logo() {
  return (
    <Link to="/feed" className="flex items-center gap-2 py-1">
      <img
        src={pawpalsLogo}
        alt="PawPals"
        className="h-12 w-auto object-contain select-none"
        style={{ display: 'block' }}
      />
    </Link>
  )
}

export default Logo

