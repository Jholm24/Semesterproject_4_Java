const MACHINE_TYPES = {
  warehouse: {
    id: 'warehouse', label: 'Warehouse', accent: 'var(--c-wh)', accentSoft: 'var(--c-wh-soft)',
    icon: (
      <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
        <path d="M2 6 L8 2 L14 6 V14 H2 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/>
        <path d="M6 14 V9 H10 V14" stroke="currentColor" strokeWidth="1.5"/>
      </svg>
    ),
    badge: 'Parts', protocol: 'SOAP · :8081'
  },
  agv: {
    id: 'agv', label: 'AGV', accent: 'var(--c-agv)', accentSoft: 'var(--c-agv-soft)',
    icon: (
      <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
        <rect x="2" y="5" width="12" height="6" rx="1" stroke="currentColor" strokeWidth="1.5"/>
        <circle cx="5" cy="13" r="1.5" stroke="currentColor" strokeWidth="1.5"/>
        <circle cx="11" cy="13" r="1.5" stroke="currentColor" strokeWidth="1.5"/>
      </svg>
    ),
    badge: 'Mobile', protocol: 'REST · :8082'
  },
  assembly: {
    id: 'assembly', label: 'Assemble Table', accent: 'var(--c-as)', accentSoft: 'var(--c-as-soft)',
    icon: (
      <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
        <path d="M2 12 H14 M4 12 V8 M12 12 V8 M3 8 H13 L11 4 H5 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/>
      </svg>
    ),
    badge: 'Station', protocol: 'MQTT · :1883'
  },
};

const MACHINE_POOL = {
  warehouse: [
    { id: 'wh-1', type: 'warehouse', name: 'Parts Warehouse 01', status: 'online', capacity: 78, serial: 'WH-A7·4821', location: 'Hall A · Bay 1' },
    { id: 'wh-2', type: 'warehouse', name: 'Parts Warehouse 02', status: 'online', capacity: 92, serial: 'WH-A7·4822', location: 'Hall A · Bay 2' },
    { id: 'wh-3', type: 'warehouse', name: 'Parts Warehouse 03', status: 'online', capacity: 100, serial: 'WH-A7·4823', location: 'Hall A · Bay 3' },
    { id: 'wh-4', type: 'warehouse', name: 'Parts Warehouse 04', status: 'online', capacity: 56, serial: 'WH-B2·4824', location: 'Hall B · Bay 1' },
    { id: 'wh-5', type: 'warehouse', name: 'Parts Warehouse 05', status: 'online', capacity: 100, serial: 'WH-B2·4825', location: 'Hall B · Bay 2' },
  ],
  agv: [
    { id: 'agv-1', type: 'agv', name: 'AGV 01', status: 'active', stateLabel: 'Running', battery: 87, serial: 'AGV-V3·7781', location: 'Track N' },
    { id: 'agv-2', type: 'agv', name: 'AGV 02', status: 'idle', stateLabel: 'Docked', battery: 100, serial: 'AGV-V3·7782', location: 'Dock 2' },
    { id: 'agv-3', type: 'agv', name: 'AGV 03', status: 'idle', stateLabel: 'Docked', battery: 94, serial: 'AGV-V3·7783', location: 'Dock 3' },
    { id: 'agv-4', type: 'agv', name: 'AGV 04', status: 'idle', stateLabel: 'Idle', battery: 72, serial: 'AGV-V3·7784', location: 'Track S' },
    { id: 'agv-5', type: 'agv', name: 'AGV 05', status: 'idle', stateLabel: 'Idle', battery: 100, serial: 'AGV-V3·7785', location: 'Dock 5' },
  ],
  assembly: [
    { id: 'as-1', type: 'assembly', name: 'Assemble Table 01', status: 'active', stateLabel: 'Operational', activeTasks: 3, completed: 28, serial: 'AS-T1·2201', location: 'Station 1' },
    { id: 'as-2', type: 'assembly', name: 'Assemble Table 02', status: 'idle', stateLabel: 'Paused', activeTasks: 0, completed: 128, serial: 'AS-T1·2202', location: 'Station 2' },
    { id: 'as-3', type: 'assembly', name: 'Assemble Table 03', status: 'idle', stateLabel: 'Ready', activeTasks: 0, completed: 0, serial: 'AS-T1·2203', location: 'Station 3' },
    { id: 'as-4', type: 'assembly', name: 'Assemble Table 04', status: 'idle', stateLabel: 'Ready', activeTasks: 0, completed: 0, serial: 'AS-T2·2204', location: 'Station 4' },
    { id: 'as-5', type: 'assembly', name: 'Assemble Table 05', status: 'idle', stateLabel: 'Ready', activeTasks: 0, completed: 0, serial: 'AS-T2·2205', location: 'Station 5' },
  ],
};

