import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;


public class DrawingMain{

    public static void main(String [] args) throws IOException, InterruptedException{

        File drawingFile = new File("/Users/niluthiru/Desktop/Java algorithm portrait /Einstein.jpg");
        BufferedImage image = ImageIO.read(drawingFile); //buffered image to manipulate the drawing file image data.
        
        System.out.println("Height of drawing image " + image.getHeight()); //print image height
        System.out.println("Width of drawing image " + image.getWidth()); //print image width

        AlgorithmDrawing drawingObj = new AlgorithmDrawing(image);
        drawingObj.createTravellingSalesmanDrawing();
        
    }

}