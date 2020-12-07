# FindnWatch
  Have you ever been in situation where you use half your window screen to do work and the other half to watch a movie. Don't you wish you can just use all your window space for work and have your movie playing somewhere else. The only issue is where is that somewhere else. If you leave it stagnant in a corner, then you might end up needing that space. Here comes FindnWatch!!!  FindnWatch will read each rgb value on your screen. Once the program has found a rectangle made of the same color, then the program will place a Selenium browser in that spot. You will simply copy the link that you wanted to watch the movie from and the program will put that link in the Selenium Browser. Besides the basic functionality, there are commands you can run that are listed below. If you forget any of these commands, don't fret. There is a system tray icon that is there for any assistance of reference to these commands. finding the largest spot on the screen then placing a chrome browser in that space.

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
  - ```Alt + a``` alternates between a fullscreen browser and the current browser's position/size
  - ```Alt + f``` alternates between largest and second largest rectangle
  - ```Alt + d``` alternates between browser taking up the left half or right half of the screen
  
## Author
  Michael Askndafi
  