const LINES = [
  { id: 'line-1', name: 'Line-01 · Skateboard', product: 'Pro Deck 8.0"' },
  { id: 'line-2', name: 'Line-02 · Desk Lamp', product: 'Studio Lamp v2' },
  { id: 'line-3', name: 'Line-03 · Pending', product: '—' },
];

const LINE_DEFAULTS = {
  'line-1': {
    status: 'running', cycles: 247, warnings: 2, success: 98.4,
    machines: ['wh-1', 'agv-1', 'as-1'],
    log: [
      { t: '14:02:18', lvl: 'ok', m: 'AGV 01 · MOVE_TO_ASSEMBLY completed' },
      { t: '14:02:11', lvl: 'info', m: 'Warehouse 01 · picked tray #4821' },
      { t: '14:01:47', lvl: 'ok', m: 'Assemble Table 01 · cycle 247 accepted' },
      { t: '13:58:02', lvl: 'warn', m: 'AGV 01 · battery below 90%' },
    ],
  },
  'line-2': {
    status: 'paused', cycles: 128, warnings: 0, success: 97.1,
    machines: ['wh-2', 'agv-2', 'agv-3', 'as-2'],
    log: [
      { t: '13:40:02', lvl: 'info', m: 'Line-02 · paused by operator M. Jensen' },
      { t: '13:39:48', lvl: 'ok', m: 'Assemble Table 02 · cycle 128 accepted' },
      { t: '13:38:12', lvl: 'ok', m: 'AGV 03 · returned to dock' },
    ],
  },
  'line-3': {
    status: 'standby', cycles: 0, warnings: 0, success: 0,
    machines: [],
    log: [
      { t: '09:00:00', lvl: 'info', m: 'Line-03 · awaiting product assignment' },
    ],
  },
};

