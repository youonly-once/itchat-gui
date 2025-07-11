package cn.shu.wechat.utils;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.swing.utils.MultipartBodyPublisher;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * HTTP访问类，对Apache HttpClient进行简单封装，适配器模式
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年4月9日 下午7:05:04
 */
@Log4j2
public class HttpUtil {

    /**
     * 用于接收文件或者其它类型的client 无超时时间（例如下载大文件）
     */
    //private static final CloseableHttpClient myHttpClient;

    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }
    };




    private static final HttpClient clientWithRedirect;

    static {
        try {
            clientWithRedirect = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .sslContext(get())
                    .sslParameters(new SSLParameters())
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static SSLContext get() throws KeyManagementException, NoSuchAlgorithmException {
        // 2. 初始化 SSLContext，使用上述信任管理器
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }
    private static final HttpClient clientNoRedirect = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private HttpUtil() {}


    public static HttpResponse.BodyHandler<Void> getProgressBytesBodyHandler(BlockingQueue<Long> process, Path path) throws IOException {
        AtomicLong readEd = new AtomicLong(0);
        Files.createDirectories(path.getParent());
        // 追加写入文件
        return HttpResponse.BodyHandlers.ofByteArrayConsumer(bytesOptional -> {
            byte[] bytes = bytesOptional.orElse(new byte[]{});
            readEd.addAndGet(bytes.length);
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                out.write(bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            process.offer(readEd.get());
        });
    }

    public static HttpResponse.BodyHandler<Void> getProgressBytesBodyHandler(BlockingQueue<Long> process,String path) throws IOException {
        return getProgressBytesBodyHandler(process, Path.of(path));
    }

    public static <R> HttpResponse.BodyHandler<R> getJsonEntityBodyHandler(Class<R> clazz){
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                s -> {
                    return JSON.parseObject(s,clazz);
                });
    }


    /**
     * 循环接收消息的client 有超时时间，避免卡住
     */
    //private static final CloseableHttpClient receiveHttpClient;

    //public static final CookieStore cookieStore;
    private final static WechatConfiguration config = SpringContextHolder.getBean(WechatConfiguration.class);
//    static {
//        cookieStore = new BasicCookieStore();
//
//        // 将CookieStore设置到MyHttpClient中
//        myHttpClient = HttpClients.custom().setDefaultCookieStore(cookieStore)
//                .build();
//
//
//        //循环接收消息，需要设置超时时间，否则可能卡住
//        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager();
//        connManager.setSocketConfig(SocketConfig.custom().setSoTimeout(30000).build());
//        receiveHttpClient = HttpClients.custom().setDefaultCookieStore(cookieStore)
//                .setConnectionManager(connManager)
//                .setConnectionTimeToLive(30000L, TimeUnit.MILLISECONDS)
//                .build();
//    }

//    public static String getCookie(String name) {
//        List<Cookie> cookies = cookieStore.getCookies();
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equalsIgnoreCase(name)) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//
//    }


    /**
     * 获取cookies
     *
     * @author SXS
     * @date 2017年5月7日 下午8:37:17
     * @return
     */
/*	public static MyHttpClient getInstance() {
		if (instance == null) {
			synchronized (MyHttpClient.class) {
				if (instance == null) {
					instance = new MyHttpClient();
				}
			}
		}
		return instance;
	}*/

//    /**
//     * 处理GET请求
//     *
//     * @param url
//     * @param params
//     * @return
//     * @author SXS
//     * @date 2017年4月9日 下午7:06:19
//     */
//    public static HttpEntity doGetOfReceive(String url, List<BasicNameValuePair> params, boolean redirect,
//                                            Map<String, String> headerMap) {
//        HttpEntity entity = null;
//        HttpGet httpGet;
//
//        try {
//            if (params != null) {
//                String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
//                httpGet = new HttpGet(url + "?" + paramStr);
//            } else {
//                httpGet = new HttpGet(url);
//            }
//            if (!redirect) {
//                httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build()); // 禁止重定向
//            }
//            httpGet.setHeader("User-Agent", USER_AGENT);
//
//            httpGet.setHeader("client-version", UOSConfig.UOS_PATCH_CLIENT_VERSION);
//            httpGet.setHeader("extspam", UOSConfig.UOS_PATCH_EXTSPAM);
//            httpGet.setHeader("referer", UOSConfig.REFERER);
//
//
//            if (headerMap != null) {
//                Set<Entry<String, String>> entries = headerMap.entrySet();
//                for (Entry<String, String> entry : entries) {
//                    httpGet.setHeader(entry.getKey(), entry.getValue());
//                }
//            }
//            CloseableHttpResponse response = receiveHttpClient.execute(httpGet);
//            entity = response.getEntity();
//        } catch (IOException e) {
//            // log.error(e.getMessage());
//        }
//
//        return entity;
//    }


//    /**
//     * 执行 GET 请求
//     */
//    public static String doGet(String url, Map<String, String> params, Map<String, String> headers) {
//        try {
//            String paramStr = (params != null && !params.isEmpty()) ?
//                    params.entrySet().stream()
//                            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
//                                    URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
//                            .collect(Collectors.joining("&")) : "";
//
//            if (!paramStr.isEmpty()) {
//                url += url.contains("?") ? "&" + paramStr : "?" + paramStr;
//            }
//
//            HttpRequest.Builder builder = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .GET();
//
//            if (headers != null) {
//                headers.forEach(builder::header);
//            }
//
//            HttpRequest request = builder.build();
//            HttpResponse<String> response = clientWithRedirect.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//
//        } catch (IOException | InterruptedException e) {
//            log.error("GET 请求失败: {}", e.getMessage());
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }


