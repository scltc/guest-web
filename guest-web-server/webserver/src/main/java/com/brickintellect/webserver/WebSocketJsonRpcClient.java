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


import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.ACCEPT_ENCODING;
import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.CONTENT_ENCODING;
import static com.googlecode.jsonrpc4j.JsonRpcBasicServer.JSONRPC_CONTENT_TYPE;

/**
 * A JSON-RPC client that uses the HTTP protocol.
 */
@SuppressWarnings("unused")
public class WebSocketJsonRpcClient extends JsonRpcClient implements IJsonRpcClient {
	
	private int connectionTimeoutMillis = 60 * 1000;
	private int readTimeoutMillis = 60 * 1000 * 2;
	
	/**
	 * Creates the {@link JsonRpcHttpClient} bound to the given {@code serviceUrl}.
	 * The headers provided in the {@code headers} map are added to every request
	 * made to the {@code serviceUrl}.
	 *
	 * @param mapper              the {@link ObjectMapper} to use for json&lt;-&gt;java conversion
	 */
	public WebSocketJsonRpcClient(ObjectMapper mapper) {
		super(mapper);
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
	public Object invoke(String methodName, Object argument, Type returnType, Map<String, String> extraHeaders) throws Throwable {
		HttpURLConnection connection = prepareConnection();
		try {
				connection.connect();
				try (OutputStream send = connection.getOutputStream()) {
					super.invoke(methodName, argument, send);
				}
			
			// read and return value
			try {
				try (InputStream answer = connection.getInputStream()) {
					return super.readResponse(returnType, answer);
				}
			} catch (JsonMappingException e) {
				// JsonMappingException inherits from IOException
				throw e;
			} catch (IOException e) {
				if (connection.getErrorStream() == null) {
					throw new Exception("Caught error with no response body.", e);
				}

				try (InputStream answer = connection.getErrorStream()) {
					return super.readResponse(returnType, answer);
				} catch (IOException ef) {
					throw new Exception(readErrorString(connection), ef);
				}
			}
		} finally {
			connection.disconnect();
		}
		
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
	public <T> T invoke(String methodName, Object argument, Class<T> clazz, Map<String, String> extraHeaders) throws Throwable {
		return (T) invoke(methodName, argument, Type.class.cast(clazz), extraHeaders);
	}
	
	/**
	 * Prepares a connection to the server.
	 *
	 * @param extraHeaders extra headers to add to the request
	 * @return the unopened connection
	 * @throws IOException
	 */
	private HttpURLConnection prepareConnection() throws IOException {
		
		// create URLConnection
        HttpURLConnection connection = null;

		// connection.setConnectTimeout(connectionTimeoutMillis);
		// connection.setReadTimeout(readTimeoutMillis);
		// connection.setAllowUserInteraction(false);
		// connection.setDefaultUseCaches(false);
		// connection.setDoInput(true);
		// connection.setDoOutput(true);
		// connection.setUseCaches(false);
		// connection.setInstanceFollowRedirects(true);
		// connection.setRequestMethod("POST");
		
		return connection;
	}
	
	private static String readErrorString(final HttpURLConnection connection) {
		try (InputStream stream = connection.getErrorStream()) {
			StringBuilder buffer = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
				for (int ch = reader.read(); ch >= 0; ch = reader.read()) {
					buffer.append((char) ch);
				}
			}
			return buffer.toString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	/**
	 * @return the connectionTimeoutMillis
	 */
	public int getConnectionTimeoutMillis() {
		return connectionTimeoutMillis;
	}
	
	/**
	 * @param connectionTimeoutMillis the connectionTimeoutMillis to set
	 */
	public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
		this.connectionTimeoutMillis = connectionTimeoutMillis;
	}
	
	/**
	 * @return the readTimeoutMillis
	 */
	public int getReadTimeoutMillis() {
		return readTimeoutMillis;
	}
	
	/**
	 * @param readTimeoutMillis the readTimeoutMillis to set
	 */
	public void setReadTimeoutMillis(int readTimeoutMillis) {
		this.readTimeoutMillis = readTimeoutMillis;
	}
}