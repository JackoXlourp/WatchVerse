import {initializeApp} from "firebase-admin/app";
import {getAuth} from "firebase-admin/auth";
import {getFirestore} from "firebase-admin/firestore";
import {setGlobalOptions} from "firebase-functions/v2";
import {HttpsError, onCall} from "firebase-functions/v2/https";

initializeApp();

setGlobalOptions({
  region: "northamerica-northeast1",
  maxInstances: 10,
});

interface MigrationPayload {
  legacyCloudKitRecordID: string;

  displayName: string;
  joinedDate: number;

  isFounder: boolean;

  showReleaseYears: boolean;
  notifyNewUniverses: boolean;

  selectedUniverseFilters: Record<string, string[]>;
  journeyPositions: Record<string, string>;

  watchedMovies: string[];
  skippedMovies: string[];

  unlockedBadges: string[];
  shownBadgePopups: string[];
}

/**
 * Validates and returns a string value.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {string} The validated string.
 */
function requireString(
  value: unknown,
  field: string
): string {
  if (typeof value !== "string") {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be a string.`
    );
  }

  return value;
}

/**
 * Validates and returns a boolean value.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {boolean} The validated boolean.
 */
function requireBoolean(
  value: unknown,
  field: string
): boolean {
  if (typeof value !== "boolean") {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be a boolean.`
    );
  }

  return value;
}

/**
 * Validates and returns a finite number.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {number} The validated number.
 */
function requireNumber(
  value: unknown,
  field: string
): number {
  if (
    typeof value !== "number" ||
    !Number.isFinite(value)
  ) {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be a valid number.`
    );
  }

  return value;
}

/**
 * Validates and returns an array of strings.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {string[]} The validated string array.
 */
function requireStringArray(
  value: unknown,
  field: string
): string[] {
  if (
    !Array.isArray(value) ||
    !value.every((item) => typeof item === "string")
  ) {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be an array of strings.`
    );
  }

  return value;
}

/**
 * Validates and returns a string map.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {Record<string, string>} The validated string map.
 */
function requireStringMap(
  value: unknown,
  field: string
): Record<string, string> {
  if (
    typeof value !== "object" ||
    value === null ||
    Array.isArray(value)
  ) {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be an object.`
    );
  }

  const result: Record<string, string> = {};

  for (const [key, item] of Object.entries(value)) {
    if (typeof item !== "string") {
      throw new HttpsError(
        "invalid-argument",
        `${field}.${key} must be a string.`
      );
    }

    result[key] = item;
  }

  return result;
}

/**
 * Validates and returns a map of string arrays.
 *
 * @param {unknown} value The value to validate.
 * @param {string} field The field name used in error messages.
 * @return {Record<string, string[]>} The validated string-array map.
 */
function requireStringArrayMap(
  value: unknown,
  field: string
): Record<string, string[]> {
  if (
    typeof value !== "object" ||
    value === null ||
    Array.isArray(value)
  ) {
    throw new HttpsError(
      "invalid-argument",
      `${field} must be an object.`
    );
  }

  const result: Record<string, string[]> = {};

  for (const [key, item] of Object.entries(value)) {
    result[key] = requireStringArray(
      item,
      `${field}.${key}`
    );
  }

  return result;
}

/**
 * Parses and validates a CloudKit migration payload.
 *
 * @param {unknown} rawData The raw callable function payload.
 * @return {MigrationPayload} The validated migration payload.
 */
function parseMigrationPayload(
  rawData: unknown
): MigrationPayload {
  if (
    typeof rawData !== "object" ||
    rawData === null ||
    Array.isArray(rawData)
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Migration data is invalid."
    );
  }

  const data = rawData as Record<string, unknown>;

  return {
    legacyCloudKitRecordID: requireString(
      data.legacyCloudKitRecordID,
      "legacyCloudKitRecordID"
    ),

    displayName: requireString(
      data.displayName,
      "displayName"
    ),

    joinedDate: requireNumber(
      data.joinedDate,
      "joinedDate"
    ),

    isFounder: requireBoolean(
      data.isFounder,
      "isFounder"
    ),

    showReleaseYears: requireBoolean(
      data.showReleaseYears,
      "showReleaseYears"
    ),

    notifyNewUniverses: requireBoolean(
      data.notifyNewUniverses,
      "notifyNewUniverses"
    ),

    selectedUniverseFilters: requireStringArrayMap(
      data.selectedUniverseFilters,
      "selectedUniverseFilters"
    ),

    journeyPositions: requireStringMap(
      data.journeyPositions,
      "journeyPositions"
    ),

    watchedMovies: requireStringArray(
      data.watchedMovies,
      "watchedMovies"
    ),

    skippedMovies: requireStringArray(
      data.skippedMovies,
      "skippedMovies"
    ),

    unlockedBadges: requireStringArray(
      data.unlockedBadges,
      "unlockedBadges"
    ),

    shownBadgePopups: requireStringArray(
      data.shownBadgePopups,
      "shownBadgePopups"
    ),
  };
}

export const migrateCloudKitUser = onCall(
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "You must be signed in."
      );
    }

    const payload = parseMigrationPayload(
      request.data
    );

    const firebaseUID = request.auth.uid;

    const authUser = await getAuth().getUser(
      firebaseUID
    );

    const appleProvider = authUser.providerData.find(
      (provider) => provider.providerId === "apple.com"
    );

    if (!appleProvider) {
      throw new HttpsError(
        "failed-precondition",
        "This Firebase account is not linked to Apple."
      );
    }

    if (
      appleProvider.uid !==
      payload.legacyCloudKitRecordID
    ) {
      throw new HttpsError(
        "permission-denied",
        "The Apple identity does not match the legacy account."
      );
    }

    /*
     * Founder is privileged.
     *
     * A modified client could lie about the CloudKit isFounder
     * value, so this callable does not grant Founder status.
     *
     * Existing Founder users need a separate trusted migration
     * path before this function is used for them.
     */

    const db = getFirestore();

    const userReference = db
      .collection("users")
      .doc(firebaseUID);

    await db.runTransaction(
      async (transaction) => {
        const existingDocument =
          await transaction.get(userReference);

        if (existingDocument.exists) {
          throw new HttpsError(
            "already-exists",
            "A Firestore profile already exists for this account."
          );
        }

        transaction.set(
          userReference,
          {
            uid: firebaseUID,

            displayName: payload.displayName,

            email: authUser.email ?? "",

            joinedDate: payload.joinedDate,

            isFounder: payload.isFounder,

            showReleaseYears:
              payload.showReleaseYears,

            notifyNewUniverses:
              payload.notifyNewUniverses,

            selectedUniverseFilters:
              payload.selectedUniverseFilters,

            journeyPositions:
              payload.journeyPositions,

            watchedMovies:
              payload.watchedMovies,

            skippedMovies:
              payload.skippedMovies,

            unlockedBadges:
              payload.unlockedBadges,

            shownBadgePopups:
              payload.shownBadgePopups,

            schemaVersion: 2,

            cloudKitMigrationVersion: 1,

            legacyCloudKitRecordID:
              payload.legacyCloudKitRecordID,
          }
        );
      }
    );

    return {
      success: true,
      schemaVersion: 2,
      cloudKitMigrationVersion: 1,
    };
  }
);
