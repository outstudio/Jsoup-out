package org.jsoup;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 * A Connection provides a convenient interface to fetch content from the web, and parse them into Documents.
 * <p>
 * To get a new Connection, use {@link org.jsoup.Jsoup#connect(String)}. Connections contain {@link Connection.Request}
 * and {@link Connection.Response} objects. The request objects are reusable as prototype requests.
 * <p>
 * Request configuration can be made using either the shortcut methods in Connection (e.g. {@link #userAgent(String)}),
 * or by methods in the Connection.Request object directly. All request configuration must be made before the request
 * is executed.
 */
public interface Connection {

    /**
     * GET, POST, PUT, DELETE, OPTIONS http methods.
     */
    public enum Method {
        GET, POST, PUT, DELETE, OPTIONS
    }

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    public Connection url(URL url);

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    public Connection url(String url);

    /**
     * Set the request user-agent header.
     * @param userAgent user-agent to use
     * @return this Connection, for chaining
     */
    public Connection userAgent(String userAgent);

    /**
     * Set the request timeouts (connect and read). If a timeout occurs, an IOException will be thrown. The default
     * timeout is 3 seconds (3000 millis). A timeout of zero is treated as an infinite timeout.
     * @param millis number of milliseconds (thousandths of a second) before timing out connects or reads.
     * @return this Connection, for chaining
     */
    public Connection timeout(int millis);

    /**
     * Set the maximum bytes to read from the (uncompressed) connection into the body, before the connection is closed,
     * and the input truncated. The default maximum is 1MB. A max size of zero is treated as an infinite amount (bounded
     * only by your patience and the memory available on your machine).
     * @param bytes number of bytes to read from the input before truncating
     * @return this Connection, for chaining
     */
    public Connection maxBodySize(int bytes);

    /**
     * Set the request referrer (aka "referer") header.
     * @param referrer referrer to use
     * @return this Connection, for chaining
     */
    public Connection referrer(String referrer);

    /**
     * Configures the connection to (not) follow server redirects. By default this is <b>true</b>.
     * @param followRedirects true if server redirects should be followed.
     * @return this Connection, for chaining
     */
    public Connection followRedirects(boolean followRedirects);

    /**
     * Set the request method to use, GET or POST. Default is GET.
     * @param method HTTP request method
     * @return this Connection, for chaining
     */
    public Connection method(Method method);

    /**
     * Configures the connection to not throw exceptions when a HTTP error occurs. (4xx - 5xx, e.g. 404 or 500). By
     * default this is <b>false</b>; an IOException is thrown if an error is encountered. If set to <b>true</b>, the
     * response is populated with the error body, and the status message will reflect the error.
     * @param ignoreHttpErrors - false (default) if HTTP errors should be ignored.
     * @return this Connection, for chaining
     */
    public Connection ignoreHttpErrors(boolean ignoreHttpErrors);

    /**
     * Ignore the document's Content-Type when parsing the response. By default this is <b>false</b>, an unrecognised
     * content-type will cause an IOException to be thrown. (This is to prevent producing garbage by attempting to parse
     * a JPEG binary image, for example.) Set to true to force a parse attempt regardless of content type.
     * @param ignoreContentType set to true if you would like the content type ignored on parsing the response into a
     * Document.
     * @return this Connection, for chaining
     */
    public Connection ignoreContentType(boolean ignoreContentType);

    /**
     * Add a request data parameter. Request parameters are sent in the request query string for GETs, and in the request
     * body for POSTs. A request may have multiple values of the same name.
     * @param key data key
     * @param value data value
     * @return this Connection, for chaining
     */
    public Connection data(String key, String value);

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data collection of data parameters
     * @return this Connection, for chaining
     */
    public Connection data(Collection<KeyVal> data);

    /**
     * Adds all of the supplied data to the request data parameters
     * @param data map of data parameters
     * @return this Connection, for chaining
     */
    public Connection data(Map<String, String> data);

    /**
     * Add a number of request data parameters. Multiple parameters may be set at once, e.g.:
     * <code>.data("name", "jsoup", "language", "Java", "language", "English");</code> creates a query string like:
     * <code>?name=jsoup&language=Java&language=English</code>
     * @param keyvals a set of key value pairs.
     * @return this Connection, for chaining
     */
    public Connection data(String... keyvals);

    /**
     * Add a raw data. valid for {@link Method.POST} only
     * @param rawdata
     * @return this Connection, for chaining
     */
    public Connection rawData(String rawdata);
    
    /**
     * Specify the encoding of request data.
     * @param charset the encoding of request data
     * @return this Connection, for chaining
     */
    public Connection encoding(String charset);
    
    /**
     * Set a request header.
     * @param name header name
     * @param value header value
     * @return this Connection, for chaining
     * @see org.jsoup.Connection.Request#headers()
     */
    public Connection header(String name, String value);

    /**
     * Set a cookie to be sent in the request.
     * @param name name of cookie
     * @param value value of cookie
     * @return this Connection, for chaining
     */
    public Connection cookie(String name, String value);

    /**
     * Adds each of the supplied cookies to the request.
     * @param cookies map of cookie name -> value pairs
     * @return this Connection, for chaining
     */
    public Connection cookies(Map<String, String> cookies);

    /**
     * Provide an alternate parser to use when parsing the response to a Document.
     * @param parser alternate parser
     * @return this Connection, for chaining
     */
    public Connection parser(Parser parser);

    /**
     * Provide a proxy setting.
     * @param proxy HTTP request method
     * @return this Connection, for chaining
     * @see java.net.Proxy
     */
    public Connection proxy(Proxy proxy);

    /**
     * Provide a proxy setting.
     * @param type the <code>Type</code> of the proxy
     * @param host the Host name
     * @param port The port number
     * @return this Connection, for chaining
     * @see java.net.Proxy
     * @see java.net.InetSocketAddress
     */
    public Connection proxy(Proxy.Type type, String host, int port);
    
    /**
     * Execute the request as a GET, and parse the result.
     * @return parsed Document
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    public Document get() throws IOException;

    /**
     * Execute the request as a POST, and parse the result.
     * @return parsed Document
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    public Document post() throws IOException;

    /**
     * Execute the request.
     * @return a response object
     * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    public Response execute() throws IOException;

    /**
     * Get the request object associated with this connection
     * @return request
     */
    public Request request();

    /**
     * Set the connection's request
     * @param request new request object
     * @return this Connection, for chaining
     */
    public Connection request(Request request);

    /**
     * Get the response, once the request has been executed
     * @return response
     */
    public Response response();

    /**
     * Set the connection's response
     * @param response new response
     * @return this Connection, for chaining
     */
    public Connection response(Response response);


    /**
     * Common methods for Requests and Responses
     * @param <T> Type of Base, either Request or Response
     */
    interface Base<T extends Base<?>> {

        /**
         * Get the URL
         * @return URL
         */
        public URL url();

        /**
         * Set the URL
         * @param url new URL
         * @return this, for chaining
         */
        public T url(URL url);

        /**
         * Get the request method
         * @return method
         */
        public Method method();

        /**
         * Set the request method
         * @param method new method
         * @return this, for chaining
         */
        public T method(Method method);

        /**
         * Get the value of a header. This is a simplified header model, where a header may only have one value.
         * <p>
         * Header names are case insensitive.
         * @param name name of header (case insensitive)
         * @return value of header, or null if not set.
         * @see #hasHeader(String)
         * @see #cookie(String)
         */
        public String header(String name);

        /**
         * Set a header. This method will overwrite any existing header with the same case insensitive name. 
         * @param name Name of header
         * @param value Value of header
         * @return this, for chaining
         */
        public T header(String name, String value);

        /**
         * Check if a header is present
         * @param name name of header (case insensitive)
         * @return if the header is present in this request/response
         */
        public boolean hasHeader(String name);

        /**
         * Remove a header by name
         * @param name name of header to remove (case insensitive)
         * @return this, for chaining
         */
        public T removeHeader(String name);

        /**
         * Retrieve all of the request/response headers as a map
         * @return headers
         */
        public Map<String, String> headers();

        /**
         * Get a cookie value by name from this request/response.
         * <p>
         * Response objects have a simplified cookie model. Each cookie set in the response is added to the response
         * object's cookie key=value map. The cookie's path, domain, and expiry date are ignored.
         * @param name name of cookie to retrieve.
         * @return value of cookie, or null if not set
         */
        public String cookie(String name);

        /**
         * Set a cookie in this request/response.
         * @param name name of cookie
         * @param value value of cookie
         * @return this, for chaining
         */
        public T cookie(String name, String value);

        /**
         * Check if a cookie is present
         * @param name name of cookie
         * @return if the cookie is present in this request/response
         */
        public boolean hasCookie(String name);

        /**
         * Remove a cookie by name
         * @param name name of cookie to remove
         * @return this, for chaining
         */
        public T removeCookie(String name);

        /**
         * Retrieve all of the request/response cookies as a map
         * @return cookies
         */
        public Map<String, String> cookies();

    }

    /**
     * Represents a HTTP request.
     */
    public interface Request extends Base<Request> {
        /**
         * Get the request timeout, in milliseconds.
         * @return the timeout in milliseconds.
         */
        public int timeout();

        /**
         * Update the request timeout.
         * @param millis timeout, in milliseconds
         * @return this Request, for chaining
         */
        public Request timeout(int millis);

        /**
         * Get the maximum body size, in bytes.
         * @return the maximum body size, in bytes.
         */
        public int maxBodySize();

        /**
         * Update the maximum body size, in bytes.
         * @param bytes maximum body size, in bytes.
         * @return this Request, for chaining
         */
        public Request maxBodySize(int bytes);

        /**
         * Get the current followRedirects configuration.
         * @return true if followRedirects is enabled.
         */
        public boolean followRedirects();

        /**
         * Configures the request to (not) follow server redirects. By default this is <b>true</b>.
         *
         * @param followRedirects true if server redirects should be followed.
         * @return this Request, for chaining
         */
        public Request followRedirects(boolean followRedirects);

        /**
         * Get the current ignoreHttpErrors configuration.
         * @return true if errors will be ignored; false (default) if HTTP errors will cause an IOException to be thrown.
         */
        public boolean ignoreHttpErrors();

    	/**
    	 * Configures the request to ignore HTTP errors in the response.
    	 * @param ignoreHttpErrors set to true to ignore HTTP errors.
         * @return this Request, for chaining
    	 */
        public Request ignoreHttpErrors(boolean ignoreHttpErrors);

        /**
         * Get the current ignoreContentType configuration.
         * @return true if invalid content-types will be ignored; false (default) if they will cause an IOException to be thrown.
         */
        public boolean ignoreContentType();

        /**
    	 * Configures the request to ignore the Content-Type of the response.
    	 * @param ignoreContentType set to true to ignore the content type.
         * @return this Request, for chaining
    	 */
        public Request ignoreContentType(boolean ignoreContentType);

        /**
         * Add a data parameter to the request
         * @param keyval data to add.
         * @return this Request, for chaining
         */
        public Request data(KeyVal keyval);

        /**
         * Get all of the request's data parameters
         * @return collection of keyvals
         */
        public Collection<KeyVal> data();
        
        /**
         * Get the current isRawData configuration.
         * @return true if rawData was set; false (default)
         */
        public boolean isRawData();
        
        /**
         * Specify the rawdata. valid for {@link Method.POST} only. <br/>
         * You have to set {@link Connection.header} to not "application/x-www-form-urlencoded" <br/>
         * for example use: .header("Content-Type", "application/json")
         * @param rawdata
         * @return this Request, for chaining
         */
        public Request rawData(String rawdata); 
        
        /**
         * Get raw data. valid for {@link Method.POST} only
         * @return null (default)
         */
        public String rawData();
        
        /**
         * Specify the encoding of request data
         * @param charset the encoding of request data
         * @return this Request, for chaining
         */
        public Request encoding(String charset);

        /**
         * Get the current encoding of request data
         * @return current encoding of request data
         */
        public String encoding();
        
        /**
         * Specify the parser to use when parsing the document.
         * @param parser parser to use.
         * @return this Request, for chaining
         */
        public Request parser(Parser parser);

        /**
         * Get the current parser to use when parsing the document.
         * @return current Parser
         */
        public Parser parser();
        
        /**
         * get the proxy setting.
         * @return the proxy
         * @see java.net.Proxy
         */
         public Proxy proxy();

        /**
         * add a proxy setting to the request.
         * @return the proxy
         * @see java.net.Proxy
         */
         public Proxy proxy(Proxy proxy);
    }

    /**
     * Represents a HTTP response.
     */
    public interface Response extends Base<Response> {
    	
    	/**
         * Get the status code of the response.
         * @return status code
         */
        public int statusCode();

        /**
         * Get the status message of the response.
         * @return status message
         */
        public String statusMessage();

        /**
         * Get the character set name of the response.
         * @return character set name
         */
        public String charset();

        /**
         * Get the response content type (e.g. "text/html");
         * @return the response content type
         */
        public String contentType();

        /**
         * Parse the body of the response as a Document.
         * @return a parsed Document
         * @throws IOException on error
         */
        public Document parse() throws IOException;

        /**
         * Get the body of the response as a plain string.
         * @return body
         */
        public String body();

        /**
         * Get the body of the response as an array of bytes.
         * @return body bytes
         */
        public byte[] bodyAsBytes();
    }

    /**
     * A Key Value tuple.
     */
    public interface KeyVal {

        /**
         * Update the key of a keyval
         * @param key new key
         * @return this KeyVal, for chaining
         */
        public KeyVal key(String key);

        /**
         * Get the key of a keyval
         * @return the key
         */
        public String key();

        /**
         * Update the value of a keyval
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        public KeyVal value(String value);

        /**
         * Get the value of a keyval
         * @return the value
         */
        public String value();
    }
}

