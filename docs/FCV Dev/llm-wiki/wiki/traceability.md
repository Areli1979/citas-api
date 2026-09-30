# Trazabilidad

## HECHO

Las sesiones S2-S6 requieren commits y evidencias especÃ­ficas. El backend y frontend deben mantener historial trazable; las pruebas y la evidencia cross-repo son parte de la evaluaciÃ³n.

## HECHO â€” 2026-09-17

Las HU-001 a HU-036 existen en `docs/FCV Dev/scrum/`. HU-001/002/003 tienen avance parcial; HU-004 define por ahora solo el contrato de identidad. La implementaciÃ³n backend de HU-005/006/007 cuenta con `AuthIntegrationTest`, `IdentityTest` y `AuthRequestGuardTest`; la integraciÃ³n web se controla mediante HU-033.

## HECHO â€” 2026-09-22

El catÃ¡logo de ocho subagentes fue versionado en `docs/FCV Dev/subagents/` y enlazado desde el orquestador. `citas-web` contiene trabajo local React/Vite de autenticaciÃ³n y pruebas; no debe declararse completado hasta ejecutar build, typecheck, tests y verificaciÃ³n cross-repo.

## HECHO â€” 2026-09-22 Â· IntegraciÃ³n de autenticaciÃ³n

El prototipo `citas-web/portal-de-citas.zip` se importÃ³ como React/Vite y se integrÃ³ con HU-005/006/007. La comprobaciÃ³n usa MySQL persistente, CORS explÃ­cito, registro/login/refresh/logout reales y pruebas de frontend. HU-033 permanece en progreso porque las pantallas de perfil, agenda y roles posteriores siguen fuera del corte de autenticaciÃ³n.

## HECHO - 2026-09-23 · Integracion de oferta profesional y agenda

HU-015, HU-016 y HU-017 cuentan con contrato REST backend y pantalla ADMIN React para crear profesionales, asignar especialidades con primaria, asignar una o dos sedes y activar/desactivar. `SchedulingServiceIntegrationTest` verifica rol PROFESSIONAL, persistencia 3FN, relaciones N:M y rechazo de bloques para profesionales inactivos. La suite frontend ejecuta typecheck, 9 pruebas Vitest y build en Docker. La agenda completa continua dependiendo de HU-018 a HU-024.


