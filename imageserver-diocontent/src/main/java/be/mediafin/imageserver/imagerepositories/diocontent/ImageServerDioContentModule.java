package be.mediafin.imageserver.imagerepositories.diocontent;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;

import java.util.Set;

public class ImageServerDioContentModule extends AcrossModule {

    public ImageServerDioContentModule() {
        setExposeFilter(new AnnotationBeanFilter(true, Exposed.class));
    }

    @Override
    public String getName() {
        return "ImageServerDioContent";
    }

    @Override
    public String getDescription() {
        return "Mediafin Image Server DioContent Image Repository Module";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers(Set<ApplicationContextConfigurer> contextConfigurers) {
        contextConfigurers.add(new AnnotatedClassConfigurer(ImageServerDioContentConfig.class));
    }

}