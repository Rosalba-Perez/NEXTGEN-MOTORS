package com.concesionario.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ISupabaseStorageService {
    String uploadFile(MultipartFile file) throws IOException;
}
