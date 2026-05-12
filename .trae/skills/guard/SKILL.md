---
name: "guard"
description: "Full safety mode combining destructive warnings and directory restrictions. Invoke when user asks for guard mode, full safety, or maximum safety."
---

# Guard

Full safety mode combining destructive command warnings with directory-scoped edits.

## When to Use

- User asks for "guard mode"
- "Full safety" or "maximum safety"
- Working with production systems
- Debugging live environments

## Combines

- `/careful` warnings for destructive commands
- `/freeze` blocking edits outside specified directory
