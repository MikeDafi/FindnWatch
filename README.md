# FindnWatch
  The capability of dynamically finding the largest spot on the screen then placing a chrome browser in that space.

## Getting Started
  Clone the repo below to a working repository, enter the directory, run the executable processed for Windows
 ```
 git clone https://github.com/MikeDafi/FindnWatch.git
 cd FindnWatch
 ./FindnWatch1.1\ Executable.exe
 ```

### Prerequisites
- Updated version of Chrome
- An Internet Connection to the device running the software
- JRE 1.8  or Newer
- To run the script, installer Inno Setup Compiler
 
### Installing
 Within the cloned repository, click on **FindnWatch Installer**:
 ```
 ./FindnWatch1.1\ Installer.e
 ```
 For a console-driven  installer, click on **FindnWatch1.1 w Console Installer**
  ```
 ./FindnWatch1.1\ w\ Console\ Installer.e
 ```
### Features:
- configured to setup a localhost
- informs the status of internet connection
- scans the screen for the largest rectangle(s)
- informs the user if no sufficient rectangle was found
- informs the user if their current clipboard suffices as input to the browser
- places a chrome browser in the located rectangle
  - ```Alt + q``` quits the browser
  - ```Alt + w``` rescreenshots and refinds the largest rectangles(s)
  - ```Alt + e``` brings the browser to the front of the screen
  - ```Alt + s``` quits browser, reopens browser with new contents in clipboard as input to the browser
  - ```Alt + a``` alternates betweens a fullscreen browser and the current browser's position/size
  - ```Alt + f``` alternates between largest and second largest rectangle
  - ```Alt + d``` alternates between browser taking up the left half or right half of the screen
  
### RoadMap
  Implementing an installer for Mac OS users
  
## Author
  Michael Askndafi
  
