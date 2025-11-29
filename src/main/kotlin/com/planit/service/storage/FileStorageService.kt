package com.planit.service.storage

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class FileStorageService {

    private val uploadDir = Paths.get("uploads")

    init {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }
    }

    fun storeFile(file: MultipartFile): String {
        val filename = "${UUID.randomUUID()}-${file.originalFilename}"
        val targetLocation = uploadDir.resolve(filename)
        Files.copy(file.inputStream, targetLocation)
        return "/uploads/$filename" // Return the path to be saved in the database
    }
}
