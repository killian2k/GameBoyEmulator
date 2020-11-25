package ch.epfl.gameboj.gui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;

/**
 * Not instanciable class that contains only a static method - convert() - to convert a gameboy Image into an JavaFX image.
 */
public final class ImageConverter {
    private static final int[] COLORS = new int[] {0xFF_FF_FF_FF, 0xFF_D3_D3_D3,
            0xFF_A9_A9_A9, 0xFF_00_00_00};
    private static final int COLOR_BORDER = 0xFF_0C306F;

    private ImageConverter() {};

    
    /*public static Image convert(LcdImage image){
        /*Preconditions.checkArgument(image != null &&
                image.height() == LcdController.LCD_HEIGHT &&
                image.width() == LcdController.LCD_WIDTH);
                        Preconditions.checkArgument(image != null);
        WritableImage imageToDisplay = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        PixelWriter writer = imageToDisplay.getPixelWriter();
        for(int y = 0;y < LcdController.LCD_HEIGHT;++y) {
            for(int x = 0;x < LcdController.LCD_WIDTH;++x) {
                writer.setArgb(x, y, COLORS[image.get(x, y)]);
            }
        }

        return imageToDisplay;
    }*/
    
    /**
     * Convert a gameboy image into a JavaFX image.
     * @param image is the image from the gameboy.
     * @return the javaFX image corresponding.
     * @throws IllegalArgumentException if the image is null.
     */
    public static Image convert(LcdImage image){
        return convert(image,-1);
    }
    
    /**
     * Convert a gameboy image into a JavaFX image with a grid
     * @param image : is the image to convert
     * @param pixelEdgeSquare : is the size of the edge of each cell of the grid. If not positiv, no grid is displayed.
     * @return the javaFX image corresponding.
     * @throws IllegalArgumentException if the image is null.
     */
    public static Image convert(LcdImage image, int pixelEdgeSquare){  
        Preconditions.checkArgument(image != null);
        
        boolean border = pixelEdgeSquare > 0;
        int borderRows = (border)?1+(int)Math.ceil(image.width()/(double)pixelEdgeSquare):0;
        int borderLines = (border)?1+(int)Math.ceil(image.height()/(double)pixelEdgeSquare):0;
        
        int finalImageWidth = image.width() + borderRows;
        int finalImageHeight = image.height() + borderLines;
        
        WritableImage imageToDisplay = new WritableImage(finalImageWidth, finalImageHeight);
        PixelWriter writer = imageToDisplay.getPixelWriter();
        
        
        int borderYNumber = 0;
        for(int y = 0;y < finalImageHeight;++y) {
            int borderXNumber = 0;
            
            //Draw border line
            if(border && (y%(pixelEdgeSquare+1) == 0 || y == finalImageHeight)) {
                for(int x = 0;x < finalImageWidth;++x) {
                    writer.setArgb(x, y, COLOR_BORDER);
                }
                ++borderYNumber;
            }
            else {
            //Draw line of image
                for(int x = 0;x < finalImageWidth;++x) {
                    if(border && (x%(pixelEdgeSquare+1) == 0 || x == finalImageWidth)) {
                        writer.setArgb(x, y, COLOR_BORDER);
                        ++borderXNumber;
                    }else
                        writer.setArgb(x, y, COLORS[image.get(x-borderXNumber, y-borderYNumber)]);
                }
            }
        }

        return imageToDisplay;
    }
    
    
}
