package my.projects.java;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static my.projects.java.MyBot.*;

public class JsonWebParser {
    private static final String JSON_PARSER_ERROR = "JsonParser Error {0}";
    private Logger logger;
    public static final int MAX_MESSAGE_LENGTH = 4096;

    public JsonWebParser() {
    }

    public JsonWebParser(Logger logger) {
        this.logger = logger;
    }

    protected Map<String, Map<String, ArrayList<DistrictsDTO>>> findDrugFromWeb(MyBot bot, String drug) {
        return getDistrictsDTOSFromJson(bot, drug);
    }

    private Map<String, Map<String, ArrayList<DistrictsDTO>>> getDistrictsDTOSFromJson(MyBot bot, String drug) {
        ResponseDTO responseDTO = getResponseFromJson(bot, drug);
        Map<String, Map<String, ArrayList<DistrictsDTO>>> mapMap = new LinkedHashMap<>();

        if (responseDTO == null) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "1: " + RESPONSE_DTO_IS_NULL);
            mapMap.put(RESPONSE_DTO_IS_NULL, new HashMap<>());
            return mapMap;
        }

        if (responseDTO.getResult().isEmpty()) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "2: " + ERROR_IN_FIND_DRUGS);
            mapMap.put(ERROR_IN_FIND_DRUGS, new HashMap<>());
            return mapMap;
        }


        if (!responseDTO.getSuccess()) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "3: " + UNSUCCESSFUL_RESULT);
            mapMap.put(UNSUCCESSFUL_RESULT, new HashMap<>());
            return mapMap;
        }

        JSONObject drugs = responseDTO.getResult();
        Iterator<String> drugsIterator = drugs.keys();
        while (drugsIterator.hasNext()) {
            String nextDrug = drugsIterator.next();
            Map<String, ArrayList<DistrictsDTO>> map = new HashMap<>();
            JSONObject districts = drugs.getJSONObject(nextDrug);
            Iterator<String> districtsIterator = districts.keys();
            while (districtsIterator.hasNext()) {
                String nextDistrict = districtsIterator.next();
                JSONArray stores = districts.getJSONArray(nextDistrict);

                ArrayList<DistrictsDTO> districtsDTOS = new ArrayList<>();
                Field[] fields = DistrictsDTO.class.getDeclaredFields();
                for (Object obj : stores) {
                    JSONObject storeObject = (JSONObject) obj;

                    String addressSplit = storeObject.getString(fields[2].getName());
                    int position = addressSplit.indexOf(" * " + INFO_BEFORE_VISIT);

                    districtsDTOS.add(new DistrictsDTO(
                            storeObject.getString(fields[0].getName()),
                            storeObject.getString(fields[1].getName()),
                            addressSplit.substring(0, position),
                            storeObject.getString(fields[3].getName()),
                            storeObject.getString(fields[4].getName()),
                            storeObject.getString(fields[5].getName()),
                            new Benefit(bot.getBenefits().get(0), storeObject.getInt(fields[6].getName())),
                            new Benefit(bot.getBenefits().get(1), storeObject.getInt(fields[7].getName())),
                            new Benefit(bot.getBenefits().get(2), storeObject.getInt(fields[8].getName())),
                            new Benefit(bot.getBenefits().get(3), storeObject.getInt(fields[9].getName())),
                            storeObject.getInt(fields[10].getName()))
                    );
                }
                map.put(nextDistrict, districtsDTOS);
            }
            mapMap.put(nextDrug, map);
        }

        if (mapMap.isEmpty()) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "4: " + "Map is empty");
        }

        return mapMap;
    }

    private ResponseDTO getResponseFromJson(MyBot bot, String drug) {
        try {
            JSONObject jsonObject = new JSONObject(getDataFromJson(bot, drug));
            if (jsonObject.isEmpty()) {
                logger.log(Level.WARNING, JSON_PARSER_ERROR, "5: " + "JsonObject is empty");
            }

            return new ResponseDTO(
                    jsonObject.getJSONObject("result"),
                    jsonObject.getBoolean("success"));
        } catch (JSONException e) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "6: " + "JsonObject exception");
        }
        return null;
    }


    private String getDataFromJson(MyBot bot, String drug) {
        try {
            String url = bot.getPathToJsonFromWeb() + URLEncoder.encode(drug, StandardCharsets.UTF_8);
            return readUrl(url);
        } catch (IOException e) {
            logger.log(Level.WARNING, JSON_PARSER_ERROR, "7: " + "Connection");
            return "{\"result\": {\"Drugname\":\"Drugname\"},\"success\":false}";
        }
    }

    private String readUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        StringBuilder buffer = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
        }

        return buffer.toString();
    }
}