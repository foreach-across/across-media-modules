package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import be.mediafin.imageserver.imagerepositories.diocontent.data.DioContentImageParametersDao;
import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageParameters;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.*;
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
    public ImageParameters getImageParameters(int id) {
        return dioContentImageParametersDao.getById(id);
    }

    @Override
    public RetrievedImage retrieveImage(int imageId, Map<String, String> repositoryParameters) {
        int dioContentId = extractId(repositoryParameters);

        ByteArrayOutputStream data = null;
        try {
            DioContentClient client = new DefaultRestDioContentClient(serverUrl, username, password);
            Attachment attachment = client.getAttachmentWithRole(dioContentId, AttachmentRole.ORIGINAL);
            ImageType imageType = ImageType.getForContentType(attachment.getFileInfo().getMimeType());

            data = new ByteArrayOutputStream();
            client.downloadAttachment(attachment.getId(), data);
            data.flush();

            byte[] imageBytes = data.toByteArray();
            Dimensions dimensions = imageTransformService.computeDimensions(imageType, imageBytes);

            DioContentImageParameters imageParameters = new DioContentImageParameters();
            imageParameters.setImageId(imageId);
            imageParameters.setDioContentId(dioContentId);
            imageParameters.setDimensions(dimensions);
            imageParameters.setImageType(imageType);
            dioContentImageParametersDao.insert(imageParameters);

            return new RetrievedImage(imageParameters, imageBytes);
        } catch (Exception e) {
            throw new ImageCouldNotBeRetrievedException(e);
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    @Override
    public boolean parametersAreEqual(int imageId, Map<String, String> repositoryParameters) {
        DioContentImageParameters storedImage = dioContentImageParametersDao.getById(imageId);
        int suppliedId = extractId(repositoryParameters);
        return (storedImage.getDioContentId() == suppliedId);
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
