package org.jboss.resteasy.reactive.server.handlers;

import java.util.List;

import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.MessageBodyWriter;

import org.jboss.resteasy.reactive.common.util.MediaTypeHelper;
import org.jboss.resteasy.reactive.common.util.ServerMediaType;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.core.ServerSerialisers;
import org.jboss.resteasy.reactive.server.core.serialization.FixedEntityWriterArray;
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler;

/**
 * Handler that negotiates the content type for endpoints that
 * have multiple produces types, or for whatever reason can't have
 * their writer list and media type determined at build time.
 */
public class VariableProducesHandler implements ServerRestHandler {

    public static final MessageBodyWriter[] EMPTY = new MessageBodyWriter[0];
    final ServerMediaType mediaTypeList;
    final ServerSerialisers serialisers;

    public VariableProducesHandler(ServerMediaType mediaTypeList, ServerSerialisers serialisers) {
        this.mediaTypeList = mediaTypeList;
        this.serialisers = serialisers;
    }

    @Override
    public void handle(ResteasyReactiveRequestContext requestContext) throws Exception {
        Object entity = requestContext.getResult();
        if (entity instanceof Response) {
            return;
        }
        if (entity == null) {
            //TODO?
            return;
        }
        MediaType res = null;
        List<String> accepts = requestContext.getHttpHeaders().getRequestHeader(HttpHeaders.ACCEPT);
        for (String accept : accepts) {
            res = mediaTypeList.negotiateProduces(accept).getKey();
            if (res != null) {
                break;
            }
        }
        if (res == null) { // fallback to ensure that MessageBodyWriter is passed the proper media type
            res = mediaTypeList.negotiateProduces(requestContext.serverRequest().getRequestHeader(HttpHeaders.ACCEPT))
                    .getKey();
        }
        if (res == null) {
            throw new WebApplicationException(Response
                    .notAcceptable(Variant.mediaTypes(mediaTypeList.getSortedMediaTypes()).build())
                    .build());
        }
        if (MediaTypeHelper.isUnsupportedWildcardSubtype(res)) { // spec says the acceptable wildcard subtypes are */* or application/*
            throw new NotAcceptableException();
        }
        List<MessageBodyWriter<?>> writers = serialisers.findWriters(null, entity.getClass(), res, RuntimeType.SERVER);
        if (writers == null || writers.isEmpty()) {
            throw new WebApplicationException(Response
                    .notAcceptable(Variant.mediaTypes(mediaTypeList.getSortedMediaTypes()).build())
                    .build());
        }
        requestContext.setResponseContentType(res);
        requestContext.setEntityWriter(new FixedEntityWriterArray(writers.toArray(EMPTY), serialisers));
    }
}
