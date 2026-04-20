function Topbar({ nav }) {
  const goto = (v) => () => nav.setView(v);
  const active = nav.view;
  const [notifOpen, setNotifOpen] = React.useState(false);
  const [notifPos, setNotifPos] = React.useState({ top: 56, right: 24 });
  const notifRef = React.useRef(null);
  const btnRef = React.useRef(null);

  const DEFAULT_LINES = [
    { id: 'line-1', name: 'Line-01 · Skateboard' },
    { id: 'line-2', name: 'Line-02 · Desk Lamp' },
    { id: 'line-3', name: 'Line-03 · Pending' },
  ];
  const [LINES, setLines] = React.useState(() => {
    try {
      const raw = localStorage.getItem('sb_lines');
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length) return parsed.map(l => ({ id: l.id, name: l.name }));
      }
    } catch(e) {}
    return DEFAULT_LINES;
  });
  React.useEffect(() => {
    const read = () => {
      try {
        const raw = localStorage.getItem('sb_lines');
        if (!raw) { setLines(DEFAULT_LINES); return; }
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length) setLines(parsed.map(l => ({ id: l.id, name: l.name })));
      } catch(e) {}
    };
    read();
    window.addEventListener('sb-lines-change', read);
    window.addEventListener('storage', read);
    const id = setInterval(read, 1500);
    return () => {
      window.removeEventListener('sb-lines-change', read);
      window.removeEventListener('storage', read);
      clearInterval(id);
    };
  }, []);
  const currentLine = LINES.find(l => l.id === nav.activeLine) || LINES[0];

  // Subscribe to the per-line state (persisted + broadcast by Dashboard) so the status dot is live.
  const [lineStatus, setLineStatus] = React.useState(() => {
    try {
      const saved = JSON.parse(localStorage.getItem('sb_byline_v3') || '{}');
      return saved?.[currentLine.id]?.status || 'standby';
    } catch(e) { return 'standby'; }
  });
  React.useEffect(() => {
    const read = (byLineFromEvent) => {
      try {
        const saved = byLineFromEvent || JSON.parse(localStorage.getItem('sb_byline_v3') || '{}');
        const s = saved?.[currentLine.id]?.status || 'standby';
        setLineStatus(s);
      } catch(e) {}
    };
    read();
    const onChange = (e) => read(e.detail);
    const onStorage = () => read();
    window.addEventListener('sb-byline-change', onChange);
    window.addEventListener('storage', onStorage);
    return () => {
      window.removeEventListener('sb-byline-change', onChange);
      window.removeEventListener('storage', onStorage);
    };
  }, [currentLine.id]);

  const STATUS_META = {
    running: { cls: 'ok',    label: 'RUNNING' },
    paused:  { cls: 'warn',  label: 'PAUSED'  },
    stopped: { cls: 'neut',  label: 'STOPPED' },
    standby: { cls: 'neut',  label: 'STANDBY' },
    alarm:   { cls: 'err',   label: 'ALARM'   },
  };
  const statusMeta = STATUS_META[lineStatus] || STATUS_META.standby;

  const NOTIFS = [
    { id: 'n1', lvl: 'warn', t: '13:58', title: 'AGV 1 · battery below 90%', sub: 'Line-01 · return to dock recommended', unread: true },
    { id: 'n2', lvl: 'err',  t: '13:40', title: 'Line-02 · paused by operator',      sub: 'M. Nørgaard engaged safety stop', unread: true },
    { id: 'n3', lvl: 'ok',   t: '13:12', title: 'Assemble Table 1 · cycle 246 OK',  sub: 'Success rate: 98.4% (last 24h)', unread: false },
    { id: 'n4', lvl: 'info', t: '12:47', title: 'New template saved',                sub: 'Skateboard · Full Cycle v2', unread: false },
    { id: 'n5', lvl: 'info', t: '08:00', title: 'Shift started',                      sub: 'Day shift · 4 operators logged in', unread: false },
  ];
  const unread = NOTIFS.filter(n => n.unread).length;

  React.useEffect(() => {
    if (!notifOpen) return;
    const anchor = () => {
      if (!btnRef.current) return;
      const r = btnRef.current.getBoundingClientRect();
      // Right-align popup to bell, but keep clear of control-board sidebar (220+24=244 from right).
      const minRightGap = 260; // px from viewport right to popup's right edge
      const desiredRight = window.innerWidth - r.right;
      const right = Math.max(desiredRight, minRightGap);
      setNotifPos({ top: r.bottom + 10, right });
    };
    anchor();
    window.addEventListener('resize', anchor);
    window.addEventListener('scroll', anchor, true);
    return () => { window.removeEventListener('resize', anchor); window.removeEventListener('scroll', anchor, true); };
  }, [notifOpen]);

  React.useEffect(() => {
    if (!notifOpen) return;
    const onDown = (e) => { if (notifRef.current && !notifRef.current.contains(e.target)) setNotifOpen(false); };
    const onKey  = (e) => { if (e.key === 'Escape') setNotifOpen(false); };
    document.addEventListener('mousedown', onDown);
    document.addEventListener('keydown', onKey);
    return () => { document.removeEventListener('mousedown', onDown); document.removeEventListener('keydown', onKey); };
  }, [notifOpen]);

  return (
    <header className="topbar">
      <div className="tb-left">
        <button className="tb-brand" onClick={goto('dashboard')}>
          <svg width="22" height="22" viewBox="0 0 28 28" fill="none" aria-hidden="true">
            <rect x="1" y="10" width="26" height="8" rx="1.5" stroke="currentColor" strokeWidth="1.8"/>
            <circle cx="7" cy="22" r="3" stroke="currentColor" strokeWidth="1.8"/>
            <circle cx="21" cy="22" r="3" stroke="currentColor" strokeWidth="1.8"/>
          </svg>
          <span className="tb-brand-name">Skateboard Productions</span>
        </button>
        <div className="tb-line-sel-wrap" title={`Current line · ${statusMeta.label}`}>
          <span className={'tb-line-status tb-line-status-' + statusMeta.cls} aria-label={statusMeta.label} />
          <span className="mono tb-line-k">LINE</span>
          <select className="tb-line-sel" value={currentLine.id} onChange={e => nav.setActiveLine(e.target.value)}>
            {LINES.map(L => <option key={L.id} value={L.id}>{L.name}</option>)}
          </select>
        </div>
        {nav.role === 'manager' && (
          <nav className="tb-nav">
            <button className={active==='dashboard'?'on':''} onClick={goto('dashboard')}>Overview</button>
            <button className={active==='lines'?'on':''} onClick={goto('lines')}>Production Lines</button>
            <button className={active==='builder'?'on':''} onClick={goto('builder')}>Task Builder</button>
            <button className={active==='employees'?'on':''} onClick={goto('employees')}>Employees</button>
          </nav>
        )}
      </div>
      <div className="tb-right">
        <div className="tb-env mono">
          <span className="dot dot-ok"/> ENV · prod · 2.4.1
        </div>
        <div className="tb-notif-wrap" ref={notifRef}>
          <button
            ref={btnRef}
            className={'tb-ic' + (notifOpen ? ' tb-ic-on' : '')}
            title="Notifications"
            onClick={() => setNotifOpen(o => !o)}
            aria-expanded={notifOpen}
          >
            <svg width="16" height="16" viewBox="0 0 20 20" fill="none">
              <path d="M4 15 L16 15 M6 15 V9 A4 4 0 0 1 14 9 V15 M8 17 A2 2 0 0 0 12 17" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            {unread > 0 && <span className="tb-badge">{unread}</span>}
          </button>
          {notifOpen && (
            <div
              className="notif-pop"
              role="dialog"
              aria-label="Notifications"
              style={{ top: notifPos.top + 'px', right: notifPos.right + 'px' }}
            >
              <header className="notif-head">
                <div>
                  <div className="sec-kicker mono">INBOX</div>
                  <div className="notif-title">Notifications</div>
                </div>
                <button className="notif-mark mono">Mark all read</button>
              </header>
              <ul className="notif-list">
                {NOTIFS.map(n => (
                  <li key={n.id} className={'notif-item notif-' + n.lvl + (n.unread ? ' notif-unread' : '')}>
                    <span className={'notif-dot notif-dot-' + n.lvl}/>
                    <div className="notif-body">
                      <div className="notif-row">
                        <div className="notif-t">{n.title}</div>
                        <div className="mono notif-time">{n.t}</div>
                      </div>
                      <div className="notif-sub">{n.sub}</div>
                    </div>
                  </li>
                ))}
              </ul>

            </div>
          )}
        </div>
        <div className="tb-user">
          <div className="tb-avatar">{(nav.user||'?').slice(0,2).toUpperCase()}</div>
          <div className="tb-user-meta">
            <div className="tb-user-name">{nav.user}</div>
            <div className="tb-user-role mono">{nav.role === 'manager' ? 'MANAGER' : 'OPERATOR'}</div>
          </div>
        </div>
        <button className="tb-logout" onClick={nav.logout}>Sign out</button>
      </div>
    </header>
  );
}

Object.assign(window, { Topbar });
