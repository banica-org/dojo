package com.dojo.notifications.model;

import com.hubspot.slack.client.methods.JsonStatus;
import com.hubspot.slack.client.methods.MethodWriteMode;
import com.hubspot.slack.client.methods.RateLimitingTier;
import com.hubspot.slack.client.methods.RateLimitingTiers;
import com.hubspot.slack.client.methods.SlackMethod;

public enum SlackMethodsLookupByEmail implements SlackMethod {
    users_lookupByEmail(MethodWriteMode.READ, RateLimitingTiers.TIER_3, JsonStatus.ACCEPTS_JSON)
    ;

    private final MethodWriteMode writeMode;
    private final RateLimitingTier rateLimitingTier;
    private final JsonStatus jsonStatus;

    SlackMethodsLookupByEmail(
            MethodWriteMode writeMode,
            RateLimitingTier rateLimitingTier,
            JsonStatus jsonStatus
    ) {
        this.writeMode = writeMode;
        this.rateLimitingTier = rateLimitingTier;
        this.jsonStatus = jsonStatus;
    }

    @Override
    public String getMethod() {
        return name().replace('_', '.');
    }

    @Override
    public MethodWriteMode getWriteMode() {
        return writeMode;
    }

    @Override
    public JsonStatus jsonWhitelistStatus() {
        return jsonStatus;
    }

    @Override
    public RateLimitingTier getRateLimitingTier() {
        return rateLimitingTier;
    }
}
