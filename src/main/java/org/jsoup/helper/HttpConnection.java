package org.jsoup.helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TokenQueue;

/**
 * Implementation of {@link Connection}.
 * @see org.jsoup.Jsoup#connect(String) 
 */
public class HttpConnection implements Connection {
    public static Connection connect(String url) {
        Connection con = new HttpConnection();
        con.url(url);
        return con;
    }

    public static Connection connect(URL url) {
        Connection con = new HttpConnection();
        con.url(url);
        return con;
    }

	private static String encodeUrl(String url) {
		if(url == null)
			return null;
    	return url.replaceAll(" ", "%20");
	}

    private Connection.Request req;
    private Connection.Response res;

	private HttpConnection() {
        req = new Request();
        res = new Response();
    }

	@Override
    public Connection url(URL url) {
        req.url(url);
        return this;
    }

	@Override
    public Connection url(String url) {
        Validate.notEmpty(url, "Must supply a valid URL");
        try {
            req.url(new URL(encodeUrl(url)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

	@Override
    public Connection userAgent(String userAgent) {
        Validate.notNull(userAgent, "User agent must not be null");
        req.header("User-Agent", userAgent);
        return this;
    }

	@Override
    public Connection timeout(int millis) {
        req.timeout(millis);
        return this;
    }

	@Override
    public Connection maxBodySize(int bytes) {
        req.maxBodySize(bytes);
        return this;
    }

	@Override
    public Connection followRedirects(boolean followRedirects) {
        req.followRedirects(followRedirects);
        return this;
    }

	@Override
    public Connection referrer(String referrer) {
        Validate.notNull(referrer, "Referrer must not be null");
        req.header("Referer", referrer);
        return this;
    }

	@Override
    public Connection method(Method method) {
        req.method(method);
        return this;
    }

	@Override
    public Connection ignoreHttpErrors(boolean ignoreHttpErrors) {
		req.ignoreHttpErrors(ignoreHttpErrors);
		return this;
	}

	@Override
    public Connection ignoreContentType(boolean ignoreContentType) {
        req.ignoreContentType(ignoreContentType);
        return this;
    }

	@Override
    public Connection data(String key, String value) {
        req.data(KeyVal.create(key, value));
        return this;
    }

	@Override
    public Connection data(Map<String, String> data) {
        Validate.notNull(data, "Data map must not be null");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            req.data(KeyVal.create(entry.getKey(), entry.getValue()));
        }
        return this;
    }

	@Override
    public Connection data(String... keyvals) {
        Validate.notNull(keyvals, "Data key value pairs must not be null");
        Validate.isTrue(keyvals.length %2 == 0, "Must supply an even number of key value pairs");
        for (int i = 0; i < keyvals.length; i += 2) {
            String key = keyvals[i];
            String value = keyvals[i+1];
            Validate.notEmpty(key, "Data key must not be empty");
            Validate.notNull(value, "Data value must not be null");
            req.data(KeyVal.create(key, value));
        }
        return this;
    }

	@Override
    public Connection data(Collection<Connection.KeyVal> data) {
        Validate.notNull(data, "Data collection must not be null");
        for (Connection.KeyVal entry: data) {
            req.data(entry);
        }
        return this;
    }
    
	@Override
    public Connection rawData(String rawdata) {
    	req.rawData(rawdata);
        return this;
    }
    
	@Override
    public Connection encoding(String charset) {
        req.encoding(charset);
        return this;
        }
    
	@Override
    public Connection header(String name, String value) {
        req.header(name, value);
        return this;
    }

	@Override
    public Connection cookie(String name, String value) {
        req.cookie(name, value);
        return this;
    }

	@Override
    public Connection cookies(Map<String, String> cookies) {
        Validate.notNull(cookies, "Cookie map must not be null");
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            req.cookie(entry.getKey(), entry.getValue());
        }
        return this;
    }

	@Override
    public Connection parser(Parser parser) {
        req.parser(parser);
        return this;
    }
	
	@Override
    public Connection proxy(Proxy proxy) {
        req.proxy(proxy);
        return this;
    }
	
	@Override
	public Connection proxy(Proxy.Type type, String host, int port) {
        req.proxy(new Proxy(type, new InetSocketAddress(host, port)));
        return this;
    }

	@Override
    public Document get() throws IOException {
        req.method(Method.GET);
        execute();
        return res.parse();
    }

	@Override
    public Document post() throws IOException {
        req.method(Method.POST);
        execute();
        return res.parse();
    }

	@Override
    public Connection.Response execute() throws IOException {
        res = Response.execute(req);
        return res;
    }

	@Override
    public Connection.Request request() {
        return req;
    }

	@Override
    public Connection request(Connection.Request request) {
        req = request;
        return this;
    }

	@Override
    public Connection.Response response() {
        return res;
    }

	@Override
    public Connection response(Connection.Response response) {
        res = response;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static abstract class Base<T extends Connection.Base> implements Connection.Base<T> {
        URL url;
        Method method;
        Map<String, String> headers;
        Map<String, String> cookies;

        private Base() {
            headers = new LinkedHashMap<String, String>();
            cookies = new LinkedHashMap<String, String>();
        }

        @Override
        public URL url() {
            return url;
        }

        @Override
        public T url(URL url) {
            Validate.notNull(url, "URL must not be null");
            this.url = url;
            return (T) this;
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public T method(Method method) {
            Validate.notNull(method, "Method must not be null");
            this.method = method;
            return (T) this;
        }

        @Override
        public String header(String name) {
            Validate.notNull(name, "Header name must not be null");
            return getHeaderCaseInsensitive(name);
        }

        @Override
        public T header(String name, String value) {
            Validate.notEmpty(name, "Header name must not be empty");
            Validate.notNull(value, "Header value must not be null");
            removeHeader(name); // ensures we don't get an "accept-encoding" and a "Accept-Encoding"
            headers.put(name, value);
            return (T) this;
        }

        @Override
        public boolean hasHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            return getHeaderCaseInsensitive(name) != null;
        }

        @Override
        public T removeHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            Map.Entry<String, String> entry = scanHeaders(name); // remove is case insensitive too
            if (entry != null)
                headers.remove(entry.getKey()); // ensures correct case
            return (T) this;
        }

        @Override
        public Map<String, String> headers() {
            return headers;
        }

        private String getHeaderCaseInsensitive(String name) {
            Validate.notNull(name, "Header name must not be null");
            // quick evals for common case of title case, lower case, then scan for mixed
            String value = headers.get(name);
            if (value == null)
                value = headers.get(name.toLowerCase());
            if (value == null) {
                Map.Entry<String, String> entry = scanHeaders(name);
                if (entry != null)
                    value = entry.getValue();
            }
            return value;
        }

        private Map.Entry<String, String> scanHeaders(String name) {
            String lc = name.toLowerCase();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase().equals(lc))
                    return entry;
            }
            return null;
        }

        @Override
        public String cookie(String name) {
            Validate.notNull(name, "Cookie name must not be null");
            return cookies.get(name);
        }

        @Override
        public T cookie(String name, String value) {
            Validate.notEmpty(name, "Cookie name must not be empty");
            Validate.notNull(value, "Cookie value must not be null");
            cookies.put(name, value);
            return (T) this;
        }

        @Override
        public boolean hasCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            return cookies.containsKey(name);
        }

