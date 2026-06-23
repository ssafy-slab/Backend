# AI Place Matching Design

AI schedule items return `placeName` and `regionHint`, never an internal `placeId`.

Before storing an `AI_SUGGESTION`, the backend searches `PLACE` and `REGION`. It links `suggested_place_id` only when one exact normalized place-name candidate remains and the optional region hint matches that candidate's region name, full region name, or address. Missing, partial, or ambiguous matches remain `NULL`.

The original AI text is always retained in `suggested_place_name` and `suggested_region_hint`. Applying a matched suggestion copies `suggested_place_id` into `SCHEDULE_ITEM.place_id`; unmatched suggestions remain valid free-form schedules.

Tests cover exact unique matching, ambiguous matching, unmatched places, persistence, API response fields, and GMS JSON parsing.
