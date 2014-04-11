package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.installers.InitialSchemaInstaller;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;

import java.util.Set;

@AcrossDepends(required = {"ImageServerCoreModule"})
public class DioContentImageRepositoryModule extends AcrossModule {

    public DioContentImageRepositoryModule() {
        setExposeFilter(new AnnotationBeanFilter(true, Exposed.class));
    }

    @Override
    public String getName() {
        return "ImageServerDioContentImageRepository";
    }

    @Override
    public String getDescription() {
        return "Mediafin Image Server DioContent Image Repository Module";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers(Set<ApplicationContextConfigurer> contextConfigurers) {
        contextConfigurers.add(new AnnotatedClassConfigurer(DioContentImageRepositoryConfig.class));
    }

    @Override
    public Object[] getInstallers() {
        return new Object[]{new InitialSchemaInstaller()};
    }
}