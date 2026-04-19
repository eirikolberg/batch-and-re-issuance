## Oppgaver

### Bakgrunn: 
I denne appen ligger forenklede versjoner av token-endepunktet og credential-endepunktet i vår issuer.
Vi skal utvide disse med to nye, kule funksjoner:
- Re-issuance
- Batch issuance

### Oppgave 1: Re-issuance
Re-issuance er en funksjon som lar en holder be om å få re-utstedt et credential som allerede er utstedt til dem.
Gå til [TokenController](TokenController.kt) og gjennomfør oppgavene som står der.

### Oppgave 2: Batch issuance
Batch issuance er en funksjon som lar en issuer utstede mange credentials i én operasjon.
Gå til [CredentialController](CredentialController.kt) og gjennomfør oppgavene som står der.
