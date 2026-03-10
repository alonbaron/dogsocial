import { Link } from 'react-router-dom'
import pawpalsLogo from '../assets/pawpals-logo.png'

const FEATURES = [
  { icon: '📸', title: 'Photo posts', body: 'Share moments with your pup — photos up to 5 MB, reactions, and a beautiful feed.' },
  { icon: '🐕', title: 'Dog profiles', body: 'Add all your dogs with breed and bio. Your pack lives on your profile.' },
  { icon: '🤝', title: 'Friends & follows', body: 'Follow other owners, see mutual friends, and build your community.' },
  { icon: '📅', title: 'Playdates', body: 'Schedule playdates by username, accept incoming requests, and track upcoming sessions.' },
  { icon: '✏️', title: 'Edit everything', body: 'Update your avatar, username, bio, posts — full control over your profile.' },
  { icon: '🔍', title: 'Discovery', body: 'Browse all posts and search for people to follow.' },
]

function LandingPage() {
  return (
    <div className="mx-auto max-w-5xl space-y-16 py-4">
      {/* Hero */}
      <div className="grid items-center gap-10 md:grid-cols-2">
        <div>
          <img src={pawpalsLogo} alt="PawPals" className="h-16 w-auto mix-blend-multiply mb-6" />
          <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 leading-tight">
            The social network<br />
            <span className="bg-gradient-to-r from-pink-500 to-rose-500 bg-clip-text text-transparent">
              for dog owners.
            </span>
          </h1>
          <p className="mt-4 text-base leading-relaxed text-slate-600 max-w-sm">
            Share posts, follow friends, schedule playdates, and manage your pack — all in one clean, modern app.
          </p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            <Link to="/register" className="btn-primary">
              Get started — it&apos;s free
            </Link>
            <Link to="/login" className="btn-secondary">
              Log in
            </Link>
          </div>
        </div>

        {/* Mock card preview */}
        <div className="rounded-3xl bg-gradient-to-br from-pink-400/10 via-rose-400/8 to-orange-400/10 p-5 ring-1 ring-pink-100">
          <div className="space-y-3">
            <div className="card p-4">
              <div className="flex items-center gap-3 mb-3">
                <div className="h-9 w-9 rounded-full bg-gradient-to-br from-pink-300 to-rose-400 flex items-center justify-center text-lg">🐾</div>
                <div>
                  <p className="text-sm font-semibold text-slate-900">@luna_lover</p>
                  <p className="text-[11px] text-slate-400">2h ago</p>
                </div>
              </div>
              <p className="text-sm text-slate-800 mb-3">Took Luna to the park — she made 3 new friends in 10 minutes! 🐕</p>
              <div className="rounded-xl bg-gradient-to-br from-amber-100 to-orange-100 h-28 flex items-center justify-center text-5xl">🌳🐕🌳</div>
              <div className="mt-3 flex gap-2">
                <span className="badge badge-pink">👍 12</span>
                <span className="badge badge-slate">👎 0</span>
              </div>
            </div>

            <div className="card p-4">
              <div className="flex items-center gap-3 mb-2">
                <div className="h-9 w-9 rounded-full bg-gradient-to-br from-violet-300 to-purple-400 flex items-center justify-center text-lg">🐶</div>
                <div>
                  <p className="text-sm font-semibold text-slate-900">@max_dog_dad</p>
                  <p className="text-[11px] text-slate-400">5h ago</p>
                </div>
              </div>
              <p className="text-sm text-slate-800">Anyone up for a playdate this Saturday at Riverside Park? 🎾</p>
            </div>
          </div>
        </div>
      </div>

      {/* Features */}
      <div>
        <h2 className="text-xl font-bold text-slate-900 mb-6 text-center">Everything you need</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map(({ icon, title, body }) => (
            <div key={title} className="card p-5 hover:shadow-md transition-shadow duration-200">
              <div className="text-3xl mb-3">{icon}</div>
              <h3 className="font-bold text-slate-900 mb-1">{title}</h3>
              <p className="text-sm leading-relaxed text-slate-500">{body}</p>
            </div>
          ))}
        </div>
      </div>

      {/* CTA */}
      <div className="rounded-3xl bg-gradient-to-br from-pink-500 to-rose-600 px-8 py-12 text-center text-white shadow-xl">
        <div className="text-5xl mb-4">🐾</div>
        <h2 className="text-2xl font-extrabold mb-2">Ready to join the pack?</h2>
        <p className="text-pink-100 text-sm mb-8 max-w-xs mx-auto">Create your free account and connect with dog owners in your area.</p>
        <Link to="/register" className="inline-flex items-center justify-center rounded-2xl bg-white px-8 py-3 text-sm font-bold text-pink-600 shadow hover:shadow-md hover:bg-pink-50 transition">
          Create account
        </Link>
      </div>
    </div>
  )
}

export default LandingPage
