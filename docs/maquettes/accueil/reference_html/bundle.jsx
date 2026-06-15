
// ===== ui.jsx =====
// ui.jsx — palette sombre CSP, icônes, petits composants partagés
const C = {
  red:'#F5333F', redDeep:'#D21F2B', redSoft:'rgba(245,51,63,0.14)',
  blue:'#4CCBEE', blueDeep:'#1FA9D2', blueSoft:'rgba(76,203,238,0.15)',
  green:'#3DD68C',
  bg:'#0D0F11', surface:'#15181B', surface2:'#1C2024', surface3:'#262B31',
  ink:'#F4F6F7', ink2:'#C5CCD1', muted:'#8A929A', muted2:'#5E666D',
  line:'rgba(255,255,255,0.09)', line2:'rgba(255,255,255,0.055)',
};

const greeting = () => (new Date().getHours() >= 16 ? 'Bonsoir' : 'Bonjour');

// ── Icônes (traits simples) ───────────────────────────────────
function Icon({ name, size = 22, color = 'currentColor', sw = 1.9, style = {} }) {
  const p = { fill:'none', stroke:color, strokeWidth:sw, strokeLinecap:'round', strokeLinejoin:'round' };
  const paths = {
    calendar:(<g {...p}><rect x="3" y="4.5" width="18" height="16.5" rx="3"/><path d="M3 9.5h18M8 2.5v4M16 2.5v4"/></g>),
    clock:(<g {...p}><circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 1.8"/></g>),
    pin:(<g {...p}><path d="M12 21.5s7-6.2 7-11.4A7 7 0 0 0 5 10.1C5 15.3 12 21.5 12 21.5Z"/><circle cx="12" cy="9.8" r="2.6"/></g>),
    share:(<g {...p}><path d="M12 15.5V4M8 7.5 12 3.5l4 4M5 13v5.5a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V13"/></g>),
    heart:(<path d="M12 20.5S3.5 15 3.5 8.9A4.6 4.6 0 0 1 12 6.2a4.6 4.6 0 0 1 8.5 2.7C20.5 15 12 20.5 12 20.5Z" fill="none" stroke={color} strokeWidth={sw} strokeLinejoin="round"/>),
    heartFill:(<path d="M12 20.5S3.5 15 3.5 8.9A4.6 4.6 0 0 1 12 6.2a4.6 4.6 0 0 1 8.5 2.7C20.5 15 12 20.5 12 20.5Z" fill={color} stroke={color} strokeWidth={sw} strokeLinejoin="round"/>),
    back:(<path d="M15 4 7 12l8 8" fill="none" stroke={color} strokeWidth={2.4} strokeLinecap="round" strokeLinejoin="round"/>),
    chevR:(<path d="M9 4l8 8-8 8" fill="none" stroke={color} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round"/>),
    map:(<g {...p}><path d="M9 4 3.5 6.2v13.3L9 17.3l6 2.2 5.5-2.2V4L15 6.2 9 4Z"/><path d="M9 4v13.3M15 6.2v13.3"/></g>),
    camera:(<g {...p}><path d="M4 8.5A2.5 2.5 0 0 1 6.5 6h1l1.3-2h6.4L16.5 6h1A2.5 2.5 0 0 1 20 8.5v9A2.5 2.5 0 0 1 17.5 20h-11A2.5 2.5 0 0 1 4 17.5Z"/><circle cx="12" cy="13" r="3.4"/></g>),
    comment:(<path d="M5 5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H9l-4 3.5V17H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z" fill="none" stroke={color} strokeWidth={sw} strokeLinejoin="round"/>),
    bell:(<g {...p}><path d="M18 8.5a6 6 0 0 0-12 0c0 6-2.5 7.5-2.5 7.5h17S18 14.5 18 8.5Z"/><path d="M10.2 20a2 2 0 0 0 3.6 0"/></g>),
    plus:(<path d="M12 5v14M5 12h14" fill="none" stroke={color} strokeWidth={2.4} strokeLinecap="round"/>),
    check:(<path d="M5 12.5 10 17l9-10" fill="none" stroke={color} strokeWidth={2.4} strokeLinecap="round" strokeLinejoin="round"/>),
    star:(<path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 17l-5.2 2.6 1-5.8-4.3-4.1 5.9-.9L12 3.5Z" fill={color} stroke="none"/>),
    users:(<g {...p}><circle cx="9" cy="8" r="3.2"/><path d="M3.5 19.5a5.5 5.5 0 0 1 11 0M16 5.2a3.2 3.2 0 0 1 0 5.6M17 14.5a5.5 5.5 0 0 1 3.5 5"/></g>),
    route:(<g {...p}><circle cx="6" cy="18.5" r="2.4"/><circle cx="18" cy="5.5" r="2.4"/><path d="M8.4 18.5H14a3 3 0 0 0 0-6h-4a3 3 0 0 1 0-6h5.6"/></g>),
    home:(<path d="M4 11.5 12 4l8 7.5M6 10v9h12v-9" fill="none" stroke={color} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round"/>),
    user:(<g {...p}><circle cx="12" cy="8" r="4"/><path d="M4.5 20a7.5 7.5 0 0 1 15 0"/></g>),
    calPlus:(<g {...p}><path d="M21 12.5V7a2.5 2.5 0 0 0-2.5-2.5h-13A2.5 2.5 0 0 0 3 7v11a2.5 2.5 0 0 0 2.5 2.5H12M3 9.5h18M8 2.5v4M16 2.5v4M17 15v6M14 18h6"/></g>),
    dots:(<g fill={color}><circle cx="5" cy="12" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="19" cy="12" r="2"/></g>),
  };
  return (<svg width={size} height={size} viewBox="0 0 24 24" style={style}>{paths[name]}</svg>);
}

// ── Badge logo CSP (cercle rouge/cyan) ───────────────────────
function CspBadge({ size = 44, ring = false }) {
  return (
    <div style={{
      width:size, height:size, borderRadius:'50%', overflow:'hidden', position:'relative',
      flexShrink:0, boxShadow: ring ? '0 0 0 2px '+C.bg+', 0 0 0 3.5px rgba(255,255,255,0.15)' : 'none',
    }}>
      <div style={{position:'absolute', inset:0, top:0, height:'50%', background:C.red}} />
      <div style={{position:'absolute', inset:0, top:'50%', background:C.blue}} />
      <div style={{position:'absolute', inset:0, display:'flex', alignItems:'center', justifyContent:'center',
        fontWeight:900, fontSize:size*0.30, color:'#111', letterSpacing:'0.02em',
        fontVariationSettings:"'wdth' 120,'wght' 900"}}>CSP</div>
    </div>
  );
}

// ── Icône calendrier (style choisi : pastille rouge + glyphe) ─
function CalIcon({ size = 46 }) {
  return (
    <div style={{
      width:size, height:size, borderRadius:14, background:C.redSoft,
      display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0,
      border:'1px solid rgba(245,51,63,0.22)',
    }}>
      <Icon name="calendar" size={size*0.5} color={C.red} sw={1.9} />
    </div>
  );
}

// ── Avatar initiales ─────────────────────────────────────────
function Avatar({ name = '', bg = C.surface3, size = 32, you = false }) {
  const initials = you ? 'Moi'
    : name.split(' ').filter(Boolean).map(w=>w[0]).slice(0,2).join('').toUpperCase();
  return (
    <div style={{
      width:size, height:size, borderRadius:'50%', flexShrink:0,
      background: you ? C.blue : bg, color: you ? '#08252e' : C.ink,
      display:'flex', alignItems:'center', justifyContent:'center',
      fontSize: you ? size*0.32 : size*0.4, fontWeight:700, border:'2px solid '+C.bg,
    }}>{initials}</div>
  );
}

// ── Statut (pastille) ────────────────────────────────────────
function StatusTag({ status }) {
  const map = {
    open:{ t:'Ouvert', c:C.green, bg:'rgba(61,214,140,0.13)' },
    cancelled:{ t:'Annulé', c:C.red, bg:C.redSoft },
    full:{ t:'Complet', c:C.blue, bg:C.blueSoft },
  };
  const s = map[status] || map.open;
  return (
    <span style={{
      display:'inline-flex', alignItems:'center', gap:6, padding:'4px 10px', borderRadius:999,
      background:s.bg, color:s.c, fontSize:12, fontWeight:700, letterSpacing:'0.01em',
    }}>
      <span style={{width:6, height:6, borderRadius:'50%', background:s.c}} />{s.t}
    </span>
  );
}

Object.assign(window, { C, greeting, Icon, CspBadge, CalIcon, Avatar, StatusTag });


// ===== home.jsx =====
// home.jsx — écran d'accueil, 2 variations (A: cartes, B: agenda)
const fmtDate = (e) => `${e.weekday} ${e.day} ${e.month} · ${e.time}`;

// ── En-tête : logo club + cloche ─────────────────────────────
function HomeHeader() {
  return (
    <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', padding:'2px 0 16px'}}>
      <img src="assets/logo-csp.png" alt="Cyclo Sport Pantin"
        style={{height:50, width:'auto', borderRadius:11, display:'block',
          boxShadow:'0 2px 10px rgba(0,0,0,0.35)'}} />
      <button className="tap" style={{
        width:42, height:42, borderRadius:'50%', border:'1px solid '+C.line,
        background:C.surface, display:'flex', alignItems:'center', justifyContent:'center',
        position:'relative'}}>
        <Icon name="bell" size={20} color={C.ink2} />
        <span style={{position:'absolute', top:9, right:10, width:8, height:8, borderRadius:'50%',
          background:C.red, border:'2px solid '+C.surface}} />
      </button>
    </div>
  );
}

function Greeting() {
  return (
    <div style={{marginBottom:18}}>
      <div className="display" style={{fontSize:34, color:C.ink, lineHeight:1.02}}>{greeting()}</div>
      <div style={{fontSize:15, color:C.muted, marginTop:6, fontWeight:500}}>Prêt·e à rouler&nbsp;?</div>
    </div>
  );
}

// ── Mise en avant : à la une (haut rouge / bas cyan) ─────────
function NextRideHero({ ev, onOpen }) {
  const cyanInk = '#0a2c36';
  return (
    <div className="tap fade-up" onClick={()=>onOpen(ev.id)} style={{
      position:'relative', overflow:'hidden', borderRadius:22, marginBottom:24,
      boxShadow:'0 16px 32px -12px rgba(0,0,0,0.55)',
    }}>
      {/* haut : rouge */}
      <div style={{position:'relative', overflow:'hidden', background:'linear-gradient(135deg, #F5333F 0%, #D21F2B 82%)', padding:'18px 18px 15px'}}>
        <div className="display-wide" style={{position:'absolute', right:-8, top:-16, fontSize:96,
          color:'rgba(255,255,255,0.13)', pointerEvents:'none', userSelect:'none', lineHeight:1}}>CSP</div>
        <div style={{position:'relative'}}>
          <span style={{display:'inline-flex', alignItems:'center', gap:7, padding:'5px 11px', borderRadius:999,
            background:'rgba(255,255,255,0.2)', color:'#fff', fontSize:11.5, fontWeight:800, letterSpacing:'0.06em'}}>
            <Icon name="route" size={13} color="#fff" sw={2.2} /> PROCHAINE SORTIE
          </span>
          <div className="display" style={{fontSize:22, color:'#fff', lineHeight:1.1, marginTop:13, maxWidth:'94%'}}>{ev.title}</div>
        </div>
      </div>
      {/* bas : cyan (comme le logo) */}
      <div style={{background:C.blue, padding:'14px 18px 16px'}}>
        <div style={{display:'flex', alignItems:'center', gap:16, color:cyanInk, fontSize:14, fontWeight:700}}>
          <span style={{display:'inline-flex', alignItems:'center', gap:6}}><Icon name="calendar" size={15} color={cyanInk} sw={2} /> {ev.weekday} {ev.day} {ev.month}</span>
          <span style={{display:'inline-flex', alignItems:'center', gap:6}}><Icon name="clock" size={15} color={cyanInk} sw={2} /> {ev.time}</span>
        </div>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginTop:14}}>
          <div style={{display:'flex', alignItems:'center'}}>
            <div style={{display:'flex'}}>
              {['Léa Bart','Marc D','Tom R'].map((n,i)=>(
                <div key={i} style={{marginLeft:i?-10:0}}><Avatar name={n} size={28} bg="#ffffff" /></div>
              ))}
            </div>
            <span style={{marginLeft:10, color:cyanInk, fontSize:13, fontWeight:700}}>{ev.participants} participants</span>
          </div>
          <span style={{display:'inline-flex', alignItems:'center', gap:6, padding:'9px 15px', borderRadius:999,
            background:'#fff', color:C.redDeep, fontSize:14, fontWeight:800}}>Voir <Icon name="chevR" size={14} color={C.redDeep} sw={2.4} /></span>
        </div>
      </div>
    </div>
  );
}

// ── Variation A : cartes ─────────────────────────────────────
function EventCard({ ev, onOpen }) {
  return (
    <div className="tap" onClick={()=>onOpen(ev.id)} style={{
      display:'flex', alignItems:'center', gap:13, padding:'12px 13px', borderRadius:16,
      background:C.surface, border:'1px solid '+C.line2,
    }}>
      <CalIcon />
      <div style={{flex:1, minWidth:0}}>
        <div style={{fontSize:15.5, fontWeight:700, color:C.ink, lineHeight:1.25,
          overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{ev.title}</div>
        <div style={{fontSize:13, color:C.muted, marginTop:3, fontWeight:500}}>{fmtDate(ev)}</div>
      </div>
      {ev.status==='cancelled' && <span style={{fontSize:11.5, fontWeight:700, color:C.red, marginRight:2}}>Annulé</span>}
      <Icon name="chevR" size={17} color={C.muted2} />
    </div>
  );
}

// ── Agenda / timeline (jour 3 lettres + nombre + mois) ───────
function AgendaItem({ ev, onOpen, last }) {
  const cancelled = ev.status==='cancelled';
  const dotC = cancelled ? C.red : C.blue;
  return (
    <div className="tap" onClick={()=>onOpen(ev.id)} style={{display:'flex', gap:14, alignItems:'stretch'}}>
      <div style={{width:44, textAlign:'center', paddingTop:7, flexShrink:0}}>
        <div style={{fontSize:11, color:C.muted, fontWeight:700, lineHeight:1}}>{ev.wd3}</div>
        <div className="display" style={{fontSize:21, color:C.ink, lineHeight:1.1, margin:'2px 0 1px'}}>{ev.day}</div>
        <div style={{fontSize:10, color:C.muted, fontWeight:700, letterSpacing:'0.04em'}}>{ev.monthShort}</div>
      </div>
      <div style={{position:'relative', width:14, flexShrink:0, display:'flex', justifyContent:'center'}}>
        <div style={{position:'absolute', top:0, bottom:0, width:2, background:C.line}} />
        {cancelled ? (
          <div style={{position:'absolute', top:11, width:20, height:20, borderRadius:'50%', background:C.bg,
            display:'flex', alignItems:'center', justifyContent:'center'}}>
            <svg width="12" height="12" viewBox="0 0 12 12"><path d="M2.2 2.2l7.6 7.6M9.8 2.2l-7.6 7.6" stroke={C.red} strokeWidth="2.4" strokeLinecap="round"/></svg>
          </div>
        ) : (
          <div style={{position:'absolute', top:16, width:12, height:12, borderRadius:'50%', background:dotC,
            boxShadow:'0 0 0 4px '+C.bg}} />
        )}
      </div>
      <div style={{flex:1, minWidth:0, paddingBottom: last?2:16}}>
        <div style={{display:'flex', alignItems:'center', gap:10, borderRadius:14, padding:'12px 13px',
          background:C.surface, border:'1px solid '+C.line2}}>
          <div style={{flex:1, minWidth:0}}>
            <div style={{fontSize:15, fontWeight:700, color:C.ink, lineHeight:1.25,
              textDecoration: cancelled? 'line-through':'none'}}>{ev.title}</div>
            <div style={{display:'flex', alignItems:'center', gap:7, fontSize:13, marginTop:5, fontWeight:600}}>
              <Icon name="clock" size={14} color={cancelled? C.red : C.muted} />
              <span style={{color: cancelled? C.red : C.muted, textDecoration: cancelled? 'line-through':'none'}}>{ev.time}</span>
              {cancelled && <span style={{color:C.red, fontWeight:800}}>· Annulé</span>}
            </div>
          </div>
          <Icon name="chevR" size={17} color={C.muted2} />
        </div>
      </div>
    </div>
  );
}

// ── Barre de navigation ──────────────────────────────────────
function BottomNav({ insetB = 8 }) {
  const tabs = [['home','Accueil',true],['calendar','Événements',false],['user','Profil',false]];
  return (
    <div style={{
      display:'flex', borderTop:'1px solid '+C.line, background:'rgba(13,15,17,0.92)',
      backdropFilter:'blur(12px)', WebkitBackdropFilter:'blur(12px)',
      paddingBottom:insetB, flexShrink:0,
    }}>
      {tabs.map(([icon,label,active])=>(
        <div key={label} className="tap" style={{flex:1, display:'flex', flexDirection:'column', alignItems:'center',
          gap:4, padding:'10px 0 8px', color: active? C.red : C.muted2}}>
          <Icon name={icon} size={23} color={active? C.red : C.muted2} sw={active?2.1:1.8} />
          <span style={{fontSize:10.5, fontWeight: active?700:600}}>{label}</span>
        </div>
      ))}
    </div>
  );
}

// ── Écran ────────────────────────────────────────────────────
function HomeScreen({ events, featured, onOpen, insets={top:16,bottom:8} }) {
  const agenda = events.filter(e=>!e.featured);
  return (
    <div style={{height:'100%', display:'flex', flexDirection:'column', background:C.bg}}>
      <div className="csp-scroll" style={{flex:1, overflowY:'auto', padding:`${insets.top}px 18px 22px`}}>
        <Greeting />
        <NextRideHero ev={featured} onOpen={onOpen} />
        <div style={{display:'flex', alignItems:'baseline', justifyContent:'space-between', marginBottom:16}}>
          <div className="display" style={{fontSize:17, color:C.ink}}>Sorties suivantes</div>
        </div>
        <div className="fade-up">
          {agenda.map((e,i)=>(
            <AgendaItem key={e.id} ev={e} onOpen={onOpen} last={i===agenda.length-1} />
          ))}
        </div>
      </div>
      <BottomNav insetB={insets.bottom} />
    </div>
  );
}

Object.assign(window, { HomeScreen, BottomNav });


// ===== detail.jsx =====
// detail.jsx — écran détail d'un événement (thème sombre CSP, interactif)
const CC = C, Ic = Icon, Badge = CspBadge, Av = Avatar;

function GlassBtn({ children, onClick, active }) {
  return (
    <button className="tap" onClick={onClick} style={{
      width:42, height:42, borderRadius:'50%', border:'1px solid rgba(255,255,255,0.16)',
      background:'rgba(10,12,14,0.42)', backdropFilter:'blur(10px)', WebkitBackdropFilter:'blur(10px)',
      display:'flex', alignItems:'center', justifyContent:'center', padding:0,
    }}>{children}</button>
  );
}

function InfoRow({ icon, title, sub, action, last }) {
  return (
    <div style={{display:'flex', alignItems:'center', gap:14, padding:'14px 0',
      borderBottom: last? 'none' : '1px solid '+CC.line2}}>
      <div style={{width:38, height:38, borderRadius:11, background:CC.redSoft, flexShrink:0,
        display:'flex', alignItems:'center', justifyContent:'center'}}>
        <Ic name={icon} size={19} color={CC.red} />
      </div>
      <div style={{flex:1, minWidth:0}}>
        <div style={{fontSize:15, fontWeight:700, color:CC.ink, lineHeight:1.3}}>{title}</div>
        <div style={{fontSize:13, color:CC.muted, marginTop:2}}>{sub}</div>
      </div>
      {action && <button className="tap" style={{width:38, height:38, borderRadius:11, border:'1px solid '+CC.line,
        background:CC.surface2, display:'flex', alignItems:'center', justifyContent:'center'}}>
        <Ic name={action} size={18} color={CC.ink2} /></button>}
    </div>
  );
}

function SectionTitle({ children, action, onAction }) {
  return (
    <div style={{display:'flex', alignItems:'baseline', justifyContent:'space-between', margin:'26px 0 12px'}}>
      <div className="display" style={{fontSize:18, color:CC.ink}}>{children}</div>
      {action && <span className="tap" onClick={onAction} style={{fontSize:13.5, color:CC.blue, fontWeight:700}}>{action}</span>}
    </div>
  );
}

function MapPlaceholder({ label }) {
  return (
    <div style={{position:'relative', height:158, borderRadius:16, overflow:'hidden',
      background:CC.surface2, border:'1px solid '+CC.line2}}>
      <div style={{position:'absolute', inset:0,
        backgroundImage:'repeating-linear-gradient(45deg, rgba(255,255,255,0.03) 0 1px, transparent 1px 13px), repeating-linear-gradient(-45deg, rgba(255,255,255,0.03) 0 1px, transparent 1px 13px)'}} />
      {/* fausses voies */}
      <div style={{position:'absolute', left:'-5%', right:'-5%', top:'58%', height:8, background:'rgba(76,203,238,0.12)', transform:'rotate(-7deg)'}} />
      <div style={{position:'absolute', left:'30%', top:'-10%', bottom:'-10%', width:7, background:'rgba(255,255,255,0.05)', transform:'rotate(9deg)'}} />
      {/* épingle */}
      <div style={{position:'absolute', left:'50%', top:'46%', transform:'translate(-50%,-100%)'}}>
        <div style={{width:30, height:30, borderRadius:'50% 50% 50% 0', background:CC.red, transform:'rotate(-45deg)',
          boxShadow:'0 6px 14px rgba(0,0,0,0.4)', display:'flex', alignItems:'center', justifyContent:'center'}}>
          <div style={{width:9, height:9, borderRadius:'50%', background:'#fff', transform:'rotate(45deg)'}} />
        </div>
      </div>
      <div style={{position:'absolute', left:12, bottom:10, fontSize:10.5, letterSpacing:'0.08em',
        fontFamily:'ui-monospace,Menlo,monospace', color:CC.muted2}}>CARTE — {label}</div>
    </div>
  );
}

function DetailScreen({ ev, onBack, insets={top:54, bottom:24} }) {
  const cancelled = ev.status==='cancelled';
  const [liked, setLiked] = React.useState(false);
  const [joined, setJoined] = React.useState(false);
  const [expanded, setExpanded] = React.useState(false);
  const [chip, setChip] = React.useState(0);
  const [text, setText] = React.useState('');
  const [comments, setComments] = React.useState(ev.comments || []);
  const count = ev.participants + (joined?1:0);

  const send = () => {
    const t = text.trim(); if(!t) return;
    setComments([{ name:'Moi', time:"À l'instant", text:t, you:true, likes:0 }, ...comments]);
    setText('');
  };

  return (
    <div style={{height:'100%', display:'flex', flexDirection:'column', background:CC.bg}}>
      <div className="csp-scroll" style={{flex:1, overflowY:'auto'}}>
        {/* HÉRO */}
        <div style={{position:'relative', height:212}}>
          <img src="assets/logo-csp.png" alt="" style={{width:'100%', height:'100%', objectFit:'cover', display:'block'}} />
          <div style={{position:'absolute', inset:0, background:'linear-gradient(180deg, rgba(13,15,17,0.45) 0%, rgba(13,15,17,0) 26%, rgba(13,15,17,0) 60%, rgba(13,15,17,0.85) 100%)'}} />
          <div style={{position:'absolute', top:insets.top, left:14, right:14, display:'flex', justifyContent:'space-between'}}>
            <GlassBtn onClick={onBack}><Ic name="back" size={20} color="#fff" /></GlassBtn>
            <div style={{display:'flex', gap:10}}>
              <GlassBtn><Ic name="share" size={18} color="#fff" /></GlassBtn>
              <GlassBtn onClick={()=>setLiked(v=>!v)} active={liked}>
                <Ic name={liked?'heartFill':'heart'} size={19} color={liked?CC.red:'#fff'} />
              </GlassBtn>
            </div>
          </div>
          {cancelled && (
            <div style={{position:'absolute', left:0, right:0, bottom:0, background:CC.redDeep, color:'#fff',
              textAlign:'center', padding:'9px', fontSize:14, fontWeight:800, letterSpacing:'0.02em'}}>Annulé</div>
          )}
        </div>

        <div style={{padding:'18px 18px 8px'}}>
          {!cancelled && <div style={{marginBottom:10}}><StatusTag status={ev.status} /></div>}
          <div className="display" style={{fontSize:27, color:CC.ink, lineHeight:1.08}}>{ev.title}</div>

          {/* chips de dates */}
          <div className="csp-scroll" style={{display:'flex', gap:8, overflowX:'auto', margin:'16px -18px 4px', padding:'0 18px'}}>
            {ev.chips.map((c,i)=>(
              <button key={i} className="tap" onClick={()=>setChip(i)} style={{
                flexShrink:0, padding:'8px 13px', borderRadius:11, fontSize:13, fontWeight:700,
                border:'1px solid '+(chip===i? 'transparent':CC.line),
                background: chip===i? CC.red : CC.surface, color: chip===i? '#fff' : CC.ink2,
              }}>{c}</button>
            ))}
          </div>

          {/* infos */}
          <div style={{marginTop:12}}>
            <InfoRow icon="calendar" title={ev.dateLong} sub={ev.timeRange+' GMT+2'} action="calPlus" />
            <InfoRow icon="pin" title={ev.place} sub={ev.address} action="map" last />
          </div>

          {/* organisateur */}
          <div style={{display:'flex', alignItems:'center', gap:13, padding:'13px', borderRadius:16,
            background:CC.surface, border:'1px solid '+CC.line2, marginTop:6}}>
            <Badge size={46} />
            <div style={{flex:1}}>
              <div style={{fontSize:15.5, fontWeight:800, color:CC.ink}}>Cyclo Sport Pantin</div>
              <div style={{display:'flex', alignItems:'center', gap:6, marginTop:3, fontSize:13, color:CC.muted}}>
                <Ic name="star" size={14} color={CC.red} /><b style={{color:CC.ink2}}>4,9</b> · {ev.members} membres
              </div>
            </div>
            <button className="tap" style={{padding:'8px 14px', borderRadius:999, border:'1px solid '+CC.line,
              background:CC.surface2, color:CC.ink, fontSize:13, fontWeight:700}}>Suivre</button>
          </div>

          {/* rendez-vous */}
          <SectionTitle>Rendez-vous</SectionTitle>
          <ul style={{margin:0, paddingLeft:18, color:CC.ink2, fontSize:14.5, lineHeight:1.6}}>
            {ev.desc.slice(0, expanded? ev.desc.length : 2).map((d,i)=>(
              <li key={i} style={{marginBottom:7}}>{d}</li>
            ))}
          </ul>
          {ev.desc.length>2 && (
            <span className="tap" onClick={()=>setExpanded(v=>!v)} style={{display:'inline-block', marginTop:6,
              color:CC.blue, fontSize:14, fontWeight:700}}>{expanded?'Réduire':'Lire la suite'}</span>
          )}

          {/* participation */}
          <div style={{display:'flex', gap:24, marginTop:22}}>
            <div>
              <div style={{fontSize:13, color:CC.muted, fontWeight:600, marginBottom:8}}>Organisation 1</div>
              <Badge size={40} />
            </div>
            <div>
              <div style={{fontSize:13, color:CC.muted, fontWeight:600, marginBottom:8}}>Participants {count}</div>
              <div style={{display:'flex'}}>
                {joined && <div style={{zIndex:3}}><Av you size={40} /></div>}
                {['Léa Bart','Marc Dauphin','Tom Roy'].map((n,i)=>(
                  <div key={i} style={{marginLeft:(i||joined)?-12:0, zIndex:2-i}}><Av name={n} size={40} /></div>
                ))}
                <div style={{marginLeft:-12, width:40, height:40, borderRadius:'50%', background:CC.surface3,
                  border:'2px solid '+CC.bg, display:'flex', alignItems:'center', justifyContent:'center',
                  fontSize:12, fontWeight:700, color:CC.ink2}}>+{Math.max(0,count-3)}</div>
              </div>
            </div>
          </div>

          {/* lieu */}
          <SectionTitle action="Itinéraire">Lieu</SectionTitle>
          <MapPlaceholder label={ev.place.split('—')[0].trim()} />
          <div style={{borderLeft:'3px solid '+CC.red, paddingLeft:12, marginTop:14}}>
            <div style={{fontSize:15, fontWeight:700, color:CC.ink}}>{ev.place}</div>
            <div style={{fontSize:13.5, color:CC.muted, marginTop:2}}>{ev.address}</div>
          </div>

          {/* photos */}
          <SectionTitle action="Ajouter">Photos</SectionTitle>
          <div style={{display:'flex', alignItems:'center', gap:13, padding:'18px', borderRadius:16,
            border:'1px dashed '+CC.line, background:CC.surface}}>
            <div style={{width:42, height:42, borderRadius:11, background:CC.surface2, display:'flex',
              alignItems:'center', justifyContent:'center'}}><Ic name="camera" size={21} color={CC.muted} /></div>
            <div style={{fontSize:13.5, color:CC.muted, lineHeight:1.45}}>Pas encore de photos. Elles apparaîtront ici une fois la sortie passée.</div>
          </div>

          {/* commentaires */}
          <SectionTitle>Commentaires {comments.length}</SectionTitle>
          <div style={{display:'flex', flexDirection:'column', gap:16}}>
            {comments.map((c,i)=>(
              <div key={i} style={{display:'flex', gap:11}}>
                {c.you ? <Av you size={36} /> : <Av name={c.name} size={36} bg={CC.surface3} />}
                <div style={{flex:1}}>
                  <div style={{fontSize:14, color:CC.ink}}><b>{c.name}</b>
                    <span style={{color:CC.muted2, fontWeight:500, marginLeft:8, fontSize:12.5}}>{c.time}</span></div>
                  <div style={{fontSize:14, color:CC.ink2, marginTop:3, lineHeight:1.5}}>{c.text}</div>
                  <div style={{display:'flex', alignItems:'center', gap:18, marginTop:7, fontSize:13, color:CC.muted, fontWeight:600}}>
                    <span className="tap" style={{display:'inline-flex', alignItems:'center', gap:5}}><Ic name="heart" size={14} color={CC.muted} /> {c.likes}</span>
                    <span className="tap">Répondre</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
          {/* champ commentaire */}
          <div style={{display:'flex', alignItems:'center', gap:10, marginTop:16}}>
            <Av you size={36} />
            <div style={{flex:1, display:'flex', alignItems:'center', gap:8, background:CC.surface,
              border:'1px solid '+CC.line, borderRadius:999, padding:'4px 4px 4px 16px'}}>
              <input value={text} onChange={e=>setText(e.target.value)}
                onKeyDown={e=>{if(e.key==='Enter') send();}}
                placeholder="Laissez un commentaire…" style={{flex:1, border:'none', outline:'none',
                background:'transparent', color:CC.ink, fontSize:14, fontFamily:'inherit'}} />
              <button className="tap" onClick={send} style={{width:34, height:34, borderRadius:'50%', border:'none',
                background: text.trim()? CC.red : CC.surface3, display:'flex', alignItems:'center', justifyContent:'center'}}>
                <Ic name="share" size={16} color={text.trim()? '#fff' : CC.muted} sw={2.2} />
              </button>
            </div>
          </div>

          {/* à venir */}
          <SectionTitle action="Tout voir">Événements à venir</SectionTitle>
          <div style={{display:'flex', alignItems:'center', gap:13, padding:'11px', borderRadius:16,
            background:CC.surface, border:'1px solid '+CC.line2}}>
            <div style={{width:54, height:54, borderRadius:12, overflow:'hidden', flexShrink:0}}>
              <img src="assets/logo-csp.png" alt="" style={{width:'100%', height:'100%', objectFit:'cover'}} />
            </div>
            <div style={{flex:1}}>
              <div style={{fontSize:14.5, fontWeight:700, color:CC.ink, lineHeight:1.3}}>{ev.nextTitle}</div>
              <div style={{fontSize:12.5, color:CC.muted, marginTop:3}}>{ev.nextDate}</div>
            </div>
            <Ic name="chevR" size={17} color={CC.muted2} />
          </div>
        </div>
      </div>

      {/* CTA fixe */}
      <div style={{padding:`12px 18px ${insets.bottom}px`, borderTop:'1px solid '+CC.line,
        background:'rgba(13,15,17,0.94)', backdropFilter:'blur(12px)', WebkitBackdropFilter:'blur(12px)', flexShrink:0}}>
        {cancelled ? (
          <div style={{textAlign:'center', padding:'15px', borderRadius:14, background:CC.surface2,
            color:CC.muted, fontSize:15, fontWeight:700}}>Événement annulé</div>
        ) : (
          <button className="tap" onClick={()=>setJoined(v=>!v)} style={{
            width:'100%', padding:'15px', borderRadius:14, border:'none', cursor:'pointer',
            fontSize:16, fontWeight:800, fontFamily:'inherit',
            display:'flex', alignItems:'center', justifyContent:'center', gap:9,
            background: joined? CC.surface2 : CC.red, color: joined? CC.green : '#fff',
            boxShadow: joined? 'none' : '0 10px 22px -8px rgba(245,51,63,0.6)',
            border: joined? '1px solid '+CC.line : 'none',
          }}>
            {joined ? <><Ic name="check" size={19} color={CC.green} /> Vous participez</>
                    : <>Participer · {count} inscrits</>}
          </button>
        )}
      </div>
    </div>
  );
}

Object.assign(window, { DetailScreen });


// ===== app.jsx =====
// app.jsx — données + plan de travail (Android + iPhone, variations A/B + détail)
const K = C, Mark = CspBadge;

const EVENTS = [
  {
    id:'equip', title:'Présentation des équipements 2026',
    wd3:'Dim.', weekday:'dim.', day:21, month:'juin', monthShort:'juin', time:'19h00', status:'open',
    participants:42, members:248,
    dateLong:'dimanche 21 juin 2026', timeRange:'19:00 – 21:00',
    place:'Le Local — Pantin', address:'14 rue Cartier-Bresson, Pantin',
    chips:['Dim 21 juin','Présentation','Essayage'],
    desc:[
      'Rendez-vous au local du club pour découvrir la collection 2026 (maillots, cuissards, coupe-vent).',
      'Essayage des tailles sur place — pensez à venir un peu en avance.',
      'Prise des commandes groupées à l’issue de la réunion, paiement en ligne.',
      'Un pot convivial clôturera la soirée. Ouvert à tous les adhérents.',
    ],
    comments:[{ name:'Sophie Renard', time:'Il y a 3 h', likes:2,
      text:'Super, j’avais hâte de voir les nouveaux coloris ! Je passe avec deux amis intéressés par le club.' }],
    nextTitle:'Sortie CSP', nextDate:'sam. 27 juin · 8h00 · Annulé',
  },
  {
    id:'sortie', title:'Sortie CSP',
    wd3:'Sam.', weekday:'sam.', day:27, month:'juin', monthShort:'juin', time:'8h00', status:'cancelled',
    participants:1, members:248,
    dateLong:'samedi 27 juin 2026', timeRange:'08:00 – 12:30',
    place:'Le Drapeau — Vincennes', address:'18 avenue de Paris, Vincennes',
    chips:['Sam 27 juin','Sam 4 juil.','Sam 11 juil.'],
    desc:[
      'Rendez-vous en face de la brasserie Le Drapeau.',
      'Départ à 8h00, retour vers 12h30. Prévoyez un peu d’avance en cas d’imprévu sur le trajet.',
      'Allure groupe 2 (28–30 km/h), parcours roulant vers la vallée de Chevreuse.',
      'Pensez aux bidons et à un coupe-vent, la météo reste fraîche le matin.',
    ],
    comments:[{ name:'Arnaud Vigouroux', time:'Il y a 8 h', likes:0,
      text:'Sortie classique au Drapeau annulée : venez plutôt rouler sur le 2×100 dimanche !' }],
    nextTitle:'Rallye US Bois Saint-Denis', nextDate:'mer. 17 juin · 8h00',
  },
  {
    id:'rallye-bsd', title:'Rallye US Bois Saint-Denis',
    wd3:'Mer.', weekday:'mer.', day:17, month:'juin', monthShort:'juin', time:'8h00', status:'open', featured:true,
    participants:36, members:248,
    dateLong:'mercredi 17 juin 2026', timeRange:'08:00 – 13:00',
    place:'Bois Saint-Denis — Clamart', address:'Avenue du Bois, Clamart',
    chips:['Mer 17 juin','60 km','100 km'],
    desc:[
      'Rallye organisé par l’US Bois Saint-Denis, le CSP roule en groupe.',
      'Rendez-vous 7h45 au local pour partir ensemble, inscriptions sur place dès 8h00.',
      'Deux parcours balisés au choix : 60 km (groupe découverte) ou 100 km (groupe sportif).',
      'Ravitaillement à mi-parcours offert. Casque obligatoire, licence ou certificat à jour.',
    ],
    comments:[{ name:'Karim Belkacem', time:'Il y a 1 j', likes:4,
      text:'Je serai sur le 100 km, on part tranquille les 20 premiers kilomètres pour s’échauffer 👍' }],
    nextTitle:'Rallye CSP', nextDate:'dim. 28 juin · 7h30',
  },
  {
    id:'rallye-csp', title:'Rallye CSP',
    wd3:'Dim.', weekday:'dim.', day:28, month:'juin', monthShort:'juin', time:'7h30', status:'open',
    participants:58, members:248,
    dateLong:'dimanche 28 juin 2026', timeRange:'07:30 – 13:00',
    place:'Parc de la Villette — Pantin', address:'211 av. Jean Jaurès, Paris 19e',
    chips:['Dim 28 juin','45 km','90 km','130 km'],
    desc:[
      'Le grand rendez-vous annuel du club ! Trois distances pour tous les niveaux.',
      'Accueil et retrait des plaques dès 7h00 au Parc de la Villette.',
      'Parcours fléchés, signaleurs aux points clés, ravitaillements garnis.',
      'Repas convivial à l’arrivée (sur réservation). Bénévoles bienvenus.',
    ],
    comments:[{ name:'Hélène Moreau', time:'Il y a 2 j', likes:6,
      text:'Je m’occupe du ravito du km 60 comme l’an dernier. Il me manque deux bénévoles, MP !' }],
    nextTitle:'Présentation des équipements 2026', nextDate:'dim. 21 juin · 19h00',
  },
];

const FEATURED = EVENTS.find(e=>e.featured) || EVENTS[0];
const INSETS = {
  ios:{ top:54, bottom:24 },
  android:{ top:8, bottom:10 },
};

function AppInstance({ start='home', startEventId }) {
  const [screen, setScreen] = React.useState(start);
  const [eventId, setEventId] = React.useState(startEventId || EVENTS[0].id);
  const open = (id)=>{ setEventId(id); setScreen('detail'); };
  const ev = EVENTS.find(e=>e.id===eventId) || EVENTS[0];
  const ins = INSETS.android;
  return (
    <window.AndroidDevice dark>
      <div key={screen} className={screen==='detail'?'screen-enter':''} style={{height:'100%'}}>
        {screen==='home'
          ? <HomeScreen events={EVENTS} featured={FEATURED} onOpen={open}
              insets={{top:16, bottom: ins.bottom}} />
          : <DetailScreen ev={ev} onBack={()=>setScreen('home')} insets={ins} />}
      </div>
    </window.AndroidDevice>
  );
}

function Caption({ frame }) {
  return (
    <div style={{display:'flex', alignItems:'center', justifyContent:'center', gap:7, marginTop:14,
      color:K.muted, fontSize:13, fontWeight:600}}>
      <span style={{width:7, height:7, borderRadius:'50%', background: frame==='android'? K.green : K.blue}} />
      {frame==='android' ? 'Android' : 'iPhone'}
    </div>
  );
}

function Section({ index, title, desc, children }) {
  return (
    <section style={{marginBottom:64}}>
      <div style={{display:'flex', alignItems:'baseline', gap:14, marginBottom:26, maxWidth:920}}>
        <span className="display-wide" style={{fontSize:15, color:K.red,
          fontVariationSettings:"'wght' 800"}}>{index}</span>
        <div>
          <div className="display" style={{fontSize:22, color:K.ink, letterSpacing:'-0.01em'}}>{title}</div>
          <div style={{fontSize:14, color:K.muted, marginTop:4, fontWeight:500}}>{desc}</div>
        </div>
      </div>
      <div style={{display:'flex', flexWrap:'wrap', gap:40}}>{children}</div>
    </section>
  );
}

function Board() {
  return (
    <div style={{minHeight:'100vh', padding:'46px 40px 80px',
      background:'radial-gradient(1200px 600px at 22% -8%, #14171B 0%, #08090B 60%)'}}>
      {/* en-tête plan de travail */}
      <header style={{display:'flex', alignItems:'center', gap:16, marginBottom:48}}>
        <Mark size={52} />
        <div>
          <div className="display" style={{fontSize:26, color:K.ink, letterSpacing:'-0.01em'}}>Cyclo Sport Pantin — App mobile</div>
          <div style={{fontSize:14.5, color:K.muted, marginTop:5, fontWeight:500}}>
            Maquette Android · thème sombre · prototype cliquable (touchez un événement → détail)
          </div>
        </div>
      </header>

      <Section index="01" title="Accueil"
        desc="Salutation, mise en avant « Prochaine sortie » et liste des sorties suivantes.">
        <AppInstance start="home" />
      </Section>

      <Section index="02" title="Écran détail — au clic sur un événement"
        desc="Structure reprise de l’écran Meetup, recolorisée CSP. Participer, J’aime et commentaires fonctionnent.">
        <AppInstance start="detail" startEventId="rallye-bsd" />
      </Section>
    </div>
  );
}

(function boot(){
  if(!(window.IOSDevice && window.AndroidDevice)) return setTimeout(boot, 20);
  ReactDOM.createRoot(document.getElementById('root')).render(<Board />);
})();

