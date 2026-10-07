# Agent Registry

## 1. Purpose

Agent Registry is responsible for storing and managing all AI Agent definitions used by the system.

Agent definitions are stored in PostgreSQL instead of being hard-coded inside the application.

This allows the system to:

- Add new Agents without redesigning the application.
- Version Agent behavior.
- Change Agent responsibilities over time.
- Keep historical Agent Runs reproducible.
- Load only the Agent required by the current workflow step.
- Prevent responsibilities from different roles from being mixed together.

---

# 2. Core Concept

An Agent is separated into:

```text
Agent
│
├── Identity
│
└── Versions
     │
     ├── System Instruction
     ├── Responsibilities
     ├── Contracts
     └── Model Configuration
```

Example:

```text
BA
│
├── v1
│   ├── System Instruction
│   ├── Responsibilities
│   └── Contracts
│
├── v2
│
└── v3
```

Agent identity and Agent behavior are intentionally separated.

The `BA` Agent remains the same logical role while its behavior can evolve through different versions.

---

# 3. Database Structure

The initial Agent Registry contains four tables:

```text
agents
   │
   │ 1:N
   ▼
agent_versions
   │
   ├───────────────┐
   │ 1:N           │ 1:N
   ▼               ▼
agent_          agent_
responsibilities contracts
```

Tables:

```text
agents
agent_versions
agent_responsibilities
agent_contracts
```

---

# 4. agents

The `agents` table stores the identity of an Agent.

It answers:

> Who is this Agent?

Examples:

```text
BA
TECH_LEAD
BACKEND
FRONTEND
QA
```

Main fields:

```text
id
code
name
description
enabled
created_at
updated_at
```

Example:

```text
code:
BA

name:
Business Analyst
```

The table must NOT contain all Agent behavior or responsibilities.

Those belong to a specific Agent Version.

---

# 5. agent_versions

The `agent_versions` table stores different versions of an Agent.

Relationship:

```text
Agent
  │
  ├── Version 1
  ├── Version 2
  └── Version 3
```

Main fields:

```text
id
agent_id
version
system_instruction
status

model_provider
model_name
temperature
max_tokens

created_at
```

Possible status:

```text
DRAFT
ACTIVE
INACTIVE
```

Example:

```text
BA
│
├── v1 → INACTIVE
├── v2 → ACTIVE
└── v3 → DRAFT
```

Versioning exists so changing Agent behavior does not destroy historical behavior.

An Agent Run should eventually reference the exact Agent Version that executed it.

---

# 6. Why Agent Versioning Is Required

Suppose BA v1 contains:

```text
Detect missing requirements.
Ask clarification questions.
```

Later BA v2 becomes more advanced:

```text
Detect missing requirements.
Detect business edge cases.
Detect conflicts with existing decisions.
Generate structured clarification questions.
```

Existing executions must still be traceable to:

```text
BA v1
```

while new executions may use:

```text
BA v2
```

Therefore Agent definitions must not simply be overwritten.

---

# 7. agent_responsibilities

The `agent_responsibilities` table describes what an Agent Version:

```text
MUST_DO

CAN_DO

MUST_NOT_DO
```

Each responsibility is stored separately instead of storing every responsibility inside one large prompt.

Relationship:

```text
Agent Version
     │
     ├── Responsibility
     ├── Responsibility
     ├── Responsibility
     └── Responsibility
```

Main fields:

```text
id
agent_version_id
responsibility_type
content
priority
enabled
created_at
```

Supported types:

```text
MUST_DO
CAN_DO
MUST_NOT_DO
```

---

# 8. Responsibility Types

## MUST_DO

Mandatory Agent behavior.

Example:

```text
Detect missing information.

Generate clarification questions.

Present the final requirement for human confirmation.
```

---

## CAN_DO

Behavior the Agent is allowed to perform but is not always required.

Example:

```text
Suggest possible business options.

Explain why clarification is required.
```

---

## MUST_NOT_DO

Explicit Agent boundaries.

Example:

```text
Never invent missing business information.

Never convert assumptions into project facts.

Never modify confirmed decisions without human approval.
```

`MUST_NOT_DO` is especially important for preventing responsibility leakage between Agents.

For example:

```text
BA
↓
must not make Backend implementation decisions
```

---

# 9. agent_contracts

Responsibilities describe:

> What should the Agent do?

Contracts describe:

> How must the Agent interact with the system?

The initial contract types are:

```text
INPUT

OUTPUT

CONSTRAINT

COMPLETION
```

Main fields:

```text
id
agent_version_id
contract_type
name
content
enabled
created_at
updated_at
```

`content` uses PostgreSQL `JSONB`.

This allows contracts to have structured definitions instead of unstructured text.

---

# 10. INPUT Contract

Defines what information an Agent expects to receive.

Example:

```json
{
  "required": [
    "project_id",
    "specification"
  ],
  "optional": [
    "relevant_confirmed_decisions",
    "related_artifacts"
  ]
}
```

