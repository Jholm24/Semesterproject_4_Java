// Main App — routes between login, manager dashboard, and operator view

function App() {
  // Persist nav state so user can refresh and land where they were
  const [view, setView] = useState(() => localStorage.getItem('sb_view') || 'login');
  const [role, setRole] = useState(() => localStorage.getItem('sb_role') || null);
  const [user, setUser] = useState(() => localStorage.getItem('sb_user') || null);
  const [activeLine, setActiveLine] = useState(() => localStorage.getItem('sb_line') || 'line-1');

  useEffect(() => { localStorage.setItem('sb_view', view); }, [view]);
  useEffect(() => { if (role) localStorage.setItem('sb_role', role); else localStorage.removeItem('sb_role'); }, [role]);
  useEffect(() => { if (user) localStorage.setItem('sb_user', user); else localStorage.removeItem('sb_user'); }, [user]);
  useEffect(() => { localStorage.setItem('sb_line', activeLine); }, [activeLine]);

  const logout = () => { setRole(null); setUser(null); setView('login'); };

  const nav = { view, setView, role, setRole, user, setUser, activeLine, setActiveLine, logout };

  return (
    <div className="app-root" data-screen-label={
      view === 'login' ? '01 Login' :
      view === 'dashboard' ? (role === 'manager' ? '02 Manager Dashboard' : '02 Operator Dashboard') :
      view === 'builder' ? '03 Task Builder' :
      view === 'employees' ? '04 Employees' :
      view === 'lines' ? '05 Production Lines' : view
    }>
      {view === 'login' && <Login nav={nav} />}
      {view !== 'login' && (
        <>
          <Topbar nav={nav} />
          {view === 'dashboard' && role === 'manager' && <ManagerDashboard nav={nav} />}
          {view === 'dashboard' && role === 'operator' && <OperatorDashboard nav={nav} />}
          {view === 'builder' && <TaskBuilder nav={nav} />}
          {view === 'employees' && <Employees nav={nav} />}
          {view === 'lines' && <LinesManager nav={nav} />}
        </>
      )}
      <TweaksPanel />
    </div>
  );
}

Object.assign(window, { App });
