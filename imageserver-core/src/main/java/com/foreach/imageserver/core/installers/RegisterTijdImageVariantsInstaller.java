package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.data.ApplicationDao;
import com.foreach.imageserver.core.data.ImageVariantDao;
import org.springframework.beans.factory.annotation.Autowired;

@Installer(description = "Registers image variants for Tijd website", version = 1, phase = InstallerPhase.AfterContextBoostrap)
public class RegisterTijdImageVariantsInstaller {

    @Autowired
    private ImageVariantDao imageVariantDao;

    @Autowired
    private ApplicationDao applicationDao;

    @InstallerMethod
    public void install() {
        Application mfnApplication = new Application();
        mfnApplication.setActive(true);
        mfnApplication.setCode("mfn");
        mfnApplication.setName("mfn");
        int applicationId = applicationDao.insertApplication(mfnApplication);

        //TODO: currently the aspect ratio from original Format is not used...

        createVariant(applicationId, 80, 0); //W80
        createVariant(applicationId, 88, 0); //W88
        createVariant(applicationId, 110, 0); //W110
        createVariant(applicationId, 120, 0); //W120
        createVariant(applicationId, 140, 0); //W140
        createVariant(applicationId, 170, 0); //W170
        createVariant(applicationId, 230, 0); //W230
        createVariant(applicationId, 250, 0); //W250
        createVariant(applicationId, 300, 0); //W300
        createVariant(applicationId, 600, 0); //W600
        createVariant(applicationId, 1024, 0); //W1024_4_3
        createVariant(applicationId, 2048, 0); //W1024_4_3
        createVariant(applicationId, 1024, 0); //W1024_3_2
        createVariant(applicationId, 2048, 0); //W2048_3_2
        createVariant(applicationId, 568, 237); //W568H237
        createVariant(applicationId, 1136, 473); //W1136H473
        createVariant(applicationId, 100, 120); //W100H120
        createVariant(applicationId, 200, 240); //W200H240
        createVariant(applicationId, 972, 768); //W972H768
        createVariant(applicationId, 1944, 1536); //W1944H1536
        createVariant(applicationId, 0, 80); //SLIDER_THUMB
        createVariant(applicationId, 0, 0); //SLIDER
        createVariant(applicationId, 60, 0); //SLIDER_THUMB_SMALL
        createVariant(applicationId, 80, 0); //SLIDER_THUMB_NEW_SMALL
        createVariant(applicationId, 369, 0); //W369H246
        createVariant(applicationId, 568, 0); //W568H247
        createVariant(applicationId, 568, 0); //W568H378
        createVariant(applicationId, 966, 0); //W966H282
        createVariant(applicationId, 170, 0); //W170H120
        createVariant(applicationId, 250, 0); //W250H166
        createVariant(applicationId, 87, 0); //W87H56
        createVariant(applicationId, 170, 0); //W170H51
        createVariant(applicationId, 767, 0); //SLIDER_FULL
        createVariant(applicationId, 0, 100); //H100
        createVariant(applicationId, 0, 280); //H280
        createVariant(applicationId, 0, 511); //H511
        createVariant(applicationId, 0, 76); //H76
        createVariant(applicationId, 0, 246); //H246
        createVariant(applicationId, 0, 378); //H378
    }

    private void createVariant(int applicationId, int width, int height) {

        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setWidth(width);
        imageVariant.setHeight(height);
        imageVariant.setKeepAspect(true);
        imageVariant.setStretch(false);

        try {
            imageVariantDao.insertVariant(applicationId, imageVariant);
        } catch (Exception exp) {
            //TODO:
            //Probably a unique constraint violation, let's ignore these for now
        }
    }

}
