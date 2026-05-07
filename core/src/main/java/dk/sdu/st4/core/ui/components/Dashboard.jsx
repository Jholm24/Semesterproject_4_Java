const API_BASE = 'http://localhost:8080';

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

// ── API response → UI machine object converters ───────────────────────────────

function toAgvMachine(m) {
  const stateLabels = { Idle: 'Idle', Executing: 'Running', Charging: 'Charging' };
  return {
    id: m.serialNumber,
    type: 'agv',
    name: 'AGV ' + m.serialNumber,
    status: m.poolStatus === 'active' ? 'active' : 'idle',
    stateLabel: stateLabels[m.agvState] || (m.agvState || '—'),
    battery: m.battery,
    serial: m.serialNumber,
    _program: m.program || null,
  };
}

function toWarehouseMachine(m) {
  const stateLabels = { 0: 'Idle', 1: 'Executing', 2: 'Error' };
  return {
    id: m.serialNumber,
    type: 'warehouse',
    name: 'Warehouse ' + m.serialNumber,
    status: m.poolStatus === 'active' ? 'active' : 'idle',
    stateLabel: stateLabels[m.warehouseState] || '—',
    warehouseState: m.warehouseState,
    serial: m.serialNumber,
  };
}

function toAssemblyMachine(m) {
  const stateLabels = { 0: 'Idle', 1: 'Executing', 2: 'Error' };
  return {
    id: m.serialNumber,
    type: 'assembly',
    name: 'Assembly ' + m.serialNumber,
    status: m.poolStatus === 'active' ? 'active' : 'idle',
    stateLabel: stateLabels[m.state] || '—',
    state: m.state,
    healthy: m.healthy,
    operationId: m.operationId,
    lastOperationId: m.lastOperationId,
    serial: m.serialNumber,
  };
}

// ── Lines fetched from DB via /api/lines ──────────────────────────────────────

// ── Manager dashboard ─────────────────────────────────────────────────────────