//    /**
//     * 处理POST请求
//     *
//     * @param url
//     * @param paramsStr
//     * @return
//     * @author SXS
//     * @date 2017年4月9日 下午7:06:35
//     */
//    public static HttpEntity doPost(String url, String paramsStr) {
//        HttpEntity entity = null;
//        HttpPost httpPost;
//        try {
//            StringEntity params = new StringEntity(paramsStr, Consts.UTF_8);
//            httpPost = new HttpPost(url);
//            httpPost.setEntity(params);
//            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
//            httpPost.setHeader("User-Agent", config.getUserAgent());
//
//            httpPost.setHeader("client-version", "2.0.0");
//            httpPost.setHeader("extspam", "Gp8ICJkIEpkICggwMDAwMDAwMRAGGoAI1GiJSIpeO1RZTq9QBKsRbPJdi84ropi16EYI10WB6g74sGmRwSNXjPQnYUKYotKkvLGpshucCaeWZMOylnc6o2AgDX9grhQQx7fm2DJRTyuNhUlwmEoWhjoG3F0ySAWUsEbH3bJMsEBwoB//0qmFJob74ffdaslqL+IrSy7LJ76/G5TkvNC+J0VQkpH1u3iJJs0uUYyLDzdBIQ6Ogd8LDQ3VKnJLm4g/uDLe+G7zzzkOPzCjXL+70naaQ9medzqmh+/SmaQ6uFWLDQLcRln++wBwoEibNpG4uOJvqXy+ql50DjlNchSuqLmeadFoo9/mDT0q3G7o/80P15ostktjb7h9bfNc+nZVSnUEJXbCjTeqS5UYuxn+HTS5nZsPVxJA2O5GdKCYK4x8lTTKShRstqPfbQpplfllx2fwXcSljuYi3YipPyS3GCAqf5A7aYYwJ7AvGqUiR2SsVQ9Nbp8MGHET1GxhifC692APj6SJxZD3i1drSYZPMMsS9rKAJTGz2FEupohtpf2tgXm6c16nDk/cw+C7K7me5j5PLHv55DFCS84b06AytZPdkFZLj7FHOkcFGJXitHkX5cgww7vuf6F3p0yM/W73SoXTx6GX4G6Hg2rYx3O/9VU2Uq8lvURB4qIbD9XQpzmyiFMaytMnqxcZJcoXCtfkTJ6pI7a92JpRUvdSitg967VUDUAQnCXCM/m0snRkR9LtoXAO1FUGpwlp1EfIdCZFPKNnXMeqev0j9W9ZrkEs9ZWcUEexSj5z+dKYQBhIICviYUQHVqBTZSNy22PlUIeDeIs11j7q4t8rD8LPvzAKWVqXE+5lS1JPZkjg4y5hfX1Dod3t96clFfwsvDP6xBSe1NBcoKbkyGxYK0UvPGtKQEE0Se2zAymYDv41klYE9s+rxp8e94/H8XhrL9oGm8KWb2RmYnAE7ry9gd6e8ZuBRIsISlJAE/e8y8xFmP031S6Lnaet6YXPsFpuFsdQs535IjcFd75hh6DNMBYhSfjv456cvhsb99+fRw/KVZLC3yzNSCbLSyo9d9BI45Plma6V8akURQA/qsaAzU0VyTIqZJkPDTzhuCl92vD2AD/QOhx6iwRSVPAxcRFZcWjgc2wCKh+uCYkTVbNQpB9B90YlNmI3fWTuUOUjwOzQRxJZj11NsimjOJ50qQwTTFj6qQvQ1a/I+MkTx5UO+yNHl718JWcR3AXGmv/aa9rD1eNP8ioTGlOZwPgmr2sor2iBpKTOrB83QgZXP+xRYkb4zVC+LoAXEoIa1+zArywlgREer7DLePukkU6wHTkuSaF+ge5Of1bXuU4i938WJHj0t3D8uQxkJvoFi/EYN/7u2P1zGRLV4dHVUsZMGCCtnO6BBigFMAA=");
//            httpPost.setHeader("referer", "https://wx.qq.com/?&lang=zh_CN&target=t");
//
//            CloseableHttpResponse response = myHttpClient.execute(httpPost);
//            entity = response.getEntity();
//
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//
//        return entity;
//    }

    public static<R> R doPost(String url, String jsonBody, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", config.getUserAgent())
                .header("client-version", "2.0.0")
                .header("extspam", "Gp8ICJkIEpkICggwMDAwMDAwMRAGGoAI1GiJSIpeO1RZTq9QBKsRbPJdi84ropi16EYI10WB6g74sGmRwSNXjPQnYUKYotKkvLGpshucCaeWZMOylnc6o2AgDX9grhQQx7fm2DJRTyuNhUlwmEoWhjoG3F0ySAWUsEbH3bJMsEBwoB//0qmFJob74ffdaslqL+IrSy7LJ76/G5TkvNC+J0VQkpH1u3iJJs0uUYyLDzdBIQ6Ogd8LDQ3VKnJLm4g/uDLe+G7zzzkOPzCjXL+70naaQ9medzqmh+/SmaQ6uFWLDQLcRln++wBwoEibNpG4uOJvqXy+ql50DjlNchSuqLmeadFoo9/mDT0q3G7o/80P15ostktjb7h9bfNc+nZVSnUEJXbCjTeqS5UYuxn+HTS5nZsPVxJA2O5GdKCYK4x8lTTKShRstqPfbQpplfllx2fwXcSljuYi3YipPyS3GCAqf5A7aYYwJ7AvGqUiR2SsVQ9Nbp8MGHET1GxhifC692APj6SJxZD3i1drSYZPMMsS9rKAJTGz2FEupohtpf2tgXm6c16nDk/cw+C7K7me5j5PLHv55DFCS84b06AytZPdkFZLj7FHOkcFGJXitHkX5cgww7vuf6F3p0yM/W73SoXTx6GX4G6Hg2rYx3O/9VU2Uq8lvURB4qIbD9XQpzmyiFMaytMnqxcZJcoXCtfkTJ6pI7a92JpRUvdSitg967VUDUAQnCXCM/m0snRkR9LtoXAO1FUGpwlp1EfIdCZFPKNnXMeqev0j9W9ZrkEs9ZWcUEexSj5z+dKYQBhIICviYUQHVqBTZSNy22PlUIeDeIs11j7q4t8rD8LPvzAKWVqXE+5lS1JPZkjg4y5hfX1Dod3t96clFfwsvDP6xBSe1NBcoKbkyGxYK0UvPGtKQEE0Se2zAymYDv41klYE9s+rxp8e94/H8XhrL9oGm8KWb2RmYnAE7ry9gd6e8ZuBRIsISlJAE/e8y8xFmP031S6Lnaet6YXPsFpuFsdQs535IjcFd75hh6DNMBYhSfjv456cvhsb99+fRw/KVZLC3yzNSCbLSyo9d9BI45Plma6V8akURQA/qsaAzU0VyTIqZJkPDTzhuCl92vD2AD/QOhx6iwRSVPAxcRFZcWjgc2wCKh+uCYkTVbNQpB9B90YlNmI3fWTuUOUjwOzQRxJZj11NsimjOJ50qQwTTFj6qQvQ1a/I+MkTx5UO+yNHl718JWcR3AXGmv/aa9rD1eNP8ioTGlOZwPgmr2sor2iBpKTOrB83QgZXP+xRYkb4zVC+LoAXEoIa1+zArywlgREer7DLePukkU6wHTkuSaF+ge5Of1bXuU4i938WJHj0t3D8uQxkJvoFi/EYN/7u2P1zGRLV4dHVUsZMGCCtnO6BBigFMAA=")
                .header("referer", "https://wx.qq.com/?&lang=zh_CN&target=t")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<R> response = clientWithRedirect.send(request, bodyHandler);

        return response.body();
    }

