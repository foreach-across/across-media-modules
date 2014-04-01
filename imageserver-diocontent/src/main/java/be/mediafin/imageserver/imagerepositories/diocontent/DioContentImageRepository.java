package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import be.mediafin.imageserver.imagerepositories.diocontent.data.DioContentImageParametersDao;
import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@Exposed
@Conditional(DioContentRepositoryConditional.class)
public class DioContentImageRepository implements ImageRepository {

    public static final String CODE = "dc";

    @Value("${imagerepository.diocontent.serverUrl}")
    private String serverUrl;

    @Value("${imagerepository.diocontent.username}")
    private String username;

    @Value("${imagerepository.diocontent.password}")
    private String password;

    @Autowired
    private DioContentImageParametersDao dioContentImageParametersDao;

    @Autowired
    private ImageTransformService imageTransformService;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public RetrievedImage retrieveImage(int imageId, Map<String, String> repositoryParameters) {
        int dioContentId = extractId(repositoryParameters);

        InMemoryImageSource imageSource = retrieveImageFromDioContent(dioContentId);
        Dimensions dimensions = imageTransformService.computeDimensions(imageSource.stream());

        DioContentImageParameters imageParameters = new DioContentImageParameters();
        imageParameters.setImageId(imageId);
        imageParameters.setDioContentId(dioContentId);
        dioContentImageParametersDao.insert(imageParameters);

        return new RetrievedImage(dimensions, imageSource.getImageType(), imageSource.getImageBytes());
    }

    @Override
    public byte[] retrieveImage(int imageId) {
        // TODO We blindly assume that the image has not changed since we last downloaded it.
        DioContentImageParameters imageParameters = dioContentImageParametersDao.getById(imageId);
        InMemoryImageSource imageSource = retrieveImageFromDioContent(imageParameters.getDioContentId());
        return imageSource.getImageBytes();
    }

    private InMemoryImageSource retrieveImageFromDioContent(int dioContentId) {
        ByteArrayOutputStream data = null;
        try {
            DioContentClient client = new DefaultRestDioContentClient(serverUrl, username, password);
            Attachment attachment = client.getAttachmentWithRole(dioContentId, AttachmentRole.ORIGINAL);
            ImageType imageType = ImageType.getForContentType(attachment.getFileInfo().getMimeType());

            data = new ByteArrayOutputStream();
            client.downloadAttachment(attachment.getId(), data);
            data.flush();

            return new InMemoryImageSource(imageType, data.toByteArray());
        } catch (Exception e) {
            throw new ImageCouldNotBeRetrievedException(e);
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    private int extractId(Map<String, String> repositoryParameters) {
        if (!repositoryParameters.containsKey("id")) {
            throw new MissingRepositoryParameterException("Missing repository parameter 'id'.");
        }

        try {
            return Integer.valueOf(repositoryParameters.get("id"));
        } catch (NumberFormatException e) {
            throw new MissingRepositoryParameterException("Repository parameter 'id' should be an integer number.");
        }
    }

}
