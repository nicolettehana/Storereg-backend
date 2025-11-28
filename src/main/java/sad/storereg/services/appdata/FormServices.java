package sad.storereg.services.appdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sad.storereg.config.FileStorageProperties;
import sad.storereg.exception.UnauthorizedException;
import sad.storereg.models.appdata.FormPath;
import sad.storereg.repo.appdata.AcceptanceRejectionLettersRepository;
import sad.storereg.repo.appdata.FormPathRepository;

@Service
@RequiredArgsConstructor
public class FormServices {
	
	private final FormPathRepository formPathRepo;
	private final FileStorageProperties fileStoragePorperties;
	private Path fileUploadLocation;
	
	@Value("${orders.dir}")
	private String ordersDir;
	
	@Value("${wl.dir}")
	private String wlDir;
	
	@Value("${applicantLetters.dir}")
	private String applicantLettersDir;
	
	@Value("${vacatedocs.dir}")
	private String vacateDocsDir;
	
	public String uploadFile(MultipartFile file, String applicationNo) {
	    // Step 1: Normalize the base upload directory
	    this.fileUploadLocation = Paths.get(fileStoragePorperties.getUploadDir(applicationNo)).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload certificate.", e);
	    }

	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        File targetFile = targetLocation.toFile();
	        String canonicalDirPath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalDirPath)) {
	            throw new SecurityException("Potential path traversal attempt detected");
	        }

	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return uuid.toString();

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.", ex);
	    }
	}

	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png");
	
	public String getExtensionByStringHandling(String filename) {
	    if (filename == null || !filename.contains(".")) {
	        throw new UnauthorizedException("Filename is invalid or missing extension");
	    }

	    String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

	    if (ext.equals("pdf") || ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) {
	        return "." + ext;
	    } else {
	        throw new UnauthorizedException("Invalid file type: " + ext);
	    }
	}

	private static byte[] getFileContent(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.readAllBytes(path);
	}

	private static String getMimeType(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.probeContentType(path);
	}

	private boolean isValidFileContent(String fileName) throws IOException {
		String fileMimeType;
		try {
			fileMimeType = getMimeType(fileName);
			if (!ALLOWED_MIME_TYPES.contains(fileMimeType)) {
				return false;
			}
		} catch (IOException e) {
			throw e;
		}

		// Check file content using Apache Tika
		try {
			byte[] fileContent = getFileContent(fileName);
			Tika tika = new Tika();
			String detectedMimeType = tika.detect(fileContent);
			return fileMimeType.equals(detectedMimeType);
		} catch (IOException e) {
			throw e;
		}
	}
	
	public String uploadOrder(MultipartFile file, String applicationNo) {

	    this.fileUploadLocation = Paths.get(ordersDir, applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload certificate.", e);
	    }

	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
	    
	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;
	        System.out.println("extension: "+originalFileName);
	        System.out.println("Filename: "+filename);

	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        System.out.println("TargetLocation: "+targetLocation);

	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return targetLocation.toString();

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.");
	    }
	}
	
	public Map<String,String> uploadOrder2(MultipartFile file, String applicationNo) {

	    this.fileUploadLocation = Paths.get(ordersDir, applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload certificate.", e);
	    }

	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }
	        Map<String,String> response = new HashMap<>();
	        response.put("formCode", uuid.toString());
	        response.put("formPath", targetLocation.toString());
	        return response;

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.");
	    }
	}
	
	public FormPath uploadApplicantDecision(MultipartFile file, String applicationNo) {

	    this.fileUploadLocation = Paths.get(applicantLettersDir, applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload letter.", e);
	    }

	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return c;

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.", ex);
	    }
	}

	
	public String uploadDocument(MultipartFile file, String applicationNo) {

	    // Secure and normalize upload directory
	    this.fileUploadLocation = Paths.get(vacateDocsDir, applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload document.", e);
	    }

	    // Clean the original file name
	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        // Resolve and normalize the final file path
	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        // Canonical path check to prevent path traversal
	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        // Save the file
	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        // Save file metadata
	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        // Validate file content
	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return uuid.toString();

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.", ex);
	    }
	}
	
	public String uploadApprovalOrder(MultipartFile file, String applicationNo) {

	    // Normalize and secure the upload directory
	    this.fileUploadLocation = Paths.get(ordersDir, "WL", applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload document.", e);
	    }

	    // Clean and sanitize original filename
	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        // Resolve and normalize final file path
	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        // Prevent path traversal: check canonical path
	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        // Save file
	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        // Save file metadata
	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        // Validate content
	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return uuid.toString();

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.", ex);
	    }
	}

	@Transactional
	public UUID uploadWLAREP(MultipartFile file, String applicationNo) {

	    this.fileUploadLocation = Paths.get(wlDir, applicationNo).toAbsolutePath().normalize();

	    try {
	        Files.createDirectories(this.fileUploadLocation);
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to create directory to upload waiting list order.", e);
	    }

	    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

	    try {
	        UUID uuid = UUID.randomUUID();
	        String extension = getExtensionByStringHandling(originalFileName);
	        String filename = uuid + extension;

	        Path targetLocation = this.fileUploadLocation.resolve(filename).normalize();

	        File targetFile = targetLocation.toFile();
	        String canonicalBasePath = this.fileUploadLocation.toFile().getCanonicalPath();
	        String canonicalTargetPath = targetFile.getCanonicalPath();

	        if (!canonicalTargetPath.startsWith(canonicalBasePath)) {
	            throw new SecurityException("Path traversal attempt detected.");
	        }

	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

	        FormPath c = FormPath.builder()
	                .formCode(uuid)
	                .path(targetLocation.toString())
	                .extension(extension)
	                .build();
	        formPathRepo.save(c);

	        if (!isValidFileContent(targetLocation.toString())) {
	            throw new UnauthorizedException("Invalid file content");
	        }

	        return uuid;

	    } catch (IOException ex) {
	        throw new RuntimeException("Unable to upload form.");
	    }
	}
}
