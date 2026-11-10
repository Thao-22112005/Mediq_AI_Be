package com.mediqai.ai.service;

import com.mediqai.ai.client.GeminiClient;
import com.mediqai.ai.dto.request.ChatRequest;
import com.mediqai.ai.dto.response.AiAnalysisResponse;
import com.mediqai.ai.dto.response.ChatResponse;
import com.mediqai.ai.dto.response.ConversationResponse;
import com.mediqai.ai.dto.response.MessageResponse;
import com.mediqai.ai.entity.Conversation;
import com.mediqai.ai.entity.Message;
import com.mediqai.ai.repository.ConversationRepository;
import com.mediqai.ai.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiClient geminiClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AiAnalysisParser aiAnalysisParser;

    private String generateConversationTitle(String message) {

        if (message == null || message.isBlank()) {
            return "Cuộc trò chuyện mới";
        }

        String title = message.trim();

        // Xóa xuống dòng
        title = title.replaceAll("\\s+", " ");

        // Giới hạn độ dài
        if (title.length() > 50) {
            title = title.substring(0, 50).trim() + "...";
        }

        return title;
    }


    @Transactional
    public ChatResponse chat(
            String patientEmail,
            ChatRequest request
    ) {

        Conversation conversation;

        // 1. TẠO HOẶC LẤY CUỘC HỘI THOẠI
        if (request.getConversationId() == null) {

            conversation = Conversation.builder()
                    .patientEmail(patientEmail)
                    .title(generateConversationTitle(request.getMessage()))
                    .build();

            conversation = conversationRepository.save(conversation);

        } else {

            conversation = conversationRepository
                    .findById(request.getConversationId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy cuộc hội thoại"
                            )
                    );

            if (!conversation.getPatientEmail()
                    .equalsIgnoreCase(patientEmail)) {

                throw new RuntimeException(
                        "Bạn không có quyền truy cập cuộc hội thoại này"
                );
            }
        }

        // 2. LƯU TIN NHẮN CỦA BỆNH NHÂN
        Message patientMessage = Message.builder()
                .conversationId(conversation.getId())
                .sender(Message.Sender.PATIENT)
                .content(request.getMessage())
                .build();

        messageRepository.save(patientMessage);

        // =========================================================
        // 3. CHỈ LẤY 5 TIN NHẮN GẦN NHẤT ĐỂ GỬI CHO GEMINI
        // =========================================================

        List<Message> messages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        int maxHistoryMessages = 5;

        int startIndex = Math.max(
                0,
                messages.size() - maxHistoryMessages
        );

        List<Message> recentMessages =
                messages.subList(
                        startIndex,
                        messages.size()
                );

        String conversationHistory =
                recentMessages.stream()
                        .map(message ->
                                message.getSender()
                                        + ": "
                                        + message.getContent()
                        )
                        .collect(Collectors.joining("\n"));

        // =========================================================
        // 4. PROMPT NGẮN GỌN CHO GEMINI
        // =========================================================

        String prompt = """
            Bạn là trợ lý AI tư vấn sức khỏe của hệ thống MEDIQ-AI.

            VAI TRÒ:
            - Hỗ trợ bệnh nhân thu thập và đánh giá sơ bộ triệu chứng.
            - Không chẩn đoán chắc chắn.
            - Không kê đơn thuốc.
            - Nếu có dấu hiệu nguy hiểm, ưu tiên hướng dẫn đi cấp cứu.

            QUY TẮC HỘI THOẠI:
            - Nếu thiếu thông tin, hỏi tối đa 1-2 câu quan trọng nhất.
            - Không hỏi lại thông tin bệnh nhân đã cung cấp.
            - Ưu tiên: vị trí triệu chứng, thời điểm bắt đầu, mức độ 0-10,
              diễn biến và triệu chứng đi kèm.
            - Ưu tiên kiểm tra dấu hiệu nguy hiểm trước nếu triệu chứng có khả năng cấp tính.
            - Cố gắng hoàn thành đánh giá trong khoảng 3-5 lượt hỏi.
            - Khi đủ thông tin thì không hỏi thêm.

            STATUS:
            - NEED_MORE_INFO: chưa đủ thông tin, cần hỏi thêm.
            - READY: đủ thông tin để đánh giá sơ bộ.
            - EMERGENCY: có dấu hiệu có thể cần cấp cứu.

            TRIAGE:
            - LOW: nhẹ, không có dấu hiệu cảnh báo.
            - MEDIUM: cần bác sĩ đánh giá nhưng chưa cấp cứu.
            - HIGH: đáng lo ngại, nên đi khám sớm.
            - EMERGENCY: có dấu hiệu đe dọa tính mạng hoặc cần cấp cứu.

            SPECIALTY - chỉ được dùng một mã:
            CAP_CUU
            NOI_TONG_QUAT
            TIM_MACH
            HO_HAP
            TIEU_HOA
            THAN_KINH
            TAI_MUI_HONG
            MAT
            DA_LIEU
            CO_XUONG_KHOP
            NOI_TIET
            TIET_NIEU
            SAN_PHU_KHOA
            RANG_HAM_MAT
            KHAC
            CHUA_XAC_DINH

            QUY TẮC SPECIALTY:
            - EMERGENCY -> CAP_CUU.
            - Đủ thông tin -> chọn chuyên khoa phù hợp.
            - Chưa đủ thông tin -> CHUA_XAC_DINH.
            - Không phù hợp chuyên khoa nào -> KHAC.
            - Chỉ trả về mã enum, không trả tên tiếng Việt.

            POSSIBLE CONDITIONS:
            - NEED_MORE_INFO -> [].
            - READY -> 1-3 bệnh/tình trạng có thể liên quan.
            - EMERGENCY -> có thể [] nếu chưa đủ cơ sở.
            - Không khẳng định bệnh nhân mắc bệnh.
            - Dùng cách diễn đạt như "có thể liên quan", "khả năng", "đánh giá sơ bộ".

            QUY TẮC AN TOÀN:
            - Đây chỉ là đánh giá sơ bộ, không thay thế bác sĩ.
            - Không chẩn đoán chắc chắn.
            - Không kê đơn thuốc.
            - Nếu có dấu hiệu cấp cứu, ưu tiên cảnh báo và hướng dẫn đến cơ sở y tế gần nhất.

            OUTPUT:
            Chỉ trả về JSON hợp lệ.
            Không markdown.
            Không ```json.
            Không thêm nội dung ngoài JSON.

            {
              "status": "NEED_MORE_INFO | READY | EMERGENCY",
              "reply": "Câu trả lời dành cho bệnh nhân",
              "question": "Câu hỏi tiếp theo hoặc chuỗi rỗng",
              "symptoms": ["triệu chứng 1", "triệu chứng 2"],
              "triageLevel": "LOW | MEDIUM | HIGH | EMERGENCY",
              "specialty": "CAP_CUU | NOI_TONG_QUAT | TIM_MACH | HO_HAP | TIEU_HOA | THAN_KINH | TAI_MUI_HONG | MAT | DA_LIEU | CO_XUONG_KHOP | NOI_TIET | TIET_NIEU | SAN_PHU_KHOA | RANG_HAM_MAT | KHAC | CHUA_XAC_DINH",
              "summary": "Tóm tắt nếu đủ thông tin, nếu chưa đủ để chuỗi rỗng",
              "possibleConditions": ["khả năng 1", "khả năng 2"]
            }

            LỊCH SỬ GẦN NHẤT:
            %s

            Hãy dựa vào lịch sử trên và tin nhắn mới nhất để đưa ra phản hồi.
            """.formatted(conversationHistory);

        // =========================================================
        // 5. GỌI GEMINI
        // =========================================================

        String aiJson =
                geminiClient.generateContent(prompt);

        // =========================================================
        // 6. PARSE KẾT QUẢ AI
        // =========================================================

        AiAnalysisResponse analysis =
                aiAnalysisParser.parse(aiJson);

        String aiResponse =
                analysis.getReply();

        // =========================================================
        // 7. LƯU CÂU TRẢ LỜI CỦA AI
        // =========================================================

        Message aiMessage = Message.builder()
                .conversationId(conversation.getId())
                .sender(Message.Sender.AI)
                .content(aiResponse)
                .build();

        messageRepository.save(aiMessage);

        // =========================================================
        // 8. TRẢ RESPONSE
        // =========================================================

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .message(aiResponse)
                .analysis(analysis)
                .build();
    }


    public List<ConversationResponse> getConversations(String patientEmail) {

        return conversationRepository
                .findByPatientEmailOrderByUpdatedAtDesc(patientEmail)
                .stream()
                .map(conversation -> ConversationResponse.builder()
                        .id(conversation.getId())
                        .title(conversation.getTitle())
                        .createdAt(conversation.getCreatedAt())
                        .updatedAt(conversation.getUpdatedAt())
                        .build())
                .toList();
    }


    public List<MessageResponse> getMessages(
            String patientEmail,
            Long conversationId
    ) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy cuộc hội thoại"
                        )
                );

        // Chỉ cho phép chủ sở hữu conversation xem lịch sử
        if (!conversation.getPatientEmail()
                .equalsIgnoreCase(patientEmail)) {

            throw new RuntimeException(
                    "Bạn không có quyền truy cập cuộc hội thoại này"
            );
        }

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(message -> MessageResponse.builder()
                        .id(message.getId())
                        .sender(message.getSender())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteConversation(
            String patientEmail,
            Long conversationId
    ) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy cuộc hội thoại"
                        )
                );

        // Kiểm tra conversation có thuộc bệnh nhân đang đăng nhập không
        if (!conversation.getPatientEmail()
                .equalsIgnoreCase(patientEmail)) {

            throw new RuntimeException(
                    "Bạn không có quyền xóa cuộc hội thoại này"
            );
        }

        // Xóa toàn bộ message trước
        messageRepository.deleteAll(
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversationId
                        )
        );

        // Sau đó xóa conversation
        conversationRepository.delete(conversation);
    }
}