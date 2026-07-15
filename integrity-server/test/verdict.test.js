import test from 'node:test';
import assert from 'node:assert/strict';
import {
  evaluateVerdict,
  requestHash,
  validateRequestBinding
} from '../src/verdict.js';

const packageName = 'com.resistine.android';
const now = 1_750_000_000_000;

function trustedPayload(hash = requestHash(packageName, 'action-1', now)) {
  return {
    requestDetails: {
      requestPackageName: packageName,
      requestHash: hash,
      timestampMillis: String(now)
    },
    accountDetails: { appLicensingVerdict: 'LICENSED' },
    appIntegrity: {
      appRecognitionVerdict: 'PLAY_RECOGNIZED',
      packageName,
      certificateSha256Digest: ['release-cert']
    },
    deviceIntegrity: { deviceRecognitionVerdict: ['MEETS_DEVICE_INTEGRITY'] },
    environmentDetails: {
      playProtectVerdict: 'NO_ISSUES',
      appAccessRiskVerdict: { appsDetected: ['KNOWN_INSTALLED'] }
    }
  };
}

test('validates package, freshness, and action request hash', () => {
  const hash = requestHash(packageName, 'action-1', now);
  const result = validateRequestBinding({
    body: { packageName, actionId: 'action-1', timestampMillis: now, requestHash: hash },
    payload: trustedPayload(hash),
    expectedPackageName: packageName,
    now
  });
  assert.equal(result.expectedHash, hash);
});

test('rejects a replay binding with a mismatched action hash', () => {
  assert.throws(() => validateRequestBinding({
    body: {
      packageName,
      actionId: 'different-action',
      timestampMillis: now,
      requestHash: requestHash(packageName, 'different-action', now)
    },
    payload: trustedPayload(),
    expectedPackageName: packageName,
    now
  }), /hash mismatch/);
});

test('returns trusted only when all required verdicts pass', () => {
  const result = evaluateVerdict(trustedPayload(), {
    expectedPackageName: packageName,
    expectedCertificateSha256: 'release-cert'
  });
  assert.equal(result.decision, 'TRUSTED');
  assert.equal(result.remediationDialogType, undefined);
});

test('selects unknown app-access remediation for capturing risk', () => {
  const payload = trustedPayload();
  payload.environmentDetails.appAccessRiskVerdict.appsDetected.push('UNKNOWN_CAPTURING');
  const result = evaluateVerdict(payload, { expectedPackageName: packageName });
  assert.equal(result.decision, 'REVIEW');
  assert.equal(result.remediationDialogType, 2);
});

test('selects Play remediation for harmful app verdicts', () => {
  const payload = trustedPayload();
  payload.environmentDetails.playProtectVerdict = 'HIGH_RISK';
  const result = evaluateVerdict(payload, { expectedPackageName: packageName });
  assert.equal(result.decision, 'REVIEW');
  assert.equal(result.remediationDialogType, 5);
});
