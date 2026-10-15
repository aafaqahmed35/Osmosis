package com.osmosis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmosis.controller.dto.EventPayload;
import com.osmosis.service.BloomFilterService;
import com.osmosis.service.CountMinSketchService;
import com.osmosis.service.HyperLogLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloomFilterService bloomFilterService;

    @MockBean
    private CountMinSketchService countMinSketchService;

    @MockBean
    private HyperLogLogService hyperLogLogService;

    @Test
    @DisplayName("Should process new unique event and return HTTP 200 with metrics")
    void shouldProcessNewEventSuccessfully() throws Exception {
        String streamKey = "user-session-12345";
        EventPayload payload = new EventPayload(streamKey);

        when(bloomFilterService.isDuplicate(streamKey)).thenReturn(false);
        when(countMinSketchService.incrementAndGetFrequency(streamKey)).thenReturn(1L);
        when(hyperLogLogService.addAndGetUniqueCount(streamKey)).thenReturn(100L);

        mockMvc.perform(post("/api/v1/engine/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.duplicate").value(false))
                .andExpect(jsonPath("$.frequency").value(1))
                .andExpect(jsonPath("$.globalUniqueElements").value(100))
                .andExpect(jsonPath("$.totalProcessingTimeMs").isNumber());

        verify(bloomFilterService).isDuplicate(streamKey);
        verify(countMinSketchService).incrementAndGetFrequency(streamKey);
        verify(hyperLogLogService).addAndGetUniqueCount(streamKey);
    }

    @Test
    @DisplayName("Should process duplicate event and return HTTP 200 with duplicate flag true")
    void shouldProcessDuplicateEventSuccessfully() throws Exception {
        String streamKey = "existing-user-999";
        EventPayload payload = new EventPayload(streamKey);

        when(bloomFilterService.isDuplicate(streamKey)).thenReturn(true);
        when(countMinSketchService.incrementAndGetFrequency(streamKey)).thenReturn(7L);
        when(hyperLogLogService.addAndGetUniqueCount(streamKey)).thenReturn(500L);

        mockMvc.perform(post("/api/v1/engine/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.frequency").value(7))
                .andExpect(jsonPath("$.globalUniqueElements").value(500))
                .andExpect(jsonPath("$.totalProcessingTimeMs").isNumber());

        verify(bloomFilterService).isDuplicate(streamKey);
        verify(countMinSketchService).incrementAndGetFrequency(streamKey);
        verify(hyperLogLogService).addAndGetUniqueCount(streamKey);
    }
}
