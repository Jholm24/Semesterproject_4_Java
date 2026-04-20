function TaskBuilder({ nav }) {
  const PALETTE = [
    { id:'wh-pick', type:'warehouse', op:'PICK_TRAY', label:'Pick Tray', desc:'Warehouse → outlet', dur:4.2 },
    { id:'wh-insert', type:'warehouse', op:'INSERT_ITEM', label:'Insert Item', desc:'Assembled → warehouse', dur:3.8 },
    { id:'agv-move-wh', type:'agv', op:'MOVE_TO_STORAGE', label:'Move → Storage', desc:'AGV travel to warehouse', dur:6.1 },
    { id:'agv-pick-wh', type:'agv', op:'PICK_WAREHOUSE', label:'Pick · Warehouse', desc:'AGV grab from outlet', dur:2.4 },
    { id:'agv-move-as', type:'agv', op:'MOVE_TO_ASSEMBLY', label:'Move → Assembly', desc:'AGV travel to station', dur:5.9 },
    { id:'agv-put-as', type:'agv', op:'PUT_ASSEMBLY', label:'Put · Assembly', desc:'AGV drop to station', dur:2.2 },
    { id:'agv-pick-as', type:'agv', op:'PICK_ASSEMBLY', label:'Pick · Assembly', desc:'AGV grab finished', dur:2.4 },
    { id:'agv-put-wh', type:'agv', op:'PUT_WAREHOUSE', label:'Put · Warehouse', desc:'AGV drop to warehouse', dur:2.2 },
    { id:'as-op', type:'assembly', op:'START_OPERATION', label:'Start Operation', desc:'Begin assembly cycle', dur:12.0 },
    { id:'as-health', type:'assembly', op:'CHECK_HEALTH', label:'Check Health', desc:'Await emulator/checkhealth', dur:0.8 },
  ];
  const DEFAULT_SEQ = [
    'wh-pick','agv-move-wh','agv-pick-wh','agv-move-as','agv-put-as','as-op','as-health','agv-pick-as','agv-move-wh','agv-put-wh','wh-insert'
  ];

  const lineId = nav.activeLine || 'line-1';
  const seqKey = `sb_seq_${lineId}`;

  // Resolve current line name from localStorage or fallback
  const lineName = (() => {
    try {
      const lines = JSON.parse(localStorage.getItem('sb_lines') || '[]');
      const found = lines.find(l => l.id === lineId);
      if (found) return found.name;
    } catch(e) {}
    const fallbacks = { 'line-1': 'Line-01 · Skateboard', 'line-2': 'Line-02 · Desk Lamp', 'line-3': 'Line-03 · Pending' };
    return fallbacks[lineId] || lineId.toUpperCase();
  })();

  const loadSeq = () => {
    try { const s = JSON.parse(localStorage.getItem(seqKey)); if (Array.isArray(s) && s.length) return s; } catch(e){}
    return DEFAULT_SEQ.map((id,i) => ({ id, key: 'k'+i }));
  };

  const [seq, setSeq] = useState(loadSeq);
  const [dragId, setDragId] = useState(null);
  const [templateName, setTemplateName] = useState(() => lineName + ' · Full Cycle');

  // Reload seq when active line changes
  useEffect(() => {
    setSeq(loadSeq());
    setTemplateName(lineName + ' · Full Cycle');
  }, [lineId]);

  useEffect(() => { localStorage.setItem(seqKey, JSON.stringify(seq)); }, [seq, seqKey]);

  const addStep = (pid) => setSeq(s => [...s, { id: pid, key: 'k'+Date.now()+Math.random() }]);
  const removeStep = (key) => setSeq(s => s.filter(x => x.key !== key));
  const moveStep = (from, to) => setSeq(s => {
    const arr = [...s]; const [m] = arr.splice(from,1); arr.splice(to,0,m); return arr;
  });

  const totalDur = seq.reduce((a,x) => a + (PALETTE.find(p=>p.id===x.id)?.dur || 0), 0);

  const typeColor = { warehouse: 'var(--c-wh)', agv: 'var(--c-agv)', assembly: 'var(--c-as)' };

  return (
    <main className="tb-page">
      <header className="tb-header">
        <div>
          <div className="sec-kicker mono">TASK BUILDER</div>
          <h1 className="sec-title">Sequence Editor — {templateName}</h1>
          <div className="tb-sub mono">DRAG · DROP · reorder · save as template</div>
        </div>
        <div className="tb-head-actions">
          <div className="tb-summary">
            <div><span className="mono tb-k">STEPS</span><span className="tb-v">{seq.length}</span></div>
            <div><span className="mono tb-k">DUR</span><span className="tb-v">{totalDur.toFixed(1)}s</span></div>
          </div>
          <button className="btn-ghost" onClick={() => setSeq(DEFAULT_SEQ.map((id,i)=>({id,key:'k'+i})))}>Reset</button>
          <button className="btn-primary">Save Template</button>
        </div>
      </header>

      <div className="tb-workspace">
        <aside className="tb-palette">
          <div className="tb-palette-head mono">COMPONENT OPS</div>
          {Object.keys(MACHINE_TYPES).map(type => (
            <div key={type} className="tb-palette-group" style={{'--c': typeColor[type]}}>
              <div className="tb-palette-group-head">
                <span className="tb-palette-dot"/>
                <span>{MACHINE_TYPES[type].label}</span>
              </div>
              {PALETTE.filter(p=>p.type===type).map(p => (
                <div key={p.id} className="tb-palette-item"
                  draggable
                  onDragStart={(e) => { e.dataTransfer.setData('text/plain', 'pal:'+p.id); }}
                  onDoubleClick={() => addStep(p.id)}>
                  <div className="tb-pi-code mono">{p.op}</div>
                  <div className="tb-pi-label">{p.label}</div>
                  <div className="tb-pi-desc">{p.desc}</div>
                  <div className="tb-pi-dur mono">~{p.dur}s</div>
                </div>
              ))}
            </div>
          ))}
        </aside>

        <section className="tb-canvas"
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            const d = e.dataTransfer.getData('text/plain');
            if (d.startsWith('pal:')) addStep(d.slice(4));
          }}>
          <div className="tb-canvas-head">
            <div className="mono tb-canvas-kicker">PRODUCTION CYCLE · {lineName.toUpperCase()}</div>
            <div className="tb-canvas-legend">
              <span><span className="lg-dot" style={{background:'var(--c-wh)'}}/>Warehouse</span>
              <span><span className="lg-dot" style={{background:'var(--c-agv)'}}/>AGV</span>
              <span><span className="lg-dot" style={{background:'var(--c-as)'}}/>Assembly</span>
            </div>
          </div>
          <ol className="tb-seq">
            {seq.map((s, i) => {
              const p = PALETTE.find(x=>x.id===s.id); if (!p) return null;
              return (
                <li key={s.key} className={`tb-step ${dragId===s.key?'drag':''}`}
                  style={{'--c': typeColor[p.type]}}
                  draggable
                  onDragStart={(e) => { setDragId(s.key); e.dataTransfer.setData('text/plain', 'seq:'+i); }}
                  onDragEnd={() => setDragId(null)}
                  onDragOver={(e) => e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault();
                    const d = e.dataTransfer.getData('text/plain');
                    if (d.startsWith('seq:')) { moveStep(parseInt(d.slice(4)), i); }
                    else if (d.startsWith('pal:')) { setSeq(ss => { const arr=[...ss]; arr.splice(i,0,{id:d.slice(4),key:'k'+Date.now()+Math.random()}); return arr; }); }
                  }}>
                  <div className="tb-step-num mono">{String(i+1).padStart(2,'0')}</div>
                  <div className="tb-step-rail"><span className="tb-step-rail-dot"/></div>
                  <div className="tb-step-body">
                    <div className="tb-step-op mono">{MACHINE_TYPES[p.type].label.toUpperCase()} · {p.op}</div>
                    <div className="tb-step-label">{p.label}</div>
                    <div className="tb-step-desc">{p.desc}</div>
                  </div>
                  <div className="tb-step-dur mono">~{p.dur}s</div>
                  <button className="tb-step-x" onClick={() => removeStep(s.key)}>×</button>
                </li>
              );
            })}
            <li className="tb-drop-hint mono">+ Drag or double-click an operation in the panel</li>
          </ol>
        </section>
      </div>
    </main>
  );
}

