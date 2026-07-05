# Product Spec — KAAM (formerly working title "Kindred")

*Open-minded dating app for Android. Draft v0.1 — 2026-07-05.*

> **Positioning rule (read first):** In all public-facing text — Play Store listing, app name,
> onboarding copy, notifications — this is a **dating app for open-minded people**, never a
> "hookup app." Google Play rejects apps whose stated primary purpose is facilitating sexual
> encounters, and bans sexually explicit content entirely. Desires are expressed through
> curated **interest tags**, not free-form explicit text. This framing is how Feeld, Pure,
> and 3Fun stay on the store. Every feature below is designed with this constraint in mind.

---

## 1. Product

### The gap
Mainstream dating apps (Tinder, Bumble, Hinge) are branded around relationships. Users who
want casual connections, specific arrangements, or non-traditional dynamics either lie about
intent (bad matches, wasted time) or leave. The product bet: **honest intent matching** —
let people state what they're actually looking for, match only with people looking for the
same thing.

### Target user
- 18+ (hard requirement, enforced), primarily 21–35
- Comfortable with casual/non-traditional dating, frustrated by intent-mismatch on mainstream apps
- Initial market: India (affects compliance, payments, and moderation norms — see §5)

### Core loop
1. User sets **intent** (what they're looking for) + **interest tags** from a curated list
2. Discovery feed shows nearby profiles with compatible intent
3. Mutual like → match → chat unlocks (no unsolicited messages, ever)
4. Meet safely (safety features in §4)

---

## 2. MVP feature set

Build in this order. Everything in "Later" is explicitly out of scope for v1.

### V1 (MVP)
| # | Feature | Notes |
|---|---------|-------|
| 1 | Phone/Google auth + 18+ age gate | Firebase Auth. DOB required; under-18 blocked at signup |
| 2 | Profile | Photos (moderated), bio, intent, interest tags, basic filters |
| 3 | Intent + interest tags | Curated list (e.g. "casual", "adventurous", "open relationship", "friends first"). No free-form explicit tags |
| 4 | Discovery | Card stack of nearby compatible profiles. Distance shown as fuzzy ranges ("~3 km"), never exact location |
| 5 | Matching | Mutual like → match. Chat only between matches |
| 6 | Chat | Text + moderated photo sharing (recipient must opt in to view images). Realtime via Firestore |
| 7 | Block & report | One tap from profile or chat. Report reasons feed the moderation queue |
| 8 | Photo moderation | Every uploaded photo passes automated NSFW check (Cloud Vision SafeSearch) before going live; flagged items go to a manual review queue |
| 9 | Account deletion | Full data wipe, required by Play policy and DPDP |

### Later (v2+)
- Couples/joint profiles, group chats, events
- Selfie-based photo verification badge
- Premium tier (see who liked you, unlimited likes, travel mode) — the monetization path
- Video chat, voice notes
- iOS

---

## 3. Screens (MVP)

1. **Onboarding** — phone/Google sign-in → DOB gate → guidelines consent ("respect, consent, no explicit content") → profile setup wizard (photos → intent → tags → bio)
2. **Discovery** — card stack, like/pass, filter sheet (distance, age, intent)
3. **Matches** — grid of matches + chat list
4. **Chat** — one conversation; report/block/unmatch in the overflow menu
5. **Profile (own)** — edit profile, settings, privacy controls, delete account
6. **Profile (other)** — full view from discovery or chat, report/block

---

## 4. Trust & safety (not optional — Play requires most of this for UGC apps)

- **Age gate:** DOB at signup, stored; obviously-underage photos are a moderation ban reason
- **Photo pipeline:** upload → Cloud Function → Vision SafeSearch → auto-reject explicit /
  auto-approve clean / queue borderline for manual review. Nothing is publicly visible unmoderated
- **Chat images:** blurred until recipient taps to reveal; "report this image" on every image
- **Report/block:** blocking is immediate and silent; reports create moderation tickets;
  3 upheld reports = suspension pending review
- **Location privacy:** store geohash truncated to ~1 km precision. Never store or show exact
  coordinates. Distance displayed in ranges
- **No screenshots in chat:** `FLAG_SECURE` on chat screens (deterrent, not guarantee)
- **Safety center:** static in-app page — meeting-safety tips, consent guidelines, local
  emergency numbers, grievance contact (IT Rules 2021 requires a grievance officer contact for India)

---

## 5. Compliance checklist

- [ ] Play Store: app category "Dating", content rating questionnaire answered honestly (Mature 17+)
- [ ] Play UGC policy: report/block/moderation in place before launch (§4)
- [ ] India DPDP Act: consent screen for data processing at signup; data deletion; privacy policy URL
- [ ] IT Rules 2021: grievance officer named in the app + policy page
- [ ] Privacy policy + terms of service hosted publicly (required for Play listing)
- [ ] No explicit content anywhere: listing screenshots, app icon, notifications, tag names

---

## 6. Architecture

**Client:** Kotlin, Jetpack Compose, single-activity, MVVM
- Modules: `app`, `core:ui`, `core:data`, `feature:auth`, `feature:profile`, `feature:discovery`, `feature:chat`
- Libraries: Hilt (DI), Coil (images), Navigation Compose, Firebase SDKs, DataStore (prefs)

**Backend:** Firebase (no servers to run — right call for a solo MVP)
- **Auth:** phone + Google sign-in
- **Firestore:** profiles, likes, matches, messages, reports (schema below)
- **Storage:** photos (private bucket; served via short-lived signed URLs)
- **Cloud Functions:** photo moderation pipeline, match creation on mutual like,
  push notification fan-out, account deletion cascade
- **FCM:** match + message notifications (generic text — "You have a new message", never content)

### Firestore data model (v1)

```
users/{uid}
  displayName, dob, gender, bio
  intent: string            // one of curated list
  tags: string[]            // curated interest tags
  photos: [{path, status: pending|approved|rejected}]
  geohash: string           // truncated ~1km
  lastActive, createdAt
  settings: {ageRange, maxDistanceKm, showMe}

likes/{fromUid_toUid}       // write-once; Cloud Function checks reverse doc → creates match

matches/{matchId}
  uids: [uidA, uidB]
  createdAt, lastMessageAt

matches/{matchId}/messages/{msgId}
  senderUid, type: text|image, text?, imagePath?, sentAt, revealed?: bool

reports/{reportId}
  reporterUid, reportedUid, matchId?, reason, detail, status: open|upheld|dismissed

blocks/{uid}/blocked/{blockedUid}
```

**Security rules principles:** users read/write only their own doc; profile reads exclude
`dob` precision and raw geohash; messages readable only by the two match participants;
`likes` are create-only (no reading who liked you — that's the premium feature later);
reports write-only for users, readable only by admin.

---

## 7. Build roadmap

| Phase | Deliverable | Scope |
|-------|------------|-------|
| 0 | Project scaffold | Compose app + module structure, Firebase project wired, CI-less local build working |
| 1 | Auth + profile | Sign-in, age gate, profile wizard, photo upload with moderation pipeline |
| 2 | Discovery + matching | Feed query (geohash + intent filter), like/pass, match Cloud Function |
| 3 | Chat | Realtime messages, image reveal flow, notifications |
| 4 | Safety + polish | Report/block, safety center, account deletion, `FLAG_SECURE` |
| 5 | Launch prep | Play listing, privacy policy, content rating, closed beta (Play internal testing track) |

Each phase ends with the app runnable on a device/emulator — no phase is "done" untested.

---

## 8. Open questions (decide before Phase 0)

1. **App name** — "Kindred" is a placeholder. Needs a Play Store + trademark check.
2. **Manual moderation** — who reviews the queue day-to-day? Solo-founder answer: you, via a
   simple admin web page (can be a Firebase-hosted internal tool in Phase 4).
3. **Firebase project + billing** — Blaze (pay-as-you-go) plan is required for Cloud Functions
   and Vision API. Needs a card on the Google Cloud account. Free tier covers early testing.
4. **Monetization timing** — v1 free; premium tier design deferred to v2.