//    public static<R> R doPost(String url, String jsonBody,Map<String,String> headers, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {
//        HttpRequest.Builder post = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .timeout(Duration.ofSeconds(30))
//                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
//        for (Entry<String, String> stringStringEntry : headers.entrySet()) {
//            post.header(stringStringEntry.getKey(), stringStringEntry.getValue());
//        }
//
//        HttpResponse<R> response = clientWithRedirect.send(post.build(), bodyHandler);
//
//        return response.body();
//    }

    /**
     * 上传文件到服务器
     *
     * @param url
     * @param
     * @return
     * @author SXS
     * @date 2017年5月7日 下午9:19:23
     */
    public static<R> R doPostFile(String url, MultipartBodyPublisher multipart, HttpResponse.BodyHandler<R> handler) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", config.getUserAgent())
                .header("client-version", "2.0.0")
                .header("extspam", "Gp8ICJkIEpkICggwMDAwMDAwMRAGGoAI1GiJSIpeO1RZTq9QBKsRbPJdi84ropi16EYI10WB6g74sGmRwSNXjPQnYUKYotKkvLGpshucCaeWZMOylnc6o2AgDX9grhQQx7fm2DJRTyuNhUlwmEoWhjoG3F0ySAWUsEbH3bJMsEBwoB//0qmFJob74ffdaslqL+IrSy7LJ76/G5TkvNC+J0VQkpH1u3iJJs0uUYyLDzdBIQ6Ogd8LDQ3VKnJLm4g/uDLe+G7zzzkOPzCjXL+70naaQ9medzqmh+/SmaQ6uFWLDQLcRln++wBwoEibNpG4uOJvqXy+ql50DjlNchSuqLmeadFoo9/mDT0q3G7o/80P15ostktjb7h9bfNc+nZVSnUEJXbCjTeqS5UYuxn+HTS5nZsPVxJA2O5GdKCYK4x8lTTKShRstqPfbQpplfllx2fwXcSljuYi3YipPyS3GCAqf5A7aYYwJ7AvGqUiR2SsVQ9Nbp8MGHET1GxhifC692APj6SJxZD3i1drSYZPMMsS9rKAJTGz2FEupohtpf2tgXm6c16nDk/cw+C7K7me5j5PLHv55DFCS84b06AytZPdkFZLj7FHOkcFGJXitHkX5cgww7vuf6F3p0yM/W73SoXTx6GX4G6Hg2rYx3O/9VU2Uq8lvURB4qIbD9XQpzmyiFMaytMnqxcZJcoXCtfkTJ6pI7a92JpRUvdSitg967VUDUAQnCXCM/m0snRkR9LtoXAO1FUGpwlp1EfIdCZFPKNnXMeqev0j9W9ZrkEs9ZWcUEexSj5z+dKYQBhIICviYUQHVqBTZSNy22PlUIeDeIs11j7q4t8rD8LPvzAKWVqXE+5lS1JPZkjg4y5hfX1Dod3t96clFfwsvDP6xBSe1NBcoKbkyGxYK0UvPGtKQEE0Se2zAymYDv41klYE9s+rxp8e94/H8XhrL9oGm8KWb2RmYnAE7ry9gd6e8ZuBRIsISlJAE/e8y8xFmP031S6Lnaet6YXPsFpuFsdQs535IjcFd75hh6DNMBYhSfjv456cvhsb99+fRw/KVZLC3yzNSCbLSyo9d9BI45Plma6V8akURQA/qsaAzU0VyTIqZJkPDTzhuCl92vD2AD/QOhx6iwRSVPAxcRFZcWjgc2wCKh+uCYkTVbNQpB9B90YlNmI3fWTuUOUjwOzQRxJZj11NsimjOJ50qQwTTFj6qQvQ1a/I+MkTx5UO+yNHl718JWcR3AXGmv/aa9rD1eNP8ioTGlOZwPgmr2sor2iBpKTOrB83QgZXP+xRYkb4zVC+LoAXEoIa1+zArywlgREer7DLePukkU6wHTkuSaF+ge5Of1bXuU4i938WJHj0t3D8uQxkJvoFi/EYN/7u2P1zGRLV4dHVUsZMGCCtnO6BBigFMAA=")
                .header("referer", "https://wx.qq.com/?&lang=zh_CN&target=t")
                .header("Content-Type","multipart/form-data; boundary="+multipart.getBoundary())
                .POST(multipart.build())
                .build();

        HttpResponse<R> response = clientWithRedirect.send(request, handler);
        return response.body();

    }




