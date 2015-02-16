Outreach mobile application

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:21.+'
    compile 'com.google.android.gms:play-services:6.5.87'
    compile 'com.daimajia.numberprogressbar:library:1.2@aar'
    compile 'com.android.support:recyclerview-v7:21.0.+'
    compile 'com.android.support:cardview-v7:21.0.+'
    compile 'com.joanzapata.pdfview:android-pdfview:1.0.+@aar'
    compile 'com.nispok:snackbar:2.7.5'
    compile 'com.mcxiaoke.volley:library:1.0.+'
    compile 'com.jpardogo.materialtabstrip:library:1.0.8'
    compile 'com.google.apis:google-api-services-plus:v1-rev204-1.19.1'
    compile 'com.google.apis:google-api-services-drive:v2-rev155-1.19.1'
    compile 'com.google.api-client:google-api-client:1.18.0-rc'
    compile 'com.google.api-client:google-api-client-android:1.18.0-rc'
    compile 'com.google.http-client:google-http-client-android:1.18.0-rc'
    compile 'com.google.http-client:google-http-client:1.18.0-rc'
    compile('com.crashlytics.sdk.android:crashlytics:2.2.0@aar') {
        transitive = true;
    }
