package com.brickintellect.webserver;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.jsonrpc4j.IJsonRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcClient;

import org.nanohttpd.protocols.websockets.WebSocket;

import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.ACCEPT_ENCODING;
import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.CONTENT_ENCODING;
import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.JSONRPC_CONTENT_TYPE;

/**
 * A wrapper around com.googlecode.jsonrpc4j.JsonRpcClient to allow its use with
 * WebSockets instead of the HTTP protocol.
 */
@SuppressWarnings("unused")
public class WebSocketJsonRpcClient extends JsonRpcClient implements IJsonRpcClient, WebSocketSession.IEndpoint {

	private WebSocketSession session;
	private int endpoint;

	/**
	 * Creates the {@link JsonRpcHttpClient} bound to the given {@code serviceUrl}.
	 * The headers provided in the {@code headers} map are added to every request
	 * made to the {@code serviceUrl}.
	 *
	 * @param mapper the {@link ObjectMapper} to use for json&lt;-&gt;java
	 *               conversion
	 */
	public WebSocketJsonRpcClient(WebSocketSession session, int endpoint, ObjectMapper mapper) {
		super(mapper);

		this.session = session;
		this.endpoint = endpoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invoke(String methodName, Object argument) throws Throwable {
		invoke(methodName, argument, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(String methodName, Object argument, Type returnType) throws Throwable {
		return invoke(methodName, argument, returnType, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(String methodName, Object argument, Type returnType, Map<String, String> extraHeaders)
			throws Throwable {

		// Send the request.
		try (OutputStream output = new WebSocketOutputStream(session, endpoint)) {
			super.invoke(methodName, argument, output);
		}

		return null;
/*
		// Read and return response.
		try {
			try (InputStream output = session.GetInputStream(endpoint)) {
				return super.readResponse(returnType, output);
			}
		} catch (JsonMappingException e) {
			// JsonMappingException inherits from IOException
			throw e;
		} catch (IOException e) {
			throw e;
		}
		*/
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(String methodName, Object argument, Class<T> clazz) throws Throwable {
		return (T) invoke(methodName, argument, Type.class.cast(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(String methodName, Object argument, Class<T> clazz, Map<String, String> extraHeaders)
			throws Throwable {
		return (T) invoke(methodName, argument, Type.class.cast(clazz), extraHeaders);
	}

	public int endpointNumber() {
		return 2;
	}

	public void onMessage(WebSocketInputStream input) {

	}
}