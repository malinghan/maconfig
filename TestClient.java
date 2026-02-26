import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // 测试配置获取
        String configUrl = "http://127.0.0.1:9091/v1/configs?app=demo-app&env=dev&ns=default";
        HttpRequest configRequest = HttpRequest.newBuilder()
                .uri(URI.create(configUrl))
                .GET()
                .build();
        
        HttpResponse<String> configResponse = client.send(configRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Config Response Status: " + configResponse.statusCode());
        System.out.println("Config Response Body: " + configResponse.body());
        
        // 测试版本获取
        String versionUrl = "http://127.0.0.1:9091/v1/version?app=demo-app&env=dev&ns=default";
        HttpRequest versionRequest = HttpRequest.newBuilder()
                .uri(URI.create(versionUrl))
                .GET()
                .build();
        
        HttpResponse<String> versionResponse = client.send(versionRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Version Response Status: " + versionResponse.statusCode());
        System.out.println("Version Response Body: " + versionResponse.body());
    }
}