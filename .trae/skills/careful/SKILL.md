---
name: "careful"
description: "Safety warnings for destructive commands. Invoke before rm -rf, DROP TABLE, force-push, git reset --hard, kubectl delete, or similar dangerous operations."
---

# Careful

Safety guardrails for destructive commands.

## When to Use

- Before `rm -rf`
- Before `DROP TABLE`
- Before force-push
- Before `git reset --hard`
- Before `kubectl delete`
- Any destructive operation

## Behavior

Warns about destructive operations and requires user confirmation before proceeding.
