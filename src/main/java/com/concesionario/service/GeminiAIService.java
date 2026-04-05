package com.concesionario.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GeminiAIService {

    private final ChatModel chatModel;

    public GeminiAIService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String analizarYResponder(String mensajeUsuario) {
        var systemMessage = new SystemMessage("""
            Eres Dante, el asesor experto de 'NextGen Motors'. 
            Tu meta es ser útil, persuasivo y amable. 
            Responde de forma natural a cualquier duda del usuario.
            """);

        var userMessage = new UserMessage(mensajeUsuario);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // ✅ CAMBIO AQUÍ: Usamos .getText() si .getContent() falla
        // Dependiendo de tu versión exacta de Spring AI 1.x, intenta uno de estos dos:
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}