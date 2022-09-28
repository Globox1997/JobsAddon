package net.jobsaddon.criteria;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.JsonHelper;

public class JobPredicate {
    private final String jobName;

    public JobPredicate(String jobName) {
        this.jobName = jobName;
    }

    public boolean test(String jobName) {
        if (this.jobName.equals(jobName))
            return true;
        else
            return false;
    }

    public static JobPredicate fromJson(JsonElement json) {
        String jobName = JsonHelper.asString(json, "job_name");
        return new JobPredicate(jobName);
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("job_name", this.jobName);
        return jsonObject;
    }

}
