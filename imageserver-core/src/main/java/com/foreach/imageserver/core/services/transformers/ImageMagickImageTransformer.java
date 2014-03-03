package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
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
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

@Component
public class ImageMagickImageTransformer implements ImageTransformer {
    public static final int GS_MAX_DENSITY = 1200;
    public static final int GS_DEFAULT_DENSITY = 72;
    public static final int GS_DENSITY_STEP = 300;
    public static final String ALPHA_BACKGROUND = "white";

    private static final Logger LOG = LoggerFactory.getLogger(ImageMagickImageTransformer.class);

    private final int priority;
    private final boolean ghostScriptEnabled;

    @Value("${transformer.imagemagick.enabled}")
    private boolean enabled;

    @Override
    public String getName() {
        return "imagemagick";
    }

    @Autowired
    public ImageMagickImageTransformer(@Value("${transformer.imagemagick.priority}") int priority,
                                       @Value("${transformer.imagemagick.path}") String imageMagickPath,
                                       @Value("${transformer.imagemagick.ghostscript}") boolean ghostScriptEnabled) {
        this.priority = priority;
        this.ghostScriptEnabled = ghostScriptEnabled;

        ProcessStarter.setGlobalSearchPath(new File(imageMagickPath).getAbsolutePath());
    }

    @Override
    public ImageTransformerPriority canExecute(ImageTransformerAction action) {
        ImageType imageType = action.getImageFile().getImageType();

        if ((imageType == ImageType.EPS || imageType == ImageType.PDF) && !ghostScriptEnabled) {
            return ImageTransformerPriority.UNABLE;
        }

        return action instanceof ImageModifyAction ? ImageTransformerPriority.PREFERRED : ImageTransformerPriority.FALLBACK;
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
        ImageFile imageFile = action.getImageFile();

        InputStream stream = null;
        try {
            stream = imageFile.openContentStream();

            Info info = new Info("-", stream, false);
            action.setResult(new Dimensions(info.getImageWidth(), info.getImageHeight()));
        } catch (Exception e) {
            LOG.error("Failed to get image dimensions {}: {}", action, e);
            throw new ImageModificationException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void executeModification(ImageModifyAction action) {
        try {
            ImageModifier modifier = action.getModifier();

            ConvertCmd cmd = new ConvertCmd();

            IMOperation op = new IMOperation();
            Dimensions appliedDensity = setDensityIfRequired(op, action.getImageFile(), modifier);
            op.addImage("-");

            if (shouldRemoveTransparency(action.getImageFile().getImageType(), modifier.getOutput())) {
                op.background(ALPHA_BACKGROUND);
                op.flatten();
            }

            if (modifier.hasCrop()) {
                Crop crop = applyDensity(modifier.getCrop(), appliedDensity);
                op.crop(crop.getWidth(), crop.getHeight(), crop.getX(), crop.getY());
            }

            op.resize(modifier.getWidth(), modifier.getHeight(), "!");
            op.colorspace("sRGB");
            op.addImage(modifier.getOutput().getExtension() + ":-");

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            cmd.setInputProvider(new Pipe(action.getImageFile().openContentStream(), null));
            cmd.setOutputConsumer(new Pipe(null, os));

            cmd.run(op);

            byte[] bytes = os.toByteArray();
            ImageFile result =
                    new ImageFile(action.getModifier().getOutput(), bytes.length, new ByteArrayInputStream(bytes));
            action.setResult(result);
        } catch (Exception e) {
            LOG.error("Failed to apply modification {}: {}", action, e);
            throw new ImageModificationException(e);
        }
    }

    private Crop applyDensity(Crop crop, Dimensions density) {
        if (density != null) {
            double widthFactor = (double) density.getWidth() / GS_DEFAULT_DENSITY;
            double heightFactor = (double) density.getHeight() / GS_DEFAULT_DENSITY;

            return new Crop(Double.valueOf(widthFactor * crop.getX()).intValue(),
                    Double.valueOf(heightFactor * crop.getY()).intValue(),
                    Double.valueOf(widthFactor * crop.getWidth()).intValue(),
                    Double.valueOf(heightFactor * crop.getHeight()).intValue());
        }

        return crop;
    }

    private Dimensions setDensityIfRequired(IMOperation operation, ImageFile original, ImageModifier modifier) {
        if (original.getImageType().isScalable()) {
            Dimensions density = modifier.getDensity();

            if (density != null && !Dimensions.EMPTY.equals(
                    density) && (density.getHeight() > 1 || density.getWidth() > 1)) {
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

    private boolean shouldRemoveTransparency(ImageType original, ImageType requested) {
        return original.hasTransparency() && !requested.hasTransparency();
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
