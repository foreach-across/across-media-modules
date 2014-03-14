package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;
import org.apache.commons.io.IOUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

// TODO Support for vector formats using Ghostscript is untested.
@Component
@Conditional(ImageMagickImageTransformerConditional.class)
public class ImageMagickImageTransformer implements ImageTransformer {
    public static final int GS_MAX_DENSITY = 1200;
    public static final int GS_DEFAULT_DENSITY = 72;
    public static final int GS_DENSITY_STEP = 300;
    public static final String ALPHA_BACKGROUND = "white";

    private static final Logger LOG = LoggerFactory.getLogger(ImageMagickImageTransformer.class);

    private final int order;
    private final boolean ghostScriptEnabled;

    @Autowired
    public ImageMagickImageTransformer(@Value("${transformer.imagemagick.priority}") int order,
                                       @Value("${transformer.imagemagick.path}") String imageMagickPath,
                                       @Value("${transformer.imagemagick.ghostscript}") boolean ghostScriptEnabled,
                                       @Value("${transformer.imagemagick.usegraphicsmagick}") boolean useGraphicsMagick) {
        this.order = order;
        this.ghostScriptEnabled = ghostScriptEnabled;

        ProcessStarter.setGlobalSearchPath(new File(imageMagickPath).getAbsolutePath());

        /**
         * I'd rather not set a global system property for this, but this is the only way to have Info use
         * GraphicsMagick.
         */
        if (useGraphicsMagick) {
            System.setProperty("im4java.useGM", "true");
        }
    }

    @Override
    public ImageTransformerPriority canExecute(ImageCalculateDimensionsAction action) {
        return canExecute(action.getImageSource().getImageType());
    }

    @Override
    public ImageTransformerPriority canExecute(ImageModifyAction action) {
        return canExecute(action.getSourceImageSource().getImageType());
    }

    @Override
    public Dimensions execute(ImageCalculateDimensionsAction action) {
        if (canExecute(action) == ImageTransformerPriority.UNABLE) {
            throw new UnsupportedOperationException();
        }

        ImageSource imageSource = action.getImageSource();
        InputStream stream = null;
        try {
            stream = imageSource.getImageStream();
            Info info = new Info("-", stream, false);
            return new Dimensions(info.getImageWidth(), info.getImageHeight());
        } catch (Exception e) {
            LOG.error("Failed to get image dimensions: {}", e);
            throw new ImageModificationException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Override
    public ImageSource execute(ImageModifyAction action) {
        if (canExecute(action) == ImageTransformerPriority.UNABLE) {
            throw new UnsupportedOperationException();
        }

        InputStream imageStream = null;
        ByteArrayOutputStream os = null;
        try {
            ConvertCmd cmd = new ConvertCmd();

            IMOperation op = new IMOperation();
            Dimensions appliedDensity = setDensityIfRequired(op, action);
            op.addImage("-");

            if (shouldRemoveTransparency(action)) {
                op.background(ALPHA_BACKGROUND);
                op.flatten();
            }

            Crop crop = applyDensity(action.getCrop(), appliedDensity);
            op.crop(crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY());

            op.resize(action.getOutputDimensions().getWidth(), action.getOutputDimensions().getHeight(), "!");
            op.colorspace("RGB");
            op.addImage(action.getOutputType().getExtension() + ":-");

            imageStream = action.getSourceImageSource().getImageStream();
            os = new ByteArrayOutputStream();

            cmd.setInputProvider(new Pipe(imageStream, null));
            cmd.setOutputConsumer(new Pipe(null, os));

            cmd.run(op);

            byte[] bytes = os.toByteArray();
            return new ImageSource(action.getOutputType(), new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            LOG.error("Failed to apply modification: {}", e);
            throw new ImageModificationException(e);
        } finally {
            IOUtils.closeQuietly(imageStream);
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    private ImageTransformerPriority canExecute(ImageType imageType) {
        if ((imageType == ImageType.EPS || imageType == ImageType.PDF) && !ghostScriptEnabled) {
            return ImageTransformerPriority.UNABLE;
        } else {
            return ImageTransformerPriority.PREFERRED;
        }
    }

    private Crop applyDensity(Crop crop, Dimensions density) {
        if (density != null) {
            double widthFactor = (double) density.getWidth() / GS_DEFAULT_DENSITY;
            double heightFactor = (double) density.getHeight() / GS_DEFAULT_DENSITY;

            return new Crop(
                    Double.valueOf(widthFactor * crop.getX()).intValue(),
                    Double.valueOf(heightFactor * crop.getY()).intValue(),
                    Double.valueOf(widthFactor * crop.getWidth()).intValue(),
                    Double.valueOf(heightFactor * crop.getHeight()).intValue());
        }

        return crop;
    }

    private Dimensions setDensityIfRequired(IMOperation operation, ImageModifyAction action) {
        if (action.getSourceImageSource().getImageType().isScalable()) {
            Dimensions density = action.getDensity();

            if (density != null && (density.getHeight() > 1 || density.getWidth() > 1)) {
                int horizontalDensity = calculateDensity(density.getWidth());
                int verticalDensity = calculateDensity(density.getHeight());

                LOG.debug("Applying density {}x{}", horizontalDensity, verticalDensity);
                operation.density(horizontalDensity, verticalDensity);

                return new Dimensions(horizontalDensity, verticalDensity);
            }
        }

        return null;
    }

    private int calculateDensity(int multiplier) {
        int raw = Math.min(GS_MAX_DENSITY, GS_DEFAULT_DENSITY * Math.max(multiplier, 1));
        int times = raw / GS_DENSITY_STEP;
        int remainder = raw % GS_DENSITY_STEP;

        return remainder == 0 ? raw : Math.min(GS_MAX_DENSITY, (times + 1) * GS_DENSITY_STEP);
    }

    private boolean shouldRemoveTransparency(ImageModifyAction action) {
        return action.getSourceImageSource().getImageType().hasTransparency() && !action.getOutputType().hasTransparency();
    }
}
