package com.ed.wew.api;

import java.util.*;

public class Params {
    private Map<String, Object> params = new LinkedHashMap();
    private List<CalculationType> statistics = new ArrayList();

    public Map<String, Object> getParams() {
        return params;
    }

    public void putParam(final String key, final Object value) {
        params.put(key, value);
    }

    public List<CalculationType> getStatistics() {
        return statistics;
    }

}
