package selenium;


import io.github.bonigarcia.wdm.WebDriverManager;

public class Space extends Thread{

	public void run() {
		try {
			WebDriverManager.chromedriver().setup();
		}catch(Exception e) {}
		
	}

   double XCoordinate;
   double XSecondCoordinate;
   double YCoordinate;
   double width;
   double height;
   double size;
   int color;
 
   public Space(){
    XCoordinate = 0;
    XSecondCoordinate = 0;
    YCoordinate =0;
    width = 0;
    height = 0;
    size = 0;
    color = 0;
   }

   public Space(Space other){
    XCoordinate = other.XCoordinate;
    XSecondCoordinate = other.XSecondCoordinate;
    YCoordinate = other.YCoordinate;
    width = other.width;
    height = other.height;
    size = other.size;
    color = other.color;

   }
 }


