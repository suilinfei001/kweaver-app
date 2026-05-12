---
name: "review"
description: "Code review for catching bugs before merge. Invoke before merging, when user asks for review, or before shipping."
---

# Code Review

Pre-landing PR review to catch bugs that CI can't find.

## When to Use

- Before merging a PR
- Before shipping code
- User asks for "code review"
- Pre-deployment check

## Focus Areas

- SQL safety
- LLM trust boundary violations
- Conditional side effects
- Structural issues
- Security concerns
