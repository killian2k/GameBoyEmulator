package ch.epfl.gameboj.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public final class Main extends Application{
    public static final int HEIGHT_WINDOW = LcdController.LCD_HEIGHT;
    public static final int WIDTH_WINDOW = LcdController.LCD_WIDTH;
    private static final String TITLE_WINDOW = "Gameboy";
    private static final int VALUE_DELTA_PLAY_MODE = -1;
    private static final int RATIO_TILES_WINDOW = 2;

    private final Map<Object, Joypad.Key> mapKey = new HashMap<>();
    private final ImageView viewGameboyScreen = new ImageView();
    private Image currentImage;
    private long speedCpu = 1;
    private int ratioScreen = 2;
    private AnimationTimer timer;
    private GameBoy gb;
    private long start;
    private Cartridge cartridge;
    private Stage mainStage;
    private ImageView viewTiles;
    private Stage windowTiles;
    private long deltaTime = VALUE_DELTA_PLAY_MODE;

    private enum sizeScreen { SMALL, MEDIUM, BIG, HUGE}
    private sizeScreen actualSizeScreen = sizeScreen.MEDIUM;


    /**
     * Method that launch the gameboy.
     * @param args : must contain only the file path and name of the rom to play.
     */
    public static void main(String[] args) {
        Application.launch(args);
    }



    @Override
    public void start(Stage stage) throws Exception {
        if(super.getParameters().getRaw().size() != 1)         
            System.exit(1);
        cartridge = Cartridge.ofFile(new File(super.getParameters().getRaw().get(0)));
        gb = new GameBoy(cartridge);

        mainStage = stage;
        mainStage.setTitle(TITLE_WINDOW);
        mainStage.setResizable(false);
        mainStage.setOnCloseRequest(k -> System.exit(1));

        start = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateImage(now - start);
            }
        };        


        fillInMapKeys();
        updateSizeWindow();

        viewGameboyScreen.setOnKeyPressed(ke -> gameboyKeyEvent(ke,true));
        viewGameboyScreen.setOnKeyReleased(ke -> gameboyKeyEvent(ke,false));

        stage.setScene(new Scene(new BorderPane(viewGameboyScreen)));
        stage.show();
        viewGameboyScreen.requestFocus();

        timer.start();        
    }
    private double speedEffective = 1;
    private long deltaCpuCycles = 0;
    private void updateImage(long elapsed) {
        if(speedEffective < speedCpu)
            speedEffective += 0.01;
        else if(speedEffective > speedCpu){
            deltaCpuCycles += (long)(elapsed * GameBoy.CYCLES_PER_NANOSEC * (speedEffective-speedCpu));
            speedEffective = speedCpu;
        }

        long cyclesElapsed = (long)(elapsed * GameBoy.CYCLES_PER_NANOSEC * speedEffective + deltaCpuCycles);

        gb.runUntil(cyclesElapsed);

        currentImage = ImageConverter.convert(gb.lcdController().currentImage());
        if(viewTiles != null) {
            Image image = ImageConverter.convert(gb.lcdController().getAllTilesImage(), LcdController.EDGE_TILE);
            viewTiles.setImage(image);
            viewTiles.setFitHeight(image.getHeight()*RATIO_TILES_WINDOW);
            viewTiles.setFitWidth(image.getWidth()*RATIO_TILES_WINDOW);
            windowTiles.sizeToScene();
        }

        //currentImage = ImageConverter.convert(gb.lcdController().getAllTilesDisplayed());
        viewGameboyScreen.setImage(currentImage);
    }

    private void gameboyKeyEvent(KeyEvent ke, boolean isPressed) {
        Key k = mapKey.get(mapKey.containsKey(ke.getText())?ke.getText():ke.getCode());
        if(k != null) {
            if(isPressed)
                gb.joypad().keyPressed(k);
            else
                gb.joypad().keyReleased(k);
        }

        if(ke.getCode().equals(KeyCode.PRINTSCREEN) && !isPressed)
            screenshot();
        //Manage pause play
        else if(ke.getCode().equals(KeyCode.PAUSE) || ke.getCode().equals(KeyCode.F1) && !isPressed) {
            pausePlay(deltaTime == VALUE_DELTA_PLAY_MODE);
        }
        else if(ke.getCode().equals(KeyCode.F2) && !isPressed) {
            if(speedCpu == 1)
                speedCpu = 2;
            else
                speedCpu = 1;
        }
        else if(ke.isControlDown() && ke.getCode().equals(KeyCode.R) && isPressed) {
            restartGameboy();
        }
        else if(ke.getCode().equals(KeyCode.F11) && !isPressed) {
            actualSizeScreen = sizeScreen.values()[(actualSizeScreen.ordinal()+1)%sizeScreen.values().length];
            manageSizeScreenGameBoy(actualSizeScreen);
        }
        else if(ke.getCode().equals(KeyCode.F10) && !isPressed) {
            getWindowTiles();
        }

    }
   
    private void getWindowTiles() {
        if(viewTiles == null) {
            // New window (Stage)
            windowTiles = new Stage();
            viewTiles =  new ImageView();
            windowTiles.setTitle("Tiles of the memory");
            windowTiles.setScene(new Scene(new BorderPane(viewTiles)));
            
            windowTiles.setResizable(false);
            windowTiles.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    viewTiles = null;
                }
            });
            // Specifies the owner Window (parent) for new window
            windowTiles.initOwner(mainStage);

            windowTiles.show();
            mainStage.requestFocus();
        }      
        // Set position of second window, related to primary window.
        windowTiles.setX(mainStage.getX() + mainStage.getWidth());
        windowTiles.setY(mainStage.getY());

    }

    private void fillInMapKeys() {
        mapKey.put("a", Key.A);
        mapKey.put("b", Key.B);
        mapKey.put("s", Key.SELECT);
        mapKey.put(" ", Key.START);
        mapKey.put(KeyCode.UP, Key.UP);
        mapKey.put(KeyCode.DOWN, Key.DOWN);
        mapKey.put(KeyCode.LEFT, Key.LEFT);
        mapKey.put(KeyCode.RIGHT, Key.RIGHT);
    }

    
    private void pausePlay(boolean toPause) {
        if(toPause && deltaTime == VALUE_DELTA_PLAY_MODE) {
            timer.stop();
            deltaTime = System.nanoTime() - start;
        } else {
            timer.start();
            start = System.nanoTime() - deltaTime;
            deltaTime = VALUE_DELTA_PLAY_MODE;
        }
    }

    private void restartGameboy() {
        if(deltaTime == VALUE_DELTA_PLAY_MODE)
            pausePlay(true);
        Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment redémarrer la Gameboy?\n Toute progression non-sauvegardée sera perdue.", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            timer.stop();
            gb = new GameBoy(cartridge);
            start = System.nanoTime();
            timer.start();
        } 
        else {
            pausePlay(false);
        }
        
    }

    private void manageSizeScreenGameBoy(sizeScreen size) {
        switch(size) {
        default:case SMALL:ratioScreen = 1;break;
        case MEDIUM:ratioScreen = 2;break;
        case BIG:ratioScreen = 3;break;
        case HUGE:ratioScreen = 4;break;
        }
        updateSizeWindow();
    }

    private void updateSizeWindow() {
        viewGameboyScreen.setFitHeight(HEIGHT_WINDOW*ratioScreen);
        viewGameboyScreen.setFitWidth(WIDTH_WINDOW*ratioScreen);
        mainStage.sizeToScene();
    }

    private void screenshot() {
        int width = LcdController.LCD_WIDTH;
        int height = LcdController.LCD_HEIGHT;
        PixelReader r = currentImage.getPixelReader();

        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int l = 0;l < width;++l)
            for(int c = 0;c<height;++c)
                buffer.setRGB(l, c, r.getArgb(l, c));

        try {
            File f = new File("screenshot/img_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()) + ".png");
            f.mkdirs();
            ImageIO.write(buffer,"png",f);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