//    public static HttpResponse<String> doPostFile(String url, HttpEntity reqEntity) throws IOException, InterruptedException {
//        String boundary = "----JavaHttpClientBoundary" + UUID.randomUUID();
//
//        // 读取文件字节
//        byte[] fileBytes = Files.readAllBytes(filePath);
//
//        // 构造 multipart/form-data body
//        StringBuilder sb = new StringBuilder();
//        sb.append("--").append(boundary).append("\r\n");
//        sb.append("Content-Disposition: form-data; name=\"").append(fileFieldName).append("\"; filename=\"")
//                .append(filePath.getFileName()).append("\"\r\n");
//        sb.append("Content-Type: ").append(fileMimeType).append("\r\n\r\n");
//
//        byte[] header = sb.toString().getBytes();
//        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();
//
//        // 拼接完整请求体：header + 文件内容 + footer
//        byte[] body = new byte[header.length + fileBytes.length + footer.length];
//        System.arraycopy(header, 0, body, 0, header.length);
//        System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
//        System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);
//
//        HttpClient client = HttpClient.newHttpClient();
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .timeout(Duration.ofSeconds(60))
//                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
//                .build();
//
//        return client.send(request, HttpResponse.BodyHandlers.ofString());
//    }

    /**
     * 执行 GET 请求
     */
    public static<R> R doGet(String url, Map<String, String> params, Map<String, String> headers, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {
        return doGet(url, params, headers, true,bodyHandler);
    }

    /**
     * 执行 GET 请求（支持重定向控制）
     */
    public static<R> R doGet(String url, Map<String, String> params, Map<String, String> headers, boolean allowRedirect, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {

            String paramStr = (params != null && !params.isEmpty()) ?
                    params.entrySet().stream()
                            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                                    URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                            .collect(Collectors.joining("&")) : "";

            if (!paramStr.isEmpty()) {
                url += url.contains("?") ? "&" + paramStr : "?" + paramStr;
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();

            if (headers != null) {
                headers.forEach(builder::header);
            }

            HttpRequest request = builder.build();
            HttpClient client = allowRedirect ? clientWithRedirect : clientNoRedirect;
            HttpResponse<R> response = client.send(request, bodyHandler);
            return response.body();

    }

    /**
     * 执行 POST 请求（JSON 提交）
     */
    public static<R> R doPost(String url, String jsonBody, Map<String, String> headers, HttpResponse.BodyHandler<R> bodyHandler) {
        return doPost(url, jsonBody, headers, true,bodyHandler);
    }

    /**
     * 执行 POST 请求（支持重定向控制）
     */
    public static<R> R doPost(String url, String jsonBody, Map<String, String> headers, boolean allowRedirect,HttpResponse.BodyHandler<R> bodyHandler) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (headers != null) {
                headers.forEach(builder::header);
            }

            HttpClient client = allowRedirect ? clientWithRedirect : clientNoRedirect;
            HttpResponse<R> send = client.send(builder.build(), bodyHandler);
            return send.body();
        } catch (IOException | InterruptedException e) {
            log.error("POST 请求失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 获取 Cookie 值
     */
    public static String getCookie(String name) {
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .map(c -> c.getValue())
                .findFirst().orElse(null);
    }

}