        @Override
        public T removeCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            cookies.remove(name);
            return (T) this;
        }

        @Override
        public Map<String, String> cookies() {
            return cookies;
        }
    }

    public static class Request extends Base<Connection.Request> implements Connection.Request {
        private int timeoutMilliseconds;
        private int maxBodySizeBytes;
        private boolean followRedirects;
        private Collection<Connection.KeyVal> data;
        private boolean ignoreHttpErrors = false;
        private boolean ignoreContentType = false;
        private Parser parser;
        private String rawData;
        private String encoding;
        private Proxy proxy;

      	private Request() {
            timeoutMilliseconds = 3000;
            maxBodySizeBytes = 1024 * 1024; // 1MB
            followRedirects = true;
            data = new ArrayList<Connection.KeyVal>();
            method = Connection.Method.GET;
            headers.put("Accept-Encoding", "gzip");
            parser = Parser.htmlParser();
            encoding = DataUtil.defaultCharset;
            proxy = Proxy.NO_PROXY;
        }

      	@Override
        public int timeout() {
            return timeoutMilliseconds;
        }

        @Override
        public Request timeout(int millis) {
            Validate.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
            timeoutMilliseconds = millis;
            return this;
        }

        @Override
        public int maxBodySize() {
            return maxBodySizeBytes;
        }

        @Override
        public Connection.Request maxBodySize(int bytes) {
            Validate.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
            maxBodySizeBytes = bytes;
            return this;
        }

        @Override
        public boolean followRedirects() {
            return followRedirects;
        }

        @Override
        public Connection.Request followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        @Override
        public boolean ignoreHttpErrors() {
            return ignoreHttpErrors;
        }

        @Override
        public Connection.Request ignoreHttpErrors(boolean ignoreHttpErrors) {
            this.ignoreHttpErrors = ignoreHttpErrors;
            return this;
        }

        @Override
        public boolean ignoreContentType() {
            return ignoreContentType;
        }

        @Override
        public Connection.Request ignoreContentType(boolean ignoreContentType) {
            this.ignoreContentType = ignoreContentType;
            return this;
        }

        @Override
        public Request data(Connection.KeyVal keyval) {
            Validate.notNull(keyval, "Key val must not be null");
            data.add(keyval);
            return this;
        }

        @Override
        public Collection<Connection.KeyVal> data() {
            return data;
        }
        
        @Override
        public boolean isRawData() {
        	return rawData != null;
        }
        
        @Override
        public Request rawData(String value) {
        	this.rawData = value;
        	return this;
        }
        
        @Override
        public String rawData() {
            return rawData;
        }
        
		@Override
		public Request encoding(String charset) {
			this.encoding = charset;
			return this;
		}

		@Override
		public String encoding() {
			return encoding;
		}
        
		@Override
        public Request parser(Parser parser) {
        	this.parser = parser;
        	return this;
        }
        
		@Override
        public Parser parser() {
        	return parser;
        }
        
		@Override
        public Proxy proxy() {
        	return proxy;
        }

		@Override
        public Proxy proxy(Proxy proxy) {
            this.proxy = proxy;
            return proxy;
        }
    }

    public static class Response extends Base<Connection.Response> implements Connection.Response {
        private static final int MAX_REDIRECTS = 20;
        private int statusCode;
        private String statusMessage;
        private ByteBuffer byteData;
        private String charset;
        private String contentType;
        private boolean executed = false;
        private int numRedirects = 0;
        private Connection.Request req;

        Response() {
            super();
        }

        private Response(Response previousResponse) throws IOException {
            super();
            if (previousResponse != null) {
                numRedirects = previousResponse.numRedirects + 1;
                if (numRedirects >= MAX_REDIRECTS)
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", previousResponse.url()));
            }
        }
        
        static Response execute(Connection.Request req) throws IOException {
            return execute(req, null);
        }

        static Response execute(Connection.Request req, Response previousResponse) throws IOException {
            Validate.notNull(req, "Request must not be null");
            String protocol = req.url().getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https"))
                throw new MalformedURLException("Only http & https protocols supported");

            // set up the request for execution
            if (req.method() == Connection.Method.GET && req.data().size() > 0)
                serialiseRequestUrl(req); // appends query string
            HttpURLConnection conn = createConnection(req);
            Response res;
            try {
                conn.connect();
                if (req.method() == Connection.Method.POST){
                	if (req.isRawData()) {
                		writeRawPost(req.rawData(), req.encoding(), conn.getOutputStream());
					}else{
						writePost(req.data(), req.encoding(), conn.getOutputStream());
					}
                }

                int status = conn.getResponseCode();
                boolean needsRedirect = false;
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
                        needsRedirect = true;
                    else if (!req.ignoreHttpErrors())
                        throw new HttpStatusException("HTTP error fetching URL", status, req.url().toString());
                }
                res = new Response(previousResponse);
                res.setupFromConnection(conn, previousResponse);
                if (needsRedirect && req.followRedirects()) {
                    req.method(Method.GET); // always redirect with a get. any data param from original req are dropped.
                    req.data().clear();

                    String location = res.header("Location");
                    if (location != null && location.startsWith("http:/") && location.charAt(6) != '/') // fix broken Location: http:/temp/AAG_New/en/index.php
                        location = location.substring(6);
                    req.url(new URL(req.url(), encodeUrl(location)));

                    for (Map.Entry<String, String> cookie : res.cookies.entrySet()) { // add response cookies to request (for e.g. login posts)
                        req.cookie(cookie.getKey(), cookie.getValue());
                    }
                    return execute(req, res);
                }
                res.req = req;

                // check that we can handle the returned content type; if not, abort before fetching it
                String contentType = res.contentType();
                if (contentType != null && !req.ignoreContentType() && (!(contentType.startsWith("text/") || contentType.startsWith("application/xml") 
                		 || contentType.startsWith("application/json") || contentType.startsWith("application/xhtml+xml"))))
                    throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, application/json, or application/xhtml+xml",
                            contentType, req.url().toString());

                InputStream bodyStream = null;
                InputStream dataStream = null;
                try {
                    dataStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                    bodyStream = res.hasHeader("Content-Encoding") && res.header("Content-Encoding").equalsIgnoreCase("gzip") ?
                            new BufferedInputStream(new GZIPInputStream(dataStream)) :
                            new BufferedInputStream(dataStream);

                    res.byteData = DataUtil.readToByteBuffer(bodyStream, req.maxBodySize());
                    res.charset = DataUtil.getCharsetFromContentType(res.contentType); // may be null, readInputStream deals with it
                } finally {
                    if (bodyStream != null) bodyStream.close();
                    if (dataStream != null) dataStream.close();
                }
            } finally {
                // per Java's documentation, this is not necessary, and precludes keepalives. However in practise,
                // connection errors will not be released quickly enough and can cause a too many open files error.
                conn.disconnect();
            }

            res.executed = true;
            return res;
        }

        public int statusCode() {
            return statusCode;
        }

        public String statusMessage() {
            return statusMessage;
        }

        public String charset() {
            return charset;
        }

        public String contentType() {
            return contentType;
        }

        public Document parse() throws IOException {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before parsing response");
            Document doc = DataUtil.parseByteData(byteData, charset, url.toExternalForm(), req.parser());
            byteData.rewind();
            charset = doc.outputSettings().charset().name(); // update charset from meta-equiv, possibly
            return doc;
        }

        public String body() {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
            // charset gets set from header on execute, and from meta-equiv on parse. parse may not have happened yet
            String body;
            if (charset == null)
                body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
            else
                body = Charset.forName(charset).decode(byteData).toString();
            byteData.rewind();
            return body;
        }

        public byte[] bodyAsBytes() {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
            return byteData.array();
        }

        // set up connection defaults, and details from request
        private static HttpURLConnection createConnection(Connection.Request req) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) req.url().openConnection(req.proxy());
            conn.setRequestMethod(req.method().name());
            conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
            conn.setConnectTimeout(req.timeout());
            conn.setReadTimeout(req.timeout());
            if (req.method() == Method.POST)
                conn.setDoOutput(true);
            if (req.cookies().size() > 0)
                conn.addRequestProperty("Cookie", getRequestCookieString(req));
            for (Map.Entry<String, String> header : req.headers().entrySet()) {
                conn.addRequestProperty(header.getKey(), header.getValue());
            }
            return conn;
        }

        // set up url, method, header, cookies
        private void setupFromConnection(HttpURLConnection conn, Connection.Response previousResponse) throws IOException {
            method = Connection.Method.valueOf(conn.getRequestMethod());
            url = conn.getURL();
            statusCode = conn.getResponseCode();
            statusMessage = conn.getResponseMessage();
            contentType = conn.getContentType();

            Map<String, List<String>> resHeaders = conn.getHeaderFields();
            processResponseHeaders(resHeaders);

            // if from a redirect, map previous response cookies into this response
            if (previousResponse != null) {
                for (Map.Entry<String, String> prevCookie : previousResponse.cookies().entrySet()) {
                    if (!hasCookie(prevCookie.getKey()))
                        cookie(prevCookie.getKey(), prevCookie.getValue());
                }
            }
        }

        void processResponseHeaders(Map<String, List<String>> resHeaders) {
            for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null)
                    continue; // http/1.1 line

                List<String> values = entry.getValue();
                if (name.equalsIgnoreCase("Set-Cookie")) {
                    for (String value : values) {
                        if (value == null)
                            continue;
                        TokenQueue cd = new TokenQueue(value);
                        String cookieName = cd.chompTo("=").trim();
                        String cookieVal = cd.consumeTo(";").trim();
                        if (cookieVal == null)
                            cookieVal = "";
                        // ignores path, date, domain, secure et al. req'd?
                        // name not blank, value not null
                        if (cookieName != null && cookieName.length() > 0)
                            cookie(cookieName, cookieVal);
                    }
                } else { // only take the first instance of each header
                    if (!values.isEmpty())
                        header(name, values.get(0));
                }
            }
        }

        private static void writeRawPost(String data, String encoding, OutputStream outputStream) throws IOException {
            OutputStreamWriter w = new OutputStreamWriter(outputStream, encoding);
            w.write(data);
            w.close();
        }

        private static void writePost(Collection<Connection.KeyVal> data, String encoding, OutputStream outputStream) throws IOException {
            OutputStreamWriter w = new OutputStreamWriter(outputStream, encoding);
            boolean first = true;
            for (Connection.KeyVal keyVal : data) {
                if (!first) 
                    w.append('&');
                else
                    first = false;
                
                w.write(URLEncoder.encode(keyVal.key(), encoding));
                w.write('=');
                w.write(URLEncoder.encode(keyVal.value(), encoding));
            }
            w.close();
        }
        
        private static String getRequestCookieString(Connection.Request req) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> cookie : req.cookies().entrySet()) {
                if (!first)
                    sb.append("; ");
                else
                    first = false;
                sb.append(cookie.getKey()).append('=').append(cookie.getValue());
                // todo: spec says only ascii, no escaping / encoding defined. validate on set? or escape somehow here?
            }
            return sb.toString();
        }

        // for get url reqs, serialise the data map into the url
        private static void serialiseRequestUrl(Connection.Request req) throws IOException {
            URL in = req.url();
            StringBuilder url = new StringBuilder();
            boolean first = true;
            // reconstitute the query, ready for appends
            url
                .append(in.getProtocol())
                .append("://")
                .append(in.getAuthority()) // includes host, port
                .append(in.getPath())
                .append("?");
            if (in.getQuery() != null) {
                url.append(in.getQuery());
                first = false;
            }
            String encoding = req.encoding();
            for (Connection.KeyVal keyVal : req.data()) {
                if (!first)
                    url.append('&');
                else
                    first = false;
                url
                    .append(URLEncoder.encode(keyVal.key(), encoding))
                    .append('=')
                    .append(URLEncoder.encode(keyVal.value(), encoding));
            }
            req.url(new URL(url.toString()));
            req.data().clear(); // moved into url as get params
        }
    }

    public static class KeyVal implements Connection.KeyVal {
        private String key;
        private String value;

        public static KeyVal create(String key, String value) {
            Validate.notEmpty(key, "Data key must not be empty");
            Validate.notNull(value, "Data value must not be null");
            return new KeyVal(key, value);
        }

        private KeyVal(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public KeyVal key(String key) {
            Validate.notEmpty(key, "Data key must not be empty");
            this.key = key;
            return this;
        }

        public String key() {
            return key;
        }

        public KeyVal value(String value) {
            Validate.notNull(value, "Data value must not be null");
            this.value = value;
            return this;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }      
    }
}
