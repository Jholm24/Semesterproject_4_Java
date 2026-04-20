const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accentWarehouse": "#2E55E6",
  "accentAgv": "#2F9D5E",
  "accentAssembly": "#D48A2E",
  "density": "comfy",
  "typeSystem": "inter-mono",
  "paperTint": "warm"
}/*EDITMODE-END*/;

function TweaksPanel() {
  const [open, setOpen] = useState(false);
  const [vis, setVis] = useState(false);
  const [cfg, setCfg] = useState(TWEAK_DEFAULTS);

  useEffect(() => {
    const onMsg = (e) => {
      if (!e.data) return;
      if (e.data.type === '__activate_edit_mode') { setVis(true); setOpen(true); }
      if (e.data.type === '__deactivate_edit_mode') { setVis(false); setOpen(false); }
    };
    window.addEventListener('message', onMsg);
    window.parent.postMessage({type:'__edit_mode_available'}, '*');
    return () => window.removeEventListener('message', onMsg);
  }, []);

  useEffect(() => {
    const r = document.documentElement;
    r.style.setProperty('--c-wh', cfg.accentWarehouse);
    r.style.setProperty('--c-agv', cfg.accentAgv);
    r.style.setProperty('--c-as', cfg.accentAssembly);
    r.setAttribute('data-density', cfg.density);
    r.setAttribute('data-type', cfg.typeSystem);
    r.setAttribute('data-paper', cfg.paperTint);
  }, [cfg]);

  const set = (patch) => { const next = {...cfg, ...patch}; setCfg(next); window.parent.postMessage({type:'__edit_mode_set_keys', edits: patch}, '*'); };

  if (!vis) return null;
  return (
    <div className={`tweaks ${open?'on':''}`}>
      <button className="tweaks-toggle" onClick={()=>setOpen(o=>!o)}>{open?'–':'⚙'} Tweaks</button>
      {open && (
        <div className="tweaks-body">
          <div className="tw-grp">
            <div className="tw-label">Accent · Warehouse</div>
            <input type="color" value={cfg.accentWarehouse} onChange={e=>set({accentWarehouse:e.target.value})}/>
          </div>
          <div className="tw-grp">
            <div className="tw-label">Accent · AGV</div>
            <input type="color" value={cfg.accentAgv} onChange={e=>set({accentAgv:e.target.value})}/>
          </div>
          <div className="tw-grp">
            <div className="tw-label">Accent · Assembly</div>
            <input type="color" value={cfg.accentAssembly} onChange={e=>set({accentAssembly:e.target.value})}/>
          </div>
          <div className="tw-grp">
            <div className="tw-label">Density</div>
            <div className="tw-seg">
              {['compact','comfy','spacious'].map(d => <button key={d} className={cfg.density===d?'on':''} onClick={()=>set({density:d})}>{d}</button>)}
            </div>
          </div>
          <div className="tw-grp">
            <div className="tw-label">Paper tint</div>
            <div className="tw-seg">
              {['warm','cool','neutral'].map(p => <button key={p} className={cfg.paperTint===p?'on':''} onClick={()=>set({paperTint:p})}>{p}</button>)}
            </div>
          </div>
          <div className="tw-grp">
            <div className="tw-label">Type system</div>
            <div className="tw-seg">
              {[['inter-mono','Inter + Mono'],['grotesk','Space Grotesk'],['serif','Serif heads']].map(([v,l]) => <button key={v} className={cfg.typeSystem===v?'on':''} onClick={()=>set({typeSystem:v})}>{l}</button>)}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

Object.assign(window, { TweaksPanel });
