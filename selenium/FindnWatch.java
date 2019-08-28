/*
 * Author : Michael Askndafi
 */


package selenium;

import java.awt.datatransfer.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.awt.Graphics2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.chrome.ChromeOptions;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.github.bonigarcia.wdm.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class FindnWatch extends Space implements NativeKeyListener {
	private static byte[] pixels;// holds all the colors as bytes(blue->green->red)

	private static int globalIndex = 0; // swap between largest and next largest Supreme

	private static boolean threadFinished; //checks if threads are finished to instantiated ChromeDriver

	private static boolean hasAlphaChannel;// alpha pixel is available

	private static int pixelLength;// either 3(argb) or 4(rgb)

	private static int screenHeight;// length of screen

	private static int screenWidth;// width of the screen

	private static WebDriver driver;// the window to manipulate

	private static Robot robot; // used to capture the screen

	private static BufferedImage image; // screenshots the screen

	private static Rectangle imgSize; // the rectangle of the monitor screen

	private static ArrayList<String> pidInfo = new ArrayList<String>(); // finding the running chrome pids

	private static boolean hasMaximized = false; // checks if already maximized

	private static int chromeWidth = 300; // minimum width of the selenium driver

	private static int chromeHeight = 200; // minimum height of the selenium driver

	private static ArrayList<Space> Supremes = new ArrayList<Space>();// the largest rectangle on the screen

	private static ExecutorService es = null;//the thread pool client
	

	public static void main(String args[])
			throws IOException, UnsupportedFlavorException, AWTException, NativeHookException, InterruptedException {

		
		//turns off logging, for GlobalNativeHook
		LogManager.getLogManager().reset();
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);

		Space Temp = new Space();// the current rectangle on the screen

		Supremes.add(new Space());

		// checks if there is internet and outputs an appropriate message.
		while (!netIsAvailable()) {
			if (!errorMessageConfig("There is no Internet")) {
				System.exit(1);
			}
		}

		//keeps track of the threads so as to await before making a new ChromeDriver
		//sometimes thread finishes after newly instantiated driver(not allowed)
		es = Executors.newCachedThreadPool();
		es.execute(new Runnable() {

			@Override
			public void run() {

				try {
					WebDriverManager.chromedriver().setup();
				} catch (Exception e) {
				}

			}
		});
		es.shutdown();//closes the pool

		// find the dimensions(width and height) of the current monitor
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		screenHeight = (int) screenSize.getHeight();
		screenWidth = (int) screenSize.getWidth();

		// rectangle of the width and height
		imgSize = new Rectangle(screenWidth, screenHeight);

		robot = new Robot();// for grabbing screenshot

		// finds the largest rectangle
		Supremes = configureImage(Temp, Supremes);

		// setups the driver
		updatedSupremeNewWindow(Supremes, globalIndex);

	}// main() ends here

	
	/*
	 * Purpose : Creates an error message of the issue 
	 * Description : Creates a frame as a container for a passed by value message of the error, in which the user
	 * can end the program or try to fix the issue. 
	 * Param : errorMessage - the string to output the error currently happening 
	 * Errors : none
	 * Return : true - the error Message wasn't dismissed, false - should end program
	 */
	public static boolean errorMessageConfig(String errorMessage) {
		JFrame frmOpt = new JFrame();// creates a new frame as a container for the message(only way to set always on
										// top
		//space is default button activation, i'm changing that to the 'enter' key
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);//let's you press enter when message displays
		UIManager.put("OptionPane.okButtonText", "Try Again");// every okButton is masked with a "Try Again message

		frmOpt.setSize(400, 200);// the width x height of the frame is set

		frmOpt.setVisible(false); // showMessageDialog is one window and frmOpt is another, set the frame to
									// empty(no double windows)

		frmOpt.setAlwaysOnTop(true); // keeps the error message in the foreground for the user to click out of
										// message

		frmOpt.setLocationRelativeTo(null); // by setting to null, the location is centered to the screen's width and
											// height

		// html styled message to allow style configurations and alignment within frame
		String message = "<html><body><div  style = 'font-size:12px;font-style:bold;' width='200px' align='center'>"
				+ errorMessage + "</div></body></html>";

		// interprets the html message as a JLabel
		JLabel messageLabel = new JLabel(message);


		Object[] options = { "Try Again", "Exit" };// the titles of the two optionDialog buttons

		// the error Message with the message displayed
		
		int n = JOptionPane.showOptionDialog(frmOpt, messageLabel, "FindnWatch", JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE, new ImageIcon("./FindnWatchErrorIcon.png"), options, options[0]);

		// will try again
		if (n == JOptionPane.YES_OPTION) {
			return true;
		}

		return false;// will quit program
	}

	/*
	 * Purpose : Finds the chrome processes currently running 
	 * Description : Retrives
	 * 					all the processes currently running into the arraylist, closes the task
	 * 					manager file, parses each process to be separated by one space, then finds
	 * 					the chrome processes and saves their pid, amounting to a list of pids that
	 * 					indicate the chrome processes running before the new selenium driver 
	 * Param : pidInfo - the arrayList to encapsulate each pid 
	 * Errors : IO Exception for using the static Runtime methods 
	 * Return : none
	 */
	public static void getChromePIDS(ArrayList<String> pidInfo) throws IOException {

		String line;// will hold the current process

		// retrieves all the processes
		Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");

		// opens the Process variable as a file to read from
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

		// adds each line until the BufferedReader has no more info
		while ((line = input.readLine()) != null) {
			pidInfo.add(line);
		}

		input.close(); // closes the file

		// goes through each line in the arrayList and keeps only the PID
		for (String currentLine : new ArrayList<>(pidInfo)) {

			pidInfo.remove(currentLine); // removes the currentline

			// checks if the process is a chrome browser
			if (currentLine.startsWith("chrome.exe")) {

				// replaces the multitude of spaces with a single space
				String oneSpacedLine = currentLine.replaceAll("^ +| +$|( )+", "$1");

				// skips the first word to get to the start of the PID
				int start = oneSpacedLine.indexOf(" ") + 1;

				// end of PID
				int end = oneSpacedLine.indexOf(" ", start + 1);

				// adds the PID
				pidInfo.add(oneSpacedLine.substring(start, end));
			}
		}
	}

	/*
	 * Purpose : Finds the selenium chrome driver from the current running chrome
	 * drivers 
	 * Description : Retrieves the currently running processes at the time,
	 * 				parses the processes to see if the chrome PID exists in the pidInfo List, if
	 * 				so the process would be terminated so the pid will be returned 
	 * 				Param : pidInfo - the arrayList to encapsulate each pid 
	 * Errors : IO Exception for using the static Runtime methods 
	 * Return : the pid that should be terminated
	 */
	public static String foundSeleniumChrome(ArrayList<String> pidInfo) throws IOException {

		String line; // carrying the current line

		// retrieves the currently running processes
		Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");

		// builds the processes variable as a file to read from
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

		// parses each process until the end of file
		while ((line = input.readLine()) != null) {

			// checks if the process is a chrome driver
			if (line.contains("chrome.exe")) {

				// eliminates all the extra spaces with a single space
				String oneSpacedLine = line.replaceAll("^ +| +$|( )+", "$1");

				// finds the pid which is after the first space
				int start = oneSpacedLine.indexOf(" ") + 1;

				// finds the end of pid
				int end = oneSpacedLine.indexOf(" ", start + 1);

				// returns the pid
				oneSpacedLine = oneSpacedLine.substring(start, end);

				// if the pid isn't there then it's a selenium driver
				if (!pidInfo.contains(oneSpacedLine)) {
					return oneSpacedLine;// returns the pid of the selenium driver
				}
			}
		}

		return null; // no selenium driver is running
	}

	/*
	 * Purpose : For remembering what PID is the running selenium driver 
	 * Description : uses the param to write to a file that must be opened to remember what to kill later
	 * Param : lineContent - the string to input into the opened file that indicates the pid that is open
	 * that will be inputted 
	 * Errors : IO Exception for using the BufferedWriter
	 * Return : none
	 */
	public static void usingDriver(String lineContent) throws IOException {

		// the space acts as a delimiter
		String fileContent = lineContent + " ";

		// opens the file
		BufferedWriter writer = new BufferedWriter(new FileWriter("./existence.txt", true));

		// writes the string
		writer.write(fileContent);

		// close the file
		writer.close();
	}

	/*
	 * Purpose : For ending the running selenium drivers 
	 * Description : By accessing the file passed in, the running PIDs are forcefully 
	 * 					terminated then empty the file. 
	 * Param : fileName - the file path to be found 
	 * Errors : IO Exception for using the Runtime type,FileNotFoundException is for using File type 
	 * Return : none
	 */
	public static void endRunningDriver(String fileName) throws FileNotFoundException, IOException {

		// gets the instance of the file
		Scanner scan = new Scanner(new File(fileName));

		int start = 0;// start of the file

		// goees through each line, terminating selenium drivers
		while (scan.hasNext()) {

			// interpretst the line as a string
			String line = scan.nextLine().toString();

			// finishes the current pid
			int end = line.indexOf(" ", start);

			// substring of the pid
			
			if(end < start) {
				continue;
			}
			
			String searchStr = line.substring(start, end);

			// forcefully ends tasks based off of PID
			Runtime.getRuntime().exec("taskkill /F /T /PID " + searchStr);

			// next pid based off of end as a startpoint
			start = line.indexOf(" ", end + 1);

		}
		scan.close();// closes the file

		PrintWriter pw = null;// initializes pw

		// creates a file with that filename
		try {
			// C:\\Program Files (x86)\\FindnWatch\\existence.txt
			pw = new PrintWriter("./existence.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null) {
			pw.close();// closes an existing file
		}

	}

	/*
	 * Purpose : Runs the selenium driver 
	 * Description : Configures the selenium
	 * driver with dimensions of the parameter Supreme then applies the selenium
	 * ratio model to frame correctly. Applies a global listener. 
	 * Param : Supremes holds the dimensions for the largest rectangles
	 * 			currentIndex - the index in Supremes to output 
	 * Errors : IO Exception for using the
	 * Runtime, UnsupportedFlavorException is for DataFlavor, NativeHookException is
	 * for GlobalHook 
	 * Return : null
	 */
	public static void updatedSupremeNewWindow(ArrayList<Space> Supremes, int currentIndex)
			throws IOException, UnsupportedFlavorException, NativeHookException, InterruptedException {

		// declares the options for this driver
		ChromeOptions options = new ChromeOptions();
		
		if(!new File("./userProfile").isDirectory()) {
			// the adblocker extension passed into the working directory
			options.addExtensions(new File("./AdBlockPlusChrome.crx"));

			//the extension to set the video tag to the size of the browser window
			options.addExtensions(new File("./FullScreenVideotoWindow.crx"));
		}
			
		// remembers the user's information
		options.addArguments("--user-data-dir=./userProfile");

		// uses the ratio of driver improper output to calculate the proper coordinates
		Supremes = findActualDimensions(Supremes);

		// initializes the width and height before booting up window
		options.addArguments("--window-size=" + (int) Supremes.get(globalIndex).width + ","
				+ (int) Supremes.get(globalIndex).height);

		// initializes the x and y position relative to the screen of the window
		options.addArguments("--window-position=" + (int) Supremes.get(globalIndex).XCoordinate + ","
				+ (int) Supremes.get(globalIndex).YCoordinate);

		// clears window of unneeded clutter
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        options.addArguments("--dns-prefetch-disable");
        options.addArguments("enable-features=NetworkServiceInProcess");

		// uses system clipboard to find the link
		String link = "";
		Clipboard mainClipBoard = null;
		try {
			// acquires the current state of the clipboard
			mainClipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
			link = (String) mainClipBoard.getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
		}

		// checks the beginning of the clipboard for netflix

		while (!(link.length() < 1000)) {

			// prints the error message
			if (!errorMessageConfig("Invalid Link, check what has been copied")) {
				System.exit(1);
			}

			// gets the clipboard again
			try {
				// acquires the current state of the clipboard
				mainClipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
				link = (String) mainClipBoard.getData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
			}
		}

		// gets the current pids
		getChromePIDS(pidInfo);

		// ends the selenium drivers
		endRunningDriver("./existence.txt");

		// adds the link to initially go to the website from the clipboard
		if(link.startsWith("http") || link.contains(".com")) {
			options.addArguments("-app=" + link);
		}else {
			options.addArguments("-app=" + "https://www.google.com/search?q=" + link);
		}
		if (!threadFinished) {
			threadFinished = es.awaitTermination(1, TimeUnit.MINUTES);
		}

		driver = new ChromeDriver(options);

		usingDriver(foundSeleniumChrome(pidInfo));
		
		// for only registering once
		boolean newlyRegistered = false;

		try {
			// checks if the global listener exists
			if (!GlobalScreen.isNativeHookRegistered()) {
				GlobalScreen.registerNativeHook();
				newlyRegistered = true;
			}
		} catch (NativeHookException ex) {
			ex.printStackTrace();
		}

		// for registering a key listener once
		if (GlobalScreen.isNativeHookRegistered() && newlyRegistered) {
			GlobalScreen.addNativeKeyListener(new FindnWatch());
		}

		
		
	}

	/*
	 * Purpose : Resizes the window with the dimensions of Supreme Description :
	 * Configures the selenium driver with dimensions of the parameter Supreme then
	 * applies the selenium ratio model to frame correctly. Applies a global
	 * listener. 
	 * Param : Supremes - holds the dimensions for the largest rectangles
	 * 			currentIndex - the index of Supremes that will be output
	 * Errors : none 
	 * Return : none
	 */
	public static void resizeCurrentWindow(ArrayList<Space> Supremes, int currentIndex) {

		// instantiates a dimension with Supreme's qualities
		org.openqa.selenium.Dimension d = new org.openqa.selenium.Dimension((int) Supremes.get(currentIndex).width,
				(int) Supremes.get(currentIndex).height);
		// sets the size of the window
		driver.manage().window().setSize(d);

		// positions the driver on the screen
		driver.manage().window().setPosition(
				new Point((int) Supremes.get(currentIndex).XCoordinate, (int) Supremes.get(currentIndex).YCoordinate));
	}

	/*
	 * Purpose : Gets the rectangle 
	 * Description : Finds the rectangle with only 3
	 * bytes for rgb 
	 * Param : Temp - the current rectangle working on 
	 * Supremes - the largest Rectangles 
	 * Errors : none 
	 * Return : The newly updated array of largest rectangles
	 */
	public static ArrayList<Space> configureImage(Space Temp, ArrayList<Space> Supremes) throws IOException {

		// a buffered image with the screen width and height, only using rgb
		image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);

		// assign the bufferedimage as a graphics2d reference to redraw the image
		// as a copy of the screenshot
		Graphics2D g2 = (Graphics2D) image.getGraphics();

		// use the captured screenshot to draw the image
		BufferedImage capture = robot.createScreenCapture(imgSize);

		// draws the image with capture as reference, null as no filter, and starting
		// from 0,0 of capture
		g2.drawImage(capture, null, 0, 0);

		// instantiates a global array for the pixels of the image
		FastRGB(image);

		// as reference for future colors(initial color)
		Temp.color = getRGB(0, 0);

		// finds the rectangle on the screen

		Supremes = getRectangle(Temp, Supremes);


		// means there were no rectangles that met the minimum size, default to
		// left-upper corner
		while (Supremes.get(0).XCoordinate == 0 && Supremes.get(0).YCoordinate == 0) {

			// error message
			if (!errorMessageConfig("Insufficient Space")) {
				System.exit(1);
			}
			// attempts to find a new rectangle
			Supremes = configureImage(Temp, Supremes);

		}

		return Supremes;// the array of rectangles

	}

	/*
	 * Purpose : Uses the found ratio to properly set the dimensions of selenium
	 * Description : Because the selenium driver doesn't output the driver to the
	 * exact coordinates that were initialized, the function resolves this flaw with
	 * the given ratios 
	 * Param : The arraylist of Spaces to adjust their widths and heights 
	 * Errors : none 
	 * Return : the newly configured arraylist of Spaces
	 */
	public static ArrayList<Space> findActualDimensions(ArrayList<Space> Supremes) {
		// Formula for width (768/960 * actual width)
		// Formula for height (825/1030 * actual height)
		double verticalRatio = 825.0 / 1030.0;
		double horizontalRatio = 768.0 / 960.0;

		// iterates across each supreme to adjust
		for (Space Supreme : Supremes) {

			Supreme.width *= horizontalRatio;
			Supreme.height *= verticalRatio;
			
			//if the 16 by 9 ratio is smaller than found, then height isn't altered
//			if(Supreme.width * 9/16 < Supreme.height) {
//				Supreme.height = Supreme.width * 9/16;
//			}
			Supreme.XCoordinate *= horizontalRatio;
			Supreme.YCoordinate *= verticalRatio;

		}
		return Supremes;
	}

	/*
	 * Purpose : Searches across each pixel to find the largest rectangle
	 * Description : Once a row of pixels with roughly the same colors are found,
	 * the method will see if this possible width is big enough and bigger than a
	 * previous rectangle, then finds the rectangle 
	 * Param : Temp - the Space that is used to traverse the screen
	 * 			Supremes - the largest rectangles are placed here
	 * Errors : none
	 * Return : the largest rectangles
	 */
	public static ArrayList<Space> getRectangle(Space Temp, ArrayList<Space> Supremes) throws IOException {

		int sameColorIndex = 0;//the index of the color at the beginning of the rectangle
		double tallest = 0;//the tallest height on that current's row color

		Space[] colSpaces = new Space[screenWidth];// all the spaces on that row
		ArrayList<Space> possibleSpaces = new ArrayList<Space>();//the spacest that could be big enough


		// traverses through each row and column of the image
		for (int currentRow = 0; currentRow < screenHeight; currentRow++) {
			
			sameColorIndex = 0;
			tallest = 0;

			for (int currentColumn = 0; currentColumn < screenWidth; currentColumn+=5) {


				

				// pixel RGB at the current column and row
				int color = getRGB(currentColumn, currentRow);

				//intializes the height and coordinates
				if(currentRow == 0) {
					colSpaces[currentColumn] = new Space();
					colSpaces[currentColumn].color = getRGB(currentColumn,0);
					colSpaces[currentColumn].height = 1;
					colSpaces[currentColumn].XCoordinate = currentColumn;
				}


				//the color on this row checks with the row above it
				if(IsSameColor(color,colSpaces[currentColumn].color)) {
					colSpaces[currentColumn].height += 1;
					colSpaces[currentColumn].size = colSpaces[currentColumn].height * colSpaces[currentColumn].width;
				}else {
					//different color
					colSpaces[currentColumn].color = color;
					colSpaces[currentColumn].YCoordinate = currentRow;
					colSpaces[currentColumn].height = 1;
					colSpaces[currentColumn].width = 1;
					colSpaces[currentColumn].size = colSpaces[currentColumn].height * colSpaces[currentColumn].width;			
				}


				//the first column doesn't have a column before it
				if (currentColumn - 1 >= 0) {

					//the color of the left-uppermost column of current rect
					int rectColor = colSpaces[sameColorIndex].color;

					//checks if the column before is the same
					if(IsSameColor(rectColor,colSpaces[currentColumn].color) && currentColumn + 5 < screenWidth) {

						//new tallest height
						if(tallest == 0) {

							//the highest height so far
							tallest = colSpaces[sameColorIndex].height;
							possibleSpaces.add(new Space(colSpaces[sameColorIndex]));

						}


						//a > b
						if(tallest > colSpaces[currentColumn].height) {
							tallest = colSpaces[currentColumn].height;

							//find a, replace a with b and check if a was big
							for(Space space : possibleSpaces) {

								//found a
								if(space.height > colSpaces[currentColumn].height) {

									//minimum width
									if(currentColumn - space.XCoordinate > chromeWidth) {

										//minimum height
										if(space.height > chromeHeight) {

											//updates internal variables
											space.XSecondCoordinate = currentColumn;
											space.width = space.XSecondCoordinate - space.XCoordinate;
											int height = (int) space.height;
//											if(space.height > space.width * 9 / 16) {
//												space.height = space.width * 9 / 16;
//											}else if(space.height * 16 /9 < space.width) {
//												space.width = space.height * 16 / 9;
//											}
											
											
											space.size = space.width * space.height;
											checkPossible(Supremes,space);
											space.height = height;
										}
									}
									//smaller height
									space.height = colSpaces[currentColumn].height;
									space.YCoordinate = currentRow - space.height;
								}

							}


							// tallest < b
						}else if (tallest < colSpaces[currentColumn].height) {
							tallest = colSpaces[currentColumn].height;
							
							possibleSpaces.add(new Space(colSpaces[currentColumn]));
						}

					}else {

						
						sameColorIndex = currentColumn;//new beginning to rectangle
						tallest = 0;//the new tallest height
						while(!possibleSpaces.isEmpty()) {


							possibleSpaces.get(0).width = currentColumn - possibleSpaces.get(0).XCoordinate;
							possibleSpaces.get(0).XSecondCoordinate = currentColumn;
//							if(possibleSpaces.get(0).height > possibleSpaces.get(0).width * 9 / 16) {
//								possibleSpaces.get(0).height = possibleSpaces.get(0).width * 9 / 16;
//
//							}else if(possibleSpaces.get(0).height * 16 / 9 < possibleSpaces.get(0).width) {
//								possibleSpaces.get(0).width = possibleSpaces.get(0).height * 16 / 9;
//							}
							
						
							
							possibleSpaces.get(0).size = possibleSpaces.get(0).width * possibleSpaces.get(0).height;
							if(possibleSpaces.get(0).width > chromeWidth && possibleSpaces.get(0).height > chromeHeight) {
								checkPossible(Supremes,possibleSpaces.get(0));

							}


							possibleSpaces.remove(0);


						}

					}	
				}		
			}

		}

		return Supremes;
	}
	/*
	 * Purpose : Sees if temporary space can be fit into top two Supremes
	 * Description : Compares the Supremes sizes and makes sure that they are distant enough from each other
	 * rectangle, or go full screen 
	 * Param : Supremes - the top rectangles
	 * 			possibleSpace - the space that will be compared with the Suprmemes
	 * Errors : none 
	 * Return : none
	 */
	public static void checkPossible( ArrayList<Space> Supremes, Space possibleSpace) {
		

		//just big enough
		if (Supremes.get(Supremes.size() - 1).size < possibleSpace.size) {
			//the biggest
			
				

				int index = Supremes.size() - 1;
				
				//intially has one 
				if(Supremes.get(0).XCoordinate == 0 && Supremes.get(0).YCoordinate == 0) {
					Supremes.remove(0);
					index = 0;
				}

				boolean smaller = false; //if true then possibleSpace exists in a space where there
										 //is a larger rectangle in that area
				for(int i = Supremes.size() - 1; i >= 0; i--) {
					//distant enough away from the biggest rectangle
					
					
					if(Math.abs(Supremes.get(i).YCoordinate - possibleSpace.YCoordinate) <= 100 &&
							Math.abs(Supremes.get(i).XCoordinate - possibleSpace.XCoordinate) <= 100) {
						
						//no need to have a smaller rectangle in the same space
						if(possibleSpace.size > Supremes.get(i).size) {	
							Supremes.remove(i);//removes smaller rect
							continue;//skips the index assignment because it was deleted
						}else {
							smaller = true;//can't add possibleSpace because too small(some other rect bigger in that area
						}
					}
					
					//determines where to put the possibleSpace, if bigger than where the smaller rect is
					//is where to put the possibleSpace
					if(possibleSpace.size > Supremes.get(i).size) {
						index = i;
					}
					
				}

				//if there are too many removals, set index to a valid number
				if(index > Supremes.size()) {
					index = Supremes.size();
				}

				if(!smaller) {
					//add where the index before is bigger 
					Supremes.add(index, new Space(possibleSpace));
				}
			
		} else {//means this rect is big enough but smaller than any currently stored rectangles 
			
			boolean cantAdd = true;//for seeing if should add or replace a rect in that area
			
			//checks to see if there are any bigger rects in the possibleSpace's area 
			for(int i = 0; i < Supremes.size(); i++) {
				
				//checks if they are too far away
				if (Math.abs(Supremes.get(i).XCoordinate - possibleSpace.XCoordinate) <= 100
						&& Math.abs(Supremes.get(i).YCoordinate - possibleSpace.YCoordinate) <= 100) {
					
					//checks if the possibleSpace is bigger to replace the current area's rect
					if(possibleSpace.size > Supremes.get(i).size) {
						Supremes.set(i, new Space(possibleSpace));
					}


						cantAdd = false;//no need to add the possibleSpace at the end
						break;
					
				}
			}

			

			if(cantAdd) {
				Supremes.add( new Space(possibleSpace));
			}
			
		}
		
	}

	public void nativeKeyPressed(NativeKeyEvent e) {

	}

	/*
	 * Purpose : Listens to keys without there being focus on the selenium driver
	 * Description : Depending on the input of keyboard keys, the driver will react.
	 * the driver can resize, rescan then resize, quit, switch to the next largest
	 * rectangle, or go full screen 
	 * Param : e - the event holding the keyboard input
	 * Errors : none 
	 * Return : none
	 */
	public void nativeKeyReleased(NativeKeyEvent e) {

		// checks if 'Alt' was pressed
		boolean isAltPressed = (e.getModifiers() & NativeKeyEvent.ALT_MASK) > 0;

		if (isAltPressed) {

			// quits the driver if the driver exists
			if (e.getKeyCode() == NativeKeyEvent.VC_Q) {

				// checks if there is a driver that is working
				if (!driver.toString().contains("(null)")) {
					try {
						// no more keyboard listeners
						GlobalScreen.unregisterNativeHook();
					} catch (NativeHookException ne) {
					}
					// quits out of the driver completely, including any children
					driver.quit();
					System.exit(1);
				}

				// resizes based off of earlier image
			} else if (e.getKeyCode() == NativeKeyEvent.VC_W) {

				// checks if exists
				if (!driver.toString().contains("(null)")) {

					// brings the driver to front
					driver.switchTo().window(driver.getWindowHandle());

					// moves the driver to get out of the way of monitor scanning
					driver.manage().window().setPosition(new Point(-2000, 0));

					// resets the size of rectangles
					Supremes.removeAll(Supremes);
					for (int index = 0; index < 2; index++) {
						Supremes.add(new Space());
					}

					// for later functions
					Space Temp = new Space();
					globalIndex = 0;// the initial index to start at

					// all these method calls are mentioned before
					try {
						Supremes = configureImage(Temp, Supremes);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Supremes = findActualDimensions(Supremes);
					resizeCurrentWindow(Supremes, globalIndex);
				}else {
					System.exit(1);
				}

				// brings the driver to the front
			} else if (e.getKeyCode() == NativeKeyEvent.VC_E) {
				if (!driver.toString().contains("(null)")) {
					driver.switchTo().window(driver.getWindowHandle());
				}else {
					System.exit(1);
				}

				// rescan and resize
			} else if (e.getKeyCode() == NativeKeyEvent.VC_S) {
				if (!driver.toString().contains("(null)")) {
					driver.quit();
					System.exit(1);
					
				}

				Supremes.removeAll(Supremes);
				for (int index = 0; index < 2; index++) {
					Supremes.add(new Space());
				}
				Space Temp = new Space();
				globalIndex = 0;
				try {
					Supremes = configureImage(Temp, Supremes);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				// restarts the driver, keeping the current listener
				try {
					updatedSupremeNewWindow(Supremes, globalIndex);
				} catch (IOException | UnsupportedFlavorException | NativeHookException | InterruptedException e1) {
				}

				// alternates between globalIndex and fullscreen
			} else if (e.getKeyCode() == NativeKeyEvent.VC_A) {
				if (!driver.toString().contains("(null)")) {
					driver.switchTo().window(driver.getWindowHandle());
					if (hasMaximized) {
						resizeCurrentWindow(Supremes, globalIndex);
					} else {
						driver.manage().window().fullscreen();

					}

					hasMaximized = !hasMaximized;
				}else {
					System.exit(1);
				}

				// switches between largest and second largest rectangles
			} else if (e.getKeyCode() == NativeKeyEvent.VC_F) {
				if (!driver.toString().contains("(null)")) {
					
					//switches to that window
					driver.switchTo().window(driver.getWindowHandle());
					
					if (Supremes.get(1).XCoordinate != 0 || Supremes.get(1).YCoordinate != 0) {
						if (globalIndex + 1 == Supremes.size()) {
							globalIndex = 0;
						} else {
							globalIndex += 1;
						}
						
						resizeCurrentWindow(Supremes, globalIndex);
					}

				}else {
					System.exit(1);
				}

				// has the driver take up left side of screen or right side of screen
			} else if (e.getKeyCode() == NativeKeyEvent.VC_D) {
				if (!driver.toString().contains("(null)")) {
					driver.switchTo().window(driver.getWindowHandle());
					Supremes.removeAll(Supremes);

					// sets up the dimensions for left(index = 0) and right(index = 1)
					for (int index = 0; index < 2; index++) {
						Supremes.add(new Space());
						Supremes.get(index).XCoordinate = index * screenWidth / 2;
						Supremes.get(index).height = screenHeight;
						Supremes.get(index).width = screenWidth / 2;
					}

					ArrayList<Double> supHeights = new ArrayList<Double>();
					for(Space Supreme : Supremes) {
						supHeights.add(Supreme.height);
					}
					// uses the ratio test again
					Supremes = findActualDimensions(Supremes);

					//switches between indexes
					if (globalIndex + 1 == Supremes.size()) {
						globalIndex = 0;
					} else {
						globalIndex += 1;
					}

					double verticalRatio = 825.0 / 1030.0;
					int index = 0;
					for (Space Supreme : Supremes) {
						Supreme.height = supHeights.get(index);
						Supreme.height *= verticalRatio;
						index++;
					}
					
					resizeCurrentWindow(Supremes, globalIndex);

				}else {
					System.exit(1);
				}

			}
		}
	}
	
	//necessary for NativeKeyListener Interface
	public void nativeKeyTyped(NativeKeyEvent e) {

	}

	/*
	 * Purpose : Compares two colors to see if they are roughly the same
	 * Description : By relying on the red, green, blue and possibly alpha colors on a 
	 * 					pixel, the method compares each
	 * 				of those factors and checks the difference of the two.
	 * Param : imageCompare - the pixel to compare with
	 * 			originalCompare - the other pixel to compare with
	 * Errors : IO Exception for using the static Runtime methods
	 * Return : true if they are the same color, otherwise false
	 */
	public static boolean IsSameColor(int imageCompare, int originalCompare) {
		int restriction = 20;
		if (getGreen(imageCompare) - getGreen(originalCompare) > restriction
				|| getGreen(originalCompare) - getGreen(imageCompare) > restriction) {
			return false;
		}
		if (getBlue(imageCompare) - getBlue(originalCompare) > restriction
				|| getBlue(originalCompare) - getBlue(imageCompare) > restriction) {
			return false;
		}
		if (getRed(imageCompare) - getRed(originalCompare) > restriction
				|| getRed(originalCompare) - getRed(imageCompare) > restriction) {
			return false;
		}
		if (hasAlphaChannel) {
			if (getAlpha(imageCompare) - getAlpha(originalCompare) > restriction
					|| getAlpha(originalCompare) - getAlpha(imageCompare) > restriction) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Purpose : Uses the imge to make an array of bytes indicative of the pixels
	 * Description : Using the getDataBuffer() method I retrieve the pixels as bytes
	 * 					and a routine to indicate the length of the pixels
	 * Param : image - the image captured to convert to pixels
	 * Errors : none
	 * Return : none
	 */
	public static void FastRGB(BufferedImage image) {
		pixels = (((DataBufferByte) image.getRaster().getDataBuffer())).getData();
		hasAlphaChannel = image.getAlphaRaster() != null;
		pixelLength = 3;
		if (hasAlphaChannel) {
			pixelLength = 4;
		}
	}

	/*
	 * Purpose : finds the rgb of pixel at the coordinates on the screen
	 * Description : Using the y-coordinate as number of rows to traverse and x to add to y, the argb
	 * 				 was found by starting from the position(blue) and iterating(green) and iterating(red)
	 * 				 packaging the pixel into an int.
	 * Param : x - the pixel's x coordinate
	 * 		   y - the pixel's y coordinate
	 * Errors : none
	 * Return : the pixel colors as an int
	 */
	public static int getRGB(int x, int y) {
		
		//using the width of the screen, y traverses across the pixels array
		int pos = (y * screenWidth * pixelLength) + (x * pixelLength);
		
		//since a 32-bit float can only go up to 2^24 + 1(or 16777216 then
		//-16777216 isn't possible, that way in case no pixels were found, 
		//argb would be invalid
		int argb = -16777216;
		if (hasAlphaChannel) {
			argb = (((int) pixels[pos++] & 0xff) << 24);
		}

		
		argb += ((int) pixels[pos++] & 0xff);//blue
		argb += (((int) pixels[pos++] & 0xff) << 8);//green
		argb += (((int) pixels[pos++] & 0xff) << 16);//red
		return argb;
	}
	
	/*
	 * Purpose : Gets the alpha color of the pixel
	 * Description : Masks the parameter and shifts over to return that part of the pixel
	 * Param : the packaged int with the colors of the pixel
	 * Errors : none
	 * Return : the alpha element of the parameters
	 */
	public static int getAlpha(int RGB) {
		if (hasAlphaChannel) {
			return (RGB >> 24) & 0xff;
		} else {
			return 0;
		}
	}
	/*
	 * Purpose : Gets the red color of the pixel
	 * Description : Masks the parameter and shifts over to return that part of the pixel
	 * Param : the packaged int with the colors of the pixel
	 * Errors : none
	 * Return : the red element of the parameters
	 */
	public static int getRed(int RGB) {
		return (RGB >> 16) & 0xff;
	}
	/*
	 * Purpose : Gets the green color of the pixel
	 * Description : Masks the parameter and shifts over to return that part of the pixel
	 * Param : the packaged int with the colors of the pixel
	 * Errors : none
	 * Return : the green element of the parameters
	 */
	public static int getGreen(int RGB) {
		return (RGB >> 8) & 0xff;
	}
	/*
	 * Purpose : Gets the blue color of the pixel
	 * Description : Masks the parameter and shifts over to return that part of the pixel
	 * Param : the packaged int with the colors of the pixel
	 * Errors : none
	 * Return : the blue element of the parameters
	 */
	public static int getBlue(int RGB) {
		return RGB & 0xff;
	}

	/*
	 * Purpose : Checks if there is internet connected to the device
	 * Description : attempts to connect into a port with the given url and if there are any errors
	 * 				 that means there is problem with the wifi
	 * Param : none
	 * Errors : MalformedUrlException - for url.openConnection, IOException - for the RuntimeException thrown
	 * Return : true - internet exists, false - no internet
	 */
	private static boolean netIsAvailable() {
		try {
			final URL url = new URL("http://www.google.com");
			final URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream().close();
			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			return false;
		} 
	}

}// class ends here
