# Lake Effect Scouting App

This is a scouting app built for the FIRST Robotics Competition. This app communicates with bluetooth with the [compainion server app](https://github.com/LakeEffectRobotics/LakeEffectScoutingServerApp/).

Scouts must download this app and record data about each match played. The data is then pulled from the server device. For the data to be pulled, the Server device must be paired with each client device.

The releases tab contains releases for each FIRST Robotics season.

Each year more and more features have been added, so releases for older seasons might not have as many features or be MUCH more buggy as they are not needed anymore (the first year, FIRST Stronghold, especially). Only use these for testing purposes or if you really need to simulate an old event.

# Building

Clone the repository and open it with Android Studio. This repository requires no extra libraries.

The device must support bluetooth to use this app, or else the app will not function at all.

Make sure to use the same versions of the server and client are used, or else there may be unintended consequences.

# Export Data

The exported data can be found on the server device in the folder "#ScoutingApp" folder. All data can be found in the csv folder with the robot number you are looking for.

There is a seperate EventData folder (as of FIRST Power Up 2017) that contains data for a [visual demonstration of the data](https://github.com/Aree-Vanier/Scouting-Event-Reader). Feed that folder directly into that reader to use it. Most of that data can also be found in the normal csv files as well for statistics and averaging purposes.

# Python Helper Files

See the "python parsing" file in this repository for helper python programs. Specify the folder of the scouting data on your computer when running them and they will combine or average the files according to their purpose.

Make sure to move the files from the phone onto the computer before using these helper programs. They do not use the "EventData" folder.

The results for these programs will be put in a folder called "results"

# Other Questions

You can contact @ajayyy or make an issue.

# Download on the Google Play Store

https://play.google.com/store/apps/details?id=ca.lakeeffect.scoutingapp

This build might contain team specific data (like logos). To remove this, please clone this repository and build it yourself.

Scheuduling and robot lists are all sent from the server.
