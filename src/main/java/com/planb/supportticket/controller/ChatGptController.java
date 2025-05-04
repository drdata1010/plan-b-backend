package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ChatGptRequest;
import com.planb.supportticket.dto.ChatGptResponse;
import com.planb.supportticket.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for ChatGPT API operations.
 */
@RestController
@RequestMapping("/chatgpt")
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatGptService chatGptService;

    /**
     * Sends a message to ChatGPT and returns the response.
     *
     * @param request the request containing the message
     * @return the response from ChatGPT
     */
    @PostMapping("/send")
    public ResponseEntity<ChatGptResponse> sendMessage(@RequestBody ChatGptRequest request) {
        String response = chatGptService.sendMessage(request.getMessage());
        return ResponseEntity.ok(new ChatGptResponse(response));
    }
}