function ManagerDashboard({ nav }) {
  // Lines config from DB (id, name, product, status, cycles, success, warnings, machines, operators)
  const [dbLines, setDbLines] = useState([]);

  useEffect(() => {
    let active = true;
    const poll = async () => {
      try {
        const res = await fetch(API_BASE + '/api/lines');
        if (!res.ok) return;
        const data = await res.json();
        if (!active) return;
        setDbLines(data);
        // Merge DB line data into byLine state (machines/operators from DB; preserve local UI state)
        setByLine(prev => {
          const next = { ...prev };
          data.forEach(l => {
            if (!next[l.id]) {
              next[l.id] = { status: l.status, cycles: l.cycles, warnings: l.warnings,
                             success: l.success, machines: l.machines, operators: l.operators, log: [] };
            } else {
              next[l.id] = { ...next[l.id], machines: l.machines, operators: l.operators,
                             cycles: l.cycles, success: l.success, warnings: l.warnings };
            }
          });
          return next;
        });
      } catch {}
    };
    poll();
    const id = setInterval(poll, 3000);
    return () => { active = false; clearInterval(id); };
  }, []);

  const currentLine = dbLines.find(l => l.id === nav.activeLine) || dbLines[0] || { id: '', name: '', product: '' };

  const [byLine, setByLine] = useState({});

  const L = byLine[currentLine.id] || { status: 'standby', cycles: 0, warnings: 0, success: 0, machines: [], operators: [], log: [] };
  const { machines: machineIds, status: lineStatus, cycles, warnings, success, log } = L;

  // ── Live machine pool from backend ──────────────────────────────────────
  const [machinePool, setMachinePool] = useState({ agv: [], warehouse: [], assembly: [] });

  useEffect(() => {
    let active = true;
    const poll = async () => {
      try {
        const res = await fetch(API_BASE + '/api/machines');
        if (!res.ok) throw new Error('non-2xx');
        const data = await res.json();
        if (!active) return;
        setMachinePool({
          agv:       (data.agv       || []).map(toAgvMachine),
          warehouse: (data.warehouse || []).map(toWarehouseMachine),
          assembly:  (data.assembly  || []).map(toAssemblyMachine),
        });
      } catch { /* backend offline — pool stays empty */ }
    };
    poll();
    const id = setInterval(poll, 2000);
    return () => { active = false; clearInterval(id); };
  }, []);

  // poolIndex: serialNumber → live machine object
  const poolIndex = useMemo(() => {
    const idx = {};
    Object.values(machinePool).flat().forEach(m => { idx[m.id] = m; });
    return idx;
  }, [machinePool]);

  const machines = (machineIds || []).map(id => poolIndex[id]).filter(Boolean);

  const occupiedIds = useMemo(() => {
    const s = new Set();
    dbLines.forEach(l => (l.machines || []).forEach(id => s.add(id)));
    return s;
  }, [dbLines]);

  const availableByType = {
    warehouse: (machinePool.warehouse || []).filter(m => !occupiedIds.has(m.id)),
    agv:       (machinePool.agv       || []).filter(m => !occupiedIds.has(m.id)),
    assembly:  (machinePool.assembly  || []).filter(m => !occupiedIds.has(m.id)),
  };

  const patchLine = (patch) => setByLine(b => {
    const base = b[currentLine.id] || { status: 'standby', cycles: 0, warnings: 0, success: 0, machines: [], operators: [], log: [] };
    const updated = { ...b, [currentLine.id]: { ...base, ...patch } };
    // Persist status/cycles/success/warnings changes to DB
    if (patch.status !== undefined || patch.cycles !== undefined ||
        patch.success !== undefined || patch.warnings !== undefined) {
      const L2 = updated[currentLine.id];
      fetch(API_BASE + '/api/lines', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: currentLine.id, status: L2.status,
          cycles: L2.cycles || 0, success: L2.success || 0, warnings: L2.warnings || 0 }),
      }).catch(() => {});
    }
    window.dispatchEvent(new CustomEvent('sb-byline-change', { detail: updated }));
    return updated;
  });
  const setMachines = (next) => patchLine({ machines: typeof next === 'function' ? next(machineIds) : next });
  const setLineStatus = (s) => patchLine({ status: s });
  const setLog = (next) => patchLine({ log: typeof next === 'function' ? next(L.log) : next });

  // ── Live backend polling: line status + events ──────────────────────────
  const [backendOnline, setBackendOnline] = useState(false);

  useEffect(() => {
    let active = true;
    const poll = async () => {
      try {
        const res = await fetch(API_BASE + '/api/status');
        if (!res.ok) throw new Error('non-2xx');
        const data = await res.json();
        if (!active) return;
        setBackendOnline(true);
        if (data.lineStatus && data.lineStatus !== lineStatus) {
          patchLine({ status: data.lineStatus });
        }
      } catch { if (active) setBackendOnline(false); }
    };
    poll();
    const id = setInterval(poll, 2000);
    return () => { active = false; clearInterval(id); };
  }, [lineStatus, currentLine.id]);

  useEffect(() => {
    let active = true;
    const poll = async () => {
      try {
        const res = await fetch(API_BASE + '/api/events');
        if (!res.ok) throw new Error('non-2xx');
        const evts = await res.json();
        if (active && evts.length > 0) patchLine({ log: evts.slice(0, 10) });
      } catch { }
    };
    const id = setInterval(poll, 2000);
    return () => { active = false; clearInterval(id); };
  }, [currentLine.id]);

  const addMachineById = (poolId) => {
    if (!poolId) return;
    if (machineIds.includes(poolId)) return;
    setMachines(ids => [...ids, poolId]);
  };
  const removeMachine = (id) => setMachines(ids => ids.filter(x => x !== id));

  const doControl = async (cmd) => {
    const now = new Date();
    const t = now.toTimeString().slice(0,8);
    if (cmd === 'start') { setLineStatus('running'); setLog(l => [{t, lvl:'ok',   m: 'Line started — production queue initiated'}, ...l].slice(0,12)); }
    if (cmd === 'pause') { setLineStatus('paused');  setLog(l => [{t, lvl:'info', m: 'Line paused'}, ...l].slice(0,12)); }
    if (cmd === 'stop')  { setLineStatus('stopped'); setLog(l => [{t, lvl:'warn', m: 'Line stopped — parked in safe state'}, ...l].slice(0,12)); }
    if (cmd === 'abort') { patchLine({ status: 'alarm', warnings: (L.warnings||0)+1, log: [{t, lvl:'err', m: 'ABORT — emergency stop engaged'}, ...L.log].slice(0,12) }); }
    try {
      await fetch(API_BASE + '/api/control', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: cmd }),
      });
    } catch { }
  };

  const grouped = { warehouse: [], agv: [], assembly: [] };
  machines.forEach(m => grouped[m.type] && grouped[m.type].push(m));
  const [openMachine, setOpenMachine] = useState(null);
  const liveOpenMachine = openMachine ? machines.find(m => m.id === openMachine.id) : null;

  return (
    <main className="dash">
      <div className="dash-main">
        <StatsBar cycles={cycles} success={success} warnings={warnings} lineStatus={lineStatus} currentLine={currentLine} backendOnline={backendOnline} />
        <ComponentManager grouped={grouped} available={availableByType} onAdd={addMachineById} machinePool={machinePool} />
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
      {liveOpenMachine && (
        <MachineDetail
          machine={liveOpenMachine}
          lineStatus={lineStatus}
          onClose={() => setOpenMachine(null)}
          onRemove={() => { removeMachine(liveOpenMachine.id); setOpenMachine(null); }}
          activity={log.slice(0, 4)}
        />
      )}
    </main>
  );
}

