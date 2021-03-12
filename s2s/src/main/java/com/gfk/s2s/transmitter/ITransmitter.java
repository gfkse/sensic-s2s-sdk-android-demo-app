package com.gfk.s2s.transmitter;

import com.gfk.s2s.builder.request.IRequest;

public interface ITransmitter {
    void sendRequest(final IRequest request);
}
