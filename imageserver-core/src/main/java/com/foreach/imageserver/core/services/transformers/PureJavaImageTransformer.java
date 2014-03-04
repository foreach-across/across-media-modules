package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@org.springframework.stereotype.Component
public class PureJavaImageTransformer implements ImageTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(PureJavaImageTransformer.class);

    private final int priority;

    @Value("${transformer.java.enabled}")
    private boolean enabled;

    @Autowired
    public PureJavaImageTransformer(@Value("${transformer.java.priority}") int priority) {
        this.priority = priority;
    }

    @Override
    public String getName() {
        return "java";
    }

    @Override
    public ImageTransformerPriority canExecute(ImageTransformerAction action) {
        ImageType imageType = action.getImageFile().getImageType();

        switch (imageType) {
            case JPEG:
            case GIF:
            case PNG:
            case TIFF:
            case PDF:
                return ImageTransformerPriority.PREFERRED;
            case SVG:
            case EPS:
                return ImageTransformerPriority.UNABLE;
        }

        // Current implementation is very dodgy, make sure modifications only go through imagemagick
        return action instanceof ImageModifyAction ? ImageTransformerPriority.UNABLE : ImageTransformerPriority.FALLBACK;
    }

    @Override
    public void execute(ImageTransformerAction action) {
        if (action instanceof ImageModifyAction) {
            executeModification((ImageModifyAction) action);
        } else if (action instanceof ImageCalculateDimensionsAction) {
            calculateDimensions((ImageCalculateDimensionsAction) action);
        }
    }

    private void calculateDimensions(ImageCalculateDimensionsAction action) {
        InputStream stream = null;

        try {
            stream = action.getImageFile().openContentStream();

            if (action.getImageFile().getImageType() == ImageType.PDF) {
                PDDocument pdf = null;

                try {
                    pdf = PDDocument.load(stream);
                    PDPage firstPage = (PDPage) pdf.getDocumentCatalog().getAllPages().get(0);
                    PDRectangle cropBox = firstPage.findMediaBox();

                    action.setResult(
                            new Dimensions(Math.round(cropBox.getWidth()), Math.round(cropBox.getHeight())));
                } finally {
                    if (pdf != null) {
                        pdf.close();
                    }
                }

            } else {
                ImageInfo imageInfo = Imaging.getImageInfo(stream, "");
                action.setResult(new Dimensions(imageInfo.getWidth(), imageInfo.getHeight()));
            }
        } catch (Exception e) {
            LOG.error("Failed to calculate image dimensions {}: {}", action, e);
            throw new ImageModificationException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void executeModification(ImageModifyAction action) {
        if (true) {
            throw new RuntimeException("Unfinished, I should use the given crop");
        }
        try {
            ImageFile original = action.getImageFile();
            ImageModification modifier = action.getVariant();

            BufferedImage bufferedImage = readImage(original);

            bufferedImage = getScaledInstance(bufferedImage, modifier.getVariant().getWidth(), modifier.getVariant().getHeight(),
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, original.getImageType().getExtension(), os);

            byte[] content = os.toByteArray();

            action.setResult(
                    new ImageFile(original.getImageType(), content.length, new ByteArrayInputStream(content)));
        } catch (Exception e) {
            LOG.error("exception applying transform {} ", e);
        }
    }

    private static BufferedImage readImage(ImageFile imageFile) throws IOException {
        byte[] bytes = IOUtils.toByteArray(imageFile.openContentStream());

        try {
            return Imaging.getBufferedImage(bytes);
        } catch (Exception e) {
            LOG.debug("Couldn't read image using commons imaging library: {}", imageFile);
        }

        ImageInputStream is = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));
        ImageReader reader = ImageIO.getImageReaders(is).next();
        try {
            reader.setInput(is);
            ImageReadParam param = reader.getDefaultReadParam();

            ImageTypeSpecifier typeToUse = null;

            for (Iterator<ImageTypeSpecifier> i = reader.getImageTypes(0); i.hasNext(); ) {
                ImageTypeSpecifier type = i.next();
                if (type.getColorModel().getColorSpace().isCS_sRGB()) {
                    typeToUse = type;
                }
            }
            if (typeToUse != null) {
                param.setDestinationType(typeToUse);
            }

            return reader.read(0, param);
        } finally {
            // guarantee we close the reader here, even if we throw an IOException
            // since readers keep the entire image in the buffer this could lead to out of memory errors otherwise
            reader.dispose();
            // also we intentionally do not close the imageinputstream here, since it was passed to us and maybe it will
            // be used later on.
        }
    }

    private BufferedImage getScaledInstance(BufferedImage img,
                                            int targetWidth,
                                            int targetHeight,
                                            Object interpolationHint,
                                            boolean preserveAlpha) {
        boolean hasPossibleAlphaChannel = img.getTransparency() != Transparency.OPAQUE;

        // rescale while ignoring the preserveAlpha flag, otherwise we lose the transparency at this point
        int imageTypeForScaling = hasPossibleAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage result = img;
        int w = img.getWidth();
        int h = img.getHeight();
        do {
            if (w > targetWidth) {
                w = Math.max(w / 2, targetWidth);
            }
            if (h > targetHeight) {
                h = Math.max(h / 2, targetHeight);
            }

            BufferedImage tmp = new BufferedImage(w, h, imageTypeForScaling);

            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
            g2.drawImage(result, 0, 0, w, h, null);
            g2.dispose();
            result = tmp;
        }
        while (w > targetWidth || h > targetHeight);

        return hasPossibleAlphaChannel && !preserveAlpha ? getFlattenedBufferedImageWithWhiteBG(result) : result;
    }

    // add white background if we don't want to preserve the alpha channel
    private BufferedImage getFlattenedBufferedImageWithWhiteBG(BufferedImage result) {
        if (result.getTransparency() == Transparency.OPAQUE) {
            // no alpha channel, so no need to transform anything
            return result;
        }
        // create new buffered image with a white background and draw the result onto it
        BufferedImage res = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = res.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, result.getWidth(), result.getHeight());
        graphics.drawImage(result, 0, 0, null);
        graphics.dispose();
        return res;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
