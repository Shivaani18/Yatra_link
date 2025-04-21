import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {
    private Stage splashStage;
    private final double FADE_DURATION = 2000;
    
    public void show() {
        splashStage = new Stage(StageStyle.TRANSPARENT);
        
        // Create temple logo/image
        ImageView logo = new ImageView("temple_logo.png"); // Add your logo
        logo.setFitHeight(150);
        logo.setFitWidth(150);
        
        // Create title
        Label title = new Label("Temple Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.GOLD);
        title.setEffect(new DropShadow(10, Color.BLACK));
        
        // Layout
        StackPane root = new StackPane();
        root.getChildren().addAll(logo, title);
        StackPane.setMargin(title, new Insets(150, 0, 0, 0));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a237e, #311b92);");
        
        Scene scene = new Scene(root, 600, 400);
        scene.setFill(null);
        splashStage.setScene(scene);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        splashStage.show();
    }
    
    public void hide(Runnable finishedHandler) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), 
            splashStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            splashStage.close();
            finishedHandler.run();
        });
        fadeOut.play();
    }
}