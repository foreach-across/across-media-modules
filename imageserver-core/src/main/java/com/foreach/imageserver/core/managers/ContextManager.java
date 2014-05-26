package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Context;

import java.util.Collection;

public interface ContextManager {
    Context getByCode(String code);

    Collection<Context> getAllContexts();

    Collection<Context> getForResolution(int resolutionId);

    void updateContextsForResolution(int resolutionId, Collection<Context> contexts);
}