function generatePassword() {
  const upper = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
  const lower = 'abcdefghjkmnpqrstuvwxyz';
  const digits = '23456789';
  const special = '!@#$%&*';
  const all = upper + lower + digits + special;
  let pw = [
    upper[Math.floor(Math.random()*upper.length)],
    lower[Math.floor(Math.random()*lower.length)],
    digits[Math.floor(Math.random()*digits.length)],
    special[Math.floor(Math.random()*special.length)],
  ];
  for (let i = 0; i < 8; i++) pw.push(all[Math.floor(Math.random()*all.length)]);
  return pw.sort(() => Math.random()-0.5).join('');
}

function NewEmployeeModal({ onClose, onCreate }) {
  const [name, setName]       = useState('');
  const [role, setRole]       = useState('operator');
  const [password, setPassword] = useState(() => generatePassword());
  const [copied, setCopied]   = useState(false);
  const [showPw, setShowPw]   = useState(false);

  // Auto-generate username from full name: firstname initial + lastname, lowercase, no spaces
  const deriveUsername = (fullName) => {
    const parts = fullName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '';
    if (parts.length === 1) return parts[0].toLowerCase();
    return (parts[0][0] + parts[parts.length - 1]).toLowerCase();
  };

  const [username, setUsername] = useState('');
  const [usernameTouched, setUsernameTouched] = useState(false);

  const handleNameChange = (val) => {
    setName(val);
    if (!usernameTouched) setUsername(deriveUsername(val));
  };

  const lines = (() => {
    try {
      const ls = JSON.parse(localStorage.getItem('sb_lines') || '[]');
      if (ls.length) return ['—', ...ls.map(l => l.name)];
    } catch(e) {}
    return ['—','Line-01 · Skateboard','Line-02 · Desk Lamp','Line-03 · Pending'];
  })();

  useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  const copyPw = () => {
    navigator.clipboard.writeText(password).then(() => { setCopied(true); setTimeout(() => setCopied(false), 2000); });
  };

  const submit = () => {
    if (!name.trim()) return;
    const initials = name.trim().split(' ').map(p=>p[0]).join('').slice(0,2).toUpperCase();
    const id = 'e' + Date.now();
    const since = new Date().toISOString().slice(0,7);
    onCreate({ id, name: name.trim(), username: username.trim() || deriveUsername(name), role, line: '—', since, pic: initials, password });
  };

  return (
    <div className="mdetail-backdrop" onMouseDown={onClose}>
      <div className="mdetail" style={{width:'min(500px,100%)'}} onMouseDown={e=>e.stopPropagation()} role="dialog" aria-label="New employee">
        <div className="mdetail-accent-bar" style={{background:'var(--c-agv)'}}/>
        <header className="mdetail-head">
          <div className="mdetail-head-left">
            <div>
              <div className="mdetail-kicker mono">NEW EMPLOYEE</div>
              <h2 className="mdetail-title">Add staff member</h2>
            </div>
          </div>
          <button className="mdetail-close" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M2 2 L12 12 M12 2 L2 12" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            </svg>
          </button>
        </header>

        <div className="mdetail-body" style={{gap:16}}>
          {/* Name */}
          <div className="nl-field">
            <span className="mono nl-k">FULL NAME</span>
            <input className="nl-input" value={name} onChange={e=>handleNameChange(e.target.value)} placeholder="e.g. Jonas Vestergaard" autoFocus/>
          </div>

          {/* Username */}
          <div className="nl-field">
            <span className="mono nl-k">USERNAME</span>
            <div style={{position:'relative'}}>
              <input
                className="nl-input"
                value={username}
                onChange={e=>{ setUsername(e.target.value); setUsernameTouched(true); }}
                placeholder="auto-generated from name"
                style={{fontFamily:'var(--ff-mono)', paddingRight: 80}}
              />
              <button
                onClick={() => { setUsername(deriveUsername(name)); setUsernameTouched(false); }}
                style={{position:'absolute',right:8,top:'50%',transform:'translateY(-50%)',background:'none',border:0,color:'var(--ink-3)',fontSize:10,letterSpacing:'0.08em',cursor:'pointer',padding:'2px 6px'}}
                className="mono"
                title="Re-generate from name"
              >↺ RESET</button>
            </div>
            <div className="mono" style={{fontSize:10,color:'var(--ink-3)',marginTop:4}}>Used to log in · auto-derived as first initial + last name</div>
          </div>

          {/* Role */}
          <div className="nl-field">
            <span className="mono nl-k">ROLE</span>
            <select className="nl-input" value={role} onChange={e=>setRole(e.target.value)}>
              <option value="operator">Operator</option>
              <option value="manager">Manager</option>
            </select>
          </div>

          {/* Password */}
          <div className="nl-field">
            <span className="mono nl-k">INITIAL PASSWORD</span>
            <div style={{display:'flex', gap:8}}>
              <div style={{position:'relative', flex:1}}>
                <input
                  className="nl-input"
                  type={showPw ? 'text' : 'password'}
                  value={password}
                  onChange={e=>setPassword(e.target.value)}
                  style={{paddingRight:36, fontFamily:'var(--ff-mono)', letterSpacing:'0.06em'}}
                />
                <button onClick={()=>setShowPw(s=>!s)} style={{position:'absolute',right:10,top:'50%',transform:'translateY(-50%)',background:'none',border:0,color:'var(--ink-3)',cursor:'pointer',padding:2,fontSize:12}}>
                  {showPw ? '🙈' : '👁'}
                </button>
              </div>
              <button className="btn-ghost-sm" onClick={()=>setPassword(generatePassword())} title="Generate new password" style={{whiteSpace:'nowrap'}}>↺ Generate</button>
              <button className="btn-ghost-sm" onClick={copyPw} style={{whiteSpace:'nowrap', color: copied ? 'var(--ok)' : undefined}}>
                {copied ? '✓ Copied' : 'Copy'}
              </button>
            </div>
            <div className="mono" style={{fontSize:10, color:'var(--ink-3)', marginTop:4}}>Share this password securely — the employee should change it on first login.</div>
          </div>
        </div>

        <footer className="nl-foot">
          <button className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={submit} disabled={!name.trim()}>Create employee</button>
        </footer>
      </div>
    </div>
  );
}

