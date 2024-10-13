package com.challange.brokeragemanagementapi.controller;

import com.challange.brokeragemanagementapi.model.request.DepositRequest;
import com.challange.brokeragemanagementapi.model.request.WithdrawRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AssetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser")
    public void listAssets_ShouldReturnAssets() throws Exception {
        mockMvc.perform(get("/api/assets/customer/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.assetDtoList", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void depositMoney_ValidInput_ShouldReturnAsset() throws Exception {
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/assets/deposit/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetName", is("TRY")))
                .andExpect(jsonPath("$.size", is(greaterThanOrEqualTo(100.0))))
                .andExpect(jsonPath("$.usableSize", is(greaterThanOrEqualTo(100.0))));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void withdrawMoney_ValidInput_ShouldReturnAsset() throws Exception {
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setAmount(BigDecimal.valueOf(50.0));
        withdrawRequest.setIban("TR12345678901111111");

        mockMvc.perform(post("/api/assets/withdraw/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetName", is("TRY")))
                .andExpect(jsonPath("$.size", is(greaterThanOrEqualTo(50.0))))
                .andExpect(jsonPath("$.usableSize", is(greaterThanOrEqualTo(50.0))));
    }
}