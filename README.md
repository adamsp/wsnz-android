What's Shaking, NZ? for Android
============
This is an app for monitoring quakes all around New Zealand. For more details see the website at http://www.whatsshaking.co.nz

You can download the app from Google Play here: https://play.google.com/store/apps/details?id=speakman.whatsshakingnz

You will need a Google Maps API key to run this app, you can get that here: https://developers.google.com/android/maps-api-signup

## Acquiring the code

This project uses Git Submodules. Clone the code using whatever method you normally do - if you used the Github application, you should not need to do anything further, it should have pulled all the required code for you, including the ActionBarSherlock submodule.

If you cloned by running `git clone git://github.com/adamsp/wsnz-android.git` command, you also need to do the following:

```
cd wsnz-android/
git submodule init
git submodule update
```
	
This will download the required ActionBarSherlock library. This references a fork (https://github.com/adamsp/ActionBarSherlock) of the offical ABS project that has the Maps version of the Android support library: https://github.com/petedoyle/android-support-v4-googlemaps

## Eclipse

Some issues when importing the existing code into Eclipse:
- You will probably need to specify a different workspace to the directory the code is in (ie, clone to C:/code/wsnz-android and specify workspace C:/Users/username/workspace) before you can succesfully use the File -> New -> Project -> Android -> Android Project from Existing Code option. See these links for details: http://stackoverflow.com/questions/4054216/opening-existing-project-from-source-control and http://stackoverflow.com/questions/5784652/eclipse-invalid-project-description-when-creating-new-project-from-existing-so?lq=1 and http://code.google.com/p/android/issues/detail?id=8431
- You will probably need to specify the correct version of Android for the library and the project, after importing into Eclipse. Right-click the project in Eclipse -> Properties -> Android, and select Google APIs 15. You may need to do this for both.
- Make sure you're using Java 1.6 to compile with. I had troubles at one point where Eclipse decided to compile against Java 1.5.
