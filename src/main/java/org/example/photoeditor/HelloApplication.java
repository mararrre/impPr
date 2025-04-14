package org.example.photoeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Button btn1 = new Button("Upload image");
        Button btn2 = new Button("Choose effect");
        Button btn3 = new Button("Apply effect");
        Button btn4 = new Button("Download image");
        ComboBox<String> effects = new ComboBox<>();
        effects.getItems().addAll("ЧБ эффект", "Инверсия", "Блюр");
        effects.setPromptText("Выбрать эффект");
        effects.setVisible(false); // по умолчанию скрыт

        ImageView imageview = new ImageView();
        imageview.setFitWidth(400);
        imageview.setPreserveRatio(true);

        ImageView imageview2 = new ImageView();
        imageview2.setFitWidth(400);
        imageview2.setPreserveRatio(true);

        VBox vbox1 = new VBox();
        vbox1.getChildren().addAll(btn1, btn2, effects);

        VBox vbox2 = new VBox();
        vbox2.getChildren().addAll(btn3, btn4);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(imageview, imageview2);

        final Image[] originalImage = new Image[1];  // Локальная переменная, которая хранит изображение

        btn1.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Загружаем изображение
                originalImage[0] = new Image(selectedFile.toURI().toString());
                imageview.setImage(originalImage[0]);  // Отображаем картинку на imageview
                System.out.println("Картинка загружена: " + selectedFile.getName());
            } else {
                System.out.println("Картинка не выбрана");
            }
        });

        btn2.setOnAction(e -> {
            effects.setVisible(true);
        });

        btn3.setOnAction(e ->{
            String selectedEffect = effects.getValue();


            Image effectedImage = switch (selectedEffect) {
                case "ЧБ эффект" -> applyGrayscaleEffect(originalImage[0]);
                case "Инверсия" -> applyInvertEffect(originalImage[0]);
                case "Блюр" -> applyBlurEffect(originalImage[0]);
                default -> originalImage[0];
            };

            imageview2.setImage(effectedImage);
        });

        btn4.setOnAction(e -> {
            // Проверяем, что изображение не пустое
            if (imageview2.getImage() != null) {
                // Создаем объект FileChooser
                FileChooser fileChooser = new FileChooser();

                // Устанавливаем папку по умолчанию для диалогового окна
                File defaultDirectory = new File(System.getProperty("user.home") + "/Downloads");
                if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                    fileChooser.setInitialDirectory(defaultDirectory);
                }

                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );

                // Позволяем пользователю выбрать место и имя для сохранения
                File selectedFile = fileChooser.showSaveDialog(stage);

                if (selectedFile != null) {
                    try {
                        // Логика для сохранения файла
                        // (та же, что была описана в предыдущем ответе)
                        Image image = imageview2.getImage();
                        WritableImage writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
                        PixelReader pixelReader = image.getPixelReader();
                        writableImage.getPixelWriter().setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(),
                                pixelReader, 0, 0);

                        BufferedImage bufferedImage = new BufferedImage((int) writableImage.getWidth(), (int) writableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < writableImage.getHeight(); y++) {
                            for (int x = 0; x < writableImage.getWidth(); x++) {
                                javafx.scene.paint.Color color = writableImage.getPixelReader().getColor(x, y);
                                int argb = ((int) (color.getOpacity() * 255) << 24) |
                                        ((int) (color.getRed() * 255) << 16) |
                                        ((int) (color.getGreen() * 255) << 8) |
                                        ((int) (color.getBlue() * 255));
                                bufferedImage.setRGB(x, y, argb);
                            }
                        }

                        // Сохраняем изображение
                        ImageIO.write(bufferedImage, "PNG", selectedFile);
                        System.out.println("Изображение сохранено: " + selectedFile.getAbsolutePath());
                    } catch (IOException ex) {
                        System.out.println("Ошибка при сохранении изображения: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Сохранение отменено");
                }
            } else {
                System.out.println("Изображение для сохранения не выбрано");
            }
        });


        BorderPane borderpane = new BorderPane();
        borderpane.setLeft(vbox1);
        borderpane.setRight(vbox2);
        borderpane.setCenter(hbox);

        Scene scene = new Scene(borderpane);

        stage.setTitle("Photo Editor");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();


    }

    //методы

    private Image applyGrayscaleEffect(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                writer.setColor(x, y, new Color(gray, gray, gray, color.getOpacity()));
            }
        }
        return result;
    }

    private Image applyInvertEffect(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x, y, new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity()));
            }
        }
        return result;
    }

    private Image applyBlurEffect(Image image) {
        javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur();
        ImageView tempView = new ImageView(image);
        tempView.setEffect(blur);

        SnapshotParameters params = new SnapshotParameters();
        return tempView.snapshot(params, null);
    }

    public static void main(String[] args) {
        launch();
    }
}