The Context Builder will eventually use this information when preparing an Agent execution.

---

# 11. OUTPUT Contract

Defines the structure an Agent must return.

Agents should not return only uncontrolled natural-language text.

Example:

```json
{
  "status": "NEEDS_CLARIFICATION",
  "unknowns": [],
  "ambiguities": [],
  "conflicts": [],
  "clarificationQuestions": []
}
```

Structured output allows the backend to safely determine what happens next.

Example:

```text
BA Response
     │
     ▼
status
     │
     ├── NEEDS_CLARIFICATION
     │
     ├── CONFLICT
     │
     └── READY_FOR_CONFIRMATION
```

---

# 12. CONSTRAINT Contract

Defines behavioral restrictions that must be respected during execution.

Example:

```text
Missing information
       ↓
MUST NOT GUESS
       ↓
Ask Human
```

Constraints supplement the Agent's `MUST_NOT_DO` responsibilities and may contain structured execution rules.

---

# 13. COMPLETION Contract

Defines when an Agent is considered finished.

Example for BA:

```text
No blocking unknown
        +
No blocking ambiguity
        +
No unresolved conflict
        +
Human confirmation
        =
BA COMPLETE
```

Completion must not be decided solely because the LLM says:

```text
"I'm done."
```

The application will eventually enforce completion conditions.

---

# 14. Current BA Agent

The first registered Agent is:

```text
Code:
BA

Name:
Business Analyst

Version:
1

Status:
DRAFT
```

BA v1 currently contains:

```text
18 MUST_DO responsibilities

4 CAN_DO responsibilities

10 MUST_NOT_DO responsibilities
```

Total:

```text
32 responsibilities
```

BA v1 intentionally remains:

```text
DRAFT
```

because its contracts have not yet been finalized.

---

# 15. BA Agent Core Rule

BA acts as the Requirement Gate.

Its fundamental rule is:

```text
UNKNOWN
   ↓
ASK

AMBIGUOUS
   ↓
ASK

CONFLICT
   ↓
ASK

CONFIRMED
   ↓
CONTINUE
```

BA must never silently transform:

```text
AI ASSUMPTION
```

into:

```text
PROJECT FACT
```

---

# 16. Human Confirmation Authority

An Agent cannot confirm its own output.

For example, the LLM may return:

```text
READY_FOR_CONFIRMATION
```

but it cannot independently change the requirement to:

```text
APPROVED
```

The expected flow is:

```text
BA Agent
   │
   ▼
READY_FOR_CONFIRMATION
   │
   ▼
Human
   │
   ├── Reject / Change
   │
   └── Confirm
           │
           ▼
      Application
           │
           ▼
       APPROVED
```

Human confirmation is therefore an application-level authority, not an LLM-level authority.

---

# 17. Runtime Loading

Agents are loaded dynamically.

Example:

```text
Orchestrator
     │
     │ requires BA
     ▼
Agent Registry
     │
     ▼
agents
     │
     ▼
agent_versions
     │
     ├── system_instruction
     │
     ├── responsibilities
     │
     └── contracts
             │
             ▼
       Context Builder
             │
             ▼
         BA Runtime
```

Only the Agent required for the current workflow step should be loaded.

---

# 18. Important Design Decision

Agent behavior is stored in the database rather than being fully hard-coded in Java.

Spring Boot is responsible for:

```text
Loading Agent definitions

Validating Agent configuration

Building runtime context

Enforcing workflow rules

Enforcing approval rules

Persisting execution state
```

The LLM is responsible for:

```text
Reasoning

Analysis

Generating questions

Generating suggestions

Producing structured Agent output
```

The LLM must not be responsible for enforcing critical system state.

---

# 19. Current Implementation Status

Completed:

```text
[x] PostgreSQL database created

[x] Agent Registry database structure

[x] agents table

[x] agent_versions table

[x] agent_responsibilities table

[x] agent_contracts table

[x] BA Agent created

[x] BA Agent Version 1 created

[x] BA v1 system instruction created

[x] BA v1 responsibilities created
```

Pending:

```text
[ ] BA INPUT contract

[ ] BA OUTPUT contract

[ ] BA CONSTRAINT contract

[ ] BA COMPLETION contract

[ ] Review BA v1

[ ] Human approve BA v1

[ ] Change BA v1 from DRAFT → ACTIVE

[ ] Spring Boot Agent Registry implementation
```

---

# 20. Next Step

The next implementation step is:

```text
Define BA Agent Contracts
          │
          ├── INPUT
          ├── OUTPUT
          ├── CONSTRAINT
          └── COMPLETION
          │
          ▼
Review
          │
          ▼
Human Confirm
          │
          ▼
BA v1 = ACTIVE
```

After BA v1 is finalized, the Spring Boot backend can implement the Agent Registry and dynamically load BA v1 from PostgreSQL.