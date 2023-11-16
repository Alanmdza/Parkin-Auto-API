import org.json.JSONObject;

public class ModelDAO {
    private Model model = new Model();
    private static ModelDAO instance = null;

    private ModelDAO() {
        // Constructor privado
    }
    public static ModelDAO getInstance() {
        if (instance == null) {
            instance = new ModelDAO();
        }
        return instance;
    }

    public JSONObject getLugares(){
        return model.getLugares();
    }
}
