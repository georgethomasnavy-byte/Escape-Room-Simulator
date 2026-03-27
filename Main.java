import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

// --- ADDED IMPORTS ---
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
// --- END OF ADDED IMPORTS ---

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Main extends Application {
    // ==== Game State ====
    private int teus = 200;
    private final Set<String> inventory = new HashSet<>();
    private int timeLeft = 300; // 5 min
    private boolean running = true;

    private Label teusLabel;
    private Label timerLabel;
    private Label hintLabel;
    private Label notificationLabel; // For big messages
    private ImageView backgroundImageView;

    private AnimationTimer animationTimer;

    @Override
    public void start(Stage stage) {
        showMainMenu(stage);
    }

    // ==== Main Menu ====
    private void showMainMenu(Stage stage) {
        // 1. Create a StackPane as the root
        StackPane root = new StackPane();

        // 2. Load your new menu background image
        ImageView backgroundView = loadImageView("/images/main_menu.jpg", "images/main_menu.jpg", 400, 300);

        // 3. Add the background (or a fallback color) to the root
        if (backgroundView != null) {
            root.getChildren().add(backgroundView);
        } else {
            // Fallback color if "main_menu.jpg" is missing
            Rectangle bgRect = new Rectangle(400, 300, Color.web("#2b2b2b"));
            root.getChildren().add(bgRect);
        }

        // 4. Create your buttons just like before
        Button startBtn = new Button("Start Game");
        Button exitBtn = new Button("Exit");

        startBtn.setOnAction(e -> showLevel1(stage));
        exitBtn.setOnAction(e -> stage.close());

        // 5. Create the VBox for your buttons
        VBox buttonLayout = new VBox(20, startBtn, exitBtn);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.setStyle("-fx-padding: 50; -fx-font-size: 16; -fx-background-color: transparent;");

        // 6. Add the button VBox on TOP of the background
        root.getChildren().add(buttonLayout);

        // 7. Create the scene using the StackPane
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Escape Room Simulator");
        stage.show();
    }

    // ==== Level 1 ====
    private void showLevel1(Stage stage) {
        // HUD labels
        teusLabel = new Label("TEUs: " + teus);
        timerLabel = new Label("Time: " + timeLeft);
        hintLabel = new Label("");
        teusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        timerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        hintLabel.setStyle("-fx-padding: 6 0 0 0; -fx-text-fill: white;");

        Button hintBtn = new Button("Hint (-50 TEUs)");
        hintBtn.setOnAction(e -> {
            if (teus >= 50) {
                teus -= 50;
                teusLabel.setText("TEUs: " + teus);
                hintLabel.setText("Hint: Look under the bed.");
            } else {
                hintLabel.setText("Not enough TEUs!");
            }
        });

        VBox hud = new VBox(6, teusLabel, timerLabel, hintBtn, hintLabel);
        hud.setAlignment(Pos.TOP_LEFT);
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Root stack so background sits behind everything and overlay elements sit on top
        StackPane root = new StackPane();

        // 1) Background: Load the initial closed room image
        backgroundImageView = loadImageView("/images/room_closed.jpg", "images/room_closed.jpg", 600, 400);
        if (backgroundImageView != null) {
            root.getChildren().add(backgroundImageView);
        } else {
            Rectangle bgRect = new Rectangle(600, 400, Color.web("#2b2b2b"));
            root.getChildren().add(bgRect);
            hintLabel.setText("Error: room_closed.jpg not found!");
            if (hintLabel.getScene() == null) {
                hintLabel.setStyle("-fx-padding: 6 0 0 0; -fx-text-fill: red; -fx-font-weight: bold;");
                root.getChildren().add(hintLabel);
                StackPane.setAlignment(hintLabel, Pos.CENTER);
            }
        }

        // 2) Interactive "hitboxes" (Rectangles) for bed and door
        
        // Bed Hitbox
        Rectangle bedHitbox = new Rectangle(150, 100, Color.TRANSPARENT);
        bedHitbox.setLayoutX(200);
        bedHitbox.setLayoutY(250);
        bedHitbox.setStrokeWidth(2);
        bedHitbox.setOnMouseClicked(e -> searchBed(bedHitbox));

        // Door Hitbox
        Rectangle doorHitbox = new Rectangle(100, 220, Color.TRANSPARENT);
        doorHitbox.setLayoutX(400);
        doorHitbox.setLayoutY(100);
        doorHitbox.setStrokeWidth(2);
        doorHitbox.setOnMouseClicked(e -> tryOpenDoor(stage, doorHitbox));

        // Overlay Pane for hitboxes and HUD
        Pane overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);

        VBox hudWrapper = new VBox(hud);
        hudWrapper.setAlignment(Pos.TOP_LEFT);
        hudWrapper.setLayoutX(10);
        hudWrapper.setLayoutY(10);
        overlayPane.getChildren().add(hudWrapper);

        overlayPane.getChildren().addAll(bedHitbox, doorHitbox);
        root.getChildren().add(overlayPane);

        
        // 3) Big Notification Label (sits on top of everything)
        notificationLabel = new Label("");
        notificationLabel.setStyle(
                "-fx-font-size: 40; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: rgba(0,0,0,0.7); " +
                "-fx-padding: 20; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
        );
        notificationLabel.setVisible(false); // Hide it until needed
        root.getChildren().add(notificationLabel); // Add to root StackPane to center it
        StackPane.setAlignment(notificationLabel, Pos.CENTER);
        
        // Moves the notification label down 50 pixels from the center
        notificationLabel.setTranslateY(150);


        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Level 1 - The Awakening");
        stage.show();

        // Start or restart timer
        running = true;
        startTimer();
    }

    // search bed handler
    private void searchBed(Rectangle bedHitbox) {
        if (!inventory.contains("AccessCard")) {
            inventory.add("AccessCard");
            bedHitbox.setDisable(true);
            hintLabel.setText("You found an Access Card!"); // Update small HUD
            
            notificationLabel.setText("Got Key!");
            notificationLabel.setVisible(true);
        } else {
            hintLabel.setText("You already have the card.");
            notificationLabel.setText("Got Key!");
            notificationLabel.setVisible(true);
        }
    }

    // try to open door
    private void tryOpenDoor(Stage stage, Rectangle doorHitbox) {
        if (inventory.contains("AccessCard")) {
            hintLabel.setText("Unlocked!"); // Update small HUD
            running = false;
            if (animationTimer != null) animationTimer.stop();
            doorHitbox.setDisable(true);

            // 1. Show the "Level Complete" message
            notificationLabel.setText("Level Complete! 🎉");
            notificationLabel.setVisible(true);

            // 2. Show the open door image
            Image openDoorImage = loadImage("images/room_open.jpg");
            if (openDoorImage != null) {
                backgroundImageView.setImage(openDoorImage);
            } else {
                hintLabel.setText("Unlocked! But couldn't load room_open.jpg");
            }
            
            // 3. Create a 2-second pause
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            
            // 4. After the pause, load Level 2
            pause.setOnFinished(e -> {
                showLevel2(stage);
            });
            
            // 5. Start the pause
            pause.play();

        } else {
            hintLabel.setText("Locked! Need Card.");
            notificationLabel.setVisible(false);
        }
    }
    
    // ==== Level 2 ====
    private void showLevel2(Stage stage) {
        // --- 1. Reset Game State ---
        timeLeft = 300; // Reset timer to 5 minutes
        inventory.clear(); // Start with an empty inventory for this level
        hintLabel.setText(""); // Clear old hints
        notificationLabel.setVisible(false); // Hide old notifications

        // --- 2. Setup HUD ---
        teusLabel.setText("TEUs: " + teus);
        timerLabel.setText("Time: " + timeLeft);

        Button hintBtn = new Button("Hint (-50 TEUs)");
        hintBtn.setOnAction(e -> {
            if (teus >= 50) {
                teus -= 50;
                teusLabel.setText("TEUs: " + teus);
                hintLabel.setText("Hint: Check the table first."); // New hint
            } else {
                hintLabel.setText("Not enough TEUs!");
            }
        });

        VBox hud = new VBox(6, teusLabel, timerLabel, hintBtn, hintLabel);
        hud.setId("hud-level2"); // <-- ID for lookup
        hud.setAlignment(Pos.TOP_LEFT);
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-border-radius: 6; -fx-background-radius: 6;");

        // --- 3. Setup Scene Root ---
        StackPane root = new StackPane();

        // --- 4. Load New Background (room2_closed.jpg) ---
        backgroundImageView = loadImageView("/images/room2_closed.jpg", "images/room2_closed.jpg", 600, 400);
        if (backgroundImageView != null) {
            root.getChildren().add(backgroundImageView);
        } else {
            Rectangle bgRect = new Rectangle(600, 400, Color.web("#555555"));
            root.getChildren().add(bgRect);
            hintLabel.setText("Error: room2_closed.jpg not found!");
            hintLabel.setStyle("-fx-padding: 6 0 0 0; -fx-text-fill: red; -fx-font-weight: bold;");
            root.getChildren().add(hintLabel);
            StackPane.setAlignment(hintLabel, Pos.CENTER);
        }

        // --- 5. Create New Hitboxes ---
        
        // Table Hitbox (Bottom Right)
        Rectangle tableHitbox = new Rectangle(150, 100, Color.TRANSPARENT);
        tableHitbox.setLayoutX(300); // 400 pixels from left
        tableHitbox.setLayoutY(200); // 280 pixels from top
        
        tableHitbox.setOnMouseClicked(e -> searchTable(tableHitbox));

        // Door Hitbox (Top Left)
        Rectangle doorHitbox = new Rectangle(100, 220, Color.TRANSPARENT);
        doorHitbox.setLayoutX(50);  // 50 pixels from left
        doorHitbox.setLayoutY(80);  // 80 pixels from top
        
        doorHitbox.setOnMouseClicked(e -> tryOpenLevel2Door(stage, doorHitbox, tableHitbox));


        // --- 6. Setup Overlay ---
        Pane overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);
        
        VBox hudWrapper = new VBox(hud);
        hudWrapper.setAlignment(Pos.TOP_LEFT);
        hudWrapper.setLayoutX(10);
        hudWrapper.setLayoutY(10);
        overlayPane.getChildren().add(hudWrapper);

        // Add new hitboxes
        overlayPane.getChildren().addAll(tableHitbox, doorHitbox);
        root.getChildren().add(overlayPane);

        // --- 7. Add Notification Label ---
        root.getChildren().add(notificationLabel);

        // --- 8. Show Scene and Start Timer ---
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Level 2 - The Office");
        stage.show();

        // Start timer for this level
        running = true;
        startTimer();
    }

    // ==== Level 2 Handlers ====

    /**
     * Called when the user clicks the table.
     * It adds a "CodeFound" item to the inventory and shows the number.
     */
    private void searchTable(Rectangle tableHitbox) {
        if (!inventory.contains("CodeFound")) {
            inventory.add("CodeFound");
            tableHitbox.setDisable(true); // Can't click it again
            
            
            hintLabel.setText("You found a note!"); // Update small HUD
            
            // Show the number as requested
            notificationLabel.setText("6136071");
            notificationLabel.setVisible(true);
        } else {
            hintLabel.setText("You already checked the table.");
        }
    }

    // --- REPLACED THIS ENTIRE METHOD ---

    /**
     * Called when the user clicks the door.
     * Checks if the user has "CodeFound", then shows a password dialog.
     */
    private void tryOpenLevel2Door(Stage stage, Rectangle doorHitbox, Rectangle tableHitbox) {
        // 1. Check if the user has found the code by clicking the table
        if (inventory.contains("CodeFound")) {
            
            // 2. Create the password dialog window
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Door Lock");
            dialog.setHeaderText("A small keypad is on the door.");
            dialog.setContentText("Enter Code:");

            // --- 3. ADDED CODE TO MOVE THE POPUP ---
            // Get the main window's top-left corner position
            double windowX = stage.getX();
            double windowY = stage.getY();

            // Set the dialog's position to be near the window's top-left
            dialog.setX(windowX + 50);
            dialog.setY(windowY + 50);
            // --- END OF ADDED CODE ---

            // 4. Show the dialog and wait for the user to type something
            Optional<String> result = dialog.showAndWait();

            // 5. Check what the user typed
            if (result.isPresent() && result.get().equals("6136071")) {
                
                // --- 6. CORRECT PASSWORD (WIN CONDITION) ---
                hintLabel.setText("It opened!");
                running = false; // Stop the timer
                if (animationTimer != null) animationTimer.stop();
                
                // Disable hitboxes
                doorHitbox.setDisable(true); 
                tableHitbox.setDisable(true);
                
                
                // Show "Level Complete" message
                notificationLabel.setText("Level Complete! 🎉");
                notificationLabel.setVisible(true); // Show win message

                // Swap background to the OPEN door image
                Image openDoorImage = loadImage("images/room2_open.jpg");
                if (openDoorImage != null) {
                    backgroundImageView.setImage(openDoorImage);
                } else {
                    hintLabel.setText("Unlocked! But couldn't load room2_open.jpg");
                }
                
                // Add the "Main Menu" button
                Button menuBtn = new Button("Main Menu");
                menuBtn.setOnAction(e -> showMainMenu(stage));
                
                VBox hud = (VBox) stage.getScene().lookup("#hud-level2");
                if (hud != null) {
                    hud.getChildren().add(menuBtn);
                }

            } else if (result.isPresent()) {
                // --- 7. WRONG PASSWORD ---
                hintLabel.setText("Wrong code!");
                showSimpleAlert("Lock Error", "Incorrect Code", "The code you entered is wrong. Please try again.");
            
            } else {
                // --- 8. USER CLICKED CANCEL ---
                hintLabel.setText("You backed away from the lock.");
            }

        } else {
            // --- 9. USER HASN'T FOUND THE CODE YET ---
            hintLabel.setText("It's locked. Is there a code somewhere?");
            notificationLabel.setVisible(false); // Hide any old numbers
        }
    }
    
    // --- END OF REPLACED METHOD ---


    // --- ADDED NEW HELPER METHOD ---
    /**
     * A helper method to show a simple information popup.
     */
    private void showSimpleAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // --- END OF ADDED METHOD ---


    // helper to load images
    private Image loadImage(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream("/" + filePath);
            if (is != null) {
                return new Image(is);
            } else {
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    return new Image(fis);
                } catch (Exception e) {
                    System.err.println("Failed to load image from local file: " + filePath + " - " + e.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    // Helper to load images and create ImageView
    private ImageView loadImageView(String resourcePath, String filePath, double fitW, double fitH) {
        Image img = loadImage(filePath);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(fitW);
            iv.setFitHeight(fitH);
            iv.setPreserveRatio(false); // Set to false to fill 600x400
            return iv;
        }
        return null;
    }

// ==== Timer ====
    private void startTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        animationTimer = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) lastTime = now;
                if (now - lastTime >= 1_000_000_000L) {
                    if (running && timeLeft > 0) {
                        timeLeft--;
                        timerLabel.setText("Time: " + timeLeft);
                        if (timeLeft == 0) {
                            hintLabel.setText("Time's up! You failed.");
                            notificationLabel.setText("Time's Up! You Failed.");
                            notificationLabel.setVisible(true);
                            running = false;
                            stop();
                        }
                    }
                    lastTime = now;
                }
            }
        };
        animationTimer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}