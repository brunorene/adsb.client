package pt.brene.adsb.api;

import com.eaio.uuid.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    private Gson gson;

    public JsonTransformer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(UUID.class, (JsonSerializer<UUID>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()));
        this.gson = gsonBuilder.create();
    }

    @Override
    public String render(Object model) throws Exception {
        return gson.toJson(model);
    }
}
