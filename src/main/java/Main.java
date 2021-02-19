import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Solution mySolution = new Solution(3, 13);
        System.out.println(mySolution);
        System.out.println(DistanceCalculator.distance(60.64, 3.72, 60.77, 3.50, "N") + " Nautical Miles\n");

        JSONObject obj = new JSONObject();
        for (int vesselNumber = 0; vesselNumber < mySolution.getOrderSequences().size(); vesselNumber++) {
            JSONArray orderSequence = new JSONArray();
            for (int orderIdx = 0; orderIdx < mySolution.getOrderSequences().get(vesselNumber).size(); orderIdx++) {
                orderSequence.add("" + mySolution.getOrderSequences().get(vesselNumber).get(orderIdx));
                obj.put("" + (vesselNumber + 1), orderSequence);
            }
        }

        try {
            FileWriter file = new FileWriter("data.json");
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(obj.toJSONString());
    }
}
