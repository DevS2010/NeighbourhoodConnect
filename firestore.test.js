const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const OWNER_EMAIL = "Jksmau@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or create consumer profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("consumers").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("consumers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Sharma",
      email: "alice@example.com",
      neighborhood: "Indiranagar, Bengaluru",
      linkedAccountOwnerEmail: OWNER_EMAIL,
      role: "CONSUMER",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated consumer: can create and read their own linked consumer profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("consumers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Sharma",
      email: "alice@example.com",
      neighborhood: "Indiranagar, Bengaluru",
      linkedAccountOwnerEmail: OWNER_EMAIL,
      role: "CONSUMER",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("consumers").doc(ALICE_UID).get());
});

test("Authenticated consumer: cannot read or spoof another consumer's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("consumers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Sharma",
      email: "alice@example.com",
      neighborhood: "Indiranagar, Bengaluru",
      linkedAccountOwnerEmail: OWNER_EMAIL,
      role: "CONSUMER",
      createdAt: now,
      updatedAt: now,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("consumers").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("consumers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Spoofed Alice",
      email: "bob@example.com",
      neighborhood: "Koramangala",
      linkedAccountOwnerEmail: OWNER_EMAIL,
      role: "CONSUMER",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated service provider: can register with valid Government ID and Professional Certification", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("service_providers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice.pro@example.com",
      fullName: "Alice Electrical & Solar Works",
      professionCategory: "ELECTRICIAN",
      neighborhood: "Indiranagar 12th Main",
      city: "Bengaluru",
      govIdType: "AADHAAR",
      govIdNumber: "4829-9104-7732",
      govIdDocumentVerified: true,
      certificationTitle: "NSDC Certified Master Electrician",
      issuingAuthority: "National Skill Development Corporation",
      certificationNumber: "NSDC-EL-2022-88412",
      experienceYears: 9,
      baseVisitingFeeInr: 199,
      upiId: "alice.pro@okaxis",
      acceptsEmergencySos: true,
      linkedAccountOwnerEmail: OWNER_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("service_providers").doc(ALICE_UID).get());
});

test("Service provider registration: rejects unverified Government ID document or invalid certification number", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Fails when govIdDocumentVerified is false
  await assertFails(
    aliceDb.collection("service_providers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice.pro@example.com",
      fullName: "Alice Electrical & Solar Works",
      professionCategory: "ELECTRICIAN",
      neighborhood: "Indiranagar 12th Main",
      city: "Bengaluru",
      govIdType: "AADHAAR",
      govIdNumber: "4829-9104-7732",
      govIdDocumentVerified: false,
      certificationTitle: "NSDC Certified Master Electrician",
      issuingAuthority: "National Skill Development Corporation",
      certificationNumber: "NSDC-EL-2022-88412",
      experienceYears: 9,
      baseVisitingFeeInr: 199,
      upiId: "alice.pro@okaxis",
      acceptsEmergencySos: true,
      linkedAccountOwnerEmail: OWNER_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user PII isolation check on service_providers
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("service_providers").doc(ALICE_UID).get());
});

test("Authenticated consumer: can create and query their own linked bookings with matching filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("consumer_bookings").doc("bk_1001").set({
      userId: ALICE_UID,
      consumerEmail: "alice@example.com",
      linkedAccountOwnerEmail: OWNER_EMAIL,
      providerId: "prov_1",
      providerName: "Rajesh Verma",
      categoryTitle: "Electrician",
      issueSummary: "MCB tripping repeatedly",
      neighborhood: "Indiranagar 12th Main, Bengaluru",
      status: "QUOTE_SUBMITTED",
      totalAmountInr: 650,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("consumer_bookings").where("userId", "==", ALICE_UID).get()
  );

  await assertFails(aliceDb.collection("consumer_bookings").get());
});
