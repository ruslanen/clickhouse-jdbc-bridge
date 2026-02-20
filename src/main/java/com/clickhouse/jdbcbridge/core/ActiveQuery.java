/**
 * Copyright 2019-2022, Zhichun Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clickhouse.jdbcbridge.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an active query that can be cancelled.
 * Thread-safe for concurrent cancellation attempts.
 * 
 * @since 2.0
 */
public class ActiveQuery {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActiveQuery.class);

    private final String queryId;
    private final ResponseWriter writer;
    private final long startTime;
    private final AtomicBoolean cancelled;

    public ActiveQuery(String queryId, ResponseWriter writer) {
        this.queryId = queryId;
        this.writer = writer;
        this.startTime = System.currentTimeMillis();
        this.cancelled = new AtomicBoolean(false);
    }

    public String getQueryId() {
        return queryId;
    }

    public ResponseWriter getWriter() {
        return writer;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Idempotent: cancels the query exactly once.
     * 
     * @return true if cancellation was performed, false if already cancelled
     */
    public boolean cancel() {
        if (cancelled.compareAndSet(false, true)) {
            if (log.isDebugEnabled()) {
                log.debug("Cancelling query [{}] after {} ms", queryId, 
                    System.currentTimeMillis() - startTime);
            }
            writer.cancel();
            return true;
        }
        return false;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }
}

