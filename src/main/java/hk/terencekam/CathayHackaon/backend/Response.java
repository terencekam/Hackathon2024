package hk.terencekam.CathayHackaon.backend;

import org.json.JSONObject;
import org.json.JSONString;

import java.net.http.HttpResponse;

public record Response(int code, JSONObject jsonObject) {
    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", jsonObject=" + jsonObject +
                '}';
    }
}
