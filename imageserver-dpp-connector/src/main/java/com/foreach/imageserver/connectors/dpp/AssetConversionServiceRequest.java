package com.foreach.imageserver.connectors.dpp;

import be.persgroep.red.acr.Asset;
import be.persgroep.red.acr.Conversion;
import be.persgroep.red.acr.conversions.*;
import com.foreach.imageserver.connectors.dpp.ws.HttpWebServiceRequest;
import com.foreach.imageserver.connectors.dpp.ws.WebServiceInvoker;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;

import java.io.IOException;

public class AssetConversionServiceRequest extends HttpWebServiceRequest {
    private static final String SERVICE_NAME = "DPP assetconversie";
    private static final AssetConversionServiceInvoker.PdfAssetConversionServiceInvoker
            PDF_ASSET_CONVERSION_SERVICE_INVOKER = new AssetConversionServiceInvoker.PdfAssetConversionServiceInvoker();
    private static final AssetConversionServiceInvoker.ImageAssetConversionServiceInvoker
            IMAGE_ASSET_CONVERSION_SERVICE_INVOKER =
            new AssetConversionServiceInvoker.ImageAssetConversionServiceInvoker();

    private ByteArrayPartSource fileToConvert;
    private Asset.Format targetFormat;
    private Conversion conversion;

    private WebServiceInvoker invoker = null;

    public AssetConversionServiceRequest(ByteArrayPartSource fileToConvert,
                                         Asset.Format targetFormat,
                                         Conversion conversion) {
        this.fileToConvert = fileToConvert;
        this.targetFormat = targetFormat;
        this.conversion = conversion;

        this.invoker = getInvoker();

        // some defaults
        setHeader("Accept", "application/octet-stream");
        setHeader("Content-Type", "multipart/form-data");
        setRequestMethod("POST");

        try {
            setContent(fileToConvert.createInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to add the file to the AssetConversionServiceRequest", e);
        }
    }

    @Override
    public String getExtraParams() {
        if (conversion instanceof CropConversion) {
            return getExtraParamsForCropConversion((CropConversion) conversion);
        } else if (conversion instanceof ResizeConversion || conversion instanceof ImageConversion) {
            return getExtraParamsForResizeConversion((ImageConversion) conversion);
        } else if (conversion instanceof LowresConversion) {
            return getExtraParamsForLowresConversion((LowresConversion) conversion);
        }
        throw new IllegalArgumentException(
                "I don't know how to handle the following type of conversion: " + conversion.getClass().getName());
    }

    @Override
    public WebServiceInvoker getInvoker() {
        if (invoker != null) {
            return invoker;
        }
        String filenameLower = fileToConvert.getFileName().toLowerCase();
        String extension = filenameLower.substring(filenameLower.lastIndexOf(".") + 1);
        Asset.Format format = getFormatFromExtension(extension);
        if (format == null) {
            if ("eps".equals(extension)) {
                return new AssetConversionServiceInvoker.ImageAssetConversionServiceInvoker();
            }
        } else {
            switch (format) {
                case PDF:
                    return PDF_ASSET_CONVERSION_SERVICE_INVOKER;
                case PNG:
                case JPEG:
                case TIFF:
                    return IMAGE_ASSET_CONVERSION_SERVICE_INVOKER;
                default:

                    throw new IllegalArgumentException(
                            "No invoker registered to handle this file: " + fileToConvert.getFileName());
            }
        }
        return null;
    }

    public ByteArrayPartSource getFileToConvert() {
        return fileToConvert;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    private Asset.Format getFormatFromExtension(String extension) {
        for (Asset.Format format : Asset.Format.values()) {
            if (format.getExtension().equalsIgnoreCase(extension)) {
                return format;
            }
        }
        return null;
        //throw new IllegalArgumentException( "We can't handle files with extension " + extension );
    }

    private String getExtraParamsForResizeConversion(ImageConversion conversion) {
        StringBuilder extraParams = new StringBuilder("resize?");
        appendDefaultExtraParamsForImageConversion(extraParams, conversion);
        if (conversion instanceof ResizeConversion) {
            ResizeConversion resizeConversion = (ResizeConversion) conversion;
            // add params
            if (resizeConversion.getSize() != null) {
                ResizeConversion.Size size = resizeConversion.getSize();
                if (size instanceof Height) {
                    extraParams.append("&height=").append(((Height) size).getValue());
                } else if (size instanceof Width) {
                    extraParams.append("&width=").append(((Width) size).getValue());
                } else if (size instanceof WidthAndHeight) {
                    WidthAndHeight widthAndHeight = (WidthAndHeight) size;
                    extraParams.append("&height=").append(widthAndHeight.getHeight().getValue());
                    extraParams.append("&width=").append(widthAndHeight.getWidth().getValue());
                } else if (size instanceof MaximumWidthAndHeight) {
                    MaximumWidthAndHeight maximumWidthAndHeight = (MaximumWidthAndHeight) size;
                    extraParams.append("&maxheight=").append(maximumWidthAndHeight.getMaximumHeight().getValue());
                    extraParams.append("&maxwidth=").append(maximumWidthAndHeight.getMaximumWidth().getValue());
                } else if (size instanceof MinimumWidthAndHeight) {
                    throw new UnsupportedOperationException(
                            "The MinimumWidthAndHeight resizing conversion is not supported at the moment");
                }
            }
        }

        return extraParams.toString();
    }

    private String getExtraParamsForCropConversion(CropConversion conversion) {
        StringBuilder extraParams = new StringBuilder("crop?");
        appendDefaultExtraParamsForImageConversion(extraParams, conversion);
        // add params
        if (invoker instanceof AssetConversionServiceInvoker.PdfAssetConversionServiceInvoker) {
            // TODO get value from conversion object if the parameter exists
            extraParams.append("&pdfbox=").append("trimbox");
        }
        if (conversion.getArea() != null) {
            CropBox cropBox = (CropBox) conversion.getArea();
            if (cropBox.getHeight() > 0) {
                extraParams.append("&height=").append(cropBox.getHeight());
            }
            if (cropBox.getWidth() > 0) {
                extraParams.append("&width=").append(cropBox.getWidth());
            }
            if (cropBox.getXCoordinate() > 0) {
                extraParams.append("&x=").append(cropBox.getXCoordinate());
            }
            if (cropBox.getYCoordinate() > 0) {
                extraParams.append("&y=").append(cropBox.getYCoordinate());
            }
        }
        return extraParams.toString();
    }

    private String getExtraParamsForLowresConversion(LowresConversion conversion) {
        // TODO which params are supported for lowres?
        return "lowres";
    }

    private void appendDefaultExtraParamsForImageConversion(StringBuilder extraParams, ImageConversion conversion) {
        extraParams.append("quality=").append(conversion.getQuality());
        if (targetFormat != null) {
            extraParams.append("&format=").append(targetFormat.name());
        }
        if (conversion.getBackgroundColor() != null) {
            // TODO add backgroundcolor if supported by service
        }
        if (conversion.getColorspace() != null) {
            extraParams.append("&colorspace=").append(conversion.getColorspace().name());
        }
        if (conversion.isKeepProfiles()) {
            extraParams.append("&noprofiles=true");
        }
        if (conversion.getResolution() != null) {
            extraParams.append("&density=").append(conversion.getResolution().getHorizontalResolution());
        }
        extraParams.append("&");
    }

}
