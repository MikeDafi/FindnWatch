package selenium;

import javafx.application.Application;
import javafx.stage.Stage;

public class Space extends Application{

	

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

@Override
public void start(Stage primaryStage) throws Exception {
	// TODO Auto-generated method stub
	
}

 }


