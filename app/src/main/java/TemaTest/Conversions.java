package TemaTest;

import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;

interface Conversions {
    public abstract JSONObject toJSONObject();
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
}
