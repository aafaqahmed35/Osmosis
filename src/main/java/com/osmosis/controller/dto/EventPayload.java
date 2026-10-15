package com.osmosis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload representing an incoming event or stream identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {
    private String streamKey;
}
