# FindnWatch
  The capability of dynamically finding the largest spot on the screen then placing a chrome browser in that space.

## Getting Started

### On Windows
  Clone the repo below to a working repository, enter the directory. Copy over ```bin``` and ```lib``` from your jre folder installed on your device to a folder named ```jre``` in the directory as the executable then run the executable processed for Windows as follows
 ```
 cd FindnWatch
 ./FindnWatch1.4\ Executable.exe
 ```
### On Any Platform 
  With the runnable jar, execute the jar from the terminal or with the script:
  
  ```
  java -jar FindnWatch1.4\ Runnable\ Jar.jar
  ```
  or
  ```
  ./FindnWatch1.4\ Jar\ Script.sh
  ```
  
### Prerequisites
- Updated version of Chrome
- An Internet Connection to the device running the software
- JRE 1.8  or Newer

### Quick Installation
Go to http://www.mediafire.com/file/79u3nj00crlo64e/FindnWatch1.4_Installer.exe/file
Done

### Features:
- configured to setup a localhost
- informs the status of internet connection
- dynamically scans the screen for the largest rectangle(s)
- Time 0(n^2)
- Space 0(n)
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
  
## Author
  Michael Askndafi
  
