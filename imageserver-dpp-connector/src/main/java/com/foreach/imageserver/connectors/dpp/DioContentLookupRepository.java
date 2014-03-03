package com.foreach.imageserver.connectors.dpp;

import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.exceptions.RepositoryLookupException;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
    public boolean isValidURI(String uri) {
        return StringUtils.startsWithIgnoreCase(uri, "dc:");
    }

    @Override
    public RepositoryLookupResult fetchImage(String uri) {
        try {
            DioContentClient client = new DefaultRestDioContentClient(serverUrl, login, password);
            int dcId = Integer.valueOf(StringUtils.replace(uri, "dc:", ""));

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

            RepositoryLookupResult lookupResult = new RepositoryLookupResult();
            lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
            lookupResult.setImageType(imageType);
            lookupResult.setContent(new ByteArrayInputStream(data.toByteArray()));

            return lookupResult;
        } catch (Exception e) {
            throw new RepositoryLookupException(e);
        }
    }
}
