package com.cookpad.android.puree;

import com.cookpad.android.puree.storage.PureeStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class PureeOutput {
    protected Configuration conf;
    protected PureeStorage storage;
    protected List<PureeFilter> filters = new ArrayList<>();
    protected EmitCallback emitCallback = EmitCallback.DEFAULT;

    public void registerFilter(PureeFilter filter) {
        filters.add(filter);
    }

    public List<PureeFilter> getFilters() {
        return filters;
    }

    public void setEmitCallback(EmitCallback emitCallback) {
        this.emitCallback = emitCallback;
    }

    public void initialize(PureeStorage storage) {
        this.storage = storage;
        this.conf = configure(new Configuration());
    }

    public void start(JSONObject serializedLog) {
        try {
            serializedLog = applyFilters(serializedLog);
            emit(serializedLog);

            List<JSONObject> serializedLogs = new ArrayList<>();
            serializedLogs.add(serializedLog);
            applyAfterFilters(type(), serializedLogs);
        } catch (JSONException e) {
            // do nothing
        }
    }

    protected JSONObject applyFilters(JSONObject serializedLog) throws JSONException {
        for (PureeFilter filter : filters) {
            serializedLog = filter.call(serializedLog);
        }
        return serializedLog;
    }

    protected void applyAfterFilters(String type, List<JSONObject> serializedLogs) {
        emitCallback.call(type, serializedLogs);
    }

    public abstract String type();

    public abstract Configuration configure(Configuration conf);

    public abstract void emit(JSONObject serializedLog);

    public static class Configuration {
        private int flushInterval = 2 * 60 * 1000;
        private int logsPerRequest = 100;

        public int getFlushInterval() {
            return flushInterval;
        }

        public void setFlushInterval(int flushInterval) {
            this.flushInterval = flushInterval;
        }

        public int getLogsPerRequest() {
            return logsPerRequest;
        }

        public void setLogsPerRequest(int logsPerRequest) {
            this.logsPerRequest = logsPerRequest;
        }
    }
}

