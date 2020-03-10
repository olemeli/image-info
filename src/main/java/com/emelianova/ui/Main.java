package com.emelianova.ui;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.emelianova.imageinfo.ImageInfo;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public static class ImageInfoEntry {
        private final SimpleStringProperty name;
        private final SimpleStringProperty size;
        private final SimpleStringProperty resolution;
        private final SimpleStringProperty colorDepth;
        private final SimpleStringProperty compression;

        private ImageInfoEntry(String name, String size, String resolution,
                               String colorDepth, String compression) {
            this.name = new SimpleStringProperty(name);
            this.size = new SimpleStringProperty(size);
            this.resolution = new SimpleStringProperty(resolution);
            this.colorDepth = new SimpleStringProperty(colorDepth);
            this.compression = new SimpleStringProperty(compression);
        }

        public ImageInfoEntry(ImageInfo ii) {
            this.name = new SimpleStringProperty(ii.getName());
            if (ii.getWidth() == 0 || ii.getHeight() == 0) {
                this.size = new SimpleStringProperty("Not available");
            } else {
                this.size = new SimpleStringProperty(String.format("%d x %d px", ii.getWidth(), ii.getHeight()));
            }
            if (ii.getWidthDpi() == 0 || ii.getHeightDpi() == 0) {
                this.resolution = new SimpleStringProperty("Not available");
            } else {
                this.resolution = new SimpleStringProperty(String.format("%d x %d dpi",
                        ii.getWidthDpi(), ii.getHeightDpi()));
            }
            this.colorDepth = new SimpleStringProperty(String.valueOf(ii.getColorDepth()));
            this.compression = new SimpleStringProperty(ii.getCompression());
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getSize() {
            return size.get();
        }

        public void setSize(String size) {
            this.size.set(size);
        }

        public String getResolution() {
            return resolution.get();
        }

        public void setResolution(String resolution) {
            this.resolution.set(resolution);
        }

        public String getColorDepth() {
            return colorDepth.get();
        }

        public void setColorDepth(String colorDepth) {
            this.colorDepth.set(colorDepth);
        }

        public String getCompression() {
            return compression.get();
        }

        public void setCompression(String compression) {
            this.compression.set(compression);
        }
    }

    private TableView<ImageInfoEntry> table = new TableView<>();
    private final ObservableList<ImageInfoEntry> iieList = FXCollections.observableArrayList();

    private void readDirectory(File dir) {
        for (final File file : dir.listFiles()) {
            if (!file.isDirectory())
                readFile(file);
        }
    }

    private void readFile(File file) {
        try {
            ImageInfo ii = ImageInfo.readImageInfo(file);
            if (ii != null) {
                iieList.add(new ImageInfoEntry(ii));
            }
        } catch (ImageProcessingException | IOException | MetadataException e) {
            e.printStackTrace();
        }
    }

    public void start(Stage primaryStage) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        Button dirButton = new Button("Select Directory");
        dirButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory == null) return;

            iieList.clear();
            readDirectory(selectedDirectory);
        });

        FileChooser fileChooser = new FileChooser();

        Button fileButton = new Button("Select File");
        fileButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile == null) return;

            iieList.clear();
            readFile(selectedFile);
        });

        table.setEditable(true);

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setMinWidth(200);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<ImageInfoEntry, String>("name"));

        TableColumn sizeCol = new TableColumn("Size");
        sizeCol.setMinWidth(100);
        sizeCol.setCellValueFactory(
                new PropertyValueFactory<ImageInfoEntry, String>("size"));

        TableColumn resolutionCol = new TableColumn("Resolution");
        resolutionCol.setMinWidth(100);
        resolutionCol.setCellValueFactory(
                new PropertyValueFactory<ImageInfoEntry, String>("resolution"));

        TableColumn colorDepthCol = new TableColumn("Color Depth");
        colorDepthCol.setMinWidth(100);
        colorDepthCol.setCellValueFactory(
                new PropertyValueFactory<ImageInfoEntry, String>("colorDepth"));

        TableColumn compressionCol = new TableColumn("Compression");
        compressionCol.setMinWidth(200);
        compressionCol.setCellValueFactory(
                new PropertyValueFactory<ImageInfoEntry, String>("compression"));

        table.setItems(iieList);
        table.getColumns().addAll(nameCol, sizeCol, resolutionCol, colorDepthCol, compressionCol);


        HBox hBox = new HBox(dirButton, fileButton);
        VBox vBox = new VBox(hBox, table);
        Scene scene = new Scene(vBox, 700, 300);

        primaryStage.setScene(scene);
        primaryStage.show();


    }
}
