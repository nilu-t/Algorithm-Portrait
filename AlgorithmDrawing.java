import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Graphics;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class AlgorithmDrawing{

    /*
     * This project uses travelling salesman algorithm visualizer given an image from the user.
     */

     final int blackColour = 0; //Colour code for black.
     final int whiteColour = 255; //Color code for white.
     final int thresholdColor = 110; //The threshold color which determines if the buffered image pixel at (i,j) will be white or black.
     final Color black = new Color(blackColour); //black colour object.
     final Color white = new Color(whiteColour); //white colour object.

     private Color [][] allPixels; //2D array which stores all the pixels. 
     private Graphics imageGraphics;
     private BufferedImage image; //the Buffered image to manipulate the image data.

     private ArrayList <Integer> drawnXList; //list of all X coordinates which were drawn in order.
     private ArrayList <Integer> drawnYList; //list of all Y coordinates which were drawn in order.

     private int numOfPixels; //the number of pixels which are drawn.
     private boolean [] visited; 

     private ArrayList <Integer> shortestPixelNumList;
     private HashMap <Integer, ArrayList<Integer> > shortestPixelTourMap; //used after applying nearest neighbour TSP algorithm. Key is the pixel number, value is the list of the tour. 
     private HashMap <Integer,Double> shortestPixelTourDistMap; //used after applying nearest neighbour TSP algorithm. Key is the pixel number, value is the distance. 

     private double [][] adjacencyDistanceMatrix; //euclidean adjacency distance matrix.


     //overloaded constructor which sets the buffered image.
     public AlgorithmDrawing(BufferedImage image){
        this.image = image; 
        
        this.allPixels = new Color [image.getWidth()][image.getHeight()]; //initializing array of all the rgb pixels in the picture.
        this.drawnXList = new ArrayList<>(); //initializing ArrayList of all drawn (in order) x-coordinates.
        this.drawnYList = new ArrayList<>();//initializing ArrayList of all drawn (in order) y-coordinates.
        this.shortestPixelNumList = new ArrayList<>();
        this.shortestPixelTourMap = new HashMap<>();
        this.shortestPixelTourDistMap = new HashMap<>(); 
     }

     /**
      * This method creates the drawn image using Travelling Salesman (Nearest Neighbour) algorithm to draw the shortest tour image and the Dijkstra algorithm to draw the shortest path from the first and last pixel.
      * @throws IOException
      * @throws InterruptedException
      */
     public void createTravellingSalesmanDrawing() throws IOException, InterruptedException{
         createAllPixelsArray(); //instantiating the 2D array of all the image pixels.
         drawPixelledImage(); //drawing the image by plotting a random 50% subset of all image pixels from the buffered image.
         createDistanceAdjacencyMatrix(); //create the adjacency matrix.

         System.out.println("num of pixels: " + this.numOfPixels);

         Graphics2D g2d = (Graphics2D) this.imageGraphics;
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

         for(int i = 0; i < this.numOfPixels; i++){
            applyTravellingSalesmanAlg(i); //applying the travelling salesman algorithm for each "start" pixel index.
         }//end of for loop.


         applyDijkstraAlg(); //applying the 

         drawShortestTour(g2d); //draw the shortest tour after the TSP NN algorithm is applied.

         createImageFile(); //creating the image file within this project folder called "drawnImage.png".
     }

     
    /**
     * This method instantiates the array of all 2d array color pixels using the buffered image drawing.
     */
    public void createAllPixelsArray(){

         for (int j = 0; j < image.getHeight(); j++){
             for(int i = 0; i < image.getWidth(); i++){
 
                 //for every pixel in the image, a new colour object is obtained.
                 Color pixelColour = new Color(image.getRGB(i, j));
                 this.allPixels[i][j] = pixelColour;
 
                 //rgb:0,0,0 is black. If each rgb component is less then threshold then the new colour of the pixel will be black.
                 if(allPixels[i][j].getRed() <= thresholdColor || allPixels[i][j].getGreen() <= thresholdColor || allPixels[i][j].getBlue() <= thresholdColor){
                     this.allPixels[i][j] = black; //setting the pixel 2d array at (i,j) to black colour object.
                 }
                 else{
                     this.allPixels[i][j] = white; //setting the pixel 2d array at (i,j) to white colour object.
                 }
 
                 image.setRGB(i, j, Color.WHITE.getRGB()); //setting the original buffered image at pixel (i,j) with white RGB value. This "removes" the pixels of the original buffered image.
 
             }//end of inner for-loop.
         }//end of outer for-loop.
    }

    /**
    * This method draws only a random subset of the black pixels. 
    * This is done by iterating through all the image pixels in which every 4th height and 4th width pixels are skipped. Then there is a 50% chance the pixel will be drawn.
    * The drawn pixel will be 1x1 in size. 
    */
    public void drawPixelledImage(){
        this.imageGraphics = this.image.getGraphics(); //image graphics object.
        this.imageGraphics.setColor(Color.BLACK); //the colour of the pixel to be drawn.

        for(int j = 0; j < image.getHeight(); j+=4){
            for(int i = 0; i < image.getWidth(); i+=4){

                //if the pixel at (i,j) is black then there is a 50% chance a 1x1 pixel is drawn at (i,j).
                if(Math.random() < 0.5){
                    if(allPixels[i][j] == black){
                        this.imageGraphics.setColor(Color.BLACK); 
                        //this.imageGraphics.drawString("("+ i + "," + j +")" ,i, j);  //the size of the pixel drawn is 1x1 in size.
                        this.imageGraphics.fillOval(i, j, 1, 1); //drawing a 1x1 pixel.

                        this.drawnXList.add(i); //add pixel coordinate at i to drawnXList.
                        this.drawnYList.add(j); //add pixel coordinate at j to drawnYList.
                        this.numOfPixels++; //incrementing the number of pixels by 1.
                    }

                }
            }//end of inner for-loop.
        }//end of outer for-loop.
        
    }

    /**
     * This method draws the shortest tour after the shortest tour via travelling salesman algorithm is found.
     * @param g2d
     */
    public void drawShortestTour(Graphics2D g2d){
        
         double shortestDistanceTour = Integer.MAX_VALUE;
         int startIndex = -1;

         //iterating through the HashMap with all distances to find the pixel start index with the shortest distance. 
         for(int i = 0; i < this.shortestPixelTourDistMap.size(); i++){

            if(this.shortestPixelTourDistMap.get(i) < shortestDistanceTour){
                shortestDistanceTour = this.shortestPixelTourDistMap.get(i);
                startIndex = i;
            }
         }

         System.out.println("The shortest tour distance is " + shortestDistanceTour + " for start index " + startIndex);
         System.out.println("The shortest tour is " +this.shortestPixelTourMap.get(startIndex));

         //drawing the shortest distance tour. 
         for(int i = 1; i < this.numOfPixels; i++){
             g2d.drawLine(this.drawnXList.get(this.shortestPixelTourMap.get(startIndex).get(i-1)), this.drawnYList.get(this.shortestPixelTourMap.get(startIndex).get(i-1)), this.drawnXList.get(this.shortestPixelTourMap.get(startIndex).get(i)), this.drawnYList.get(this.shortestPixelTourMap.get(startIndex).get(i)));
         }

    }

    /**
     * 
     */
    public void applyDijkstraAlg(){

    }

    /**
     * Nearest neighbour travelling salesman algorithm implementation for the starting pixel index.
     */
    public void applyTravellingSalesmanAlg(int startIndex){
        this.shortestPixelNumList.clear(); // clear the pixel list
        this.visited = new boolean[this.numOfPixels];

        double totalDistance = 0.0;
        int nextPixel = 0;
        int pixelVisitCount = 1; //initally one because the first pixel is already visited.

        int firstPixel = startIndex;

        this.visited[firstPixel] = true; // the pixel at the start location is already visited.
        this.shortestPixelNumList.add(firstPixel);

        //iterating through all possible distances for each pixel (xCord, yCord) to get the tour route from TSP nearest neighbour algorithm. 
        for (int j = pixelVisitCount; j != this.numOfPixels; j++){

            double minimumDistance = Integer.MAX_VALUE; //before iterating throught the pixels the minimum distance is set as the largest possible Integer value, to ultimately compare and find the real minimum distance from the adjacency matrix.

            for( int i = 0; i < this.numOfPixels; i++){
                double euclideanDistance = this.adjacencyDistanceMatrix[startIndex][i]; //traverse the entire row for the start pixel. 

                /*
                 * The non-visited pixels shortest euclidean distance is determined.
                 */
                if(this.visited[i] == false && (euclideanDistance < minimumDistance)){
                    minimumDistance = euclideanDistance;
                    nextPixel = i; // the next pixel is the neighbour pixel which will be used to find its shortest neighbour.
                }
            }

            this.visited[nextPixel] = true;

            startIndex = nextPixel; //the start location is now the shortest neihbour pixel.

            pixelVisitCount++; //increment the pixel visited.

            totalDistance += minimumDistance; //incrementing the total distance taken by the minimum distance found.

            this.shortestPixelNumList.add(startIndex);

            }//end of while loop.

            this.shortestPixelNumList.add(firstPixel);

            totalDistance += (this.adjacencyDistanceMatrix[firstPixel][startIndex] * 2); //including the distances for the first pixel and also the distance for the second pixel.

            this.shortestPixelTourDistMap.put(firstPixel,totalDistance); //now putting the tour distance of the pixel in the HashMap.

            this.shortestPixelTourMap.put(firstPixel, this.shortestPixelNumList);

            System.out.println("TOTAL DISTANCE FOR PIXEL #: " + (firstPixel + 1) + " IS " + totalDistance);

    }

    public void createDistanceAdjacencyMatrix(){
        this.adjacencyDistanceMatrix = new double[numOfPixels][numOfPixels];

        //iterating through all the possible distances for the pixel (xCord, yCord) to create adjacency matrix of euclidean distances.
        for(int j = 0; j < this.numOfPixels; j++){

            for(int i = 0; i < this.numOfPixels; i++){
                double euclideanDistance = Math.hypot(drawnXList.get(j) - drawnXList.get(i), drawnYList.get(j) - drawnYList.get(i)); //Math.hypot returns the sqrt(x^2 + y^2) in this case it returns the euclidean distance of sqrt((x1-x2)^2 + (y1-y2)^2)
                
                //add edge.
                this.adjacencyDistanceMatrix[i][j] = euclideanDistance; 
                this.adjacencyDistanceMatrix[j][i] = euclideanDistance; 
            }
        }

    }

    public void createImageFile() throws IOException{
        ImageIO.write(image, "png", new File("drawnImage.png")); //writing the image as a new file.
        this.imageGraphics.dispose(); //dispose the graphics object.
    }

}//end of class.