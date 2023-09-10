# SmartHome Gesture Control 
This application allows users to practice SmartHome control gestures by recording practice
snippets and uploading them to a server for processing and results.

## FogServer
Images are processed by a local fog server running a REST api via Flask.

This Flask server is in development and can only connected by localhost. This means
that it can only be connected to by an Android emulator on the same machine as the fog
server (IP '10.0.0.2' in the Android Emulator connects to the locahost 127.0.0.1 IP).

To run the fog server, navigate to ./fogserver:

`py server.py`

## SmartHome Gesture Control Application
This application will allow a user to select a gesture. Once selected, the user can watch
an expert video of the gesture being performed. Then, they will be given the option to record
a video of themselves practicing the gesture. Afterwords, the gesture will be uploaded to the
fog server for processing.

### Specs
Testing and development was done via an Android emulator through Android Studio.

This application was developed an tested with the following constraints:

Gradle:     8
JDK:        8
API:        34
Test HW:    Pixel 7 API 34

## Future Enhancements
1. For screen 3, after recording the video, replay it for review.
2. For screen 3, allow upload, re-record, and return to selection options.
3. Add a 'back' button for each screen.
4. Restrict dropdown size from selection screen.
5. Delete video once uploaded to the fogserver.