# Schedule Detail Layout and Pagination Design

## Layout

- Desktop uses two columns: a 300px left rail and a flexible right workspace.
- Left rail stacks the full schedule above the preparation checklist.
- Right workspace stacks schedule chat above AI suggestions.
- Tablet and mobile retain a single-column flow.
- Schedule and checklist sections keep internal scrolling so the page does not grow solely from long lists.

## AI suggestion pagination

- Display three suggestions per page for the selected status.
- Slice the flat suggestion list first, then group only those three items by analysis run.
- Show previous/next controls and current/total page below the cards.
- Reset to page one when the status, trip, or freshly loaded suggestion result changes.
- Clamp the current page when actions remove the final item from a page.

## Accessibility

- Previous and next buttons expose descriptive labels and disabled states.
- The page indicator uses `aria-live="polite"`.