function EditEmployeeModal({ employee, onClose, onSave }) {
  const deriveUsername = (fullName) => {
    const parts = fullName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '';
    if (parts.length === 1) return parts[0].toLowerCase();
    return (parts[0][0] + parts[parts.length - 1]).toLowerCase();
  };

  const [name, setName]       = useState(employee.name);
  const [username, setUsername] = useState(employee.username || deriveUsername(employee.name));
  const [role, setRole]       = useState(employee.role);
  const [line, setLine]       = useState(employee.line || '—');
  const [newPassword, setNewPassword] = useState('');
  const [copied, setCopied]   = useState(false);

  const lines = (() => {
    try {
      const ls = JSON.parse(localStorage.getItem('sb_lines') || '[]');
      if (ls.length) return ['—', ...ls.map(l => l.name)];
    } catch(e) {}
    return ['—','Line-01 · Skateboard','Line-02 · Desk Lamp','Line-03 · Pending'];
  })();

  useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  const copyPw = () => {
    navigator.clipboard.writeText(newPassword).then(() => { setCopied(true); setTimeout(() => setCopied(false), 2000); });
  };

  const submit = () => {
    if (!name.trim()) return;
    const initials = name.trim().split(' ').map(p=>p[0]).join('').slice(0,2).toUpperCase();
    const updated = { ...employee, name: name.trim(), username: username.trim() || deriveUsername(name), role, pic: initials };
    if (newPassword) updated.password = newPassword;
    onSave(updated);
  };

  return (
    <div className="mdetail-backdrop" onMouseDown={onClose}>
      <div className="mdetail" style={{width:'min(500px,100%)'}} onMouseDown={e=>e.stopPropagation()} role="dialog" aria-label="Edit employee">
        <div className="mdetail-accent-bar" style={{background:'var(--c-wh)'}}/>
        <header className="mdetail-head">
          <div className="mdetail-head-left">
            <div>
              <div className="mdetail-kicker mono">EDIT EMPLOYEE · {employee.id.toUpperCase()}</div>
              <h2 className="mdetail-title">{employee.name}</h2>
            </div>
          </div>
          <button className="mdetail-close" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M2 2 L12 12 M12 2 L2 12" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            </svg>
          </button>
        </header>

        <div className="mdetail-body" style={{gap:16}}>
          {/* Name */}
          <div className="nl-field">
            <span className="mono nl-k">FULL NAME</span>
            <input className="nl-input" value={name} onChange={e=>setName(e.target.value)} autoFocus/>
          </div>

          {/* Username */}
          <div className="nl-field">
            <span className="mono nl-k">USERNAME</span>
            <input className="nl-input" value={username} onChange={e=>setUsername(e.target.value)} style={{fontFamily:'var(--ff-mono)'}}/>
          </div>

          {/* Role */}
          <div className="nl-field">
            <span className="mono nl-k">ROLE</span>
            <select className="nl-input" value={role} onChange={e=>setRole(e.target.value)}>
              <option value="operator">Operator</option>
              <option value="manager">Manager</option>
            </select>
          </div>

          {/* Reset password */}
          <div className="nl-field">
            <span className="mono nl-k">RESET PASSWORD</span>
            <div style={{display:'flex', gap:8}}>
              <input
                className="nl-input"
                type="text"
                value={newPassword}
                readOnly
                placeholder="Leave blank to keep current password"
                style={{fontFamily:'var(--ff-mono)', flex:1, color: newPassword ? 'var(--ink)' : 'var(--ink-4)'}}
              />
              <button className="btn-ghost-sm" onClick={()=>setNewPassword(generatePassword())} style={{whiteSpace:'nowrap'}}>↺ Generate</button>
              {newPassword && <button className="btn-ghost-sm" onClick={copyPw} style={{whiteSpace:'nowrap', color: copied ? 'var(--ok)' : undefined}}>{copied ? '✓ Copied' : 'Copy'}</button>}
            </div>
            {newPassword && <div className="mono" style={{fontSize:10,color:'var(--ink-3)',marginTop:4}}>A new password has been generated — share it securely before saving.</div>}
          </div>
        </div>

        <footer className="nl-foot">
          <button className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={submit} disabled={!name.trim()}>Save changes</button>
        </footer>
      </div>
    </div>
  );
}