function ManagerDashboard({ nav }) {
  const currentLine = LINES.find(l => l.id === nav.activeLine) || LINES[0];

  const [byLine, setByLine] = useState(() => {
    const saved = localStorage.getItem('sb_byline_v3');
    if (saved) { try { return JSON.parse(saved); } catch(e){} }
    return LINE_DEFAULTS;
  });

  useEffect(() => {
    localStorage.setItem('sb_byline_v3', JSON.stringify(byLine));
    window.dispatchEvent(new CustomEvent('sb-byline-change', { detail: byLine }));
  }, [byLine]);

  const L = byLine[currentLine.id] || LINE_DEFAULTS[currentLine.id];
  const { machines: machineIds, status: lineStatus, cycles, warnings, success, log } = L;

  // hydrate machine objects from pool by id
  const poolIndex = useMemo(() => {
    const idx = {};
    Object.values(MACHINE_POOL).flat().forEach(m => { idx[m.id] = m; });
    return idx;
  }, []);
  const liveOverrides = L.overrides || {};
  const machines = machineIds.map(id => ({ ...(poolIndex[id] || {}), ...(liveOverrides[id] || {}) })).filter(m => m.id);

  // which ids are claimed across ALL lines
  const occupiedIds = useMemo(() => {
    const s = new Set();
    Object.values(byLine).forEach(lineData => {
      (lineData.machines || []).forEach(id => s.add(id));
    });
    return s;
  }, [byLine]);

  const availableByType = {
    warehouse: MACHINE_POOL.warehouse.filter(m => !occupiedIds.has(m.id)),
    agv:       MACHINE_POOL.agv.filter(m => !occupiedIds.has(m.id)),
    assembly:  MACHINE_POOL.assembly.filter(m => !occupiedIds.has(m.id)),
  };

  const patchLine = (patch) => setByLine(b => ({ ...b, [currentLine.id]: { ...b[currentLine.id], ...patch } }));
  const setMachines = (next) => patchLine({ machines: typeof next === 'function' ? next(machineIds) : next });
  const setLineStatus = (s) => patchLine({ status: s });
  const setLog = (next) => patchLine({ log: typeof next === 'function' ? next(L.log) : next });

  // Simulate live updates while running
  useEffect(() => {
    if (lineStatus !== 'running') return;
    const id = setInterval(() => {
      setByLine(b => {
        const cur = b[currentLine.id]; if (!cur) return b;
        const ov = { ...(cur.overrides || {}) };
        (cur.machines || []).forEach(mid => {
          const base = poolIndex[mid]; if (!base) return;
          const existing = ov[mid] || {};
          if (base.type === 'agv') ov[mid] = { ...existing, battery: Math.max(12, (existing.battery ?? base.battery ?? 87) - 0.3) };
          if (base.type === 'warehouse') ov[mid] = { ...existing, capacity: Math.max(5, (existing.capacity ?? base.capacity ?? 78) - 0.5) };
          if (base.type === 'assembly') ov[mid] = { ...existing, completed: (existing.completed ?? base.completed ?? 0) + 1 };
        });
        return { ...b, [currentLine.id]: { ...cur, cycles: cur.cycles + 1, overrides: ov } };
      });
    }, 1400);
    return () => clearInterval(id);
  }, [lineStatus, currentLine.id, poolIndex]);

  const addMachineById = (poolId) => {
    if (!poolId) return;
    if (machineIds.includes(poolId)) return;
    setMachines(ids => [...ids, poolId]);
  };
  const removeMachine = (id) => setMachines(ids => ids.filter(x => x !== id));

  const doControl = (cmd) => {
    const now = new Date();
    const t = now.toTimeString().slice(0,8);
    if (cmd === 'start') { setLineStatus('running'); setLog(l => [{t, lvl:'ok', m: 'Line started — production queue initiated'}, ...l].slice(0,12)); }
    if (cmd === 'pause') { setLineStatus('paused'); setLog(l => [{t, lvl:'info', m: 'Line paused'}, ...l].slice(0,12)); }
    if (cmd === 'stop') { setLineStatus('stopped'); setLog(l => [{t, lvl:'warn', m: 'Line stopped — parked in safe state'}, ...l].slice(0,12)); }
    if (cmd === 'abort') { patchLine({ status: 'alarm', warnings: (L.warnings||0)+1, log: [{t, lvl:'err', m: 'ABORT — emergency stop engaged'}, ...L.log].slice(0,12) }); }
  };

  const grouped = { warehouse: [], agv: [], assembly: [] };
  machines.forEach(m => grouped[m.type] && grouped[m.type].push(m));
  const [openMachine, setOpenMachine] = useState(null);
  const liveOpenMachine = openMachine ? machines.find(m => m.id === openMachine.id) : null;

  return (
    <main className="dash">
      <div className="dash-main">
        <StatsBar cycles={cycles} success={success} warnings={warnings} lineStatus={lineStatus} currentLine={currentLine} />
        <ComponentManager grouped={grouped} available={availableByType} onAdd={addMachineById} />
        <MachineGrid grouped={grouped} removeMachine={removeMachine} lineStatus={lineStatus} onOpen={(m) => setOpenMachine(m)} />
        <EventLog log={log} />
      </div>
      <aside className="dash-aside">
        <ControlBoard lineStatus={lineStatus} doControl={doControl} />
        <button className="task-builder-btn" onClick={() => nav.setView('builder')}>
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
            <path d="M2 4 H14 M2 8 H10 M2 12 H14" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            <circle cx="13" cy="8" r="1.5" fill="currentColor"/>
          </svg>
          Task Builder
        </button>
      </aside>
      {liveOpenMachine && <MachineDetail machine={liveOpenMachine} lineStatus={lineStatus} onClose={() => setOpenMachine(null)} onRemove={() => { removeMachine(liveOpenMachine.id); setOpenMachine(null); }} />}
    </main>
  );
}

