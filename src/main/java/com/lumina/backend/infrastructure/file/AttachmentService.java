package com.lumina.backend.infrastructure.file;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.lumina.backend.shared.exception.BusinessException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment saveAttachment(MultipartFile file) throws Exception {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(fileName.contains("..")) {
                throw new BusinessException("Invalid file name: " + fileName);
            }

            if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
                throw new BusinessException("Invalid file format! You can only upload PDF, PNG or JPEG files.. (" + file.getContentType() + ")");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException("The file size is too large! You can upload a maximum of 10MB.");
            }

            Attachment attachment = new Attachment(fileName,
                    file.getContentType(),
                    file.getBytes());

            return attachmentRepository.save(attachment);

        } catch (IOException e) {
            throw new Exception("The file couldn't be saved: " + fileName);
        }
    }

    public Attachment getAttachment(String fileId) throws Exception {
        return attachmentRepository.findById(fileId)
                .orElseThrow(() -> new Exception("The file wasn't found: " + fileId));
    }
}