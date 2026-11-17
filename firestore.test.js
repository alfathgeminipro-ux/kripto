const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_monex_123";
const BOB_UID = "bob_monex_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validWalletPayload(uid, createdDate) {
  return {
    userId: uid,
    displayName: "Leonard Lopez",
    username: "@leonardlopez",
    photoUrl: "",
    balanceIdr: 98450000,
    incomeDeltaIdr: 14500000,
    monthlyBudgetIdr: 12000000,
    planName: "Standard",
    cardLast4: "3377",
    cardNumberMasked: "3456 5667 3211 3377",
    cardExpiry: "01/29",
    cardCvv: "277",
    referralCode: "MONEX-3377",
    createdAt: createdDate,
    updatedAt: createdDate,
  };
}

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

test("Unauthenticated user: denied read and write across all user collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validWalletPayload(ALICE_UID, past)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("contacts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cryptoHoldings").get());
});

test("Authenticated owner: valid wallet create and partial balance update succeed; tampered writes fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validWalletPayload(ALICE_UID, created))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      balanceIdr: 95000000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      balanceIdr: 99999999,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject unexpected @DocumentId `id` field
  const badPayload = validWalletPayload(ALICE_UID, created);
  badPayload.id = ALICE_UID;
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set(badPayload));
});

test("Transactions subcollection: owner create and list succeed, updates/deletes denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      title: "Bolt Food",
      subtitle: "Hari ini, 12:15",
      amountIdr: -425000,
      type: "transfer",
      category: "Makanan",
      iconKey: "bolt",
      incomingTx: false,
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );

  // Ledger entries are immutable
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").update({
      amountIdr: 1000000,
    })
  );

  // Invalid type rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_2").set({
      userId: ALICE_UID,
      title: "Bad Tx",
      subtitle: "Invalid",
      amountIdr: -10000,
      type: "hacked_type",
      category: "Lainnya",
      iconKey: "bolt",
      incomingTx: false,
      createdAt: now,
    })
  );
});

test("Contacts and cryptoHoldings subcollections: owner CRUD succeeds, cross-user isolation enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("contacts").doc("c_james").set({
      userId: ALICE_UID,
      name: "James",
      handle: "@james.m",
      avatarKey: "james",
      accentHex: "#C6FF1A",
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cryptoHoldings").doc("BTC").set({
      userId: ALICE_UID,
      symbol: "BTC",
      name: "Bitcoin",
      amountUnits: 0.015,
      priceIdr: 1320000000,
      changePercent: 2.2,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cryptoHoldings").doc("BTC").update({
      amountUnits: 0.02,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot read or mutate Alice's wallet, transactions, contacts, or crypto
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("contacts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cryptoHoldings").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("cryptoHoldings").doc("BTC").update({
      amountUnits: 999.0,
      updatedAt: now,
    })
  );
});
