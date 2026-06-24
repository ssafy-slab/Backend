# Schedule Height and AI Place Notice Design

## Goal

Give the full schedule panel more default vertical space on desktop and remove the warning shown for AI suggestions that are not linked to a place record.

## Design

- Keep the current responsive two-column layout.
- Change the desktop schedule panel minimum height from `360px` to `480px`.
- Change its desktop maximum height from `calc(100vh - 420px)` to `calc(100vh - 300px)`.
- Keep the existing non-desktop `680px` maximum.
- Do not render the DB linkage warning in AI suggestion cards.
- Keep place lookup, free-schedule application, voting, and acceptance behavior unchanged.

## Verification

- Component test asserts the expanded desktop height classes.
- Component test asserts the removed notice is not rendered.
- Run the schedule detail test, full frontend tests, type-check, and production build.
