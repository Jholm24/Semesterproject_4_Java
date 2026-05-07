# Production Tasks

Source: task.md — validated against codebase before execution.

---

## Task 1 — Production line panel wiring (Line-01, Line-02, etc.)

**Status:** Pending

**Problem:**
- `Dashboard.jsx:83-93` has `LINES` and `LINE_DEFAULTS` hardcoded with 3 fixed IDs.
- `LinesManager` (Other.jsx) stores lines in `localStorage('sb_lines')` and `sb_byline_v3`.
- Dashboard writes byLine state to `sb_byline_v4`, but `Topbar.jsx:55` reads from `sb_byline_v3` on cold load — **key mismatch breaks topbar status after refresh**.
- New lines created in LinesManager never appear in the Manager Dashboard.

**Steps:**
1. Replace static `LINES` const in `Dashboard.jsx` with a `useState` reading `localStorage('sb_lines')`, subscribing to `sb-lines-change` events (same pattern as Topbar).
2. Migrate byLine storage key from `sb_byline_v4` → `sb_byline_v3` throughout Dashboard.jsx.
3. Make `LINE_DEFAULTS` initialization dynamic — build defaults for any line from `sb_lines` that has no saved state yet.

---

## Task 2 — Employees panel

**Status:** Pending

**Problem:**
- Assigning an operator to a line via `NewLineModal`/`EditLineModal` updates `line.operators[]` but does NOT write back to the operator's `line` field in `sb_employees_v2`.
- Changing an employee's line via the table dropdown updates `sb_employees_v2` but does NOT update the old/new line's `operators[]` in `sb_lines`.
- `Tasks` button at `Other.jsx:473` has no handler.

**Steps:**
1. In `LinesManager.createLine()` and `updateLine()`: after saving the line, sync `sb_employees_v2` so each operator in `draft.operators` gets `line` set to the line name, and previously-assigned operators not in `draft.operators` get `line` reset to `'—'`.
2. In the Employees table line dropdown `onChange`: also update `sb_lines` to add the operator to the new line's `operators[]` and remove from the old line's `operators[]`.
3. Wire the `Tasks` button to `nav.setActiveLine(employee.lineId)` then `nav.setView('builder')` — only if the employee is assigned to a known line.

---

## Task 3 — Task builder palette

**Status:** Pending

**Problem:**
- "Save Template" button (`Other.jsx:74`) has no `onClick` — templates cannot be persisted.
- No way to load a previously saved template.

**Steps:**
1. Add `onClick` to "Save Template" that saves `{ name: templateName, seq }` to `localStorage('sb_templates')` as an array, namespaced by line ID.
2. Add a "Load" dropdown next to the template name input listing saved templates for the current line; selecting one replaces the current `seq`.

---

## Task 4 — Error fix

**Problem:**
Uncaught TypeError: Cannot read properties of undefined (reading 'map')
at ManagerDashboard (<anonymous>:343:29)
at renderWithHooks (react-dom.development.js:15496:20)
at updateFunctionComponent (react-dom.development.js:19627:22)
at beginWork (react-dom.development.js:21650:18)
at beginWork$1 (react-dom.development.js:27475:16)
at performUnitOfWork (react-dom.development.js:26609:14)
at workLoopSync (react-dom.development.js:26515:7)
at renderRootSync (react-dom.development.js:26483:9)
at recoverFromConcurrentError (react-dom.development.js:25899:22)
at performConcurrentWorkOnRoot (react-dom.development.js:25799:24)
at workLoop (react.development.js:2653:36)
at flushWork (react.development.js:2626:16)
at MessagePort.performWorkUntilDeadline (react.development.js:2920:23)Understand this error

## Task 5 — CLAUDE.md update

**Status:** Pending (do last)

Update CLAUDE.md to reflect the UI wiring completed in Tasks 1-3.
