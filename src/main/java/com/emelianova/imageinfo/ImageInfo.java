package com.emelianova.imageinfo;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.bmp.BmpHeaderDescriptor;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.jfif.JfifDirectory;
import com.drew.metadata.jpeg.JpegCommentDirectory;
import com.drew.metadata.jpeg.JpegDescriptor;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.pcx.PcxDirectory;
import com.drew.metadata.png.PngDescriptor;
import com.drew.metadata.png.PngDirectory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ImageInfo {

    public void setName(String name) {
        this.name = name;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidthDpi(int widthDpi) {
        this.widthDpi = widthDpi;
    }

    public void setHeightDpi(int heightDpi) {
        this.heightDpi = heightDpi;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidthDpi() {
        return widthDpi;
    }

    public int getHeightDpi() {
        return heightDpi;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public String getCompression() {
        return compression;
    }

    private String name;
    private int width;
    private int height;
    private int widthDpi;
    private int heightDpi;
    private int colorDepth; // bits per pixel
    private String compression;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private String addAnsiColor(String s, boolean cond) {
        if (cond) {
            return ANSI_RED + s + ANSI_RESET;
        }
        return s;
    }

    @Override
    public String toString() {
        return addAnsiColor(String.format("%-30s %-20s %-20s %-10s %-20s",
                name, String.format("%d x %d px", width, height), String.format("%d x %d dpi", widthDpi, heightDpi),
                String.valueOf(colorDepth), compression),
                width == 0 || height == 0 || widthDpi == 0 || heightDpi == 0 ||
                        colorDepth == 0 || compression == null || compression.equals(""));
    }

    public static ImageInfo readTIFFInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        ExifIFD0Directory exifIFD0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(exifIFD0Dir);

        ii.setWidth(exifIFD0Dir.getInt(ExifIFD0Directory.TAG_IMAGE_WIDTH));
        ii.setHeight(exifIFD0Dir.getInt(ExifIFD0Directory.TAG_IMAGE_HEIGHT));
        ii.setWidthDpi(exifIFD0Dir.getInt(ExifIFD0Directory.TAG_X_RESOLUTION));
        ii.setHeightDpi(exifIFD0Dir.getInt(ExifIFD0Directory.TAG_Y_RESOLUTION));

        int[] bitsPerSamples = exifIFD0Dir.getIntArray(ExifIFD0Directory.TAG_BITS_PER_SAMPLE);
        int colorDepth = 0;
        for (int sample : bitsPerSamples) {
            colorDepth += sample;
        }
        ii.setColorDepth(colorDepth);

        ii.setCompression(descriptor.getCompressionDescription());

        return ii;
    }

    public static ImageInfo readJPEGInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        JpegDescriptor descriptor = new JpegDescriptor(jpegDirectory);

        ii.setWidth(jpegDirectory.getImageWidth());
        ii.setHeight(jpegDirectory.getImageHeight());
        ii.setColorDepth(jpegDirectory.getNumberOfComponents() * jpegDirectory.getInt(JpegDirectory.TAG_DATA_PRECISION));
        ii.setCompression(descriptor.getImageCompressionTypeDescription());

        if (metadata.containsDirectoryOfType(JfifDirectory.class)) {
            JfifDirectory dir = metadata.getFirstDirectoryOfType(JfifDirectory.class);
            ii.setWidthDpi(dir.getResX());
            ii.setHeightDpi(dir.getResY());
        } else if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
            ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            ii.setWidthDpi(dir.getInt(ExifIFD0Directory.TAG_X_RESOLUTION));
            ii.setHeightDpi(dir.getInt(ExifIFD0Directory.TAG_Y_RESOLUTION));
        }

        return ii;
    }

    public static ImageInfo readPCXInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        PcxDirectory pcxDirectory = metadata.getFirstDirectoryOfType(PcxDirectory.class);

        ii.setWidth(pcxDirectory.getInt(PcxDirectory.TAG_XMAX) - pcxDirectory.getInt(PcxDirectory.TAG_XMIN));
        ii.setHeight(pcxDirectory.getInt(PcxDirectory.TAG_YMAX) - pcxDirectory.getInt(PcxDirectory.TAG_YMIN));
        ii.setWidthDpi(pcxDirectory.getInt(PcxDirectory.TAG_HORIZONTAL_DPI));
        ii.setHeightDpi(pcxDirectory.getInt(PcxDirectory.TAG_VERTICAL_DPI));
        ii.setColorDepth(pcxDirectory.getInt(PcxDirectory.TAG_BITS_PER_PIXEL));

        ii.setCompression("RLE");

        return ii;
    }

    public static ImageInfo readGIFInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        GifHeaderDirectory gifHeaderDirectory = metadata.getFirstDirectoryOfType(GifHeaderDirectory.class);

        ii.setWidth(gifHeaderDirectory.getInt(GifHeaderDirectory.TAG_IMAGE_WIDTH));
        ii.setHeight(gifHeaderDirectory.getInt(GifHeaderDirectory.TAG_IMAGE_HEIGHT));
        ii.setColorDepth(gifHeaderDirectory.getInt(GifHeaderDirectory.TAG_BITS_PER_PIXEL));

        ii.setCompression("LZW");

        return ii;
    }

    public static ImageInfo readBMPInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        BmpHeaderDirectory bmpHeaderDirectory = metadata.getFirstDirectoryOfType(BmpHeaderDirectory.class);

        ii.setWidth(bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH));
        ii.setHeight(bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT));
        ii.setWidthDpi((int) (bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_X_PIXELS_PER_METER) * 0.0254));
        ii.setHeightDpi((int) (bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_Y_PIXELS_PER_METER) * 0.0254));
        ii.setColorDepth(bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_BITS_PER_PIXEL));
        ii.setCompression(bmpHeaderDirectory.getCompression().toString());

        return ii;
    }

    public static ImageInfo readPNGInfo(final Metadata metadata, final ImageInfo ii) throws MetadataException {
        for (Directory directory : metadata.getDirectories()) {
            if (directory.containsTag(PngDirectory.TAG_IMAGE_WIDTH) && ii.width == 0) {
                ii.setWidth(directory.getInt(PngDirectory.TAG_IMAGE_WIDTH));
            }
            if (directory.containsTag(PngDirectory.TAG_IMAGE_HEIGHT) && ii.height == 0) {
                ii.setHeight(directory.getInt(PngDirectory.TAG_IMAGE_HEIGHT));
            }
            if (directory.containsTag(PngDirectory.TAG_BITS_PER_SAMPLE) && ii.colorDepth == 0) {
                ii.setColorDepth(directory.getInt(PngDirectory.TAG_BITS_PER_SAMPLE));
            }
            if (directory.containsTag(PngDirectory.TAG_PIXELS_PER_UNIT_X) && ii.widthDpi == 0) {
                ii.setWidthDpi((int) (directory.getInt(PngDirectory.TAG_PIXELS_PER_UNIT_X) * 0.0254));
            }
            if (directory.containsTag(PngDirectory.TAG_PIXELS_PER_UNIT_Y) && ii.heightDpi == 0) {
                ii.setHeightDpi((int) (directory.getInt(PngDirectory.TAG_PIXELS_PER_UNIT_Y) * 0.0254));
            }
            if (directory.containsTag(PngDirectory.TAG_COMPRESSION_TYPE) &&
                    (ii.compression == null || ii.compression.equals(""))) {
                PngDescriptor descriptor = new PngDescriptor((PngDirectory) directory);
                ii.setCompression(descriptor.getCompressionTypeDescription());
            }
        }

        return ii;
    }

    public static ImageInfo readImageInfo(final File image)
            throws IOException, ImageProcessingException, MetadataException {
        ImageInfo ii = new ImageInfo();
        ii.setName(image.getName());

        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(image);
        } catch (ImageProcessingException e) {
            return null;
        }
        FileTypeDirectory fileTypeDir = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
        String fileType = fileTypeDir.getString(FileTypeDirectory.TAG_DETECTED_FILE_TYPE_NAME);

        if (fileType.equals("TIFF")) {
            ii = readTIFFInfo(metadata, ii);
        } else if (fileType.equals("JPEG")) {
            ii = readJPEGInfo(metadata, ii);
        } else if (fileType.equals("PCX")) {
            ii = readPCXInfo(metadata, ii);
        } else if (fileType.equals("GIF")) {
            ii = readGIFInfo(metadata, ii);
        } else if (fileType.equals("BMP")) {
            ii = readBMPInfo(metadata, ii);
        } else if (fileType.equals("PNG")) {
            ii = readPNGInfo(metadata, ii);
        }

        return ii;
    }
}
