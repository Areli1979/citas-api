# Log de operaciones

| Fecha | OperaciÃ³n | Fuentes/pÃ¡ginas | Resultado |
|---|---|---|---|
| 2026-09-17 | INGEST inicial | SRC-PRD-001, SRC-TECH-001, SRC-DB-001, SRC-TRACE-001 | Estructura y pÃ¡ginas iniciales creadas tras aprobaciÃ³n del usuario |
| 2026-09-17 | DECISIÃ“N | HU-001 a HU-007; `contracts.md`, `decisions.md` | Contrato y corte backend de identidad aprobados; HU-033 conserva integraciÃ³n web |
| 2026-09-17 | LEARN/LINT | Flyway V1, pruebas MySQL 8.4; `data-integrity.md`, `traceability.md` | Corte 3FN y evidencia backend aÃ±adidos; RAW intacto, sin nuevos enlaces estructurales |
| 2026-09-22 | LEARN/DECISIÃ“N | `docs/FCV Dev/subagents/`, orquestador, `index.md`, `subagents.md`, arquitectura, decisiones, riesgos y trazabilidad | Ocho subagentes versionados; protocolo de delegaciÃ³n y nueva ubicaciÃ³n documental registrados; frontend React/Vite reconocido como trabajo pendiente de verificaciÃ³n |
| 2026-09-22 | LINT | CatÃ¡logo de subagentes y LLM Wiki | Enlaces Markdown relativos verificados; referencias operativas del orquestador actualizadas; se conserva como riesgo explÃ­cito la allowlist histÃ³rica de la Skill Scrum |
| 2026-09-22 | LEARN/DECISIÃ“N | HU-033, `contracts.md`, `traceability.md` | Corte React de autenticaciÃ³n integrado y validado contra API/MySQL; CORS explÃ­cito y compatibilidad Flyway con el esquema 3FN existente documentados; HU-033 queda en progreso. |
| 2026-09-22 | LINT | `data-integrity.md`, HU-005 a HU-007 | Evidencia actualizada a 9/9 pruebas Maven y modelo persistente `refresh_tokens`; integraciÃ³n web USER confirmada sin cerrar HU posteriores. |
| 2026-09-22 | DECISIÃ“N | HU-001, HU-002, HU-011, HU-014 a HU-024 y `contracts.md` | Cierre S2 confirmado para fundaciÃ³n y modelo 3FN; corte S3 aprobado con afiliaciÃ³n opcional, agenda real y contrato REST compartido. |
| 2026-09-24 | LEARN/LINT | S3, V1/V2, schedulingApi.ts, pruebas backend/frontend | Testcontainers validÃ³ las migraciones y reglas de agenda; el cliente adapta disponibilidad y bloques sin duplicar reglas de negocio. |
| 2026-09-23 | LINT/INTEGRACION | HU-015, HU-016, HU-017; `SchedulingServiceIntegrationTest`, `DashboardScreen.tsx`, `schedulingApi.ts` | Alta de profesionales, asignacion de especialidades/primaria, sedes y estado ADMIN integrados con REST/MySQL; prueba de rol, persistencia y bloqueo de agenda inactiva aprobada; frontend lint, 9 pruebas y build aprobados. |

