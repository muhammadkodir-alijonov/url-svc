# Claude AI Agent Guidelines for URL Shortener Service

## Workflow Orchestration

### 1. Plan Node Default
- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately - don't keep pushing
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy
- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop
- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done
- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)
- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes - don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing
- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

## Task Management

1. **Plan First**: Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan**: Check in before starting implementation
3. **Track Progress**: Mark items complete as you go
4. **Explain Changes**: High-level summary at each step
5. **Document Results**: Add review section to `tasks/todo.md`
6. **Capture Lessons**: Update `tasks/lessons.md` after corrections

## Core Principles

- **Simplicity First**: Make every change as simple as possible. Impact minimal code.
- **No Laziness**: Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact**: Changes should only touch what's necessary. Avoid introducing bugs.

## Project-Specific Rules

### File Creation Policy
- **DO NOT** create unnecessary documentation files (`.md`, `.ps1`, `.sh`, `.bat`)
- Only create files when explicitly requested by the user
- Focus on code fixes and functional changes
- Use chat responses to explain changes instead of creating guide files

### Vault Integration
- Vault is already set up via `setup-vault.sh`
- Secrets are stored in Vault at: `secret/url-shortener/{env}/{service}/config`
- Application should read from Vault, not from `application-dev.properties`
- Current issue: Application not reading from Vault despite setup being complete

### Current Task Context
- Fix Vault integration in Quarkus application
- Environment: Kubernetes (Docker Desktop)
- Services: PostgreSQL, Keycloak, Redis, Pulsar, Vault
- Error: `VaultClientException{operationName='VAULT [SECRETS (kv2)] Read Secret', requestPath='http://localhost:30200/v1/secret/data/secret/url-shortener/dev/database/postgres', status=404}`

## Working Directory
- Root: `C:\Users\muhammadqodir.a\Desktop\shortener-url\url-svc`
- Source: `src/main/java/com/example/`
- Config: `src/main/resources/`
- Infrastructure: `infrastructure/kubernetes/`
- Tasks: `tasks/` (to be created if needed)

