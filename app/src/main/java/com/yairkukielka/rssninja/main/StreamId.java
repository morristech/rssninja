package com.yairkukielka.rssninja.main;

/**
 * Stream to be loaded
 */
public class StreamId {

    private String streamId;
    /**
     * If it's a stream that's a group of streams, mix is true. If it's a simple stream, mix is false
     */
    private Boolean mix;

    public StreamId(String streamId, Boolean mix) {
        this.streamId = streamId;
        this.mix = mix;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public Boolean getMix() {
        return mix;
    }

    public void setMix(Boolean mix) {
        this.mix = mix;
    }
}
