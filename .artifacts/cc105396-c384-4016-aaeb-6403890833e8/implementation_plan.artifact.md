# Ad Waterfall and Consent Implementation Plan

This plan outlines the implementation of a multi-network ad waterfall (AdMob -> Facebook -> Unity) and a format-based fallback system, along with consent-aware loading for rewarded and interstitial ads.

## Proposed Changes

### [Ad Management]

#### [MODIFY] [AdManagerHelper.java](file:///D:/AndroidStudioProjects/Lammummaa-Kutaa-8ffaa/app/src/main/java/com/beckytech/lammummaakutaa8ffaa/service/AdManagerHelper.java)
- Update `initialize` to include Facebook and Unity SDK initialization.
- Implement a recursive waterfall loading mechanism for:
    - **Interstitials/Rewarded**: AdMob -> Facebook -> Unity.
    - **Banners/Native**: Implement the sequence Native -> MREC -> Standard Banner across the networks.
- Implement the requested consent-based loading logic:
    - **Consent**: Rewarded -> Rewarded Interstitial -> Interstitial.
    - **No Consent**: Rewarded Interstitial -> Interstitial.
- Refactor existing methods to use these new waterfall patterns.

#### [MODIFY] [strings.xml](file:///D:/AndroidStudioProjects/Lammummaa-Kutaa-8ffaa/app/src/main/res/values/strings.xml)
- Add placeholder ad unit IDs for Facebook and Unity Ads.

#### [MODIFY] [MyApplication.java](file:///D:/AndroidStudioProjects/Lammummaa-Kutaa-8ffaa/app/src/main/java/com/beckytech/lammummaakutaa8ffaa/MyApplication.java) (if it exists)
- Ensure basic SDK init happens if needed, though `AdManagerHelper` seems to handle it.

## Verification Plan

### Automated Tests
- I'll add log statements to verify the waterfall sequence when an ad fails to load from a higher-priority network.

### Manual Verification
- Deploy to an emulator/device.
- Observe ad loading logs in Logcat.
- Verify that consent forms are still showing as expected.