function StatsBar({ cycles, success, warnings, lineStatus, currentLine }) {
  const statusLabels = { standby:'Standby', running:'Running', paused:'Paused', stopped:'Stopped', alarm:'ALARM', idle:'Idle' };
  return (
    <section className="stats-bar">
      <div className="stats-head">
        <div className="stats-head-left">
          <div className="sec-kicker mono">01 · OVERVIEW {currentLine ? '· ' + currentLine.name.toUpperCase() : ''}</div>
          <h1 className="sec-title">Process Overview</h1>
          {currentLine && <div className="sec-sub mono">Producing → {currentLine.product}</div>}
        </div>
        <div className={`line-status line-status-${lineStatus}`}>
          <span className="ls-pulse"/>
          <span className="mono ls-label">STATUS</span>
          <span className="ls-value">{statusLabels[lineStatus] || lineStatus}</span>
        </div>
      </div>
      <div className="stats-grid">
        <Stat icon="pulse" label="Total Cycles" value={cycles} />
        <Stat icon="check" label="Success Rate" value={success.toFixed(1)+'%'} sub="last 24h" tone="ok"/>
        <Stat icon="alert" label="Warnings" value={warnings} tone={warnings>0?'warn':'ok'}/>
        <Stat icon="clock" label="Avg. Cycle Time" value="42.6s" sub="target: <45s" tone="ok"/>
      </div>
    </section>
  );
}

function Stat({ icon, label, value, sub, tone }) {
  const icons = {
    pulse: <path d="M1 8 H4 L6 3 L10 13 L12 8 H15" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>,
    check: <path d="M3 8 L7 12 L13 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" fill="none"/>,
    alert: <><path d="M8 3 V9" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/><circle cx="8" cy="12" r="1" fill="currentColor"/></>,
    clock: <><circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5" fill="none"/><path d="M8 5 V8 L10 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/></>,
  };
  return (
    <div className={`stat stat-${tone||''}`}>
      <div className="stat-icon">
        <svg width="16" height="16" viewBox="0 0 16 16">{icons[icon]}</svg>
      </div>
      <div className="stat-body">
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {sub && <div className="stat-sub mono">{sub}</div>}
      </div>
    </div>
  );
}

