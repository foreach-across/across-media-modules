package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Context;

public interface ContextManager {
    Context getByCode(String code);
}
