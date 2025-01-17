-keepclassmembers class * extends android.webkit.WebView {
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
   public void loadUrl(java.lang.String);
   public void evaluateJavascript(java.lang.String, android.webkit.ValueCallback);
}

-keep public class * {
    public protected *;
}


-keepattributes *Annotation*

-dontshrink
-dontoptimize
-keep class ** { *; }

-allowaccessmodification
-dontpreverify
-classobfuscationdictionary obfuscation-dictionary.txt
-renamesourcefileattribute SourceFile
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, EnclosingMethod
-optimizationpasses 3
-mergeinterfacesaggressively