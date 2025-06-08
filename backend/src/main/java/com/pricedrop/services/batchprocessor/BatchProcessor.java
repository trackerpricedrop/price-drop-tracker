package com.pricedrop.services.batchprocessor;

import java.util.List;

public interface BatchProcessor <T> {
    int LIMIT = 1;
    void handleBatch(int start, List<T> arrayList);
}
