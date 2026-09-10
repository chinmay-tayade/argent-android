# Security

## Threat model (state it in two sentences)

The attacker has physical possession of the device, possibly unlocked, and
can observe network traffic on a hostile Wi-Fi. Argent's job is to ensure
that (a) an unlocked stolen device cannot be used to move money without the
user's biometric/PIN, and (b) a network attacker cannot read or forge API
traffic.

Out of scope for v0.1: a rooted device with a determined attacker and
unlimited time (documented below), OS-level zero-days, and supply-chain
attacks on dependencies (mitigated only by Dependabot + CodeQL).

## Key storage — Android Keystore

- The app's data-encryption key and session key are generated in the
  Keystore and are **non-exportable** — key material never enters app memory.
- `setUserAuthenticationRequired(true)` on the session key, with a validity
  window, so using it requires a recent biometric/PIN auth.
- `setInvalidatedByBiometricEnrollment(true)` — enrolling a new fingerprint
  invalidates the key. This is deliberate: it means an attacker who adds
  their own biometric can't then use the existing key.

## Data at rest

- Secrets (refresh token) → Keystore-wrapped via Jetpack Security /
  Tink `Aead`. No hand-rolled crypto.
- The Room database itself → SQLCipher is wired but off by default in v0.1
  (documented cost/benefit: it protects against a rooted-device file dump,
  at a startup + query cost; the sensitive fields are already minimal).
- The sync operation queue holds amounts + beneficiary ids → encrypted at
  rest (Milestone 3).

## Authentication

- OAuth2 / OIDC against the mock IdP. Access token in memory only. Refresh
  token encrypted at rest.
- On cold start / resume after the session validity window: re-auth required
  before any authenticated call.
- Token refresh is single-flight — concurrent 401s trigger one refresh, others
  await it.

## Biometrics — `BiometricPrompt` + `CryptoObject`

The important detail: biometric auth **unlocks a cryptographic key**, it does
not set a boolean.

```
BiometricPrompt.authenticate(promptInfo, CryptoObject(cipher))
  → onAuthenticationSucceeded(result)
      → result.cryptoObject.cipher   // now usable
      → decrypt the session key material with it
```

If the flow only checked "did `onAuthenticationSucceeded` fire", an attacker
who can hook the callback (on a compromised device) bypasses it. Binding the
prompt to a `Cipher` initialised with a Keystore key means a bypass yields
nothing usable.

Fallback: device PIN via `setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)`.

## Transport

- OkHttp `CertificatePinner` pins the API's leaf + a backup pin.
  - **Operational risk, stated:** a mispublished pin bricks the app until an
    update ships. Mitigation: pin the intermediate CA as backup, keep the pin
    set in remote config (not hardcoded) once there's a backend for it, and
    document the rotation runbook.
- `cleartextTrafficPermitted = false`. TLS 1.2 minimum.
- No custom `TrustManager`.

## Session & leakage

- Inactivity timeout → wipe in-memory state, require re-auth.
- `onStop` → clear sensitive state holders, so a memory dump of a
  backgrounded app yields less.
- `FLAG_SECURE` on auth, transfer, and account-detail screens → no
  screenshots, no thumbnail in the app switcher.
- Logging: a `Redacted` wrapper type for money amounts and account numbers;
  the logging interceptor redacts known-sensitive headers and body fields.
  No PII in analytics or crash payloads — enforced by a lint rule
  (Milestone 3).

## Root / integrity detection

Argent **does not hard-fail on a rooted device.** Reasoning:

- Root detection is a cat-and-mouse game; a determined attacker defeats it.
- Hard-failing punishes legitimate power users and security researchers.
- The real mitigations are the ones above (non-exportable keys, crypto-bound
  biometrics, pinning) — they degrade gracefully rather than relying on a
  binary "is this device safe" check.

What it *does*: on detected root, it surfaces a non-blocking warning and
raises the risk signal attached to high-value operations (lower transfer
limit, always require step-up auth). Play Integrity API is noted as the
production-grade signal to add when there's a backend to verify the token
server-side — client-side alone is worthless.

## Résumé gate

Before "implemented biometric-gated secure storage" goes on a CV, be able to
explain, unprompted: why a `CryptoObject`-bound key beats a boolean; what
happens to the key on new biometric enrollment; single-flight token refresh;
and the certificate-pinning operational trade-off.
