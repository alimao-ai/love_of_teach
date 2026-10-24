package com.loveofteach.platform.service;

import com.loveofteach.platform.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 将教学资源文件保存到受限的本地目录，并校验扩展名与路径边界。
 * 正式部署可替换为对象存储实现。
 */
@Service
@Slf4j
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "txt", "md", "mp4", "webm", "zip");

    private final Path root;

    public LocalFileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择需要上传的文件");
        }
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "resource" : file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型：" + extension);
        }
        validateContent(file, extension);
        String storageKey = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件路径不合法");
        }
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException("文件保存失败：" + exception.getMessage());
        }
        return new StoredFile(
                storageKey,
                originalFilename,
                file.getContentType(),
                file.getSize());
    }

    public Resource load(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件路径不合法");
        }
        try {
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("资源文件不存在");
            }
            return resource;
        } catch (IOException exception) {
            throw new BusinessException("资源文件读取失败");
        }
    }

    public void delete(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            log.warn("Unable to remove orphaned upload {}", storageKey, exception);
        }
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            throw new BusinessException("文件缺少扩展名");
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects files whose leading bytes do not match the declared extension.
     * This prevents a renamed executable or arbitrary binary from bypassing the
     * extension whitelist.
     */
    private void validateContent(MultipartFile file, String extension) {
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(512);
        } catch (IOException exception) {
            throw new BusinessException("文件读取失败：" + exception.getMessage());
        }
        boolean valid = switch (extension) {
            case "pdf" -> startsWith(header, new byte[]{'%', 'P', 'D', 'F'});
            case "docx", "pptx", "xlsx", "zip" ->
                    startsWith(header, new byte[]{'P', 'K', 3, 4})
                            || startsWith(header, new byte[]{'P', 'K', 5, 6})
                            || startsWith(header, new byte[]{'P', 'K', 7, 8});
            case "doc", "ppt", "xls" ->
                    startsWith(header, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
            case "mp4" -> header.length >= 8
                    && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
            case "webm" ->
                    startsWith(header, new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3});
            case "txt", "md" -> isText(header);
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("文件内容与扩展名不匹配：" + extension);
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isText(byte[] value) {
        if (value.length == 0) {
            return false;
        }
        if (startsWith(value, new byte[]{0, 0, (byte) 0xFE, (byte) 0xFF})) {
            return isUtf32Text(value, 4, false);
        }
        if (startsWith(value, new byte[]{(byte) 0xFF, (byte) 0xFE, 0, 0})) {
            return isUtf32Text(value, 4, true);
        }
        if (startsWith(value, new byte[]{(byte) 0xFE, (byte) 0xFF})) {
            return isUtf16Text(value, 2, false);
        }
        if (startsWith(value, new byte[]{(byte) 0xFF, (byte) 0xFE})) {
            return isUtf16Text(value, 2, true);
        }
        for (byte current : value) {
            int unsigned = Byte.toUnsignedInt(current);
            if (unsigned == 0 || (unsigned < 0x09)
                    || (unsigned > 0x0D && unsigned < 0x20)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUtf16Text(byte[] value, int offset, boolean littleEndian) {
        int limit = value.length - ((value.length - offset) % 2);
        for (int index = offset; index + 1 < limit; index += 2) {
            int low = Byte.toUnsignedInt(value[index]);
            int high = Byte.toUnsignedInt(value[index + 1]);
            char character = (char) (littleEndian
                    ? low | (high << 8)
                    : (low << 8) | high);
            if (!isDisplayableTextCharacter(character)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUtf32Text(byte[] value, int offset, boolean littleEndian) {
        int limit = value.length - ((value.length - offset) % 4);
        for (int index = offset; index + 3 < limit; index += 4) {
            int b0 = Byte.toUnsignedInt(value[index]);
            int b1 = Byte.toUnsignedInt(value[index + 1]);
            int b2 = Byte.toUnsignedInt(value[index + 2]);
            int b3 = Byte.toUnsignedInt(value[index + 3]);
            int codePoint = littleEndian
                    ? b0 | (b1 << 8) | (b2 << 16) | (b3 << 24)
                    : (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
            if (codePoint == 0 || !Character.isValidCodePoint(codePoint)) {
                return false;
            }
            char character = Character.isBmpCodePoint(codePoint)
                    ? (char) codePoint
                    : 'A';
            if (!isDisplayableTextCharacter(character)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDisplayableTextCharacter(char character) {
        int unsigned = character;
        return unsigned != 0
                && (unsigned >= 0x20 || unsigned == '\t'
                || unsigned == '\n' || unsigned == '\r');
    }

    public record StoredFile(
            String storageKey,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
    }
}
