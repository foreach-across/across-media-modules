package com.foreach.imageserver.connectors.dpp;


import be.persgroep.red.acr.Asset;
import be.persgroep.red.acr.Conversion;
import com.foreach.imageserver.connectors.dpp.ws.HttpWebServiceRequest;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.springframework.stereotype.Service;

@Service
public class AssetConversionServiceImpl implements AssetConversionService {
    @Override
    public byte[] convert( ByteArrayPartSource file, Asset.Format targetFormat, Conversion conversion ) {
        HttpWebServiceRequest request = new AssetConversionServiceRequest( file, targetFormat, conversion );
        AssetConversionServiceResponse response = (AssetConversionServiceResponse) request.invoke();
        return response.getResponse();
    }
}