---
name: "ship"
description: "Automated deployment workflow. Invoke when user asks to ship, deploy, or merge code to production."
---

# Ship

Automated deployment and release workflow.

## When to Use

- User asks to "ship" code
- Ready to deploy to production
- Merging to main branch
- Creating a release

## Process

1. Detect and merge base branch
2. Run tests
3. Review diff
4. Bump version
5. Update changelog
6. Commit and push
7. Create PR
