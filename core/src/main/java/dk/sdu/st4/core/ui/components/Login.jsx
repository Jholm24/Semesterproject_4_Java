function Login({ nav }) {
  const [tab, setTab] = useState('manager');
  const [username, setUsername] = useState(tab === 'manager' ? 'mikkel.h' : 'lars.o');
  const [password, setPassword] = useState('••••••••');
  const [err, setErr] = useState('');

  React.useEffect(() => {
    setUsername(tab === 'manager' ? 'mikkel.h' : 'lars.o');
  }, [tab]);

  const submit = (e) => {
    e && e.preventDefault();
    if (!username || !password) { setErr('Please fill in all fields'); return; }
    nav.setRole(tab);
    nav.setUser(username);
    nav.setView(tab === 'manager' ? 'dashboard' : 'dashboard');
  };

  return (
    <div className="login-screen">
      <div className="login-left">
        <div className="login-brand">
          <div className="brand-mark">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect x="1" y="10" width="26" height="8" rx="1.5" stroke="currentColor" strokeWidth="1.6"/>
              <circle cx="7" cy="22" r="3" stroke="currentColor" strokeWidth="1.6"/>
              <circle cx="21" cy="22" r="3" stroke="currentColor" strokeWidth="1.6"/>
              <path d="M4 10 L24 10" stroke="currentColor" strokeWidth="1.6"/>
            </svg>
          </div>
          <div>
            <div className="brand-name">Skateboard Productions</div>
            <div className="brand-sub">Proud producer of many products</div>
          </div>
        </div>

        <div className="login-card">
          <div className="login-tabs">
            <button className={`ltab ${tab==='manager'?'on':''}`} onClick={() => setTab('manager')}>
              <span className="ltab-ic">◇</span> Manager
            </button>
            <button className={`ltab ${tab==='operator'?'on':''}`} onClick={() => setTab('operator')}>
              <span className="ltab-ic">◈</span> Operatør
            </button>
          </div>

          <form onSubmit={submit} className="login-form">
            <label className="lf">
              <span className="lf-label">Brugernavn</span>
              <input value={username} onChange={e=>{setUsername(e.target.value); setErr('');}} autoComplete="off"/>
            </label>
            <label className="lf">
              <span className="lf-label">Adgangskode</span>
              <input type="password" value={password} onChange={e=>{setPassword(e.target.value); setErr('');}}/>
            </label>
            <div className="lf-row">
              <label className="lf-check">
                <input type="checkbox" defaultChecked/> Husk mig
              </label>
              <a className="lf-forgot" href="#" onClick={e=>e.preventDefault()}>Glemt adgangskode?</a>
            </div>
            {err && <div className="lf-err">{err}</div>}
            <button type="submit" className="lf-submit">
              Log ind som {tab === 'manager' ? 'Manager' : 'Operatør'} →
            </button>
          </form>

          <div className="login-foot">
            <div className="lf-chip"><span className="dot dot-ok"/> Auth-service tilsluttet</div>
            <div className="lf-chip"><span className="dot"/> JWT · v2.4.1</div>
          </div>
        </div>
      </div>

      <div className="login-right">
        <div className="lr-grid" aria-hidden="true"/>
        <div className="lr-meta">
          <div className="lr-meta-row">
            <span className="mono">SYS</span>
            <span>Production Line Integration</span>
          </div>
          <div className="lr-meta-row">
            <span className="mono">MOD</span>
            <span>AGV · Warehouse · Assembly</span>
          </div>
          <div className="lr-meta-row">
            <span className="mono">VER</span>
            <span>2.4.1 — build 2026.04.20</span>
          </div>
        </div>

        <div className="lr-diagram">
          <div className="ld-node ld-wh"><div className="ld-tag">01 · WAREHOUSE</div><div className="ld-name">SOAP · :8081</div></div>
          <div className="ld-arrow"/>
          <div className="ld-node ld-agv"><div className="ld-tag">02 · AGV</div><div className="ld-name">REST · :8082</div></div>
          <div className="ld-arrow"/>
          <div className="ld-node ld-as"><div className="ld-tag">03 · ASSEMBLY</div><div className="ld-name">MQTT · :1883</div></div>
        </div>

        <div className="lr-foot mono">
          © 2026 SDU · ST4 · Skateboard Assembly Line
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { Login });