function Employees({ nav }) {
  const [employees, setEmployees] = useState(() => {
    const s = localStorage.getItem('sb_employees_v2');
    if (s) try { return JSON.parse(s); } catch(e){}
    return [
      { id:'e1', name:'Lars Ottesen', role:'operator', line:'Line-01', since:'2023-08', pic:'LO' },
      { id:'e2', name:'Mette Nørgaard', role:'operator', line:'Line-02', since:'2024-01', pic:'MN' },
      { id:'e3', name:'Anders Kjær', role:'operator', line:'Line-01', since:'2022-11', pic:'AK' },
      { id:'e4', name:'Sofie Bech', role:'operator', line:'Line-03', since:'2025-02', pic:'SB' },
      { id:'e5', name:'Mikkel Hansen', role:'manager', line:'—', since:'2021-05', pic:'MH' },
      { id:'e6', name:'Johan Vest', role:'operator', line:'—', since:'2023-03', pic:'JV' },
    ];
  });
  useEffect(() => { localStorage.setItem('sb_employees_v2', JSON.stringify(employees)); }, [employees]);
  const [q, setQ] = useState('');
  const [showNewModal, setShowNewModal] = useState(false);
  const filtered = employees.filter(e => e.name.toLowerCase().includes(q.toLowerCase()));

  const [editingEmp, setEditingEmp] = useState(null);
  const [removingEmp, setRemovingEmp] = useState(null);
  const addEmployee = (emp) => { setEmployees(es => [...es, emp]); setShowNewModal(false); };
  const updateEmployee = (updated) => { setEmployees(es => es.map(e => e.id===updated.id ? {...e, ...updated} : e)); setEditingEmp(null); };
  const removeEmployee = (id) => { setEmployees(es => es.filter(e => e.id !== id)); setRemovingEmp(null); };

  return (
    <main className="emp-page">
      <header className="emp-head">
        <div>
          <div className="sec-kicker mono">EMPLOYEES</div>
          <h1 className="sec-title">Staff & Assignments</h1>
        </div>
        <div className="emp-actions">
          <input className="emp-search" placeholder="Search employee…" value={q} onChange={e=>setQ(e.target.value)}/>

          <button className="btn-primary" onClick={() => setShowNewModal(true)}>+ New employee</button>
        </div>
      </header>
      <div className="emp-table">
        <div className="emp-row emp-row-head mono">
          <span>Employee</span><span>Role</span><span>Line</span><span>Since</span><span>Action</span>
        </div>
        {filtered.map(e => (
          <div key={e.id} className="emp-row">
            <div className="emp-name">
              <div className="emp-pic">{e.pic}</div>
              <div>
                <div className="emp-n">{e.name}</div>
                <div className="mono emp-id">{e.username ? e.username : '#'+e.id.toUpperCase()}</div>
              </div>
            </div>
            <div className="mono">{e.role.toUpperCase()}</div>
            <div>
              <select className="emp-line-sel" value={e.line || '—'} onChange={ev => setEmployees(es => es.map(x => x.id===e.id ? {...x, line: ev.target.value} : x))}>
                {(()=>{ try { const ls=JSON.parse(localStorage.getItem('sb_lines')||'[]'); if(ls.length) return ['—',...ls.map(l=>l.name)].map(l=><option key={l}>{l}</option>); } catch(ex){} return ['—','Line-01 · Skateboard','Line-02 · Desk Lamp','Line-03 · Pending'].map(l=><option key={l}>{l}</option>); })()}
              </select>
            </div>
            <div className="mono">{e.since}</div>
            <div className="emp-handle">
              <button className="btn-ghost-sm">Tasks</button>
              <button className="btn-ghost-sm" onClick={() => setEditingEmp(e)}>Edit</button>
              <button className="btn-ghost-sm btn-danger" onClick={() => setRemovingEmp(e)}>Remove</button>
            </div>
          </div>
        ))}
      </div>
      {showNewModal && <NewEmployeeModal onClose={() => setShowNewModal(false)} onCreate={addEmployee} />}
      {editingEmp && <EditEmployeeModal employee={editingEmp} onClose={() => setEditingEmp(null)} onSave={updateEmployee} />}
      {removingEmp && <ConfirmModal title={`Remove ${removingEmp.name}?`} body="This will permanently delete the employee record." confirmLabel="Remove" danger onConfirm={() => removeEmployee(removingEmp.id)} onClose={() => setRemovingEmp(null)} />}
    </main>
  );
}

