package datadog.trace.instrumentation.netty41.client;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.propagate;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.netty41.client.NettyHttpClientDecorator.DECORATE;
import static datadog.trace.instrumentation.netty41.client.NettyHttpClientDecorator.DECORATE_SECURE;
import static datadog.trace.instrumentation.netty41.client.NettyHttpClientDecorator.NETTY_CLIENT_REQUEST;
import static datadog.trace.instrumentation.netty41.client.NettyResponseInjectAdapter.SETTER;

import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import datadog.trace.context.TraceScope;
import datadog.trace.instrumentation.netty41.AttributeKeys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpClientRequestTracingHandler extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise prm) {
    if (!(msg instanceof HttpRequest)) {
      ctx.write(msg, prm);
      return;
    }

    TraceScope parentScope = null;
    final TraceScope.Continuation continuation =
        ctx.channel().attr(AttributeKeys.PARENT_CONNECT_CONTINUATION_ATTRIBUTE_KEY).getAndRemove();
    if (continuation != null) {
      parentScope = continuation.activate();
    }

    final HttpRequest request = (HttpRequest) msg;

    ctx.channel().attr(AttributeKeys.CLIENT_PARENT_ATTRIBUTE_KEY).set(activeSpan());
    boolean isSecure = ctx.pipeline().get("ssl") != null;
    NettyHttpClientDecorator decorate = isSecure ? DECORATE_SECURE : DECORATE;

    final AgentSpan span = startSpan(NETTY_CLIENT_REQUEST);
    span.setMeasured(true);
    try (final AgentScope scope = activateSpan(span)) {
      decorate.afterStart(span);
      decorate.onRequest(span, request);
      decorate.onPeerConnection(span, (InetSocketAddress) ctx.channel().remoteAddress());

      // AWS calls are often signed, so we can't add headers without breaking the signature.
      if (!request.headers().contains("amz-sdk-invocation-id")) {
        propagate().inject(span, request.headers(), SETTER);
      }

      ctx.channel().attr(AttributeKeys.CLIENT_ATTRIBUTE_KEY).set(span);

      try {
        ctx.write(msg, prm);
      } catch (final Throwable throwable) {
        decorate.onError(span, throwable);
        decorate.beforeFinish(span);
        span.finish();
        throw throwable;
      }
    } finally {
      if (null != parentScope) {
        parentScope.close();
      }
    }
  }
}
