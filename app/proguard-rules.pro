# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.shockwave.**

# AdMob Mediation Liftoff (Vungle)
-keep class com.vungle.ads.** { *; }
-keep class com.google.ads.mediation.vungle.** { *; }

# AdMob Mediation Unity
-keep class com.unity3d.ads.** { *; }
-keep class com.google.ads.mediation.unity.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# WorkManager
-keep class androidx.work.impl.** { *; }
-keep class androidx.sqlite.db.framework.** { *; }

# General
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# R8 missing classes rules
-dontwarn com.unity3d.ads.AdFormat
-dontwarn com.unity3d.ads.IUnityAdsInitializationListener
-dontwarn com.unity3d.ads.IUnityAdsLoadListener
-dontwarn com.unity3d.ads.IUnityAdsShowListener
-dontwarn com.unity3d.ads.IUnityAdsTokenListener
-dontwarn com.unity3d.ads.TokenConfiguration
-dontwarn com.unity3d.ads.UnityAds$UnityAdsInitializationError
-dontwarn com.unity3d.ads.UnityAds$UnityAdsLoadError
-dontwarn com.unity3d.ads.UnityAds$UnityAdsShowCompletionState
-dontwarn com.unity3d.ads.UnityAds$UnityAdsShowError
-dontwarn com.unity3d.ads.UnityAds
-dontwarn com.unity3d.ads.UnityAdsLoadOptions
-dontwarn com.unity3d.ads.UnityAdsShowOptions
-dontwarn com.unity3d.ads.metadata.MediationMetaData
-dontwarn com.unity3d.ads.metadata.MetaData
-dontwarn com.unity3d.services.banners.BannerErrorCode
-dontwarn com.unity3d.services.banners.BannerErrorInfo
-dontwarn com.unity3d.services.banners.BannerView$IListener
-dontwarn com.unity3d.services.banners.BannerView
-dontwarn com.unity3d.services.banners.UnityBannerSize