function StatsBar({ cycles, success, warnings, lineStatus, currentLine, backendOnline }) {
  const statusLabels = { standby:'Standby', running:'Running', paused:'Paused', stopped:'Stopped', alarm:'ALARM', idle:'Idle' };
  return (
    <section className="stats-bar">
      <div className="stats-head">
        <div className="stats-head-left">
          <div className="sec-kicker mono">01 · OVERVIEW {currentLine ? '· ' + currentLine.name.toUpperCase() : ''}</div>
          <h1 className="sec-title">Process Overview</h1>
          {currentLine && <div className="sec-sub mono">Producing → {currentLine.product}</div>}
        </div>
        <div style={{display:'flex', gap:10, alignItems:'center'}}>
          <div className="lf-chip" style={{fontSize:10}}>
            <span className={`dot ${backendOnline ? 'dot-ok' : ''}`}/>
            {backendOnline ? 'Backend · LIVE' : 'Backend · offline'}
          </div>
          <div className={`line-status line-status-${lineStatus}`}>
            <span className="ls-pulse"/>
            <span className="mono ls-label">STATUS</span>
            <span className="ls-value">{statusLabels[lineStatus] || lineStatus}</span>
          </div>
        </div>
      </div>
      <div className="stats-grid">
        <Stat icon="pulse" label="Total Cycles" value={cycles} />
        <Stat icon="check" label="Success Rate" value={(success || 0).toFixed(1)+'%'} sub="last 24h" tone="ok"/>
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

function ComponentManager({ grouped, available, onAdd, machinePool }) {
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
        <div className="kompo-hint mono">Select an available unit from the pool</div>
      </div>
      <div className="kompo-row">
        {Object.values(MACHINE_TYPES).map(t => {
          const avail = available[t.id] || [];
          const total = (machinePool[t.id] || []).length;
          const isOpen = openType === t.id;
          return (
            <div key={t.id} className={'kompo-card' + (isOpen ? ' kompo-open' : '')} style={{'--c': t.accent, '--cs': t.accentSoft}}>
              <div className="kompo-ic">{t.icon}</div>
              <div className="kompo-meta">
                <div className="kompo-label">{t.label}</div>
                <div className="kompo-sub mono">{grouped[t.id].length} on line · {avail.length}/{total} available</div>
              </div>
              <button
                className="kompo-add"
                onClick={() => setOpenType(o => o === t.id ? null : t.id)}
                disabled={avail.length === 0}
                title={total === 0 ? 'No machines in pool' : avail.length === 0 ? 'All units occupied by other lines' : 'Add available unit'}
              >
                <span>{avail.length > 0 ? '+' : '—'}</span> {total > 0 && avail.length === 0 ? 'Full' : avail.length > 0 ? 'Add' : '—'}
                <svg className="kompo-caret" width="9" height="9" viewBox="0 0 9 9" fill="none"><path d="M1.5 3 L4.5 6 L7.5 3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
              </button>
              {isOpen && (
                <div className="kompo-pop" role="menu">
                  <div className="kompo-pop-head">
                    <span className="mono kompo-pop-k">AVAILABLE {t.label.toUpperCase()} UNITS</span>
                    <span className="mono kompo-pop-count">{avail.length} / {total}</span>
                  </div>
                  {avail.length === 0 ? (
                    <div className="kompo-pop-empty mono">{total === 0 ? 'No machines loaded from pool.' : 'All units are currently occupied by other production lines.'}</div>
                  ) : (
                    <ul className="kompo-pop-list">
                      {avail.map(u => (
                        <li key={u.id}>
                          <button className="kompo-pop-item" onClick={() => { onAdd(u.id); setOpenType(null); }}>
                            <span className="kompo-pop-ic" style={{background: t.accentSoft, color: t.accent}}>{t.icon}</span>
                            <span className="kompo-pop-body">
                              <span className="kompo-pop-name">{u.name}</span>
                              <span className="mono kompo-pop-meta">{u.serial} · {t.protocol}</span>
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
  const statusTone = machine.status === 'active' ? 'ok' : 'idle';
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
        <span className={`mcard-status mono mcard-status-${statusTone}`}>{machine.status === 'active' ? 'Active' : 'Idle'}</span>
        <button className="mcard-x" onClick={onRemove} title="Remove">×</button>
      </header>

      {machine.type === 'warehouse' && (
        <div className="mcard-body">
          <div className="mcard-row">
            <span className="mcard-k">State</span>
            <span className="mcard-v mono">{machine.stateLabel}</span>
          </div>
          <div className="mcard-meta mono">
            <span>SOAP · :8081</span>
            <span>{machine.serial}</span>
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
            <span className="mcard-v mono">{machine.battery !== null && machine.battery !== undefined ? Math.round(machine.battery) + '%' : '—'}</span>
          </div>
          {machine.battery !== null && machine.battery !== undefined && (
            <div className="mcard-bar"><div className="mcard-bar-fill" style={{width: machine.battery+'%'}}/></div>
          )}
          <div className="mcard-meta mono">
            <span>PROG · {machine._program || '—'}</span>
            <span>{machine._program ? 'LIVE' : machine.serial}</span>
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
            <span className="mcard-k">Health</span>
            <span className="mcard-v mono">{machine.healthy === true ? 'Healthy' : machine.healthy === false ? 'Unhealthy' : '—'}</span>
          </div>
          <div className="mcard-row">
            <span className="mcard-k">Current Op</span>
            <span className="mcard-v mono">{machine.operationId >= 0 ? machine.operationId : '—'}</span>
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
        {(log || []).map((e, i) => (
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

function MachineDetail({ machine, lineStatus, onClose, onRemove, activity }) {
  const t = MACHINE_TYPES[machine.type];
  React.useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = prev; };
  }, [onClose]);

  const statusLabel = machine.status === 'active' ? 'Active' : 'Idle';
  const statusTone  = machine.status === 'active' ? 'ok' : 'idle';

  const telemetry = machine.type === 'warehouse' ? [
    { k: 'STATE',    v: machine.stateLabel },
    { k: 'SERIAL',   v: machine.serial },
    { k: 'POOL',     v: machine.status === 'active' ? 'In use' : 'Available' },
    { k: 'PROTOCOL', v: 'SOAP' },
  ] : machine.type === 'agv' ? [
    { k: 'STATE',    v: machine.stateLabel },
    { k: 'BATTERY',  v: machine.battery !== null && machine.battery !== undefined ? Math.round(machine.battery) + '%' : '—' },
    { k: 'PROGRAM',  v: machine._program || '—' },
    { k: 'SERIAL',   v: machine.serial },
    { k: 'POOL',     v: machine.status === 'active' ? 'In use' : 'Available' },
    { k: 'PROTOCOL', v: 'REST' },
  ] : [
    { k: 'STATE',      v: machine.stateLabel },
    { k: 'HEALTH',     v: machine.healthy === true ? 'Healthy' : machine.healthy === false ? 'Unhealthy' : '—' },
    { k: 'CURRENT OP', v: machine.operationId >= 0 ? machine.operationId : '—' },
    { k: 'LAST OP',    v: machine.lastOperationId >= 0 ? machine.lastOperationId : '—' },
    { k: 'SERIAL',     v: machine.serial },
    { k: 'POOL',       v: machine.status === 'active' ? 'In use' : 'Available' },
    { k: 'PROTOCOL',   v: 'MQTT' },
  ];

  const accent     = t.accent;
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
            {machine.type === 'agv' && machine.battery !== null && machine.battery !== undefined && (
              <div className="mdetail-bar-wrap">
                <div className="mdetail-bar"><div className="mdetail-bar-fill" style={{width: machine.battery + '%', background: accent}}/></div>
                <div className="mono mdetail-bar-k">Battery level</div>
              </div>
            )}
          </section>

          <section className="mdetail-sec">
            <div className="sec-kicker mono">RECENT ACTIVITY</div>
            {activity && activity.length > 0 ? (
              <ul className="mdetail-log">
                {activity.map((e, i) => (
                  <li key={i} className={`mdetail-log-row mdetail-log-${e.lvl}`}>
                    <span className="mono mdetail-log-t">{e.t}</span>
                    <span className="mdetail-log-dot"/>
                    <span className="mdetail-log-m">{e.m}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="mono" style={{fontSize:11, opacity:0.5, padding:'8px 0'}}>No recent activity</div>
            )}
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
