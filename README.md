# MyMoviesApp

Movies app created for Android nanodegree project offered by Udacity.
To run this application,
(a) Edit the app/build.gradle file to contain your api_key to themoviedb 
Replace the api key in the following section

    buildTypes.each {
        it.buildConfigField "String", "MOVIEDB_API_KEY", "<Your API Key here>"
    }
(b) Deploy the project to a device/emulator
