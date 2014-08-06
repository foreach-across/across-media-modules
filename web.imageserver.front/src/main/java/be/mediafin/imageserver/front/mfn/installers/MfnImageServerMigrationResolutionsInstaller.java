package be.mediafin.imageserver.front.mfn.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

@Installer(description = "Installs the new set of resoutions for MFN sites", version = 1, phase = InstallerPhase.AfterModuleBootstrap, runCondition = InstallerRunCondition.AlwaysRun)
public class MfnImageServerMigrationResolutionsInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(MfnImageServerMigrationResolutionsInstaller.class);

    private static final String[] ONLINE = new String[]{"ONLINE"};
    private static final String[] DIGITAL = new String[]{"DIGITAL"};
    private static final String[] BOTH = new String[]{"ONLINE", "DIGITAL"};

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageContextService contextService;

    @InstallerMethod
    public void installResolutions() {
        // Old site - compatibility resolutions
        define(ONLINE, 0, 100, null, false);
        define(ONLINE, 107, 80, null, false);
        define(ONLINE, 170, 51, null, false);
        define(ONLINE, 178, 118, null, false);
        define(ONLINE, 89, 59, null, false);
        define(ONLINE, 44, 29, null, false);
        define(ONLINE, 1136, 639, null, false);
        define(ONLINE, 568, 320, null, false);
        define(ONLINE, 852, 479, null, false);
        define(ONLINE, 468, 310, null, false);

        // Resolution defined by both
        define(BOTH, 1024, 768, "Tablet fullscreen/Site background", true);

        // Tablet resolutions
        define(DIGITAL, 2047, 1536, "Tablet fullscreen - retina", false);
        define(DIGITAL, 200, 240, "Tablet promo header - retina", false);
        define(DIGITAL, 100, 120, "Tablet promo header", true, "teaser-default", "author-default");
        define(DIGITAL, 972, 768, "Tablet large photo template", true, "citation-default");
        define(DIGITAL, 1944, 1536, "Tablet large photo template - retina", false);

        // New site image formats
        define(ONLINE, 0, 0, "Original size", false);
        define(ONLINE, 340, 226, "Inline foto", true);
        define(ONLINE, 682, 0, "Mediamaster - 1:1", false);
        define(ONLINE, 682, 455, "Mediamaster - 3:2", true);
        define(ONLINE, 120, 80, "Thumbnail - normal", true);
        define(ONLINE, 85, 57, "Thumbnail - small", true);
        define(ONLINE, 1000, 0, "Fullscreen - 1:1", false);
        define(ONLINE, 1000, 667, "Fullscreen - 3:2", false);
        define(ONLINE, 900, 600, "Mediamaster - overlay", true, "slider-default");
        define(ONLINE, 630, 350, "Teaser - large", true, "teaser-default");
        define(ONLINE, 310, 170, "Teaser - medium", true);
        define(ONLINE, 228, 155, "Teaser - small", true);
        define(BOTH, 100, 100, "Main navigation tile", true, "author-default");
        define(ONLINE, 320, 210, "Main navigation tile - Sabato", false);
        define(ONLINE, 1440, 810, "Mediamaster: 16:9", true);
        define(ONLINE, 1425, 594, "Mediamaster - 2.4:1", true);
    }

    private void define(String[] contexts, int width, int height, String name, boolean configurable, String... tags) {
        ImageResolution existing = imageService.getResolution(width, height);

        if (existing == null) {
            LOG.debug("Creating new image resolution {} with name {}", new Dimensions(width, height), name);
            existing = new ImageResolution();
        } else {
            LOG.debug("Updating image resolution with id {} and name {}", existing.getId(), existing.getName());
        }

        existing.setWidth(width);
        existing.setHeight(height);
        existing.setConfigurable(configurable);
        existing.setName(name);
        existing.setTags(new HashSet<>(Arrays.asList(tags)));

        Collection<ImageContext> contextCollection = determineContexts(contexts);

        imageService.saveImageResolution(existing, contextCollection);
    }

    private Collection<ImageContext> determineContexts(String[] contexts) {
        Collection<ImageContext> contextCollection = new LinkedList<>();

        for (String code : contexts) {
            contextCollection.add(contextService.getByCode(code));
        }

        return contextCollection;
    }
}