function LinesManager({ nav }) {
  const LINE_SEEDS = [
    { id:'line-1', name:'Line-01 · Skateboard', product:'Pro Deck 8.0"', status:'running', cycles:247, success:98.4, components:{warehouse:1,agv:1,assembly:1}, ops:3, machines:['wh-1','agv-1','as-1'], operators:[] },
    { id:'line-2', name:'Line-02 · Desk Lamp',  product:'Studio Lamp v2', status:'paused',  cycles:128, success:97.1, components:{warehouse:1,agv:2,assembly:1}, ops:2, machines:['wh-2','agv-2','agv-3','as-2'], operators:[] },
    { id:'line-3', name:'Line-03 · Pending',    product:'—',              status:'standby', cycles:0,   success:0,    components:{warehouse:0,agv:0,assembly:0}, ops:0, machines:[], operators:[] },
  ];
  const [lines, setLines] = useState(() => {
    const s = localStorage.getItem('sb_lines');
    if (s) try {
      const parsed = JSON.parse(s);
      if (Array.isArray(parsed) && parsed.length) {
        // Migrate legacy entries: ensure every line has machines[] and operators[].
        return parsed.map(L => {
          const seed = LINE_SEEDS.find(s => s.id === L.id);
          return {
            ...L,
            machines: Array.isArray(L.machines) ? L.machines : (seed ? seed.machines : []),
            operators: Array.isArray(L.operators) ? L.operators : (seed ? seed.operators : []),
          };
        });
      }
    } catch(e){}
    return LINE_SEEDS;
  });
  useEffect(() => {
    localStorage.setItem('sb_lines', JSON.stringify(lines));
    window.dispatchEvent(new CustomEvent('sb-lines-change', { detail: lines }));
  }, [lines]);
  const [modal, setModal] = useState(false); // false | 'create' | editingLine
  const [removing, setRemoving] = useState(null);

  const computeComps = (machines) => {
    const c = { warehouse: 0, agv: 0, assembly: 0 };
    machines.forEach(mid => {
      if (mid.startsWith('wh')) c.warehouse++;
      else if (mid.startsWith('agv')) c.agv++;
      else if (mid.startsWith('as')) c.assembly++;
    });
    return c;
  };

  const createLine = (draft) => {
    // figure out next id
    const used = new Set(lines.map(l => l.id));
    let n = 1; while (used.has('line-' + n)) n++;
    const id = 'line-' + n;
    const paddedNum = String(n).padStart(2,'0');
    const comps = computeComps(draft.machines);
    const newLine = {
      id,
      name: `Line-${paddedNum} · ${draft.title || 'Untitled'}`,
      product: draft.product || '—',
      status: 'standby',
      cycles: 0, success: 0,
      components: comps,
      ops: draft.operators.length,
      machines: draft.machines,
      operators: draft.operators,
    };
    setLines(ls => [...ls, newLine]);

    // seed byLine so the topbar / dashboard pick it up
    try {
      const by = JSON.parse(localStorage.getItem('sb_byline_v3') || '{}');
      by[id] = { status: 'standby', cycles: 0, warnings: 0, success: 0, machines: draft.machines, log: [{ t: new Date().toTimeString().slice(0,8), lvl: 'info', m: `Line created · ${draft.title}` }] };
      localStorage.setItem('sb_byline_v3', JSON.stringify(by));
      window.dispatchEvent(new CustomEvent('sb-byline-change', { detail: by }));
    } catch(e) {}
    setModal(false);
  };

  const updateLine = (id, draft) => {
    // preserve the Line-NN prefix, swap the product label after the ·
    const paddedNum = id.replace('line-','').padStart(2,'0');
    const comps = computeComps(draft.machines);
    setLines(ls => ls.map(L => L.id === id ? {
      ...L,
      name: `Line-${paddedNum} · ${draft.title || 'Untitled'}`,
      product: draft.product || '—',
      components: comps,
      ops: draft.operators.length,
      machines: draft.machines,
      operators: draft.operators,
    } : L));
    // sync machines into byLine
    try {
      const by = JSON.parse(localStorage.getItem('sb_byline_v3') || '{}');
      if (by[id]) {
        by[id] = { ...by[id], machines: draft.machines };
        localStorage.setItem('sb_byline_v3', JSON.stringify(by));
        window.dispatchEvent(new CustomEvent('sb-byline-change', { detail: by }));
      }
    } catch(e) {}
    setModal(false);
  };

  const removeLine = (id) => {
    setLines(ls => ls.filter(L => L.id !== id));
    try {
      const by = JSON.parse(localStorage.getItem('sb_byline_v3') || '{}');
      delete by[id];
      localStorage.setItem('sb_byline_v3', JSON.stringify(by));
      window.dispatchEvent(new CustomEvent('sb-byline-change', { detail: by }));
    } catch(e) {}
    if (nav.activeLine === id) nav.setActiveLine('line-1');
    setRemoving(null);
  };

  return (
    <main className="lines-page">
      <header className="emp-head">
        <div>
          <div className="sec-kicker mono">PRODUCTION LINES</div>
          <h1 className="sec-title">Overview of all lines</h1>
        </div>
        <div className="emp-actions">
          <button className="btn-primary" onClick={() => setModal('create')}>+ New line</button>
        </div>
      </header>
      <div className="lines-grid">
        {lines.map(L => (
          <article key={L.id} className={`line-card line-status-${L.status}`}>
            <header className="line-card-head">
              <div>
                <div className="mono line-card-id">{L.id.toUpperCase()}</div>
                <div className="line-card-name">{L.name}</div>
              </div>
              <span className={`cboard-chip cboard-chip-${L.status}`}>{L.status.toUpperCase()}</span>
            </header>
            <div className="line-card-product">
              <span className="mono line-card-k">PRODUCT</span>
              <span>{L.product}</span>
            </div>
            <div className="line-card-stats">
              <div><div className="mono line-card-k">CYCLES</div><div className="line-card-v">{L.cycles}</div></div>
              <div><div className="mono line-card-k">SUCCESS</div><div className="line-card-v">{L.success.toFixed(1)}%</div></div>
              <div><div className="mono line-card-k">OPERATORS</div><div className="line-card-v">{L.ops}</div></div>
            </div>
            <div className="line-card-comps">
              <div className="lcc" style={{'--c':'var(--c-wh)'}}><span className="mono">WH</span><span>{L.components.warehouse}</span></div>
              <div className="lcc" style={{'--c':'var(--c-agv)'}}><span className="mono">AGV</span><span>{L.components.agv}</span></div>
              <div className="lcc" style={{'--c':'var(--c-as)'}}><span className="mono">AS</span><span>{L.components.assembly}</span></div>
            </div>
            <footer className="line-card-foot">
              <button className="btn-ghost-sm" onClick={() => { nav.setActiveLine(L.id); nav.setView('dashboard'); }}>Open →</button>
              <button className="btn-ghost-sm" onClick={() => { nav.setActiveLine(L.id); nav.setView('builder'); }}>Task Builder</button>
              <button className="btn-ghost-sm" onClick={() => setModal(L)}>Edit</button>
              <button className="btn-ghost-sm btn-ghost-danger" onClick={() => setRemoving(L)} disabled={lines.length <= 1}>Remove</button>
            </footer>
          </article>
        ))}
      </div>
      {modal && (
        <NewLineModal
          key={modal === 'create' ? 'new' : modal.id}
          editing={modal === 'create' ? null : modal}
          onClose={() => setModal(false)}
          onCreate={createLine}
          onUpdate={updateLine}
          existingLines={lines}
        />
      )}
      {removing && (
        <ConfirmModal
          title={`Remove ${removing.name}?`}
          body={`This will delete the line and release its ${(removing.machines||[]).length} assigned machine(s) back to the pool. Historical metrics will be lost.`}
          confirmLabel="Remove line"
          danger
          onConfirm={() => removeLine(removing.id)}
          onClose={() => setRemoving(null)}
        />
      )}
    </main>
  );
}

