---
name: "freeze"
description: "Restrict file edits to a specific directory. Invoke when user asks to freeze, restrict edits, or lock down a directory."
---

# Freeze

Restrict file edits to a specific directory for the session.

## When to Use

- Debugging to prevent accidentally fixing unrelated code
- User asks to "freeze" or "lock down"
- Wanting to scope changes to one module

## Behavior

Blocks edits outside the allowed path until unfrozen.