function ComponentManager({ grouped, available, onAdd }) {
  const [openType, setOpenType] = React.useState(null);
  const rootRef = React.useRef(null);
  React.useEffect(() => {
    if (!openType) return;
    const onDown = (e) => { if (rootRef.current && !rootRef.current.contains(e.target)) setOpenType(null); };
    const onKey = (e) => { if (e.key === 'Escape') setOpenType(null); };
    document.addEventListener('mousedown', onDown);
    document.addEventListener('keydown', onKey);
    return () => { document.removeEventListener('mousedown', onDown); document.removeEventListener('keydown', onKey); };
  }, [openType]);

  return (
    <section className="kompo" ref={rootRef}>
      <div className="kompo-head">
        <div>
          <div className="sec-kicker mono">02 · COMPONENTS</div>
          <h2 className="sec-title">Component Manager</h2>
        </div>
        <div className="kompo-hint mono">Pool: 5 of each type · select an available unit</div>
      </div>
      <div className="kompo-row">
        {Object.values(MACHINE_TYPES).map(t => {
          const avail = available[t.id] || [];
          const isOpen = openType === t.id;
          return (
            <div key={t.id} className={'kompo-card' + (isOpen ? ' kompo-open' : '')} style={{'--c': t.accent, '--cs': t.accentSoft}}>
              <div className="kompo-ic">{t.icon}</div>
              <div className="kompo-meta">
                <div className="kompo-label">{t.label}</div>
                <div className="kompo-sub mono">{grouped[t.id].length} on line · {avail.length}/5 available</div>
              </div>
              <button
                className="kompo-add"
                onClick={() => setOpenType(o => o === t.id ? null : t.id)}
                disabled={avail.length === 0}
                title={avail.length === 0 ? 'All units occupied by other lines' : 'Add available unit'}
              >
                <span>{avail.length === 0 ? '—' : '+'}</span> {avail.length === 0 ? 'Full' : 'Add'}
                <svg className="kompo-caret" width="9" height="9" viewBox="0 0 9 9" fill="none"><path d="M1.5 3 L4.5 6 L7.5 3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
              </button>
              {isOpen && (
                <div className="kompo-pop" role="menu">
                  <div className="kompo-pop-head">
                    <span className="mono kompo-pop-k">AVAILABLE {t.label.toUpperCase()} UNITS</span>
                    <span className="mono kompo-pop-count">{avail.length} / 5</span>
                  </div>
                  {avail.length === 0 ? (
                    <div className="kompo-pop-empty mono">All 5 units are currently occupied by other production lines.</div>
                  ) : (
                    <ul className="kompo-pop-list">
                      {avail.map(u => (
                        <li key={u.id}>
                          <button className="kompo-pop-item" onClick={() => { onAdd(u.id); setOpenType(null); }}>
                            <span className="kompo-pop-ic" style={{background: t.accentSoft, color: t.accent}}>{t.icon}</span>
                            <span className="kompo-pop-body">
                              <span className="kompo-pop-name">{u.name}</span>
                              <span className="mono kompo-pop-meta">{u.serial} · {u.location}</span>
                            </span>
                            <span className="kompo-pop-add mono">+ Add</span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
}

function MachineGrid({ grouped, removeMachine, lineStatus, onOpen }) {
  return (
    <section className="grid3">
      {Object.values(MACHINE_TYPES).map(t => (
        <div key={t.id} className="col" style={{'--c': t.accent, '--cs': t.accentSoft}}>
          <header className="col-head">
            <span className="col-ic">{t.icon}</span>
            <span className="col-title">{t.label} ({grouped[t.id].length})</span>
            <span className="col-dots">· · ·</span>
          </header>
          <div className="col-body">
            {grouped[t.id].length === 0 && (
              <div className="col-empty">
                <div className="col-empty-box">No {t.label.toLowerCase()}</div>
                <div className="mono col-empty-sub">Add via Component Manager ↑</div>
              </div>
            )}
            {grouped[t.id].map(m => <MachineCard key={m.id} machine={m} onRemove={() => removeMachine(m.id)} onOpen={() => onOpen(m)} lineStatus={lineStatus}/>)}
          </div>
        </div>
      ))}
    </section>
  );
}

function MachineCard({ machine, onRemove, onOpen, lineStatus }) {
  const t = MACHINE_TYPES[machine.type];
  const statusTone = machine.status === 'online' || machine.status === 'active' ? 'ok' : 'idle';
  const handleCardClick = (e) => {
    if (e.target.closest('.mcard-x')) return;
    onOpen && onOpen();
  };
  return (
    <article className="mcard mcard-click" onClick={handleCardClick} role="button" tabIndex={0} onKeyDown={(e) => { if (e.key==='Enter' || e.key===' ') { e.preventDefault(); onOpen && onOpen(); } }}>
      <header className="mcard-head">
        <span className="mcard-ic">{t.icon}</span>
        <span className="mcard-name">{machine.name}</span>
        <span className={`mcard-badge mcard-badge-${statusTone}`}>{t.badge}</span>
        <span className={`mcard-status mono mcard-status-${statusTone}`}>{machine.status === 'online' ? 'Online' : machine.status === 'active' ? 'Active' : 'Idle'}</span>
        <button className="mcard-x" onClick={onRemove} title="Remove">×</button>
      </header>

      {machine.type === 'warehouse' && (
        <div className="mcard-body">
          <div className="mcard-row">
            <span className="mcard-k">Capacity</span>
            <span className="mcard-v mono">{machine.capacity.toFixed(0)}%</span>
          </div>
          <div className="mcard-bar"><div className="mcard-bar-fill" style={{width: machine.capacity+'%'}}/></div>
          <div className="mcard-meta mono">
            <span>TRAYS · 14/18</span>
            <span>IN · 42 · OUT · 37</span>
          </div>
        </div>
      )}

      {machine.type === 'agv' && (
        <div className="mcard-body">
          <div className="mcard-row">
            <span className="mcard-k">Status</span>
            <span className="mcard-v mono">{machine.stateLabel}</span>
          </div>
          <div className="mcard-row">
            <span className="mcard-k">Battery</span>
            <span className="mcard-v mono">{Math.round(machine.battery)}%</span>
          </div>
          <div className="mcard-bar"><div className="mcard-bar-fill" style={{width: machine.battery+'%'}}/></div>
          <div className="mcard-meta mono">
            <span>PROG · MoveToAssembly</span>
            <span>POS · B·02</span>
          </div>
        </div>
      )}

      {machine.type === 'assembly' && (
        <div className="mcard-body">
          <div className="mcard-row">
            <span className="mcard-k">Status</span>
            <span className="mcard-v mono">{machine.stateLabel}</span>
          </div>
          <div className="mcard-row">
            <span className="mcard-k">Active Tasks</span>
            <span className="mcard-v mono">{machine.activeTasks}</span>
          </div>
          <div className="mcard-row">
            <span className="mcard-k">Completed</span>
            <span className="mcard-v mono">{machine.completed}</span>
          </div>
          <div className="mcard-meta mono">
            <span>TOPIC · emulator/status</span>
            <span>QoS · 1</span>
          </div>
        </div>
      )}
    </article>
  );
}

function EventLog({ log }) {
  return (
    <section className="evlog">
      <header className="evlog-head">
        <div>
          <div className="sec-kicker mono">03 · EVENT LOG</div>
          <h2 className="sec-title">Live Activity</h2>
        </div>
        <div className="mono evlog-sub">latest 10 events</div>
      </header>
      <ul className="evlog-list">
        {log.map((e, i) => (
          <li key={i} className={`ev ev-${e.lvl}`}>
            <span className="ev-time mono">{e.t}</span>
            <span className="ev-dot"/>
            <span className="ev-msg">{e.m}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}

function ControlBoard({ lineStatus, doControl }) {
  const btns = [
    { k:'start', label:'Start', cls:'cb-start', ic: <path d="M5 3 L13 8 L5 13 Z" fill="currentColor"/> },
    { k:'pause', label:'Pause', cls:'cb-pause', ic: <><rect x="4" y="3" width="3" height="10" fill="currentColor"/><rect x="9" y="3" width="3" height="10" fill="currentColor"/></> },
    { k:'stop', label:'Stop', cls:'cb-stop', ic: <rect x="3" y="3" width="10" height="10" fill="currentColor"/> },
    { k:'abort', label:'Abort', cls:'cb-alarm', ic: <path d="M8 2 L14 13 H2 Z M8 6 V10 M8 12 V12.5" stroke="currentColor" strokeWidth="1.8" fill="none" strokeLinejoin="round"/> },
  ];
  return (
    <div className="cboard">
      <header className="cboard-head">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5"/>
          <path d="M8 4 V8 L10.5 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
        </svg>
        <span>Control Board</span>
      </header>
      <div className="cboard-status">
        <span className="mono cboard-k">STATUS</span>
        <span className={`cboard-chip cboard-chip-${lineStatus}`}>{lineStatus.toUpperCase()}</span>
      </div>
      <div className="cboard-btns">
        {btns.map(b => (
          <button key={b.k} className={`cbtn ${b.cls}`} onClick={() => doControl(b.k)}>
            <svg width="16" height="16" viewBox="0 0 16 16">{b.ic}</svg>
            <span>{b.label}</span>
          </button>
        ))}
      </div>
      <footer className="cboard-foot mono">
        EMERGENCY · press Abort to halt
      </footer>
    </div>
  );
}

Object.assign(window, { ManagerDashboard, StatsBar, ComponentManager, MachineGrid, MachineCard, MachineDetail, EventLog, ControlBoard, MACHINE_TYPES });

function MachineDetail({ machine, lineStatus, onClose, onRemove }) {
  const t = MACHINE_TYPES[machine.type];
  React.useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = prev; };
  }, [onClose]);

  const statusLabel = machine.status === 'online' ? 'Online' : machine.status === 'active' ? 'Active' : 'Idle';
  const statusTone  = machine.status === 'online' || machine.status === 'active' ? 'ok' : 'idle';

  const telemetry = machine.type === 'warehouse' ? [
    { k: 'CAPACITY',    v: Math.round(machine.capacity) + '%' },
    { k: 'TRAYS',       v: '14 / 18' },
    { k: 'IN / OUT',    v: '42 / 37' },
    { k: 'TEMP',        v: '21.3 °C' },
    { k: 'UPTIME',      v: '14d 02h' },
    { k: 'PROTOCOL',    v: 'SOAP · :8081' },
  ] : machine.type === 'agv' ? [
    { k: 'STATE',       v: machine.stateLabel },
    { k: 'BATTERY',     v: Math.round(machine.battery) + '%' },
    { k: 'PROGRAM',     v: 'MoveToAssembly' },
    { k: 'POSITION',    v: 'B · 02' },
    { k: 'SPEED',       v: '0.8 m/s' },
    { k: 'PROTOCOL',    v: 'REST · :8082' },
  ] : [
    { k: 'STATE',       v: machine.stateLabel },
    { k: 'ACTIVE',      v: machine.activeTasks },
    { k: 'COMPLETED',   v: machine.completed },
    { k: 'CURRENT OP',  v: 'START_OPERATION' },
    { k: 'CYCLE TIME',  v: '42.6 s' },
    { k: 'PROTOCOL',    v: 'MQTT · :1883' },
  ];

  const activity = [
    { t: '14:02:18', m: machine.type === 'agv' ? 'MOVE_TO_ASSEMBLY completed' : machine.type === 'warehouse' ? 'Picked tray #4821' : 'Cycle 247 accepted', lvl: 'ok' },
    { t: '14:01:47', m: 'Health check responded · 120ms', lvl: 'info' },
    { t: '14:00:12', m: machine.type === 'agv' ? 'Battery dropped below 90%' : machine.type === 'warehouse' ? 'Outlet free · idle 4s' : 'Operation started', lvl: machine.type === 'agv' ? 'warn' : 'ok' },
    { t: '13:58:02', m: 'Status update published', lvl: 'info' },
  ];

  const accent = t.accent;
  const accentSoft = t.accentSoft;

  return (
    <div className="mdetail-backdrop" onClick={onClose}>
      <div className="mdetail" onClick={(e) => e.stopPropagation()} style={{'--accent': accent, '--accent-soft': accentSoft}}>
        <div className="mdetail-accent-bar"/>
        <header className="mdetail-head">
          <div className="mdetail-head-left">
            <span className="mdetail-ic" style={{background: accentSoft, color: accent}}>{t.icon}</span>
            <div>
              <div className="mono mdetail-kicker">{t.label.toUpperCase()} · {machine.id.toUpperCase()}</div>
              <h2 className="mdetail-title">{machine.name}</h2>
              <div className="mdetail-sub">
                <span className={`mdetail-chip mdetail-chip-${statusTone}`}>
                  <span className="mdetail-pulse"/> {statusLabel}
                </span>
                <span className="mono mdetail-meta">{t.protocol}</span>
              </div>
            </div>
          </div>
          <button className="mdetail-close" onClick={onClose} title="Close (Esc)">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M2 2 L12 12 M12 2 L2 12" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            </svg>
          </button>
        </header>

        <div className="mdetail-body">
          <section className="mdetail-sec">
            <div className="sec-kicker mono">TELEMETRY</div>
            <div className="mdetail-telem">
              {telemetry.map((r, i) => (
                <div key={i} className="mdetail-telem-row">
                  <span className="mono mdetail-telem-k">{r.k}</span>
                  <span className="mdetail-telem-v mono">{r.v}</span>
                </div>
              ))}
            </div>
            {machine.type === 'warehouse' && (
              <div className="mdetail-bar-wrap">
                <div className="mdetail-bar"><div className="mdetail-bar-fill" style={{width: machine.capacity + '%', background: accent}}/></div>
                <div className="mono mdetail-bar-k">Capacity fill</div>
              </div>
            )}
            {machine.type === 'agv' && (
              <div className="mdetail-bar-wrap">
                <div className="mdetail-bar"><div className="mdetail-bar-fill" style={{width: machine.battery + '%', background: accent}}/></div>
                <div className="mono mdetail-bar-k">Battery level</div>
              </div>
            )}
          </section>

          <section className="mdetail-sec">
            <div className="sec-kicker mono">RECENT ACTIVITY</div>
            <ul className="mdetail-log">
              {activity.map((e, i) => (
                <li key={i} className={`mdetail-log-row mdetail-log-${e.lvl}`}>
                  <span className="mono mdetail-log-t">{e.t}</span>
                  <span className="mdetail-log-dot"/>
                  <span className="mdetail-log-m">{e.m}</span>
                </li>
              ))}
            </ul>
          </section>

          <section className="mdetail-sec">
            <div className="sec-kicker mono">ACTIONS</div>
            <div className="mdetail-actions">
              <button className="mdetail-btn mdetail-btn-primary">Send Command</button>
              <button className="mdetail-btn">Check Health</button>
              <button className="mdetail-btn">View Logs</button>
              <button className="mdetail-btn mdetail-btn-danger" onClick={onRemove}>Remove component</button>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
