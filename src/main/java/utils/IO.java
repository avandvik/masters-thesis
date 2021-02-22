package utils;

import alns.Solution;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class IO {

    public static void WriteToJson(Solution solution) {
        JSONObject obj = new JSONObject();
        for (int vesselNumber = 0; vesselNumber < solution.getOrderSequences().size(); vesselNumber++) {
            JSONArray orderSequence = new JSONArray();
            for (int orderIdx = 0; orderIdx < solution.getOrderSequences().get(vesselNumber).size(); orderIdx++) {
                orderSequence.add("" + solution.getOrderSequences().get(vesselNumber).get(orderIdx));
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
    }
}
