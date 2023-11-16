import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private JSONObject jsonObject = new JSONObject(Main.performHttpGet("http://localhost:8180"));
    private JSONObject lugares = jsonObject.getJSONObject("lugares");
    private List<String> deshabilitados = new ArrayList<>();

    public JSONObject getLugares() {
        return lugares;
    }

    public List<String> getHabilitados() {
        return deshabilitados;
    }
}
