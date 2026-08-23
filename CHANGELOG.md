# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.6] - 2026-08-23
### Added
- **Multi-Network Ad Competition**: Implemented randomized load requests between AdMob and Liftoff (Vungle) for Native, MREC, and App Open formats to optimize eCPM.
- **Enhanced Ad Waterfall**: Robust fallback chains implemented across AdMob, Liftoff, and Unity Ads (Native -> MREC -> Standard Banner).
- **Enterprise-Level Documentation**: Updated README and added project CHANGELOG.

### Fixed
- **MediaCodec NO_MEMORY Issue**: Resolved `android.media.MediaCodec$CodecException` by optimizing ad initialization and resource cleanup.
- **Resource Management**: Implemented explicit `destroy()` calls for Native Ads and proper lifecycle handling for WebViews.
- **Memory Pressure**: Limited concurrent ad loads and added muted video options to prevent hardware decoder exhaustion.

### Optimized
- **Ad Frequency**: Adjusted ViewPager2 ad triggers to occur every 5 pages instead of every page.
- **PDF Rendering Stability**: Improved resource releasing in `PdfAdapter`.

## [2.0.5] - 2026-07-15
### Changed
- Optimized Ad Layout: Switched native ads from rectangle to compact banner style.
- Ad Stability: Implemented smart fallback mechanisms.
- Improved Experience: All rewarded ads now function as standard interstitials.

### Added
- Collapsible Banner support in main activities.

## [2.0.4] - 2026-06-20
### Added
- ViewPager2 for chapter navigation.
- Native PDF rendering with vertical scroll.
- AdMob mediation with Facebook and Unity.
- 16KB page size compatibility.
