package com.foreach.imageserver.connectors.dpp;

import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Exposed
public class DioContentLookupRepository implements ImageLookupRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DioContentLookupRepository.class);

    private final String serverUrl;
    private final String login;
    private final String password;

    public DioContentLookupRepository(String serverUrl, String login, String password) {
        this.serverUrl = serverUrl;
        this.login = login;
        this.password = password;
        LOG.info("Registered DioContentLookupRepository on endpoint {}", serverUrl);
    }

    @Override
    public String getCode() {
        return "dc";
    }

    @Override
    public RepositoryLookupResult fetchImage(Map<String, String> params) {
        RepositoryLookupResult lookupResult = new RepositoryLookupResult();

        try {
            DioContentClient client = new DefaultRestDioContentClient(serverUrl, login, password);
            String idAsString = params.get("id");
            if (idAsString == null) {
                lookupResult.setStatus(RepositoryLookupStatus.ERROR);
                return lookupResult;
            }
            int dcId;
            try {
                dcId = Integer.valueOf(idAsString);
            } catch (NumberFormatException exp) {
                lookupResult.setStatus(RepositoryLookupStatus.ERROR);
                return lookupResult;
            }
            LOG.debug("Requesting ORIGINAL image with dio:content id {}", dcId);

            Attachment attachment = client.getAttachmentWithRole(dcId, AttachmentRole.ORIGINAL);
            ImageType imageType = ImageType.getForContentType(attachment.getFileInfo().getMimeType());

            ByteArrayOutputStream data = new ByteArrayOutputStream();

            try {
                client.downloadAttachment(attachment.getId(), data);
                data.flush();
            } finally {
                IOUtils.closeQuietly(data);
            }


            lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
            lookupResult.setImageType(imageType);
            lookupResult.setContent(new ByteArrayInputStream(data.toByteArray()));
        } catch (Exception e) {
            lookupResult.setStatus(RepositoryLookupStatus.ERROR);
        }
        return lookupResult;
    }
}
