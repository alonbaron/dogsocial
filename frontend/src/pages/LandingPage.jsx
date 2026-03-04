import { Link } from 'react-router-dom'
import pawpalsLogo from '../assets/pawpals-logo.png'

function Feature({ title, body }) {
  return (
    <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
      <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
      <p className="mt-1 text-xs leading-relaxed text-slate-500">{body}</p>
    </div>
  )
}

function LandingPage() {
  return (
    <div className="mx-auto max-w-5xl">
      <div className="grid items-center gap-8 md:grid-cols-2">
        <div>
          <img
            src={pawpalsLogo}
            alt="PawPals"
            className="h-20 w-auto mix-blend-multiply"
          />
          <h1 className="mt-4 text-3xl font-semibold tracking-tight text-slate-900">
            The social network for dog owners.
          </h1>
          <p className="mt-3 text-sm leading-relaxed text-slate-600">
            Share posts, follow friends, react and comment, and schedule
            playdates — all in one clean, modern app.
          </p>
          <div className="mt-6 flex flex-wrap items-center gap-3">
            <Link
              to="/register"
              className="rounded-full bg-pink-500 px-5 py-2 text-xs font-medium text-white shadow-sm hover:bg-pink-600"
            >
              Create account
            </Link>
            <Link
              to="/login"
              className="rounded-full border border-slate-200 bg-white px-5 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50"
            >
              Log in
            </Link>
            <Link
              to="/browse"
              className="text-xs font-medium text-pink-600 hover:underline"
            >
              Browse the community
            </Link>
          </div>
        </div>

        <div className="rounded-3xl bg-gradient-to-br from-pink-500/10 via-purple-500/10 to-indigo-500/10 p-6 ring-1 ring-slate-100">
          <div className="space-y-3">
            <div className="rounded-2xl bg-white p-4 shadow-sm">
              <div className="flex items-center justify-between text-xs text-slate-500">
                <span className="font-medium text-slate-800">someone@paws.com</span>
                <span>just now</span>
              </div>
              <p className="mt-2 text-sm text-slate-800">
                Took Luna to the park — she made 3 new friends in 10 minutes.
              </p>
              <div className="mt-3 flex gap-2">
                <span className="rounded-full bg-pink-100 px-3 py-1 text-[11px] font-medium text-pink-700">
                  👍 12
                </span>
                <span className="rounded-full bg-slate-100 px-3 py-1 text-[11px] font-medium text-slate-600">
                  👎 0
                </span>
              </div>
            </div>

            <div className="rounded-2xl bg-white p-4 shadow-sm">
              <div className="flex items-center justify-between text-xs text-slate-500">
                <span className="font-medium text-slate-800">you@paws.com</span>
                <span>today</span>
              </div>
              <p className="mt-2 text-sm text-slate-800">
                Anyone up for a playdate this weekend?
              </p>
              <div className="mt-3 text-[11px] text-slate-500">
                Playdate requests, incoming & upcoming.
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-10 grid gap-4 md:grid-cols-3">
        <Feature
          title="Instagram-like feed"
          body="A fast, clean feed with reactions and smooth interactions."
        />
        <Feature
          title="Friends & follows"
          body="Follow other owners and see mutual friends in one place."
        />
        <Feature
          title="Playdates"
          body="Request, approve, and track upcoming and past playdates."
        />
      </div>
    </div>
  )
}

export default LandingPage

