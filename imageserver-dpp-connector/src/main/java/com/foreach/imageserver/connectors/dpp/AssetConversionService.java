package com.foreach.imageserver.connectors.dpp;

import be.persgroep.red.acr.Asset;
import be.persgroep.red.acr.Conversion;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;

/**
 * @author niels
 * @since 26/02/13
 */
public interface AssetConversionService {

    String BASE_URL = "http://assetconversie.persgroep.be/rest/";
    String PDF_CONVERSION_URL = BASE_URL + "pdf/";
    String IMG_CONVERSION_URL = BASE_URL + "image/";

    String RESPONSE_ENCODING = "application/octet-stream";

    byte[] convert(ByteArrayPartSource fileToConvert, Asset.Format targetFormat, Conversion conversion);
}
