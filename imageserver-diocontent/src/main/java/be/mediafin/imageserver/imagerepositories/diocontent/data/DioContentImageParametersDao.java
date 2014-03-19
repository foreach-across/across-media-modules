package be.mediafin.imageserver.imagerepositories.diocontent.data;

import be.mediafin.imageserver.imagerepositories.diocontent.business.DioContentImageParameters;
import org.springframework.stereotype.Repository;

@Repository
public interface DioContentImageParametersDao {
    DioContentImageParameters getById(int id);

    void insert(DioContentImageParameters imageParameters);
}
