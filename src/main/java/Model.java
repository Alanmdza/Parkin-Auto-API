import org.json.JSONObject;

public class Model {
    JSONObject jsonObject = new JSONObject(Main.performHttpGet("http://localhost:8180"));
    JSONObject lugares = jsonObject.getJSONObject("lugares");

    public JSONObject getLugares(){
        return lugares;
    }

    
}
