import org.json.JSONObject;
import org.json.JSONArray;

public class JsonManipulator {
    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nombre", "Ejemplo");
        System.out.println(jsonObject.toString());

        JSONArray jsonArray = new JSONArray();
        jsonArray.put("item1");
        jsonArray.put("item2");
        System.out.println(jsonArray.toString());
    }
}