# Resistine Play Integrity verifier

This service is the required trust boundary for the Android Play Integrity integration. The app never interprets an opaque Integrity token locally. The verifier authenticates to Google, decodes the token, validates the package, request hash, timestamps, optional release certificate, and one-time action identifier, then returns only sanitized verdict labels.

## Configuration

Set these environment variables:

- `EXPECTED_PACKAGE_NAME` defaults to `com.resistine.android`.
- `EXPECTED_CERTIFICATE_SHA256` should be the base64url SHA-256 certificate digest from the decoded Play Integrity payload for the production Play signing certificate. Leaving it unset skips the additional certificate pin and is not recommended for production.
- `GOOGLE_APPLICATION_CREDENTIALS` can point to a service-account credential for local development. In Cloud Run or another Google runtime, prefer Application Default Credentials through the workload identity.
- `PORT` defaults to `8080`.

Enable Play Integrity API in the same Cloud project that is linked to the app in Play Console. Enable the Play Protect and app-access-risk optional verdicts in Play Console. Grant the verifier identity permission to call the Play Integrity decode API.

```bash
npm install
npm test
npm start
```

The Android endpoint is `https://your-verifier.example/v1/integrity/verify`; `/healthz` is available for platform health checks. Terminate TLS at the hosting platform or reverse proxy. Do not expose this service over cleartext HTTP and do not log request bodies because they contain opaque integrity tokens.

## Deployment behavior

The verifier accepts at most 64 KiB, never caches responses, rejects tokens and client actions older than two minutes, rate-limits decode attempts per source address, and keeps a short in-memory replay set. For a multi-instance deployment, replace replay and rate-limit state with an atomic shared store such as Redis or a database uniqueness constraint keyed by the action ID, and enforce an additional quota at the load balancer.

The remediation response uses the official dialog type codes: licensing, unknown/all app-access risk, base integrity, or strong integrity/Play Protect remediation. A production service may tighten or relax the `TRUSTED` decision policy while continuing to return the raw sanitized labels.