function NewLineModal({ onClose, onCreate, onUpdate, existingLines, editing }) {
  // When editing, seed title from what's after the ' · ' in the line name (e.g. "Line-01 · Skateboard" -> "Skateboard").
  const seedTitle = editing ? (editing.name.split(' · ').slice(1).join(' · ') || editing.name) : '';
  const [title, setTitle] = useState(seedTitle);
  const [product, setProduct] = useState(editing ? (editing.product === '—' ? '' : editing.product) : '');
  const [picked, setPicked] = useState(editing ? [...(editing.machines || [])] : []); // machine ids
  const [ops, setOps] = useState(editing ? [...(editing.operators || [])] : []); // operator ids

  // occupied machines across OTHER lines (exclude the one being edited)
  const occupied = new Set();
  existingLines.forEach(l => {
    if (editing && l.id === editing.id) return;
    (l.machines || []).forEach(m => occupied.add(m));
  });

  const POOL = {
    warehouse: [
      { id:'wh-1', name:'Parts Warehouse 01', loc:'Hall A · Bay 1' },
      { id:'wh-2', name:'Parts Warehouse 02', loc:'Hall A · Bay 2' },
      { id:'wh-3', name:'Parts Warehouse 03', loc:'Hall A · Bay 3' },
      { id:'wh-4', name:'Parts Warehouse 04', loc:'Hall B · Bay 1' },
      { id:'wh-5', name:'Parts Warehouse 05', loc:'Hall B · Bay 2' },
    ],
    agv: [
      { id:'agv-1', name:'AGV 01', loc:'Track N' },
      { id:'agv-2', name:'AGV 02', loc:'Dock 2' },
      { id:'agv-3', name:'AGV 03', loc:'Dock 3' },
      { id:'agv-4', name:'AGV 04', loc:'Track S' },
      { id:'agv-5', name:'AGV 05', loc:'Dock 5' },
    ],
    assembly: [
      { id:'as-1', name:'Assemble Table 01', loc:'Station 1' },
      { id:'as-2', name:'Assemble Table 02', loc:'Station 2' },
      { id:'as-3', name:'Assemble Table 03', loc:'Station 3' },
      { id:'as-4', name:'Assemble Table 04', loc:'Station 4' },
      { id:'as-5', name:'Assemble Table 05', loc:'Station 5' },
    ],
  };

  // employees
  const EMPS = (() => {
    try {
      const s = localStorage.getItem('sb_employees_v2');
      if (s) return JSON.parse(s).filter(e => e.role === 'operator' && e.status !== 'unemployed');
    } catch(e){}
    return [
      { id:'e1', name:'Lars Ottesen', pic:'LO', shift:'Day' },
      { id:'e2', name:'Mette Nørgaard', pic:'MN', shift:'Evening' },
      { id:'e3', name:'Anders Kjær', pic:'AK', shift:'Day' },
      { id:'e4', name:'Sofie Bech', pic:'SB', shift:'Day' },
    ];
  })();

  const togglePick = (id) => setPicked(p => p.includes(id) ? p.filter(x => x!==id) : [...p, id]);
  const toggleOp   = (id) => setOps(o => o.includes(id) ? o.filter(x => x!==id) : [...o, id]);

  React.useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  const canSubmit = title.trim().length > 0;

  const submit = () => {
    if (!canSubmit) return;
    const draft = { title: title.trim(), product: product.trim() || '—', machines: picked, operators: ops };
    if (editing) onUpdate(editing.id, draft);
    else onCreate(draft);
  };

  const TYPES = [
    { key:'warehouse', label:'Warehouse', accent:'var(--c-wh)', soft:'var(--c-wh-soft)' },
    { key:'agv',       label:'AGV',       accent:'var(--c-agv)', soft:'var(--c-agv-soft)' },
    { key:'assembly',  label:'Assembly',  accent:'var(--c-as)',  soft:'var(--c-as-soft)'  },
  ];

  return (
    <div className="mdetail-backdrop" onMouseDown={onClose}>
      <div className="mdetail newline-modal" onMouseDown={e => e.stopPropagation()} role="dialog" aria-label={editing ? 'Edit line' : 'Create new line'}>
        <header className="mdetail-head nl-head">
          <div className="mdetail-head-left">
            <div>
              <div className="mdetail-kicker mono">{editing ? 'EDIT LINE' : 'NEW LINE'}</div>
              <div className="mdetail-title">{editing ? editing.name : 'Create a production line'}</div>
            </div>
          </div>
          <button className="mdetail-close" onClick={onClose} aria-label="Close">×</button>
        </header>
        <div className="mdetail-body nl-body">
          <section className="nl-section">
            <label className="nl-field">
              <span className="mono nl-k">TITLE</span>
              <input
                className="nl-input"
                value={title}
                onChange={e => setTitle(e.target.value)}
                placeholder="e.g. Skateboard"
                autoFocus
              />
            </label>
            <label className="nl-field">
              <span className="mono nl-k">PRODUCT</span>
              <input
                className="nl-input"
                value={product}
                onChange={e => setProduct(e.target.value)}
                placeholder="e.g. Pro Deck 8.0&quot;"
              />
            </label>
          </section>

          <section className="nl-section">
            <div className="nl-section-head">
              <span className="mono nl-k">MACHINES</span>
              <span className="mono nl-hint">{picked.length} selected · available units only</span>
            </div>
            <div className="nl-machines">
              {TYPES.map(t => (
                <div key={t.key} className="nl-mcol" style={{'--c':t.accent,'--cs':t.soft}}>
                  <div className="nl-mcol-head">
                    <span className="nl-mcol-label">{t.label}</span>
                    <span className="mono nl-mcol-count">
                      {picked.filter(id => POOL[t.key].some(m=>m.id===id)).length}/{POOL[t.key].filter(m=>!occupied.has(m.id)).length}
                    </span>
                  </div>
                  <ul className="nl-mlist">
                    {POOL[t.key].map(m => {
                      const isOccupied = occupied.has(m.id);
                      const isPicked = picked.includes(m.id);
                      return (
                        <li key={m.id}>
                          <button
                            className={'nl-mitem' + (isPicked ? ' is-picked' : '') + (isOccupied ? ' is-occupied' : '')}
                            onClick={() => !isOccupied && togglePick(m.id)}
                            disabled={isOccupied}
                          >
                            <span className="nl-mcheck" aria-hidden>
                              {isPicked ? '✓' : ''}
                            </span>
                            <span className="nl-mbody">
                              <span className="nl-mname">{m.name}</span>
                              <span className="mono nl-mmeta">{isOccupied ? 'In use' : m.loc}</span>
                            </span>
                          </button>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              ))}
            </div>
          </section>

          <section className="nl-section">
            <div className="nl-section-head">
              <span className="mono nl-k">OPERATORS</span>
              <span className="mono nl-hint">{ops.length} assigned</span>
            </div>
            <ul className="nl-ops">
              {EMPS.map(e => {
                const on = ops.includes(e.id);
                return (
                  <li key={e.id}>
                    <button className={'nl-op' + (on ? ' is-on' : '')} onClick={() => toggleOp(e.id)}>
                      <span className="nl-op-avatar">{e.pic || e.name.split(' ').map(p=>p[0]).join('').slice(0,2)}</span>
                      <span className="nl-op-body">
                        <span className="nl-op-name">{e.name}</span>
                        <span className="mono nl-op-meta">{e.shift || 'Day'} shift</span>
                      </span>
                      <span className="nl-op-check">{on ? '✓' : '+'}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </section>
        </div>
        <footer className="nl-foot">
          <button className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={submit} disabled={!canSubmit}>{editing ? 'Save changes' : 'Create line'}</button>
        </footer>
      </div>
    </div>
  );
}

function OperatorDashboard({ nav }) {
  const [assigned] = useState(['Line-01 · Skateboard']);
  const [lineStatus, setLineStatus] = useState(() => localStorage.getItem('sb_linestatus') || 'standby');
  const [cycles, setCycles] = useState(247);
  useEffect(() => { localStorage.setItem('sb_linestatus', lineStatus); }, [lineStatus]);
  useEffect(() => {
    if (lineStatus !== 'running') return;
    const id = setInterval(() => setCycles(c => c+1), 1500);
    return () => clearInterval(id);
  }, [lineStatus]);

  const task = { name:'Skateboard · Full Cycle v2', steps:11, current:4, currentOp:'MOVE_TO_ASSEMBLY' };

  return (
    <main className="op-page">
      <header className="op-head">
        <div>
          <div className="sec-kicker mono">OPERATOR</div>
          <h1 className="sec-title">Your Assigned Lines</h1>
        </div>
        <div className="op-head-meta mono">Shift · Day · 08:00–16:00</div>
      </header>

      <section className="op-lines">
        {assigned.map(name => (
          <article key={name} className={`op-line line-status-${lineStatus}`}>
            <header className="op-line-head">
              <div>
                <div className="mono sec-kicker">ASSIGNED</div>
                <h2 className="op-line-name">{name}</h2>
              </div>
              <span className={`cboard-chip cboard-chip-${lineStatus}`}>{lineStatus.toUpperCase()}</span>
            </header>

            <div className="op-stats">
              <div className="op-stat"><div className="mono op-k">CYCLES</div><div className="op-v">{cycles}</div></div>
              <div className="op-stat"><div className="mono op-k">SUCCESS</div><div className="op-v">98.4%</div></div>
              <div className="op-stat"><div className="mono op-k">BATTERY · AGV-1</div><div className="op-v">87%</div></div>
              <div className="op-stat"><div className="mono op-k">CAPACITY · WH-1</div><div className="op-v">78%</div></div>
            </div>

            <div className="op-task">
              <div className="op-task-head">
                <span className="mono sec-kicker">ACTIVE TASK TEMPLATE</span>
                <span className="mono op-task-dur">STEP {task.current}/{task.steps} · {task.currentOp}</span>
              </div>
              <div className="op-task-name">{task.name}</div>
              <div className="op-task-bar">
                <div className="op-task-bar-fill" style={{width: (task.current/task.steps*100)+'%'}}/>
              </div>
            </div>

            <div className="op-controls">
              <button className="op-btn op-start" onClick={() => setLineStatus('running')}>
                <svg width="18" height="18" viewBox="0 0 16 16"><path d="M5 3 L13 8 L5 13 Z" fill="currentColor"/></svg>
                Start Production
              </button>
              <button className="op-btn op-stop" onClick={() => setLineStatus('stopped')}>
                <svg width="18" height="18" viewBox="0 0 16 16"><rect x="3" y="3" width="10" height="10" fill="currentColor"/></svg>
                Stop (Safe Park)
              </button>
              <button className="op-btn op-abort" onClick={() => setLineStatus('alarm')}>
                <svg width="18" height="18" viewBox="0 0 16 16" fill="none"><path d="M8 2 L14 13 H2 Z M8 6 V10" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round"/></svg>
                ABORT
              </button>
            </div>
          </article>
        ))}
      </section>

      <section className="op-activity">
        <header className="op-act-head">
          <h3 className="sec-title">Live Telemetry</h3>
          <span className="mono">↻ auto-refresh</span>
        </header>
        <ul className="evlog-list">
          <li className="ev ev-ok"><span className="ev-time mono">14:02:18</span><span className="ev-dot"/><span className="ev-msg">AGV 1 · MOVE_TO_ASSEMBLY completed</span></li>
          <li className="ev ev-info"><span className="ev-time mono">14:02:11</span><span className="ev-dot"/><span className="ev-msg">Warehouse 1 · picked tray #4821</span></li>
          <li className="ev ev-ok"><span className="ev-time mono">14:01:47</span><span className="ev-dot"/><span className="ev-msg">Assemble Table 1 · cycle {cycles} accepted</span></li>
          <li className="ev ev-warn"><span className="ev-time mono">13:58:02</span><span className="ev-dot"/><span className="ev-msg">AGV 1 · battery below 90%</span></li>
        </ul>
      </section>
    </main>
  );
}

function ConfirmModal({ title, body, confirmLabel = 'Confirm', danger = false, onConfirm, onClose }) {
  React.useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);
  return (
    <div className="mdetail-backdrop" onMouseDown={onClose}>
      <div className="mdetail confirm-modal" onMouseDown={e => e.stopPropagation()} role="alertdialog" aria-label={title}>
        <header className="mdetail-head">
          <div className="mdetail-head-left">
            <div>
              <div className="mdetail-kicker mono">{danger ? 'CONFIRM REMOVAL' : 'CONFIRM'}</div>
              <div className="mdetail-title">{title}</div>
            </div>
          </div>
          <button className="mdetail-close" onClick={onClose} aria-label="Close">×</button>
        </header>
        <div className="mdetail-body">
          <p className="confirm-body">{body}</p>
        </div>
        <footer className="nl-foot">
          <button className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className={danger ? 'btn-danger' : 'btn-primary'} onClick={onConfirm} autoFocus>{confirmLabel}</button>
        </footer>
      </div>
    </div>
  );
}

Object.assign(window, { TaskBuilder, Employees, LinesManager, OperatorDashboard, ConfirmModal });
